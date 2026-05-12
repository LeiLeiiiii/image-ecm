package com.sunyard.sunafm.controller;

import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.sunafm.dto.AfmDetNoteDetailsDTO;
import com.sunyard.sunafm.dto.AfmDetOnlineInvoiceAttrDTO;
import com.sunyard.sunafm.dto.AfmDetOnlineInvoiceDetDTO;
import com.sunyard.sunafm.service.DetInvoiceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author P-JWei
 * @date 2024/3/7 15:58:42
 * @title
 * @description  在线检测/发票检测
 */
@RestController
@RequestMapping("det/invoice")
public class DetInvoiceController extends BaseController {

    @Resource
    private DetInvoiceService detInvoiceService;

    /**
     * 开始检测
     */
    @OperationLog("在线检测-发票检测-开始检测")
    @PostMapping("det")
    public Result det(MultipartFile file, Long exifId, String token) {
        return detInvoiceService.det(file, exifId, token, getToken().getId());
    }

    /**
     * 获取发票查重结果
     */
    @OperationLog("在线检测-发票检测-获取发票查重结果")
    @PostMapping("detDupResult")
    public Result detDupResult(Long noteId, String token, PageForm pageForm){
        return detInvoiceService.detDupResult(noteId, token, pageForm);
    }

    /**
     * 获取发票连续结果
     */
    @OperationLog("在线检测-发票检测-获取发票连续结果")
    @PostMapping("detLinkResult")
    public Result detLinkResult(Long noteId, String token, PageForm pageForm){
        return detInvoiceService.detLinkResult(noteId, token, pageForm);
    }


    /**
     * 发票记录详情
     */
    @OperationLog("在线检测-发票检测-发票记录详情")
    @PostMapping("noteDetails")
    public Result<AfmDetNoteDetailsDTO> noteDetails(Long exifId, String token) {
        return detInvoiceService.noteDetails(exifId, token);
    }

    /**
     * 发票记录属性
     */
    @OperationLog("在线检测-发票检测-发票记录属性")
    @PostMapping("noteAttr")
    public Result<AfmDetOnlineInvoiceAttrDTO> noteAttr(Long exifId, String token) {
        return detInvoiceService.noteAttr(exifId, token);
    }

    /**
     * 发票结果--详情、比对
     */
    @OperationLog("在线检测-发票检测-发票结果--详情、比对")
    @PostMapping("resultDetails")
    public Result<AfmDetOnlineInvoiceDetDTO> resultDetails(Long exifId, String token) {
        return detInvoiceService.resultDetails(exifId, token);
    }
}
