package com.sunyard.ecm.oldToNew.socket.util;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtils {

	public static String getCurrentDateTime(String pattern) {
		return getCurrentDateTime(pattern, null);
	}

	public static String getCurrentDateTime(String pattern, String zone) {
		DateFormat df = getDateFormat(pattern);
		if (zone != null && !zone.equals("")) {
			df.setTimeZone(TimeZone.getTimeZone(zone));
		}
		return df.format(new Date());
	}

	public static Calendar getCalendar(String pattern, String dateTime) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(formatStrToDate(pattern, dateTime));
		return calendar;
	}

	public static String getDateTime(String pattern, long timeMillis) {
		DateFormat df = getDateFormat(pattern);
		Date date = new Date(timeMillis);
		return df.format(date);
	}
	
	public static String formatDateTime(String oldPattern, String newPattern,
			String dateTime) {
		DateFormat df = getDateFormat(oldPattern);
		Date date = df.parse(dateTime, new ParsePosition(0));
		df = getDateFormat(newPattern);
		return df.format(date);
	}

	public static String formatDateToStr(String pattern, Date date) {
		return getDateFormat(pattern).format(date);
	}
	
	public static Date formatStrToDate(String pattern, String dateTime) {
		DateFormat df = getDateFormat(pattern);
		return df.parse(dateTime, new ParsePosition(0));
	}

	/*
	 * Get date format by pattern.
	 */
	protected static DateFormat getDateFormat(String pattern) {
		return new SimpleDateFormat(pattern);
	}
	
	public static int timeConsume(long startTime){
		long endTime = System.currentTimeMillis();
		return (int)(endTime - startTime);
	}
	
}
