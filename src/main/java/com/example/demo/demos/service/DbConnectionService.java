package com.example.demo.demos.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.demos.dbswitch.common.entity.DbConnectionCreateRequest;
import com.example.demo.demos.entity.BackupReqVO;
import com.example.demo.demos.entity.DatabaseConnectionEntity;

import java.sql.SQLException;
import java.util.List;

public interface DbConnectionService extends IService<DatabaseConnectionEntity> {

    String test(Long id) throws SQLException;

    List<DatabaseConnectionEntity> getAllConnections();

    String preTest(DbConnectionCreateRequest request);

    String addDataSource(DatabaseConnectionEntity databaseConnectionEntity);

    void backUp(BackupReqVO reqVO);

    List<String> getSchemas(Long id);
}
