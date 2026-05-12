package com.sunyard.module.storage.ecmbank.service;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import com.sunyard.client.SunEcmClientApi;
import com.sunyard.client.bean.ClientBatchBean;
import com.sunyard.client.bean.ClientBatchFileBean;
import com.sunyard.client.bean.ClientBatchIndexBean;
import com.sunyard.client.bean.ClientFileBean;
import com.sunyard.client.impl.SunEcmClientSocketApiImpl;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.conversion.XmlUtils;
import com.sunyard.framework.common.util.encryption.Md5Utils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.storage.config.properties.StorageBankEcmProperties;
import com.sunyard.module.storage.constant.StateConstants;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.ecmbank.util.EcmUtils;
import com.sunyard.module.storage.vo.BackEcmRequestVo;
import com.sunyard.module.storage.vo.DownFileVO;
import com.sunyard.module.storage.vo.UploadListVO;
import com.sunyard.util.OptionKey;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouleibin
 * @date 2021/12/20 9:36
 * @Desc
 */
@Slf4j
@Service
public class StorageBankEcmFileService {

    @Resource
    private StorageBankEcmProperties storageBankEcmProperties;
    @Resource
    private SnowflakeUtils snowflakeUtils;

    /**
     * 银行影像上传
     *
     * @return Result
     */
    public Result<List<SysFileDTO>> backEcmUploadFile(List<UploadListVO> uploadVOList) {

        String ecmIp=storageBankEcmProperties.getEcmIp();
        Integer ecmPort=storageBankEcmProperties.getEcmPort();
        String userNo=storageBankEcmProperties.getUserNo();
        String password=storageBankEcmProperties.getPassword();
        String groupName=storageBankEcmProperties.getGroupName();
        String modelCode=storageBankEcmProperties.getModelCode();
        String filePart=storageBankEcmProperties.getFilePart();
        String prefixPath=storageBankEcmProperties.getPrefixPath();

        AssertUtils.isTrue(CollectionUtil.isEmpty(uploadVOList), "上传的文件不可为空!");
        String batchId = uploadVOList.get(0).getStEquipmentId().toString();
        //上传日期
        String indexValue = uploadVOList.get(0).getMd5();
        String batchIdPath = prefixPath + File.separator + batchId;
        //将上传的文件放到相应的文件夹下
        saveFile(uploadVOList, batchIdPath);
        File imagefile = new File(batchIdPath);
        //删除上次交互的xml文件
        com.sunyard.framework.common.util.FileUtils.deleteXml(imagefile);
        List<File> imagefiles = Arrays.asList(imagefile.listFiles());
        if (imagefile == null || imagefiles == null || imagefiles.size() == 0) {
            log.info("路径下文件不存在");
            return Result.error("路径下文件不存在", ResultCode.SYSTEM_ERROR);
        }
        /*
         * 进行排序
         */
        List<SysFileDTO> list = new ArrayList<>();
        ClientBatchBean clientBatchBean = new ClientBatchBean();
        clientBatchBean.setModelCode(modelCode);
        clientBatchBean.setUser(userNo);
        clientBatchBean.setPassWord(password);
        // 是否作为断点续传上传
        clientBatchBean.setBreakPoint(false);
        // 是否为批次下的文件添加MD5码
        clientBatchBean.setOwnMD5(false);
        // =========================设置索引对象信息开始=========================
        ClientBatchIndexBean clientBatchIndexBean = new ClientBatchIndexBean();
        clientBatchIndexBean.setAmount("");
        // 索引自定义属性
        clientBatchIndexBean.addCustomMap("BUSI_SERIAL_NO", batchId);
        clientBatchIndexBean.addCustomMap("CREATEDATE", indexValue);
        // =========================设置索引对象信息结束=========================
        // =========================设置文档部件信息开始=========================
        ClientBatchFileBean clientBatchFileBean = new ClientBatchFileBean();
        clientBatchFileBean.setFilePartName(filePart);
        // =========================设置文档部件信息结束=========================
        //拼装xml
        Document sortdoc = DocumentHelper.createDocument();
        Element sortElem = sortdoc.addElement("root");
        Element sortnodeElem = sortElem.addElement("node");
        sortnodeElem.addAttribute("name", "BUSI_SERIAL_NO");
        // =========================添加文件=========================
        for (int i = 0; i < imagefiles.size(); i++) {
            if (imagefiles.get(i).isFile()) {
                SysFileDTO sysFileDTO = new SysFileDTO();
                File imageFile = imagefiles.get(i);
                String fileName = imageFile.getName();
                log.info(batchId + "<><><>文件部件添加:" + fileName);
                // 添加FileBean
                ClientFileBean fileBean = new ClientFileBean();
                fileBean.setFileName(imageFile.getPath());
                fileBean.setFileFormat(fileName.substring(fileName.lastIndexOf(".") + 1));
                sysFileDTO.setExt(fileBean.getFileFormat());
                //文件名称
                fileBean.addOtherAtt("TRUENAME", fileName);
                try {
                    byte[] bytes = Files.readAllBytes(imageFile.toPath());
                    //文件大小
                    fileBean.addOtherAtt("FILEATTR", String.valueOf(bytes.length));
                } catch (IOException e) {
                    log.error("异常描述", e);
                    throw new RuntimeException(e);
                }
                sysFileDTO.setOriginalFilename(fileName);
                try {
                    sysFileDTO.setFileMd5(
                            Md5Utils.calculateMD5(FileUtils.readFileToByteArray(imageFile)));
                } catch (IOException e) {
                    log.error("异常描述", e);
                    throw new RuntimeException(e);
                }
                sysFileDTO.setId(snowflakeUtils.nextId());
                fileBean.addOtherAtt("SHOWNAME", batchId + "-" + sysFileDTO.getId());
                list.add(sysFileDTO);
                // 0,2
                try {
                    fileBean.addOtherAtt("FILEMD5",
                            Md5Utils.calculateMD5(Files.readAllBytes(imageFile.toPath())));
                } catch (IOException e) {
                    log.error("异常描述", e);
                    throw new RuntimeException(e);
                }
                clientBatchFileBean.addFile(fileBean);
                Element sortitemElem = sortnodeElem.addElement("item");
                sortitemElem.addAttribute("filename", fileName);
            }
        }
        // =======================添加排序文档============================
        // 生产排序报文
        String sortfileName = "sort_" + System.currentTimeMillis() + ".xml";
        XmlUtils.createXml(sortdoc, batchIdPath + File.separator + sortfileName);
        ClientFileBean sortFileBean = new ClientFileBean();
        sortFileBean.setFileName(batchIdPath + File.separator + sortfileName);
        sortFileBean.setFileFormat("xml");
        sortFileBean.addOtherAtt("FILEMD5",
                Md5Utils.getHash(batchIdPath + File.separator + sortfileName, "MD5"));
        // 0,2
        sortFileBean.addOtherAtt("FILEATTR", "0");
        // KJ_IMAGE
        sortFileBean.addOtherAtt("FILEFORM", "SORT_");
        // KJ_IMAGE
        sortFileBean.addOtherAtt("SHOWNAME", sortfileName);
        clientBatchFileBean.addFile(sortFileBean);
        // =========================添加文件=========================
        clientBatchBean.setIndex_Object(clientBatchIndexBean);
        clientBatchBean.addDocument_Object(clientBatchFileBean);
        SunEcmClientApi clientApi = new SunEcmClientSocketApiImpl(ecmIp, ecmPort);
        String resultMsg = null;
        try {
            resultMsg = clientApi.upload(clientBatchBean, groupName);
        } catch (Exception e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
        log.info("#######上传批次返回的信息[" + resultMsg + "]#######");

        if (resultMsg.contains("SUCCESS")) {
            log.info("上传成功");
            String contentId = resultMsg.replace("SUCCESS<<::>>", "");
            list.forEach(a -> a.setFilename(contentId));
        } else if (resultMsg.contains("FAIL")) {
            log.info("上传失败");
            deleteFilesInDirectory(imagefile);
            return Result.error("上传失败", ResultCode.SYSTEM_ERROR);
        }
        //删除文件
        deleteFilesInDirectory(imagefile);
        return Result.success(list);
    }

    /**
     * 银行影像文件 补传
     *
     * @param file 文件对象
     * @return Result
     */
    public Result<List<SysFileDTO>> supplementaryFile(List<UploadListVO> file) {
        String ecmIp=storageBankEcmProperties.getEcmIp();
        Integer ecmPort=storageBankEcmProperties.getEcmPort();
        String userNo=storageBankEcmProperties.getUserNo();
        String password=storageBankEcmProperties.getPassword();
        String groupName=storageBankEcmProperties.getGroupName();
        String modelCode=storageBankEcmProperties.getModelCode();
        String filePart=storageBankEcmProperties.getFilePart();
        String prefixPath=storageBankEcmProperties.getPrefixPath();

        BackEcmRequestVo backEcmUploadBody = new BackEcmRequestVo();
        backEcmUploadBody.setBatchId(file.get(0).getStEquipmentId().toString());
        backEcmUploadBody.setFiles(file);
        backEcmUploadBody.setStartDate(file.get(0).getMd5());
        backEcmUploadBody.setContentId(file.get(0).getFileSource());
        //将上传的文件放到相应的文件夹下
        String newFilePath = prefixPath + File.separator + backEcmUploadBody.getBatchId();
        saveFile(backEcmUploadBody.getFiles(), newFilePath);
        File imagefile = new File(newFilePath);
        com.sunyard.framework.common.util.FileUtils.deleteXml(imagefile);
        List<File> imagefiles = Arrays.asList(imagefile.listFiles());
        if (imagefile == null || imagefiles == null || imagefiles.size() == 0) {
            log.info("路径下文件不存在");
            return Result.success();
        }
        List<SysFileDTO> list = new ArrayList<>();
        Collections.sort(imagefiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile()) {
                    return -1;
                }
                if (o1.isFile() && o2.isDirectory()) {
                    return 1;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
        SunEcmClientApi clientApi = new SunEcmClientSocketApiImpl(ecmIp, ecmPort);
        /****** begin 新建 批次对象 ******/
        ClientBatchBean clientBatchBean = new ClientBatchBean();
        /****** end 新建 批次对象 ******/
        /****** begin 配置 批次对象 信息 ******/
        clientBatchBean.setUser(userNo);
        clientBatchBean.setPassWord(password);
        clientBatchBean.setModelCode(modelCode);
        // 设置文件传输是否需要MD5的校验
        clientBatchBean.setOwnMD5(false);
        /****** begin 配置 批次对象 中的 索引对象 信息 ******/
        // 影像批次号，唯一索引
        String contentId = backEcmUploadBody.getContentId();
        // 必要信息，更新信息必须要传contentID
        clientBatchBean.getIndex_Object().setContentID(contentId);
        // 必要信息，自定义属性中必须有8位数字字段，用以分表
        String createDate = "CREATEDATE";

        clientBatchBean.getIndex_Object().addCustomMap(createDate,
                backEcmUploadBody.getStartDate());
        /****** end 配置 批次对象 中的 索引对象 信息 ******/

        /****** begin 配置 批次对象 中的 文档部件对象 信息 ******/
        ClientBatchFileBean documentObject = new ClientBatchFileBean();
        documentObject.setFilePartName(filePart);
        /****** end 配置 批次对象 中的 文档部件对象 信息 ******/

        /****** begin 四类更新操作逐一测试 ******/
        try {
            // 新增
            documentObject = updateAdd(documentObject, newFilePath, backEcmUploadBody.getBatchId(),
                    list);
            // 替换
            // documentObject = updateReplace(documentObject);
            // 删除
            // documentObject = updateDelete(documentObject);
            // 修改文档部件字段，不替换文件
            // documentObject = updateModify(documentObject);
        } catch (Exception e1) {
            log.error("异常描述", e1);
            throw new RuntimeException(e1.getMessage(), e1);
        }
        /****** end 四类更新操作逐一测试 ******/

        // 将更新后的 文档部件 对象 ，添加到 批次对象 中
        clientBatchBean.addDocument_Object(documentObject);
        /****** end 配置批次对象信息 ******/

        try {
            String resultMsg = clientApi.update(clientBatchBean, groupName, true);
            list.forEach(s -> s.setFilename(backEcmUploadBody.getContentId()));
            log.info("******resultMsg:" + resultMsg);
        } catch (Exception e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
        deleteFilesInDirectory(
                new File(prefixPath + File.separator + backEcmUploadBody.getBatchId()));
        return Result.success(list);
    }

    /**
     * 根据contentId (第一次上传返回的批次号)
     * @param contentId 内容id
     * @param createDate 创建日期
     * @return Result
     */
    public Result<List<SysFileDTO>> queryFileListByContentId(String contentId, String createDate) {
        String ecmIp=storageBankEcmProperties.getEcmIp();
        Integer ecmPort=storageBankEcmProperties.getEcmPort();
        String userNo=storageBankEcmProperties.getUserNo();
        String password=storageBankEcmProperties.getPassword();
        String groupName=storageBankEcmProperties.getGroupName();
        String modelCode=storageBankEcmProperties.getModelCode();
        String filePart=storageBankEcmProperties.getFilePart();
        String indexName=storageBankEcmProperties.getIndexName();

        // ecm初始化数据，根据情况改造
        EcmUtils ecmUtils = new EcmUtils(ecmIp, ecmPort, userNo, password);
        ecmUtils.set(groupName, modelCode, filePart, indexName);
        // ecm初始化结束，准备查看文件
        String xml = ecmUtils.queryBatch(contentId, createDate);
        List<SysFileDTO> batchImgList;
        try {
            batchImgList = parseEcmXml(xml, "1");
        } catch (DocumentException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
        return Result.success(batchImgList);

    }

    /**
     * 文件下载
     * @param downFileVO downFileVO
     * @return Result
     */
    public byte[] downloadFiles(DownFileVO downFileVO) {
        String prefixPath=storageBankEcmProperties.getPrefixPath();

        Assert.notNull(downFileVO.getFileId(), "文件id不能为空!");
        Assert.notNull(downFileVO.getOrgCode(), "contentId不可为空!");
        Assert.notNull(downFileVO.getOrgName(), "uploadDate不能为空!");
        Result<List<SysFileDTO>> listResult = queryFileListByContentId(downFileVO.getOrgCode(),
                downFileVO.getOrgName());
        List<SysFileDTO> data = listResult.getData();
        List<SysFileDTO> needList = data.stream()
                .filter(datum -> downFileVO.getFileId().contains(datum.getId()))
                .collect(Collectors.toList());
        String path = prefixPath + File.separator + UUID.randomUUID();
        File file = new File(path);
        // 检查路径是否存在，如果不存在，则创建它
        if (!file.exists()) {
            // 创建路径中的所有文件夹
            file.mkdirs();
        }
        //删除下面的文件
        deleteFilesInDirectory(file);
        byte[] fileBytes;
        if (StateConstants.COMMON_ONE.equals(downFileVO.getIsPack())) {
            //压缩包下载
            for (SysFileDTO sysFileDTO : needList) {
                try {
                    downloadFile(sysFileDTO.getUrl(), file, sysFileDTO.getOriginalFilename());
                } catch (IOException e) {
                    log.error("异常描述", e);
                    throw new RuntimeException(e);
                }
            }
            // 创建ZIP文件
            File zipFile = new File(file.getAbsolutePath(), "files.zip");
            try {
                createZip(file, zipFile);
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
            // 将ZIP文件转换成字节数组
            try {
                fileBytes = Files.readAllBytes(zipFile.toPath());
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
        } else {
            try {
                downloadFile(needList.get(0).getUrl(), file, needList.get(0).getOriginalFilename());
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
            try {
                fileBytes = FileUtils.readFileToByteArray(
                        new File(path + File.separator + needList.get(0).getOriginalFilename()));
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
        }
        //删除文件
        deleteDirectory(file);
        return fileBytes;
    }

    /**
     * 新增一个文件
     * @param clientBatchFileBean 客户端批量文件对象
     * @param filePath 文件路径
     * @param batchId 批量id
     * @param list list
     * @return ClientBatchFileBean
     * @throws Exception 异常
     */
    private ClientBatchFileBean updateAdd(ClientBatchFileBean clientBatchFileBean, String filePath,
                                          String batchId, List<SysFileDTO> list)
            throws Exception {
        // ------文件转换开始------
        /*        String filePath = "C:/Users/Administrator/Pictures/Camera Roll/xsq.png"; //具体图片路径需要你们自己传
        File file = new File(filePath);
        String smallPath = filePath.substring(0, filePath.indexOf(".")) + "_nail.jpg";
        String joinPath = filePath.substring(0, filePath.indexOf(".")) + "_total.jpg";*/
        List<File> files = listFiles(filePath);
        for (File file : files) {
            SysFileDTO sysFileDTO = new SysFileDTO();

            ClientFileBean clientFileBean = new ClientFileBean();
            // 必选字段，设置操作类型为追加
            clientFileBean.setOptionType(OptionKey.U_ADD);
            // ------文件转换结束------
            clientFileBean.setFileName(file.getPath());
            sysFileDTO.setExt(file.getPath().substring(file.getPath().lastIndexOf(".") + 1));
            clientFileBean.setFileFormat(sysFileDTO.getExt());
            clientFileBean.setFilesize(String.valueOf(file.length()));
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                //文件大小
                clientFileBean.addOtherAtt("FILEATTR", String.valueOf(bytes.length));
                sysFileDTO.setSize(Long.valueOf(bytes.length));
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
            clientFileBean.addOtherAtt("TRUENAME", file.getName());
            sysFileDTO.setOriginalFilename(file.getName());
            sysFileDTO.setFileMd5(Md5Utils.calculateMD5(Files.readAllBytes(file.toPath())));
            clientFileBean.addOtherAtt("FILEMD5", sysFileDTO.getFileMd5());
            sysFileDTO.setId(snowflakeUtils.nextId());
            clientFileBean.addOtherAtt("SHOWNAME", batchId + "-" + sysFileDTO.getId());
            /*      clientFileBean.addOtherAtt("BUSI_FILE_PAGENUM", "1");// 文件页码
            clientFileBean.addOtherAtt("BUSI_FILE_SCANUSER", "10010001");// 上传用户，柜员号
            clientFileBean.addOtherAtt("BUSI_FILE_TYPE", "60050304");// 文件条码号，既目录id
            clientFileBean.addOtherAtt("FILE_CN_NAME", "001-60050304-fr");// 文件名称展现时使用*/
            // 必选字段，将文件对象添加到文档部件对象中
            clientBatchFileBean.addFile(clientFileBean);
            list.add(sysFileDTO);
        }
        return clientBatchFileBean;
    }

    /**
     * 获取文件夹下所有文件
     *
     * @param directoryPath 文件夹
     * @return Result
     */
    private List<File> listFiles(String directoryPath) {
        List<File> fileList = new ArrayList<>();
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        fileList.add(file);
                    } else if (file.isDirectory()) {
                        fileList.addAll(listFiles(file.getAbsolutePath()));
                    }
                }
            }
        }
        return fileList;
    }

    /**
     * 删除文件夹下文件
     *
     * @param directory 文件夹
     */
    private void deleteFilesInDirectory(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 递归删除子目录中的文件
                    deleteFilesInDirectory(file);
                } else {
                    // 删除文件
                    file.delete();
                }
            }
        }
    }

    /**
     * 将文件 保存到相应文件夹下
     *
     * @param uploadVOList 文件对象
     * @param filePath 文件地址
     */
    private void saveFile(List<UploadListVO> uploadVOList, String filePath) {
        File file = new File(filePath);
        // 检查路径是否存在，如果不存在，则创建它
        if (!file.exists()) {
            // 创建路径中的所有文件夹
            file.mkdirs();
        }
        for (UploadListVO uploadListVO : uploadVOList) {
            Path path = Paths.get(filePath, uploadListVO.getFileName());
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
            try {
                Files.write(path, uploadListVO.getFileByte());
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 删除文件夹及其内容
     *
     * @param directory 要删除的文件夹
     */
    private void deleteDirectory(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return;
        }
        // 递归删除文件夹下的所有内容
        deleteFilesInDirectory(directory);
        // 删除最后一级文件夹
        directory.delete();
    }

    /**
     * 创建ZIP文件
     * @param directory 文件夹
     * @param zipFile 压缩文件
     * @throws IOException 异常
     */
    private void createZip(File directory, File zipFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        zipDirectory(directory, directory, zos);
        zos.close();
        fos.close();
    }

    /**
     * 下载文件并保存到临时文件夹
     * @param downloadUrl 文件下载地址
     * @param tempDir 临时文件夹
     * @param fileName 文件名
     * @throws IOException 异常
     */
    private void downloadFile(String downloadUrl, File tempDir, String fileName)
            throws IOException {
        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        InputStream inputStream = connection.getInputStream();
        File file = new File(tempDir, fileName);
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();
    }

    /**
     * 将文件夹中的所有文件添加到ZIP文件中
     * @param baseDir 根文件夹
     * @param directory 文件对象
     * @param zos zip输出流
     * @throws IOException 异常
     */
    private void zipDirectory(File baseDir, File directory, ZipOutputStream zos)
            throws IOException {
        File[] files = directory.listFiles();
        byte[] buffer = new byte[1024];
        int bytesRead;
        for (File file : files) {
            if (file.equals(new File(directory, "files.zip"))) {
                continue; // 如果是ZIP文件本身，则跳过
            }
            if (file.isDirectory()) {
                zipDirectory(baseDir, file, zos);
            } else {
                FileInputStream fis = new FileInputStream(file);
                String entryName = file.getAbsolutePath()
                        .substring(baseDir.getAbsolutePath().length() + 1);
                ZipEntry zipEntry = new ZipEntry(entryName);
                zos.putNextEntry(zipEntry);
                while ((bytesRead = fis.read(buffer)) != -1) {
                    zos.write(buffer, 0, bytesRead);
                }
                fis.close();
            }
        }
    }

    /**
     * 解析返回的xml
     *
     * @param xml xml
     * @param oneMkdir oneMkdir
     * @return Result
     * @throws DocumentException 异常
     */
    private List<SysFileDTO> parseEcmXml(String xml, String oneMkdir) throws DocumentException {
        List<String> indexList = new ArrayList<>();
        //创建批次一级目录
        indexList.add(oneMkdir);
        Document document = XmlUtils.stringConvertDoc(xml);
        Element root = document.getRootElement();
        Element batchBean = root.element("BatchBean");
        Element documentObjects = batchBean.element("document_Objects");
        Element batchFileBean = documentObjects.element("BatchFileBean");
        List<Element> fileBeans = batchFileBean.element("files").elements("FileBean");
        List<SysFileDTO> sysFileDtoList = new ArrayList<>();
        //保存文件相关信息
        if (fileBeans != null && !fileBeans.isEmpty()) {
            for (Element fileBean : fileBeans) {
                if (!"SORT_".equals(
                        fileBean.element("otherAtt").element("FILEFORM").elementText("string"))) {
                    //  Map<String, Object> fileMap = new HashMap<>();
                    LinkedHashMap<String, Object> fileMap = new LinkedHashMap<>();
                    String trueName = fileBean.element("otherAtt").element("TRUENAME")
                            .elementText("string");
                    String showName = fileBean.element("otherAtt").element("SHOWNAME")
                            .elementText("string");
                    SysFileDTO sysFileDTO = new SysFileDTO();
                    //文件大小
                    String size = fileBean.element("otherAtt").element("FILEATTR")
                            .elementText("string");
                    String md5 = fileBean.element("otherAtt").element("FILEMD5")
                            .elementText("string");
                    String url = fileBean.attributeValue("URL");
                    String ext = fileBean.attributeValue("FILE_FORMAT");
                    String value = showName.split("-")[1];
                    sysFileDTO.setId(Long.valueOf(value));
                    sysFileDTO.setOriginalFilename(trueName);
                    sysFileDTO.setExt(ext);
                    sysFileDTO.setSize(Long.parseLong(size));
                    sysFileDTO.setFileMd5(md5);
                    sysFileDTO.setUrl(url);
                    sysFileDTO.setFilename(showName);
                    sysFileDtoList.add(sysFileDTO);
                }
            }
        }
        return sysFileDtoList;
    }
}
