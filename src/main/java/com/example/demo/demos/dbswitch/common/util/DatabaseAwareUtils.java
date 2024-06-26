// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.common.util;

import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库类型识别工具类
 *
 * @author tang
 */
@Slf4j
@UtilityClass
public final class DatabaseAwareUtils {

  private static final Map<String, ProductTypeEnum> productNameMap;

  private static final Map<String, ProductTypeEnum> driverNameMap;

  static {
    productNameMap = new HashMap<>();
    driverNameMap = new HashMap<>();

    productNameMap.put("Microsoft SQL Server", ProductTypeEnum.SQLSERVER);
    productNameMap.put("DM DBMS", ProductTypeEnum.DM);
    productNameMap.put("KingbaseES", ProductTypeEnum.KINGBASE);
    productNameMap.put("Apache Hive", ProductTypeEnum.HIVE);
    productNameMap.put("MySQL", ProductTypeEnum.MYSQL);
    productNameMap.put("MariaDB", ProductTypeEnum.MARIADB);
    productNameMap.put("Oracle", ProductTypeEnum.ORACLE);
    productNameMap.put("PostgreSQL", ProductTypeEnum.POSTGRESQL);
    productNameMap.put("Highgo", ProductTypeEnum.HIGHGO);
    productNameMap.put("DB2 for Unix/Windows", ProductTypeEnum.DB2);
    productNameMap.put("Hive", ProductTypeEnum.HIVE);
    productNameMap.put("SQLite", ProductTypeEnum.SQLITE3);
    productNameMap.put("OSCAR", ProductTypeEnum.OSCAR);
    productNameMap.put("GBase", ProductTypeEnum.GBASE8A);
    productNameMap.put("Adaptive Server Enterprise", ProductTypeEnum.SYBASE);
    productNameMap.put("ClickHouse", ProductTypeEnum.CLICKHOUSE);

    driverNameMap.put("MySQL Connector Java", ProductTypeEnum.MYSQL);
    driverNameMap.put("MariaDB Connector/J", ProductTypeEnum.MARIADB);
    driverNameMap.put("Oracle JDBC driver", ProductTypeEnum.ORACLE);
    driverNameMap.put("PostgreSQL JDBC Driver", ProductTypeEnum.POSTGRESQL);
    driverNameMap.put("Kingbase8 JDBC Driver", ProductTypeEnum.KINGBASE);
    driverNameMap.put("IBM Data Server Driver for JDBC and SQLJ", ProductTypeEnum.DB2);
    driverNameMap.put("dm.jdbc.driver.DmDriver", ProductTypeEnum.DM);
    driverNameMap.put("Hive JDBC", ProductTypeEnum.HIVE);
    driverNameMap.put("SQLite JDBC", ProductTypeEnum.SQLITE3);
    driverNameMap.put("OSCAR JDBC DRIVER", ProductTypeEnum.OSCAR);
    driverNameMap.put("GBase JDBC Driver", ProductTypeEnum.GBASE8A);
    driverNameMap.put("jConnect (TM) for JDBC (TM)", ProductTypeEnum.SYBASE);
    driverNameMap.put("ClickHouse JDBC Driver", ProductTypeEnum.CLICKHOUSE);
  }

  /**
   * 获取数据库的产品名
   *
   * @param dataSource 数据源
   * @return 数据库产品名称字符串
   */
  public static ProductTypeEnum getProductTypeByDataSource(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection()) {
      String productName = connection.getMetaData().getDatabaseProductName();
      String driverName = connection.getMetaData().getDriverName();
      if (driverNameMap.containsKey(driverName)) {
        ProductTypeEnum productType = driverNameMap.get(driverName);
        if (productType == ProductTypeEnum.POSTGRESQL) {
          String url = connection.getMetaData().getURL();
          if (null != url && url.contains("jdbc:opengauss:")) {
            return ProductTypeEnum.OPENGAUSS;
          }
          if (null != url && url.contains("jdbc:highgo:")) {
            return ProductTypeEnum.HIGHGO;
          }
        }
        return productType;
      }
      boolean haveStarRocks = false;
      try {
        // 此查询语句是Starrocks查询be节点是否存活，可以用来判断是否是Starrocks数据源
        haveStarRocks = connection.createStatement().execute("show backends");
      } catch (Exception sqlException) {
        log.info("Failed to execute sql :show backends, so guesses it is mysql datasource!");
      }
      if (haveStarRocks) {
        return ProductTypeEnum.STARROCKS;
      }
      ProductTypeEnum type = productNameMap.get(productName);
      if (null != type) {
        return type;
      }
      String url = connection.getMetaData().getURL();
      if (null != url && url.contains("mongodb://")) {
        return ProductTypeEnum.MONGODB;
      }
      if (null != url && url.contains("jest://")) {
        return ProductTypeEnum.ELASTICSEARCH;
      }
      throw new IllegalStateException("Unable to detect database type from data source instance");
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  /**
   * 检查MySQL数据库表的存储引擎是否为Innodb
   *
   * @param schemaName schema名
   * @param tableName  table名
   * @param dataSource 数据源
   * @return 为Innodb存储引擎时返回True, 否在为false
   */
  public static boolean isMysqlInnodbStorageEngine(String schemaName, String tableName,
      DataSource dataSource) {
    String sql = "SELECT count(*) as total FROM information_schema.tables "
        + "WHERE table_schema=? AND table_name=? AND ENGINE='InnoDB'";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, schemaName);
      ps.setString(2, tableName);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0;
        }
      }

      return false;
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

}
