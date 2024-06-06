package com.example.demo.demos.handler;

import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.StatementWrapper;
import com.example.demo.demos.dbswitch.common.entity.CloseableDataSource;
import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.dbswitch.common.util.ExamineUtils;
import com.example.demo.demos.dbswitch.core.exchange.MemChannel;
import com.example.demo.demos.dbswitch.data.config.DbswichPropertiesConfiguration;
import com.example.demo.demos.dbswitch.data.domain.ReaderTaskParam;
import com.example.demo.demos.dbswitch.data.entity.SourceDataSourceProperties;
import com.example.demo.demos.dbswitch.data.entity.TargetDataSourceProperties;
import com.example.demo.demos.dbswitch.data.util.JsonUtils;
import com.example.demo.demos.dbswitch.provider.ProductFactoryProvider;
import com.example.demo.demos.dbswitch.provider.ProductProviderFactory;
import com.example.demo.demos.dbswitch.provider.manage.TableManageProvider;
import com.example.demo.demos.dbswitch.provider.meta.MetadataProvider;
import com.example.demo.demos.dbswitch.provider.query.TableDataQueryProvider;
import com.example.demo.demos.dbswitch.provider.sync.TableDataSynchronizeProvider;
import com.example.demo.demos.dbswitch.provider.transform.RecordTransformProvider;
import com.example.demo.demos.dbswitch.provider.write.TableDataWriteProvider;
import com.example.demo.demos.dbswitch.schema.ColumnDescription;
import com.example.demo.demos.dbswitch.schema.TableDescription;
import com.example.demo.demos.service.DefaultMetadataService;
import com.example.demo.demos.service.MetadataService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Data
public class BackupHandler {
    private TableDataQueryProvider sourceQuerier;
    private RecordTransformProvider transformProvider;
    private TableDataWriteProvider targetWriter;
    private TableManageProvider targetTableManager;
    private TableDataSynchronizeProvider targetSynchronizer;

    private final CloseableDataSource sourceDataSource;

    private final CloseableDataSource targetDataSource;

    private String sourceSchemaName;
    private String sourceTableName;
    private ProductTypeEnum sourceProductType;
    private ProductTypeEnum targetProductType;

    private MetadataService sourceMetaDataService;
    private List<ColumnDescription> sourceColumnDescriptions;

    private List<ColumnDescription> targetColumnDescriptions;

    private List<String> sourcePrimaryKeys;
    private List<String> targetPrimaryKeys;


    private String sourceTableRemarks;


    private static final String QUERY_TABLE_METADATA_SQL =
            "SELECT `TABLE_COMMENT`,`TABLE_TYPE` FROM `information_schema`.`TABLES` "
                    + "WHERE `TABLE_SCHEMA` = ? AND `TABLE_NAME` = ?";

    public BackupHandler(CloseableDataSource sourceDataSource,
                         String sourceSchemaName,
                         ProductTypeEnum sourceProductType
                         ) {

        this.sourceDataSource = sourceDataSource;
        this.targetDataSource = sourceDataSource;
        this.sourceSchemaName = sourceSchemaName;
        this.sourceProductType = sourceProductType;
        this.targetProductType = sourceProductType;
    }

