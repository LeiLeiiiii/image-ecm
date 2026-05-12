package com.sunyard.sunafm.manager;

import cn.hutool.core.io.IoUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.afm.api.dto.AfmDetPsDTO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.module.storage.api.FileStorageApi;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.vo.UploadListVO;
import com.sunyard.sunafm.constant.AfmConstant;
import com.sunyard.sunafm.constant.OcrConstant;
import com.sunyard.sunafm.mapper.AfmFileExifMapper;
import com.sunyard.sunafm.mapper.AfmImagePsNoteMapper;
import com.sunyard.sunafm.po.AfmFileExif;
import com.sunyard.sunafm.po.AfmImagePsNote;
import com.sunyard.sunafm.service.DetFalsifyService;
import com.sunyard.sunafm.util.CommonHttpRequest;
import org.apache.http.HttpEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author P-JWei
 * @date 2024/4/23 14:30:30
 * @title
 * @description 篡改检测实现类
 */
@Service
public class FalsifyService {
    @Value("${storage.url}")
    private String storageUrl;
    @Resource
    private AfmFileExifMapper afmFileExifMapper;
    @Resource
    private AfmImagePsNoteMapper afmImagePsNoteMapper;
    @Resource
    private FileStorageApi storageApi;
    @Resource
    private DetFalsifyService detFalsifyService;


    /**
     *  开始检测
     */
    public void detPs(AfmDetPsDTO dto) {
        //检测入参
        checkParam(dto);
        Long checkId;
        //通过拿到文件md5，判断是否上传过
        AfmFileExif afmFileExif = afmFileExifMapper
                .selectOne(new LambdaQueryWrapper<AfmFileExif>()
                        .eq(AfmFileExif::getFileMd5, dto.getFileMd5())
                        .eq(AfmFileExif::getFileIndex, dto.getSourceSys()+"_"+dto.getFileIndex())
                        .eq(AfmFileExif::getBusinessIndex, dto.getBusinessIndex())
                        .eq(AfmFileExif::getMaterialType, dto.getMaterialType()));
        if (null != afmFileExif) {
            //采用已存在的文件相关信息进行检测
            checkId = afmFileExif.getExifId();
        } else {
            //采用传入的文件信息进行检测
            //上传文件
            Map<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Cookie", "Sunyard-Token=" + dto.getFileToken());
            HttpEntity execute = CommonHttpRequest.get(dto.getFileUrl()).header(headerMap).execute();
            InputStream inputStream;
            try {
                inputStream = execute.getContent();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            UploadListVO uploadListVO = getUploadListVO(IoUtil.readBytes(inputStream), dto.getFileName());
            List<UploadListVO> uploadList = new ArrayList<>();
            uploadList.add(uploadListVO);
            Result<List<SysFileDTO>> listResult = storageApi.uploadBatch(uploadList);
            //存入把文件流、存入文件表
            AfmFileExif psAfmFileExif = new AfmFileExif();
            BeanUtils.copyProperties(dto, psAfmFileExif);
            psAfmFileExif.setFileUrl(storageUrl + listResult.getData().get(0).getId());
            psAfmFileExif.setFileIndex(psAfmFileExif.getSourceSys()+"_"+psAfmFileExif.getFileIndex());
            afmFileExifMapper.insert(psAfmFileExif);
            checkId = psAfmFileExif.getExifId();
        }
        //进行检测
        detFalsifyService.det(null, checkId, dto.getFileToken(), null);
    }

    /**
     * 获取ps结果
     */
    public Map psResult(AfmDetPsDTO dto) {
        Map<String, String> map = new HashMap<>();
        Assert.notNull(dto.getFileMd5(), "文件MD5不能为空");
        AfmFileExif afmFileExif = afmFileExifMapper
                .selectOne(new LambdaQueryWrapper<AfmFileExif>()
                        .eq(AfmFileExif::getFileMd5, dto.getFileMd5())
                        .eq(AfmFileExif::getFileIndex, dto.getSourceSys()+"_"+dto.getFileIndex())
                        .eq(AfmFileExif::getBusinessIndex, dto.getBusinessIndex())
                        .eq(AfmFileExif::getMaterialType, dto.getMaterialType()));
        if (null != afmFileExif) {
            List<AfmImagePsNote> afmImagePsNotes = afmImagePsNoteMapper
                    .selectList(new LambdaQueryWrapper<AfmImagePsNote>()
                            .eq(AfmImagePsNote::getExifId, afmFileExif.getExifId())
                            .orderByDesc(AfmImagePsNote::getPsDetTime));
            if (!CollectionUtils.isEmpty(afmImagePsNotes)) {
                AfmFileExif psAfmFileExif = afmFileExifMapper.selectById(afmImagePsNotes.get(0).getPsDetFileId());
                map.put("psCount", afmImagePsNotes.get(0).getPsCount().toString());
                map.put("psFileUrl", psAfmFileExif.getFileUrl());
            } else {
                map.put("psCount", "");
                map.put("psFileUrl", "");
            }
        } else {
            map.put("psCount", "");
            map.put("psFileUrl", "");
        }

        return map;
    }

    /**
     * 校验参数
     */
    private void checkParam(AfmDetPsDTO vo) {
        AssertUtils.isNull(vo.getInvoiceType(), "接口类型不能为空");
        AssertUtils.isNull(vo.getFileMd5(), "文件md5不能为空");
        AssertUtils.isNull(vo.getBusinessIndex(), "业务主索引不能为空");
        AssertUtils.isNull(vo.getSourceSys(), "来源系统不能为空");
        AssertUtils.isNull(vo.getFileUrl(), "文件url不能为空");
        AssertUtils.isNull(vo.getFileName(), "文件名称不能为空");
        AssertUtils.isNull(vo.getFileIndex(), "文件id不能为空");
        AssertUtils.isNull(vo.getBusinessTypeCode(), "业务类型不能为空");
        AssertUtils.isNull(vo.getMaterialTypeCode(), "资料类型不能为空");
        AssertUtils.isNull(vo.getBusinessTypeName(), "业务类型中文名不能为空");
        AssertUtils.isNull(vo.getMaterialTypeName(), "资料类型中文名不能为空");
        vo.setBusinessType(vo.getBusinessTypeCode() + AfmConstant.SUFF + vo.getBusinessTypeName());
        vo.setMaterialType(vo.getMaterialTypeCode() + AfmConstant.SUFF + vo.getMaterialTypeName());
    }

    /**
     * 生成文件上传vo
     */
    private UploadListVO getUploadListVO(byte[] data, String fileName) {
        UploadListVO uploadListVO = new UploadListVO();
        uploadListVO.setFileByte(data);
        uploadListVO.setFileName(fileName);
        uploadListVO.setStEquipmentId(OcrConstant.MINIO);
        uploadListVO.setUserId(null);
        uploadListVO.setFileSource(OcrConstant.APPLICATION);
        return uploadListVO;
    }

}
