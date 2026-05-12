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
public enum QuartzEnum {
    /**
     *system-service
     */
    QUARTZ_SYSTEM("com.sunyard.module.system", "system-service"),
    /**
     *storage-service
     */
    QUARTZ_STORAGE("com.sunyard.module.storage", "storage-service"),
    /**
     * eam-service
     */
    QUARTZ_ENT("com.sunyard.suneam", "eam-service"),
    /**
     * ecm-service
     */
    QUARTZ_ECM("com.sunyard.ecm", "ecm-service"),
    /**
     * edm-service
     */
    QUARTZ_EDM("com.sunyard.edm", "edm-service"),
    /**
     * afm-service
     */
    QUARTZ_AFM("com.sunyard.sunafm", "afm-service"),
    /**
     * clm-service
     */
    QUARTZ_CLM("com.sunyard.sunclm", "clm-service");

    /**
     * 服务路径
     */
    private String systemPath;
    /**
     * 服务名称
     */
    private String systemName;
    public static String getSystemNameStr(String systemPath) {
        for (QuartzEnum quartzEnum : QuartzEnum.values()) {
            if (systemPath.contains(quartzEnum.getSystemPath())) {
                return quartzEnum.getSystemName();
            }
        }
        return null;
    }
}
