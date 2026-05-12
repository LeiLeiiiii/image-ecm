package com.sunyard.edm.controller;

import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.constant.DocLogsConstants;
import com.sunyard.edm.dto.DocBsDocumentDTO;
import com.sunyard.edm.dto.DocBsDocumentUserDTO;
import com.sunyard.edm.dto.DocSysHouseDTO;
import com.sunyard.edm.dto.PromptDTO;
import com.sunyard.edm.dto.UserTeamDeptDTO;
import com.sunyard.edm.dto.UserTeamDeptInitDTO;
import com.sunyard.edm.service.CenterCommonService;
import com.sunyard.edm.service.SysCapacityService;
import com.sunyard.edm.service.SysFolderService;
import com.sunyard.edm.vo.DocBsDocumentVO;
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
 * @author huronghao
 * @Type
 * @Desc 系统管理-文件夹管理
 * @date 2022-12-13 16:39
 */
@RestController
@RequestMapping("sys/folder")
public class SysFolderController extends BaseController {

    @Resource
    private SysFolderService folderService;
    @Resource
    private CenterCommonService commonService;
    @Resource
    private SysCapacityService sysCapacityService;

    /**
     * 文件夹列表
     */
    @PostMapping("selectFolder")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + DocLogsConstants.COMMON_GETLIST)
    public Result<List<DocBsDocumentDTO>> selectFolder(Long houseId, String folderName) {
        return Result.success(folderService.selectFolder(houseId, folderName, getToken()));
    }

    /**
     * 子级文件夹
     */
    @PostMapping("selectChild")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + "查询文件夹子级")
    public Result<List<DocBsDocumentDTO>> selectChild(Long busId) {
        return Result.success(folderService.selectChild(busId, getToken()));
    }

    /**
     * 文件夹详情
     */
    @PostMapping("getInfoFolder")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + DocLogsConstants.COMMON_GETINFO)
    public Result<DocBsDocumentDTO> getInfoFolder(Long busId, PageForm pageForm) {
        DocBsDocumentDTO infoFolder = folderService.getInfoFolder(busId, pageForm);
        return Result.success(infoFolder);
    }

    /**
     * 权限详情
     */
    @PostMapping("getInfoAuth")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + DocLogsConstants.COMMON_GETINFO)
    public Result<List<DocBsDocumentUserDTO>> getInfoAuth(Long busId) {
        List<DocBsDocumentUserDTO> infoAuth = folderService.getInfoAuth(busId);
        return Result.success(infoAuth);
    }

    /**
     * 添加文件夹
     */
    @PostMapping("addFolder")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + DocLogsConstants.COMMON_ADD)
    public Result addFolder(@RequestBody DocBsDocumentVO document) {
        folderService.addFolder(document, getToken());
        return Result.success(true);
    }

    /**
     * 编辑文件夹
     */
    @PostMapping("updateFolder")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + DocLogsConstants.COMMON_UPDATE)
    public Result updateFolder(@RequestBody DocBsDocumentVO document) {
        folderService.updateFolder(document, getToken());
        return Result.success(true);
    }

    /**
     * 删除提示
     */
    @PostMapping("delPrompt")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + DocLogsConstants.COMMON_DELETE)
    public Result<PromptDTO> delPrompt(Long[] busIds) {
        return Result.success(folderService.delPrompt(busIds, getToken(), DocConstants.COMPANY));
    }

    /**
     * 删除文件夹批量
     */
    @PostMapping("delBatchFolder")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + DocLogsConstants.COMMON_DELETE)
    public Result delBatchFolder(Long[] busIds) {
        folderService.delBatchFolder(busIds, getToken());
        return Result.success(true);
    }

    /**
     * 上级文件夹
     */
    @PostMapping("getFolderTree")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + "文件夹树")
    public Result<List<DocBsDocumentDTO>> getFolderTree(Long houseId) {
        return Result.success(folderService.getFolderTree(houseId));
    }


    /**
     * 复用上级权限
     */
    @PostMapping("reuseAuth")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + "复用上级权限")
    public Result<UserTeamDeptInitDTO> reuseAuth(Long parentId, Integer type) {
        return Result.success(commonService.reuseAuth(parentId, type));
    }

    /**
     * 获取文件夹的顺序号
     */
    @PostMapping("getFolderSeq")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + "获取文件夹的顺序号")
    public Result<Integer> getFolderSeq(Long parentId, Long houseId) {
        return Result.success(commonService.getFolderSeq(parentId, houseId, getToken(), DocConstants.COMPANY));
    }

    /**
     * 获取组织团队用户列表
     */
    @PostMapping("getUserByDeptOrTeam")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + "获取组织团队用户列表")
    public Result<UserTeamDeptDTO> getUserByDeptOrTeam(UserTeamDeptDTO extend, PageForm pageForm) {
        UserTeamDeptDTO ret = commonService.getUserByDeptOrTeam(extend, pageForm, getToken());
        return Result.success(ret);
    }

    /**
     * 文档库切换
     */
    @PostMapping("searchHouse")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + "文档库切换")
    public Result<List<DocSysHouseDTO>> searchHouse() {
        List<DocSysHouseDTO> docSysHouseDTOList = commonService.queryHouseList(getToken());
        return Result.success(docSysHouseDTOList);
    }


    /**
     * 回收站天数
     */
    @PostMapping("getDelDay")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "获取回收站天数")
    public Result<String> getDelDay() {
        return Result.success(sysCapacityService.getDelDay());
    }

}
