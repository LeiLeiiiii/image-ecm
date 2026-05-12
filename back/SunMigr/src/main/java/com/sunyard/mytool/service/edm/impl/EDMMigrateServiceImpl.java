package com.sunyard.mytool.service.edm.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.sunyard.mytool.constant.MigrateConstant;
import com.sunyard.mytool.dto.DocBsDocumentDTO;
import com.sunyard.mytool.entity.*;
import com.sunyard.mytool.service.edm.*;
import com.sunyard.mytool.service.file.UploadFileService;
import com.sunyard.mytool.service.file.impl.FileStroageServiceManager;
import com.sunyard.mytool.service.sys.SysUserService;
import com.sunyard.mytool.until.IDUtils;
import com.sunyard.mytool.until.RedisUtil;
import com.sunyard.mytool.until.UUIDUtil;
import com.sunyard.mytool.until.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;


@Slf4j
@Service
public class EDMMigrateServiceImpl implements EDMMigrateService {

    @Value("${houseId}")
    private Long houseId = 2005463973144551424L;

    @Value("${ksXmlPath}")
    private String ksXmlPath;

    @Value("${unknownUserId}")
    private Long unknownUserId ;

    @Value("${unknownUserName}")
    private String unknownUserName;

    @Value("${equipmentId}")
    private Long equipmentId = 0L;

    @Value("${historyTagId}")
    private Long historyTagId = 0L;

    @Autowired
    private RedisUtil redisUtil;
    @Resource
    private IDUtils idUtils;
    @Autowired
    private DocTempService docTempService;
    @Autowired
    private DocBsDocumentService docBsDocumentService;
    @Autowired
    private UploadFileService uploadFileService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private NewEDMService newEDMService;
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private FileStroageServiceManager fileStroageServiceManager;

    @Async("EdmMigrateExecutor")
    @Override
    public Future<String> asyncMigrate(DocTemp docTemp) {
        MDC.put("traceId", UUIDUtil.generateUUID());
        log.info("开始迁移数据id: {} , TxPath: {}", docTemp.getPkId(),docTemp.getTxPath());
        ArrayList<DocBsDocument> folderList = new ArrayList<>();
        StFile stFile = null;
        DocBsDocumentDTO fileDoc = new DocBsDocumentDTO();
        String txPath = docTemp.getTxPath();
        if (txPath.startsWith("/")){
            txPath = txPath.substring(1);  //路径以 / 开头 ,则去掉 /
        }
        if (txPath.endsWith("/")){
            txPath = txPath.substring(0, txPath.length() - 1);  //路径以 / 结尾,去掉末尾 /
        }
        String[] paths = txPath.split("/");
        log.info("拆分后的路径数组: {}", Arrays.toString(paths));
        String key = "edm_migrate_lock:"+ paths[0];
        boolean lock = false;
        try {
            lock = redisUtil.safeLock(key);  //获取锁
            if (!lock) {
                //把状态改回待迁移
                docTemp.setMigStatus(MigrateConstant.MIGRATE_WAITING);
                docTempService.updateById(docTemp);
                log.info("根目录:{}正在执行中,跳过本次执行:{} , id: {} 状态改回待迁移", paths[0],docTemp.getTxPath(),docTemp.getPkId());
                return new AsyncResult<String>(null);
            }
            log.info("获取锁: {} 成功", key);
            Long folderId = null;
            int level = 0;
            String currentPath = "";

            // 1.逐级创建文件夹
            for (int i = 0; i < paths.length - 1; i++) {
                String folderName = paths[i];
                currentPath += "/" + folderName;
                folderId = createFolder(folderId, folderName, level, folderList, currentPath);
                level++;
            }

            // 2. 最后一段判断是文件夹还是文件
            String fileName = paths[paths.length - 1];
            List<DocTemp> versionDocTemps = null;
            if (isFile(fileName)) {
                String fileExt = fileName.substring(fileName.lastIndexOf('.')+1).toLowerCase();
                docTemp.setFileName(fileName);
                docTemp.setFileExt(fileExt);
                // 3.上传文件
                stFile = uploadFileService.uploadEDMFile(docTemp);
                docTemp.setFileMD5(stFile.getFileMd5());
                //4.解析ksxml
                versionDocTemps = parseKsXml(docTemp);
                //5.组装源文件数据
                fileDoc = createDoc(fileName, folderId, docTemp, stFile);
                //docTemp.setSourceFileMD5(fileDoc.getSourceFileMD5());

            } else {
                createFolder(folderId, fileName, level, folderList, currentPath);
            }
            //6.写库
            newEDMService.buildMainData(folderList, fileDoc, stFile,null);

            //7.文件迁移成功修改中间表
            docTemp.setMigTime(new Date());
            docTemp.setMigStatus(MigrateConstant.MIGRATE_SUCCESS);
            docTemp.setFailReason("");
            docTempService.updateById(docTemp);

            //7.迁移历史文件
            if (versionDocTemps != null && !versionDocTemps.isEmpty()){
                migrateHistoryFile(docTemp, versionDocTemps, folderId);
            }

        } catch (Exception e) {
            log.error("迁移失败，id: {}, txPath: {}", docTemp.getPkId(), docTemp.getTxPath(), e);
            // 发生异常改中间表状态
            String failReason = e.getMessage();
            if (StringUtils.isNotBlank(failReason) && failReason.length() > 1024) {
                failReason = failReason.substring(0, 1024) + "(后略)";
            }
            docTemp.setMigTime(new Date());
            docTemp.setMigStatus(MigrateConstant.MIGRATE_FAIL);
            docTemp.setFailReason(failReason);
            docTempService.updateById(docTemp);
        } finally {
            // 只有在确实获取到锁的情况下才释放锁
            if (lock) {
                redisUtil.releaseLockSafely(key);
            }
        }
        return new AsyncResult<String>(null);
    }

