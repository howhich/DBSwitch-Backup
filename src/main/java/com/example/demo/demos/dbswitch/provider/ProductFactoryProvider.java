// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.provider;

import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.dbswitch.features.ProductFeatures;
import com.example.demo.demos.dbswitch.provider.manage.DefaultTableManageProvider;
import com.example.demo.demos.dbswitch.provider.manage.TableManageProvider;
import com.example.demo.demos.dbswitch.provider.meta.MetadataProvider;
import com.example.demo.demos.dbswitch.provider.query.DefaultTableDataQueryProvider;
import com.example.demo.demos.dbswitch.provider.query.TableDataQueryProvider;
import com.example.demo.demos.dbswitch.provider.sync.DefaultTableDataSynchronizeProvider;
import com.example.demo.demos.dbswitch.provider.sync.TableDataSynchronizeProvider;
import com.example.demo.demos.dbswitch.provider.transform.MappedTransformProvider;
import com.example.demo.demos.dbswitch.provider.transform.RecordTransformProvider;
import com.example.demo.demos.dbswitch.provider.write.DefaultTableDataWriteProvider;
import com.example.demo.demos.dbswitch.provider.write.TableDataWriteProvider;

import javax.sql.DataSource;

public interface ProductFactoryProvider {

  /**
   * 获取数据库类型
   *
   * @return ProductTypeEnum
   */
  ProductTypeEnum getProductType();

  /**
   * 获取数据源
   *
   * @return DataSource
   */
  DataSource getDataSource();

  /**
   * 获取数据库特征
   *
   * @return ProductFeatures
   */
  ProductFeatures getProductFeatures();

  /**
   * 获取元数据查询Provider
   *
   * @return MetadataQueryProvider
   */
  MetadataProvider createMetadataQueryProvider();

  /**
   * 获取表数据查询Provider
   *
   * @return TableDataQueryProvider
   */
  default TableDataQueryProvider createTableDataQueryProvider() {
    return new DefaultTableDataQueryProvider(this);
  }

  /**
   * 获取记录转换Provider
   *
   * @return RecordTransformProvider
   */
  default RecordTransformProvider createRecordTransformProvider() {
    return new MappedTransformProvider(this);
  }

  /**
   * 获取表批量写入Provider
   *
   * @param useInsert 是否使用insert写入(只对PG有效)
   * @return TableWriteProvider
   */
  default TableDataWriteProvider createTableDataWriteProvider(boolean useInsert) {
    return new DefaultTableDataWriteProvider(this);
  }

  /**
   * 获取表操作Provider
   *
   * @return TableManageProvider
   */
  default TableManageProvider createTableManageProvider() {
    return new DefaultTableManageProvider(this);
  }

  /**
   * 获取数据同步Provider
   *
   * @return TableDataSynchronizeProvider
   */
  default TableDataSynchronizeProvider createTableDataSynchronizeProvider() {
    return new DefaultTableDataSynchronizeProvider(this);
  }

}
