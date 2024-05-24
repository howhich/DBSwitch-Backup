package com.example.demo.demos.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.demos.entity.DatabaseConnectionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DbConnectionMapper extends BaseMapper<DatabaseConnectionEntity> {
}
