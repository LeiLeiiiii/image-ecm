package com.sunyard.framework.elasticsearch.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;


/**
 * @author P-JWei
 * @date 2023/10/11 17:32:44
 * @title
 * @description
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BaseBizObjEs {

    /**
     * 业务来源（内部服务模块系统号：acc\ent\ecm...）
     */
    private String baseBizSource;

    /**
     * 业务流水号（由业务系统生成）
     */
    private Long baseBizSourceId;

    /**
     * 创建人
     */
    private Date baseCreateTime;

    /**
     * 创建时间
     */
    private Long baseCreateUser;

}
