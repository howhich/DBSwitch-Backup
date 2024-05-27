// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.data.domain;

import com.example.demo.demos.dbswitch.common.entity.CloseableDataSource;
import com.example.demo.demos.dbswitch.common.entity.InvisibleDataSource;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.logging.Logger;

@Slf4j
public class WrapCommonDataSource implements CloseableDataSource {

  private InvisibleDataSource commonDataSource;
  private URLClassLoader urlClassLoader;

  public WrapCommonDataSource(InvisibleDataSource commonDataSource, URLClassLoader urlClassLoader) {
    this.commonDataSource = Objects.requireNonNull(commonDataSource);
    this.urlClassLoader = Objects.requireNonNull(urlClassLoader);
  }

  @Override
  public String getJdbcUrl() {
    return commonDataSource.getJdbcUrl();
  }

  @Override
  public String getDriverClass() {
    return commonDataSource.getDriverClassName();
  }

  @Override
  public String getUserName() {
    return commonDataSource.getProperties().getProperty("user");
  }

  @Override
  public String getPassword() {
    return commonDataSource.getProperties().getProperty("password");
  }

  @Override
  public Connection getConnection() throws SQLException {
    return commonDataSource.getConnection();
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return commonDataSource.getConnection(username, password);
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return commonDataSource.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return commonDataSource.isWrapperFor(iface);
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return commonDataSource.getLogWriter();
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    commonDataSource.setLogWriter(out);
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    commonDataSource.setLoginTimeout(seconds);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return commonDataSource.getLoginTimeout();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return commonDataSource.getParentLogger();
  }

  public void close() {
  }
}
