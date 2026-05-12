package com.sunyard.module.system.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.dto.SysPostListDTO;
import com.sunyard.module.system.service.SysPostService;
import com.sunyard.module.system.vo.SysPostVO;

/**
 * 基础管理/通用管理/岗位管理
 *
 * @Author wangmeiling 2025/9/5
 */
@RestController
@RequestMapping("basics/currency/post")
public class BasicsCurrencyPostController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.MENU_CURRENCY + "-岗位管理->";
    @Resource
    private SysPostService sysPostService;

    /**
     * 分页查询岗位
     *
     * @param sysPostListDTO   查询参数
     * @return result
     */
    @OperationLog(BASELOG + "查询岗位")
    @PostMapping("search")
    public Result search(@RequestBody SysPostListDTO sysPostListDTO) {
        return Result.success(sysPostService.search(sysPostListDTO));
    }

    /**
     * 查询所有岗位
     *
     * @param instId   组织id
     * @return result
     */
    @OperationLog(BASELOG + "查询所有岗位")
    @PostMapping("searchAll")
    public Result searchAll(Long instId, Long deptId) {
        return Result.success(sysPostService.searchAll(instId, deptId));
    }

    /**
     * 查询单个岗位详情
     *
     * @param postId   岗位id
     * @return result
     */
    @OperationLog(BASELOG + "查询单个岗位详情")
    @PostMapping("select")
    public Result select(Long postId) {
        return Result.success(sysPostService.select(postId));
    }

    /**
     * 新增岗位
     *
     * @param sysPostVO 岗位
     * @return result
     */
    @OperationLog(BASELOG + "新增岗位")
    @PostMapping("add")
    public Result add(@RequestBody SysPostVO sysPostVO) {
        Long userId = getToken().getId();
        sysPostService.add(sysPostVO, userId);
        return Result.success(true);
    }

    /**
     * 修改岗位信息
     *
     * @param sysPostVO 岗位obj
     * @return result
     */
    @OperationLog(BASELOG + "修改岗位信息")
    @PostMapping("edit")
    public Result edit(@RequestBody SysPostVO sysPostVO) {
        Long userId = getToken().getId();
        sysPostService.edit(sysPostVO, userId);
        return Result.success(true);
    }

    /**
     * 删除岗位信息
     *
     * @param postId 岗位id
     * @return result
     */
    @OperationLog(BASELOG + "删除岗位信息")
    @PostMapping("delete")
    public Result delete(Long postId) {
        sysPostService.delete(postId);
        return Result.success(true);
    }

}
