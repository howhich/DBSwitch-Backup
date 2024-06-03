// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.handler;

import cn.hutool.core.date.StopWatch;
import com.example.demo.demos.dbswitch.core.exchange.BatchElement;
import com.example.demo.demos.dbswitch.core.exchange.MemChannel;
import com.example.demo.demos.dbswitch.core.robot.RobotReader;
import com.example.demo.demos.dbswitch.core.task.TaskProcessor;
import com.example.demo.demos.dbswitch.data.domain.WriterTaskParam;
import com.example.demo.demos.dbswitch.data.domain.WriterTaskResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * 数据写入线程体（多个表的写）
 *
 * @author tang
 */
@Slf4j
public class WriterTaskThread extends TaskProcessor<WriterTaskResult> {

  private final WriterTaskParam taskParam;

  public WriterTaskThread(WriterTaskParam taskParam) {
    this.taskParam = taskParam;
  }

  @Override
  protected WriterTaskResult doProcess() {
    MemChannel memChannel = this.taskParam.getMemChannel();
    RobotReader robotReader = this.taskParam.getRobotReader();
    WriterTaskResult taskResult = WriterTaskResult.builder().success(true).build();
    StopWatch stopWatch = new StopWatch(Thread.currentThread().getName());
    stopWatch.start();

    try {
      BatchElement elem;
      while (!memChannel.isEmpty() || robotReader.getRemainingCount() > 0) {
        elem = memChannel.poll();
        if (Thread.currentThread().isInterrupted()) {
          break;
        }
        if (null != elem) {
          try {
            Long ret = Long.valueOf(elem.getArg2().size());
            if (this.taskParam.isConcurrentWrite()) {
              elem.getHandler().apply(elem.getArg1(), elem.getArg2(), log);
            } else {
              synchronized (this.taskParam) {
                elem.getHandler().apply(elem.getArg1(), elem.getArg2(), log);
              }
            }
            Long count = taskResult.getPerf().get(elem.getTableNameMapString());
            Long total = ret + Optional.ofNullable(count).orElse(0L);
            taskResult.getPerf().put(elem.getTableNameMapString(), total);
          } catch (Throwable t) {
            taskResult.setSuccess(false);
            taskResult.getExcept().putIfAbsent(elem.getTableNameMapString(), t);
          }
        }
      }
    } finally {
      stopWatch.stop();
      taskResult.setDuration(stopWatch.getTotalTimeMillis());
    }
    return taskResult;
  }

  public WriterTaskResult exceptProcess(Throwable t) {
    return WriterTaskResult.builder()
        .success(false)
        .duration(0)
        .throwable(t)
        .build();
  }

}
