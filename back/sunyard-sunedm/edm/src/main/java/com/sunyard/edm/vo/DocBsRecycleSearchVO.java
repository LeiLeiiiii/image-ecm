package com.sunyard.edm.vo;

import lombok.Data;
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
public class DocBsRecycleSearchVO {
    /**
	 * 前端筛选
	 */
    List<Long> tagId;

    /**
	 * 文档库id
	 */
    Long houseId;
    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date delStartDate;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date delEndDate;


    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createStartDate;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createEndDate;


    /**
	 * 后缀字典表值
	 */
    private String dictionSuffix;

    /**
	 * 前端后缀清除展示
	 */
    private Boolean dictionSuffixFlag;


    /**
	 * 上传时间排序 0升序 1降序
	 */
    private String delTimeSort;

    /**
	 * 剩余保留时间排序 0升序 1降序
	 */
    private String recycleDateSort;

    /**
	 * 所有者
	 */
    private String docOwner;

    /**
	 * 文档名称
	 */
    private String docName;

    /**
	 * 类型：0:企业，1:个人
	 */
    private Integer docType;

}
