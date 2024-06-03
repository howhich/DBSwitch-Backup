package com.example.demo.demos.service;


import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.example.demo.demos.dbswitch.common.entity.CloseableDataSource;
//import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
//import com.example.demo.demos.dbswitch.common.util.JdbcUrlUtils;
//import com.example.demo.demos.dbswitch.data.util.DataSourceUtils;
//import com.example.demo.demos.dbswitch.service.DefaultMetadataService;
//import com.example.demo.demos.dbswitch.service.MetadataService;
import com.example.demo.demos.Exception.DbswitchException;
import com.example.demo.demos.dbswitch.common.entity.CloseableDataSource;
import com.example.demo.demos.dbswitch.common.entity.DbConnectionCreateRequest;
import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.dbswitch.common.util.DSTestUtils;
import com.example.demo.demos.dbswitch.common.util.DSUtils;
import com.example.demo.demos.dbswitch.common.util.JdbcUrlUtils;
import com.example.demo.demos.dbswitch.core.exchange.MemChannel;
import com.example.demo.demos.dbswitch.core.task.TaskParam;
import com.example.demo.demos.dbswitch.data.config.DbswichPropertiesConfiguration;
import com.example.demo.demos.dbswitch.data.domain.ReaderTaskParam;
import com.example.demo.demos.dbswitch.data.entity.SourceDataSourceProperties;
import com.example.demo.demos.dbswitch.data.entity.TargetDataSourceProperties;
import com.example.demo.demos.dbswitch.data.util.JsonUtils;
import com.example.demo.demos.dbswitch.schema.TableDescription;
import com.example.demo.demos.entity.DatabaseConnectionEntity;
import com.example.demo.demos.handler.BackupHandler;
import com.example.demo.demos.handler.ReaderTaskThread;
import com.example.demo.demos.handler.WriteHandler;
import com.example.demo.demos.mapper.DbConnectionMapper;
import com.example.demo.demos.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//import static com.example.demo.demos.dbswitch.common.util.DSUtils.createURLClassLoader;


@Service
@Slf4j
public class DbConnectionServiceImpl extends ServiceImpl<DbConnectionMapper, DatabaseConnectionEntity> implements DbConnectionService  {
    @Resource
    private DbConnectionMapper dbConnectionMapper;
    @Resource
    private DriverLoadService driverLoadService;

    @Override
    public String test(Long id) throws SQLException {
        DatabaseConnectionEntity databaseConnectionEntity = dbConnectionMapper.selectById(id);
        if (Objects.isNull(databaseConnectionEntity)) {
            return "连接不存在";
        }
        File driverVersionFile = driverLoadService.getVersionDriverFile(databaseConnectionEntity.getType(), databaseConnectionEntity.getVersion());
        String driverPath = driverVersionFile.getAbsolutePath();
        Boolean b = DSTestUtils.MysqlTest(databaseConnectionEntity, driverPath);
//        DataSource dataSource = createCommonDataSource(databaseConnectionEntity.getUrl(),
//                databaseConnectionEntity.getDriver(),
//                driverPath,
//                databaseConnectionEntity.getUsername(),
//                databaseConnectionEntity.getPassword());
//        try {
//            Connection connection = dataSource.getConnection();
//            Boolean resultSet = connection.createStatement().execute("select 1");
//            System.out.println(resultSet);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
        return "result:" + b;
    }

    @Override
    public List<DatabaseConnectionEntity> getAllConnections() {
        List<DatabaseConnectionEntity> databaseConnectionEntities = dbConnectionMapper.
                selectList(new LambdaQueryWrapper<DatabaseConnectionEntity>());
        return databaseConnectionEntities;
    }

    @Override
    public String preTest(DbConnectionCreateRequest request) {
        File driverVersionFile = driverLoadService.getVersionDriverFile(request.getType(), request.getVersion());
        String driverPath = driverVersionFile.getAbsolutePath();

        DatabaseConnectionEntity dbConn = new DatabaseConnectionEntity();
        dbConn = request.toDatabaseConnection();
        Boolean b = DSTestUtils.MysqlTest(dbConn, driverPath);

        return "result" + b;
    }

    @Override
    public String addDataSource(DatabaseConnectionEntity databaseConnectionEntity) {
        if (StringUtils.isBlank(databaseConnectionEntity.getName())) {
            return "name is empty";
        }
        if (Objects.nonNull(dbConnectionMapper.selectOne(
                new LambdaQueryWrapper<DatabaseConnectionEntity>().
                        eq(DatabaseConnectionEntity::getName, databaseConnectionEntity.getName())))) {
            System.out.println("EXISTS!!!");
            return "name exists" + databaseConnectionEntity.getName() ;
        }

        DatabaseConnectionEntity connection = new DatabaseConnectionEntity();
        BeanUtils.copyProperties(databaseConnectionEntity, connection);
        validJdbcUrlFormat(connection);
        dbConnectionMapper.insert(connection);
        return "success";
    }

