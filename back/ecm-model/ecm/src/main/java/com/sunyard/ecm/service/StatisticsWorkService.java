package com.sunyard.ecm.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.ecm.statistics.EcmWorkStatisticsDTO;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmBusiStatisticsMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.mapper.EcmUserBusiFileStatisticsMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmBusiStatistics;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.po.EcmUserBusiFileStatistics;
import com.sunyard.ecm.util.CommonUtils;
import com.sunyard.ecm.util.EasyExcelUtils;
import com.sunyard.ecm.util.PageInfoUtils;
import com.sunyard.ecm.vo.EcmStatisticsVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.date.DateUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.InstApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysInstDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author 朱山成
 * @time 2024/6/12 9:32
 **/
@Slf4j
@Service
public class StatisticsWorkService {
    @Value("${statistics.day:90}")
    private Integer statisticsDays;
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private EcmBusiStatisticsMapper ecmBusiStatisticsMapper;
    @Resource
    private EcmUserBusiFileStatisticsMapper ecmUserBusiFileStatisticsMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private InstApi instApi;
    @Resource
    private StatisticsBusiService statisticsBusiService;

    /**
     * 统计分析定时任务
     */
    public void ecmStatisticAnalysisTask() {
        updateThatDays();

        //定时任务凌晨触发，统计前一天的数据
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Date date = DateUtils.parseDate(yesterday);
        // 查询当天 新增的业务
        List<EcmBusiInfo> ecmBusiInfoList = ecmBusiInfoMapper
                .selectList(new LambdaQueryWrapper<EcmBusiInfo>().between(EcmBusiInfo::getCreateTime,
                        DateUtils.getDayBeginTime(date), DateUtils.getDayEndTime(date)));
        if (!CollectionUtils.isEmpty(ecmBusiInfoList)) {
            List<EcmBusiStatistics> ecmBusiStatisticsList = new ArrayList<>();
            // 按appCode和orgCode进行分组
            Map<String, Map<String, List<EcmBusiInfo>>> groupedByAppAndOrg = ecmBusiInfoList
                    .stream().collect(Collectors.groupingBy(EcmBusiInfo::getAppCode,
                            Collectors.groupingBy(EcmBusiInfo::getOrgCode)));
            // 获取当前时间
            String formattedDate = DateUtils.dateToString(date, "yyyyMMdd");
            saveBusiStatistics(groupedByAppAndOrg, formattedDate, ecmBusiStatisticsList);
            // 当天的数据插入数据库
            if (!CollectionUtils.isEmpty(ecmBusiStatisticsList)) {
                insertEcmBusiStatisticss(ecmBusiStatisticsList);
                //ecmBusiStatisticsMapper.insertBatch(ecmBusiStatisticsList);
            }
        }
        // 计算总数 存入缓存
        statisticsBusiService.trafficTotal(false);
    }

    private void insertEcmBusiStatisticss(List<EcmBusiStatistics> ecmBusiStatisticsList) {
        MybatisBatch<EcmBusiStatistics> mybatisBatch = new MybatisBatch<>(sqlSessionFactory,
                ecmBusiStatisticsList);
        MybatisBatch.Method<EcmBusiStatistics> method = new MybatisBatch.Method<>(
                EcmBusiStatisticsMapper.class);
        mybatisBatch.execute(method.insert());
    }

    /**
     * 用户工作量统计定时任务
     */
    public void ecmUserStatisticAnalysisTask() {
        updateThatDaysUserStatis();
        //定时任务凌晨触发，统计前一天的数据
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Date date = DateUtils.parseDate(yesterday);
        //先判断是否统计过前一天的数据，避免多次触发定时任务（手动）
        List<EcmUserBusiFileStatistics> ecmUserBusiFileStatistics = ecmUserBusiFileStatisticsMapper
                .selectList(new LambdaUpdateWrapper<EcmUserBusiFileStatistics>().eq(
                        EcmUserBusiFileStatistics::getStatsDate,
                        Integer.valueOf(DateUtils.dateToString(date, "yyyyMMdd"))));
        if (CollectionUtils.isEmpty(ecmUserBusiFileStatistics)) {
            saveWorkStatistaicUser(date);
        }
    }

