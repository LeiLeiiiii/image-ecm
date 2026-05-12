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
public class FileConfigCreateReqDTO extends FileConfigBaseDTO {
    /**
     * 存储器,参见 FileStorageEnum 枚举类
     */
    @NotNull(message = "存储器不能为空")
    private Integer storage;

    /**
     * 存储配置,配置是动态参数，所以使用 Map 接收
     */
    @NotNull(message = "存储配置不能为空")
    private Map<String, Object> config;

}
