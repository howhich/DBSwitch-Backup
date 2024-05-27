// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.data.domain;

import com.example.demo.demos.dbswitch.common.entity.PrintablePerfStat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 并发写入统计格式化信息
 *
 * @author tang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WriterPerfStat extends PrintablePerfStat {

  private long duration;

  @Override
  public String getPrintableString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Total Writer Duration: \t" + (duration / 1000.0) + " s \n");
    return sb.toString();
  }
}
