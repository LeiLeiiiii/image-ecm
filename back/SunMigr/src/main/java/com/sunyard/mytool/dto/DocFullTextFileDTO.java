package com.sunyard.mytool.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
public class DocFullTextFileDTO implements Serializable {
    /**
     * 文档id
     */
    private Long busId;
    /**
     * 文件名
     */
    private String docName;
    /**
     * 上传时间
     */
    private Date creatTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 所有者
     */
    private String docOwnerStr;

    /**
     * 标签
     */
    private List<Long> tagIds;
    /**
     * 文档类型 后缀
     */
    private String suffix;
    /**
     * 附件名称
     */
    private List<String> attchName;

    /**
     * 文档内容
     */
    private String content;


}
