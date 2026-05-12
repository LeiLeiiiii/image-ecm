package com.sunyard.ecm.oldToNew.po;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * @author yzy
 * @since 2025/02/19 11:38
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmOperateLog对象", description = "影像业务操作表")
@TableName("ecm_grant_table")
public class BGrantTable {
    private String id;
    private byte[] grantService;
    private byte[] grantAccess;

}
