package com.sunyard.module.system.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 岗位接收类
 *
 * @Author wangmeiling 2025/9/5
 */
@Data
public class SysPostVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long postId;

    private Long instId;

    private String name;

    private String remarks;

    /**
     * 岗位代码
     */
    private String postCode;

}
