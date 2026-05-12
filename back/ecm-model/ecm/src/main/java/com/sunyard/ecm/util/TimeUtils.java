package com.sunyard.ecm.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class TimeUtils {
    /**
     * 某个日期是否在某个时间段内包含边界日期
     *
     * @param timeStr
     * @param startStr
     * @param endStr
     * @return
     */
    public static boolean isInInterval(String timeStr, String startStr, String endStr) {
        Date time = null;
        Date start = null;
        Date end = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            time = sdf.parse(timeStr);
            start = sdf.parse(startStr);
            end = sdf.parse(endStr);
        } catch (ParseException e) {
            log.error("{},{},{}时间转换错误", timeStr, startStr, endStr, e);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(end);
        endCalendar.add(Calendar.DAY_OF_MONTH, 1); // 增加 1 天使区间闭合

        return calendar.compareTo(startCalendar) >= 0 && calendar.compareTo(endCalendar) < 0;
    }
}
