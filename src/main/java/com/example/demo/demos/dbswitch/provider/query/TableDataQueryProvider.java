// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.provider.query;

import com.example.demo.demos.dbswitch.common.entity.ResultSetWrapper;
import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.dbswitch.schema.SchemaTableData;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;

/**
 * 表数据查询
 */
public interface TableDataQueryProvider {

  /**
   * 获取数据库类型
   *
   * @return ProductTypeEnum
   */
  ProductTypeEnum getProductType();

  /**
   * 获取读取(fetch)数据的批次大小
   *
   * @return 批次大小
   */
  int getQueryFetchSize();

  /**
   * 设置读取(fetch)数据的批次大小
   *
   * @param size 批次大小
   */
  void setQueryFetchSize(int size);

  /**
   * 获取指定schema下表的结果集
   *
   * @param schemaName 模式名称
   * @param tableName  表名称
   * @param fields     字段列表
   * @return 结果集包装对象
   */
  default ResultSetWrapper queryTableData(String schemaName, String tableName, List<String> fields) {
    return queryTableData(schemaName, tableName, fields, Collections.emptyList());
  }

  /**
   * 获取指定schema下表的按主键有序的结果集
   *
   * @param schemaName 模式名称
   * @param tableName  表名称
   * @param fields     字段列表
   * @param orders     排序字段列表
   * @return 结果集包装对象
   */
  ResultSetWrapper queryTableData(String schemaName, String tableName, List<String> fields,
      List<String> orders);

  /**
   * 获取指定模式表内的数据
   *
   * @param connection JDBC连接
   * @param schemaName 模式名称
   * @param tableName  表名称
   * @param rowCount   记录的行数
   * @return 数据内容
   */
  SchemaTableData queryTableData(Connection connection, String schemaName, String tableName, int rowCount);
}
