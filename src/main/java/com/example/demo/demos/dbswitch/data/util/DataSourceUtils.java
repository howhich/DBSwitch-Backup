// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.data.util;

import cn.hutool.core.util.ClassLoaderUtil;
import com.example.demo.demos.dbswitch.common.entity.CloseableDataSource;
import com.example.demo.demos.dbswitch.common.entity.InvisibleDataSource;
import com.example.demo.demos.dbswitch.common.entity.JarClassLoader;
import com.example.demo.demos.dbswitch.common.util.ExamineUtils;
import com.example.demo.demos.dbswitch.data.domain.WrapCommonDataSource;
import com.example.demo.demos.dbswitch.data.domain.WrapHikariDataSource;
import com.example.demo.demos.dbswitch.data.entity.SourceDataSourceProperties;
import com.example.demo.demos.dbswitch.data.entity.TargetDataSourceProperties;
import com.zaxxer.hikari.HikariDataSource;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DataSource工具类
 *
 * @author tang
 */
@Slf4j
@UtilityClass
public final class  DataSourceUtils {

  public static final int MAX_THREAD_COUNT = 10;
  public static final int MAX_TIMEOUT_MS = 60000;

  private static final Map<String, URLClassLoader> classLoaderMap = new ConcurrentHashMap<>();

  /**
   * 创建于指定数据库连接描述符的连接池
   *
   * @param properties 数据库连接描述符
   * @return HikariDataSource连接池
   */
  public static CloseableDataSource createSourceDataSource(
      SourceDataSourceProperties properties) {
    Properties parameters = new Properties();
    HikariDataSource ds = new HikariDataSource();
    ds.setPoolName("The_Source_DB_Connection");
    ds.setJdbcUrl(properties.getUrl());
    if (properties.getDriverClassName().contains("oracle")) {
      ds.setConnectionTestQuery("SELECT 'Hello' from DUAL");
      // https://blog.csdn.net/qq_20960159/article/details/78593936
      System.getProperties().setProperty("oracle.jdbc.J2EE13Compliant", "true");
      // Oracle在通过jdbc连接的时候需要添加一个参数来设置是否获取注释
      parameters.put("remarksReporting", "true");
    } else if (properties.getDriverClassName().contains("db2")) {
      ds.setConnectionTestQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
    } else if (properties.getDriverClassName().contains("mongodb")) {
      ds.setConnectionTestQuery("use admin;");
    } else if (!ds.getJdbcUrl().contains("jdbc:jest://")) {
      ds.setConnectionTestQuery("SELECT 1");
    }
    ds.setMaximumPoolSize(MAX_THREAD_COUNT);
    ds.setMinimumIdle(MAX_THREAD_COUNT);
    ds.setMaxLifetime(properties.getMaxLifeTime());
    ds.setConnectionTimeout(properties.getConnectionTimeout());
    ds.setIdleTimeout(MAX_TIMEOUT_MS);

    URLClassLoader urlClassLoader = createURLClassLoader(
        properties.getDriverPath(),
        properties.getDriverClassName());
    InvisibleDataSource dataSource = createInvisibleDataSource(
        urlClassLoader,
        properties.getUrl(),
        properties.getDriverClassName(),
        properties.getUsername(),
        properties.getPassword(),
        parameters
    );
    ds.setDataSource(dataSource);

    return new WrapHikariDataSource(ds, urlClassLoader);
  }

