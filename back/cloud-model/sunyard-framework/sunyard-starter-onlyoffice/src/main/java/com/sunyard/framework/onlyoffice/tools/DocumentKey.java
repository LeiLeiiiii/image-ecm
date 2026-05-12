package com.sunyard.framework.onlyoffice.tools;

import java.util.Calendar;
import java.util.Random;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;



/**
 * @author 朱山成
 */
public class DocumentKey {

    private static Snowflake snowflake;

    static {
        // 0 ~ 31 位，可以采用配置的方式使用
        long workerId;
        try {
            workerId = NetUtil.ipv4ToLong(NetUtil.getLocalhostStr());
        } catch (Exception e) {
            workerId = NetUtil.getLocalhostStr().hashCode();
        }

        workerId = workerId >> 16 & 31;

        long dataCenterId = 1L;
        snowflake = new Snowflake(workerId,dataCenterId);
    }

    /**
     * 生成id
     * @return Result
     */
    public static String snowflakeId() {
        return String.valueOf(snowflake.nextId());
    }

    /**
     * 生成短码
     * @return Result
     */
    public static String shortCode() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        // 打乱排序：2020年为准 + 小时 + 周期 + 日 + 三位随机数
        StringBuilder idStr = new StringBuilder();
        idStr.append(year-2020);
        idStr.append(hour);
        //(“%04d”, 99)	0099
        idStr.append(String.format("%02d",week));
        idStr.append(day);
        idStr.append(String.format("%03d",new Random().nextInt(1000)));

        return idStr.toString();
    }
}
