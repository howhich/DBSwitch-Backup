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

import com.example.demo.demos.dbswitch.common.util.ExamineUtils;
import com.example.demo.demos.dbswitch.core.robot.RobotReader;
import com.example.demo.demos.dbswitch.core.robot.RobotWriter;
import org.springframework.core.task.AsyncTaskExecutor;

public abstract class AbstractBatchExchanger {

  private MemChannel memChannel;
  private AsyncTaskExecutor readThreadExecutor;
  private AsyncTaskExecutor writeThreadExecutor;

  public AbstractBatchExchanger(AsyncTaskExecutor readExecutor, AsyncTaskExecutor writeExecutor, int channelMaxSize) {
    ExamineUtils.checkNotNull(readExecutor, "readExecutor");
    ExamineUtils.checkNotNull(writeExecutor, "writeExecutor");
    this.memChannel = MemChannel.createNewChannel(channelMaxSize);
    this.readThreadExecutor = readExecutor;
    this.writeThreadExecutor = writeExecutor;
  }

  public MemChannel getMemChannel() {
    return memChannel;
  }

  public int getChannelWaitingNum() {
    return memChannel.size();
  }

  public void exchange(RobotReader reader, RobotWriter writer) {
    // 为reader和writer配置数据传输隧道
    reader.setChannel(this.memChannel);
    writer.setChannel(this.memChannel);

    // 初始化reader和writer
    reader.init(readThreadExecutor);
    writer.init(writeThreadExecutor);

    // 启动reader和writer的并行工作
    writer.startWork();
    reader.startWork();

    // writer会等待reader执行完
    writer.waitForFinish();

    // 收集统计信息
    Throwable throwable = collectPerfStats(reader, writer);
    if (null != throwable) {
      if (throwable instanceof RuntimeException) {
        throw (RuntimeException) throwable;
      }
      throw new RuntimeException(throwable);
    }
  }

  protected abstract Throwable collectPerfStats(RobotReader reader, RobotWriter writer);
}
