// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.handler;

import cn.hutool.core.io.unit.DataSizeUtil;
import com.example.demo.demos.dbswitch.calculate.*;
import com.example.demo.demos.dbswitch.common.consts.Constants;
import com.example.demo.demos.dbswitch.common.entity.CloseableDataSource;
import com.example.demo.demos.dbswitch.common.entity.ResultSetWrapper;
import com.example.demo.demos.dbswitch.common.type.CaseConvertEnum;
import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.dbswitch.common.util.DatabaseAwareUtils;
import com.example.demo.demos.dbswitch.common.util.ExamineUtils;
import com.example.demo.demos.dbswitch.common.util.JdbcTypesUtils;
import com.example.demo.demos.dbswitch.common.util.PatterNameUtils;
import com.example.demo.demos.dbswitch.core.exchange.BatchElement;
import com.example.demo.demos.dbswitch.core.exchange.MemChannel;
import com.example.demo.demos.dbswitch.core.task.TaskProcessor;
import com.example.demo.demos.dbswitch.data.config.DbswichPropertiesConfiguration;
import com.example.demo.demos.dbswitch.data.domain.ReaderTaskParam;
import com.example.demo.demos.dbswitch.data.domain.ReaderTaskResult;
import com.example.demo.demos.dbswitch.data.entity.SourceDataSourceProperties;
import com.example.demo.demos.dbswitch.data.entity.TargetDataSourceProperties;
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
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 数据读取线程体（一个表的读）
 *
 * @author tang
 */
@Slf4j
public class ReaderTaskThread extends TaskProcessor<ReaderTaskResult> {

  private final long MAX_CACHE_BYTES_SIZE = 128 * 1024 * 1024;

  private final DbswichPropertiesConfiguration properties;
  private final SourceDataSourceProperties sourceProperties;
  private final TargetDataSourceProperties targetProperties;
  private int fetchSize = Constants.MINIMUM_FETCH_SIZE;
  private TableDescription tableDescription;
  private MemChannel memChannel;

  // 来源端
  private final CloseableDataSource sourceDataSource;
  private MetadataService sourceMetaDataService;
  private ProductTypeEnum sourceProductType;
  private String sourceSchemaName;
  private String sourceTableName;
  private String sourceTableRemarks;
  private List<ColumnDescription> sourceColumnDescriptions;
  private List<String> sourcePrimaryKeys;

  // 目的端
  private final CloseableDataSource targetDataSource;
  private ProductTypeEnum targetProductType;
  private Set<String> targetExistTables;
  private String targetSchemaName;
  private String targetTableName;
  private List<ColumnDescription> targetColumnDescriptions;
  private List<String> targetPrimaryKeys;

  // 日志输出字符串使用
  private String tableNameMapString;

  // 统计信息
  AtomicLong totalBytes = new AtomicLong(0);
  AtomicLong totalCount = new AtomicLong(0);

  private CountDownLatch robotCountDownLatch;

  public ReaderTaskThread(ReaderTaskParam taskParam) {
    this.sourceDataSource = taskParam.getSourceDataSource();
    this.targetDataSource = taskParam.getTargetDataSource();
    this.tableDescription = taskParam.getTableDescription();
    this.memChannel = taskParam.getMemChannel();
    this.properties = taskParam.getConfiguration();
    this.sourceProperties = this.properties.getSource();
    this.targetProperties = this.properties.getTarget();
    this.sourceSchemaName = this.sourceProperties.getSourceSchema();
    this.sourceTableName = this.tableDescription.getTableName();
    this.targetExistTables = taskParam.getTargetExistTables();
    this.robotCountDownLatch = taskParam.getCountDownLatch();
  }

