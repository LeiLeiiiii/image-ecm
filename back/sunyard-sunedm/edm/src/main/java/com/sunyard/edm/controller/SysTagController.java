package com.sunyard.edm.controller;

import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.constant.DocLogsConstants;
import com.sunyard.edm.dto.DocSysTagDTO;
import com.sunyard.edm.dto.PromptDTO;
import com.sunyard.edm.po.DocSysTag;
import com.sunyard.edm.service.SysTagService;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author huronghao
 * @Type
 * @Desc 系统管理-标签管理
 * @date 2022-12-12 14:40
 */
@RestController
@RequestMapping("sys/tag")
public class SysTagController extends BaseController {

    @Resource
    private SysTagService tagService;

    /**
     * 标签列表
     */
    @PostMapping("selectTag")
    @OperationLog(DocLogsConstants.SYS_TAG + DocLogsConstants.COMMON_GETLIST)
    public Result<List<DocSysTagDTO>> selectTag(String tagName) {
        return Result.success(tagService.selectTag(tagName));
    }

    /**
     * 子级标签
     */
    @PostMapping("selectChild")
    @OperationLog(DocLogsConstants.SYS_TAG + "查询标签子级")
    public Result<List<DocSysTagDTO>> selectChild(Long tagId) {
        return Result.success(tagService.selectChild(tagId));
    }

    /**
     * 添加标签
     */
    @PostMapping("addTag")
    @OperationLog(DocLogsConstants.SYS_TAG + DocLogsConstants.COMMON_ADD)
    public Result<String> addTag(@RequestBody DocSysTag tag) {
        return Result.success(tagService.addTag(tag) > DocConstants.ZERO ? "操作成功" : "操作失败");
    }

    /**
     * 编辑标签
     */
    @PostMapping("updateTag")
    @OperationLog(DocLogsConstants.SYS_TAG + DocLogsConstants.COMMON_UPDATE)
    public Result<String> updateTag(@RequestBody DocSysTag tag) {
        return Result.success(tagService.updateTag(tag) > DocConstants.ZERO ? "操作成功" : "操作失败");
    }

    /**
     * 删除提示
     */
    @PostMapping("delPrompt")
    @OperationLog(DocLogsConstants.SYS_TAG + "删除提示")
    public Result<PromptDTO> delPrompt(Long[] tagIds) {
        return Result.success(tagService.delPrompt(tagIds));
    }

    /**
     * 删除标签批量
     */
    @PostMapping("delBatchTag")
    @OperationLog(DocLogsConstants.SYS_TAG + DocLogsConstants.COMMON_DELETE)
    public Result delBatchTag(Long[] tagIds) {
        tagService.delBatchTag(tagIds);
        return Result.success(true);
    }

}
