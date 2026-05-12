package com.sunyard.mytool.service.ecm.impl;

import com.sunyard.mytool.constant.MigrateConstant;
import com.sunyard.mytool.constant.RedisKeyConstant;
import com.sunyard.mytool.dto.EcmAppDefDto;
import com.sunyard.mytool.entity.KsXmlInfo;
import com.sunyard.mytool.entity.StFile;
import com.sunyard.mytool.entity.VersionInfo;
import com.sunyard.mytool.entity.ecm.*;
import com.sunyard.mytool.service.ecm.*;
import com.sunyard.mytool.until.IDUtils;
import com.sunyard.mytool.until.RedisUtil;
import com.sunyard.mytool.until.UUIDUtil;
import com.sunyard.mytool.until.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Future;


@Slf4j
@Service
public class ECMMigrateServiceImpl implements ECMMigrateService {

    @Autowired
    private BatchTempService batchTempService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private FileTempService fileTempService;
    @Resource
    private IDUtils idUtils;
    @Autowired
    private EcmBusiInfoService ecmBusiInfoService;
    @Autowired
    private EcmFileInfoService ecmFileInfoService;
    @Value("${ecm-migration.thread-pool.upload.upload-size}")
    private Integer uploadSize;
    @Value("${ksXmlPath}")
    private String ksXmlPath;
    @Autowired
    private NewEcmService newEcmService;

