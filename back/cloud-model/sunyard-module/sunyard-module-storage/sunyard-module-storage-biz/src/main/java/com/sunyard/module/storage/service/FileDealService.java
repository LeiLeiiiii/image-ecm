package com.sunyard.module.storage.service;

import static com.sunyard.module.storage.util.ImgUpdateUtils.handleImgByInputStream;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spire.pdf.graphics.PdfImageType;
import com.sunyard.framework.common.util.http.HttpUtils;
import com.sunyard.module.storage.api.FileHandleApi;
import com.sunyard.module.storage.constant.CachePrefixConstants;
import com.sunyard.module.storage.dto.ecm.DocFileZip;
import com.sunyard.module.storage.dto.ecm.DownloadFileZip;
import com.sunyard.module.storage.ecm.dto.EcmFileBelong;
import com.sunyard.module.storage.vo.FileByteVO;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spire.pdf.PdfDocument;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.FileUtils;
import com.sunyard.framework.common.util.IoUtils;
import com.sunyard.framework.common.util.ZipUtils;
import com.sunyard.framework.common.util.encryption.Base64Utils;
import com.sunyard.framework.common.util.encryption.Md5Utils;
import com.sunyard.framework.img.util.ImageToPngUtils;
import com.sunyard.framework.img.util.ImgPythonCheckUtils;
import com.sunyard.framework.img.util.RectifyImageUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.framework.onlyoffice.tools.OnlyOfficeUtil;
import com.sunyard.framework.redis.constant.TimeOutConstants;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.framework.spire.constant.OnlineConstants;
import com.sunyard.framework.spire.util.ConvertPdfUtils;
import com.sunyard.framework.spire.util.ImageWaterUtils;
import com.sunyard.framework.spire.util.FileDecryptUtil;
import com.sunyard.framework.spire.util.SplitPdfUtils;
import com.sunyard.framework.spire.util.WatermarkUtils;
import com.sunyard.module.storage.config.factory.FileStorage;
import com.sunyard.module.storage.config.factory.impl.FileStorageService;
import com.sunyard.module.storage.config.properties.StorageOnlyOfficeProperties;
import com.sunyard.module.storage.config.properties.StorageUploadProperties;
import com.sunyard.module.storage.constant.FileConstants;
import com.sunyard.module.storage.constant.ImgsConstants;
import com.sunyard.module.storage.constant.StateConstants;
import com.sunyard.module.storage.constant.SunCacheDelConstants;
import com.sunyard.module.storage.dto.EncryptDTO;
import com.sunyard.module.storage.dto.FileEncryptInfoDTO;
import com.sunyard.module.storage.dto.MixedPastingSplitDTO;
import com.sunyard.module.storage.dto.StFileDTO;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.dto.SysFilePictureRotatingDTO;
import com.sunyard.module.storage.dto.UploadDTO;
import com.sunyard.module.storage.manager.AsyncUploadService;
import com.sunyard.module.storage.manager.StFileService;
import com.sunyard.module.storage.mapper.StEquipmentMapper;
import com.sunyard.module.storage.po.StEquipment;
import com.sunyard.module.storage.po.StFile;
import com.sunyard.module.storage.util.FileDealUtils;
import com.sunyard.module.storage.util.FileTypeUtils;
import com.sunyard.module.storage.util.SunCacheUtils;
import com.sunyard.module.storage.util.WordTextExtractor;
import com.sunyard.module.storage.vo.DownFileVO;
import com.sunyard.module.storage.vo.FileMergeVO;
import com.sunyard.module.storage.vo.FileSplitPdfVO;
import com.sunyard.module.storage.vo.PictureRotatingInfoVO;
import com.sunyard.module.storage.vo.PictureRotatingVO;
import com.sunyard.module.storage.vo.WaterMarkConfigVO;
import com.sunyard.module.system.api.DictionaryApi;
import com.sunyard.module.system.api.InstApi;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysDictionaryDTO;
import com.sunyard.module.system.api.dto.SysInstDTO;
import com.sunyard.module.system.api.dto.SysParamDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.filters.Canvas;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * 文件处理实现类
 *
 * @author： zyl @Description：
 * @since 2023/7/6 17:58
 */
@Slf4j
@Service
public class FileDealService {

    public final static List<String> CANSPLITFILETYPE = Arrays.asList("pdf", "ofd", "tiff", "tif",
            "heif", "txt", "ppt", "pptx", "xls", "xlsx", "docx", "doc", "wps");
    public final static List<String> VIDEOS = Arrays.asList("wmv", "asf", "rm", "rmvb", "avi",
            "mov", "mpg", "flv", "mp4");
    public final static List<String> AUDIOS = Arrays.asList("amr", "ogg", "m4a", "mp3", "wav");
    public final static List<String> DOCS = Arrays.asList("txt", "doc", "wps", "docx", "xls", "ppt",
            "pptx", "xlsx", "ini", "pdf");
    @Resource
    private StorageUploadProperties storageUploadProperties;
    @Resource
    private StorageOnlyOfficeProperties storageOnlyOfficeProperties;
    @Resource
    private Executor executor;
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private RedisUtils redisUtil;
    @Resource
    private OnlyOfficeUtil onlyOfficeUtil;
    @Resource
    private StEquipmentMapper stEquipmentMapper;
    @Resource
    private ParamApi paramApi;
    @Resource
    private UserApi userApi;
    @Resource
    private InstApi instApi;
    @Resource
    private DictionaryApi dictionaryApi;
    @Resource
    private AsyncUploadService asyncUploadService;
    @Resource
    private CacheCommonService cacheCommonService;
    @Resource
    private SplitUploadTaskService splitUploadTaskService;
    @Resource
    private FileStorageService fileStorageService;
    @Resource
    private EncryptService encryptService;
    @Resource
    private StFileService stFileService;

    @Resource
    private FileHandleApi fileHandleApi;

    /**
     * 图片处理 旋转、淡化 ，亮化，锐化 去黑边 裁剪
     *
     * @param pictureRotatingVO 图片处理信息
     * @param token             token
     * @return Result
     */
    public List<SysFilePictureRotatingDTO> updateImg(PictureRotatingVO pictureRotatingVO,
                                                     AccountToken token) {
        AssertUtils.isNull(pictureRotatingVO.getRotationAngle(), "参数有误");
        List<Long> fileIdList = pictureRotatingVO.getRotationAngle().stream()
                .map(PictureRotatingInfoVO::getNewFileId).collect(Collectors.toList());
        AssertUtils.isNull(fileIdList, "文件id不能为空");

        // 旧文件
        List<StFileDTO> stFileList = stFileService.selectFileDTOByIds(fileIdList);
        Map<Long, List<StFileDTO>> stSysFileMap = stFileList.stream()
                .collect(Collectors.groupingBy(StFileDTO::getId));
        AssertUtils.isNull(stSysFileMap, "请传正确的文件id");
        List<StFileDTO> stFiles = new ArrayList<>();
        List<PictureRotatingInfoVO> rotationAngle = pictureRotatingVO.getRotationAngle();
        List<SysFilePictureRotatingDTO> stSysFileList = new ArrayList<>();
        int length = 0;
        for (PictureRotatingInfoVO p : rotationAngle) {
            AssertUtils.isNull(p.getRotationAngle(), "参数错误");
            StFileDTO file = stSysFileMap.get(p.getNewFileId()).get(0);
            // 获取对应的存储平台
            Long equipmentId = file.getEquipmentId();
            FileStorage fileStorage = fileStorageService
                    .getFileStorage(String.valueOf(equipmentId));
            // 获取文件流
            if (ObjectUtil.isEmpty(equipmentId)) {
                Result.error("设备id为空", ResultCode.PARAM_ERROR);
            }
            InputStream fileInputStream = null;
            // 判断是否进行了裁剪操作
            String fileName = storageUploadProperties.getSftpDirectory() + pictureRotatingVO.getBusiBatchNo() + "-"
                    + p.getNewFileId() + "." + file.getExt();
            File tempFile = new File(fileName);
            if (tempFile.exists()) {
                try {
                    fileInputStream = new FileInputStream(tempFile);
                } catch (FileNotFoundException e) {
                    log.error("文件不存在", e);
                    throw new RuntimeException(e);
                }
                p.setX1(null);
            } else {
                fileInputStream = getFileInputStream(fileStorage, file.getObjectKey(),
                        file.getIsEncrypt(), file.getEncryptKey(), file.getEncryptType(),
                        file.getEncryptLen() == null ? 0 : file.getEncryptLen().intValue());
            }
            InputStream inputStream = null;
            InputStream inputStream1 = null;
            // 偏离转正
            if (!"false".equals(storageUploadProperties.getPythonDir())) {
                inputStream = ImgPythonCheckUtils.handleImgByInputStream(fileInputStream, fileName,
                        file.getExt(), storageUploadProperties.getPythonDir(), JSONObject.toJSONString(p));
            } else {
                // 加载本地opencv
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                inputStream = handleImgByInputStream(fileInputStream, p);
            }

            AssertUtils.isNull(inputStream, "参数错误");
            // 拿到文件名
            String originalFilename = file.getOriginalFilename();
            // 获取加密方式
            String value = paramApi.searchValueByKey(StateConstants.FILE_ENCRYPT_TYPE).getData()
                    .getValue();
            // 是否加密
            if (StateConstants.IS_ENCRYPT.equals(pictureRotatingVO.getIsEncrypt())) {
                // 加密
                FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(inputStream,
                        Base64Utils.encodeBase64(storageUploadProperties.getEncryptKey()),
                        StateConstants.FILE_ENCRYPT_TYPE_AES.equals(value) ? 0 : 1);
                EncryptDTO dto = encryptService.encrypt(fileEncryptInfoDTO);
                inputStream1 = dto.getInputStream();
                length = dto.getLength();
                if (ObjectUtil.isNull(inputStream1)) {
                    log.info("加密出的件流为空");
                }
            } else {
                inputStream1 = inputStream;
            }
            // 文件上传
            StFileDTO stFile = useS3Upload(originalFilename, pictureRotatingVO.getBusiBatchNo(),
                    token.getId(), inputStream1, equipmentId, pictureRotatingVO.getIsEncrypt(),
                    value);
            stFile.setEncryptLen((long) length);
            stFiles.add(stFile);
            SysFilePictureRotatingDTO sysFilePictureRotatingDTO = new SysFilePictureRotatingDTO();
            BeanUtils.copyProperties(stFile, sysFilePictureRotatingDTO);
            sysFilePictureRotatingDTO.setOldFileId(p.getFileId());
            //去除非必须信息
            sysFilePictureRotatingDTO.setBucketName(null);
            sysFilePictureRotatingDTO.setFilePath(null);
            sysFilePictureRotatingDTO.setObjectKey(null);
            sysFilePictureRotatingDTO.setUrl(null);
            stSysFileList.add(sysFilePictureRotatingDTO);
            // 关闭文件流
            try {
                if (ObjectUtil.isNotEmpty(fileInputStream)) {
                    fileInputStream.close();
                }
                if (ObjectUtil.isNotEmpty(inputStream)) {
                    inputStream.close();
                }
                if (ObjectUtil.isNotEmpty(inputStream1) && inputStream1 != inputStream) {
                    inputStream1.close();
                }
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            } finally {
                if (tempFile.exists()) {
                    FileUtil.del(tempFile);
                }
            }
        }
        stFileService.insertBatch(stFiles);
        return stSysFileList;
    }

