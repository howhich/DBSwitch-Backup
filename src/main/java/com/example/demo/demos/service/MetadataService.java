// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.service;

import com.example.demo.demos.dbswitch.provider.meta.MetadataProvider;
import com.example.demo.demos.dbswitch.schema.*;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public interface MetadataService {

  /**
   * 资源释放
   */
  void close();

  /**
   * 获取数据源对象
   *
   * @return
   */
  DataSource getDataSource();

  /**
   * 获取数据库的schema模式列表
   *
   * @return
   */
  List<String> querySchemaList();

  /**
   * 获取指定Schema下所有的表列表
   *
   * @param schemaName 模式名称
   * @return
   */
  List<TableDescription> queryTableList(String schemaName);

  /**
   * 获取物理表的DDL建表语句
   *
   * @param schemaName 模式名称
   * @param tableName  表名称
   * @return
   */
  String getTableDDL(String schemaName, String tableName);

  /**
   * 获取物理表的注释
   *
   * @param schemaName 模式名称
   * @param tableName  表名称
   * @return
   */
  String getTableRemark(String schemaName, String tableName);

  /**
   * 获取物理表的DDL建表语句
   *
   * @param schemaName 模式名称
   * @param tableName  表名称
   * @return
   */
  String getViewDDL(String schemaName, String tableName);

  /**
   * 获取指定schema.table的字段名列表
   *
   * @param schemaName 模式名称
   * @param tableName  表或视图名称
   * @return
   */
  List<String> queryTableColumnName(String schemaName, String tableName);

  /**
   * 获取指定schema.table的表结构字段信息
   *
   * @param schemaName 模式名称
   * @param tableName  表或视图名称
   * @return
   */
  List<ColumnDescription> queryTableColumnMeta(String schemaName, String tableName);

  /**
   * 获取指定SQL结构字段信息
   *
   * @param querySql 查询的SQL语句
   * @return
   */
  List<ColumnDescription> querySqlColumnMeta(String querySql);

  /**
   * 获取表的主键信息字段列表
   *
   * @param schemaName
   * @param tableName
   * @return
   */
  List<String> queryTablePrimaryKeys(String schemaName, String tableName);

  /**
   * 获取表的索引列表
   *
   * @param schemaName
   * @param tableName
   * @return
   */
  List<IndexDescription> queryTableIndexes(String schemaName, String tableName);

  /**
   * 测试数据库SQL查询
   *
   * @param sql 待查询的SQL语句
   */
  void testQuerySQL(String sql);

  /**
   * 获取表的元数据
   *
   * @param schemaName 模式名称
   * @param tableName  表名称
   * @return
   */
  SchemaTableMeta queryTableMeta(String schemaName, String tableName);

  /**
   * 获取表的数据内容
   *
   * @param schemaName 模式名称
   * @param tableName  表名称
   * @param rowCount   记录总数
   * @return
   */
  SchemaTableData queryTableData(String schemaName, String tableName, int rowCount);

  /**
   * 根据字段结构信息组装对应数据库的建表DDL语句
   *
   * @param provider      目的数据库类型
   * @param fieldNames    字段结构信息
   * @param primaryKeys   主键字段信息
   * @param schemaName    模式名称
   * @param tableName     表名称
   * @param autoIncr      是否允许主键自增
   * @param tblProperties 表的属性信息
   * @return 对应数据库的DDL建表语句
   */
  List<String> getDDLCreateTableSQL(MetadataProvider provider, List<ColumnDescription> fieldNames,
      List<String> primaryKeys, String schemaName, String tableName, String tableRemarks,
      boolean autoIncr, Map<String, String> tblProperties);
}