    private void saveWorkStatistaicUser(Date date) {
        String s = DateUtils.dateToString(date, DateUtils.YYYY_MM_DD);
        List<EcmFileInfoDTO> ecmFileInfos = ecmFileInfoMapper.selectFileAndAppcode(s + " 00:00:00",
                s + " 23:59:59");
        if (!CollectionUtils.isEmpty(ecmFileInfos)) {
            Set<String> collect1 = ecmFileInfos.stream().map(EcmFileInfoDTO::getCreateUser)
                    .collect(Collectors.toSet());
            ArrayList<String> strings = new ArrayList<>(collect1);
            List<SysUserDTO> sysUserDTOS = userApi.queryInstCodeByUsername(strings);
            if (!CollectionUtils.isEmpty(sysUserDTOS)) {
                Map<String, List<SysUserDTO>> collect2 = sysUserDTOS.stream()
                        .collect(Collectors.groupingBy(SysUserDTO::getLoginName));
                //根据用户分类
                Map<String, List<EcmFileInfoDTO>> collect = ecmFileInfos.stream()
                        .collect(Collectors.groupingBy(EcmFileInfoDTO::getCreateUser));
                List<EcmUserBusiFileStatistics> ecmStatisticsDTO = new ArrayList<>();
                for (String username : collect.keySet()) {
                    List<EcmFileInfoDTO> ecmFileInfos1 = collect.get(username);
                    if (!CollectionUtils.isEmpty(ecmFileInfos1)) {
                        List<SysUserDTO> sysUserDTOS1 = collect2.get(username);
                        //根据业务分类
                        Map<String, List<EcmFileInfoDTO>> collect3 = ecmFileInfos1.stream()
                                .filter(m -> m.getAppCode() != null)
                                .collect(Collectors.groupingBy(EcmFileInfoDTO::getAppCode));

                        for (String appCode : collect3.keySet()) {
                            List<EcmFileInfoDTO> ecmFileInfoDTOS = collect3.get(appCode);
                            if (!CollectionUtils.isEmpty(ecmFileInfoDTOS)) {
                                EcmFileInfoDTO ecmFileInfoDTO = ecmFileInfoDTOS.get(0);

                                EcmUserBusiFileStatistics statistics = new EcmUserBusiFileStatistics();
                                long fileId = snowflakeUtil.nextId();

                                if (!CollectionUtils.isEmpty(sysUserDTOS1)) {
                                    statistics.setOrgCode(sysUserDTOS1.get(0).getInstNo());
                                    statistics.setCreateUser(
                                            sysUserDTOS1.get(0).getName() + "(" + username + ")");
                                    statistics.setStatsUser(username);
                                } else {
                                    statistics.setStatsUser(ecmFileInfoDTO.getCreateUser());
                                    statistics.setOrgCode(ecmFileInfoDTO.getOrgCode());
                                    statistics.setCreateUser(ecmFileInfoDTO.getCreateUserName()
                                            + "(" + ecmFileInfoDTO.getCreateUser() + ")");
                                }
                                statistics.setFileNumber(
                                        Long.parseLong(String.valueOf(ecmFileInfoDTOS.size())));
                                statistics.setAppCode(appCode);
                                statistics.setCreateTime(date);
                                statistics.setStatsDate(
                                        Integer.valueOf(DateUtils.dateToString(date, "yyyyMMdd")));
                                statistics.setId(fileId);
                                ecmStatisticsDTO.add(statistics);
                            }

                        }
                    }
                }
                if (!CollectionUtils.isEmpty(ecmStatisticsDTO)) {
                    insertEcmUserBusiFileStatisticss(ecmStatisticsDTO);
                    //ecmUserBusiFileStatisticsMapper.insertBatch(ecmStatisticsDTO);
                }
            }
        }
    }

