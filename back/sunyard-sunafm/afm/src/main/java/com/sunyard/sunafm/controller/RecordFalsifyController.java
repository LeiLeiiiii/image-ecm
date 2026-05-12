package com.sunyard.sunafm.controller;

import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.sunafm.dto.AfmDetNoteDetailsDTO;
import com.sunyard.sunafm.dto.AfmDetNotePsDTO;
import com.sunyard.sunafm.dto.AfmDetNotePsListDTO;
import com.sunyard.sunafm.service.RecordFalsifyService;
import com.sunyard.sunafm.vo.AfmDetNoteListVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author P-JWei
 * @date 2024/3/7 15:55:46
 * @title
 * @description 检测记录/篡改检测
 */
@RestController
@RequestMapping("record/falsify")
public class RecordFalsifyController extends BaseController {

    @Resource
    private RecordFalsifyService recordFalsifyService;


    /**
     * 篡改检测表格
     */
    @OperationLog("检测记录-篡改检测-篡改检测表格")
    @PostMapping("psDetList")
    public Result<PageInfo<AfmDetNotePsListDTO>> psDetList(AfmDetNoteListVO afmDetNoteListVO, PageForm pageForm) {
        return recordFalsifyService.psDetList(afmDetNoteListVO, pageForm);
    }

    /**
     * 篡改记录详情
     */
    @OperationLog("检测记录-篡改检测-篡改记录详情")
    @PostMapping("noteDetails")
    public Result<List<AfmDetNoteDetailsDTO>> noteDetails(Long[] ids, String token) {
        return recordFalsifyService.noteDetails(ids,token);
    }

    /**
     * 篡改结果详情
     */
    @OperationLog("检测记录-篡改检测-篡改结果详情")
    @PostMapping("resultDetails")
    public Result<AfmDetNotePsDTO> resultDetails(Long exifId) {
        return recordFalsifyService.resultDetails(exifId);
    }



    /**
     * 导出
     */
    @OperationLog("检测记录-篡改检测-导出")
    @PostMapping("exportList")
    public void exportList(HttpServletResponse response, AfmDetNoteListVO afmDetNoteListVO) {
        recordFalsifyService.exportList(response, afmDetNoteListVO);
    }
}
