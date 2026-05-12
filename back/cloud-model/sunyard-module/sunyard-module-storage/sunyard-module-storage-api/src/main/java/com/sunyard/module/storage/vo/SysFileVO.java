package com.sunyard.module.storage.vo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author panjiazhu
 * @date 2022/7/28
 */
@Data
public class SysFileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fileName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date delStartTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date delEndTime;

}
