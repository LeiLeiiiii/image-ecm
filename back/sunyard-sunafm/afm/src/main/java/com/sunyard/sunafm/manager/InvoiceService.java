package com.sunyard.sunafm.manager;

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.afm.api.dto.AfmDetInvoiceDTO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.module.storage.api.FileStorageApi;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.vo.UploadListVO;
import com.sunyard.sunafm.constant.AfmConstant;
import com.sunyard.sunafm.constant.OcrConstant;
import com.sunyard.sunafm.dto.AfmDetOnlineResultDetailsDTO;
import com.sunyard.sunafm.enums.InvoiceValidateEnum;
import com.sunyard.sunafm.mapper.AfmFileExifMapper;
import com.sunyard.sunafm.mapper.AfmInvoiceDetNoteAssocMapper;
import com.sunyard.sunafm.mapper.AfmInvoiceDetNoteMapper;
import com.sunyard.sunafm.mapper.AfmInvoiceFileDataMapper;
import com.sunyard.sunafm.po.AfmFileExif;
import com.sunyard.sunafm.po.AfmInvoiceDetNote;
import com.sunyard.sunafm.po.AfmInvoiceDetNoteAssoc;
import com.sunyard.sunafm.po.AfmInvoiceFileData;
import com.sunyard.sunafm.util.CommonHttpRequest;
import org.apache.http.HttpEntity;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author P-JWei
 * @date 2024/4/23 16:12:56
 * @title
 * @description 在线检测/发票检测实现类
 */
@Service
public class InvoiceService {

    @Value("${storage.url}")
    private String storageUrl;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private AfmInvoiceFileDataMapper afmInvoiceFileDataMapper;
    @Resource
    private AfmFileExifMapper afmFileExifMapper;
    @Resource
    private AfmInvoiceDetNoteMapper afmInvoiceDetNoteMapper;
    @Resource
    private AfmInvoiceDetNoteAssocMapper afmInvoiceDetNoteAssocMapper;
    @Resource
    private FileStorageApi storageApi;


