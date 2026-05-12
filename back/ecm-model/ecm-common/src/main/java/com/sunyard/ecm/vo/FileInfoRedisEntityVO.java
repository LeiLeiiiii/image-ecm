package com.sunyard.ecm.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @Description：文件实体信息VO
 * @create： 2023/6/2 15:08
 */
@Data
public class FileInfoRedisEntityVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 源资料节点id(选中的左侧树节点)
     */
    private String sourceDocId;

    /**
     * 源资料节点的业务id
     */
    private Long sourceBusiId;

    /**
     * 源资料节点的业务类型代码
     */
    private String sourceAppCode;

    /**
     * 源资料节点的业务类型代码
     */
    private String sourceAppTypeName;

    /**
     * 源资料节点的业务主索引
     */
    private String sourceBusiNo;

    /**
     * 源资料类型id
     */
    private String sourceDocTypeId;

    /**
     * 选中要复用的资料节点信息
     */
    private List<MultiplexFileVO> multiplexFileVO;

}
