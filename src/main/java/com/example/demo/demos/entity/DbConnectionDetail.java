package com.example.demo.demos.entity;

import lombok.Data;

import java.sql.Date;

@Data
public class DbConnectionDetail {
    private Long id;
    private String name;
    private String version;
    private String driver;
    private String address;
    private String port;
    private String databaseName;
    private String url;
    private String username;
    private String password;
    private Date createTime;
    private Date updateTime;

}
