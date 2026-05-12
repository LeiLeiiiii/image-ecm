package com.sunyard.ecm.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.ecm.constant.DocRightConstants;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.EcmBusExtendDTO;
import com.sunyard.ecm.dto.EcmRuleDataDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiStructureTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmDocrightDefDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.ecm.TypeStateDTO;
import com.sunyard.ecm.dto.redis.EcmBusiDocRedisDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmFileHistoryMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.po.EcmBusiDoc;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmFileHistory;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.service.ModelBusiService;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.module.storage.api.FileHandleApi;
import com.sunyard.module.system.api.DictionaryApi;
import com.sunyard.module.system.api.dto.SysDictionaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author： zyl
 * @create： 2023/4/13 16:25
 * @desc：公共接口实现类
 */
@Slf4j
@Service
public class CommonService {
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmFileHistoryMapper ecmFileHistoryMapper;
    @Resource
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Resource
    private FileHandleApi fileHandleApi;
    @Resource
    private DictionaryApi dictionaryApi;
    @Resource
    private StaticTreePermissService staticTreePermissService;
    @Resource
    private ModelBusiService modelBusiService;

    // 创建一个固定大小的线程池，这里设置为10个线程
    private final ExecutorService executor = new ThreadPoolExecutor(
            10, // 核心线程数
            10, // 最大线程数
            0L, // 空闲线程存活时间
            TimeUnit.MILLISECONDS, // 时间单位
            new LinkedBlockingQueue<Runnable>(), // 任务队列
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(10);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "MyThreadPool-" + threadNumber.getAndIncrement());
                }
            }
    );



    /**
     * 构建动态树的资料树
     */
    public void buildDynTree(EcmBusiDocRedisDTO ecmBusiDocDto, List<EcmBusiDoc> ecmBusiDocs) {

        for (EcmBusiDoc ecmBusiDoc : ecmBusiDocs) {
            //第一层级
            EcmBusiDocRedisDTO ecmBusiDocRedisDTO = new EcmBusiDocRedisDTO();
            BeanUtils.copyProperties(ecmBusiDoc, ecmBusiDocRedisDTO);
            ecmBusiDocRedisDTO.setParent(ecmBusiDoc.getParentId() + "");
            if (ecmBusiDoc.getParentId() != null && ecmBusiDoc.getParentId().equals(ecmBusiDocDto.getDocId())) {
                if (CollectionUtils.isEmpty(ecmBusiDocDto.getChildren())) {
                    ecmBusiDocDto.setChildren(new ArrayList<>());
                }
                List<EcmBusiDocRedisDTO> children = ecmBusiDocDto.getChildren();
                children.add(ecmBusiDocRedisDTO);
            }
        }

        List<EcmBusiDocRedisDTO> children = ecmBusiDocDto.getChildren();
        if (!CollectionUtils.isEmpty(children)) {
            for (EcmBusiDocRedisDTO ecmBusiDocRedisDTO : children) {
                buildDynTree(ecmBusiDocRedisDTO, ecmBusiDocs);
            }
        }
    }

    /**
     * 根据业务类型获取关联资料信息
     */
    public List<EcmBusiDocRedisDTO> getDocList(String appTypeId) {
        //根据业务类型查询关联的资料
        List<EcmDocTreeDTO> ecmDocTreeDTOS = modelBusiService.searchOldRelevanceInformation(appTypeId);
        ecmDocTreeDTOS = ecmDocTreeDTOS.stream().sorted(Comparator.comparing(EcmDocTreeDTO::getDocSort)).collect(Collectors.toList());
        List<EcmBusiDocRedisDTO> resultData = copyTree(ecmDocTreeDTOS);
        return resultData;
    }


    /**
     * 组装资料树
     *
     * @param sourceTree
     */
    public List<EcmBusiDocRedisDTO> copyTree(List<EcmDocTreeDTO> sourceTree) {
        List<EcmBusiDocRedisDTO> copiedTree = new ArrayList<>();
        if (sourceTree != null) {
            for (EcmDocTreeDTO sourceNode : sourceTree) {
                EcmBusiDocRedisDTO copiedNode = new EcmBusiDocRedisDTO();
                // 复制节点属性，可以根据需要复制其他属性
                BeanUtil.copyProperties(sourceNode, copiedNode);
                // 设置type属性
                copiedNode.setNodeType(sourceNode.getType());
//                copiedNode.setDocId(sourceNode.getDocCode());
                List<EcmDocTreeDTO> children = sourceNode.getChildren();
                // 递归复制子节点
                copiedNode.setChildren(BeanUtil.copyToList(copyTree(children), EcmBusiDocRedisDTO.class));
                copiedTree.add(copiedNode);
            }
        }
        return copiedTree;
    }


    /**
     * 多维度排序
     */
    public String getRuleDataSort(List<EcmRuleDataDTO> ecmRuleDataDTO) {
        if (CollectionUtils.isEmpty(ecmRuleDataDTO)) {
            return null;
        }
        List<EcmRuleDataDTO> dtos = ecmRuleDataDTO.stream().sorted(Comparator.comparing(EcmRuleDataDTO::getDimensionCode)).collect(Collectors.toList());

        StringBuffer str = new StringBuffer();
        for (EcmRuleDataDTO dto : dtos) {
            str.append(dto.getDimensionValue() + ";");
        }
        return str.toString();
    }


    /**
     * 处理多维度数据
     */
    public List<EcmDocrightDefDTO> dealRuleData(EcmBusExtendDTO busExtendDTO, AccountTokenExtendDTO token, Integer rightVer) {
        if (busExtendDTO != null) {
            if (!CollectionUtils.isEmpty(busExtendDTO.getEcmRuleDataDTO())) {
                //多维度
                List<EcmRuleDataDTO> ecmRuleDataDTO = busExtendDTO.getEcmRuleDataDTO();

                String ruleDataSort = getRuleDataSort(ecmRuleDataDTO);

                ArrayList<String> objects = new ArrayList<>();
                objects.add(ruleDataSort);
                List<EcmDocrightDefDTO> ecmDocrightDefExtendsByRole = staticTreePermissService.getEcmDocrightDefExtendsByRole(busExtendDTO.getAppCode(), rightVer, objects, token.getUsername(), null, DocRightConstants.LOT_DIM);
                return ecmDocrightDefExtendsByRole;
            } else if (rightVer != null) {
                //角色
                List<EcmDocrightDefDTO> ecmDocrightDefDTOS = staticTreePermissService.roleDimLogic(busExtendDTO.getAppCode(), rightVer, token);
                return ecmDocrightDefDTOS;
            }
        }
        return null;
    }

    /**
     * 新增一条文件历史记录
     */
    @Transactional(rollbackFor = Exception.class)
    public EcmFileHistory insertFileHistory(Long busiId, Long fileId, Long newFileId, String fileOperation, String userId, Long fileSize,String ext) {
        EcmFileHistory ecmFileHistory = new EcmFileHistory();
        ecmFileHistory.setBusiId(busiId);
        ecmFileHistory.setFileId(fileId);
        ecmFileHistory.setNewFileSize(fileSize);
        ecmFileHistory.setNewFileId(newFileId);
        ecmFileHistory.setFileOperation(fileOperation);
        ecmFileHistory.setCreateUser(userId);
        ecmFileHistory.setCreateTime(new Date());
        ecmFileHistory.setNewFileExt(ext);
        ecmFileHistoryMapper.insert(ecmFileHistory);
        return ecmFileHistory;
    }

    /**
     * 目标节点 新增校验，文件类型、大小、数量、md5校验
     */
    public void checkDocRightTarget(List<FileInfoRedisDTO> selectedFiles, EcmBusiInfoRedisDTO busiInfoTarget, EcmBusiStructureTreeDTO targetDocNode, String operation,
                                    Boolean blag, List<FileInfoRedisDTO> fileInfoRedis, List<EcmDocrightDefDTO> currentDocRight) {
        List<String> noRightFilesByFormat = new ArrayList<>();
        List<String> noRightFilesBySize = new ArrayList<>();
        List<String> noRightFilesByMd5 = new ArrayList<>();
        String targetDocTypeId = targetDocNode.getDocCode();
        String targetDocTypeName = targetDocNode.getName();
        List<EcmDocrightDefDTO> docRightList = new ArrayList<>();
        List<FileInfoRedisDTO> targetFileInfos = new ArrayList<>();
        if (!org.apache.commons.collections4.CollectionUtils.isEmpty(currentDocRight)) {
            docRightList = currentDocRight;
        }

        if (!org.apache.commons.collections4.CollectionUtils.isEmpty(fileInfoRedis)) {
            //业务对应的文件数量
            targetFileInfos = fileInfoRedis;
            //归类目标节点对应的文件数量
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(targetFileInfos)) {
                targetFileInfos = targetFileInfos.stream().filter(f -> f.getDocCode() != null && f.getDocCode().equals(targetDocNode.getDocCode())).collect(Collectors.toList());
            }
        }
        //获取target的md5列表
        List<String> targetMd5List = targetFileInfos.stream()
                .filter(p -> p.getDocId() != null)
                .filter(p -> p.getDocId().equals(targetDocNode.getDocCode()) && IcmsConstants.ZERO.equals(p.getState()))
                .map(EcmFileInfoDTO::getFileMd5)
                .collect(Collectors.toList());
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(docRightList)) {
            return;
        }
        Map<String, List<EcmDocrightDefDTO>> rightGroupedByDocTypeId = docRightList.stream()
                .filter(p -> !ObjectUtils.isEmpty(p.getDocCode()))
                .collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
        //新增权限校验
        List<EcmDocrightDefDTO> ecmDocrightDefDTOS = rightGroupedByDocTypeId.get(targetDocTypeId);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(ecmDocrightDefDTOS)) {
            return;
        }
        EcmDocrightDefDTO targetDocRight = ecmDocrightDefDTOS.get(0);
        String addRight = targetDocRight.getAddRight();
        if (IcmsConstants.ZERO.toString().equals(addRight)) {
            //无新增权限
            AssertUtils.isTrue(true, operation + "失败，节点【 " + targetDocRight.getDocName() + " 】无新增权限");
        }
        Integer maxPages = 1000;
       // Map<String, List<EcmFileTypeDef>> fileTypeGroupByFormat = null;
        if(IcmsConstants.DYNAMIC_TREE.equals(busiInfoTarget.getTreeType())){
            //动态树不校验文件格式
            Map<String, List<EcmDocrightDefDTO>> collect = currentDocRight.stream().collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
            maxPages = collect.get(targetDocRight.getDocCode()).get(0).getMaxLen();
        }else{
            EcmDocDef ecmDocDef = ecmDocDefMapper.selectById(targetDocRight.getDocCode());
//            ArrayList<EcmFileTypeDef> newFileTypeList = getAllLimitByDoc(ecmDocDef);
//            newFileTypeList.forEach(p -> p.setFileTypeCode(p.getFileTypeCode().toLowerCase()));
//            fileTypeGroupByFormat = newFileTypeList.stream()
//                    .collect(Collectors.groupingBy(EcmFileTypeDef::getFileTypeCode));
            //校验文件数量
            if(ecmDocDef.getMaxFiles()!=null){
                maxPages = ecmDocDef.getMaxFiles();
            }
        }

        for (FileInfoRedisDTO selectedFile : selectedFiles) {
            //校验文件类型
//            if (IcmsConstants.STATIC_TREE.equals(busiInfoTarget.getTreeType())) {
//                List<EcmFileTypeDef> ecmFileTypeDefs = fileTypeGroupByFormat.get(selectedFile.getFormat().toLowerCase());
//                if (org.apache.commons.collections4.CollectionUtils.isEmpty(ecmFileTypeDefs)) {
//                    //无该类型权限
//                    noRightFilesByFormat.add(selectedFile.getFormat());
//                } else {
//                    //有该类型权限
//                    //获取最新时间的值
////                    List<EcmFileTypeDef> collect = ecmFileTypeDefs.stream().sorted(Comparator.comparing(EcmFileTypeDef::getCreateTime).reversed()).collect(Collectors.toList());
//                    Long uploadSize = ecmFileTypeDefs.get(0).getUploadSize();
//                    //mb 转 byte
//                    Long uploadSizeByByte = uploadSize * 1024 * 1024;
//                    //校验文件大小
//                    if (selectedFile.getSize() > uploadSizeByByte) {
//                        //超过上传最大大小
//                        noRightFilesBySize.add(selectedFile.getNewFileName());
//                    }
//                }
//                AssertUtils.isTrue(!org.apache.commons.collections4.CollectionUtils.isEmpty(noRightFilesByFormat), operation + "失败，该资料节点不支持上传【 " + String.join("、", noRightFilesByFormat) + " 】类型的文件");
//                AssertUtils.isTrue(!org.apache.commons.collections4.CollectionUtils.isEmpty(noRightFilesBySize), operation + "失败，文件【 " + String.join("、", noRightFilesBySize) + " 】的文件大小超过了最大上传大小");
//            }
            //校验md5
            if (targetMd5List.contains(selectedFile.getFileMd5())) {
                //文件重复
                noRightFilesByMd5.add(selectedFile.getNewFileName());
            }
        }
        //断言抛出
        if (!IcmsConstants.REPEAT_FILE_STRING.equals(operation)) {
            AssertUtils.isTrue(!org.apache.commons.collections4.CollectionUtils.isEmpty(noRightFilesByMd5), "文件【 " + String.join("、", noRightFilesByMd5) + " 】已存在无需再操作");
        }

        //获取当前节点已有文件数量
        LambdaUpdateWrapper<EcmFileInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(EcmFileInfo::getBusiId, targetDocNode.getBusiId());
        wrapper.eq(EcmFileInfo::getDocCode, targetDocNode.getDocCode());
        wrapper.eq(EcmFileInfo::getState, IcmsConstants.ZERO);
        Long aLong = ecmFileInfoMapper.selectCount(wrapper);
        if (blag) {
            AssertUtils.isTrue(aLong >= maxPages, "节点【 " + targetDocTypeName + " 】文件数量已超过最大上传数量【" + maxPages + "】");
        }
    }

    /**
     * 根据业务类型获取关联资料信息
     */
    public List<EcmBusiDocRedisDTO> getDocList1(String appTypeId, Integer rightVer) {
        //根据业务类型查询关联的资料
        List<EcmDocTreeDTO> ecmDocTreeDTOS = modelBusiService.searchOldRelevanceInformation1(appTypeId, rightVer);
        ecmDocTreeDTOS = ecmDocTreeDTOS.stream().sorted(Comparator.comparing(EcmDocTreeDTO::getDocSort)).collect(Collectors.toList());
        // 对每个 EcmDocTreeDTO 对象的 children 进行排序
        ecmDocTreeDTOS.forEach(doc -> {
            List<EcmDocTreeDTO> children = doc.getChildren(); // 获取子节点
            if (children != null) { // 检查子节点是否为 null
                doc.setChildren(
                        children.stream()
                                .sorted(Comparator.comparing(EcmDocTreeDTO::getDocSort))
                                .collect(Collectors.toList())
                );
            }
        });
        List<EcmBusiDocRedisDTO> resultData = copyTree(ecmDocTreeDTOS);
        return resultData;
    }

    /**
     * 将为加密的文件升级为加密
     */
    public void encryptFile(Integer isEncrypt, List<Long> fileIds) {
        if (IcmsConstants.YES_ENCRYPT.equals(isEncrypt) && CollectionUtil.isNotEmpty(fileIds)) {
            executor.execute(() -> {
                // 这里是你要执行的任务逻辑
                // 注意确保任务是线程安全的
                ArrayList<Long> files = new ArrayList<>(fileIds);
                fileHandleApi.encryptStFile(files);
            });
        }
    }

    /**
     * 更新业务主表数据
     */
    @Async("GlobalThreadPool")
    public void updateEcmBusiInfo(EcmBusiInfo info, String username) {
        LambdaUpdateWrapper<EcmBusiInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(EcmBusiInfo::getBusiNo, info.getBusiNo());
        wrapper.eq(EcmBusiInfo::getAppCode, info.getAppCode());
        wrapper.set(EcmBusiInfo::getUpdateUser, username);
        ecmBusiInfoMapper.update(info, wrapper);
    }

    /**
     * 操作文件时验证权限
     */
    public void checkDocRight(List<String> rights, List<FileInfoRedisDTO> fileInfos, List<Long> fileIdList, String uesrName, List<EcmDocrightDefDTO> docRightList) {
        if (ObjectUtils.isEmpty(fileIdList)) {
            AssertUtils.isTrue(true, "权限校验接口参数有误");
        }
        Map<Long, List<FileInfoRedisDTO>> dtos = new HashMap<>();
        if (CollectionUtil.isNotEmpty(fileInfos)) {
            //扩展了彻底删除,所以不能过滤state为0
            dtos = fileInfos.stream()
                    .collect(Collectors.groupingBy(FileInfoRedisDTO::getFileId));
        }
        if (dtos.isEmpty()) {
            return;
        }
        Set<String> docCodes = fileInfos.stream()
                .filter(s->fileIdList.contains(s.getFileId()))
                .map(FileInfoRedisDTO::getDocCode).collect(Collectors.toSet());
        Map<String, List<EcmDocrightDefDTO>> collect = docRightList.stream()
                .collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
        //.filter(s -> docCodes.contains(s.getDocCode()))
        for(String right:rights){
            switch (right){
                case DocRightConstants.UPDATE_RIGHT:
                    List<EcmDocrightDefDTO> updateRight = docRightList.stream()
                            .filter(s -> docCodes.contains(s.getDocCode()))
                            .filter(s -> StateConstants.ZERO.toString().equals(s.getUpdateRight()))
                            .collect(Collectors.toList());
                    if(!CollectionUtils.isEmpty(updateRight)){
                        AssertUtils.isTrue(true, "操作失败,暂无修改权限");
                    }
                    break;
                case DocRightConstants.OTHER_UPDATE:
                    for (Long id : fileIdList) {
                        List<FileInfoRedisDTO> fileInfoRedisDTOS = dtos.get(id);

                        //先校验修改权限
                        //如果文件在未归类则可以跳过权限（未归类的文件拥有处理权限）
                        if(!IcmsConstants.UNCLASSIFIED_ID.equals(fileInfoRedisDTOS.get(0).getDocCode())) {
                            List<EcmDocrightDefDTO> ecmDocrightDefDTOS = collect.get(fileInfoRedisDTOS.get(0).getDocCode());
                            if (StateConstants.ZERO.toString().equals(ecmDocrightDefDTOS.get(0).getUpdateRight())) {
                                AssertUtils.isTrue(true, "操作失败,暂无修改影像文件权限");
                            }
                            Map<String, List<EcmDocrightDefDTO>> otherUpdates = docRightList.stream()
                                    .filter(s -> StateConstants.ZERO.toString().equals(s.getOtherUpdate()))
                                    .collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
                            if (otherUpdates.containsKey(fileInfoRedisDTOS.get(0).getDocId())) {
                                if (!fileInfoRedisDTOS.get(0).getCreateUser().equals(uesrName)) {
                                    AssertUtils.isTrue(true, "操作失败,暂无修改他人影像文件权限");
                                }
                            }
                        }else if(IcmsConstants.ONE.equals(fileInfoRedisDTOS.get(0).getState())){
                            //已删除的文件 如果删除前是未归类，则校验上传人和删除人是否为同一人
                            if (!fileInfoRedisDTOS.get(0).getCreateUser().equals(uesrName)) {
                                AssertUtils.isTrue(true, "操作失败,暂无修改他人影像文件权限");
                            }
                        }
                    }
                    break;
                default:
                    AssertUtils.isTrue(true, "权限校验类型有误");
            }
        }

    }

    /**
     * 递归提取父节点
     *
     * @param currentNode 当前节点
     * @param treeDTOS 树形节点列表
     * @param groupedByDocCode 按 DocCode 分组的所有节点
     */
    private void extractParent(EcmDocDef currentNode, List<EcmDocTreeDTO> treeDTOS, Map<String, List<EcmDocDef>> groupedByDocCode) {
        String parentCode = currentNode.getParent();
        if (!"0".equals(parentCode)) {
            List<EcmDocDef> parentNodes = groupedByDocCode.get(parentCode);
            if (parentNodes != null && !parentNodes.isEmpty()) {
                EcmDocDef parentNode = parentNodes.get(0); // 取第一个父节点
                EcmDocTreeDTO parentDTO = new EcmDocTreeDTO();
                BeanUtils.copyProperties(parentNode, parentDTO);
                // 防止重复添加
                if (!treeDTOS.contains(parentDTO)) {
                    treeDTOS.add(parentDTO);
                }
                // 递归处理父节点的父节点
                extractParent(parentNode, treeDTOS, groupedByDocCode);
            }
        }
    }

    /**
     * 构建树结构
     */
    private List<EcmDocTreeDTO> buildTree(List<EcmDocTreeDTO> treeDTOS) {
        Map<String, List<EcmDocTreeDTO>> childrenMap = treeDTOS.stream()
                .collect(Collectors.groupingBy(EcmDocTreeDTO::getParent));
        List<EcmDocTreeDTO> tree = new ArrayList<>();
        for (EcmDocTreeDTO node : treeDTOS) {
            if (IcmsConstants.DOC_LEVEL_FIRST.equals(node.getParent())) {
                String label = "(" + node.getDocCode() + ")" + node.getDocName();
                node.setLabel(label);
                node.setDocName(label);
                tree.add(node);
                addChildren(node, childrenMap);
            }
        }
        return tree;
    }

    /**
     * 将子节点添加到父节点
     *
     * @param parentNode 父节点
     * @param childrenMap 子节点分组
     */
    private void addChildren(EcmDocTreeDTO parentNode, Map<String, List<EcmDocTreeDTO>> childrenMap) {
        List<EcmDocTreeDTO> children = childrenMap.get(parentNode.getDocCode());
        if (children != null) {
            parentNode.setChildren(children);
            for (EcmDocTreeDTO child : children) {
                String label = "(" + child.getDocCode() + ")" + child.getDocName();
                child.setLabel(label);
                child.setDocName(label);
                addChildren(child, childrenMap);
            }
        }
    }

    private static String handleUpdate(EcmDocDef docDef, String imgLimit, String limitFormat1) {
        if(StringUtils.isEmpty(imgLimit)){
            return imgLimit;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(imgLimit);
            if(jsonObject!=null){
                String limitFormat = jsonObject.getString("limit_format");
                if(limitFormat==null||!limitFormat.equals(limitFormat1)){
                    jsonObject.put("limit_format", limitFormat1);
                    return jsonObject.toJSONString();
                }
            }
        }catch (Exception e){
            log.error(docDef.getDocCode()+"资料节点数据有误");
        }
        return null;
    }

    /**
     * 查询筛选数据
     */
    public Result<List<EcmDocTreeDTO>> searchSift(List<String> docCodes,List<TypeStateDTO> typeStates) {
        List<EcmDocDef> allEcmDocDefs = ecmDocDefMapper.selectList(null);
        //如果传来的docCodes为空,则默认查全部
        if(CollectionUtils.isEmpty(docCodes)){
            docCodes=allEcmDocDefs.stream().map(EcmDocDef::getDocCode).collect(Collectors.toList());
        }
        //根据自动归类类型ID查询值
        Result<List<SysDictionaryDTO>> dictionaryResult = dictionaryApi.selectValueByParentKey(IcmsConstants.DICTIONARY_CODE, null);
        //根据docCode分组
        Map<String, List<EcmDocDef>> groupedByDocCode = allEcmDocDefs.stream()
                .collect(Collectors.groupingBy(EcmDocDef::getDocCode));
        //根据Parent分组
        Map<String, List<EcmDocDef>> groupedByParentCode = allEcmDocDefs.stream()
                .collect(Collectors.groupingBy(EcmDocDef::getParent));
        List<EcmDocDef> filteredLeafNodes = new ArrayList<>();
        docCodes.forEach(docCode->{
            getFilteredLeafNodesFromChildren(groupedByDocCode.get(docCode).get(0),filteredLeafNodes,groupedByParentCode);
        });
        List<EcmDocDef> ecmDocDefs = getLastNodes(filteredLeafNodes, typeStates);
        // 构建树结构
        List<EcmDocTreeDTO> ecmDocTreeDTOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ecmDocDefs)){
            for (EcmDocDef ecmDocDef : ecmDocDefs) {
                EcmDocTreeDTO ecmDocTreeDTO = new EcmDocTreeDTO();
                BeanUtils.copyProperties(ecmDocDef, ecmDocTreeDTO);
                //自动归类标签名封装
                if(dictionaryResult.isSucc()) {
                    if (ecmDocDef.getAutoClassificationId() != null && !ecmDocDef.getAutoClassificationId().isEmpty()) {
                        //多选拆分
                        String[] classificationIds = ecmDocDef.getAutoClassificationId().split(",");

                        StringBuilder autoClassificationNames = new StringBuilder();

                        if (dictionaryResult.getData() != null) {
                            for (String classificationId : classificationIds) {
                                for (SysDictionaryDTO sysDictionaryDTO : dictionaryResult.getData()) {
                                    // 遍历字典数据，查找与 classificationId 匹配的项
                                    if (sysDictionaryDTO.getDicVal().equals(classificationId)) {
                                        if (autoClassificationNames.length() > 0) {
                                            autoClassificationNames.append(",");
                                        }
                                        autoClassificationNames.append(sysDictionaryDTO.getRemark());
                                        break;
                                    }
                                }
                            }
                        }

                        // 设置拼接后的名称字符串
                        ecmDocTreeDTO.setAutoClassificationName(autoClassificationNames.toString());
                    }
                }
                ecmDocTreeDTOS.add(ecmDocTreeDTO);
                // 递归提取父节点
                extractParent(ecmDocDef, ecmDocTreeDTOS, groupedByDocCode);
            }
            // 将 ecmDocTreeDTOS 转换为树形结构
            List<EcmDocTreeDTO> tree = buildTree(ecmDocTreeDTOS);
            return Result.success(tree);
        }
        return Result.success(ecmDocTreeDTOS);
    }

    /**
     * 多条件AND组合过滤节点
     * @param filteredLeafNodes 待过滤的节点列表
     * @param conditions 条件列表（每个条件包含type和state）
     * @return 满足所有条件的节点列表
     */
    private List<EcmDocDef> getLastNodes(List<EcmDocDef> filteredLeafNodes, List<TypeStateDTO> conditions) {
        // 空条件直接返回原始列表
        if (conditions == null || conditions.isEmpty()) {
            return filteredLeafNodes;
        }

        // 流式过滤：所有条件必须同时满足（AND逻辑）
        return filteredLeafNodes.stream()
                .filter(doc -> checkAllConditions(doc, conditions))
                .collect(Collectors.toList());
    }

    /**
     * 检查单个节点是否满足所有条件
     * @param doc 待检查的节点
     * @param conditions 所有条件
     * @return 是否满足所有条件
     */
    private boolean checkAllConditions(EcmDocDef doc, List<TypeStateDTO> conditions) {
        // 遍历所有条件，必须全部满足
        for (TypeStateDTO condition : conditions) {
            Integer type = condition.getType();
            Integer state = condition.getState();

            // 单个条件的state为null时，跳过该条件（不参与过滤）
            if (state == null) {
                continue;
            }

            // 检查当前条件是否满足
            boolean isMatch = false;
            if (Objects.equals(type, IcmsConstants.REGULARIZE)) {
                isMatch = Objects.equals(doc.getIsRegularized(), state);
            } else if (Objects.equals(type, IcmsConstants.OBSCURE)) {
                isMatch = Objects.equals(doc.getIsObscured(), state);
            } else if (Objects.equals(type, IcmsConstants.REFLECTIVE)) {
                isMatch = Objects.equals(doc.getIsReflective(), state);
            } else if (Objects.equals(type, IcmsConstants.MISS_CORNER)) {
                isMatch = Objects.equals(doc.getIsCornerMissing(), state);
            } else if (Objects.equals(type, IcmsConstants.REMAKE)) {
                isMatch = Objects.equals(doc.getIsRemade(), state);
            } else if (Objects.equals(type, IcmsConstants.AUTOMATIC_CLASSIFICATION)) {
                isMatch = Objects.equals(doc.getIsAutoClassified(), state);
            } else {
                // 存在未知类型时，视为不满足条件
                return false;
            }

            // 只要有一个条件不满足，直接返回false
            if (!isMatch) {
                return false;
            }
        }
        // 所有条件都满足
        return true;
    }


    /**
     * 从当前节点的子节点递归查找符合条件的最子节点
     */
    private void getFilteredLeafNodesFromChildren(EcmDocDef currentNode,
                                                  List<EcmDocDef> filteredLeafNodes, Map<String, List<EcmDocDef>> groupedByParentCode) {
        //获取当前节点的所有子节点
        List<EcmDocDef> childrenNodes = groupedByParentCode.get(currentNode.getDocCode());
        if (childrenNodes != null && !childrenNodes.isEmpty()) {
            for (EcmDocDef child : childrenNodes) {
                if (groupedByParentCode.get(child.getDocCode())==null){
                    if (!filteredLeafNodes.contains(child)){
                    filteredLeafNodes.add(child);}
                }
                else {
                    // 递归检查子节点的子节点
                    getFilteredLeafNodesFromChildren(child,filteredLeafNodes, groupedByParentCode);
                }
            }
        }
        else {
            if (!filteredLeafNodes.contains(currentNode)){
            filteredLeafNodes.add(currentNode);}
        }
    }


    private  Map<String, List<EcmDocDef>> getGroupedByDocCode(List<String> docCodes){
        LambdaQueryWrapper<EcmDocDef> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(EcmDocDef::getDocCode,docCodes);
        List<EcmDocDef> ecmDocDefs=ecmDocDefMapper.selectList(queryWrapper);
        // 按照 docCode 分组
        return ecmDocDefs.stream()
                .collect(Collectors.groupingBy(EcmDocDef::getDocCode));
    }


}
