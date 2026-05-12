package com.sunyard.module.system.dto;

import com.sunyard.framework.common.page.PageForm;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName SysPostListDTO
 * @Author wml
 * @Date 2025/9/5 10:51
 **/
@Data
public class SysPostListDTO extends PageForm implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private Long instId;

    private Long deptId;
}
