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

import com.example.demo.demos.dbswitch.common.entity.CloseableDataSource;
import com.example.demo.demos.dbswitch.core.exchange.MemChannel;
import com.example.demo.demos.dbswitch.core.task.TaskParam;
import com.example.demo.demos.dbswitch.data.config.DbswichPropertiesConfiguration;
import com.example.demo.demos.dbswitch.schema.TableDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * 读取任务线程的入参
 *
 * @author tang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReaderTaskParam implements TaskParam {

  private MemChannel memChannel;
  private TableDescription tableDescription;
  private DbswichPropertiesConfiguration configuration;
  private CloseableDataSource sourceDataSource;
  private CloseableDataSource targetDataSource;
  private Set<String> targetExistTables;
  private CountDownLatch countDownLatch;
}
