package com.sunyard.module.storage.config.factory.impl;

import static com.sunyard.module.storage.config.factory.impl.LocalFileStorage.PRE_SIGN_URL_EXPIRE;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.shiro.util.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;

import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.sunyard.module.storage.config.factory.FileStorage;
import com.sunyard.module.storage.constant.FileConstants;
import com.sunyard.module.storage.dto.FilePartInfoDTO;
import com.sunyard.module.storage.dto.StFileDTO;
import com.sunyard.module.storage.dto.UploadDTO;
import com.sunyard.module.storage.po.StEquipment;
import com.sunyard.module.storage.po.StFile;
import com.sunyard.module.storage.vo.UploadSplitVO;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListPartsRequest;
import software.amazon.awssdk.services.s3.model.ListPartsResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.NoSuchUploadException;
import software.amazon.awssdk.services.s3.model.Part;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.StorageClass;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * @author zyl
 * @Description
 * @since 2024/3/19 10:02
 */
@Slf4j
@Data
public class SoftWarnS3FileStorage implements FileStorage {
    private StEquipment equipment;
    private String platform;
    @Resource
    private final S3Client s3Client;

    @Resource
    private final S3Presigner s3Presigner;

    public SoftWarnS3FileStorage(StEquipment stEquipment) {
        platform = String.valueOf(stEquipment.getId());
        equipment = stEquipment;
        AwsBasicCredentials credentials = AwsBasicCredentials.create(stEquipment.getAccessKey(),
                stEquipment.getAccessSecret());

        S3ClientBuilder s3ClientBuilder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials));
        if (!stEquipment.getStorageAddress().startsWith("http")) {
            stEquipment.setStorageAddress("https://" + stEquipment.getStorageAddress());
        }
        s3Client = s3ClientBuilder.endpointOverride(URI.create(stEquipment.getStorageAddress())) // 使用 MinIO 的端点
                .region(Region.of(stEquipment.getDomainName())) // 这里可以任意选择一个区域，因为 MinIO 不完全依赖 AWS 的区域
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true)//启用路径模式
                        .chunkedEncodingEnabled(false)//启用块传输
                        .build())
                .build();

        s3Presigner = S3Presigner.builder().region(Region.of(stEquipment.getDomainName())) // 替换为您的 S3 区域
                .credentialsProvider(() -> credentials) // 使用您的访问密钥
                .build();

    }

    @Override
    public String getBucketName() {
        return equipment.getBucket();
    }

    @Override
    public StFileDTO initTask(StFileDTO stFile) {
        //首先获取当前时间作为文件上传到云存储服务的文件夹名称，同时获取云存储服务的bucket名称以及上传的文件名。
        String key = stFile.getObjectKey();
        //根据上传的文件名获取文件后缀名，并使用工具类StrUtil和DateUtil生成唯一的文件名。
        //获取上传文件的Content-Type，并创建一个ObjectMetadata对象来保存Content-Type信息。
        String contentType = MediaTypeFactory.getMediaType(key)
                .orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
        try {
            CreateMultipartUploadRequest.Builder createMultipartUploadRequestBuilder = CreateMultipartUploadRequest
                    .builder();
            createMultipartUploadRequestBuilder.bucket(equipment.getBucket())
                    .key(stFile.getObjectKey()).storageClass(StorageClass.STANDARD);

            if (StringUtils.hasText(contentType)) {
                createMultipartUploadRequestBuilder.contentType(contentType);
            }

            // 开启上传任务
            CreateMultipartUploadResponse createMultipartUploadResponse = s3Client
                    .createMultipartUpload(createMultipartUploadRequestBuilder.build());
            String uploadId = createMultipartUploadResponse.uploadId();
            stFile.setUploadId(uploadId);
            stFile.setBucketName(equipment.getBucket());

        } catch (S3Exception e) {
            // 处理AWS S3服务异常
            // ...（例如，记录日志、抛出更具体的异常等）
            throw new RuntimeException("初始化多部分上传失败", e);
        }

        return stFile;
    }

    @Override
    public List<Part> getTaskInfo(StFileDTO stFile) {
        try {
            // 尝试获取对象头部信息，但这并不直接告诉我们它是否是一个多部分上传
            // 这里主要是为了捕获 NoSuchKeyException 来确定对象不存在
            s3Client.headObject(HeadObjectRequest.builder().bucket(equipment.getBucket())
                    .key(stFile.getObjectKey()).build());

            // 如果对象存在，并且我们有一个有效的 uploadId，则尝试列出部件
            if (stFile.getUploadId() != null && !stFile.getUploadId().isEmpty()) {
                ListPartsRequest listPartsRequest = ListPartsRequest.builder()
                        .bucket(stFile.getBucketName()).key(stFile.getObjectKey())
                        .uploadId(stFile.getUploadId()).build();
                ListPartsResponse partListing = s3Client.listParts(listPartsRequest);
                return partListing.parts(); // 注意：在 SDK 2.x 中，使用 parts() 而不是 getParts()
            }

            // 如果没有 uploadId，或者我们不想处理多部分上传的情况，可以返回空列表
            return Collections.emptyList();

        } catch (NoSuchKeyException e) {
            // 对象不存在
            return Collections.emptyList();
        } catch (SdkClientException | SdkServiceException e) {
            // 处理其他类型的异常，如网络错误或权限问题
            return Collections.emptyList(); // 或者你可以抛出一个自定义的异常
        }
    }

    @Override
    public String genPreSignUploadUrl(StFileDTO file) {
        Map<String, String> params = new HashMap<>(6);
        //        params.put("partNumber", String.valueOf(file.getConfigId()));
        params.put("partNumber", "");
        params.put("uploadId", file.getUploadId());
        URL url = s3Presigner.presignPutObject(x -> x
                .signatureDuration(Duration.ofMillis(PRE_SIGN_URL_EXPIRE.intValue()))
                .putObjectRequest(
                        y -> y.bucket(file.getBucketName()).key(file.getObjectKey()).build())
                .build()).url();
        String baseUrl = url.toString();

        String queryString = params.entrySet().stream().map(entry -> {
            try {
                return URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                        + URLEncoder.encode(entry.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.joining("&"));

        // 如果URL已经包含查询参数，则添加&；否则添加?
        if (baseUrl.contains("?")) {
            return baseUrl + "&" + queryString;
        } else {
            return baseUrl + "?" + queryString;
        }
    }

    @Override
    public FilePartInfoDTO uploadSplit(UploadSplitVO uploadSplitVO) {
        FilePartInfoDTO filePartInfoDTO = new FilePartInfoDTO();
        InputStream inputStream = uploadSplitVO.getInputStream();
        try {
            // 设置分片上传参数
            UploadPartRequest partRequest = UploadPartRequest.builder()
                    .bucket(equipment.getBucket()).key(uploadSplitVO.getKey())
                    .uploadId(uploadSplitVO.getUploadId()).partNumber(uploadSplitVO.getPartNumber())
                    .build();

            // 注意：AWS SDK 2.x 不直接接受InputStream和partSize的组合。你需要使用RequestBody来包装InputStream
            RequestBody requestBody = RequestBody.fromInputStream(inputStream,
                    uploadSplitVO.getPartSize());

            // 执行分片上传
            UploadPartResponse partResponse = s3Client.uploadPart(partRequest, requestBody);

            String eTag = partResponse.eTag();

            filePartInfoDTO.setPartSize(uploadSplitVO.getPartSize())
                    .setPartNumber(uploadSplitVO.getPartNumber()).setCreateTime(new Date())
                    .setETag(eTag);

        } catch (NoSuchUploadException e) {
            log.error("上传id无效");
            //无效需要清理掉无效的文件
            throw new RuntimeException("上传id无效", e);
        } catch (S3Exception e) {
            log.error("上传分片文件失败", e);
            throw new RuntimeException("上传分片文件失败:", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭输入流失败", e);
                    throw new RuntimeException(e);
                }
            }
        }

        return filePartInfoDTO;
    }

    @Override
    public void merge(StFileDTO stFile, LockTemplate lockTemplate) {
        completeMultipartUploadConcurrently(stFile, lockTemplate);
        //        // 判断文件是否存在（在 S3 中，直接检查对象是否存在，因为 multipart 上传会创建一个特殊的 multipart 上下文）
        //        // 注意：这里不直接检查对象是否存在，因为 multipart 上传中对象是在完成上传后创建的
        //
        //        // 列出所有分块
        //        ListPartsRequest listPartsRequest = ListPartsRequest.builder()
        //                .bucket(equipment.getBucket())
        //                .key(stFile.getObjectKey())
        //                .uploadId(stFile.getUploadId()).build();
        //        ListPartsResponse listPartsResponse = null;
        //        try {
        //            listPartsResponse = s3Client.listParts(listPartsRequest);
        //
        //        }catch (NoSuchUploadException e){
        //            try {
        //                Thread.sleep(10000);
        //            } catch (InterruptedException ex) {
        //                throw new RuntimeException(ex);
        //            }
        //            try {
        //                listPartsResponse = s3Client.listParts(listPartsRequest);
        //            }catch (NoSuchUploadException m){
        //                m.printStackTrace();
        //            }
        //
        //        }catch (SdkClientException m){
        //            log.info("报错了2："+stFile.getUploadId());
        //        }
        //        List<Part> parts = listPartsResponse.parts();
        //
        //        // 检查已上传的分块数量是否与记录中的数量对应
        //        if (stFile.getChunkNum() != parts.size()) {
        //            //有可能分片上传还没有完全成功
        //            throw new RuntimeException(ResultCode.OSS_FAIL_SIZE.getMsg());
        //        }
        //
        //        List<CompletedPart> completedParts = parts.stream().map(partSummary -> CompletedPart.builder()
        //                .partNumber(partSummary.partNumber()) // 假设 PartETag 有一个名为 partNumber 的方法
        //                .eTag(partSummary.eTag()) // 假设 PartETag 有一个名为 eTag 的方法
        //                .build()).collect(Collectors.toList());
        //
        //        // 合并 multipart 上传
        //        CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder()
        //                .bucket(equipment.getBucket())
        //                .key(stFile.getObjectKey())
        //                .uploadId(stFile.getUploadId())
        //                .multipartUpload(CompletedMultipartUpload.builder().parts(completedParts) // 注意这里使用的是 CompletedPart 列表
        //                        .build()).build();
        //
        //        try {
        //            s3Client.completeMultipartUpload(completeMultipartUploadRequest);
        //        }catch (S3Exception s3Exception){
        //            s3Exception.printStackTrace();
        //        }
    }

    public void completeMultipartUploadConcurrently(StFileDTO stFile, LockTemplate lockTemplate) {
        String uploadId = stFile.getUploadId();
        String bucket = equipment.getBucket();
        String key = stFile.getObjectKey();

        // 1. 获取分布式锁
        String lockKey = "UPLOAD_COMPLETE:" + uploadId;
        LockInfo lock = lockTemplate.lock(lockKey, FileConstants.MERRGE_EXPIRE,
                FileConstants.MERRGE_ACQUIRETIMEOUT);
        if (lock == null) {
            log.warn("文件合并操作超时，文件可能正在被其他进程处理: {}", uploadId);
            throw new RuntimeException("文件正在处理中，请稍后重试");
        }

        try {
            // 2. 检查对象是否已存在（幂等性检查）
            if (isObjectExists(bucket, key)) {
                log.info("Upload already completed: {}", uploadId);
                return;
            }

            // 3. 列出分片
            ListPartsRequest listRequest = ListPartsRequest.builder().bucket(bucket).key(key)
                    .uploadId(uploadId).build();

            ListPartsResponse partsResponse;
            try {
                partsResponse = s3Client.listParts(listRequest);
            } catch (NoSuchUploadException e) {
                // 检查是否对象已存在（竞态条件）
                if (isObjectExists(bucket, key)) {
                    log.info("Upload completed by another process: {}", uploadId);
                    return;
                }
                throw new RuntimeException("Upload does not exist and was not completed", e);
            }
            List<Part> parts = partsResponse.parts();
            // 4. 验证分片（更健壮的检查）todo
            if (stFile.getChunkNum() != parts.size()) {
                throw new RuntimeException("Not all parts are uploaded");
            }

            // 5. 完成上传
            List<CompletedPart> completedParts = partsResponse
                    .parts().stream().map(part -> CompletedPart.builder()
                            .partNumber(part.partNumber()).eTag(part.eTag()).build())
                    .collect(Collectors.toList());

            CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest
                    .builder().bucket(bucket).key(key).uploadId(uploadId)
                    .multipartUpload(
                            CompletedMultipartUpload.builder().parts(completedParts).build())
                    .build();

            try {
                s3Client.completeMultipartUpload(completeRequest);
                log.info("Multipart upload completed successfully: {}", uploadId);
            } catch (NoSuchUploadException e) {
                // 再次检查幂等性
                if (isObjectExists(bucket, key)) {
                    log.info("Upload completed by concurrent process: {}", uploadId);
                    return;
                }
                throw e;
            }

        } finally {
            lockTemplate.releaseLock(lock);
        }
    }

    // 辅助方法：检查对象是否存在
    private boolean isObjectExists(String bucket, String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.warn("Failed to check object existence", e);
            return false;
        }
    }

    // 辅助方法：验证所有期望的分片都已上传
    private boolean isUploadComplete(List<Part> uploadedParts, Set<Integer> expectedPartNumbers) {
        Set<Integer> uploadedPartNumbers = uploadedParts.stream().map(Part::partNumber)
                .collect(Collectors.toSet());
        return uploadedPartNumbers.containsAll(expectedPartNumbers);
    }

    @Override
    public StFileDTO upload(UploadDTO uploadDTO) {
        String key = uploadDTO.getKey();
        StFileDTO stFile = null;

        InputStream inputStream = uploadDTO.getInputStream();
        stFile = new StFileDTO().setBucketName(equipment.getBucket())
                .setFilePath(equipment.getBucket() + "/" + key).setUrl(StrUtil.format("{}/{}/{}",
                        equipment.getStorageAddress(), equipment.getBucket(), key));
        s3Client.putObject(
                PutObjectRequest.builder().bucket(equipment.getBucket()).key(key).build(),
                RequestBody.fromInputStream(inputStream, uploadDTO.getFileSize()));
        return stFile;
    }

    @Override
    public InputStream getFileStream(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(equipment.getBucket()).key(key).build();
            ResponseBytes<GetObjectResponse> objectAsBytes = s3Client
                    .getObjectAsBytes(getObjectRequest);
            InputStream inputStream = objectAsBytes.asInputStream();
            return inputStream;

        } catch (S3Exception e) {
            log.error("获取文件流错误:",e);
            return null;
        }
    }

    @Override
    public Long getFileSize(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(equipment.getBucket())
                .key(key).build();
        // 获取对象元数据
        ResponseInputStream<GetObjectResponse> object = s3Client.getObject(getObjectRequest);
        return object.response().contentLength();
    }

    @Override
    public StFileDTO replaceFile(UploadDTO uploadDTO) {
        delete(uploadDTO.getKey());
        //删除元数据
        return upload(uploadDTO);
    }

    @Override
    public String getPath(String key) {
        return StrUtil.format("{}/{}/{}", equipment.getStorageAddress(), equipment.getBucket(),
                key);
    }

    @Override
    public boolean delete(String key) {
        try {
            log.info("正在删除对象: {}/{}", equipment.getBucket(), key);
            s3Client.deleteObject(
                    DeleteObjectRequest.builder().bucket(equipment.getBucket()).key(key).build());
            return true;
        } catch (S3Exception e) {
            log.error("删除对象失败: {}/{}", equipment.getBucket(), key, e);
            throw new RuntimeException("删除对象时发生错误", e);
        }
    }

    @Override
    public void download(StFile stFile, Consumer<InputStream> consumer) {

    }

    @Override
    public void cancelFileUpload(String bucketName, String objectKey, String uploadId) {
        try {
            AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                    .bucket(bucketName).key(objectKey).uploadId(uploadId).build();

            s3Client.abortMultipartUpload(abortRequest);
        } catch (S3Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException("中止多部分上传时发生错误", e);
        }
    }
}
