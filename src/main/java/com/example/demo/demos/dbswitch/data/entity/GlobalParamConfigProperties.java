// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.data.entity;

import lombok.Data;

/**
 * 全局参数配置
 *
 * @author tang
 */
@Data
public class GlobalParamConfigProperties {

  private int channelQueueSize;

  private int writeThreadNum;

  public GlobalParamConfigProperties() {
    this.channelQueueSize = 100;
    this.writeThreadNum = getDefaultWriteThreadNum();
  }

  private int getDefaultWriteThreadNum() {
    int availableProcessorCount = Runtime.getRuntime().availableProcessors();
    return Math.min(Math.max(4, availableProcessorCount), 8);
  }

}
