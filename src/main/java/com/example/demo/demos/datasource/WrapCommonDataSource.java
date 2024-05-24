package com.example.demo.demos.datasource;

import com.example.demo.demos.dbswitch.common.entity.CloseableDataSource;
import com.example.demo.demos.dbswitch.common.entity.InvisibleDataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.logging.Logger;

public class WrapCommonDataSource implements CloseableDataSource {

    private InvisibleDataSource commonDataSource;
    private URLClassLoader urlClassLoader;

    public WrapCommonDataSource(InvisibleDataSource commonDataSource, URLClassLoader urlClassLoader) {
        this.commonDataSource = Objects.requireNonNull(commonDataSource);
        this.urlClassLoader = Objects.requireNonNull(urlClassLoader);
    }

    @Override
    public String getJdbcUrl() {
        return null;
    }

    @Override
    public String getDriverClass() {
        return null;
    }

    @Override
    public String getUserName() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public Connection getConnection() throws SQLException {
        return commonDataSource.getConnection();
//        return null;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
