// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.common.type;

/**
 * 数据库表类型:视图表、物理表
 *
 * @author tang
 */
public enum ProductTableEnum {
  /**
   * 物理表
   */
  TABLE(0),

  /**
   * 视图表
   */
  VIEW(1);

  private int index;

  ProductTableEnum(int idx) {
    this.index = idx;
  }

  public int getIndex() {
    return index;
  }
}
