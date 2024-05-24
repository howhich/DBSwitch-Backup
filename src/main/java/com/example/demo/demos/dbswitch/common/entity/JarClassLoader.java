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

import com.example.demo.demos.dbswitch.common.util.ExamineUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库驱动jar的ClassLoader
 *
 * @author tang
 */
public class JarClassLoader extends URLClassLoader {

  public JarClassLoader(String path, ClassLoader parent) {
    this(new String[]{path}, parent);
  }

  public JarClassLoader(String[] paths, ClassLoader parent) {
    super(getURLs(paths), parent);
  }

  private static URL[] getURLs(String[] paths) {
    ExamineUtils.checkArgument(
        null != paths && 0 != paths.length,
        "jar file path is empty.");
    List<String> dirs = new ArrayList<>();
    for (String path : paths) {
      dirs.add(path);
      collectDirs(path, dirs);
    }
    List<URL> urls = new ArrayList<>();
    for (String path : dirs) {
      urls.addAll(doGetURLs(path));
    }
    if (urls.isEmpty()) {
      throw new RuntimeException("No jar file found from path :"
          + Arrays.stream(paths).collect(Collectors.joining(",")) + "!");
    }
    return urls.toArray(new URL[0]);
  }

  private static void collectDirs(String path, List<String> collector) {
    if (StringUtils.isEmpty(path)) {
      return;
    }
    File current = new File(path);
    if (!current.exists() || !current.isDirectory()) {
      return;
    }
    for (File child : current.listFiles()) {
      if (!child.isDirectory()) {
        continue;
      }
      collector.add(child.getAbsolutePath());
      collectDirs(child.getAbsolutePath(), collector);
    }
  }

  private static List<URL> doGetURLs(final String path) {
    ExamineUtils.checkArgument(StringUtils.isNotBlank(path), "jar path is empty.");
    File jarPath = new File(path);
    if (!jarPath.exists() || !jarPath.isDirectory()) {
      return Collections.emptyList();
    }
    FileFilter jarFilter = name -> name.getName().endsWith(".jar");
    File[] allJars = new File(path).listFiles(jarFilter);
    List<URL> jarURLs = new ArrayList<>(allJars.length);
    for (int i = 0; i < allJars.length; i++) {
      try {
        jarURLs.add(allJars[i].toURI().toURL());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return jarURLs;
  }

}
