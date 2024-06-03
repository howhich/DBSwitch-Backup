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

import com.example.demo.demos.dbswitch.annotation.Product;
import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.dbswitch.features.ProductFeatures;
import com.example.demo.demos.dbswitch.provider.AbstractFactoryProvider;
import com.example.demo.demos.dbswitch.provider.manage.TableManageProvider;
import com.example.demo.demos.dbswitch.provider.meta.MetadataProvider;
import com.example.demo.demos.dbswitch.provider.sync.TableDataSynchronizeProvider;
import com.example.demo.demos.dbswitch.provider.write.TableDataWriteProvider;

import javax.sql.DataSource;

@Product(ProductTypeEnum.SQLITE3)
public class SqliteFactoryProvider extends AbstractFactoryProvider {

  public SqliteFactoryProvider(DataSource dataSource) {
    super(dataSource);
  }

  public ProductFeatures getProductFeatures() {
    return new SqliteFeatures();
  }

  @Override
  public MetadataProvider createMetadataQueryProvider() {
    return new SqliteMetadataQueryProvider(this);
  }

  @Override
  public TableDataWriteProvider createTableDataWriteProvider(boolean useInsert) {
    return new SqliteTableDataWriteProvider(this);
  }

  @Override
  public TableManageProvider createTableManageProvider() {
    return new SqliteTableManageProvider(this);
  }

  @Override
  public TableDataSynchronizeProvider createTableDataSynchronizeProvider() {
    return new SqliteTableSynchronizer(this);
  }

}