    @Override
    public void backUp(Long id) {
        DatabaseConnectionEntity entity = dbConnectionMapper.selectById(id);
        String path = driverLoadService.getVersionDriverFile(entity.getType(), entity.getVersion()).getAbsolutePath();
        CloseableDataSource commonDataSource = DSUtils.createCommonDataSource(entity.getUrl()
                , entity.getDriver(), path, entity.getUsername(), entity.getPassword());
        BackupHandler backupHandler = new BackupHandler(commonDataSource,
                entity.getDatabaseName(), "user", entity.getType());
        backupHandler.backUp();
//        ReaderTaskThread readerTaskThread = new ReaderTaskThread();
    }




    private void validJdbcUrlFormat(DatabaseConnectionEntity conn) {
        String typeName = conn.getType().getName().toUpperCase();
        ProductTypeEnum supportDbType = ProductTypeEnum.valueOf(typeName);
        if (!conn.getUrl().startsWith(supportDbType.getUrlPrefix())) {
            throw new DbswitchException(ResultCode.ERROR_INVALID_JDBC_URL, conn.getUrl());
        }

        for (int i = 0; i < supportDbType.getUrl().length; ++i) {
            String pattern = supportDbType.getUrl()[i];
            Matcher matcher = JdbcUrlUtils.getPattern(pattern).matcher(conn.getUrl());
            if (!matcher.matches()) {
                if (i == supportDbType.getUrl().length - 1) {
                    throw new DbswitchException(ResultCode.ERROR_INVALID_JDBC_URL, conn.getUrl());
                }
            } else {
                if (supportDbType.hasDatabaseName() && StringUtils.isBlank(matcher.group("database"))) {
                    throw new DbswitchException(ResultCode.ERROR_INVALID_JDBC_URL,
                            "库名没有指定 :" + conn.getUrl());
                }
                if (supportDbType.hasFilePath() && StringUtils.isBlank(matcher.group("file"))) {
                    throw new DbswitchException(ResultCode.ERROR_INVALID_JDBC_URL,
                            "文件路径没有指定 :" + conn.getUrl());
                }

                break;
            }
        }
    }
    private List<TableDescription> splitReaderTask(DbswichPropertiesConfiguration configuration,CloseableDataSource sourceDataSource) {
        List<TableDescription> tableDescriptions = new ArrayList<>();

        MetadataService sourceMetaDataService = new DefaultMetadataService(sourceDataSource);

        // 判断处理的策略：是排除还是包含
        SourceDataSourceProperties sourceProperties = configuration.getSource();
        List<String> includes =
                StreamUtil.of(StrUtil.split(sourceProperties.getSourceIncludes(), StrPool.COMMA))
                        .collect(Collectors.toList());
        log.info("Includes tables is :{}", JsonUtils.toJsonString(includes));
        List<String> filters =
                StreamUtil.of(StrUtil.split(sourceProperties.getSourceExcludes(), StrPool.COMMA))
                        .collect(Collectors.toList());
        log.info("Filter tables is :{}", JsonUtils.toJsonString(filters));

        boolean useExcludeTables = includes.isEmpty();
        if (useExcludeTables) {
            log.info("!!!! Use dbswitch.source.source-excludes parameter to filter tables");
        } else {
            log.info("!!!! Use dbswitch.source.source-includes parameter to filter tables");
        }

        List<String> schemas =
                StreamUtil.of(StrUtil.split(sourceProperties.getSourceSchema(), StrPool.COMMA))
                        .collect(Collectors.toList());
        log.info("Source schema names is :{}", JsonUtils.toJsonString(schemas));

        for (String schema : schemas) {
            List<TableDescription> tableList = sourceMetaDataService.queryTableList(schema);
            if (tableList.isEmpty()) {
                log.warn("### Find source database table list empty for schema name is : {}", schema);
            } else {
                String allTableType = sourceProperties.getTableType();
                for (TableDescription td : tableList) {
                    // 当没有配置迁移的表名时，默认为根据类型同步所有
                    if (includes.isEmpty()) {
                        if (null != allTableType && !allTableType.equals(td.getTableType())) {
                            continue;
                        }
                    }

                    String tableName = td.getTableName();
                    if (useExcludeTables) {
                        if (!filters.contains(tableName)) {
                            tableDescriptions.add(td);
                        }
                    } else {
                        if (includes.size() == 1 && (includes.get(0).contains("*") || includes.get(0).contains("?"))) {
                            if (Pattern.matches(includes.get(0), tableName)) {
                                tableDescriptions.add(td);
                            }
                        } else if (includes.contains(tableName)) {
                            tableDescriptions.add(td);
                        }
                    }
                }
            }
        }
        return tableDescriptions;
    }
}
