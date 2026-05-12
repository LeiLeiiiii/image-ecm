package com.sunyard.ecm.dto.ecm;

import com.sunyard.ecm.po.EcmFileLabel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author： ty
 * @create： 2023/4/25 14:41
 * @desc: 文件信息DTO类
 */
@Data
public class EcmFileInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "文件id")
    private Long fileId;

    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "资料树主键")
    private String docId;

    @ApiModelProperty(value = "资料树标记主键")
    private Long markDocId;

    @ApiModelProperty(value = "最新的文件id")
    private Long newFileId;

    @ApiModelProperty(value = "文件大小")
    private Long newFileSize;

    @ApiModelProperty(value = "锁定状态（0:未锁定、1:锁定（移动））")
    private Integer newFileLock;

    @ApiModelProperty(value = "文件名称")
    private String newFileName;

    @ApiModelProperty(value = "文件地址")
    private String newFileUrl;

    @ApiModelProperty(value = "缩略图地址")
    private String thumFileUrl;

    @ApiModelProperty(value = "文件唯一md5（可查重使用）")
    private String fileMd5;

    @ApiModelProperty(value = "是否复用（默认0，1:复用）")
    private Integer fileReuse;

    @ApiModelProperty(value = "顺序（在doc_id下排序）")
    private Double fileSort;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "状态(默认正常展示:0,已删除:1)")
    private Integer state;

    @ApiModelProperty(value = "文件大小")
    private Long size;
    @ApiModelProperty(value = "文件大小")
    private String sizeStr;
    @ApiModelProperty(value = "文件格式")
    private String format;
    @ApiModelProperty(value = "创建人名称")
    private String createUserName;
    @ApiModelProperty(value = "最新修改人名称")
    private String updateUserName;
    @ApiModelProperty(value = "资料权限版本")
    private Integer rightVer;
    @ApiModelProperty(value = "资料类型名称")
    private String docName;
    @ApiModelProperty(value = "业务批次号")
    private String busiBatchNo;
    @ApiModelProperty(value = "new标记: 0无，1有")
    private Integer newTag;
    @ApiModelProperty(value = "旋转前文件id")
    private Long oldFileId;
    @ApiModelProperty(value = "资料类型id")
    private String docCode;
    @ApiModelProperty(value = "文件EXIF")
    private HashMap fileExif;
    @ApiModelProperty(value = "sourceFileMd5")
    private String sourceFileMd5;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "文件原始名称")
    private String originalFilename;

    @ApiModelProperty(value = "文件名")
    private String filename;

    @ApiModelProperty(value = "文件扩展名")
    private String ext;

    private String newFileExt;

    @ApiModelProperty(value = "上传Id（每个文件有唯一的一个上传id）")
    private String uploadId;

    @ApiModelProperty(value = "所属桶名")
    private String bucketName;

    @ApiModelProperty(value = "文件的key(桶下的文件路径)")
    private String objectKey;

    @ApiModelProperty(value = "每个分片大小（byte）")
    private Long chunkSize;

    @ApiModelProperty(value = "分片数量")
    private Integer chunkNum;

    @ApiModelProperty(value = "文件来源")
    private String fileSource;

    @ApiModelProperty(value = "文件访问地址(local)")
    private String url;

    @ApiModelProperty(value = "文件相对路径")
    private String filePath;

    @ApiModelProperty(value = "存储平台")
    private String platform;

    @ApiModelProperty(value = "基础存储路径（文件所在位置）")
    private String basePath;

    @ApiModelProperty(value = "缩略图路径")
    private String thUrl;

    @ApiModelProperty(value = "缩略图名称")
    private String thFilename;

    @ApiModelProperty(value = "缩略图大小")
    private Double thSize;

    @ApiModelProperty(value = "删除状态(否:0,是:1)")
    private Integer isDeleted;

    @ApiModelProperty(value = "是否上传完成（0:未完成，1：完成）")
    private Integer isUploadOk;

    @ApiModelProperty(value = "备注")
    private String comment;

    @ApiModelProperty(value = "业务类型id")
    private String appCode;

    @ApiModelProperty(value = "业务类型名称")
    private String appTypeName;

    @ApiModelProperty(value = "业务编号")
    private String busiNo;

    @ApiModelProperty(value = "文件new标签标识(1-有new标签 ，0-没有new标签)")
    private Integer signFlag;

    @ApiModelProperty(value = "机构号")
    private String orgCode;

    @ApiModelProperty("资料文件顺序")
    private String docFileSort;

    @ApiModelProperty(value = "存储设备id")
    private Long equipmentId;

    @ApiModelProperty(value = "树类型(0:静态树 1:动态树)")
    private String treeType;

    @ApiModelProperty(value = "文稿类识别转正配置开关(关:0,开:1)")
    private Integer isRegularized;

    @ApiModelProperty(value = "是否加密0否 1是")
    private Integer isEncrypt;

    @ApiModelProperty(value = "审核查重状态")
    private Integer checkRepeatStatus;

    @ApiModelProperty(value = "机构名称")
    private String orgName;

    /**
     * 文档类型第一页的文本内容
     */
    @ApiModelProperty(value = "文档类型第一页的文本内容")
    private String contentFirstPage;

    @ApiModelProperty(value = "文件标签")
    private List<EcmFileLabel> ecmFileLabels;

    @ApiModelProperty(value = "是否为文本查重")
    private boolean isTextDet;

    @ApiModelProperty(value = "文本查重内容")
    private String textAll;

    @ApiModelProperty(value = "文件是否有密码,true/false")
    private Boolean isFilePassword;

    @ApiModelProperty(value = "是否到期：0-未到期，1-已到期")
    private Integer isExpired;

    @ApiModelProperty(value = "是否到期字符串")
    private String isExpiredStr;

    @ApiModelProperty(value = "到期时间")
    private Date expireDate;

    @ApiModelProperty(value = "单证类型")
    private String dtdTypeName;
}
