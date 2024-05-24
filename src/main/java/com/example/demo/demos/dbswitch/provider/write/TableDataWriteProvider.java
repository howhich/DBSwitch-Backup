// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.provider.write;

import java.util.List;

public interface TableDataWriteProvider {

  /**
   * 批量写入预处理
   *
   * @param schemaName schema名称
   * @param tableName  table名称
   */
  void prepareWrite(String schemaName, String tableName, List<String> fieldNames);

  /**
   * 批量数据写入
   *
   * @param fieldNames   字段名称列表
   * @param recordValues 数据记录
   * @return 返回实际写入的数据记录条数
   */
  long write(List<String> fieldNames, List<Object[]> recordValues);
}
