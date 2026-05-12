package com.sunyard.module.auth.util;
/*
 * Project: Sunyard
 *
 * File Created at 2025/9/22
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CaptchaUtil;

/**
 * @author Leo
 * @Desc
 * @date 2025/9/22 9:02
 */
public class VerifyCodeUtils {

    /**
     * 根据类型生成验证码
    * */
    public static AbstractCaptcha generateVerifyCode(String verifyCodeType) {
        AbstractCaptcha captcha = null;
        switch (verifyCodeType) {
            case "circle":
                captcha = CaptchaUtil.createCircleCaptcha(130, 48);
                break;
            case "shear":
                captcha = CaptchaUtil.createShearCaptcha(130, 48);
                break;
            case "line":
                 captcha = CaptchaUtil.createLineCaptcha(130, 48);
                break;
            default:
                captcha = CaptchaUtil.createGifCaptcha(130, 48);
        }
        return captcha;
    }
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/9/22 Leo creat
 */
