package com.sunyard.sunafm.service;

import cn.hutool.core.io.IoUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.sunafm.constant.AfmConstant;
import com.sunyard.sunafm.dto.AfmDetNoteDetailsDTO;
import com.sunyard.sunafm.dto.AfmDetNotePsDTO;
import com.sunyard.sunafm.dto.AfmDetNotePsListDTO;
import com.sunyard.sunafm.dto.AfmDetNotePsListExcelDTO;
import com.sunyard.sunafm.mapper.AfmFileExifMapper;
import com.sunyard.sunafm.mapper.AfmImagePsNoteMapper;
import com.sunyard.sunafm.po.AfmFileExif;
import com.sunyard.sunafm.po.AfmImagePsNote;
import com.sunyard.sunafm.util.CommonHttpRequest;
import com.sunyard.sunafm.vo.AfmDetNoteListVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author P-JWei
 * @date 2024/4/7 9:12:26
 * @title
 * @description 检测记录/篡改检测实现类
 */
@Slf4j
@Service
public class RecordFalsifyService {

    @Resource
    private AfmImagePsNoteMapper afmImagePsNoteMapper;
    @Resource
    private AfmFileExifMapper afmFileExifMapper;

    @Resource
    private CommonService commonService;

    /**
     * 篡改检测表格
     */
    public Result<PageInfo<AfmDetNotePsListDTO>> psDetList(AfmDetNoteListVO vo, PageForm pageForm) {
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<AfmImagePsNote> afmImagePsNotes = afmImagePsNoteMapper
                .selectList(new LambdaQueryWrapper<AfmImagePsNote>()
                        //SourceSys为AFM，说明是在线上传的文件记录，不查出
                        .ne(AfmImagePsNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                        .eq(StringUtils.hasText(vo.getSourceSys()), AfmImagePsNote::getSourceSys, vo.getSourceSys())
                        .like(StringUtils.hasText(vo.getBusinessType()), AfmImagePsNote::getBusinessType, vo.getBusinessType())
                        .like(StringUtils.hasText(vo.getMaterialType()), AfmImagePsNote::getMaterialType, vo.getMaterialType())
                        .like(StringUtils.hasText(vo.getFileName()), AfmImagePsNote::getFileName, vo.getFileName())
                        .like(StringUtils.hasText(vo.getBusinessIndex()), AfmImagePsNote::getBusinessIndex, vo.getBusinessIndex())
                        .eq(null != vo.getDetResult(), AfmImagePsNote::getPsDetResult, vo.getDetResult())
                        .eq(StringUtils.hasText(vo.getUploadUserName()), AfmImagePsNote::getUploadUserName, vo.getUploadUserName())
                        .between(!ObjectUtils.isEmpty(vo.getToDetTime()) && !ObjectUtils.isEmpty(vo.getDoDetTime()),
                                AfmImagePsNote::getPsDetTime, vo.getToDetTime(), vo.getDoDetTime())
                        .orderByDesc(AfmImagePsNote::getPsDetTime)
                );
        Map<String, String> afmSource = commonService.getAfmSource();
        PageInfo<AfmImagePsNote> afmImagePsNotePageInfo = new PageInfo<>(afmImagePsNotes);
        PageInfo<AfmDetNotePsListDTO> result = new PageInfo<>();
        if (!CollectionUtils.isEmpty(afmImagePsNotes)) {
            List<AfmDetNotePsListDTO> resultList = new ArrayList<>();
            afmImagePsNotePageInfo.getList().forEach(item -> {
                AfmDetNotePsListDTO afmDetNotePsListDTO = new AfmDetNotePsListDTO();
                BeanUtils.copyProperties(item, afmDetNotePsListDTO);
                afmDetNotePsListDTO.setSourceSys(afmSource.get(afmDetNotePsListDTO.getSourceSys()));
                afmDetNotePsListDTO.setUploadUser(item.getUploadUserName());
                afmDetNotePsListDTO.setPsDetResultStr(item.getPsDetResult() == 0 ? "正常" : "疑似篡改");
                afmDetNotePsListDTO.setBusinessType(item.getBusinessType().split(AfmConstant.SUFF)[1]);
                afmDetNotePsListDTO.setMaterialType(item.getMaterialType().split(AfmConstant.SUFF)[1]);
                resultList.add(afmDetNotePsListDTO);
            });
            result.setList(resultList);
            result.setTotal(afmImagePsNotePageInfo.getTotal());
            result.setPageNum(afmImagePsNotePageInfo.getPageNum());
            result.setPageSize(afmImagePsNotePageInfo.getPageSize());
        }else{
            result.setList(new ArrayList<>());
            result.setTotal(afmImagePsNotePageInfo.getTotal());
            result.setPageNum(afmImagePsNotePageInfo.getPageNum());
            result.setPageSize(afmImagePsNotePageInfo.getPageSize());
        }
        return Result.success(result);
    }

    /**
     * 篡改记录详情
     */
    public Result<List<AfmDetNoteDetailsDTO>> noteDetails(Long[] ids, String token) {
        Assert.notEmpty(ids, "参数错误");
        List<AfmDetNoteDetailsDTO> result = new ArrayList<>();
        List<AfmImagePsNote> afmImagePsNotes = afmImagePsNoteMapper.selectBatchIds(Arrays.asList(ids));
        Map<String, String> afmSource = commonService.getAfmSource();
        afmImagePsNotes.forEach(item -> {
            AfmFileExif afmFileExif = afmFileExifMapper.selectById(item.getExifId());
            AfmFileExif psAfmFileExif = afmFileExifMapper.selectById(item.getPsDetFileId());
            AfmDetNoteDetailsDTO afmDetNoteDetailsDTO = new AfmDetNoteDetailsDTO();
            if (StringUtils.hasText(afmFileExif.getFileExif())) {
                afmDetNoteDetailsDTO = JSONObject.parseObject(afmFileExif.getFileExif(), AfmDetNoteDetailsDTO.class);
            }
            BeanUtils.copyProperties(afmFileExif, afmDetNoteDetailsDTO);
            afmDetNoteDetailsDTO.setSourceSys(afmSource.get(afmDetNoteDetailsDTO.getSourceSys()));
            afmDetNoteDetailsDTO.setBusinessType(afmDetNoteDetailsDTO.getBusinessType().split(AfmConstant.SUFF)[1]);
            afmDetNoteDetailsDTO.setMaterialType(afmDetNoteDetailsDTO.getMaterialType().split(AfmConstant.SUFF)[1]);
            afmDetNoteDetailsDTO.setExifId(psAfmFileExif.getExifId());
            String[] fileFormat = item.getFileName().split("\\.");
            afmDetNoteDetailsDTO.setFileFormat(fileFormat[fileFormat.length - 1]);
            HashMap<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Cookie", "Sunyard-Token=" + token);
            HttpEntity execute = CommonHttpRequest
                    .get(psAfmFileExif.getFileUrl())
                    .header(headerMap)
                    .execute();
            InputStream inputStream;
            try {
                inputStream = execute.getContent();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            afmDetNoteDetailsDTO.setFileBase64(Base64.getEncoder().encodeToString(IoUtil.readBytes(inputStream)));
            result.add(afmDetNoteDetailsDTO);
        });
        return Result.success(result);
    }

    /**
     * 篡改结果详情
     */
    public Result<AfmDetNotePsDTO> resultDetails(Long id) {
        Assert.isTrue(null != id, "参数错误");
        AfmDetNotePsDTO result = new AfmDetNotePsDTO();
        AfmFileExif afmFileExif = afmFileExifMapper.selectById(id);
        List<AfmImagePsNote> afmImagePsNotes = afmImagePsNoteMapper
                .selectList(new LambdaQueryWrapper<AfmImagePsNote>()
                        .eq(AfmImagePsNote::getPsDetFileId, id)
                        .orderByDesc(AfmImagePsNote::getCreateTime)
                );
        result.setPsCount(afmImagePsNotes.get(0).getPsCount());
        result.setFileUrl(afmFileExif.getFileUrl());
        return Result.success(result);
    }

    /**
     * 导出
     */
    public void exportList(HttpServletResponse response, AfmDetNoteListVO vo) {
        List<AfmImagePsNote> afmImagePsNotes;
        if (!CollectionUtils.isEmpty(vo.getNoteIds())) {
            afmImagePsNotes = afmImagePsNoteMapper.selectBatchIds(vo.getNoteIds());
        } else {

            LambdaQueryWrapper<AfmImagePsNote> wrapper = new LambdaQueryWrapper<AfmImagePsNote>()
                    .ne(AfmImagePsNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                    .eq(StringUtils.hasText(vo.getSourceSys()), AfmImagePsNote::getSourceSys, vo.getSourceSys())
                    .eq(StringUtils.hasText(vo.getBusinessIndex()), AfmImagePsNote::getBusinessIndex, vo.getBusinessIndex())
                    .likeRight(StringUtils.hasText(vo.getBusinessType()), AfmImagePsNote::getBusinessType, vo.getBusinessType())
                    .likeRight(StringUtils.hasText(vo.getMaterialType()), AfmImagePsNote::getMaterialType, vo.getMaterialType())
                    .between(!ObjectUtils.isEmpty(vo.getToDetTime()) && !ObjectUtils.isEmpty(vo.getDoDetTime()),
                            AfmImagePsNote::getPsDetTime, vo.getToDetTime(), vo.getDoDetTime());

            if (vo.getDetResult() != null) {
                if (vo.getDetResult() == 0) {
                    //仅正常
                    wrapper.eq(AfmImagePsNote::getPsDetResult, 0);
                } else if (vo.getDetResult() == 1) {
                    //篡改
                    wrapper.eq(AfmImagePsNote::getPsDetResult, 1);
                } else {
                    //所有
                }
            }
            wrapper.orderByDesc(AfmImagePsNote::getPsDetTime);
            afmImagePsNotes = afmImagePsNoteMapper.selectList(wrapper);
        }
        Map<String, String> afmSource = commonService.getAfmSource();
        List<AfmDetNotePsListExcelDTO> list = new ArrayList<>();
        for (AfmImagePsNote item : afmImagePsNotes) {
            AfmDetNotePsListExcelDTO dto = new AfmDetNotePsListExcelDTO();
            BeanUtils.copyProperties(item, dto);
            dto.setSourceSys(afmSource.get(dto.getSourceSys()));
            dto.setUploadUser(item.getUploadUserName());
            dto.setBusinessType(item.getBusinessType().split(AfmConstant.SUFF)[1]);
            dto.setMaterialType(item.getMaterialType().split(AfmConstant.SUFF)[1]);
            dto.setPsDetResultStr(item.getPsDetResult() == 0 ? "正常" : "疑似篡改");
            list.add(dto);
        }
        try {
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("篡改检测记录表.xlsx", "UTF-8"));
            ServletOutputStream outputStream = response.getOutputStream();
            EasyExcel.write(outputStream, AfmDetNotePsListExcelDTO.class).sheet("篡改检测记录表").doWrite(list);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("导出失败", e);
        }
    }
}
