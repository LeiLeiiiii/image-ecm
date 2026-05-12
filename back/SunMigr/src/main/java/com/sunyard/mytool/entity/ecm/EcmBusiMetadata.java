package com.sunyard.mytool.entity.ecm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 影像业务属性值实体类（批次属性扩展表）
 */

@TableName("ecm_busi_metadata")
public class EcmBusiMetadata {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.NONE)
    private Long id;

    /**
     * 业务表主键
     */
    @TableField(value = "busi_id")
    private Long busiId;

    /**
     * 业务属性表主键
     */
    @TableField(value = "app_attr_id")
    private Long appAttrId;

    /**
     * 扩展属性值
     */
    @TableField(value = "app_attr_val")
    private String appAttrVal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBusiId() {
        return busiId;
    }

    public void setBusiId(Long busiId) {
        this.busiId = busiId;
    }

    public Long getAppAttrId() {
        return appAttrId;
    }

    public void setAppAttrId(Long appAttrId) {
        this.appAttrId = appAttrId;
    }

    public String getAppAttrVal() {
        return appAttrVal;
    }

    public void setAppAttrVal(String appAttrVal) {
        this.appAttrVal = appAttrVal;
    }
}
