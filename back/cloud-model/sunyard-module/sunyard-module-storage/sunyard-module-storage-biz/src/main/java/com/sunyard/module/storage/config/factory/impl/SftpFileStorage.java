package com.sunyard.module.storage.config.factory.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.function.Consumer;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import com.baomidou.lock.LockTemplate;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.FtpUtils;
import com.sunyard.framework.common.util.date.DateUtils;
import com.sunyard.module.storage.config.factory.FileStorage;
import com.sunyard.module.storage.config.factory.FileStorageClientFactory;
import com.sunyard.module.storage.config.properties.StorageUploadProperties;
import com.sunyard.module.storage.dto.FilePartInfoDTO;
import com.sunyard.module.storage.dto.StFileDTO;
import com.sunyard.module.storage.dto.UploadDTO;
import com.sunyard.module.storage.po.StEquipment;
import com.sunyard.module.storage.po.StFile;
import com.sunyard.module.storage.vo.UploadSplitVO;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.Part;

/**
 * @author zyl
 * @Description
 * @since 2024/4/29 14:53
 */
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class SftpFileStorage implements FileStorage {
    private String platformType;
    private String bucketName;
    private String domain;
    private String basePath;
    private FileStorageClientFactory<ChannelSftp> clientFactory;
    private StorageUploadProperties storageUploadProperties;

    public SftpFileStorage(StEquipment config, FileStorageClientFactory<ChannelSftp> clientFactory, ApplicationContext applicationContext){
        bucketName = config.getBucket();
        domain = config.getDomainName();
        basePath = config.getBasePath();
        platformType = String.valueOf(config.getStorageType());
        storageUploadProperties = applicationContext.getBean(StorageUploadProperties.class);
        this.clientFactory = clientFactory;
    }

    public ChannelSftp getClient() {
        return clientFactory.getClient();
    }

    @Override
    public String getPlatform() {
        return null;
    }


    @Override
    public String getBucketName() {
        return null;
    }

    @Override
    public StFileDTO initTask(StFileDTO stFile) {
        String uploadId = IdUtil.randomUUID();
        stFile.setUploadId(uploadId);
        stFile.setBucketName(basePath);
        return stFile;
    }

    @Override
    public List<Part> getTaskInfo(StFileDTO stFile) {
        ChannelSftp channelSftp = getClient();
        //如果是nas存储
        //判断文件是否上传成功
        SftpATTRS lstat = null;
        List<Part> exitPartList = new ArrayList<>();
        try {
            lstat = channelSftp.lstat(stFile.getFilePath());

            if (lstat == null) {
                //此步骤只徐然返回的list的size对应已上传到数量就行，用于前端来判断续传前已经传了多少个子文件了
                //1、根据index来合并指定temporaryFolder里面的分片文件
                File file = new File(basePath + storageUploadProperties.getTemporaryFolder() + File.separator
                        + stFile.getUploadId());
                //获取分片文件集
                extracted(exitPartList, file.listFiles());
            }
        } catch (SftpException e) {
            log.error("没有文件,{}", e);
        } finally {
            channelSftp.disconnect();
        }
        return exitPartList;
    }

    @Override
    public String genPreSignUploadUrl(StFileDTO stFile) {
        return null;
    }

    @Override
    public FilePartInfoDTO uploadSplit(UploadSplitVO uploadSplitVO) {
        ChannelSftp channelSftp = getClient();
        try {
            String url = basePath + File.separator + storageUploadProperties.getTemporaryFolder() + File.separator
                    + uploadSplitVO.getUploadId() + File.separator;
            String partFileName = uploadSplitVO.getPartNumber() + "-" + uploadSplitVO.getFileName();
            try {
                channelSftp.lstat(url + partFileName);
            } catch (SftpException e) {
                FtpUtils.mkdirDir(channelSftp, url.split(File.separator), "", url.split(File.separator).length, 0);
            }
            FtpUtils.upload(channelSftp, url, uploadSplitVO.getInputStream(), partFileName);
            channelSftp.disconnect();
            return new FilePartInfoDTO()
                    .setPartSize(uploadSplitVO.getPartSize())
                    .setPartNumber(uploadSplitVO.getPartNumber())
                    .setCreateTime(new Date());
        } catch (Exception e) {
            log.error("异常信息",e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void merge(StFileDTO stFile, LockTemplate lockTemplate) {
        ChannelSftp channelSftp = getClient();
        String url = basePath + File.separator + storageUploadProperties.getTemporaryFolder() + File.separator
                + stFile.getUploadId() + File.separator;
        String lastFile = basePath + File.separator + DateUtils.getDate();
        try {
            //每隔1S重试一次，10次后url中还没有文件则返回失败
            boolean isExist = true;
            int count = 10;
            while (isExist && count > 0) {
                try {
                    --count;
                    Thread.sleep(1000);
                    channelSftp.stat(url);
                    isExist = false;
                } catch (Exception e) {
                    log.error("异常描述",e);
                    throw new RuntimeException(e);
                }
            }
            if (!isExist) {
                Vector ls = channelSftp.ls(url);
                Iterator iterator = ls.iterator();
                Map<String, InputStream> splitFileMap = new HashMap<>(6);
                while (iterator.hasNext()) {
                    ChannelSftp.LsEntry file = (ChannelSftp.LsEntry) iterator.next();
                    //文件名称
                    String fileName = file.getFilename();
                    if (fileName.contains("-")) {
                        splitFileMap.put(fileName, channelSftp.get(url + fileName));
                    }
                }
                //对分片文件按照1、2、3...排序
                Vector<InputStream> vector = sortSplitFile(splitFileMap);
                for (InputStream inputStream : vector) {
                    //返回文件流(因ChannelSftp获取的InputStream无法知道size，需进行in->out->in操作)
                    ByteArrayOutputStream data = new ByteArrayOutputStream();
                    byte[] one = new byte[1024];
                    int len = -1;
                    while ((len = inputStream.read(one)) != -1) {
                        data.write(one, 0, len);
                    }
                    FtpUtils.shardUpload(channelSftp,lastFile,new ByteArrayInputStream(data.toByteArray()),stFile.getFilename()+ "." +stFile.getExt());
                    // 关闭输入流
                    inputStream.close();
                    data.close();
                }
                log.info("合并完成!");
                //删除分片
                delSplit(channelSftp, url);
            }
        } catch (Exception e) {
            log.error("异常描述",e);
            throw new RuntimeException(e);
        } finally {
            channelSftp.disconnect();
        }
    }

    @Override
    public StFileDTO upload(UploadDTO uploadDTO) {
        StFileDTO stFile = new StFileDTO().setBucketName(bucketName)
                .setFilePath(basePath + "/" + uploadDTO.getKey())
                .setUrl(StrUtil.format("{}/{}/{}", domain, bucketName, uploadDTO.getKey()));
        ChannelSftp channelSftp = getClient();
        ThreadLocal<List<Part>> partTags = new ThreadLocal<List<Part>>();
        partTags.set(new ArrayList<>());
        ThreadLocal<Long> filePosition = new ThreadLocal<>();
        filePosition.set(0L);
        InputStream inputStream = uploadDTO.getInputStream();
        long contentLength = uploadDTO.getFileSize();
        long partSize = uploadDTO.getChunkSize();
        int partNumber = 1;
        String eTagMarker = null;
        // 检查是否存在之前上传的分片记录
        List<Part> existingPartTags = getExistingPartTagsByFtp(uploadDTO.getKey(), uploadDTO.getMd5(), channelSftp);
        if (!existingPartTags.isEmpty()) {
            partTags.set(new ArrayList<>(existingPartTags));
            partNumber = existingPartTags.size() + 1;
            // 设置已上传的分片标识
            filePosition.set(partSize * existingPartTags.size());
        }
        for (; filePosition.get() < contentLength; partNumber++) {
            // 检查当前分片是否已上传
            if (isPartUploadedByFyp(partTags.get(), partNumber)) {
                continue; // 跳过已上传的分片
            }
            // 计算当前分片大小
            long remainingSize = contentLength - filePosition.get();
            partSize = Math.min(partSize, remainingSize);
            // 上传分片
            uploadSplitByFtp(inputStream, uploadDTO.getMd5(),uploadDTO.getFileName(), partNumber, channelSftp, partSize, uploadDTO.getChunkSize());

            Part partSummary = Part.builder()
                    .size(partSize)
                    .partNumber(partNumber)
                    .build();

            partTags.get().add(partSummary);
            filePosition.set(filePosition.get() + partSize);
        }
        // 清理ThreadLocal中的数据
        partTags.remove();
        filePosition.remove();
        // 检查所有分片是否已上传完成
        if (isMultipartUploadCompleteByFtp(uploadDTO.getKey(), channelSftp)) {
            // 合并分片
            merge(uploadDTO.getMd5(), uploadDTO.getFileName(), channelSftp);
            log.info("完成合并分片");
        }
        return stFile;
    }

    @Override
    public InputStream getFileStream(String key) {
        ChannelSftp channelSftp = getClient();
        InputStream inputStream;
        try {
            inputStream = channelSftp.get(basePath + "/" + key);
        } catch (SftpException e) {
            log.error("获取文件资源失败",e);
            throw new RuntimeException(e);
        }
        return inputStream;
    }

    @Override
    public Long getFileSize(String key) {
        ChannelSftp channelSftp = getClient();
        SftpATTRS attrs;
        long fileSize;
        try {
            attrs = channelSftp.stat(basePath + "/" + key);
            fileSize = attrs.getSize();
        } catch (SftpException e) {
            log.error("异常描述",e);
            throw new RuntimeException(e);
        }
        return fileSize;
    }

    @Override
    public StFileDTO replaceFile(UploadDTO uploadDTO) {
        ChannelSftp channelSftp = getClient();
        try {
            FtpUtils.upload(channelSftp, File.separator + FilenameUtils.getPath(uploadDTO.getFilePath()), uploadDTO.getInputStream(), uploadDTO.getFileName());
            log.info("文件替换成功！");
        } catch (Exception e) {
            log.error("异常描述",e);
            throw new RuntimeException(e);
        } finally {
            channelSftp.disconnect();
        }
        return null;
    }

    @Override
    public String getPath(String key) {
        return  StrUtil.format("{}/{}/{}", domain, bucketName, key);
    }

    @Override
    public boolean delete(String url) {
        ChannelSftp channelSftp = getClient();
        try {
            channelSftp.rm(basePath + "/" + url);
        } catch (SftpException e) {
            log.error("异常描述",e);
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public void download(StFile stFile, Consumer<InputStream> consumer) {

    }

    @Override
    public void cancelFileUpload(String bucketName, String objectKey, String uploadId) {

    }

    /**
     * 上传分片文件通过ftp
     * @param inputStream 输入流
     * @param md5 MD5
     * @param fileName 文件名
     * @param partNumber 分片数
     * @param channelSftp ftp连接
     * @param partSize 分片大小
     * @param chunkSize 桶大小
     * @return Result
     */
    private Result uploadSplitByFtp(InputStream inputStream, String md5, String fileName ,int partNumber, ChannelSftp channelSftp, long partSize, Long chunkSize) {
        String url = basePath + File.separator + storageUploadProperties.getTemporaryFolder() + File.separator
                + md5 + File.separator;
        String partFileName = partNumber + "-" +fileName.substring(0, fileName.lastIndexOf(".")) + "." + fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        try {
            try {
                channelSftp.lstat(url + partFileName);
                return Result.success("");
            } catch (SftpException e) {
                String[] urlParts = url.split(File.separatorChar == '\\' ? "\\\\" : "/");
                FtpUtils.mkdirDir(channelSftp, urlParts, "", urlParts.length, 0);
            }
            InputStream inputStream1 = splitFile(inputStream, partSize, partNumber - 1, chunkSize);
            FtpUtils.upload(channelSftp, url, inputStream1, partFileName);
            channelSftp.disconnect();
            return Result.success("");
        } catch (Exception e) {
            log.error("异常描述",e);
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param key key
     * @param md5 md5
     * @param channelSftp ftp对象
     * @return Result
     */
    private List<Part> getExistingPartTagsByFtp(String key,String md5, ChannelSftp channelSftp) {
        SftpATTRS lstat = null;
        List<Part> exitPartList = new ArrayList<>();
        try {
            lstat = channelSftp.lstat(basePath + "/" + key);

            if (lstat == null) {
                //此步骤只徐然返回的list的size对应已上传到数量就行，用于前端来判断续传前已经传了多少个子文件了
                //1、根据index来合并指定temporaryFolder里面的分片文件
                File file = new File(basePath + storageUploadProperties.getTemporaryFolder() + File.separator
                        + md5);
                //获取分片文件集
                extracted(exitPartList, file.listFiles());
            }
        } catch (Exception e) {
            log.error("异常描述",e);
            throw new RuntimeException(e);
        } finally {
            channelSftp.disconnect();
        }
        return exitPartList;
    }

    /**
     * 对分片名重命名
     * @param exitPartList 分片list
     * @param files 文件数组
     */
    private void extracted(List<Part> exitPartList, File[] files) {
        if (null != files && files.length > 0) {
            for (File item : files) {
                Part partSummary = Part.builder()
                        .size(item.length())
                        .partNumber(Integer.parseInt(item.getName().split("-")[0]))
                        .build();
                exitPartList.add(partSummary);
            }
        }
    }

    /**
     * 判断分片上传是否成功
     * @param partTags 分片标识
     * @param partNumber 分片数量
     * @return boolean
     */
    private boolean isPartUploadedByFyp(List<Part> partTags, int partNumber) {
        if (!CollectionUtils.isEmpty(partTags)) {
            for (Part partSummary : partTags) {
                if (partSummary.partNumber() == partNumber) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查分片上传是否完成
     * @param key key
     * @param channelSftp ftp连接
     * @return Result
     */
    private boolean isMultipartUploadCompleteByFtp(String key, ChannelSftp channelSftp) {
        SftpATTRS lstat = null;
//        List<PartSummary> exitPartList = new ArrayList<>();
        try {
            lstat = channelSftp.lstat(basePath + "/" +key);
            if (lstat == null) {
                return false;
            }
        } catch (SftpException e) {
            log.error("没有文件", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("ftp连接失败", e);
            throw new RuntimeException(e);
        } finally {
            channelSftp.disconnect();
        }
        return true;
    }

    /**
     * 合并文件
     * @param sourceFileMd5 源文件md5
     * @param fileNames 文件名
     * @param channelSftp ftp连接
     * @return Result
     */
    private Result merge(String sourceFileMd5, String fileNames, ChannelSftp channelSftp) {
        String url = basePath + File.separator + storageUploadProperties.getTemporaryFolder() + File.separator
                + sourceFileMd5 + File.separator;
        String lastFile = basePath + File.separator + DateUtils.getDate();
        try {
            //每隔1S重试一次，10次后url中还没有文件则返回失败
            boolean isExist = true;
            int count = 10;
            while (isExist && count > 0) {
                try {
                    --count;
                    Thread.sleep(1000);
                    channelSftp.stat(url);
                    isExist = false;
                } catch (Exception e) {
                    log.error("异常描述",e);
                    throw new RuntimeException(e);
                }
            }
            if (isExist) {
                return Result.error("文件合并失败：" + url, ResultCode.SYSTEM_BUSY_ERROR);
            }
            Vector ls = channelSftp.ls(url);
            Iterator iterator = ls.iterator();
            Map<String, InputStream> splitFileMap = new HashMap<>(6);
            while (iterator.hasNext()) {
                ChannelSftp.LsEntry file = (ChannelSftp.LsEntry) iterator.next();
                //文件名称
                String fileName = file.getFilename();
                if (fileName.contains("-")) {
                    splitFileMap.put(fileName, channelSftp.get(url + fileName));
                }
            }
            //对分片文件按照1、2、3...排序
            Vector<InputStream> vector = sortSplitFile(splitFileMap);
            SequenceInputStream sequenceInputStream = new SequenceInputStream(vector.elements());
            FtpUtils.upload(channelSftp, lastFile, sequenceInputStream, fileNames);
            log.info("合并完成!");
            //删除分片
            delSplit(channelSftp, url);
        } catch (Exception e) {
            log.error("异常描述",e);
            throw new RuntimeException(e);
        } finally {
            channelSftp.disconnect();
        }
        return Result.success("");
    }

    /**
     * 对分片文件按照1、2、3...排序，属顺序不对拼接出的文件会损坏
     *
     * @param map 分片文件map
     * @return Result
     */
    private Vector<InputStream> sortSplitFile(Map<String, InputStream> map) {
        // 将 Map 转换为 List
        List<Map.Entry<String, InputStream>> list = new ArrayList<>(map.entrySet());

        // 对 List 进行排序，排序规则是按照键的字典顺序排序
        Collections.sort(list, new Comparator<Entry<String, InputStream>>() {
            @Override
            public int compare(Map.Entry<String, InputStream> o1, Map.Entry<String, InputStream> o2) {
                int num1 = Integer.parseInt(o1.getKey().split("-")[0]);
                int num2 = Integer.parseInt(o2.getKey().split("-")[0]);
                return Integer.compare(num1, num2);
            }
        });

        // 创建一个新的 Vector<InputStream>，并按照排序后的顺序填充
        Vector<InputStream> sortedStreams = new Vector<>();
        for (Map.Entry<String, InputStream> entry : list) {
            sortedStreams.add(entry.getValue());
        }

        return sortedStreams;
    }

    /**
     * 删除分片
     *
     * @param channelSftp ftp连接
     * @param url url
     */
    private void delSplit(ChannelSftp channelSftp, String url) {
        try {
            // 列出文件夹中的所有文件
            Vector ls = channelSftp.ls(url);
            Iterator iterator = ls.iterator();

            while (iterator.hasNext()) {
                ChannelSftp.LsEntry file = (ChannelSftp.LsEntry) iterator.next();
                // 获取文件名称
                String fileName = file.getFilename();

                // 检查是否是文件而不是子目录
                if (!file.getAttrs().isDir()) {
                    // 使用rm方法删除文件
                    channelSftp.rm(url + fileName);
                    log.info("删除文件: {}", fileName);
                }
            }
            channelSftp.rmdir(url);
            log.info("文件夹及其所有文件已删除: {}", url);
        } catch (Exception e) {
            log.error("删除分片文件失败: {}", url,e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 拆分文件
     *
     * @param inputStream       文件
     * @param partSize   默认的分片大小
     * @param partNumber 上一个分片的partNumber
     * @param chunkSize 组块大小
     * @return Result
     */
    private InputStream splitFile(InputStream inputStream, long partSize, int partNumber, Long chunkSize) {
        // 创建输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
             BufferedOutputStream bos = new BufferedOutputStream(outputStream)) {
            // 计算当前部分的起始和结束位置
            long start = partNumber * partSize;
            long end = (partNumber + 1) * partSize;
            // 定位到起始位置
            bis.skip(start);

            // 每次读取一定大小的数据进行写入
            byte[] buffer = new byte[(int) Math.min(chunkSize, partSize)];
            int bytesRead;
            long bytesWritten = 0;
            while ((bytesRead = bis.read(buffer)) != -1 && bytesWritten < partSize) {
                bos.write(buffer, 0, bytesRead);
                bytesWritten += bytesRead;
            }
            //确保缓冲区数据被立即刷新到输出流
            bos.flush();
        } catch (IOException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
        // 将输出流转换为输入流
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

}
