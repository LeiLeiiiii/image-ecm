package com.sunyard.ecm.dto.ecm;


import lombok.Data;

/**
 * @author yzy
 * @date 2024/11/18
 * @describe 获取可配置角色列表DTO
 */
@Data
public class EcmGetRoleListDTO {
    private String appCode;
    private Integer rightVer;
}
