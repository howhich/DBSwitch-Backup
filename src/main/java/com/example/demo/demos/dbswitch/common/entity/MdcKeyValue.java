// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.common.entity;

public class MdcKeyValue {

  private String mdcKey;
  private String mdcValue;

  public MdcKeyValue(String mdcKey, String mdcValue) {
    this.mdcKey = mdcKey;
    this.mdcValue = mdcValue;
  }

  public String getMdcKey() {
    return mdcKey;
  }

  public void setMdcKey(String mdcKey) {
    this.mdcKey = mdcKey;
  }

  public String getMdcValue() {
    return mdcValue;
  }

  public void setMdcValue(String mdcValue) {
    this.mdcValue = mdcValue;
  }
}
