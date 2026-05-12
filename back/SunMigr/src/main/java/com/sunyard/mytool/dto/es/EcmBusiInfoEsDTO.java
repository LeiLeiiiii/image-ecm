package com.sunyard.mytool.dto.es;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description ES业务信息参数DTO
 */
@Data
@Accessors(chain = true)
public class EcmBusiInfoEsDTO implements Serializable {
    /******************************* 业公共查询 *******************************/


    private Long busiId;


    private String appCode;


    private String appTypeName;


    private String busiNo;


    private String creatUserName;


    private String updateUserName;


    private String appAttrs;


    private List<Map<String , String>> appAttrMap;

    /**
     * 开始时间
     */
    private Long createDate;


    /**
     * 修改时间
     */
    private Long updateTime;

    private Integer isDeleted;

    /**
     * 机构号
     */
    private String orgCode;

}
