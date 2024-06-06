package com.example.demo.demos.controller;

import com.example.demo.demos.dbswitch.common.entity.DbConnectionCreateRequest;
import com.example.demo.demos.entity.BackupReqVO;
import com.example.demo.demos.entity.DatabaseConnectionEntity;
import com.example.demo.demos.service.DbConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/datasource")
public class DataSourceController {
    @Autowired
    private DbConnectionService dbConnectionService;
    @GetMapping("/list")
    public List<DatabaseConnectionEntity> list() {
        return dbConnectionService.getAllConnections();
    }
    @GetMapping("/test/{id}")
    public String test(@PathVariable("id") Long id) throws SQLException {
        return dbConnectionService.test(id);
    }
    @PostMapping("/add")
    public String add(@RequestBody DatabaseConnectionEntity databaseConnectionEntity) {
        dbConnectionService.addDataSource(databaseConnectionEntity);
        return "success";
    }
    @PostMapping("/preTest")
    public String preTest(@RequestBody DbConnectionCreateRequest request) {
        dbConnectionService.preTest(request);
        return "success";
    }
    @PostMapping("/backup")
    public String backUp(@RequestBody BackupReqVO reqVO){
        dbConnectionService.backUp(reqVO);
        return "success";
    }
    @GetMapping("/getSchemas/{id}")
    public List<String> getSchemas(@PathVariable("id") Long id){
        return dbConnectionService.getSchemas(id);
    }

    @GetMapping("/clear")
    public String clear() {
        dbConnectionService.clear();
        return "success";
    }
}
