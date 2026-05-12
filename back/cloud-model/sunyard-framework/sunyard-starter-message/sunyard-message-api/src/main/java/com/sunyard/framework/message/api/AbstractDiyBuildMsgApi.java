package com.sunyard.framework.message.api;

import com.sunyard.module.system.api.dto.MessageDTO;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author P-JWei
 * @date 2023/7/10 14:25
 * @title 自定义msg构建类
 * @description 需自定义msg，只需extends此类，实现三个set方法。添加type、title、content集。然后集合生成符合条件的msg对象
 */
public abstract class AbstractDiyBuildMsgApi {

    private MessageDTO messageDTO;

    public AbstractDiyBuildMsgApi() {
        setTypeList();
        setTitleList();
        setContentList();
    }

    /**
     * 消息类型map
     */
    protected List<String> typeList = new ArrayList<>();

    /**
     * 消息标题list
     */
    protected List<String> titleList = new ArrayList<>();

    /**
     * 消息内容list
     */
    protected List<String> contentList = new ArrayList<>();

    /**
     * 设置类型
     */
    protected abstract void setTypeList();

    /**
     * 设置标题
     */
    protected abstract void setTitleList();

    /**
     * 设置内容
     */
    protected abstract void setContentList();

    /**
     * 发送消息
     *
     * @param appName      服务名
     * @param acceptUser   接收者
     * @param typeIndex    类型
     * @param titleIndex   标题
     * @param contentIndex 内容
     * @return AbstractDiyBuildMsgApi
     */
    public AbstractDiyBuildMsgApi getMsgObj(String appName, Long acceptUser, int typeIndex, int titleIndex, int contentIndex) {
        Assert.notNull(appName, "appName为空");
        Assert.notNull(acceptUser, "acceptUser为空");
        Assert.notEmpty(typeList, "type集合为空");
        Assert.notEmpty(titleList, "title集合为空");
        Assert.notEmpty(contentList, "content集合为空");
        Assert.isTrue(typeIndex < typeList.size(), "typeIndex超过最大值，无法获取指定type");
        Assert.isTrue(titleIndex < titleList.size(), "titleIndex超过最大值，无法获取指定title");
        Assert.isTrue(contentIndex < contentList.size(), "contentIndex超过最大值，无法获取指定content");
        messageDTO = new MessageDTO();
        messageDTO.setMsgSystem(appName);
        messageDTO.setAcceptUser(acceptUser);
        messageDTO.setInformTime(new Date());

        messageDTO.setMsgType(typeList.get(typeIndex));
        messageDTO.setMsgHead(titleList.get(titleIndex));
        messageDTO.setMsgContent(contentList.get(contentIndex));
        return this;
    }

    /**
     * 往content里添加业务信息
     *
     * @param business 业务信息
     * @return Result
     */
    public AbstractDiyBuildMsgApi addBusinessInfoToContent(String... business) {
        int count = StringUtils.countOccurrencesOf(messageDTO.getMsgContent(), "%s");
        Assert.isTrue(count <= business.length, "参数不足，无法生成内容");
        String content = String.format(messageDTO.getMsgContent(), business);
        Assert.isTrue(content.length() <= 255, "数据过长");
        messageDTO.setMsgContent(content);
        return this;
    }

    /**
     * 往content里添加业务信息
     *
     * @param body 业务json格式信息
     * @return Result
     */
    public AbstractDiyBuildMsgApi addBusinessInfoToBody(String body) {
        Assert.isTrue(body.length() <= 1000, "数据过长");
        messageDTO.setMsgBody(body);
        return this;
    }

    /**
     * 构建完成
     *
     * @return Result
     */
    public MessageDTO build() {
        return messageDTO;
    }

}
