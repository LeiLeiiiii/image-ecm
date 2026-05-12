package com.sunyard.framework.onlyoffice.dto;


import java.io.Serializable;

import com.sunyard.framework.onlyoffice.dto.documentfunction.FwDocumentInfo;
import com.sunyard.framework.onlyoffice.dto.documentfunction.FwDocumentPermission;

import lombok.Builder;
import lombok.Data;

/**
 * file文档部分
 * @author PJW
 */
@Data
@Builder(toBuilder = true)
public class FwDocumentConfig implements Serializable {
    /**
     * 文件类型 如 docx
     * 只需要文件的扩展名
     */
    private String fileType;

    /**
     * 文件名称
     */
    private String title;

    /**
     * 文件访问的url
     */
    private String url;

    /**
     * 定义用于服务的文档识别的唯一文档标识符。在发送已知密钥的情况下，文档将从高速缓存中取出。
     * 每次编辑和保存文档时，必须重新生成密钥。
     * 文档url可以用作键，但是没有特殊字符，长度限制为20个符号。
     */
    private String key;

    /**
     * 文件作者信息
     */
    private FwDocumentInfo info;

    /**
     * 权限
     */
    private FwDocumentPermission permissions;


}
