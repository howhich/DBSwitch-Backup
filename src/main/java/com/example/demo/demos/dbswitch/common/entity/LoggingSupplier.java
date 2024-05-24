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

import java.util.function.Supplier;

public class LoggingSupplier<T> extends AbstractLogging implements Supplier<T> {

  private final Supplier<T> command;

  public LoggingSupplier(Supplier<T> command, MdcKeyValue mdc) {
    super(mdc);
    this.command = command;
  }

  public Supplier<T> getCommand() {
    return command;
  }

  @Override
  public T get() {
    try {
      setupMdc();
      return command.get();
    } finally {
      cleanMdc();
    }
  }

}
