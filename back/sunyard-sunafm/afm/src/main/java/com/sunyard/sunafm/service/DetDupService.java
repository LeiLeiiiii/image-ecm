package com.sunyard.sunafm.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.afm.api.dto.AfmDetImgDetDTO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.sunafm.constant.AfmConstant;
import com.sunyard.sunafm.dto.AfmDetOnlineFileDTO;
import com.sunyard.sunafm.dto.AfmDetOnlineImgDetDTO;
import com.sunyard.sunafm.mapper.AfmFileExifMapper;
import com.sunyard.sunafm.mapper.AfmImageDupAssocMapper;
import com.sunyard.sunafm.mapper.AfmImageDupNoteMapper;
import com.sunyard.sunafm.mapper.AfmServerMapper;
import com.sunyard.sunafm.po.AfmFileExif;
import com.sunyard.sunafm.po.AfmImageDupAssoc;
import com.sunyard.sunafm.po.AfmImageDupNote;
import com.sunyard.sunafm.po.AfmServer;
import com.sunyard.sunafm.util.CnnHttpUtil;
import com.sunyard.sunafm.util.Md5Utils;
import com.sunyard.sunafm.util.MilvusExprUtils;
import com.sunyard.sunafm.vo.AfmDetOnlineListVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author P-JWei
 * @date 2024/3/20 14:05:53
 * @title
 * @description 在线检测/图像查重实现类
 */
@Slf4j
@Service
public class DetDupService {
    @Value("${cnn.baseUrl}")
    private String cnnUrl;
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private AfmImageDupNoteMapper afmImageDupNoteMapper;
    @Resource
    private AfmFileExifMapper afmFileExifMapper;
    @Resource
    private AfmImageDupAssocMapper afmImageDupAssocMapper;
    @Resource
    private AfmServerMapper afmServerMapper;
    @Resource
    private CommonService commonService;

