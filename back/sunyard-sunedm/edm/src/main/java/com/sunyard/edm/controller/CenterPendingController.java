package com.sunyard.edm.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.constant.DocLogsConstants;
import com.sunyard.edm.dto.DocSysTagDTO;
import com.sunyard.edm.service.CenterDocumentService;
import com.sunyard.edm.service.CenterPendingService;
import com.sunyard.edm.service.SysTagService;
import com.sunyard.edm.vo.DocBsCompanyGroundingVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;

/**
 * @Author PJW 2022/12/12 14:01
 * @desc 文档中心-未上架文档
 */
@RestController
@RequestMapping("center/pending")
public class CenterPendingController extends BaseController {

    @Resource
    private SysTagService sysTagService;
    @Resource
    private CenterDocumentService centerDocumentService;
    @Resource
    private CenterPendingService centerPendingService;

    /**
     * 文档库列表
     */
    @PostMapping("search")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + DocLogsConstants.COMMON_GETLIST)
    public Result search(DocBsCompanyGroundingVO v, PageForm p) {
        v.setUserId(getToken().getId());
        v.setDeptId(getToken().getDeptId());
        v.setInstId(getToken().getInstId());
        return centerPendingService.search(v, p);
    }

    /**
     * 加入回收站（删除）
     */
    @PostMapping("addRecycle")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "加入回收站（删除）")
    public Result addRecycle(@RequestBody Long[] busIds) {
        centerDocumentService.addRecycle(getToken(), busIds, DocConstants.COMPANY);
        return Result.success(true);
    }


    /**
     * 重新上架
     */
    @PostMapping("reGrounding")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "重新上架")
    public Result reGrounding(@RequestBody Long[] busIds) {
        return centerPendingService.reGrounding(busIds, getToken());
    }

    /**
     * 标签次联框
     */
    @PostMapping("getTagTree")
    @OperationLog(DocLogsConstants.SHAPE + "标签次联框")
    public Result<List<DocSysTagDTO>> getTagTree() {
        return Result.success(sysTagService.getTagTree());
    }

}
