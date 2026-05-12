package com.sunyard.module.system.weaver.bo;

import org.thymeleaf.util.StringUtils;

import lombok.Data;

/**
 * @author 周磊斌
 * @date 2017年11月07日 上午10:03:45
 * @@description 获取分部列表
 */
@Data
public class QueryInstRequest extends OaBaseRequest {
    /** */
    private static final long serialVersionUID = 1L;
        /** oa中分部id */
        private String id;
        /** 分部编号 */
        private String subcompanycode;
        /** 分部名称 */
        private String subcompanyname;
        /** 修改时间戳；（使用>=） */
        private String modified;
        /** 封存标志；默认查询非封存数据。1:封存。 2：获取全部数据 */
        private String canceled;
        /** 指定获取自定义字段数据字段列表，（多个逗号分隔），具体参考 【组织权限中心-自定义设置-分部字段定义】; 示例： zdy2 */
        private String custom_data;
        /** 页大小，默认20 */
        private Integer pagesize;
        /** 当前页，默认1 */
        private Integer curpage;

    /** /api/hrm/resful/getHrmsubcompanyWithPag */
    @Override
    public String getUrl() {
        return StringUtils.concat(getBaseUrl(), "getHrmsubcompanyWithPage");
    }

}