  @Override
  protected void beforeProcess() {
    if (sourceProperties.getFetchSize() >= fetchSize) {
      fetchSize = sourceProperties.getFetchSize();
    }

    this.sourceProductType = DatabaseAwareUtils.getProductTypeByDataSource(sourceDataSource);
    this.targetProductType = DatabaseAwareUtils.getProductTypeByDataSource(targetDataSource);

    if (this.targetProductType.isLikeHive()) {
      // !! hive does not support upper table name and upper column name
      properties.getTarget().setTableNameCase(CaseConvertEnum.LOWER);
      properties.getTarget().setColumnNameCase(CaseConvertEnum.LOWER);
    }

    // 获取映射转换后新的表名
    this.targetSchemaName = properties.getTarget().getTargetSchema();
    this.targetTableName = properties.getTarget()
        .getTableNameCase()
        .convert(
            PatterNameUtils.getFinalName(
                tableDescription.getTableName(),
                sourceProperties.getRegexTableMapper()
            )
        );

    if (StringUtils.isEmpty(this.targetTableName)) {
      throw new RuntimeException("表名的映射规则配置有误，不能将[" + this.sourceTableName + "]映射为空");
    }

    this.tableNameMapString = String.format("%s.%s --> %s.%s",
        tableDescription.getSchemaName(), tableDescription.getTableName(),
        targetSchemaName, targetTableName);
  }

