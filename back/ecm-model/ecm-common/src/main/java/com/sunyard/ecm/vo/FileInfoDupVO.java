package com.sunyard.ecm.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author scm
 * @since 2023/8/31 16:58
 * @desc 文件复制VO
 */
@Data
public class FileInfoDupVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 源资料节点代码(选中的左侧树节点)
     */
    private String sourceDocCode;

    /**
     * 源资料节点的业务代码
     */
    private String sourceAppCode;

    /**
     * 源业务编号
     */
    private String sourceBusiNo;

    /**
     * 选中要复用的资料节点信息
     */
    private List<MultiplexFileDupVO> multiplexFileDupVOS;
}