    @Async("EcmMigrateExecutor")
    @Override
    public Future<String> asyncMigrate(BatchTemp batchTempDTO) {
        MDC.put("traceId", UUIDUtil.generateUUID());
        long start0 = System.currentTimeMillis();
        String migrateBatchLockKey = null;
        Long id = batchTempDTO.getId();
        try {
            BatchTemp batchTemp = batchTempService.getByPK(id);
            if (batchTemp == null) {
                throw new RuntimeException("找不到对应待迁移批次");
            }
            String busiNo = batchTemp.getBusiNo();
            String appCode = batchTemp.getAppCode();
            //基础信息从redis去取

            EcmAppDefDto ecmAppDefDto = (EcmAppDefDto) redisUtil.hget(RedisKeyConstant.REDIS_APP_DEF, appCode);
            if (ecmAppDefDto == null) {
                throw new RuntimeException("影像系统未配置业务类型:" + appCode);
            }
            boolean isLockedSuccessfully = false;
            migrateBatchLockKey = RedisKeyConstant.getMigrateBatchLockKey(appCode, busiNo);
            try {
                isLockedSuccessfully = tryLock(migrateBatchLockKey);
            } catch (Exception e) {
                log.error("批次加锁失败:{}", batchTempDTO.getId(), e);
                throw new RuntimeException("批次加锁失败: " + batchTempDTO.getId(), e);
            }

            //查询文件中间表待迁移文件
            List<FileTemp> fileTempList = fileTempService.listByAppCodeAndBusiNo(batchTempDTO.getAppCode(), batchTempDTO.getBusiNo());
            if (fileTempList == null || fileTempList.size() == 0) {
                batchTempService.updateOneMigrateBatchStatus(id, MigrateConstant.MIGRATE_SUCCESS, "该批次无待迁移文件数据");
                log.error("该批次无待迁移文件数据,批次ID:{}", batchTempDTO.getId());
                return new AsyncResult<String>(null);
            }
            boolean successed = true;
            //把不可迁移文件过滤掉,并做标记
            filterFileTempList(fileTempList,successed);
            if (fileTempList.size() == 0) {
                batchTempService.updateOneMigrateBatchStatus(id, MigrateConstant.MIGRATE_FAIL, "没有有效的文件数据可执行迁移");
                log.error("没有有效的文件数据可执行迁移,批次ID:{}", batchTempDTO.getId());
                return new AsyncResult<String>(null);
            }
            //构建历史版本文件信息
            List<FileTemp> versionfileTemps = buildHistoryFileInfo(fileTempList,successed);
            if (!versionfileTemps.isEmpty()){
                fileTempService.saveBatch(versionfileTemps);
            }
            //将版本数据添加进待迁移列表
            fileTempList.addAll(versionfileTemps);
            if (fileTempList.size() == 0) {
                batchTempService.updateOneMigrateBatchStatus(id, MigrateConstant.MIGRATE_FAIL, "没有有效的文件数据可执行迁移");
                log.error("没有有效的文件数据可执行迁移,批次ID:{}", batchTempDTO.getId());
                return new AsyncResult<String>(null);
            }
            long busiId = idUtils.nextId();
            //先查下ecminfo表是否存在对应批次
            EcmBusiInfo ecmBusiInfo = ecmBusiInfoService.getByAppCodeAndBusiNo(appCode, busiNo);
            if (ecmBusiInfo != null){
                busiId = ecmBusiInfo.getBusiId();
            }

            long startUp = System.currentTimeMillis();
            Map<Future<StFile>, FileTemp> futureFileTempMap = new HashMap<>();
            List<Pair<StFile, FileTemp>> successPairs = new ArrayList<>();
            Iterator<FileTemp> fileTempIterator = fileTempList.iterator();
            //1.文件上传
            //防止文件上传线程池被撑爆,做此处理,每次循环只上传uploadSize个文件
            while (fileTempIterator.hasNext()) {
                for (int workingTaskIndex = 0; fileTempIterator.hasNext() && workingTaskIndex < uploadSize; workingTaskIndex++) {
                    FileTemp fileTemp = fileTempIterator.next();
                    Future<StFile> fileTempFuture = newEcmService.asyncUploadFile(ecmAppDefDto, fileTemp);
                    futureFileTempMap.put(fileTempFuture, fileTemp);
                }

                for (Map.Entry<Future<StFile>, FileTemp> entry : futureFileTempMap.entrySet()) {
                    Future<StFile> future = entry.getKey();
                    FileTemp fileTemp = entry.getValue();
                    try {
                        StFile stFile = future.get();
                        fileTemp.setFileMd5(stFile.getFileMd5());
                        successPairs.add(Pair.of(stFile, fileTemp));
                    } catch (Exception e) {
                        log.error("文件上传失败: {}", fileTemp.getFileName(), e);
                        successed = false;
                        String errorMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                        //文件上传失败,修改状态
                        fileTempService.updateMigStatusById(fileTemp.getFileId(), MigrateConstant.MIGRATE_FAIL, errorMessage);
                    }
                }
                futureFileTempMap.clear();
            }
            log.info("业务: {},文件成功上传数量: {} ,总耗时: {}ms", batchTempDTO.getAppCode() + ":" + batchTempDTO.getBusiNo(), successPairs.size(), System.currentTimeMillis() - startUp);
            /*if (!fileTempList.isEmpty() && !successPairs.isEmpty()) {
                throw new CommonException("该批次没有有效的文件可执行迁移" + batchTempDTO.getAppCode() + ":" + batchTempDTO.getBusiNo());
            }*/
            //2.主表写入
            if (!successPairs.isEmpty()) {
                newEcmService.buildMainData(ecmAppDefDto, batchTemp, busiId, successPairs);
            }
            ArrayList<FileTemp> successFile = new ArrayList<>();
            //3.批量更新文件表状态
            for (Pair<StFile, FileTemp> pair : successPairs) {
                //String fileId = pair.getRight().getFileId();
                FileTemp successFileTemp = pair.getRight();
                successFileTemp.setMigStatus(MigrateConstant.MIGRATE_SUCCESS);
                successFileTemp.setFailReason("");
                successFileTemp.setMigTime(new Date());
                successFile.add(successFileTemp);
            }
            //fileTempService.updateMigStatusByIds(successIds, MigrateConstant.MIGRATE_SUCCESS);
            fileTempService.updateSuccessBatch(successFile);
            //4.更新批次表迁移状态
            if (successed) {
                batchTempService.updateOneMigrateBatchStatus(id, MigrateConstant.MIGRATE_SUCCESS, "");
            } else {
                batchTempService.updateOneMigrateBatchStatus(id, MigrateConstant.MIGRATE_FAIL, "批次下有文件未迁移成功");
            }
            log.info("业务: {},迁移完成,总耗时: {}ms", batchTempDTO.getAppCode() + ":" + batchTempDTO.getBusiNo(), System.currentTimeMillis() - start0);
        } catch (Exception e) {
            log.error("迁移业务id{}迁移失败", id, e);
            batchTempService.updateOneMigrateBatchStatus(id, MigrateConstant.MIGRATE_FAIL, e.getMessage());//todo
            fileTempService.updateMigStatusByAppCodeAndBusiNo(batchTempDTO.getAppCode(), batchTempDTO.getBusiNo(), MigrateConstant.MIGRATE_FAIL, e.getMessage());
            log.info("业务: {},迁移失败!!!,总耗时: {}ms", batchTempDTO.getAppCode() + ":" + batchTempDTO.getBusiNo(), System.currentTimeMillis() - start0);
        } finally {
            try {
                if (migrateBatchLockKey != null) {
                    redisUtil.releaseLock(migrateBatchLockKey);
                }
            } catch (Exception e) {
                log.error("迁移解锁发生异常:{}", migrateBatchLockKey);
            }

        }
        return null;
    }

