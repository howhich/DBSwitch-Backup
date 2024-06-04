package com.example.demo.demos.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class TaskCron {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String cron;
    private String taskName;
    private String taskStatus;
    private Long datasourceId;
    @TableField(value = "last_execute_time", insertStrategy = FieldStrategy.DEFAULT, updateStrategy = FieldStrategy.DEFAULT)
    private Timestamp lastExecuteTime;
}
