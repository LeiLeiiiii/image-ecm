package com.sunyard.ecm.manager;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.ecm.mapper.EcmFileAttrOperationMapper;
import com.sunyard.ecm.po.EcmFileAttrOperation;
import com.sunyard.framework.common.util.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yzy
 * @desc
 * @since 2025/9/19
 */
@Slf4j
@Service
public class FileAttrOperationService {

    @Resource
    private EcmFileAttrOperationMapper operationMapper;

    /**
     * 根据文件ID查询操作记录，并按dtdTypeId分组
     *
     * @param fileId 文件ID
     * @return 键为dtdTypeId，值为该类型的所有操作记录列表
     */
    public Map<Long, List<Map<String, Object>>> queryOperationsGroupByDtdTypeId(String fileId) {
        // 1. 校验参数
        AssertUtils.isNull(fileId, "文件ID不能为空");

        // 2. 查询该文件的所有操作记录，按时间倒序排列
        LambdaQueryWrapper<EcmFileAttrOperation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmFileAttrOperation::getFileId, fileId);
        queryWrapper.orderByDesc(EcmFileAttrOperation::getCreateTime);
        List<EcmFileAttrOperation> operations = operationMapper.selectList(queryWrapper);
        if (operations.isEmpty()) {
            return Collections.emptyMap();
        }

        // 3. 解析所有操作记录，按dtdTypeId分组
        Map<Long, List<Map<String, Object>>> groupedMap = new HashMap<>();

        for (EcmFileAttrOperation operation : operations) {
            // 解析操作记录中的变更详情
            processOperationDetails(operation, groupedMap);
        }

        // 4. 对每个dtdTypeId的操作记录按时间倒序排列（最新的在前）
        Map<Long, List<Map<String, Object>>> sortedResult = new HashMap<>(groupedMap.size());
        for (Map.Entry<Long, List<Map<String, Object>>> entry : groupedMap.entrySet()) {
            List<Map<String, Object>> opList = entry.getValue();
            // 复制新列表，避免修改原集合
            List<Map<String, Object>> sortedList = new ArrayList<>(opList);
            // 按时间倒序（实际已在SQL层完成，此处仅为兜底）
            sortedList.sort((o1, o2) -> {
                Date d1 = (Date) o1.get("operateTime");
                Date d2 = (Date) o2.get("operateTime");
                if (d1 == null || d2 == null) return 0;
                return d2.compareTo(d1); // 倒序
            });
            sortedResult.put(entry.getKey(), sortedList);
        }
        return sortedResult;
    }

    /**
     * 处理单条操作记录的详情，将其按dtdTypeId添加到分组Map中
     */
    private void processOperationDetails(EcmFileAttrOperation operation,
                                         Map<Long, List<Map<String, Object>>> groupedMap) {
        // 解析所有变更详情
        Map<String, Object> changeDetails = parseJsonToMap(operation.getChangeDetails());
        Map<String, Object> addDetails = parseJsonToMap(operation.getAddDetails());
        Map<String, Object> deleteDetails = parseJsonToMap(operation.getDeleteDetails());

        // 处理新增的单证类型
        if (addDetails != null && !addDetails.isEmpty()) {
            processDtdDetails(addDetails, operation, "新增", groupedMap);
        }

        // 处理修改的单证类型（changeDetails中包含修改和新增，这里只处理修改）
        if (changeDetails != null && !changeDetails.isEmpty()) {
            processDtdDetails(changeDetails, operation, "修改", groupedMap);
        }

        // 处理删除的单证类型
        if (deleteDetails != null && !deleteDetails.isEmpty()) {
            processDtdDetails(deleteDetails, operation, "删除", groupedMap);
        }
    }

    /**
     * 处理特定类型的单证操作详情
     */
    private void processDtdDetails(Map<String, Object> detailsMap,
                                   EcmFileAttrOperation operation,
                                   String operationType,
                                   Map<Long, List<Map<String, Object>>> groupedMap) {
        for (Map.Entry<String, Object> entry : detailsMap.entrySet()) {
            // dtdTypeId从键中解析（字符串转Long）
            Long dtdTypeId;
            try {
                dtdTypeId = Long.parseLong(entry.getKey());
            } catch (NumberFormatException e) {
                // 忽略格式错误的记录
                continue;
            }

            // 解析单条单证类型的操作详情
            Map<String, Object> dtdDetail = (Map<String, Object>) entry.getValue();

            // 构建完整的操作记录信息
            Map<String, Object> record = new HashMap<>();
            record.put("operateTime", operation.getCreateTime());
            record.put("operatorName", operation.getOperatorName());
            record.put("operatorId", operation.getOperatorId());
            record.put("operationType", operationType);
            record.put("remark", operation.getRemark());
            record.put("details", dtdDetail); // 包含属性变化等详细信息

            // 添加到分组Map中
            groupedMap.computeIfAbsent(dtdTypeId, k -> new ArrayList<>()).add(record);
        }
    }

    /**
     * 将JSON字符串解析为Map
     */
    private Map<String, Object> parseJsonToMap(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return null;
        }
        try {
            return JSONObject.parseObject(jsonStr, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            // 日志记录：JSON解析失败
            return null;
        }
    }
}