    /**
     * 识别发票文件
     */
    public Result<Long> uploadInvoice(AfmDetInvoiceDTO dto) {
        checkParam(dto);
        Long exifId;
        //通过拿到文件md5，判断是否上传过
        AfmFileExif afmFileExif = afmFileExifMapper
                .selectOne(new LambdaQueryWrapper<AfmFileExif>()
                        .eq(AfmFileExif::getFileMd5, dto.getFileMd5())
                        .eq(AfmFileExif::getFileIndex, dto.getSourceSys()+"_"+dto.getFileIndex())
                        .eq(AfmFileExif::getBusinessIndex, dto.getBusinessIndex())
                        .eq(AfmFileExif::getMaterialType, dto.getMaterialType()));
        if (null != afmFileExif) {
            //采用已存在的文件相关信息进行检测
            exifId = afmFileExif.getExifId();
            //查询是否识别过发票信息
            AfmInvoiceFileData afmInvoiceFileData = afmInvoiceFileDataMapper
                    .selectOne(new LambdaQueryWrapper<AfmInvoiceFileData>()
                            .eq(AfmInvoiceFileData::getFileMd5, afmFileExif.getFileMd5()));
            if (null == afmInvoiceFileData) {
                //进行ocr识别 插入数据进发票表
                Map<String, String> headerMap = new HashMap<>(1);
                headerMap.put("Cookie", "Sunyard-Token=" + dto.getFileToken());
                HttpEntity execute = CommonHttpRequest.get(dto.getFileUrl()).header(headerMap).execute();
                InputStream inputStream;
                try {
                    inputStream = execute.getContent();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                byte[] bytes = IoUtil.readBytes(inputStream);
                String invoiceOcrInfo = invoiceOcr(OcrConstant.SUN_OCR_URL, Base64.getEncoder().encodeToString(bytes));
                if (!StringUtils.hasText(JSONObject.parseObject(invoiceOcrInfo)
                        .getJSONObject("result")
                        .get(OcrConstant.INVOICE_TYPE).toString())) {
                    //提取不出信息，则此文件也不是发票文件
                    return Result.error("请上传发票文件", ResultCode.SYSTEM_BUSY_ERROR);
                }
                //解析ocr信息获取发票号并把信息存入afm_invoice_file_data表
                afmInvoiceFileData = analysisInvoiceOcrInfoAndInsertDb(invoiceOcrInfo,
                        afmFileExif.getFileMd5());
                afmInvoiceFileDataMapper.insert(afmInvoiceFileData);
            }
        } else {
            //采用传入的文件信息进行检测
            //进行ocr识别 插入数据进行发票表 存入文件 并返回文件id
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("Cookie", "Sunyard-Token=" + dto.getFileToken());
            HttpEntity execute = CommonHttpRequest.get(dto.getFileUrl()).header(headerMap).execute();
            InputStream inputStream;
            try {
                inputStream = execute.getContent();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            byte[] bytes = IoUtil.readBytes(inputStream);
            String base64File = Base64.getEncoder().encodeToString(bytes);
            //上传文件
            UploadListVO uploadListVO = getUploadListVO(null, bytes, dto.getFileName());
            List<UploadListVO> uploadList = new ArrayList<>();
            uploadList.add(uploadListVO);
            Result<List<SysFileDTO>> listResult = storageApi.uploadBatch(uploadList);
            AfmFileExif newAfmFileExif = new AfmFileExif();
            BeanUtils.copyProperties(dto, newAfmFileExif);
            newAfmFileExif.setFileUrl(storageUrl + listResult.getData().get(0).getId());
            newAfmFileExif.setFileIndex(newAfmFileExif.getSourceSys()+"_"+newAfmFileExif.getFileIndex());
            afmFileExifMapper.insert(newAfmFileExif);
            exifId = newAfmFileExif.getExifId();
            //查询是否识别过发票信息
            AfmInvoiceFileData afmInvoiceFileData = afmInvoiceFileDataMapper
                    .selectOne(new LambdaQueryWrapper<AfmInvoiceFileData>()
                            .eq(AfmInvoiceFileData::getFileMd5, newAfmFileExif.getFileMd5()));
            if (null == afmInvoiceFileData) {
                //进行ocr识别发票信息
                String invoiceOcrInfo = invoiceOcr(OcrConstant.SUN_OCR_URL, base64File);
                if (!StringUtils.hasText(JSONObject.parseObject(invoiceOcrInfo)
                        .getJSONObject("result")
                        .get(OcrConstant.INVOICE_TYPE).toString())) {
                    //提取不出信息，则此文件也不是发票文件
                    return Result.error("请上传发票文件", ResultCode.SYSTEM_BUSY_ERROR);
                }

                //解析ocr信息获取发票号并把信息存入afm_invoice_file_data表
                afmInvoiceFileData = analysisInvoiceOcrInfoAndInsertDb(invoiceOcrInfo,
                        newAfmFileExif.getFileMd5());
                afmInvoiceFileDataMapper.insert(afmInvoiceFileData);
            }
        }
        return Result.success(exifId);
    }

    /**
     * 验真
     */
    public Result<Integer> invoiceVerify(Long exifId) {
        AfmFileExif afmFileExif = afmFileExifMapper.selectById(exifId);
        AfmInvoiceFileData afmInvoiceFileData = afmInvoiceFileDataMapper
                .selectOne(new LambdaQueryWrapper<AfmInvoiceFileData>()
                        .eq(AfmInvoiceFileData::getFileMd5, afmFileExif.getFileMd5()));
        //发票验真
        Integer result = invoiceValidate(afmInvoiceFileData.getInvoiceCode(),
                afmInvoiceFileData.getInvoiceNum(),
                afmInvoiceFileData.getInvoiceCheckCode(),
                afmInvoiceFileData.getInvoiceTotal(),
                afmInvoiceFileData.getInvoiceTotal(),
                afmInvoiceFileData.getInvoiceDate(),
                afmInvoiceFileData.getInvoiceType());
        return Result.success(result);
    }

    /**
     * 查重
     */
    public Result<List<AfmDetOnlineResultDetailsDTO>> invoiceDup(Long exifId, String token) {
        AfmFileExif afmFileExif = afmFileExifMapper.selectById(exifId);
        AfmInvoiceFileData afmInvoiceFileData = afmInvoiceFileDataMapper
                .selectOne(new LambdaQueryWrapper<AfmInvoiceFileData>()
                        .eq(AfmInvoiceFileData::getFileMd5, afmFileExif.getFileMd5()));
        List<AfmDetOnlineResultDetailsDTO> afmDetOnlineResultDetailsDTOS =
                invoiceDup(afmInvoiceFileData.getInvoiceNum(), afmInvoiceFileData.getFileMd5(), token);
        return Result.success(afmDetOnlineResultDetailsDTOS);
    }

    /**
     * 连续
     */
    public Result<List<AfmDetOnlineResultDetailsDTO>> invoiceLink(Long exifId, String token) {
        AfmFileExif afmFileExif = afmFileExifMapper.selectById(exifId);
        AfmInvoiceFileData afmInvoiceFileData = afmInvoiceFileDataMapper
                .selectOne(new LambdaQueryWrapper<AfmInvoiceFileData>()
                        .eq(AfmInvoiceFileData::getFileMd5, afmFileExif.getFileMd5()));
        List<AfmDetOnlineResultDetailsDTO> afmDetOnlineResultDetailsDTOS =
                invoiceLink(afmInvoiceFileData.getInvoiceNum(), 1, token);
        return Result.success(afmDetOnlineResultDetailsDTOS);
    }

    /**
     * 记录结果
     */
    public void record(AfmDetInvoiceDTO dto, Long exifId, Integer verify, List<AfmDetOnlineResultDetailsDTO> invoiceDup, List<AfmDetOnlineResultDetailsDTO> invoiceLink) {
        AfmInvoiceDetNote afmInvoiceDetNote = new AfmInvoiceDetNote();
        BeanUtils.copyProperties(dto, afmInvoiceDetNote);
        //把这三个检测记录存入数据库(不更新，存新的数据)
        setResultToObj(exifId, verify, invoiceDup, invoiceLink, afmInvoiceDetNote);
        afmInvoiceDetNoteMapper.insert(afmInvoiceDetNote);
        List<Long> dupIdList = new ArrayList<>();
        List<Long> linkIdList = new ArrayList<>();
        if (null != invoiceDup) {
            dupIdList = invoiceDup.stream().map(AfmDetOnlineResultDetailsDTO::getExifId).collect(Collectors.toList());
        }
        if (null != invoiceLink) {
            linkIdList = invoiceLink.stream().map(AfmDetOnlineResultDetailsDTO::getExifId).collect(Collectors.toList());
        }
        //把关联数据存入关联表
        List<AfmInvoiceDetNoteAssoc> accocList = getAfmInvoiceDetNoteAssoc(afmInvoiceDetNote.getId(), dupIdList, linkIdList);
        MybatisBatch<AfmInvoiceDetNoteAssoc> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, accocList);
        MybatisBatch.Method<AfmInvoiceDetNoteAssoc> method = new MybatisBatch.Method<>(AfmInvoiceDetNoteAssocMapper.class);
        mybatisBatch.execute(method.insert());
    }

    /**
     * 返回值处理
     */
    public Map verifyResult(AfmDetInvoiceDTO dto) {
        Map<String, Integer> map = new HashMap<>();
        Assert.notNull(dto.getFileMd5(), "文件MD5不能为空");
        AfmFileExif afmFileExif = afmFileExifMapper
                .selectOne(new LambdaQueryWrapper<AfmFileExif>()
                        .eq(AfmFileExif::getFileMd5, dto.getFileMd5())
                        .eq(AfmFileExif::getFileIndex, dto.getSourceSys()+"_"+dto.getFileIndex())
                        .eq(AfmFileExif::getBusinessIndex, dto.getBusinessIndex())
                        .eq(AfmFileExif::getMaterialType, dto.getMaterialType()));
        if (null != afmFileExif) {
            List<AfmInvoiceDetNote> afmInvoiceDetNotes = afmInvoiceDetNoteMapper
                    .selectList(new LambdaQueryWrapper<AfmInvoiceDetNote>()
                            .eq(AfmInvoiceDetNote::getExifId, afmFileExif.getExifId())
                            .orderByDesc(AfmInvoiceDetNote::getInvoiceDetTime));
            map.put("verify", afmInvoiceDetNotes.get(0).getInvoiceVerifyResult());
        } else {
            map.put("verify", null);
        }

        return map;
    }

    /**
     * 查重
     */
    public Map dupResult(AfmDetInvoiceDTO dto) {
        Map map = new HashMap<>();
        Assert.notNull(dto.getFileMd5(), "文件MD5不能为空");
        AfmFileExif afmFileExif = afmFileExifMapper
                .selectOne(new LambdaQueryWrapper<AfmFileExif>()
                        .eq(AfmFileExif::getFileMd5, dto.getFileMd5())
                        .eq(AfmFileExif::getFileIndex, dto.getSourceSys()+"_"+dto.getFileIndex())
                        .eq(AfmFileExif::getBusinessIndex, dto.getBusinessIndex())
                        .eq(AfmFileExif::getMaterialType, dto.getMaterialType()));
        if (null != afmFileExif) {
            List<AfmInvoiceDetNote> afmInvoiceDetNotes = afmInvoiceDetNoteMapper
                    .selectList(new LambdaQueryWrapper<AfmInvoiceDetNote>()
                            .eq(AfmInvoiceDetNote::getExifId, afmFileExif.getExifId())
                            .orderByDesc(AfmInvoiceDetNote::getInvoiceDetTime));
            if (!CollectionUtils.isEmpty(afmInvoiceDetNotes)) {
                List<AfmInvoiceDetNoteAssoc> afmInvoiceDetNoteAssocs = afmInvoiceDetNoteAssocMapper
                        .selectList(new LambdaQueryWrapper<AfmInvoiceDetNoteAssoc>()
                                .eq(AfmInvoiceDetNoteAssoc::getInvoiceNoteId, afmInvoiceDetNotes.get(0).getId()));
                List<String> dupList = new ArrayList<>();
                afmInvoiceDetNoteAssocs.forEach(item -> {
                    AfmFileExif reAfmFileExif = afmFileExifMapper.selectById(item.getAssocExifId());
                    if (item.getAssocType() == 0) {
                        dupList.add(reAfmFileExif.getFileUrl());
                    }
                });
                map.put("dup", dupList);
            } else {
                map.put("dup", "");
            }
        } else {
            map.put("dup", "");
        }

        return map;
    }

    /**
     *
     */
    public Map linkResult(AfmDetInvoiceDTO dto) {
        Map map = new HashMap<>();
        Assert.notNull(dto.getFileMd5(), "文件MD5不能为空");
        AfmFileExif afmFileExif = afmFileExifMapper
                .selectOne(new LambdaQueryWrapper<AfmFileExif>()
                        .eq(AfmFileExif::getFileMd5, dto.getFileMd5())
                        .eq(AfmFileExif::getFileIndex, dto.getSourceSys()+"_"+dto.getFileIndex())
                        .eq(AfmFileExif::getBusinessIndex, dto.getBusinessIndex())
                        .eq(AfmFileExif::getMaterialType, dto.getMaterialType()));
        if (null != afmFileExif) {
            List<AfmInvoiceDetNote> afmInvoiceDetNotes = afmInvoiceDetNoteMapper
                    .selectList(new LambdaQueryWrapper<AfmInvoiceDetNote>()
                            .eq(AfmInvoiceDetNote::getExifId, afmFileExif.getExifId())
                            .orderByDesc(AfmInvoiceDetNote::getInvoiceDetTime));
            if (!CollectionUtils.isEmpty(afmInvoiceDetNotes)) {
                List<AfmInvoiceDetNoteAssoc> afmInvoiceDetNoteAssocs = afmInvoiceDetNoteAssocMapper
                        .selectList(new LambdaQueryWrapper<AfmInvoiceDetNoteAssoc>()
                                .eq(AfmInvoiceDetNoteAssoc::getInvoiceNoteId, afmInvoiceDetNotes.get(0).getId()));
                List<String> linkList = new ArrayList<>();
                afmInvoiceDetNoteAssocs.forEach(item -> {
                    AfmFileExif reAfmFileExif = afmFileExifMapper.selectById(item.getAssocExifId());
                    if (item.getAssocType() == 1) {
                        linkList.add(reAfmFileExif.getFileUrl());
                    }
                });
                map.put("link", linkList);
            } else {
                map.put("link", "");
            }
        } else {
            map.put("link", "");
        }

        return map;
    }

    /**
     * 校验参数
     */
    private void checkParam(AfmDetInvoiceDTO vo) {
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
     * 获取发票ocr信息
     */
    private String invoiceOcr(String url, String base64) {
        // 准备请求参数（表单数据）
        Map<String, String> paramMap = new HashMap<>(6);
        paramMap.put("image", base64);
        HttpEntity execute = CommonHttpRequest.post(url).form(paramMap).execute();
        return CommonHttpRequest.getResponseString(execute);
    }

    /**
     * 解析发票ocr信息
     */
    private AfmInvoiceFileData analysisInvoiceOcrInfoAndInsertDb(String str, String fileMd5) {
        AfmInvoiceFileData afmInvoiceFileData = new AfmInvoiceFileData();
        JSONObject resultJson = JSONObject.parseObject(str);
        JSONObject invoiceOcrJson = JSONObject.parseObject(resultJson.get("result").toString());
        String invoiceCode = invoiceOcrJson.get(OcrConstant.INVOICE_CODE).toString();
        String invoiceNum = invoiceOcrJson.get(OcrConstant.INVOICE_NUM).toString();
        String invoiceCheckCode = invoiceOcrJson.get(OcrConstant.INVOICE_CHECK_CODE).toString();
        String invoiceDate = invoiceOcrJson.get(OcrConstant.INVOICE_DATE).toString();
        String invoiceTotal = invoiceOcrJson.get(OcrConstant.INVOICE_TOTAL).toString();
        String invoiceType = invoiceOcrJson.get(OcrConstant.INVOICE_TYPE).toString();
        afmInvoiceFileData.setFileMd5(fileMd5);
        afmInvoiceFileData.setInvoiceCode(invoiceCode);
        afmInvoiceFileData.setInvoiceNum(invoiceNum);
        afmInvoiceFileData.setInvoiceCheckCode(invoiceCheckCode);
        afmInvoiceFileData.setInvoiceType(invoiceType);
        afmInvoiceFileData.setInvoiceDate(invoiceDate);
        afmInvoiceFileData.setInvoiceTotal(invoiceTotal);
        return afmInvoiceFileData;
    }

    /**
     * 获取发票验真结果
     */
    private Integer invoiceValidate(String code, String number, String checkCode, String pretaxAmount,
                                    String total, String date, String type) {
        long timestamp = System.currentTimeMillis() / 1000;
        String token = DigestUtils.md5DigestAsHex((OcrConstant.APPKEY + "+" + (System.currentTimeMillis() / 1000) + "+" + OcrConstant.APPSECRET).getBytes(StandardCharsets.UTF_8));
        Map<String, String> map = new HashMap<>(10);
        map.put("app_key", OcrConstant.APPKEY);
        map.put("timestamp", Long.toString(timestamp));
        map.put("token", token);
        map.put("code", code);
        map.put("number", number);
        map.put("check_code", checkCode);
        map.put("pretax_amount", pretaxAmount);
        map.put("total", total);
        map.put("date", date);
        map.put("type", InvoiceValidateEnum.getCodeByDesc(type));
        HttpEntity execute = CommonHttpRequest
                .post(OcrConstant.VALIDATE_API_URL)
                .body(JSON.toJSONString(map))
                .execute();
        String responseString = CommonHttpRequest.getResponseString(execute);
        //处理responseString拿到是否为真
        return analysisValidate(responseString);
    }

    /**
     * 获取发票重复数据
     */
    private List<AfmDetOnlineResultDetailsDTO> invoiceDup(String invoiceNum, String fileMd5, String token) {
        List<AfmDetOnlineResultDetailsDTO> result = new ArrayList<>();
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
                AfmDetOnlineResultDetailsDTO resultItem = new AfmDetOnlineResultDetailsDTO();
                BeanUtils.copyProperties(item, resultItem);
                resultItem.setInvoiceNum(invoiceNum);
                resultItem.setDetTime(new Date());
                HashMap<String, String> headerMap = new HashMap<>(1);
                headerMap.put("Cookie", "Sunyard-Token=" + token);
                HttpEntity execute = CommonHttpRequest
                        .get(item.getFileUrl())
                        .header(headerMap)
                        .execute();
                InputStream inputStream;
                try {
                    inputStream = execute.getContent();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                byte[] bytes = IoUtil.readBytes(inputStream);
                resultItem.setFileBase64(Base64.getEncoder().encodeToString(bytes));
                result.add(resultItem);
            });
        }
        return result;
    }

