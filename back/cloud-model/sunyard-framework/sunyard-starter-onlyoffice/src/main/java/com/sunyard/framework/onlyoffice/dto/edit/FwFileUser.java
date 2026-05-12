package com.sunyard.framework.onlyoffice.dto.edit;

import java.io.Serializable;

import lombok.Data;

/**
 * 使用文件的用户信息
 * @author PJW
 */
@Data
public class FwFileUser implements Serializable {

    /**
     * 用户唯一标识
     */
    private String id;

    /**
     * 用户 全称
     */
    private String name;

    /**
     * 组
     */
    private String[] group;
}
