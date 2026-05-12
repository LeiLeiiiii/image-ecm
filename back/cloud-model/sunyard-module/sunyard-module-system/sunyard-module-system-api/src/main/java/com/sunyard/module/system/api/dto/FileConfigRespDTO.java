package com.sunyard.module.system.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 *
 * @author PJW
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileConfigRespDTO extends FileConfigBaseDTO {


    /**
     * 编号
     */
    private Long id;

    /**
     * 存储器,参见 FileStorageEnum 枚举类
     */
    @NotNull(message = "存储器不能为空")
    private Integer storage;

    /**
     * 是否为主配置
     */
    @NotNull(message = "是否为主配置不能为空")
    private Boolean master;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
