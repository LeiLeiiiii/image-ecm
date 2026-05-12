package com.sunyard.afm.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 同步ecm和afm数据dto
 */
@Data
public class AfmDetUpdateDto {

    /**
     * 业务索引（主索引）
     */
    private String businessIndex;

    /**
     * 同步的文件id
     */
    private List<String> fileIds;

    /**
     * 同步类型： 1：修改 2：删除
     */
    private int type;
}
