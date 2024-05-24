// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.provider.transform;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 字段值映射关系表
 */
public class ColumnValueDataMapTable {

  // <SchemaName,TableName,ColumnName> -> Map<String,String>
  private Map<SchemaTableColumnTuple, Map<String, String>> valueMap = new HashMap<>();

  public ColumnValueDataMapTable() {
  }

  public ColumnValueDataMapTable(SchemaTableColumnTuple tuple, Map<String, String> values) {
    valueMap.clear();
    valueMap.put(tuple, values);
  }

  private SchemaTableColumnTuple buildTuple(String schema, String table, String column) {
    return SchemaTableColumnTuple
        .builder()
        .schema(schema)
        .table(table)
        .column(column)
        .build();
  }

  public boolean isEmpty() {
    return valueMap.isEmpty();
  }

  public void put(SchemaTableColumnTuple tuple, Map<String, String> values) {
    valueMap.put(tuple, values);
  }

  public boolean contains(String schema, String table, String column) {
    return valueMap.containsKey(buildTuple(schema, table, column));
  }

  public Map<String, String> get(String schema, String table, String column) {
    Map<String, String> map = valueMap.get(buildTuple(schema, table, column));
    if (null == map) {
      map = Collections.emptyMap();
    }
    return map;
  }
}
