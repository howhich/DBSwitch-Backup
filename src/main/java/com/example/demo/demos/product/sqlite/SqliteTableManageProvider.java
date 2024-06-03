// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.product.sqlite;

import com.example.demo.demos.dbswitch.provider.ProductFactoryProvider;
import com.example.demo.demos.dbswitch.provider.manage.DefaultTableManageProvider;

public class SqliteTableManageProvider extends DefaultTableManageProvider {

  public SqliteTableManageProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public void truncateTableData(String schemaName, String tableName) {
    String sql = String.format("DELETE FROM \"%s\".\"%s\" ", schemaName, tableName);
    this.executeSql(sql);

    try {
      sql = String.format("DELETE FROM sqlite_sequence WHERE name = '%s' ", tableName);
      this.executeSql(sql);
    } catch (Exception e) {
      // ignore
    }

  }

  @Override
  public void dropTable(String schemaName, String tableName) {
    String sql = String.format("DROP TABLE \"%s\".\"%s\" ", schemaName, tableName);
    this.executeSql(sql);
  }

}
