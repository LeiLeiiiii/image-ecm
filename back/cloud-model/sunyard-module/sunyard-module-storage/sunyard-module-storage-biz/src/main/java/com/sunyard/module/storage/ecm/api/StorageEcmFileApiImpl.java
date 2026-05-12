package com.sunyard.module.storage.ecm.api;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.FileUtils;
import com.sunyard.framework.common.util.StreamUtils;
import com.sunyard.framework.common.util.ZipUtils;
import com.sunyard.framework.common.util.conversion.XmlUtils;
import com.sunyard.framework.common.util.date.DateUtils;
import com.sunyard.framework.common.util.http.HttpUtils;
import com.sunyard.insurance.ecm.socket.client.AutoScanApi;
import com.sunyard.module.storage.api.StorageEcmFileApi;
import com.sunyard.module.storage.config.properties.StorageNonBankEcmProperties;
import com.sunyard.module.storage.constant.StateConstants;
import com.sunyard.module.storage.dto.ecm.EcmBaseData;
import com.sunyard.module.storage.dto.ecm.EcmBatch;
import com.sunyard.module.storage.dto.ecm.EcmFileRequestBody;
import com.sunyard.module.storage.dto.ecm.EcmMetaData;
import com.sunyard.module.storage.dto.ecm.EcmMetaDataSingle;
import com.sunyard.module.storage.dto.ecm.EcmNode;
import com.sunyard.module.storage.dto.ecm.EcmPage;
import com.sunyard.module.storage.dto.ecm.EcmPageids;
import com.sunyard.module.storage.dto.ecm.EcmPages;
import com.sunyard.module.storage.dto.ecm.EcmReceive;
import com.sunyard.module.storage.dto.ecm.EcmRequest;
import com.sunyard.module.storage.dto.ecm.EcmRequestBusiData;
import com.sunyard.module.storage.dto.ecm.EcmRoot;
import com.sunyard.module.storage.ecm.dto.EcmListResponse;
import com.sunyard.module.storage.ecm.dto.EcmResponse;
import com.sunyard.module.storage.ecm.dto.NewEcmResponse;
import com.sunyard.module.storage.ecm.dto.QueryDownloadBusiData;
import com.sunyard.module.storage.ecm.dto.QueryImgBusiData;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouleibin
 * @date 2021/12/20 9:36
 * @Desc
 */
@Slf4j
@RestController
public class StorageEcmFileApiImpl implements StorageEcmFileApi {

    @Resource
    private StorageNonBankEcmProperties storageNonBankEcmProperties;

    /***/
    @Override
    public Result uploadFileSimple(byte[] uploadFileInputStream, String no, String type,
                                   String filename) {
        String organCode = storageNonBankEcmProperties.getOrganCode();
        String role = storageNonBankEcmProperties.getRole();
        String docCode = storageNonBankEcmProperties.getDocCode();
        Assert.notNull(no, "编号不能为空");
        Assert.notNull(type, "类型不能为空");

        // 第六层
        EcmPage ecmPage = new EcmPage();
        // 文件名
        ecmPage.setFileName(filename);
        // 上传时间
        ecmPage.setUpTime(DateUtils.getTime());
        List<EcmPage> ecmPageList = new ArrayList<>();
        ecmPageList.add(ecmPage);
        // 第五层
        EcmNode ecmNode = new EcmNode();
        ecmNode.setId(docCode);
        ecmNode.setAction("ADD");
        ecmNode.setPage(ecmPageList);
        List<EcmNode> list = new ArrayList<>();
        list.add(ecmNode);
        // 第四层
        EcmPages ecmPages = new EcmPages();
        ecmPages.setNode(list);
        // 第三层
        EcmBatch batch = new EcmBatch();
        batch.setAppCode(type);
        // --------------档案号
        batch.setBusiNo(no);
        batch.setPages(ecmPages);
        //开启ES
        batch.setIsToEs("1");
        // 第二层
        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setUserCode("SunAM");
        ecmBaseData.setUserName("SunAM");
        ecmBaseData.setOrgCode(organCode);
        ecmBaseData.setOrgName(organCode);
        ecmBaseData.setRoleCode(role);
        EcmMetaDataSingle ecmMetaData = new EcmMetaDataSingle();
        ecmMetaData.setEcmBatch(batch);
        // 第一层
        EcmRoot ecmRoot = new EcmRoot();
        ecmRoot.setEcmBaseData(ecmBaseData);
        ecmRoot.setEcmMetaDataSingle(ecmMetaData);
        log.info(String.valueOf(ecmRoot));
        EcmFileRequestBody ecmFileRequestBody = new EcmFileRequestBody();
        ecmFileRequestBody.setEcmRoot(ecmRoot);
        ecmFileRequestBody.setUploadFilebytes(uploadFileInputStream);
        return this.uploadSendSunICMS(ecmFileRequestBody);
    }

