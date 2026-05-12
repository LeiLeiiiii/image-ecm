package com.sunyard.module.storage.vo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author： zyl
 * @Description：
 * @create： 2023/7/19 13:13
 */
@Data
public class UploadListVO implements Serializable {

    /**
     * 文件
     */
    private byte[] fileByte;
    /**
     * 设备id  / 银行影像上传  batchId(arcId)
     */
    private Long stEquipmentId;
    /**
     * 上传人id
     */
    private Long userId;
    /**
     * 文件来源(服务名：使用spring:application:name)/ 银行影像上传  filePath  /银行 补传接口 contentId
     */
    @NotNull(message = "文件来源不能为空")
    private String fileSource;
    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件
     */
    private MultipartFile multipartFile;

    /**
     * 文件md5 非必填可为空 / 银行上传 上传日期 yyyyMMdd
     */
    private String md5;
    /**
     * 是否加密 加密：1  不加密：其他数字或空
     */
    private Integer isEncrypt;

    /**
     * 值为1 则为银行影像上传 2则为银行影像补传
     */
    private Integer type;
}
