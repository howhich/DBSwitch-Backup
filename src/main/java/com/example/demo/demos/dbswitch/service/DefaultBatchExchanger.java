// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.service;

import com.example.demo.demos.dbswitch.common.entity.PrintablePerfStat;
import com.example.demo.demos.dbswitch.core.exchange.AbstractBatchExchanger;
import com.example.demo.demos.dbswitch.core.robot.RobotReader;
import com.example.demo.demos.dbswitch.core.robot.RobotWriter;
import com.example.demo.demos.dbswitch.data.domain.*;
import com.google.common.collect.Sets;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.List;
import java.util.Optional;

/**
 * 连接读取和写入的交换器
 *
 * @author tang
 */
public class DefaultBatchExchanger extends AbstractBatchExchanger {

  private List<PrintablePerfStat> perfStats;

  public DefaultBatchExchanger(
      AsyncTaskExecutor readExecutor,
      AsyncTaskExecutor writeExecutor,
      int channelMaxSize,
      List<PrintablePerfStat> perfStats) {
    super(readExecutor, writeExecutor, channelMaxSize);
    this.perfStats = perfStats;
  }

  @Override
  protected Throwable collectPerfStats(RobotReader reader, RobotWriter writer) {
    Throwable throwable = null;
    Optional<ReaderTaskResult> readResult = reader.getWorkResult();
    Optional<WriterTaskResult> writeResult = writer.getWorkResult();
    if (readResult.isPresent() && writeResult.isPresent()) {
      ReaderTaskResult r = readResult.get();
      WriterTaskResult w = writeResult.get();
      long total = r.getSuccessCount() + r.getFailureCount();
      long failure = Sets.union(r.getExcept().keySet(), w.getExcept().keySet()).size();
      perfStats.add(new ReaderPerfStat(total, failure, r.getTotalBytes()));
      perfStats.add(new WriterPerfStat(w.getDuration()));
      perfStats.add(new ComparePerfStat(r.getPerf(), w.getPerf()));
      perfStats.add(new ExceptPerfStat(r.getExcept(), w.getExcept()));
      throwable = (null != w.getThrowable()) ? w.getThrowable() : r.getThrowable();
    } else {
      if (readResult.isPresent()) {
        ReaderTaskResult r = readResult.get();
        long total = r.getSuccessCount() + r.getFailureCount();
        perfStats.add(new ReaderPerfStat(total, r.getFailureCount(), r.getTotalBytes()));
        throwable = r.getThrowable();
      }
      if (writeResult.isPresent()) {
        WriterTaskResult w = writeResult.get();
        perfStats.add(new WriterPerfStat(w.getDuration()));
        throwable = w.getThrowable();
      }
    }
    return throwable;
  }
}
