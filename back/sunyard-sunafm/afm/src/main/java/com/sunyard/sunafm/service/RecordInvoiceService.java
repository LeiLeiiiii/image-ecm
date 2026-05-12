package com.sunyard.sunafm.service;

import cn.hutool.core.io.IoUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.sunafm.constant.AfmConstant;
import com.sunyard.sunafm.constant.OcrConstant;
import com.sunyard.sunafm.dto.AfmDetNoteDetailsDTO;
import com.sunyard.sunafm.dto.AfmDetNoteInvoiceDetDTO;
import com.sunyard.sunafm.dto.AfmDetNoteInvoiceListDTO;
import com.sunyard.sunafm.dto.AfmDetNoteInvoiceListExcelDTO;
import com.sunyard.sunafm.dto.AfmDetNoteResultDetailsDTO;
import com.sunyard.sunafm.mapper.AfmFileExifMapper;
import com.sunyard.sunafm.mapper.AfmInvoiceDetNoteAssocMapper;
import com.sunyard.sunafm.mapper.AfmInvoiceDetNoteMapper;
import com.sunyard.sunafm.mapper.AfmInvoiceFileDataMapper;
import com.sunyard.sunafm.po.AfmFileExif;
import com.sunyard.sunafm.po.AfmInvoiceDetNote;
import com.sunyard.sunafm.po.AfmInvoiceDetNoteAssoc;
import com.sunyard.sunafm.po.AfmInvoiceFileData;
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
import java.util.stream.Collectors;

/**
 * @author P-JWei
 * @date 2024/4/7 16:07:24
 * @title
 * @description 检测记录/发票检测实现类
 */
@Slf4j
@Service
public class RecordInvoiceService {

    @Resource
    private AfmInvoiceDetNoteMapper afmInvoiceDetNoteMapper;
    @Resource
    private AfmFileExifMapper afmFileExifMapper;
    @Resource
    private AfmInvoiceFileDataMapper afmInvoiceFileDataMapper;
    @Resource
    private AfmInvoiceDetNoteAssocMapper afmInvoiceDetNoteAssocMapper;
    @Resource
    private CommonService commonService;

