// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.common.entity;


import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.entity.DatabaseConnectionEntity;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DbConnectionCreateRequest {

  private String name;
  private ProductTypeEnum type;
  private String version;
  private String driver;
  private Integer mode;
  private String address;
  private String port;
  private String databaseName;
  private String characterEncoding;
  private String url;
  private String username;
  private String password;

  public DatabaseConnectionEntity toDatabaseConnection() {
    DatabaseConnectionEntity databaseConnectionEntity = new DatabaseConnectionEntity();
    databaseConnectionEntity.setId(null);
    databaseConnectionEntity.setName(name);
    databaseConnectionEntity.setType(type);
    databaseConnectionEntity.setVersion(version.trim());
    databaseConnectionEntity.setDriver(driver.trim());
    databaseConnectionEntity.setMode(0);
    databaseConnectionEntity.setAddress(address.trim());
    databaseConnectionEntity.setPort(port.trim());
    databaseConnectionEntity.setDatabaseName(databaseName.trim());
    databaseConnectionEntity.setCharacterEncoding(characterEncoding.trim());
    databaseConnectionEntity.setUrl(url.trim());
    databaseConnectionEntity.setUsername(username);
    databaseConnectionEntity.setPassword(password);

    return databaseConnectionEntity;
  }

}
