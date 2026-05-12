package com.sunyard.edm.controller;


import com.github.pagehelper.PageInfo;
import com.sunyard.edm.constant.DocLogsConstants;
import com.sunyard.edm.dto.DocSysTeamDTO;
import com.sunyard.edm.dto.TeamInfoDTO;
import com.sunyard.edm.po.DocSysTeam;
import com.sunyard.edm.service.SysTeamService;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.api.dto.SysRoleDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author PJW 2022/12/12 14:01
 * @Desc 系统管理-团队管理
 */
@RestController
@RequestMapping("sys/team")
public class SysTeamController extends BaseController {

    @Resource
    private SysTeamService teamService;

    /**
     * 团队列表
     */
    @PostMapping("searchTeam")
    @OperationLog(DocLogsConstants.TEAM + DocLogsConstants.COMMON_GETLIST)
    public Result<PageInfo<DocSysTeam>> searchTeam(DocSysTeamDTO docSysTeamVo, PageForm pageForm) {
        PageInfo<DocSysTeam> docSysTeamPageInfo = teamService.searchTeam(docSysTeamVo, pageForm);
        return Result.success(docSysTeamPageInfo);
    }


    /**
     * 新增
     */
    @PostMapping("addTeam")
    @OperationLog(DocLogsConstants.TEAM + DocLogsConstants.COMMON_ADD)
    public Result addTeam(@RequestBody DocSysTeamDTO docSysTeamVo) {
        teamService.addTeam(docSysTeamVo);
        return Result.success(true);
    }


    /**
     * 查询详情
     */
    @PostMapping("getInfo")
    @OperationLog(DocLogsConstants.TEAM + DocLogsConstants.COMMON_GETLIST)
    public Result<TeamInfoDTO> getInfo(Long teamId, PageForm pageForm) {
        return Result.success(teamService.getInfo(teamId, pageForm));
    }


    /**
     * 修改
     */
    @PostMapping("updateTeam")
    @OperationLog(DocLogsConstants.TEAM + DocLogsConstants.COMMON_UPDATE)
    public Result updateTeam(@RequestBody DocSysTeamDTO docSysTeamVo) {
        teamService.updateTeam(docSysTeamVo);
        return Result.success(true);
    }

    /**
     * 修改-展示原有团队的成员
     */
    @PostMapping("selectTeamUser")
    @OperationLog(DocLogsConstants.TEAM + DocLogsConstants.COMMON_GETLIST)
    public Result<List<SysUserDTO>> selectTeamUser(Long teamId) {
        return Result.success(teamService.selectTeamUser(teamId));
    }

    /**
     * 计算权限 机构 部门 团队 用户 的数量
     */
    @PostMapping("getTypeNum")
    @OperationLog(DocLogsConstants.LIBRARY + DocLogsConstants.COMMON_GETLIST)
    public Result<TeamInfoDTO> getTypeNum(Long teamId) {
        return Result.success(teamService.getTypeNum(teamId));
    }


    /**
     * 删除
     */
    @PostMapping("delTeam")
    @OperationLog(DocLogsConstants.TEAM + DocLogsConstants.COMMON_DELETE)
    public Result delTeam(@RequestBody Long[] teamIds) {
        teamService.delTeam(teamIds);
        return Result.success(true);
    }


    /**
     * 获取成员列表
     */
    @PostMapping("getUser")
    @OperationLog(DocLogsConstants.TEAM + "获取组织团队用户列表")
    public Result<PageInfo<SysUserDTO>> getInstTeamUser(SysUserDTO userExtend, PageForm pageForm) {
        return Result.success(teamService.getInstTeamUser(userExtend, pageForm));
    }

    /**
     * 获取角色列表
     */
    @PostMapping("getRole")
    @OperationLog(DocLogsConstants.TEAM + "获取角色列表")
    public Result<List<SysRoleDTO>> getRole() {
        return Result.success(teamService.getRole(getToken()));
    }
}
