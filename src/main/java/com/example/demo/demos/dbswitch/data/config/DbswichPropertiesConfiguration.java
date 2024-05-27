// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.data.config;

import com.example.demo.demos.dbswitch.data.entity.GlobalParamConfigProperties;
import com.example.demo.demos.dbswitch.data.entity.SourceDataSourceProperties;
import com.example.demo.demos.dbswitch.data.entity.TargetDataSourceProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 属性映射配置
 *
 * @author tang
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dbswitch")
@PropertySource(
    value = {"classpath:config.properties", "classpath:config.yml", "classpath:config.yaml"},
    ignoreResourceNotFound = true,
    factory = DbswitchPropertySourceFactory.class)
public class DbswichPropertiesConfiguration {

  private SourceDataSourceProperties source = new SourceDataSourceProperties();

  private SourceDataSourceProperties target = new SourceDataSourceProperties();

  private GlobalParamConfigProperties config = new GlobalParamConfigProperties();
}
