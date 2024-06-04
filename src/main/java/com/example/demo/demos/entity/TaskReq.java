package com.example.demo.demos.entity;

import lombok.Data;

@Data
public class TaskReq {
    private Long dataSourceId;
    private String taskName;
    private String cron;
    private String taskDesc;
}
