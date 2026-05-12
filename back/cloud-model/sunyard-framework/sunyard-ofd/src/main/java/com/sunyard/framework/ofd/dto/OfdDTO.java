package com.sunyard.framework.ofd.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author HRH
 * @date 2023/7/24
 * @describe
 */
@Data
public class OfdDTO implements Serializable {

    /**
     * 模板数据
     */
    private Map<String, Object> dataModel;
    /**
     * 生成word路径
     */
    private String outWordPath;

    /**
     * 模板路径
     */
    private String wordPath;
    /**
     * 生成ofd路径
     */
    private String ofdPath;
}
