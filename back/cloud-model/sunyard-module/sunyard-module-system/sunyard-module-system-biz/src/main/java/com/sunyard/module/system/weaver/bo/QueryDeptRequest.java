package com.sunyard.module.system.weaver.bo;

import org.thymeleaf.util.StringUtils;

import lombok.Data;

/**
 * @author 周磊斌
 * @description 获取部门列表
 */
@Data
public class QueryDeptRequest extends OaBaseRequest {
    /** */
    private static final long serialVersionUID = 1L;

    /** oa中部门id */
    private String id;
    /** 部门编号 */
    private String departmentcode;
    /** 部门名称 */
    private String departmentname;
    /** 分部id */
    private String subcompanyid1;
    /** 修改时间戳；（使用>=） */
    private String modified;
    /** 封存标志；默认查询非封存数据。1:封存。 2：获取全部数据 */
    private String canceled;
    /** 值内容是指定获取OA自定义字段的列表（具体看 【组织权限中心】-【自定义设置】-【部门字段定义】），
     * 示例: custom_data:’xlk,field2,field3,field4’ 获取自定义 信息中
     * */
    private String custom_data;
    /** 页大小，默认20 */
    private Integer pagesize;
    /** 当前页，默认1 */
    private Integer curpage;

    /** /api/hrm/resful/getHrmdepartmentWithPage */
    @Override
    public String getUrl() {
        return StringUtils.concat(getBaseUrl(), "getHrmdepartmentWithPage");
    }

}
