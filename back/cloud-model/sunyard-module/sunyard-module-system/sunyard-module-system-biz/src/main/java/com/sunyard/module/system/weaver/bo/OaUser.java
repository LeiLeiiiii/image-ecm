package com.sunyard.module.system.weaver.bo;
/*
 * Project: Sunyard
 *
 * File Created at 2025/7/19
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import java.io.Serializable;

import lombok.Data;

/**
 * @author Leo
 * @Desc
 * @date 2025/7/19 14:43
 */
@Data
public class OaUser implements Serializable, Comparable<OaUser> {
    //-----------------入库数据
    /** 人员id */
    private String id;
    /** 登录名 */
    private String loginid;
    /** 人员名称 */
    private String lastname;
    /** 编号 */
    private String workcode;

    /** 邮箱 */
    private String email;
    /** 岗位id */
    private String jobtitle;
    /** 性别 */
    private String sex;
    /** 分部id */
    private String subcompanyid1;
    /** 部门id */
    private String departmentid;
    /** 移动电话 */
    private String mobile;
    /** 状态: 0 试用 1 正式 2 临时 3 试用延期 4 解聘 5 离职 6 退休 7 无效 */
    private String status;
    //----------------------下面的数据应该用不到
    //            //-----------------公司信息
    //            /** 主账号id （当accounttype 为 1 有效） */
    //            private String belongto;
    //            /**  主次账号标志： 1 次账号 ,其他 主账号 */
    //            private String accounttype;
    //            /** 分部编码 */
    //            private String subcompanycode;
    //            /** 分部名称 */
    //            private String subcompanyname;
    //            /** 部门编码 */
    //            private String departmentcode;
    //            /** 部门名称 */
    //            private String departmentname;
    //            /** 职级 仅支持整数字 */
    //            private String joblevel;
    //            /** 职责描述 */
    //            private String jobactivitydesc;
    //            /** 办公地点 */
    //            private String locationid;
    //            /** 办公电话 */
    //            private String telephone;
    //            /** 工会会员 */
    //            private String islabouunion;
    //            /** 合同开始日期 */
    //            private String startdate;
    //            /** 合同结束日期 */
    //            private String enddate;
    //            /** 入职日期 */
    //            private String companystartdate;
    //
    //            /** 上级人员id */
    //            private String managerid;
    //            /** 助理人员id */
    //            private String assistantid;
    //            /** 创建日期 */
    //            private String createdate;
    //            /** 密码 密文 */
    //            private String password;
    //            /** 创建日期(旧字段) */
    //            private String created;
    //            /** 最后修改日期 */
    //            private String lastmoddate;
    //            /** 系统语言 */
    //            private String language;
    //            /** 修改时间 */
    //            private String modified;
    //            /** 外键 （新增） */
    //            private String outkey;
    //            /** 安全级别 */
    //            private String seclevel;
    //            /** 排序 */
    //            private String dsporder;
    //            /** 个人自定义数据 */
    //            private String person_custom_data;
    //            //-----------------个人信息
    //            /** 身份证 */
    //            private String certificatenum;
    //            /** 民族 */
    //            private String folk;
    //            /** 生日 */
    //            private String birtday;
    //            /** 籍贯 */
    //            private String nativeplace;
    //            /** 户口 */
    //            private String residentplace;
    //            /** 户口 */
    //            private String regresidentplace;
    //            /** 暂住证号码 */
    //            private String tempresidentnumber;
    //            /** 身高 */
    //            private String height;
    //            /** 体重 */
    //            private String weight;
    //            /** 学历 */
    //            private String educationlevel;
    //            /** 学位 */
    //            private String degree;
    //            /** 职称 */
    //            private String jobcall;
    //            /** 政治面貌 */
    //            private String policy;
    //            /** 入团日期 */
    //            private String bememberdate;
    //            /** 入党日期 */
    //            private String bepartydate;
    //            /** 健康状况 良好 、 一般 、 较差 、 优秀 */
    //            private String healthinfo;
    //            /** 婚姻状况 */
    //            private String maritalstatus;
    //            /** 家庭联系方式 */
    //            private String homeaddress;
    //            /** 参加工作日期 */
    //            private String workstartdate;
    //            /** 其他电话 */
    //            private String mobilecall;

    @Override
    public int compareTo(OaUser o) {
        return this.getId().compareTo(o.getId());
    }
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/7/19 Leo creat
 */
