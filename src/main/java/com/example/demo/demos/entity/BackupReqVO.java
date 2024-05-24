package com.example.demo.demos.entity;

import lombok.Data;

@Data
public class BackupReqVO {
    private String dbName;
    private String backupPath;
}
