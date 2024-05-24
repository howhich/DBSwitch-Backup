// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.common.type;

/**
 * 处理大小写转换的枚举类
 *
 * @author tang
 */
public enum CaseConvertEnum {
  NONE(s -> s),
  UPPER(String::toUpperCase),
  LOWER(String::toLowerCase),
  ;

  private Converter function;

  CaseConvertEnum(Converter function) {
    this.function = function;
  }

  public String convert(String name) {
    return function.convert(name);
  }

  interface Converter {

    String convert(String s);
  }

}
