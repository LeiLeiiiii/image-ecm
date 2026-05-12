package com.sunyard.edm.controller;

import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.constant.DocLogsConstants;
import com.sunyard.edm.dto.DocBsShapeAcceptDTO;
import com.sunyard.edm.dto.DocBsShapeMeToDTO;
import com.sunyard.edm.dto.DocSysTagDTO;
import com.sunyard.edm.service.CenterDocumentService;
import com.sunyard.edm.service.CenterShapeService;
import com.sunyard.edm.service.SysTagService;
import com.sunyard.edm.vo.DocBsShapeVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
/**
 *
 * @Author PJW
 * @date 2024/3/7 15:58:13
 * @title
 * @description  文档中心-分享中心
 */
@RestController
@RequestMapping("center/shape")
public class CenterShapeController extends BaseController {

    @Resource
    private SysTagService docSysTagService;
    @Resource
    private CenterShapeService centerShapeService;
    @Resource
    private CenterDocumentService centerDocumentService;


    /**
     * 分享给我的-分享列表
     */
    @OperationLog(DocLogsConstants.SHAPE + "分享给我的-分享列表")
    @PostMapping("queryToMe")
    Result queryToMe(DocBsShapeVO s, PageForm p) {
        s.setUserId(getToken().getId());
        s.setDeptId(getToken().getDeptId());
        s.setInstId(getToken().getInstId());
        return centerShapeService.queryToMe(s, p);
    }

    /**
     * 我的分享-分享列表
     */
    @OperationLog(DocLogsConstants.SHAPE + "我的分享-分享列表")
    @PostMapping("queryMeTo")
    Result<DocBsShapeMeToDTO> queryMeTo(DocBsShapeVO s, PageForm p) {
        s.setUserId(getToken().getId());
        s.setDeptId(getToken().getDeptId());
        s.setInstId(getToken().getInstId());
        return centerShapeService.queryMeTo(s, p);
    }

    /**
     * 分享文档详情
     */
    @OperationLog(DocLogsConstants.SHAPE + "分享文档详情")
    @PostMapping("queryDoc")
    Result queryDoc(Long shapeId, Long docId, Integer type) {
        centerShapeService.addShapePreview(shapeId, type);
        return Result.success(centerDocumentService.getInfo(docId, getToken().getId()));
    }

    /**
     * 分享文档详情
     */
    @OperationLog(DocLogsConstants.SHAPE + "外链-分享文档详情")
    @PostMapping("queryDocLink")
    Result queryDocLink(Long shapeId, Long docId) {
        centerShapeService.changeLinkValid(shapeId);
        centerShapeService.addShapePreview(shapeId, DocConstants.ONE);
        return Result.success(centerDocumentService.getInfo(docId, null));
    }

    /**
     * 外链-校验有效期
     */
    @OperationLog(DocLogsConstants.SHAPE + "外链-校验有效期")
    @PostMapping("checkValid")
    Result checkValid(Long shapeId) {
        return centerShapeService.checkValid(shapeId);
    }

    /**
     * 我的分享-分享对象详情
     */
    @OperationLog(DocLogsConstants.SHAPE + "我的分享-分享对象详情")
    @PostMapping("queryAccept")
    Result<List<DocBsShapeAcceptDTO>> queryAccept(Long shapeId) {
        return centerShapeService.queryAccept(shapeId);
    }

    /**
     * 我的分享-外部分享-查询连接、密码
     */
    @OperationLog(DocLogsConstants.SHAPE + "我的分享-外部分享-查询连接、密码")
    @PostMapping("queryLink")
    Result queryLink(Long shapeId) {
        return centerShapeService.queryLink(shapeId);
    }

    /**
     * 我的分享-取消分享
     */
    @OperationLog(DocLogsConstants.SHAPE + "我的分享-取消分享")
    @PostMapping("cancelShape")
    Result cancelShape(Long shapeId) {
        return centerShapeService.cancelShape(shapeId);
    }

    /**
     * 标签次联框
     */
    @PostMapping("getTagTree")
    @OperationLog(DocLogsConstants.SHAPE + "标签次联框")
    public Result<List<DocSysTagDTO>> getTagTree() {
        return Result.success(docSysTagService.getTagTree());
    }
}
