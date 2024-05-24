package com.example.demo.demos.dbswitch.common.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.json.JSONUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.*;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@UtilityClass
public final class ObjectCastUtils {

  /**
   * 将任意类型转换为java.lang.Byte类型
   *
   * @param in 任意类型的对象实例
   * @return java.lang.Byte类型
   */
  public static Byte castToByte(final Object in) {
    if (in instanceof Number) {
      return ((Number) in).byteValue();
    } else if (in instanceof java.util.Date) {
      return Long.valueOf(((java.util.Date) in).getTime()).byteValue();
    } else if (in instanceof String) {
      try {
        return Byte.parseByte(in.toString());
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.lang.String类型转换为java.lang.Byte类型:%s", e.getMessage()));
      }
    } else if (in instanceof Character) {
      try {
        return Byte.parseByte(in.toString());
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.lang.Character类型转换为java.lang.Byte类型:%s", e.getMessage()));
      }
    } else if (in instanceof java.sql.Clob) {
      try {
        return Byte.parseByte(clob2Str((java.sql.Clob) in));
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.sql.Clob类型转换为java.lang.Byte类型:%s", e.getMessage()));
      }
    } else if (in instanceof Boolean) {
      return (Boolean) in ? (byte) 1 : (byte) 0;
    }

    return null;
  }


