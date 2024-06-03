// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.product.sqlite;

import com.example.demo.demos.dbswitch.common.consts.Constants;
import com.example.demo.demos.dbswitch.common.util.DDLFormatterUtils;
import com.example.demo.demos.dbswitch.provider.ProductFactoryProvider;
import com.example.demo.demos.dbswitch.provider.meta.AbstractMetadataProvider;
import com.example.demo.demos.dbswitch.schema.ColumnDescription;
import com.example.demo.demos.dbswitch.schema.ColumnMetaData;
import com.example.demo.demos.dbswitch.schema.TableDescription;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.Collections;
import java.util.List;

@Slf4j
public class SqliteMetadataQueryProvider extends AbstractMetadataProvider {

  private static final String SHOW_CREATE_TABLE_SQL =
      "SELECT sql FROM \"sqlite_master\" where type='table' and tbl_name=? ";
  private static final String SHOW_CREATE_VIEW_SQL =
      "SELECT sql FROM \"sqlite_master\" where type='view' and tbl_name=? ";

  public SqliteMetadataQueryProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public List<String> querySchemaList(Connection connection) {
    return Collections.singletonList("main");
  }

  @Override
  public String getTableDDL(Connection connection, String schemaName, String tableName) {
    try (PreparedStatement ps = connection.prepareStatement(SHOW_CREATE_TABLE_SQL)) {
      ps.setString(1, tableName);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs != null && rs.next()) {
          return DDLFormatterUtils.format(rs.getString(1));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return "";
  }

  @Override
  public String getViewDDL(Connection connection, String schemaName, String tableName) {
    try (PreparedStatement ps = connection.prepareStatement(SHOW_CREATE_VIEW_SQL)) {
      ps.setString(1, tableName);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs != null && rs.next()) {
          return DDLFormatterUtils.format(rs.getString(1));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return "";
  }

  @Override
  public List<ColumnDescription> querySelectSqlColumnMeta(Connection connection, String sql) {
    String querySQL = String.format(" %s LIMIT 0 ", sql.replace(";", ""));
    return this.getSelectSqlColumnMeta(connection, querySQL);
  }

  @Override
  protected String getTableFieldsQuerySQL(String schemaName, String tableName) {
    return String.format("SELECT * FROM \"%s\".\"%s\"  ", schemaName, tableName);
  }

  @Override
  public void testQuerySQL(Connection connection, String sql) {
    String testQuerySql = String.format("explain %s", sql.replace(";", ""));
    if (log.isDebugEnabled()) {
      log.debug("Execute sql :{}", testQuerySql);
    }
    try (Statement st = connection.createStatement()) {
      st.execute(testQuerySql);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc,
      boolean addCr, boolean withRemarks) {
    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();
    int type = v.getType();

    String retval = " \"" + fieldname + "\"   ";

    switch (type) {
      case ColumnMetaData.TYPE_TIMESTAMP:
      case ColumnMetaData.TYPE_TIME:
      case ColumnMetaData.TYPE_DATE:
        // sqlite中没有时间数据类型
        retval += "DATETIME";
        break;
      case ColumnMetaData.TYPE_BOOLEAN:
        retval += "CHAR(1)";
        break;
      case ColumnMetaData.TYPE_NUMBER:
      case ColumnMetaData.TYPE_INTEGER:
      case ColumnMetaData.TYPE_BIGNUMBER:
        if (null != pks && !pks.isEmpty() && pks.contains(fieldname)) {
          // 关键字 AUTOINCREMENT 只能⽤于整型（INTEGER）字段。
          if (useAutoInc) {
            retval += "INTEGER AUTOINCREMENT";
          } else {
            retval += "BIGINT ";
          }
        } else {
          if (precision != 0 || length < 0 || length > 18) {
            retval += "NUMERIC";
          } else {
            retval += "INTEGER";
          }
        }
        break;
      case ColumnMetaData.TYPE_STRING:
        if (length < 1 || length >= Constants.CLOB_LENGTH) {
          retval += "TEXT";
        } else {
          if (length <= 2000) {
            retval += "VARCHAR(" + length + ")";
          } else {
            retval += "TEXT";
          }
        }
        break;
      case ColumnMetaData.TYPE_BINARY:
        retval += "BLOB";
        break;
      default:
        retval += "TEXT";
        break;
    }

    if (addCr) {
      retval += Constants.CR;
    }

    return retval;
  }

  @Override
  public List<String> getTableColumnCommentDefinition(TableDescription td,
      List<ColumnDescription> cds) {
    return Collections.emptyList();
  }


}
