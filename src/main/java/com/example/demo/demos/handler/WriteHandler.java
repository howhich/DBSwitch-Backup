package com.example.demo.demos.handler;

import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.example.demo.demos.dbswitch.common.entity.CloseableDataSource;
import com.example.demo.demos.dbswitch.common.entity.ResultSetWrapper;
import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.dbswitch.core.exchange.BatchElement;
import com.example.demo.demos.dbswitch.core.exchange.MemChannel;
import com.example.demo.demos.dbswitch.core.task.TaskProcessor;
import com.example.demo.demos.dbswitch.data.config.DbswichPropertiesConfiguration;
import com.example.demo.demos.dbswitch.data.domain.ReaderTaskParam;
import com.example.demo.demos.dbswitch.data.domain.ReaderTaskResult;
import com.example.demo.demos.dbswitch.data.entity.SourceDataSourceProperties;
import com.example.demo.demos.dbswitch.data.entity.TargetDataSourceProperties;
import com.example.demo.demos.dbswitch.data.util.JsonUtils;
import com.example.demo.demos.dbswitch.provider.manage.TableManageProvider;
import com.example.demo.demos.dbswitch.provider.query.TableDataQueryProvider;
import com.example.demo.demos.dbswitch.provider.transform.RecordTransformProvider;
import com.example.demo.demos.dbswitch.provider.write.TableDataWriteProvider;
import com.example.demo.demos.dbswitch.schema.ColumnDescription;
import com.example.demo.demos.dbswitch.schema.TableDescription;
import com.example.demo.demos.service.DefaultMetadataService;
import com.example.demo.demos.service.MetadataService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import com.example.demo.demos.dbswitch.common.util.JdbcTypesUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class WriteHandler extends TaskProcessor<ReaderTaskResult> {
    private final CloseableDataSource sourceDataSource;

    private final CloseableDataSource targetDataSource;

    private String sourceSchemaName;
    private String sourceTableName;
    private ProductTypeEnum sourceProductType;
    private ProductTypeEnum targetProductType;
    AtomicLong totalBytes = new AtomicLong(0);


    private MetadataService sourceMetaDataService;
    private List<ColumnDescription> sourceColumnDescriptions;

    private List<ColumnDescription> targetColumnDescriptions;

    private List<String> sourcePrimaryKeys;
    private List<String> targetPrimaryKeys;
    private String sourceTableRemarks;

    private MemChannel memChannel;
    private TableDescription tableDescription;
    private final DbswichPropertiesConfiguration properties;
    private final SourceDataSourceProperties sourceProperties;
    private final TargetDataSourceProperties targetProperties;

    private Set<String> targetExistTables;
    private CountDownLatch robotCountDownLatch;
    private String tableNameMapString;


    AtomicLong totalCount = new AtomicLong(0);


    public WriteHandler(ReaderTaskParam taskParam
                        ) {
        this.sourceDataSource = taskParam.getSourceDataSource();
        this.targetDataSource = taskParam.getTargetDataSource();
        this.tableDescription = taskParam.getTableDescription();
        this.memChannel = taskParam.getMemChannel();
        this.properties = null;
        this.sourceProperties = this.properties.getSource();
        this.targetProperties = this.properties.getTarget();
        this.sourceSchemaName = this.sourceProperties.getSourceSchema();
        this.sourceTableName = this.tableDescription.getTableName();
        this.targetExistTables = taskParam.getTargetExistTables();
        this.robotCountDownLatch = taskParam.getCountDownLatch();
    }
    public void doFullCoverSynchronize(TableDataWriteProvider tableWriter, TableManageProvider tableManager,
                                        TableDataQueryProvider sourceQuerier, RecordTransformProvider transformer) {
        final int BATCH_SIZE = 5000;

        final long MAX_CACHE_BYTES_SIZE = 128 * 1024 * 1024;

        List<String> sourceFields = new ArrayList<>();
        List<String> targetFields = new ArrayList<>();
        for (int i = 0; i < targetColumnDescriptions.size(); ++i) {
            ColumnDescription scd = sourceColumnDescriptions.get(i);
            ColumnDescription tcd = targetColumnDescriptions.get(i);
            if (!StringUtils.isEmpty(tcd.getFieldName())) {
                sourceFields.add(scd.getFieldName());
                targetFields.add(tcd.getFieldName());
            }
        }
        // 准备目的端的数据写入操作
        tableWriter.prepareWrite(sourceSchemaName, sourceTableName, targetFields);

        // 清空目的端表的数据
        tableManager.truncateTableData(sourceSchemaName, sourceTableName);

        // 查询源端数据并写入目的端
        sourceQuerier.setQueryFetchSize(BATCH_SIZE);

        ResultSetWrapper srs = sourceQuerier.queryTableData(
                sourceSchemaName, sourceTableName, sourceFields
        );

        List<Object[]> cache = new LinkedList<>();
        long cacheBytes = 0;
        try (ResultSet rs = srs.getResultSet()) {
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                Object[] record = new Object[sourceFields.size()];
                long bytes = 0;
                for (int i = 1; i <= sourceFields.size(); ++i) {
                    try {
                        Object value = rs.getObject(i);
                        bytes += JdbcTypesUtils.getObjectSize(metaData.getColumnType(i), value);
                        record[i - 1] = value;
                    } catch (Exception e) {
                        log.warn("!!! Read data from table [ {} ] use function ResultSet.getObject() error",
                                 e);
                        record[i - 1] = null;
                    }
                }

                cache.add(transformer.doTransform(sourceSchemaName, sourceTableName, sourceFields, record));
                cacheBytes += bytes;
                totalCount.incrementAndGet();

                if (cache.size() >= BATCH_SIZE || cacheBytes >= MAX_CACHE_BYTES_SIZE) {
                    final long finalCacheBytes = cacheBytes;
                    this.memChannel.add(
                            BatchElement.builder()
                                    .tableNameMapString(tableNameMapString)
                                    .handler((arg1, arg2, logger) -> {
                                        long ret = tableWriter.write(arg1, arg2);
                                        logger.info("[FullCoverSync] handle write table [{}] batch record count: {}, the bytes size: {}",
                                                tableNameMapString, ret, DataSizeUtil.format(finalCacheBytes));
                                        return ret;
                                    })
                                    .arg1(Lists.newArrayList(targetFields))
                                    .arg2(Lists.newArrayList(cache))
                                    .build()
                    );
                    cache.clear();
                    totalBytes.addAndGet(cacheBytes);
                    cacheBytes = 0;
                }
            }

            if (cache.size() > 0) {
                final long finalCacheBytes = cacheBytes;
                this.memChannel.add(
                        BatchElement.builder()
                                .tableNameMapString(tableNameMapString)
                                .handler((arg1, arg2, logger) -> {
                                    long ret = tableWriter.write(arg1, arg2);
                                    logger.info("[FullCoverSync] handle write table [{}] batch record count: {}, the bytes size: {}",
                                            tableNameMapString, ret, DataSizeUtil.format(finalCacheBytes));
                                    return ret;
                                })
                                .arg1(Lists.newArrayList(targetFields))
                                .arg2(Lists.newArrayList(cache))
                                .build()
                );
                cache.clear();
                totalBytes.addAndGet(cacheBytes);
            }

            log.info("[FullCoverSync] handle read table [{}] total record count: {}, total bytes = {}",
                    tableNameMapString, totalCount.get(), DataSizeUtil.format(totalBytes.get()));
        } catch (Throwable e) {
            log.warn("[FullCoverSync] handle read table [{}] error: {}", e.getMessage());
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        } finally {
            // 如果正在读取大表数据的话，这里的close()会很慢
            srs.close();
        }

    }

    private void processBackUp(BackupHandler handler) {
        MemChannel memChannel= MemChannel.createNewChannel(5000);
        ReaderTaskParam readerTaskParam = new ReaderTaskParam();
//        readerTaskParam.getConfiguration()
        readerTaskParam.setMemChannel(memChannel);
        readerTaskParam.setSourceDataSource(handler.getSourceDataSource());
        readerTaskParam.setTargetDataSource(handler.getSourceDataSource());

        DbswichPropertiesConfiguration properties = new DbswichPropertiesConfiguration();
        SourceDataSourceProperties sourceDataSourceProperties= new SourceDataSourceProperties();
        sourceDataSourceProperties.setUsername(handler.getSourceDataSource().getUserName());
        sourceDataSourceProperties.setPassword(handler.getSourceDataSource().getPassword());
        sourceDataSourceProperties.setDriverClassName(handler.getSourceDataSource().getDriverClass());
        sourceDataSourceProperties.setUrl(handler.getSourceDataSource().getJdbcUrl());
        sourceDataSourceProperties.setDriverPath(handler.getSourceDataSource().getDriverClass());

        TargetDataSourceProperties targetDataSourceProperties = new TargetDataSourceProperties();
        BeanUtils.copyProperties(sourceDataSourceProperties,targetDataSourceProperties);

        properties.setSource(sourceDataSourceProperties);
        properties.setTarget(targetDataSourceProperties);
        List<TableDescription> tableDescriptions = splitReaderTask();
        readerTaskParam.setConfiguration(properties);
        readerTaskParam.setCountDownLatch(new CountDownLatch(1));

        tableDescriptions.forEach(tableDescription -> {
            readerTaskParam.setTableDescription(tableDescription);
            WriteHandler writeHandler = new WriteHandler(readerTaskParam);
            writeHandler.doFullCoverSynchronize(handler.getTargetWriter(),
                    handler.getTargetTableManager(), handler.getSourceQuerier(), handler.getTransformProvider());
        });

    }


    @Override
    protected ReaderTaskResult doProcess() {
        return null;
    }

    @Override
    protected ReaderTaskResult exceptProcess(Throwable t) {
        return null;
    }

    private List<TableDescription> splitReaderTask() {
        List<TableDescription> tableDescriptions = new ArrayList<>();

        MetadataService sourceMetaDataService = new DefaultMetadataService(sourceDataSource);

        // 判断处理的策略：是排除还是包含
        SourceDataSourceProperties sourceProperties = null;
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
