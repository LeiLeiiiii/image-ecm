package com.sunyard.module.storage.api;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.storage.constant.ApiConstants;
import com.sunyard.module.storage.dto.ecm.EcmFileRequestBody;
import com.sunyard.module.storage.dto.ecm.EcmRoot;

/**
 * @author raochangmei
 * @Desc
 * @date 2022/6/15 15:58
 */
@FeignClient(name = ApiConstants.NAME)
public interface StorageEcmFileApi {
    String PREFIX = ApiConstants.PREFIX + "/storageEcmFile/";

    /**
     * uploadFileSimple
     *
     * @param uploadFilebytes uploadFilebytes
     * @param no              no
     * @param type            type
     * @param filename        filename
     * @return Result
     */
    @PostMapping(PREFIX + "uploadFileSimple")
    Result uploadFileSimple(@RequestBody byte[] uploadFilebytes, @RequestParam("no") String no,
                            @RequestParam("type") String type, @RequestParam("filename") String filename);

    /**
     * uploadFilebytes
     *
     * @param uploadFilebytes uploadFilebytes
     * @param no              no
     * @param type            type
     * @param filename        filename
     * @return Result
     */
    @PostMapping(PREFIX + "uploadNewFileSimple")
    Result uploadNewFileSimple(@RequestBody byte[] uploadFilebytes, @RequestParam("no") String no,
                               @RequestParam("type") String type, @RequestParam("filename") String filename);

    /**
     * uploadFileSimpleBatch
     *
     * @param bytes     bytes
     * @param no        no
     * @param type      type
     * @param filenames filenames
     * @return Result
     */
    @PostMapping(PREFIX + "uploadFileSimpleBatch")
    Result uploadFileSimpleBatch(@RequestBody List<byte[]> bytes, @RequestParam("no") String no,
                                 @RequestParam("type") String type, @RequestParam("filenames") String[] filenames);

    /**
     * 批量上传
     *
     * @param fileBytes   文件字节数组列表
     * @param arcId       档案ID-批次号
     * @param arcTypeNo   档案类型编号
     * @param fileNames   文件名称列表
     * @param materialsNo 资料类型编号
     * @return Result
     */
    @PostMapping(PREFIX + "uploadArcFileBatch")
    Result uploadArcFileBatch(@RequestBody List<byte[]> fileBytes,
                              @RequestParam("arcId") String arcId,
                              @RequestParam("arcTypeNo") String arcTypeNo,
                              @RequestParam("fileNames") String[] fileNames,
                              @RequestParam("materialsNo") String materialsNo);

    /**
     * uploadSendSunICMS
     *
     * @param ecmFileRequestBody ecmFileRequestBody
     * @return Result
     */
    @PostMapping(PREFIX + "uploadSendSunICMS")
    Result uploadSendSunICMS(@RequestBody EcmFileRequestBody ecmFileRequestBody);

    /**
     * uploadArcFile
     *
     * @param uploadFilebytes uploadFilebytes
     * @param fileName        fileName
     * @param arcNo           arcNo
     * @param arcTitle        arcTitle
     * @param arcTypeNo       arcTypeNo
     * @param materialsNo     materialsNo
     * @param remark          remark
     * @return Result
     */
    @PostMapping(PREFIX + "uploadArcFile")
    Result uploadArcFile(@RequestBody byte[] uploadFilebytes, @RequestParam("fileName") String fileName,
                         @RequestParam("arcNo") String arcNo, @RequestParam("arcTitle") String arcTitle,
                         @RequestParam("arcTypeNo") String arcTypeNo, @RequestParam("materialsNo") String materialsNo,
                         @RequestParam("remark") String remark);

    /**
     * uploadNewSendSunICMS
     *
     * @param ecmFileRequestBody ecmFileRequestBody
     * @return Result
     */
    @PostMapping(PREFIX + "uploadNewSendSunICMS")
    Result uploadNewSendSunICMS(@RequestBody EcmFileRequestBody ecmFileRequestBody);

    /**
     * 不带文件的上传，用于pageid指定删除
     *
     * @param ecmRoot ecmRoot
     * @return Result
     */
    @PostMapping(PREFIX + "uploadSendSunICMSNoFile")
    Result uploadSendSunICMSNoFile(@RequestBody EcmRoot ecmRoot);

    /**
     * 文件下载
     *
     * @param appCode    业务类型
     * @param businessNo 批次号
     * @param pageid     pageid
     * @return Result EcmReceive
     */
    @PostMapping(PREFIX + "getResourceSydEcm0010ByPageId")
    Result getResourceSydEcm0010ByPageId(@RequestParam("appCode") String appCode,
                                         @RequestParam("businessNo") String businessNo, @RequestParam("pageid") String[] pageid);

    /**
     * 文件下载
     *
     * @param appCode    业务类型
     * @param businessNo 批次号
     * @param userCode   用户id
     * @param userName   用户名
     * @return
     */
    @PostMapping(PREFIX + "getResourceSydEcm0009")
    Result getResourceSydEcm0009(@RequestParam("appCode") String appCode, @RequestParam("businessNo") String businessNo,
                                 @RequestParam("userCode") String userCode, @RequestParam("userName") String userName);

    /**
     * 删除影像中的档案文件通过文件名
     *
     * @param appCode
     * @param businessNo
     * @param userId
     * @param userName
     * @param materialsNo
     * @param fileNameList
     * @return
     */
    @PostMapping(PREFIX + "delArcFileByFileName")
    Result delBatchArcFileByFileName(@RequestParam("appName") String appName, @RequestParam("appCode") String appCode, @RequestParam("businessNo") String businessNo,
                                     @RequestParam("userId") String userId, @RequestParam("userName") String userName,
                                     @RequestParam("materialsNo") String materialsNo, @RequestParam("fileNameList") List<String> fileNameList);

    /**
     * 删除影像中的档案文件通过pageId  pageId 从档案上传到影像后成功返回结果中有pageId
     *
     * @param appCode
     * @param businessNo
     * @param userId
     * @param userName
     * @param materialsNo
     * @param pageIdList
     * @return
     */
    @PostMapping(PREFIX + "delArcFileByPageId")
    Result delBatchArcFileByPageId(@RequestParam("appName") String appName, @RequestParam("appCode") String appCode, @RequestParam("businessNo") String businessNo,
                                   @RequestParam("userId") String userId, @RequestParam("userName") String userName,
                                   @RequestParam("materialsNo") String materialsNo, @RequestParam("pageIdList") List<String> pageIdList);

    /**
     * 删除影像中的档案文件通过文件md5
     *
     * @param appCode
     * @param businessNo
     * @param userId
     * @param userName
     * @param materialsNo
     * @param md5List
     * @return
     */
    @PostMapping(PREFIX + "delBatchArcFileByMd5")
    Result delBatchArcFileByMd5(@RequestParam("appName") String appName, @RequestParam("appCode") String appCode, @RequestParam("businessNo") String businessNo,
                                @RequestParam("userId") String userId, @RequestParam("userName") String userName,
                                @RequestParam("materialsNo") String materialsNo, @RequestParam("md5List") List<String> md5List);


}
