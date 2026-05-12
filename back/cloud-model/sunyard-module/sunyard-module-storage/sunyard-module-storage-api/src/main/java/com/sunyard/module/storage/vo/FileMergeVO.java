package com.sunyard.module.storage.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/5/5 14:54
 */
@Data
public class FileMergeVO implements Serializable {

    /**
     * 合成后文件的名字-默认名称为资料类型名称
     */
    private String name;

    /**
     * 业务批次号
     */
    private String busiBatchNo;

    /**
     * 存储设备id
     */
    private Long equipmentId;

    /**
     * 要合并的文件信息集合
     */
    private List<Long> fileIdList;

    /**
     * 是否加密0否 1是
     */
    private Integer isEncrypt;

    /**
     * 文件密码
     */
    private String password;
}