    /***/
    @Override
    public Result uploadNewFileSimple(byte[] uploadFilebytes, String no, String type,
                                      String filename) {
        String organCode = storageNonBankEcmProperties.getOrganCode();
        String role = storageNonBankEcmProperties.getRole();
        Assert.notNull(no, "编号不能为空");
        Assert.notNull(type, "类型不能为空");

        // 第六层
        EcmPage ecmPage = new EcmPage();
        // 文件名
        ecmPage.setFileName(filename);
        // 上传时间
        ecmPage.setUpTime(DateUtils.getTime());
        List<EcmPage> ecmPageList = new ArrayList<>();
        ecmPageList.add(ecmPage);
        // 第五层
        EcmNode ecmNode = new EcmNode();
        ecmNode.setId("fileUpload");
        ecmNode.setAction("ADD");
        ecmNode.setPage(ecmPageList);
        List<EcmNode> list = new ArrayList<>();
        list.add(ecmNode);
        // 第四层
        EcmPages ecmPages = new EcmPages();
        ecmPages.setNode(list);
        // 第三层
        EcmBatch batch = new EcmBatch();
        batch.setAppCode(type);
        // --------------档案号
        batch.setBusiNo(no);
        batch.setPages(ecmPages);
        //开启ES
        batch.setIsToEs("1");
        // 第二层
        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setUserCode("SunAM");
        ecmBaseData.setUserName("SunAM");
        ecmBaseData.setOrgCode(organCode);
        ecmBaseData.setOrgName(organCode);
        ecmBaseData.setRoleCode(role);
        EcmMetaDataSingle ecmMetaData = new EcmMetaDataSingle();
        ecmMetaData.setEcmBatch(batch);
        // 第一层
        EcmRoot ecmRoot = new EcmRoot();
        ecmRoot.setEcmBaseData(ecmBaseData);
        ecmRoot.setEcmMetaDataSingle(ecmMetaData);
        log.info(String.valueOf(ecmRoot));
        EcmFileRequestBody ecmFileRequestBody = new EcmFileRequestBody();
        ecmFileRequestBody.setEcmRoot(ecmRoot);
        ecmFileRequestBody.setUploadFilebytes(uploadFilebytes);
        return this.uploadNewSendSunICMS(ecmFileRequestBody);
    }

    @Override
    public Result uploadFileSimpleBatch(List<byte[]> bytes, String no, String type,
                                        String[] filenames) {
        String organCode = storageNonBankEcmProperties.getOrganCode();
        String role = storageNonBankEcmProperties.getRole();
        String docCode = storageNonBankEcmProperties.getDocCode();
        Assert.notNull(no, "编号不能为空");
        Assert.notNull(type, "类型不能为空");
        List<EcmNode> list = new ArrayList<>();
        for (String filename : filenames) {
            //第六层
            EcmPage ecmPage = new EcmPage();
            //文件名
            ecmPage.setFileName(filename);
            //上传时间
            ecmPage.setUpTime(DateUtils.getTime());
            //第五层
            EcmNode ecmNode = new EcmNode();
            ecmNode.setId(docCode);
            ecmNode.setAction("ADD");
            List<EcmPage> ecmPageList = new ArrayList<>();
            ecmPageList.add(ecmPage);
            ecmNode.setPage(ecmPageList);
            list.add(ecmNode);
        }
        //第四层
        EcmPages ecmPages = new EcmPages();
        ecmPages.setNode(list);
        //第三层
        EcmBatch batch = new EcmBatch();
        batch.setAppCode(type);
        //--------------档案号
        batch.setBusiNo(no);
        batch.setPages(ecmPages);
        //开启ES
        batch.setIsToEs("1");
        //第二层
        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setUserCode("SunAM");
        ecmBaseData.setUserName("SunAM");
        ecmBaseData.setOrgCode(organCode);
        ecmBaseData.setOrgName(organCode);
        ecmBaseData.setRoleCode(role);
        EcmMetaDataSingle ecmMetaData = new EcmMetaDataSingle();
        ecmMetaData.setEcmBatch(batch);
        //第一层
        EcmRoot ecmRoot = new EcmRoot();
        ecmRoot.setEcmBaseData(ecmBaseData);
        ecmRoot.setEcmMetaDataSingle(ecmMetaData);
        log.info(String.valueOf(ecmRoot));
        return this.uploadSendSunICMSBatch(ecmRoot, StreamUtils.byteArrays2InputStreams(bytes));
    }

    @Override
    public Result uploadArcFileBatch(List<byte[]> fileBytes, String arcId, String arcTypeNo,
                                     String[] fileNames, String materialsNo) {
        String organCode = storageNonBankEcmProperties.getOrganCode();
        String role = storageNonBankEcmProperties.getRole();
        cn.hutool.core.lang.Assert.notEmpty(arcId, "档案ID不能为空");
        cn.hutool.core.lang.Assert.notEmpty(arcTypeNo, "档案分类编号（业务类型）不能为空");
        cn.hutool.core.lang.Assert.notEmpty(materialsNo, "资料类型编号不能为空!");
        List<EcmNode> list = new ArrayList<>();
        for (String filename : fileNames) {
            // 第六层
            EcmPage ecmPage = new EcmPage();
            // 文件名
            ecmPage.setFileName(filename);
            // 上传时间
            ecmPage.setUpTime(DateUtils.getTime());
            // 第五层
            EcmNode ecmNode = new EcmNode();
            ecmNode.setId(materialsNo);
            ecmNode.setAction("ADD");
            List<EcmPage> ecmPageList = new ArrayList<>();
            ecmPageList.add(ecmPage);
            ecmNode.setPage(ecmPageList);
            list.add(ecmNode);
        }
        // 第四层
        EcmPages ecmPages = new EcmPages();
        ecmPages.setNode(list);
        // 第三层
        EcmBatch batch = new EcmBatch();
        batch.setAppCode(arcTypeNo);
        // 档案ID-批次号
        batch.setBusiNo(arcId);
        batch.setPages(ecmPages);
        //开启ES
        batch.setIsToEs("1");
        // 第二层
        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setUserCode("SunAM");
        ecmBaseData.setUserName("SunAM");
        ecmBaseData.setOrgCode(organCode);
        ecmBaseData.setOrgName(organCode);
        ecmBaseData.setRoleCode(role);
        EcmMetaDataSingle ecmMetaData = new EcmMetaDataSingle();
        ecmMetaData.setEcmBatch(batch);
        //第一层
        EcmRoot ecmRoot = new EcmRoot();
        ecmRoot.setEcmBaseData(ecmBaseData);
        ecmRoot.setEcmMetaDataSingle(ecmMetaData);
        log.info(String.valueOf(ecmRoot));
        return this.uploadSendSunICMSBatch(ecmRoot, StreamUtils.byteArrays2InputStreams(fileBytes));
    }

