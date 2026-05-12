package com.sunyard.sunafm.controller;

import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.sunafm.dto.AfmDetNoteDetailsDTO;
import com.sunyard.sunafm.dto.AfmDetNoteInvoiceAttrDTO;
import com.sunyard.sunafm.dto.AfmDetNoteInvoiceDetDTO;
import com.sunyard.sunafm.dto.AfmDetNoteInvoiceListDTO;
import com.sunyard.sunafm.service.RecordInvoiceService;
import com.sunyard.sunafm.vo.AfmDetNoteListVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author P-JWei
 * @date 2024/3/7 15:56:06
 * @title
 * @description 检测记录/发票检测
 */
@RestController
@RequestMapping("record/invoice")
public class RecordInvoiceController extends BaseController {

    @Resource
    private RecordInvoiceService recordInvoiceService;

    /**
     * 发票检测表格
     */
    @OperationLog("检测记录-发票检测-发票检测表格")
    @PostMapping("invoiceDetList")
    public Result<PageInfo<AfmDetNoteInvoiceListDTO>> invoiceDetList(AfmDetNoteListVO afmDetNoteListVO, PageForm pageForm) {
        return recordInvoiceService.invoiceDetList(afmDetNoteListVO, pageForm);
    }

    /**
     * 发票记录详情
     */
    @OperationLog("检测记录-发票检测-发票记录详情")
    @PostMapping("noteDetails")
    public Result<List<AfmDetNoteDetailsDTO>> noteDetails(Long[] ids, String token) {
        return recordInvoiceService.noteDetails(ids, token);
    }

    /**
     * 发票记录属性
     */
    @OperationLog("检测记录-发票检测-发票记录属性")
    @PostMapping("noteAttr")
    public Result<AfmDetNoteInvoiceAttrDTO> noteAttr(Long id, String token) {
        return recordInvoiceService.noteAttr(id, token);
    }

    /**
     * 发票结果--详情、比对
     */
    @OperationLog("检测记录-发票检测-发票结果--详情、比对")
    @PostMapping("resultDetails")
    public Result<AfmDetNoteInvoiceDetDTO> resultDetails(Long id, String token) {
        return recordInvoiceService.resultDetails(id, token);
    }

    /**
     * 导出
     */
    @OperationLog("检测记录-发票检测-导出")
    @PostMapping("exportList")
    public void exportList(HttpServletResponse response, AfmDetNoteListVO afmDetNoteListVO) {
        recordInvoiceService.exportList(response, afmDetNoteListVO);
    }
}
