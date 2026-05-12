package com.sunyard.sunafm.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.afm.api.dto.AfmDetImgDetDTO;
import com.sunyard.afm.api.dto.AfmDetUpdateDto;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.sunafm.constant.AfmConstant;
import com.sunyard.sunafm.constant.DetNoteConstants;
import com.sunyard.sunafm.dto.AfmDetNoteImgListDTO;
import com.sunyard.sunafm.dto.AfmDetNoteImgListExelDTO;
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
import com.sunyard.sunafm.vo.AfmDetNoteListVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author P-JWei
 * @date 2024/3/20 14:05:53
 * @title
 * @description 检测记录/图像查重实现类
 */
@Slf4j
@Service
public class RecordDupService {
    @Value("${cnn.baseUrl}")
//    @Value("http://127.0.0.1:9006")
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
     * 使用base64的实时接口
     */
    public Map antiFraudDetNowBase64(AfmDetImgDetDTO vo) {
        AssertUtils.isNull(vo.getBase64(), "文件base64不能为空");
        AssertUtils.isNull(vo.getFileLimit(), "文件限制条数不能为空");
        AssertUtils.isNull(vo.getFileSimilarity(), "相似度指标不能为空");
        AssertUtils.isNull(vo.getFileMd5(), "文件md5不能为空");

        //获取文件base64
        String url = cnnUrl + "/queryFilesSingleNoSaveByBase64";
        Map map = new HashMap();
        map.put("token", vo.getFileToken() == null ? "" : vo.getFileToken());
        map.put("file_limit", vo.getFileLimit());
        map.put("file_similarity", vo.getFileSimilarity());
        map.put("file_base64", vo.getBase64());
        map.put("ex_list", vo.getExList() == null ? new ArrayList<>() : vo.getExList());
        map.put("is_opencv_check", vo.getIsOpencvCheck() == null ? 0 : vo.getIsOpencvCheck());
        map.put("thread_num", vo.getThreadNum() == null ? 3 : vo.getThreadNum());
        queryExprHandle(vo, map, AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE);
        String s = JSON.toJSONString(map);
        String cnnStr = CnnHttpUtil.getHttp(url, s);
        log.info("大模型返回：{}", cnnStr);
        AssertUtils.isNull(cnnStr, "服务器连接失败，请稍后重试");
        JSONObject jsonObject1 = null;
        try {
            jsonObject1 = JSONObject.parseObject(cnnStr);
        } catch (Exception e) {
            AssertUtils.isNull(cnnStr, "服务器连接失败，请稍后重试");
        }
        AssertUtils.isNull(jsonObject1, "服务器连接失败，请稍后重试");
        Boolean succ = jsonObject1.getBoolean("succ");
        if (succ == null) {
            AssertUtils.isTrue(true, cnnStr);
        }
        if (succ) {
            Map<String, Double> map1 = new HashMap();

            List<Map> list = new ArrayList<>();
            List<Double> fzs = new ArrayList<>();
            JSONArray array = jsonObject1.getJSONArray("data");
            if (!CollectionUtils.isEmpty(array)) {
                for (int i = 0; i < array.size(); i++) {
                    JSONObject jsonObject = array.getJSONObject(i);
                    String fileMd5 = jsonObject.getString("file_id");
                    Double similarity = jsonObject.getDouble("similarity");
                    map1.put(fileMd5, similarity);
                }
                map1.put(vo.getFileMd5(), 1.0);
                List<AfmFileExif> afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                        .in(AfmFileExif::getFileMd5, map1.keySet())
                        .orderByDesc(AfmFileExif::getCreateTime));
                Map<String, List<AfmFileExif>> collect = afmFileExifs.stream().collect(Collectors.groupingBy(AfmFileExif::getFileIndex));
                for (String id : collect.keySet()) {
                    AfmFileExif exif2 = collect.get(id).get(0);
                    HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
                    objectObjectHashMap.put("exifId", exif2.getExifId());
                    objectObjectHashMap.put("fileFullPath", exif2.getFileUrl());
                    objectObjectHashMap.put("fileUrl", exif2.getFileUrl());
                    objectObjectHashMap.put("fileIndex", exif2.getFileIndex());
                    objectObjectHashMap.put("fileName", exif2.getFileName());
                    objectObjectHashMap.put("sourceSys", exif2.getSourceSys());
                    objectObjectHashMap.put("businessType", exif2.getBusinessType().split(AfmConstant.SUFF)[1]);
                    objectObjectHashMap.put("businessIndex", exif2.getBusinessIndex());
                    objectObjectHashMap.put("materialType", exif2.getMaterialType().split(AfmConstant.SUFF)[1]);
                    objectObjectHashMap.put("uploadUserCode", exif2.getUploadUserCode());
                    objectObjectHashMap.put("uploadUserName", exif2.getUploadUserName());
                    objectObjectHashMap.put("uploadOrg", exif2.getUploadOrg());
                    objectObjectHashMap.put("fileMd5", exif2.getFileMd5());
                    objectObjectHashMap.put("createTime", exif2.getCreateTime());
                    objectObjectHashMap.put("similarity", convertToPercentage(map1.get(exif2.getFileMd5())));
                    fzs.add(map1.get(exif2.getFileMd5()));
                    list.add(objectObjectHashMap);
                }

            }
            List<AfmFileExif> afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                    .eq(AfmFileExif::getFileMd5, vo.getFileMd5()));
            for(AfmFileExif exif2:afmFileExifs){
                HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
                objectObjectHashMap.put("exifId", exif2.getExifId());
                objectObjectHashMap.put("fileFullPath", exif2.getFileUrl());
                objectObjectHashMap.put("fileUrl", exif2.getFileUrl());
                objectObjectHashMap.put("fileIndex", exif2.getFileIndex());
                objectObjectHashMap.put("fileName", exif2.getFileName());
                objectObjectHashMap.put("sourceSys", exif2.getSourceSys());
                objectObjectHashMap.put("businessType", exif2.getBusinessType().split(AfmConstant.SUFF)[1]);
                objectObjectHashMap.put("businessIndex", exif2.getBusinessIndex());
                objectObjectHashMap.put("materialType", exif2.getMaterialType().split(AfmConstant.SUFF)[1]);
                objectObjectHashMap.put("uploadUserCode", exif2.getUploadUserCode());
                objectObjectHashMap.put("uploadUserName", exif2.getUploadUserName());
                objectObjectHashMap.put("uploadOrg", exif2.getUploadOrg());
                objectObjectHashMap.put("fileMd5", exif2.getFileMd5());
                objectObjectHashMap.put("createTime", exif2.getCreateTime());
                objectObjectHashMap.put("similarity", convertToPercentage(1.0));
                fzs.add(1.0);
                list.add(objectObjectHashMap);
            }