    private void migrateHistoryFile(DocTemp docTemp, List<DocTemp> versionDocTemps, Long folderId) {
        for (DocTemp versionDocTemp : versionDocTemps) {
            try {
                //上传文件
                StFile versionStFile = uploadFileService.uploadEDMFile(versionDocTemp);
                versionDocTemp.setFileMD5(versionStFile.getFileMd5());
                //组装DocBsDocument
                SysUser fileUser = sysUserService.selectSysUser(versionDocTemp.getCreateUser());
                Long fileCreateUserId = fileUser == null ? unknownUserId : fileUser.getUserId();
                //文件后缀
                String suffix = "."+versionDocTemp.getFileExt().toLowerCase();
                DocBsDocument file = new DocBsDocument();
                file.setFolderId(folderId);
                file.setDocName(versionDocTemp.getFileName());
                file.setDocSuffix(suffix);
                file.setDocOwner(fileCreateUserId);
                file.setDocCreator(fileCreateUserId);
                file.setUploadTime(versionDocTemp.getCreateTime());
                file.setCreateTime(versionDocTemp.getCreateTime());
                file.setUpdateTime(versionDocTemp.getModifyTime());
                DocBsDocumentDTO docBsDocumentDTO = handleDocFile(file, versionStFile, fileUser);
                //历史文件设置标签
                DocBsTagDocument docBsTagDocument = new DocBsTagDocument();
                long docTagId = IdUtil.getSnowflake().nextId();
                docBsTagDocument.setId(docTagId);
                docBsTagDocument.setDocId(docBsDocumentDTO.getDoc().getBusId());
                docBsTagDocument.setTagId(historyTagId); //历史版本文件标签id
                docBsTagDocument.setCreateTime(new Date());
                docBsTagDocument.setIsDeleted(0);
                //写库
                newEDMService.buildMainData(new ArrayList<>(), docBsDocumentDTO, versionStFile,docBsTagDocument);
                //更新至中间表
                versionDocTemp.setMigTime(new Date());
                versionDocTemp.setMigStatus(MigrateConstant.MIGRATE_SUCCESS);
                versionDocTemp.setFailReason("");
                docTempService.saveOrUpdate(versionDocTemp);
            } catch (Exception e) {
                log.error("id: {} 迁移历史版本: {} 失败", docTemp.getPkId(), versionDocTemp.getFileVersion(), e);
                String failReason = e.getMessage();
                if (StringUtils.isNotBlank(failReason) && failReason.length() > 1024) {
                    failReason = failReason.substring(0, 1024) + "(后略)";
                }
                versionDocTemp.setMigTime(new Date());
                versionDocTemp.setMigStatus(MigrateConstant.MIGRATE_FAIL);
                versionDocTemp.setFailReason(failReason);
                docTempService.saveOrUpdate(versionDocTemp);
            }
        }
    }

