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
 * 读取任务线程的出参
 *
 * @author tang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReaderTaskResult implements TaskResult {

  @Builder.Default
  private Map<String, Long> perf = new HashMap<>();

  @Builder.Default
  private Map<String, Throwable> except = new HashMap<>();

  private String tableNameMapString;

  private long successCount;

  private long failureCount;

  private long recordCount;

  private long totalBytes;

  private Throwable throwable;

  @Override
  public void padding() {
    if (successCount > 0 && null != tableNameMapString) {
      perf.put(tableNameMapString, recordCount);
    }
    if (null != throwable && null != tableNameMapString) {
      except.putIfAbsent(tableNameMapString, throwable);
    }
  }
}
