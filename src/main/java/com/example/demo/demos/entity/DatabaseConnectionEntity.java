// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.entity;

import com.baomidou.mybatisplus.annotation.*;
//import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.EnumTypeHandler;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "DBSWITCH_DATABASE_CONNECTION", autoResultMap = true)
public class DatabaseConnectionEntity {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  @TableField("name")
  private String name;

  @TableField(value = "type", typeHandler = EnumTypeHandler.class)
  private ProductTypeEnum type;

  @TableField("version")
  private String version;

  @TableField("driver")
  private String driver;

  @TableField("mode")
  private Integer mode;

  @TableField("address")
  private String address;

  @TableField("port")
  private String port;

  @TableField("database_name")
  private String databaseName;

  @TableField("character_encoding")
  private String characterEncoding;

  @TableField("url")
  private String url;

  @TableField("username")
  private String username;

  @TableField("password")
  private String password;

  @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
  private Timestamp createTime;

  @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
  private Timestamp updateTime;
}
