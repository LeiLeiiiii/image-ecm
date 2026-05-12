package com.sunyard.framework.spire.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhouleibin
 * pdf开始和解决拆分
 */
@Data
public class PdfSplitDTO implements Serializable {
    private Integer start;
    private Integer end;
}
