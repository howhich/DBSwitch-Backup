// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.provider.manage;

public interface TableManageProvider {

  /**
   * 清除指定表的所有数据
   *
   * @param schemaName 模式名称
   * @param tableName  表名称
   */
  void truncateTableData(String schemaName, String tableName);

  /**
   * 删除指定物理表
   *
   * @param schemaName 模式名称
   * @param tableName  表名称
   */
  void dropTable(String schemaName, String tableName);
}
