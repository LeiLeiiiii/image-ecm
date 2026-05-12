package com.sunyard.module.system.weaver.bo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * @author 周磊斌
 * @date 2017年11月07日 上午10:03:45
 * @version
 */
@Data
public class QueryUserResponse extends OaBaseResponse {
    /** */
    private static final long serialVersionUID = -2219378467471934616L;
    /** 编码 */
    private String code;
    /** 返回结果数据集 */
    private ResultBody data;

    @Data
    public static class ResultBody implements Serializable {
        /** 分页数量 */
        private Integer pageSize;
        /** 页码 */
        private Integer page;
        /** 总数 */
        private Integer totalSize;
        /** 分页数据 */
        private List<OaUser> dataList;

    }
}
