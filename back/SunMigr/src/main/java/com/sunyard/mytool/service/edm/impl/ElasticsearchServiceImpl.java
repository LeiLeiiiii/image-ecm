package com.sunyard.mytool.service.edm.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunyard.mytool.constant.MigrateConstant;
import com.sunyard.mytool.dto.DocBsDocumentDTO;
import com.sunyard.mytool.dto.DocFullTextFileDTO;
import com.sunyard.mytool.dto.es.EcmBusiInfoEsDTO;
import com.sunyard.mytool.dto.es.EcmFileInfoEsDTO;
import com.sunyard.mytool.entity.DocBsDocument;
import com.sunyard.mytool.entity.DocUploadRecord;
import com.sunyard.mytool.entity.es.EsEcmBusi;
import com.sunyard.mytool.entity.es.EsEcmFile;
import com.sunyard.mytool.mapper.db.edm.DocUploadRecordMapper;
import com.sunyard.mytool.mapper.es.EsEcmBusiMapper;
import com.sunyard.mytool.mapper.es.EsEcmFileMapper;
import com.sunyard.mytool.service.edm.ElasticsearchService;
import com.sunyard.mytool.until.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;
@Slf4j
@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {
    private final List suffixArr = new ArrayList(Arrays.asList("txt", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "jpg", "png", "jpeg"));
    @Value("${elasticsearch.indexName:document}")
    private String index;
    @Value("${elasticsearch.esTempPath:/home/temp/esTemp}")
    private String tempPath;
    @Resource
    private EdmEsUtils esUtil;
    @Resource
    private DocUploadRecordMapper docUploadRecordMapper;

    //@Value("${spring.application.name:sunmigration}") //todo
    //private String application = "migr-service";
    @Value("${elasticsearch.bizIndex:ecm_busi_dev}")
    private String bizIndex;
    @Value("${elasticsearch.fileIndex:ecm_file_dev}")
    private String fileIndex;
    @Resource
    private EsEcmFileMapper esEcmFileMapper;
    @Resource
    private EsEcmBusiMapper esEcmBusiMapper;
    @Resource
    private EcmEsUtils ecmEsUtils;

    /**
     * es 添加数据。
     */
    @Override
    public void addFullTextPath(DocBsDocumentDTO docBsDocumentDTO) {
        long sTime = System.currentTimeMillis();
        DocBsDocument docBsDocument = docBsDocumentDTO.getDoc();
        AssertUtils.isNull(docBsDocument.getBusId(), "id:文档id不能为空");
        AssertUtils.isNull(docBsDocument.getDocName(), "name:文档名不能为空");
        AssertUtils.isNull(docBsDocument.getDocSuffix(), "suffix:文档后缀不能为空");
        AssertUtils.isNull(docBsDocumentDTO.getDocUrl(), "url:文档路径不能为空");
        String str = docBsDocumentDTO.getDocUrl();
        log.info("docUrl: {}", str);
        String name = docBsDocument.getDocName();
        DocUploadRecord docUploadRecord = new DocUploadRecord();
        docUploadRecord.setBusId(docBsDocument.getBusId());
        docUploadRecord.setIndexName(index);
        //处理url
        try {
            File file;
            String prefix = str.split(":")[0].toLowerCase();
            switch (prefix) {
                case "file":
                    file = FileUtils.getFileByPath(str, tempPath);
                    break;
                case "http":
                case "https":
                    file = FileUtils.getFileByUrl(str, tempPath + name);
                    break;
                default:
                    file = new File(str);
                    AssertUtils.isTrue(!file.exists(), str + ":文件不存在");
            }
            AssertUtils.isNull(file, "文件解析失败");
            byte[] bytes = FileUtils.getContent(file);
            AssertUtils.isTrue(null == bytes, "文件解析失败");
            String base64FileContent = Base64.getEncoder().encodeToString(bytes);
            AssertUtils.isNull(base64FileContent, "base64FileContent:文件内容不能为空");
            /*Result<SysUserDTO> result = userApi.getUserByUserId(docBsDocumentExtend.getDocOwner());
            SysUserDTO sysUser = result.getData();
            //查询所有者
            Result<List<SysUserDTO>> userListByUserIds = userApi.getUserListByUserIds(new Long[]{docBsDocumentExtend.getDocOwner()});
            AssertUtils.isNull(userListByUserIds.getData(), "参数错误，存在未知用户！");*/
            DocFullTextFileDTO docFullTextFileDTO = new DocFullTextFileDTO();
            docFullTextFileDTO.setDocName(name);
            docFullTextFileDTO.setSuffix(docBsDocument.getDocSuffix());
            docFullTextFileDTO.setContent(base64FileContent);
            docFullTextFileDTO.setBusId(docBsDocument.getBusId());
            docFullTextFileDTO.setDocOwnerStr(docBsDocumentDTO.getUserName());

            if (ObjectUtils.isEmpty(docFullTextFileDTO.getCreatTime())) {
                docFullTextFileDTO.setCreatTime(docBsDocument.getCreateTime());
            }
            docFullTextFileDTO.setUpdateTime(docBsDocument.getUpdateTime());
            esUtil.indexFullTextFile(index, docBsDocument.getBusId().toString(), docFullTextFileDTO);

            docUploadRecord.setIsSucceed(1);
            docUploadRecord.setCreateTime(new Date());
            docUploadRecordMapper.insert(docUploadRecord);
            log.info("es写入成功，耗时：{}ms", System.currentTimeMillis() - sTime);
        } catch (Exception e) {
            docUploadRecord.setIsSucceed(0);
            String exMsg = e.getMessage();
            docUploadRecord.setExceptionMsg(exMsg != null && exMsg.length() > 5000 ? exMsg.substring(0, 5000) : exMsg);
            docUploadRecord.setCreateTime(new Date());
            docUploadRecordMapper.insert(docUploadRecord);
            log.error("es写入失败：{}", e.getMessage(), e);
            throw new RuntimeException("es写入失败：" + e.getMessage());
        }
    }


    /**
     * 添加es业务信息
     */
    public void addEsBusiInfo(EcmBusiInfoEsDTO ecmBusiInfoEsDTO, Long userId) {
        //上传到es
        uploadEs(ecmBusiInfoEsDTO.getBusiId(), ecmBusiInfoEsDTO, 0, userId);
    }

    /**
     * 添加es文件信息
     */
    @Async("EsExecutor")
    public Future<String> addEsFileInfo(EcmFileInfoEsDTO ecmFileInfoEsDTO, Long userId) {
        //上传到es
        uploadEs(ecmFileInfoEsDTO.getFileId(), ecmFileInfoEsDTO, 1, userId);
        return new AsyncResult<String>(null);
    }


    /**
     * 添加业务信息到es
     */
    private void uploadEs(Long id, Object object, Integer type, Long userId) {
        /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 设置时区为 GMT+8*/

        JSONObject ecmJson = JsonUtils.parseObject(object);
        Map map = JsonUtils.parseObject(JsonUtils.toJSONString(ecmJson), Map.class);
        if (MigrateConstant.ZERO.equals(type)) {
            EcmBusiInfoEsDTO ecmBusiInfoEsDTO = (EcmBusiInfoEsDTO) object;
            EsEcmBusi esEcmBusi = new EsEcmBusi();
            BeanUtils.copyProperties(ecmBusiInfoEsDTO, esEcmBusi);
            esEcmBusi.setId(esEcmBusi.getBusiId() + "");
            esEcmBusi.setBaseBizSourceId(esEcmBusi.getBusiId());
            esEcmBusi.setBaseBizSource("migr-service"); //todo
            esEcmBusi.setBaseCreateTime(ecmBusiInfoEsDTO.getCreateDate());
            /*esEcmBusi.setCreateDate(sdf.format(ecmBusiInfoEsDTO.getCreateDate()));
            esEcmBusi.setUpdateTime(sdf.format(ecmBusiInfoEsDTO.getUpdateTime()));*/
            esEcmBusi.setBaseCreateUser(userId);
            System.out.println(JsonUtils.toJSONString(esEcmBusi));
            esEcmBusiMapper.insert(esEcmBusi, bizIndex);
            //logger.info("成功添加业务信息到es:{}", map);
        } else {
            //判断文件信息是否已经存在es中
            boolean doc = ecmEsUtils.isExistsDocument(fileIndex, "_doc", id + "");
            if (doc) {
                //使用脚本script 完成部分字段的更新
                final String[] scriptCode = {" "};
                final Integer[] a = {1};
                Map<String, Object> scriptParams = new HashMap<>();
                //将实体类中的属性提取出，批量新增到es中
                map.forEach((k, v) -> {
                    scriptCode[MigrateConstant.ZERO] = scriptCode[MigrateConstant.ZERO] + "ctx._source." + k + "=params." + "newValue" + a[MigrateConstant.ZERO] + ";";
                    scriptParams.put("newValue" + a[MigrateConstant.ZERO], v);
                    a[MigrateConstant.ZERO]++;
                });
                EcmFileInfoEsDTO esEcmFile = (EcmFileInfoEsDTO) object;
                String suffix = FilenameUtils.getExtension(esEcmFile.getFileName());
                EsEcmFile baseFileObjEs = getBaseFileObjEs(id, esEcmFile.getFileName(), suffix, null,esEcmFile.getNewFileSize());
                BeanUtils.copyProperties(esEcmFile, baseFileObjEs);
                //设置fileExif
                baseFileObjEs.setExif(JSON.toJSONString(esEcmFile.getFileExif()));
                //baseFileObjEs.setExif(esEcmFile.getFileExif());
                baseFileObjEs.setCreateDate(esEcmFile.getCreateDate());
                baseFileObjEs.setUpdateTime(esEcmFile.getUpdateTime());
                esEcmFileMapper.updateById(baseFileObjEs, fileIndex);
                //logger.info("成功修改文件信息到es:{},id:{}", map, id);
            } else {
                EcmFileInfoEsDTO esEcmFile = (EcmFileInfoEsDTO) object;
                String suffix = FilenameUtils.getExtension(esEcmFile.getFileName());
                Long newFileSize = esEcmFile.getNewFileSize();
                //判断文件后缀是否走管道
                if (suffixArr.contains(suffix)){
                    File file = new File(esEcmFile.getFilePath());
                    byte[] bytes = FileUtils.getContent(file);
                    String base64FileContent = Base64.getEncoder().encodeToString(bytes);
                    EsEcmFile baseFileObjEs = getBaseFileObjEs(id, esEcmFile.getFileName(), suffix, base64FileContent,newFileSize);
                    BeanUtils.copyProperties(esEcmFile, baseFileObjEs);
                    baseFileObjEs.setId(null);
                    baseFileObjEs.setExif(JSON.toJSONString(esEcmFile.getFileExif()));
                    baseFileObjEs.setFileBytes(bytes);
                    if(!CollectionUtils.isEmpty(esEcmFile.getFileLabel())){
                        baseFileObjEs.setFileLabel(JSON.toJSONString(esEcmFile.getFileLabel()));
                    }
                    baseFileObjEs.setCreateDate(esEcmFile.getCreateDate());
                    baseFileObjEs.setUpdateTime(esEcmFile.getUpdateTime());
                    ecmEsUtils.indexPipeline( id + "", baseFileObjEs, fileIndex,true);
                }else {
                    EsEcmFile baseFileObjEs = getBaseFileObjEs(id, esEcmFile.getFileName(), suffix, null,newFileSize);
                    BeanUtils.copyProperties(esEcmFile, baseFileObjEs);
                    //设置fileExif
                    baseFileObjEs.setExif(JSON.toJSONString(esEcmFile.getFileExif()));
                    if(!CollectionUtils.isEmpty(esEcmFile.getFileLabel())){
                        baseFileObjEs.setFileLabel(JSON.toJSONString(esEcmFile.getFileLabel()));
                    }
                    //baseFileObjEs.setExif(esEcmFile.getFileExif());
                    baseFileObjEs.setCreateDate(esEcmFile.getCreateDate());
                    baseFileObjEs.setUpdateTime(esEcmFile.getUpdateTime());
                    esEcmFileMapper.insert(baseFileObjEs, fileIndex);
                }
            }
            //logger.info("成功添加文件信息到es:{}", map);
        }
    }

    private EsEcmFile getBaseFileObjEs(Long fileId, String fileName, String suffix, String base64FileContent,Long fileSize) {
        EsEcmFile baseFileObjEs = new EsEcmFile();
        baseFileObjEs.setId(null == fileId ? null : fileId + "");
        baseFileObjEs.setBaseBizSource("migr-service"); //todo
        baseFileObjEs.setBaseBizSourceId(fileId);
        //目前跟sourceId一致
        baseFileObjEs.setFileId(fileId + "");
        baseFileObjEs.setFileName(fileName);
        baseFileObjEs.setFileSuffix(suffix.toLowerCase());
        //如是文本文件提取赋值
        baseFileObjEs.setTitle("");
        baseFileObjEs.setAbstracts("");
        //文件内容
        baseFileObjEs.setContent(base64FileContent);
        //文件大小
        baseFileObjEs.setNewFileSize(fileSize);
        return baseFileObjEs;
    }
}
