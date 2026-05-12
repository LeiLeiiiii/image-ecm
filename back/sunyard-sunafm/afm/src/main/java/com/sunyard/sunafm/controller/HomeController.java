package com.sunyard.sunafm.controller;

import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.sunafm.dto.AfmDetNoteDetailsDTO;
import com.sunyard.sunafm.dto.AfmDetOnlineImgDetDTO;
import com.sunyard.sunafm.dto.AfmHomeListDTO;
import com.sunyard.sunafm.dto.AfmHomeProfileDTO;
import com.sunyard.sunafm.dto.ArmHomeCountDTO;
import com.sunyard.sunafm.service.HomeService;
import com.sunyard.sunafm.service.RecordDupService;
import com.sunyard.sunafm.service.RecordFalsifyService;
import com.sunyard.sunafm.service.RecordInvoiceService;
import com.sunyard.sunafm.vo.AfmDetNoteListVO;
import com.sunyard.sunafm.vo.AfmHomeVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author P-JWei
 * @date 2024/3/7 15:50:55
 * @title
 * @description 首页
 */
@RestController
@RequestMapping("home")
public class HomeController extends BaseController {
    @Resource
    private HomeService homeService;
    @Resource
    private RecordDupService recordDupService;
    @Resource
    private RecordInvoiceService recordInvoiceService;
    @Resource
    private RecordFalsifyService recordFalsifyService;
    /**
     * 图像查重总数
     */
    @OperationLog("首页-图像查重总数")
    @PostMapping("imgDupCount")
    public Result<ArmHomeCountDTO> imgDupCount(AfmHomeVO afmHomeVO) {
        return homeService.imgDupCount(afmHomeVO);
    }

    /**
     * 篡改检测总数
     */
    @OperationLog("首页-篡改检测总数")
    @PostMapping("psDetCount")
    public Result<ArmHomeCountDTO> psDetCount(AfmHomeVO afmHomeVO) {
        return homeService.psDetCount(afmHomeVO);
    }

    /**
     * 发票检测总数
     */
    @OperationLog("首页-发票检测总数")
    @PostMapping("invoiceDetCount")
    public Result<ArmHomeCountDTO> invoiceDetCount(AfmHomeVO afmHomeVO) {
        return homeService.invoiceDetCount(afmHomeVO);
    }

    /**
     * 图像查重异常数据表格
     */
    @OperationLog("首页-图像查重异常数据表格")
    @PostMapping("imgDupList")
    public Result<PageInfo<AfmHomeListDTO>> imgDupList(AfmHomeVO afmHomeVO, PageForm pageForm) {
        return homeService.imgDupList(afmHomeVO,pageForm);
    }

    /**
     * 篡改检测异常数据表格
     */
    @OperationLog("首页-篡改检测异常数据表格")
    @PostMapping("psDetList")
    public Result<PageInfo<AfmHomeListDTO>> psDetList(AfmHomeVO afmHomeVO, PageForm pageForm) {
        return homeService.psDetList(afmHomeVO, pageForm);
    }

    /**
     * 发票检测异常数据表格
     */
    @OperationLog("首页-发票检测异常数据表格")
    @PostMapping("invoiceDetList")
    public Result<PageInfo<AfmHomeListDTO>> invoiceDetList(AfmHomeVO afmHomeVO, PageForm pageForm) {
        return homeService.invoiceDetList(afmHomeVO,pageForm);
    }

    /**
     * 图像查重异常数据分布图
     */
    @OperationLog("首页-图像查重异常数据分布图")
    @PostMapping("imgDupProfile")
    public Result<AfmHomeProfileDTO> imgDupProfile(AfmHomeVO afmHomeVO) {
        return homeService.imgDupProfile(afmHomeVO);
    }

    /**
     * 篡改检测异常数据分布图
     */
    @OperationLog("首页-篡改检测异常数据分布图")
    @PostMapping("psDetProfile")
    public Result<AfmHomeProfileDTO> psDetProfile(AfmHomeVO afmHomeVO) {
        return homeService.psDetProfile(afmHomeVO);
    }

    /**
     * 发票检测异常数据分布图
     */
    @OperationLog("首页-发票检测异常数据分布图")
    @PostMapping("invoiceDetProfile")
    public Result<AfmHomeProfileDTO> invoiceDetProfile(AfmHomeVO afmHomeVO) {
        return homeService.invoiceDetProfile(afmHomeVO);
    }

    /**
     * 查重记录详情
     */
    @OperationLog("检测记录-图像查重-查重记录详情")
    @PostMapping("dupNoteDetails")
    public Result<List<AfmDetOnlineImgDetDTO>> dupNoteDetails(@RequestBody AfmDetNoteListVO vo) {
        return Result.success(recordDupService.noteDetails(vo));
    }

    /**
     * 发票记录详情
     */
    @OperationLog("检测记录-发票检测-发票记录详情")
    @PostMapping("invoiceNoteDetails")
    public Result<List<AfmDetNoteDetailsDTO>> invoiceNoteDetails(Long[] ids, String token) {
        return recordInvoiceService.noteDetails(ids, token);
    }

    /**
     * 篡改记录详情
     */
    @OperationLog("检测记录-篡改检测-篡改记录详情")
    @PostMapping("falsifyNoteDetails")
    public Result<List<AfmDetNoteDetailsDTO>> falsifyNoteDetails(Long[] ids, String token) {
        return recordFalsifyService.noteDetails(ids,token);
    }
}