    /**
     *  在线查重
     */
    public AfmDetImgDetDTO dupOnline(MultipartFile file, Long exifId, String tokenValue, AccountToken token) {
        Assert.isTrue(null != file || null != exifId, "参数错误");
        AfmDetImgDetDTO retStr = new AfmDetImgDetDTO();
        retStr.setSourceSys(AfmConstant.AFM_SOURCESYS);
        AfmImageDupNote afmImageDupNote = new AfmImageDupNote();
        afmImageDupNote.setUploadUserCode(token.getUsername());
        afmImageDupNote.setUploadUserName(token.getName());
        afmImageDupNote.setUploadOrg(token.getOrgCode());
        afmImageDupNote.setUploadOrgName(token.getOrgName());
        afmImageDupNote.setSourceSys(AfmConstant.AFM_SOURCESYS);
        afmImageDupNote.setCreateTime(new Date());
        Long noteId = null;
        String cnnStr = null;
        String md5 = null;
        Boolean flag = false;
        if (file != null) {
            // 读取MultipartFile内容为字节数组
            byte[] fileContent = new byte[0];
            try {
                // 返回压缩后的图像字节数组
                fileContent = file.getBytes();
            } catch (IOException e) {
                e.printStackTrace();
            }
            md5 = Md5Utils.calculateMD5(fileContent);
            String base64 = Md5Utils.convertMultipartFileToBase64(fileContent);
            afmImageDupNote.setExifIdOrMd5(md5);
            afmImageDupNote.setFileName(file.getOriginalFilename());

            List<String> exList = new ArrayList<>();
            exList.add(md5);
            List<AfmImageDupNote> afmImageDupNotes = afmImageDupNoteMapper.selectList(new LambdaQueryWrapper<AfmImageDupNote>()
                    .eq(AfmImageDupNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                    .eq(AfmImageDupNote::getExifIdOrMd5, md5).orderByDesc(AfmImageDupNote::getCreateTime));
            JSONObject systemFileNet = commonService.getSystemFileNet();
            if (!CollectionUtils.isEmpty(afmImageDupNotes)) {
                AfmImageDupNote exif1 = afmImageDupNotes.get(0);
                afmImageDupNote.setId(exif1.getId());
                afmImageDupNote.setSimilarity(exif1.getSimilarity());
                List<AfmImageDupAssoc> list1 = afmImageDupAssocMapper.selectList(new LambdaQueryWrapper<AfmImageDupAssoc>()
                        .eq(AfmImageDupAssoc::getDupNoteId, exif1.getId()));
                if (!CollectionUtils.isEmpty(list1)) {
                    List<Long> collect = list1.stream().map(AfmImageDupAssoc::getAssocExifId).collect(Collectors.toList());
                    List<AfmFileExif> afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>().in(AfmFileExif::getExifId, collect));
                    List<String> collect1 = afmFileExifs.stream().map(AfmFileExif::getFileMd5).collect(Collectors.toList());
                    exList.addAll(collect1);
                }
                noteId = exif1.getId();
                flag = false;
            } else {
                noteId = snowflakeUtil.nextId();
                afmImageDupNote.setSimilarity(systemFileNet.getDouble(AfmConstant.FILE_SIMILARITY_SYSTEM));
                flag = true;
            }

            //获取文件base64
            String url = cnnUrl + "/queryFilesSingleNoSaveByBase64";
            Map map = new HashMap();
            map.put("token", tokenValue);
            map.put("file_limit", systemFileNet.getInteger(AfmConstant.FILE_NUM_SYSTEM));
            map.put("file_similarity", systemFileNet.getDouble(AfmConstant.FILE_SIMILARITY_SYSTEM));
            map.put("file_base64", base64);
            map.put("ex_list", exList);
            map.put("is_opencv_check", 1);
            map.put("thread_num", 3);
            List<AfmServer> afmServers = afmServerMapper.selectList(new LambdaQueryWrapper<AfmServer>()
                    .eq(AfmServer::getStatus, AfmConstant.YES));
            map.put("query_server", JSONObject.toJSONString(afmServers));
            String s = JSON.toJSONString(map);
            cnnStr = CnnHttpUtil.getHttp(url, s);

            retStr.setFileMd5(md5);
        } else if (exifId != null) {
            //根据exifId 获取文件进行查重
            AfmFileExif exif = afmFileExifMapper.selectById(exifId);
            afmImageDupNote.setSourceSys(exif.getSourceSys());
            afmImageDupNote.setBusinessType(exif.getBusinessType());
            afmImageDupNote.setBusinessIndex(exif.getBusinessIndex());
            afmImageDupNote.setMaterialType(exif.getMaterialType());
            afmImageDupNote.setUploadUserCode(exif.getUploadUserCode());
            afmImageDupNote.setUploadUserName(exif.getUploadUserName());
            afmImageDupNote.setUploadOrg(exif.getUploadOrg());
            afmImageDupNote.setUploadOrgName(exif.getUploadOrg());
            AssertUtils.isNull(exif, "参数错误");
            md5 = exif.getFileMd5();
            retStr.setSourceSys(exif.getSourceSys());
            AfmDetImgDetDTO dto = new AfmDetImgDetDTO();
            dto.setFileToken(tokenValue);
            JSONObject systemFileNet = commonService.getSystemFileNet();
            dto.setFileLimit(systemFileNet.getInteger(AfmConstant.FILE_NUM_SYSTEM));
            dto.setFileSimilarity(systemFileNet.getDouble(AfmConstant.FILE_SIMILARITY_SYSTEM));
            afmImageDupNote.setSimilarity(systemFileNet.getDouble(AfmConstant.FILE_SIMILARITY_SYSTEM));
            String url = cnnUrl + "/queryFilesSingleNoSave";

            List<String> exList = new ArrayList<>();
            exList.add(exif.getFileMd5());
            afmImageDupNote.setExifIdOrMd5(exif.getExifId().toString());
            afmImageDupNote.setFileName(exif.getFileName());
            List<AfmImageDupNote> afmImageDupNotes = afmImageDupNoteMapper.selectList(new LambdaQueryWrapper<AfmImageDupNote>()
                    .eq(AfmImageDupNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                    .eq(AfmImageDupNote::getExifIdOrMd5, exif.getExifId().toString())
                    .orderByDesc(AfmImageDupNote::getCreateTime));
            if (!CollectionUtils.isEmpty(afmImageDupNotes)) {
                AfmImageDupNote exif1 = afmImageDupNotes.get(0);
                afmImageDupNote.setId(exif1.getId());
                List<AfmImageDupAssoc> list1 = afmImageDupAssocMapper.selectList(new LambdaQueryWrapper<AfmImageDupAssoc>()
                        .eq(AfmImageDupAssoc::getDupNoteId, exif1.getId()));
                if (!CollectionUtils.isEmpty(list1)) {
                    List<Long> collect = list1.stream().map(AfmImageDupAssoc::getAssocExifId).collect(Collectors.toList());
                    List<AfmFileExif> afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>().in(AfmFileExif::getExifId, collect));
                    List<String> collect1 = afmFileExifs.stream().map(AfmFileExif::getFileMd5).collect(Collectors.toList());
                    exList.addAll(collect1);
                }
                noteId = exif1.getId();
                flag= false;
            } else {
                noteId = snowflakeUtil.nextId();
                flag = true;
            }

            dto.setFileUrl(exif.getFileUrl());
            dto.setFileMd5(exif.getFileMd5());
            dto.setMaterialType(exif.getMaterialType());
            dto.setFileExif(exif.getFileExif());
            dto.setExList(exList);
            //发起查重
            String s = getParamCnn(dto,AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE);
            cnnStr = CnnHttpUtil.getHttp(url, s);
            retStr.setExifId(exif.getExifId());
            retStr.setFileIndex(exif.getFileIndex());
        }
        afmImageDupNote.setImgDupTime(new Date());
        CnnHttpUtil.CnnRetHandle result = CnnHttpUtil.getCnnRetHandle(cnnStr);
        if (result.succ) {
            JSONArray array = result.jsonObject1.getJSONArray("data");
            //保存相似度
            List<AfmImageDupAssoc> afmImageDupAssocs = saveAssoc(afmImageDupNote, noteId, array, md5, retStr.getExifId(),flag);
            if(!CollectionUtils.isEmpty(afmImageDupAssocs)){
                afmImageDupAssocMapper.insertBatch(afmImageDupAssocs);
            }
            if (afmImageDupNote.getId() == null) {
                afmImageDupNote.setId(noteId);
                //插入查重数据
                afmImageDupNoteMapper.insert(afmImageDupNote);
            } else {
                //更新查重数据
                afmImageDupNoteMapper.updateById(afmImageDupNote);
            }

        }
        //获取结果
        return retStr;
    }

