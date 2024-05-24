package com.example.demo.demos.service;

import com.example.demo.demos.config.DbswitchConfig;
import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DriverLoadService {

  private Map<ProductTypeEnum, Map<String, File>> drivers = new EnumMap<>(ProductTypeEnum.class);

  @Resource
  private DbswitchConfig dbswitchConfig;

  @EventListener(ApplicationReadyEvent.class)
  public void loadDrivers() {
    try {
      doLoadDrivers();
      log.info("Finish load jdbc drivers from local path: {}", dbswitchConfig.getDriversBasePath());
    } catch (Exception e) {
      log.error("load drivers failed:{}", e.getMessage(), e);
      throw e;
    }
  }

  private void doLoadDrivers() {
    String driversBasePath = dbswitchConfig.getDriversBasePath();
    File file = new File(driversBasePath);
    File[] types = file.listFiles();
    if (ArrayUtils.isEmpty(types)) {
      throw new IllegalArgumentException(
          "No drivers type found from path:" + driversBasePath);
    }
    for (File type : types) {
      if (!ProductTypeEnum.exists(type.getName())) {
        continue;
      }
      File[] driverVersions = type.listFiles();
      if (ArrayUtils.isEmpty(driverVersions)) {
        throw new IllegalArgumentException(
            "No driver version found from path:" + type.getAbsolutePath());
      }
      for (File driverVersion : driverVersions) {
        if (ArrayUtils.isEmpty(driverVersion.listFiles())) {
          throw new IllegalArgumentException(
              "No driver version jar file found from path:" + driverVersion.getAbsolutePath());
        }
        ProductTypeEnum typeEnum = ProductTypeEnum.of(type.getName());
        Map<String, File> versionMap = drivers.computeIfAbsent(typeEnum, k -> new HashMap<>());
        versionMap.put(driverVersion.getName(), driverVersion);
        log.info("Load driver for {} ,version:{},path:{}",
            typeEnum.getName(), driverVersion.getName(), driverVersion.getAbsolutePath());
      }
    }
  }

  public List<String> getDriverVersion(ProductTypeEnum dbTypeEnum) {
    return Optional.ofNullable(drivers.get(dbTypeEnum)).orElseGet(HashMap::new)
        .keySet().stream().collect(Collectors.toList());
  }

  public Map<String, File> getDriverVersionWithPath(ProductTypeEnum dbTypeEnum) {
    return Optional.ofNullable(drivers.get(dbTypeEnum)).orElse(new HashMap<>());
  }

  public File getVersionDriverFile(ProductTypeEnum dbTypeEnum, String driverVersion) {
    return drivers.get(dbTypeEnum).get(driverVersion);
  }

}