    @Override
    public Result uploadNewSendSunICMS(EcmFileRequestBody ecmFileRequestBody) {
        String ip = storageNonBankEcmProperties.getIp();
        Integer socketPort = storageNonBankEcmProperties.getSocketPort();
        String systemId = storageNonBankEcmProperties.getSystemName();
        String licenseKey = storageNonBankEcmProperties.getKey();
        String path = storageNonBankEcmProperties.getLocation();

        EcmRoot ecmRoot = ecmFileRequestBody.getEcmRoot();
        byte[] uploadFilebytes = ecmFileRequestBody.getUploadFilebytes();
        String arcNo = ecmRoot.getEcmMetaDataSingle().getEcmBatch().getBusiNo();
        String uploadFileName = ecmRoot.getEcmMetaDataSingle().getEcmBatch().getPages().getNode()
                .get(0).getPage().get(0).getFileName();
        String xmlPath = path + "/" + "busi.xml";
        String uploadPath = path + "/" + uploadFileName;
        String zipPath = path + "/" + arcNo + ".zip";
        List<String> delFiles = new ArrayList<>();
        delFiles.add(xmlPath);
        delFiles.add(uploadPath);
        delFiles.add(zipPath);
        File file1 = new File(path);
        if (!file1.exists() && !file1.isDirectory()) {
            file1.mkdir();

        }
        try {
            String xml = XmlUtils.marshalToFile(ecmRoot, xmlPath);
            log.debug("uploadSendSunICMS:{}", xml);
            List<File> zipFileList = new ArrayList<>();
            zipFileList.add(new File(xmlPath));
            File uploadFile = new File(uploadPath);
            FileUtils.cpFile(StreamUtils.byteArray2InputStream(uploadFilebytes), uploadFile);
            zipFileList.add(uploadFile);

            FileOutputStream fos2 = new FileOutputStream(new File(zipPath));
            ZipUtils.toZip(zipFileList, fos2);
            // -------------------
            AutoScanApi autoScanApi = new AutoScanApi(ip, socketPort.intValue(),
                    systemId + "#" + licenseKey);
            autoScanApi.setFormat("xml");
            String returnMsg = autoScanApi.ScanImageFile("", zipPath);
            NewEcmResponse ecmResponse = XmlUtils.unmarshal(returnMsg, NewEcmResponse.class);
            log.debug("uploadSendSunICMS-returnMsg:{}", returnMsg);
            if ("200".equals(ecmResponse.getResponseCode())
                    || "1".equals(ecmResponse.getResponseCode())) {

                return Result.success(ecmResponse.getPage().getNewPageBody());
            } else {
                return Result.error(ecmResponse.getResponseMsg(), ResultCode.PARAM_ERROR);
            }
            // ------------------
        } catch (Exception e) {
            log.error("uploadSendSunICMS-error", e);
            throw new SunyardException(ResultCode.PARAM_ERROR, "附件上传失败");
        } finally {
            // 强制删除生成的所有文件
            for (String f : delFiles) {
                FileUtils.delete(f);
            }
        }
    }

