package com.example.demo.demos.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.demos.entity.DatabaseConnectionEntity;
import com.example.demo.demos.entity.TaskCron;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<TaskCron> {
}
