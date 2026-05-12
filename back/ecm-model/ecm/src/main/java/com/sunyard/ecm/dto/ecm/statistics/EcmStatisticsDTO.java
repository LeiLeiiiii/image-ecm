package com.sunyard.ecm.dto.ecm.statistics;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;

/**
 @Author 朱山成
 @time 2024/6/11 16:31
 **/
@Data
public class EcmStatisticsDTO {

    /**
     * 机构集合
     */
    private List<String> orgCodes;
    /**
     * 业务号集合
     */
    private List<String> appCodes;
    /**
     * 统计单位 0天 1月 2年
     */
    private Integer unit;
    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDate;

    /**
     * 当前页
     */
    private Integer pageNum;
    /**
     * 页数
     */
    private Integer pageSize;
    /**
     * 排序字段
     */
    private String sortColumn;
    /**
     * 排序方式
     */
    private String sortRule;
    /**
     * 业务总数量
     */
    private Integer appCodeSize;

    /**
     * 将前端 sortColumn 映射为数据库实际列名
     */
    public String getSortColumnDbName() {
        if (ObjectUtils.isEmpty(sortColumn)) {
            return null;
        }
        switch (sortColumn) {
            case "daySize":
                return "a.busi_number";
            case "fileNumber":
                return "a.file_number";
            case "fileSize":
                return "a.file_size";
            case "createTime":
                return "a.create_time";
            case "orgCode":
                return "a.org_code";
            case "appCode":
                return "a.app_code";
            default:
                return null;
        }
    }

    /**
     * 判断该字段是否在聚合后支持 Java 排序
     */
    public boolean isSortable() {
        if (ObjectUtils.isEmpty(sortColumn) || ObjectUtils.isEmpty(sortRule)) {
            return false;
        }
        return "daySize".equals(sortColumn)
            || "fileNumber".equals(sortColumn)
            || "fileSize".equals(sortColumn)
            || "createDate".equals(sortColumn);
    }
}