    private void updateThatDaysUserStatis() {
        //统计N天内的数据，假设当前没有数据则进行统计
        LocalDate pastDate = LocalDate.now().minusDays(statisticsDays + 1);
        LocalDate yesterday = LocalDate.now().minusDays(2);

        Date startDate = DateUtils.parseDate(pastDate);
        Date endDate = DateUtils.parseDate(yesterday);
        String startD = DateUtils.dateToString(startDate, "yyyyMMdd");
        String endD = DateUtils.dateToString(endDate, "yyyyMMdd");

        List<Integer> allDatesInRange = CommonUtils.getAllDatesInRange(startD, endD);
        List<EcmUserBusiFileStatistics> ecmUserBusiFileStatistics = ecmUserBusiFileStatisticsMapper
                .selectList(new LambdaUpdateWrapper<EcmUserBusiFileStatistics>()
                        .in(EcmUserBusiFileStatistics::getStatsDate, allDatesInRange));
        Set<Integer> collect = ecmUserBusiFileStatistics.stream()
                .map(EcmUserBusiFileStatistics::getStatsDate).collect(Collectors.toSet());
        List<Integer> collect1 = allDatesInRange.stream().filter(s -> !collect.contains(s))
                .collect(Collectors.toList());
        for (Integer i : collect1) {
            Date date = DateUtils.dateTime("yyyyMMdd", i.toString());
            saveWorkStatistaicUser(date);
        }
    }

    private void insertEcmUserBusiFileStatisticss(List<EcmUserBusiFileStatistics> ecmStatisticsDTO) {
        MybatisBatch<EcmUserBusiFileStatistics> mybatisBatch = new MybatisBatch<>(sqlSessionFactory,
                ecmStatisticsDTO);
        MybatisBatch.Method<EcmUserBusiFileStatistics> method = new MybatisBatch.Method<>(
                EcmUserBusiFileStatisticsMapper.class);
        mybatisBatch.execute(method.insert());
    }

    private long getTotalNewFileSizeKB(long totalNewFileSize) {
        long totalNewFileSizeKB = 0L;
        if (totalNewFileSize > 0 && totalNewFileSize <= 1024) {
            totalNewFileSizeKB = 1L;
        } else if (totalNewFileSize > 1024) {
            BigDecimal b = new BigDecimal(totalNewFileSize);
            BigDecimal kb = new BigDecimal(1024);
            BigDecimal divide = b.divide(kb, 0, BigDecimal.ROUND_HALF_UP);
            totalNewFileSizeKB = divide.longValue();
        }
        return totalNewFileSizeKB;
    }

