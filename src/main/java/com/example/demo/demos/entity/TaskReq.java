package com.example.demo.demos.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class TaskReq {
    private Long id;
    private String cron;
    private String taskName;
    private Long datasourceId;
    private String schemaName;
}
