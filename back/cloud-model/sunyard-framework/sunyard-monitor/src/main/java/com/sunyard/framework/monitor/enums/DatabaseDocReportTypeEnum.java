package com.sunyard.framework.monitor.enums;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import cn.smallbun.screw.core.engine.EngineFileType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhouleibin
 * @date 2022/2/7 15:50
 * @Desc
 */
@Getter
@AllArgsConstructor
public enum DatabaseDocReportTypeEnum {
    HTML(EngineFileType.HTML,"0"),
    WORD(EngineFileType.WORD,"1"),
    MD(EngineFileType.MD,"2");

    private EngineFileType name;
    private String code;

    public static DatabaseDocReportTypeEnum getEnum(String code) {
        for (DatabaseDocReportTypeEnum enums : DatabaseDocReportTypeEnum.values()) {
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
