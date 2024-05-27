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

import org.springframework.core.task.AsyncTaskExecutor;

public interface Robot {

  void init(AsyncTaskExecutor threadExecutor);

  void startWork();

  void interrupt();
}