    private List<DocTemp> parseKsXml(DocTemp docTemp) {
        ArrayList<DocTemp> versionDocTemps = new ArrayList<>();
        String xmlPath = ksXmlPath + docTemp.getTxPath() + ".ks_xml";
        try {
            KsXmlInfo ksXmlInfo = XmlUtil.parseXmlFile(xmlPath);
            List<VersionInfo> versionInfos = ksXmlInfo.getVersionInfos();
            log.info("id：{} 存在 {} 个历史版本，", docTemp.getPkId(), versionInfos.size() - 1);
            for (VersionInfo versionInfo : versionInfos) {
                String versionCreateUser = versionInfo.getVersionCreateUser();
                Date fileCreateTime = versionInfo.getVersionCreateTime() == null ? new Date() : versionInfo.getVersionCreateTime();
                Date fileModifyTime = versionInfo.getVersionModifyTime() == null ? new Date() : versionInfo.getVersionModifyTime();
                String sourceFileMd5 = versionInfo.getMD5();
                if (versionInfo.isCurrent()) {
                    docTemp.setCreateUser(versionCreateUser);
                    docTemp.setCreateTime(fileCreateTime);
                    docTemp.setModifyTime(fileModifyTime);
                    docTemp.setSourceFileMD5(sourceFileMd5);
                } else {
                    // 拼接新文件名
                    DocTemp docTempVersion = new DocTemp();
                    String baseName = docTemp.getFileName().substring(0, docTemp.getFileName().lastIndexOf("."));
                    String newFileName = baseName + "_V" + versionInfo.getVersionNumber() + "." + docTemp.getFileExt();
                    //版本文件完整路径: E:/EDM/datas/contents.versions/publicfile/CCD/上海分行/aa企业管理有限公司/源文件名/1
                    //  相对路径:  /CCD/上海分行/aa企业管理有限公司/源文件名/1"
                    String docVersionPath = Paths.get(docTemp.getTxPath(), versionInfo.getVersionNumber()).toString();
                    //上传文件
                    docTempVersion.setPkId(idUtils.getUUIDBits(20));
                    docTempVersion.setTxPath(docVersionPath);
                    docTempVersion.setFileName(newFileName);
                    docTempVersion.setFileExt(docTemp.getFileExt());
                    docTempVersion.setSourceFileMD5(sourceFileMd5);
                    docTempVersion.setIsHistory("1");
                    docTempVersion.setFileVersion(versionInfo.getVersionNumber());
                    docTempVersion.setSourceFileId(docTemp.getPkId());
                    docTempVersion.setCreateUser(versionCreateUser);
                    docTempVersion.setCreateTime(fileCreateTime);
                    docTempVersion.setModifyTime(fileModifyTime);
                    versionDocTemps.add(docTempVersion);
                }
            }
        } catch (Exception e) {
            log.error("id: {} 解析ks_xml文件失败", docTemp.getPkId(), e);
            throw new RuntimeException(e);
        }
        return versionDocTemps;
    }

