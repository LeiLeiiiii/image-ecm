package com.sunyard.ecm.dto.ecm;

import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author： ty
 * @create： 2023/5/10 14:08
 * @desc: 业务版本DTO类
 */
@Data
public class EcmBusiVersionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务提交节点表id")
    private Long id;

    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间(根据创建时间设定节点)")
    private Date createTime;

    @ApiModelProperty(value = "创建人名称")
    private String createUserName;

    @ApiModelProperty(value = "文件对象")
    private List<FileInfoRedisDTO> fileInfoRedisEntities;
}