    /**
     * 分页
     */
    public Map antiFraudDetResPage(AfmDetImgDetDTO dto, PageForm pageForm) {
        AssertUtils.isTrue(StringUtils.isBlank(dto.getFileIndex()) && StringUtils.isBlank(dto.getFileMd5()), "参数有误");
        Map map = new HashMap();
        List<AfmImageDupNote> afmImageDupNotes = afmImageDupNoteMapper.selectList(new LambdaQueryWrapper<AfmImageDupNote>()
                .eq(AfmImageDupNote::getSourceSys, dto.getSourceSys())
                .eq(null != dto.getExifId(), AfmImageDupNote::getExifIdOrMd5, dto.getExifId())
                .eq(StringUtils.isNotBlank(dto.getFileMd5()) && StringUtils.isBlank(dto.getFileIndex()),
                        AfmImageDupNote::getExifIdOrMd5, dto.getFileMd5())
                .orderByDesc(AfmImageDupNote::getCreateTime));
        Map<String, String> afmSource = commonService.getAfmSource();
        if (!CollectionUtils.isEmpty(afmImageDupNotes)) {
            AfmImageDupNote afmImageDupNote = afmImageDupNotes.get(0);
            Map map1 = new HashMap();
            map1.put("businessIndex", afmImageDupNote.getBusinessIndex());
            map1.put("createTime", afmImageDupNote.getCreateTime());
            map1.put("fileName", afmImageDupNote.getFileName());
            map1.put("sourceSys", afmSource.get(afmImageDupNote.getSourceSys()));
            if (dto.getExifId() != null) {
                AfmFileExif exif1 = afmFileExifMapper.selectById(dto.getExifId());
                if (exif1 != null) {
                    JSONObject jsonObject = JSONObject.parseObject(exif1.getFileExif());
                    if (exif1.getBusinessType() != null) {
                        String[] split = exif1.getBusinessType().split(AfmConstant.SUFF);
                        map1.put("businessType", split[1]);
                    }
                    if (exif1.getMaterialType() != null) {
                        String[] split1 = exif1.getMaterialType().split(AfmConstant.SUFF);
                        map1.put("materialType", split1[1]);
                    }
                    map1.put("businessIndex", exif1.getBusinessIndex());
                    map1.put("fileSize", jsonObject.getLong("fileSize") / 1000);
                    map1.put("uploadOrg", afmImageDupNote.getUploadOrg());
                    map1.put("userName", afmImageDupNote.getUploadUserName());
                    map1.put("fileUrl", exif1.getFileUrl());
                    map1.put("sourceSys", afmSource.get(exif1.getSourceSys()));
                }
            }

            map.put("noteObj", map1);
            PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
            List<AfmDetOnlineImgDetDTO> afmFileExifs = afmFileExifMapper.queryFileByNoteId(afmImageDupNote.getId(),
                    afmImageDupNote.getSimilarity(),null,
                    dto.getExifId(),
                    AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE);
            PageInfo<AfmDetOnlineImgDetDTO> pageInfo = new PageInfo<>(afmFileExifs);
            pageInfo.getList().forEach(exif -> {
                if (exif.getBusinessType() != null) {
                    exif.setBusinessType(exif.getBusinessType().split(AfmConstant.SUFF)[1]);
                }
                if (exif.getMaterialType() != null) {
                    exif.setMaterialType(exif.getMaterialType().split(AfmConstant.SUFF)[1]);
                }
                if (exif.getFileUrl() != null) {
                    exif.setFileFullPath(exif.getFileUrl());
                }
                if (StringUtils.isNotBlank(exif.getFileExif())) {
                    JSONObject jsonObject = JSONObject.parseObject(exif.getFileExif());
                    if (jsonObject.getLong("fileSize") != null) {
                        exif.setFileSize((jsonObject.getLong("fileSize") / 1000) + "KB");
                    }
                    if (jsonObject.getString("format") != null) {
                        exif.setFormat(jsonObject.getString("format"));
                    }
                    HashMap map2 = JSONObject.parseObject(exif.getFileExif(), HashMap.class);
                    exif.setFileExifMap(map2);
                }
                exif.setSourceSys(afmSource.get(exif.getSourceSys()));
                exif.setImgDupTime(exif.getCreateTime());
                exif.setSimilarity(exif.getSimilarity());
                exif.setSimilarity(convertToPercentage(Double.parseDouble(exif.getSimilarity())));
            });

            map.put("page", pageInfo);
            return map;
        }
        return null;
    }