    /**
     * 构建历史文件信息
     */
    private List<FileTemp> buildHistoryFileInfo(List<FileTemp> fileTempList,boolean successed) {
        ArrayList<FileTemp> versionfileTemps = new ArrayList<>();
        Iterator<FileTemp> iterator = fileTempList.iterator();
        while (iterator.hasNext()) {
            FileTemp fileTemp = null;
            KsXmlInfo docInfo = null;
            try {
                fileTemp = iterator.next();
                String path = fileTemp.getFilePath();
                String originalName = path.substring(path.lastIndexOf("/") + 1);
                String ext = originalName.substring(originalName.lastIndexOf(".") + 1);
                String xmlPath = ksXmlPath + fileTemp.getFilePath() + ".ks_xml";
                log.info("ID: {} ,ksxml路径为: {} ,开始解析", fileTemp.getFileId(),xmlPath);
                docInfo = XmlUtil.parseXmlFile(xmlPath);
                List<VersionInfo> versionInfos = docInfo.getVersionInfos();
                for (VersionInfo versionInfo : versionInfos) {
                    if (versionInfo.isCurrent()){
                        fileTemp.setUpUser(versionInfo.getVersionCreateUser());
                        fileTemp.setUpTime(versionInfo.getVersionCreateTime());
                        fileTemp.setModUser(versionInfo.getVersionModifyUser());
                        fileTemp.setModTime(versionInfo.getVersionModifyTime());
                        fileTemp.setSourceFileMD5(versionInfo.getMD5());
                        fileTemp.setFileName(originalName);
                        fileTemp.setFileExt(ext);
                    }else {
                        FileTemp vsersionFileTemp = new FileTemp();
                        BeanUtils.copyProperties(fileTemp, vsersionFileTemp);
                        vsersionFileTemp.setFileId(idUtils.getUUIDBits(20));
                        String baseName  = originalName.substring(0, originalName.lastIndexOf("."));
                        // 拼接新文件名
                        String newFileName = baseName + "_V" +versionInfo.getVersionNumber() + "." + ext;
                        vsersionFileTemp.setFileName(newFileName);
                        vsersionFileTemp.setFileExt(ext);
                        //版本文件路径: E:/EDM/datas/contents.versions/publicfile/CCD/上海分行/aa企业管理有限公司/源文件名/1"
                        String fileVersionPath = Paths.get(fileTemp.getFilePath(), versionInfo.getVersionNumber()).toString();
                        vsersionFileTemp.setLabelName("历史版本");
                        vsersionFileTemp.setFilePath(fileVersionPath);
                        vsersionFileTemp.setSourceFileMD5(versionInfo.getMD5());
                        vsersionFileTemp.setIsEncrypt(0);
                        vsersionFileTemp.setUpUser(versionInfo.getVersionCreateUser());
                        vsersionFileTemp.setUpTime(versionInfo.getVersionCreateTime());
                        vsersionFileTemp.setModUser(versionInfo.getVersionModifyUser());
                        vsersionFileTemp.setModTime(versionInfo.getVersionModifyTime());
                        vsersionFileTemp.setIsHistory("1");
                        vsersionFileTemp.setCloudType(1);
                        vsersionFileTemp.setSourceFileId(fileTemp.getFileId());
                        vsersionFileTemp.setFileVersion(versionInfo.getVersionNumber());
                        vsersionFileTemp.setFailReason("");
                        versionfileTemps.add(vsersionFileTemp);
                    }
                }
            } catch (Exception e) {
                successed = false;
                String fileId = null;
                if(fileTemp != null) {
                    fileId = fileTemp.getFileId();
                    log.error("文件已被过滤,fileId :{}", fileId, e);
                    fileTemp.setMigStatus(-1);
                    fileTemp.setFailReason(e.getMessage());
                    fileTemp.setMigTime(new Date());
                    fileTempService.saveOrUpdateFileTemp(fileTemp);
                    //把文件移出待处理列表
                    iterator.remove();
                }
            }

        }
        return versionfileTemps;
    }

    /**
     * 把不可迁移文件过滤掉,并做标记
     */
    private void filterFileTempList(List<FileTemp> fileTempList,boolean successed) {
        long start = System.currentTimeMillis();
        try {
            Iterator<FileTemp> iterator = fileTempList.iterator();
            while (iterator.hasNext()) {
                FileTemp fileTemp = null;
                try {
                    fileTemp = iterator.next();
                    EcmDocDef ecmDocDef = (EcmDocDef) redisUtil.hget(RedisKeyConstant.REDIS_DOC_DEF, fileTemp.getDocCode());
                    if (ecmDocDef == null && !fileTemp.getDocCode().equals("DELETE") && !fileTemp.getDocCode().equals("UNTYPE")) {
                        throw new RuntimeException("文件表FILEID为: "+fileTemp.getFileId()+" 的资料类型: " + fileTemp.getDocCode() + " 未在影像系统中定义");
                    }
                } catch (Exception e) {
                    successed = false; //做标记
                    String fileId = null;
                    if(fileTemp != null) {
                        fileId = fileTemp.getFileId();
                    }
                    log.error("文件已被过滤,fileId :{}", fileId, e);
                    fileTemp.setMigStatus(-1);
                    fileTemp.setFailReason(e.getMessage());
                    fileTemp.setMigTime(new Date());
                    fileTempService.saveOrUpdateFileTemp(fileTemp);
                    //把文件移出待处理列表
                    iterator.remove();
                }
            }
        } finally {
            log.debug("*filterFileTempList*耗时:{}", System.currentTimeMillis() - start);
        }
    }

    /**
     * 加锁，防并发
     * @return
     */
    private boolean tryLock(String migrateBatchLockKey) {
        //由于数据库锁在迁移程序内部经常锁不住，这里先加一个redis锁，降低脏数据概率
        //String migrateBatchLockKey = RedisKeyConstant.getMigrateBatchLockKey(appCode,busiNo);
        if (redisUtil.lock(migrateBatchLockKey)) {
            return true;
        } else {
            return false;
        }
    }
}
