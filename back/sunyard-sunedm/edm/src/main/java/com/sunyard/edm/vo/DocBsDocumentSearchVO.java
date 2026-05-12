package com.sunyard.edm.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @author huronghao
 * @Type
 * @Desc
 * @date 2022-12-13 17:30
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsDocumentSearchVO {

    /**
	 * 分页
	 */
    private Integer pageNum = 1;
    /**
	 * 分页数量
	 */
    private Integer pageSize = 20;

    /**
	 * 排除当前文档所用的字段
	 */
    private List<Long> relBusId;

    /**
	 * 所属文档库id
	 */
    private Long houseId;

    /**
     * 开始时间
     */
    /**
	 * 更新开始时间
	 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updateStartDate;

    /**
     * 结束时间
     */
    /**
	 * 更新结束时间
	 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updateEndDate;

    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createStartDate;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createEndDate;


    /**
	 * 后缀字典表值
	 */
    private String dictionSuffix;


    /**
	 * 附件名称
	 */
    private String attchName;

    /**
	 * 文件夹id
	 */
    private Long folderId;

    /**
	 * 文件夹ids
	 */
    private List<Long> folderIds;

    /**
	 * 上传时间排序 0升序 1降序
	 */
    private String createTime;

    /**
	 * 更新时间排序 0升序 1降序
	 */
    private String updateTime;

    /**
	 * 后端用-仅展示有管理权限和编辑权限的文档
	 */
    private Boolean showFlag;

    /**
	 * 类型：0:企业，1:个人
	 */
    private Integer docType;

    /**
	 * 搜索所用的标签id
	 */
    private Long tagId;

    /**
	 * 文档标题
	 */
    private String docName;

    /**
	 * 所有者搜索条件
	 */
    private String docOwnerStr;

    /**
	 * 所属团队搜索条件
	 */
    private String teamName;

    /**
	 * 文档检索关键字
	 */
    private String key;

    /**
	 * 前端后缀清除展示
	 */
    private Boolean dictionSuffixFlag;

}
