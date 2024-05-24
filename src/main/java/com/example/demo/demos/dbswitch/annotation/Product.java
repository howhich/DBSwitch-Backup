package com.example.demo.demos.dbswitch.annotation;


import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Product {

  ProductTypeEnum value();
}
