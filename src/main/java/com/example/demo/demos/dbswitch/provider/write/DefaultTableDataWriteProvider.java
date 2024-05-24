// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.provider.write;

import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.dbswitch.provider.AbstractCommonProvider;
import com.example.demo.demos.dbswitch.provider.ProductFactoryProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class DefaultTableDataWriteProvider
    extends AbstractCommonProvider
    implements TableDataWriteProvider {

  protected JdbcTemplate jdbcTemplate;
  protected String schemaName;
  protected String tableName;
  protected Map<String, Integer> columnType;

  public DefaultTableDataWriteProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
    this.jdbcTemplate = new JdbcTemplate(factoryProvider.getDataSource());
    this.schemaName = null;
    this.tableName = null;
    this.columnType = null;
  }

  protected String getPrepareInsertTableSql(List<String> fieldNames) {
    ProductTypeEnum productType = getProductType();
    String fullTableName = productType.quoteSchemaTableName(schemaName, tableName);
    return String.format("INSERT INTO %s ( %s ) VALUES ( %s )",
        fullTableName,
        productType.quoteName(StringUtils.join(fieldNames, productType.quoteName(","))),
        StringUtils.join(Collections.nCopies(fieldNames.size(), "?"), ","));
  }

  @Override
  public void prepareWrite(String schemaName, String tableName, List<String> fieldNames) {
    this.columnType = getTableColumnMetaData(schemaName, tableName, fieldNames);
    if (this.columnType.isEmpty()) {
      throw new RuntimeException(
          String.format("获取表:%s.%s 的字段的元信息时失败. 请联系 DBA 核查该库、表信息.",
              schemaName, tableName));
    }
    this.schemaName = schemaName;
    this.tableName = tableName;
  }

  @Override
  public long write(List<String> fieldNames, List<Object[]> recordValues) {
    if (recordValues.isEmpty()) {
      return 0;
    }
    String sqlInsert = getPrepareInsertTableSql(fieldNames);
    int[] argTypes = new int[fieldNames.size()];
    for (int i = 0; i < fieldNames.size(); ++i) {
      String col = fieldNames.get(i);
      argTypes[i] = this.columnType.get(col);
    }

    PlatformTransactionManager tx = new DataSourceTransactionManager(getDataSource());
    TransactionStatus status = tx.getTransaction(getDefaultTransactionDefinition());
    try {
      jdbcTemplate.batchUpdate(sqlInsert, recordValues, argTypes);
      tx.commit(status);
      int affectCount = recordValues.size();
      recordValues.clear();
      if (log.isDebugEnabled()) {
        log.debug("{} insert data affect count: {}", getProductType(), affectCount);
      }
      return affectCount;
    } catch (Exception e) {
      tx.rollback(status);
      throw e;
    }
  }


}
