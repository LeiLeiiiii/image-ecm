package com.sunyard.sunafm.service;

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.module.storage.api.FileStorageApi;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.vo.UploadListVO;
import com.sunyard.sunafm.constant.AfmConstant;
import com.sunyard.sunafm.constant.OcrConstant;
import com.sunyard.sunafm.dto.AfmDetNoteDetailsDTO;
import com.sunyard.sunafm.dto.AfmDetOnlineFileDTO;
import com.sunyard.sunafm.dto.AfmDetOnlinePsDetDTO;
import com.sunyard.sunafm.mapper.AfmFileExifMapper;
import com.sunyard.sunafm.mapper.AfmImagePsNoteMapper;
import com.sunyard.sunafm.po.AfmFileExif;
import com.sunyard.sunafm.po.AfmImagePsNote;
import com.sunyard.sunafm.util.CommonHttpRequest;
import com.sunyard.sunafm.util.Md5Utils;
import com.sunyard.sunafm.vo.AfmDetOnlineListVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author P-JWei
 * @date 2024/4/1 9:42:06
 * @title
 * @description 在线检测/篡改检测实现类
 */
@Slf4j
@Service
public class DetFalsifyService {

    @Value("${storage.url}")
    private String storageUrl;
    @Value("${ps.appId}")
    private String psAppId;
    @Value("${ps.appSecret}")
    private String psAppSecret;
    @Value("${ps.url}")
    private String psUrl;
    @Resource
    private AfmImagePsNoteMapper afmImagePsNoteMapper;
    @Resource
    private AfmFileExifMapper afmFileExifMapper;
    @Resource
    private FileStorageApi storageApi;



