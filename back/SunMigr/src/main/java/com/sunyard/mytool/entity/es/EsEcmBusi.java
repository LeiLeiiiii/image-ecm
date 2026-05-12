package com.sunyard.mytool.entity.es;


import com.sunyard.mytool.entity.es.base.BaseBizObjEs;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.easyes.annotation.HighLight;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.IndexName;
import org.dromara.easyes.annotation.rely.IdType;

import java.io.Serializable;
import java.util.Date;

/**
 * @description ES业务数据
 */
@Data
@EqualsAndHashCode(callSuper = false)
@IndexName
public class EsEcmBusi extends BaseBizObjEs implements Serializable {

    private static final long serialVersionUID = 1L;

    @IndexId(type = IdType.CUSTOMIZE)
    private String id;

    /**
     * 业务id
     */
    private Long busiId;

    /**
     * 业务号
     */
    @HighLight(preTag = "<span style=\'color:red\'>", postTag = "</span>", fragmentSize = 2000, numberOfFragments = 5)
    private String busiNo;

    /**
     * 业务类型id
     */
    private String appCode;

    /**
     * 业务类型名称
     */
    private String appTypeName;

    /**
     * 业务类型的属性
     */
    @HighLight(preTag = "<span style=\'color:red\'>", postTag = "</span>", fragmentSize = 2000, numberOfFragments = 5)
    private String appAttrs;

    /**
     * 创建人名
     */
    @HighLight(preTag = "<span style=\'color:red\'>", postTag = "</span>", fragmentSize = 2000, numberOfFragments = 5)
    private String creatUserName;

    /**
     * 创建日期
     */
    private Long createDate;

    /**
     * 修改人名
     */
    @HighLight(preTag = "<span style=\'color:red\'>", postTag = "</span>", fragmentSize = 2000, numberOfFragments = 5)
    private String updateUserName;

    /**
     * 修改日期
     */
    private Long updateTime;

    /**
     * 删除状态(否:0,是:1)
     */
    private Integer isDeleted;

    /**
     * 机构号
     */
    private String orgCode;

}
