package com.sunyard.module.system.api.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
* 文件配置 Base VO，提供给添加、修改、详细的子 VO 使用
 * @author PJW
 */
@Data
public class FileConfigBaseDTO {

    /**
     * 配置名
     */
    @NotNull(message = "配置名不能为空")
    private String name;

    /**
     * 备注
     */
    private String remark;

}
