package com.sunyard.sunafm.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author P-JWei
 * @date 2024/3/11 11:21:19
 * @title
 * @description
 */
@Data
public class AfmLogManaSysVO implements Serializable {

    /**
     * 操作内容
     */
    private String opContent;

    /**
     * 操作人
     */
    private String opUserName;

    /**
     * 操作时间左区间
     */
    private Date toOpTime;

    /**
     * 操作右区间
     */
    private Date doOpTime;
}
