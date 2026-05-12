package com.sunyard.module.storage.config.factory;

import com.baomidou.lock.LockTemplate;
import com.sunyard.module.storage.dto.FilePartInfoDTO;
import com.sunyard.module.storage.dto.StFileDTO;
import com.sunyard.module.storage.dto.UploadDTO;
import com.sunyard.module.storage.po.StFile;
import com.sunyard.module.storage.vo.UploadSplitVO;
import software.amazon.awssdk.services.s3.model.Part;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

/**
 * 文件存储接口，对应各个平台
 *
 * @author PJW
 */
public interface FileStorage extends AutoCloseable {

    /**
     * 获取平台
     *
     * @return Result
     */
    String getPlatform();



    /**
     * 获取平台类型
     *
     * @return Result
     */
    String getBucketName();


    /**
     * 初始化一个任务
     *
     * @param stFile 文件对象
     * @return
     */
    StFileDTO initTask(StFileDTO stFile);

    /**
     * 获取上传进度
     *
     * @param stFile 文件信息
     * @return Result
     */
    List<Part> getTaskInfo(StFileDTO stFile);

    /**
     * 生成预签名上传url
     *
     * @param stFile 文件信息
     * @return Result
     */
    String genPreSignUploadUrl(StFileDTO stFile);

    /**
     * 分片文件上传
     *
     * @param uploadSplitVO 分片文件信息
     * @return Result
     */
    FilePartInfoDTO uploadSplit(UploadSplitVO uploadSplitVO);

    /**
     * 合并分片
     *
     * @param stFile       文件信息
     * @param lockTemplate
     */
    void merge(StFileDTO stFile, LockTemplate lockTemplate);

    /******************************************************普通上传***************************************************/

    /**
     * 初始化一个任务
     *
     * @param uploadDTO 上传对象
     * @return Result
     */
    StFileDTO upload(UploadDTO uploadDTO);
    /******************************************************文件处理***************************************************/

    /**
     * 获取文件流
     *
     * @param key St_file表中的object_key
     * @return Result
     */
    InputStream getFileStream(String key);

    /**
     * 获取文件大小
     *
     * @param key St_file表中的object_key
     * @return Result
     */
    Long getFileSize(String key);

    /**
     * 替换文件
     * @param uploadDTO 上传对象
     * @return StFile
     */
    StFileDTO replaceFile(UploadDTO uploadDTO);

    /**
     * 获取文件地址
     *
     * @param key key
     * @return Result
     */
    String getPath(String key);

    /**
     * 删除文件
     *
     * @param url 文件相对路径
     * @return Result
     */
    boolean delete(String url);

    /**
     * 下载文件
     * @param stFile 文件对象
     * @param consumer consumer
     */
    void download(StFile stFile, Consumer<InputStream> consumer);

    /***/
    void cancelFileUpload(String bucketName, String objectKey, String uploadId);

    /**
     * 释放相关资源
     */
    @Override
    default void close() {}
}
