// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.provider;

import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.dbswitch.common.util.ExamineUtils;
import com.example.demo.demos.dbswitch.features.ProductFeatures;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractCommonProvider {

  private ProductFactoryProvider factoryProvider;
  private DataSource dataSource;
  private ProductFeatures productFeatures;

  protected AbstractCommonProvider(ProductFactoryProvider factoryProvider) {
    ExamineUtils.checkNotNull(factoryProvider, "factoryProvider");
    this.factoryProvider = factoryProvider;
    this.dataSource = factoryProvider.getDataSource();
    this.productFeatures = factoryProvider.getProductFeatures();
  }

  public ProductTypeEnum getProductType() {
    return this.factoryProvider.getProductType();
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public ProductFeatures getProductFeatures() {
    return productFeatures;
  }

  protected String getTableFieldsQuerySQL(String schemaName, String tableName) {
    ProductTypeEnum productType = getProductType();
    String fullTableName = productType.quoteSchemaTableName(schemaName, tableName);
    return String.format("SELECT %s FROM %s WHERE 1=2", "*", fullTableName);
  }

  protected Map<String, Integer> getTableColumnMetaData(String schemaName, String tableName, List<String> fieldNames) {
    ProductTypeEnum productType = getProductType();
    String fullTableName = productType.quoteSchemaTableName(schemaName, tableName);
    String queryColumnStr = CollectionUtils.isEmpty(fieldNames) ? "*"
        : productType.quoteName(StringUtils.join(fieldNames, productType.quoteName(",")));
    String sql = String.format("SELECT %s FROM %s WHERE 1=2", queryColumnStr, fullTableName);

    Map<String, Integer> columnMetaDataMap = new HashMap<>();
    try (Connection connection = dataSource.getConnection()) {
      try (Statement stmt = connection.createStatement();
          ResultSet rs = stmt.executeQuery(sql);) {
        ResultSetMetaData rsMetaData = rs.getMetaData();
        for (int i = 0, len = rsMetaData.getColumnCount(); i < len; i++) {
          columnMetaDataMap.put(rsMetaData.getColumnName(i + 1), rsMetaData.getColumnType(i + 1));
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(
          String.format("获取表:%s.%s 的字段的元信息时失败. 请联系 DBA 核查该库、表信息.",
              schemaName, tableName), e);
    }
    return columnMetaDataMap;
  }

  protected TransactionDefinition getDefaultTransactionDefinition() {
    DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
    definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    return definition;
  }

}
