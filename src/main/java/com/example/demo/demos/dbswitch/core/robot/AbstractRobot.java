// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.core.robot;

import com.example.demo.demos.dbswitch.core.exchange.MemChannel;
import com.example.demo.demos.dbswitch.core.task.TaskResult;

import java.util.Optional;

public abstract class AbstractRobot<R extends TaskResult> implements Robot {

  private MemChannel channel;

  public void setChannel(MemChannel channel) {
    this.channel = channel;
  }

  public MemChannel getChannel() {
    return this.channel;
  }

  public void clearChannel() {
    this.channel.clear();
  }

  public abstract Optional<R> getWorkResult();
}
