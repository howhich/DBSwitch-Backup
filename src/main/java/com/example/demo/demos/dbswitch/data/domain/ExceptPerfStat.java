package com.example.demo.demos.dbswitch.data.domain;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.example.demo.demos.dbswitch.common.entity.PrintablePerfStat;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptPerfStat extends PrintablePerfStat {

  private Map<String, Throwable> readMap;

  private Map<String, Throwable> writeMap;

  @Override
  public String getPrintableString() {
    StringBuilder sb = new StringBuilder();
    Set<String> tableNamesSet = Sets.union(readMap.keySet(), writeMap.keySet());
    if (tableNamesSet.size() > 0) {
      sb.append("Exception Detail Information Follows:\n");
      int i = 1;
      for (String tableMapName : tableNamesSet) {
        Throwable readException = readMap.getOrDefault(tableMapName, null);
        Throwable writeException = writeMap.getOrDefault(tableMapName, null);
        if (null != readException) {
          sb.append("[" + i + "]" + tableMapName + " Read Stack Information :\n");
          sb.append(ExceptionUtil.stacktraceToString(readException));
        }
        if (null != writeException) {
          sb.append("[" + i + "]" + tableMapName + " Write Stack Information :\n");
          sb.append(ExceptionUtil.stacktraceToString(writeException));
        }
        i++;
      }
    }
    return sb.toString();
  }
}
