package com.sunyard.ecm.vo;

import com.sunyard.ecm.dto.ecm.EcmAppDocRelDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/5/8 15:50
 * @Description：资料树VO
 */
@Data
public class EcmDocTreeVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务类型主键")
    private String appCode;

    @ApiModelProperty(value = "树")
    private List<EcmAppDocRelDTO> list;
}
