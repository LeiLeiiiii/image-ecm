package com.sunyard.module.system.weaver.bo;

import lombok.Data;
import org.thymeleaf.util.StringUtils;

/**
 * @author wml
 * @description 获取岗位列表请求参数
 */
@Data
public class QueryPostRequest extends OaBaseRequest {
    /** */
    private static final long serialVersionUID = 1L;

    /** oa中岗位id */
    private String id;
    /** 岗位编号 */
    private String jobtitlecode;
    /** 岗位名称 */
    private String jobtitlename;
    /** 修改时间戳；（使用>=） */
    private String modified;
    /** 创建时间戳；（使用>=） */
    private String created;
    /** 页大小，默认20 */
    private Integer pagesize;
    /** 当前页，默认1 */
    private Integer curpage;

    /** /api/hrm/resful/getHrmdepartmentWithPage */
    @Override
    public String getUrl() {
        return StringUtils.concat(getBaseUrl(), "getJobtitleInfoWithPage");
    }

}
