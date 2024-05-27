// Copyright tang.  All rights reserved.
// https://example.demo.demos.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.example.demo.demos.dbswitch.data.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.system.JvmInfo;
import cn.hutool.system.OsInfo;
import cn.hutool.system.RuntimeInfo;
import cn.hutool.system.SystemUtil;
import com.example.demo.demos.dbswitch.common.util.PomVersionUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public final class MachineUtils {

  public static String getPrintInformation() {
    OsInfo osInfo = SystemUtil.getOsInfo();
    JvmInfo jvmInfo = SystemUtil.getJvmInfo();
    RuntimeInfo runtimeInfo = SystemUtil.getRuntimeInfo();

    StringBuilder sb = new StringBuilder();
    sb.append("\tOS Arch:\t" + osInfo.getArch());
    sb.append("\n\tOS Name:\t" + osInfo.getName());
    sb.append("\n\tOS Version:\t" + osInfo.getVersion());
    sb.append("\n\tJavaVM Name:\t" + jvmInfo.getName());
    sb.append("\n\tJavaVM Version:\t" + jvmInfo.getVersion());
    sb.append("\n\tJavaVM Vendor:\t" + jvmInfo.getVendor());
    sb.append("\n\tJavaVM Info:\t" + jvmInfo.getInfo());
    sb.append("\n\tSystem Max Memory: \t" + FileUtil.readableFileSize(runtimeInfo.getMaxMemory()));
    sb.append("\n\tSystem Total Memory: \t" + FileUtil.readableFileSize(runtimeInfo.getTotalMemory()));
    sb.append("\n\tSystem Free Memory: \t" + FileUtil.readableFileSize(runtimeInfo.getFreeMemory()));
    sb.append("\n\tSystem Usable Memory: \t" + FileUtil.readableFileSize(runtimeInfo.getUsableMemory()));
    sb.append("\n\tRelease dbswitch Version: \t" + PomVersionUtils.getProjectVersion());

    return sb.toString();
  }
}
