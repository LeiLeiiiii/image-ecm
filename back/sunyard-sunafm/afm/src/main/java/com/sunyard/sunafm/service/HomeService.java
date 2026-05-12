package com.sunyard.sunafm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.sunafm.constant.AfmConstant;
import com.sunyard.sunafm.dto.AfmHomeListDTO;
import com.sunyard.sunafm.dto.AfmHomeProfileDTO;
import com.sunyard.sunafm.dto.ArmHomeCountDTO;
import com.sunyard.sunafm.enums.InvoiceDetResultEnum;
import com.sunyard.sunafm.mapper.AfmImageDupNoteMapper;
import com.sunyard.sunafm.mapper.AfmImagePsNoteMapper;
import com.sunyard.sunafm.mapper.AfmInvoiceDetNoteMapper;
import com.sunyard.sunafm.po.AfmImageDupNote;
import com.sunyard.sunafm.po.AfmImagePsNote;
import com.sunyard.sunafm.po.AfmInvoiceDetNote;
import com.sunyard.sunafm.vo.AfmHomeVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author P-JWei
 * @date 2024/4/7 16:55:49
 * @title
 * @description 首页实现类
 */
@Service
public class HomeService {

    @Resource
    private AfmImageDupNoteMapper afmImageDupNoteMapper;
    @Resource
    private AfmImagePsNoteMapper afmImagePsNoteMapper;
    @Resource
    private AfmInvoiceDetNoteMapper afmInvoiceDetNoteMapper;
    @Resource
    private CommonService commonService;

