package com.sunyard.ecm.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author scm
 * @since 2023/8/31 16:59
 * @desc 文件复制VO
 */
@Data
public class MultiplexFileDupVO implements Serializable {
    /**
     * 目标资料节点代码(选中的左侧树节点)
     */
    private List<String> targetDocCode;

    /**
     * 目标资料节点的业务代码
     */
    private String targetAppCode;

    /**
     * 目标业务编号
     */
    private List<String> targetBusiNo;
}
