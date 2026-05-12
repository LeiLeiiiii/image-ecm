package com.sunyard.ecm.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.constant.BusiInfoConstants;
import com.sunyard.ecm.constant.RedisConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.statistics.EcmBusiStatisticsDTO;
import com.sunyard.ecm.dto.ecm.statistics.EcmStatisticsDTO;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmBusiStatisticsMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.util.EasyExcelUtils;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.redis.util.RedisUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author 朱山成
 * @time 2024/6/12 9:32
 **/
@Slf4j
@Service
public class StatisticsBusiService {
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private EcmBusiStatisticsMapper ecmBusiStatisticsMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;

    /**
     * 业务量统计-总计
     */
    public Result<EcmBusiStatisticsDTO> trafficTotal(boolean flag) {
        if (flag) {
            String s = redisUtils.get(RedisConstants.BUSI_VOLUME_STATISTICS);
            if (!ObjectUtils.isEmpty(s)) {
                EcmBusiStatisticsDTO ecmBusiStatisticsDTO = JSON.parseObject(s,
                        EcmBusiStatisticsDTO.class);
                return Result.success(ecmBusiStatisticsDTO);
            }
        }
        EcmBusiStatisticsDTO ecmBusiStatisticsDTO = ecmBusiStatisticsMapper.selectCounts();
        if (ObjectUtils.isEmpty(ecmBusiStatisticsDTO)) {
            ecmBusiStatisticsDTO = new EcmBusiStatisticsDTO();
            ecmBusiStatisticsDTO.setStorageTotal(0L).setBusiTotal(0L).setFileTotal(0L);
        }
        Long storageTotal = ecmBusiStatisticsDTO.getStorageTotal();
        double storageTotalGB = storageTotal / (double) (1024 * 1024);
        // 保留两位小数
        storageTotalGB = Math.round(storageTotalGB * 100.0) / 100.0;
        // 检查是否小于0.01
        if (storageTotalGB < 0.01) {
            storageTotalGB = 0.01;
        }
        ecmBusiStatisticsDTO.setStorageTotalGb(storageTotalGB);
        String jsonString = JSON.toJSONString(ecmBusiStatisticsDTO);
        redisUtils.set(RedisConstants.BUSI_VOLUME_STATISTICS, jsonString, 86400);
        return Result.success(ecmBusiStatisticsDTO);
    }

