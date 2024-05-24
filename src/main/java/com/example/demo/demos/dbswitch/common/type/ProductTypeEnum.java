// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.common.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 数据库产品类型的枚举定义
 *
 * @author Tang
 */
@Getter
@AllArgsConstructor
public enum ProductTypeEnum {

  /**
   * MySQL数据库类型
   */
  MYSQL(1, "`", "MySQL", "com.mysql.jdbc.Driver", 3306,
      "/* ping */ SELECT 1",
      "jdbc:mysql://",
      new String[]{"jdbc:mysql://{host}[:{port}]/[{database}][\\?{params}]"},
      "jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&tinyInt1isBit=false&rewriteBatchedStatements=true&useCompression=true"),

  /**
   * MariaDB数据库类型
   */
  MARIADB(2, "`", "MariaDB", "org.mariadb.jdbc.Driver", 3306,
      "SELECT 1",
      "jdbc:mariadb://",
      new String[]{"jdbc:mariadb://{host}[:{port}]/[{database}][\\?{params}]"},
      "jdbc:mariadb://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&tinyInt1isBit=false&rewriteBatchedStatements=true&useCompression=true"),

  /**
   * Oracle数据库类型
   */
  ORACLE(3, "\"", "Oracle", "oracle.jdbc.driver.OracleDriver", 1521,
      "SELECT 'Hello' from DUAL",
      "jdbc:oracle:thin:@",
      new String[]{"jdbc:oracle:thin:@{host}:{port}:{database}",
          "jdbc:oracle:thin:@//{host}[:{port}]/{database}"},
      "jdbc:oracle:thin:@127.0.0.1:1521:ORCL"),

  /**
   * Microsoft SQL Server数据库类型(>=2005)
   */
  SQLSERVER(4, "\"", "SqlServer", "com.microsoft.sqlserver.jdbc.SQLServerDriver", 1433,
      "SELECT 1+2 as a",
      "jdbc:sqlserver://",
      new String[]{"jdbc:sqlserver://{host}[:{port}][;DatabaseName={database}][;{params}]"},
      "jdbc:sqlserver://127.0.0.1:1433;DatabaseName=test"),

  /**
   * PostgreSQL数据库类型
   */
  POSTGRESQL(5, "\"", "PostgreSql", "org.postgresql.Driver", 5432,
      "SELECT 1",
      "jdbc:postgresql://",
      new String[]{"jdbc:postgresql://{host}[:{port}]/[{database}][\\?{params}]"},
      "jdbc:postgresql://127.0.0.1:5432/test"),

  /**
   * DB2数据库类型
   */
  DB2(6, "\"", "DB2", "com.ibm.db2.jcc.DB2Driver", 50000,
      "SELECT 1 FROM SYSIBM.SYSDUMMY1",
      "jdbc:db2://",
      new String[]{"jdbc:db2://{host}:{port}/{database}[:{params}]"},
      "jdbc:db2://127.0.0.1:50000/testdb:driverType=4;fullyMaterializeLobData=true;fullyMaterializeInputStreams=true;progressiveStreaming=2;progresssiveLocators=2;"),

  /**
   * [国产] 达梦(DM)数据库类型
   */
  DM(7, "\"", "DM", "dm.jdbc.driver.DmDriver", 5236,
      "SELECT 'Hello' from DUAL",
      "jdbc:dm://",
      new String[]{"jdbc:dm://{host}:{port}[/{database}][\\?{params}]"},
      "jdbc:dm://127.0.0.1:5236"),

  /**
   * [国产] 金仓(Kingbase)数据库类型
   */
  KINGBASE(8, "\"", "KingBase", "com.kingbase8.Driver", 54321,
      "SELECT 1",
      "jdbc:kingbase8://",
      new String[]{"jdbc:kingbase8://{host}[:{port}]/[{database}][\\?{params}]"},
      "jdbc:kingbase8://127.0.0.1:54321/test"),

  /**
   * [国产] 神通(Oscar)数据库类型
   */
  OSCAR(9, "\"", "Oscar", "com.oscar.Driver", 2003,
      "SELECT 1",
      "jdbc:oscar://",
      new String[]{"jdbc:oscar://{host}[:{port}]/[{database}][\\?{params}]"},
      "jdbc:oscar://127.0.0.1:2003/OSCRDB"),

