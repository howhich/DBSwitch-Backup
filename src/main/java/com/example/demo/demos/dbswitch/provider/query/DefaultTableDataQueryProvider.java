// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.provider.query;

import cn.hutool.core.util.HexUtil;
import com.example.demo.demos.dbswitch.common.consts.Constants;
import com.example.demo.demos.dbswitch.common.entity.ResultSetWrapper;
import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.dbswitch.common.util.ObjectCastUtils;
import com.example.demo.demos.dbswitch.provider.AbstractCommonProvider;
import com.example.demo.demos.dbswitch.provider.ProductFactoryProvider;
import com.example.demo.demos.dbswitch.schema.SchemaTableData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DefaultTableDataQueryProvider
    extends AbstractCommonProvider
    implements TableDataQueryProvider {

  private final int resultSetType = ResultSet.TYPE_FORWARD_ONLY;
  private final int concurrency = ResultSet.CONCUR_READ_ONLY;

  private int fetchSize = Constants.DEFAULT_FETCH_SIZE;

  public DefaultTableDataQueryProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public int getQueryFetchSize() {
    return this.fetchSize;
  }

  @Override
  public void setQueryFetchSize(int size) {
    if (size < Constants.MINIMUM_FETCH_SIZE) {
      throw new IllegalArgumentException(
          "设置的批量处理行数的大小fetchSize=" + size + "不得小于" + Constants.MINIMUM_FETCH_SIZE);
    }
    this.fetchSize = size;
  }

  @Override
  public ResultSetWrapper queryTableData(String schemaName, String tableName, List<String> fields,
      List<String> orders) {
    ProductTypeEnum productType = getProductType();
    StringBuilder sb = new StringBuilder("SELECT ");
    sb.append(productType.quoteName(StringUtils.join(fields, productType.quoteName(","))));
    sb.append(" FROM ");
    sb.append(productType.quoteSchemaTableName(schemaName, tableName));
    if (CollectionUtils.isNotEmpty(orders)) {
      sb.append(" ORDER BY ");
      sb.append(productType.quoteName(StringUtils.join(orders, productType.quoteName(","))));
    }
    return this.selectTableData(sb.toString(), getProductFeatures().convertFetchSize(this.fetchSize));
  }

  protected ResultSetWrapper selectTableData(String sql, int fetchSize) {
    if (log.isDebugEnabled()) {
      log.debug("Query table data sql :{}", sql);
    }

    try {
      Connection connection = getDataSource().getConnection();
      Statement statement = connection.createStatement(resultSetType, concurrency);
      statement.setQueryTimeout(Constants.DEFAULT_QUERY_TIMEOUT_SECONDS);
      statement.setFetchSize(fetchSize);
      return ResultSetWrapper.builder()
          .connection(connection)
          .statement(statement)
          .resultSet(statement.executeQuery(sql))
          .build();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  @Override
  public SchemaTableData queryTableData(Connection connection, String schemaName, String tableName,
      int rowCount) {
    ProductTypeEnum productType = getProductType();
    String fullTableName = productType.quoteSchemaTableName(schemaName, tableName);
    String querySQL = String.format("SELECT * FROM %s ", fullTableName);
    SchemaTableData data = new SchemaTableData();
    data.setSchemaName(schemaName);
    data.setTableName(tableName);
    data.setColumns(new ArrayList<>());
    data.setRows(new ArrayList<>());
    try (Statement st = connection.createStatement()) {
      beforeExecuteQuery(connection, schemaName, tableName);
      try (ResultSet rs = st.executeQuery(querySQL)) {
        ResultSetMetaData m = rs.getMetaData();
        int count = m.getColumnCount();
        for (int i = 1; i <= count; i++) {
          data.getColumns().add(m.getColumnLabel(i));
        }

        int counter = 0;
        while (rs.next() && counter++ < rowCount) {
          List<Object> row = new ArrayList<>(count);
          for (int i = 1; i <= count; i++) {
            Object value = rs.getObject(i);
            if (value instanceof byte[]) {
              row.add(HexUtil.encodeHexStr((byte[]) value));
            } else if (value instanceof java.sql.Clob) {
              row.add(ObjectCastUtils.castToString(value));
            } else if (value instanceof java.sql.Blob) {
              byte[] bytes = ObjectCastUtils.castToByteArray(value);
              row.add(HexUtil.encodeHexStr(bytes));
            } else {
              row.add(null == value ? null : value.toString());
            }
          }
          data.getRows().add(row);
        }

        return data;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  protected void beforeExecuteQuery(Connection connection, String schema, String table) {
    // nothing except for hive
  }

}