    /**
     * 业务量统计-柱状图/折状图
     */
    public Result<Map<String, Object>> trafficSearch(EcmStatisticsDTO ecmStatisticsDTO,
                                                     AccountTokenExtendDTO token) {
        AssertUtils.isNull(ecmStatisticsDTO.getUnit(), "参数错误");
        AssertUtils.isNull(ecmStatisticsDTO.getStartDate(), "参数错误");
        AssertUtils.isNull(ecmStatisticsDTO.getEndDate(), "参数错误");
        List<EcmBusiStatisticsDTO> list = new ArrayList<>();
        switch (ecmStatisticsDTO.getUnit()) {
            case 0:
                // 天
                list = ecmBusiStatisticsMapper.selectCountsDay(ecmStatisticsDTO.getOrgCodes(),
                        ecmStatisticsDTO.getAppCodes(), ecmStatisticsDTO.getStartDate(),
                        ecmStatisticsDTO.getEndDate());
                break;
            case 1:
                // 月
                list = ecmBusiStatisticsMapper.selectCountsMoon(ecmStatisticsDTO.getOrgCodes(),
                        ecmStatisticsDTO.getAppCodes(), ecmStatisticsDTO.getStartDate(),
                        ecmStatisticsDTO.getEndDate());
                break;
            case 2:
                // 年
                list = ecmBusiStatisticsMapper.selectCountsYear(ecmStatisticsDTO.getOrgCodes(),
                        ecmStatisticsDTO.getAppCodes(), ecmStatisticsDTO.getStartDate(),
                        ecmStatisticsDTO.getEndDate());
                break;
            default:
                break;
        }
        // 统计数据为空，直接返回
        if (CollUtil.isEmpty(list)) {
            return Result.success(new HashMap<>(1));
        }

        Map<String, Object> map = new HashMap<>();
        Set<String> appCodes = list.stream().map(EcmBusiStatisticsDTO::getAppCode)
                .collect(Collectors.toSet());
        List<EcmAppDef> appCodeList = ecmAppDefMapper.selectList(
                new LambdaQueryWrapper<EcmAppDef>().in(EcmAppDef::getAppCode, appCodes));
        Map<String, List<EcmAppDef>> collect = appCodeList.stream()
                .collect(Collectors.groupingBy(EcmAppDef::getAppCode));
        Map<String, List<EcmBusiStatisticsDTO>> maps = list.stream()
                .collect(Collectors.groupingBy(EcmBusiStatisticsDTO::getCreateDate));
        // TreeMap会自动按key排序
        List<Map<String, Object>> mapList = getMaps(maps, collect);
        //填充空数据
        SimpleDateFormat sdf = getSimpleDateFormat(ecmStatisticsDTO);
        List<LocalDate> allDates = generateDateRange(
                convertDateToLocalDate(ecmStatisticsDTO.getStartDate()),
                convertDateToLocalDate(ecmStatisticsDTO.getEndDate()), ecmStatisticsDTO.getUnit());
        for (LocalDate date : allDates) {
            String formattedDate = sdf
                    .format(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            List<EcmBusiStatisticsDTO> dto = maps.getOrDefault(formattedDate, new ArrayList<>());
            if (CollectionUtils.isEmpty(dto)) {
                List<Map<String, Object>> map2 = new ArrayList<>();
                Map<String, Object> map3 = new HashMap<>();
                Map<String, Object> map4 = new HashMap<>();
                map4.put("appCode", "");
                map4.put("appName", "");
                map4.put("value", "");
                map2.add(map4);
                map3.put("date", formattedDate);
                map3.put("busi", map2);
                mapList.add(map3);
            }
        }
        //排序
        List<Map<String, Object>> sortedMapList = mapList.stream()
                .sorted(Comparator.comparing(m -> {
                    String date = (String) m.get("date");
                    return parseDate(date, sdf); // 解析日期
                })).map(sortMap -> {
                    //截取日期 去掉年份前端不展示年份
                    String date = (String) sortMap.get("date");
                    // 判断日期格式是否为 "yyyy-MM-dd"
                    if (ecmStatisticsDTO.getUnit() == 0) {
                        sortMap.put("date", date.substring(5)); // 更新 map
                    }
                    // 如果日期格式不是完整的年月日格式，直接返回原始 map
                    return sortMap;
                }).collect(Collectors.toList());

        map.put("busiList", sortedMapList);
        map.put("appCodeList", appCodeList);

        return Result.success(map);
    }

    // 根据不同的日期格式解析日期
    private static Date parseDate(String dateStr, SimpleDateFormat format) {
        try {
            return format.parse(dateStr); // 尝试解析 MM-dd 格式
        } catch (Exception e1) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr); // 如果都解析失败
        }
    }

    private List<Map<String, Object>> getMaps(Map<String, List<EcmBusiStatisticsDTO>> maps,
                                              Map<String, List<EcmAppDef>> collect) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        maps.forEach((createDate, busiStatisticsDTOList) -> {
            List<Map<String, Object>> map2 = new ArrayList<>();
            Map<String, Object> map3 = new HashMap<>();
            for (EcmBusiStatisticsDTO busiStatisticsDTO : busiStatisticsDTOList) {
                Map<String, Object> map4 = new HashMap<>();
                map4.put("appCode", busiStatisticsDTO.getAppCode());
                List<EcmAppDef> ecmAppDefs = collect.get(busiStatisticsDTO.getAppCode());
                map4.put("appName", ecmAppDefs.get(0).getAppName());
                map4.put("value", busiStatisticsDTO.getCount());
                map2.add(map4);
            }
            map3.put("date", createDate);
            map3.put("busi", map2);
            mapList.add(map3);
        });
        return mapList;
    }

    /**
     * 业务量统计-表格
     */
    public Result trafficForm(EcmStatisticsDTO ecmStatisticsDTO, AccountTokenExtendDTO token) {
        AssertUtils.isNull(ecmStatisticsDTO.getPageNum(), "参数错误");
        AssertUtils.isNull(ecmStatisticsDTO.getPageSize(), "参数错误");
        AssertUtils.isNull(ecmStatisticsDTO.getStartDate(), "开始时间不可为空!");
        AssertUtils.isNull(ecmStatisticsDTO.getEndDate(), "结束时间不可为空!");
        PageHelper.startPage(ecmStatisticsDTO.getPageNum(), ecmStatisticsDTO.getPageSize());
        List<EcmBusiStatisticsDTO> ecmBusiStatistics = ecmBusiStatisticsMapper
                .selectEcmBusiStatisticsDTOList(ecmStatisticsDTO.getOrgCodes(),
                        ecmStatisticsDTO.getAppCodes(), ecmStatisticsDTO.getStartDate(),
                        ecmStatisticsDTO.getEndDate(), ecmStatisticsDTO.getSortColumnDbName(),
                        ecmStatisticsDTO.getSortRule());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        ecmBusiStatistics.forEach(s -> {
            s.setAppName("(" + s.getAppCode() + ")" + s.getAppName());
            s.setFileSize(Double.valueOf(String.format("%.4f", (s.getFileSize() / (1024 * 1024)))));
            s.setCreateDate(sdf.format(s.getCreateTime()));
        });
        return Result.success(new PageInfo<>(ecmBusiStatistics));
    }

    /**
     * 业务量统计-表格
     */
    public Result trafficFormTotal(EcmStatisticsDTO ecmStatisticsDTO, AccountTokenExtendDTO token) {
        AssertUtils.isNull(ecmStatisticsDTO.getPageNum(), "参数错误");
        AssertUtils.isNull(ecmStatisticsDTO.getPageSize(), "参数错误");
        AssertUtils.isNull(ecmStatisticsDTO.getStartDate(), "开始时间不可为空!");
        AssertUtils.isNull(ecmStatisticsDTO.getEndDate(), "结束时间不可为空!");
        List<EcmBusiStatisticsDTO> ecmBusiStatistics = ecmBusiStatisticsMapper
                .selectEcmBusiStatisticsDTOList(ecmStatisticsDTO.getOrgCodes(),
                        ecmStatisticsDTO.getAppCodes(), ecmStatisticsDTO.getStartDate(),
                        ecmStatisticsDTO.getEndDate(), ecmStatisticsDTO.getSortColumnDbName(),
                        ecmStatisticsDTO.getSortRule());
        ecmBusiStatistics.forEach(s -> {
            s.setAppName("(" + s.getAppCode() + ")" + s.getAppName());
            s.setFileSize(Double.valueOf(String.format("%.4f", (s.getFileSize() / (1024 * 1024)))));
        });
        // 根据时间维度选择分组规则
        SimpleDateFormat sdf = getSimpleDateFormat(ecmStatisticsDTO);
        // 生成日期范围
        List<LocalDate> allDates = generateDateRange(
                convertDateToLocalDate(ecmStatisticsDTO.getStartDate()),
                convertDateToLocalDate(ecmStatisticsDTO.getEndDate()), ecmStatisticsDTO.getUnit());
        //查询时业务和机构拼接
        String searchOrgCodes = String.join(";", ecmStatisticsDTO.getOrgCodes());
        if ("".equals(searchOrgCodes)) {
            searchOrgCodes = "全部机构";
        }
        //拼接查询业务类型名称
        String searchAppNames = "";
        if (CollectionUtil.isNotEmpty(ecmStatisticsDTO.getAppCodes())) {
            List<EcmAppDef> appCodeList = ecmAppDefMapper
                    .selectList(new LambdaQueryWrapper<EcmAppDef>().in(EcmAppDef::getAppCode,
                            ecmStatisticsDTO.getAppCodes()));
            searchAppNames = appCodeList.stream()
                    .map(app -> String.format("(%s)%s", app.getAppCode(), app.getAppName()))
                    .collect(Collectors.joining(";"));
        } else {
            searchAppNames = "全部业务";
        }
        //判断是否勾选了全部的业务
        if (ecmStatisticsDTO.getAppCodeSize() != null
                && ecmStatisticsDTO.getAppCodeSize() == ecmStatisticsDTO.getAppCodes().size()) {
            searchAppNames = "全部业务";
        }
        List<EcmBusiStatisticsDTO> result = getEcmBusiStatisticsDTOS(ecmBusiStatistics, sdf,
                allDates, searchOrgCodes, searchAppNames);
        if (ecmStatisticsDTO.isSortable()) {
            String sortColumn = ecmStatisticsDTO.getSortColumn();
            String sortRule = ecmStatisticsDTO.getSortRule();
            Comparator<EcmBusiStatisticsDTO> comparator = null;
            switch (sortColumn) {
                case "daySize":
                    comparator = Comparator.comparing(EcmBusiStatisticsDTO::getDaySize);
                    break;
                case "fileNumber":
                    comparator = Comparator.comparing(EcmBusiStatisticsDTO::getFileNumber);
                    break;
                case "fileSize":
                    comparator = Comparator.comparingDouble(EcmBusiStatisticsDTO::getFileSize);
                    break;
                case "createDate":
                    comparator = Comparator.comparing(EcmBusiStatisticsDTO::getCreateDate);
                    break;
            }
            if (comparator != null) {
                if ("desc".equalsIgnoreCase(sortRule)) {
                    comparator = comparator.reversed();
                }
                result = result.stream().sorted(comparator).collect(Collectors.toList());
            }
        }
        //全部数据汇总数据
        EcmBusiStatisticsDTO totals = getTotalInfos(ecmStatisticsDTO, sdf, searchOrgCodes,
                searchAppNames, result);
        //手动分页
        int total = result.size();
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageSize(ecmStatisticsDTO.getPageSize());
        pageInfo.setPageNum(ecmStatisticsDTO.getPageNum());
        int startIndex = (ecmStatisticsDTO.getPageNum() - 1) * ecmStatisticsDTO.getPageSize();
        int endIndex = Math.min(startIndex + ecmStatisticsDTO.getPageSize(), total);
        pageInfo.setTotal(total);
        result = result.subList(startIndex, endIndex);
        pageInfo.setList(result);
        Map<String, Object> map = new HashMap<>();
        map.put("result", pageInfo);
        map.put("total", totals);
        return Result.success(map);
    }

    private EcmBusiStatisticsDTO getTotalInfos(EcmStatisticsDTO ecmStatisticsDTO,
                                               SimpleDateFormat sdf, String searchOrgCodes,
                                               String searchAppNames,
                                               List<EcmBusiStatisticsDTO> result) {
        long totalDaySizes = result.stream().mapToLong(EcmBusiStatisticsDTO::getDaySize).sum();
        long totalFileNumbers = result.stream().mapToLong(EcmBusiStatisticsDTO::getFileNumber)
                .sum();
        double totalFileSizes = result.stream().mapToDouble(EcmBusiStatisticsDTO::getFileSize)
                .sum();
        //        String totalAppNames = ecmBusiStatistics.stream().map(EcmBusiStatisticsDTO::getAppName).distinct().collect(Collectors.joining(";"));
        //        String totalOrgCodes = ecmBusiStatistics.stream().map(EcmBusiStatisticsDTO::getOrgCode).distinct().collect(Collectors.joining(";"));
        String totalCreateDate = sdf.format(ecmStatisticsDTO.getStartDate()) + "~"
                + sdf.format(ecmStatisticsDTO.getEndDate());
        EcmBusiStatisticsDTO totals = new EcmBusiStatisticsDTO();
        totals.setAppName(searchAppNames);
        totals.setOrgCode(searchOrgCodes);
        totals.setCreateDate(totalCreateDate);
        totals.setDaySize(totalDaySizes);
        totals.setFileNumber(totalFileNumbers);
        totals.setFileSize(Double.valueOf(String.format("%.4f", totalFileSizes)));
        return totals;
    }

    private List<EcmBusiStatisticsDTO> getEcmBusiStatisticsDTOS(List<EcmBusiStatisticsDTO> ecmBusiStatistics,
                                                                SimpleDateFormat sdf,
                                                                List<LocalDate> allDates,
                                                                String searchOrgCodes,
                                                                String searchAppNames) {
        Map<String, List<EcmBusiStatisticsDTO>> collect = ecmBusiStatistics.stream()
                .collect(Collectors.groupingBy(data -> sdf.format(data.getCreateTime())));
        List<EcmBusiStatisticsDTO> result = new ArrayList<>();
        for (LocalDate date : allDates) {
            String formattedDate = sdf
                    .format(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            List<EcmBusiStatisticsDTO> dtos = collect.getOrDefault(formattedDate,
                    new ArrayList<>());

            EcmBusiStatisticsDTO ecmBusiStatisticsDTO = new EcmBusiStatisticsDTO();

            // 如果没有数据，填充零
            if (dtos.isEmpty()) {
                ecmBusiStatisticsDTO.setAppName(searchAppNames);
                ecmBusiStatisticsDTO.setOrgCode(searchOrgCodes);
                ecmBusiStatisticsDTO.setCreateDate(formattedDate);
                ecmBusiStatisticsDTO.setDaySize(0L);
                ecmBusiStatisticsDTO.setFileNumber(0L);
                ecmBusiStatisticsDTO.setFileSize(0d);
                result.add(ecmBusiStatisticsDTO);
                continue;
            }

            //            String appNames = dtos.stream().map(EcmBusiStatisticsDTO::getAppName).distinct().collect(Collectors.joining(";"));
            //            String orgCodes = dtos.stream().map(EcmBusiStatisticsDTO::getOrgCode).distinct().collect(Collectors.joining(";"));
            long daySizes = dtos.stream().mapToLong(EcmBusiStatisticsDTO::getDaySize).sum();
            long fileNumbers = dtos.stream().mapToLong(EcmBusiStatisticsDTO::getFileNumber).sum();
            double fileSizes = dtos.stream().mapToDouble(EcmBusiStatisticsDTO::getFileSize).sum();

            ecmBusiStatisticsDTO.setAppName(searchAppNames);
            ecmBusiStatisticsDTO.setOrgCode(searchOrgCodes);
            ecmBusiStatisticsDTO.setCreateDate(formattedDate);
            ecmBusiStatisticsDTO.setDaySize(daySizes);
            ecmBusiStatisticsDTO.setFileNumber(fileNumbers);
            ecmBusiStatisticsDTO.setFileSize(Double.valueOf(String.format("%.4f", fileSizes)));

            result.add(ecmBusiStatisticsDTO);
        }
        return result;
    }

    // 将 Date 转换为 LocalDate
    private LocalDate convertDateToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // 生成时间范围（按天、月或年）
    private List<LocalDate> generateDateRange(LocalDate startDate, LocalDate endDate, int unit) {
        List<LocalDate> dateRange = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            dateRange.add(currentDate);
            if (unit == 0) { // 按天
                currentDate = currentDate.plusDays(1);
            } else if (unit == 1) { // 按月
                currentDate = currentDate.plusMonths(1);
            } else { // 按年
                currentDate = currentDate.plusYears(1);
            }
        }

        return dateRange;
    }

    /**
     * 业务量统计-导出表格
     */
    public void trafficBusiExport(HttpServletResponse response, EcmStatisticsDTO ecmStatisticsDTO,
                                  AccountTokenExtendDTO token) {
        List<EcmBusiStatisticsDTO> ecmBusiStatistics = getEcmBusiStatisticsDTOS(ecmStatisticsDTO);
        //导出明细记录
        try {
            EasyExcelUtils.writeListTo(response, ecmBusiStatistics,
                    BusiInfoConstants.BUSI_STATISTICS);
        } catch (IOException e) {
            log.error("导出失败", e);
        }
    }

    /**
     * 业务量统计-导出表格
     */
    public void trafficBusiTotalExport(HttpServletResponse response,
                                       EcmStatisticsDTO ecmStatisticsDTO,
                                       AccountTokenExtendDTO token) {
        List<EcmBusiStatisticsDTO> ecmBusiStatistics = getEcmBusiStatisticsDTOS(ecmStatisticsDTO);
        // 根据时间维度选择分组规则
        SimpleDateFormat sdf = getSimpleDateFormat(ecmStatisticsDTO);
        // 生成日期范围
        List<LocalDate> allDates = generateDateRange(
                convertDateToLocalDate(ecmStatisticsDTO.getStartDate()),
                convertDateToLocalDate(ecmStatisticsDTO.getEndDate()), ecmStatisticsDTO.getUnit());
        //查询时业务和机构拼接
        String searchOrgCodes = String.join(";", ecmStatisticsDTO.getOrgCodes());
        if ("".equals(searchOrgCodes)) {
            searchOrgCodes = "全部机构";
        }
        //拼接查询业务类型名称
        String searchAppNames = "";
        if (CollectionUtil.isNotEmpty(ecmStatisticsDTO.getAppCodes())) {
            List<EcmAppDef> appCodeList = ecmAppDefMapper
                    .selectList(new LambdaQueryWrapper<EcmAppDef>().in(EcmAppDef::getAppCode,
                            ecmStatisticsDTO.getAppCodes()));
            searchAppNames = appCodeList.stream()
                    .map(app -> String.format("(%s)%s", app.getAppCode(), app.getAppName()))
                    .collect(Collectors.joining(";"));
        } else {
            searchAppNames = "全部业务";
        }
        List<EcmBusiStatisticsDTO> result = getEcmBusiStatisticsDTOS(ecmBusiStatistics, sdf,
                allDates, searchOrgCodes, searchAppNames);
        //全部数据汇总数据
        EcmBusiStatisticsDTO totals = getTotalInfos(ecmStatisticsDTO, sdf, searchOrgCodes,
                searchAppNames, result);
        result.add(totals);
        //导出明细记录
        try {
            EasyExcelUtils.writeListTo(response, result, BusiInfoConstants.BUSI_STATISTICS);
        } catch (IOException e) {
            log.error("导出失败", e);
        }
    }

    private SimpleDateFormat getSimpleDateFormat(EcmStatisticsDTO ecmStatisticsDTO) {
        SimpleDateFormat sdf;
        // 根据期望的汇总维度选择格式
        if (ecmStatisticsDTO.getUnit() == 0) {
            sdf = new SimpleDateFormat("yyyy-MM-dd");
        } else if (ecmStatisticsDTO.getUnit() == 1) {
            sdf = new SimpleDateFormat("yyyy-MM");
        } else {
            sdf = new SimpleDateFormat("yyyy");
        }
        return sdf;
    }

    private List<EcmBusiStatisticsDTO> getEcmBusiStatisticsDTOS(EcmStatisticsDTO ecmStatisticsDTO) {
        AssertUtils.isNull(ecmStatisticsDTO.getStartDate(), "开始时间不可为空!");
        AssertUtils.isNull(ecmStatisticsDTO.getEndDate(), "结束时间不可为空!");
        List<EcmBusiStatisticsDTO> ecmBusiStatistics = ecmBusiStatisticsMapper
                .selectEcmBusiStatisticsDTOList(ecmStatisticsDTO.getOrgCodes(),
                        ecmStatisticsDTO.getAppCodes(), ecmStatisticsDTO.getStartDate(),
                        ecmStatisticsDTO.getEndDate(), ecmStatisticsDTO.getSortColumnDbName(),
                        ecmStatisticsDTO.getSortRule());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        ecmBusiStatistics.forEach(s -> {
            s.setAppName("(" + s.getAppCode() + ")" + s.getAppName());
            s.setFileSize(Double.valueOf(String.format("%.4f", (s.getFileSize() / (1024 * 1024)))));
            s.setCreateDate(sdf.format(s.getCreateTime()));
        });
        return ecmBusiStatistics;
    }

    /**
     * 驼峰命名转下划线命名
     * @param camelCase 驼峰命名的字符串
     * @return 下划线命名的字符串
     */
    private String camelToSnake(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }

        StringBuilder result = new StringBuilder();
        char[] chars = camelCase.toCharArray();

        for (char c : chars) {
            if (Character.isUpperCase(c)) {
                result.append("_");
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * 业务量统计-饼图
     */
    public Result trafficPie(EcmStatisticsDTO ecmStatisticsDTO, AccountTokenExtendDTO token) {
        List<EcmBusiStatisticsDTO> ecmBusiStatistics = ecmBusiStatisticsMapper.trafficPie(
                ecmStatisticsDTO.getOrgCodes(), ecmStatisticsDTO.getAppCodes(),
                ecmStatisticsDTO.getStartDate(), ecmStatisticsDTO.getEndDate());
        if (CollectionUtils.isEmpty(ecmBusiStatistics)) {
            return Result.success(new ArrayList<>());
        }
        //赋值appName
        // 1. 获取所有的 appCode 列表
        Set<String> appCodes = ecmBusiStatistics.stream().map(EcmBusiStatisticsDTO::getAppCode)
                .collect(Collectors.toSet());

        // 2. 批量查询 appCode 对应的 appName（假设 AppInfoMapper 有批量查询方法）
        List<EcmAppDef> appInfoList = ecmAppDefMapper.selectList(
                new LambdaQueryWrapper<EcmAppDef>().in(EcmAppDef::getAppCode, appCodes));

        // 3. 将 appCode 和 appName 映射到一个 Map 中
        Map<String, String> appNameMap = appInfoList.stream()
                .collect(Collectors.toMap(EcmAppDef::getAppCode, EcmAppDef::getAppName));

        // 4. 遍历 ecmBusiStatistics 并设置 appName
        for (EcmBusiStatisticsDTO dto : ecmBusiStatistics) {
            String appCode = dto.getAppCode();
            // 从 Map 中获取 appName
            String appName = appNameMap.get(appCode);
            if (appName != null) {
                dto.setAppName(appName);
            } else {
                dto.setAppName("");
            }
        }
        return Result.success(ecmBusiStatistics);
    }
}
