package com.sunyard.ecm.oldToNew.socket.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期时间工具类
 * 
 * @author wuzelin
 * 
 */
public class DateUtils {

	/**
	 * 时间格式转换
	 * 
	 * @param date
	 * @param str
	 * @return
	 */
	public static String formatDate(Date date, String str) {
		SimpleDateFormat format = new SimpleDateFormat(str);
		return format.format(date);
	}

	/**
	 * 获取当前时间
	 * 
	 * @return
	 */
	public static Date getDateNow() {
		Date date = Calendar.getInstance().getTime();
		return date;
	}

	/**
	 * 2010-01-10
	 * 
	 * @return
	 */
	public static String getDateStr() {
		return formatDate(getDateNow(), "yyyy-MM-dd");
	}

	/**
	 * 2010年01月10日
	 * 
	 * @return
	 */
	public static String getDateStrC() {
		return formatDate(getDateNow(), "yyyy年MM月dd日");
	}

	/**
	 * 2010-01-10 11:36:58
	 * 
	 * @return
	 */
	public static String getDateTimeStr() {
		return formatDate(getDateNow(), "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 2010年01月10日 11:36:58
	 * 
	 * @return
	 */
	public static String getDateTimeStrC() {
		return formatDate(getDateNow(), "yyyy年MM月dd日 HH时mm分ss秒");
	}

	/**
	 * 11:29:27
	 * 
	 * @return
	 */
	public static String getTimeStr() {
		return formatDate(getDateNow(), "HH:mm:ss");
	}

	/**
	 * 11时29分27秒
	 * 
	 * @return
	 */
	public static String getTimeStrC() {
		return formatDate(getDateNow(), "HH时mm分ss秒");
	}

	/**
	 * 20110107
	 * 
	 * @return
	 */
	public static String getDateStrCompact() {
		return formatDate(getDateNow(), "yyyyMMdd");
	}

	/**
	 * 20110107113209
	 * 
	 * @return
	 */
	public static String getDateTimeStrCompact() {
		return formatDate(getDateNow(), "yyyyMMddHHmmss");
	}

	/**
	 * 2010
	 * 
	 * @return
	 */
	public static String getYearStr() {
		return formatDate(getDateNow(), "yyyy");
	}

	/**
	 * 2010年
	 * 
	 * @return
	 */
	public static String getYearStrC() {
		return formatDate(getDateNow(), "yyyy年");
	}

	/**
	 * 01
	 * 
	 * @return
	 */
	public static String getMonthStr() {
		return formatDate(getDateNow(), "MM");
	}

	/**
	 * 01月
	 * 
	 * @return
	 */
	public static String getMonthStrC() {
		return formatDate(getDateNow(), "MM月");
	}

	/**
	 * 07
	 * 
	 * @return
	 */
	public static String getDayStr() {
		return formatDate(getDateNow(), "dd");
	}

	/**
	 * 07日
	 * 
	 * @return
	 */
	public static String getDayStrC() {
		return formatDate(getDateNow(), "dd日");
	}

	/**
	 * 13 hour
	 * 
	 * @return
	 */
	public static String getHour() {
		return formatDate(getDateNow(), "HH");
	}

	/**
	 * 
	 * @Description 获取当前日期早多少天或者晚多少天时间
	 * @param num
	 *            负数代表后几天，正数代表前几天
	 * @param format
	 *            输出日期格式
	 * @return
	 */
	public static String getDateBefAft(int dayNum, String format) {
		Calendar now = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		now.add(Calendar.DAY_OF_YEAR, -dayNum);
		return formatter.format(now.getTime());
	}

	public static Date strToDateLong(String parameter) {
		// TODO Auto-generated method stub
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(parameter, pos);
		return strtodate;
	}
}