    @Override
    public Result uploadSendSunICMS(EcmFileRequestBody ecmFileRequestBody) {
        String ip = storageNonBankEcmProperties.getIp();
        Integer socketPort = storageNonBankEcmProperties.getSocketPort();
        String systemId = storageNonBankEcmProperties.getSystemName();
        String licenseKey = storageNonBankEcmProperties.getKey();
        String path = storageNonBankEcmProperties.getLocation();

        EcmRoot ecmRoot = ecmFileRequestBody.getEcmRoot();
        byte[] uploadFilebytes = ecmFileRequestBody.getUploadFilebytes();
        String arcNo = ecmRoot.getEcmMetaDataSingle().getEcmBatch().getBusiNo();
        String uploadFileName = ecmRoot.getEcmMetaDataSingle().getEcmBatch().getPages().getNode()
                .get(0).getPage().get(0).getFileName();
        String xmlPath = path + "/" + "busi.xml";
        String uploadPath = path + "/" + uploadFileName;
        String zipPath = path + "/" + arcNo + ".zip";
        List<String> delFiles = new ArrayList<>();
        delFiles.add(xmlPath);
        delFiles.add(uploadPath);
        delFiles.add(zipPath);
        File file1 = new File(path);
        if (!file1.exists() && !file1.isDirectory()) {
            file1.mkdir();
        }
        try {
            String xml = XmlUtils.marshalToFile(ecmRoot, xmlPath);
            log.debug("uploadSendSunICMS:{}", xml);
            List<File> zipFileList = new ArrayList<>();
            File file = new File(xmlPath);
            XmlUtils.xmlStrConvertFile(xml, file);
            zipFileList.add(file);
            File uploadFile = new File(uploadPath);
            FileUtils.cpFile(StreamUtils.byteArray2InputStream(uploadFilebytes), uploadFile);
            zipFileList.add(uploadFile);

            FileOutputStream fos2 = new FileOutputStream(new File(zipPath));
            ZipUtils.toZip(zipFileList, fos2);
            // -------------------
            AutoScanApi autoScanApi = new AutoScanApi(ip, socketPort.intValue(),
                    systemId + "#" + licenseKey);
            autoScanApi.setFormat("xml");
            String returnMsg = autoScanApi.ScanImageFile("", zipPath);
            EcmResponse ecmResponse = XmlUtils.unmarshal(returnMsg, EcmResponse.class);
            log.debug("uploadSendSunICMS-returnMsg:{}", returnMsg);
            if ("200".equals(ecmResponse.getResponseCode())
                    || "1".equals(ecmResponse.getResponseCode())) {
                return Result.success(ecmResponse.getPage());
            } else {
                return Result.error(ecmResponse.getResponseMsg(), ResultCode.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("uploadSendSunICMS-error",e);
            throw new SunyardException(ResultCode.PARAM_ERROR, "附件上传失败");
        } finally {
            // 强制删除生成的所有文件
            for (String f : delFiles) {
                FileUtils.delete(f);
            }
        }
    }

    /***/
    public Result uploadFileSimpleBatch(List<InputStream> uploadFileInputStreams, String no,
                                        String type, List<String> filenames) {
        String ip2 = storageNonBankEcmProperties.getIp2();
        Integer socketPort = storageNonBankEcmProperties.getSocketPort();
        String systemId = storageNonBankEcmProperties.getSystemName();
        String organCode = storageNonBankEcmProperties.getOrganCode();
        String licenseKey = storageNonBankEcmProperties.getKey();
        String role = storageNonBankEcmProperties.getRole();
        String docCode = storageNonBankEcmProperties.getDocCode();
        String serverParameter = storageNonBankEcmProperties.getServerParameter();

        Assert.notNull(no, "编号不能为空");
        Assert.notNull(type, "类型不能为空");

        List<EcmNode> list = new ArrayList<>();
        for (String filename : filenames) {
            // 第六层
            EcmPage ecmPage = new EcmPage();
            // 文件名
            ecmPage.setFileName(filename);
            // 上传时间
            ecmPage.setUpTime(DateUtils.getTime());
            // 第五层
            EcmNode ecmNode = new EcmNode();
            ecmNode.setId(docCode);
            ecmNode.setAction("ADD");
            List<EcmPage> ecmPageList = new ArrayList<>();
            ecmPageList.add(ecmPage);
            ecmNode.setPage(ecmPageList);
            list.add(ecmNode);
        }
        // 第四层
        EcmPages ecmPages = new EcmPages();
        ecmPages.setNode(list);
        // 第三层
        EcmBatch batch = new EcmBatch();
        batch.setAppCode(type);
        // --------------档案号
        batch.setBusiNo(no);
        batch.setPages(ecmPages);
        //开启ES
        batch.setIsToEs("1");
        // 第二层
        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setUserCode("SunAM");
        ecmBaseData.setUserName("SunAM");
        ecmBaseData.setOrgCode(organCode);
        ecmBaseData.setOrgName(organCode);
        ecmBaseData.setRoleCode(role);
        EcmMetaDataSingle ecmMetaData = new EcmMetaDataSingle();
        ecmMetaData.setEcmBatch(batch);
        // 第一层
        EcmRoot ecmRoot = new EcmRoot();
        ecmRoot.setEcmBaseData(ecmBaseData);
        ecmRoot.setEcmMetaDataSingle(ecmMetaData);
        log.info(String.valueOf(ecmRoot));
        return this.uploadSendSunICMSBatch(ecmRoot, uploadFileInputStreams);
    }

    /***/
    @Override
    public Result uploadArcFile(byte[] uploadFilebytes, String fileName, String arcNo,
                                String arcTitle, String arcTypeNo, String materialsNo,
                                String remark) {
        String organCode = storageNonBankEcmProperties.getOrganCode();
        String role = storageNonBankEcmProperties.getRole();
        String docCode = storageNonBankEcmProperties.getDocCode();

        Assert.notNull(fileName, "附件名不能为空");
        Assert.notNull(arcNo, "档案id不能为空");
        Assert.notNull(arcTitle, "档案提名不能为空");
        Assert.notNull(arcTypeNo, "档案类型不能为空");
        if (!StringUtils.hasText(materialsNo)) {
            materialsNo = docCode;
        }

        // 第六层
        EcmPage ecmPage = new EcmPage();
        // 文件名
        ecmPage.setFileName(fileName);
        // 上传时间
        ecmPage.setUpTime(DateUtils.getTime());
        ecmPage.setRemark(remark);
        // 第五层
        EcmNode ecmNode = new EcmNode();
        ecmNode.setId(materialsNo);
        ecmNode.setAction("ADD");
        List<EcmPage> ecmPageList = new ArrayList<>();
        ecmPageList.add(ecmPage);
        ecmNode.setPage(ecmPageList);
        List<EcmNode> list = new ArrayList<>();
        list.add(ecmNode);
        // 第四层
        EcmPages ecmPages = new EcmPages();
        ecmPages.setNode(list);
        // 第三层
        EcmBatch batch = new EcmBatch();
        batch.setAppCode(arcTypeNo);
        // --------------档案号
        batch.setBusiNo(arcNo);
        batch.setBusiTitle(arcTitle);
        batch.setPages(ecmPages);
        //开启ES
        batch.setIsToEs("1");
        // 第二层
        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setUserCode("SunAM");
        ecmBaseData.setUserName("SunAM");
        ecmBaseData.setOrgCode(organCode);
        ecmBaseData.setOrgName(organCode);
        ecmBaseData.setRoleCode(role);
        EcmMetaDataSingle ecmMetaData = new EcmMetaDataSingle();
        ecmMetaData.setEcmBatch(batch);
        // 第一层
        EcmRoot ecmRoot = new EcmRoot();
        ecmRoot.setEcmBaseData(ecmBaseData);
        ecmRoot.setEcmMetaDataSingle(ecmMetaData);
        EcmFileRequestBody ecmFileRequestBody = new EcmFileRequestBody();
        ecmFileRequestBody.setEcmRoot(ecmRoot);
        ecmFileRequestBody.setUploadFilebytes(uploadFilebytes);
        return this.uploadSendSunICMS(ecmFileRequestBody);
    }

    /**
     * 不带文件的上传，用于pageid指定删除
     *
     * @param ecmRoot
     * @return Result
     */
    @Override
    public Result uploadSendSunICMSNoFile(EcmRoot ecmRoot) {
        String ip = storageNonBankEcmProperties.getIp();
        Integer socketPort = storageNonBankEcmProperties.getSocketPort();
        String systemId = storageNonBankEcmProperties.getSystemName();
        String licenseKey = storageNonBankEcmProperties.getKey();
        String path = storageNonBankEcmProperties.getLocation();

        File file2 = new File(path);
        if (!file2.exists() && !file2.isDirectory()) {
            file2.mkdir();

        }
        String arcNo = ecmRoot.getEcmMetaDataSingle().getEcmBatch().getBusiNo();
        String xmlPath = path + "/" + "busi.xml";
        String zipPath = path + "/" + arcNo + ".zip";
        List<String> delFiles = new ArrayList<>();
        delFiles.add(xmlPath);
        delFiles.add(zipPath);
        try {
            String xml = XmlUtils.marshalToFile(ecmRoot, xmlPath);
            log.debug("uploadSendSunICMS:{}", xml);
            List<File> zipFileList = new ArrayList<>();
            zipFileList.add(new File(xmlPath));

            FileOutputStream fos2 = new FileOutputStream(new File(zipPath));
            ZipUtils.toZip(zipFileList, fos2);
            // -------------------
            AutoScanApi autoScanApi = new AutoScanApi(ip, socketPort.intValue(),
                    systemId + "#" + licenseKey);
            autoScanApi.setFormat("xml");
            String returnMsg = autoScanApi.ScanImageFile("", zipPath);
            EcmResponse ecmResponse = XmlUtils.unmarshal(returnMsg, EcmResponse.class);
            log.debug("uploadSendSunICMS-returnMsg:{}", returnMsg);
            if ("200".equals(ecmResponse.getResponseCode())
                    || "1".equals(ecmResponse.getResponseCode())) {
                return Result.success(ecmResponse.getPage());
            } else {
                return Result.error(ecmResponse.getResponseMsg(), ResultCode.PARAM_ERROR);
            }
            // ------------------
        } catch (Exception e) {
            log.error("uploadSendSunICMS-error",e);
            throw new SunyardException(ResultCode.PARAM_ERROR, "附件上传失败");
        } finally {
            // 强制删除生成的所有文件
            for (String f : delFiles) {
                FileUtils.delete(f);
            }
        }
    }

    @Override
    public Result getResourceSydEcm0010ByPageId(String appCode, String businessNo,
                                                String[] pageid) {
        String ip2 = storageNonBankEcmProperties.getIp2();
        String port2 = storageNonBankEcmProperties.getPort2();
        String licenseKey = storageNonBankEcmProperties.getKey();
        String serverParameter = storageNonBankEcmProperties.getServerParameter();
        String interfaceAddress = storageNonBankEcmProperties.getInterfaceAddress();

        String address = serverParameter + "/" + interfaceAddress;
        List<EcmPage> fileUrl = getFileUrl(ip2, port2, address, licenseKey, appCode, businessNo,
                Arrays.asList(pageid));
        if (CollectionUtils.isEmpty(fileUrl)) {
            return null;
        }
        List<Map> list = new ArrayList<Map>();
        for (EcmPage e : fileUrl) {
            Map map = new HashMap(6);
            map.put("pageUrl", e.getPageUrl());
            map.put("thumUrl", e.getThumUrl());
            map.put("pageId", e.getPageId());
            list.add(map);
        }
        return Result.success(JSON.toJSONString(list));
    }

    /**
     * 影像下载接口
     *
     * @param appCode
     * @param businessNo
     * @param userCode
     * @param userName
     * @return
     */
    @Override
    public Result getResourceSydEcm0009(String appCode, String businessNo, String userCode,
                                        String userName) {
        String ip2 = storageNonBankEcmProperties.getIp2();
        String port2 = storageNonBankEcmProperties.getPort2();
        String licenseKey = storageNonBankEcmProperties.getKey();
        String role = storageNonBankEcmProperties.getRole();
        String serverParameter = storageNonBankEcmProperties.getServerParameter();
        String interfaceAddress = storageNonBankEcmProperties.getInterfaceAddress();

        String address = serverParameter + "/" + interfaceAddress;
        return getResourceSyd(ip2, port2, address, licenseKey, appCode, businessNo, userCode,
                userName, role, EcmRequestBusiData.CODE_DOWNLOAD);
    }

    @Override
    public Result delBatchArcFileByFileName(String appName, String appCode, String businessNo,
                                            String userCode, String userName, String materialsNo,
                                            List<String> fileNameList) {
        String ip = storageNonBankEcmProperties.getIp();
        String port2 = storageNonBankEcmProperties.getPort2();
        String serverParameter = storageNonBankEcmProperties.getServerParameter();
        String interfaceAddress = storageNonBankEcmProperties.getInterfaceAddress();

        String address = serverParameter + "/" + interfaceAddress;
        return delBatchArcFile("DEL", appCode, appName, businessNo, userCode, userName,
                Collections.singletonList(materialsNo), fileNameList, null, null, ip, port2,
                address);
    }

    @Override
    public Result delBatchArcFileByPageId(String appName, String appCode, String businessNo,
                                          String userCode, String userName, String materialsNo,
                                          List<String> pageIdList) {
        String ip = storageNonBankEcmProperties.getIp();
        String port2 = storageNonBankEcmProperties.getPort2();
        String serverParameter = storageNonBankEcmProperties.getServerParameter();
        String interfaceAddress = storageNonBankEcmProperties.getInterfaceAddress();

        String address = serverParameter + "/" + interfaceAddress;
        return delBatchArcFile("DEL_ID", appCode, appName, businessNo, userCode, userName,
                Collections.singletonList(materialsNo), null, pageIdList, null, ip, port2, address);
    }

    @Override
    public Result delBatchArcFileByMd5(String appName, String appCode, String businessNo,
                                       String userCode, String userName, String materialsNo,
                                       List<String> md5List) {
        String ip = storageNonBankEcmProperties.getIp();
        String port2 = storageNonBankEcmProperties.getPort2();
        String serverParameter = storageNonBankEcmProperties.getServerParameter();
        String interfaceAddress = storageNonBankEcmProperties.getInterfaceAddress();

        String address = serverParameter + "/" + interfaceAddress;
        return delBatchArcFile("DEL_MD5", appCode, appName, businessNo, userCode, userName,
                Collections.singletonList(materialsNo), null, null, md5List, ip, port2, address);
    }

    /**
     * @param delType         DELETE:删除整个资料分类
     *                        DEL_ID：为根据PAGEID删除文件
     *                        DEL_MD5: 为根据MD5删除文件
     *                        DEL: 为根据fileName删除文件
     * @param appCode
     * @param appName
     * @param businessNo
     * @param userCode
     * @param userName
     * @param materialsNoList
     * @param fileNameList
     * @param pageIdList
     * @param md5List
     * @param ip
     * @param port
     * @param address
     * @return
     */
    private Result delBatchArcFile(String delType, String appCode, String appName,
                                   String businessNo, String userCode, String userName,
                                   List<String> materialsNoList, List<String> fileNameList,
                                   List<String> pageIdList, List<String> md5List, String ip,
                                   String port, String address) {
        String organCode = storageNonBankEcmProperties.getOrganCode();
        String comCode = storageNonBankEcmProperties.getComCode();
        String licenseKey = storageNonBankEcmProperties.getKey();
        String role = storageNonBankEcmProperties.getRole();
        String referer = storageNonBankEcmProperties.getReferer();

        AssertUtils.isNull(appCode, "业务号不为空");
        AssertUtils.isNull(businessNo, "批次号不为空");
        AssertUtils.isNull(userCode, "用户id不为空");
        AssertUtils.isNull(userName, "用户名不为空");
        AssertUtils.isNull(fileNameList, "文件名不为空");

        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setUserCode(userCode);
        ecmBaseData.setUserName(userName);
        ecmBaseData.setOrgCode(organCode);
        ecmBaseData.setOrgName(organCode);
        ecmBaseData.setRoleCode(role);

        EcmMetaData ecmMetaData = new EcmMetaData();
        EcmBatch metaDataBatch = new EcmBatch();
        metaDataBatch.setAppCode(appCode);
        metaDataBatch.setAppName(appName);
        metaDataBatch.setBusiNo(businessNo);
        metaDataBatch.setComCode(comCode);
        //        metaDataBatch.setEcmVtree();
        EcmPages ecmPages = new EcmPages();
        List<EcmNode> ecmNodeList = new ArrayList<>();
        for (String materialsNo : materialsNoList) {
            EcmNode ecmNode = new EcmNode();
            ecmNode.setAction(delType);
            ecmNode.setId(materialsNo);
            List<EcmPage> ecmPageList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(fileNameList)) {
                for (String fileName : fileNameList) {
                    EcmPage ecmPage = new EcmPage();
                    ecmPage.setFileName(fileName);
                    ecmPageList.add(ecmPage);
                }
            }
            if (!CollectionUtils.isEmpty(pageIdList)) {
                for (String pageId : pageIdList) {
                    EcmPage ecmPage = new EcmPage();
                    ecmPage.setPageId(pageId);
                    ecmPageList.add(ecmPage);
                }
            }
            if (!CollectionUtils.isEmpty(md5List)) {
                for (String md5 : md5List) {
                    EcmPage ecmPage = new EcmPage();
                    ecmPage.setMd5(md5);
                    ecmPageList.add(ecmPage);
                }
            }
            ecmNode.setPage(ecmPageList);
            ecmNodeList.add(ecmNode);
        }
        ecmPages.setNode(ecmNodeList);
        metaDataBatch.setPages(ecmPages);

        ecmMetaData.addMetaDataBatch(metaDataBatch);
        QueryDownloadBusiData queryImgBusiData = new QueryDownloadBusiData();
        queryImgBusiData.setEcmBaseData(ecmBaseData);
        queryImgBusiData.setEcmMetaData(ecmMetaData);

        // 拼接请求参数
        EcmRequestBusiData ecmRequestBusiData = new EcmRequestBusiData();
        ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_DEL);
        ecmRequestBusiData.setFormat(EcmRequestBusiData.FORMAT_XML);
        ecmRequestBusiData.setBusiData(queryImgBusiData);

        // 组装请求
        EcmRequest ecmRequest = new EcmRequest();
        ecmRequest.setIp(ip);
        ecmRequest.setPort(port);
        ecmRequest.setAddress(address);
        ecmRequest.setKey(licenseKey);
        ecmRequest.setEcmRequestBusiData(ecmRequestBusiData);

        String resourceAccessUrl = ecmRequest.getRequestUrl();
        HttpUtils httpUtils = HttpUtils.init();
        httpUtils.setHeader("Referer", referer);
        Map<String, String> result = httpUtils.post(resourceAccessUrl);
        EcmReceive result1 = XmlUtils.unmarshal(result.get("result"), EcmReceive.class);
        return "200".equals(result1.getResponseCode()) ? Result.success(result1)
                : Result.error(result1.getResponseMsg(), ResultCode.PARAM_ERROR);
    }

