package com.sunyard.module.system.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author raochangmei
 * @date 2022-05-25
 */
@Data
public class SysDictionaryDTO implements Serializable {

    private List<SysDictionaryDTO> children;
    private String name;

    private String code;

    private String desc;

    private String label;
    private String value;

    private Long id;

    private String dicKey;

    private String dicVal;

    /**
     * 字典类型，0：目录  1：字典  2：字典数据
     * 只有新创建的字典才使用
     */
    private String dicType;

    private String remark;

    private Integer dicSequen;

    private Integer dicLevel;

    private Long parentId;

    private Integer systemCode;

    private String dicExtra;

    private Integer isDeleted;

    private Date createTime;

    private Date updateTime;

}
