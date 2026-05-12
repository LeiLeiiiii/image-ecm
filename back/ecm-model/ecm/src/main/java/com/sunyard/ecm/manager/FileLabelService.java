package com.sunyard.ecm.manager;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.ecm.annotation.WebsocketNoticeAnnotation;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmFileLabelDto;
import com.sunyard.ecm.dto.ecm.EcmFileLabelExtendDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.es.EsEcmFile;
import com.sunyard.ecm.mapper.EcmFileLabelMapper;
import com.sunyard.ecm.mapper.es.EsEcmFileMapper;
import com.sunyard.ecm.po.EcmFileLabel;
import com.sunyard.ecm.po.EcmFileTagOperationHistory;
import com.sunyard.ecm.vo.EcmFileLableVO;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.dromara.easyes.core.conditions.update.LambdaEsUpdateWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Wenbiwen
 * @since 2025/2/18
 * @desc 影像标签服务接口
 */
@Slf4j
@Service
public class FileLabelService {
    @Value("${fileIndex:ecm_file_dev}")
    private String fileIndex;
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private EcmFileLabelMapper ecmFileLabelMapper;
    @Resource
    private EsEcmFileMapper esEcmFileMapper;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private FileTagOperationHistoryService fileTagOperationHistoryService;

    /**
     * id查询标签
     */
    public List<EcmFileLabelExtendDTO> getLabelByFileId(EcmFileLableVO ecmFileLableVO) {
        Long busiId = ecmFileLableVO.getBusiId();
        Long fileId = ecmFileLableVO.getFileId();
        List<String> docCode = ecmFileLableVO.getDocCode();
        AssertUtils.isNull(busiId, "业务id不能为空");
        List<EcmFileLabel> ecmFileLabels = ecmFileLabelMapper.selectList(new LambdaQueryWrapper<EcmFileLabel>()
                .eq(EcmFileLabel::getBusiId, busiId)
                .eq(fileId != null, EcmFileLabel::getFileId, fileId));
        List<Long> collect = ecmFileLabels.stream().map(EcmFileLabel::getFileId).collect(Collectors.toList());
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(busiId, collect);
        if(CollectionUtil.isEmpty(fileInfoRedis)){
            return new ArrayList<>();
        }
        // 非已删除资料需要过滤未删除文件
        boolean flag=true;
        if (!CollectionUtil.isEmpty(docCode)) {
            if (!(docCode.size() == 1 && IcmsConstants.DELETED_CODE.equals(docCode.get(0)))) {
                flag = false;
                // 需要过滤state = 0的文件
                fileInfoRedis = fileInfoRedis.stream()
                        .filter(s -> StateConstants.ZERO.equals(s.getState()))
                        .collect(Collectors.toList());
            }
        }
        List<Long> collect1;
        if (!CollectionUtil.isEmpty(docCode)) {
            if(flag){
                //已删除的传来的是1，但是文件再未归类中，需要用2处理
                collect1 = fileInfoRedis.stream().filter(s ->StateConstants.COMMON_ONE.equals(s.getState()))
                        .map(FileInfoRedisDTO::getFileId).collect(Collectors.toList());
            }else {
                //标记节点用markId判断
                if(Boolean.TRUE.equals(ecmFileLableVO.getIsMark())){
                    collect1 = fileInfoRedis.stream().filter(s -> s.getMarkDocId() != null && docCode.contains(String.valueOf(s.getMarkDocId())))
                            .map(FileInfoRedisDTO::getFileId).collect(Collectors.toList());
                }else {
                    collect1 = fileInfoRedis.stream().filter(s -> docCode.contains(s.getDocCode()) || (s.getMarkDocId() != null && docCode.contains(String.valueOf(s.getMarkDocId()))))
                            .map(FileInfoRedisDTO::getFileId).collect(Collectors.toList());
                }
            }

        } else {
            collect1 = fileInfoRedis.stream().map(FileInfoRedisDTO::getFileId).collect(Collectors.toList());
        }

        if (CollectionUtil.isEmpty(collect1)) {
            return new ArrayList<>();
        }
        ecmFileLabels = ecmFileLabels.stream().filter(s -> collect1.contains(s.getFileId())).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(ecmFileLabels)) {
            return new ArrayList<>();
        }

        //去重
        //基础标签
        Map<Long, List<EcmFileLabel>> collect3 = ecmFileLabels.stream().filter(s -> s.getLabelId() != null).collect(Collectors.groupingBy(EcmFileLabel::getLabelId));

        //自定义标签
        Set<String> collect4 = ecmFileLabels.stream().filter(s -> s.getLabelId() == null).map(EcmFileLabel::getLabelName).collect(Collectors.toSet());
        Map<String, List<EcmFileLabel>> collect5 = ecmFileLabels.stream().filter(s -> s.getLabelId() == null).collect(Collectors.groupingBy(EcmFileLabel::getLabelName));

