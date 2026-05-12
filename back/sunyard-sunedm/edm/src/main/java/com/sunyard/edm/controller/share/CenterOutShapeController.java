package com.sunyard.edm.controller.share;

import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.constant.DocLogsConstants;
import com.sunyard.edm.controller.BaseController;
import com.sunyard.edm.service.share.CenterOutShapeService;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @desc 对外分享专用
 */
@RestController
@RequestMapping("center/outShape")
public class CenterOutShapeController extends BaseController {

    @Resource
    private CenterOutShapeService centerOutShapeService;

    /**
     * 分享文档详情
     *
     * @param shapeId 文档id
     * @param docId   文档id
     */
    @OperationLog(DocLogsConstants.SHAPE + "外链-分享文档详情")
    @PostMapping("queryDocLinkOut")
    Result queryDocLink(Long shapeId, Long docId) {
        centerOutShapeService.changeLinkValid(shapeId);
        centerOutShapeService.addShapePreview(shapeId, DocConstants.ONE);
        return Result.success(centerOutShapeService.getInfo(docId, null));
    }

    /**
     * 外链-校验有效期
     *
     * @param shapeId 文档id
     */
    @OperationLog(DocLogsConstants.SHAPE + "外链-校验有效期")
    @PostMapping("checkValidOut")
    Result checkValid(Long shapeId) {
        return centerOutShapeService.checkValid(shapeId);
    }

}
