package com.sunyard.module.system.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 *
 * @author PJW
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileConfigUpdateReqDTO extends FileConfigBaseDTO {

    /**
     * 编号
     */
    @NotNull(message = "编号不能为空")
    private Long id;

    /**
     * 存储配置,配置是动态参数，所以使用 Map 接收
     */
    @NotNull(message = "存储配置不能为空")
    private Map<String, Object> config;

}
