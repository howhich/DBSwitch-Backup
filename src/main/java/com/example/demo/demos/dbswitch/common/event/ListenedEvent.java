// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.common.event;

import com.google.common.base.Preconditions;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;

@Getter
public class ListenedEvent extends java.util.EventObject {

  private String name;
  private Object[] args;

  public ListenedEvent(Object source, String event) {
    this(source, event, Collections.emptyList().toArray());
  }

  public ListenedEvent(Object source, String event, Object... args) {
    super(source);
    this.name = event;
    this.args = args;
  }

  public void checkArgs(Class<?>... classes) throws IllegalArgumentException {
    Preconditions.checkArgument(this.args.length == classes.length,
        "The args count of event '%s' should be %s(actual %s)",
        this.name, classes.length, this.args.length);
    int i = 0;
    for (Class<?> c : classes) {
      Object arg = this.args[i++];
      if (arg == null) {
        continue;
      }
      Preconditions.checkArgument(c.isAssignableFrom(arg.getClass()),
          "The arg '%s'(%s) can't match %s",
          arg, arg.getClass(), c);
    }
  }

  @Override
  public String toString() {
    return String.format("Event{name='%s', args=%s}",
        this.name, Arrays.asList(this.args));
  }
}