  @Override
  protected ReaderTaskResult doProcess() {
    log.info("Begin Migrate table for {}", tableNameMapString);

    this.sourceMetaDataService = new DefaultMetadataService(sourceDataSource, sourceProductType);

    // 读取源表的表及字段元数据
    this.sourceTableRemarks = sourceMetaDataService
        .getTableRemark(sourceSchemaName, sourceTableName);
    checkInterrupt();
    this.sourceColumnDescriptions = sourceMetaDataService
        .queryTableColumnMeta(sourceSchemaName, sourceTableName);
    checkInterrupt();
    this.sourcePrimaryKeys = sourceMetaDataService
        .queryTablePrimaryKeys(sourceSchemaName, sourceTableName);
    checkInterrupt();

    // 根据表的列名映射转换准备目标端表的字段信息
    this.targetColumnDescriptions = sourceColumnDescriptions.stream()
        .map(column -> {
          String newName = properties.getTarget().getColumnNameCase()
              .convert(
                  PatterNameUtils.getFinalName(
                      column.getFieldName(),
                      sourceProperties.getRegexColumnMapper())
              );
          ColumnDescription description = column.copy();
          description.setFieldName(newName);
          description.setLabelName(newName);
          return description;
        }).collect(Collectors.toList());
    this.targetPrimaryKeys = sourcePrimaryKeys.stream()
        .map(name ->
            properties.getTarget().getColumnNameCase()
                .convert(
                    PatterNameUtils.getFinalName(
                        name,
                        sourceProperties.getRegexColumnMapper())
                )
        ).collect(Collectors.toList());

    // 打印表名与字段名的映射关系
    List<String> columnMapperPairs = new ArrayList<>();
    Map<String, String> mapChecker = new HashMap<>();
    for (int i = 0; i < sourceColumnDescriptions.size(); ++i) {
      String sourceColumnName = sourceColumnDescriptions.get(i).getFieldName();
      String targetColumnName = targetColumnDescriptions.get(i).getFieldName();
      if (StringUtils.hasLength(targetColumnName)) {
        columnMapperPairs.add(String.format("%s --> %s", sourceColumnName, targetColumnName));
        mapChecker.put(sourceColumnName, targetColumnName);
      } else {
        columnMapperPairs.add(String.format(
            "%s --> %s",
            sourceColumnName,
            String.format("<!Field(%s) is Deleted>", (i + 1))
        ));
      }
    }
    log.info("Mapping relation : \ntable mapper : {}  \ncolumn mapper :\n\t{} ",
        tableNameMapString, String.join("\n\t", columnMapperPairs));
    Set<String> valueSet = new HashSet<>(mapChecker.values());
    if (valueSet.size() <= 0) {
      throw new RuntimeException("字段映射配置有误，禁止通过映射将表所有的字段都删除!");
    }
    if (!valueSet.containsAll(this.targetPrimaryKeys)) {
      throw new RuntimeException("字段映射配置有误，禁止通过映射将表的主键字段删除!");
    }
    if (mapChecker.keySet().size() != valueSet.size()) {
      throw new RuntimeException("字段映射配置有误，禁止将多个字段映射到一个同名字段!");
    }

    checkInterrupt();

    ProductFactoryProvider sourceFactoryProvider = ProductProviderFactory
        .newProvider(sourceProductType, sourceDataSource);
    ProductFactoryProvider targetFactoryProvider = ProductProviderFactory
        .newProvider(targetProductType, targetDataSource);
    TableDataQueryProvider sourceQuerier = sourceFactoryProvider.createTableDataQueryProvider();
    RecordTransformProvider transformProvider = sourceFactoryProvider.createRecordTransformProvider();
    TableDataWriteProvider targetWriter = targetFactoryProvider.createTableDataWriteProvider(
        properties.getTarget().getWriterEngineInsert());
    MetadataProvider targetMetaProvider = targetFactoryProvider.createMetadataQueryProvider();
    TableManageProvider targetTableManager = targetFactoryProvider.createTableManageProvider();
    TableDataSynchronizeProvider targetSynchronizer = targetFactoryProvider.createTableDataSynchronizeProvider();

    if (sourceProductType.isMongodb()) {
      properties.getTarget().setTargetDrop(true);
    }

    if (targetProductType.isMongodb() || targetProductType.isElasticSearch()) {
      try {
        targetFactoryProvider.createTableManageProvider()
            .dropTable(targetSchemaName, targetTableName);
        log.info("Target Table {}.{} is exits, drop it now !", targetSchemaName, targetTableName);
      } catch (Exception e) {
        log.info("Target Table {}.{} is not exits, create it!", targetSchemaName, targetTableName);
      }
      return doFullCoverSynchronize(targetWriter, targetTableManager, sourceQuerier, transformProvider);
    } else if (properties.getTarget().getTargetDrop() || targetProductType.isLikeHive()) {
      /*
        如果配置了dbswitch.target.datasource-target-drop=true时，
        <p>
        先执行drop table语句，然后执行create table语句
       */

      try {
        targetFactoryProvider.createTableManageProvider()
            .dropTable(targetSchemaName, targetTableName);
        log.info("Target Table {}.{} is exits, drop it now !", targetSchemaName, targetTableName);
      } catch (Exception e) {
        log.info("Target Table {}.{} is not exits, create it!", targetSchemaName, targetTableName);
      }

      // 生成建表语句并创建
      List<String> sqlCreateTable = sourceMetaDataService.getDDLCreateTableSQL(
          targetMetaProvider,
          targetColumnDescriptions.stream()
              .filter(column -> StringUtils.hasLength(column.getFieldName()))
              .collect(Collectors.toList()),
          targetPrimaryKeys,
          targetSchemaName,
          targetTableName,
          sourceTableRemarks,
          properties.getTarget().getCreateTableAutoIncrement(),
          getTblProperties()
      );

      JdbcTemplate targetJdbcTemplate = new JdbcTemplate(targetDataSource);
      for (String sql : sqlCreateTable) {
        checkInterrupt();
        log.info("Execute SQL: \n{}", sql);
        targetJdbcTemplate.execute(sql);
      }

      // 如果只想创建表，这里直接返回
      if (null != properties.getTarget().getOnlyCreate()
          && properties.getTarget().getOnlyCreate()) {
        return ReaderTaskResult.builder()
            .tableNameMapString(tableNameMapString)
            .successCount(1).build();
      }

      if (targetProductType.isLikeHive()) {
        return ReaderTaskResult.builder()
            .tableNameMapString(tableNameMapString)
            .successCount(1).build();
      }

      checkInterrupt();
      return doFullCoverSynchronize(targetWriter, targetTableManager, sourceQuerier, transformProvider);
    } else {
      // 对于只想创建表的情况，不提供后续的变化量数据同步功能
      if (null != properties.getTarget().getOnlyCreate()
          && properties.getTarget().getOnlyCreate()) {
        return ReaderTaskResult.builder()
            .tableNameMapString(tableNameMapString)
            .successCount(1).build();
      }

      checkInterrupt();

      if (!targetExistTables.contains(targetTableName)) {
        // 当目标端不存在该表时，则生成建表语句并创建
        List<String> sqlCreateTable = sourceMetaDataService.getDDLCreateTableSQL(
            targetMetaProvider,
            targetColumnDescriptions.stream()
                .filter(column -> StringUtils.hasLength(column.getFieldName()))
                .collect(Collectors.toList()),
            targetPrimaryKeys,
            targetSchemaName,
            targetTableName,
            sourceTableRemarks,
            properties.getTarget().getCreateTableAutoIncrement(),
            getTblProperties()
        );

        JdbcTemplate targetJdbcTemplate = new JdbcTemplate(targetDataSource);
        for (String sql : sqlCreateTable) {
          targetJdbcTemplate.execute(sql);
          log.info("Execute SQL: \n{}", sql);
        }

        checkInterrupt();
        return doFullCoverSynchronize(targetWriter, targetTableManager, sourceQuerier, transformProvider);
      }

      // 判断是否具备变化量同步的条件：（1）两端表结构一致，且都有一样的主键字段；(2)MySQL使用Innodb引擎；
      if (properties.getTarget().getChangeDataSync()) {
        // 根据主键情况判断同步的方式：增量同步或覆盖同步
        MetadataService metaDataByDatasourceService =
            new DefaultMetadataService(targetDataSource, targetProductType);
        List<String> dbTargetPks = metaDataByDatasourceService.queryTablePrimaryKeys(
            targetSchemaName, targetTableName);

        if (!targetPrimaryKeys.isEmpty() && !dbTargetPks.isEmpty()
            && targetPrimaryKeys.containsAll(dbTargetPks)
            && dbTargetPks.containsAll(targetPrimaryKeys)) {
          if (targetProductType == ProductTypeEnum.MYSQL
              && !DatabaseAwareUtils.isMysqlInnodbStorageEngine(
              targetSchemaName, targetTableName, targetDataSource)) {
            return doFullCoverSynchronize(targetWriter, targetTableManager, sourceQuerier, transformProvider);
          } else {
            return doIncreaseSynchronize(targetSynchronizer, transformProvider);
          }
        } else {
          return doFullCoverSynchronize(targetWriter, targetTableManager, sourceQuerier, transformProvider);
        }
      } else {
        return doFullCoverSynchronize(targetWriter, targetTableManager, sourceQuerier, transformProvider);
      }
    }
  }