    private DocBsDocumentDTO handleDocFile(DocBsDocument file, StFile stFile, SysUser fileUser) {
        long busId = IdUtil.getSnowflake().nextId();
        file.setBusId(busId);
        file.setHouseId(houseId);
        file.setType(1);
        file.setDocSize(stFile.getSize());
        file.setFileId(stFile.getId());
        file.setDocStatus(2);// 已上架
        file.setDocType(0);  // 企业文档
        file.setRecycleStatus(0);
        file.setIsDeleted(0);
        DocBsDocumentDTO docBsDocumentDTO = new DocBsDocumentDTO();
        docBsDocumentDTO.setDoc(file);
        //docBsDocumentDTO.setSourceFileMD5(sourceFileMd5);
        String userName = fileUser == null ? unknownUserName : fileUser.getName();
        docBsDocumentDTO.setUserName(userName);
        StEquipment stEquipment = fileStroageServiceManager.getStEquipment(equipmentId);
        docBsDocumentDTO.setDocUrl(String.format("%s/%s",stEquipment.getBucket(), stFile.getObjectKey()));
        return docBsDocumentDTO;
    }


    /**
     * 创建单个文件夹
     *
     * @param folderId   父级ID
     * @param folderName 文件夹名称
     * @param level      层级
     * @return 新创建的文件夹ID
     */
    private Long createFolder(Long folderId, String folderName, int level, List<DocBsDocument> folderList, String currentPath) {
        log.info("开始组装文件夹: {}", folderName);
        // 先查询文件夹是否存在
        DocBsDocument document = docBsDocumentService.getDocByConditions(folderId, folderName);
        if (document != null) {
            log.info("文件夹: {} 已存在,无需创建", folderName);
            return document.getBusId();
        }
        // 构建XML路径并解析
        String xmlPath = ksXmlPath + currentPath + ".ks_xml";
        log.info("ksxml路径为: {}", xmlPath);
        DocumentInfo docInfo = parseXmlFile(xmlPath);
        SysUser floderUser = sysUserService.selectSysUser(docInfo.getFileCreateUser());
        Long createUserId = floderUser == null ? unknownUserId : floderUser.getUserId();
        Date createTime = docInfo.getFileCreateTime() == null ? new Date() : docInfo.getFileCreateTime();
        Date modifyTime = docInfo.getFileModifyTime() == null ? new Date() : docInfo.getFileModifyTime();
        // 不存在就新建文件夹
        DocBsDocument folder = new DocBsDocument();
        long busId = IdUtil.getSnowflake().nextId();
        folder.setBusId(busId);
        folder.setHouseId(houseId);
        folder.setDocSeq(1L);
        folder.setType(0);
        folder.setFolderLevel(level);
        folder.setParentId(folderId);
        folder.setDocName(folderName);
        //folder.setDocDescribe("");
        //folder.setRelDoc();  无附件
        //folder.getDocSuffix();
        folder.setDocSize(0L);
        //folder.setDocUrl("");
        //folder.setFileId();
        folder.setDocStatus(2);// 已上架
        folder.setDocType(0);  // 企业文档
        folder.setDocOwner(createUserId); // 创建者id
        folder.setDocCreator(createUserId);
        //folder.setRecycleDate(null);
        folder.setRecycleStatus(0);
        folder.setFolderId(folderId);
        folder.setUploadTime(createTime);
        //folder.setLowerTime();   //下架时间
        folder.setCreateTime(createTime);
        folder.setUpdateTime(modifyTime);  //更新时间
        folder.setIsDeleted(0);
        //docBsDocumentService.save(folder);
        folderList.add(folder);
        return folder.getBusId();
    }