    /**
     * 开始检测
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<AfmDetOnlinePsDetDTO> det(MultipartFile file, Long exifId, String token, Long userId) {
        // 使用Assert确保至少有一个参数是非空的
        Assert.isTrue(null != file || null != exifId, "参数错误");
        //文件byte数组去篡改检测
        byte[] fileByte;
        String fileName;
        String psFileBase64;
        AfmDetOnlinePsDetDTO result = new AfmDetOnlinePsDetDTO();
        AfmImagePsNote newAfmImagePsNote = new AfmImagePsNote();
        if (null == exifId) {
            try {
                fileByte = file.getBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            fileName = file.getOriginalFilename();
            //根据文件生成md5
            String fileMd5 = Md5Utils.calculateMD5(fileByte);
            //用md5去查文件表，拿exifId，拿不到则插入
            AfmFileExif afmFileExif = afmFileExifMapper
                    .selectOne(new LambdaQueryWrapper<AfmFileExif>()
                            .eq(AfmFileExif::getFileIndex, fileMd5)
                            .eq(AfmFileExif::getFileMd5, fileMd5)
                            .eq(AfmFileExif::getSourceSys, AfmConstant.AFM_SOURCESYS));
            if (null != afmFileExif) {
                exifId = afmFileExif.getExifId();
                BeanUtils.copyProperties(afmFileExif, newAfmImagePsNote);
            } else {
                //上传文件
                UploadListVO uploadListVO = getUploadListVO(userId, fileByte, fileName);
                List<UploadListVO> uploadList = new ArrayList<>();
                uploadList.add(uploadListVO);
                Result<List<SysFileDTO>> listResult = storageApi.uploadBatch(uploadList);
                //存入把文件流存入文件表
                afmFileExif = getAfmFileExif(listResult.getData().get(0));
                afmFileExif.setFileName(fileName);
                afmFileExif.setFileIndex(afmFileExif.getSourceSys()+"_"+afmFileExif.getFileIndex());
                afmFileExifMapper.insert(afmFileExif);
                exifId = afmFileExif.getExifId();
                BeanUtils.copyProperties(afmFileExif, newAfmImagePsNote);
            }
        } else {
            // 通过文件url获取文件流
            AfmFileExif afmFileExif = afmFileExifMapper.selectById(exifId);
            HashMap<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Cookie", "Sunyard-Token=" + token);
            HttpEntity execute = CommonHttpRequest
                    .get(afmFileExif.getFileUrl())
                    .header(headerMap)
                    .execute();
            InputStream inputStream;
            try {
                inputStream = execute.getContent();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            fileByte = IoUtil.readBytes(inputStream);
            fileName = afmFileExif.getFileName();
            BeanUtils.copyProperties(afmFileExif, newAfmImagePsNote);
        }
        // exifId 去篡改记录表找记录，是否已检测过，已检测过则直接去拿结果并返回
        List<AfmImagePsNote> afmImagePsNotes = afmImagePsNoteMapper
                .selectList(new LambdaQueryWrapper<AfmImagePsNote>()
                        .eq(AfmImagePsNote::getExifId, exifId)
                        .orderByDesc(AfmImagePsNote::getPsDetTime));
        if (!CollectionUtils.isEmpty(afmImagePsNotes)) {
            //存在即返回最新数据
            AfmImagePsNote afmImagePsNote = afmImagePsNotes.get(0);
            //获取含篡改信息的文件url
            AfmFileExif afmFileExif = afmFileExifMapper.selectById(afmImagePsNote.getPsDetFileId());
            if (null != afmFileExif) {
                HashMap<String, String> headerMap = new HashMap<>(1);
                headerMap.put("Cookie", "Sunyard-Token=" + token);
                HttpEntity execute = CommonHttpRequest
                        .get(afmFileExif.getFileUrl())
                        .header(headerMap)
                        .execute();
                InputStream inputStream;
                try {
                    inputStream = execute.getContent();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                result.setPsFileBase64(Base64.getEncoder().encodeToString(IoUtil.readBytes(inputStream)));
            }
            result.setSourceFileBase64(Base64.getEncoder().encodeToString(fileByte));
            result.setPsCount(afmImagePsNote.getPsCount());
        } else {
            // 没有记录则拿到文件，调用合合接口进行检测，返回结果并存入篡改记录表
            String responseString = getPsResult(fileByte);
            if (!AfmConstant.SUCC_CODE.equals(JSONObject.parseObject(responseString).get("code").toString())) {
                return Result.error("远程篡改接口异常", ResultCode.SYSTEM_BUSY_ERROR);
            }
            //处理接口，存入检测记录进记录表并把返回得文件流存入存储服务
            Map<String, Object> analysisMap = analysisPsResponse(responseString);
            int psCount = 0;
            Long psExifId;
            if (!CollectionUtils.isEmpty(analysisMap)) {
                //有篡改
                psCount = Integer.parseInt(analysisMap.get("count").toString());
                psFileBase64 = analysisMap.get("base64").toString();
                //上传文件
                UploadListVO uploadListVO = getUploadListVO(userId, Base64.getDecoder().decode(analysisMap.get("base64").toString()), "ps_" + fileName);
                List<UploadListVO> uploadList = new ArrayList<>();
                uploadList.add(uploadListVO);
                Result<List<SysFileDTO>> listResult = storageApi.uploadBatch(uploadList);
                //存入把文件流存入文件表
                AfmFileExif afmFileExif = getAfmFileExif(listResult.getData().get(0));
                afmFileExif.setFileName("ps_" + fileName);
                afmFileExif.setFileIndex(afmFileExif.getSourceSys()+"_"+afmFileExif.getFileIndex());
                afmFileExifMapper.insert(afmFileExif);
                psExifId = afmFileExif.getExifId();
                result.setPsFileBase64(psFileBase64);
            } else {
                //无篡改 传文件id
                psExifId = exifId;
                //无篡改返回原文件id
                result.setPsFileBase64(Base64.getEncoder().encodeToString(fileByte));
            }
            // 把篡改记录表存入
            getAfmImagePsNote(exifId, fileName, psCount, psExifId, newAfmImagePsNote);
            afmImagePsNoteMapper.insert(newAfmImagePsNote);
            result.setSourceFileBase64(Base64.getEncoder().encodeToString(fileByte));
            result.setPsCount(psCount);
        }

        return Result.success(result);
    }

    /**
     * 选择文件
     */
    public Result<PageInfo<AfmDetOnlineFileDTO>> chooseFile(AfmDetOnlineListVO vo, PageForm pageForm) {
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<AfmFileExif> afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                .eq(StringUtils.hasText(vo.getSourceSys()), AfmFileExif::getSourceSys, vo.getSourceSys())
                .eq(StringUtils.hasText(vo.getSourceSys()), AfmFileExif::getBusinessType, vo.getSourceSys())
                .eq(StringUtils.hasText(vo.getSourceSys()), AfmFileExif::getBusinessIndex, vo.getSourceSys())
                .eq(StringUtils.hasText(vo.getSourceSys()), AfmFileExif::getMaterialType, vo.getSourceSys())
                .eq(StringUtils.hasText(vo.getSourceSys()), AfmFileExif::getFileName, vo.getSourceSys())
                .eq(StringUtils.hasText(vo.getSourceSys()), AfmFileExif::getUploadUserName, vo.getSourceSys())
                .eq(StringUtils.hasText(vo.getSourceSys()), AfmFileExif::getUploadOrg, vo.getSourceSys())
                .between(!ObjectUtils.isEmpty(vo.getCreateTimeStart()) && !ObjectUtils.isEmpty(vo.getCreateTimeEnd()),
                        AfmFileExif::getCreateTime, vo.getCreateTimeStart(), vo.getCreateTimeEnd())
                .orderByDesc(AfmFileExif::getCreateTime)
        );
        PageInfo<AfmFileExif> afmFileExifPageInfo = new PageInfo<>(afmFileExifs);
        List<AfmFileExif> list = afmFileExifPageInfo.getList();
        PageInfo<AfmDetOnlineFileDTO> result = new PageInfo<>();
        if (!CollectionUtils.isEmpty(list)) {
            List<AfmDetOnlineFileDTO> afmDetOnlineFileDTOS = new ArrayList<>();
            list.stream().forEach(item -> {
                AfmDetOnlineFileDTO afmDetOnlineFileDTO = new AfmDetOnlineFileDTO();
                BeanUtils.copyProperties(item, afmDetOnlineFileDTO);
                afmDetOnlineFileDTO.setUploadTime(item.getCreateTime());
                afmDetOnlineFileDTOS.add(afmDetOnlineFileDTO);
            });
            result.setList(afmDetOnlineFileDTOS);
            result.setTotal(afmFileExifPageInfo.getTotal());
        }
        return Result.success(result);
    }

    /**
     * 篡改记录详情
     */
    public Result<AfmDetNoteDetailsDTO> noteDetails(Long id) {
        Assert.isTrue(null != id, "参数错误");
        AfmImagePsNote afmImagePsNote = afmImagePsNoteMapper.selectById(id);
        AfmFileExif afmFileExif = afmFileExifMapper.selectById(afmImagePsNote.getExifId());
        AfmDetNoteDetailsDTO afmDetNoteDetailsDTO = new AfmDetNoteDetailsDTO();
        if (StringUtils.hasText(afmFileExif.getFileExif())) {
            afmDetNoteDetailsDTO = JSONObject.parseObject(afmFileExif.getFileExif(), AfmDetNoteDetailsDTO.class);
            BeanUtils.copyProperties(afmImagePsNote, afmDetNoteDetailsDTO);
        }
        return Result.success(afmDetNoteDetailsDTO);
    }

    /**
     * 获取缩略图
     */
    public void thumbnail(Long fileId) {

    }

    /**
     * 获取含篡改信息图
     */
    public void originalDrawing(Long fileId) {

    }


    /**
     * 获取篡改检测对象
     */
    private void getAfmImagePsNote(Long exifId, String fileName, int psCount, Long psExifId, AfmImagePsNote afmImagePsNote) {
        afmImagePsNote.setExifId(exifId);
        afmImagePsNote.setFileName(fileName);
        afmImagePsNote.setPsDetTime(new Date());
        afmImagePsNote.setPsDetResult(psCount == 0 ? OcrConstant.PS_DET_RESULT_NO : OcrConstant.PS_DET_RESULT_YES);
        afmImagePsNote.setPsCount(psCount);
        afmImagePsNote.setPsDetFileId(psExifId);
        afmImagePsNote.setCreateTime(null);
    }

    /**
     * 获取篡改文件对象
     */
    private AfmFileExif getAfmFileExif(SysFileDTO upload) {
        AfmFileExif afmFileExif = new AfmFileExif();
        afmFileExif.setSourceSys(AfmConstant.AFM_SOURCESYS);
        afmFileExif.setFileIndex(upload.getFileMd5());
        afmFileExif.setFileMd5(upload.getFileMd5());
        afmFileExif.setFileUrl(storageUrl + upload.getId());
        return afmFileExif;
    }

    /**
     * 生成文件上传vo
     */
    private UploadListVO getUploadListVO(Long userId, byte[] data, String fileName) {
        UploadListVO uploadListVO = new UploadListVO();
        uploadListVO.setFileByte(data);
        uploadListVO.setFileName(fileName);
        uploadListVO.setStEquipmentId(OcrConstant.MINIO);
        uploadListVO.setUserId(userId);
        uploadListVO.setFileSource(OcrConstant.APPLICATION);
        return uploadListVO;
    }

    /**
     * 请求合合ps检测接口
     */
    private String getPsResult(byte[] fileByte) {
        log.info("文件大小：{}", fileByte.length);
        Map<String, String> map = new HashMap<>();
        map.put("x-ti-app-id", psAppId);
        map.put("x-ti-secret-code", psAppSecret);
        HttpEntity execute = CommonHttpRequest.post(psUrl)
                .header(map)
                .octetStream(fileByte)
                .execute();
        String responseString = CommonHttpRequest.getResponseString(execute);
        log.info("合合ps检测结果：{}",responseString);
        return responseString;
    }

    /**
     * 解析json
     */
    private Map<String, Object> analysisPsResponse(String json) {
        JSONObject psObj = JSONObject.parseObject(json)
                .getJSONObject("result")
                .getJSONObject("image_property")
                .getJSONObject("ps");
        Map<String, Object> map = new HashMap<>(2);
        //是否篡改 0 无篡改 1 有篡改
        Integer isTampered = Integer.valueOf(psObj.get("is_tampered").toString());
        if (1 == isTampered) {
            //篡改处
            map.put("count", psObj.getJSONArray("tampered_scores").size());
            //含篡改区域的文件的base64
            map.put("base64", psObj.get("image").toString());
        }
        return map;
    }
}
