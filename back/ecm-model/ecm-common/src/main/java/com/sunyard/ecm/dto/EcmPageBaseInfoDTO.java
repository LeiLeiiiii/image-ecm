package com.sunyard.ecm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * @author scm
 * @since 2023/8/10 14:29
 * @desc 用户基本信息DTO
 */
@Data
public class EcmPageBaseInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * token信息
     */
    String accessToken;

    /**
     * 业务表主键
     */
    List<Long> busiId;

    /**
     * 前端页面地址
     */
    String pageUrl;


    /**
     * 前端页面地址
     */
    String appId;


    /**
     * 前端页面地址
     */
    List<String> role;


    /**
     * 这次会话的唯一值
     */
    String flagId;

    /**
     * 页面url一次有效UUID
     */
    String nonce;


    /**
     * 前端页面地址
     */
    String username;



    /**
     * 前端页面地址
     */
    String usercode;



    /**
     * 前端页面地址
     */
    String orgCode;



    /**
     * 前端页面地址
     */
    String orgName;

}
