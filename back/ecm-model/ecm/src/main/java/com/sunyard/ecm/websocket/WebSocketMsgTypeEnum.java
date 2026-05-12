package com.sunyard.ecm.websocket;

/**
 * @author yzy
 * @desc
 * @since 2025/3/20
 */
public enum WebSocketMsgTypeEnum {

    /**
     * 自动归类数量
     */
    AUTO_CLASS_NUM("autoClassNum"),
    ;

    private final String description;

    WebSocketMsgTypeEnum(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     */
    public String description() {
        return description;
    }

    /**
     * 通过描述获取枚举类
     * @param description
     * @return
     */
    public static WebSocketMsgTypeEnum fromDescription(String description) {
        for (WebSocketMsgTypeEnum type : WebSocketMsgTypeEnum.values()) {
            if (type.description.equals(description)) {
                return type;
            }
        }
        return null;
    }
}
