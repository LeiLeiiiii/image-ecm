package com.sunyard.edm.controller;

import com.sunyard.edm.constant.DocLogsConstants;
import com.sunyard.edm.dto.DocSysTagDTO;
import com.sunyard.edm.service.CenterCollectionService;
import com.sunyard.edm.service.CenterDocumentService;
import com.sunyard.edm.service.SysTagService;
import com.sunyard.edm.vo.DocBsCollectionVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author PJW 2022/12/12 14:01
 * @desc 首页-我的收藏
 */
@RestController
@RequestMapping("center/collection")
public class CenterCollectionController extends BaseController {

    @Resource
    private SysTagService docSysTagService;
    @Resource
    private CenterCollectionService centerCollectionService;
    @Resource
    private CenterDocumentService centerDocumentService;


    /**
     * 收藏列表
     *
     * @param c 查询条件入参
     * @param p 分页参数
     */
    @OperationLog(DocLogsConstants.COLLECTION + "收藏列表")
    @PostMapping("query")
    public Result query(DocBsCollectionVO c, PageForm p) {
        c.setUserId(getToken().getId());
        c.setDeptId(getToken().getDeptId());
        c.setInstId(getToken().getInstId());
        return centerCollectionService.query(c, p);
    }

    /**
     * 收藏文档详情
     *
     * @param docId 文档id
     */
    @OperationLog(DocLogsConstants.COLLECTION + "收藏文档详情")
    @PostMapping("queryDoc")
    public Result queryDoc(Long docId) {
        return Result.success(centerDocumentService.getInfo(docId, getToken().getId()));
    }

    /**
     * 取消收藏
     *
     * @param collectionId 收藏id
     */
    @OperationLog(DocLogsConstants.COLLECTION + "取消收藏")
    @PostMapping("cancelCollection")
    public Result cancelCollection(Long collectionId) {
        return centerCollectionService.cancelCollection(collectionId);
    }

    /**
     * 标签次联框
     *
     * @return 标签次联框
     */
    @PostMapping("getTagTree")
    @OperationLog(DocLogsConstants.SHAPE + "标签次联框")
    public Result<List<DocSysTagDTO>> getTagTree() {
        return Result.success(docSysTagService.getTagTree());
    }
}
