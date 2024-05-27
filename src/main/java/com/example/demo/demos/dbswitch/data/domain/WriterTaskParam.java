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

import com.example.demo.demos.dbswitch.core.exchange.MemChannel;
import com.example.demo.demos.dbswitch.core.robot.RobotReader;
import com.example.demo.demos.dbswitch.core.task.TaskParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 写入任务线程的执行结果
 *
 * @author tang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WriterTaskParam implements TaskParam {

  private MemChannel memChannel;
  private RobotReader robotReader;
  private boolean concurrentWrite;
}
