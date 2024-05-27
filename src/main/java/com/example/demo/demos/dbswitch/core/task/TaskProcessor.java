// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.core.task;

import java.util.concurrent.CancellationException;
import java.util.function.Supplier;

public abstract class TaskProcessor<R extends TaskResult> implements Supplier<R> {

  /**
   * 任务执行期间用于检查是否接收到任务中断信号
   */
  protected void checkInterrupt() {
    if (Thread.currentThread().isInterrupted()) {
      throw new CancellationException("task is interrupted");
    }
  }

  /**
   * 前置处理准备
   */
  protected void beforeProcess() {

  }

  /**
   * 处理过程
   *
   * @return R extends TaskResult
   */
  protected abstract R doProcess();

  /**
   * 异常处理
   *
   * @param t
   */
  protected abstract R exceptProcess(Throwable t);

  /**
   * 后置处理
   */
  protected void afterProcess() {

  }

  @Override
  public R get() {
    try {
      checkInterrupt();
      beforeProcess();
      checkInterrupt();
      return doProcess();
    } catch (Throwable t) {
      return exceptProcess(t);
    } finally {
      afterProcess();
    }
  }

}
