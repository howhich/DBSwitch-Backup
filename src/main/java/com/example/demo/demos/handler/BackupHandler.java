package com.example.demo.demos.handler;

import com.example.demo.demos.dbswitch.common.entity.CloseableDataSource;
import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.dbswitch.common.util.ExamineUtils;
import com.example.demo.demos.dbswitch.data.config.DbswichPropertiesConfiguration;
import com.example.demo.demos.dbswitch.provider.ProductFactoryProvider;
import com.example.demo.demos.dbswitch.provider.ProductProviderFactory;
import com.example.demo.demos.dbswitch.provider.manage.TableManageProvider;
import com.example.demo.demos.dbswitch.provider.meta.MetadataProvider;
import com.example.demo.demos.dbswitch.provider.query.TableDataQueryProvider;
import com.example.demo.demos.dbswitch.provider.sync.TableDataSynchronizeProvider;
import com.example.demo.demos.dbswitch.provider.transform.RecordTransformProvider;
import com.example.demo.demos.dbswitch.provider.write.TableDataWriteProvider;
import com.example.demo.demos.dbswitch.schema.ColumnDescription;
import com.example.demo.demos.service.DefaultMetadataService;
import com.example.demo.demos.service.MetadataService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
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
                         String sourceTableName,
                         ProductTypeEnum sourceProductType
                         ) {

        this.sourceDataSource = sourceDataSource;
        this.targetDataSource = sourceDataSource;
        this.sourceSchemaName = sourceSchemaName;
        this.sourceTableName = sourceTableName;
        this.sourceProductType = sourceProductType;
        this.targetProductType = sourceProductType;
    }
    public void backUp(){
        this.sourceMetaDataService = new DefaultMetadataService(sourceDataSource, sourceProductType);

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

        List<String> sqlCreateTable = sourceMetaDataService.getDDLCreateTableSQL(
                targetMetaProvider,
                targetColumnDescriptions.stream()
                        .filter(column -> StringUtils.hasLength(column.getFieldName()))
                        .collect(Collectors.toList()),
                targetPrimaryKeys,
                sourceSchemaName,
                sourceTableName+"1",
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
