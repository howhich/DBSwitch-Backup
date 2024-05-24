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

import java.util.List;

/**
 * 值映射转换器接口
 */
public interface RecordTransformProvider {

  String getTransformerName();

  Object[] doTransform(String schema, String table, List<String> fieldNames, Object[] recordValue);
}
