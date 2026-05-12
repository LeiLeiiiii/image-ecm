package com.sunyard.edm.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.constant.DocLogsConstants;
import com.sunyard.edm.dto.AnnonInfoDTO;
import com.sunyard.edm.dto.DocBsHomeDTO;
import com.sunyard.edm.dto.DocBsMessageDTO;
import com.sunyard.edm.dto.DocBsRecentlyDTO;
import com.sunyard.edm.service.CenterDocumentService;
import com.sunyard.edm.service.CenterShapeService;
import com.sunyard.edm.service.SysNoticeService;
import com.sunyard.edm.service.WorkbenchService;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;

/**
 * @Author PJW 2022/12/12 14:03
 * @desc 首页-工作台
 */
@RestController
@RequestMapping("workbench")
public class WorkbenchController extends BaseController {

    @Resource
    private WorkbenchService workbenchService;
    @Resource
    private CenterShapeService centerShapeService;
    @Resource
    private CenterDocumentService centerDocumentService;
    @Resource
    private SysNoticeService sysNoticeService;

    /**
     * 查询首页-公告栏模块的数据：全部
     *
     * @param pageForm
     */
    @OperationLog(DocLogsConstants.HOME + "查询首页-公告栏模块的数据：全部")
    @PostMapping("queryNotice")
    public Result<DocBsHomeDTO> queryNotice(PageForm pageForm) {
        return workbenchService.queryNotice(getToken().getId(), getToken().getDeptId(),
                getToken().getInstId(), pageForm);
    }

    /**
     * 查询首页-分享中心模块的数据：最新的5条
     */
    @OperationLog(DocLogsConstants.HOME + "查询首页-分享中心模块的数据：最新的5条")
    @PostMapping("queryShape")
    public Result<List<DocBsHomeDTO>> queryShape() {
        return workbenchService.queryShape(getToken().getId(), getToken().getDeptId(),
                getToken().getInstId());
    }

    /**
     * 查询首页-最新消息模块的数据：最新的5条
     */
    @OperationLog(DocLogsConstants.HOME + "查询首页-最新消息模块的数据：最新的5条")
    @PostMapping("queryMessage")
    public Result<List<DocBsHomeDTO>> queryMessage() {
        return workbenchService.queryMessage(getToken().getId());
    }

    /**
     * 查询首页-最新消息模块的数据总条数
     */
    @OperationLog(DocLogsConstants.HOME + "查询首页-最新消息模块的数据总条数")
    @PostMapping("queryMessageCount")
    public Result<Integer> queryMessageCount() {
        return workbenchService.queryMessageCount(getToken().getId());
    }

    /**
     * 查询'消息通知'数据：30天之内
     */
    @OperationLog(DocLogsConstants.HOME + "查询'消息通知'数据：30天之内")
    @PostMapping("queryMessageAll")
    public Result<List<DocBsMessageDTO>> queryMessageAll() {
        return workbenchService.queryMessageAll(getToken().getId(), getToken().getDeptId(),
                getToken().getInstId());
    }

    /**
     * 查询'最近打开'数据：最新的10条
     */
    @OperationLog(DocLogsConstants.HOME + "查询'最近打开'数据：最新的10条")
    @PostMapping("queryRecently")
    public Result<List<DocBsRecentlyDTO>> queryRecently() {
        return workbenchService.queryRecently(getToken().getId(), getToken().getDeptId(),
                getToken().getInstId());
    }

    /**
     * 消息接收范围设置
     *
     * @param key 消息范围 id数组
     */
    @OperationLog(DocLogsConstants.HOME + "消息接收范围设置")
    @PostMapping("msgRangeSet")
    public Result msgRangeSet(Integer[] key) {
        return workbenchService.msgRangeSet(key, getToken().getId());
    }

    /**
     * 消息接收范围查询
     */
    @OperationLog(DocLogsConstants.HOME + "消息接收范围查询")
    @PostMapping("msgRangeQuery")
    public Result msgRangeQuery() {
        return workbenchService.msgRangeQuery(getToken().getId());
    }

    /**
     * 一键已读
     */
    @OperationLog(DocLogsConstants.HOME + "一键已读")
    @PostMapping("oneButtonRead")
    public Result oneButtonRead() {
        return workbenchService.oneButtonRead(getToken().getId());
    }

    /**
     * 单独已读
     *
     * @param messageId 消息id
     */
    @OperationLog(DocLogsConstants.HOME + "单独已读")
    @PostMapping("onlyRead")
    public Result onlyRead(Long messageId) {
        return workbenchService.onlyRead(messageId);
    }

    /**
     * 分享文档详情
     *
     * @param shapeId 分享id
     * @param docId   文档id
     */
    @OperationLog(DocLogsConstants.SHAPE + "分享文档详情")
    @PostMapping("queryDoc")
    Result queryDoc(Long shapeId, Long docId) {
        centerShapeService.addShapePreview(shapeId, DocConstants.ONE);
        return Result.success(centerDocumentService.getInfo(docId, getToken().getId()));
    }

    /**
     * 查询详情
     */
    @PostMapping("getInfo")
    @OperationLog(DocLogsConstants.ANNOUN + DocLogsConstants.COMMON_GETLIST)
    public Result<AnnonInfoDTO> getInfo(Long ananounId, PageForm pageForm) {
        return Result.success(sysNoticeService.getInfo(ananounId, pageForm));
    }
}
