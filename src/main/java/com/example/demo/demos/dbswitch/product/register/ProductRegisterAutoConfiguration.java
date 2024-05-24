// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.product.register;

import com.example.demo.demos.dbswitch.annotation.Product;
import com.example.demo.demos.dbswitch.common.consts.Constants;
import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.dbswitch.provider.ProductFactoryProvider;
import com.example.demo.demos.dbswitch.provider.ProductProviderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

@Slf4j
@Configuration
@ConditionalOnClass(ProductProviderFactory.class)
public class ProductRegisterAutoConfiguration implements InitializingBean, BeanClassLoaderAware {

  private static final Set<String> providers = new HashSet<>();

  private ClassLoader classLoader;

  private int parseLine(BufferedReader reader, int lc, List<String> names)
      throws IOException, ServiceConfigurationError {
    String ln = reader.readLine();
    if (ln == null) {
      return -1;
    }
    int ci = ln.indexOf('#');
    if (ci >= 0) {
      ln = ln.substring(0, ci);
    }
    ln = ln.trim();
    int n = ln.length();
    if (n != 0) {
      if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0)) {
        log.error("Illegal configuration-file syntax: {}", ln);
      }
      int cp = ln.codePointAt(0);
      if (!Character.isJavaIdentifierStart(cp)) {
        log.error("Illegal provider-class name: {}", ln);
      }
      for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
        cp = ln.codePointAt(i);
        if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
          log.error("Illegal provider-class name: {}", ln);
        }
      }
      if (!providers.contains(ln) && !names.contains(ln)) {
        names.add(ln);
      }
    }
    return lc + 1;
  }

  private List<String> parse(URL url) throws ServiceConfigurationError {
    InputStream in = null;
    BufferedReader reader = null;
    ArrayList<String> names = new ArrayList<>();
    try {
      in = url.openStream();
      reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
      int lc = 1;
      while ((lc = parseLine(reader, lc, names)) >= 0) {
      }
    } catch (IOException x) {
      log.error("Error reading configuration file", x);
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
        if (in != null) {
          in.close();
        }
      } catch (IOException y) {
        log.error("Error closing configuration file", y);
      }
    }
    return names;
  }

  @Override
  public void setBeanClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    log.info("Register database product now ...");
    ClassLoader loader = (null != classLoader)
        ? classLoader
        : ProductProviderFactory.class.getClassLoader();
    Enumeration<URL> resources = loader.getResources(Constants.SPI_FILE);
    while (resources.hasMoreElements()) {
      URL url = resources.nextElement();
      providers.addAll(parse(url));
    }
    int totalCount = 0;
    for (String className : providers) {
      Class<?> aClass = classLoader.loadClass(className);
      if (ProductFactoryProvider.class.isAssignableFrom(aClass)) {
        if (aClass.isAnnotationPresent(Product.class)) {
          Product annotation = aClass.getAnnotation(Product.class);
          ProductTypeEnum productType = annotation.value();
          if (null != productType) {
            ProductProviderFactory.register(productType, className);
            ++totalCount;
          }
        }
      }
    }
    log.info("Finish to register total {} database product !", totalCount);
  }
}