    /**
     * 图像查重总数
     */
    public Result<ArmHomeCountDTO> imgDupCount(AfmHomeVO vo) {
        ArmHomeCountDTO armHomeCountDTO = new ArmHomeCountDTO();
        List<AfmImageDupNote> list = afmImageDupNoteMapper
                .selectList(new LambdaQueryWrapper<AfmImageDupNote>()
                        .select(AfmImageDupNote::getSourceSys, AfmImageDupNote::getImgDupResult, AfmImageDupNote::getImgDupTime)
                        .ne(AfmImageDupNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                        .between(AfmImageDupNote::getImgDupTime, vo.getToTime(), vo.getDoTime()));
        armHomeCountDTO.setCount((long) list.size());
        armHomeCountDTO.setAbnormalCount(list.stream().filter(item -> item.getImgDupResult() != 0).count());
        armHomeCountDTO.setDetailsList(countDates(list.stream().map(AfmImageDupNote::getImgDupTime).collect(Collectors.toList()), vo.getToTime(), vo.getDoTime()));
        return Result.success(armHomeCountDTO);
    }

    /**
     * 篡改检测总数
     */
    public Result<ArmHomeCountDTO> psDetCount(AfmHomeVO vo) {
        ArmHomeCountDTO armHomeCountDTO = new ArmHomeCountDTO();
        List<AfmImagePsNote> list = afmImagePsNoteMapper
                .selectList(new LambdaQueryWrapper<AfmImagePsNote>()
                        .select(AfmImagePsNote::getSourceSys, AfmImagePsNote::getPsDetResult, AfmImagePsNote::getPsDetTime)
                        .ne(AfmImagePsNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                        .between(AfmImagePsNote::getPsDetTime, vo.getToTime(), vo.getDoTime()));
        armHomeCountDTO.setCount((long) list.size());
        armHomeCountDTO.setAbnormalCount(list.stream().filter(item -> item.getPsDetResult() != 0).count());
        armHomeCountDTO.setDetailsList(countDates(list.stream().map(AfmImagePsNote::getPsDetTime).collect(Collectors.toList()), vo.getToTime(), vo.getDoTime()));
        return Result.success(armHomeCountDTO);
    }

    /**
     * 发票检测总数
     */
    public Result<ArmHomeCountDTO> invoiceDetCount(AfmHomeVO vo) {
        ArmHomeCountDTO armHomeCountDTO = new ArmHomeCountDTO();
        List<AfmInvoiceDetNote> list = afmInvoiceDetNoteMapper
                .selectList(new LambdaQueryWrapper<AfmInvoiceDetNote>()
                        .select(AfmInvoiceDetNote::getSourceSys,
                                AfmInvoiceDetNote::getInvoiceVerifyResult,
                                AfmInvoiceDetNote::getInvoiceDupResult,
                                AfmInvoiceDetNote::getInvoiceLinkResult,
                                AfmInvoiceDetNote::getInvoiceDetTime)
                        .ne(AfmInvoiceDetNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                        .between(AfmInvoiceDetNote::getInvoiceDetTime, vo.getToTime(), vo.getDoTime()));
        armHomeCountDTO.setCount((long) list.size());
        armHomeCountDTO.setAbnormalCount(list.stream().filter(item -> (
                (null != item.getInvoiceVerifyResult() && item.getInvoiceVerifyResult() == 0) ||
                        (null != item.getInvoiceDupResult() && item.getInvoiceDupResult() == 0) ||
                        (null != item.getInvoiceLinkResult() && item.getInvoiceLinkResult() == 0))).count());
        armHomeCountDTO.setDetailsList(countDates(list.stream().map(AfmInvoiceDetNote::getInvoiceDetTime).collect(Collectors.toList()), vo.getToTime(), vo.getDoTime()));
        return Result.success(armHomeCountDTO);
    }

    /**
     * 图像查重异常数据表格
     */
    public Result<PageInfo<AfmHomeListDTO>> imgDupList(AfmHomeVO vo, PageForm pageForm) {
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<AfmImageDupNote> afmImageDupNotes = afmImageDupNoteMapper.selectList(new LambdaQueryWrapper<AfmImageDupNote>()
                .select(AfmImageDupNote::getId,
                        AfmImageDupNote::getSourceSys,
                        AfmImageDupNote::getExifIdOrMd5,
                        AfmImageDupNote::getFileName,
                        AfmImageDupNote::getImgDupResult,
                        AfmImageDupNote::getImgDupTime)
                .ne(AfmImageDupNote::getImgDupResult, 0)
                .ne(AfmImageDupNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                .between(AfmImageDupNote::getImgDupTime, vo.getToTime(), vo.getDoTime())
                .orderByDesc(AfmConstant.NO.equals(vo.getTimeSort()), AfmImageDupNote::getImgDupTime)
                .orderByAsc(AfmConstant.YES.equals(vo.getTimeSort()), AfmImageDupNote::getImgDupTime)
                .orderByDesc(AfmConstant.NO.equals(vo.getResultSort()), AfmImageDupNote::getImgDupResult)
                .orderByAsc(AfmConstant.YES.equals(vo.getResultSort()), AfmImageDupNote::getImgDupResult)
        );
        PageInfo<AfmImageDupNote> afmImageDupNotePageInfo = new PageInfo<>(afmImageDupNotes);
        Map<String, String> afmSource = commonService.getAfmSource();
        PageInfo<AfmHomeListDTO> result = new PageInfo<>();
        if (!CollectionUtils.isEmpty(afmImageDupNotes)) {
            List<AfmHomeListDTO> list = new ArrayList<>();
            afmImageDupNotePageInfo.getList().forEach(item -> {
                AfmHomeListDTO i = new AfmHomeListDTO();
                i.setExifIdOrMd5(item.getExifIdOrMd5());
                i.setSourceSys(afmSource.get(item.getSourceSys()));
                i.setFileName(item.getFileName());
                i.setDetResult(convertToPercentage(item.getImgDupResult()));
                i.setDetTime(item.getImgDupTime());
                list.add(i);
            });
            result.setList(list);
            result.setTotal(afmImageDupNotePageInfo.getTotal());
            result.setPageNum(pageForm.getPageNum());
            result.setPageSize(pageForm.getPageSize());
        }
        return Result.success(result);
    }

    /**
     * 篡改检测异常数据表格
     */
    public Result<PageInfo<AfmHomeListDTO>> psDetList(AfmHomeVO vo, PageForm pageForm) {
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<AfmImagePsNote> afmImagePsNotes = afmImagePsNoteMapper.selectList(new LambdaQueryWrapper<AfmImagePsNote>()
                .select(AfmImagePsNote::getId,
                        AfmImagePsNote::getSourceSys,
                        AfmImagePsNote::getExifId,
                        AfmImagePsNote::getFileName,
                        AfmImagePsNote::getPsCount,
                        AfmImagePsNote::getPsDetTime)
                .ne(AfmImagePsNote::getPsDetResult, 0)
                .ne(AfmImagePsNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                .between(AfmImagePsNote::getPsDetTime, vo.getToTime(), vo.getDoTime())
                .orderByDesc(AfmConstant.NO.equals(vo.getResultSort()), AfmImagePsNote::getPsCount)
                .orderByAsc(AfmConstant.YES.equals(vo.getResultSort()), AfmImagePsNote::getPsCount)
                .orderByDesc(AfmConstant.NO.equals(vo.getTimeSort()), AfmImagePsNote::getPsDetTime)
                .orderByAsc(AfmConstant.YES.equals(vo.getTimeSort()), AfmImagePsNote::getPsDetTime)
        );
        PageInfo<AfmImagePsNote> afmImagePsNotePageInfo = new PageInfo<>(afmImagePsNotes);
        Map<String, String> afmSource = commonService.getAfmSource();
        PageInfo<AfmHomeListDTO> result = new PageInfo<>();
        if (!CollectionUtils.isEmpty(afmImagePsNotes)) {
            List<AfmHomeListDTO> list = new ArrayList<>();
            afmImagePsNotePageInfo.getList().forEach(item -> {
                AfmHomeListDTO i = new AfmHomeListDTO();
                i.setId(item.getId());
                i.setSourceSys(afmSource.get(item.getSourceSys()));
                i.setExifIdOrMd5(String.valueOf(item.getExifId()));
                i.setFileName(item.getFileName());
                i.setDetResult(String.valueOf(item.getPsCount()));
                i.setDetTime(item.getPsDetTime());
                list.add(i);
            });
            result.setList(list);
            result.setTotal(afmImagePsNotePageInfo.getTotal());
            result.setPageNum(pageForm.getPageNum());
            result.setPageSize(pageForm.getPageSize());
        }else {
            result.setList(new ArrayList<>());
            result.setTotal(afmImagePsNotePageInfo.getTotal());
            result.setPageNum(pageForm.getPageNum());
            result.setPageSize(pageForm.getPageSize());
        }
        return Result.success(result);
    }

    /**
     * 发票检测异常数据表格
     */
    public Result<PageInfo<AfmHomeListDTO>> invoiceDetList(AfmHomeVO vo, PageForm pageForm) {
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<AfmInvoiceDetNote> afmInvoiceDetNotes = afmInvoiceDetNoteMapper.selectList(new LambdaQueryWrapper<AfmInvoiceDetNote>()
                .select(AfmInvoiceDetNote::getId,
                        AfmInvoiceDetNote::getSourceSys,
                        AfmInvoiceDetNote::getFileName,
                        AfmInvoiceDetNote::getInvoiceVerifyResult,
                        AfmInvoiceDetNote::getInvoiceDupResult,
                        AfmInvoiceDetNote::getInvoiceLinkResult,
                        AfmInvoiceDetNote::getInvoiceDetTime)
                .ne(AfmInvoiceDetNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                .between(AfmInvoiceDetNote::getInvoiceDetTime, vo.getToTime(), vo.getDoTime())
                .and(wrapper -> wrapper.eq(AfmInvoiceDetNote::getInvoiceVerifyResult, 0)
                        .or().eq(AfmInvoiceDetNote::getInvoiceDupResult, 0)
                        .or().eq(AfmInvoiceDetNote::getInvoiceLinkResult, 0))
                .orderByDesc(AfmConstant.NO.equals(vo.getTimeSort()), AfmInvoiceDetNote::getInvoiceDetTime)
                .orderByAsc(AfmConstant.YES.equals(vo.getTimeSort()), AfmInvoiceDetNote::getInvoiceDetTime));
        PageInfo<AfmInvoiceDetNote> afmInvoiceDetNotePageInfo = new PageInfo<>(afmInvoiceDetNotes);
        Map<String, String> afmSource = commonService.getAfmSource();
        PageInfo<AfmHomeListDTO> result = new PageInfo<>();
        if (!CollectionUtils.isEmpty(afmInvoiceDetNotes)) {
            List<AfmHomeListDTO> list = new ArrayList<>();
            afmInvoiceDetNotePageInfo.getList().forEach(item -> {
                AfmHomeListDTO i = new AfmHomeListDTO();
                i.setId(item.getId());
                i.setSourceSys(afmSource.get(item.getSourceSys()));
                i.setExifIdOrMd5(String.valueOf(item.getId()));
                i.setFileName(item.getFileName());
                i.setDetResult(String.valueOf(getInvoiceDetResult(item)));
                i.setDetTime(item.getInvoiceDetTime());
                list.add(i);
            });
            result.setList(list);
            result.setTotal(afmInvoiceDetNotePageInfo.getTotal());
            result.setPageNum(pageForm.getPageNum());
            result.setPageSize(pageForm.getPageSize());
        }
        return Result.success(result);
    }

    /**
     * 图像查重异常数据分布图
     */
    public Result<AfmHomeProfileDTO> imgDupProfile(AfmHomeVO vo) {
        AfmHomeProfileDTO result = new AfmHomeProfileDTO();
        List<AfmImageDupNote> afmImageDupNotes = afmImageDupNoteMapper.selectList(new LambdaQueryWrapper<AfmImageDupNote>()
                .select(AfmImageDupNote::getSourceSys)
                .ne(AfmImageDupNote::getImgDupResult, 0)
                .ne(AfmImageDupNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                .between(AfmImageDupNote::getImgDupTime, vo.getToTime(), vo.getDoTime()));
        Map<String, String> afmSource = commonService.getAfmSource();
        afmImageDupNotes.forEach(item -> item.setSourceSys(afmSource.get(item.getSourceSys())));
        List<Map<String, Object>> resultList = afmImageDupNotes.stream()
                .collect(Collectors.groupingBy(AfmImageDupNote::getSourceSys, Collectors.counting()))
                .entrySet().stream().map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("value", entry.getValue());
                    map.put("name", entry.getKey());
                    return map;
                }).collect(Collectors.toList());
        result.setProfileData(resultList);
        return Result.success(result);
    }

    /**
     * 篡改检测异常数据分布图
     */
    public Result<AfmHomeProfileDTO> psDetProfile(AfmHomeVO vo) {
        AfmHomeProfileDTO result = new AfmHomeProfileDTO();
        List<AfmImagePsNote> afmImagePsNotes = afmImagePsNoteMapper.selectList(new LambdaQueryWrapper<AfmImagePsNote>()
                .select(AfmImagePsNote::getSourceSys)
                .ne(AfmImagePsNote::getPsDetResult, 0)
                .ne(AfmImagePsNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                .between(AfmImagePsNote::getPsDetTime, vo.getToTime(), vo.getDoTime()));
        Map<String, String> afmSource = commonService.getAfmSource();
        afmImagePsNotes.forEach(item -> item.setSourceSys(afmSource.get(item.getSourceSys())));
        List<Map<String, Object>> resultList = afmImagePsNotes.stream().collect(Collectors.groupingBy(AfmImagePsNote::getSourceSys, Collectors.counting()))
                .entrySet().stream().map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("value", entry.getValue());
                    map.put("name", entry.getKey());
                    return map;
                }).collect(Collectors.toList());
        result.setProfileData(resultList);
        return Result.success(result);
    }

    /**
     * 发票检测异常数据分布图
     */
    public Result<AfmHomeProfileDTO> invoiceDetProfile(AfmHomeVO vo) {
        AfmHomeProfileDTO result = new AfmHomeProfileDTO();
        List<AfmInvoiceDetNote> afmInvoiceDetNotes = afmInvoiceDetNoteMapper.selectList(new LambdaQueryWrapper<AfmInvoiceDetNote>()
                .select(AfmInvoiceDetNote::getSourceSys)
                .ne(AfmInvoiceDetNote::getSourceSys, AfmConstant.AFM_SOURCESYS)
                .between(AfmInvoiceDetNote::getInvoiceDetTime, vo.getToTime(), vo.getDoTime())
                .and(wrapper -> wrapper.eq(AfmInvoiceDetNote::getInvoiceVerifyResult, 0)
                        .or().eq(AfmInvoiceDetNote::getInvoiceDupResult, 0)
                        .or().eq(AfmInvoiceDetNote::getInvoiceLinkResult, 0)));
        Map<String, String> afmSource = commonService.getAfmSource();
        afmInvoiceDetNotes.forEach(item -> item.setSourceSys(afmSource.get(item.getSourceSys())));
        List<Map<String, Object>> resultList = afmInvoiceDetNotes.stream().collect(Collectors.groupingBy(AfmInvoiceDetNote::getSourceSys, Collectors.counting()))
                .entrySet().stream().map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("value", entry.getValue());
                    map.put("name", entry.getKey());
                    return map;
                }).collect(Collectors.toList());
        result.setProfileData(resultList);
        return Result.success(result);
    }

    /**
     * 发票检测结果转描述
     */
    private String getInvoiceDetResult(AfmInvoiceDetNote afmInvoiceDetNote) {
        StringBuilder stringBuilder = new StringBuilder();
        if (null != afmInvoiceDetNote.getInvoiceVerifyResult() && 0 == afmInvoiceDetNote.getInvoiceVerifyResult()) {
            stringBuilder.append(InvoiceDetResultEnum.getDescByCode(1)).append(" ");
        }
        if (null != afmInvoiceDetNote.getInvoiceDupResult() && 0 == afmInvoiceDetNote.getInvoiceDupResult()) {
            stringBuilder.append(InvoiceDetResultEnum.getDescByCode(2)).append(" ");
            return InvoiceDetResultEnum.getDescByCode(2);
        }
        if (null != afmInvoiceDetNote.getInvoiceLinkResult() && 0 == afmInvoiceDetNote.getInvoiceLinkResult()) {
            stringBuilder.append(InvoiceDetResultEnum.getDescByCode(3)).append(" ");
        }
        return stringBuilder.toString();
    }

    /**
     * 统计时间区间内每天总数
     */
    private List<Map<String, String>> countDates(List<Date> list, Date toTime, Date doTime) {
        // 找到最小日期和最大日期
        Date minDate = truncateTime(toTime);
        Date maxDate = truncateTime(doTime);
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        // 创建包含所有日期的列表
        List<Map<String, String>> counts = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(minDate);
        while (!calendar.getTime().after(maxDate)) {
            Map<String, String> dayMap = new HashMap<>();
            dayMap.put("name", sdf.format(calendar.getTime()));
            dayMap.put("value", "0");
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            counts.add(dayMap);
        }

        // 统计每个日期出现的次数
        for (Date item : list) {
            calendar.setTime(item);
            for (Map<String, String> dayMap : counts) {
                if (dayMap.get("name").equals(sdf.format(item))) {
                    int count = Integer.parseInt(dayMap.get("value"));
                    dayMap.put("value", String.valueOf(count + 1));
                }
            }
        }

        return counts;
    }

    /**
     * 去除时间的时分秒
     */
    private Date truncateTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 百分比转换
     */
    private String convertToPercentage(double number) {
        // 格式化百分比
        DecimalFormat df = new DecimalFormat("0.00%");
        // 将小数乘以100，以获取百分比形式
        double percentage = number * 100;
        // 使用格式化对象将数字转换为字符串，并进行四舍五入
        return df.format(percentage / 100);
    }
}
