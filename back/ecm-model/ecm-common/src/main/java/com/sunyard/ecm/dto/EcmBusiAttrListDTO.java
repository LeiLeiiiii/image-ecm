package com.sunyard.ecm.dto;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 *  获取业务属性参数
 * @since 2023/9/07 16:55
 */
@Data
@ToString
@Accessors(chain = true)
public class EcmBusiAttrListDTO extends EcmUserDTO {

    /**
     * 业务类型编号
     */
    private String appCode;

    /**
     * 业务编号
     */
    private String busiNo;


}
