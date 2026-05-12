package com.sunyard.edm.controller;


import com.github.pagehelper.PageInfo;
import com.sunyard.edm.constant.DocLogsConstants;
import com.sunyard.edm.dto.DocSysHouseDTO;
import com.sunyard.edm.dto.DocSysHouseUserDTO;
import com.sunyard.edm.dto.HouseInfoDTO;
import com.sunyard.edm.dto.UserTeamDeptDTO;
import com.sunyard.edm.service.CenterCommonService;
import com.sunyard.edm.service.SysLibraryService;
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
 * @Desc 系统管理-文档库管理
 */
@RestController
@RequestMapping("sys/library")
public class SysLibraryController extends BaseController {

    @Resource
    private CenterCommonService docCommonService;
    @Resource
    private SysLibraryService sysLibraryService;

    /**
     * 文档库列表
     */
    @PostMapping("searchHouse")
    @OperationLog(DocLogsConstants.LIBRARY + DocLogsConstants.COMMON_GETLIST)
    public Result<PageInfo<DocSysHouseDTO>> searchHouse(DocSysHouseDTO houseVo, PageForm pageForm) {
        PageInfo<DocSysHouseDTO> docSysHousePageInfo = sysLibraryService.queryHouseList(houseVo, pageForm, getToken());
        return Result.success(docSysHousePageInfo);
    }


    /**
     * 新增
     */
    @PostMapping("addHouse")
    @OperationLog(DocLogsConstants.LIBRARY + DocLogsConstants.COMMON_ADD)
    public Result addHouse(@RequestBody DocSysHouseDTO houseVo) {
        sysLibraryService.addHouse(houseVo);
        return Result.success(true);
    }

    /**
     * 查询详情
     */
    @PostMapping("getInfo")
    @OperationLog(DocLogsConstants.LIBRARY + DocLogsConstants.COMMON_GETLIST)
    public Result<HouseInfoDTO> getInfo(Long houseId, PageForm pageForm) {
        return Result.success(sysLibraryService.getInfo(getToken(), houseId, pageForm));
    }

    /**
     * 计算权限 机构 部门 团队 用户 的数量
     */
    @PostMapping("getTypeNum")
    @OperationLog(DocLogsConstants.LIBRARY + DocLogsConstants.COMMON_GETLIST)
    public Result<HouseInfoDTO> getTypeNum(Long houseId) {
        return Result.success(sysLibraryService.getTypeNum(houseId));
    }

    /**
     * 修改
     */
    @PostMapping("updateHouse")
    @OperationLog(DocLogsConstants.LIBRARY + DocLogsConstants.COMMON_UPDATE)
    public Result updateHouse(@RequestBody DocSysHouseDTO houseVo) {
        sysLibraryService.updateHouse(houseVo, getToken());
        return Result.success(true);
    }


    /**
     * 删除
     */
    @PostMapping("delHouse")
    @OperationLog(DocLogsConstants.LIBRARY + DocLogsConstants.COMMON_DELETE)
    public Result delHouse(@RequestBody Long[] houseIds) {
        sysLibraryService.delHouse(houseIds, getToken());
        return Result.success(true);
    }

    /**
     * 修改-展示原有文档库关联的成员
     */
    @PostMapping("selectHouseUser")
    @OperationLog(DocLogsConstants.LIBRARY + DocLogsConstants.COMMON_GETLIST)
    public Result<List<DocSysHouseUserDTO>> selectHouseUser(Long houseId) {
        return Result.success(sysLibraryService.selectHouseUser(houseId));
    }

    /**
     * 获取文件夹的顺序号
     */
    @PostMapping("getFolderSeq")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + "获取文件夹的顺序号")
    public Result<Integer> getFolderSeq() {
        return Result.success(sysLibraryService.getFolderSeq());
    }

    /**
     * 获取组织团队用户列表
     */
    @PostMapping("getInstTeamUser")
    @OperationLog(DocLogsConstants.LIBRARY + "获取组织团队用户列表")
    public Result<UserTeamDeptDTO> getInstTeamUser(UserTeamDeptDTO extend, PageForm pageForm) {
        return Result.success(docCommonService.getUserByDeptOrTeam(extend, pageForm, getToken()));
    }
}
