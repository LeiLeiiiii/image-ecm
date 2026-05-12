package com.sunyard.ecm.manager;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmFileLabelDto;
import com.sunyard.ecm.dto.ecm.EcmFileTagOperationHistoryDTO;
import com.sunyard.ecm.mapper.EcmFileTagOperationHistoryMapper;
import com.sunyard.ecm.po.EcmFileLabel;
import com.sunyard.ecm.po.EcmFileTagOperationHistory;
import com.sunyard.ecm.po.EcmSysLabel;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yzy
 * @desc
 * @since 2025/9/18
 */
@Slf4j
@Service
public class FileTagOperationHistoryService {
    @Resource
    private EcmFileTagOperationHistoryMapper fileTagOperationHistoryMapper;
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;


    /**
     * 核心比对方法：计算单个文件的“新增标签”和“删除标签”
     * @param oldLabels 操作前标签列表
     * @param newLabels 操作后标签列表
     * @return 差异结果（新增+删除）
     */
    private Map<String, List<EcmFileLabel>> compareLabelDiff(List<EcmFileLabel> oldLabels, List<EcmFileLabel> newLabels) {
        // 转换为EcmSysLabel（历史记录存储的标签类型，需确保字段匹配）

        // 构建标签唯一标识映射（系统标签：labelId；自定义标签：labelName）
        Map<String, EcmFileLabel> oldLabelMap = new HashMap<>();
        for (EcmFileLabel label : oldLabels) {
            String key = label.getLabelId() != null ? "sys_" + label.getLabelId() : "custom_" + label.getLabelName();
            oldLabelMap.put(key, label);
        }

        Map<String, EcmFileLabel> newLabelMap = new HashMap<>();
        for (EcmFileLabel label : newLabels) {
            String key = label.getLabelId() != null ? "sys_" + label.getLabelId() : "custom_" + label.getLabelName();
            newLabelMap.put(key, label);
        }

        // 计算新增标签（新标签有，旧标签没有）
        List<EcmFileLabel> addedLabels = newLabelMap.entrySet().stream()
                .filter(entry -> !oldLabelMap.containsKey(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        // 计算删除标签（旧标签有，新标签没有）
        List<EcmFileLabel> deletedLabels = oldLabelMap.entrySet().stream()
                .filter(entry -> !newLabelMap.containsKey(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        // 返回差异结果
        Map<String, List<EcmFileLabel>> diffMap = new HashMap<>();
        diffMap.put("added", addedLabels);
        diffMap.put("deleted", deletedLabels);
        return diffMap;
    }


    /**
     * 转换EcmFileLabel → EcmSysLabel（历史记录用）
     */
    private List<EcmSysLabel> convertToEcmSysLabel(List<EcmFileLabel> fileLabels) {
        if (CollectionUtil.isEmpty(fileLabels)) {
            return Collections.emptyList();
        }
        return fileLabels.stream()
                .map(label -> {
                    EcmSysLabel sysLabel = new EcmSysLabel();
                    sysLabel.setLabelId(label.getId()); // 对应历史记录的tag_id
                    sysLabel.setLabelName(label.getLabelName()); // 对应历史记录的tag_name
                    return sysLabel;
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建历史记录列表（按文件维度）
     */
    public List<EcmFileTagOperationHistory> buildOperationHistory(EcmFileLabelDto dto,
                                                                   Map<Long, List<EcmFileLabel>> oldLabelMap,
                                                                   Map<Long, List<EcmFileLabel>> newLabelMap, AccountTokenExtendDTO tokenExtendDTO) {
        List<EcmFileTagOperationHistory> historyList = new ArrayList<>();


        // 遍历每个文件，生成单独的历史记录
        for (Long fileId : dto.getFileIdList()) {
            List<EcmFileLabel> oldLabels = oldLabelMap.getOrDefault(fileId, Collections.emptyList());
            List<EcmFileLabel> newLabels = newLabelMap.getOrDefault(fileId, Collections.emptyList());

            // 比对差异
            Map<String, List<EcmFileLabel>> diffMap = compareLabelDiff(oldLabels, newLabels);
            List<EcmFileLabel> addedLabels = diffMap.get("added");
            List<EcmFileLabel> deletedLabels = diffMap.get("deleted");

            // 若没有新增也没有删除，跳过（避免空记录）
            if (CollectionUtil.isEmpty(addedLabels) && CollectionUtil.isEmpty(deletedLabels)) {
                continue;
            }

            // 构建历史记录对象
            EcmFileTagOperationHistory history = new EcmFileTagOperationHistory();
            history.setId(snowflakeUtil.nextId());
            history.setFileId(String.valueOf(fileId));
            history.setOperatorId(tokenExtendDTO.getUsername());
            history.setOperatorName(tokenExtendDTO.getName());
            history.setAddTags(addedLabels);
            history.setDeleteTags(deletedLabels);
            history.setRemark(buildRemark(dto, addedLabels, deletedLabels));

            historyList.add(history);
        }
        return historyList;
    }

    /**
     * 构建历史记录备注（可选，增强可读性）
     */
    private String buildRemark(EcmFileLabelDto dto, List<EcmFileLabel> addedLabels, List<EcmFileLabel> deletedLabels) {
        StringBuilder remark = new StringBuilder();
        remark.append("业务ID：").append(dto.getBusiId()).append("；");
        if (!CollectionUtil.isEmpty(addedLabels)) {
            String addedNames = addedLabels.stream().map(EcmFileLabel::getLabelName).collect(Collectors.joining(","));
            remark.append("新增标签：").append(addedNames).append("；");
        }
        if (!CollectionUtil.isEmpty(deletedLabels)) {
            String deletedNames = deletedLabels.stream().map(EcmFileLabel::getLabelName).collect(Collectors.joining(","));
            remark.append("删除标签：").append(deletedNames).append("；");
        }
        return remark.toString();
    }

    /**
     * 批量插入
     */
    @Transactional
    public void insertBatch(List<EcmFileTagOperationHistory> list){
        MybatisBatch<EcmFileTagOperationHistory> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, list);
        MybatisBatch.Method<EcmFileTagOperationHistory> method = new MybatisBatch.Method<>(EcmFileTagOperationHistoryMapper.class);
        mybatisBatch.execute(method.insert());
    }

    /**
     * 根据文件ID和标签ID获取历史记录，且仅保留相关标签
     * 1. 筛选出包含目标labelId的历史记录
     * 2. 对这些记录的addTags和deleteTags仅保留目标labelId的标签
     */
    public List<EcmFileTagOperationHistory> getFileLabelHistory(
            EcmFileTagOperationHistoryDTO queryDTO) {

        // 1. 基础查询：根据fileId获取所有历史记录
        LambdaQueryWrapper<EcmFileTagOperationHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmFileTagOperationHistory::getFileId, queryDTO.getFileId());
        wrapper.orderByDesc(EcmFileTagOperationHistory::getCreateTime);
        List<EcmFileTagOperationHistory> allHistories =
                fileTagOperationHistoryMapper.selectList(wrapper);

        List<String> targetLabelId = queryDTO.getLabelIds();
        if (targetLabelId == null || targetLabelId.isEmpty()) {
            return allHistories;
        }

        // 2. 处理每条记录：过滤标签列表，仅保留目标labelId的标签
        return allHistories.stream()
                .map(history -> {
                    // 复制原对象（避免修改数据库查询缓存的对象）
                    EcmFileTagOperationHistory filteredHistory = new EcmFileTagOperationHistory();
                    BeanUtils.copyProperties(history, filteredHistory);

                    // 过滤addTags：仅保留目标labelId的标签
                    List<EcmFileLabel> filteredAddTags = filterTags(history.getAddTags(), targetLabelId);
                    filteredHistory.setAddTags(filteredAddTags);

                    // 过滤deleteTags：仅保留目标labelId的标签
                    List<EcmFileLabel> filteredDeleteTags = filterTags(history.getDeleteTags(), targetLabelId);
                    filteredHistory.setDeleteTags(filteredDeleteTags);

                    return filteredHistory;
                })
                // 仅保留至少有一个标签匹配的记录（避免空记录）
                .filter(history ->
                        (history.getAddTags() != null && !history.getAddTags().isEmpty()) ||
                                (history.getDeleteTags() != null && !history.getDeleteTags().isEmpty())
                )
                .collect(Collectors.toList());
    }

    /**
     * 过滤标签列表，仅保留与目标labelId匹配的标签
     */
    private List<EcmFileLabel> filterTags(List<EcmFileLabel> tags, List<String> targetLabelIdList) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        Set<Long> targetLabelIdSet = targetLabelIdList.stream()
                .map(labelIdStr -> {
                    try {
                        // 处理字符串转Long可能出现的格式异常
                        return Long.parseLong(labelIdStr);
                    } catch (NumberFormatException e) {
                        // 日志记录无效的ID格式（如非数字字符串）
                        log.warn("无效的labelId格式：{}", labelIdStr);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<EcmFileLabel> filtered = new ArrayList<>();
        // 遍历标签列表
        for (Object object : tags) {
            EcmFileLabel tag= JSON.parseObject(JSON.toJSONString(object), EcmFileLabel.class);
            // 避免tag或tag.getId()为null导致的空指针
            if (tag != null && tag.getId() != null) {
                // 检查是否包含在目标ID集合中
                if (targetLabelIdSet.contains(tag.getId())) {
                    filtered.add(tag);
                }
            }
        }

        return filtered.isEmpty() ? null : filtered;
    }


}
