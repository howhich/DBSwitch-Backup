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

import java.util.function.Function;

public class LoggingFunction<T, R> extends AbstractLogging implements Function<T, R> {

  private final Function<T, R> command;

  public LoggingFunction(Function<T, R> command, MdcKeyValue mdc) {
    super(mdc);
    this.command = command;
  }

  @Override
  public R apply(T t) {
    try {
      setupMdc();
      return command.apply(t);
    } finally {
      cleanMdc();
    }
  }

}