    /**
     * 文件下载
     *
     * @param ip      影像系统IP
     * @param port    影像系统端口
     * @param address 影像接口地址
     * @param key     影像授权密钥
     * @param appCode 业务类型
     * @param busiNo  批次号
     * @param pageid
     * @return Result
     */
    private List<EcmPage> getFileUrl(String ip, String port, String address, String key,
                                     String appCode, String busiNo, List<String> pageid) {
        String organCode = storageNonBankEcmProperties.getOrganCode();
        String role = storageNonBankEcmProperties.getRole();
        String referer = storageNonBankEcmProperties.getReferer();

        // 拼接xml参数
        QueryDownloadBusiData queryImgBusiData = new QueryDownloadBusiData();
        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setUserCode(StateConstants.SUNAM_CODE);
        ecmBaseData.setUserName(StateConstants.SUNAM_CODE);
        ecmBaseData.setOrgCode(organCode);
        ecmBaseData.setOrgName(organCode);
        ecmBaseData.setRoleCode(role);
        queryImgBusiData.setEcmBaseData(ecmBaseData);

        EcmMetaData ecmMetaData = new EcmMetaData();
        EcmBatch metaDataBatch = new EcmBatch();
        metaDataBatch.setAppCode(appCode);
        metaDataBatch.setBusiNo(busiNo);
        ecmMetaData.addMetaDataBatch(metaDataBatch);
        EcmPageids ecmPageids = new EcmPageids();
        ecmPageids.setPageid(pageid);
        ecmMetaData.setEcmPageids(ecmPageids);
        queryImgBusiData.setEcmMetaData(ecmMetaData);

        // 拼接请求参数
        EcmRequestBusiData ecmRequestBusiData = new EcmRequestBusiData();
        ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_RESOURCE_ACCESS);
        ecmRequestBusiData.setFormat(EcmRequestBusiData.FORMAT_XML);
        ecmRequestBusiData.setBusiData(queryImgBusiData);

