package com.sunyard.mytool.service.ecm.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.sunyard.mytool.constant.MigrateConstant;
import com.sunyard.mytool.constant.RedisKeyConstant;
import com.sunyard.mytool.dto.BusiAttrDTO;
import com.sunyard.mytool.dto.EcmAppAttrDTO;
import com.sunyard.mytool.dto.EcmAppDefDto;
import com.sunyard.mytool.dto.es.EcmBusiInfoEsDTO;
import com.sunyard.mytool.dto.es.EcmFileInfoEsDTO;
import com.sunyard.mytool.entity.StEquipment;
import com.sunyard.mytool.entity.StFile;
import com.sunyard.mytool.entity.SysUser;
import com.sunyard.mytool.entity.ecm.*;
import com.sunyard.mytool.service.ecm.*;
import com.sunyard.mytool.service.edm.ElasticsearchService;
import com.sunyard.mytool.service.file.FileStroageService;
import com.sunyard.mytool.service.file.UploadFileService;
import com.sunyard.mytool.service.file.impl.FileStroageServiceManager;
import com.sunyard.mytool.service.st.StFileService;
import com.sunyard.mytool.service.sys.SysUserService;
import com.sunyard.mytool.until.ExifConverterUtil;
import com.sunyard.mytool.until.IDUtils;
import com.sunyard.mytool.until.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Future;

import static com.sunyard.mytool.constant.RedisKeyConstant.getBusiAllPrefix;
import static com.sunyard.mytool.constant.RedisKeyConstant.getBusiFile;

