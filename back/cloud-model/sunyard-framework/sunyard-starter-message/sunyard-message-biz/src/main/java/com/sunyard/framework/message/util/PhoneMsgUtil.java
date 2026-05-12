package com.sunyard.framework.message.util;

import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.provider.enumerate.SupplierType;

/**
 * @author P-JWei
 * @date 2023/7/5 9:20
 * @title 手机短信
 * @description
 */
public class PhoneMsgUtil {
    SmsResponse smsResponse = SmsFactory.createSmsBlend(SupplierType.ALIBABA).sendMessage("11111111111", "下班了哥们，跑~~~");

    /**
     * 短信发送
     *
     * @param msg   短信内容
     * @param phone 手机号码
     * @return Result
     */
    public static boolean sendToUser(String msg, String phone) {
        SmsResponse smsResponse = SmsFactory.createSmsBlend(SupplierType.ALIBABA).sendMessage("11111111111", "下班了哥们，跑~~~");
        return true;
    }
}
