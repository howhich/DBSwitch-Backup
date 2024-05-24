package com.example.demo.demos.dbswitch.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * 机器统计信息
 */
@Slf4j
@UtilityClass
public class MachineInfoUtils {

  private static NumberFormat fmtI = new DecimalFormat("###,###", new DecimalFormatSymbols(Locale.ENGLISH));

  /**
   * 获取操作系统信息
   *
   * @return String
   */
  public static String getOSInfo() {
    OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    StringBuffer sb = new StringBuffer();
    sb.append("Operation System Information :" + "\r\n");
    sb.append("OS Name: " + os.getName() + "\r\n");
    sb.append("OS Core Arch : " + os.getArch() + "\r\n");
    sb.append("Available CPU Count: " + os.getAvailableProcessors() + "\r\n");
    sb.append("System Avg Load: " + os.getSystemLoadAverage() + "\r\n");
    sb.append("JAVA Version: " + System.getProperty("java.version") + "\r\n");
    return sb.toString();
  }

  /**
   * 日志打印JVM信息
   *
   * @return String
   */
  public static void printJVMInfo() {
    List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
    for (MemoryPoolMXBean pool : pools) {
      final String kind = pool.getType().name();
      final MemoryUsage usage = pool.getUsage();
      log.info("model:" + getKindName(kind)
          + ", name:" + pool.getName()
          + ", init:" + bytesToMB(usage.getInit())
          + ", used:" + bytesToMB(usage.getUsed())
          + ", available:" + bytesToMB(usage.getCommitted())
          + ", max:" + bytesToMB(usage.getMax()));
    }
  }

  protected static String getKindName(String kind) {
    if ("NON_HEAP".equals(kind)) {
      return "NonHeap";
    } else {
      return "Heap";
    }
  }

  protected static String bytesToMB(long bytes) {
    return fmtI.format((bytes / 1024 / 1024)) + " MB";
  }

  public static void main(String[] args) {
    log.info(getOSInfo());
    printJVMInfo();
  }
}