  /**
   * 创建于指定数据库连接描述符的连接池
   *
   * @param properties 数据库连接描述符
   * @return HikariDataSource连接池
   */
  public static CloseableDataSource createTargetDataSource(
      TargetDataSourceProperties properties) {
    HikariDataSource ds = new HikariDataSource();
    ds.setPoolName("The_Target_DB_Connection");
    ds.setJdbcUrl(properties.getUrl());
    if (properties.getDriverClassName().contains("oracle")) {
      ds.setConnectionTestQuery("SELECT 'Hello' from DUAL");
    } else if (properties.getDriverClassName().contains("db2")) {
      ds.setConnectionTestQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
    } else if (properties.getDriverClassName().contains("mongodb")) {
      ds.setConnectionTestQuery("use admin;");
    } else if (!ds.getJdbcUrl().contains("jdbc:jest://")) {
      ds.setConnectionTestQuery("SELECT 1");
    }
    ds.setMaximumPoolSize(MAX_THREAD_COUNT);
    ds.setMinimumIdle(MAX_THREAD_COUNT);
    ds.setMaxLifetime(properties.getMaxLifeTime());
    ds.setConnectionTimeout(properties.getConnectionTimeout());
    ds.setIdleTimeout(MAX_TIMEOUT_MS);

    URLClassLoader urlClassLoader = createURLClassLoader(
        properties.getDriverPath(),
        properties.getDriverClassName());
    InvisibleDataSource dataSource = createInvisibleDataSource(
        urlClassLoader,
        properties.getUrl(),
        properties.getDriverClassName(),
        properties.getUsername(),
        properties.getPassword(),
        new Properties()
    );

    // 如果是Greenplum数据库，这里需要关闭会话的查询优化器
    if (properties.getDriverClassName().contains("postgresql")) {
      String versionString = executeStringReturnedSql(dataSource, "SELECT version()");
      if (Objects.nonNull(versionString) && versionString.contains("Greenplum")) {
        log.info(
            "#### Target database is Greenplum Cluster, Close Optimizer now: set optimizer to 'off' ");
        ds.setConnectionInitSql("set optimizer to 'off'");
      }
    }

    ds.setDataSource(dataSource);

    return new WrapHikariDataSource(ds, urlClassLoader);
  }

  public static CloseableDataSource createCommonDataSource(
      String jdbcUrl,
      String driverClass,
      String driverPath,
      String username,
      String password) {
    URLClassLoader urlClassLoader = createURLClassLoader(
        driverPath,
        driverClass);
    InvisibleDataSource dataSource = createInvisibleDataSource(
        urlClassLoader,
        jdbcUrl,
        driverClass,
        username,
        password,
        new Properties()
    );
    return new WrapCommonDataSource(dataSource, urlClassLoader);
  }

  public static boolean supportConcurrentWrite(TargetDataSourceProperties properties) {
    return !properties.getDriverClassName().contains("sqlite")
        && !properties.getUrl().contains("jdbc:sqlite:");
  }

  private static InvisibleDataSource createInvisibleDataSource(
      ClassLoader cl,
      String jdbcUrl,
      String driverClass,
      String username,
      String password,
      Properties properties) {
    return new InvisibleDataSource(
        cl,
        jdbcUrl,
        driverClass,
        username,
        password,
        properties);
  }

  private static URLClassLoader createURLClassLoader(
      String driverPath, String driverClass) {
    ExamineUtils.checkArgument(
        StringUtils.isNoneBlank(driverPath),
        "Invalid driver path,can not be empty!");
    ExamineUtils.checkArgument(
        StringUtils.isNoneBlank(driverClass),
        "Invalid driver class,can not be empty!");
    ClassLoader parent = driverClass.contains("postgresql")
        ? ClassLoaderUtil.getContextClassLoader()
        : ClassLoaderUtil.getSystemClassLoader().getParent();
    URLClassLoader loader = getOrCreateClassLoader(driverPath, parent);
    try {
      Class<?> clazz = loader.loadClass(driverClass);
      clazz.getConstructor().newInstance();
      return loader;
    } catch (Exception e) {
      log.error("Could not load class : {} from driver path: {}", driverClass, driverPath, e);
      throw new RuntimeException(e);
    }
  }

  private static URLClassLoader getOrCreateClassLoader(
      String path, ClassLoader parent) {
    URLClassLoader urlClassLoader = classLoaderMap.get(path);
    if (null == urlClassLoader) {
      synchronized (DataSourceUtils.class) {
        urlClassLoader = classLoaderMap.get(path);
        if (null == urlClassLoader) {
          log.info("Create jar classLoader from path: {}", path);
          urlClassLoader = new JarClassLoader(path, parent);
          classLoaderMap.put(path, urlClassLoader);
        }
      }
    }
    return urlClassLoader;
  }

  private static String executeStringReturnedSql(
      DataSource dataSource, String sql) {
    try (Connection connection = dataSource.getConnection()) {
      try (Statement statement = connection.createStatement()) {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
          if (resultSet.next()) {
            return resultSet.getString(1);
          }
          return null;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
