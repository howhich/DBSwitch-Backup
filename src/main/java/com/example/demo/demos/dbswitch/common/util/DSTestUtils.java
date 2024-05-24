package com.example.demo.demos.dbswitch.common.util;

import com.example.demo.demos.entity.DatabaseConnectionEntity;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static com.example.demo.demos.dbswitch.common.util.DSUtils.createCommonDataSource;
@Slf4j
public class DSTestUtils {
    public static Boolean MysqlTest(DatabaseConnectionEntity databaseConnectionEntity, String driverPath) {
        Boolean result = false;
        DataSource dataSource = createCommonDataSource(databaseConnectionEntity.getUrl(),
                databaseConnectionEntity.getDriver(),
                driverPath,
                databaseConnectionEntity.getUsername(),
                databaseConnectionEntity.getPassword());
        try {
            Connection connection = dataSource.getConnection();
            String sql = databaseConnectionEntity.getType().getSql();
            result = connection.createStatement().execute(sql);
            log.info("EXECUTING TEST SQL:{}",sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
