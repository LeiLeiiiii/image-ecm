package com.sunyard.edm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 文档库和用户关联表
 * </p>
 *
 * @author pjw
 * @since 2022-12-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocSysHouseUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 主键id
	 */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
	 * 文档库id
	 */
    private Long houseId;

    /**
	 * 用户id/部门id/机构id/团队id
	 */
    private Long relId;

    /**
	 * 关联的类型，0:用户、1:机构、2:部门、3:团队
	 */
    private Integer type;

    /**
	 * 权限，0:可查看，1:可编辑，2：可管理
	 */
    private Integer permissType;

    /**
	 * 创建时间
	 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
	 * 更新时间
	 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
	 * 删除状态(否:0,是:1)
	 */
    @TableLogic
    private Integer isDeleted;


}
