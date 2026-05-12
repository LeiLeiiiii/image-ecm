package com.sunyard.module.storage.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author： zyl
 * @Description：
 * @create： 2023/6/26 13:23
 * Jackson 在解析 JSON 数据时忽略未知字段，而不会抛出异常
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class DownFileVO {

    /**
     * 下载的文件id
     */
    private List<Long> fileId;

    /**
     * 压缩包名字
     */
    private String busiNo;

    /**
     * 是否压缩包的形式下载 (1:以压缩包的形式下载,不是1不以压缩包的形式下载)
     */
    private Integer isPack;

    /**
     * 页面标识 (对外接口使用)
     */
    private String pageFlag;

    /**
     * 会话id
     */
    private String sessionId;

    /**
     * 登录名称
     */
    private String username;

    /**
     * 用户name
     */
    private String name;

    /**
     * 机构code  /银行影像  contentId
     */
    private String orgCode;

    /**
     * 机构名称/银行影像 uploadDate上传日期yyyyMMdd(银行影像使用)
     */
    private String orgName;

    /**
     * Map<文件id ( st_file表中的id ) , 文件名称>
     */
    private Map<String, String> fileIdNameMap;

    /**
     * 文件名称(包含后缀)  用于分段下载所需要的必要的属性，如果不需要分片下载 则可以不传
     */
    private String fileName;
    /**
     * 值为1 则为银行影像下载 其他都为minio
     */
    private Integer type;

    /**
     * 文档密码
     */
    private String password;

}