  public static byte[] castToByteArray(final Object in) {
    if (in instanceof byte[]) {
      return (byte[]) in;
    } else if (in instanceof java.util.Date) {
      return in.toString().getBytes();
    } else if (in instanceof java.sql.Blob) {
      return blob2Bytes((java.sql.Blob) in);
    } else if (in instanceof String || in instanceof Character) {
      return in.toString().getBytes();
    } else if (in instanceof java.sql.Clob) {
      return clob2Str((java.sql.Clob) in).getBytes();
    } else {
      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
          ObjectOutputStream oos = new ObjectOutputStream(bos)) {
        oos.writeObject(in);
        oos.flush();
        return bos.toByteArray();
      } catch (Exception e) {
        log.error("Field value convert from {} to byte[] failed:", in.getClass().getName(), e);
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * 将任意类型转换为java.lang.Short类型
   *
   * @param in 任意类型的对象实例
   * @return java.lang.Short类型
   */
  public static Short castToShort(final Object in) {
    if (in instanceof Number) {
      return ((Number) in).shortValue();
    } else if (in instanceof Byte) {
      return (short) (((byte) in) & 0xff);
    } else if (in instanceof java.util.Date) {
      return (short) ((java.util.Date) in).getTime();
    } else if (in instanceof java.util.Calendar) {
      return (short) ((java.util.Calendar) in).getTime().getTime();
    } else if (in instanceof LocalDateTime) {
      return (short) Timestamp.valueOf((LocalDateTime) in).getTime();
    } else if (in instanceof OffsetDateTime) {
      return (short) Timestamp.valueOf(((OffsetDateTime) in).toLocalDateTime())
          .getTime();
    } else if (in instanceof String || in instanceof Character) {
      try {
        String s = in.toString().trim();
        if (s.equalsIgnoreCase("true")) {
          return Short.valueOf((short) 1);
        } else if (s.equalsIgnoreCase("false")) {
          return Short.valueOf((short) 0);
        } else {
          return Short.parseShort(s);
        }
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.lang.String类型转换为java.lang.Short类型:%s", e.getMessage()));
      }
    } else if (in instanceof java.sql.Clob) {
      try {
        String s = clob2Str((java.sql.Clob) in).trim();
        if (s.equalsIgnoreCase("true")) {
          return Short.valueOf((short) 1);
        } else if (s.equalsIgnoreCase("false")) {
          return Short.valueOf((short) 0);
        } else {
          return Short.parseShort(s);
        }
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.sql.Clob类型转换为java.lang.Short类型:%s", e.getMessage()));
      }
    } else if (in instanceof Boolean) {
      return (Boolean) in ? (short) 1 : (short) 0;
    }

    return null;
  }

  /**
   * 将任意类型转换为java.lang.Integer类型
   *
   * @param in 任意类型的对象实例
   * @return java.lang.Integer类型
   */
  public static Integer castToInteger(final Object in) {
    if (in instanceof Number) {
      return ((Number) in).intValue();
    } else if (in instanceof Byte) {
      return (((byte) in) & 0xff);
    } else if (in instanceof java.util.Date) {
      return (int) ((java.util.Date) in).getTime();
    } else if (in instanceof java.util.Calendar) {
      return (int) ((java.util.Calendar) in).getTime().getTime();
    } else if (in instanceof LocalDateTime) {
      return (int) Timestamp.valueOf((LocalDateTime) in).getTime();
    } else if (in instanceof OffsetDateTime) {
      return (int) Timestamp.valueOf(((OffsetDateTime) in).toLocalDateTime())
          .getTime();
    } else if (in instanceof String || in instanceof Character) {
      try {
        String s = in.toString().trim();
        if (s.equalsIgnoreCase("true")) {
          return Integer.valueOf(1);
        } else if (s.equalsIgnoreCase("false")) {
          return Integer.valueOf(0);
        } else {
          return Integer.parseInt(s);
        }
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.lang.String类型转换为java.lang.Integer类型:%s", e.getMessage()));
      }
    } else if (in instanceof java.sql.Clob) {
      try {
        String s = clob2Str((java.sql.Clob) in).trim();
        if (s.equalsIgnoreCase("true")) {
          return Integer.valueOf(1);
        } else if (s.equalsIgnoreCase("false")) {
          return Integer.valueOf(0);
        } else {
          return Integer.parseInt(s);
        }
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.sql.Clob类型转换为java.lang.Integer类型:%s", e.getMessage()));
      }
    } else if (in instanceof Boolean) {
      return (Boolean) in ? (int) 1 : (int) 0;
    }

    return null;
  }

  /**
   * 将任意类型转换为java.lang.Long类型
   *
   * @param in 任意类型的对象实例
   * @return java.lang.Long类型
   */
  public static Long castToLong(final Object in) {
    if (in instanceof Number) {
      return ((Number) in).longValue();
    } else if (in instanceof Byte) {
      return (long) (((byte) in) & 0xff);
    } else if (in instanceof java.util.Date) {
      return ((java.util.Date) in).getTime();
    } else if (in instanceof java.util.Calendar) {
      return ((java.util.Calendar) in).getTime().getTime();
    } else if (in instanceof LocalDateTime) {
      return Timestamp.valueOf((LocalDateTime) in).getTime();
    } else if (in instanceof OffsetDateTime) {
      return Timestamp.valueOf(((OffsetDateTime) in).toLocalDateTime())
          .getTime();
    } else if (in instanceof String || in instanceof Character) {
      try {
        String s = in.toString().trim();
        if (s.equalsIgnoreCase("true")) {
          return Long.valueOf(1);
        } else if (s.equalsIgnoreCase("false")) {
          return Long.valueOf(0);
        } else {
          return Long.parseLong(s);
        }
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.lang.String类型转换为java.lang.Long类型:%s", e.getMessage()));
      }
    } else if (in instanceof java.sql.Clob) {
      try {
        String s = clob2Str((java.sql.Clob) in).trim();
        if (s.equalsIgnoreCase("true")) {
          return Long.valueOf(1);
        } else if (s.equalsIgnoreCase("false")) {
          return Long.valueOf(0);
        } else {
          return Long.parseLong(s);
        }
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.sql.Clob类型转换为java.lang.Long类型:%s", e.getMessage()));
      }
    } else if (in instanceof Boolean) {
      return (Boolean) in ? (long) 1 : (long) 0;
    }

    return null;
  }

  /**
   * 将任意类型转换为java.lang.Number类型
   *
   * @param in 任意类型的对象实例
   * @return java.lang.Number类型
   */
  public static Number castToNumeric(final Object in) {
    if (in instanceof Number) {
      return (Number) in;
    } else if (in instanceof java.util.Date) {
      return ((java.util.Date) in).getTime();
    } else if (in instanceof java.util.Calendar) {
      return ((java.util.Calendar) in).getTime().getTime();
    } else if (in instanceof LocalDateTime) {
      return Timestamp.valueOf((LocalDateTime) in).getTime();
    } else if (in instanceof OffsetDateTime) {
      return Timestamp.valueOf(((OffsetDateTime) in).toLocalDateTime())
          .getTime();
    } else if (in instanceof String || in instanceof Character) {
      try {
        String s = in.toString().trim();
        if (s.equalsIgnoreCase("true")) {
          return Integer.valueOf(1);
        } else if (s.equalsIgnoreCase("false")) {
          return Integer.valueOf(0);
        } else {
          return new BigDecimal(s);
        }
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.lang.String类型转换为java.lang.Number类型:%s", e.getMessage()));
      }
    } else if (in instanceof java.sql.Clob) {
      try {
        String s = clob2Str((java.sql.Clob) in).trim();
        if (s.equalsIgnoreCase("true")) {
          return Integer.valueOf(1);
        } else if (s.equalsIgnoreCase("false")) {
          return Integer.valueOf(0);
        } else {
          return new BigDecimal(s);
        }
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.sql.Clob类型转换为java.lang.Number类型:%s", e.getMessage()));
      }
    } else if (in instanceof Boolean) {
      return (Boolean) in ? (long) 1 : (long) 0;
    }

    return null;
  }

  /**
   * 将任意类型转换为java.lang.Float类型
   *
   * @param in 任意类型的对象实例
   * @return java.lang.Float类型
   */
  public static Float castToFloat(final Object in) {
    if (in instanceof Number) {
      return ((Number) in).floatValue();
    } else if (in instanceof java.util.Date) {
      return (float) ((java.util.Date) in).getTime();
    } else if (in instanceof java.util.Calendar) {
      return (float) ((java.util.Calendar) in).getTime().getTime();
    } else if (in instanceof LocalDateTime) {
      return (float) Timestamp.valueOf((LocalDateTime) in).getTime();
    } else if (in instanceof OffsetDateTime) {
      return (float) Timestamp.valueOf(((OffsetDateTime) in).toLocalDateTime())
          .getTime();
    } else if (in instanceof String || in instanceof Character) {
      try {
        String s = in.toString().trim();
        if (s.equalsIgnoreCase("true")) {
          return Float.valueOf(1);
        } else if (s.equalsIgnoreCase("false")) {
          return Float.valueOf(0);
        } else {
          return Float.parseFloat(s);
        }
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.lang.String类型转换为java.lang.Float类型:%s", e.getMessage()));
      }
    } else if (in instanceof java.sql.Clob) {
      try {
        String s = clob2Str((java.sql.Clob) in).trim();
        if (s.equalsIgnoreCase("true")) {
          return Float.valueOf(1);
        } else if (s.equalsIgnoreCase("false")) {
          return Float.valueOf(0);
        } else {
          return Float.parseFloat(s);
        }
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.sql.Clob类型转换为java.lang.Float类型:%s", e.getMessage()));
      }
    } else if (in instanceof Boolean) {
      return (Boolean) in ? 1f : 0f;
    }

    return null;
  }

  /**
   * 将任意类型转换为java.lang.Double类型
   *
   * @param in 任意类型的对象实例
   * @return java.lang.Double类型
   */
  public static Double castToDouble(final Object in) {
    if (in instanceof Number) {
      return ((Number) in).doubleValue();
    } else if (in instanceof java.util.Date) {
      return (double) ((java.util.Date) in).getTime();
    } else if (in instanceof java.util.Calendar) {
      return (double) ((java.util.Calendar) in).getTime().getTime();
    } else if (in instanceof LocalDateTime) {
      return (double) Timestamp.valueOf((LocalDateTime) in).getTime();
    } else if (in instanceof OffsetDateTime) {
      return (double) Timestamp.valueOf(((OffsetDateTime) in).toLocalDateTime())
          .getTime();
    } else if (in instanceof String || in instanceof Character) {
      try {
        String s = in.toString().trim();
        if (s.equalsIgnoreCase("true")) {
          return Double.valueOf(1);
        } else if (s.equalsIgnoreCase("false")) {
          return Double.valueOf(0);
        } else {
          return Double.parseDouble(s);
        }
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将将java.lang.String类型转换为java.lang.Double类型:%s", e.getMessage()));
      }
    } else if (in instanceof java.sql.Clob) {
      try {
        String s = clob2Str((java.sql.Clob) in).trim();
        if (s.equalsIgnoreCase("true")) {
          return Double.valueOf(1);
        } else if (s.equalsIgnoreCase("false")) {
          return Double.valueOf(0);
        } else {
          return Double.parseDouble(s);
        }
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.sql.Clob类型转换为java.lang.Double类型:%s", e.getMessage()));
      }
    } else if (in instanceof Boolean) {
      return (Boolean) in ? 1d : 0d;
    }

    return null;
  }

  /**
   * 将任意类型转换为java.time.LocalDate类型
   *
   * @param in 任意类型的对象实例
   * @return java.time.LocalDate类型
   */
  public static LocalDate castToLocalDate(final Object in) {
    if (in instanceof java.sql.Time) {
      java.sql.Time date = (java.sql.Time) in;
      LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault())
          .toLocalDate();
      return localDate;
    } else if (in instanceof Timestamp) {
      Timestamp t = (Timestamp) in;
      LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
      return localDateTime.toLocalDate();
    } else if (in instanceof java.util.Date) {
      java.util.Date date = (java.util.Date) in;
      LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault())
          .toLocalDate();
      return localDate;
    } else if (in instanceof java.util.Calendar) {
      java.sql.Date date = new java.sql.Date(((java.util.Calendar) in).getTime().getTime());
      LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault())
          .toLocalDate();
      return localDate;
    } else if (in instanceof LocalDate) {
      return (LocalDate) in;
    } else if (in instanceof LocalTime) {
      return LocalDate.MIN;
    } else if (in instanceof LocalDateTime) {
      return ((LocalDateTime) in).toLocalDate();
    } else if (in instanceof OffsetDateTime) {
      return ((OffsetDateTime) in).toLocalDate();
    } else if (in.getClass().getName().equals("oracle.sql.TIMESTAMP")) {
      Class<?> clz = in.getClass();
      try {
        Method m = clz.getMethod("timestampValue");
        Timestamp date = (Timestamp) m.invoke(in);
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if (in.getClass().getName().equals("microsoft.sql.DateTimeOffset")) {
      Class<?> clz = in.getClass();
      try {
        Method m = clz.getMethod("getTimestamp");
        Timestamp t = (Timestamp) m.invoke(in);
        LocalDateTime localDateTime = LocalDateTime
            .ofInstant(t.toInstant(), ZoneId.systemDefault());
        return localDateTime.toLocalDate();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if (in instanceof String || in instanceof Character) {
      try {
        java.sql.Time date = java.sql.Time.valueOf(in.toString());
        LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault())
            .toLocalDate();
        return localDate;
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(
            String.format("无法将java.lang.String类型转换为java.sql.Time类型:%s", e.getMessage()));
      }
    } else if (in instanceof java.sql.Clob) {
      try {
        java.sql.Time date = java.sql.Time.valueOf(clob2Str((java.sql.Clob) in));
        LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault())
            .toLocalDate();
        return localDate;
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.sql.Clob类型转换为java.sql.Time类型:%s", e.getMessage()));
      }
    } else if (in instanceof Number) {
      Timestamp t = new Timestamp(((Number) in).longValue());
      LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
      return localDateTime.toLocalDate();
    }

    return null;
  }

  /**
   * 将任意类型转换为java.time.LocalTime类型
   *
   * @param in 任意类型的对象实例
   * @return java.time.LocalDate类型
   */
  public static LocalTime castToLocalTime(final Object in) {
    if (in instanceof java.sql.Time) {
      java.sql.Time date = (java.sql.Time) in;
      LocalTime localTime = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault())
          .toLocalTime();
      return localTime;
    } else if (in instanceof Timestamp) {
      Timestamp t = (Timestamp) in;
      LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
      return localDateTime.toLocalTime();
    } else if (in instanceof java.util.Date) {
      return LocalTime.of(0, 0, 0);
    } else if (in instanceof java.util.Calendar) {
      java.sql.Date date = new java.sql.Date(((java.util.Calendar) in).getTime().getTime());
      LocalDateTime localDateTime = Instant.ofEpochMilli(date.getTime())
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime();
      return localDateTime.toLocalTime();
    } else if (in instanceof LocalDate) {
      return LocalTime.of(0, 0, 0);
    } else if (in instanceof LocalTime) {
      return (LocalTime) in;
    } else if (in instanceof LocalDateTime) {
      return ((LocalDateTime) in).toLocalTime();
    } else if (in instanceof OffsetDateTime) {
      return ((OffsetDateTime) in).toLocalTime();
    } else if (in.getClass().getName().equals("oracle.sql.TIMESTAMP")) {
      Class<?> clz = in.getClass();
      try {
        Method m = clz.getMethod("timestampValue");
        Timestamp date = (Timestamp) m.invoke(in);
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault())
            .toLocalDateTime();
        return localDateTime.toLocalTime();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if (in.getClass().getName().equals("microsoft.sql.DateTimeOffset")) {
      Class<?> clz = in.getClass();
      try {
        Method m = clz.getMethod("getTimestamp");
        Timestamp t = (Timestamp) m.invoke(in);
        LocalDateTime localDateTime = LocalDateTime
            .ofInstant(t.toInstant(), ZoneId.systemDefault());
        return localDateTime.toLocalTime();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if (in instanceof String || in instanceof Character) {
      try {
        java.sql.Time date = java.sql.Time.valueOf(in.toString());
        return LocalTime.ofSecondOfDay(date.getTime());
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(
            String.format("无法将java.lang.String类型转换为java.sql.Time类型:%s", e.getMessage()));
      }
    } else if (in instanceof java.sql.Clob) {
      try {
        java.sql.Time date = java.sql.Time.valueOf(clob2Str((java.sql.Clob) in));
        return LocalTime.ofSecondOfDay(date.getTime());
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.sql.Clob类型转换为java.sql.Time类型:%s", e.getMessage()));
      }
    } else if (in instanceof Number) {
      Timestamp t = new Timestamp(((Number) in).longValue());
      LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
      return localDateTime.toLocalTime();
    }

    return null;
  }

  /**
   * 将任意类型转换为java.time.LocalDateTime类型
   *
   * @param in 任意类型的对象实例
   * @return java.time.LocalDateTime类型
   */
  public static LocalDateTime castToLocalDateTime(final Object in) {
    if (in instanceof Timestamp) {
      Timestamp t = (Timestamp) in;
      LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
      return localDateTime;
    } else if (in instanceof java.sql.Date) {
      java.sql.Date date = (java.sql.Date) in;
      LocalDate localDate = date.toLocalDate();
      LocalTime localTime = LocalTime.of(0, 0, 0);
      LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
      return localDateTime;
    } else if (in instanceof java.sql.Time) {
      java.sql.Time date = (java.sql.Time) in;
      Timestamp t = new Timestamp(date.getTime());
      LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
      return localDateTime;
    } else if (in instanceof java.util.Date) {
      Timestamp t = new Timestamp(((java.util.Date) in).getTime());
      LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
      return localDateTime;
    } else if (in instanceof java.util.Calendar) {
      Timestamp t = new Timestamp(((java.util.Calendar) in).getTime().getTime());
      LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
      return localDateTime;
    } else if (in instanceof LocalDate) {
      LocalDate localDate = (LocalDate) in;
      LocalTime localTime = LocalTime.of(0, 0, 0);
      LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
      return localDateTime;
    } else if (in instanceof LocalTime) {
      LocalDate localDate = LocalDate.MIN;
      LocalTime localTime = (LocalTime) in;
      LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
      return localDateTime;
    } else if (in instanceof LocalDateTime) {
      return (LocalDateTime) in;
    } else if (in instanceof OffsetDateTime) {
      return ((OffsetDateTime) in).toLocalDateTime();
    } else if (in.getClass().getName().equals("oracle.sql.TIMESTAMP")) {
      Class<?> clz = in.getClass();
      try {
        Method m = clz.getMethod("timestampValue");
        Timestamp t = (Timestamp) m.invoke(in);
        LocalDateTime localDateTime = LocalDateTime
            .ofInstant(t.toInstant(), ZoneId.systemDefault());
        return localDateTime;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if (in.getClass().getName().equals("microsoft.sql.DateTimeOffset")) {
      Class<?> clz = in.getClass();
      try {
        Method m = clz.getMethod("getTimestamp");
        Timestamp t = (Timestamp) m.invoke(in);
        LocalDateTime localDateTime = LocalDateTime
            .ofInstant(t.toInstant(), ZoneId.systemDefault());
        return localDateTime;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if (in instanceof String || in instanceof Character) {
      try {
        Timestamp t = Timestamp.valueOf(in.toString());
        LocalDateTime localDateTime = LocalDateTime
            .ofInstant(t.toInstant(), ZoneId.systemDefault());
        return localDateTime;
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(
            String.format("无法将java.lang.String类型转换为java.sql.TimeStamp类型:%s", e.getMessage()));
      }
    } else if (in instanceof java.sql.Clob) {
      try {
        Timestamp t = Timestamp.valueOf(clob2Str((java.sql.Clob) in));
        LocalDateTime localDateTime = LocalDateTime
            .ofInstant(t.toInstant(), ZoneId.systemDefault());
        return localDateTime;
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.sql.Clob类型转换为java.sql.TimeStamp类型:%s", e.getMessage()));
      }
    } else if (in instanceof Number) {
      Timestamp t = new Timestamp(((Number) in).longValue());
      LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
      return localDateTime;
    }

    return null;
  }

  /**
   * 将任意类型转换为java.time.LocalDateTime类型
   *
   * @param in 任意类型的对象实例
   * @return java.sql.Timestamp类型
   */
  public static Timestamp castToTimestamp(final Object in) {
    if (in instanceof Timestamp) {
      return (Timestamp) in;
    } else if (in instanceof java.sql.Date) {
      java.sql.Date date = (java.sql.Date) in;
      LocalDate localDate = date.toLocalDate();
      LocalTime localTime = LocalTime.of(0, 0, 0);
      LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
      return Timestamp.valueOf(localDateTime);
    } else if (in instanceof java.sql.Time) {
      java.sql.Time date = (java.sql.Time) in;
      return new Timestamp(date.getTime());
    } else if (in instanceof java.util.Date) {
      return new Timestamp(((java.util.Date) in).getTime());
    } else if (in instanceof java.util.Calendar) {
      return new Timestamp(((java.util.Calendar) in).getTime().getTime());
    } else if (in instanceof LocalDate) {
      LocalDate localDate = (LocalDate) in;
      LocalTime localTime = LocalTime.of(0, 0, 0);
      LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
      return Timestamp.valueOf(localDateTime);
    } else if (in instanceof LocalTime) {
      LocalDate localDate = LocalDate.MIN;
      LocalTime localTime = (LocalTime) in;
      LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
      return Timestamp.valueOf(localDateTime);
    } else if (in instanceof LocalDateTime) {
      return Timestamp.valueOf((LocalDateTime) in);
    } else if (in instanceof OffsetDateTime) {
      return Timestamp.valueOf(((OffsetDateTime) in).toLocalDateTime());
    } else if (in.getClass().getName().equals("oracle.sql.TIMESTAMP")) {
      Class<?> clz = in.getClass();
      try {
        Method m = clz.getMethod("timestampValue");
        Timestamp t = (Timestamp) m.invoke(in);
        LocalDateTime localDateTime = LocalDateTime
            .ofInstant(t.toInstant(), ZoneId.systemDefault());
        return Timestamp.valueOf(localDateTime);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if (in.getClass().getName().equals("microsoft.sql.DateTimeOffset")) {
      Class<?> clz = in.getClass();
      try {
        Method m = clz.getMethod("getTimestamp");
        Timestamp t = (Timestamp) m.invoke(in);
        LocalDateTime localDateTime = LocalDateTime
            .ofInstant(t.toInstant(), ZoneId.systemDefault());
        return Timestamp.valueOf(localDateTime);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if (in instanceof String || in instanceof Character) {
      try {
        Timestamp t = Timestamp.valueOf(in.toString());
        LocalDateTime localDateTime = LocalDateTime
            .ofInstant(t.toInstant(), ZoneId.systemDefault());
        return Timestamp.valueOf(localDateTime);
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(
            String.format("无法将java.lang.String类型转换为java.sql.TimeStamp类型:%s", e.getMessage()));
      }
    } else if (in instanceof java.sql.Clob) {
      try {
        Timestamp t = Timestamp.valueOf(clob2Str((java.sql.Clob) in));
        LocalDateTime localDateTime = LocalDateTime
            .ofInstant(t.toInstant(), ZoneId.systemDefault());
        return Timestamp.valueOf(localDateTime);
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.sql.Clob类型转换为java.sql.TimeStamp类型:%s", e.getMessage()));
      }
    } else if (in instanceof Number) {
      Timestamp t = new Timestamp(((Number) in).longValue());
      LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
      return Timestamp.valueOf(localDateTime);
    }

    return null;
  }

  /**
   * 将任意类型转换为Boolean类型
   *
   * @param in 任意类型的对象实例
   * @return Boolean类型
   */
  public static Boolean castToBoolean(final Object in) {
    if (in instanceof Boolean) {
      return (Boolean) in;
    } else if (in instanceof Number) {
      return ((Number) in).intValue() != 0;
    } else if (in instanceof String || in instanceof Character) {
      try {
        return Boolean.parseBoolean(in.toString());
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(
            String.format("无法将java.lang.String类型转换为java.lang.Boolean类型:%s", e.getMessage()));
      }
    } else if (in instanceof java.sql.Clob) {
      try {
        return Boolean.parseBoolean(clob2Str((java.sql.Clob) in));
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            String.format("无法将java.sql.Clob类型转换为java.lang.Boolean类型:%s", e.getMessage()));
      }
    }

    return null;
  }

  public static byte[] blob2Bytes(java.sql.Blob blob) {
    try (java.io.InputStream inputStream = blob.getBinaryStream()) {
      try (java.io.BufferedInputStream is = new java.io.BufferedInputStream(inputStream)) {
        byte[] bytes = new byte[(int) blob.length()];
        int len = bytes.length;
        int offset = 0;
        int read = 0;
        while (offset < len && (read = is.read(bytes, offset, len - offset)) >= 0) {
          offset += read;
        }
        return bytes;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String clob2Str(java.sql.Clob clob) {
    try (java.io.Reader is = clob.getCharacterStream()) {
      java.io.BufferedReader reader = new java.io.BufferedReader(is);
      StringBuffer sb = new StringBuffer();
      char[] buffer = new char[4096];
      for (int i = reader.read(buffer); i > 0; i = reader.read(buffer)) {
        sb.append(buffer, 0, i);
      }
      return sb.toString();
    } catch (SQLException | java.io.IOException e) {
      log.warn("Field Value convert from java.sql.Clob to java.lang.String failed:", e);
      return null;
    }
  }

  public static String castToString(final Object in) {
    if (in instanceof Character) {
      return in.toString();
    } else if (in instanceof String) {
      return in.toString();
    } else if (in instanceof Character) {
      return in.toString();
    } else if (in instanceof java.sql.Clob) {
      return clob2Str((java.sql.Clob) in);
    } else if (in instanceof java.sql.Blob) {
      return Base64.encode(blob2Bytes((java.sql.Blob) in));
    } else if (in instanceof Number) {
      return in.toString();
    } else if (in instanceof java.sql.RowId) {
      return in.toString();
    } else if (in instanceof Boolean) {
      return in.toString();
    } else if (in instanceof java.util.Date) {
      return in.toString();
    } else if (in instanceof LocalDate) {
      return in.toString();
    } else if (in instanceof LocalTime) {
      return in.toString();
    } else if (in instanceof LocalDateTime) {
      return in.toString();
    } else if (in instanceof OffsetDateTime) {
      return in.toString();
    } else if (in instanceof java.sql.SQLXML) {
      return in.toString();
    } else if (in instanceof java.sql.Array) {
      return in.toString();
    } else if (in instanceof java.util.UUID) {
      return in.toString();
    } else if ("org.postgresql.util.PGobject".equals(in.getClass().getName())) {
      return in.toString();
    } else if ("org.postgresql.jdbc.PgSQLXML".equals(in.getClass().getName())) {
      try {
        Class<?> clz = in.getClass();
        Method getString = clz.getMethod("getString");
        return getString.invoke(in).toString();
      } catch (Exception e) {
        return "";
      }
    } else if (in.getClass().getName().equals("oracle.sql.INTERVALDS")) {
      return in.toString();
    } else if (in.getClass().getName().equals("oracle.sql.INTERVALYM")) {
      return in.toString();
    } else if (in.getClass().getName().equals("oracle.sql.TIMESTAMPLTZ")) {
      return in.toString();
    } else if (in.getClass().getName().equals("oracle.sql.TIMESTAMPTZ")) {
      return in.toString();
    } else if (in.getClass().getName().equals("oracle.sql.BFILE")) {
      Class<?> clz = in.getClass();
      try {
        Method methodFileExists = clz.getMethod("fileExists");
        boolean exists = (boolean) methodFileExists.invoke(in);
        if (!exists) {
          return "";
        }

        Method methodOpenFile = clz.getMethod("openFile");
        methodOpenFile.invoke(in);

        try {
          Method methodCharacterStreamValue = clz.getMethod("getBinaryStream");
          java.io.InputStream is = (java.io.InputStream) methodCharacterStreamValue.invoke(in);

          String line;
          StringBuilder sb = new StringBuilder();

          java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is));
          while ((line = br.readLine()) != null) {
            sb.append(line);
          }

          return sb.toString();
        } finally {
          Method methodCloseFile = clz.getMethod("closeFile");
          methodCloseFile.invoke(in);
        }
      } catch (java.lang.reflect.InvocationTargetException ex) {
        log.warn("Error for handle oracle.sql.BFILE: ", ex);
        return "";
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if (in.getClass().getName().equals("microsoft.sql.DateTimeOffset")) {
      return in.toString();
    } else if (in instanceof byte[]) {
      return new String((byte[]) in);
    } else if (in instanceof Map) {
      return JSONUtil.toJsonStr(in);
    } else if(in instanceof Collection){
      return JSONUtil.toJsonStr(in);
    }

    return null != in ? in.toString() : null;
  }

  public static String objectToString(final Object in) {
    String v = in.toString();
    String a = in.getClass().getName() + "@" + Integer.toHexString(in.hashCode());
    if (a.length() == v.length() && StringUtils.equals(a, v)) {
      throw new UnsupportedOperationException("Unsupported convert "
          + in.getClass().getName() + " to java.lang.String");
    }

    return v;
  }

  public static Object castByJdbcType(int jdbcType, Object value) {
    switch (jdbcType) {
      case Types.BIT:
      case Types.TINYINT:
        return convert(value, ObjectCastUtils::castToByte);
      case Types.SMALLINT:
        return convert(value, ObjectCastUtils::castToShort);
      case Types.INTEGER:
        return convert(value, ObjectCastUtils::castToInteger);
      case Types.BIGINT:
        return convert(value, ObjectCastUtils::castToLong);
      case Types.NUMERIC:
      case Types.DECIMAL:
        return convert(value, ObjectCastUtils::castToNumeric);
      case Types.FLOAT:
      case Types.REAL:
        return convert(value, ObjectCastUtils::castToFloat);
      case Types.DOUBLE:
        return convert(value, ObjectCastUtils::castToDouble);
      case Types.BOOLEAN:
        return convert(value, ObjectCastUtils::castToBoolean);
      case Types.TIME:
        return convert(value, ObjectCastUtils::castToLocalTime);
      case Types.DATE:
        return convert(value, ObjectCastUtils::castToLocalDate);
      case Types.TIMESTAMP:
        return convert(value, ObjectCastUtils::castToTimestamp);
      case Types.BINARY:
      case Types.VARBINARY:
      case Types.BLOB:
      case Types.LONGVARBINARY:
        return convert(value, ObjectCastUtils::castToByteArray);
      case Types.CHAR:
      case Types.NCHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
      case Types.NVARCHAR:
      case Types.LONGNVARCHAR:
      case Types.CLOB:
      case Types.NCLOB:
      case Types.NULL:
      case Types.OTHER:
      default:
        return convert(value, ObjectCastUtils::castToString);
    }
  }

  private static Object convert(Object value, Function<Object, Object> func) {
    try {
      return func.apply(value);
    } catch (Exception e) {
      return null;
    }
  }

  public static Object castByDetermine(final Object in) {
    if (null == in) {
      return null;
    }

    if (in instanceof BigInteger) {
      return ((BigInteger) in).longValue();
    } else if (in instanceof BigDecimal) {
      BigDecimal decimal = (BigDecimal) in;
      return decimal.doubleValue();
    } else if (in instanceof java.sql.Clob) {
      return clob2Str((java.sql.Clob) in);
    } else if (in instanceof java.sql.Array
        || in instanceof java.sql.SQLXML) {
      try {
        return objectToString(in);
      } catch (Exception e) {
        log.warn("Unsupported type for convert {} to java.lang.String", in.getClass().getName());
        return null;
      }
    } else if (in instanceof java.sql.Blob) {
      try {
        return blob2Bytes((java.sql.Blob) in);
      } catch (Exception e) {
        log.warn("Unsupported type for convert {} to byte[] ", in.getClass().getName());
        return null;
      }
    } else if (in instanceof java.sql.Struct) {
      log.warn("Unsupported type for convert {} to java.lang.String", in.getClass().getName());
      return null;
    }

    return in;
  }

}
