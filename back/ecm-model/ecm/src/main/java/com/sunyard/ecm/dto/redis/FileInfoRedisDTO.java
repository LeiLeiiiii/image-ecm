package com.sunyard.ecm.dto.redis;

import com.sunyard.ecm.dto.ecm.EcmDocrightDefDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.po.EcmFileHistory;
import com.sunyard.ecm.po.EcmFileLabel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * @author lw
 * @since 2023/8/9 17:34
 * @Description 文件信息DTO类
 */
@Data
public class FileInfoRedisDTO extends EcmFileInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "文件EXIF")
    private HashMap fileExif;

    @ApiModelProperty(value = "文件历史版本")
    private List<EcmFileHistory> fileHistories;

    @ApiModelProperty(value = "文件标签")
    private List<EcmFileLabel> ecmFileLabels;

    @ApiModelProperty(value = "文件批注数量")
    private Integer fileCommentCount;

    @ApiModelProperty(value = "文件全路径")
    private String fileFullPath;

    @ApiModelProperty(value = "缓存中的地址")
    private String fileFullPathCache;

    @ApiModelProperty(value = "缓存中的缩略图地址")
    private String fileFullPathCacheThumbnail;

    @ApiModelProperty(value = "权限")
    private EcmDocrightDefDTO docRight;

    @ApiModelProperty(value = "0:展示锁，1：展示图标，2：展示缩略图")
    private Integer showType;

    @ApiModelProperty(value = "计算后文件大小")
    private String fileSize;

    @ApiModelProperty(value = "文件单位")
    private String fileUnit;

    @ApiModelProperty(value = "文件名称")
    private String FileName;

    @ApiModelProperty(value = "异常结果标记")
    private Boolean abnormalResultMarking;

    @ApiModelProperty(value = "智能处理状态")
    private String taskType;

    @ApiModelProperty(value = "业务状态")
    private Integer status;

}
