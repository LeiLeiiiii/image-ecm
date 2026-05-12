package com.sunyard.mytool.service.file.impl;



import com.sunyard.mytool.dto.UploadDTO;
import com.sunyard.mytool.entity.StEquipment;
import com.sunyard.mytool.service.file.FileStroageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;


public class FileStroageServiceImpl implements FileStroageService {
    Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Resource
    private final S3Client s3Client;
    @Resource
    private final StEquipment equipment;

    public FileStroageServiceImpl(StEquipment stEquipment) {
        equipment = stEquipment;
        AwsBasicCredentials credentials = AwsBasicCredentials.create(stEquipment.getAccessKey(), stEquipment.getAccessSecret());

        S3ClientBuilder s3ClientBuilder = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(credentials));
        if (!stEquipment.getStorageAddress().startsWith("http")) {
            stEquipment.setStorageAddress("https://" + stEquipment.getStorageAddress());
        }
        //设备如果是oss需要专门处理下  以中国香港地域为例，S3兼容的外网Endpoint格式为s3.oss-cn-hongkong.aliyuncs.com 且路径模式要关闭
        if (stEquipment.getStorageAddress().contains("oss")) {
            String s3CompatibleEndpoint = stEquipment.getStorageAddress().replace("oss-", "s3.oss-");
            s3Client = s3ClientBuilder
                    .region(Region.of(stEquipment.getDomainName()))
                    .endpointOverride(URI.create(s3CompatibleEndpoint))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(false)
                            .chunkedEncodingEnabled(false)
                            .build())
                    .build();

        } else {
            s3Client = s3ClientBuilder
                    .endpointOverride(URI.create(stEquipment.getStorageAddress())) // 使用 MinIO 的端点
                    .region(Region.of(stEquipment.getDomainName())) // 这里可以任意选择一个区域，因为 MinIO 不完全依赖 AWS 的区域
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)//启用路径模式
                            .chunkedEncodingEnabled(false)//启用块传输
                            .build())
                    .build();
        }
        /*s3Client = s3ClientBuilder
                .endpointOverride(URI.create(stEquipment.getStorageAddress())) // 使用 MinIO 的端点
                .region(Region.of(stEquipment.getDomainName())) // 这里可以任意选择一个区域，因为 MinIO 不完全依赖 AWS 的区域
                .serviceConfiguration(S3Configuration.builder()
                    .pathStyleAccessEnabled(true)//启用路径模式
                    .chunkedEncodingEnabled(false)//启用块传输
                    .build())
                .build();*/
    }

    @Override
    public void fileUpload(String filePath, String bucketName, String key) {
        File file = new File(filePath);
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromFile(file)
        );

    }

    @Override
    public void fileUpload(File file, String bucketName, String key) {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromFile(file)
        );

    }

    /**
     * 获取指定 bucket 和 key 对应的文件输入流
     *
     * @param bucket 存储桶名称
     * @param key    文件路径/键名
     * @return 文件输入流
     */
    public InputStream getFileStream(String bucket, String key) {
        try {
            // 构建 GetObjectRequest 请求
            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(objectRequest);
            InputStream inputStream = objectAsBytes.asInputStream();
            return inputStream;
            // 获取文件流
            //return s3Client.getObject(objectRequest);
        } catch (S3Exception e) {
            throw new RuntimeException("获取文件流失败: bucket=" + bucket + ", key=" + key + e.getMessage(), e);
        }
    }

    /**
     * 获取指定 bucket 和 key 对应的文件大小（单位：字节）
     *
     * @param bucket 存储桶名称
     * @param key    文件路径/键名
     * @return 文件大小（字节）
     */
    public long getFileSize(String bucket, String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);
            return headObjectResponse.contentLength();
        } catch (S3Exception e) {
            throw new RuntimeException("无法获取文件大小: bucket=" + bucket + ", key=" + key, e);
        }
    }

    @Override
    public void upload(UploadDTO uploadDTO) {
        String key = uploadDTO.getKey();
        InputStream inputStream = uploadDTO.getInputStream();
        s3Client.putObject(PutObjectRequest.builder().bucket(equipment.getBucket()).key(key).build(),
                RequestBody.fromInputStream(inputStream, uploadDTO.getFileSize()));
    }



    public static void  file2Upload() throws IOException {
        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create("gvcya0uQBoeSUZeH", "b37hlK9Q8AioS7Ebp8aNHUUg1WRyD4rW");
            S3ClientBuilder s3ClientBuilder = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(credentials));
            S3Client s3Client = s3ClientBuilder
                    .endpointOverride(URI.create("http://172.1.3.165:8089")) // 使用 MinIO 的端点
                    .region(Region.of("us-east-1")) // 这里可以任意选择一个区域，因为 MinIO 不完全依赖 AWS 的区域
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)//启用路径模式
                            .chunkedEncodingEnabled(false)//启用块传输
                            .build())
                    .build();
            String filePath = "D:\\temp\\sunyard\\data\\UWC\\2025\\07\\03\\39\\89\\4501c76810883a6dad53d296bf5e9386_1\\6baf05dc-8d17-4542-bc86-c8f563e1fac4.jpg";
            File file = new File(filePath);
            InputStream inputStream = new FileInputStream(file);
            Long fileSize = file.length();
            s3Client.putObject(PutObjectRequest.builder().bucket("bigfile").key("1/testwamg.jpg").build(),
                    RequestBody.fromInputStream(inputStream, fileSize));

            // 5. 关闭流
            inputStream.close();
            System.out.println("文件上传成功！");
        } catch (AwsServiceException e) {
            throw new RuntimeException(e);
        } catch (SdkClientException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) throws IOException {
        file2Upload();
    }


}