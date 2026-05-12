package com.sunyard.edm.controller;


import com.github.pagehelper.PageInfo;
import com.sunyard.edm.constant.DocLogsConstants;
import com.sunyard.edm.dto.AnnonInfoDTO;
import com.sunyard.edm.dto.DocSysAnnounDTO;
import com.sunyard.edm.dto.DocSysAnnounUserDTO;
import com.sunyard.edm.dto.UserTeamDeptDTO;
import com.sunyard.edm.po.DocSysAnnoun;
import com.sunyard.edm.service.CenterCommonService;
import com.sunyard.edm.service.SysLibraryService;
import com.sunyard.edm.service.SysNoticeService;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author PJW 2022/12/12 14:01
 * @desc 系统管理-公告管理
 *
 */
@RestController
@RequestMapping("sys/notice")
public class SysNoticeController extends BaseController {

    @Resource
    private SysNoticeService sysNoticeService;
    @Resource
    private CenterCommonService docCommonService;
    @Resource
    private SysLibraryService sysLibraryService;
    /**
     * 公告列表
     */
    @PostMapping("searchAnnoun")
    @OperationLog(DocLogsConstants.ANNOUN + DocLogsConstants.COMMON_GETLIST)
    public Result<PageInfo<DocSysAnnoun>> searchAnnoun(DocSysAnnounDTO docSysAnnounVo, PageForm pageForm) {
        PageInfo<DocSysAnnoun> docSysAnnounPageInfo = sysNoticeService.searchAnnoun(docSysAnnounVo, pageForm, getToken());
        return Result.success(docSysAnnounPageInfo);
    }


    /**
     * 新增
     */
    @PostMapping("addAnnoun")
    @OperationLog(DocLogsConstants.ANNOUN + DocLogsConstants.COMMON_ADD)
    public Result addAnnoun(@RequestBody DocSysAnnounDTO docSysAnnounVo) {
        sysNoticeService.addAnnoun(docSysAnnounVo);
        return Result.success(true);
    }

    /**
     * 查询详情
     */
    @PostMapping("getInfo")
    @OperationLog(DocLogsConstants.ANNOUN + DocLogsConstants.COMMON_GETLIST)
    public Result<AnnonInfoDTO> getInfo(Long ananounId, PageForm pageForm) {
        return Result.success(sysNoticeService.getInfo(ananounId, pageForm));
    }

    /**
     * 修改-展示原有公告关联的成员
     */
    @PostMapping("selectHouseUser")
    @OperationLog(DocLogsConstants.ANNOUN + DocLogsConstants.COMMON_GETLIST)
    public Result<List<DocSysAnnounUserDTO>> selectAnnounUser(Long announId) {
        return Result.success(sysNoticeService.selectAnnounUser(announId));
    }

    /**
     * 计算权限 机构 部门 团队 用户 的数量
     */
    @PostMapping("getTypeNum")
    @OperationLog(DocLogsConstants.ANNOUN + DocLogsConstants.COMMON_GETLIST)
    public Result<AnnonInfoDTO> getTypeNum(Long announId) {
        return Result.success(sysNoticeService.getTypeNum(announId));
    }

    /**
     * 修改
     */
    @PostMapping("updateAnnoun")
    @OperationLog(DocLogsConstants.ANNOUN + DocLogsConstants.COMMON_UPDATE)
    public Result updateAnnoun(@RequestBody DocSysAnnounDTO docSysAnnounVo) {
        sysNoticeService.updateAnnoun(docSysAnnounVo);
        return Result.success(true);
    }


    /**
     * 删除
     */
    @PostMapping("delAnnoun")
    @OperationLog(DocLogsConstants.ANNOUN + DocLogsConstants.COMMON_DELETE)
    public Result delAnnoun(Long ananounId) {
        sysNoticeService.delAnnoun(ananounId);
        return Result.success(true);
    }


    /**
     * 获取组织团队用户列表
     */
    @PostMapping("getInstTeamUser")
    @OperationLog(DocLogsConstants.ANNOUN + "获取组织团队用户列表")
    public Result<UserTeamDeptDTO> getInstTeamUser(UserTeamDeptDTO extend, PageForm pageForm) {
        return Result.success(docCommonService.getUserByDeptOrTeam(extend, pageForm, getToken()));
    }
}
