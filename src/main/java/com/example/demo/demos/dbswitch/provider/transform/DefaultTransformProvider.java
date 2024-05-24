// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.provider.transform;

import com.example.demo.demos.dbswitch.provider.ProductFactoryProvider;

import java.util.List;

/**
 * 默认的转换器(无转换)
 */
public class DefaultTransformProvider implements RecordTransformProvider {

  private ProductFactoryProvider factoryProvider;

  public DefaultTransformProvider(ProductFactoryProvider factoryProvider) {
    this.factoryProvider = factoryProvider;
  }

  protected ProductFactoryProvider getFactoryProvider() {
    return this.factoryProvider;
  }

  @Override
  public String getTransformerName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public Object[] doTransform(String schema, String table, List<String> fieldNames, Object[] recordValue) {
    return recordValue;
  }

}
