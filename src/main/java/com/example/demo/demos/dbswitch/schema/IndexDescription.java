// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.schema;

import com.example.demo.demos.dbswitch.common.type.TableIndexEnum;

import java.util.List;

public class IndexDescription {

  private TableIndexEnum indexType;

  private String indexName;

  private List<IndexFieldMeta> indexFields;

  public IndexDescription(TableIndexEnum indexType, String indexName,
      List<IndexFieldMeta> indexFields) {
    this.indexType = indexType;
    this.indexName = indexName;
    this.indexFields = indexFields;
  }

  public TableIndexEnum getIndexType() {
    return indexType;
  }

  public void setIndexType(TableIndexEnum indexType) {
    this.indexType = indexType;
  }

  public String getIndexName() {
    return indexName;
  }

  public void setIndexName(String indexName) {
    this.indexName = indexName;
  }

  public List<IndexFieldMeta> getIndexFields() {
    return indexFields;
  }

  public void setIndexFields(List<IndexFieldMeta> indexFields) {
    this.indexFields = indexFields;
  }
}
