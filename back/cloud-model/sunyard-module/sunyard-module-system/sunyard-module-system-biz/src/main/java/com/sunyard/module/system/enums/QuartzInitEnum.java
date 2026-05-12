package com.sunyard.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author P-JWei
 * @date 2024/2/5 13:53:08
 * @title
 * @description
 */
@Getter
@AllArgsConstructor
public enum QuartzInitEnum {
    /**
     * system-service
     */
    QUARTZ_SYSTEM(0, "system-service"),
    /**
     * storage-service
     */
    QUARTZ_STORAGE(1, "storage-service"),
    /**
     * eam-service
     */
    QUARTZ_ENT(2, "eam-service"),
    /**
     * ecm-service
     */
    QUARTZ_ECM(3, "ecm-service"),
    /**
     * edm-service
     */
    QUARTZ_EDM(4, "edm-service"),
    /**
     * afm-service
     */
    QUARTZ_AFM(5, "afm-service"),
    /**
     * clm-service
     */
    QUARTZ_CLM(6, "clm-service");

    /**
     * 服务路径
     */
    private Integer code;
    /**
     * 服务名称
     */
    private String systemName;

    public static Integer getCode(String systemName) {
        for (QuartzInitEnum quartzEnum : QuartzInitEnum.values()) {
            if (systemName.equals(quartzEnum.getSystemName())) {
                return quartzEnum.getCode();
            }
        }
        return null;
    }
}