    public void backUp(){
        this.sourceMetaDataService = new DefaultMetadataService(sourceDataSource, sourceProductType);

        List<String> schemas = this.sourceMetaDataService.querySchemaList();

        List<TableDescription> tableDescriptions = sourceMetaDataService.queryTableList(this.sourceSchemaName);
        tableDescriptions.forEach(tableDescription -> {
            sourceTableName =  tableDescription.getTableName();

            // 获取 备注 列 主键
            this.sourceTableRemarks = sourceMetaDataService.getTableRemark(sourceSchemaName, sourceTableName);
            this.sourceColumnDescriptions = sourceMetaDataService.queryTableColumnMeta(sourceSchemaName, sourceTableName);
            this.sourcePrimaryKeys = sourceMetaDataService.queryTablePrimaryKeys(sourceSchemaName,sourceTableName);
            // 通过列 获取值
            this.targetPrimaryKeys = this.sourcePrimaryKeys;
            this.targetColumnDescriptions = this.sourceColumnDescriptions;

            ProductFactoryProvider sourceFactoryProvider = ProductProviderFactory
                    .newProvider(sourceProductType, sourceDataSource);
            ProductFactoryProvider targetFactoryProvider = ProductProviderFactory
                    .newProvider(targetProductType, targetDataSource);
            sourceQuerier = sourceFactoryProvider.createTableDataQueryProvider();
            transformProvider = sourceFactoryProvider.createRecordTransformProvider();
            targetWriter = targetFactoryProvider.createTableDataWriteProvider(
                    false);
            MetadataProvider targetMetaProvider = targetFactoryProvider.createMetadataQueryProvider();
            targetTableManager = targetFactoryProvider.createTableManageProvider();
            targetSynchronizer = targetFactoryProvider.createTableDataSynchronizeProvider();

            LocalTime now = LocalTime.now();
            int hour = now.getHour();
            int minute = now.getMinute();
            int second = now.getSecond();
            String  detailTableName = sourceTableName + "_" + hour + "_" + minute + "_" + second;
            List<String> sqlCreateTable = sourceMetaDataService.getDDLCreateTableSQL(
                    targetMetaProvider,
                    targetColumnDescriptions.stream()
                            .filter(column -> StringUtils.hasLength(column.getFieldName()))
                            .collect(Collectors.toList()),
                    targetPrimaryKeys,
                    sourceSchemaName,
                    detailTableName,
                    sourceTableRemarks,
                    false,
//                properties.getTarget().getCreateTableAutoIncrement(),
                    getTblProperties()
            );
            JdbcTemplate targetJdbcTemplate = new JdbcTemplate(targetDataSource);
            for (String sql : sqlCreateTable) {
                log.info("Execute SQL: \n{}", sql);
                targetJdbcTemplate.execute(sql);
            }
            List<Map<String, Object>> maps = targetJdbcTemplate.queryForList("SELECT * FROM " + sourceTableName);
            StringBuilder InsertSQL = new StringBuilder();
            InsertSQL.append("INSERT INTO " + detailTableName + "(" );
            for (int i = 0; i < targetColumnDescriptions.size(); i++){
                InsertSQL.append(targetColumnDescriptions.get(i).getFieldName());
                InsertSQL.append(i==targetColumnDescriptions.size()-1?") ":",");
            }
            InsertSQL.append("VALUES");

            for (int i = 0; i < maps.size(); i++){
                InsertSQL.append("(");
                Map<String, Object> stringObjectMap = maps.get(i);
                int count = 0;
                for (Map.Entry<String,Object> entry : stringObjectMap.entrySet()){
                    if (entry.getValue() instanceof String){
                        InsertSQL.append("'" + entry.getValue() + "'" );
                    }else {
                        InsertSQL.append(entry.getValue() );
                    }

                    if(count!=targetColumnDescriptions.size()-1){
                        InsertSQL.append(",");
                    }
                    count++;
                }

                InsertSQL.append(")");
                InsertSQL.append(i==maps.size()-1?";":",");
            }
             log.info("Execute SQL: \n{}", InsertSQL);
            targetJdbcTemplate.execute(InsertSQL.toString());


        });
    }
    public List<String> getAllSchemas(){
        this.sourceMetaDataService = new DefaultMetadataService(sourceDataSource, sourceProductType);

        List<String> schemas = this.sourceMetaDataService.querySchemaList();
        return schemas;
    }
    public Map<String, String> getTblProperties() {
        Map<String, String> ret = new HashMap<>();
        if (targetProductType.isLikeHive()) {
            // hive.sql.database.type: MYSQL, POSTGRES, ORACLE, DERBY, DB2
            final List<ProductTypeEnum> supportedProductTypes =
                    Arrays.asList(ProductTypeEnum.MYSQL, ProductTypeEnum.ORACLE,
                            ProductTypeEnum.DB2, ProductTypeEnum.POSTGRESQL);
            ExamineUtils.check(supportedProductTypes.contains(sourceProductType),
                    "Unsupported data from %s to Hive", sourceProductType.name());

            String fullTableName = sourceProductType.quoteSchemaTableName(sourceSchemaName, sourceTableName);
            List<String> columnNames = sourceColumnDescriptions.stream().map(ColumnDescription::getFieldName)
                    .collect(Collectors.toList());
            String querySql = String.format("SELECT %s FROM %s",
                    columnNames.stream()
                            .map(s -> sourceProductType.quoteName(s))
                            .collect(Collectors.joining(",")),
                    fullTableName);
            String databaseType = sourceProductType.name().toUpperCase();
            if (ProductTypeEnum.POSTGRESQL == sourceProductType) {
                databaseType = "POSTGRES";
            } else if (ProductTypeEnum.SQLSERVER == sourceProductType) {
                databaseType = "MSSQL";
            }
            ret.put("hive.sql.database.type", databaseType);
            ret.put("hive.sql.jdbc.driver", sourceDataSource.getDriverClass());
            ret.put("hive.sql.jdbc.url", sourceDataSource.getJdbcUrl());
            ret.put("hive.sql.dbcp.username", sourceDataSource.getUserName());
            ret.put("hive.sql.dbcp.password", sourceDataSource.getPassword());
            ret.put("hive.sql.query", querySql);
            ret.put("hive.sql.jdbc.read-write", "read");
            ret.put("hive.sql.jdbc.fetch.size", "2000");
            ret.put("hive.sql.dbcp.maxActive", "1");
        }
        return ret;
    }

}