    /**
     * 发票检测表格
     */
    public Result<PageInfo<AfmDetNoteInvoiceListDTO>> invoiceDetList(AfmDetNoteListVO vo, PageForm pageForm) {
        // 0 1 2 3
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<AfmInvoiceDetNote> afmInvoiceDetNotes = afmInvoiceDetNoteMapper
                .selectList(new LambdaQueryWrapper<AfmInvoiceDetNote>()
                        //SourceSys为AFM，说明是在线上传的文件记录，不查出
                        .ne(AfmInvoiceDetNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                        .eq(StringUtils.hasText(vo.getSourceSys()), AfmInvoiceDetNote::getSourceSys, vo.getSourceSys())
                        .like(StringUtils.hasText(vo.getBusinessType()), AfmInvoiceDetNote::getBusinessType, vo.getBusinessType())
                        .like(StringUtils.hasText(vo.getMaterialType()), AfmInvoiceDetNote::getMaterialType, vo.getMaterialType())
                        .like(StringUtils.hasText(vo.getFileName()), AfmInvoiceDetNote::getFileName, vo.getFileName())
                        .like(StringUtils.hasText(vo.getBusinessIndex()), AfmInvoiceDetNote::getBusinessIndex, vo.getBusinessIndex())
                        .eq(null != vo.getDetResult() && 0 == vo.getDetResult(), AfmInvoiceDetNote::getInvoiceVerifyResult, 1)
                        .eq(null != vo.getDetResult() && 0 == vo.getDetResult(), AfmInvoiceDetNote::getInvoiceDupResult, 1)
                        .eq(null != vo.getDetResult() && 0 == vo.getDetResult(), AfmInvoiceDetNote::getInvoiceLinkResult, 1)
                        .eq(null != vo.getDetResult() && 1 == vo.getDetResult(), AfmInvoiceDetNote::getInvoiceVerifyResult, 0)
                        .eq(null != vo.getDetResult() && 2 == vo.getDetResult(), AfmInvoiceDetNote::getInvoiceDupResult, 0)
                        .eq(null != vo.getDetResult() && 3 == vo.getDetResult(), AfmInvoiceDetNote::getInvoiceLinkResult, 0)
                        .eq(StringUtils.hasText(vo.getUploadUserName()), AfmInvoiceDetNote::getUploadUserName, vo.getUploadUserName())
                        .between(!ObjectUtils.isEmpty(vo.getToDetTime()) && !ObjectUtils.isEmpty(vo.getDoDetTime()),
                                AfmInvoiceDetNote::getInvoiceDetTime, vo.getToDetTime(), vo.getDoDetTime())
                        .orderByDesc(AfmInvoiceDetNote::getInvoiceDetTime));
        PageInfo<AfmInvoiceDetNote> afmImagePsNotePageInfo = new PageInfo<>(afmInvoiceDetNotes);
        Map<String, String> afmSource = commonService.getAfmSource();
        PageInfo<AfmDetNoteInvoiceListDTO> result = new PageInfo<>();
        if (!CollectionUtils.isEmpty(afmInvoiceDetNotes)) {
            List<AfmDetNoteInvoiceListDTO> resultList = new ArrayList<>();
            afmImagePsNotePageInfo.getList().forEach(item -> {
                AfmDetNoteInvoiceListDTO afmDetNoteInvoiceListDTO = new AfmDetNoteInvoiceListDTO();
                BeanUtils.copyProperties(item, afmDetNoteInvoiceListDTO);
                afmDetNoteInvoiceListDTO.setSourceSys(afmSource.get(afmDetNoteInvoiceListDTO.getSourceSys()));
                afmDetNoteInvoiceListDTO.setUploadUser(item.getUploadUserName());
                afmDetNoteInvoiceListDTO.setInvoiceDetResultStr(getInvoiceDetResultStr(item, afmDetNoteInvoiceListDTO));
                afmDetNoteInvoiceListDTO.setBusinessType(item.getBusinessType().split(AfmConstant.SUFF)[1]);
                afmDetNoteInvoiceListDTO.setMaterialType(item.getMaterialType().split(AfmConstant.SUFF)[1]);
                resultList.add(afmDetNoteInvoiceListDTO);
            });
            result.setList(resultList);
            result.setTotal(afmImagePsNotePageInfo.getTotal());
            result.setPageNum(afmImagePsNotePageInfo.getPageNum());
            result.setPageSize(afmImagePsNotePageInfo.getPageSize());
        }
        return Result.success(result);
    }

    private String getInvoiceDetResultStr(AfmInvoiceDetNote obj, AfmDetNoteInvoiceListDTO afmDetNoteInvoiceListDTO) {
        StringBuilder stringBuilder = new StringBuilder();
        if (null != obj.getInvoiceVerifyResult() && obj.getInvoiceVerifyResult() == 0) {
            afmDetNoteInvoiceListDTO.setInvoiceDetResult(0);
            stringBuilder.append("验真不通过 ");
        }
        if (null != obj.getInvoiceDupResult() && obj.getInvoiceDupResult() == 0) {
            afmDetNoteInvoiceListDTO.setInvoiceDetResult(0);
            stringBuilder.append("发票号重复 ");
        }
        if (null != obj.getInvoiceLinkResult() && obj.getInvoiceLinkResult() == 0) {
            afmDetNoteInvoiceListDTO.setInvoiceDetResult(0);
            stringBuilder.append("发票号连续 ");
        }
        if (stringBuilder.length() == 0) {
            afmDetNoteInvoiceListDTO.setInvoiceDetResult(1);
            stringBuilder.append("正常");
        }
        return stringBuilder.toString();
    }

    /**
     * 发票记录详情
     */
    public Result<List<AfmDetNoteDetailsDTO>> noteDetails(Long[] ids, String token) {
        Assert.notEmpty(ids, "参数错误");
        List<AfmDetNoteDetailsDTO> result = new ArrayList<>();
        List<AfmInvoiceDetNote> afmInvoiceDetNotes = afmInvoiceDetNoteMapper.selectBatchIds(Arrays.asList(ids));
        Map<String, String> afmSource = commonService.getAfmSource();
        afmInvoiceDetNotes.forEach(item -> {
            AfmFileExif afmFileExif = afmFileExifMapper.selectById(item.getExifId());
            AfmDetNoteDetailsDTO afmDetNoteDetailsDTO = new AfmDetNoteDetailsDTO();
            if (StringUtils.hasText(afmFileExif.getFileExif())) {
                afmDetNoteDetailsDTO = JSONObject.parseObject(afmFileExif.getFileExif(), AfmDetNoteDetailsDTO.class);
            }
            BeanUtils.copyProperties(afmFileExif, afmDetNoteDetailsDTO);
            afmDetNoteDetailsDTO.setSourceSys(afmSource.get(afmDetNoteDetailsDTO.getSourceSys()));
            afmDetNoteDetailsDTO.setBusinessType(afmDetNoteDetailsDTO.getBusinessType().split(AfmConstant.SUFF)[1]);
            afmDetNoteDetailsDTO.setMaterialType(afmDetNoteDetailsDTO.getMaterialType().split(AfmConstant.SUFF)[1]);
            afmDetNoteDetailsDTO.setExifId(afmFileExif.getExifId());
            String[] fileFormat = item.getFileName().split("\\.");
            afmDetNoteDetailsDTO.setFileFormat(fileFormat[fileFormat.length - 1]);
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
            afmDetNoteDetailsDTO.setFileBase64(Base64.getEncoder().encodeToString(IoUtil.readBytes(inputStream)));
            result.add(afmDetNoteDetailsDTO);
        });
        return Result.success(result);
    }

    /**
     * 发票记录属性
     *
     */
    public Result noteAttr(Long id, String token) {
        Assert.isTrue(null != id, "参数错误");
        AfmInvoiceDetNote afmInvoiceDetNote = afmInvoiceDetNoteMapper.selectById(id);
        AfmFileExif afmFileExif = afmFileExifMapper.selectById(afmInvoiceDetNote.getExifId());
        //进行ocr信息提取
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
        byte[] bytes = IoUtil.readBytes(inputStream);
        String base64File = Base64.getEncoder().encodeToString(bytes);
        String invoiceOcrInfo = invoiceOcr(OcrConstant.SUN_OCR_URL, base64File);
        if (!StringUtils.hasText(JSONObject.parseObject(invoiceOcrInfo)
                .getJSONObject("result")
                .get(OcrConstant.INVOICE_TYPE).toString())) {
            //提取不出信息，则此文件也不是发票文件
            return Result.error("请上传发票文件", ResultCode.SYSTEM_BUSY_ERROR);
        }
        return Result.success(JSONObject.parseObject(invoiceOcrInfo).getJSONObject("result"));
    }

    /**
     * 获取发票ocr信息
     */
    private String invoiceOcr(String url, String base64) {
        // 准备请求参数（表单数据）
        Map<String, String> paramMap = new HashMap<>(6);
        paramMap.put("image", base64);
        HttpEntity execute = CommonHttpRequest
                .post(url)
                .form(paramMap)
                .execute();
        return CommonHttpRequest.getResponseString(execute);
    }

    /**
     * 发票结果--详情、比对
     */
    public Result<AfmDetNoteInvoiceDetDTO> resultDetails(Long id, String token) {
        Assert.isTrue(null != id, "参数错误");
        AfmDetNoteInvoiceDetDTO result = new AfmDetNoteInvoiceDetDTO();
        AfmInvoiceDetNote afmInvoiceDetNote = afmInvoiceDetNoteMapper.selectById(id);
        //发票验真结果
        result.setIsVerify(afmInvoiceDetNote.getInvoiceVerifyResult());
        //发票查重、连续结果
        List<AfmInvoiceDetNoteAssoc> afmInvoiceDetNoteAssocs = afmInvoiceDetNoteAssocMapper
                .selectList(new LambdaQueryWrapper<AfmInvoiceDetNoteAssoc>()
                        .eq(AfmInvoiceDetNoteAssoc::getInvoiceNoteId, id));
        setDupAndLinkResult(afmInvoiceDetNoteAssocs, result, token);
        return Result.success(result);
    }

    private void setDupAndLinkResult(List<AfmInvoiceDetNoteAssoc> list, AfmDetNoteInvoiceDetDTO result, String token) {
        List<AfmDetNoteResultDetailsDTO> invoiceDupList = new ArrayList<>();
        List<AfmDetNoteResultDetailsDTO> invoiceLinkList = new ArrayList<>();
        Map<String, String> afmSource = commonService.getAfmSource();
        list.forEach(item -> {
            AfmDetNoteResultDetailsDTO obj = new AfmDetNoteResultDetailsDTO();
            AfmFileExif afmFileExif = afmFileExifMapper.selectById(item.getAssocExifId());
            BeanUtils.copyProperties(afmFileExif, obj);
            obj.setSourceSys(afmSource.get(obj.getSourceSys()));
            obj.setBusinessType(obj.getBusinessType().split(AfmConstant.SUFF)[1]);
            obj.setMaterialType(obj.getMaterialType().split(AfmConstant.SUFF)[1]);
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
            obj.setFileBase64(Base64.getEncoder().encodeToString(IoUtil.readBytes(inputStream)));
            Integer assocType = item.getAssocType();
            if (assocType == 0) {
                //重复
                invoiceDupList.add(obj);
            } else if (assocType == 1) {
                //连续
                invoiceLinkList.add(obj);
            } else {

            }
        });
        result.setInvoiceDupList(invoiceDupList);
        result.setInvoiceLinkList(invoiceLinkList);
    }

    /**
     * 导出
     */
    public void exportList(HttpServletResponse response, AfmDetNoteListVO vo) {
        List<AfmInvoiceDetNote> afmInvoiceDetNotes;
        if (!CollectionUtils.isEmpty(vo.getNoteIds())) {
            afmInvoiceDetNotes = afmInvoiceDetNoteMapper.selectBatchIds(vo.getNoteIds());
        } else {
            LambdaQueryWrapper<AfmInvoiceDetNote> wrapper = new LambdaQueryWrapper<AfmInvoiceDetNote>()
                    .ne(AfmInvoiceDetNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                    .eq(StringUtils.hasText(vo.getSourceSys()), AfmInvoiceDetNote::getSourceSys, vo.getSourceSys())
                    .eq(StringUtils.hasText(vo.getBusinessIndex()), AfmInvoiceDetNote::getBusinessIndex, vo.getBusinessIndex())
                    .likeRight(StringUtils.hasText(vo.getBusinessType()), AfmInvoiceDetNote::getBusinessType, vo.getBusinessType())
                    .likeRight(StringUtils.hasText(vo.getMaterialType()), AfmInvoiceDetNote::getMaterialType, vo.getMaterialType())
                    .between(!ObjectUtils.isEmpty(vo.getToDetTime()) && !ObjectUtils.isEmpty(vo.getDoDetTime()),
                            AfmInvoiceDetNote::getInvoiceDetTime, vo.getToDetTime(), vo.getDoDetTime());
            if (vo.getDetResult() != null) {
                if (vo.getDetResult() == 0) {
                    //仅正常
                    wrapper.eq(AfmInvoiceDetNote::getInvoiceVerifyResult, 1)
                            .eq(AfmInvoiceDetNote::getInvoiceDupResult, 1)
                            .eq(AfmInvoiceDetNote::getInvoiceLinkResult, 1);
                } else if (vo.getDetResult() == 1) {
                    //验真不通过
                    wrapper.eq(AfmInvoiceDetNote::getInvoiceVerifyResult, 0);
                } else if (vo.getDetResult() == 2) {
                    //查重不通过
                    wrapper.eq(AfmInvoiceDetNote::getInvoiceDupResult, 0);
                } else if (vo.getDetResult() == 3) {
                    //连续不通过
                    wrapper.eq(AfmInvoiceDetNote::getInvoiceLinkResult, 0);
                } else {
                    //所有
                }
            }
            wrapper.orderByDesc(AfmInvoiceDetNote::getInvoiceDetTime);
            afmInvoiceDetNotes = afmInvoiceDetNoteMapper.selectList(wrapper);
        }
        Map<String, String> afmSource = commonService.getAfmSource();
        List<AfmDetNoteInvoiceListExcelDTO> list = new ArrayList<>();
        for (AfmInvoiceDetNote item : afmInvoiceDetNotes) {
            AfmDetNoteInvoiceListExcelDTO dto = new AfmDetNoteInvoiceListExcelDTO();
            BeanUtils.copyProperties(item, dto);
            dto.setSourceSys(afmSource.get(dto.getSourceSys()));
            dto.setUploadUser(item.getUploadUserName());
            dto.setBusinessType(item.getBusinessType().split(AfmConstant.SUFF)[1]);
            dto.setMaterialType(item.getMaterialType().split(AfmConstant.SUFF)[1]);
            if (null == item.getInvoiceVerifyResult()) {
                dto.setInvoiceVerifyResultStr("未检测");
            } else {
                dto.setInvoiceVerifyResultStr(item.getInvoiceVerifyResult() == 1 ? "验真通过" : "验真未通过");
            }
            if (null == item.getInvoiceDupResult()) {
                dto.setInvoiceDupResultStr("未检测");
            } else {
                dto.setInvoiceDupResultStr(item.getInvoiceDupResult() == 1 ? "发票号未重复" : "发票号重复");
            }
            if (null == item.getInvoiceLinkResult()) {
                dto.setInvoiceLinkResultStr("未检测");
            } else {
                dto.setInvoiceLinkResultStr(item.getInvoiceLinkResult() == 1 ? "发票号无连续" : "发票号连续");
            }
            list.add(dto);
        }
        try {
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("发票检测记录表.xlsx", "UTF-8"));
            ServletOutputStream outputStream = response.getOutputStream();
            EasyExcel.write(outputStream, AfmDetNoteInvoiceListExcelDTO.class).sheet("发票检测记录").doWrite(list);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("导出失败", e);
        }
    }

    /**
     * 获取发票重复数据
     */
    private List<AfmDetNoteResultDetailsDTO> invoiceDup(String invoiceNum, String fileMd5) {
        List<AfmDetNoteResultDetailsDTO> result = new ArrayList<>();
        //排除自身
        List<AfmInvoiceFileData> afmInvoiceFileData = afmInvoiceFileDataMapper
                .selectList(new LambdaQueryWrapper<AfmInvoiceFileData>()
                        .select(AfmInvoiceFileData::getFileMd5)
                        .eq(AfmInvoiceFileData::getInvoiceNum, invoiceNum)
                        .ne(AfmInvoiceFileData::getFileMd5, fileMd5));
        //存在重复则添加list并返回
        if (!CollectionUtils.isEmpty(afmInvoiceFileData)) {
            List<String> reFileMd5 = afmInvoiceFileData.stream()
                    .map(AfmInvoiceFileData::getFileMd5)
                    .collect(Collectors.toList());
            List<AfmFileExif> afmFileExifs = afmFileExifMapper
                    .selectList(new LambdaQueryWrapper<AfmFileExif>()
                            .in(AfmFileExif::getFileMd5, reFileMd5));
            afmFileExifs.stream().forEach(item -> {
                AfmDetNoteResultDetailsDTO resultItem = new AfmDetNoteResultDetailsDTO();
                BeanUtils.copyProperties(item, resultItem);
                result.add(resultItem);
            });
        }
        return result;
    }

    /**
     * 获取发票连续数据
     */
    private List<AfmDetNoteResultDetailsDTO> invoiceLink(String invoiceNum, Integer count) {
        List<AfmDetNoteResultDetailsDTO> result = new ArrayList<>();
        //List<AfmInvoiceFileData> afmInvoiceFileData = afmInvoiceFileDataMapper.searchInvoiceLink(invoiceNum, count);
        List<AfmInvoiceFileData> afmInvoiceFileData = getAfmInvoiceFileData(invoiceNum, count);
        if (!CollectionUtils.isEmpty(afmInvoiceFileData)) {
            List<String> reFileMd5 = afmInvoiceFileData.stream()
                    .map(AfmInvoiceFileData::getFileMd5)
                    .collect(Collectors.toList());
            List<AfmFileExif> afmFileExifs = afmFileExifMapper
                    .selectList(new LambdaQueryWrapper<AfmFileExif>()
                            .in(AfmFileExif::getExifId, reFileMd5));
            afmFileExifs.stream().forEach(item -> {
                AfmDetNoteResultDetailsDTO resultItem = new AfmDetNoteResultDetailsDTO();
                BeanUtils.copyProperties(item, resultItem);
                result.add(resultItem);
            });
        }
        return result;
    }

    private List<AfmInvoiceFileData> getAfmInvoiceFileData(String invoiceNum, Integer count) {
        return afmInvoiceFileDataMapper.selectList(new LambdaQueryWrapper<AfmInvoiceFileData>()
                .eq(AfmInvoiceFileData::getInvoiceNum, Long.parseLong(invoiceNum) - count)
                .or().eq(AfmInvoiceFileData::getInvoiceNum, Long.parseLong(invoiceNum) + count));
    }
}