  /**
   * [国产] 南大通用(GBase8A)数据库类型
   */
  GBASE8A(10, "`", "GBase8A", "com.gbase.jdbc.Driver", 5258,
      "/* ping */ SELECT 1",
      "jdbc:gbase://",
      new String[]{"jdbc:gbase://{host}[:{port}]/[{database}][\\?{params}]"},
      "jdbc:gbase://127.0.0.1:5258/test"),

  /**
   * Highgo数据库类型:https://blog.csdn.net/weixin_39676699/article/details/134338598
   */
  HIGHGO(11, "\"", "HighGo", "com.highgo.jdbc.Driver", 5866,
      "SELECT 1",
      "jdbc:highgo://",
      new String[]{"jdbc:highgo://{host}[:{port}]/[{database}][\\?{params}]"},
      "jdbc:highgo://127.0.0.1:5866/highgo"),

  /**
   * Sybase 数据库类型
   */
  SYBASE(12, "\"", "Sybase", "com.sybase.jdbc4.jdbc.SybDriver", 5000,
      "SELECT 1+2 as a",
      "jdbc:sybase:Tds:",
      new String[]{"jdbc:sybase:Tds:{host}[:{port}][/{database}][\\?{params}]"},
      "jdbc:sybase:Tds:127.0.0.1:5000/test?charset=cp936"),

  /**
   * Hive 数据库类型
   */
  HIVE(13, "`", "Hive", "org.apache.hive.jdbc.HiveDriver", 10000,
      "SELECT 1",
      "jdbc:hive2://",
      new String[]{"jdbc:hive2://{host}[:{port}]/[{database}][\\?{params}]"},
      "jdbc:hive2://172.17.2.12:10000/default"),

  /**
   * Sqlite v3数据库类型
   */
  // 参考文章：https://blog.csdn.net/wank1259162/article/details/104946744
  SQLITE3(14, "\"", "Sqlite3", "org.sqlite.JDBC", 0,
      "SELECT 1",
      "jdbc:sqlite:",
      new String[]{"jdbc:sqlite:{file}", "jdbc:sqlite::resource:{file}"},
      "jdbc:sqlite:/tmp/test.db"),

  /**
   * OpenGauss数据库类型
   */
  OPENGAUSS(15, "\"", "OpenGauss", "org.opengauss.Driver", 15432,
      "SELECT 1",
      "jdbc:opengauss://",
      new String[]{"jdbc:opengauss://{host}[:{port}]/[{database}][\\?{params}]"},
      "jdbc:opengauss://127.0.0.1:5866/test"),

  /**
   * ClickHouse数据库类型
   */
  CLICKHOUSE(16, "`", "ClickHouse", "com.clickhouse.jdbc.ClickHouseDriver", 8123,
      "SELECT 1",
      "jdbc:clickhouse://",
      new String[]{"jdbc:clickhouse://{host}[:{port}]/[{database}][\\?{params}]"},
      "jdbc:clickhouse://127.0.0.1:8123/default"),

  /**
   * MongoDB数据库类型
   */
  MONGODB(17, "\"", "MongoDB", "com.gitee.jdbc.mongodb.JdbcDriver", 27017,
      "use admin;",
      "jdbc:mongodb://",
      new String[]{"jdbc:mongodb://{host}[:{port}]/[{database}][\\?{params}]"},
      "jdbc:mongodb://172.17.2.12:27017/admin?authSource=admin&authMechanism=SCRAM-SHA-1"),

  /**
   * ElasticSearch数据库类型
   */
  ELASTICSEARCH(18, "\"", "ElasticSearch", "com.gitee.jdbc.elasticsearch.JdbcDriver", 9200,
      "",
      "jdbc:jest://",
      new String[]{"jdbc:jest://{host}[:{port}][\\?{params}]"},
      "jdbc:jest://172.17.2.12:9200?useHttps=false"),

  /**
   * StarRocks数据库类型
   */
  STARROCKS(19, "`", "StarRocks", "com.mysql.cj.jdbc.Driver", 9030,
      "/* ping */ SELECT 1",
      "jdbc:mysql://",
      new String[]{"jdbc:mysql://{host}[:{port}]/[{database}][\\?{params}]"},
      "jdbc:mysql://127.0.0.1:9030/test?useUnicode=true&characterEncoding=utf-8&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&tinyInt1isBit=false&rewriteBatchedStatements=true&useCompression=true"),

