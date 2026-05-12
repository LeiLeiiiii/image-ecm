package com.sunyard.ecm.dto.redis;

import com.sunyard.ecm.dto.ecm.EcmDocrightDefDTO;
import com.sunyard.ecm.vo.AppTypeBusiVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author lw
 * @since 2023/8/9 17:34
 * @Description 用户业务信息DTO类
 */
@Data
public class UserBusiRedisDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务类型ids")
    private List<String> appType;
    @ApiModelProperty(value = "处于拆分中的文件id")
    private HashSet<Long> splitFileId;

    @ApiModelProperty(value = "业务")
    private List<Long> busiId;
    @ApiModelProperty(value = "业务")
    private String flagId;
    @ApiModelProperty(value = "业务类型-业务关联关系")
    private List<AppTypeBusiVO> relation;

    @ApiModelProperty(value = "扫描类型（1-单扫 2-批扫）")
    private Integer modelType;

    @ApiModelProperty(value = "动态树的权限列表")
    private Map<Long,List<EcmDocrightDefDTO>> docRightList;

    @ApiModelProperty(value = "仅展示当前节点，如果这个值为空，则展示所有的节点")
    private Map<Long,List<String>> docCodeShow;

    @ApiModelProperty(value = "对外用户名")
    private String username;

    @ApiModelProperty(value = "对外用户名")
    private String usercode;

    @ApiModelProperty(value = "角色code")
    private List<String> role;

    @ApiModelProperty(value = "角色code")
    private List<Long> roleIds;

    @ApiModelProperty(value = "机构code")
    private String org;

    @ApiModelProperty(value = "机构code")
    private String orgName;

    @ApiModelProperty(value = "机构id")
    private Long instId;

    @ApiModelProperty(value = "角色id")
    private Long roleId;

    @ApiModelProperty(value = "是否是查看页面，0：查看，1：采集")
    private Integer isShow;

}
