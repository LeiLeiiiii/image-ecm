package com.sunyard.ecm.dto;

import com.sunyard.framework.common.token.AccountToken;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lw
 * @since 2023/8/9 17:34
 * @Description 登录扩展类
 */
@Data
public class AccountTokenExtendDTO extends AccountToken {
    private String tokenValue;

    @ApiModelProperty(value = "是否是查看页面，0：查看，1：采集")
    private Integer isShow;
}