    /**
     * 获取查重结果
     */
    public Map antiFraudDetRes(AfmDetImgDetDTO dto) {
        Map map = new HashMap();
        map.put("similarityList", null);
        AfmFileExif exif1 = null;
        if (dto.getExifId() != null) {
            exif1 = afmFileExifMapper.selectById(dto.getExifId());
        } else {
            List<AfmFileExif> afmImageDupNotes = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                    .eq(AfmFileExif::getFileIndex, dto.getSourceSys() + "_" + dto.getFileIndex())
                    .orderByDesc(AfmFileExif::getCreateTime));
            if (CollectionUtils.isEmpty(afmImageDupNotes)) {
                return map;
            } else {
                exif1 = afmImageDupNotes.get(0);
            }
        }

        List<AfmImageDupNote> afmImageDupNotes1 = afmImageDupNoteMapper.selectList(new LambdaQueryWrapper<AfmImageDupNote>()
                .eq(dto.getNoteId() != null, AfmImageDupNote::getId, dto.getNoteId())
                .eq(AfmImageDupNote::getExifIdOrMd5, exif1.getExifId()).orderByDesc(AfmImageDupNote::getCreateTime));
        if (!CollectionUtils.isEmpty(afmImageDupNotes1)) {
            AfmImageDupNote afmImageDupNote = afmImageDupNotes1.get(0);
            map.put("afmImageDupNote", afmImageDupNote);
            map.put("isDet", "正常");
            List<AfmDetOnlineImgDetDTO> afmFileExifs = afmFileExifMapper.queryFileByNoteId(afmImageDupNote.getId(),
                    afmImageDupNote.getSimilarity(), dto.getSourceSys() + "_" +dto.getFileIndex(), dto.getExifId(),
                    AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE);
            if (!CollectionUtils.isEmpty(afmFileExifs)) {
                ArrayList<Map> objects = new ArrayList<>();
                Map<String, String> afmSource = commonService.getAfmSource();
                for (AfmDetOnlineImgDetDTO exif : afmFileExifs) {
                    HashMap map1 = JSONObject.parseObject(exif.getFileExif(), HashMap.class);
                    if (map1 != null) {
                        map1.put("exifId", exif.getExifId());
                        map1.put("fileFullPath", exif.getFileUrl());
                        map1.put("fileUrl", exif.getFileUrl());
                        String fileIndex = exif.getFileIndex();
                        if(fileIndex.startsWith(exif.getSourceSys()+"_")){
                            fileIndex = fileIndex.split("_")[1];
                        }
                        map1.put("fileIndex", fileIndex);
                        map1.put("fileName", exif.getFileName());
                        map1.put("sourceSys", afmSource != null ? afmSource.get(exif.getSourceSys()) : dto.getSourceSys());
                        map1.put("businessType", exif.getBusinessType() != null ? exif.getBusinessType().split(AfmConstant.SUFF)[1] : exif.getBusinessType());
                        map1.put("businessIndex", exif.getBusinessIndex());
                        map1.put("materialType", exif.getMaterialType() != null ? exif.getMaterialType().split(AfmConstant.SUFF)[1] : exif.getMaterialType());
                        map1.put("uploadUserCode", exif.getUploadUserCode());
                        map1.put("uploadUserName", exif.getUploadUserName());
                        map1.put("uploadOrg", exif.getUploadOrg());
                        map1.put("fileMd5", exif.getFileMd5());
                        map1.put("createTime", exif.getCreateTime());
                        map1.put("similarity", convertToPercentage(Double.parseDouble(exif.getSimilarity())));
                        objects.add(map1);
                    }
                }
                Double similarity = Double.valueOf(afmFileExifs.get(0).getSimilarity());
                map.put("isDet", similarity > afmImageDupNote.getSimilarity() ? "疑似重复" : "正常");
                map.put("similarityList", objects);
            }
        }
        return map;
    }

    /**
     * 选择文件条件
     */
    public Map queryChooseConditions() {
        List<AfmFileExif> afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                .isNotNull(AfmFileExif::getBusinessType)
                .select(AfmFileExif::getBusinessType)
                .groupBy(AfmFileExif::getBusinessType));
        List<String> stringList =new ArrayList<>();
        if(!CollectionUtils.isEmpty(afmFileExifs)){
            stringList=afmFileExifs.stream()
                    .map(AfmFileExif::getBusinessType)
                    .distinct()
                    .collect(Collectors.toList());
        }
        List<Map> afmSourceLabel = commonService.getAfmSourceLabel(AfmConstant.AFM_SOURCESYS);
        List<AfmFileExif> afmFileExifs1 = afmFileExifMapper.selectList(
                new LambdaQueryWrapper<AfmFileExif>()
                        .isNotNull(AfmFileExif::getMaterialType)
                        .eq(AfmFileExif::getIsDeleted, 0)
                        .select(AfmFileExif::getMaterialType)
                        .groupBy(AfmFileExif::getMaterialType));
        List<String> stringList2 =new ArrayList<>();
        if(!CollectionUtils.isEmpty(afmFileExifs1)){
            stringList2 = afmFileExifs1.stream()
                    .map(AfmFileExif::getMaterialType)
                    .collect(Collectors.toList());
        }
        Map map = new HashMap();
        map.put("appTypeList", getMaps(stringList));
        map.put("sourceList", afmSourceLabel);
        map.put("materialTypeList", getMaps(stringList2));
        return map;
    }

    /**
     * 选择文件列表
     */
    public PageInfo<AfmDetOnlineFileDTO> chooseFile(AfmDetOnlineListVO afmDetOnlineListVO, PageForm pageForm) {
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<AfmFileExif> afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                .eq(AfmFileExif::getIsVector, AfmConstant.YES)
                .like(StringUtils.isNotBlank(afmDetOnlineListVO.getBusinessIndex()), AfmFileExif::getBusinessIndex, afmDetOnlineListVO.getBusinessIndex())
                .between(!ObjectUtils.isEmpty(afmDetOnlineListVO.getCreateTimeEnd()) && !ObjectUtils.isEmpty(afmDetOnlineListVO.getCreateTimeStart())
                        , AfmFileExif::getCreateTime, afmDetOnlineListVO.getCreateTimeStart(), afmDetOnlineListVO.getCreateTimeEnd())
                .like(StringUtils.isNotBlank(afmDetOnlineListVO.getMaterialType()), AfmFileExif::getMaterialType, afmDetOnlineListVO.getMaterialType() + AfmConstant.SUFF)
                .like(StringUtils.isNotBlank(afmDetOnlineListVO.getBusinessType()), AfmFileExif::getBusinessType, afmDetOnlineListVO.getBusinessType() + AfmConstant.SUFF)
                .eq(StringUtils.isNotBlank(afmDetOnlineListVO.getSourceSys()), AfmFileExif::getSourceSys, afmDetOnlineListVO.getSourceSys()));
        PageInfo pageInfo = new PageInfo<>(afmFileExifs);
        ArrayList<AfmDetOnlineFileDTO> objects = new ArrayList<>();
        Map<String, String> afmSource = commonService.getAfmSource();

        for (AfmFileExif exif : afmFileExifs) {
            AfmDetOnlineFileDTO dto = new AfmDetOnlineFileDTO();
            BeanUtils.copyProperties(exif, dto);
            if (StringUtils.isNotBlank(exif.getBusinessType())) {
                dto.setBusinessType(exif.getBusinessType().split(AfmConstant.SUFF)[1]);
            }
            if (StringUtils.isNotBlank(exif.getMaterialType())) {
                dto.setMaterialType(exif.getMaterialType().split(AfmConstant.SUFF)[1]);
            }
            dto.setSourceSys(afmSource.get(dto.getSourceSys()));
            dto.setUploadTime(exif.getCreateTime());
            objects.add(dto);
        }
        pageInfo.setList(objects);
        return pageInfo;
    }


    private List<Map> getMaps(List<String> stringList) {
        List<Map> ret = new ArrayList<>();
        for (String s : stringList) {
            String[] split = s.split(AfmConstant.SUFF);
            Map map1 = new HashMap();
            if (split.length == 1) {
                map1.put("label", split[0]);
                map1.put("value", split[0]);
            } else {
                map1.put("label", split[1]);
                map1.put("value", split[0]);
            }
            ret.add(map1);
        }
        return ret;
    }

    private String getParamCnn(AfmDetImgDetDTO vo,int detType) {
        Map<String, Object> mapCnn = getMapCnn(vo, null,detType);
        return JSON.toJSONString(mapCnn);
    }

    private Map<String, Object> getMapCnn(AfmDetImgDetDTO vo, Map<String, List<AfmFileExif>> collect3,int detType) {
        Map map = new HashMap<>();
        map.put("query_collect_name", new ArrayList<>());
        map.put("query_expr", "");
        map.put("file_exif", "");
        map.put("is_opencv_check", vo.getIsOpencvCheck() == null ? "1" : vo.getIsOpencvCheck());
        map.put("thread_num", vo.getThreadNum() == null ? "3" : vo.getThreadNum());
        if (!CollectionUtils.isEmpty(vo.getExList())) {
            map.put("ex_list", vo.getExList());
        } else {
            map.put("ex_list", new ArrayList<>());
        }
        if (StringUtils.isNotBlank(vo.getBase64())) {
            map.put("file_base64", vo.getBase64());
        }
        //写特征
        AfmServer afmServers = getAfmServerByCollectName(vo.getMaterialTypeCode(), vo.getServerId(), vo.getYear(),detType);
        map.put("server", JSONObject.toJSONString(afmServers));
        vo.setServerId(afmServers.getId());
        queryExprHandle(vo, map);

        if (StringUtils.isNotBlank(vo.getFileExif())) {
            //设置元数据，需要用key，多组value来表示
            //根据文件md5,查询数据库获取所有的文件数据
            List<AfmFileExif> afmFileExifs = new ArrayList<>();
            if (collect3 != null) {
                afmFileExifs = collect3.get(vo.getFileMd5());
            } else {
                afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>().eq(AfmFileExif::getFileMd5, vo.getFileMd5()));
            }
            if (CollectionUtils.isEmpty(afmFileExifs)) {
                afmFileExifs = new ArrayList<>();
            }

            List<String> collect = afmFileExifs.stream().map(AfmFileExif::getFileExif).collect(Collectors.toList());
            Set<String> set = new HashSet();
            List<JSONObject> jsonObjects = new ArrayList<>();
            for (String exifjson : collect) {
                if (StringUtils.isNotBlank(exifjson)) {
                    JSONObject jsonObject = JSONObject.parseObject(exifjson);
                    set.addAll(jsonObject.keySet());
                    jsonObjects.add(jsonObject);
                }
            }
            if (StringUtils.isNotBlank(vo.getFileExif())) {
                JSONObject jsonObject = JSONObject.parseObject(vo.getFileExif());
                jsonObjects.add(jsonObject);
                set.addAll(jsonObject.keySet());
            }
            Map<String, Set<String>> map1 = new HashMap<>();
            for (JSONObject json : jsonObjects) {
                for (String key : set) {
                    Set<String> stringList = map1.get(key);
                    if (stringList == null) {
                        stringList = new HashSet<>();
                    }
                    String string = json.getString(key);
                    if (StringUtils.isNotBlank(string)) {
                        stringList.add(string);
                    }
                    map1.put(key, stringList);
                }

            }
            String s1 = JSONObject.toJSONString(map1);
            map.put("file_exif", s1);
        }

        map.put("file_url", vo.getFileUrl());
        map.put("file_id", vo.getFileMd5());
        map.put("token", vo.getFileToken());
        if (vo.getFileLimit() != null) {
            map.put("file_limit", vo.getFileLimit());
        } else {
            JSONObject systemFileNet = commonService.getSystemFileNet();
            map.put("file_limit", systemFileNet.getInteger(AfmConstant.FILE_NUM_SYSTEM));
        }
        if (vo.getFileSimilarity() != null) {
            map.put("file_similarity", vo.getFileSimilarity());
        } else {
            map.put("file_similarity", commonService.getSimpleDefult());
        }
        return map;
    }


    private void queryExprHandle(AfmDetImgDetDTO vo, Map map) {
        //指定集合
        List<String> docCodes = new ArrayList<>();
        List<Integer> yearList = new ArrayList<>();
        //查询条件处理
        if (StringUtils.isNotBlank(vo.getQueryExpr())) {
            //集合条件
            JSONObject jsonObject = JSONObject.parseObject(vo.getQueryExpr());
            JSONArray materialType1 = jsonObject.getJSONArray("materialTypeCode");
            JSONArray years = jsonObject.getJSONArray("year");
            if (!CollectionUtils.isEmpty(materialType1)) {

                for (int i = 0; i < materialType1.size(); i++) {
                    String string = materialType1.getString(i);
                    docCodes.add(string);
                }
                if (!CollectionUtils.isEmpty(years)) {
                    for (int m = 0; m < years.size(); m++) {
                        Integer year = years.getInteger(m);
                        yearList.add(year);
                    }
                }

            }

            //查询条件
            Set<String> strings = jsonObject.keySet();
            Map<String, Set<String>> map1 = new HashMap<>();
            for (String s : strings) {
                JSONArray val = jsonObject.getJSONArray(s);
                Set<String> stringSet = new HashSet<>();
                for (int i = 0; i < val.size(); i++) {
                    String string = val.getString(i);
                    stringSet.add(string);
                }
                map1.put(s, stringSet);
            }
            String s = MilvusExprUtils.jsonAndEqIn("file_prop", map1);
            map.put("query_expr", s);
        }

        //服务器信息
        /**
         * 如果没有指定集合，则需要圈梁查询，如果指定了集合则，查对应集合的服务器即可。
         */
        List<AfmServer> collectNameBaseQuery = getCollectNameBaseQuery(docCodes, yearList);
        map.put("query_server", JSONObject.toJSONString(collectNameBaseQuery));


    }

    private List<AfmServer> getCollectNameBaseQuery(List<String> materialTypeCode, List<Integer> year) {
        //当这个文件指定的集合不存在，则证明是存特征逻辑，查询出现有可写的集合并返回集合名称
        List<AfmServer>    afmServers = afmServerMapper.selectList(new LambdaQueryWrapper<AfmServer>()
                    .isNotNull(AfmServer::getCollectionName)
                    .in(!CollectionUtils.isEmpty(materialTypeCode), AfmServer::getDocCode, materialTypeCode)
                    .in(!CollectionUtils.isEmpty(year), AfmServer::getYear, year)
                    .eq(AfmServer::getStatus, AfmConstant.YES));

        AssertUtils.isNull(afmServers, "暂无可读的向量服务，请检查配置");
        return afmServers;
    }

    /**
     * 保存相似度
     */
    private List<AfmImageDupAssoc> saveAssoc(AfmImageDupNote afmImageDupNote, long noteId, JSONArray array, String md5, Long exifId, Boolean flag) {
        List<AfmImageDupAssoc> list = new ArrayList<>();
        Map<String, Double> map = new HashMap();
        List<AfmImageDupAssoc> objects = new ArrayList<>();

        //插入相同md5的文件
        if (flag){
            List<AfmImageDupAssoc> afmImageDupAssocs = addAssoNoRecord(afmImageDupNote, noteId, md5, exifId, list);
            if(!CollectionUtils.isEmpty(afmImageDupAssocs)){
                objects.addAll(afmImageDupAssocs);
            }
        }else {
            List<AfmImageDupAssoc> list1 = afmImageDupAssocMapper.selectList(new LambdaQueryWrapper<AfmImageDupAssoc>().eq(AfmImageDupAssoc::getDupNoteId, noteId));

            if (!CollectionUtils.isEmpty(list1)) {
                list.addAll(list1);
                Map<Long, List<AfmImageDupAssoc>> collect1 = list1.stream().collect(Collectors.groupingBy(AfmImageDupAssoc::getAssocExifId));
                List<Long> exList = list1.stream().map(AfmImageDupAssoc::getAssocExifId).collect(Collectors.toList());
                if (exifId != null) {
                    exList.add(exifId);
                }

                //已存在的
                List<AfmFileExif> afmFileExifs1 = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>().in(AfmFileExif::getExifId, exList));
                List<String> collect = afmFileExifs1.stream().map(AfmFileExif::getFileMd5).collect(Collectors.toList());

                if(!CollectionUtils.isEmpty(collect)){
                    List<AfmFileExif> afmFileExifs2 = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>().in(AfmFileExif::getFileMd5, collect));

                    if (!CollectionUtils.isEmpty(afmFileExifs2)) {
                        List<AfmImageDupAssoc> afmImageDupAssocs = new ArrayList<>();
                        for (AfmFileExif exif : afmFileExifs2) {
                            if (!exList.contains(exif.getExifId())) {
                                AfmImageDupAssoc afmImageDupAssoc = new AfmImageDupAssoc();
                                afmImageDupAssoc.setId(snowflakeUtil.nextId());
                                afmImageDupAssoc.setDupNoteId(noteId);
                                afmImageDupAssoc.setAssocExifId(exif.getExifId());
                                List<AfmImageDupAssoc> list2 = collect1.get(exif.getExifId());
                                if (!CollectionUtils.isEmpty(list2)) {
                                    afmImageDupAssoc.setSimilarity(list2.get(0).getSimilarity());
                                }

                                list.add(afmImageDupAssoc);
                                afmImageDupAssocs.add(afmImageDupAssoc);

                            }
                        }
                        if(!CollectionUtils.isEmpty(afmImageDupAssocs)){
//                            insertAfmImageDupAssocs(afmImageDupAssocs);
                            objects.addAll(afmImageDupAssocs);
                        }
                    }
                }
            } else {
                List<AfmImageDupAssoc> afmImageDupAssocs = addAssoNoRecord(afmImageDupNote, noteId, md5, exifId, list);
                if(!CollectionUtils.isEmpty(afmImageDupAssocs)){
                    objects.addAll(afmImageDupAssocs);
                }
            }
        }
        if (!CollectionUtils.isEmpty(array)) {
            for (int i = 0; i < array.size(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String fileMd5 = jsonObject.getString("file_id");
                Double similarity = jsonObject.getDouble("similarity");
                map.put(fileMd5, similarity);
            }
            //
            List<AfmFileExif> afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                    .in(AfmFileExif::getFileMd5, map.keySet())
                    .orderByDesc(AfmFileExif::getCreateTime));
            Map<String, List<AfmFileExif>> collect = afmFileExifs.stream().collect(Collectors.groupingBy(AfmFileExif::getFileIndex));
            List<AfmImageDupAssoc> afmImageDupAssocs = new ArrayList<>();
            for (String id : collect.keySet()) {
                AfmFileExif exif2 = collect.get(id).get(0);
                AfmImageDupAssoc afmImageDupAssoc = new AfmImageDupAssoc();
                afmImageDupAssoc.setId(snowflakeUtil.nextId());
                afmImageDupAssoc.setDupNoteId(noteId);
                afmImageDupAssoc.setAssocExifId(exif2.getExifId());
                afmImageDupAssoc.setSimilarity(map.get(exif2.getFileMd5()));
                list.add(afmImageDupAssoc);
                afmImageDupAssocs.add(afmImageDupAssoc);
            }
            if(!CollectionUtils.isEmpty(afmImageDupAssocs)){
//                insertAfmImageDupAssocs(afmImageDupAssocs);
                objects.addAll(afmImageDupAssocs);
            }
        }
        if (!CollectionUtils.isEmpty(list)) {
            List<Double> collect1 = list.stream()
                    .filter(s -> s.getSimilarity() != null)
                    .map(AfmImageDupAssoc::getSimilarity).sorted().collect(Collectors.toList());
            Collections.reverse(collect1);
            afmImageDupNote.setImgDupResult(collect1.get(0));
        }
        return objects;
    }

    private List<AfmImageDupAssoc>  addAssoNoRecord(AfmImageDupNote afmImageDupNote, long noteId, String md5, Long exifId, List<AfmImageDupAssoc> list) {
        //关联的没有，但是这条数据同md5的有可能存在
        List<AfmFileExif> afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
//                .ne(AfmFileExif::getExifId, exifId)
//                .eq(AfmFileExif::getExifId, afmImageDupNote.getExifIdOrMd5())
//                .or()
                .eq(AfmFileExif::getFileMd5, md5));