  /**
   * 执行覆盖同步
   *
   * @param tableWriter
   * @param tableManager
   * @param sourceQuerier
   * @param transformer
   * @return ReaderTaskResult
   */
  private ReaderTaskResult doFullCoverSynchronize(TableDataWriteProvider tableWriter, TableManageProvider tableManager,
      TableDataQueryProvider sourceQuerier, RecordTransformProvider transformer) {
    final int BATCH_SIZE = fetchSize;

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
    tableWriter.prepareWrite(targetSchemaName, targetTableName, targetFields);

    // 清空目的端表的数据
    tableManager.truncateTableData(targetSchemaName, targetTableName);

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
        checkInterrupt();
        Object[] record = new Object[sourceFields.size()];
        long bytes = 0;
        for (int i = 1; i <= sourceFields.size(); ++i) {
          try {
            Object value = rs.getObject(i);
            bytes += JdbcTypesUtils.getObjectSize(metaData.getColumnType(i), value);
            record[i - 1] = value;
          } catch (Exception e) {
            log.warn("!!! Read data from table [ {} ] use function ResultSet.getObject() error",
                tableNameMapString, e);
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

    return ReaderTaskResult
        .builder()
        .tableNameMapString(tableNameMapString)
        .successCount(1)
        .failureCount(0)
        .totalBytes(totalBytes.get())
        .recordCount(totalCount.get())
        .build();
  }

  /**
   * 变化量同步
   *
   * @param synchronizer
   * @param transformer
   * @return ReaderTaskResult
   */
  private ReaderTaskResult doIncreaseSynchronize(TableDataSynchronizeProvider synchronizer,
      RecordTransformProvider transformer) {
    final int BATCH_SIZE = fetchSize;

    List<String> sourceFields = new ArrayList<>();
    List<String> targetFields = new ArrayList<>();
    Map<String, String> columnNameMaps = new HashMap<>();
    for (int i = 0; i < targetColumnDescriptions.size(); ++i) {
      ColumnDescription scd = sourceColumnDescriptions.get(i);
      ColumnDescription tcd = targetColumnDescriptions.get(i);
      if (!StringUtils.isEmpty(tcd.getFieldName())) {
        sourceFields.add(scd.getFieldName());
        targetFields.add(tcd.getFieldName());
        columnNameMaps.put(scd.getFieldName(), tcd.getFieldName());
      }
    }

    TaskParamEntity.TaskParamEntityBuilder taskBuilder = TaskParamEntity.builder();
    taskBuilder.oldDataSource(targetDataSource);
    taskBuilder.oldSchemaName(targetSchemaName);
    taskBuilder.oldTableName(targetTableName);
    taskBuilder.newDataSource(sourceDataSource);
    taskBuilder.newSchemaName(sourceSchemaName);
    taskBuilder.newTableName(sourceTableName);
    taskBuilder.fieldColumns(sourceFields);
    taskBuilder.columnsMap(columnNameMaps);
    taskBuilder.transformer(transformer);

    TaskParamEntity param = taskBuilder.build();

    synchronizer.prepare(targetSchemaName, targetTableName, targetFields, targetPrimaryKeys);

    RecordRowChangeCalculator calculator = new DefaultChangeCalculatorService();
    calculator.setFetchSize(fetchSize);
    calculator.setInterruptCheck(this::checkInterrupt);
    calculator.setRecordIdentical(false);
    calculator.setCheckJdbcType(false);

    // 执行实际的变化同步过程
    log.info("[IncreaseSync] Handle table by compare [{}] data now ... ", tableNameMapString);
    calculator.executeCalculate(param, new RecordRowHandler() {

      private long countInsert = 0;
      private long countUpdate = 0;
      private long countDelete = 0;
      private long countTotal = 0;
      private long cacheBytes = 0;
      private final List<Object[]> cacheInsert = new LinkedList<>();
      private final List<Object[]> cacheUpdate = new LinkedList<>();
      private final List<Object[]> cacheDelete = new LinkedList<>();

      @Override
      public void handle(List<String> fields, Object[] record, int[] jdbcTypes, RowChangeTypeEnum flag) {
        switch (flag) {
          case VALUE_INSERT:
            if (!targetProperties.getTargetSyncOption().callInsert()) {
              return;
            }
            cacheInsert.add(record);
            countInsert++;
            break;
          case VALUE_CHANGED:
            if (!targetProperties.getTargetSyncOption().callUpdate()) {
              return;
            }
            cacheUpdate.add(record);
            countUpdate++;
            break;
          case VALUE_DELETED:
            if (!targetProperties.getTargetSyncOption().callDelete()) {
              return;
            } else {
              cacheDelete.add(record);
              countDelete++;
            }
            break;
          default:
            return;
        }

        long bytes = JdbcTypesUtils.getRecordSize(record, jdbcTypes);
        cacheBytes += bytes;
        totalBytes.addAndGet(bytes);
        totalCount.addAndGet(1);
        countTotal++;
        checkFull(fields);
      }

      /**
       * 检测缓存是否已满，如果已满执行同步操作
       *
       * @param fields 同步的字段列表
       */
      private void checkFull(List<String> fields) {
        checkInterrupt();
        if (cacheInsert.size() >= BATCH_SIZE || cacheUpdate.size() >= BATCH_SIZE
            || cacheDelete.size() >= BATCH_SIZE || cacheBytes >= MAX_CACHE_BYTES_SIZE) {
          if (cacheDelete.size() > 0) {
            doDelete(fields);
          }

          if (cacheInsert.size() > 0) {
            doInsert(fields);
          }

          if (cacheUpdate.size() > 0) {
            doUpdate(fields);
          }

          cacheBytes = 0;
        }
      }

      @Override
      public void destroy(List<String> fields) {
        if (cacheDelete.size() > 0) {
          doDelete(fields);
        }

        if (cacheInsert.size() > 0) {
          doInsert(fields);
        }

        if (cacheUpdate.size() > 0) {
          doUpdate(fields);
        }

        log.info("[IncreaseSync] Handle table by compare [{}] total count: {}, Insert:{},Update:{},Delete:{} ",
            tableNameMapString, countTotal, countInsert, countUpdate, countDelete);
      }

      private void doInsert(List<String> fields) {
        ReaderTaskThread.this.memChannel.add(
            BatchElement.builder()
                .tableNameMapString(tableNameMapString)
                .handler((arg1, arg2, logger) -> {
                  long ret = synchronizer.executeInsert(arg2);
                  logger.info("[IncreaseSync] Handle write table [{}] record Insert count: {}",
                      tableNameMapString, ret);
                  return ret;
                })
                .arg1(fields)
                .arg2(Lists.newArrayList(cacheInsert))
                .build()
        );
        cacheInsert.clear();
      }

      private void doUpdate(List<String> fields) {
        ReaderTaskThread.this.memChannel.add(
            BatchElement.builder()
                .tableNameMapString(tableNameMapString)
                .handler((arg1, arg2, logger) -> {
                  long ret = synchronizer.executeUpdate(arg2);
                  logger.info("[IncreaseSync] Handle write table [{}] record Update count: {}",
                      tableNameMapString, ret);
                  return ret;
                })
                .arg1(fields)
                .arg2(Lists.newArrayList(cacheUpdate))
                .build()
        );
        cacheUpdate.clear();
      }

      private void doDelete(List<String> fields) {
        ReaderTaskThread.this.memChannel.add(
            BatchElement.builder()
                .tableNameMapString(tableNameMapString)
                .handler((arg1, arg2, logger) -> {
                  long ret = synchronizer.executeDelete(arg2);
                  logger.info("[IncreaseSync] Handle write table [{}] record Delete count: {}",
                      tableNameMapString, ret);
                  return ret;
                })
                .arg1(fields)
                .arg2(Lists.newArrayList(cacheDelete))
                .build()
        );
        cacheDelete.clear();
      }

    });

    return ReaderTaskResult
        .builder()
        .tableNameMapString(tableNameMapString)
        .successCount(1)
        .failureCount(0)
        .recordCount(totalCount.get())
        .totalBytes(totalBytes.get())
        .build();
  }

  /**
   * https://cwiki.apache.org/confluence/display/Hive/JDBC+Storage+Handler
   *
   * @return Map<String, String>
   */
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

  @Override
  protected ReaderTaskResult exceptProcess(Throwable t) {
    log.error("Error migration for table: {}.{}, error message: {}",
        tableDescription.getSchemaName(), tableDescription.getTableName(), t);
    return ReaderTaskResult.builder()
        .tableNameMapString(tableNameMapString)
        .successCount(0)
        .failureCount(1)
        .recordCount(totalCount.get())
        .totalBytes(totalBytes.get())
        .throwable(t)
        .build();
  }

  @Override
  protected void afterProcess() {
    robotCountDownLatch.countDown();
  }
}
