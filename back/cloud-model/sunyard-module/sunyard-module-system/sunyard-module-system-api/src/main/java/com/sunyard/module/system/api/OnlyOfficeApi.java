package com.sunyard.module.system.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.FileMetaDTO;
import com.sunyard.module.system.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author 朱山成
 */
@FeignClient(name = ApiConstants.NAME)
public interface OnlyOfficeApi {

    String PREFIX = ApiConstants.PREFIX + "/onlyOffice/";

    /**
     * 打开文档
     *
     * @param fileId               文件唯一id
     * @param fileName             文件名称
     * @param fileType             文件后缀名
     * @param fileSize             文件大小
     * @param fileUrl              文件下载地址
     * @param userId               打开文件用户id
     * @param userName             打开文件用户名
     * @param mode                 edit/view 编辑模式/查看模式
     * @param collaborativeEditing 是否修改
     * @return Result 返参
     */
    @PostMapping(PREFIX + "openDocument")
    Result<Map> openDocument(@RequestParam("fileId") String fileId,
                             @RequestParam("fileName") String fileName,
                             @RequestParam("fileType") String fileType,
                             @RequestParam("fileSize") String fileSize,
                             @RequestParam("fileUrl") String fileUrl,
                             @RequestParam("userId") String userId,
                             @RequestParam("userName") String userName,
                             @RequestParam("mode") String mode,
                             @RequestParam("collaborativeEditing") boolean collaborativeEditing);

    /**
     * 文件转换
     *
     * @param fileType    文件类型
     * @param downloadUrl 文件id
     * @param outputType  转换类型
     * @param title       转换后的名称
     * @param password    文档密码
     * @return Result 转换后的文件下载地址
     */
    @PostMapping(PREFIX + "converted")
    Result<String> converted(@RequestParam("fileType") String fileType,
                             @RequestParam("downloadUrl") String downloadUrl,
                             @RequestParam("outputType") String outputType,
                             @RequestParam("title") String title,
                             @RequestParam(value = "password", required = false) String password);

    /**
     * 处理获取onlyoffice 下载连接
     *
     * @param jsonStr json对象
     * @return Result
     */
    @PostMapping(PREFIX + "handlerStatus")
    Result<String> handlerStatus(@RequestParam("jsonStr") String jsonStr);


    /**
     * 删除临时文件
     *
     * @param key key
     */
    @PostMapping(PREFIX + "removeTempFile")
    void removeTempFile(@RequestParam("key") String key);

    /**
     * 获取文件元数据
     *
     * @param key
     * @return Result 元数据
     */
    @PostMapping(PREFIX + "getTempFile")
    Result<FileMetaDTO> getTempFile(@RequestParam("key") String key);

    /**
     * 获取历史数量上限
     *
     * @return Result 上限
     */
    @PostMapping(PREFIX + "getHistNum")
    Result<Integer> getHistNum();

    /**
     * 获取打开文档时的唯一key
     *
     * @param id     id
     * @param userId 用户id
     * @return Result
     */
    @PostMapping(PREFIX + "getKey")
    Result<String> getKey(@RequestParam("id") String id, @RequestParam("userId") Long userId);

    /**
     * 当文件没有人使用时，清空文件信息
     *
     * @param key key
     */
    @PostMapping(PREFIX + "close")
    void close(@RequestParam("key") String key);

    /**
     * redis中存放文件使用人数
     *
     * @param key   key
     * @param users 用户格式
     * @return result
     */
    @PostMapping(PREFIX + "iskey")
    Result iskey(@RequestParam("key") String key, @RequestParam("users") Integer users);

    /**
     * 获取文档是使用人数
     *
     * @param key key
     * @return result
     */
    @PostMapping(PREFIX + "getUserNum")
    Result getUserNum(@RequestParam("key") String key);

    /**
     * 获取文件id
     *
     * @param key key
     * @return result
     */
    @PostMapping(PREFIX + "getFileId")
    Result<String> getFileId(@RequestParam("key") String key);

    /**
     * 获取公共服务url
     *
     * @return result
     */
    @PostMapping(PREFIX + "getCommandServiceUrl")
    Result<String> getCommandServiceUrl();

    /**
     * 过期过期时间
     *
     * @return result
     */
    @PostMapping(PREFIX + "getTimeout")
    Result<Integer> getTimeout();

    /**
     * 下载文件
     *
     * @return result
     */
    @PostMapping(PREFIX + "downloadFileRequestPath")
    Result<String> downloadFileRequestPath();

    /**
     * =
     * 文件保存
     *
     * @param key    key
     * @param userId 用户id
     * @return result
     */
    @PostMapping(PREFIX + "save")
    Result<String> save(@RequestParam("key") String key,
                        @RequestParam("userId") String userId);


}
