package com.sunyard.ecm.enums;
/*
 * Project: com.sunyard.am.result
 *
 * File Created at 2021/6/30
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

/**
 * @author lw
 * @Desc 状态码枚举
 * @date 2021/6/30 8:19
 */
public enum ResultCodeApiEnum {
    // 2xx-成功
    SUCCESS("200", "操作成功"),

    // 4xx-客户端请求错误
    PARAM_ERROR("400", "输入参数不合法"),
    NO_LOGIN_AUTH("401", "请重新登录"),
    NO_OPERATION_AUTH("402", "缺少操作权限"),
    NO_DATA_AUTH("403", "缺少数据权限"),
    DATA_FAILED("405", "数据有误"),

    // 5xx-服务器错误
    SYSTEM_ERROR("500", "系统异常"),
    SYSTEM_BUSY_ERROR("501", "系统繁忙"),
    SYSTEM_EM_ERROR("502", "连接影像系统异常"),
    MD5_ERROR("511","文件校验md5失败"),
    BUSINO_NULL_ERROR("512","业务为空"),
    BUSI_ADD_FAILED("512","业务为空"),


    //文件下载
    BUSINO_NOTPATH("513","下载位置不能为空"),
    BUSINO_NOTNULL("514","业务不能为空"),


    //文件上传
    DOC_NORIGHT("601","资料无上传权限"),
    BUSI_FILEREPEAT("602","文件重复"),
    BUSI_NOFILETYPE("603","文件类型不允许"),
    BUSI_FILEUPLOADFAILED("604","文件上传失败");


    private String code;
    private String msg;

    ResultCodeApiEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    //todo 枚举为什么是静态方法
    public static ResultCodeApiEnum getEnum(String code) {
        for (ResultCodeApiEnum retEnum : ResultCodeApiEnum.values()) {
            if (retEnum.getCode().equals(code)) {
                return retEnum;
            }
        }
        return null;
    }

    public String getMsg() {
        return msg;
    }

    public String getCode() {
        return code;
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/6/30 zhouleibin creat
 */
