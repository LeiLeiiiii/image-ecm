package com.sunyard.framework.monitor.enums;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhouleibin
 * @date 2022/2/7 15:50
 * @Desc
 */
@Getter
@AllArgsConstructor
public enum DatabaseDocEnum {
    SYSTEM("system-service","0"),
    STORAGE("storage-service","1"),
    AM("eam-service","2"),
    ICM("ecm-service","3"),
    DM("edm-service","4"),
    AFM("afm-service","5"),
    //合同比对
    CLM("clm-service","6"),
    //分级分类
    TCS("tcs-service","7");

    private String name;
    private String code;

    public static DatabaseDocEnum getEnum(String code) {
        for (DatabaseDocEnum enums : DatabaseDocEnum.values()) {
            if (enums.getCode().equals(code)) {
                return enums;
            }
        }
        return null;
    }

}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2022/2/7 zhouleibin creat
 */
