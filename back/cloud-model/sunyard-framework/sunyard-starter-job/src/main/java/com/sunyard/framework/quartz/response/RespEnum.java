package com.sunyard.framework.quartz.response;

/**
 * @author P-JWei
 * @date 2024/1/12 13:56:04
 * @title 响应枚举
 * @description
 */
public enum RespEnum {
    /**
     * 成功
     */
    SUCCESS("成功",00000),
    /**
     * 定时任务类路径出错：未找到调用目标
     */
    FAIL_NO_FOUND_CLASS("定时任务类路径出错：未找到调用目标",00001),
    /**
     *创建定时任务出错
     */
    FAIL_CREATE_JOB("创建定时任务出错",00002),
    /**
     *暂停定时任务出错
     */
    FAIL_PAUSE_JOB("暂停定时任务出错",00003),
    /**
     *启动定时任务出错
     */
    FAIL_START_JOB("启动定时任务出错",00004),
    /**
     *运行定时任务出错
     */
    FAIL_RUN_ONE_JOB("运行定时任务出错",00005),
    /**
     *更新定时任务出错
     */
    FAIL_UPDATE_JOB("更新定时任务出错",00006),
    /**
     *删除定时任务出错
     */
    FAIL_DELETE_JOB("删除定时任务出错",00007),
    /**
     *定时任务不存在
     */
    FAIL_NOT_FOUND_JOB("定时任务不存在",00010);
    /**
     * 报错信息
     */
    private String msg;
    /**
     * 错误代码
     */
    private Integer code;

    RespEnum(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
