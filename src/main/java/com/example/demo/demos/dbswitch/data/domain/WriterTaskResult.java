// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.data.domain;

import com.example.demo.demos.dbswitch.core.task.TaskResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 写入任务线程的出参
 *
 * @author tang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WriterTaskResult implements TaskResult {

  @Builder.Default
  private Map<String, Long> perf = new HashMap<>();

  @Builder.Default
  private Map<String, Throwable> except = new HashMap<>();

  private boolean success;
  private long duration;
  private Throwable throwable;

  @Override
  public void padding() {
    if (!except.isEmpty() && null == throwable) {
      throwable = except.values().stream().findAny().get();
    }
  }
}
