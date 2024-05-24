// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.data.entity;

import com.example.demo.demos.dbswitch.common.type.CaseConvertEnum;
import com.example.demo.demos.dbswitch.common.type.SyncOptionEnum;
import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * 目标端参数配置
 *
 * @author tang
 */
@Data
public class TargetDataSourceProperties {

  private String url;
  private String driverClassName;
  private String username;
  private String password;
  private String driverPath;

  private Long connectionTimeout = TimeUnit.SECONDS.toMillis(60);
  private Long maxLifeTime = TimeUnit.MINUTES.toMillis(30);

  private String targetSchema = "";
  private CaseConvertEnum tableNameCase = CaseConvertEnum.NONE;
  private CaseConvertEnum columnNameCase = CaseConvertEnum.NONE;
  private Boolean targetDrop = Boolean.TRUE;
  private Boolean onlyCreate = Boolean.FALSE;
  private Boolean changeDataSync = Boolean.FALSE;
  private Boolean createTableAutoIncrement = Boolean.FALSE;
  private Boolean writerEngineInsert = Boolean.FALSE;

  private String beforeSqlScripts;
  private String afterSqlScripts;
  private SyncOptionEnum targetSyncOption = SyncOptionEnum.INSERT_UPDATE_DELETE;
}
