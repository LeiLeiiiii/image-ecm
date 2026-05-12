package com.sunyard.ecm.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.framework.common.page.PageForm;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author： ypy
 * @Description：影像期限VO
 * @create： 2023/5/17 14:05
 */
@Data
public class FileExpireVO extends PageForm implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "文件ids")
    private List<Long> fileIds;

    @ApiModelProperty(value = "失效时间")
    private Date expireDate;

    @ApiModelProperty(value = "业务编号")
    private String busiNo;

    @ApiModelProperty(value = "业务类型")
    private List<String> appCode;

    @ApiModelProperty(value = "资料类型")
    private List<String> docCode;

    @ApiModelProperty(value = "单证类型")
    private String dtdCode;

    @ApiModelProperty(value = "业务属性列表")
    private List<EcmAppAttrDTO> attrList;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建日期 起")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String expireDateStart;

    @ApiModelProperty(value = "创建日期 止")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String expireDateEnd;
}
