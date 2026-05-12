package com.sunyard.module.storage.config.factory.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.shiro.codec.Hex;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;

import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.date.DateUtils;
import com.sunyard.framework.common.util.encryption.AesUtils;
import com.sunyard.framework.common.util.encryption.Sm2Util;
import com.sunyard.module.storage.config.factory.FileStorage;
import com.sunyard.module.storage.config.properties.StorageUploadProperties;
import com.sunyard.module.storage.constant.FileConstants;
import com.sunyard.module.storage.dto.FilePartInfoDTO;
import com.sunyard.module.storage.dto.StFileDTO;
import com.sunyard.module.storage.dto.UploadDTO;
import com.sunyard.module.storage.manager.StFileService;
import com.sunyard.module.storage.po.StEquipment;
import com.sunyard.module.storage.po.StFile;
import com.sunyard.module.storage.service.EncryptService;
import com.sunyard.module.storage.vo.UploadSplitVO;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.dto.SysParamDTO;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.Part;

/**
 * @author zyl
 * @Description
 * @since 2024/3/18 15:28
 */
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class LocalFileStorage implements FileStorage {
    private static final Pattern PART_NUMBER_PATTERN = Pattern.compile("(\\d+)");
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 100;
    /**
     * 预签名url过期时间(ms)
     */
    public static final Long PRE_SIGN_URL_EXPIRE = 60 * 10 * 1000L;
    private final Pattern pattern = Pattern.compile("^\\d+");

    private String basePath;
    private String platform;
    private String platformType;
    private String domain;
    private String bucketName;

    private StorageUploadProperties storageUploadProperties;
    private StFileService stFileService;
    private EncryptService encryptService;
    private ParamApi paramApi;

    public LocalFileStorage(StEquipment config, ApplicationContext applicationContext) {
        platform = String.valueOf(config.getId());
        basePath = config.getBasePath();
        domain = config.getDomainName();
        platformType = String.valueOf(config.getStorageType());
        bucketName = config.getBasePath();
        stFileService = applicationContext.getBean(StFileService.class);
        encryptService = applicationContext.getBean(EncryptService.class);
        storageUploadProperties = applicationContext.getBean(StorageUploadProperties.class);
    }

    @Override
    public String getPlatform() {
        return platform;
    }

    @Override
    public StFileDTO initTask(StFileDTO stFile) {
        String uploadId = IdUtil.randomUUID();
        stFile.setUploadId(uploadId);
        stFile.setBucketName(basePath);
        return stFile;
    }

    @Override
    public String getPath(String key) {
        return StrUtil.format("{}/{}", basePath, key);
    }

    @Override
    public List<Part> getTaskInfo(StFileDTO stFile) {
        //此步骤只徐然返回的list的size对应已上传到数量就行，用于前端来判断续传前已经传了多少个子文件了
        List<Part> exitPartList = new ArrayList<>();
        //1、根据index来合并指定temporaryFolder里面的分片文件
        //        File file = new File(stFile.getBasePath() + "/temporaryFolder" + File.separator
        //                + stFile.getBusiBatchNo() + File.separator
        //                + stFile.getFileMd5());
        File file = new File(storageUploadProperties.getTemporaryFolder() + File.separator + stFile.getFileMd5());
        File[] files = file.listFiles();
        if (null != files && files.length > 0) {
            for (File item : files) {
                Part partSummary = Part.builder().size(item.length())
                        .partNumber(Integer.parseInt(item.getName().split("-")[0])).build();
                exitPartList.add(partSummary);
            }
        }

        return exitPartList;
    }

    @Override
    public String genPreSignUploadUrl(StFileDTO stFile) {
        StringBuffer nasUrl = new StringBuffer(stFile.getUrl());
        nasUrl.append("&identifier=" + stFile.getFileMd5());
        nasUrl.append("&fileName=" + stFile.getFilename() + "." + stFile.getExt());
        nasUrl.append("&equipmentId=" + stFile.getEquipmentId());
        //        nasUrl.append("&busiBatchNo=" + stFile.getBusiBatchNo());
        nasUrl.append("&id=" + stFile.getId());
        return nasUrl.toString();
    }

    @Override
    public FilePartInfoDTO uploadSplit(UploadSplitVO uploadSplitVO) {
        InputStream inputStream = uploadSplitVO.getInputStream();
        try {
            String url = basePath + storageUploadProperties.getTemporaryFolder() + File.separator + uploadSplitVO.getFileId()
                    + File.separator;
            String partFileName = uploadSplitVO.getPartNumber() + "-" + uploadSplitVO.getFileName();
            Path path = Paths.get(url);
            if (Files.exists(path) && Files.isDirectory(path)) {
            } else {
                // 如果目录不存在，则创建新目录
                try {
                    Files.createDirectories(path);
                    log.info("目录已创建" + url);
                } catch (IOException e) {
                    log.error("无法创建目录", e);
                    throw new RuntimeException(e);
                }
            }
            File dir = FileUtil.file(url);
            File part = FileUtil.file(dir, partFileName);
            long partSize = uploadSplitVO.getInputStream().available();
            FileUtil.writeFromStream(inputStream, part);
            return new FilePartInfoDTO().setPartSize(partSize)
                    .setPartNumber(uploadSplitVO.getPartNumber()).setCreateTime(new Date());
        } catch (Exception e) {
            log.error("上传分片失败", e);
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    @Override
    public void merge(StFileDTO stFile, LockTemplate lockTemplate) {
        mergeFileChunks(stFile, lockTemplate);
    }

    public void mergeFileChunks(StFileDTO stFile, LockTemplate lockTemplate) {
        String fileId = stFile.getId().toString();
        LockInfo lock = lockTemplate.lock("UPLOAD_COMPLETE" + fileId, FileConstants.MERRGE_EXPIRE,
                FileConstants.MERRGE_ACQUIRETIMEOUT);
        if (lock == null) {
            log.warn("文件合并操作超时，文件可能正在被其他进程处理: {}", fileId);
            throw new RuntimeException("文件正在处理中，请稍后重试");
        }
        try {
            mergeWithRetry(stFile, MAX_RETRY_ATTEMPTS);
        } finally {
            lockTemplate.releaseLock(lock);
        }

    }

    private void mergeWithRetry(StFileDTO stFile, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                doMerge(stFile);
                log.info("文件合并成功: {}", stFile.getId());
                return;
            } catch (Exception e) {
                if (attempt == maxRetries) {
                    log.error("文件合并失败，已达最大重试次数: {}", stFile.getId(), e);
                    throw new RuntimeException("文件合并失败",e);
                }

                log.warn("文件合并失败，第{}次重试: {}", attempt, stFile.getId(), e);
                try {
                    Thread.sleep(RETRY_DELAY_MS * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("操作被中断", ie);
                }
            }
        }
    }

    private void doMerge(StFileDTO stFile) throws IOException {
        // 使用NIO Path构建路径，确保跨平台兼容性
        Path tempDir = Paths.get(stFile.getBucketName(), storageUploadProperties.getTemporaryFolder(),
                stFile.getId().toString());
        Path targetDir = Paths.get(stFile.getBucketName(), stFile.getObjectKey()).getParent();
        Path targetFile = Paths.get(stFile.getBucketName(), stFile.getObjectKey());

        log.debug("开始合并文件，分片目录: {}, 目标文件: {}", tempDir, targetFile);

        // 验证分片目录
        validateTempDirectory(tempDir);

        // 获取并验证分片文件
        List<Path> partFiles = getValidatedPartFiles(tempDir);
        if (partFiles.isEmpty()) {
            throw new IOException("未找到有效的分片文件: " + tempDir);
        }

        log.info("找到 {} 个分片文件，开始合并: {}", partFiles.size(), stFile.getId());

        // 创建目标目录
        createTargetDirectory(targetDir);

        // 执行文件合并（使用文件锁确保原子性）
        mergeFilesWithLock(partFiles, targetFile);

        // 异步清理临时文件（避免阻塞当前操作）
        // 文件合并完成后立即返回，清理操作异步执行但不阻塞
        CompletableFuture.runAsync(() -> {
            cleanupTempFilesAsync(tempDir);
        }).exceptionally(e -> {
            log.warn("异步清理失败: {}", tempDir, e);
            return null;
        });

        log.info("文件合并完成: {}", targetFile);
    }

    private void validateTempDirectory(Path tempDir) throws IOException {
        if (!Files.exists(tempDir)) {
            throw new IOException("分片目录不存在: " + tempDir);
        }
        if (!Files.isDirectory(tempDir)) {
            throw new IOException("分片路径不是目录: " + tempDir);
        }
        if (!Files.isReadable(tempDir)) {
            throw new IOException("分片目录不可读: " + tempDir);
        }
    }

    private List<Path> getValidatedPartFiles(Path tempDir) throws IOException {
        try (Stream<Path> stream = Files.list(tempDir)) {
            return stream.filter(this::isValidPartFile).sorted(this::comparePartFiles)
                    .collect(Collectors.toList());
        }
    }

    private boolean isValidPartFile(Path filePath) {
        try {
            return Files.exists(filePath) && Files.isRegularFile(filePath)
                    && Files.isReadable(filePath) && Files.size(filePath) > 0
                    && !isFileLocked(filePath);
        } catch (IOException e) {
            log.warn("检查分片文件失败: {}", filePath, e);
            return false;
        }
    }

    private boolean isFileLocked(Path filePath) {
        try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            FileLock lock = channel.tryLock(0, Long.MAX_VALUE, true);
            if (lock != null) {
                lock.release();
                return false; // 文件未被锁定
            }
            return true; // 文件被锁定
        } catch (IOException e) {
            return true; // 无法访问视为被锁定
        }
    }

    private int comparePartFiles(Path path1, Path path2) {
        try {
            String fileName1 = path1.getFileName().toString();
            String fileName2 = path2.getFileName().toString();

            int number1 = extractPartNumber(fileName1);
            int number2 = extractPartNumber(fileName2);

            return Integer.compare(number1, number2);
        } catch (Exception e) {
            // 如果提取数字失败，使用文件名自然排序
            return path1.getFileName().toString().compareTo(path2.getFileName().toString());
        }
    }

    private int extractPartNumber(String fileName) {
        Matcher matcher = PART_NUMBER_PATTERN.matcher(fileName);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("分片文件名数字格式错误: {}", fileName);
                throw new IllegalArgumentException("分片文件名数字格式错误: " + fileName);
            }
        }
        throw new IllegalArgumentException("分片文件名不包含数字: " + fileName);
    }

    private void createTargetDirectory(Path targetDir) throws IOException {
        if (!Files.exists(targetDir)) {
            try {
                Files.createDirectories(targetDir);
                log.debug("创建目标目录: {}", targetDir);
            } catch (IOException e) {
                throw new IOException("创建目标目录失败: " + targetDir, e);
            }
        }
    }

    private void mergeFilesWithLock(List<Path> partFiles, Path targetFile) throws IOException {
        // 使用文件锁确保合并操作的原子性
        try (FileChannel targetChannel = FileChannel.open(targetFile, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE, StandardOpenOption.APPEND);
                FileLock lock = targetChannel.lock()) {

            for (Path partFile : partFiles) {
                mergeSingleFile(partFile, targetChannel);
            }

            // 强制刷盘，确保数据持久化
            targetChannel.force(true);
        }
    }

    private void mergeSingleFile(Path partFile, FileChannel targetChannel) throws IOException {
        // 再次验证文件状态（防止竞态条件）
        if (!Files.exists(partFile) || !Files.isReadable(partFile)) {
            throw new IOException("分片文件已不存在或不可读: " + partFile);
        }

        log.debug("合并分片文件: {}", partFile.getFileName());

        try (FileChannel sourceChannel = FileChannel.open(partFile, StandardOpenOption.READ)) {
            long fileSize = Files.size(partFile);
            long transferred = 0;

            // 使用transferTo进行高效的文件复制（零拷贝）
            while (transferred < fileSize) {
                transferred += sourceChannel.transferTo(transferred, fileSize - transferred,
                        targetChannel);
            }
        }
    }

    @Async("GlobalThreadPool")
    public void cleanupTempFilesAsync(Path tempDir) {
        try {
            // 延迟清理，避免影响可能还在进行的其他操作
            Thread.sleep(TimeUnit.SECONDS.toMillis(30));

            if (Files.exists(tempDir)) {
                log.info("开始清理临时文件: {}", tempDir);

                // 递归删除临时目录
                deleteDirectoryRecursively(tempDir);

                // 尝试删除父级空目录
                cleanupParentDirectories(tempDir);

                log.info("临时文件清理完成: {}", tempDir);
            }
        } catch (Exception e) {
            log.warn("清理临时文件失败: {}", tempDir, e);
        }
    }

    private void deleteDirectoryRecursively(Path directory) throws IOException {
        if (Files.exists(directory) && Files.isDirectory(directory)) {
            try (Stream<Path> walk = Files.walk(directory)) {
                walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        log.warn("删除文件失败: {}", path, e);
                    }
                });
            }
        }
    }

    private void cleanupParentDirectories(Path tempDir) {
        try {
            Path parentDir = tempDir.getParent();
            if (parentDir != null && Files.exists(parentDir)) {
                try (Stream<Path> stream = Files.list(parentDir)) {
                    boolean isEmpty = !stream.findAny().isPresent();
                    if (isEmpty) {
                        Files.deleteIfExists(parentDir);
                        log.debug("删除空目录: {}", parentDir);
                    }
                }
            }
        } catch (IOException e) {
            log.error("清理父目录失败", e);
        }
    }

    @Override
    public StFileDTO upload(UploadDTO uploadDTO) {
        String url = basePath + File.separator + uploadDTO.getKey();
        FileUtil.writeFromStream(uploadDTO.getInputStream(), url);
        StFileDTO stFile = new StFileDTO().setBucketName(basePath)
                .setFilePath(uploadDTO.getFilePath()).setUrl(url);
        return stFile;
    }

    @Override
    public InputStream getFileStream(String key) {
        File file = new File(basePath + "/" + key);
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                log.error("获取文件流失败", e);
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public Long getFileSize(String key) {
        File file = new File(basePath + "/" + key);
        if (file.exists()) {
            return file.length();
        }
        return null;
    }

    @Override
    public StFileDTO replaceFile(UploadDTO uploadDTO) {
        try {
            // 删除原文件
            boolean deleteSuccess = FileUtil
                    .del(basePath + File.separator + uploadDTO.getFilePath());
            if (!deleteSuccess) {
                // 如果删除失败，可以根据需要进行相应的处理
                log.error("删除原文件失败：" + uploadDTO.getFilePath());
            }
            // 重新写入新文件
            FileUtil.writeFromStream(uploadDTO.getInputStream(),
                    basePath + File.separator + uploadDTO.getFilePath());
            log.info("文件替换成功");
        } catch (Exception e) {
            log.error("文件替换失败", e);
            throw new RuntimeException(e);
        }
        StFileDTO stFile = new StFileDTO();
        return stFile;
    }

    @Override
    public boolean delete(String url) {
        try {
            return FileUtil.del(basePath + "/" + url);
        } catch (Exception e) {
            log.error("删除文件失败", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void download(StFile stFile, Consumer<InputStream> consumer) {

    }

    @Override
    public void cancelFileUpload(String bucketName, String objectKey, String uploadId) {

    }

    /**
     * 判断时候进行秒传
     *
     * @return Result
     */
    private Boolean isSecondsUpload() {
        Result<SysParamDTO> paramDtoResult = paramApi
                .searchValueByKey("FILE_UPLOAD_SECOND_PASS_SWITCH");
        if (ObjectUtil.isNotEmpty(paramDtoResult.getData())
                && "1".equals(paramDtoResult.getData().getValue())) {
            return true;
        }

        return false;
    }

    /**
     * 加密文件
     *
     * @param partFile     分片文件
     * @param encryptIndex 加密标识
     * @param encryptKey   加密key
     */
    private void encryptFile(File partFile, String encryptIndex, String encryptKey) {
        //临时路径
        String s = partFile.getParent() + "/" + UUID.fastUUID() + partFile.getName();
        // 清除文件内容
        try (InputStream fileInputStream = new FileInputStream(partFile);
                FileOutputStream fileOutputStream = new FileOutputStream(s);
                BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream)) {
            //截取配置加密范围进行加密
            byte[] part = new byte[storageUploadProperties.getPartEncryptSizeForLocal() * 1024];
            fileInputStream.read(part);
            byte[] encryptByte = new byte[0];
            //根据标识判断是否是SM2、还是AES
            if (encryptIndex.equals(storageUploadProperties.getEncryptIndex())) {
                String encrypt = AesUtils.encrypt(part, encryptKey);
                encryptByte = encrypt.getBytes(StandardCharsets.UTF_8);
            } else if (encryptIndex.equals(storageUploadProperties.getSm2EncryptIndex())) {
                String encrypt = Sm2Util.encrypt(part);
                encryptByte = Hex.decode(encrypt);
            }
            //写入加密数据
            fileOutputStream.write(encryptByte);
            //写入标识符
            fileOutputStream.write(encryptIndex.getBytes(StandardCharsets.UTF_8));
            //写入未加密的部分
            byte[] buffer = new byte[1024 * 4];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            //确保缓冲区数据被立即刷新到文件
            bos.flush();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("加密失败", e);
                throw new RuntimeException(e);
            }
        } finally {
            // 文件1和文件2的路径
            Path source = Paths.get(s);
            Path destination = Paths.get(partFile.getPath());
            try {
                //将加密后的文件复制到源文件中
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error("加密文件失败", e);
                throw new RuntimeException(e);
            }
            //删除临时文件
            FileUtil.del(s);
        }
    }

    /**
     * 删除空文件夹
     *
     * @param directoryToBeDeleted 文件对象
     * @return boolean
     */
    private static boolean deleteDirectory(File directoryToBeDeleted) {
        // 如果目录不存在，则直接返回 true
        if (!directoryToBeDeleted.exists()) {
            return true;
        }

        // 如果是文件而不是目录，则直接删除
        if (directoryToBeDeleted.isFile()) {
            return directoryToBeDeleted.delete();
        }

        // 递归删除目录中的内容
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }

        // 删除空目录
        return directoryToBeDeleted.delete();
    }

}