    /**
     * 图片预处理
     */
    public List<ResponseEntity<byte[]>> pretreatUpdateImg(PictureRotatingVO pictureRotatingVO,
                                                          AccountToken token,
                                                          HttpServletRequest request) {
        // 加载本地opencv
        List<Long> fileIdList = pictureRotatingVO.getRotationAngle().stream()
                .map(PictureRotatingInfoVO::getNewFileId).collect(Collectors.toList());
        AssertUtils.isNull(fileIdList, "文件id不能为空");

        // 查询文件信息
        List<StFileDTO> stFileList = stFileService.selectFileDTOByIds(fileIdList);
        AssertUtils.isNull(stFileList, "参数错误:文件信息有误");
        Map<Long, List<StFileDTO>> stSysFileMap = stFileList.stream()
                .collect(Collectors.groupingBy(StFileDTO::getId));
        AssertUtils.isNull(stSysFileMap, "请传正确的文件id");
        List<ResponseEntity<byte[]>> responseEntityList = new ArrayList<>();
        for (PictureRotatingInfoVO p : pictureRotatingVO.getRotationAngle()) {
            AssertUtils.isNull(p.getRotationAngle(), "参数错误");
            // 文件信息
            StFileDTO stFile = stSysFileMap.get(p.getNewFileId()).get(0);
            Long equipmentId = stFile.getEquipmentId();
            FileStorage fileStorage = fileStorageService
                    .getFileStorage(String.valueOf(equipmentId));
            // 源文件的文件流或者最后一次裁剪后的文件流
            InputStream inputStreamFromUrl = null;
            // 编辑后的文件流
            InputStream inputStream = null;
            try {
                // 判断是否进行了裁剪操作
                String fileName = storageUploadProperties.getSftpDirectory() + pictureRotatingVO.getBusiBatchNo() + "-"
                        + p.getNewFileId() + "." + stFile.getExt();
                File tempFile = new File(fileName);
                if (tempFile.exists()) {
                    inputStreamFromUrl = new FileInputStream(tempFile);
                } else {
                    // 获取文件流
                    inputStreamFromUrl = getFileInputStream(fileStorage, stFile.getObjectKey(),
                            stFile.getIsEncrypt(), stFile.getEncryptKey(), stFile.getEncryptType(),
                            stFile.getEncryptLen() == null ? 0 : stFile.getEncryptLen().intValue());
                }
                // 拿到文件名
                String originalFilename = stFile.getOriginalFilename();

                // 偏离转正
                if (!"false".equals(storageUploadProperties.getPythonDir())) {
                    inputStream = ImgPythonCheckUtils.handleImgByInputStream(inputStreamFromUrl,
                            fileName, stFile.getExt(), storageUploadProperties.getPythonDir(), JSONObject.toJSONString(p));
                } else {
                    // 加载本地opencv
                    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                    inputStream = handleImgByInputStream(inputStreamFromUrl, p);
                }

                // inputStream = ImgUpdateUtils.handleImgByInputStream(inputStreamFromUrl, p);
                // 如果有裁剪操作则在本地缓存一份文件
                if (ObjectUtil.isNotEmpty(p.getX1())) {
                    FileUtil.writeFromStream(inputStream, tempFile, true);
                    inputStream = new FileInputStream(tempFile);
                }
                //加水印
                if (inputStream != null) {
                    WaterMarkConfigVO waterMarkConfigVO = getWaterMarkConfig(
                            ImgsConstants.WATERMARK_TYPE_SHOW, token);
                    if (waterMarkConfigVO.getOpenFlag()) {
                        Font font = new Font(waterMarkConfigVO.getWaterStyleFamilyValue(),
                                Font.BOLD,
                                Integer.parseInt(waterMarkConfigVO.getWaterStyleSizeValue()));
                        Color color = Color.decode(waterMarkConfigVO.getColor1());
                        String waterCachefileName = storageUploadProperties.getSftpDirectory()
                                + SunCacheDelConstants.DEL_SESSION_SUNYARD + p.getNewFileId() + "-"
                                + request.getSession().getId() + "." + stFile.getExt();
                        ImageWaterUtils.markImgByConfig(inputStream, waterCachefileName,
                                waterMarkConfigVO.getNum(), font, waterMarkConfigVO.getMarkValue(),
                                color, stFile.getExt());
                        inputStream = new FileInputStream(waterCachefileName);
                    }
                }
                byte[] bytes;
                // 如果多张图片则返回缩略图
                if (pictureRotatingVO.getRotationAngle().size() >= StateConstants.COMMON_TWO) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    Thumbnails.of(inputStream)
                            .size(ImgsConstants.THUMBNAILS_WIDTH, ImgsConstants.THUMBNAILS_HIGHT)
                            .outputFormat(ImgsConstants.THUMBNAILS_OUTPUTFORMAT)
                            .toOutputStream(output);
                    bytes = output.toByteArray();
                } else {
                    bytes = FileUtils.read(inputStream);
                }
                if (!ObjectUtils.isEmpty(bytes)) {
                    // 设置响应头，指定文件类型和文件名
                    HttpHeaders headers = new HttpHeaders();
                    // 根据文件扩展名获取对应的 MIME 类型
                    String contentType = FileTypeUtils.getContentTypeByExtension(stFile.getExt());
                    headers.setContentType(MediaType.parseMediaType(contentType));
                    headers.setContentDispositionFormData("inline", originalFilename);
                    responseEntityList
                            .add(new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK));
                }
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            } finally {
                try {
                    if (ObjectUtil.isNotEmpty(inputStreamFromUrl)) {
                        inputStreamFromUrl.close();
                    }
                } catch (IOException e) {
                    log.error("异常描述", e);
                    throw new RuntimeException(e);
                }
                try {
                    if (ObjectUtil.isNotEmpty(inputStream)) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    log.error("异常描述", e);
                    throw new RuntimeException(e);
                }
            }
        }
        return responseEntityList;
    }

    /**
     * 图片旋转
     *
     * @param pictureRotatingVO 图片处理信息
     */
    public List<SysFilePictureRotatingDTO> picturesRotating(PictureRotatingVO pictureRotatingVO,
                                                            AccountToken token) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        List<SysFilePictureRotatingDTO> stSysFileList = new ArrayList<>();
        List<Long> fileIdList = pictureRotatingVO.getRotationAngle().stream()
                .map(PictureRotatingInfoVO::getNewFileId).collect(Collectors.toList());
        AssertUtils.isNull(fileIdList, "文件id不能为空");
        Map<Long, List<StFileDTO>> stSysFileMap = stFileService.selectFileDTOByIds(fileIdList)
                .stream().collect(Collectors.groupingBy(StFileDTO::getId));
        AssertUtils.isNull(stSysFileMap, "请传正确的文件id");
        AssertUtils.isNull(pictureRotatingVO.getStEquipmentId(), "设备id不能为空");
        Long equipmentId = pictureRotatingVO.getStEquipmentId();
        FileStorage fileStorage = fileStorageService.getFileStorage(String.valueOf(equipmentId));
        for (PictureRotatingInfoVO p : pictureRotatingVO.getRotationAngle()) {
            int length = 0;
            StFileDTO stFile = stSysFileMap.get(p.getNewFileId()).get(StateConstants.ZERO);
            InputStream inputStreamFromUrl = getFileInputStream(fileStorage, stFile.getObjectKey(),
                    stFile.getIsEncrypt(), stFile.getEncryptKey(), stFile.getEncryptType(),
                    stFile.getEncryptLen() == null ? 0 : stFile.getEncryptLen().intValue());
            // 读取InputStream并转换为OpenCV的Mat对象
            byte[] byteArray = new byte[0];
            byteArray = FileUtils.read(inputStreamFromUrl);
            // 图片对象
            String ext = stSysFileMap.get(p.getNewFileId()).get(0).getExt();
            File file1 = new File(System.currentTimeMillis() + "." + ext);
            // 旋转
            ByteArrayInputStream inputStream = null;
            try {
                BufferedImage sourceImage = ImageIO.read(new ByteArrayInputStream(byteArray));
                Integer targetHeight = sourceImage.getHeight();
                Integer targetWidth = sourceImage.getWidth();
                if (p.getRotationAngle().equals(ImgsConstants.ROTATIONANGLE_90)
                        || p.getRotationAngle().equals(ImgsConstants.ROTATIONANGLE_270)) {
                    targetHeight = sourceImage.getWidth();
                    targetWidth = sourceImage.getHeight();
                }
                Thumbnails.of(sourceImage).scale(ImgsConstants.ROTATIONANGLE_SCALE_1)
                        .rotate(p.getRotationAngle()).addFilter(new Canvas(targetWidth,
                                targetHeight, Positions.CENTER, Color.WHITE))
                        .toFile(file1);
                byte[] bytes = com.alibaba.excel.util.FileUtils.readFileToByteArray(file1);
                String originalFilename = stFile.getOriginalFilename();
                // 是否加密
                // 获取加密方式
                String value = paramApi.searchValueByKey(StateConstants.FILE_ENCRYPT_TYPE).getData()
                        .getValue();
                if (StateConstants.IS_ENCRYPT.equals(pictureRotatingVO.getIsEncrypt())) {
                    try {
                        FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(
                                new ByteArrayInputStream(bytes),
                                Base64Utils.encodeBase64(storageUploadProperties.getEncryptKey()),
                                StateConstants.FILE_ENCRYPT_TYPE_AES.equals(value) ? 0 : 1);
                        EncryptDTO dto = encryptService.encrypt(fileEncryptInfoDTO);
                        InputStream encrypt = dto.getInputStream();
                        length = dto.getLength();
                        inputStream = new ByteArrayInputStream(IoUtils.input2byte(encrypt));
                        encrypt.close();
                    } catch (Exception e) {
                        log.error("文件旋转加密失败", e);
                        throw new RuntimeException(e);
                    }
                } else {
                    inputStream = new ByteArrayInputStream(bytes);
                }
                StFileDTO stFile2 = useS3Upload(originalFilename,
                        pictureRotatingVO.getBusiBatchNo(), token.getId(), inputStream,
                        pictureRotatingVO.getStEquipmentId(), pictureRotatingVO.getIsEncrypt(),
                        value);
                if (stFile2 != null) {
                    stFile2.setEncryptLen((long) length);
                    stFileService.insert(stFile2);
                }
                SysFilePictureRotatingDTO sysFilePictureRotatingDTO = new SysFilePictureRotatingDTO();
                BeanUtils.copyProperties(stFile2, sysFilePictureRotatingDTO);
                sysFilePictureRotatingDTO.setOldFileId(p.getFileId());
                stSysFileList.add(sysFilePictureRotatingDTO);
                // 删除缓存数据
                com.alibaba.excel.util.FileUtils.delete(file1);
            } catch (Exception e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            } finally {
                // 关闭文件流
                try {
                    if (ObjectUtil.isNotEmpty(inputStream)) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    log.error("异常描述", e);
                    throw new RuntimeException(e);
                }
                try {
                    if (ObjectUtil.isNotEmpty(inputStreamFromUrl)) {
                        inputStreamFromUrl.close();
                    }
                } catch (IOException e) {
                    log.error("异常描述", e);
                    throw new RuntimeException(e);
                }
            }
        }
        return stSysFileList;
    }

    /**
     * 合并文件(图片、文档类型文件合并为pdf并上传到OSS)
     */
    public SysFileDTO mergeFile(FileMergeVO inputStreamList, AccountToken token) {
        List<Long> fileIdList = inputStreamList.getFileIdList();
        AssertUtils.isNull(fileIdList, "要合并的文件id不能为空");
        AssertUtils.isNull(inputStreamList.getEquipmentId(), "设备id不能为空");
        AssertUtils.isNull(inputStreamList.getName(), "文件名不能为空");
        StFileDTO stFile;
        List<InputStream> pdfStreams = getMergePdf(fileIdList, inputStreamList.getPassword());
        InputStream mergedPdfStream;
        int length = 0;
        // 获取加密方式
        String value = paramApi.searchValueByKey(StateConstants.FILE_ENCRYPT_TYPE).getData()
                .getValue();
        if (StateConstants.IS_ENCRYPT.equals(inputStreamList.getIsEncrypt())) {
            FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(
                    FileDealUtils.mergePdfStreams(pdfStreams), Base64Utils.encodeBase64(storageUploadProperties.getEncryptKey()),
                    StateConstants.FILE_ENCRYPT_TYPE_AES.equals(value) ? 0 : 1);
            EncryptDTO dto = encryptService.encrypt(fileEncryptInfoDTO);
            mergedPdfStream = dto.getInputStream();
            length = dto.getLength();
        } else {
            mergedPdfStream = FileDealUtils.mergePdfStreams(pdfStreams);
        }
        stFile = useS3Upload(inputStreamList.getName() + ".pdf", inputStreamList.getBusiBatchNo(),
                token.getId(), mergedPdfStream, inputStreamList.getEquipmentId(),
                inputStreamList.getIsEncrypt(), value);
        if (stFile != null) {
            stFile.setEncryptLen((long) length);
            stFileService.insert(stFile);
        }
        SysFileDTO sysFileDTO = new SysFileDTO();
        BeanUtils.copyProperties(stFile, sysFileDTO);
        //去除非必须信息
        sysFileDTO.setBucketName(null);
        sysFileDTO.setFilePath(null);
        sysFileDTO.setObjectKey(null);
        sysFileDTO.setUrl(null);
        // 关闭文件流
        try {
            if (ObjectUtil.isNotEmpty(mergedPdfStream)) {
                mergedPdfStream.close();
            }
            pdfStreams.forEach(p -> {
                try {
                    if (ObjectUtil.isNotEmpty(p)) {
                        p.close();
                    }
                } catch (IOException e) {
                    log.error("异常描述", e);
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
        return sysFileDTO;
    }

    /**
     * 拆分(将pdf拆分为图片并上传到OSS)
     *
     * @param ecmSplitPdfVo 分片文件对象
     * @param token         token
     * @return Result
     */
    public List<SysFileDTO> splitFile(FileSplitPdfVO ecmSplitPdfVo, AccountToken token) {
        StFileDTO stFile = stFileService.selectFileDTO(Long.valueOf(ecmSplitPdfVo.getNewFileId()));
        AssertUtils.isNull(stFile, "文件不存在");
        AssertUtils.isNull(stFile.getEquipmentId(), "参数错误");
        stFile.setPassword(ecmSplitPdfVo.getPassword());
        String ext = stFile.getExt().toLowerCase();
        AssertUtils.isTrue(!CANSPLITFILETYPE.contains(ext), "只能拆分" + CANSPLITFILETYPE);
        // 获取文件流
        long t11 = System.currentTimeMillis();
        InputStream inputStream;
        if (SunCacheUtils.TIFFLIST.contains(ext) || SunCacheUtils.OFD.contains(ext)
                || SunCacheUtils.HEIF.contains(ext) || SunCacheUtils.DOCS.contains(ext)) {
            // 获取水印配置
            WaterMarkConfigVO waterMarkConfigVO = getWaterMarkConfig(
                    ImgsConstants.WATERMARK_TYPE_SHOW, token);
            inputStream = getInputStream(stFile.getId(), token.getId().toString(), stFile,
                    waterMarkConfigVO);
        } else {
            inputStream = getFileInputStream(String.valueOf(stFile.getEquipmentId()),
                    stFile.getObjectKey(), stFile.getIsEncrypt(), stFile.getEncryptKey(),
                    stFile.getEncryptType(),
                    stFile.getEncryptLen() == null ? 0 : stFile.getEncryptLen().intValue());
        }
        long t22 = System.currentTimeMillis();
        log.info("获取文件流的操作耗时：{}(毫秒)", t22 - t11);
        long t1 = System.currentTimeMillis();
        List<ByteArrayOutputStream> outputStreams = SplitPdfUtils.splitPdfByImgRange(inputStream);
        long t2 = System.currentTimeMillis();
        log.info("冰蓝工具拆分文件的操作耗时：{}(毫秒)", t2 - t1);
        List<SysFileDTO> stStFileList = new ArrayList<>();
        // 拆分后的文件信息
        List<StFileDTO> stFiles = new ArrayList<>();
        int i = 1;
        int length = 0;
        // 加密方式
        String value = paramApi.searchValueByKey(StateConstants.FILE_ENCRYPT_TYPE).getData()
                .getValue();
        for (ByteArrayOutputStream o : outputStreams) {
            // 拆分后的图片命名
            String fileName = ecmSplitPdfVo.getFilename();
            String newFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "_" + i
                    + ".jpg";
            ByteArrayInputStream decryptStream = null;
            if (StateConstants.IS_ENCRYPT.equals(ecmSplitPdfVo.getIsEncrypt())) {
                InputStream encrypt = null;
                try {
                    FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(
                            new ByteArrayInputStream(o.toByteArray()),
                            Base64Utils.encodeBase64(storageUploadProperties.getEncryptKey()),
                            StateConstants.FILE_ENCRYPT_TYPE_AES.equals(value) ? 0 : 1);
                    EncryptDTO dto = encryptService.encrypt(fileEncryptInfoDTO);
                    encrypt = dto.getInputStream();
                    length = dto.getLength();
                    decryptStream = new ByteArrayInputStream(IOUtils.toByteArray(encrypt));

                } catch (Exception e) {
                    log.error("拆分加密失败", e);
                    throw new RuntimeException(e);
                } finally {
                    try {
                        if (encrypt != null) {
                            encrypt.close();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                decryptStream = new ByteArrayInputStream(o.toByteArray());
            }
            // 复制一份输入流,来供下面两个方法各自使用
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                IOUtils.copy(decryptStream, baos);
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
            // 整理文件信息
            StFileDTO stFileNew = handleFileInfo(token, stFile.getEquipmentId(),
                    new ByteArrayInputStream(baos.toByteArray()), newFileName,
                    ecmSplitPdfVo.getIsEncrypt(), value);
            // 异步多线程上传文件
            asyncUploadService.useS3Upload(newFileName, stFileNew.getEquipmentId(),
                    stFileNew.getObjectKey(), stFileNew.getIsEncrypt(),
                    new ByteArrayInputStream(baos.toByteArray()));
            stFileNew.setEncryptLen((long) length);
            stFiles.add(stFileNew);
            SysFileDTO sysFileDTO = new SysFileDTO();
            BeanUtils.copyProperties(stFileNew, sysFileDTO);
            stStFileList.add(sysFileDTO);
            ++i;
            try {
                if (ObjectUtil.isNotEmpty(o)) {
                    o.close();
                }
                if (ObjectUtil.isNotEmpty(decryptStream)) {
                    decryptStream.close();
                }
                if (baos != null) {
                    baos.close();
                }

            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
        }
        long t5 = System.currentTimeMillis();
        stFileService.insertBatch(stFiles);
        long t6 = System.currentTimeMillis();
        log.info("拆分后批量插入数据库的操作耗时：{}(毫秒)", t6 - t5);
        // 关闭文件流
        try {
            if (ObjectUtil.isNotEmpty(inputStream)) {
                inputStream.close();
            }
        } catch (IOException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
        return stStFileList;
    }

    /**
     * 处理文件信息
     *
     * @param token                用户信息
     * @param equipmentId          存储设备信息
     * @param byteArrayInputStream 文件输入流
     * @param newFileName          文件名称
     * @return Result
     */
    private StFileDTO handleFileInfo(AccountToken token, Long equipmentId,
                                     ByteArrayInputStream byteArrayInputStream, String newFileName,
                                     Integer isEncrypt, String encryptType) {
        StFileDTO stFile = new StFileDTO();
        // 根据上传的文件名获取文件后缀名，并使用工具类StrUtil和DateUtil生成唯一的文件名。
        String suffix = newFileName.substring(newFileName.lastIndexOf(".") + 1,
                newFileName.length());
        String fileName1 = IdUtil.randomUUID();
        String key = splitUploadTaskService.getPathKey(fileName1, suffix);
        String contentType = MediaTypeFactory.getMediaType(key)
                .orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
        String md5 = null;
        Long fileSize = (long) byteArrayInputStream.available();
        try {
            md5 = Md5Utils.calculateMD5(byteArrayInputStream);
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
        // 获取对应的存储平台
        FileStorage fileStorage = fileStorageService.getFileStorage(String.valueOf(equipmentId));
        // 添加文件信息
        stFile.setUrl(fileStorage.getPath(key)).setBucketName(fileStorage.getBucketName())
                .setFilePath(fileStorage.getBucketName() + "/" + key).setObjectKey(key)
                .setExt(suffix).setFilename(fileName1).setOriginalFilename(newFileName)
                .setSize(fileSize).setCreateUser(token.getId())
                // .setBusiBatchNo(IdUtil.randomUUID())
                .setFileMd5(md5).setSourceFileMd5(md5).setEquipmentId(equipmentId)
                .setId(snowflakeUtil.nextId()).setCreateTime(new Date())
                // 设置是否加密、加密密钥、加密标识符
                .setIsEncrypt(isEncrypt)
                .setEncryptKey(StateConstants.IS_ENCRYPT.equals(isEncrypt)
                        ? (StateConstants.FILE_ENCRYPT_TYPE_AES.equals(encryptType)
                        ? Base64Utils.encodeBase64(storageUploadProperties.getEncryptKey())
                        : null)
                        : null)
                .setEncryptType(StateConstants.IS_ENCRYPT.equals(isEncrypt)
                        ? (StateConstants.FILE_ENCRYPT_TYPE_AES.equals(encryptType) ? 0 : null)
                        : null);
        // .setEncryptIndex(StateConstants.IS_ENCRYPT.equals(isEncrypt) ?
        // (StateConstants.FILE_ENCRYPT_TYPE_AES.equals(encryptType) ? ENCRYPT_INDEX : SM2_ENCRYPT_INDEX) : null);
        return stFile;
    }

    /**
     * 创建输入流资源
     * 查看图片-pdf资源
     *
     * @param request  请求头
     * @param response 响应头
     * @param fileId   文件id
     * @param token    token
     * @param type     类型
     */
    public void createInputStreamResources(HttpServletRequest request, HttpServletResponse response,
                                           Long fileId, AccountToken token, Integer type, String password) {
        StFileDTO stFile = stFileService.selectFileDTO(fileId);
        AssertUtils.isNull(stFile, "文件不存在");
        stFile.setPassword(password);
        cacheCommonService.waitTransBase(fileId);

        // 获取水印配置
        WaterMarkConfigVO waterMarkConfigVO = getWaterMarkConfig(ImgsConstants.WATERMARK_TYPE_SHOW,
                token);
        String sessionId = request.getSession().getId();
        InputStream inputStream = getInputStream(fileId, sessionId, stFile, waterMarkConfigVO);

        try (ServletOutputStream servletOutputStream = response.getOutputStream()) {
            String ext = stFile.getExt().toLowerCase();
            if (inputStream == null || inputStream.available() == 0) {
                log.info("存储设备中找不到文件");
                throw new SunyardException(ResultCode.PARAM_ERROR, "文件不存在");
            } else {
                if (VIDEOS.contains(ext)) {
                    String name = stFile.getOriginalFilename().replaceFirst("[.][^.]+$", "");
                    response.setHeader("Content-disposition",
                            "inline;filename=" + URLEncoder.encode(name + ".mp4", "UTF-8"));
                    FileDealUtils.loadVideosAudisoPaf(request, response, stFile, inputStream,
                            storageUploadProperties.getSftpDirectory(), ".mp4");
                } else if (AUDIOS.contains(ext)) {
                    String name = stFile.getOriginalFilename().replaceFirst("[.][^.]+$", "");
                    response.setHeader("Content-disposition",
                            "inline;filename=" + URLEncoder.encode(name + ".mp3", "UTF-8"));
                    FileDealUtils.loadVideosAudisoPaf(request, response, stFile, inputStream,
                            storageUploadProperties.getSftpDirectory(), ".mp3");
                } else if (DOCS.contains(ext)) {
                    String name = stFile.getOriginalFilename().replaceFirst("[.][^.]+$", "");
                    response.setHeader("Content-disposition",
                            "inline;filename=" + URLEncoder.encode(name + ".pdf", "UTF-8"));
                    // 获取pdf文件路径
                    String filePath = getFile(fileId, stFile, waterMarkConfigVO, sessionId);
                    // 懒加载pdf
                    FileDealUtils.loadPdf(response, request, filePath, inputStream);
                } else {
                    response.setHeader("Content-disposition", "inline;filename="
                            + URLEncoder.encode(stFile.getOriginalFilename(), "UTF-8"));
                    servletOutputStream.write(FileUtils.read(inputStream));
                    FileUtils.printFile(response, servletOutputStream, stFile.getOriginalFilename(),
                            type);
                }
            }
        } catch (IOException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 获取图片缩略图
     *
     * @param request  请求头
     * @param response 响应头
     * @param fileId   文件id
     * @param token    token
     * @param type     类型
     */
    public void getThumbnail(HttpServletRequest request, HttpServletResponse response, Long fileId,
                             AccountToken token, Integer type) {
        StFileDTO stFile = stFileService.selectFileDTO(fileId);
        AssertUtils.isNull(stFile, "文件不存在");
        // 等待缓存
        cacheCommonService.waitTransBase(fileId);
        if (!ObjectUtil.isNull(stFile)) {
            // 从缓存获取文件
            InputStream inputStream = cacheCommonService
                    .getFileCache(SunCacheDelConstants.THUMBNAIL + stFile.getId(), "png");
            if (inputStream == null) {
                log.info("缓存中找不到文件");
                // 直接从存储设备中获取
                // 获取对应的存储平台
                FileStorage fileStorage = fileStorageService
                        .getFileStorage(String.valueOf(stFile.getEquipmentId()));
                // 从存储设备中获取文件流
                inputStream = fileStorage.getFileStream(stFile.getObjectKey());
                try {
                    if (inputStream == null || inputStream.available() == 0) {
                        log.info("存储设备中找不到文件");
                        throw new SunyardException(ResultCode.PARAM_ERROR, "文件不存在");
                    }
                    // 解密
                    if (SunCacheDelConstants.IS_ENCRYPT.equals(stFile.getIsEncrypt())) {
                        FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(inputStream,
                                stFile.getEncryptKey(), stFile.getEncryptType(),
                                stFile.getEncryptLen() == null ? 0
                                        : stFile.getEncryptLen().intValue());
                        inputStream = encryptService.decrypt(fileEncryptInfoDTO);
                    }
                    if (inputStream.available() == 0) {
                        log.info("解密失败");
                        throw new SunyardException(ResultCode.PARAM_ERROR, "解密失败");
                    }
                } catch (IOException e) {
                    log.error("异常描述", e);
                    throw new RuntimeException(e);
                }
                InputStream fileStream = fileStorage.getFileStream(stFile.getObjectKey());
                // 重新缓存
                cacheCommonService.cacheFile(fileStream, stFile.getUrl(), stFile.getId(),
                        stFile.getExt(), stFile.getIsEncrypt(), stFile.getEncryptKey(),
                        stFile.getEncryptType(),
                        stFile.getEncryptLen() == null ? 0 : stFile.getEncryptLen().intValue(), stFile.getPassword());

                // 从缓存获取文件
                inputStream = cacheCommonService
                        .getFileCache(SunCacheDelConstants.THUMBNAIL + stFile.getId(), "png");

            }

            // 文件下载
            FileDealUtils.getResponseByInputStream(response, fileId, type, inputStream);
        }
    }

    /**
     * 获取水印配置
     *
     * @param type  类型
     * @param token token
     * @return Result
     */
    public WaterMarkConfigVO getWaterMarkConfig(Integer type, AccountToken token) {
        WaterMarkConfigVO waterMarkConfigVO = new WaterMarkConfigVO();
        Result<SysParamDTO> sysParamDtoResult = paramApi
                .searchValueByKey(StateConstants.WATERMARK_PARAM_FILE);
        SysParamDTO sysParamDTO = sysParamDtoResult.getData();
        if (ObjectUtil.isNull(sysParamDTO)) {
            waterMarkConfigVO.setOpenFlag(false);
            return waterMarkConfigVO;
        }
        JSONObject jsonObject = JSONObject.parseObject(sysParamDTO.getValue());
        waterMarkConfigVO = JSONObject.toJavaObject(jsonObject, WaterMarkConfigVO.class);
        // 获取水印内容
        String markValue = getMarkValue(waterMarkConfigVO.getCheckList(),
                waterMarkConfigVO.getContentValue(), token);
        waterMarkConfigVO.setMarkValue(markValue);
        // 对应操作类型下是否开启水印 查看 打印 下载
        if (type.equals(ImgsConstants.WATERMARK_TYPE_DOWNLOAD)) {
            waterMarkConfigVO.setOpenFlag(waterMarkConfigVO.getSwitchValueDownload());
        } else if (type.equals(ImgsConstants.WATERMARK_TYPE_PRINTING)) {
            // 打印
            waterMarkConfigVO.setOpenFlag(waterMarkConfigVO.getSwitchValuePrint());
        } else {
            // 查看
            waterMarkConfigVO.setOpenFlag(waterMarkConfigVO.getSwitchValueShow());
        }
        // 填充中文字体
        List<SysDictionaryDTO> dicDTOList = dictionaryApi
                .selectValueByParentKey(StateConstants.COMMON_WATERMARK_FAMILYNAME, null).getData();
        for (SysDictionaryDTO dicDTO : dicDTOList) {
            if (dicDTO.getDicVal().equals(waterMarkConfigVO.getWaterStyleFamilyValue())) {
                waterMarkConfigVO.setWaterStyleFamilyValue(dicDTO.getRemark());
                break;
            }
        }
        log.info("水印配置：waterMarkConfig:{}", waterMarkConfigVO);
        return waterMarkConfigVO;
    }

    /**
     * 判断文件是否有密码保护
     *
     * @param fileIds 文件ID列表
     * @return 统一响应结果（true:至少一个文件有密码；false:所有文件无密码）
     */
    public Result isPasswordProtected(List<Long> fileIds) {
        // 1. 参数校验
        if (fileIds == null || fileIds.isEmpty()) {
            return Result.success(false);
        }

        List<StFileDTO> stFiles = stFileService.selectFileDTOByIds(fileIds);
        if (stFiles == null || stFiles.isEmpty()) {
            return Result.success(false);
        }

        // 2. 统计加密文件数量（使用原子类确保线程安全）
        AtomicInteger encryptedCount = new AtomicInteger(0);

        stFiles.forEach(file -> {
            // 已发现2个及以上加密文件，直接跳过后续判断
            if (encryptedCount.get() >= 2) {
                return;
            }

            String fileExt = file.getExt();
            // 只处理目标文件类型（PDF、DOCX、XLS）
            if (!(SunCacheUtils.PDF.contains(fileExt)
                    || SunCacheUtils.DOCX.contains(fileExt)
                    || SunCacheUtils.XLS.contains(fileExt))) {
                return;
            }

            // 3. 获取文件流（使用try-with-resources自动关闭流）
            try (InputStream inputStream = getFileInputStream(
                    String.valueOf(file.getEquipmentId()),
                    file.getObjectKey(),
                    file.getIsEncrypt(),
                    file.getEncryptKey(),
                    file.getEncryptType(),
                    file.getEncryptLen() == null ? 0 : file.getEncryptLen().intValue()
            )) {
                // 4. 根据文件类型尝试打开，捕获加密异常
                if (SunCacheUtils.PDF.contains(fileExt)) {
                    PdfDocument pdf = new PdfDocument();
                    pdf.loadFromStream(inputStream);
                    pdf.close();
                } else if (SunCacheUtils.DOCX.contains(fileExt)) {
                    if (WordTextExtractor.isDocFile(inputStream)) {
                        HWPFDocument doc = new HWPFDocument(inputStream);
                        doc.close();
                    } else {
                        XWPFDocument doc = new XWPFDocument(inputStream);
                        doc.close();
                    }
                } else if (SunCacheUtils.XLS.contains(fileExt)) {
                    Workbook workbook = WorkbookFactory.create(inputStream);
                    workbook.close();
                }

            } catch (EncryptedDocumentException e) {
                // 捕获excel密码问题
                encryptedCount.incrementAndGet();
            } catch (RuntimeException e) {
                log.error("系统异常",e);
                // 捕获PDF密码问题
                if ("can not open an encrypted document. The password is invalid.".equals(e.getMessage())) {
                    encryptedCount.incrementAndGet();
                }
            } catch (IOException e) {
                // 处理doc格式加密
                if (e instanceof FileNotFoundException) {
                    encryptedCount.incrementAndGet();
                }
            }
        });

        // 5. 根据加密文件数量返回不同结果
        int count = encryptedCount.get();
        if (count == 0) {
            return Result.success("false");
        } else if (count == 1) {
            return Result.success("true");
        } else {
            return Result.success("noSupport");
        }
    }


    /**
     * 文件下载
     *
     * @param sessionId  sessionId
     * @param downFileVO 文件下载对象
     * @param token      token
     * @param response   响应头
     */
    public void downFile(String sessionId, DownFileVO downFileVO, AccountToken token,
                         HttpServletResponse response) {
        Assert.notNull(downFileVO.getFileId(), "文件id不能为空!");
        WaterMarkConfigVO waterMarkConfigVO = getWaterMarkConfig(
                ImgsConstants.WATERMARK_TYPE_DOWNLOAD, token);
        if (downFileVO.getIsPack().equals(StateConstants.COMMON_ONE)) {
            // 将文件封装到压缩包中
            // 生成的压缩文件临时路径
            String zipPath = storageUploadProperties.getFileDownTemp() + System.currentTimeMillis() + ".zip";
            InputStream fileZipInputStream = getFileZipInputStream(sessionId, downFileVO,
                    waterMarkConfigVO, zipPath);
            String fileName = UUID.fastUUID() + ".zip";
            response.setContentType(FileDealUtils.getContentType(fileName));
            response.setContentLengthLong(new File(zipPath).length());
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            //客户端页面和文件设置缓存
            response.setHeader("Cache-Control", "public, max-age=604800");
            response.setHeader("Pragma", "cache");
            //            response.setHeader("Expires", "0");
            try (ServletOutputStream outputStream = response.getOutputStream();
                 BufferedInputStream bin = new BufferedInputStream(fileZipInputStream)) {
                byte[] b = new byte[1024 * 4];
                int len = 0;
                while ((len = bin.read(b)) != -1) {
                    outputStream.write(b, 0, len);
                }
                outputStream.flush();
                log.info("文件的二进制流输出完成,下载文件名:{}", fileName);
                // 删除生成的压缩文件临时路径
                FileUtil.del(zipPath);
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            } finally {
                try {
                    if (null != fileZipInputStream) {
                        fileZipInputStream.close();
                    }
                } catch (IOException e) {
                    log.error("异常描述", e);
                    throw new RuntimeException(e);
                }

            }
        } else {
            // 单个文件形式下载
            AssertUtils.isTrue(CollectionUtils.isEmpty(downFileVO.getFileId())
                    || downFileVO.getFileId().size() != StateConstants.COMMON_ONE, "传参有误");
            StFileDTO stFile = stFileService
                    .selectFileDTO(downFileVO.getFileId().get(StateConstants.ZERO));
            stFile.setPassword(downFileVO.getPassword());
            AssertUtils.isNull(stFile, "参数错误");
            InputStream in = null;
            BufferedInputStream bin = null;
            ServletOutputStream outStream = null;
            // 获取文件输入流
            long l1 = System.currentTimeMillis();
            in = getDownInputStream(sessionId, waterMarkConfigVO, stFile);
            long l2 = System.currentTimeMillis();
            log.info("获取文件输入流时间：{}", l2 - l1);
            String originalFilename = FileTypeUtils.getOriginalFilename(downFileVO, stFile);
            // 设置响应头，指定文件类型和下载的文件名
            // 根据文件扩展名获取对应的 MIME 类型
            // response.setContentType("application/octet-stream");
            //客户端页面和文件设置缓存
            response.setHeader("Cache-Control", "public, max-age=604800");
            response.setHeader("Pragma", "cache");
            //            response.setHeader("Expires", "0");
            String encode = null;
            try {
                encode = URLEncoder.encode(originalFilename, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
            response.setHeader("Content-disposition", "attachment;filename=" + encode);
            try {
                bin = new BufferedInputStream(in);
                outStream = response.getOutputStream();
                byte[] b = new byte[1024 * 4];
                int len = 0;
                while ((len = bin.read(b)) != -1) {
                    outStream.write(b, 0, len);
                }
                log.info("文件的二进制流输出完成,下载文件名:{}", originalFilename);
            } catch (IOException e) {
                log.error("下载失败", e);
                throw new RuntimeException(e);
            } finally {
                try {
                    log.info("正在关闭文件流");
                    if (null != outStream) {
                        outStream.flush();
                        outStream.close();
                        outStream = null;
                    }
                    if (null != bin) {
                        bin.close();
                        bin = null;
                    }
                    if (null != in) {
                        in.close();
                        in = null;
                    }
                } catch (IOException e) {
                    log.error("关闭文件流失败", e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 文件下载
     *
     * @param downFileVO 文件下载对象
     * @param token      token
     * @param request    请求头
     * @param response   响应头
     */
    public void shardingDownFile(DownFileVO downFileVO, AccountToken token,
                                 HttpServletRequest request, HttpServletResponse response) {
        String sessionId = request.getSession().getId();
        Assert.notNull(downFileVO.getFileId(), "文件id不能为空!");
        WaterMarkConfigVO waterMarkConfigVO = getWaterMarkConfig(
                ImgsConstants.WATERMARK_TYPE_DOWNLOAD, token);
        // 要下载的文件流
        InputStream inputStream = null;
        // 压缩包文件路径
        String zipPath = null;
        // 要下载的文件大小
        long fileLen = 0;
        if (downFileVO.getIsPack().equals(StateConstants.COMMON_ONE)) {
            if (ObjectUtil.isEmpty(downFileVO.getFileName())) {
                downFileVO.setFileName(UUID.fastUUID() + ".zip");
            }
            // 压缩包下载
            // 将文件封装到压缩包中
            // 生成的压缩文件临时路径
            String zipFilePath = storageUploadProperties.getFileDownTemp() + downFileVO.getFileName();
            File zipFile = new File(zipFilePath);
            // 判断压缩文件是否存在
            if (!zipFile.exists()) {
                String fileName = downFileVO.getFileName() + ".zip";
                zipPath = storageUploadProperties.getFileDownTemp() + fileName;
                inputStream = getFileZipInputStream(sessionId, downFileVO, waterMarkConfigVO,
                        zipPath);
            } else {
                try {
                    inputStream = new FileInputStream(zipFile);
                } catch (FileNotFoundException e) {
                    log.error("异常描述", e);
                    throw new RuntimeException(e);
                }
                zipPath = zipFilePath;
            }
            fileLen = new File(zipPath).length();
        } else {
            // 单个文件形式下载
            AssertUtils.isTrue(CollectionUtils.isEmpty(downFileVO.getFileId())
                    || downFileVO.getFileId().size() != StateConstants.COMMON_ONE, "传参有误");
            StFileDTO stFile = stFileService
                    .selectFileDTO(downFileVO.getFileId().get(StateConstants.ZERO));
            AssertUtils.isNull(stFile, "参数错误");
            stFile.setPassword(downFileVO.getPassword());
            // 获取文件输入流
            long l1 = System.currentTimeMillis();
            inputStream = getDownInputStream(sessionId, waterMarkConfigVO, stFile);
            long l2 = System.currentTimeMillis();
            log.info("获取文件输入流时间：{}", l2 - l1);
            String originalFilename = FileTypeUtils.getOriginalFilename(downFileVO, stFile);
            downFileVO.setFileName(originalFilename);
            try {
                // 文件超过2G会有问题，inputStream.available()的最大值就是2G
                fileLen = inputStream.available();
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
        }
        String encode = null;
        try {
            encode = URLEncoder.encode(downFileVO.getFileName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
        // response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition", "attachment;filename=" + encode);
        //客户端页面和文件设置缓存
        response.setHeader("Cache-Control", "public, max-age=604800");
        response.setHeader("Pragma", "cache");
        //        response.setHeader("Expires", "0");
        boolean isRange = true;
        String range = request.getHeader("Range");
        // 查看请求头中有没有Range
        if (range == null) {
            range = "bytes=0-";
            isRange = false;
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        }
        long end = 0;
        try (ServletOutputStream outputStream = response.getOutputStream();
             BufferedInputStream bin = new BufferedInputStream(inputStream)) {
            if (!ObjectUtil.isEmpty(inputStream)) {
                byte[] b = new byte[1024 * 4];
                int len = 0;
                if (isRange) {
                    response.setHeader("Accept-Ranges", "bytes");
                    long start = Long
                            .parseLong(range.substring(range.indexOf("=") + 1, range.indexOf("-")));

                    if (range.endsWith(FileConstants.SPLIT)) {
                        end = fileLen - 1;
                    } else {
                        end = Long.parseLong(range.substring(range.indexOf("-") + 1));
                    }
                    String contentRange = "bytes " + start + "-" + end + "/" + fileLen;
                    response.setHeader("Content-Range", contentRange);
                    long skip = bin.skip(start);
                    long totalBytesRead = 0;
                    while ((len = bin.read(b)) != -1) {
                        outputStream.write(b, 0, len);
                        totalBytesRead += len;
                    }
                } else {
                    while ((len = bin.read(b)) != -1) {
                        outputStream.write(b, 0, len);
                    }
                }
            }
            outputStream.flush();
            outputStream.close();
            log.info("文件的二进制流输出完成,下载文件名:{}", downFileVO.getFileName());
            boolean isPack = downFileVO.getIsPack().equals(StateConstants.COMMON_ONE);
            boolean isEnd = end == fileLen || !isRange;
            if (isPack && isEnd) {
                // 删除生成的压缩文件临时路径
                FileUtil.del(zipPath);
            }
        } catch (IOException e) {
            log.error("下载失败", e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                    inputStream = null;
                }
            } catch (IOException e) {
                log.error("关闭文件流失败", e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 文件下载
     *
     * @param sessionId  sessionId
     * @param downFileVO 文件下载对象
     * @param token      token
     * @param zipPath    zip压缩包的路径，没有则为null
     * @return Result
     */
    public InputStream downFileByInputStream(String sessionId, DownFileVO downFileVO,
                                             AccountToken token, String zipPath) {
        Assert.notNull(downFileVO.getFileId(), "文件id不能为空!");
        WaterMarkConfigVO waterMarkConfigVO = getWaterMarkConfig(
                ImgsConstants.WATERMARK_TYPE_DOWNLOAD, token);
        if (zipPath != null) {
            // 将文件封装到临时压缩包中
            return getFileZipInputStream(sessionId, downFileVO, waterMarkConfigVO, zipPath);
        }
        // 单个文件形式下载
        AssertUtils.isTrue(CollectionUtils.isEmpty(downFileVO.getFileId())
                || downFileVO.getFileId().size() != StateConstants.COMMON_ONE, "传参有误");
        StFileDTO stfile = stFileService
                .selectFileDTO(downFileVO.getFileId().get(StateConstants.ZERO));
        AssertUtils.isNull(stfile, "文件不存在");
        stfile.setPassword(downFileVO.getPassword());
        // 将输入流转换为字节数组
        return getDownInputStream(sessionId, waterMarkConfigVO, stfile);
    }

    /**
     * 文件下载
     *
     * @param sessionId  sessionId
     * @param downFileVO 文件下载对象
     * @param token      token
     * @param response   响应头
     * @return Result
     */
    public ResponseEntity<byte[]> downFileByResponseEntity(String sessionId, DownFileVO downFileVO,
                                                           AccountToken token,
                                                           HttpServletResponse response) {
        Assert.notNull(downFileVO.getFileId(), "文件id不能为空!");
        WaterMarkConfigVO waterMarkConfigVO = getWaterMarkConfig(
                ImgsConstants.WATERMARK_TYPE_DOWNLOAD, token);
        if (downFileVO.getIsPack().equals(StateConstants.COMMON_ONE)) {
            // 将文件封装到压缩包中
            // 将文件封装到临时压缩包中
            String zipPath = storageUploadProperties.getFileDownTemp() + System.currentTimeMillis() + ".zip";
            InputStream zipInputStream = getFileZipInputStream(sessionId, downFileVO,
                    waterMarkConfigVO, zipPath);
            byte[] zipBytes;
            zipBytes = FileUtils.read(zipInputStream);
            // 设置响应头，指定文件类型和下载的文件名
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/zip"));
            headers.setContentDispositionFormData("attachment", "D:\\" + UUID.fastUUID());
            // 返回响应实体
            return new ResponseEntity<byte[]>(zipBytes, headers, HttpStatus.OK);
        }
        // 单个文件形式下载
        AssertUtils.isTrue(CollectionUtils.isEmpty(downFileVO.getFileId())
                || downFileVO.getFileId().size() != StateConstants.COMMON_ONE, "传参有误");
        StFileDTO stFile = stFileService
                .selectFileDTO(downFileVO.getFileId().get(StateConstants.ZERO));
        AssertUtils.isNull(stFile, "参数错误");
        stFile.setPassword(downFileVO.getPassword());
        // 将输入流转换为字节数组
        InputStream inputStream = getDownInputStream(sessionId, waterMarkConfigVO, stFile);
        byte[] fileBytes;
        fileBytes = FileUtils.read(inputStream);
        // 设置响应头，指定文件类型和下载的文件名
        HttpHeaders headers = new HttpHeaders();
        // 根据文件扩展名获取对应的 MIME 类型
        String contentType = FileTypeUtils.getContentTypeByExtension(stFile.getExt());
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", "D:\\" + stFile.getOriginalFilename());
        try {
            if (ObjectUtil.isNotEmpty(inputStream)) {
                inputStream.close();
            }
        } catch (IOException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
        // 返回响应实体
        return new ResponseEntity<byte[]>(fileBytes, headers, HttpStatus.OK);
    }

    /**
     * 下载源文件带水印的
     *
     * @param sessionId
     * @param fileId
     * @param token
     * @param response
     */
    public void getFileByFileIdByWater(String sessionId, Long fileId, AccountToken token,
                                       HttpServletResponse response, String password) {
        Assert.notNull(fileId, "文件id不能为空!");
        WaterMarkConfigVO waterMarkConfigVO = getWaterMarkConfig(ImgsConstants.WATERMARK_TYPE_SHOW,
                token);

        StFileDTO stFile = stFileService.selectFileDTO(fileId);
        AssertUtils.isNull(stFile, "参数错误");
        stFile.setPassword(password);

        InputStream inputStream = null;
        try {
            // 获取文件输入流（可能抛异常）
            inputStream = getDownInputStream(sessionId, waterMarkConfigVO, stFile);

            // 解密
            if (password != null && !password.trim().isEmpty()
                    && FileDecryptUtil.SUPPORTED_FORMATS.contains(stFile.getExt().toLowerCase())) {
                inputStream = FileDecryptUtil.decrypt(inputStream, stFile.getExt().toLowerCase(), password);
            }

            // 获取输出流并写入数据
            try (ServletOutputStream outputStream = response.getOutputStream()) {
                response.setHeader("Content-disposition", "attachment;filename="
                        + URLEncoder.encode(stFile.getOriginalFilename(), "UTF-8"));
                outputStream.write(FileUtils.read(inputStream)); // 写入文件内容
                outputStream.flush();
            }
        } catch (Exception e) {
            log.error("接口报错", e);
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭输入流失败", e);
                }
            }
        }
    }

    /**
     * 文件打印
     *
     * @param request  请求头
     * @param response 响应头
     * @param token    token
     */
    public void printFile(HttpServletRequest request, HttpServletResponse response,
                          List<Long> fileIds, AccountToken token, String password) {
        // 从存储服务获取文件输入流
        AssertUtils.isNull(fileIds, "文件id不能为空");
        // 合并文件 多个图片、word、excel、ppt合并为一个pdf
        List<InputStream> pdfStreams = getMergePdf(fileIds, password);
        try ( // 将多个pdf文件流合成一个pdf
              InputStream mergedPdfStream = FileDealUtils.mergePdfStreams(pdfStreams);
              ServletOutputStream servletOutputStream = response.getOutputStream()) {
            // 获取水印配置
            WaterMarkConfigVO waterMarkConfigVO = getWaterMarkConfig(
                    ImgsConstants.WATERMARK_TYPE_PRINTING, token);
            if (waterMarkConfigVO.getOpenFlag()) {
                Font font = new Font(waterMarkConfigVO.getWaterStyleFamilyValue(), Font.BOLD,
                        Integer.parseInt(waterMarkConfigVO.getWaterStyleSizeValue()));
                Color color = Color.decode(waterMarkConfigVO.getColor1());
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                WatermarkUtils.addPdfWaterMarkByConfig(mergedPdfStream, outputStream,
                        waterMarkConfigVO.getNum(), font, color, waterMarkConfigVO.getMarkValue());
                outputStream.writeTo(servletOutputStream);
                FileUtils.printFile(response, servletOutputStream, "打印带水印.pdf");
            } else {
                byte[] bytes = FileUtils.read(mergedPdfStream);
                servletOutputStream.write(bytes);
                FileUtils.printFile(response, servletOutputStream, "打印.pdf");
            }
        } catch (IOException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取文件地址
     *
     * @param fileId 文件id
     * @return Result
     */
    public String getFileUrl(Long fileId) {
        AssertUtils.isNull(fileId, "文件id不能为空");
        StFileDTO stFile = stFileService.selectFileDTO(fileId);
        if (!ObjectUtil.isNull(stFile)) {
            return stFile.getUrl();
        }
        return null;
    }

    /**
     * 删除文件
     *
     * @param fileId 文件id
     */
    public void deleteFile(Long fileId) {
        AssertUtils.isNull(fileId, "参数错误:文件id不能为空");
        // 查询文件信息
        StFileDTO stFile = stFileService.selectFileDTO(fileId);
        AssertUtils.isNull(stFile, "参数错误:文件不存在");
        // 查询设配信息
        StEquipment stEquipment = stEquipmentMapper.selectById(stFile.getEquipmentId());
        AssertUtils.isNull(stEquipment, "参数错误:设备信息不存在");
        // 获取对应的存储平台
        FileStorage fileStorage = fileStorageService
                .getFileStorage(String.valueOf(stEquipment.getId()));
        fileStorage.delete(stFile.getObjectKey());
        //物理删除文件
        stFileService.physicalDeleteById(fileId);
        splitUploadTaskService.physicalDeleteByFileId(fileId);
    }

    /**
     * 获取文件流
     *
     * @param fileId            文件id
     * @param sessionId         sessionId
     * @param stFile            文件对象
     * @param waterMarkConfigVO 水印对象
     * @return Result
     */
    public InputStream getInputStream(Long fileId, String sessionId, StFileDTO stFile,
                                      WaterMarkConfigVO waterMarkConfigVO) {
        StopWatch sw = new StopWatch();
        // 文件后缀
        String fileExt = stFile.getExt().toLowerCase();
        // 文件流
        InputStream inputStream = null;
        log.info("文件类型: {}", fileExt);
        String fileName = String.valueOf(fileId);
        if (ObjectUtil.isNotEmpty(waterMarkConfigVO) && waterMarkConfigVO.getOpenFlag()) {
            log.info("开启水印");
            // 开启水印配置
            inputStream = getFileWaterInputStream(sessionId, fileId, waterMarkConfigVO,
                    stFile.getObjectKey(), fileExt, stFile.getEquipmentId(),
                    stFile.getIsEncrypt(), stFile.getEncryptKey(), stFile.getEncryptType(),
                    stFile.getEncryptLen() == null ? 0 : stFile.getEncryptLen().intValue(), stFile.getPassword());
        } else {
            //没开水印的图片直接存存储设备拿流返回
            try {
                if (SunCacheUtils.IMGS.contains(fileExt)) {
                    FileStorage fileStorage = fileStorageService
                            .getFileStorage(String.valueOf(stFile.getEquipmentId()));
                    // 从存储设备中获取文件流
                    inputStream = fileStorage.getFileStream(stFile.getObjectKey());
                    if (inputStream == null || inputStream.available() == 0) {
                        log.info("存储设备中找不到文件");
                        throw new SunyardException(ResultCode.PARAM_ERROR, "文件不存在");
                    }
                    // 解密
                    if (SunCacheDelConstants.IS_ENCRYPT.equals(stFile.getIsEncrypt())) {
                        FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(inputStream,
                                stFile.getEncryptKey(), stFile.getEncryptType(),
                                stFile.getEncryptLen() == null ? 0 : stFile.getEncryptLen().intValue());
                        inputStream = encryptService.decrypt(fileEncryptInfoDTO);
                    }
                    return inputStream;
                }
            } catch (Exception e) {
                log.error("读取文件流出错");
                throw new RuntimeException(e);
            }
        }
        // 没开启水印 或者 开启水印但是没获取到包含水印的文件流 就去缓存拿没水印的文件流
        if (inputStream == null) {
            log.info("从缓存中获取文件");
            // 从缓存中获取文件
            inputStream = cacheCommonService.getFileCache(fileName, fileExt);
        }
        try {
            if (inputStream == null || inputStream.available() == 0) {
                if (ObjectUtil.isNotEmpty(waterMarkConfigVO) && waterMarkConfigVO.getOpenFlag()) {
                    log.info("开启水印但是没获取到包含水印的文件流,去缓存拿没水印的文件流");
                } else {
                    log.info("没开启水印,缓存中找不到文件");
                }
                // 直接从存储设备中获取
                sw.start("存储设备获取文件耗时");
                // 获取对应的存储平台
                FileStorage fileStorage = fileStorageService
                        .getFileStorage(String.valueOf(stFile.getEquipmentId()));
                // 从存储设备中获取文件流
                inputStream = fileStorage.getFileStream(stFile.getObjectKey());
                if (inputStream == null || inputStream.available() == 0) {
                    log.info("存储设备中找不到文件");
                    throw new SunyardException(ResultCode.PARAM_ERROR, "文件不存在");
                }
                // 解密
                if (SunCacheDelConstants.IS_ENCRYPT.equals(stFile.getIsEncrypt())) {
                    FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(inputStream,
                            stFile.getEncryptKey(), stFile.getEncryptType(),
                            stFile.getEncryptLen() == null ? 0 : stFile.getEncryptLen().intValue());
                    inputStream = encryptService.decrypt(fileEncryptInfoDTO);
                }
                if (inputStream.available() == 0) {
                    log.info("解密失败");
                    throw new SunyardException(ResultCode.PARAM_ERROR, "解密失败");
                }
                // 重新将文件缓存到缓存目录
                log.info("重新将文件缓存到缓存目录");
                cacheCommonService.cacheFile(inputStream, stFile.getUrl(), fileId, fileExt, 0,
                        stFile.getEncryptKey(), stFile.getEncryptType(),
                        stFile.getEncryptLen() != null ? stFile.getEncryptLen().intValue()
                                : null, stFile.getPassword());
                //从缓存拿文件
                inputStream = cacheCommonService.getFileCache(fileName, fileExt);
                sw.stop();
            }
        } catch (IOException e) {
            log.error("读取文件流出错");
            throw new RuntimeException(e);
        }
        log.info(sw.prettyPrint());
        return inputStream;
    }

    /**
     * 加密文件
     *
     * @param stFileId 文件ID集合
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result encryptStFile(List<Long> stFileId) {
        AssertUtils.isNull(stFileId, "参数错误: 文件id(fileId)不能为空");
        for (Long id : stFileId) {
            StFileDTO stFile = stFileService.selectFileDTO(id);
            // 未加密才进行加密
            if (null != stFileId && StateConstants.ZERO.equals(stFile.getIsEncrypt())) {
                // 更新stFile信息
                StFileDTO stFile1 = fileEncrypt(stFile);
                stFile1.setIsEncrypt(StateConstants.IS_ENCRYPT);
                stFile1.setEncryptKey(Base64Utils.encodeBase64(storageUploadProperties.getEncryptKey()));
                // stFile1.setEncryptIndex(ENCRYPT_INDEX);
                stFileService.update(stFile1);

            }
        }
        return Result.success("");
    }

    /**
     * 文件详情列表
     *
     * @param fileIds 文件id集
     * @return Result
     */
    public List<SysFileDTO> details(List<Long> fileIds) {
        List<StFileDTO> stStFiles = stFileService.selectFileDTOByIds(fileIds);
        List<SysFileDTO> sysFileDtoList = PageCopyListUtils.copyListProperties(stStFiles,
                SysFileDTO.class);
        return sysFileDtoList;
    }

    /**
     * 获取原文件流
     *
     * @param fileId 文件id
     * @param stFile 文件对象
     * @return Result
     */
    public InputStream getInitialInputStream(Long fileId, StFileDTO stFile) {
        // 文件后缀
        String fileExt = stFile.getExt().toLowerCase();
        // 文件流
        InputStream inputStream = null;
        log.info("文件类型: {}", fileExt);
        try {
            // 获取对应的存储平台
            FileStorage fileStorage = fileStorageService
                    .getFileStorage(String.valueOf(stFile.getEquipmentId()));
            // 从存储设备中获取文件流
            inputStream = fileStorage.getFileStream(stFile.getObjectKey());
            if (inputStream == null || inputStream.available() == 0) {
                log.info("存储设备中找不到文件");
                throw new SunyardException(ResultCode.PARAM_ERROR, "文件不存在");
            }
            // 解密
            if (SunCacheDelConstants.IS_ENCRYPT.equals(stFile.getIsEncrypt())) {
                FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(inputStream,
                        stFile.getEncryptKey(), stFile.getEncryptType(),
                        stFile.getEncryptLen() == null ? 0 : stFile.getEncryptLen().intValue());
                inputStream = encryptService.decrypt(fileEncryptInfoDTO);
            }
            if (inputStream.available() == 0) {
                log.info("解密失败");
                throw new SunyardException(ResultCode.PARAM_ERROR, "解密失败");
            }
        } catch (IOException e) {
            log.error("读取文件流出错");
            throw new RuntimeException(e);
        }
        return inputStream;
    }

    /**
     * 获取源文件
     *
     * @param request
     * @param response
     * @param fileId
     */
    public void getFileByFileId(HttpServletRequest request, HttpServletResponse response,
                                Long fileId) {
        AssertUtils.isNull(fileId, "参数错误: 文件id(fileId)不能为空");
        StFileDTO stFile = stFileService.selectFileDTO(fileId);
        InputStream inputStream = getInitialInputStream(fileId, stFile);
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            if (inputStream == null || inputStream.available() == 0) {
                log.info("存储设备中找不到文件");
                throw new SunyardException(ResultCode.PARAM_ERROR, "文件不存在");
            } else {
                if (outputStream != null) {
                    response.setHeader("Content-disposition", "attachment;filename="
                            + URLEncoder.encode(stFile.getOriginalFilename(), "UTF-8"));
                    response.setContentType("application/octet-stream");
                    //客户端页面和文件设置缓存
                    response.setHeader("Cache-Control", "public, max-age=604800");
                    response.setHeader("Pragma", "cache");
                    //                    response.setHeader("Expires", "0");
                    response.setHeader("Accept-Ranges", "bytes");
                    try {
                        outputStream.write(FileUtils.read(inputStream));
                        outputStream.flush();
                    } catch (IOException e) {
                        log.error("意外错误：", e);
                    } finally {
                        try {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        } catch (IOException e) {
                            log.error("意外错误：", e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 上传文件
     */
    public StFileDTO fileUpload(String fileName2, String busiBatchNo, Long userId,
                                InputStream mergedPdfStream, Long stEquipmentId, Integer isEncrypt,
                                String encryptType) {
        return useS3Upload(fileName2, busiBatchNo, userId, mergedPdfStream, stEquipmentId,
                isEncrypt, encryptType);
    }

    /**
     * 根据文件id获取文件信息
     *
     * @param id id
     * @return Result
     */
    public SysFileDTO getFileInfo(Long id) {
        StFileDTO stFile = stFileService.selectFileDTO(id);
        SysFileDTO sysFileDTO = new SysFileDTO();
        BeanUtils.copyProperties(stFile, sysFileDTO);
        return sysFileDTO;
    }

    /**
     * 混贴拆分
     */
    public List<SysFileDTO> mixedPastingSplit(MixedPastingSplitDTO mixedPastingSplitDto) {
        List<SysFileDTO> sysFileDtoList = new ArrayList<>();
        StFileDTO stFile = stFileService.selectFileDTO(mixedPastingSplitDto.getId());
        AssertUtils.isNull(stFile, "参数错误:文件不存在");
        // 混贴拆分后的文件信息
        List<StFileDTO> stFileList = new ArrayList<>();
        // 查看存储设备是对象存储还是Nas存储
        StEquipment stEquipment = stEquipmentMapper.selectById(stFile.getEquipmentId());
        InputStream inputStream = null;
        if (!ObjectUtils.isEmpty(stEquipment)) {
            // 获取文件流
            inputStream = getFileInputStream(String.valueOf(stFile.getEquipmentId()),
                    stFile.getObjectKey(), stFile.getIsEncrypt(), stFile.getEncryptKey(),
                    stFile.getEncryptType(),
                    stFile.getEncryptLen() == null ? 0 : stFile.getEncryptLen().intValue());

        } else {
            log.info("混铁拆分中获取的文件信息有误");
        }

        // 加载本地OpenCV库
        if (!"false".equals(storageUploadProperties.getPythonDir())) {
            String fileName1 = storageUploadProperties.getSftpDirectory() + stFile.getId() + "." + stFile.getExt();
            final Integer[] a = {StateConstants.COMMON_ONE};

            for (List<Integer> position : mixedPastingSplitDto.getRegionList()) {
                PictureRotatingInfoVO p = new PictureRotatingInfoVO();
                p.setX1(position.get(0));
                p.setY1(position.get(1));
                p.setX2(position.get(0) + position.get(2));
                p.setY2(position.get(1) + position.get(3));
                p.setHorizontalMirrorTime("0");
                p.setVerticallyMirrorTime("0");
                p.setRotateTime("0");
                InputStream inputStream1 = ImgPythonCheckUtils.handleImgByInputStream(inputStream,
                        fileName1, stFile.getExt(), storageUploadProperties.getPythonDir(), JSONObject.toJSONString(p));
                int length = 0;
                // 上传拆分后的文件
                String fileName = stFile.getOriginalFilename();
                String newFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "_"
                        + a[StateConstants.ZERO] + ".png";
                SysFileDTO sysFileDTO = new SysFileDTO();
                // 判断该文件是否上传过,上传过则秒传
                byte[] bytes = FileUtils.read(inputStream1);
                String md5 = Md5Utils.calculateMD5(bytes);
                List<StFileDTO> stFiles = stFileService
                        .selectFileDTOByPO(new StFile().setSourceFileMd5(md5));
                if (CollectionUtils.isEmpty(stFiles)) {
                    // sysFileDTO = fileService.uploadFile(newFileName, null, bytes, mixedPastingSplitDT.getUserId());
                    String value = paramApi.searchValueByKey(StateConstants.FILE_ENCRYPT_TYPE)
                            .getData().getValue();
                    ByteArrayInputStream byteArrayInputStream1 = null;
                    if (StateConstants.IS_ENCRYPT.equals(mixedPastingSplitDto.getIsEncrypt())) {
                        try {
                            FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(
                                    new ByteArrayInputStream(bytes),
                                    Base64Utils.encodeBase64(storageUploadProperties.getEncryptKey()),
                                    StateConstants.FILE_ENCRYPT_TYPE_AES.equals(value) ? 0 : 1);
                            EncryptDTO dto = encryptService.encrypt(fileEncryptInfoDTO);
                            InputStream encrypt = dto.getInputStream();
                            length = dto.getLength();
                            byteArrayInputStream1 = new ByteArrayInputStream(
                                    IOUtils.toByteArray(encrypt));
                        } catch (Exception e) {
                            log.error("混合拆分加密失败：{}", e);
                            throw new RuntimeException("混合拆分加密失败", e);
                        }
                    } else {
                        byteArrayInputStream1 = new ByteArrayInputStream(bytes);
                    }
                    StFileDTO stFile1 = useS3Upload(newFileName, UUID.randomUUID().toString(),
                            mixedPastingSplitDto.getUserId(), byteArrayInputStream1,
                            stEquipment.getId(), mixedPastingSplitDto.getIsEncrypt(), value);
                    stFile1.setEncryptLen((long) length);
                    stFileList.add(stFile1);
                    BeanUtils.copyProperties(stFile1, sysFileDTO);
                } else {
                    stFileList.add(stFiles.get(StateConstants.ZERO).setEncryptLen((long) length));
                    BeanUtils.copyProperties(stFiles.get(StateConstants.ZERO), sysFileDTO);
                }
                sysFileDtoList.add(sysFileDTO);
                a[StateConstants.ZERO]++;

            }

        } else {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            byte[] byteArray = FileUtils.read(inputStream);
            List<Mat> matList = new ArrayList<>();
            mixedPastingSplitDto.getRegionList().forEach(p -> {
                // 根据坐标点，切割文件
                Mat m = Imgcodecs.imdecode(new MatOfByte(byteArray), Imgcodecs.IMREAD_COLOR);
                Mat mat = RectifyImageUtils.cutRect(m, p);
                matList.add(mat);
            });
            final Integer[] a = {StateConstants.COMMON_ONE};
            matList.forEach(p -> {
                int length = 0;
                // 保存的二进制数据
                MatOfByte b = new MatOfByte();
                // Mat转换成二进制数据。.png表示图片格式，格式不重要，基本不会对程序有任何影响。
                Imgcodecs.imencode(".png", p, b);
                // 二进制数据转换成Image
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(b.toArray());
                // 上传拆分后的文件
                String fileName = stFile.getOriginalFilename();
                String newFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "_"
                        + a[StateConstants.ZERO] + ".png";
                SysFileDTO sysFileDTO = new SysFileDTO();
                // 判断该文件是否上传过,上传过则秒传
                byte[] bytes = FileUtils.read(byteArrayInputStream);
                String md5 = Md5Utils.calculateMD5(bytes);
                List<StFileDTO> stFiles = stFileService
                        .selectFileDTOByPO(new StFile().setSourceFileMd5(md5));
                if (CollectionUtils.isEmpty(stFiles)) {
                    // sysFileDTO = fileService.uploadFile(newFileName, null, bytes, mixedPastingSplitDT.getUserId());
                    String value = paramApi.searchValueByKey(StateConstants.FILE_ENCRYPT_TYPE)
                            .getData().getValue();
                    ByteArrayInputStream byteArrayInputStream1 = null;
                    if (StateConstants.IS_ENCRYPT.equals(mixedPastingSplitDto.getIsEncrypt())) {
                        try {
                            FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(
                                    new ByteArrayInputStream(bytes),
                                    Base64Utils.encodeBase64(storageUploadProperties.getEncryptKey()),
                                    StateConstants.FILE_ENCRYPT_TYPE_AES.equals(value) ? 0 : 1);
                            EncryptDTO dto = encryptService.encrypt(fileEncryptInfoDTO);
                            InputStream encrypt = dto.getInputStream();
                            length = dto.getLength();
                            byteArrayInputStream1 = new ByteArrayInputStream(
                                    IOUtils.toByteArray(encrypt));
                        } catch (Exception e) {
                            log.error("混合拆分加密失败：{}", e);
                            throw new RuntimeException("混合拆分加密失败", e);
                        }
                    } else {
                        byteArrayInputStream1 = new ByteArrayInputStream(bytes);
                    }
                    StFileDTO stFile1 = useS3Upload(newFileName, UUID.randomUUID().toString(),
                            mixedPastingSplitDto.getUserId(), byteArrayInputStream1,
                            stEquipment.getId(), mixedPastingSplitDto.getIsEncrypt(), value);
                    stFile1.setEncryptLen((long) length);
                    stFileList.add(stFile1);
                    BeanUtils.copyProperties(stFile1, sysFileDTO);
                } else {
                    stFileList.add(stFiles.get(StateConstants.ZERO).setEncryptLen((long) length));
                    BeanUtils.copyProperties(stFiles.get(StateConstants.ZERO), sysFileDTO);
                }
                sysFileDtoList.add(sysFileDTO);
                a[StateConstants.ZERO]++;
            });
        }

        // 批量插入混贴拆分后的文件
        stFileService.insertBatch(stFileList);
        return sysFileDtoList;
    }

    /**
     * 从缓存中获取输入流
     *
     * @param request   请求头
     * @param response  响应头
     * @param fileId    文件id
     * @param userName  账号
     * @param userPhone 密码
     * @param instName  机构名
     * @param instPhone 机构电话
     */
    public void createInputStreamResourcesCacheApi(HttpServletRequest request,
                                                   HttpServletResponse response, Long fileId,
                                                   String userName, String userPhone,
                                                   String instName, String instPhone, String password) {
        StFileDTO stFile = stFileService.selectFileDTO(fileId);
        Assert.notNull(stFile, "文件信息不能为空!");
        stFile.setPassword(password);
        // 获取水印配置类型
        WaterMarkConfigVO waterMarkConfigVO = getWaterMarkConfig(0, userName, userPhone, instName,
                instPhone);
        // 处理文件和水印
        InputStream inputStream = getInputStream(fileId, request.getSession().getId(), stFile,
                waterMarkConfigVO);
        try {
            // 从输入流中读取数据并写入到输出流中
            ServletOutputStream servletOutputStream = response.getOutputStream();
            response.setHeader("Content-disposition",
                    "inline;filename=" + stFile.getOriginalFilename());
            response.setContentType("application/octet-stream");
            //客户端页面和文件设置缓存
            response.setHeader("Cache-Control", "public, max-age=604800");
            response.setHeader("Pragma", "cache");
            //            response.setHeader("Expires", "0");
            response.setHeader("Accept-Ranges", "bytes");
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // 写入实际读取的字节数
                servletOutputStream.write(buffer, 0, bytesRead);
            }
            servletOutputStream.flush();

            // 关闭输入流和输出流
            inputStream.close();
            servletOutputStream.close();
        } catch (IOException e) {
            log.error("关闭文件资源失败", e);
            throw new RuntimeException(e);
        }

    }

    /** -------------------------------------------------- 私有方法 -------------------------------------------------- */

    /**
     * 文件加密
     *
     * @param task 文件对象
     * @return StFile
     */
    @Async("GlobalThreadPool")
    public StFileDTO fileEncrypt(StFileDTO task) {
        FileStorage fileStorage = fileStorageService
                .getFileStorageVerify(String.valueOf(task.getEquipmentId()));
        splitUploadTaskService.fileEncrypt(task, fileStorage);
        return task;
    }

    /**
     * 获取水印内容
     *
     * @param checkList    水印配置内容
     * @param contentValue 自定义内容
     * @param token        token
     * @return Result
     */
    private String getMarkValue(List<String> checkList, String contentValue, AccountToken token) {
        SysUserDTO user = null;
        SysInstDTO inst = null;
        if (token.isOut()) {
            user = new SysUserDTO();
            user.setLoginName(token.getUsername());
            user.setName(token.getName());
            inst = new SysInstDTO();
            inst.setInstNo(token.getOrgCode());
            inst.setName(token.getOrgName());
        } else {
            Result<SysUserDTO> userResult = userApi.getUserDetail(token.getUsername());
            user = userResult.getData();
            Result<SysInstDTO> instResult = instApi.getInstByInstId(user.getInstId());
            inst = instResult.getData();
        }
        StringBuilder value = new StringBuilder();
        for (String checkItem : checkList) {
            if (StrUtil.isNotEmpty(user.getName()) && "username".equals(checkItem)) {
                value.append(user.getName());
            }
            if (StrUtil.isNotEmpty(user.getPhone()) && "tel".equals(checkItem)) {
                value.append(user.getPhone());
            }
            if (StrUtil.isNotEmpty(inst.getName()) && "company".equals(checkItem)) {
                value.append(inst.getName());
            }
            if ("sysDate".equals(checkItem)) {
                value.append(LocalDate.now());
            }
            if (StrUtil.isNotEmpty(contentValue) && "custom".equals(checkItem)) {
                value.append(contentValue);
            }
            value.append(" ");
        }
        return value.toString();
    }

    /**
     * 获取pdf文件路径
     *
     * @param fileId            文件id
     * @param stFile            文件对象
     * @param waterMarkConfigVO 水印对象
     * @param sessionId         sessionId
     * @return Result
     */
    private String getFile(Long fileId, StFileDTO stFile, WaterMarkConfigVO waterMarkConfigVO,
                           String sessionId) {
        String filePath = null;
        if (waterMarkConfigVO.openFlag) {
            // 加水印 则直接从缓存目录中获取水印文件
            StringBuilder fileNameSb = new StringBuilder()
                    .append(SunCacheDelConstants.DEL_SESSION_SUNYARD).append(fileId).append("-")
                    .append(sessionId).append(".pdf");
            filePath = new StringBuilder(storageUploadProperties.getSftpDirectory()).append(fileNameSb).toString();
        } else {
            // 没加水印 oss也会将OSS的文件缓存到本地
            StringBuilder stringBuilder = new StringBuilder().append(storageUploadProperties.getSftpDirectory()).append(fileId)
                    .append(".pdf");
            filePath = stringBuilder.toString();
        }
        File fileTemp = new File(filePath);
        if (!fileTemp.exists()) {
            // 如果缓存文件不存在 就直接从本地存储设备中获取
            filePath = stFile.getUrl();
        }
        return filePath;
    }

    /**
     * 上传文件 对编辑、拆分使用
     *
     * @param fileName2       文件名
     * @param busiBatchNo     业务批次号
     * @param userId          用户id
     * @param mergedPdfStream 文件流
     * @param stEquipmentId   存储设备id
     * @return Result
     */
    private StFileDTO useS3Upload(String fileName2, String busiBatchNo, Long userId,
                                  InputStream mergedPdfStream, Long stEquipmentId,
                                  Integer isEncrypt, String encryptType) {

        // 先将输入流内容缓存到字节数组中
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        try {
            while ((bytesRead = mergedPdfStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            // 关闭原始流
            mergedPdfStream.close();
        } catch (IOException e) {
            throw new RuntimeException("读取输入流失败", e);
        }
        // 上传
        StFileDTO stFile = new StFileDTO();
        // 根据上传的文件名获取文件后缀名，并使用工具类StrUtil和DateUtil生成唯一的文件名。
        String suffix = fileName2.substring(fileName2.lastIndexOf(".") + 1, fileName2.length());
        String fileName1 = IdUtil.randomUUID();
        String key = splitUploadTaskService.getPathKey(fileName1, suffix);
        UploadDTO uploadDTO = new UploadDTO();
        ByteArrayInputStream uploadStream = new ByteArrayInputStream(baos.toByteArray());
        uploadDTO.setInputStream(uploadStream);
        uploadDTO.setFileName(fileName2);
        try {
            uploadDTO.setFileSize(Long.parseLong(uploadStream.available() + ""));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        uploadDTO.setKey(key);
        uploadDTO.setFilePath(key);
        uploadDTO.setChunkSize(5 * 1024 * 1024L);
        uploadDTO.setIsEncrypt(isEncrypt);
        log.info("开始上传");
        FileStorage fileStorage = fileStorageService.getFileStorage(String.valueOf(stEquipmentId));
        try {
            stFile = fileStorage.upload(uploadDTO);
        } finally {
            try {
                uploadStream.close();
            } catch (IOException e) {
                log.error("关闭上传流失败", e);
            }
        }
        ByteArrayInputStream md5Stream = new ByteArrayInputStream(baos.toByteArray());
        try {
            String md5 = Md5Utils.calculateMD5(md5Stream);
            stFile.setObjectKey(key).setExt(suffix).setFilename(fileName1)
                    .setOriginalFilename(fileName2).setSize(uploadDTO.getFileSize())
                    .setCreateUser(userId)
                    // .setBusiBatchNo(busiBatchNo)
                    .setFileMd5(md5).setSourceFileMd5(md5).setEquipmentId(stEquipmentId)
                    .setId(snowflakeUtil.nextId()).setCreateTime(new Date())
                    // 设置是否加密、加密密钥、加密标识符
                    .setIsEncrypt(isEncrypt)
                    .setEncryptKey(StateConstants.IS_ENCRYPT.equals(isEncrypt)
                            ? (StateConstants.FILE_ENCRYPT_TYPE_AES.equals(encryptType)
                            ? Base64Utils.encodeBase64(storageUploadProperties.getEncryptKey())
                            : null)
                            : null)
                    .setEncryptType(StateConstants.IS_ENCRYPT.equals(isEncrypt)
                            ? (StateConstants.FILE_ENCRYPT_TYPE_AES.equals(encryptType) ? 0 : 1)
                            : null);
            addBusiBatchNoRedis(stFile.getId().toString());
        } catch (IOException e) {
            log.error("异常描述", e);
            throw new SunyardException(ResultCode.PARAM_ERROR, "文件上传失败");
        } catch (Exception e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        } finally {
            try {
                md5Stream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return stFile;
    }

    /**
     * 将所有文件转为pdf文件流
     *
     * @param fileIdList 文件id集
     * @return Result
     */
    private List<InputStream> getMergePdf(List<Long> fileIdList, String password) {
        List<InputStream> pdfStreams = new ArrayList<>();
        List<StFileDTO> stFileList = stFileService.selectFileDTOByIds1(fileIdList);
        AssertUtils.isNull(stFileList, "文件id有误");
        // 多个word、excel、ppt合并为一个pdf
        for (StFileDTO stFile : stFileList) {
            String fileExt = stFile.getExt();
            stFile.setPassword(password);
            // 获取对应的存储平台
            Long equipmentId = stFile.getEquipmentId();
            FileStorage fileStorage = fileStorageService
                    .getFileStorage(String.valueOf(equipmentId));
            InputStream fileInputStream;
            if (SunCacheUtils.IMGS.contains(fileExt.toLowerCase())) {
                // 图片转pdf
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                // 获取文件流
                InputStream inputStream = getFileInputStream(fileStorage, stFile.getObjectKey(),
                        stFile.getIsEncrypt(), stFile.getEncryptKey(), stFile.getEncryptType(),
                        stFile.getEncryptLen() == null ? 0 : stFile.getEncryptLen().intValue());
                InputStream inputStream1 = null;
                try {
                    ConvertPdfUtils.toImagePdf(inputStream, outputStream);
                } catch (Exception e) {
                    log.info("图片转pdf失败，修改图片格式从新转pdf", e);
                    // 特殊的图片格式转pdf时会出错，需要转png
                    if (SunCacheUtils.HEIC.contains(fileExt.toLowerCase())) {
                        try {
                            inputStream1 = ImageToPngUtils.specialImagesToJpg(inputStream);
                        }catch (Exception exception){
                            log.error("heic转流失败", exception);
                        }
                    } else {
                        inputStream1 = ImageToPngUtils.specialImagesToPng(inputStream);
                    }
                    AssertUtils.isNull(inputStream1, "参数错误");
                    ConvertPdfUtils.toImagePdf(inputStream1, outputStream);
                } finally {
                    try {
                        if (ObjectUtil.isNotEmpty(inputStream1)) {
                            inputStream1.close();
                        }
                        if (!ObjectUtils.isEmpty(inputStream)) {
                            inputStream.close();
                        } else {
                            throw new SunyardException("获取文件流为空");
                        }
                        outputStream.close();
                    } catch (IOException e) {
                        log.error("异常描述", e);
                        throw new RuntimeException(e);
                    }
                }
                fileInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            } else if (OnlineConstants.PDFLIST.contains(fileExt)) {
                fileInputStream = getFileInputStream(fileStorage, stFile.getObjectKey(),
                        stFile.getIsEncrypt(), stFile.getEncryptKey(), stFile.getEncryptType(),
                        stFile.getEncryptLen() == null ? 0 : stFile.getEncryptLen().intValue());
                //对于Pdf带文档加密的,这里需要先解密
                try {
                    fileInputStream = FileDecryptUtil.decryptPdf(fileInputStream, password);
                } catch (IOException e) {
                    log.error("pdf文档解密异常", e);
                    throw new RuntimeException(e);
                }
            } else {
                // 可以转pdf的文件转pdf
                boolean b = FileDealUtils.isPdf(fileExt.toLowerCase());
                if (!b) {
                    throw new SunyardException(ResultCode.PARAM_ERROR, "不允许" + fileExt + "类型的文件转换");
                }
                fileInputStream = getInputStream(stFile.getId(), null, stFile, null);
            }
            pdfStreams.add(fileInputStream);
        }
        return pdfStreams;
    }

    /**
     * 添加批次号进redis
     *
     * @param busiBatchNo 批次号
     */
    private void addBusiBatchNoRedis(String busiBatchNo) {
        // 创建一个 Date 对象，表示当前时间
        Date now = new Date();
        // 创建一个 SimpleDateFormat 对象，指定输出格式为 yyyy-MM-dd HH:mm
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        // 使用 SimpleDateFormat 对象将 Date 对象格式化为字符串
        String dateString = sdf.format(now);
        redisUtil.set(busiBatchNo, dateString, TimeOutConstants.ONE_HOURS);
    }

    /**
     * @param sessionId         sessionId
     * @param fileId            文件id
     * @param waterMarkConfigVO 水印对象
     * @param objectKey         key
     * @param fileExt           文件后缀
     * @param equipmentId       设备id
     * @param isEncrypt         是否加密
     * @param encryptKey        加密key
     * @param encryptType       加密标识
     * @return InputStream
     */
    private InputStream getFileWaterInputStream(String sessionId, Long fileId,
                                                WaterMarkConfigVO waterMarkConfigVO,
                                                String objectKey, String fileExt, Long equipmentId,
                                                Integer isEncrypt, String encryptKey,
                                                Integer encryptType, Integer length, String password) {
        // 根据sessionId和fileId 获取对应的水印图片
        Font font = new Font(waterMarkConfigVO.getWaterStyleFamilyValue(), Font.BOLD,
                Integer.parseInt(waterMarkConfigVO.getWaterStyleSizeValue()));
        Color color = Color.decode(waterMarkConfigVO.getColor1());
        InputStream inputStream = null;
        if (SunCacheUtils.IMGS.contains(fileExt) || SunCacheUtils.PDF.contains(fileExt)
                || SunCacheUtils.DOCS.contains(fileExt) || SunCacheUtils.OFD.contains(fileExt)
                || SunCacheUtils.TIFFLIST.contains(fileExt)) {
            log.info("添加水印");
            // 从缓存中获取水印文件
            String fileName = SunCacheDelConstants.DEL_SESSION_SUNYARD + fileId + "-" + sessionId;
            inputStream = cacheCommonService.getFileCache(fileName, fileExt.toLowerCase());
            try {
                if (inputStream == null || inputStream.available() == 0) {
                    log.info("缓存中找不到文件({}),从存储设备中取文件流并重新缓存水印文件", fileName);
                    // 因为getFileInputStream方法返回的是解密后的文件流 所以后续操作不需要解密
                    inputStream = cacheCommonService.cacheWaterFile(
                            getFileInputStream(String.valueOf(equipmentId), objectKey, isEncrypt,
                                    encryptKey, encryptType, length),
                            sessionId, fileId, fileExt, font, color, waterMarkConfigVO.getNum(),
                            waterMarkConfigVO.getMarkValue(), 0, encryptKey, encryptType, length, password);
                }
                if (password != null && !password.trim().isEmpty()) {
                    if (SunCacheUtils.PDF.contains(fileExt)) {
                        //处理PDF
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        PdfDocument pdf = new PdfDocument();
                        pdf.loadFromStream(inputStream, password);
                        pdf.decrypt();
                        pdf.saveToStream(outputStream);
                        pdf.close();
                        byte[] pdfBytes = outputStream.toByteArray();
                        return new ByteArrayInputStream(pdfBytes);
                    }
                }
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
        }
        return inputStream;
    }

    /**
     * 获取要下载的文件字节数组
     *
     * @param sessionId         sessionId
     * @param waterMarkConfigVO 水印对象
     * @param stFile            文件对象
     * @return Result
     */
    private InputStream getDownInputStream(String sessionId, WaterMarkConfigVO waterMarkConfigVO,
                                           StFileDTO stFile) {
        InputStream inputStream = null;
        try {
            // 加水印(pdf 图片格式直接从缓存获取带水印的输入流)
            String fileUrl = stFile.getUrl();
            String fileExt = stFile.getExt().toLowerCase();
            if (waterMarkConfigVO.getOpenFlag()) {
                // 根据sessionId和fileId 获取对应的水印图片
                log.info("加水印");
                Font font = new Font(waterMarkConfigVO.getWaterStyleFamilyValue(), Font.BOLD,
                        Integer.parseInt(waterMarkConfigVO.getWaterStyleSizeValue()));
                Color color = Color.decode(waterMarkConfigVO.getColor1());
                if (SunCacheUtils.IMGS.contains(fileExt) || SunCacheUtils.TIFFLIST.contains(fileExt)
                        || SunCacheUtils.PDF.contains(fileExt)
                        || SunCacheUtils.DOCX.contains(fileExt)
                        || SunCacheUtils.XLS.contains(fileExt)) {
                    try {
                        inputStream = getInputStreamWater(sessionId, waterMarkConfigVO, stFile,
                                fileExt, font, color);
                    } catch (OutOfMemoryError e) {
                        log.error("加水印失败,{}", e);
                        log.info("加水印失败直接获取存储设备中的文件流");
                        inputStream = getFileInputStream(String.valueOf(stFile.getEquipmentId()),
                                stFile.getObjectKey(), stFile.getIsEncrypt(),
                                stFile.getEncryptKey(), stFile.getEncryptType(),
                                stFile.getEncryptLen() == null ? 0
                                        : stFile.getEncryptLen().intValue());
                    }
                }
            }
            if (inputStream == null) {
                log.info("直接获取存储设备中的文件流");
                inputStream = getFileInputStream(String.valueOf(stFile.getEquipmentId()),
                        stFile.getObjectKey(), stFile.getIsEncrypt(), stFile.getEncryptKey(),
                        stFile.getEncryptType(),
                        stFile.getEncryptLen() == null ? 0 : stFile.getEncryptLen().intValue());
            }
        } catch (Exception e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
        return inputStream;
    }

    /**
     * 将文件封装到压缩包中得到压缩文件的文件流
     *
     * @param sessionId         sessionId
     * @param downFileVO        下载对象
     * @param waterMarkConfigVO 水印对象
     * @param zipPath           生成的压缩文件临时路径
     * @return Result
     */
    private InputStream getFileZipInputStream(String sessionId, DownFileVO downFileVO,
                                              WaterMarkConfigVO waterMarkConfigVO, String zipPath) {
        List<StFileDTO> stStFileList = stFileService.selectListByIdSourt(downFileVO.getFileId());
        AssertUtils.isNull(stStFileList, "文件不存在");
        InputStream inputStream = null;
        // 使用ZipOutputStream将字节数组输出流封装为压缩流，这样可以将文件逐个添加到压缩包中
        // 判断目录存储不存在 不存在则创建
        // 文件下载临时路径
        String path = storageUploadProperties.getFileDownTemp() + System.currentTimeMillis();
        try {
            // 创建一个计数器
            CountDownLatch latch = new CountDownLatch(stStFileList.size());
            for (StFileDTO stFile : stStFileList) {
                stFile.setPassword(downFileVO.getPassword());
                inputStream = null;
                String fileUrl = stFile.getUrl();
                String fileExt = stFile.getExt();
                if (waterMarkConfigVO.getOpenFlag()) {
                    log.info("开启水印了");
                    Font font = new Font(waterMarkConfigVO.getWaterStyleFamilyValue(), Font.BOLD,
                            Integer.parseInt(waterMarkConfigVO.getWaterStyleSizeValue()));
                    Color color = Color.decode(waterMarkConfigVO.getColor1());
                    if (SunCacheUtils.IMGS.contains(fileExt)
                            || SunCacheUtils.TIFFLIST.contains(fileExt)
                            || SunCacheUtils.PDF.contains(fileExt)
                            || SunCacheUtils.DOCX.contains(fileExt)
                            || SunCacheUtils.XLS.contains(fileExt)) {
                        // 无水印，重新添加水印存缓存
                        log.info("无水印，重新添加水印存缓存");
                        inputStream = getInputStreamWater(sessionId, waterMarkConfigVO, stFile,
                                fileExt, font, color);
                    }
                }
                // 不开启水印配置 或 不添加水印的文件直接获取流
                if (inputStream == null) {
                    log.info("不开启水印配置 或 不添加水印的文件直接获取流");
                    inputStream = getFileInputStream(String.valueOf(stFile.getEquipmentId()),
                            stFile.getObjectKey(), stFile.getIsEncrypt(), stFile.getEncryptKey(),
                            stFile.getEncryptType(),
                            stFile.getEncryptLen() == null ? 0 : stFile.getEncryptLen().intValue());
                }
                // 异步下载文件
                asyncDownloadFileById(inputStream, path, latch, stFile);
            }
            try {
                latch.await();
                // 所有线程都执行完毕，进行打包
                // 打包
                ZipUtils.apacheZip(path, zipPath);
                // 删除文件夹
                org.apache.tomcat.util.http.fileupload.FileUtils.deleteDirectory(new File(path));
                // 将压缩后的字节数组zipBytes赋值为outputStream的内容。
                try {
                    return new FileInputStream(zipPath);
                } catch (FileNotFoundException e) {
                    if (log.isErrorEnabled()) {
                        log.error("压缩文件不存在");
                    }
                }
            } catch (Exception e) {
                log.error("异常描述", e);
                throw new SunyardException("打压缩包出错!");
            }
        } finally {
            try {
                log.info("正在关闭文件流");
                if (null != inputStream) {
                    inputStream.close();
                    inputStream = null;
                }
            } catch (IOException e) {
                log.error("关闭文件流失败,{}", e);
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public InputStream getInputStreamWater(String sessionId, WaterMarkConfigVO waterMarkConfigVO,
                                           StFileDTO stFile, String fileExt, Font font,
                                           Color color) {
        InputStream downWaterFile = cacheCommonService.getDownWaterFile(sessionId, stFile.getId(),
                fileExt);
        if (downWaterFile == null) {
            downWaterFile = cacheCommonService
                    .getDownWaterFile(
                            getFileInputStream(String.valueOf(stFile.getEquipmentId()),
                                    stFile.getObjectKey(), stFile.getIsEncrypt(),
                                    stFile.getEncryptKey(), stFile.getEncryptType(),
                                    stFile.getEncryptLen() == null ? 0
                                            : stFile.getEncryptLen().intValue()),
                            sessionId, stFile.getId(), fileExt, font, color,
                            waterMarkConfigVO.getNum(), waterMarkConfigVO.getMarkValue(),
                            stFile.getIsEncrypt(), stFile.getEncryptKey(), stFile.getEncryptType(),
                            stFile.getEncryptLen() == null ? 0 : stFile.getEncryptLen().intValue(), stFile.getPassword());
        }
        return downWaterFile;
    }

    /**
     * 异步下载文件
     *
     * @param inputStream 输入流
     * @param path        地址
     * @param latch       CountDownLatch
     * @param stFile      文件对象
     */
    private void asyncDownloadFileById(InputStream inputStream, String path, CountDownLatch latch,
                                       StFileDTO stFile) {
        InputStream finalInputStream = inputStream;
        executor.execute(() -> {
            // 下载
            File file1 = new File(path);
            if (!file1.exists()) {
                file1.mkdirs();
            }
            // 存入指定磁盘
            File targetFile = new File(path, stFile.getOriginalFilename());
            long num = 1;
            // 判断文件是否已存在，如果存在则修改文件名
            while (targetFile.exists()) {
                // 文件名不包括后缀
                String name = stFile.getOriginalFilename().replaceFirst("[.][^.]+$", "");
                targetFile = new File(path, name + "(" + num + ")." + stFile.getExt());
                num++;
            }
            FileUtils.cpFile(finalInputStream, targetFile);
            latch.countDown();
        });
    }

    /**
     * 从存储设备中获取文件流并解密
     *
     * @param equipmentId 设备id
     * @param objectKey   key
     * @param isEncrypt   是否加密
     * @param encryptKey  加密key
     * @param encryptType 加密类型
     * @return Result
     */
    public InputStream getFileInputStream(String equipmentId, String objectKey, Integer isEncrypt,
                                          String encryptKey, Integer encryptType, Integer length) {
        // 获取对应的存储平台
        FileStorage fileStorage = fileStorageService.getFileStorage(equipmentId);
        // 从存储设备中获取文件流
        InputStream inputStream = fileStorage.getFileStream(objectKey);
        if (ObjectUtil.isNull(inputStream)) {
            log.info("从存储设备中获取文件流为空");
        }
        // 是否加密
        if (StateConstants.IS_ENCRYPT.equals(isEncrypt)) {
            // 解密
            FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(inputStream, encryptKey,
                    encryptType, length);
            inputStream = encryptService.decrypt(fileEncryptInfoDTO);
            if (ObjectUtil.isNull(inputStream)) {
                log.info("解密出的件流为空");
            }
        }
        return inputStream;
    }

    /**
     * 从存储设备中获取文件流并解密
     *
     * @param fileStorage 文件存储对象
     * @param objectKey   key
     * @param isEncrypt   是否加密
     * @param encryptKey  加密key
     * @param encryptType 加密标识
     * @return Result
     */
    private InputStream getFileInputStream(FileStorage fileStorage, String objectKey,
                                           Integer isEncrypt, String encryptKey,
                                           Integer encryptType, Integer length) {
        // 从存储设备中获取文件流
        InputStream inputStream = fileStorage.getFileStream(objectKey);
        if (ObjectUtil.isNull(inputStream)) {
            log.info("从存储设备中获取文件流为空");
        }
        // 是否加密
        if (StateConstants.IS_ENCRYPT.equals(isEncrypt)) {
            // 解密
            FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(inputStream, encryptKey,
                    encryptType, length);
            inputStream = encryptService.decrypt(fileEncryptInfoDTO);
            if (ObjectUtil.isNull(inputStream)) {
                log.info("解密出的件流为空");
            }
        }
        return inputStream;
    }

    /**
     * 获取水印配置
     *
     * @param type      类型
     * @param userName  用户名
     * @param userPhone 用户电话
     * @param instName  机构名
     * @param instPhone 机构电话
     * @return WaterMarkConfigVO
     */
    private WaterMarkConfigVO getWaterMarkConfig(Integer type, String userName, String userPhone,
                                                 String instName, String instPhone) {
        WaterMarkConfigVO waterMarkConfigVO = new WaterMarkConfigVO();
        Result<SysParamDTO> sysParamDtoResult = paramApi
                .searchValueByKey(StateConstants.WATERMARK_PARAM_FILE);
        SysParamDTO sysParamDTO = sysParamDtoResult.getData();
        if (ObjectUtil.isNull(sysParamDTO)) {
            waterMarkConfigVO.setOpenFlag(false);
            return waterMarkConfigVO;
        }

        JSONObject jsonObject = JSONObject.parseObject(sysParamDTO.getValue());
        waterMarkConfigVO = JSONObject.toJavaObject(jsonObject, WaterMarkConfigVO.class);
        // 获取水印内容
        String markValue = FileDealUtils.getMarkValue(waterMarkConfigVO.getCheckList(),
                waterMarkConfigVO.getContentValue(), userName, userPhone, instName, instPhone);
        waterMarkConfigVO.setMarkValue(markValue);
        // 对应操作类型下是否开启水印 查看 打印 下载
        if (type == null) {
            // 下载
            waterMarkConfigVO.setOpenFlag(waterMarkConfigVO.getSwitchValueDownload());
        } else {
            if (type == 1) {
                // 打印
                waterMarkConfigVO.setOpenFlag(waterMarkConfigVO.getSwitchValuePrint());
            } else {
                // 查看
                waterMarkConfigVO.setOpenFlag(waterMarkConfigVO.getSwitchValueShow());
            }
        }

        return waterMarkConfigVO;
    }

    /**
     * 在线查看(无编辑权限)
     *
     * @param fileId 文件id
     * @return 在线编辑
     */
    public Result documentView(Long fileId, String password, AccountToken token, Integer type) {
        AssertUtils.isTrue(fileId == null, "参数错误!");
        StFileDTO stFileDTO = stFileService.selectFileDTO(fileId);
        AssertUtils.isNull(stFileDTO, "参数错误:文件不存在");
        //校验文件大小
        if (stFileDTO.getSize() > storageOnlyOfficeProperties.getMaxFileSize()) {
            throw new RuntimeException("文件超过【" + storageOnlyOfficeProperties.getMaxFileSize() / 1024 / 1024 + "MB】无法打开");
        }
//        Long userId = token.getId();
        String name = token.getName();
        String username = token.getUsername();
        String orgCode = token.getOrgCode();
        String orgName = token.getOrgName();
        String mapping = "/web-api/storage";
        if (StateConstants.COMMON_ONE.equals(type)) {
            mapping = mapping + "/api";
        }
        //参数拼接
        StringBuilder paramsBuilder = new StringBuilder();
        paramsBuilder.append("fileId=").append(fileId)
                .append("&username=").append(username)
                .append("&name=").append(name)
                .append("&orgCode=").append(orgCode)
                .append("&orgName=").append(orgName);
        if (StringUtils.hasText(password)) {
            paramsBuilder.append("&password=").append(password);
        }
        String params = paramsBuilder.toString();
        //检验url是否可访问
        validateFileUrl(params);
        String url = storageOnlyOfficeProperties.getStorageUrl() + mapping
                + storageOnlyOfficeProperties.getStorageMapping() + params;
        Result<Map> mapResult = onlyOfficeUtil.openDocument(stFileDTO.getId().toString(),
                stFileDTO.getOriginalFilename(), stFileDTO.getExt(), stFileDTO.getSize().toString(),
                url, username, name, "view", false);
        Map data = mapResult.getData();
        String s = JSON.toJSONString(data);
        return Result.success(s);
    }

    /**
     * 校验文件URL是否有效
     *
     * @param params 文件接口参数
     */
    private void validateFileUrl(String params) {
        String url = storageOnlyOfficeProperties.getCheckFileUrl() + params;
        //初始化HttpUtils
        HttpUtils httpUtils = HttpUtils.init();
        //执行GET请求，获取响应结果（状态码+响应内容）
        Map<String, String> resultMap = httpUtils.get(url);
        String statusCode = resultMap.get("statusCode");
        if (!StringUtils.hasText(statusCode)) {
            throw new RuntimeException("获取文件失败");
        }

        //校验状态码：仅200表示接口成功（返回文件流）
        if (!"200".equals(statusCode)) {
            throw new RuntimeException("获取文件失败");
        }

        //若接口返回200但实际是JSON错误（非文件流），判定无效
        String result = resultMap.get("result");
        if (StringUtils.hasText(result) && (result.contains("code") && result.contains("msg"))) {
            if (result.contains("密码错误")) {
                throw new RuntimeException("密码错误");
            }
            throw new RuntimeException("获取文件失败");
        }

    }


    /**
     * 获取onlyOffice开关
     *
     * @return 在线编辑
     */
    public Result getOnlyOfficeEnable() {
        Map<String, Boolean> map = new HashMap();
        map.put("useOnlyOffice", storageOnlyOfficeProperties.getUseOnlyOffice());
        return Result.success(map);
    }

    /**
     * ODF拆分功能
     *
     * @param ecmSplitPdfVo pdf拆分实体类
     * @param token
     * @return
     */
    public HashMap<String, Object> splitPdfFile(FileSplitPdfVO ecmSplitPdfVo, AccountToken token) {
        HashMap<String, Object> returnMap = new HashMap<>();
        //存储磁盘地址
        String directory = storageUploadProperties.getPdfSplitDirectory();
        //磁盘是否有图片
        boolean flag = false;
        //pdf总页数
        int totalPage = 0;
        try {
            StFileDTO stFile = stFileService
                    .selectFileDTO(Long.valueOf(ecmSplitPdfVo.getNewFileId()));
            String redisKey = CachePrefixConstants.SPLIT_PDF_FILE + stFile.getFileMd5();
            String lockRedisKey = CachePrefixConstants.LOCK_SPLIT_PDF_FILE + stFile.getFileMd5();
            //获取缓存
            totalPage = getRedisCache(stFile, returnMap);
            //判断请求文件是否全部加载
            flag = areAllImagesExist(directory, ecmSplitPdfVo.getFileMd5(), Math.min(ecmSplitPdfVo.getSplitPageSize() * ecmSplitPdfVo.getSplitPageNum() + ecmSplitPdfVo.getSplitPageSize(), totalPage));
            if (!returnMap.isEmpty() && returnMap.containsKey("total") && flag) {
                returnMap.put("flag", flag);
                return returnMap;
            }
            // 获取文件流
            long t11 = System.currentTimeMillis();
            AssertUtils.isNull(stFile, "文件不存在");
            AssertUtils.isNull(stFile.getEquipmentId(), "参数错误");
            String ext = stFile.getExt().toLowerCase();
            AssertUtils.isTrue(!SunCacheUtils.PDF.contains(ext), "只能拆分" + SunCacheUtils.PDF);
            InputStream inputStream;
            //获取文件流
            // 获取水印配置
            WaterMarkConfigVO waterMarkConfigVO = getWaterMarkConfig(ImgsConstants.WATERMARK_TYPE_SHOW,
                    token);
            inputStream = getInputStream(Long.valueOf(ecmSplitPdfVo.getNewFileId()), null, stFile, waterMarkConfigVO);

            byte[] pdfBytes = IOUtils.toByteArray(inputStream);
            long t22 = System.currentTimeMillis();
            log.info("获取文件流的操作耗时：{}(毫秒)", t22 - t11);
            // 获取文件总页数
            if (returnMap.isEmpty() && !returnMap.containsKey("total")) {
                //文件总数
                totalPage = SplitPdfUtils.getPdfPages(pdfBytes);
                //存缓存
                redisUtil.hset(CachePrefixConstants.SPLIT_PDF_FILE_TOTAL, stFile.getFileMd5(), totalPage, TimeOutConstants.SEVEN_DAY);
                returnMap.put("total", totalPage);
            }
            totalPage = (Integer) returnMap.get("total");
            if (ecmSplitPdfVo.isFileTooLarge()) {
                //超过阈值了，起一个线程去拆
                splitUploadTaskService.asyncSplitPdfToImagesBySync(ecmSplitPdfVo, pdfBytes, totalPage, lockRedisKey, directory);
            } else {
                //没有超过文件大小阈值,执行之前的逻辑多线程拆分
                //查缓存
                long t1 = System.currentTimeMillis();
                SplitPdfUtils.convertPdfToImagesParallel(pdfBytes,
                        ecmSplitPdfVo.getSplitPageSize(), ecmSplitPdfVo.getSplitPageNum(), totalPage, ecmSplitPdfVo.getFileMd5(), directory);
                log.info("pdf拆分成图片耗时：{}(毫秒)", System.currentTimeMillis() - t1);
                //开启异步任务处理
                splitUploadTaskService.asyncSplitPdfToImages(ecmSplitPdfVo, pdfBytes, redisKey, totalPage, lockRedisKey, directory);
                long t2 = System.currentTimeMillis();
                log.info("获取文件base64信息耗时：{}(毫秒)", t2 - t1);
            }
        } catch (Exception e) {
            log.error("系统异常",e);
        }
        flag = areAllImagesExist(directory, ecmSplitPdfVo.getFileMd5(), Math.min(ecmSplitPdfVo.getSplitPageSize() * ecmSplitPdfVo.getSplitPageNum() + ecmSplitPdfVo.getSplitPageSize(), totalPage));
        returnMap.put("flag", flag);
        return returnMap;
    }

    private int getRedisCache(StFileDTO stFile, HashMap<String, Object> returnMap) {
        int totalNum = 0;
        try {
            //查缓存
            //总数
            Object total = redisUtil.hget(CachePrefixConstants.SPLIT_PDF_FILE_TOTAL, stFile.getFileMd5());
            if (ObjectUtil.isNotEmpty(total)) {
                totalNum = (Integer) total;
                returnMap.put("total", total);
            }
        } catch (Exception e) {
            log.error("获取缓存失败", e);
        }
        return totalNum;
    }

    public boolean areAllImagesExist(String prefix, String baseFilename, int pageCount) {
        String normalizedPrefix = prefix.endsWith(File.separator) ? prefix : prefix + File.separator;

        return IntStream.range(0, pageCount)
                .allMatch(page -> {
                    String filename = baseFilename + "_" + String.format("%d", page) + ".png";
                    File file = new File(normalizedPrefix + filename);
                    return file.exists();
                });
    }

    public void storeRedis(ArrayList<String> base64List, String pdfName, String redisKey,
                           Integer startPage) {
        Map<String, Object> redisHash = Stream.iterate(0, i -> i + 1).limit(base64List.size())
                .collect(Collectors.toMap(i -> pdfName + "_" + (startPage + i), base64List::get));
        redisUtil.hmset(redisKey, redisHash, TimeOutConstants.ONE_DAY);
    }

    public List<String> getAllSplitPdfImage(FileSplitPdfVO splitFileVO, String redisKey,
                                            Integer startPage, Integer endPage) {
        List<String> list = new ArrayList<>();
        if (isSplitPdfImageExist(splitFileVO, redisKey, startPage, endPage)) {
            List<String> fields = IntStream
                    .range(startPage, endPage)
                    .mapToObj(i -> splitFileVO.getFilename() + "_" + (startPage + i))
                    .collect(Collectors.toList());

            list = (List<String>) redisUtil.hget1(redisKey, fields);
        }
        return list;
    }

    public boolean isSplitPdfImageExist(FileSplitPdfVO splitFileVO, String redisKey,
                                        Integer startPage, Integer endPage) {
        if (redisUtil.hasKey(redisKey)) {
            boolean existFlag = false;
            List<String> fields = IntStream
                    .range(startPage, endPage)
                    .mapToObj(i -> splitFileVO.getFilename() + "_" + (startPage + i))
                    .collect(Collectors.toList());
            List<Object> splitPdfImages = (List<Object>) redisUtil.hget1(redisKey, fields);
            if (!CollectionUtils.isEmpty(splitPdfImages) && splitPdfImages.size() == fields.size()) {
                existFlag = splitPdfImages.stream().noneMatch(Objects::isNull);
            }
            return existFlag;
        }
        return false;
    }

    public ResponseEntity<FileSystemResource> batchDownloadZip(List<DownloadFileZip> downloadFileList, HttpServletRequest request) {
        // 线程安全的日期格式化器（避免多线程问题）
        String sessionId = request.getSession().getId();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String currentDate = dateFormat.format(new Date());

        // 1. 生成临时根目录（确保后续打包的层级正确）
        String tempRootPath = storageUploadProperties.getFileDownTemp() + System.currentTimeMillis();
        File tempRootDir = new File(tempRootPath);
        if (!tempRootDir.exists() && !tempRootDir.mkdirs()) {
            throw new SunyardException("临时目录创建失败：" + tempRootPath);
        }

        File zipFile = null;
        FileInputStream zipInputStream = null;

        try {
            if (downloadFileList.isEmpty()) {
                throw new SunyardException("无待下载文件");
            }

            // 获取第一个文件的token信息（水印配置用）
            DownloadFileZip firstFile = downloadFileList.get(0);
            AccountToken token = new AccountToken();
            token.setName(firstFile.getName());
            token.setUsername(firstFile.getUsername());
            token.setOrgCode(firstFile.getOrgCode());
            token.setOrgName(firstFile.getOrgName());
            WaterMarkConfigVO waterMarkConfigVO = getWaterMarkConfig(ImgsConstants.WATERMARK_TYPE_DOWNLOAD, token);

            // 收集所有fileId及文件目录关系
            Map<Long, EcmFileBelong> fileBelongMap = new HashMap<>();
            List<Long> allFileIds = new ArrayList<>();
            for (DownloadFileZip ecmDownload : downloadFileList) {
                String dirsFirst = ecmDownload.getDirsFirst();
                AssertUtils.isNull(dirsFirst, "一级目录名不能为空");

                List<DocFileZip> docFileList = ecmDownload.getDocFileList();
                AssertUtils.isNull(docFileList, "需要下载的文件列表不能为空");

                for (DocFileZip docFile : docFileList) {
                    String dirsSecond = docFile.getDirsSecond();
                    AssertUtils.isNull(dirsSecond, "二级目录点名称不能为空");
                    List<Long> fileIds = docFile.getFileIds();
                    AssertUtils.isNull(fileIds, "文件ID列表不能为空");

                    for (Long fileId : fileIds) {
                        allFileIds.add(fileId);
                        fileBelongMap.put(fileId, new EcmFileBelong(dirsFirst, dirsSecond));
                    }
                }
            }

            if (allFileIds.isEmpty()) {
                throw new SunyardException("无待下载文件");
            }

            // 批量查询文件信息
            List<StFileDTO> allStFileList = stFileService.selectListByIdSourt(allFileIds);
            AssertUtils.isNull(allStFileList, "未查询到任何文件信息");

            Map<Long, StFileDTO> fileIdToStFileMap = allStFileList.stream()
                    .collect(Collectors.toMap(StFileDTO::getId, stFile -> stFile, (a, b) -> b));

            // 遍历业务下载请求，创建目录并下载文件
            for (DownloadFileZip ecmDownload : downloadFileList) {
                String dirsFirst = ecmDownload.getDirsFirst();
                // 第一级目录
                File busiDir = new File(tempRootDir, dirsFirst);
                if (!busiDir.exists() && !busiDir.mkdirs()) {
                    throw new SunyardException("业务文件夹创建失败：" + busiDir.getPath());
                }

                // 遍历所有的二级目录
                List<DocFileZip> docFileList = ecmDownload.getDocFileList();
                for (DocFileZip docFile : docFileList) {
                    String dirsSecond = docFile.getDirsSecond();
                    List<Long> fileIds = docFile.getFileIds();

                    // 第二级目录
                    File docDir = new File(busiDir, dirsSecond);
                    if (!docDir.exists() && !docDir.mkdirs()) {
                        throw new SunyardException("第二级文件夹创建失败：" + docDir.getPath());
                    }

                    // 异步下载当前二级目录下的所有文件
                    CountDownLatch latch = new CountDownLatch(fileIds.size());
                    for (Long fileId : fileIds) {
                        executor.execute(() -> {
                            try (InputStream fileInputStream = getFileInputStream(sessionId, fileId, fileIdToStFileMap, waterMarkConfigVO)) {
                                if (fileInputStream == null) {
                                    log.warn("文件处理失败，未获取到有效流，fileId={}", fileId);
                                    return;
                                }

                                StFileDTO stFile = fileIdToStFileMap.get(fileId);
                                String fileName = buildUniqueFileName(stFile, fileId);
                                // 文件直接放在二级目录下（第三层：文件本身）
                                File targetFile = new File(docDir, fileName);

                                try (OutputStream out = new FileOutputStream(targetFile)) {
                                    byte[] buffer = new byte[1024 * 8];
                                    int len;
                                    while ((len = fileInputStream.read(buffer)) != -1) {
                                        out.write(buffer, 0, len);
                                    }
                                    log.info("文件下载完成：{}", targetFile.getPath());
                                }
                            } catch (Exception e) {
                                log.error("文件处理异常，fileId={}", fileId, e);
                                throw new RuntimeException("文件处理异常", e);
                            } finally {
                                latch.countDown();
                            }
                        });
                    }

                    // 等待当前资料类型所有文件下载完成
                    try {
                        latch.await();
                        log.info("二级目录【{}】下所有文件下载完成", docFile.getDirsSecond());
                    } catch (InterruptedException e) {
                        log.error("等待文件下载线程中断", e);
                        Thread.currentThread().interrupt();
                        throw new SunyardException("文件下载中断");
                    }
                }
            }

            // 2. 打包
            String zipFileName = currentDate + ".zip";
            String zipFilePath = storageUploadProperties.getFileDownTemp() + File.separator + zipFileName;
            // 打包临时根目录（临时根目录下的所有内容直接作为压缩包的根内容）
            ZipUtils.apacheZipCompress(tempRootPath, zipFilePath);
            log.info("压缩包生成完成：{}，层级结构：{} → 一级目录 → 二级目录 → 文件",
                    zipFilePath, zipFileName);

            // 3. 封装文件资源（关键修复：使用FileSystemResource而非直接返回InputStream）
            zipFile = new File(zipFilePath);
            if (!zipFile.exists()) {
                throw new SunyardException("压缩包生成失败，文件不存在：" + zipFilePath);
            }

            // 构建带自动清理的资源（通过Resource的getInputStream()关闭时触发清理）
            File finalZipFile = zipFile;
            FileSystemResource zipResource = new FileSystemResource(finalZipFile) {
                @Override
                public InputStream getInputStream() throws IOException {
                    return new FileInputStream(finalZipFile) {
                        @Override
                        public void close() throws IOException {
                            super.close();
                            // 流关闭后清理临时文件和目录
                            cleanResources(null, finalZipFile, tempRootDir);
                        }
                    };
                }
            };

            // 4. 设置响应头（关键修复：指定Content-Type和下载文件名）
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"")
                    .body(zipResource);

        } catch (Exception e) {
            log.error("批量下载打包异常", e);
            // 异常时主动清理资源
            cleanResources(zipInputStream, zipFile, tempRootDir);
            throw new SunyardException("批量下载失败：" + e.getMessage());
        }
    }

    /**
     * 主动清理资源（流、文件、目录）
     */
    private void cleanResources(InputStream inputStream, File zipFile, File tempRootDir) {
        // 关闭流
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("异常时关闭流失败", e);
            }
        }
        // 删除ZIP文件
        if (zipFile != null && zipFile.exists()) {
            boolean zipDeleted = zipFile.delete();
            log.info("ZIP文件删除结果：{}", zipDeleted);
        }
        // 删除临时根目录
        try {

            if (null != tempRootDir && tempRootDir.exists()) {
                FileUtils.deleteDirectory(tempRootDir.getAbsolutePath());
                log.info("临时根目录清理完成：{}", tempRootDir.getAbsolutePath());
            }

        } catch (Exception e) {
            log.error("临时根目录清理失败", e);
        }
    }

    /**
     * 构建唯一文件名（避免同目录下文件重复）
     */
    private String buildUniqueFileName(StFileDTO stFile, Long fileId) {
        String originalFileName = stFile.getOriginalFilename();
        String fileExt = stFile.getExt();

        if (StringUtils.isEmpty(originalFileName)) {
            return "file_" + fileId + "." + fileExt;
        }

        int lastDotIndex = originalFileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            // 原文件名无后缀，补充文件真实后缀
            return originalFileName + "_" + fileId + "." + fileExt;
        }

        // 原文件名有后缀，在文件名后加fileId避免重复
        String nameWithoutExt = originalFileName.substring(0, lastDotIndex);
        String ext = originalFileName.substring(lastDotIndex);
        return nameWithoutExt + "_" + fileId + ext;
    }

    /**
     * 获取文件输入流（含水印处理，失败降级）
     */
    private InputStream getFileInputStream(String sessionId, Long fileId, Map<Long, StFileDTO> fileIdToStFileMap, WaterMarkConfigVO waterMarkConfigVO) {
        StFileDTO stFile = fileIdToStFileMap.get(fileId);
        if (stFile == null) {
            log.warn("文件不存在，fileId={}", fileId);
            return null;
        }

        String fileExt = stFile.getExt();
        if (waterMarkConfigVO.getOpenFlag()) {
            log.info("开启水印处理，fileId={}", fileId);
            if (SunCacheUtils.IMGS.contains(fileExt)
                    || SunCacheUtils.TIFFLIST.contains(fileExt)
                    || SunCacheUtils.PDF.contains(fileExt)
                    || SunCacheUtils.DOCX.contains(fileExt)
                    || SunCacheUtils.XLS.contains(fileExt)) {
                try {
                    Font font = new Font(waterMarkConfigVO.getWaterStyleFamilyValue(), Font.BOLD,
                            Integer.parseInt(waterMarkConfigVO.getWaterStyleSizeValue()));
                    Color color = Color.decode(waterMarkConfigVO.getColor1());
                    return getInputStreamWater(sessionId, waterMarkConfigVO, stFile,
                            fileExt, font, color);
                } catch (Exception e) {
                    log.error("水印处理失败，降级获取原文件流，fileId={}", fileId, e);
                }
            }
        }

        log.info("不开启水印或非水印支持文件类型，直接获取文件流，fileId={}", fileId);
        try {
            return getFileInputStream(
                    String.valueOf(stFile.getEquipmentId()),
                    stFile.getObjectKey(),
                    stFile.getIsEncrypt(),
                    stFile.getEncryptKey(),
                    stFile.getEncryptType(),
                    stFile.getEncryptLen() == null ? 0 : stFile.getEncryptLen().intValue()
            );
        } catch (Exception e) {
            log.error("获取文件流失败，fileId={}", fileId, e);
            return null;
        }
    }

    /**
     * pdf批注文件获取
     *
     * @param response
     * @param fileName    文件名称
     * @param requestPage 请求页数
     * @param fileMd5     文件md5
     */

    public void getPdfSplitFiles(HttpServletResponse response,
                                 String fileName, String requestPage, String fileMd5) {
        AssertUtils.isNull(fileName, "文件名称不能为空");
        AssertUtils.isNull(requestPage, "请求文件页数不能为空");
        AssertUtils.isNull(fileMd5, "文件MD5不能为空");
        //存储磁盘地址
        String directory = storageUploadProperties.getPdfSplitDirectory();
        //存储到磁盘的拆分文件名
        String finalFileName = fileMd5 + "_" + requestPage + ".png";
        try {
            // 创建文件
            File file = new File(directory, finalFileName);
            InputStream inputStream = new FileInputStream(file);
            // 文件下载
            FileDealUtils.getResponseByInputStream(response, finalFileName, null, inputStream);
        } catch (Exception e) {
            log.error("获取文件流失败，fileName={}", fileName, e);
        }
    }

    public Result startBatchZip(List<DownloadFileZip> downloadFileList, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        String redisKey = CachePrefixConstants.MERGE_ZIP + sessionId;

        // 1. 检查是否已有任务在跑
        if (redisUtil.hasKey(redisKey)) {
            return Result.error("已有下载合并任务在进行中，请稍后再试", 500);
        }

        int totalFileCount = calculateTotalFileCount(downloadFileList);
        if (totalFileCount == 0) {
            return Result.error("待下载文件列表为空", 500);
        }

        // 2. 生成压缩包路径
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String zipFileName = currentDate + UUID.randomUUID() + ".zip";
        String zipFilePath = storageUploadProperties.getFileDownTemp() + File.separator + zipFileName;

        // 3. 写入 Redis 标记任务开始
        redisUtil.set(redisKey, "RUNNING", TimeOutConstants.THIRTY_MINUTE);
        String totalCountKey = CachePrefixConstants.MERGE_ZIP_TOTAL_COUNT + sessionId;
        String finishedCountKey = CachePrefixConstants.MERGE_ZIP_FINISHED_COUNT + sessionId;
        redisUtil.set(totalCountKey, String.valueOf(totalFileCount), TimeOutConstants.ONE_HOURS);
        redisUtil.set(finishedCountKey, "0", TimeOutConstants.ONE_HOURS);

        // 4. 异步执行压缩任务
        executor.execute(() -> {
            String tempRootPath = null;
            try {
                tempRootPath = storageUploadProperties.getFileDownTemp() + System.currentTimeMillis();
                File tempRootDir = new File(tempRootPath);
                if (!tempRootDir.exists() && !tempRootDir.mkdirs()) {
                    throw new SunyardException("临时目录创建失败：" + tempRootPath);
                }

                batchDownloadZipInternal(downloadFileList, request, tempRootPath, zipFilePath);
            } catch (Exception e) {
                log.error("异步压缩任务失败", e);
            } finally {
                // 5. 任务结束后清除 Redis Key
                redisUtil.del(redisKey);
//                redisUtil.del(totalCountKey);
//                redisUtil.del(finishedCountKey);
                // 清理临时目录
                if (tempRootPath != null) {
                    FileUtils.deleteDirectory(tempRootPath);
                }
            }
        });
        //把路径放在缓存,1小时内可以下载
        redisUtil.set(CachePrefixConstants.ZIP_PATH + sessionId, zipFilePath, TimeOutConstants.ONE_HOURS);
        return Result.success(sessionId);
    }

    /**
     * 批量下载文件并压缩（内部方法，供异步任务调用）
     */
    private void batchDownloadZipInternal(List<DownloadFileZip> downloadFileList,
                                          HttpServletRequest request,
                                          String tempRootPath,
                                          String zipFilePath) throws Exception {

        String sessionId = request.getSession().getId();

        // 检查输入参数
        if (downloadFileList == null || downloadFileList.isEmpty()) {
            throw new SunyardException("无待下载文件");
        }

        // 获取第一个文件的token信息（水印配置用）
        DownloadFileZip firstFile = downloadFileList.get(0);
        AccountToken token = new AccountToken();
        token.setName(firstFile.getName());
        token.setUsername(firstFile.getUsername());
        token.setOrgCode(firstFile.getOrgCode());
        token.setOrgName(firstFile.getOrgName());
        WaterMarkConfigVO waterMarkConfigVO = getWaterMarkConfig(ImgsConstants.WATERMARK_TYPE_DOWNLOAD, token);

        // 收集所有 fileId 及文件目录关系
        Map<Long, EcmFileBelong> fileBelongMap = new HashMap<>();
        List<Long> allFileIds = new ArrayList<>();
        for (DownloadFileZip ecmDownload : downloadFileList) {
            String dirsFirst = ecmDownload.getDirsFirst();
            AssertUtils.isNull(dirsFirst, "一级目录名不能为空");

            List<DocFileZip> docFileList = ecmDownload.getDocFileList();
            AssertUtils.isNull(docFileList, "需要下载的文件列表不能为空");

            for (DocFileZip docFile : docFileList) {
                String dirsSecond = docFile.getDirsSecond();
                AssertUtils.isNull(dirsSecond, "二级目录点名称不能为空");
                List<Long> fileIds = docFile.getFileIds();
                AssertUtils.isNull(fileIds, "文件ID列表不能为空");

                for (Long fileId : fileIds) {
                    allFileIds.add(fileId);
                    fileBelongMap.put(fileId, new EcmFileBelong(dirsFirst, dirsSecond));
                }
            }
        }

        if (allFileIds.isEmpty()) {
            throw new SunyardException("无待下载文件");
        }

        // 批量查询文件信息
        List<StFileDTO> allStFileList = stFileService.selectListByIdSourt(allFileIds);
        AssertUtils.isNull(allStFileList, "未查询到任何文件信息");

        Map<Long, StFileDTO> fileIdToStFileMap = allStFileList.stream()
                .collect(Collectors.toMap(StFileDTO::getId, stFile -> stFile, (a, b) -> b));

        // 遍历业务下载请求，创建目录并下载文件
        File tempRootDir = new File(tempRootPath);
        if (!tempRootDir.exists() && !tempRootDir.mkdirs()) {
            throw new SunyardException("临时目录创建失败：" + tempRootPath);
        }

        for (DownloadFileZip ecmDownload : downloadFileList) {
            String dirsFirst = ecmDownload.getDirsFirst();
            // 第一级目录
            File busiDir = new File(tempRootDir, dirsFirst);
            if (!busiDir.exists() && !busiDir.mkdirs()) {
                throw new SunyardException("业务文件夹创建失败：" + busiDir.getPath());
            }

            // 遍历所有的二级目录
            List<DocFileZip> docFileList = ecmDownload.getDocFileList();
            for (DocFileZip docFile : docFileList) {
                String dirsSecond = docFile.getDirsSecond();
                List<Long> fileIds = docFile.getFileIds();

                // 第二级目录
                File docDir = new File(busiDir, dirsSecond);
                if (!docDir.exists() && !docDir.mkdirs()) {
                    throw new SunyardException("第二级文件夹创建失败：" + docDir.getPath());
                }

                // 异步下载当前二级目录下的所有文件
                CountDownLatch latch = new CountDownLatch(fileIds.size());
                for (Long fileId : fileIds) {
                    executor.execute(() -> {
                        try (InputStream fileInputStream = getFileInputStream(sessionId, fileId, fileIdToStFileMap, waterMarkConfigVO)) {
                            if (fileInputStream == null) {
                                log.warn("文件处理失败，未获取到有效流，fileId={}", fileId);
                                return;
                            }

                            StFileDTO stFile = fileIdToStFileMap.get(fileId);
                            String fileName = buildUniqueFileName(stFile, fileId);
                            // 文件直接放在二级目录下（第三层：文件本身）
                            File targetFile = new File(docDir, fileName);

                            try (OutputStream out = new FileOutputStream(targetFile)) {
                                byte[] buffer = new byte[1024 * 8];
                                int len;
                                while ((len = fileInputStream.read(buffer)) != -1) {
                                    out.write(buffer, 0, len);
                                }
                                String finishedCountKey = CachePrefixConstants.MERGE_ZIP_FINISHED_COUNT + sessionId;
                                redisUtil.incr(finishedCountKey);
                                log.info("文件下载完成：{}", targetFile.getPath());
                            }
                        } catch (Exception e) {
                            log.error("文件处理异常，fileId={}", fileId, e);
                            throw new RuntimeException("文件处理异常", e);
                        } finally {
                            latch.countDown();
                        }
                    });
                }

                // 等待当前资料类型所有文件下载完成
                try {
                    latch.await();
                    log.info("二级目录【{}】下所有文件下载完成", docFile.getDirsSecond());
                } catch (InterruptedException e) {
                    log.error("等待文件下载线程中断", e);
                    Thread.currentThread().interrupt();
                    throw new SunyardException("文件下载中断");
                }
            }
        }

        // 打包压缩
        ZipUtils.apacheZipCompress(tempRootPath, zipFilePath);
        log.info("压缩包生成完成：{}", zipFilePath);
    }

    /**
     * 根据zip路径下载
     *
     * @param zipFileKey
     * @param request
     * @return
     */
    public ResponseEntity<?> downloadZip(String zipFileKey, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        String redisKey = CachePrefixConstants.MERGE_ZIP + sessionId;
        String totalCountKey = CachePrefixConstants.MERGE_ZIP_TOTAL_COUNT + sessionId;
        String finishedCountKey = CachePrefixConstants.MERGE_ZIP_FINISHED_COUNT + sessionId;
        redisUtil.del(totalCountKey);
        redisUtil.del(finishedCountKey);
        // 1. 检查是否还有任务在跑
        if (redisUtil.hasKey(redisKey)) {
            return ResponseEntity.status(HttpStatus.OK).body(Result.error("压缩包正在生成中，请等待", 500));
        }

        // 2. 检查文件是否存在
        String zipFilePath = redisUtil.get(CachePrefixConstants.ZIP_PATH + zipFileKey);
        if (zipFilePath == null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Result.error("压缩包不存在", ResultCode.SYSTEM_ERROR));
        }

        File zipFile = new File(zipFilePath);
        if (!zipFile.exists()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Result.error("压缩包不存在", ResultCode.SYSTEM_ERROR));
        }

        // 3. 构建带自动删除的资源
        FileSystemResource zipResource = new FileSystemResource(zipFile) {
            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(zipFile) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        // 流关闭后删除压缩包文件
                        cleanResources(null, zipFile, null);
                    }
                };
            }
        };

        // 4. 返回文件流
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + zipFile.getName() + "\"")
                .body(zipResource);
    }

    private static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    public ResponseEntity<StreamingResponseBody> downloadFile(Long fileId, String fileName) {
        try {
            byte[] fileBytes;
            //调用存储服务的获取文件字节流接口
            FileByteVO fileByteVO = new FileByteVO();
            fileByteVO.setFileId(fileId);
            Result<byte[]> fileByteResult = fileHandleApi.getFileBytes(fileByteVO);
            if (!Objects.isNull(fileByteResult) &&fileByteResult.isSucc()){
                fileBytes = fileByteResult.getData();
            } else {
                fileBytes = null;
            }
            if (fileBytes == null || fileBytes.length == 0) {
                return ResponseEntity.notFound().build();
            }

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", URLEncoder.encode(fileName, String.valueOf(StandardCharsets.UTF_8)));
            headers.setContentLength(fileBytes.length);

            // 创建流式响应体
            StreamingResponseBody stream = outputStream -> {
                try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            };
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(stream);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 检测压缩包是否能够正常下载
     * @param zipFileKey
     * @param request
     */
    public Result checkZipSuccess(String zipFileKey, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        String redisKey = CachePrefixConstants.MERGE_ZIP + sessionId;

        String totalCountKey = CachePrefixConstants.MERGE_ZIP_TOTAL_COUNT + sessionId;
        String finishedCountKey = CachePrefixConstants.MERGE_ZIP_FINISHED_COUNT + sessionId;

        // 检查是否还有任务在跑
        if (redisUtil.hasKey(redisKey)) {
            // 获取进度数据
            String totalCountStr = redisUtil.get(totalCountKey);
            String finishedCountStr = redisUtil.get(finishedCountKey);

            int totalCount = totalCountStr != null ? Integer.parseInt(totalCountStr) : 0;
            int finishedCount = finishedCountStr != null ? Integer.parseInt(finishedCountStr) : 0;

            // 返回进度信息
            Map<String, Object> progress = new HashMap<>();
            progress.put("status", "PROCESSING");
            progress.put("totalFileCount", totalCount);
            progress.put("finishedFileCount", finishedCount);
            progress.put("progress", totalCount > 0 ? (finishedCount * 100 / totalCount) : 0);
            return Result.success(progress);
        }

        // 检查文件是否存在
        String zipFilePath = redisUtil.get(CachePrefixConstants.ZIP_PATH + zipFileKey);
        if (zipFilePath == null) {
            return Result.error("压缩包路径不存在", ResultCode.SYSTEM_ERROR);
        }

        File zipFile = new File(zipFilePath);
        if (!zipFile.exists()) {
            return Result.error("压缩包文件不存在", ResultCode.SYSTEM_ERROR);
        }

        // 任务完成，返回成功状态和文件信息
        Map<String, Object> result = new HashMap<>();
        result.put("status", "COMPLETED");
        result.put("zipFilePath", zipFilePath);
        result.put("totalFileCount", redisUtil.get(totalCountKey) != null ? Integer.parseInt(redisUtil.get(totalCountKey)) : 0);
        result.put("finishedFileCount", redisUtil.get(totalCountKey) != null ? Integer.parseInt(redisUtil.get(totalCountKey)) : 0);
        result.put("progress", 100);

        return Result.success(result);
    }

    /**
     * 计算文件数量
     */
    private int calculateTotalFileCount(List<DownloadFileZip> downloadFileList) {
        if (downloadFileList == null || downloadFileList.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (DownloadFileZip ecmDownload : downloadFileList) {
            List<DocFileZip> docFileList = ecmDownload.getDocFileList();
            if (docFileList == null || docFileList.isEmpty()) {
                continue;
            }
            for (DocFileZip docFile : docFileList) {
                List<Long> fileIds = docFile.getFileIds();
                if (fileIds != null) {
                    total += fileIds.size();
                }
            }
        }
        return total;
    }

    /**
     * 核心工具方法：将多个BufferedImage垂直拼接为一张（从上到下按顺序）
     * 自动统一宽度，高度按原比例缩放，保证图片不变形
     * @param pageImages 分页图片列表
     * @return 拼接后的完整BufferedImage
     * @throws IOException 图片处理异常
     */
    private BufferedImage mergeBufferedImages(List<BufferedImage> pageImages) throws IOException {
        if (pageImages == null || pageImages.isEmpty()) {
            throw new IllegalArgumentException("分页图片列表不能为空");
        }

        // 步骤1：计算所有图片的最大宽度（作为拼接后图片的统一宽度）
        int maxWidth = 0;
        for (BufferedImage img : pageImages) {
            if (img.getWidth() > maxWidth) {
                maxWidth = img.getWidth();
            }
        }

        // 步骤2：计算统一宽度后，每张图片的缩放高度（保持宽高比，避免变形）
        List<Integer> scaledHeights = new ArrayList<>();
        int totalHeight = 0; // 拼接后图片的总高度
        for (BufferedImage img : pageImages) {
            double scale = (double) maxWidth / img.getWidth(); // 缩放比例
            int scaledHeight = (int) (img.getHeight() * scale); // 缩放后的高度
            scaledHeights.add(scaledHeight);
            totalHeight += scaledHeight;
        }

        // 步骤3：创建目标拼接图片（画布），RGB格式（支持透明，若需不透明可改为TYPE_INT_RGB）
        BufferedImage mergedImage = new BufferedImage(maxWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = mergedImage.createGraphics();

        // 步骤4：设置绘图参数，提升拼接图片质量（抗锯齿、平滑缩放）
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 步骤5：逐页绘制图片到目标画布，按顺序垂直拼接
        int currentY = 0; // 当前绘制的Y坐标（从上到下，初始为0）
        for (int i = 0; i < pageImages.size(); i++) {
            BufferedImage img = pageImages.get(i);
            int scaledHeight = scaledHeights.get(i);
            // 绘制图片：从(0, currentY)开始，宽maxWidth，高scaledHeight
            g2d.drawImage(img, 0, currentY, maxWidth, scaledHeight, null);
            currentY += scaledHeight; // 下移Y坐标，准备绘制下一张
        }

        // 步骤6：释放绘图资源（必须执行，避免内存泄漏）
        g2d.dispose();

        return mergedImage;
    }

    public void printFileImage(HttpServletRequest request, HttpServletResponse response,
                          List<Long> fileIds, AccountToken token, String password) {
        // 1. 文件ID校验、获取待合并的PDF流列表
        AssertUtils.isNull(fileIds, "文件id不能为空");
        List<InputStream> pdfStreams = getMergePdf(fileIds, password);

        try (
                // 2. 合并多个PDF流为单个PDF输入流
                InputStream mergedPdfStream = FileDealUtils.mergePdfStreams(pdfStreams);
                // 3. 响应输出流
                ServletOutputStream servletOutputStream = response.getOutputStream()
        ) {
            // 4. 获取水印配置
            WaterMarkConfigVO waterMarkConfigVO = getWaterMarkConfig(
                    ImgsConstants.WATERMARK_TYPE_PRINTING, token);

            // 存储【带水印/无水印】的最终PDF字节流
            ByteArrayOutputStream finalPdfBaos = new ByteArrayOutputStream();

            if (waterMarkConfigVO.getOpenFlag()) {
                // 5. 给PDF流加水印
                Font font = new Font(waterMarkConfigVO.getWaterStyleFamilyValue(), Font.BOLD,
                        Integer.parseInt(waterMarkConfigVO.getWaterStyleSizeValue()));
                Color color = Color.decode(waterMarkConfigVO.getColor1());
                WatermarkUtils.addPdfWaterMarkByConfig(mergedPdfStream, finalPdfBaos,
                        waterMarkConfigVO.getNum(), font, color, waterMarkConfigVO.getMarkValue());
            } else {
                // 6. 无水印则直接复制PDF流
                byte[] bytes = FileUtils.read(mergedPdfStream);
                finalPdfBaos.write(bytes);
            }

            // 将【带水印/无水印的PDF字节流】转换为【拼接后的单张图片流】并输出到前端
            pdfBytesToMergedImageStream(finalPdfBaos.toByteArray(), servletOutputStream);

            FileUtils.printFile(response, servletOutputStream, "打印图片.png");

        } catch (IOException e) {
            log.error("PDF加水印后转图片打印异常，文件ID：{}", fileIds, e);
            throw new RuntimeException("打印失败：PDF转图片异常", e);
        }
    }

    /**
     * 新增工具方法1：PDF字节数组 → 拼接后的单张图片 → 直接写入前端响应输出流
     * @param pdfBytes 带水印/无水印的PDF字节数组
     * @param outputStream 前端Servlet响应输出流
     * @throws IOException 流/图片处理异常
     */
    private void pdfBytesToMergedImageStream(byte[] pdfBytes, ServletOutputStream outputStream) throws IOException {
        // PDF字节数组转输入流，供PdfDocument加载
        try (InputStream pdfInputStream = new ByteArrayInputStream(pdfBytes)) {
            // PDF流转换为拼接后的单张BufferedImage
            BufferedImage mergedImage = pdfStreamToMergedImage(pdfInputStream);
            // 直接将图片写入前端响应流（PNG格式，打印清晰、无损）
            ImageIO.write(mergedImage, "PNG", outputStream);
            // 释放图片内存
            mergedImage.flush();
        }
    }

    /**
     * 新增工具方法2：PDF输入流 → 多页拼接后的单张BufferedImage（复用并优化原逻辑）
     * @param pdfStream 带水印/无水印的PDF输入流
     * @return 所有PDF页垂直拼接的单张图片
     * @throws IOException PDF加载/图片转换异常
     */
    private BufferedImage pdfStreamToMergedImage(InputStream pdfStream) throws IOException {
        PdfDocument doc = new PdfDocument();
        List<BufferedImage> pageImages = new ArrayList<>();
        try {
            // 加载PDF流（核心：适配内存流，无本地文件依赖）
            doc.loadFromStream(pdfStream);
            int pageCount = doc.getPages().getCount();
            if (pageCount == 0) {
                throw new IllegalArgumentException("PDF无有效页面，无法转图片");
            }
            // PDF逐页转换为Bitmap图片，存入列表
            for (int i = 0; i < pageCount; i++) {
                BufferedImage pageImage = doc.saveAsImage(i, PdfImageType.Bitmap);
                pageImages.add(pageImage);
            }
            // 多页图片垂直拼接为单张
            return mergeBufferedImages(pageImages);
        } finally {
            // 强制释放PDF文档资源
            if (doc != null) doc.close();
            // 释放单页图片内存，避免泄漏
            pageImages.forEach(BufferedImage::flush);
        }
    }

}