    /**
     * 获取发票连续数据
     */
    private List<AfmDetOnlineResultDetailsDTO> invoiceLink(String invoiceNum, Integer count, String token) {
        List<AfmDetOnlineResultDetailsDTO> result = new ArrayList<>();
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
                AfmDetOnlineResultDetailsDTO resultItem = new AfmDetOnlineResultDetailsDTO();
                BeanUtils.copyProperties(item, resultItem);
                resultItem.setInvoiceNum(invoiceNum);
                resultItem.setDetTime(new Date());
                HashMap<String, String> headerMap = new HashMap<>(1);
                headerMap.put("Cookie", "Sunyard-Token=" + token);
                HttpEntity execute = CommonHttpRequest
                        .get(item.getFileUrl())
                        .header(headerMap)
                        .execute();
                InputStream inputStream;
                try {
                    inputStream = execute.getContent();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                byte[] bytes = IoUtil.readBytes(inputStream);
                resultItem.setFileBase64(Base64.getEncoder().encodeToString(bytes));
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

    /**
     * 解析发票验真结果
     */
    private Integer analysisValidate(String jsonStr) {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Object response = jsonObject.get("response");
        if (null != response) {
            String code = JSONObject.parseObject(response.toString())
                    .getJSONObject("data")
                    .getJSONArray("identify_results")
                    .getJSONObject(0)
                    .getJSONObject("validation")
                    .get("code").toString();
            if (null != code && "10000".equals(code)) {
                return OcrConstant.VALIDATE_RESULT_TRUE;
            }
        }
        return OcrConstant.VALIDATE_RESULT_FALSE;
    }

    /**
     * 生成文件上传vo
     */
    private UploadListVO getUploadListVO(Long userId, byte[] data, String fileName) {
        UploadListVO uploadListVO = new UploadListVO();
        uploadListVO.setFileByte(data);
        uploadListVO.setFileName("ps_" + fileName);
        uploadListVO.setStEquipmentId(OcrConstant.MINIO);
        uploadListVO.setUserId(userId);
        uploadListVO.setFileSource(OcrConstant.APPLICATION);
        return uploadListVO;
    }

    /**
     * 设置AfmInvoiceDetNote结果
     */
    private void setResultToObj(Long reExifId, Integer invoiceValidateResult, List<AfmDetOnlineResultDetailsDTO> invoiceDupResult,
                                List<AfmDetOnlineResultDetailsDTO> invoiceLinkResult, AfmInvoiceDetNote afmInvoiceDetNote) {
        afmInvoiceDetNote.setId(null);
        afmInvoiceDetNote.setExifId(reExifId);
        afmInvoiceDetNote.setInvoiceDetTime(new Date());
        afmInvoiceDetNote.setInvoiceVerifyResult(invoiceValidateResult);
        if (null != invoiceDupResult) {
            if (invoiceDupResult.isEmpty()) {
                afmInvoiceDetNote.setInvoiceDupResult(1);
            } else {
                afmInvoiceDetNote.setInvoiceDupResult(0);
            }
        }
        if (null != invoiceLinkResult) {
            if (invoiceLinkResult.isEmpty()) {
                afmInvoiceDetNote.setInvoiceLinkResult(1);
            } else {
                afmInvoiceDetNote.setInvoiceLinkResult(0);
            }
        }
        afmInvoiceDetNote.setCreateTime(null);
        afmInvoiceDetNote.setUpdateTime(null);
    }

    private List<AfmInvoiceDetNoteAssoc> getAfmInvoiceDetNoteAssoc(Long noteId, List<Long> dupIdList, List<Long> linkIdList) {
        List<AfmInvoiceDetNoteAssoc> result = new ArrayList<>();
        dupIdList.forEach(item -> {
            AfmInvoiceDetNoteAssoc obj = new AfmInvoiceDetNoteAssoc();
            obj.setInvoiceNoteId(noteId);
            obj.setAssocExifId(item);
            obj.setAssocType(0);
            result.add(obj);
        });
        linkIdList.forEach(item -> {
            AfmInvoiceDetNoteAssoc obj = new AfmInvoiceDetNoteAssoc();
            obj.setInvoiceNoteId(noteId);
            obj.setAssocExifId(item);
            obj.setAssocType(1);
            result.add(obj);
        });
        return result;
    }
}
