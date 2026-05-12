package com.sunyard.framework.common.util.date;
/*
 * Project: com.sunyard.am.utils
 *
 * File Created at 2021/7/1
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.utils
 * @Desc
 * @date 2021/7/1 15:45
 */
@Slf4j
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    private static final String[] PARSE_PATTERNS =
        {"yyyy-MM-dd", "yyyyMMdd", "yyyy年MM月dd日", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd HH:mm", "yyyy/MM", "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss SSS");
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
    private static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy年MM月dd日HH:mm");

    /**
     * 获取当前Date型日期
     *
     * @return Result Date() 当前日期
     */
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * @return Result String
     */
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    /**
     * 获取YYYY_MM_DD_HH_MM_SS格式的时间
     * @return String
     */
    public static final String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 获取YYYYMMDDHHMMSS格式的时间
     * @return String
     */
    public static final String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    /**
     * 获取指定format格式的时间
     * @param format 格式
     * @return String
     */
    public static final String dateTimeNow(final String format) {
        return parseDateToStr(format, new Date());
    }

    /**
     * date转成YYYY_MM_DD的时间
     * @param date 时间对象
     * @return String
     */
    public static final String dateTime(final Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    /**
     * date转成指定格式的时间String对象
     * @param format 指定格式
     * @param date 日期对象
     * @return String
     */
    public static final String parseDateToStr(final String format, final Date date) {
        try {
            return new SimpleDateFormat(format).format(date);
        } catch (Exception e) {
            log.error("系统异常", e);
            return null;
        }
    }

    /**
     * 格式化时间成Date
     * @param format 格式
     * @param ts string类型时间
     * @return date对象
     */
    public static final Date dateTime(final String format, final String ts) {
        try {
            return new SimpleDateFormat(format).parse(ts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static final String dateTime() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 日期型字符串转化为日期 格式
     * @param str string格式时间
     * @return
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return org.apache.commons.lang3.time.DateUtils.parseDate(str.toString(), PARSE_PATTERNS);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 计算两个时间差
     * @param endDate 结束时间
     * @param nowDate 开始时间
     * @return 差值
     */
    public static String getDatePoor(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     * 获取指定时间的那天 00:00:00.000 的时间
     *
     * @param date 日期
     * @return Result
     */
    public static Date getDayBeginTime(final Date date) {
        if (null == date) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 获取指定时间的那天 23:59:59.999 的时间
     *
     * @param date 日期
     * @return Result
     */
    public static Date getDayEndTime(final Date date) {
        if (null == date) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    /**
     * 将Timestamp类型转换为long类型
     * @param timeStamp 时间戳
     * @return Result long
     * @throws ParseException 将Timestamp类型转换为long类型
     */
    public static Long timeStampToDateTime(String timeStamp) throws ParseException {
        Long result = null;
        // 2020年08月20日 09:18:45 2020年08月20日 09:18
        if (timeStamp.length() == 20) {
            result = sdf2.parse(timeStamp).getTime();
        } else {
            result = sdf3.parse(timeStamp).getTime();
        }
        return result;
    }

    /**
     * 将String类型转换为long类型
     * @param timeStamp string类型时间戳
     * @return Long
     * @throws ParseException 解析异常
     */
    public static Long stringToTimeStamp(String timeStamp) throws ParseException {
        return sdf2.parse(timeStamp).getTime();
    }

    /**
     * 将java.util.Date对象转化为java.sql.Timestamp对象 获取当前时间
     * 
     * @return Result
     */
    public static Timestamp dateTimeToTimeStamp() {
        return new Timestamp(getTimestamp());
    }

    /**
     *  获取指定格式的当前时间
     * @param format 格式
     * @return Result 获取当前时间
     */
    public static String getCurrentDate(String format) {
        return new SimpleDateFormat(format).format(new Date());
    }

    /**
     *
     * @return Result long类型的时间
     */
    public static long getTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 去除年、月、日
     * @param str String格式的时间
     * @return Result
     */
    public static String changeLongDate(String str) {
        return str.replaceAll("年", "").replaceAll("月", "").replaceAll("日", "").replaceAll(":", "").replaceAll(" ", "");
    }

    /**
     * 将日期转化为日期字符串。失败返回null。
     *
     * @param date     日期
     * @param parttern 日期格式
     * @return 日期字符串
     */
    public static String dateToString(Date date, String parttern) {
        String dateString = null;
        if (date != null) {
            try {
                dateString = getDateFormat(parttern).format(date);
            } catch (Exception e) {
                log.error("系统异常",e);
            }
        }
        return dateString;
    }

    /**
     * 获取SimpleDateFormat
     *
     * @param parttern 日期格式
     * @return SimpleDateFormat对象
     * @throws RuntimeException 异常：非法日期格式
     */
    public static SimpleDateFormat getDateFormat(String parttern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(parttern);
        simpleDateFormat.setLenient(false);
        return simpleDateFormat;
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/7/1 zhouleibin creat
 */
