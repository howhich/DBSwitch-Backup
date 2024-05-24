// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.features;

/**
 * 数据库特征定义
 */
public interface ProductFeatures {

  /**
   * FetchSize转换
   *
   * @param fetchSize
   * @return 转换后的fetchSize
   */
  default int convertFetchSize(int fetchSize) {
    return fetchSize;
  }

}
