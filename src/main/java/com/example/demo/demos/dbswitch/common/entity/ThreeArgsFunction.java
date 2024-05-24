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

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface ThreeArgsFunction<U, V, W, R> {

  R apply(U var1, V var2, W var3);

  default <T> ThreeArgsFunction<U, V, W, T> andThen(Function<? super R, ? extends T> after) {
    Objects.requireNonNull(after);
    return (u, v, w) -> {
      return after.apply(this.apply(u, v, w));
    };
  }
}