    /**
     * 创建文档
     */
    private DocBsDocumentDTO createDoc(String fileName, Long folderId, DocTemp docTemp, StFile stFile) {
        log.info("开始组装文件: {}", fileName);
        SysUser fileUser = sysUserService.selectSysUser(docTemp.getCreateUser());
        Long fileCreateUserId = fileUser == null ? unknownUserId : fileUser.getUserId();

        String suffix = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
        DocBsDocument file = new DocBsDocument();
        long busId = IdUtil.getSnowflake().nextId();
        file.setBusId(busId);
        file.setHouseId(houseId);
        //file.setDocSeq(0L);  文档不写值
        file.setType(1);
        //file.setFolderLevel(level);  文档不写值
        //file.setParentId(parentId); 文档不写值
        file.setDocName(fileName);
        //folder.setDocDescribe("");
        //folder.setRelDoc();      无附件
        file.setDocSuffix(suffix);   // 小写
        file.setDocSize(stFile.getSize());
        file.setFileId(stFile.getId());
        file.setDocStatus(2);// 已上架
        file.setDocType(0);  // 企业文档
        file.setDocOwner(fileCreateUserId); // ownerId
        file.setDocCreator(fileCreateUserId); // 需要提供creatorId
        //folder.setRecycleDate(null);
        file.setRecycleStatus(0);
        file.setFolderId(folderId);
        file.setUploadTime(docTemp.getCreateTime());
        //file.setLowerTime();
        file.setCreateTime(docTemp.getCreateTime());
        file.setUpdateTime(docTemp.getModifyTime());
        file.setIsDeleted(0);
        DocBsDocumentDTO docBsDocumentDTO = new DocBsDocumentDTO();
        docBsDocumentDTO.setDoc(file);
        docBsDocumentDTO.setSourceFileMD5(docTemp.getSourceFileMD5());
        String userName = fileUser == null ? unknownUserName : fileUser.getName();
        docBsDocumentDTO.setUserName(userName);
        StEquipment stEquipment = fileStroageServiceManager.getStEquipment(equipmentId);
        docBsDocumentDTO.setDocUrl(String.format("%s/%s", stEquipment.getBucket(), stFile.getObjectKey()));
        return docBsDocumentDTO;
    }

    /**
     * 判断路径最后一段是否为文件
     */
    private boolean isFile(String fileName) {
        // 检查是否包含点号且点号不在开头，且不是以/结尾
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 && // 点号不在开头
                lastDotIndex < fileName.length() - 1 && // 点号不在末尾
                !fileName.endsWith("/"); // 不以/结尾
    }


    /**
     * 解析ks_xml描述文件
     */
    private DocumentInfo parseXmlFile(String xmlPath) {

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
            if (fileElement == null) {
                log.error("xml描述文件缺少File节点: {}", xmlPath);
                throw new RuntimeException("xml描述文件缺少File节点");
            }

            //xml中时间有,号  给其替换成.
            String createTime = fileElement.elementText("CreateTime").replace(',', '.');
            String modifyTime = createTime;
            Element modifyTimeEL = fileElement.element("ModifyTime");
            if (modifyTimeEL!= null){
                modifyTime = modifyTimeEL.getText().replace(',', '.');
            }
            info.setFileCreateTime(sdf.parse(createTime));
            info.setFileModifyTime(sdf.parse(modifyTime));

            String createUser = fileElement.elementText("CreateUser");
            String modifyUser = createUser;
            Element ModifyUserEl = fileElement.element("ModifyUser");
            if (ModifyUserEl != null){
                modifyUser = ModifyUserEl.getText();
            }

            info.setFileCreateUser(createUser);
            info.setFileModifyUser(modifyUser);


            //解析Versions节点下的V_1信息（用于文件）
            /*Element versionsElement = root.element("Versions");
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
            }*/
            return info;
        } catch (Exception e) {
            log.error("解析XML文件失败: {}", xmlPath, e);
            throw new RuntimeException("解析XML文件失败");
        }
    }
}
