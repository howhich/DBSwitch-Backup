package com.example.demo.demos.entity;

import lombok.Data;

@Data
public class TaskReq {
    private Long id;
    private String taskName;
    private String cron;
}
