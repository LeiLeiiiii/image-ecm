package com.sunyard.ecm.util;

import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.ecm.SysStrategyDTO;
import com.sunyard.ecm.enums.EcmCheckAsyncTaskEnum;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author yzy
 * @desc 智能化处理工具类
 * @since 2026/2/3
 */
public class CheckDetectionUtils {

    /**
     * 根据开关状态获取taskType
     */
    public static String getTaskType(Map<Integer, Boolean> result, SysStrategyDTO vo, List<String> enumConfigList) {
        String taskType = IcmsConstants.ASYNC_TASK_STATUS_INIT;
        //获取菜单状态  0:开启  1：关闭
        Integer duplicateStatus = enumConfigList.contains(IcmsConstants.DUPLICATE_CHECK_STATUS) ? IcmsConstants.ZERO : IcmsConstants.ONE;
        Integer strategyStatus = enumConfigList.contains(IcmsConstants.STRATEGY_STATUS) ? IcmsConstants.ZERO : IcmsConstants.ONE;
        Integer regularizeStatus = enumConfigList.contains(IcmsConstants.REGULARIZE_STATUS) ? IcmsConstants.ZERO : IcmsConstants.ONE;
        Integer remakeStatus = enumConfigList.contains(IcmsConstants.REMAKE_STATUS) ? IcmsConstants.ZERO : IcmsConstants.ONE;
        Integer qualityStatus = enumConfigList.contains(IcmsConstants.QUALITY_CHECK_STATUS) ? IcmsConstants.ZERO : IcmsConstants.ONE;
        //单证识别
        //OCR识别开关
        if (vo.getOcrConfigStatus() && IcmsConstants.ZERO.equals(strategyStatus)) {
            taskType = updateStatus(taskType, IcmsConstants.TYPE_ONE,
                    EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
        }
        //判断是否开启翻拍检测
        if (!CollectionUtils.isEmpty(result)) {
            boolean isReShootEnabled = result.get(IcmsConstants.REMAKE);
            if (isReShootEnabled && IcmsConstants.ZERO.equals(remakeStatus)) {
                taskType = updateStatus(taskType, IcmsConstants.TYPE_SIX,
                        EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            }
            //判断是否开启模糊检测
            boolean isObscure = result.get(IcmsConstants.OBSCURE);
            if (isObscure && IcmsConstants.ZERO.equals(qualityStatus)) {
                taskType = updateStatus(taskType, IcmsConstants.TYPE_THREE,
                        EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            }

            //判断是否开启了转正检测
            boolean isRegularize = result.get(IcmsConstants.REGULARIZE);
            if (isRegularize && IcmsConstants.ZERO.equals(regularizeStatus)) {
                taskType = updateStatus(taskType, IcmsConstants.TYPE_TWO,
                        EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            }

            //判断是否开启查重检测
            boolean plagiarism = result.get(IcmsConstants.PLAGIARISM);
            if (plagiarism && IcmsConstants.ZERO.equals(duplicateStatus)) {
                taskType = updateStatus(taskType, IcmsConstants.TYPE_FOUR,
                        EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            }

            //判断是否开启反光检测
            boolean reflective = result.get(IcmsConstants.REFLECTIVE);
            if (reflective && IcmsConstants.ZERO.equals(qualityStatus)) {
                taskType = updateStatus(taskType, IcmsConstants.TYPE_EIGHT,
                        EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            }

            //判断是否开启缺角检测
            boolean missCorner = result.get(IcmsConstants.MISS_CORNER);
            if (missCorner && IcmsConstants.ZERO.equals(qualityStatus)) {
                taskType = updateStatus(taskType, IcmsConstants.TYPE_NINE,
                        EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            }
            //判断是否开启文本查重
            boolean plagiarismText = result.get(IcmsConstants.PLAGIARISM_TEXT);
            if (plagiarismText && IcmsConstants.ZERO.equals(duplicateStatus)){
                taskType = updateStatus(taskType, IcmsConstants.TYPE_TEN,
                        EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            }
        }
        return taskType;
    }

    /**
     * 更新 RemakeStatus 指定位置的值
     * @param status   原始状态字符串
     * @param position 需要更新的位置
     * @param newValue
     * @return 更新后的状态字符串
     */
    public static String updateStatus(String status, Integer position, char newValue) {
        if (position < 1 || position > IcmsConstants.LENGTH) {
            throw new IllegalArgumentException(
                    String.format("只能更新 1 到 %d 位", IcmsConstants.LENGTH)
            );
        }
        StringBuilder sb = new StringBuilder(status);
        sb.setCharAt(position - 1, newValue);
        return sb.toString();

    }
}
