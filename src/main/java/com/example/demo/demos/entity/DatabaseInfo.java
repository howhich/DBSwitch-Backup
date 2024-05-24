package com.example.demo.demos.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("database_info")
public class DatabaseInfo {
    @TableId(value="id",type = IdType.AUTO)
    private Integer id;
    private String dbName;
    private String dbPath;
    private Date lastBackupTime;
    private Integer deleted;
}