//                .or()
//                .eq(AfmFileExif::getFileMd5, afmImageDupNote.getExifIdOrMd5()));
//        if (!CollectionUtils.isEmpty(afmFileExifs)) {
//            List<String> afmFileExifs2 = afmFileExifs.stream().map(AfmFileExif::getFileMd5).collect(Collectors.toList());
//            List<AfmFileExif> afmFileExifs1 = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
//                    .in(AfmFileExif::getFileMd5, afmFileExifs2));
            if (!CollectionUtils.isEmpty(afmFileExifs)) {
                List<AfmFileExif> collect = afmFileExifs.stream().filter(s -> !s.getExifId().equals(exifId)).collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(collect)){
                    List<AfmImageDupAssoc> afmImageDupAssocs = new ArrayList<>();
                    for (AfmFileExif exif : afmFileExifs) {
                        AfmImageDupAssoc afmImageDupAssoc = new AfmImageDupAssoc();
                        afmImageDupAssoc.setId(snowflakeUtil.nextId());
                        afmImageDupAssoc.setDupNoteId(noteId);
                        afmImageDupAssoc.setAssocExifId(exif.getExifId());
                        afmImageDupAssoc.setSimilarity(AfmConstant.FILE_SAM);
                        list.add(afmImageDupAssoc);
                        afmImageDupAssocs.add(afmImageDupAssoc);
                    }
//                    if(!CollectionUtils.isEmpty(afmImageDupAssocs)){
//                        insertAfmImageDupAssocs(afmImageDupAssocs);
//                    }
                    return afmImageDupAssocs;
                }
            }
