package com.sunyard.sunafm.controller;

import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.sunafm.dto.AfmDetNoteImgListDTO;
import com.sunyard.sunafm.dto.AfmDetOnlineImgDetDTO;
import com.sunyard.sunafm.service.RecordDupService;
import com.sunyard.sunafm.vo.AfmDetNoteListVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author P-JWei
 * @date 2024/3/7 15:55:28
 * @title
 * @description  检测记录/图像查重
 */

@RestController
@RequestMapping("record/dup")
public class RecordDupController extends BaseController {
    @Resource
    private RecordDupService recordDupService;
    /**
     * 图像查重表格
     */
    @OperationLog("检测记录-图像查重-获取图像查重列表")
    @PostMapping("imgDupList")
    public Result<PageInfo<AfmDetNoteImgListDTO>> imgDupList(AfmDetNoteListVO afmDetNoteListVO, PageForm pageForm) {
        return Result.success(recordDupService.imgDupList(afmDetNoteListVO,pageForm));
    }

    /**
     * 选择文件
     */
    @OperationLog("检测记录-图像查重-图像查重列表搜索条件下拉列表数据")
    @PostMapping("queryChooseConditionsNote")
    public Result<Map> queryChooseConditionsNote() {
        return Result.success(recordDupService.queryChooseConditionsNote());
    }

    /**
     * 查重记录详情
     */
    @OperationLog("检测记录-图像查重-查重记录详情")
    @PostMapping("noteDetails")
    public Result<List<AfmDetOnlineImgDetDTO>> noteDetails(@RequestBody AfmDetNoteListVO vo) {
        return Result.success(recordDupService.noteDetails(vo));
    }

    /**
     * 导出
     */
    @OperationLog("检测记录-图像查重-导出")
    @PostMapping("exportList")
    public void exportList(HttpServletResponse response,  AfmDetNoteListVO afmDetNoteListVO) {
        recordDupService.exportList(response,afmDetNoteListVO);
    }

}