        // 组装请求
        EcmRequest ecmRequest = new EcmRequest();
        ecmRequest.setIp(ip);
        ecmRequest.setPort(port);
        ecmRequest.setAddress(address);
        ecmRequest.setKey(key);
        ecmRequest.setEcmRequestBusiData(ecmRequestBusiData);

        String resourceAccessUrl = ecmRequest.getRequestUrl();
        HttpUtils httpUtils = HttpUtils.init();
        httpUtils.setHeader("Referer", referer);
        Map<String, String> result = httpUtils.post(resourceAccessUrl);
        EcmReceive result1 = XmlUtils.unmarshal(result.get("result"), EcmReceive.class);
        return "200".equals(result1.getResponseCode()) ? result1.getPages().getPageList() : null;
    }

    /**
     * @param ecmRoot
     * @param uploadFileInputStreams
     * @return Result
     */
    public Result uploadSendSunICMSBatch(EcmRoot ecmRoot,
                                         List<InputStream> uploadFileInputStreams) {
        String ip = storageNonBankEcmProperties.getIp();
        Integer socketPort = storageNonBankEcmProperties.getSocketPort();
        String systemId = storageNonBankEcmProperties.getSystemName();
        String licenseKey = storageNonBankEcmProperties.getKey();
        String path = storageNonBankEcmProperties.getLocation();

        File file2 = new File(path);
        if (!file2.exists() && !file2.isDirectory()) {
            file2.mkdir();

        }
        String arcNo = ecmRoot.getEcmMetaDataSingle().getEcmBatch().getBusiNo();
        String xmlPath = path + "/" + "busi.xml";
        String uploadPath = path + "/";
        String zipPath = path + "/" + arcNo + ".zip";
        List<String> delFiles = new ArrayList<>();
        delFiles.add(xmlPath);
        delFiles.add(uploadPath);
        delFiles.add(zipPath);
        File file1 = new File(path);
        if (!file1.exists() && !file1.isDirectory()) {
            file1.mkdir();
        }
        try {
            String xml = XmlUtils.marshalToFile(ecmRoot, xmlPath);
            log.debug("uploadSendSunICMS:{}", xml);
            File file = new File(xmlPath);
            XmlUtils.xmlStrConvertFile(xml, file);
            List<File> zipFileList = new ArrayList<>();
            zipFileList.add(file);
            for (int i = 0; i < uploadFileInputStreams.size(); i++) {
                String uploadFileName = ecmRoot.getEcmMetaDataSingle().getEcmBatch().getPages()
                        .getNode().get(i).getPage().get(0).getFileName();
                String uploadPathFile = uploadPath + uploadFileName;
                File uploadFile = new File(uploadPathFile);
                FileUtils.cpFile(uploadFileInputStreams.get(i), uploadFile);
                zipFileList.add(uploadFile);
            }

            FileOutputStream fos2 = new FileOutputStream(new File(zipPath));
            ZipUtils.toZip(zipFileList, fos2);
            // -------------------
            AutoScanApi autoScanApi = new AutoScanApi(ip, socketPort.intValue(),
                    systemId + "#" + licenseKey);
            autoScanApi.setFormat("xml");
            String returnMsg = autoScanApi.ScanImageFile("", zipPath);
            EcmListResponse ecmResponse = XmlUtils.unmarshal(returnMsg, EcmListResponse.class);
            log.debug("uploadSendSunICMS-returnMsg:{}", returnMsg);
            if ("200".equals(ecmResponse.getResponseCode())
                    || "1".equals(ecmResponse.getResponseCode())) {
                return Result.success(ecmResponse.getPages());
            } else {
                return Result.error(ecmResponse.getResponseMsg(), ResultCode.PARAM_ERROR);
            }
            // ------------------
        } catch (Exception e) {
            log.error("uploadSendSunICMS-error", e);
            throw new SunyardException(ResultCode.PARAM_ERROR, "附件上传失败");
        } finally {
            // 强制删除生成的所有文件
            for (String f : delFiles) {
                FileUtils.delete(f);
            }
        }
    }

    /**
     * @param ip       影像系统IP
     * @param port     影像系统端口
     * @param address  影像接口地址
     * @param key      影像授权密钥
     * @param appCode  业务类型
     * @param busiNo   批次号
     * @param userCode 操作人ID
     * @param userName 操作人姓名
     * @param roleCode 角色
     * @return
     */
    public Result getResourceSyd(String ip, String port, String address, String key, String appCode,
                                 String busiNo, String userCode, String userName, String roleCode,
                                 String code) {
        String systemId = storageNonBankEcmProperties.getSystemName();

        //拼接xml参数
        QueryImgBusiData queryImgBusiData = new QueryImgBusiData();
        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setUserCode(userCode);
        ecmBaseData.setUserName(userName);
        ecmBaseData.setOrgCode("00000000");
        ecmBaseData.setOrgName("总公司");
        ecmBaseData.setRoleCode(roleCode);
        queryImgBusiData.setEcmBaseData(ecmBaseData);

        EcmMetaData ecmMetaData = new EcmMetaData();
        EcmBatch metaDataBatch = new EcmBatch();
        metaDataBatch.setAppCode(appCode);
        metaDataBatch.setBusiNo(busiNo);
        metaDataBatch.setAppName("业务");
        ecmMetaData.addMetaDataBatch(metaDataBatch);
        queryImgBusiData.setEcmMetaData(ecmMetaData);

        //拼接请求参数
        EcmRequestBusiData ecmRequestBusiData = new EcmRequestBusiData();
        ecmRequestBusiData.setCode(code);
        ecmRequestBusiData.setFormat(EcmRequestBusiData.FORMAT_XML);
        ecmRequestBusiData.setBusiData(queryImgBusiData);
        //组装请求
        EcmRequest ecmRequest = new EcmRequest();
        ecmRequest.setIp(ip);
        ecmRequest.setPort(port);
        ecmRequest.setAddress(address);
        ecmRequest.setKey(key);
        ecmRequest.setEcmRequestBusiData(ecmRequestBusiData);

        String resourceAccessUrl = ecmRequest.getRequestUrl();
        HttpUtils httpUtil = HttpUtils.init();
        httpUtil.setHeader("Referer", systemId);
        Map<String, String> result = httpUtil.post(resourceAccessUrl);
        return "200".equals(result.get("statusCode"))
                ? Result.success(JSON
                        .toJSONString(XmlUtils.unmarshal(result.get("result"), EcmReceive.class)))
                : null;

    }
}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/12/20 zhouleibin
 * creat
 */
