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

import com.example.demo.demos.dbswitch.core.task.TaskResult;

public abstract class RobotReader<R extends TaskResult> extends AbstractRobot<R> {

  public abstract void startRead();

  public void startWork() {
    startRead();
  }

  public abstract long getRemainingCount();
}