    /**
     * 更新过去n天的数据
     */
    public void updateThatDays() {
        //更新过去 n 天的数据 进行更新
        LocalDate pastDate = LocalDate.now().minusDays(statisticsDays + 1);
        LocalDate yesterday = LocalDate.now().minusDays(2);
        // 转换为 Date 类型
        // 转换为 java.sql.Date 类型
        Date startDate = DateUtils.parseDate(pastDate);
        Date endDate = DateUtils.parseDate(yesterday);
        List<EcmBusiInfo> ecmBusiInfoList2 = ecmBusiInfoMapper
                .selectList(new LambdaQueryWrapper<EcmBusiInfo>().between(EcmBusiInfo::getCreateTime,
                        DateUtils.getDayBeginTime(startDate), DateUtils.getDayEndTime(endDate)));
        Map<String, List<EcmBusiInfo>> collect = new HashMap<>();
        for (EcmBusiInfo d : ecmBusiInfoList2) {
            Date createTime = d.getCreateTime();
            String startD = DateUtils.dateToString(createTime, "yyyyMMdd");
            List<EcmBusiInfo> ecmBusiInfos1 = collect.get(startD);
            if (CollectionUtils.isEmpty(ecmBusiInfos1)) {
                ecmBusiInfos1 = new ArrayList<>();
                ecmBusiInfos1.add(d);
            } else {
                ecmBusiInfos1.add(d);
            }
            collect.put(startD, ecmBusiInfos1);
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        for (String date : collect.keySet()) {
            List<EcmBusiInfo> ecmBusiInfoList3 = collect.get(date);
            if (!CollectionUtils.isEmpty(ecmBusiInfoList3)) {
                List<EcmBusiStatistics> ecmBusiStatisticsList = new ArrayList<>();
                // 按appCode和orgCode进行分组
                Map<String, Map<String, List<EcmBusiInfo>>> groupedByAppAndOrg = ecmBusiInfoList3
                        .stream().filter(s -> s.getAppCode() != null && s.getOrgCode() != null)
                        .collect(Collectors.groupingBy(EcmBusiInfo::getAppCode,
                                Collectors.groupingBy(EcmBusiInfo::getOrgCode)));
                String formattedDate = date;
                saveBusiStatistics(groupedByAppAndOrg, formattedDate, ecmBusiStatisticsList);
                //之前每天的数据插入数据库
                if (!CollectionUtils.isEmpty(ecmBusiStatisticsList)) {
                    List<String> collect1 = ecmBusiStatisticsList.stream()
                            .map(EcmBusiStatistics::getAppCode).collect(Collectors.toList());
                    List<EcmBusiStatistics> ecmBusiStatistics = ecmBusiStatisticsMapper
                            .selectList(new LambdaUpdateWrapper<EcmBusiStatistics>()
                                    .in(EcmBusiStatistics::getAppCode, collect1));
                    ArrayList<EcmBusiStatistics> update = new ArrayList<>();
                    ArrayList<EcmBusiStatistics> add = new ArrayList<>();

                    for (EcmBusiStatistics l : ecmBusiStatisticsList) {
                        boolean flag = false;
                        for (EcmBusiStatistics d : ecmBusiStatistics) {
                            if (l.getAppCode().equals(d.getAppCode())
                                    && l.getOrgCode().equals(d.getOrgCode())
                                    && l.getStatsDate().equals(d.getStatsDate())) {
                                update.add(l);
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            add.add(l);
                        }
                    }
                    if (!CollectionUtils.isEmpty(add)) {
                        ecmBusiStatisticsMapper.insertBatch(add);

                    }
                    if (!CollectionUtils.isEmpty(update)) {
                        ecmBusiStatisticsMapper.updateBatch(update);
                    }
                }
            }
        }
    }

    /**
     * 统一的逻辑处理
     */
    private void saveBusiStatistics(Map<String, Map<String, List<EcmBusiInfo>>> groupedByAppAndOrg,
                                    String formattedDate,
                                    List<EcmBusiStatistics> ecmBusiStatisticsList) {
        groupedByAppAndOrg.forEach((appCode, orgMap) -> {
            orgMap.forEach((orgCode, list) -> {
                EcmBusiStatistics ecmBusiStatistics = new EcmBusiStatistics();
                ecmBusiStatistics.setAppCode(appCode);
                ecmBusiStatistics.setOrgCode(orgCode);
                ecmBusiStatistics.setBusiNumber(Long.valueOf(list.size()));
                List<Long> busiIds = list.stream().map(EcmBusiInfo::getBusiId)
                        .collect(Collectors.toList());
                List<EcmFileInfo> ecmFileInfos = ecmFileInfoMapper
                        .selectList(new LambdaQueryWrapper<EcmFileInfo>().in(EcmFileInfo::getBusiId, busiIds));
                if (CollectionUtils.isEmpty(ecmFileInfos)) {
                    ecmBusiStatistics.setFileNumber(0L);
                } else {
                    ecmBusiStatistics.setFileNumber(Long.valueOf(ecmFileInfos.size()));
                }
                long totalNewFileSize = ecmFileInfos.stream().mapToLong(EcmFileInfo::getNewFileSize)
                        .sum();
                //将b转为kb
                long totalNewFileSizeKB = getTotalNewFileSizeKB(totalNewFileSize);
                ecmBusiStatistics.setStatsDate(formattedDate);
                ecmBusiStatistics.setCreateTime(DateUtils.dateTime("yyyyMMdd", formattedDate));
                ecmBusiStatistics.setFileSize(totalNewFileSizeKB);
                ecmBusiStatisticsList.add(ecmBusiStatistics);
            });
        });
    }

    //todo 未使用
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
     * 工作量统计
     */
    public PageInfo<EcmWorkStatisticsDTO> workloadSearch(EcmStatisticsVO ecmStatisticsDTO,
                                                         AccountTokenExtendDTO token) {
        List<EcmWorkStatisticsDTO> ecmWorkStatisticsDTOS = getEcmWorkStatisticsDTOS(
                ecmStatisticsDTO, token);
        int pageNum = ecmStatisticsDTO.getPageNum();
        int pageSize = ecmStatisticsDTO.getPageSize();
        int total = ecmWorkStatisticsDTOS.size();
        int totalPage = (total + pageSize - 1) / pageSize;
        if (totalPage > 0 && pageNum > totalPage) {
            pageNum = 1;
        }
        PageInfo<EcmWorkStatisticsDTO> pageInfo = PageInfoUtils.getPageInfo(
                pageNum, pageSize,
                ecmWorkStatisticsDTOS);
        return pageInfo;
    }

    private List<EcmWorkStatisticsDTO> getEcmWorkStatisticsDTOS(EcmStatisticsVO ecmStatisticsDTO,
                                                                AccountTokenExtendDTO token) {
        LambdaQueryWrapper<EcmUserBusiFileStatistics> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (!CollectionUtils.isEmpty(ecmStatisticsDTO.getOrgCodes())) {
            Long[] array = new Long[ecmStatisticsDTO.getOrgCodes().size()];
            for (int i = 0; i < ecmStatisticsDTO.getOrgCodes().size(); i++) {
                array[i] = Long.parseLong(ecmStatisticsDTO.getOrgCodes().get(i));
            }
            Result<List<SysInstDTO>> instsByInstIds = instApi.getInstsByInstIds(array);
            List<SysInstDTO> data = instsByInstIds.getData();
            List<String> collect = data.stream().map(SysInstDTO::getInstNo)
                    .collect(Collectors.toList());
            objectLambdaQueryWrapper.in(EcmUserBusiFileStatistics::getOrgCode, collect);
        }
        if (ObjectUtils.isEmpty(ecmStatisticsDTO.getStartDate())
                || ObjectUtils.isEmpty(ecmStatisticsDTO.getEndDate())) {
            // 获取当前日期
            LocalDate today = LocalDate.now();
            // 获取前一天的日期
            LocalDate yesterday = today.minusDays(1);
            ecmStatisticsDTO.setEndDate(
                    Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            // 获取前一天的日期
            LocalDate oldDay = today.minusDays(30);
            objectLambdaQueryWrapper.between(EcmUserBusiFileStatistics::getCreateTime, yesterday,
                    oldDay);
        } else {
            objectLambdaQueryWrapper.between(EcmUserBusiFileStatistics::getCreateTime,
                    ecmStatisticsDTO.getStartDate(), ecmStatisticsDTO.getEndDate());
        }

        if (!CollectionUtils.isEmpty(ecmStatisticsDTO.getMapList())) {
            objectLambdaQueryWrapper.in(EcmUserBusiFileStatistics::getStatsUser,
                    ecmStatisticsDTO.getMapList());
        }
        objectLambdaQueryWrapper.orderByDesc(EcmUserBusiFileStatistics::getCreateTime);

        List<EcmUserBusiFileStatistics> ecmUserBusiFileStatistics = ecmUserBusiFileStatisticsMapper
                .selectList(objectLambdaQueryWrapper);
        List<EcmWorkStatisticsDTO> ecmWorkStatisticsDTOS = new ArrayList<>();

        if (!CollectionUtils.isEmpty(ecmUserBusiFileStatistics)) {

            Map<String, Map<String, Map<Integer, List<EcmUserBusiFileStatistics>>>> collect2 = ecmUserBusiFileStatistics
                    .stream()
                    .collect(Collectors.groupingBy(EcmUserBusiFileStatistics::getCreateUser,
                            Collectors.groupingBy(EcmUserBusiFileStatistics::getOrgCode, Collectors
                                    .groupingBy(EcmUserBusiFileStatistics::getStatsDate))));

            for (String username : collect2.keySet()) {
                Map<String, Map<Integer, List<EcmUserBusiFileStatistics>>> stringMapMap = collect2
                        .get(username);
                for (String orgCode : stringMapMap.keySet()) {
                    Map<Integer, List<EcmUserBusiFileStatistics>> integerListMap = stringMapMap
                            .get(orgCode);
                    for (Integer date : integerListMap.keySet()) {
                        List<EcmUserBusiFileStatistics> ecmUserBusiFileStatistics1 = integerListMap
                                .get(date);
                        EcmWorkStatisticsDTO dto = new EcmWorkStatisticsDTO();
                        dto.setOrgCode(orgCode);
                        HashMap<String, Long> map = new HashMap();
                        Long total = 0l;
                        Map<String, List<EcmUserBusiFileStatistics>> collect1 = ecmUserBusiFileStatistics1
                                .stream().filter(s -> s.getStatsDate().equals(date))
                                .collect(Collectors
                                        .groupingBy(EcmUserBusiFileStatistics::getAppCode));

                        for (String appCode : collect1.keySet()) {
                            final Long[] totalAppCode = { 0l };
                            collect1.get(appCode).forEach((s) -> {
                                totalAppCode[0] = totalAppCode[0] + s.getFileNumber();
                            });
                            map.put(appCode, totalAppCode[0]);
                            total = total + totalAppCode[0];
                        }

                        dto.setAppMap(map);
                        dto.setCreateUser(username);
                        dto.setTotal(total);
                        if (IcmsConstants.WORKSTATISTICS_DAY.equals(ecmStatisticsDTO.getUnit())) {
                            dto.setCreateDate(DateUtils.dateToString(
                                    DateUtils.dateTime("yyyyMMdd", date.toString()),
                                    DateUtils.YYYY_MM_DD));

                        } else if (IcmsConstants.WORKSTATISTICS_MONTH
                                .equals(ecmStatisticsDTO.getUnit())) {
                            dto.setCreateDate(DateUtils.dateToString(
                                    DateUtils.dateTime("yyyyMMdd", date.toString()), "yyyy-MM"));

                        } else if (IcmsConstants.WORKSTATISTICS_YEAR
                                .equals(ecmStatisticsDTO.getUnit())) {
                            dto.setCreateDate(DateUtils.dateToString(
                                    DateUtils.dateTime("yyyyMMdd", date.toString()), "yyyy"));

                        }
                        ecmWorkStatisticsDTOS.add(dto);
                    }

                }
            }
        }
        //排序
        String sortColumn = ecmStatisticsDTO.getSortColumn();
        String sortRule = ecmStatisticsDTO.getSortRule();
        if (!ObjectUtils.isEmpty(sortColumn) && !ObjectUtils.isEmpty(sortRule)) {
            switch (sortColumn) {
                case "orgCode":
                    if ("asc".equals(sortRule)) {
                        ecmWorkStatisticsDTOS
                                .sort(Comparator.comparing(EcmWorkStatisticsDTO::getOrgCode));
                    } else if ("desc".equals(sortRule)) {
                        ecmWorkStatisticsDTOS.sort(
                                Comparator.comparing(EcmWorkStatisticsDTO::getOrgCode).reversed());
                    }
                    break;
                case "createDate":
                    if ("asc".equals(sortRule)) {
                        ecmWorkStatisticsDTOS
                                .sort(Comparator.comparing(EcmWorkStatisticsDTO::getCreateDate));
                    } else if ("desc".equals(sortRule)) {
                        ecmWorkStatisticsDTOS.sort(Comparator
                                .comparing(EcmWorkStatisticsDTO::getCreateDate).reversed());
                    }
                    break;
                case "createUser":
                    if ("asc".equals(sortRule)) {
                        ecmWorkStatisticsDTOS
                                .sort(Comparator.comparing(EcmWorkStatisticsDTO::getCreateUser));
                    } else if ("desc".equals(sortRule)) {
                        ecmWorkStatisticsDTOS.sort(Comparator
                                .comparing(EcmWorkStatisticsDTO::getCreateUser).reversed());
                    }
                    break;
                case "total":
                    if ("asc".equals(sortRule)) {
                        ecmWorkStatisticsDTOS
                                .sort(Comparator.comparing(EcmWorkStatisticsDTO::getTotal));
                    } else if ("desc".equals(sortRule)) {
                        ecmWorkStatisticsDTOS.sort(
                                Comparator.comparing(EcmWorkStatisticsDTO::getTotal).reversed());
                    }
                    break;
                default:
                    break;
            }
        }
        return ecmWorkStatisticsDTOS;
    }

    //todo 未使用
    /**
     * 添加机构号
     */
    private void addInstNo(Map<String, List<SysUserDTO>> collect1,
                           EcmWorkStatisticsDTO ecmWorkStatisticsDTO, String username) {
        List<SysUserDTO> sysUserDTOS = collect1.get(username);
        if (!ObjectUtils.isEmpty(sysUserDTOS)) {
            ecmWorkStatisticsDTO.setOrgCode(sysUserDTOS.get(0).getInstNo());
        }
    }

    //todo 未使用
    private List<SysUserDTO> getAllUserInfo() {
        List<SysUserDTO> data = Collections.emptyList();
        Result<List<SysUserDTO>> allUserInfo = userApi.getAllUserInfo();
        if (!ObjectUtils.isEmpty(allUserInfo)) {
            data = allUserInfo.getData();
        }
        return data;
    }

    /**
     * 获取所有业务
     */
    public HashMap<String, String> searchAllBusi() {
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectList(new LambdaQueryWrapper<EcmAppDef>()
                            .eq(EcmAppDef::getIsParent,IcmsConstants.ZERO));
        HashMap<String, String> map = new HashMap<>();
        ecmAppDefs.forEach(p -> {
            map.put(p.getAppCode(), "(" + p.getAppCode() + ")" + p.getAppName());
        });
        return map;
    }

    /**
     * 导出工作量统计表格
     */
    public void workloadSearchExport(HttpServletResponse response, EcmStatisticsVO vo,
                                     AccountTokenExtendDTO token) {
        List<EcmWorkStatisticsDTO> ecmWorkStatisticsDTOS = getEcmWorkStatisticsDTOS(vo, token);
        //导出明细记录
        try {
            //处理map 将 List<EcmWorkStatisticsDTO> 转为 List<Map<String,String>>
            List<LinkedHashMap<String, String>> listMap = handleData(ecmWorkStatisticsDTOS);
            //添加生成列标题
            List<List<String>> head = new ArrayList<>();
            //添加数据
            List<List<String>> dataContent = new ArrayList<>();
            for (Map<String, String> map : listMap) {
                List<String> row = new ArrayList<>();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    row.add(entry.getValue());
                    if (dataContent.isEmpty()) {
                        List<String> hd = new ArrayList<>();
                        hd.add(entry.getKey());
                        head.add(hd);
                    }
                }
                dataContent.add(row);
            }
            EasyExcelUtils.writeToExcel(response, head, dataContent, "工作量统计","工作量统计");
        } catch (IOException e) {
            log.error("导出失败", e);
        }
    }

    private List<LinkedHashMap<String, String>> handleData(List<EcmWorkStatisticsDTO> ecmWorkStatisticsDTOS) {
        HashMap<String, String> sm = searchAllBusi();
        return ecmWorkStatisticsDTOS.stream().map(p -> {
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            map.put("机构号", p.getOrgCode());
            map.put("操作员", p.getCreateUser());
            map.put("日期", p.getCreateDate());
            HashMap<String, Long> appMap = p.getAppMap();
            sm.forEach((k, v) -> {
                if (ObjectUtils.isEmpty(appMap.get(k))) {
                    map.put(v, "0");
                } else {
                    map.put(v, String.valueOf(appMap.get(k)));
                }
            });
            map.put("总计", String.valueOf(p.getTotal()));
            return map;
        }).collect(Collectors.toList());
    }
}
