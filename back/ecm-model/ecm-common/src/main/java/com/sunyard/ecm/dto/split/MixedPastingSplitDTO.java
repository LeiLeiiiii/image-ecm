package com.sunyard.ecm.dto.split;

import lombok.Data;

import java.util.List;

/**
 * @author zyl
 * @since 2023/7/27 10:55
 * @Description 混贴拆分DTO
 */
@Data
public class MixedPastingSplitDTO {

    private Long id;
    private List<List<Integer>> regionList;
    private Long userId;
}