        List<EcmFileLabelExtendDTO> labelall = new ArrayList<>();
        Long i = 0l;
        for (String d : collect4) {
            EcmFileLabelExtendDTO extendDTO = new EcmFileLabelExtendDTO();
            extendDTO.setLabelName(d);
            extendDTO.setId(i++);
            extendDTO.setFileCount(collect5.get(d).size());
            labelall.add(extendDTO);
        }
        for (Long labelId : collect3.keySet()) {
            List<EcmFileLabel> ecmFileLabels1 = collect3.get(labelId);
            EcmFileLabel ecmFileLabel = ecmFileLabels1.get(0);
            EcmFileLabelExtendDTO extendDTO = new EcmFileLabelExtendDTO();
            extendDTO.setId(ecmFileLabel.getId());
            extendDTO.setBusiId(ecmFileLabel.getBusiId());
            extendDTO.setLabelName(ecmFileLabel.getLabelName());
            extendDTO.setLabelId(ecmFileLabel.getLabelId());
            extendDTO.setFileCount(ecmFileLabels1.size());
            labelall.add(extendDTO);
        }

        return labelall;
    }

    /**
     * 添加或修改文件标签
     */
    @WebsocketNoticeAnnotation(busiId = "#ecmFileLabels.busiId")
    public void addOrUpdateFileLabel(EcmFileLabelDto ecmFileLabels,AccountTokenExtendDTO accountTokenExtendDTO) {
        // 参数校验
        AssertUtils.isNull(ecmFileLabels.getBusiId(), "业务id不能为空");
        AssertUtils.isNull(ecmFileLabels.getFileIdList(), "文件列表不能为空");
        Set<String> labelNameSet = ecmFileLabels.getLabels().stream()
                .map(EcmFileLabel::getLabelName)
                .collect(Collectors.toSet());
        AssertUtils.isTrue(labelNameSet.size() != ecmFileLabels.getLabels().size(), "存在重复的标签名称");

        // 查询【操作前】每个文件的原始标签（按fileId分组）
        Map<Long, List<EcmFileLabel>> oldLabelMap = getOldLabelsByFileIds(ecmFileLabels.getBusiId(), ecmFileLabels.getFileIdList());

        // 构建待插入标签列表（insertFileLabels）和全量标签列表（fileLabels）
        List<EcmFileLabel> fileLabels = new ArrayList<>();
        List<EcmFileLabel> insertFileLabels = new ArrayList<>();
        boolean ifDelete = true;

        if (ecmFileLabels.getFileIdList() != null && ecmFileLabels.getFileIdList().size() > 1) {
            ifDelete = false; // 多文件时不删除旧标签，只追加
            List<EcmFileLabel> existingLabels = ecmFileLabelMapper.selectList(
                    new LambdaQueryWrapper<EcmFileLabel>()
                            .eq(EcmFileLabel::getBusiId, ecmFileLabels.getBusiId())
                            .in(EcmFileLabel::getFileId, ecmFileLabels.getFileIdList())
            );
            fileLabels.addAll(existingLabels);
            // 系统标签按labelId分组，自定义标签按labelName分组
            Map<Long, List<EcmFileLabel>> systemLabelMap = existingLabels.stream()
                    .filter(l -> l.getLabelId() != null)
                    .collect(Collectors.groupingBy(EcmFileLabel::getLabelId));
            Map<String, List<EcmFileLabel>> customLabelMap = existingLabels.stream()
                    .filter(l -> l.getLabelId() == null)
                    .collect(Collectors.groupingBy(EcmFileLabel::getLabelName));

            // 遍历每个文件和标签，判断是否需要新增
            for (Long fileId : ecmFileLabels.getFileIdList()) {
                for (EcmFileLabel newLabel : ecmFileLabels.getLabels()) {
                    boolean isExist = false;
                    // 系统标签：按labelId判断是否已存在
                    if (newLabel.getLabelId() != null && systemLabelMap.containsKey(newLabel.getLabelId())) {
                        List<EcmFileLabel> existLabels = systemLabelMap.get(newLabel.getLabelId());
                        // 筛选当前文件的已有系统标签
                        EcmFileLabel existLabel = existLabels.stream()
                                .filter(l -> l.getFileId().equals(fileId))
                                .findFirst()
                                .orElse(null);
                        if (existLabel != null) {
                            fileLabels.add(existLabel);
                            isExist = true;
                        }
                    }
                    // 自定义标签：按labelName判断是否已存在（当前文件）
                    if (!isExist && newLabel.getLabelId() == null && customLabelMap.containsKey(newLabel.getLabelName())) {
                        List<EcmFileLabel> existLabels = customLabelMap.get(newLabel.getLabelName());
                        EcmFileLabel existLabel = existLabels.stream()
                                .filter(l -> l.getFileId().equals(fileId))
                                .findFirst()
                                .orElse(null);
                        if (existLabel != null) {
                            fileLabels.add(existLabel);
                            isExist = true;
                        }
                    }
                    // 不存在则新增
                    if (!isExist) {
                        EcmFileLabel insertLabel = buildInsertLabel(ecmFileLabels, fileId, newLabel);
                        fileLabels.add(insertLabel);
                        insertFileLabels.add(insertLabel);
                    }
                }
            }
        } else {
            // 单文件
            for (Long fileId : ecmFileLabels.getFileIdList()) {
                for (EcmFileLabel newLabel : ecmFileLabels.getLabels()) {
                    EcmFileLabel insertLabel = buildInsertLabel(ecmFileLabels, fileId, newLabel);
                    fileLabels.add(insertLabel);
                    insertFileLabels.add(insertLabel);
                }
            }
        }

        //执行标签更新（删/插、缓存、ES）+ 插入历史记录（事务内）
        insertFileLableInfo(ecmFileLabels, labelNameSet, insertFileLabels, ifDelete, fileLabels, oldLabelMap,accountTokenExtendDTO);
    }


    /**
     * 事务方法：执行标签更新+历史记录插入
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertFileLableInfo(EcmFileLabelDto ecmFileLabels, Set<String> labelNameSet,
                                    List<EcmFileLabel> insertFileLabels, boolean ifDelete,
                                    List<EcmFileLabel> newAllLabels, Map<Long, List<EcmFileLabel>> oldLabelMap,AccountTokenExtendDTO tokenExtendDTO) {
        // 更新缓存（新标签列表）
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(ecmFileLabels.getBusiId(), ecmFileLabels.getFileIdList());
        Map<Long, List<EcmFileLabel>> newLabelMap = newAllLabels.stream()
                .collect(Collectors.groupingBy(EcmFileLabel::getFileId));
        for (FileInfoRedisDTO dto : fileInfoRedis) {
            dto.setEcmFileLabels(newLabelMap.get(dto.getFileId()));
        }
        busiCacheService.updateFileInfoRedis(fileInfoRedis);

        // 更新ES
        for (Long fileId : ecmFileLabels.getFileIdList()) {
            esEcmFileMapper.update(null, new LambdaEsUpdateWrapper<EsEcmFile>()
                    .indexName(fileIndex)
                    .set(EsEcmFile::getFileLabel, JSONObject.toJSONString(labelNameSet))
                    .eq(EsEcmFile::getId, String.valueOf(fileId))
            );
        }

        // 数据库更新（先删后插/直接插入）
        if (ifDelete) {
            // 单文件：先删除旧标签
            ecmFileLabelMapper.delete(new LambdaQueryWrapper<EcmFileLabel>()
                    .eq(EcmFileLabel::getBusiId, ecmFileLabels.getBusiId())
                    .in(EcmFileLabel::getFileId, ecmFileLabels.getFileIdList())
            );
        }
        // 批量插入新标签
        if (!insertFileLabels.isEmpty()) {
            MybatisBatch<EcmFileLabel> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, insertFileLabels);
            MybatisBatch.Method<EcmFileLabel> method = new MybatisBatch.Method<>(EcmFileLabelMapper.class);
            mybatisBatch.execute(method.insert());
        }

        // 构建并插入历史记录
        List<EcmFileTagOperationHistory> historyList = fileTagOperationHistoryService.buildOperationHistory(ecmFileLabels, oldLabelMap, newLabelMap,tokenExtendDTO);
        if (!historyList.isEmpty()) {
            // 批量插入历史记录（MyBatis-Plus批量插入）
            fileTagOperationHistoryService.insertBatch(historyList);
        }
    }

    /**
     * 查询标签
     */
    public List<EcmFileLabel> queryLabels(Long busiId, Long fileId) {
        LambdaQueryWrapper<EcmFileLabel> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(EcmFileLabel::getBusiId,busiId);
        lambdaQueryWrapper.eq(EcmFileLabel::getFileId,fileId);
        return ecmFileLabelMapper.selectList(lambdaQueryWrapper);
    }

    /**
     * 构建待插入的标签对象
     */
    private EcmFileLabel buildInsertLabel(EcmFileLabelDto dto, Long fileId, EcmFileLabel newLabel) {
        EcmFileLabel insertLabel = new EcmFileLabel();
        insertLabel.setId(snowflakeUtil.nextId());
        insertLabel.setFileId(fileId);
        insertLabel.setBusiId(dto.getBusiId());
        insertLabel.setLabelId(newLabel.getLabelId());
        insertLabel.setLabelName(newLabel.getLabelName());
        return insertLabel;
    }

    /**
     * 查询操作前每个文件的原始标签
     */
    private Map<Long, List<EcmFileLabel>> getOldLabelsByFileIds(Long busiId, List<Long> fileIds) {
        if (CollectionUtil.isEmpty(fileIds)) {
            return Collections.emptyMap();
        }
        // 查询当前文件已有的标签（操作前状态）
        List<EcmFileLabel> oldLabels = ecmFileLabelMapper.selectList(
                new LambdaQueryWrapper<EcmFileLabel>()
                        .eq(EcmFileLabel::getBusiId, busiId)
                        .in(EcmFileLabel::getFileId, fileIds)
        );

        // 按文件ID分组
        return oldLabels.stream()
                .collect(Collectors.groupingBy(EcmFileLabel::getFileId));
    }


}
