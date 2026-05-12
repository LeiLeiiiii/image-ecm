package com.sunyard.ecm.util;

import cn.hutool.core.collection.CollectionUtil;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.ecm.EcmBusiStructureTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.redis.EcmBusiDocRedisDTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 饶昌妹
 * @desc 文件处理工具类
 * @since 2024-12-23
 */
public class CommonUtils {


    public static List<EcmBusiDocRedisDTO> flattenTreeToDFS(List<EcmBusiDocRedisDTO> root) {
        if (CollectionUtil.isEmpty(root)) {
            return new ArrayList<>();
        }
        List<EcmBusiDocRedisDTO> result = new ArrayList<>();
        for (EcmBusiDocRedisDTO d : root) {
            dfs(d, result);
        }
        return result;
    }

    private static void dfs(EcmBusiDocRedisDTO node, List<EcmBusiDocRedisDTO> result) {
        if (node == null) return;
        result.add(node); // 先访问当前节点
        if (!CollectionUtil.isEmpty(node.getChildren())) {
            for (EcmBusiDocRedisDTO child : node.getChildren()) {
                dfs(child, result); // 递归处理子节点
            }
        }

    }

    /**
     * 时间处理，获取两个时间间隔的所有时间
     *
     * @param startDateStr
     * @param endDateStr
     * @return
     */
    public static List<Integer> getAllDatesInRange(String startDateStr, String endDateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date startDate = null;
        try {
            startDate = sdf.parse(startDateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Date endDate = null;
        try {
            endDate = sdf.parse(endDateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        List<Integer> dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        // Add one day to endDate to include endDate in the loop
        endDate = new Date(endDate.getTime() + 24 * 60 * 60 * 1000L);

        while (calendar.getTime().before(endDate)) {
            dateList.add(Integer.parseInt(sdf.format(calendar.getTime())));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dateList;
    }

    /**
     * 过滤掉不需要展示的节点
     *
     * @param strings
     * @param docTypeNode
     * @return
     */
    public static List<EcmBusiStructureTreeDTO> isshowNode(List<String> strings, List<EcmBusiStructureTreeDTO> docTypeNode) {
        List<EcmBusiStructureTreeDTO> docTypeNode2 = new ArrayList<>();
        for (EcmBusiStructureTreeDTO node : docTypeNode) {
            if (CollectionUtil.isNotEmpty(node.getChildren())) {
                List<EcmBusiStructureTreeDTO> ecmBusiStructureTreeDTOS = isshowNode(strings, node.getChildren());
                List<EcmBusiStructureTreeDTO> collect = ecmBusiStructureTreeDTOS.stream().filter(s -> !s.isLock()).collect(Collectors.toList());
                if (CollectionUtil.isEmpty(collect)) {
                    node.setLock(true);
                }
                node.setChildren(ecmBusiStructureTreeDTOS);
                docTypeNode2.add(node);
            } else {
                if (!strings.contains(node.getDocCode())) {
                    node.setLock(true);
                }
                docTypeNode2.add(node);
            }
        }
        return docTypeNode2;
    }

    /**
     * 将无权显得节点隐藏
     *
     * @param docTypeNode
     * @return
     */
    public static List<EcmBusiStructureTreeDTO> removeTreeLock(List<EcmBusiStructureTreeDTO> docTypeNode) {
        List<EcmBusiStructureTreeDTO> docTypeNode2 = new ArrayList<>();
        for (EcmBusiStructureTreeDTO node : docTypeNode) {

            if (CollectionUtil.isNotEmpty(node.getChildren())) {
                List<EcmBusiStructureTreeDTO> ecmBusiStructureTreeDTOS = removeTreeLock(node.getChildren());
                if (!org.apache.commons.collections4.CollectionUtils.isEmpty(ecmBusiStructureTreeDTOS)) {
                    if (IcmsConstants.TREE_TYPE_DOCCODE.equals(ecmBusiStructureTreeDTOS.get(0).getType())) {
                        node.setChildren(ecmBusiStructureTreeDTOS);
                        if (!node.isLock()) {
                            Integer total = 0;
                            for (EcmBusiStructureTreeDTO dto : ecmBusiStructureTreeDTOS) {
                                total = total + dto.getFileCount();
                            }
                            node.setFileCount(total);
                            docTypeNode2.add(node);
                        }
                    } else {
                        docTypeNode2.add(node);
                    }

                }
            } else {
                if (!node.isLock()) {
                    docTypeNode2.add(node);
                }
            }

        }
        return docTypeNode2;
    }

    /**
     * @param list
     * @return
     */
    public static int findMostFrequent(List<String> list) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        int maxFrequency = 0;

        for (String num : list) {
            frequencyMap.put(num, frequencyMap.getOrDefault(num, 0) + 1);
            if (frequencyMap.get(num) > maxFrequency) {
                maxFrequency = frequencyMap.get(num);
            }
        }

        return maxFrequency;
    }


    public static List<Integer> getLastNYears(int n) {
        List<Integer> years = new ArrayList<>();
        int currentYear = Year.now().getValue(); // 获取当前年份

        for (int i = 0; i < n; i++) {
            years.add(currentYear - i); // 将当前年份及其前 N-1 年添加到列表中
        }

        return years;
    }

    public static String getExt(EcmFileInfoDTO ecmFileInfoDTO) {
        if(ecmFileInfoDTO.getNewFileName()!=null) {
            int lastIndex = ecmFileInfoDTO.getNewFileName().lastIndexOf('.');
            // 提取文件扩展名
            return ecmFileInfoDTO.getNewFileName().substring(lastIndex + 1);
        }else return null;
    }
}
