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
 * 对外api接口表
 * </p>
 *
 * @author 吴丙扬
 * @since 2022-01-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysApi implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接口编码
     */
    private String apiCode;

    /**
     * 接口名称
     */
    private String apiName;

    /**
     *接口url
     */
    private String apiUrl;

    /**
     *0底座 1档案 2影像
     */
    private Integer systemType;

    /**
     *是否验签（0是 1否）
     */
    private Integer isSign;

    /**
     *是否开启时间戳校验（0是 1否）
     */
    private Integer isTimestamp;

    /**
     *是否开启referer校验（0是 1否）
     */
    private Integer isReferer;

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
