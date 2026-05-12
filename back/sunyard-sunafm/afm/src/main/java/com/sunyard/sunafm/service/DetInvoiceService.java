package com.sunyard.sunafm.service;

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
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
import com.sunyard.sunafm.dto.AfmDetOnlineInvoiceDetDTO;
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
import com.sunyard.sunafm.util.Md5Utils;
import com.sunyard.sunafm.vo.AfmDetOnlineListVO;
import org.apache.http.HttpEntity;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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
 * @date 2024/3/20 14:05:53
 * @title
 * @description 在线检测/发票检测实现类
 */
@Service
public class DetInvoiceService{
    @Value("${storage.url}")
    private String storageUrl;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private AfmInvoiceDetNoteMapper afmInvoiceDetNoteMapper;
    @Resource
    private AfmInvoiceFileDataMapper afmInvoiceFileDataMapper;
    @Resource
    private AfmInvoiceDetNoteAssocMapper afmInvoiceDetNoteAssocMapper;
    @Resource
    private AfmFileExifMapper afmFileExifMapper;
    @Resource
    private FileStorageApi storageApi;
    @Resource
    private CommonService commonService;

    /**
     * 开始检测
     */
    @Transactional(rollbackFor = Exception.class)
    public Result det(MultipartFile file, Long exifId, String token, Long userId) {
        // 使用Assert确保至少有一个参数是非空的
        Assert.isTrue(null != file || null != exifId, "参数错误");
        //返回结果obj
        AfmDetOnlineInvoiceDetDTO result = new AfmDetOnlineInvoiceDetDTO();
        //验真结果-用户返参
        Integer invoiceValidateResult;
        //查重发票号-用户发票查重
        String reInvoiceNum;
        //查重文件md5-用于发票查重
        String reFileMd5;
        //查询文件id-用于记录发票检测记录
        Long reExifId;
        //检测记录obj-用于插入检测记录
        AfmInvoiceDetNote afmInvoiceDetNote;
        //传了fileId则不取file，反之则取
        if (null == exifId) {
            afmInvoiceDetNote = new AfmInvoiceDetNote();
            //计算文件MD5，去afm_file_exif查fileIndex字段，看是否已上传过
            byte[] bytes;
            try {
                bytes = file.getBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            afmInvoiceDetNote.setFileName(file.getOriginalFilename());
            //在线检测默认来源系统为AFM
            afmInvoiceDetNote.setSourceSys(AfmConstant.AFM_SOURCESYS);
            String fileName = file.getOriginalFilename();
            String base64File = Base64.getEncoder().encodeToString(bytes);
            String fileMd5 = Md5Utils.calculateMD5(bytes);
            //设置源文件result
            result.setSourceFileBase64(base64File);
            //在线检测图片fileIndex与fileMd5一样，多条数据md5可能一样，所以查fileIndex
            AfmFileExif afmFileExif = afmFileExifMapper
                    .selectOne(new LambdaQueryWrapper<AfmFileExif>()
                            .eq(AfmFileExif::getFileIndex, fileMd5)
                            .eq(AfmFileExif::getFileMd5, fileMd5)
                            .eq(AfmFileExif::getSourceSys, AfmConstant.AFM_SOURCESYS)
                    );
            //已上传过，就拿到fileId去afm_invoice_det_note、afm_invoice_file_data拿验真结果、发票信息，这样就不用重复验真和ocr
            //没上传过，则进行ocr识别发票信息进行验真、查重、连续检测，返回结果并且存入afm_invoice_det_note、afm_invoice_file_data
            if (null != afmFileExif) {
                //通过fileId判断文件是否已经验真过，已验真过直接返回验证结果（查重、连续需要重新判断）
                List<AfmInvoiceDetNote> afmInvoiceDetNotes = afmInvoiceDetNoteMapper
                        .selectList(new LambdaQueryWrapper<AfmInvoiceDetNote>()
                                .select(AfmInvoiceDetNote::getInvoiceVerifyResult)
                                .eq(AfmInvoiceDetNote::getExifId, afmFileExif.getExifId())
                                .orderByDesc(AfmInvoiceDetNote::getInvoiceDetTime));
                //拿发票ocr信息
                AfmInvoiceFileData afmInvoiceFileData = afmInvoiceFileDataMapper
                        .selectOne(new LambdaQueryWrapper<AfmInvoiceFileData>()
                                .eq(AfmInvoiceFileData::getFileMd5, afmFileExif.getFileMd5()));
                if (!CollectionUtils.isEmpty(afmInvoiceDetNotes)) {
                    if (null == afmInvoiceFileData) {
                        //没有ocr信息即这个文件不是发票文件
                        return Result.error("请上传发票文件", ResultCode.SYSTEM_BUSY_ERROR);
                    }
                    //拿检测时间最新的，查询时已按照时间排序，存在记录则直接拿验真结果
                    invoiceValidateResult = afmInvoiceDetNotes.get(0).getInvoiceVerifyResult();
                    reInvoiceNum = afmInvoiceFileData.getInvoiceNum();
                    reExifId = afmFileExif.getExifId();
                    reFileMd5 = afmFileExif.getFileMd5();
                } else {
                    if (null == afmInvoiceFileData) {
                        String invoiceOcrInfo = invoiceOcr(OcrConstant.SUN_OCR_URL, base64File);
                        //进行ocr信息提取
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
                    //发票验真
                    invoiceValidateResult = invoiceValidate(afmInvoiceFileData.getInvoiceCode(),
                            afmInvoiceFileData.getInvoiceNum(),
                            afmInvoiceFileData.getInvoiceCheckCode(),
                            afmInvoiceFileData.getInvoiceTotal(),
                            afmInvoiceFileData.getInvoiceTotal(),
                            afmInvoiceFileData.getInvoiceDate(),
                            afmInvoiceFileData.getInvoiceType());
                    reInvoiceNum = afmInvoiceFileData.getInvoiceNum();
                    reExifId = afmFileExif.getExifId();
                    reFileMd5 = afmFileExif.getFileMd5();
                }
            } else {
                //上传文件
                UploadListVO uploadListVO = getUploadListVO(userId, bytes, fileName);
                List<UploadListVO> uploadList = new ArrayList<>();
                uploadList.add(uploadListVO);
                Result<List<SysFileDTO>> listResult = storageApi.uploadBatch(uploadList);
                //插入文件信息表
                AfmFileExif newAfmFileExif = getAfmFileExif(listResult.getData().get(0));
                newAfmFileExif.setFileName(fileName);
                newAfmFileExif.setFileIndex(newAfmFileExif.getSourceSys()+"_"+newAfmFileExif.getFileIndex());
                afmFileExifMapper.insert(newAfmFileExif);
                //拿发票ocr信息
                AfmInvoiceFileData afmInvoiceFileData = afmInvoiceFileDataMapper
                        .selectOne(new LambdaQueryWrapper<AfmInvoiceFileData>()
                                .eq(AfmInvoiceFileData::getFileMd5, newAfmFileExif.getFileMd5()));
                if (null == afmInvoiceFileData) {
                    //进行ocr信息提取
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
                //发票验真
                invoiceValidateResult = invoiceValidate(afmInvoiceFileData.getInvoiceCode(),
                        afmInvoiceFileData.getInvoiceNum(),
                        afmInvoiceFileData.getInvoiceCheckCode(),
                        afmInvoiceFileData.getInvoiceTotal(),
                        afmInvoiceFileData.getInvoiceTotal(),
                        afmInvoiceFileData.getInvoiceDate(),
                        afmInvoiceFileData.getInvoiceType());
                reInvoiceNum = afmInvoiceFileData.getInvoiceNum();
                reExifId = newAfmFileExif.getExifId();
                reFileMd5 = newAfmFileExif.getFileMd5();
            }
        } else {
            //拿到文件url，请求拿到文件流
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
            byte[] bytes = IoUtil.readBytes(inputStream);
            String base64File = Base64.getEncoder().encodeToString(bytes);
            result.setSourceFileBase64(base64File);
            //拿发票ocr信息
            AfmInvoiceFileData afmInvoiceFileData = afmInvoiceFileDataMapper
                    .selectOne(new LambdaQueryWrapper<AfmInvoiceFileData>()
                            .eq(AfmInvoiceFileData::getFileMd5, afmFileExif.getFileMd5()));
            if (null == afmInvoiceFileData) {
                //进行ocr信息提取
                String invoiceOcrInfo = invoiceOcr(OcrConstant.SUN_OCR_URL, base64File);
                if (!StringUtils.hasText(JSONObject.parseObject(invoiceOcrInfo)
                        .getJSONObject("result")
                        .get(OcrConstant.INVOICE_TYPE).toString())) {
                    return Result.error("请上传发票文件", ResultCode.SYSTEM_BUSY_ERROR);
                }
                //解析ocr信息获取发票号并把信息存入afm_invoice_file_data表
                afmInvoiceFileData = analysisInvoiceOcrInfoAndInsertDb(invoiceOcrInfo, afmFileExif.getFileMd5());
                afmInvoiceFileDataMapper.insert(afmInvoiceFileData);
            }
            reInvoiceNum = afmInvoiceFileData.getInvoiceNum();
            reExifId = exifId;
            reFileMd5 = afmFileExif.getFileMd5();
            //通过fileId判断文件是否已经验真过，已验真过直接返回验证结果（查重、连续需要重新判断）
            List<AfmInvoiceDetNote> afmInvoiceDetNotes = afmInvoiceDetNoteMapper
                    .selectList(new LambdaQueryWrapper<AfmInvoiceDetNote>()
                            .eq(AfmInvoiceDetNote::getExifId, exifId)
                            .orderByDesc(AfmInvoiceDetNote::getInvoiceDetTime));
            if (!CollectionUtils.isEmpty(afmInvoiceDetNotes)) {
                //拿检测时间最新的，查询时已按照时间排序
                afmInvoiceDetNote = afmInvoiceDetNotes.get(0);
                //拿检测时间最新的，查询时已按照时间排序，存在记录则直接拿验真结果
                invoiceValidateResult = afmInvoiceDetNote.getInvoiceVerifyResult();
            } else {
                afmInvoiceDetNote = new AfmInvoiceDetNote();
                BeanUtils.copyProperties(afmFileExif, afmInvoiceDetNote);
                //发票验真
                invoiceValidateResult = invoiceValidate(afmInvoiceFileData.getInvoiceCode(),
                        afmInvoiceFileData.getInvoiceNum(), afmInvoiceFileData.getInvoiceCheckCode(),
                        afmInvoiceFileData.getInvoiceTotal(), afmInvoiceFileData.getInvoiceTotal(),
                        afmInvoiceFileData.getInvoiceDate(), afmInvoiceFileData.getInvoiceType());
            }
        }
        result.setIsVerify(invoiceValidateResult);
        //发票查重
        List<AfmDetOnlineResultDetailsDTO> invoiceDupList = invoiceDup(reInvoiceNum, reFileMd5, token);
        List<Long> dupIdList = invoiceDupList.stream().map(AfmDetOnlineResultDetailsDTO::getExifId).collect(Collectors.toList());
        //发票连续 目前暂定半径为1（前后各一个）
        List<AfmDetOnlineResultDetailsDTO> invoiceLinkList = invoiceLink(reInvoiceNum, 1, token);
        List<Long> linkIdList = invoiceLinkList.stream().map(AfmDetOnlineResultDetailsDTO::getExifId).collect(Collectors.toList());
        //把这三个检测记录存入数据库(不更新，存新的数据)
        setResultToObj(reExifId, invoiceValidateResult, invoiceDupList.isEmpty() ? 1 : 0,
                invoiceLinkList.isEmpty() ? 1 : 0, afmInvoiceDetNote);
        afmInvoiceDetNoteMapper.insert(afmInvoiceDetNote);
        //把关联数据存入关联表
        List<AfmInvoiceDetNoteAssoc> accocList = getAfmInvoiceDetNoteAssoc(afmInvoiceDetNote.getId(), dupIdList, linkIdList);
        if(!CollectionUtils.isEmpty(accocList)){
            MybatisBatch<AfmInvoiceDetNoteAssoc> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, accocList);
            MybatisBatch.Method<AfmInvoiceDetNoteAssoc> method = new MybatisBatch.Method<>(AfmInvoiceDetNoteAssocMapper.class);
            mybatisBatch.execute(method.insert());
        }
        result.setNoteId(afmInvoiceDetNote.getId());
        return Result.success(result);
    }

    /**
     * 获取发票查重结果
     */
    public Result detDupResult(Long id, String token, PageForm pageForm) {
        Assert.isTrue(null != id, "参数错误");
        //查发票检测记录表
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<AfmInvoiceDetNoteAssoc> resultList = afmInvoiceDetNoteAssocMapper
                .selectList(new LambdaQueryWrapper<AfmInvoiceDetNoteAssoc>()
                        .eq(AfmInvoiceDetNoteAssoc::getInvoiceNoteId, id)
                        .eq(AfmInvoiceDetNoteAssoc::getAssocType, 0)
                        .orderByDesc(AfmInvoiceDetNoteAssoc::getCreateTime)
                );
        PageInfo<AfmInvoiceDetNoteAssoc> resultListPageInfo = new PageInfo<>(resultList);
        return Result.success(getResultDetails(token, pageForm, resultListPageInfo));
    }

    /**
     * 获取发票连续结果
     */
    public Result detLinkResult(Long id, String token, PageForm pageForm) {
        Assert.isTrue(null != id, "参数错误");
        //查发票检测记录表
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<AfmInvoiceDetNoteAssoc> resultList = afmInvoiceDetNoteAssocMapper
                .selectList(new LambdaQueryWrapper<AfmInvoiceDetNoteAssoc>()
                        .eq(AfmInvoiceDetNoteAssoc::getInvoiceNoteId, id)
                        .eq(AfmInvoiceDetNoteAssoc::getAssocType, 1)
                        .orderByDesc(AfmInvoiceDetNoteAssoc::getCreateTime)
                );
        PageInfo<AfmInvoiceDetNoteAssoc> resultListPageInfo = new PageInfo<>(resultList);
        return Result.success(getResultDetails(token, pageForm, resultListPageInfo));
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
                .between(!ObjectUtils.isEmpty(vo.getCreateTimeEnd()) && !ObjectUtils.isEmpty(vo.getCreateTimeEnd()),
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
     * 发票记录详情
     */
    public Result<AfmDetNoteDetailsDTO> noteDetails(Long id, String token) {
        Assert.isTrue(null != id, "参数错误");
        AfmFileExif afmFileExif = afmFileExifMapper.selectById(id);
        AfmDetNoteDetailsDTO afmDetNoteDetailsDTO = new AfmDetNoteDetailsDTO();
        if (StringUtils.hasText(afmFileExif.getFileExif())) {
            afmDetNoteDetailsDTO = JSONObject.parseObject(afmFileExif.getFileExif(), AfmDetNoteDetailsDTO.class);
        }
        Map<String, String> afmSource = commonService.getAfmSource();
        BeanUtils.copyProperties(afmFileExif, afmDetNoteDetailsDTO);
        afmDetNoteDetailsDTO.setSourceSys(afmSource.get(afmDetNoteDetailsDTO.getSourceSys()));
        afmDetNoteDetailsDTO.setBusinessType(afmDetNoteDetailsDTO.getBusinessType().split(AfmConstant.SUFF)[1]);
        afmDetNoteDetailsDTO.setMaterialType(afmDetNoteDetailsDTO.getMaterialType().split(AfmConstant.SUFF)[1]);
        afmDetNoteDetailsDTO.setExifId(afmFileExif.getExifId());
        String[] fileFormat = afmFileExif.getFileName().split("\\.");
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
        return Result.success(afmDetNoteDetailsDTO);
    }

    /**
     * 发票记录属性
     */
    public Result noteAttr(Long id, String token) {
        Assert.isTrue(null != id, "参数错误");
        AfmFileExif afmFileExif = afmFileExifMapper.selectById(id);
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
     * 发票结果--详情、比对
     */
    public Result<AfmDetOnlineInvoiceDetDTO> resultDetails(Long exifId, String token) {
        Assert.isTrue(null != exifId, "参数错误");
        AfmDetOnlineInvoiceDetDTO result = new AfmDetOnlineInvoiceDetDTO();
        List<AfmInvoiceDetNote> afmInvoiceDetNotes = afmInvoiceDetNoteMapper
                .selectList(new LambdaQueryWrapper<AfmInvoiceDetNote>()
                        .eq(AfmInvoiceDetNote::getExifId, exifId)
                        .orderByDesc(AfmInvoiceDetNote::getInvoiceDetTime));
        if (!CollectionUtils.isEmpty(afmInvoiceDetNotes)) {
            result.setIsVerify(afmInvoiceDetNotes.get(0).getInvoiceVerifyResult());
            //发票验真结果
            //发票查重、连续结果
            List<AfmInvoiceDetNoteAssoc> afmInvoiceDetNoteAssocs = afmInvoiceDetNoteAssocMapper
                    .selectList(new LambdaQueryWrapper<AfmInvoiceDetNoteAssoc>()
                            .eq(AfmInvoiceDetNoteAssoc::getInvoiceNoteId, afmInvoiceDetNotes.get(0).getId()));
            setDupAndLinkResult(afmInvoiceDetNoteAssocs, result, token);

        } else {
            result.setIsVerify(0);
            result.setInvoiceDupList(new ArrayList<>());
            result.setInvoiceLinkList(new ArrayList<>());
        }
        return Result.success(result);
    }


    private PageInfo<AfmDetOnlineResultDetailsDTO> getResultDetails(String token,
                                                                    PageForm pageForm,
                                                                    PageInfo<AfmInvoiceDetNoteAssoc> resultListPageInfo) {
        PageInfo<AfmDetOnlineResultDetailsDTO> result = new PageInfo<>();
        List<AfmDetOnlineResultDetailsDTO> list = new ArrayList<>();
        Map<String, String> afmSource = commonService.getAfmSource();
        if (!CollectionUtils.isEmpty(resultListPageInfo.getList())) {
            List<Long> reFileId = resultListPageInfo.getList().stream()
                    .map(AfmInvoiceDetNoteAssoc::getAssocExifId)
                    .collect(Collectors.toList());
            List<AfmFileExif> afmFileExifs = afmFileExifMapper
                    .selectList(new LambdaQueryWrapper<AfmFileExif>()
                            .in(AfmFileExif::getExifId, reFileId));
            afmFileExifs.stream().forEach(item -> {
                AfmDetOnlineResultDetailsDTO resultItem = new AfmDetOnlineResultDetailsDTO();
                AfmInvoiceFileData afmInvoiceFileData = afmInvoiceFileDataMapper
                        .selectOne(new LambdaQueryWrapper<AfmInvoiceFileData>()
                                .eq(AfmInvoiceFileData::getFileMd5, item.getFileMd5()));
                BeanUtils.copyProperties(item, resultItem);
                resultItem.setSourceSys(afmSource.get(resultItem.getSourceSys()));
                resultItem.setBusinessType(resultItem.getBusinessType().split(AfmConstant.SUFF)[1]);
                resultItem.setMaterialType(resultItem.getMaterialType().split(AfmConstant.SUFF)[1]);
                resultItem.setInvoiceNum(afmInvoiceFileData.getInvoiceNum());
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
                list.add(resultItem);
            });
        }
        result.setList(list);
        result.setPageNum(pageForm.getPageNum());
        result.setPageSize(pageForm.getPageSize());
        result.setTotal(resultListPageInfo.getTotal());
        return result;
    }

    private void setDupAndLinkResult(List<AfmInvoiceDetNoteAssoc> list, AfmDetOnlineInvoiceDetDTO result, String token) {
        List<AfmDetOnlineResultDetailsDTO> invoiceDupList = new ArrayList<>();
        List<AfmDetOnlineResultDetailsDTO> invoiceLinkList = new ArrayList<>();
        Map<String, String> afmSource = commonService.getAfmSource();
        list.forEach(item -> {
            AfmDetOnlineResultDetailsDTO obj = new AfmDetOnlineResultDetailsDTO();
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

    private List<AfmInvoiceFileData> getAfmInvoiceFileData(String invoiceNum, Integer count) {
        return afmInvoiceFileDataMapper.selectList(new LambdaQueryWrapper<AfmInvoiceFileData>()
                .eq(AfmInvoiceFileData::getInvoiceNum, Long.parseLong(invoiceNum) - count)
                .or().eq(AfmInvoiceFileData::getInvoiceNum, Long.parseLong(invoiceNum) + count));
    }

    /**
     * 解析发票ocr信息
     */
    private AfmInvoiceFileData analysisInvoiceOcrInfoAndInsertDb(String str, String fileMd5) {
        AfmInvoiceFileData afmInvoiceFileData = new AfmInvoiceFileData();
        JSONObject resultJson = JSONObject.parseObject(str);
        JSONObject invoiceOcrJson = JSONObject.parseObject(resultJson.get("result").toString());
        afmInvoiceFileData.setFileMd5(fileMd5);
        afmInvoiceFileData.setInvoiceCode(invoiceOcrJson.get(OcrConstant.INVOICE_CODE).toString());
        afmInvoiceFileData.setInvoiceNum(invoiceOcrJson.get(OcrConstant.INVOICE_NUM).toString());
        afmInvoiceFileData.setInvoiceCheckCode(invoiceOcrJson.get(OcrConstant.INVOICE_CHECK_CODE).toString());
        afmInvoiceFileData.setInvoiceType(invoiceOcrJson.get(OcrConstant.INVOICE_TYPE).toString());
        afmInvoiceFileData.setInvoiceDate(invoiceOcrJson.get(OcrConstant.INVOICE_DATE).toString());
        afmInvoiceFileData.setInvoiceTotal(invoiceOcrJson.get(OcrConstant.INVOICE_TOTAL).toString());
        return afmInvoiceFileData;
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
     * 设置AfmInvoiceDetNote结果
     */
    private void setResultToObj(Long reExifId, Integer invoiceValidateResult, Integer invoiceDupResult,
                                Integer invoiceLinkResult, AfmInvoiceDetNote afmInvoiceDetNote) {
        afmInvoiceDetNote.setId(null);
        afmInvoiceDetNote.setExifId(reExifId);
        afmInvoiceDetNote.setInvoiceDetTime(new Date());
        afmInvoiceDetNote.setInvoiceVerifyResult(invoiceValidateResult);
        afmInvoiceDetNote.setInvoiceDupResult(invoiceDupResult);
        afmInvoiceDetNote.setInvoiceLinkResult(invoiceLinkResult);
        afmInvoiceDetNote.setCreateTime(null);
        afmInvoiceDetNote.setUpdateTime(null);
    }

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
     * 获取插入文件对象
     */
    private AfmFileExif getAfmFileExif(SysFileDTO upload) {
        AfmFileExif afmFileExif = new AfmFileExif();
        afmFileExif.setSourceSys(AfmConstant.AFM_SOURCESYS);
        //在线检测设置fileIndex与fileMd5一样
        afmFileExif.setFileIndex(upload.getFileMd5());
        afmFileExif.setFileMd5(upload.getFileMd5());
        afmFileExif.setFileUrl(storageUrl + upload.getId());
        return afmFileExif;
    }
}