  ;

  private int id;
  private String quote;
  private String name;
  private String driver;
  private int port;
  private String sql;
  private String urlPrefix;
  private String[] url;
  private String sample;

  public boolean hasDatabaseName() {
    return !Arrays.asList(DM, SQLITE3, MYSQL, MARIADB, GBASE8A, ELASTICSEARCH).contains(this);
  }

  public boolean hasFilePath() {
    return this == SQLITE3;
  }

  public boolean hasAddress() {
    return this != SQLITE3;
  }

  public boolean noCommentStatement() {
    return Arrays.asList(
        ProductTypeEnum.MYSQL,
        ProductTypeEnum.MARIADB,
        ProductTypeEnum.GBASE8A,
        ProductTypeEnum.HIVE,
        ProductTypeEnum.SQLITE3,
        ProductTypeEnum.SYBASE
    ).contains(this);
  }

  public String quoteName(String name) {
    return String.format("%s%s%s", quote, name, quote);
  }

  public String quoteSchemaTableName(String schema, String table) {
    return String.format("%s%s%s.%s%s%s", quote, schema, quote, quote, table, quote);
  }

  /**
   * 类似于PostgreSQL系列的数据库类型
   *
   * @return boolean
   */
  public boolean isLikePostgres() {
    return this == POSTGRESQL || this == KINGBASE || this == OPENGAUSS;
  }

  /**
   * 类似于Starrocks系列的数据库类型
   *
   * @return boolean
   */
  public boolean isLikeStarRocks() {
    return this == STARROCKS;
  }

  /**
   * 类似于MySQL系列的数据库类型
   *
   * @return boolean
   */
  public boolean isLikeMysql() {
    return this == MYSQL || this == MARIADB;
  }

  /**
   * 类似于MySQL系列的数据库类型
   *
   * @return boolean
   */
  public boolean isLikeGbase8a() {
    return this == GBASE8A;
  }

  /**
   * 类似于Oracle系列的数据库类型
   *
   * @return boolean
   */
  public boolean isLikeOracle() {
    return this == ORACLE || this == DM;
  }

  /**
   * 类似于SQL Server系列的数据库类型
   *
   * @return boolean
   */
  public boolean isLikeSqlServer() {
    return this == SQLSERVER || this == SYBASE;
  }

  /**
   * 类似于Hive系列的数据库类型
   *
   * @return boolean
   */
  public boolean isLikeHive() {
    return this == HIVE;
  }

  /**
   * 是否为MongoDB数据库类型
   *
   * @return boolean
   */
  public boolean isMongodb() {
    return this == MONGODB;
  }

  /**
   * 是否为ElasticSearch数据库类型
   *
   * @return boolean
   */
  public boolean isElasticSearch() {
    return this == ELASTICSEARCH;
  }

  /**
   * 是否为ClickHouse数据库类型
   *
   * @return boolean
   */
  public boolean isClickHouse() {
    return this == CLICKHOUSE;
  }

  /**
   * 是否存在指定字符串名称的数据库类型
   *
   * @param name 字符串名称
   * @return boolean
   */
  public static boolean exists(String name) {
    return Arrays.stream(values()).anyMatch(item -> item.name().equalsIgnoreCase(name));
  }

  /**
   * 将字符串名称转换为枚举值
   *
   * @param name 字符串名称
   * @return ProductTypeEnum
   */
  public static ProductTypeEnum of(String name) {
    if (!StringUtils.isEmpty(name)) {
      for (ProductTypeEnum type : ProductTypeEnum.values()) {
        if (type.getName().equalsIgnoreCase(name)) {
          return type;
        }
      }
    }

    throw new IllegalArgumentException("cannot find enum name: " + name);
  }

  /**
   * 针对SQLite数据库的URL连接串判断
   *
   * @param url SQLite数据库的URL连接串
   * @return boolean
   */
  public static boolean isUnsupportedTargetSqlite(String url) {
    String prefix1 = "jdbc:sqlite::resource:";
    //String prefix2 = "jdbc:sqlite::memory:";
    return url.startsWith(prefix1);
  }

}
