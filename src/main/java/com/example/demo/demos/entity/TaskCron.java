package com.example.demo.demos.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class TaskCron {
    @TableId
    private Long id;
    private String cron;
    private String taskName;
}
