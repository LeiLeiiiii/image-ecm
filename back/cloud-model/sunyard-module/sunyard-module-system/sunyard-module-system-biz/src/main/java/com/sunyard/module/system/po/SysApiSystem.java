package com.sunyard.module.system.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 第三方系统api权限表
 * </p>
 *
 * @author PJW
 * @since 2022-01-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysApiSystem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接入系统名
     */
    private String systemName;

    /**
     * 接入系统Referer
     */
    private String systemReferer;

    /**
     * 授权状态（0已授权 1未授权）
     */
    private Integer status;

    /**
     * appId
     */
    private String appId;

    /**
     * appSecret
     */
    private String appSecret;

    /**
     * 公钥
     */
    private String publicKey;

    /**
     * 私钥
     */
    private String privateKey;

    /**
     * 加签方式
     */
    private String signType;

    /**
     * 数据格式
     */
    private String format;

    /**
     * 编码格式
     */
    private String charset;

    /**
     * 到期时间
     */
    private Date expirationTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 删除状态(否:0,是:1)
     */
    @TableLogic
    private Integer isDeleted;

}
