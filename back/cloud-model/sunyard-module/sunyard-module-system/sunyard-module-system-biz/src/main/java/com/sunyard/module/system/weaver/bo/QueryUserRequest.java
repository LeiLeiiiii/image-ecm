package com.sunyard.module.system.weaver.bo;

import org.thymeleaf.util.StringUtils;

import lombok.Data;

/**
 * @author 周磊斌
 * @date 2017年11月07日 上午10:03:45
 * @Description: 获取人员信息列表
 */
@Data
public class QueryUserRequest extends OaBaseRequest {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** 人员id */
    private String id;
    /** 登录名 */
    private String loginid;
    /** 编号 */
    private String workcode;
    /** 分部id */
    private String subcompanyid1;
    /** 部门id */
    private String departmentid;
    /** 岗位id */
    private String jobtitleid;
    /** 同work_custom_data解释 （工作信息） */
    private String work_custom_data;
    /** 指定获取OA自定义字段的列表（具体看【组织权限中心】-【自定义设置】-【人员卡片字段定义】基本信息），
     * 示例:base_custom_data:’field1,field2,field3,field4’ 获取基本信息中
     * */
    private String base_custom_data;
    /** 同base_custom_data解释 （个人信息） */
    private String person_custom_data;
    /** 是否ad账号；1 是ad账号；其他值非ad账号。 */
    private String isadaccount;
    /** 修改时间戳；（使用>=） */
    private String modified;
    /** 封存标志；默认查询非封存数据。1:封存。 2：获取全部数据 */
    private String canceled;
    /** 页大小，默认20 */
    private Integer pagesize;
    /** 当前页，默认1 */
    private Integer curpage;

    /** /api/hrm/resful/getHrmUserInfoWithPage */
    @Override
    public String getUrl() {
        return StringUtils.concat(getBaseUrl(), "getHrmUserInfoWithPage");
    }

}
