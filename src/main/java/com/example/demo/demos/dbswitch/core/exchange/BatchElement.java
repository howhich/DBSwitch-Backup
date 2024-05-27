// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.core.exchange;

import com.example.demo.demos.dbswitch.common.entity.ThreeArgsFunction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 队列中的批元素结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchElement {

  /**
   * 表名映射字符串，形如 A --> B 的形式
   */
  private String tableNameMapString;

  /**
   * 数据写入回调函数
   */
  private ThreeArgsFunction<List<String>, List<Object[]>, org.slf4j.Logger, Long> handler;

  /**
   * 写入回调函数的第1个参数
   */
  private List<String> arg1;

  /**
   * 写入回调函数的第2个参数
   */
  private List<Object[]> arg2;
}