            Collections.sort(list, Comparator.comparing(o -> ((String) o.get("similarity"))));
            Collections.reverse(list);
            Double maxSim = 0.0;
            if (!CollectionUtils.isEmpty(list)) {
                List<Double> collect = fzs.stream().sorted().collect(Collectors.toList());
                Collections.reverse(collect);
                maxSim = collect.get(0);
            }
            Map ret = new HashMap();
            ret.put("isDet", maxSim > vo.getFileSimilarity() ? "疑似重复" : "正常");
            ret.put("similarityList", list);
            Map result = new HashMap();
            result.put("dup", ret);
            return result;
        } else {
            String msg = jsonObject1.getString("msg");
            AssertUtils.isTrue(true, msg);
        }
        return null;
    }


    /**
     * 查询单个文件的相似度
     */
    public void dupByUrl(AfmDetImgDetDTO vo) {
        try {
            if (vo.getFileSimilarity() == null) {
                vo.setFileSimilarity(commonService.getSimpleDefult());
            }
            if (vo.getSourceSys() == null) {
                vo.setSourceSys(AfmConstant.AFM_SOURCESYS);
            }
            //校验参数
            String url = cnnUrl + "/queryFilesSingle";
            //新建文件表数据
            AfmFileExif exifNew = new AfmFileExif();
            BeanUtils.copyProperties(vo, exifNew);
            List<AfmFileExif> afmFileExifList = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                    .eq(AfmFileExif::getFileIndex, vo.getSourceSys() + "_" + vo.getFileIndex())
                    .eq(AfmFileExif::getType,AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE)
                    .orderByDesc(AfmFileExif::getCreateTime));
            AfmFileExif exifOld = null;
            if (!ObjectUtils.isEmpty(afmFileExifList)){
                exifOld = afmFileExifList.get(0);
            }
            //处理入参
            url = handleParamDet(vo, url, exifNew, exifOld);
            Date date = new Date();
            //封装python查重需要参数
            String requestParm = getParamCnn(vo,AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE);
            AfmImageDupNote afmImageDupNote = new AfmImageDupNote();
            BeanUtils.copyProperties(vo, afmImageDupNote);
            long noteId = snowflakeUtil.nextId();
            if (exifOld != null) {
                afmImageDupNote.setExifIdOrMd5(exifOld.getExifId().toString());
            }
            afmImageDupNote.setId(noteId);
            afmImageDupNote.setImgDupTime(new Date());
            afmImageDupNote.setSimilarity(vo.getFileSimilarity());
            afmImageDupNote.setCreateTime(date);
            //发起查重
            String response = CnnHttpUtil.getHttp(url, requestParm);
            log.info("当前python返回值为：{}",response);
            exifNew.setServerId(vo.getServerId());
            //处理返回值
            handleResDet(exifNew, afmImageDupNote, noteId, response,AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE);
        }catch (Exception e){
            log.error( ":查重异常", e);
            throw new SunyardException("查重异常：{}"+e.getMessage());
        }
    }

    public void dupByUrlByText(AfmDetImgDetDTO vo) {
        if (vo.getFileSimilarity() == null) {
            vo.setFileSimilarity(commonService.getSimpleDefult());
        }
        if (vo.getSourceSys() == null) {
            vo.setSourceSys(AfmConstant.AFM_SOURCESYS);
        }
        //校验参数
        String url = cnnUrl + "/saveFeatureAndQueryFilesByText";
        //插入文件表
        AfmFileExif exifNew = new AfmFileExif();
        BeanUtils.copyProperties(vo, exifNew);

        List<AfmFileExif> afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                .eq(AfmFileExif::getFileIndex, vo.getSourceSys() + "_" + vo.getFileIndex())
                .eq(AfmFileExif::getType,AfmConstant.TEXT_ANTI_FRAUD_DET_TYPE)
                .orderByDesc(AfmFileExif::getCreateTime));
        AfmFileExif exifOld = null;
        if (!ObjectUtils.isEmpty(afmFileExifs)){
            exifOld = afmFileExifs.get(0);
        }
        //处理入参
        url = handleParamDetByText(vo, url, exifNew, exifOld);
        Date date = new Date();
        //发起查重
        String requestParam = getParamCnn(vo,AfmConstant.TEXT_ANTI_FRAUD_DET_TYPE);

        AfmImageDupNote afmImageDupNote = new AfmImageDupNote();
        BeanUtils.copyProperties(vo, afmImageDupNote);
        long noteId = snowflakeUtil.nextId();
        if (exifOld != null) {
            afmImageDupNote.setExifIdOrMd5(exifOld.getExifId().toString());
        }
        afmImageDupNote.setId(noteId);
        afmImageDupNote.setImgDupTime(new Date());
        afmImageDupNote.setSimilarity(vo.getFileSimilarity());
        afmImageDupNote.setCreateTime(date);
        String response = CnnHttpUtil.getHttp(url, requestParam);
        log.info("当前文件查重结果：{}", response);
        exifNew.setServerId(vo.getServerId());
        exifNew.setFileText(getSubStringText(vo.getFileText()));
        //处理返回值
        handleResDet(exifNew, afmImageDupNote, noteId, response,AfmConstant.TEXT_ANTI_FRAUD_DET_TYPE);
    }

    private String getSubStringText(String fileText) {
        String subStringText = "";
        if (StringUtils.isBlank(fileText)){
            return null;
        }
        subStringText = fileText.substring(0, Math.min(fileText.length(), 200));
        return subStringText;
    }

    private String getParamCnn(AfmDetImgDetDTO vo,int detType) {
        Map<String, Object> mapCnn = getMapCnn(vo, null,detType);
        return JSON.toJSONString(mapCnn);
    }

    private Map<String, Object> getMapCnn(AfmDetImgDetDTO vo, Map<String, List<AfmFileExif>> AfmFileExifList,int detType) {
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
//        vo.setMaterialTypeCode(materialTypeCode);
        AfmServer afmServers = getAfmServerByCollectName(vo.getMaterialTypeCode(), vo.getServerId(), vo.getYear(),detType);
        map.put("server", JSONObject.toJSONString(afmServers));
        vo.setServerId(afmServers.getId());
        queryExprHandle(vo, map,detType);

        if (StringUtils.isNotBlank(vo.getFileExif())) {
            //设置元数据，需要用key，多组value来表示
            //根据文件md5,查询数据库获取所有的文件数据
            List<AfmFileExif> afmFileExifs = new ArrayList<>();
            if (AfmFileExifList != null) {
                afmFileExifs = AfmFileExifList.get(vo.getFileMd5());
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
        //添加文本查重内容
        map.put("file_text",vo.getFileText());
        map.put("query_text_num",AfmConstant.FILE_NUM_MILVUS_DEFULT);
        return map;
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


    private void queryExprHandle(AfmDetImgDetDTO vo, Map map, int detType) {
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
        List<AfmServer> collectNameBaseQuery = getCollectNameBaseQuery(docCodes, yearList,detType);
        map.put("query_server", JSONObject.toJSONString(collectNameBaseQuery));


    }


    /**
     * 校验参数
     */
    public void checkParam(AfmDetImgDetDTO vo) {
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
        AssertUtils.isNull(vo.getYear(), "文件所属年份不能为空");
        vo.setBusinessType(vo.getBusinessTypeCode() + AfmConstant.SUFF + vo.getBusinessTypeName());
        vo.setMaterialType(vo.getMaterialTypeCode() + AfmConstant.SUFF + vo.getMaterialTypeName());
    }


    /**
     * 获取查重结果
     */
    public Map antiFraudDetRes(AfmDetImgDetDTO dto,Integer dataType) {
        Map returnMap = new HashMap();
        returnMap.put("similarityList", null);
        AfmFileExif exif1 = null;
        if (dto.getExifId() != null) {
            exif1 = afmFileExifMapper.selectById(dto.getExifId());
        } else {
            List<AfmFileExif> afmImageDupNotes = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                    .eq(AfmFileExif::getFileIndex, dto.getSourceSys() + "_" + dto.getFileIndex())
                    .eq(AfmFileExif::getType, dataType)
                    .orderByDesc(AfmFileExif::getCreateTime));
            if (CollectionUtils.isEmpty(afmImageDupNotes)) {
                return returnMap;
            } else {
                exif1 = afmImageDupNotes.get(0);
            }
        }

        List<AfmImageDupNote> afmImageDupNotes1 = afmImageDupNoteMapper.selectList(new LambdaQueryWrapper<AfmImageDupNote>()
                .eq(dto.getNoteId() != null, AfmImageDupNote::getId, dto.getNoteId())
                .eq(AfmImageDupNote::getExifIdOrMd5, exif1.getExifId()).orderByDesc(AfmImageDupNote::getCreateTime));
        if (!CollectionUtils.isEmpty(afmImageDupNotes1)) {
            AfmImageDupNote afmImageDupNote = afmImageDupNotes1.get(0);
            returnMap.put("afmImageDupNote", afmImageDupNote);
            returnMap.put("isDet", "正常");
            List<AfmDetOnlineImgDetDTO> afmFileExifList = afmFileExifMapper.queryFileByNoteId(afmImageDupNote.getId(),
                    afmImageDupNote.getSimilarity(), dto.getSourceSys() + "_" +dto.getFileIndex(), dto.getExifId(),
                    AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE);
            if (!CollectionUtils.isEmpty(afmFileExifList)) {
                ArrayList<Map> similarityList = new ArrayList<>();
                Map<String, String> afmSource = commonService.getAfmSource();
                for (AfmDetOnlineImgDetDTO exif : afmFileExifList) {
                    HashMap<String,Object> exifMap = JSONObject.parseObject(exif.getFileExif(), HashMap.class);
                    if (exifMap != null) {
                        //赋值
                        exifToMap(exifMap, afmSource,exif,dto);
                        similarityList.add(exifMap);
                    }
                }
                Double similarity = Double.valueOf(afmFileExifList.get(0).getSimilarity());
                returnMap.put("isDet", similarity > afmImageDupNote.getSimilarity() ? "疑似重复" : "正常");
                returnMap.put("similarityList", similarityList);
            }
        }
        return returnMap;
    }

    private void exifToMap(HashMap<String, Object> exifMap, Map<String, String> afmSource, AfmDetOnlineImgDetDTO exif, AfmDetImgDetDTO dto){
        exifMap.put("exifId", exif.getExifId());
        exifMap.put("fileFullPath", exif.getFileUrl());
        exifMap.put("fileUrl", exif.getFileUrl());
        String fileIndex = exif.getFileIndex();
        if(fileIndex.startsWith(exif.getSourceSys()+"_")){
            fileIndex = fileIndex.split("_")[1];
        }
        exifMap.put("fileIndex", fileIndex);
        exifMap.put("fileName", exif.getFileName());
        exifMap.put("sourceSys", afmSource != null ? afmSource.get(exif.getSourceSys()) : dto.getSourceSys());
        exifMap.put("businessType", exif.getBusinessType() != null ? exif.getBusinessType().split(AfmConstant.SUFF)[1] : exif.getBusinessType());
        exifMap.put("businessIndex", exif.getBusinessIndex());
        exifMap.put("materialType", exif.getMaterialType() != null ? exif.getMaterialType().split(AfmConstant.SUFF)[1] : exif.getMaterialType());
        exifMap.put("uploadUserCode", exif.getUploadUserCode());
        exifMap.put("uploadUserName", exif.getUploadUserName());
        exifMap.put("uploadOrg", exif.getUploadOrg());
        exifMap.put("fileMd5", exif.getFileMd5());
        exifMap.put("createTime", exif.getCreateTime());
        exifMap.put("similarity", convertToPercentage(Double.parseDouble(exif.getSimilarity())));
        exifMap.put("fileText",exif.getFileText());
    }


    /**
     * 批量存特征
     */
    public List<AfmDetImgDetDTO> saveFeatureList(List<MultipartFile> fileList1, List<AfmDetImgDetDTO> afmDetImgDetDTOs,int detType) {
        AssertUtils.isNull(fileList1, "文件不能为空");
        AssertUtils.isTrue(fileList1.size() != afmDetImgDetDTOs.size(), "参数不匹配");
        //处理参数
        for (int i = 0; i < fileList1.size(); i++) {
            afmDetImgDetDTOs.get(i).setFile(fileList1.get(i));
        }

        List<String> collect = afmDetImgDetDTOs.stream().map(s -> {
            s.setFileIndex(s.getSourceSys() + "_" + s.getFileIndex());
            return s.getFileIndex();
        }).collect(Collectors.toList());

        String url = cnnUrl + "/saveFeatureList";
        List<AfmFileExif> afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                .in(AfmFileExif::getFileIndex, collect));

        if (!CollectionUtils.isEmpty(afmFileExifs)) {
            List<String> collect3 = afmFileExifs.stream().map(AfmFileExif::getFileIndex).collect(Collectors.toList());
            Map<String, List<AfmFileExif>> collect5 = afmFileExifs.stream().collect(Collectors.groupingBy(AfmFileExif::getFileIndex));
            for (AfmDetImgDetDTO dto : afmDetImgDetDTOs) {
                if (!collect3.contains(dto.getFileIndex())) {
                    continue;
                }
                List<AfmFileExif> afmFileExifs1 = collect5.get(dto.getFileIndex());
                dto.setServerId(afmFileExifs1.get(0).getServerId());
                dto.setExifId(afmFileExifs1.get(0).getExifId());
            }
        }
        List<String> collect6 = afmDetImgDetDTOs.stream().map(s -> s.getFileMd5()).collect(Collectors.toList());
        List<AfmFileExif> afmFileExifs1 = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>().in(AfmFileExif::getFileMd5, collect6));
        Map<String, List<AfmFileExif>> collect3 = afmFileExifs1.stream().collect(Collectors.groupingBy(AfmFileExif::getFileMd5));
        Map<Long, List<Map<String, Object>>> param = new HashMap<>();
        Map<Long, List<MultipartFile>> file = new HashMap<>();

        handleSynServer(afmDetImgDetDTOs, collect3, param, file,detType);
        Map<String, List<AfmDetImgDetDTO>> collect2 = afmDetImgDetDTOs.stream().collect(Collectors.groupingBy(AfmDetImgDetDTO::getFileIndex));

        Map<Long, List<AfmDetImgDetDTO>> collect4 = afmDetImgDetDTOs.stream().collect(Collectors.groupingBy(AfmDetImgDetDTO::getServerId));
        List<AfmServer> afmServers = afmServerMapper.selectList(new LambdaQueryWrapper<AfmServer>().in(AfmServer::getId, collect4.keySet()));
        List<AfmDetImgDetDTO> collect1 = new ArrayList<>();
        for (AfmServer server : afmServers) {
            List<AfmDetImgDetDTO> dtos = collect4.get(server.getId());
            List<Map<String, Object>> maps = param.get(server.getId());
            List<MultipartFile> multipartFiles = file.get(server.getId());
            if (dtos.size() != maps.size() || maps.size() != multipartFiles.size()) {
                log.error("参数无法匹配");
                continue;
            }

            //批量发起存特征接口
            String s = CnnHttpUtil.saveFeature(url, maps, multipartFiles, server);
            CnnHttpUtil.CnnRetHandle cnnRetHandle = CnnHttpUtil.getCnnRetHandle(s);
            JSONObject jsonObject1 = cnnRetHandle.jsonObject1;
            Integer num = jsonObject1.getJSONObject("data").getInteger("upsert_count");

            if (num == maps.size()) {
                //全成功
                saveOrUpdateCnn(dtos, collect2);
            } else {
                collect1.addAll(dtos);
            }

        }

        return collect1;
    }


    private synchronized void handleSynServer(List<AfmDetImgDetDTO> afmDetImgDetDTOs, Map<String, List<AfmFileExif>> collect3, Map<Long, List<Map<String, Object>>> param, Map<Long, List<MultipartFile>> file, int detType) {
        List<AfmServer> afmServersAll = afmServerMapper.selectList(new LambdaQueryWrapper<AfmServer>().eq(AfmServer::getStatus, AfmConstant.YES));
        //解决高并发的问题
        List<AfmServer> afmServers1 = Collections.synchronizedList(afmServersAll);
        for (AfmDetImgDetDTO vo : afmDetImgDetDTOs) {
            if (StringUtils.isEmpty(vo.getFileExif())) {
                Map map = new HashMap();
                map.put("sourceSys", vo.getSourceSys());
                map.put("materialTypeCode", vo.getMaterialTypeCode());
                map.put("materialTypeName", vo.getMaterialTypeName());
                map.put("businessTypeCode", vo.getBusinessTypeCode());
                map.put("businessTypeName", vo.getBusinessTypeName());
                map.put("year", vo.getYear());
                map.put("fileName", vo.getFileName());
                map.put("format", Md5Utils.getFileExtension(vo.getFileName()));
                String s = JSON.toJSONString(map);
                vo.setFileExif(s);
            } else {
                JSONObject map = JSONObject.parseObject(vo.getFileExif());
                map.put("year", vo.getYear());
                map.put("fileName", vo.getFileName());
                map.put("format", Md5Utils.getFileExtension(vo.getFileName()));
                map.put("materialTypeCode", vo.getMaterialTypeCode());
                map.put("materialTypeName", vo.getMaterialTypeName());
                map.put("businessTypeCode", vo.getBusinessTypeCode());
                map.put("businessTypeName", vo.getBusinessTypeName());
                String s = JSON.toJSONString(map);
                vo.setFileExif(s);
            }

            Map<String, Object> mapCnn = null;
            mapCnn = getMapCnn(vo, collect3,detType);

            List<Map<String, Object>> maps = param.get(vo.getServerId());
            if (CollectionUtils.isEmpty(maps)) {
                ArrayList<Map<String, Object>> objects = new ArrayList<>();
                objects.add(mapCnn);
                param.put(vo.getServerId(), objects);
            } else {
                maps.add(mapCnn);
            }

            List<MultipartFile> multipartFiles = file.get(vo.getServerId());
            if (CollectionUtils.isEmpty(multipartFiles)) {
                ArrayList<MultipartFile> objects = new ArrayList<>();
                objects.add(vo.getFile());
                file.put(vo.getServerId(), objects);
            } else {
                multipartFiles.add(vo.getFile());
            }
        }
    }

    /**
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateCnn(List<AfmDetImgDetDTO> collect1, Map<String, List<AfmDetImgDetDTO>> collect3) {
        List<AfmDetImgDetDTO> collect = collect1.stream().filter(s -> s.getExifId() != null).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(collect)) {
            //新增或修改
            List<AfmDetImgDetDTO> newobj = collect1.stream().filter(l -> l.getExifId() == null).collect(Collectors.toList());
            saveNewData(newobj);
            //修改部分
            for (AfmDetImgDetDTO exif : collect) {
                List<AfmDetImgDetDTO> dtos = collect3.get(exif.getFileIndex());
                if (CollectionUtils.isEmpty(dtos)) {
                    continue;
                }
                AfmDetImgDetDTO afmDetImgDetDTO = dtos.get(0);
                afmFileExifMapper.update(null, new LambdaUpdateWrapper<AfmFileExif>()
                        .set(AfmFileExif::getMaterialType, afmDetImgDetDTO.getMaterialType())
                        .set(AfmFileExif::getFileUrl, afmDetImgDetDTO.getFileUrl())
                        .set(AfmFileExif::getFileName, afmDetImgDetDTO.getFileName())
                        .set(AfmFileExif::getFileExif, afmDetImgDetDTO.getFileExif())
                        .set(AfmFileExif::getBusinessType, afmDetImgDetDTO.getBusinessType())
                        .set(AfmFileExif::getBusinessIndex, afmDetImgDetDTO.getBusinessIndex())
                        .set(AfmFileExif::getUploadOrg, afmDetImgDetDTO.getUploadOrg())
                        .set(AfmFileExif::getUploadUserCode, afmDetImgDetDTO.getUploadUserCode())
                        .set(AfmFileExif::getUploadUserName, afmDetImgDetDTO.getUploadUserName())
                        .set(AfmFileExif::getIsVector, AfmConstant.YES)
                        .eq(AfmFileExif::getExifId, exif.getExifId()));
            }
        } else {
            //全新增
            saveNewData(collect1);
        }
    }

    private void saveNewData(List<AfmDetImgDetDTO> afmDetImgDetDTOs) {
        List<AfmFileExif> dtos = new ArrayList<>();
        for (AfmDetImgDetDTO m : afmDetImgDetDTOs) {
            //新增文件表数据
            AfmFileExif afmFileExif = new AfmFileExif();
            BeanUtils.copyProperties(m, afmFileExif);
            afmFileExif.setFileIndex(afmFileExif.getFileIndex());
            afmFileExif.setBusinessType(m.getBusinessTypeCode() + AfmConstant.SUFF + m.getBusinessTypeName());
            afmFileExif.setMaterialType(m.getMaterialTypeCode() + AfmConstant.SUFF + m.getMaterialTypeName());
            afmFileExif.setExifId(snowflakeUtil.nextId());
            afmFileExif.setIsVector(AfmConstant.YES);
            afmFileExif.setServerId(m.getServerId());
            afmFileExif.setCreateTime(new Date());
            dtos.add(afmFileExif);
        }
        log.info("参数{}",dtos);
        if (!CollectionUtils.isEmpty(dtos)){
            MybatisBatch<AfmFileExif> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, dtos);
            MybatisBatch.Method<AfmFileExif> method = new MybatisBatch.Method<>(AfmFileExifMapper.class);
            mybatisBatch.execute(method.insert());
        }
    }

    /**
     * 保存特征
     */
    public void saveFeature(AfmDetImgDetDTO afmDetImgDetDTO) {
        String url = cnnUrl + "/saveFeatureSingle";
        //同一个资料节点，同一年度下的同样的md5文件只存在一份
        //发起查重
        AfmFileExif afmFileExifs = afmFileExifMapper.selectOne(new LambdaQueryWrapper<AfmFileExif>()
                .eq(AfmFileExif::getFileIndex, afmDetImgDetDTO.getSourceSys() + "_" + afmDetImgDetDTO.getFileIndex()));
        if (afmFileExifs != null) {
            if (AfmConstant.YES.equals(afmFileExifs.getIsVector())) {
                url = cnnUrl + "/updateData";
            }
            afmDetImgDetDTO.setServerId(afmFileExifs.getServerId());
        }

        AfmDetImgDetDTO afmDetImgDetDTO1 = saveHttp(afmDetImgDetDTO, url,AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE);

        if (afmFileExifs != null) {
            afmFileExifMapper.update(null, new LambdaUpdateWrapper<AfmFileExif>()
                    .set(AfmFileExif::getMaterialType, afmDetImgDetDTO.getMaterialType())
                    .set(AfmFileExif::getFileUrl, afmDetImgDetDTO.getFileUrl())
                    .set(AfmFileExif::getFileName, afmDetImgDetDTO.getFileName())
                    .set(AfmFileExif::getFileExif, afmDetImgDetDTO.getFileExif())
                    .set(AfmFileExif::getBusinessType, afmDetImgDetDTO.getBusinessType())
                    .set(AfmFileExif::getBusinessIndex, afmDetImgDetDTO.getBusinessIndex())
                    .set(AfmFileExif::getUploadOrg, afmDetImgDetDTO.getUploadOrg())
                    .set(AfmFileExif::getUploadUserCode, afmDetImgDetDTO.getUploadUserCode())
                    .set(AfmFileExif::getUploadUserName, afmDetImgDetDTO.getUploadUserName())
                    .set(AfmFileExif::getIsVector, AfmConstant.YES)
                    .eq(AfmFileExif::getExifId, afmFileExifs.getExifId()));
        } else {
            //新增文件表数据
            AfmFileExif afmFileExif = new AfmFileExif();
            BeanUtils.copyProperties(afmDetImgDetDTO, afmFileExif);
            afmFileExif.setExifId(snowflakeUtil.nextId());
            afmFileExif.setIsVector(AfmConstant.YES);
            afmFileExif.setServerId(afmDetImgDetDTO1.getServerId());
            afmFileExif.setFileIndex(afmDetImgDetDTO.getSourceSys() + "_" + afmDetImgDetDTO.getFileIndex());
            afmFileExif.setType(AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE);
            afmFileExifMapper.insert(afmFileExif);
        }

    }


    /**
     * 获取列表
     */
    public PageInfo<AfmDetNoteImgListDTO> imgDupList(AfmDetNoteListVO afmDetNoteListVO, PageForm pageForm) {
        LambdaQueryWrapper<AfmImageDupNote> eq = new LambdaQueryWrapper<AfmImageDupNote>()
                .ne(AfmImageDupNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                .like(StringUtils.isNotBlank(afmDetNoteListVO.getBusinessIndex()), AfmImageDupNote::getBusinessIndex, afmDetNoteListVO.getBusinessIndex())
                .between(!ObjectUtils.isEmpty(afmDetNoteListVO.getToDetTime()) && !ObjectUtils.isEmpty(afmDetNoteListVO.getDoDetTime())
                        , AfmImageDupNote::getCreateTime, afmDetNoteListVO.getToDetTime(), afmDetNoteListVO.getDoDetTime())
                .like(StringUtils.isNotBlank(afmDetNoteListVO.getMaterialType()), AfmImageDupNote::getMaterialType, afmDetNoteListVO.getMaterialType() + AfmConstant.SUFF)
                .like(StringUtils.isNotBlank(afmDetNoteListVO.getBusinessType()), AfmImageDupNote::getBusinessType, afmDetNoteListVO.getBusinessType() + AfmConstant.SUFF)
                .like(StringUtils.isNotBlank(afmDetNoteListVO.getFileName()), AfmImageDupNote::getFileName, afmDetNoteListVO.getFileName())
                .eq(StringUtils.isNotBlank(afmDetNoteListVO.getSourceSys()), AfmImageDupNote::getSourceSys, afmDetNoteListVO.getSourceSys())
                .eq(!ObjectUtils.isEmpty(afmDetNoteListVO.getDupType()), AfmImageDupNote::getDupType, afmDetNoteListVO.getDupType())
                .eq(null != afmDetNoteListVO.getDetResult() && AfmConstant.NO.equals(afmDetNoteListVO.getDetResult()), AfmImageDupNote::getImgDupResult, AfmConstant.NO)
                .ne(null != afmDetNoteListVO.getDetResult() && AfmConstant.YES.equals(afmDetNoteListVO.getDetResult()), AfmImageDupNote::getImgDupResult, AfmConstant.NO)
                .orderByDesc(AfmImageDupNote::getCreateTime);

        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<AfmImageDupNote> afmImageDupNotes = afmImageDupNoteMapper.selectList(eq);
        PageInfo<AfmDetNoteImgListDTO> result = new PageInfo<>();
        ArrayList<AfmDetNoteImgListDTO> objects = new ArrayList<>();
        PageInfo<AfmImageDupNote> afmImagePsNotePageInfo = new PageInfo<>(afmImageDupNotes);
        Map<String, String> afmSource = commonService.getAfmSource();
        afmImageDupNotes.forEach(s1 -> {
            AfmDetNoteImgListDTO s = new AfmDetNoteImgListDTO();
            BeanUtils.copyProperties(s1, s);
            s.setBusinessType(s1.getBusinessType().split(AfmConstant.SUFF)[1]);
            s.setMaterialType(s1.getMaterialType().split(AfmConstant.SUFF)[1]);
            if (s1.getImgDupResult() != null) {
                s.setImgDupResultStr(s1.getImgDupResult() > s1.getSimilarity() ? DetNoteConstants.REPEAT.description() : DetNoteConstants.NORMAL.description());
                s.setIsRepeat(s1.getImgDupResult() > s1.getSimilarity() ? DetNoteConstants.REPEAT.value() : DetNoteConstants.NORMAL.value());
            }
            s.setUploadUser(s1.getUploadUserName());
            s.setSourceSys(afmSource.get(s1.getSourceSys()));
            s.setNoteId(s1.getId());
            objects.add(s);
        });
        result.setList(objects);
        result.setTotal(afmImagePsNotePageInfo.getTotal());
        result.setPageNum(afmImagePsNotePageInfo.getPageNum());
        result.setPageSize(afmImagePsNotePageInfo.getPageSize());
        return result;
    }

    /**
     * 查重条件
     */
    public Map queryChooseConditionsNote() {

        List<AfmFileExif> afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                .isNotNull(AfmFileExif::getBusinessType)
                .select(AfmFileExif::getBusinessType)
                .groupBy(AfmFileExif::getBusinessType));
        List<String> stringList = afmFileExifs.stream()
                .map(AfmFileExif::getBusinessType)
                .distinct()
                .collect(Collectors.toList());
        List<Map> afmSourceLabel = commonService.getAfmSourceLabel(AfmConstant.AFM_SOURCESYS);
        List<AfmFileExif> afmFileExifs1 = afmFileExifMapper.selectList(
                new LambdaQueryWrapper<AfmFileExif>()
                        .isNotNull(AfmFileExif::getMaterialType)
                        .eq(AfmFileExif::getIsDeleted, 0)
                        .select(AfmFileExif::getMaterialType)
                        .groupBy(AfmFileExif::getMaterialType));
        List<String> stringList2 = afmFileExifs1.stream()
                .map(AfmFileExif::getMaterialType)
                .collect(Collectors.toList());
        Map map = new HashMap();
        map.put("appTypeList", getMaps(stringList));
        map.put("sourceList", afmSourceLabel);
        map.put("materialTypeList", getMaps(stringList2));
        return map;
    }

    /**
     * 获取文件详情
     */
    public List<AfmDetOnlineImgDetDTO> noteDetails(AfmDetNoteListVO vo) {
        AssertUtils.isNull(vo.getExifIds(), "参数错误");
        LinkedList linkedList = new LinkedList();
        Map<String, String> afmSource = commonService.getAfmSource();
        List<Long> exifId = vo.getExifIds();
        List<AfmFileExif> exifs = afmFileExifMapper.selectBatchIds(exifId);
        if (!CollectionUtils.isEmpty(vo.getNoteIds())) {
            List<AfmImageDupNote> list = afmImageDupNoteMapper.selectBatchIds(vo.getNoteIds());
            Map<Long, List<AfmFileExif>> collect1 = exifs.stream().collect(Collectors.groupingBy(AfmFileExif::getExifId));
            for (AfmImageDupNote note : list) {
                List<AfmFileExif> afmFileExifs = collect1.get(Long.parseLong(note.getExifIdOrMd5()));
                if (!CollectionUtils.isEmpty(afmFileExifs)) {
                    AfmFileExif exif = afmFileExifs.get(0);
                    AfmDetOnlineImgDetDTO dto = new AfmDetOnlineImgDetDTO();
                    BeanUtils.copyProperties(exif, dto);
                    dto.setBusinessType(exif.getBusinessType().split(AfmConstant.SUFF)[1]);
                    dto.setMaterialType(exif.getMaterialType().split(AfmConstant.SUFF)[1]);
                    if (StringUtils.isNotBlank(dto.getFileExif())) {
                        JSONObject jsonObject = JSONObject.parseObject(dto.getFileExif());
                        if (jsonObject.getLong("fileSize") != null) {
                            dto.setFileSize((jsonObject.getLong("fileSize") / 1000) + "KB");
                        }
                        if (jsonObject.getString("format") != null) {
                            dto.setFormat(jsonObject.getString("format"));
                        }
                        HashMap map2 = JSONObject.parseObject(exif.getFileExif(), HashMap.class);
                        dto.setFileExifMap(map2);
                        dto.setFileFullPath(exif.getFileUrl());
                        dto.setExifId(exif.getExifId());
                        dto.setSourceSys(afmSource.get(exif.getSourceSys()));
                        dto.setNoteId(note.getId());
                        dto.setId(dto.getNoteId());
                    }
                    linkedList.add(dto);
                }
            }
        } else {
            for (AfmFileExif exif : exifs) {
                AfmDetOnlineImgDetDTO dto = new AfmDetOnlineImgDetDTO();
                BeanUtils.copyProperties(exif, dto);
                dto.setBusinessType(exif.getBusinessType().split(AfmConstant.SUFF)[1]);
                dto.setMaterialType(exif.getMaterialType().split(AfmConstant.SUFF)[1]);
                if (StringUtils.isNotBlank(dto.getFileExif())) {
                    JSONObject jsonObject = JSONObject.parseObject(dto.getFileExif());
                    if (jsonObject.getLong("fileSize") != null) {
                        dto.setFileSize((jsonObject.getLong("fileSize") / 1000) + "KB");
                    }
                    if (jsonObject.getString("format") != null) {
                        dto.setFormat(jsonObject.getString("format"));
                    }
                    HashMap map2 = JSONObject.parseObject(exif.getFileExif(), HashMap.class);
                    dto.setFileExifMap(map2);
                    dto.setFileFullPath(exif.getFileUrl());
                    dto.setExifId(exif.getExifId());
                    dto.setSourceSys(afmSource.get(exif.getSourceSys()));
                    dto.setId(dto.getExifId());
                }
                linkedList.add(dto);
            }
        }

        return linkedList;
    }

    /**
     * 导出
     */
    public void exportList(HttpServletResponse response, AfmDetNoteListVO afmDetNoteListVO) {
        List<AfmImageDupNote> afmImageDupNotes = new ArrayList<>();
        if (!CollectionUtils.isEmpty(afmDetNoteListVO.getNoteIds())) {
            afmImageDupNotes = afmImageDupNoteMapper.selectBatchIds(afmDetNoteListVO.getNoteIds());
        } else {
            //获取业务日志数据
            LambdaQueryWrapper<AfmImageDupNote> eq = new LambdaQueryWrapper<AfmImageDupNote>()
                    .ne(AfmImageDupNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                    .eq(StringUtils.isNotBlank(afmDetNoteListVO.getBusinessIndex()), AfmImageDupNote::getBusinessIndex, afmDetNoteListVO.getBusinessIndex())
                    .between(!ObjectUtils.isEmpty(afmDetNoteListVO.getToDetTime()) && !ObjectUtils.isEmpty(afmDetNoteListVO.getDoDetTime())
                            , AfmImageDupNote::getCreateTime, afmDetNoteListVO.getToDetTime(), afmDetNoteListVO.getDoDetTime())
                    .likeRight(StringUtils.isNotBlank(afmDetNoteListVO.getMaterialType()), AfmImageDupNote::getMaterialType, afmDetNoteListVO.getMaterialType() + AfmConstant.SUFF)
                    .likeRight(StringUtils.isNotBlank(afmDetNoteListVO.getBusinessType()), AfmImageDupNote::getBusinessType, afmDetNoteListVO.getBusinessType() + AfmConstant.SUFF)
                    .eq(StringUtils.isNotBlank(afmDetNoteListVO.getSourceSys()), AfmImageDupNote::getSourceSys, afmDetNoteListVO.getSourceSys());

            if (afmDetNoteListVO.getDetResult() != null) {
                if (afmDetNoteListVO.getDetResult().equals(AfmConstant.YES)) {
                    eq.ge(AfmImageDupNote::getImgDupResult, commonService.getSimpleDefult());
                } else {
                    eq.le(AfmImageDupNote::getImgDupResult, commonService.getSimpleDefult());
                }
            }
            eq.orderByDesc(AfmImageDupNote::getCreateTime);
            afmImageDupNotes = afmImageDupNoteMapper.selectList(eq);
        }
        List<AfmDetNoteImgListExelDTO> list = new ArrayList<>();
        for (AfmImageDupNote dto : afmImageDupNotes) {
            AfmDetNoteImgListExelDTO afmDetNoteImgListDTO = new AfmDetNoteImgListExelDTO();
            BeanUtils.copyProperties(dto, afmDetNoteImgListDTO);
            afmDetNoteImgListDTO.setBusinessType(dto.getBusinessType().split(AfmConstant.SUFF)[1]);
            afmDetNoteImgListDTO.setMaterialType(dto.getMaterialType().split(AfmConstant.SUFF)[1]);
            afmDetNoteImgListDTO.setImgDupResultStr(dto.getImgDupResult() > commonService.getSimpleDefult() ? DetNoteConstants.REPEAT.description() : DetNoteConstants.NORMAL.description());
            list.add(afmDetNoteImgListDTO);
        }
        try {
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("查重记录表.xlsx", "UTF-8"));
            ServletOutputStream outputStream = response.getOutputStream();
            EasyExcel.write(outputStream, AfmDetNoteImgListExelDTO.class).sheet("查重记录").doWrite(list);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("导出失败", e);
        }

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

    private AfmDetImgDetDTO saveHttp(AfmDetImgDetDTO afmDetImgDetDTO, String url,int detType) {
        AfmFileExif fileExif = new AfmFileExif();
        if (AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE.equals(detType)){
            //影像查重
            handleParamDet(afmDetImgDetDTO, null, fileExif, null);
        }else if (AfmConstant.TEXT_ANTI_FRAUD_DET_TYPE.equals(detType)){
            //文本查重
            handleParamDetByText(afmDetImgDetDTO, null, fileExif, null);
        }
        afmDetImgDetDTO.setFileExif(fileExif.getFileExif());
        String s = getParamCnn(afmDetImgDetDTO,detType);
        log.info("当前请求参数为：{}",s);
        String s1 = CnnHttpUtil.getHttp(url, s);
        JSONObject jsonObject1 = JSONObject.parseObject(s1);
        Boolean succ = jsonObject1.getBoolean("succ");
        AssertUtils.isTrue(!succ, "找不到文件");
        return afmDetImgDetDTO;
    }

    /**
     * 处理入参
     */
    private String handleParamDet(AfmDetImgDetDTO vo, String url, AfmFileExif exif, AfmFileExif exif1) {
        if (exif1 != null) {
            BeanUtils.copyProperties(exif1, vo);
        }

        fileExifHandle(vo, exif, exif1);
        if (exif1 != null) {
            exif.setExifId(exif1.getExifId());
            String substring = vo.getInvoiceType().substring(0, 2);
            if (AfmConstant.DO_DET_SAVE.equals(substring)) {
                url = cnnUrl + "/updateData";
            } else if (AfmConstant.DO_DET_SAVE_AND_NOTE.equals(substring)) {
                url = cnnUrl + "/queryFilesSingleNoSave";
            }
        } else {
            exif.setIsVector(AfmConstant.YES);
            exif.setFileIndex(vo.getFileIndex());
        }
        return url;
    }

    private String handleParamDetByText(AfmDetImgDetDTO vo, String url, AfmFileExif exif, AfmFileExif exif1) {
        if (exif1 != null) {
            BeanUtils.copyProperties(exif1, vo);
        }

        fileExifHandle(vo, exif, exif1);
        if (exif1 != null) {
            exif.setExifId(exif1.getExifId());
            String substring = vo.getInvoiceType().substring(0, 2);
            if (AfmConstant.DO_DET_SAVE.equals(substring)) {
                url = cnnUrl + "/saveFeatureSingleByText";
            } else if (AfmConstant.DO_DET_SAVE_AND_NOTE.equals(substring)) {
                url = cnnUrl + "/saveFeatureAndQueryFilesByText";
            }
        } else {
            exif.setIsVector(AfmConstant.YES);
            exif.setFileIndex(vo.getFileIndex());
        }
        return url;
    }

    private void fileExifHandle(AfmDetImgDetDTO vo, AfmFileExif exif, AfmFileExif exif1) {
        if (StringUtils.isEmpty(vo.getFileExif())) {
            Map map = new HashMap();
            map.put("sourceSys", vo.getSourceSys());
            map.put("materialTypeCode", vo.getMaterialTypeCode());
            map.put("materialTypeName", vo.getMaterialTypeName());
            map.put("businessTypeCode", vo.getBusinessTypeCode());
            map.put("businessTypeName", vo.getBusinessTypeName());
            map.put("year", vo.getYear());
            map.put("fileName", vo.getFileName());
            map.put("format", Md5Utils.getFileExtension(vo.getFileName()));
            String s = JSON.toJSONString(map);
            exif.setFileExif(s);
        } else {
            JSONObject map = JSONObject.parseObject(vo.getFileExif());
            //当文件存在不允许修改来源和文件的md5
            if (exif1 == null) {
                map.put("sourceSys", vo.getSourceSys());
                map.put("fileMd5", vo.getFileMd5());
            }
            map.put("year", vo.getYear());
            map.put("fileName", vo.getFileName());
            map.put("format", Md5Utils.getFileExtension(vo.getFileName()));
            map.put("materialTypeCode", vo.getMaterialTypeCode());
            map.put("materialTypeName", vo.getMaterialTypeName());
            map.put("businessTypeCode", vo.getBusinessTypeCode());
            map.put("businessTypeName", vo.getBusinessTypeName());
            String s = JSON.toJSONString(map);
            exif.setFileExif(s);
        }
    }


    private List<AfmServer> getCollectNameBaseQuery(List<String> materialTypeCode, List<Integer> year, int detType) {
        //当这个文件指定的集合不存在，则证明是存特征逻辑，查询出现有可写的集合并返回集合名称
        List<AfmServer> afmServers = afmServerMapper.selectList(new LambdaQueryWrapper<AfmServer>()
                    .isNotNull(AfmServer::getCollectionName)
                    .in(!CollectionUtils.isEmpty(materialTypeCode), AfmServer::getDocCode, materialTypeCode)
                    .in(!CollectionUtils.isEmpty(year), AfmServer::getYear, year)
                    .eq(AfmServer::getServerType,detType)
                    .eq(AfmServer::getStatus, AfmConstant.YES));
        AssertUtils.isNull(afmServers, "暂无可读的向量服务，请检查配置");
        return afmServers;
    }


    /**
     * 处理返回值
     */
    @Lock4j(keys = "#exif.fileMd5")
    public void handleResDet(AfmFileExif exif, AfmImageDupNote afmImageDupNote, long noteId, String s1,Integer type) {
        JSONObject responseJsonObject = JSONObject.parseObject(s1);
        if (responseJsonObject != null) {
            Boolean responseFlag = responseJsonObject.getBoolean("succ");
            if (responseFlag) {
                JSONArray array = responseJsonObject.getJSONArray("data");
                //设置afm_file_exif文件类型  0：影像查重  1：文本查重
                exif.setType(type);
                //保存文件
                AfmFileExif afmFileExif = saveFileExif(exif);
                //获取文件关联表数据
                List<AfmImageDupAssoc> afmImageDupAssocs = saveAssoc(afmImageDupNote, noteId, array, exif.getFileMd5(), exif.getExifId(),type);
                afmImageDupNote.setExifIdOrMd5(exif.getExifId().toString());
                afmImageDupNote.setUploadOrgName(exif.getUploadUserName());
                afmImageDupNote.setUploadUserCode(exif.getUploadUserCode());
                afmImageDupNote.setUploadOrg(exif.getUploadOrg());
                //todo 数据库新增字段
                afmImageDupNote.setDupType(type);
                //批量插入查重数据
                handleData(afmImageDupNote, afmFileExif, afmImageDupAssocs);
            } else {
                String msg = responseJsonObject.getString("msg");
                AssertUtils.isTrue(true, msg);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleData(AfmImageDupNote afmImageDupNote, AfmFileExif afmFileExif, List<AfmImageDupAssoc> afmImageDupAssocs) {
        if(afmFileExif!=null){
            afmFileExifMapper.insert(afmFileExif);
        }
        if(!CollectionUtils.isEmpty(afmImageDupAssocs)){
            afmImageDupAssocMapper.insertBatch(afmImageDupAssocs);
        }
        //插入查重数据
        afmImageDupNoteMapper.insert(afmImageDupNote);
    }

    /**
     * 删除文件
     */
    @Transactional(rollbackFor = Exception.class)
    public void delFile(AfmDetImgDetDTO dto) {
        AssertUtils.isNull(dto.getSourceSys(),"文件来源不能为空");
        AssertUtils.isNull(dto.getFileIndex(),"文件id不能为空");
        List<AfmFileExif> afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>().eq(AfmFileExif::getFileIndex, dto.getSourceSys() + "_" + dto.getFileIndex()));
        if(!CollectionUtils.isEmpty(afmFileExifs)){
            //1、判断md5是否只有一条数据，如果只有这一条则删除向量数据库中的数据，如果存在多条则不删
            AfmFileExif exif = afmFileExifs.get(0);
            Long aLong = afmFileExifMapper.selectCount(new LambdaQueryWrapper<AfmFileExif>().eq(AfmFileExif::getFileMd5,  exif.getFileMd5()));
            if(aLong==1){
                if(exif.getServerId()!=null){
                    AfmServer afmServer = afmServerMapper.selectById(exif.getServerId());
                    if(afmServer!=null){
                        //掉反欺诈接口
                        String url = cnnUrl + "/delFile";
                        Map map = new HashMap();
                        map.put("server",JSONObject.toJSONString(afmServer));
                        map.put("file_id",exif.getFileMd5());
                        String s = JSONObject.toJSONString(map);
                        String cnnStr = CnnHttpUtil.getHttp(url, s);
                        CnnHttpUtil.getCnnRetHandle(cnnStr);
                    }

                }

            }
            //2、删除其他文件数据查重表，文件相似度表，最后是文件表
            List<AfmImageDupNote> list = afmImageDupNoteMapper.selectList(new LambdaQueryWrapper<AfmImageDupNote>().eq(AfmImageDupNote::getExifIdOrMd5, exif.getExifId()));
            if(!CollectionUtils.isEmpty(list)){
                List<Long> collect = list.stream().map(AfmImageDupNote::getId).collect(Collectors.toList());
                afmImageDupAssocMapper.delete(new LambdaQueryWrapper<AfmImageDupAssoc>().in(AfmImageDupAssoc::getDupNoteId,collect));
                afmImageDupNoteMapper.deleteBatchIds(collect);

            }
            afmFileExifMapper.deleteById(exif.getExifId());

        }

    }


    /**
     * 保存相似度
     */
    private List<AfmImageDupAssoc> saveAssoc(AfmImageDupNote afmImageDupNote, long noteId, JSONArray array, String md5, Long exifId, Integer type) {
        List<AfmImageDupAssoc> list = new ArrayList<>();
        Map<String, Double> map = new HashMap();
        List<AfmImageDupAssoc> dupAssocList = new ArrayList<>();
        //统计相同MD5数据
        List<AfmImageDupAssoc> afmImageDupAssocs = addAssoNoRecord(afmImageDupNote, noteId, md5, exifId, list, type);
        if (!CollectionUtils.isEmpty(afmImageDupAssocs)) {
            dupAssocList.addAll(afmImageDupAssocs);
        }
        //统计python返回值相关数据
        if (AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE.equals(type)){
            //影像查重逻辑，处理python返回值
            imageDupAssoc(array,map,list,noteId,dupAssocList,type);
        }else if (AfmConstant.TEXT_ANTI_FRAUD_DET_TYPE.equals(type)){
            //文本查重逻辑，处理python返回值
            textDupAssoc(array,map,list,noteId,dupAssocList,type,exifId);
        }
        if (!CollectionUtils.isEmpty(list)) {
            List<Double> collect1 = list.stream()
                    .filter(s -> s.getSimilarity() != null)
                    .map(AfmImageDupAssoc::getSimilarity).sorted().collect(Collectors.toList());
            Collections.reverse(collect1);
            afmImageDupNote.setImgDupResult(collect1.get(0));
        }
        return dupAssocList;
    }

    private void textDupAssoc(JSONArray array, Map<String, Double> map, List<AfmImageDupAssoc> list, long noteId, List<AfmImageDupAssoc> dupAssocList, Integer type, Long exifId) {
        if (!CollectionUtils.isEmpty(array)) {
            for (int i = 0; i < array.size(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String fileMd5 = jsonObject.getString("file_id");
                Double similarity = jsonObject.getDouble("rate");
                map.put(fileMd5, similarity);
            }
            List<AfmFileExif> afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                    .in(AfmFileExif::getFileMd5, map.keySet())
                    .eq(AfmFileExif::getType,type)
                    .orderByDesc(AfmFileExif::getCreateTime));
            afmFileExifs = afmFileExifs.stream().filter(s -> !s.getExifId().equals(exifId)).collect(Collectors.toList());
            Map<String, List<AfmFileExif>> collect = afmFileExifs.stream().collect(Collectors.groupingBy(AfmFileExif::getFileIndex));
            List<AfmImageDupAssoc> afmImageDupAssocs = new ArrayList<>();
            Map<Long, List<AfmImageDupAssoc>> objectMap = dupAssocList.stream()
                    .collect(Collectors.groupingBy(AfmImageDupAssoc::getAssocExifId));
            for (String id : collect.keySet()) {
                AfmFileExif exif2 = collect.get(id).get(0);
                if (objectMap.containsKey(exif2.getExifId())) {
                    continue;
                }
                AfmImageDupAssoc afmImageDupAssoc = new AfmImageDupAssoc();
                afmImageDupAssoc.setId(snowflakeUtil.nextId());
                afmImageDupAssoc.setDupNoteId(noteId);
                afmImageDupAssoc.setAssocExifId(exif2.getExifId());
                afmImageDupAssoc.setSimilarity(map.get(exif2.getFileMd5()));
                list.add(afmImageDupAssoc);
                afmImageDupAssocs.add(afmImageDupAssoc);
            }
            if(!CollectionUtils.isEmpty(afmImageDupAssocs)){
                dupAssocList.addAll(afmImageDupAssocs);
            }
        }
    }

    private void imageDupAssoc(JSONArray array, Map<String, Double> map, List<AfmImageDupAssoc> list, long noteId, List<AfmImageDupAssoc> dupAssocList, Integer type){
        if (!CollectionUtils.isEmpty(array)) {
            for (int i = 0; i < array.size(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String fileMd5 = jsonObject.getString("file_id");
                Double similarity = jsonObject.getDouble("similarity");
                map.put(fileMd5, similarity);
            }
            List<AfmFileExif> afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                    .in(AfmFileExif::getFileMd5, map.keySet())
                    .eq(AfmFileExif::getType,type)
                    .orderByDesc(AfmFileExif::getCreateTime));
            //将已经筛选出来的相同MD5的数据的文件id整理出来，后续排除重复插入
            List<Long> assocExifIds = dupAssocList.stream()
                    .map(AfmImageDupAssoc::getAssocExifId)
                    .collect(Collectors.toList());
            Map<String, List<AfmFileExif>> collect = afmFileExifs.stream().collect(Collectors.groupingBy(AfmFileExif::getFileIndex));
            List<AfmImageDupAssoc> afmImageDupAssocs = new ArrayList<>();
            for (String id : collect.keySet()) {
                AfmFileExif exif2 = collect.get(id).get(0);
                //排除相同MD5的数据
                if (assocExifIds.contains(exif2.getExifId())) {
                    continue;
                }
                AfmImageDupAssoc afmImageDupAssoc = new AfmImageDupAssoc();
                afmImageDupAssoc.setId(snowflakeUtil.nextId());
                afmImageDupAssoc.setDupNoteId(noteId);
                afmImageDupAssoc.setAssocExifId(exif2.getExifId());
                afmImageDupAssoc.setSimilarity(map.get(exif2.getFileMd5()));
                list.add(afmImageDupAssoc);
                afmImageDupAssocs.add(afmImageDupAssoc);
            }
            if(!CollectionUtils.isEmpty(afmImageDupAssocs)){
                dupAssocList.addAll(afmImageDupAssocs);
            }
        }
    }

    private List<AfmImageDupAssoc> addAssoNoRecord(AfmImageDupNote afmImageDupNote, long noteId, String md5, Long exifId, List<AfmImageDupAssoc> list, Integer type) {
        //关联的没有，但是这条数据同md5的有可能存在
        List<AfmFileExif> afmFileExifs = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                .eq(AfmFileExif::getFileMd5, md5)
                .eq(AfmFileExif::getType , type));
            if (!CollectionUtils.isEmpty(afmFileExifs)) {
                List<AfmFileExif> collect = afmFileExifs.stream().filter(s -> !s.getExifId().equals(exifId)).collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(collect)){
                    List<AfmImageDupAssoc> afmImageDupAssocs = new ArrayList<>();
                    for (AfmFileExif exif : collect) {
                        AfmImageDupAssoc afmImageDupAssoc = new AfmImageDupAssoc();
                        afmImageDupAssoc.setId(snowflakeUtil.nextId());
                        afmImageDupAssoc.setDupNoteId(noteId);
                        afmImageDupAssoc.setAssocExifId(exif.getExifId());
                        afmImageDupAssoc.setSimilarity(AfmConstant.FILE_SAM);
                        list.add(afmImageDupAssoc);
                        afmImageDupAssocs.add(afmImageDupAssoc);
                    }
                    return afmImageDupAssocs;
                }
            }
        return null;
    }

    private void insertAfmImageDupAssocs(List<AfmImageDupAssoc> afmImageDupAssocs) {
        if (!CollectionUtils.isEmpty(afmImageDupAssocs)) {
            MybatisBatch<AfmImageDupAssoc> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, afmImageDupAssocs);
            MybatisBatch.Method<AfmImageDupAssoc> method = new MybatisBatch.Method<>(AfmImageDupAssocMapper.class);
            mybatisBatch.execute(method.insert());
        }
    }

    private AfmFileExif saveFileExif(AfmFileExif exif) {
        if (exif.getExifId() != null) {
            //存在更新
            afmFileExifMapper.update(null, new LambdaUpdateWrapper<AfmFileExif>()
                    .set(AfmFileExif::getFileUrl, exif.getFileUrl())
                    .set(AfmFileExif::getFileName, exif.getFileName())
                    .set(AfmFileExif::getFileExif, exif.getFileExif())
                    .set(AfmFileExif::getBusinessType, exif.getBusinessType())
                    .set(AfmFileExif::getBusinessIndex, exif.getBusinessIndex())
                    .set(AfmFileExif::getMaterialType, exif.getMaterialType())
                    .eq(AfmFileExif::getExifId, exif.getExifId()));
        } else {
            exif.setExifId(snowflakeUtil.nextId());
            exif.setFileIndex(exif.getSourceSys() + "_" + exif.getFileIndex());
//            afmFileExifMapper.insert(exif);
            return exif;
        }
        return null;
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

    public void saveFeatureByText(AfmDetImgDetDTO afmDetImgDetDTO) {
        //文本查重保存文本信息路径
        String url = cnnUrl + "/saveFeatureSingleByText";
        //同一个资料节点，同一年度下的同样的md5文件只存在一份
        //发起查重
        AfmFileExif afmFileExifs = afmFileExifMapper.selectOne(new LambdaQueryWrapper<AfmFileExif>()
                .eq(AfmFileExif::getFileIndex, afmDetImgDetDTO.getSourceSys() + "_" + afmDetImgDetDTO.getFileIndex()));
        if (afmFileExifs != null) {
            afmDetImgDetDTO.setServerId(afmFileExifs.getServerId());
        }

        AfmDetImgDetDTO afmDetImgDetDTO1 = saveHttp(afmDetImgDetDTO, url,AfmConstant.TEXT_ANTI_FRAUD_DET_TYPE);

        if (afmFileExifs != null) {
            afmFileExifMapper.update(null, new LambdaUpdateWrapper<AfmFileExif>()
                    .set(AfmFileExif::getMaterialType, afmDetImgDetDTO.getMaterialType())
                    .set(AfmFileExif::getFileUrl, afmDetImgDetDTO.getFileUrl())
                    .set(AfmFileExif::getFileName, afmDetImgDetDTO.getFileName())
                    .set(AfmFileExif::getFileExif, afmDetImgDetDTO.getFileExif())
                    .set(AfmFileExif::getBusinessType, afmDetImgDetDTO.getBusinessType())
                    .set(AfmFileExif::getBusinessIndex, afmDetImgDetDTO.getBusinessIndex())
                    .set(AfmFileExif::getUploadOrg, afmDetImgDetDTO.getUploadOrg())
                    .set(AfmFileExif::getUploadUserCode, afmDetImgDetDTO.getUploadUserCode())
                    .set(AfmFileExif::getUploadUserName, afmDetImgDetDTO.getUploadUserName())
                    .set(AfmFileExif::getIsVector, AfmConstant.YES)
                    .eq(AfmFileExif::getExifId, afmFileExifs.getExifId()));
        } else {
            //新增文件表数据
            AfmFileExif afmFileExif = new AfmFileExif();
            BeanUtils.copyProperties(afmDetImgDetDTO, afmFileExif);
            afmFileExif.setExifId(snowflakeUtil.nextId());
            afmFileExif.setIsVector(AfmConstant.YES);
            afmFileExif.setServerId(afmDetImgDetDTO1.getServerId());
            afmFileExif.setFileIndex(afmDetImgDetDTO.getSourceSys() + "_" + afmDetImgDetDTO.getFileIndex());
            afmFileExif.setType(AfmConstant.TEXT_ANTI_FRAUD_DET_TYPE);
            afmFileExifMapper.insert(afmFileExif);
        }

    }

    /**
     *  文本查重：查询重复文件
     * @param dto
     * @return
     */
    public Map antiFraudDetResByText(AfmDetImgDetDTO dto) {
        Map map = new HashMap();
        map.put("similarityList", null);
        AfmFileExif exif1 = null;
        if (dto.getExifId() != null) {
            exif1 = afmFileExifMapper.selectById(dto.getExifId());
        } else {
            List<AfmFileExif> afmImageDupNotes = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                    .eq(AfmFileExif::getFileIndex, dto.getSourceSys() + "_" + dto.getFileIndex())
                    .eq(AfmFileExif::getType, AfmConstant.TEXT_ANTI_FRAUD_DET_TYPE)
                    .orderByDesc(AfmFileExif::getCreateTime));
            if (CollectionUtils.isEmpty(afmImageDupNotes)) {
                return map;
            } else {
                exif1 = afmImageDupNotes.get(0);
            }
        }

        List<AfmImageDupNote> afmImageDupNotes1 = afmImageDupNoteMapper.selectList(new LambdaQueryWrapper<AfmImageDupNote>()
                .eq(dto.getNoteId() != null, AfmImageDupNote::getId, dto.getNoteId())
                .eq(AfmImageDupNote::getExifIdOrMd5, exif1.getExifId())
                .orderByDesc(AfmImageDupNote::getCreateTime));
        if (!CollectionUtils.isEmpty(afmImageDupNotes1)) {
            //如果有，代表查重过，直接返回
            AfmImageDupNote afmImageDupNote = afmImageDupNotes1.get(0);
            map.put("afmImageDupNote", afmImageDupNote);
            map.put("isDet", "正常");
            List<AfmDetOnlineImgDetDTO> afmFileExifs = afmFileExifMapper.queryFileByNoteId(afmImageDupNote.getId(),
                    afmImageDupNote.getSimilarity(), dto.getSourceSys() + "_" +dto.getFileIndex(), dto.getExifId(),
                    AfmConstant.TEXT_ANTI_FRAUD_DET_TYPE);
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
        }else {
            //文本查重路径
            String url = cnnUrl + "/queryFilesSingleByTextNoSave";
            //没查重过，请求python进行查重
            //请求参数
            Map requestMap = getQueryDetTextParam(exif1,dto);
            String queryResult = CnnHttpUtil.getHttp(url, JSON.toJSONString(requestMap));
            log.info("当前文本查重返回结果为：{}",queryResult);


        }
        return map;
    }

    private Map getQueryDetTextParam(AfmFileExif exif1, AfmDetImgDetDTO dto) {
        Map map = new HashMap();
        AfmServer afmServer = afmServerMapper.selectById(exif1.getServerId());
        map.put("server", JSONObject.toJSONString(afmServer));
        //添加文本查重内容
        map.put("file_text",dto.getFileText());
        map.put("query_text_num",AfmConstant.FILE_NUM_MILVUS_DEFULT);
        /**
         * 如果没有指定集合，则需要圈梁查询，如果指定了集合则，查对应集合的服务器即可。
         */
        List<AfmServer> collectNameBaseQuery = getCollectNameBaseQuery(Arrays.asList(dto.getMaterialTypeCode()), Arrays.asList(dto.getYear()), AfmConstant.TEXT_ANTI_FRAUD_DET_TYPE);
        map.put("query_server", JSONObject.toJSONString(collectNameBaseQuery));
        map.put("query_expr","");
        map.put("file_exif","");
        map.put("file_id",exif1.getFileIndex().split("_")[0]);
        return map;
    }

    public void ecmToAfmDataSync(AfmDetUpdateDto dto) {
        //参数校验
        checkPrams(dto);
        List<String> fileIds = dto.getFileIds();
        String businessIndex = dto.getBusinessIndex();
        //数据同步
        if (AfmConstant.ECM_AFM_SYNC_UPDATE.equals(dto.getType())){
            //修改
            afmFileExifMapper.update(null,
                    new LambdaUpdateWrapper<AfmFileExif>()
                            .set(AfmFileExif::getBusinessIndex, businessIndex)
                            .in(AfmFileExif:: getFileIndex, fileIds));
        }else if (AfmConstant.ECM_AFM_SYNC_DELETE.equals(dto.getType())){
            //删除  1.删除文件表文件  2.删除向量数据库
            List<AfmFileExif> fileExifList = afmFileExifMapper.selectList(new LambdaQueryWrapper<AfmFileExif>()
                    .in(AfmFileExif::getFileIndex, fileIds));
            //删除当前文件记录
            afmFileExifMapper.delete(new LambdaUpdateWrapper<AfmFileExif>()
                            .in(AfmFileExif::getFileIndex, fileIds));
            //若删除的文件list里的MD5还存在则不删除向量数据库
            Set<String> md5Set = fileExifList.stream()
                    .map(AfmFileExif::getFileMd5)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (md5Set.isEmpty()) {
                return;
            }
            List<String> existingMd5s = afmFileExifMapper.selectObjs(
                            new LambdaQueryWrapper<AfmFileExif>()
                                    .select(AfmFileExif::getFileMd5)
                                    .in(AfmFileExif::getFileMd5, md5Set)
                    ).stream()
                    .map(obj -> (String) obj)
                    .collect(Collectors.toList());
            Set<String> existingMd5Set = new HashSet<>(existingMd5s);
            md5Set.removeAll(existingMd5Set);
            if (!md5Set.isEmpty()) {
                //同时调用python删除向量数据库的数据
                getRemoveList(md5Set,fileExifList);
                //向量删除路径
                String url = cnnUrl + "/deleteVector";
                //请求参数
                List<Map<String, Object>> requestMap = getDeleteParams(fileExifList,url);
//                String queryResult = CnnHttpUtil.getHttp(url, JSON.toJSONString(requestMap));
            }
        }
    }

    private List<Map<String, Object>> getDeleteParams(List<AfmFileExif> fileExifList,String url) {
        List<Map<String, Object>> fileQueries = new ArrayList<>();
        Map<Long, AfmServer> serverMap = afmServerMapper.selectList(null)
                .stream()
                .collect(Collectors.toMap(
                        AfmServer::getId,
                        Function.identity()
                ));
        for (AfmFileExif afmFileExif : fileExifList) {
            Map map = new HashMap();
            AfmServer afmServer = serverMap.get(afmFileExif.getServerId());
            map.put("server", JSONObject.toJSONString(afmServer));
            //添加文本查重内容
            map.put("file_type",afmFileExif.getType());
            map.put("file_id",afmFileExif.getFileMd5());
//            fileQueries.add(map);
            CnnHttpUtil.getHttp(url, JSON.toJSONString(map));
        }
        return fileQueries;
    }

    private void getRemoveList(Set<String> md5Set, List<AfmFileExif> fileExifList) {
        //md5Set 不存在的md5 需要删除   fileExifList 待删除的文件信息
        for (AfmFileExif afmFileExif : fileExifList) {
            if (!md5Set.contains(afmFileExif.getFileMd5())) {
                fileExifList.remove(afmFileExif);
            }
        }
    }

    private void checkPrams(AfmDetUpdateDto dto) {
        AssertUtils.isTrue(ObjectUtils.isEmpty(dto),"传入同步对象不能为空");
        AssertUtils.isTrue(ObjectUtils.isEmpty(dto.getFileIds()),"传入同步文件id不能为空");
        AssertUtils.isTrue(ObjectUtils.isEmpty(dto.getType()),"传入同步类型不能为空");
    }
}
