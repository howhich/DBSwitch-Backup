// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.provider.sync;

import cn.hutool.json.JSONUtil;
import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.dbswitch.provider.AbstractCommonProvider;
import com.example.demo.demos.dbswitch.provider.ProductFactoryProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据同步抽象基类
 *
 * @author tang
 */
@Slf4j
public class DefaultTableDataSynchronizeProvider
    extends AbstractCommonProvider implements TableDataSynchronizeProvider {

  private JdbcTemplate jdbcTemplate;
  private PlatformTransactionManager tx;
  private Map<String, Integer> columnType;
  protected List<String> fieldOrders;
  protected List<String> pksOrders;
  protected String insertStatementSql;
  protected String updateStatementSql;
  protected String deleteStatementSql;
  protected int[] insertArgsType;
  protected int[] updateArgsType;
  protected int[] deleteArgsType;

  public DefaultTableDataSynchronizeProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
    this.jdbcTemplate = new JdbcTemplate(factoryProvider.getDataSource());
    this.tx = new DataSourceTransactionManager(factoryProvider.getDataSource());
    this.columnType = new HashMap<>();
  }

  @Override
  public void prepare(String schemaName, String tableName, List<String> fieldNames, List<String> pks) {
    if (fieldNames.isEmpty() || pks.isEmpty() || fieldNames.size() < pks.size()) {
      throw new IllegalArgumentException("字段列表和主键列表不能为空，或者字段总个数应不小于主键总个数");
    }
    if (!fieldNames.containsAll(pks)) {
      throw new IllegalArgumentException("字段列表必须包含主键列表");
    }

    this.columnType = getTableColumnMetaData(schemaName, tableName, fieldNames);
    this.fieldOrders = new ArrayList<>(fieldNames);
    this.pksOrders = new ArrayList<>(pks);

    this.insertStatementSql = getInsertPrepareStatementSql(schemaName, tableName, fieldNames);
    this.updateStatementSql = getUpdatePrepareStatementSql(schemaName, tableName, fieldNames, pks);
    this.deleteStatementSql = getDeletePrepareStatementSql(schemaName, tableName, pks);

    insertArgsType = new int[fieldNames.size()];
    for (int k = 0; k < fieldNames.size(); ++k) {
      String field = fieldNames.get(k);
      insertArgsType[k] = this.columnType.get(field);
    }

    updateArgsType = new int[fieldNames.size()];
    int idx = 0;
    for (int i = 0; i < fieldNames.size(); ++i) {
      String field = fieldNames.get(i);
      if (!pks.contains(field)) {
        updateArgsType[idx++] = this.columnType.get(field);
      }
    }
    for (String pk : pks) {
      updateArgsType[idx++] = this.columnType.get(pk);
    }

    deleteArgsType = new int[pks.size()];
    for (int j = 0; j < pks.size(); ++j) {
      String pk = pks.get(j);
      deleteArgsType[j] = this.columnType.get(pk);
    }
  }

  @Override
  public long executeInsert(List<Object[]> records) {
    TransactionStatus status = tx.getTransaction(getDefaultTransactionDefinition());
    if (log.isDebugEnabled()) {
      log.debug("Execute Insert SQL : {}", this.insertStatementSql);
    }

    try {
      try {
        jdbcTemplate.batchUpdate(this.insertStatementSql, records, this.insertArgsType);
      } catch (Exception e) {
        if (e instanceof java.sql.BatchUpdateException) {
          for (Object[] dataList : records) {
            try {
              jdbcTemplate.update(this.insertStatementSql, dataList, this.updateArgsType);
            } catch (Exception ex) {
              log.error("Failed to insert by SQL: {}, value: {}", this.insertStatementSql,
                  JSONUtil.toJsonStr(dataList));
              throw ex;
            }
          }
        } else {
          throw e;
        }
      }

      tx.commit(status);
      return records.size();
    } catch (Exception e) {
      tx.rollback(status);
      throw e;
    }
  }

  @Override
  public long executeUpdate(List<Object[]> records) {
    List<Object[]> dataLists = new LinkedList<>();
    for (Object[] r : records) {
      Object[] nr = new Object[this.fieldOrders.size()];
      int idx = 0;
      for (int i = 0; i < this.fieldOrders.size(); ++i) {
        String field = this.fieldOrders.get(i);
        if (!this.pksOrders.contains(field)) {
          int index = this.fieldOrders.indexOf(field);
          nr[idx++] = r[index];
        }
      }
      for (int j = 0; j < this.pksOrders.size(); ++j) {
        String pk = this.pksOrders.get(j);
        int index = this.fieldOrders.indexOf(pk);
        nr[idx++] = r[index];
      }
      dataLists.add(nr);
    }

    TransactionStatus status = tx.getTransaction(getDefaultTransactionDefinition());
    if (log.isDebugEnabled()) {
      log.debug("Execute Update SQL : {}", this.updateStatementSql);
    }

    try {
      try {
        jdbcTemplate.batchUpdate(this.updateStatementSql, dataLists, this.updateArgsType);
      } catch (Exception e) {
        if (e instanceof java.sql.BatchUpdateException) {
          for (Object[] dataList : dataLists) {
            try {
              jdbcTemplate.update(this.updateStatementSql, dataList, this.updateArgsType);
            } catch (Exception ex) {
              log.error("Failed to update by SQL: {}, value: {}", this.updateStatementSql,
                  JSONUtil.toJsonStr(dataList));
              throw ex;
            }
          }
        } else {
          throw e;
        }
      }

      tx.commit(status);
      return dataLists.size();
    } catch (Exception e) {
      tx.rollback(status);
      throw e;
    }
  }

  @Override
  public long executeDelete(List<Object[]> records) {
    List<Object[]> dataLists = new LinkedList<>();
    for (Object[] r : records) {
      Object[] nr = new Object[this.pksOrders.size()];
      for (int i = 0; i < this.pksOrders.size(); ++i) {
        String pk = this.pksOrders.get(i);
        int index = this.fieldOrders.indexOf(pk);
        nr[i] = r[index];
      }
      dataLists.add(nr);
    }

    TransactionStatus status = tx.getTransaction(getDefaultTransactionDefinition());
    if (log.isDebugEnabled()) {
      log.debug("Execute Delete SQL : {}", this.deleteStatementSql);
    }

    try {
      jdbcTemplate.batchUpdate(this.deleteStatementSql, dataLists, this.deleteArgsType);
      tx.commit(status);
      return dataLists.size();
    } catch (Exception e) {
      tx.rollback(status);
      throw e;
    } finally {
      dataLists.clear();
    }
  }

  /**
   * 生成Insert操作的SQL语句
   *
   * @param schemaName 模式名称
   * @param tableName  表名称
   * @param fieldNames 字段列表
   * @return Insert操作的SQL语句
   */
  protected String getInsertPrepareStatementSql(String schemaName, String tableName,
      List<String> fieldNames) {
    List<String> placeHolders = Collections.nCopies(fieldNames.size(), "?");
    ProductTypeEnum productType = getProductType();
    String fullTableName = productType.quoteSchemaTableName(schemaName, tableName);
    return String.format("INSERT INTO %s ( %s ) VALUES ( %s )",
        fullTableName,
        productType.quoteName(StringUtils.join(fieldNames, productType.quoteName(","))),
        StringUtils.join(placeHolders, ","));
  }

  /**
   * 生成Update操作的SQL语句
   *
   * @param schemaName 模式名称
   * @param tableName  表名称
   * @param fieldNames 字段列表
   * @param pks        主键列表
   * @return Update操作的SQL语句
   */
  protected String getUpdatePrepareStatementSql(String schemaName, String tableName,
      List<String> fieldNames, List<String> pks) {
    ProductTypeEnum productType = getProductType();
    String fullTableName = productType.quoteSchemaTableName(schemaName, tableName);
    List<String> uf = fieldNames.stream()
        .filter(field -> !pks.contains(field))
        .map(field -> String.format("%s=?", productType.quoteName(field)))
        .collect(Collectors.toList());
    List<String> uw = pks.stream()
        .map(pk -> String.format("%s=?", productType.quoteName(pk)))
        .collect(Collectors.toList());
    return String.format("UPDATE %s SET %s WHERE %s",
        fullTableName, StringUtils.join(uf, " , "),
        StringUtils.join(uw, " AND "));
  }

  /**
   * 生成Delete操作的SQL语句
   *
   * @param schemaName 模式名称
   * @param tableName  表名称
   * @param pks        主键列表
   * @return Delete操作的SQL语句
   */
  protected String getDeletePrepareStatementSql(String schemaName, String tableName,
      List<String> pks) {
    ProductTypeEnum productType = getProductType();
    String fullTableName = productType.quoteSchemaTableName(schemaName, tableName);
    List<String> uw = pks.stream()
        .map(pk -> String.format("%s=?", productType.quoteName(pk)))
        .collect(Collectors.toList());
    return String.format("DELETE FROM %s WHERE %s ",
        fullTableName, StringUtils.join(uw, "  AND  "));
  }

}
