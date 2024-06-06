package com.example.demo.demos.controller;

import com.example.demo.demos.entity.TaskCron;
import com.example.demo.demos.entity.TaskReq;
import com.example.demo.demos.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {
    @Autowired
    private TaskService taskService;
    @GetMapping("/list")
    public List<TaskCron> list() {
        return taskService.getTasks();
    }
    @PostMapping("/add")
    public String add(@RequestBody TaskReq req) {
        return taskService.addTask(req);
    }
    @GetMapping("/stop/{id}")
    public String stop(@PathVariable("id") Long id) {
        return taskService.stopTask(id);
    }
    @GetMapping("/start/{id}")
    public String start(@PathVariable("id") Long id){
        return taskService.startTask(id);
    }
    @PostMapping("/update")
    public String update(@RequestBody TaskReq req){
        return taskService.updateTask(req);
    }

}