//        }
        return null;
    }

    private void insertAfmImageDupAssocs(List<AfmImageDupAssoc> afmImageDupAssocs) {
        if (!CollectionUtils.isEmpty(afmImageDupAssocs)) {
            MybatisBatch<AfmImageDupAssoc> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, afmImageDupAssocs);
            MybatisBatch.Method<AfmImageDupAssoc> method = new MybatisBatch.Method<>(AfmImageDupAssocMapper.class);
            mybatisBatch.execute(method.insert());
        }
    }

    /**
     * 获取指定文件所在集合，一般用于更新和存特征
     */
    private AfmServer getAfmServerByCollectName(String docCode, Long serverId, Integer year,int detType) {
        if (serverId != null) {
            //更新数据
            AfmServer afmServer = afmServerMapper.selectById(serverId);
            return afmServer;
        } else {
            //存特征
            AfmServer afmServers = commonService.getCollectNameBase(docCode, year,detType);
            return afmServers;
        }
    }

    /**
     * 百分比转换
     */
    private String convertToPercentage(double number) {
        // 格式化百分比
        DecimalFormat df = new DecimalFormat("0.00%");
        // 将小数乘以100，以获取百分比形式
        double percentage = number * 100;
        // 使用格式化对象将数字转换为字符串，并进行四舍五入
        return df.format(percentage / 100);
    }
}
