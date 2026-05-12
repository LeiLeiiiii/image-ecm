package com.sunyard.module.storage.po;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 文件表
 * </p>
 *
 * @author panjiazhu
 * @since 2022-07-12
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class StFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文件原始名称
     */
    private String originalFilename;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件扩展名
     */
    private String ext;

    /**
     * 文件大小
     */
    @TableField("\"SIZE\"")
    private Long size;


    /**
     * 文件的key(桶下的文件路径)
     */
    private String objectKey;



    /**
     * 文件来源
     */
    private String fileSource;

    /**
     * 存储设备id
     */
    private Long equipmentId;

    /**
     * 源文件MD5
     */
    private String sourceFileMd5;

    /**
     * 目标文件MD5
     */
    private String fileMd5;

    /**
     * 上传人
     */
    @TableField("CREATE_USER")
    private Long createUser;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME" , fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 删除状态(否:0,是:1
     */
    // 【重要检查】确认数据库列名是 IS_DELETED 还是 DEL_FLAG?
    // 如果是 DEL_FLAG，请改为 @TableLogic(value = "0", delval = "1") @TableField("DEL_FLAG")
    @TableLogic(value = "0", delval = "1") // 明确指定 0:正常, 1:删除
    @TableField("IS_DELETED")
    private Integer isDeleted;


    /**
     * 是否加密 （0否 1是）
     */
    private Integer isEncrypt;

    /**
     * 加密密钥
     */
    private String encryptKey;


    /**
     * 加密算法,0为AES,1为SM2
     */
    private Integer encryptType;

    /**
     * 加密的密文长度
     */
    private Long encryptLen;

}
