package com.sunyard.module.system.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wubingyang
 * @date 2022/1/25 11:21
 */
@Data
public class SysDeptMiddleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String no;

    private String parentNo;

    private String name;

    private String instNo;

    private List<SysDeptMiddleDTO> child;
}
