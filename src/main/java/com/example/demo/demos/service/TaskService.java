package com.example.demo.demos.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.demos.entity.TaskCron;

import java.util.List;

public interface TaskService extends IService<TaskCron> {

    List<TaskCron> getTasks();

    String addTask(Long id);

    String stopTask(Long id);

    String startTask(Long id);

    String updateTask(TaskCron taskCron);
}