@Slf4j
@Service
public class NewEcmServiceImpl implements NewEcmService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${default.loginName:WJJ}")
    private String defaultUserId;
    @Value("${default.userName:王}")
    private String defaultUserName;
    @Value("${default.file-orgcode:2851889946633218}")
    private String defaultfileOrgCode;
    @Value("${default.file-orgname:信雅达}")
    private String defaultfileOrgName;
    @Value("${ksXmlPath}")
    private String ksXmlPath;
    @Value("${fileRootPath}")
    private String fileRootPath;
    @Value("${versionPath}")
    private String versionPath;
    @Autowired
    private EcmBusiInfoService ecmBusiInfoService;
    @Autowired
    private EcmFileInfoService ecmFileInfoService;
    @Autowired
    private StFileService stFileService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private EcmBusiMetadataService ecmBusiMetadataService;
    @Autowired
    private FileStroageServiceManager fileStroageServiceManager;
    @Resource
    private RedisUtil ICMSredisUtil;
    @Autowired
    private UploadFileService uploadFileService;
    @Autowired
    private FileTempService fileTempService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private EcmBusiDocService ecmBusiDocService;
    @Autowired
    private IDUtils idUtils;
    @Autowired
    private EcmSysLabelService ecmSysLabelService;
    @Autowired
    private EcmFileLabelService ecmfilelabelService;

    @Override
    @DSTransactional
    public void buildMainData(EcmAppDefDto ecmAppDefDto, BatchTemp batchTemp, Long busiId, List<Pair<StFile, FileTemp>> successPairs) {
        long start = System.currentTimeMillis();
        ArrayList<StFile> stFiles = new ArrayList<>();
        ArrayList<EcmFileInfo> ecmFileInfos = new ArrayList<>();
        ArrayList<EcmBusiDoc> ecmBusiDocs = new ArrayList<>();
        ArrayList<EcmFileLabel> ecmFileLabels = new ArrayList<>();
        List<EcmBusiMetadata> metaDataListToInsert = new ArrayList<>();
        List<EcmBusiMetadata> metaDataListToUpdate = new ArrayList<>();
        Map<String, EcmFileInfoEsDTO> esDataMap = new HashMap<>();  // 用于暂存 ES 数据
        EcmBusiInfo ecmBusiInfo = null;
        ArrayList<EcmAppAttrDTO> attrList = null;
        try {
            //1.新建文件信息 、文件存储信息 、标记表(ecm_busi_doc)、标签表(ecm_file_label)
            handleStFileAndFileInfo(batchTemp.getOrgCode(), batchTemp.getOrgName(), busiId, successPairs, batchTemp.getAppName(), stFiles, ecmFileInfos,ecmBusiDocs,ecmFileLabels,esDataMap);
            //2.新建业务
            ecmBusiInfo = new EcmBusiInfo();
            ecmBusiInfo.setBusiId(busiId);
            handleBusiInfo(batchTemp, ecmBusiInfo,ecmFileInfos.get(0));
            //3.新建属性
            attrList = new ArrayList<>();
            handleMetaData(batchTemp, ecmBusiInfo.getBusiId(), attrList,metaDataListToInsert,metaDataListToUpdate);
            //4.主表信息写入数据库
            EcmBusiInfo dbEcmBusiInfo = ecmBusiInfoService.getByAppCodeAndBusiNo(ecmBusiInfo.getAppCode(), ecmBusiInfo.getBusiNo());
            if (dbEcmBusiInfo != null) {
                //先查询表中是否存在,存在则沿用其busiid进行修改
                ecmBusiInfo.setBusiId(dbEcmBusiInfo.getBusiId());
                ecmBusiInfoService.updateBusiInfo(ecmBusiInfo);
            } else {
                ecmBusiInfoService.insertBusiInfo(ecmBusiInfo);
            }
            ecmBusiMetadataService.saveOrUpdateMetada(metaDataListToInsert,metaDataListToUpdate);
            ecmBusiDocService.saveList(ecmBusiDocs);
            ecmFileInfoService.saveList(ecmFileInfos);
            ecmfilelabelService.saveList(ecmFileLabels);
            stFileService.saveList(stFiles);
        } catch (Exception e) {
            logger.error("业务{}存入数据库异常", batchTemp.getAppCode() + ":" + batchTemp.getBusiNo(), e);
            throw new RuntimeException("业务存入数据库异常");
        }
        //5.业务信息写入es
        handleEsBusiInfo(batchTemp, attrList, ecmBusiInfo, stFiles.get(0).getCreateUser());
        //6.文件信息写入es
        handleEsFileInfo(esDataMap);
        //7.每次写完表清空一下redis缓存
        try {
            String busiFileKey = getBusiFile(ecmBusiInfo.getBusiId());
            String busiAllPrefixKey = getBusiAllPrefix(ecmBusiInfo.getBusiId());
            redisUtil.del(busiFileKey);
            redisUtil.del(busiAllPrefixKey);
        } catch (Exception e) {
            logger.error("清缓存失败", e);
        }
        logger.info("业务{},存表总耗时:{}", batchTemp.getAppCode() + ":" + batchTemp.getBusiNo(), System.currentTimeMillis() - start);
    }

    private void handleEsFileInfo(Map<String, EcmFileInfoEsDTO> esDataMap) {
        Map<String, Future<String>> futureMap = new HashMap<>();
        for (Map.Entry<String, EcmFileInfoEsDTO> entry : esDataMap.entrySet()) {
            String fileId = entry.getKey();
            EcmFileInfoEsDTO ecmFileInfoEsDTO = entry.getValue();
            Future<String> esFileInfoFuture = elasticsearchService.addEsFileInfo(ecmFileInfoEsDTO, ecmFileInfoEsDTO.getUserId());
            futureMap.put(fileId, esFileInfoFuture);
        }
        for (String fileId: futureMap.keySet()) {
            Future<String> future = futureMap.get(fileId);
            try {
                future.get();//todo 只针对对应的那个写失败
            } catch (Exception e) {
                logger.error("文件信息写入es失败，中间表fileId: {}", fileId, e);
                throw new RuntimeException("文件信息ES写入任务执行失败");
            }
        }
    }


    public void handleEsBusiInfo(BatchTemp batchTemp, ArrayList<EcmAppAttrDTO> attrList, EcmBusiInfo ecmBusiInfo, Long userId) {
        EcmBusiInfoEsDTO ecmBusiInfoEsDTO = new EcmBusiInfoEsDTO();
        String appAttrs = null;
        try {
            List<Map<String, String>> mapList = new ArrayList<>();
            if (!org.apache.commons.collections4.CollectionUtils.isEmpty(attrList)) {
                attrList.forEach(p -> {
                    Map<String, String> appAttrIdMap = new HashMap<>();
                    appAttrIdMap.put("id", String.valueOf(p.getAppAttrId()));
                    appAttrIdMap.put("label", p.getAttrName());
                    appAttrIdMap.put("value", p.getAppAttrValue());
                    mapList.add(appAttrIdMap);
                });
                appAttrs = JSONObject.toJSONString(mapList);
            }
            long startBusiES = System.currentTimeMillis();
            String creatUserName = ecmBusiInfo.getCreateUserName();
            String updateUserName = ecmBusiInfo.getUpdateUserName();
            ecmBusiInfoEsDTO.setBusiNo(ecmBusiInfo.getBusiNo())
                    .setBusiId(ecmBusiInfo.getBusiId())
                    .setAppCode(ecmBusiInfo.getAppCode())
                    .setAppTypeName(batchTemp.getAppName())
                    .setAppAttrs(appAttrs)
                    .setAppAttrMap(mapList)
                    .setCreatUserName(creatUserName)
                    .setUpdateUserName(updateUserName)
                    .setUpdateTime(ecmBusiInfo.getUpdateTime().getTime())
                    .setCreateDate(ecmBusiInfo.getCreateTime().getTime())
                    .setIsDeleted(0)
                    .setOrgCode(batchTemp.getOrgCode());
            elasticsearchService.addEsBusiInfo(ecmBusiInfoEsDTO, userId);
            logger.info("业务: {},业务信息写入es耗时: {}", ecmBusiInfo.getAppCode() + ":" + ecmBusiInfo.getBusiNo(), System.currentTimeMillis() - startBusiES);
        } catch (Exception e) {
            logger.error("业务信息写入es失败，busiNo: {}, appCode: {}", batchTemp.getBusiNo(), batchTemp.getAppCode(), e);
            throw new RuntimeException("业务信息写入es失败!", e);
        }
    }


    @Override
    public Future<StFile> asyncUploadFile(EcmAppDefDto ecmAppDefDto,FileTemp fileTemp) {
        StEquipment stEquipment = ecmAppDefDto.getStEquipment();
        //Long stEquipmentId = stEquipment.getId();
        FileStroageService fileStroage = fileStroageServiceManager.getFileStroage(stEquipment);
        //执行上传逻辑
        return uploadFileService.asyncUploadFile(stEquipment,fileTemp, fileStroage);
    }


    /**
     * 新建业务信息
     */
    public void handleBusiInfo(BatchTemp batchTemp, EcmBusiInfo ecmBusiInfo,EcmFileInfo ecmFileInfo) {
        //创建机构
        String orgCode = batchTemp.getOrgCode() != null ? batchTemp.getOrgCode() : defaultfileOrgCode;
        String orgName = batchTemp.getOrgName() !=  null ? batchTemp.getOrgCode() : defaultfileOrgName;

        //业务信息
        ecmBusiInfo.setTreeType(batchTemp.getTreeType());
        ecmBusiInfo.setBusiNo(batchTemp.getBusiNo());
        ecmBusiInfo.setAppCode(batchTemp.getAppCode());
        ecmBusiInfo.setRightVer(1); //默认为1
        ecmBusiInfo.setOrgCode(orgCode);
        ecmBusiInfo.setCreateUser(ecmFileInfo.getCreateUser());
        ecmBusiInfo.setCreateTime(ecmFileInfo.getCreateTime());
        ecmBusiInfo.setUpdateUser(ecmFileInfo.getUpdateUser());
        ecmBusiInfo.setUpdateTime(ecmFileInfo.getUpdateTime());
        ecmBusiInfo.setIsDeleted(0);
        ecmBusiInfo.setCreateUserName(ecmFileInfo.getCreateUserName());
        ecmBusiInfo.setUpdateUserName(ecmFileInfo.getUpdateUserName());
        ecmBusiInfo.setOrgName(orgName);
        ecmBusiInfo.setStatus(1);
        //ecmBusiInfoService.saveOrUpdateRewrite(ecmBusiInfo);
    }

    /**
     * 新建属性信息
     */
    public void handleMetaData(BatchTemp batchTemp, Long busiId,ArrayList<EcmAppAttrDTO> attrList, List<EcmBusiMetadata> metaDataListToinsert, List<EcmBusiMetadata> metaDataListToupdate) {
        List<BusiAttrDTO> busiAttrDTOS = JSON.parseArray(batchTemp.getBusiAttr(), BusiAttrDTO.class);
        for (BusiAttrDTO busiAttrDTO : busiAttrDTOS) {
            EcmAppAttrDTO ecmAppAttrDTO = new EcmAppAttrDTO();
            String attrRedisKey =  batchTemp.getAppCode() + ":" + busiAttrDTO.getId();
            //判断该属性是否存在
            EcmAppAttr hget = (EcmAppAttr) ICMSredisUtil.hget(RedisKeyConstant.REDIS_APP_ATTR, attrRedisKey);
            if (hget == null) {
                throw new RuntimeException("影像系统未配置该业务属性:" + attrRedisKey);
            }
            //为后续写es做准备
            ecmAppAttrDTO.setAppAttrId(hget.getAppAttrId());
            ecmAppAttrDTO.setAttrName(hget.getAttrName());
            ecmAppAttrDTO.setAppAttrValue(busiAttrDTO.getValue());
            attrList.add(ecmAppAttrDTO);
            EcmBusiMetadata ecmBusiMetadata = null;
            ecmBusiMetadata = ecmBusiMetadataService.selectByBusiIdAndAppAttrId(busiId, hget.getAppAttrId());
            if (ecmBusiMetadata != null) {
                ecmBusiMetadata.setAppAttrVal(busiAttrDTO.getValue());
                metaDataListToupdate.add(ecmBusiMetadata);
            } else {
                ecmBusiMetadata =new EcmBusiMetadata();
                ecmBusiMetadata.setId(idUtils.nextId());
                ecmBusiMetadata.setBusiId(busiId);
                ecmBusiMetadata.setAppAttrId(hget.getAppAttrId());
                ecmBusiMetadata.setAppAttrVal(busiAttrDTO.getValue());
                metaDataListToinsert.add(ecmBusiMetadata);
            }
        }
    }

    public void handleStFileAndFileInfo(String orgCode, String orgName, Long busiId, List<Pair<StFile, FileTemp>> successPairs, String appName,
                                        List<StFile> stFiles, List<EcmFileInfo> ecmFileInfos, List<EcmBusiDoc> ecmBusiDocs,
                                        List<EcmFileLabel> ecmFileLabels,Map<String, EcmFileInfoEsDTO> esDataMap) {
        long startFileInfoES = System.currentTimeMillis();
        /*ArrayList<StFile> stFiles = new ArrayList<>();
        ArrayList<EcmFileInfo> ecmFileInfos = new ArrayList<>();*/
        // 缓存已处理的 (docCode, markName) 和对应的 EcmBusiDoc
        Map<String, EcmBusiDoc> markDocCache = new HashMap<>();
        Map<String, Future<String>> futureMap = new HashMap<>();
        for (Pair<StFile, FileTemp> pair : successPairs) {
            ArrayList<String> lableNames = new ArrayList<>();
            StFile stFile = pair.getLeft();
            FileTemp fileTemp = pair.getRight();
            //获取创建用户
            SysUser createUser = sysUserService.handleUser(fileTemp.getUpUser());
            //获取修改用户
            SysUser modUser = sysUserService.handleUser(fileTemp.getModUser());
            Date fileCreateTime = fileTemp.getUpTime() == null ? new Date() : fileTemp.getUpTime();
            Date fileModifyTime = fileTemp.getModTime() == null ? new Date() : fileTemp.getModTime();
            stFile.setCreateUser(createUser.getUserId());
            stFile.setCreateTime(fileCreateTime);
            //组装ecmFileInfo
            EcmFileInfo ecmFileInfo = new EcmFileInfo();
            ecmFileInfo.setFileId(idUtils.nextId());
            ecmFileInfo.setBusiId(busiId);
            //已删除或者未归类数据资料类型都写为微服务未归类(Sunyard_@#!_2)
            if (MigrateConstant.OLD_DELETED_CODE.equals(fileTemp.getDocCode()) || MigrateConstant.OLD_UNCLASSIFIED_ID.equals(fileTemp.getDocCode())) {
                fileTemp.setDocCode(MigrateConstant.UNCLASSIFIED_ID);
            }
            ecmFileInfo.setDocCode(fileTemp.getDocCode());;
            //处理标记文件
            if (fileTemp.getMarkName() != null && !fileTemp.getMarkName().isEmpty()){
                String markName = fileTemp.getMarkName();
                String docCode = fileTemp.getDocCode();
                String cacheKey = busiId + ":" + docCode + ":" + markName;
                // 先从缓存中查找是否已有该标记
                EcmBusiDoc ecmBusiDoc = markDocCache.get(cacheKey);
                if (ecmBusiDoc == null){
                    //判断该标记在EcmBusiDoc表中是否存在
                    ecmBusiDoc = ecmBusiDocService.selectMark(busiId,docCode,markName);
                    if (ecmBusiDoc == null) {
                        //数据库中也不存在，新建对象
                        ecmBusiDoc = new EcmBusiDoc();
                        ecmBusiDoc.setDocId(idUtils.nextId());
                        ecmBusiDoc.setBusiId(busiId);
                        ecmBusiDoc.setDocCode(docCode);
                        ecmBusiDoc.setDocName(markName);
                        ecmBusiDoc.setDocSort(0.0f);
                        ecmBusiDoc.setDocMark(1);
                        ecmBusiDocs.add(ecmBusiDoc);
                    }
                    // 将新对象加入缓存
                    markDocCache.put(cacheKey, ecmBusiDoc);
                }
                ecmFileInfo.setMarkDocId(ecmBusiDoc.getDocId());
            }
            ecmFileInfo.setNewFileId(stFile.getId()); //与st_file.id关联
            ecmFileInfo.setNewFileName(fileTemp.getFileName());
            ecmFileInfo.setFileMd5(stFile.getFileMd5());
            ecmFileInfo.setFileReuse(0);
            ecmFileInfo.setFileSort(0.0);
            ecmFileInfo.setCreateUser(createUser.getLoginName());
            ecmFileInfo.setCreateTime(fileCreateTime);
            ecmFileInfo.setUpdateUser(modUser.getLoginName());
            ecmFileInfo.setUpdateTime(fileModifyTime);
            //ecmFileInfo.setComment(); //不设置
            ecmFileInfo.setState(0); //默认0
            ecmFileInfo.setCreateUserName(createUser.getName());
            ecmFileInfo.setUpdateUserName(modUser.getName());
            ecmFileInfo.setNewFileSize(stFile.getSize());
            ecmFileInfo.setNewFileExt(fileTemp.getFileExt()); //todo
            ecmFileInfo.setIsDeleted(0);
            ecmFileInfo.setOrgCode(orgCode);
            ecmFileInfo.setOrgName(orgName);
            ecmFileInfo.setFileSource("ecm-migr");
            //ecmFileInfo.setPageId(fileTemp.getFileId());
            stFiles.add(stFile);
            ecmFileInfos.add(ecmFileInfo);
            //是否有标签
            if (fileTemp.getLabelName() != null && !fileTemp.getLabelName().isEmpty()) {
                String labelNames = fileTemp.getLabelName();
                String[] split = labelNames.split("/");
                for (String s : split) {
                    EcmFileLabel ecmFileLabel = new EcmFileLabel();
                    ecmFileLabel.setId(idUtils.nextId());
                    ecmFileLabel.setBusiId(busiId);
                    ecmFileLabel.setFileId(ecmFileInfo.getFileId());
                    ecmFileLabel.setLabelName(s);
                    //是否是系统标签
                    EcmSysLabel ecmSysLabel = (EcmSysLabel) redisUtil.hget(RedisKeyConstant.REDIS_SYS_LABEL, s);
                    if (ecmSysLabel != null){
                        //查询是否是系统标签
                        ecmFileLabel.setLabelId(ecmSysLabel.getLabelId());
                    }
                    lableNames.add(s);
                    ecmFileLabels.add(ecmFileLabel);
                }
            }
            String sourceFilePath = "";
            if ("1".equals(fileTemp.getIsHistory())){
                sourceFilePath = Paths.get(versionPath, fileTemp.getFilePath()).toString();
            }else {
                sourceFilePath = Paths.get(fileRootPath, fileTemp.getFilePath()).toString();
            }
            //写入es
            EcmFileInfoEsDTO ecmFileInfoEsDTO = new EcmFileInfoEsDTO();
            ecmFileInfoEsDTO.setFileName(ecmFileInfo.getNewFileName())
                    .setNewFileId(ecmFileInfo.getNewFileId())
                    .setFileId(ecmFileInfo.getFileId())
                    .setDocTypeName(fileTemp.getDocName())
                    .setDocCode(ecmFileInfo.getDocCode())
                    .setFormat(stFile.getExt())
                    .setFilePath(sourceFilePath)
                    //.setFileExif(fileTemp.getFileExif())
                    .setNewFileSize(ecmFileInfo.getNewFileSize())
                    .setBusiId(ecmFileInfo.getBusiId())
                    .setBusiNo(fileTemp.getBusiNo())
                    .setAppCode(fileTemp.getAppCode())
                    .setAppTypeName(appName)
                    .setCreatUserName(ecmFileInfo.getCreateUserName())
                    .setUpdateUserName(ecmFileInfo.getUpdateUserName())
                    .setUpdateTime(ecmFileInfo.getUpdateTime().getTime())
                    .setCreateDate(ecmFileInfo.getCreateTime().getTime())
                    .setOrgCode(orgCode);
            ecmFileInfoEsDTO.setUserId(createUser.getUserId());
            if (fileTemp.getFileExif() != null && !fileTemp.getFileExif().isEmpty()) {
                HashMap<String, String> exifMap = ExifConverterUtil.convertExifStringToMap(fileTemp.getFileExif());
                ecmFileInfoEsDTO.setFileExif(exifMap);
            }
            if (!lableNames.isEmpty()){
                ecmFileInfoEsDTO.setFileLabel(lableNames);
            }
            esDataMap.put(fileTemp.getFileId(), ecmFileInfoEsDTO);
            /*Future<String> esFileInfoFuture = elasticsearchService.addEsFileInfo(ecmFileInfoEsDTO, createUser.getUserId());
            futureMap.put(fileTemp.getFileId(), esFileInfoFuture);*/

        }
        logger.info("*handleStFileAndFileInfo方法耗时: {} ms", System.currentTimeMillis() - startFileInfoES);
    }


    /**
     * 解析ks_xml描述文件
     */
    /*private DocumentInfo parseXmlFile(String xmlPath) {

        File xmlFile = new File(xmlPath);
        if (!xmlFile.exists()) {
            log.error("xml描述文件不存在: {}", xmlPath);
            throw new RuntimeException("xml描述文件不存在");
        }
        try {
            // 读取XML
            Document doc = XmlUtil.readXmlDocByInStream(new FileInputStream(new File(xmlPath)));
            Element root = doc.getRootElement();
            DocumentInfo info = new DocumentInfo();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 解析File节点信息
            Element fileElement = root.element("File");
            if (fileElement != null) {
                //xml中时间有,号  给其替换成.
                String createTime = fileElement.elementText("CreateTime").replace(',', '.');
                String modifyTime = fileElement.elementText("ModifyTime").replace(',', '.');
                info.setFileCreateTime(sdf.parse(createTime));
                info.setFileModifyTime(sdf.parse(modifyTime));
                info.setFileCreateUser(fileElement.elementText("CreateUser"));
                info.setFileModifyUser(fileElement.elementText("ModifyUser"));
            }

            //解析Versions节点下的V_1信息（用于文件）
            Element versionsElement = root.element("Versions");
            if (versionsElement != null) {
                Element v1Element = versionsElement.element("V_1");
                if (v1Element != null) {
                    String vCreateTime = v1Element.elementText("CreateTime").replace(',', '.');
                    String vCodifyTime = v1Element.elementText("ModifyTime").replace(',', '.');
                    info.setVersionCreateTime(sdf.parse(vCreateTime));
                    info.setVersionModifyTime(sdf.parse(vCodifyTime));
                    info.setMD5(v1Element.elementText("MD5"));
                    info.setVersionCreateUser(v1Element.elementText("CreateUser"));
                    info.setVersionModifyUser(v1Element.elementText("ModifyUser"));
                }
            }
            return info;
        } catch (Exception e) {
            log.error("解析XML文件失败: {}", xmlPath, e);
            throw new RuntimeException("解析XML文件失败");
        }
    }*/
}
