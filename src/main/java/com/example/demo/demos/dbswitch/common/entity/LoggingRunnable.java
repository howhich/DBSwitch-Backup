// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.common.entity;

public class LoggingRunnable extends AbstractLogging implements Runnable {

  private final Runnable command;

  public LoggingRunnable(Runnable command, MdcKeyValue mdc) {
    super(mdc);
    this.command = command;
  }

  @Override
  public void run() {
    try {
      setupMdc();
      command.run();
    } finally {
      cleanMdc();
    }
  }

}
