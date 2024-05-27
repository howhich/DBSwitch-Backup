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

public abstract class RobotWriter<R extends TaskResult> extends AbstractRobot<R> {

  public abstract void startWrite();

  public void startWork() {
    startWrite();
  }

  public abstract void waitForFinish();
}
