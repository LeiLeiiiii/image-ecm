package com.sunyard.module.storage.ecm.dto;

import lombok.Data;

import java.util.List;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl.sunecm
 * @Desc
 * @date 15:08 2021/10/27
 */
@Data
public class QueryVtreeNodeDTO {

    List<String> leafs;
    List<QueryEcmPageDTO> pageList;
    private String arcId;
    private String typeNo;
    private String typeName;
    private String type;
    private Integer size;
}
