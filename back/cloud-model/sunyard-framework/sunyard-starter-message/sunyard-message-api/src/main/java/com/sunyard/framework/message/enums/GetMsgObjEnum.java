package com.sunyard.framework.message.enums;

import java.util.Date;

import org.springframework.util.Assert;

import com.sunyard.module.system.api.dto.MessageDTO;

import lombok.Getter;

/**
 * @author P-JWei
 * @date 2023/7/6 17:09
 * @title 生成msg对象类
 * @description 默认模板生成指定对象
 */
@Getter
public enum GetMsgObjEnum {

    // todo 带了也业务字段，后面看怎么改
    /**
     * 借阅
     */
    BORROW("borrow", "借阅") {
        @Override
        public MessageDTO getObj(String appName, Long acceptUser, MessageResultEnum resultEnum, String diyContent) {
            MessageDTO messageDTO = basicObj(appName, acceptUser, getCode());
            setHeadAndContent(messageDTO, resultEnum, getDesc(), diyContent);
            return messageDTO;
        }
    },
    /**
     * 销毁
     */
    DESTROY("destroy", "销毁") {
        @Override
        public MessageDTO getObj(String appName, Long acceptUser, MessageResultEnum resultEnum, String diyContent) {
            MessageDTO messageDTO = basicObj(appName, acceptUser, getCode());
            setHeadAndContent(messageDTO, resultEnum, getDesc(), diyContent);
            return messageDTO;
        }
    },
    /**
     * 资料审核
     */
    ARCAUDIT("arcaudit", "资料审核") {
        @Override
        public MessageDTO getObj(String appName, Long acceptUser, MessageResultEnum resultEnum, String diyContent) {
            MessageDTO messageDTO = basicObj(appName, acceptUser, getCode());
            setHeadAndContent(messageDTO, resultEnum, getDesc(), diyContent);
            return messageDTO;
        }
    },
    /**
     * 归档
     */
    FILING("filing", "归档") {
        @Override
        public MessageDTO getObj(String appName, Long acceptUser, MessageResultEnum resultEnum, String diyContent) {
            MessageDTO messageDTO = basicObj(appName, acceptUser, getCode());
            setHeadAndContent(messageDTO, resultEnum, getDesc(), diyContent);
            return messageDTO;
        }
    },
    /**
     * 反归档
     */
    IMFILING("imfiling", "反归档") {
        @Override
        public MessageDTO getObj(String appName, Long acceptUser, MessageResultEnum resultEnum, String diyContent) {
            MessageDTO messageDTO = basicObj(appName, acceptUser, getCode());
            setHeadAndContent(messageDTO, resultEnum, getDesc(), diyContent);
            return messageDTO;
        }
    },
    /**
     * 移交
     */
    TRANSFER("transfer", "移交") {
        @Override
        public MessageDTO getObj(String appName, Long acceptUser, MessageResultEnum resultEnum, String diyContent) {
            MessageDTO messageDTO = basicObj(appName, acceptUser, getCode());
            setHeadAndContent(messageDTO, resultEnum, getDesc(), diyContent);
            return messageDTO;
        }
    },
    /**
     * 催还
     */
    EXPEDITE("expedite", "催还") {
        @Override
        public MessageDTO getObj(String appName, Long acceptUser, MessageResultEnum resultEnum, String diyContent) {
            MessageDTO messageDTO = basicObj(appName, acceptUser, getCode());
            setHeadAndContent(messageDTO, resultEnum, getDesc(), diyContent);
            return messageDTO;
        }
    };

    private String code;

    private String desc;

    public abstract MessageDTO getObj(String appName, Long acceptUser, MessageResultEnum resultEnum, String diyContent);

    GetMsgObjEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 添加基础几段
     *
     * @param appName    服务名
     * @param acceptUser 接收者
     * @param type       类型
     * @return Result
     */
    private static MessageDTO basicObj(String appName, Long acceptUser, String type) {
        Assert.notNull(appName, "appName为空");
        Assert.notNull(acceptUser, "acceptUser为空");
        Assert.notNull(type, "type为空");
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setAcceptUser(acceptUser);
        messageDTO.setMsgType(type);
        messageDTO.setMsgSystem(appName);
        messageDTO.setInformTime(new Date());
        return messageDTO;
    }

    /**
     * 添加指定标题、内容字段
     *
     * @param messageDTO 消息dto
     * @param resultEnum 消息类型
     * @param desc       描述
     * @param diyContent 内容
     * @return Result
     */
    private static MessageDTO setHeadAndContent(MessageDTO messageDTO, MessageResultEnum resultEnum, String desc, String diyContent) {
        Assert.notNull(resultEnum, "resultEnum为空");
        Assert.notNull(desc, "desc为空");
        messageDTO.setMsgHead(resultEnum.getHead());
        messageDTO.setMsgContent(String.format(resultEnum.getContent(), desc, diyContent));
        return messageDTO;
    }

    /**
     * 获取指定枚举
     *
     * @param code code
     * @return Result
     */
    public static GetMsgObjEnum getMsgEnum(String code) {
        for (GetMsgObjEnum retEnum : GetMsgObjEnum.values()) {
            if (retEnum.getCode().equals(code)) {
                return retEnum;
            }
        }
        return null;
    }
}
