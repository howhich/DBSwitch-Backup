// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.schema;

public class IndexFieldMeta {

  private String fieldName;
  private Integer ordinalPosition;
  private Boolean isAscOrder;

  public IndexFieldMeta(String fieldName, Integer ordinalPosition, Boolean isAscOrder) {
    this.fieldName = fieldName;
    this.ordinalPosition = ordinalPosition;
    this.isAscOrder = isAscOrder;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public Integer getOrdinalPosition() {
    return ordinalPosition;
  }

  public void setOrdinalPosition(Integer ordinalPosition) {
    this.ordinalPosition = ordinalPosition;
  }

  public Boolean getAscOrder() {
    return isAscOrder;
  }

  public void setAscOrder(Boolean ascOrder) {
    isAscOrder = ascOrder;
  }
}
