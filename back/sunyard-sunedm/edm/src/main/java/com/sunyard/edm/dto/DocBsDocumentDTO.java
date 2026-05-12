package com.sunyard.edm.dto;

import com.github.pagehelper.PageInfo;
import com.sunyard.edm.po.DocBsDocument;
import com.sunyard.edm.po.DocBsDocumentUser;
import com.sunyard.edm.po.DocSysTag;
import com.sunyard.module.system.api.dto.SysDictionaryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 文件夹、文档表
 * </p>
 *
 * @author pjw
 * @since 2022-12-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsDocumentDTO extends DocBsDocument {
    /**
	 * 文档详情
	 */
    private DocBsDocument docBsDocument;

    /**
	 * 动态
	 */
    private List<DocBsDocFlowDTO> docBsDocFlowList;

    /**
	 * 关联附件
	 */
    private List<DocBsDocumentDTO> attchList;

    /**
	 * 关联文档
	 */
    private List<DocBsDocumentDTO> documentList;


    /**
	 * 删除状态(否:0,是:1)
	 */
    private Integer isPermissDeleted;

    /**
	 * 关联标签
	 */
    private List<DocSysTag> docSysTags;


    /**
	 * 关联用户权限
	 */
    private List<DocBsDocumentUser> docBsDocumentUsers;


    /**
	 * 文件夹详情分页
	 */
    private PageInfo<DocBsDocumentUserDTO> pageInfo;

    /**
	 * 权限级别 权限，0:可查看，1:可编辑，2：可管理
	 */
    private Integer permissType;

    /**
	 * 前端筛选
	 */
    private List<Long> tagId;

    /**
	 * 是否是缩略图
	 */
    private Boolean isBrevicary;

    /**
	 * 是否是全量-默认都是全量
	 */
    private Boolean isQuantity = true;

    /**
	 * 所有者中文名称
	 */
    private String docOwnerStr;

    /**
	 * 创建者中文名称
	 */
    private String docCreatorStr;

    /**
	 * 文档库中文名称
	 */
    private String houseIdStr;

    /**
	 * 文件夹中文名称
	 */
    private String folderIdStr;

    /**
	 * 文件夹中文名称-全名-目录名
	 */
    private String folderIdAllStr;

    /**
	 * 是否存在子文件夹 0没有 1有
	 */
    private Integer isChildFolder;

    /**
	 * 机构数量
	 */
    private Integer instNum;

    /**
	 * 部门数量
	 */
    private Integer deptNum;

    /**
	 * 团队数量
	 */
    private Integer teamNum;

    /**
	 * 用户数量
	 */
    private Integer userNum;

    /**
	 * 回收截止时间
	 */
    private Date recycleDate;

    /**
	 * 删除时间
	 */
    private Date delDate;

    /**
	 * 回收主键
	 */
    private Long recycleId;

    /**
	 * 后缀类型，如果是文档格式，则为null，如果是图片格式：1；视频格式：2；音频格式：3；压缩文件：4；可执行文件：5；其他所有：6
	 */
    private SysDictionaryDTO docSuffixType;

    /**
	 * 用户权限列表
	 */
    private List<DocBsDocumentUserDTO> userTeamDeptListExtends;

    /**
	 * 前端所用-标签展示
	 */
    private String tagIdStr;

    /**
	 * 前端所用-悬浮状态
	 */
    private Boolean isShow = false;

    /**
	 * 前端所用-是否跨层级（非全量文件夹树专用）
	 */
    private Boolean isShrink = false;

    /**
	 * 前端所用-是否收藏
	 */
    private Integer collectioned = 0;

    /**
	 * 前端所用-收藏Id
	 */
    private Long collectionId;

    /**
	 * 前端所用-文件夹名称
	 */
    private String name;

    /**
	 * 前端所用-文件夹名称
	 */
    private String label;


    /**
	 * 前端所用-父级文件夹名称
	 */
    private String folderNameParent;

    /**
	 * 前端所用-文件夹名称
	 */
    private String value;

    /**
	 * 前端所用-文件大小展示
	 */
    private String docSizeStr;

    /**
	 * 前端所用-子集
	 */
    private List<DocBsDocumentDTO> children;

    /**
	 * 剩余保留时间
	 */
    private String retainDay;
}
