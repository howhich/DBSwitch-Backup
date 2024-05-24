package com.example.demo.demos.dbswitch.common.util;

import cn.hutool.core.util.ClassLoaderUtil;
import com.example.demo.demos.datasource.WrapCommonDataSource;
import com.example.demo.demos.dbswitch.common.entity.CloseableDataSource;
import com.example.demo.demos.dbswitch.common.entity.InvisibleDataSource;
import com.example.demo.demos.dbswitch.common.entity.JarClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URLClassLoader;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DSUtils {
    private static final Map<String, URLClassLoader> classLoaderMap = new ConcurrentHashMap<>();

    public static CloseableDataSource createCommonDataSource(
            String jdbcUrl,
            String driverClass,
            String driverPath,
            String username,
            String password) {
        URLClassLoader urlClassLoader = createURLClassLoader(
                driverPath,
                driverClass);
        InvisibleDataSource dataSource = createInvisibleDataSource(
                urlClassLoader,
                jdbcUrl,
                driverClass,
                username,
                password,
                new Properties()
        );
        return new WrapCommonDataSource(dataSource, urlClassLoader);
    }

    private static URLClassLoader createURLClassLoader(
            String driverPath, String driverClass) {
        ExamineUtils.checkArgument(
                StringUtils.isNoneBlank(driverPath),
                "Invalid driver path,can not be empty!");
        ExamineUtils.checkArgument(
                StringUtils.isNoneBlank(driverClass),
                "Invalid driver class,can not be empty!");
        ClassLoader parent = driverClass.contains("postgresql")
                ? ClassLoaderUtil.getContextClassLoader()
                : ClassLoaderUtil.getSystemClassLoader().getParent();
        URLClassLoader loader = getOrCreateClassLoader(driverPath, parent);
        try {
            Class<?> clazz = loader.loadClass(driverClass);
            clazz.getConstructor().newInstance();
            return loader;
        } catch (Exception e) {
            log.error("Could not load class : {} from driver path: {}", driverClass, driverPath, e);
            throw new RuntimeException(e);
        }
    }
    private static InvisibleDataSource createInvisibleDataSource(
            ClassLoader cl,
            String jdbcUrl,
            String driverClass,
            String username,
            String password,
            Properties properties) {
        return new InvisibleDataSource(
                cl,
                jdbcUrl,
                driverClass,
                username,
                password,
                properties);
    }
    private static URLClassLoader getOrCreateClassLoader(
            String path, ClassLoader parent) {
        URLClassLoader urlClassLoader = classLoaderMap.get(path);
        if (null == urlClassLoader) {
            synchronized (DSUtils.class) {
                urlClassLoader = classLoaderMap.get(path);
                if (null == urlClassLoader) {
                    log.info("Create jar classLoader from path: {}", path);
                    urlClassLoader = new JarClassLoader(path, parent);
                    classLoaderMap.put(path, urlClassLoader);
                }
            }
        }
        return urlClassLoader;
    }
}
