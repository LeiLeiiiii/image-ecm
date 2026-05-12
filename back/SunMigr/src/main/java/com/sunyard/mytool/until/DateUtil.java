package com.sunyard.mytool.until;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * 时间工具类
 */
public class DateUtil {
    public static final String sdf0 = "yyyy-MM-dd HH:mm:ss";
    public static final String sdf1 = "HH:mm:ss";
    public static final String sdf2 = "yyyy年MM月dd日HH:mm:ss";
    public static final String sdf3 = "yyyy年MM月dd日HH:mm";
    public static final String sdf4 = "yyyyMMdd";
    public static final String sdf5 = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final Calendar calendar = Calendar.getInstance();


    /**
     * @param sdf
     * @return 获取当前时间
     */
    public static String getDateString(SimpleDateFormat sdf, Date date) {
        return sdf.format(date);
    }


    /**
     * @param dateString
     * @param simpleDateFormat
     * @return
     */
    public static Date parseToDate(String dateString, SimpleDateFormat simpleDateFormat)  {
        try {
            return simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException("日期转换异常:"+dateString);
        }
    }

    /**
     * 获取指定日期formDate增加指定日数dayCount后的日期
     *
     * @param formDate
     * @param dayCount
     * @return
     */
    public static Date getDateAddDay(Date formDate, int dayCount) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(formDate.toInstant(), ZoneId.systemDefault());
        localDateTime = localDateTime.plusDays(dayCount);
        Date targetDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return targetDate;
    }

    /**
     * 获取指定日期formDate增加指定月数monthCount后的日期
     *
     * @param formDate
     * @param monthCount
     * @return
     */
    public static Date getDateAddMonth(Date formDate, int monthCount) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(formDate.toInstant(), ZoneId.systemDefault());
        localDateTime = localDateTime.plusMonths(monthCount);
        Date targetDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return targetDate;
    }

    /**
     * 获取指定日期formDate增加指定小时数hourCount后的日期
     *
     * @param formDate
     * @param hourCount
     * @return
     */
    public static Date getDateAddHour(Date formDate, int hourCount) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(formDate.toInstant(), ZoneId.systemDefault());
        localDateTime = localDateTime.plusHours(hourCount);
        Date targetDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return targetDate;
    }

    /**
     * 获取指定日期formDate增加指定分钟数minuteCount后的日期
     *
     * @param formDate
     * @param minuteCount
     * @return
     */
    public static Date getDateAddMinutes(Date formDate, int minuteCount) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(formDate.toInstant(), ZoneId.systemDefault());
        localDateTime = localDateTime.plusMinutes(minuteCount);
        Date targetDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return targetDate;
    }


    /**
     * @return long类型的时间
     */
    public static long getTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * @param str
     * @return
     */
    public static String changeLongDate(String str) {
        return str.replaceAll("年", "").replaceAll("月", "").replaceAll("日", "").replaceAll(":", "").replaceAll(" ", "");
    }

    private static boolean isSpanDay(String startTime, String endTime) {
        String[] startTimeArray = startTime.split(":");
        String[] endTimeArray = endTime.split(":");
        for (int i = 0; i < 3; i++) {
            int startTimeInt = Integer.parseInt(startTimeArray[i]);
            int endTimeInt = Integer.parseInt(endTimeArray[i]);
            if (startTimeInt == endTimeInt) {
                continue;
            } else if (startTimeInt > endTimeInt) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public static boolean isTargetDateInTargetTime(Date targetDate, String startTime, String endTime) {
        String regexStr = "\\d+:\\d+:\\d+";
        if (startTime == null || !Pattern.matches(regexStr, startTime)) {
            throw new RuntimeException("非法的startTime");
        }
        if (endTime == null || !Pattern.matches(regexStr, endTime)) {
            throw new RuntimeException("非法的endTime");
        }
        SimpleDateFormat sdf=new SimpleDateFormat(sdf1);
        String targetTimeStr = sdf.format(targetDate);

        String[] targetTimeArray = targetTimeStr.split(":");
        String[] startTimeArray = startTime.split(":");
        String[] endTimeArray = endTime.split(":");
        boolean isSpanDay = isSpanDay(startTime, endTime);
        if (isSpanDay) {
            //跨日规则下判断
            for (int i = 0; i < 3; i++) {
                int startTimeInt = Integer.parseInt(startTimeArray[i]);
                int endTimeInt = Integer.parseInt(endTimeArray[i]);
                int targetTimeInt = Integer.parseInt(targetTimeArray[i]);
                if (startTimeInt == targetTimeInt || endTimeInt == targetTimeInt) {
                    continue;
                } else if (targetTimeInt > startTimeInt || targetTimeInt < endTimeInt) {
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            //非跨日规则下判断
            for (int i = 0; i < 3; i++) {
                int startTimeInt = Integer.parseInt(startTimeArray[i]);
                int endTimeInt = Integer.parseInt(endTimeArray[i]);
                int targetTimeInt = Integer.parseInt(targetTimeArray[i]);
                if (startTimeInt == targetTimeInt || endTimeInt == targetTimeInt) {
                    continue;
                } else if (targetTimeInt > startTimeInt && targetTimeInt < endTimeInt) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否未来日期,仅判断日期,不判断时间
     *
     * @param targetDate
     * @return
     * @throws Exception
     */
    public static boolean isFutureDate(Date targetDate) throws Exception {
        LocalDateTime targetLocalDateTime = date2LocalDateTime(targetDate);
        LocalDate now = LocalDate.now(ZoneId.systemDefault());
        //实际判断targetDate的时间是否超过明天0点0分0秒
        LocalDate tomorrow = now.plusDays(1);
        return targetLocalDateTime.isAfter(tomorrow.atTime(0, 0, 0));
    }

    /**
     * 判断是否为当时或者未来
     *
     * @param compareOriDate 与此时间对比是否为当时或者未来
     * @param targetDate
     * @return
     * @throws Exception
     */
    public static boolean isSameOrFutureTime(Date compareOriDate, Date targetDate) {
        return targetDate.compareTo(compareOriDate)>=0;
    }

    public static LocalDateTime date2LocalDateTime(Date date) {
        LocalDateTime localDateTime = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return localDateTime;
    }

    /**
     * 获取今天00:00:00时的Date
     *
     * @return
     * @throws Exception
     */
    public static Date getTodayMidnightDate() {
        LocalDate now = LocalDate.now(ZoneId.systemDefault());
        return localDate2Date(now);
    }

    public static Date localDate2Date(LocalDate localDate) {
        if (null == localDate) {
            return null;
        }
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * 获取指定日期23:59:59时Date
     *
     * @param targetDate
     * @return
     * @throws Exception
     */
    public static Date getTargetDateEnd(Date targetDate) throws Exception {
        LocalDate targetLocalDate = date2LocalDate(targetDate);
        LocalDateTime localDateTime = targetLocalDate.atTime(23, 59, 59);
        return localDateTime2Date(localDateTime);
    }

    public static LocalDate date2LocalDate(Date date) {
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return localDate;
    }

    public static Date localDateTime2Date(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * 判断是否当日日期,仅判断日期,不判断时间
     *
     * @param targetDate
     * @return
     */
    public static boolean isNowDate(Date targetDate) {
        SimpleDateFormat sdf=new SimpleDateFormat(sdf4);
        String nowDteString = sdf.format(new Date());
        String targetDateString = sdf.format(targetDate);
        return nowDteString.equals(targetDateString);
    }

    /**
     * 判断date1是否大于等于date2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean compateDateEqueal(Date date1, Date date2) {
        return (date1.getTime() - date2.getTime()) >= 0;
    }
    /**
     * 判断date1是否大于等于date2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean compateDate(Date date1, Date date2) {
        return (date1.getTime() - date2.getTime()) > 0;
    }


}
