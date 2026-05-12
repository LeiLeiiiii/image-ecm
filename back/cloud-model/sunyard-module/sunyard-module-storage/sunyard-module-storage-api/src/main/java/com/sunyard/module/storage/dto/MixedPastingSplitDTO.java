package com.sunyard.module.storage.dto;

import lombok.Data;

import java.util.List;

/**
 * @author zyl
 * @Description
 * @since 2023/7/27 10:55
 */
@Data
public class MixedPastingSplitDTO {

    /**
     * st-file表中的id
     */
    private Long id;
    /**
     * 拆分后的坐标集合
     */
    private List<List<Integer>> regionList;
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 是否加密
     */
    private Integer isEncrypt;
}
