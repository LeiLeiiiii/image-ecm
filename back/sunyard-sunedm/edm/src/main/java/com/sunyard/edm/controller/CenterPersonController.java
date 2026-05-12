package com.sunyard.edm.controller;


import com.github.pagehelper.PageInfo;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.constant.DocLogsConstants;
import com.sunyard.edm.dto.DocBsDocumentDTO;
import com.sunyard.edm.dto.DocBsShapeLinkDTO;
import com.sunyard.edm.dto.DocCapacityDTO;
import com.sunyard.edm.dto.DocSysHouseDTO;
import com.sunyard.edm.dto.DocSysTagDTO;
import com.sunyard.edm.dto.PromptDTO;
import com.sunyard.edm.dto.UserTeamDeptDTO;
import com.sunyard.edm.dto.UserTeamDeptInitDTO;
import com.sunyard.edm.po.DocBsDocument;
import com.sunyard.edm.service.CenterCollectionService;
import com.sunyard.edm.service.CenterDocumentService;
import com.sunyard.edm.service.CenterFolderService;
import com.sunyard.edm.service.CenterRecycleService;
import com.sunyard.edm.service.CenterShapeService;
import com.sunyard.edm.service.CenterCommonService;
import com.sunyard.edm.service.SysFolderService;
import com.sunyard.edm.service.SysCapacityService;
import com.sunyard.edm.service.SysTagService;
import com.sunyard.edm.vo.AddOrUpdateDocumentVO;
import com.sunyard.edm.vo.DocBsDocumentSearchVO;
import com.sunyard.edm.vo.DocBsDocumentVO;
import com.sunyard.edm.vo.DocBsRecycleSearchVO;
import com.sunyard.edm.vo.DocBsShapeAddVO;
import com.sunyard.edm.vo.UpgradeCompanyVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.log.annotation.OperationLog;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @Author PJW 2022/12/12 14:01
 * @desc 文档中心-个人文档库
 */
@RestController
@RequestMapping("center/person")
public class CenterPersonController extends BaseController {
    @Resource
    private SysCapacityService sysCapacityService;
    @Resource
    private SysTagService docSysTagService;
    @Resource
    private CenterDocumentService centerDocumentService;
    @Resource
    private CenterFolderService centerFolderService;
    @Resource
    private CenterShapeService centerShapeService;
    @Resource
    private CenterRecycleService centerRecycleService;
    @Resource
    private SysFolderService folderService;
    @Resource
    private CenterCommonService docCommonService;
    @Resource
    private CenterCollectionService centerCollectionService;

    /**
     * 文件夹树
     */
    @PostMapping("tree")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + DocLogsConstants.COMMON_GETLIST)
    public Result<List<DocBsDocumentDTO>> tree() {
        List<DocBsDocumentDTO> ret = centerFolderService.getFolderTree(getToken(), null, DocConstants.PERSON);
        return Result.success(ret);
    }

    /**
     * 文档库列表
     */
    @PostMapping("search")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + DocLogsConstants.COMMON_GETLIST)
    public Result<PageInfo<DocBsDocumentDTO>> search(@RequestBody DocBsDocumentSearchVO docBsDocumentExtend) {
        docBsDocumentExtend.setDocType(DocConstants.PERSON);
        PageForm pageForm = new PageForm();
        pageForm.setPageNum(docBsDocumentExtend.getPageNum());
        pageForm.setPageSize(docBsDocumentExtend.getPageSize());
        PageInfo<DocBsDocumentDTO> ret = centerDocumentService.search(getToken(), docBsDocumentExtend, pageForm);
        return Result.success(ret);
    }


    /**
     * 文档库列表
     */
    @PostMapping("searchNoFolder")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + DocLogsConstants.COMMON_GETLIST)
    public Result<PageInfo<DocBsDocumentDTO>> searchNoFolder(DocBsDocumentSearchVO docBsDocumentExtend, PageForm pageForm) {
        docBsDocumentExtend.setDocType(DocConstants.PERSON);
        PageInfo<DocBsDocumentDTO> ret = centerDocumentService.searchNoFolder(getToken(), docBsDocumentExtend, pageForm);
        return Result.success(ret);
    }

    /**
     * 文件上传
     */
    @PostMapping("addUpload")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "单个上传包含文件上传")
    public Result addUpload(
            HttpServletRequest request, AddOrUpdateDocumentVO docBsDocumentExtend) throws Exception {
        MultipartHttpServletRequest request1 = (MultipartHttpServletRequest) request;
        List<MultipartFile> file11 = request1.getFiles("file1");
        AssertUtils.isNull(file11, "参数错误");
        List<MultipartFile> file12 = request1.getFiles("file2");
        return Result.success(centerDocumentService.addUpload(file11.get(0), file12, getToken(), docBsDocumentExtend, DocConstants.PERSON));
    }

    /**
     * 文件上传
     */
    @PostMapping("batchAddUpload")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "批量上传包含文件上传")
    public Result batchAddUpload(@RequestParam("file1") List<MultipartFile> file1, AddOrUpdateDocumentVO docBsDocumentExtend) throws Exception {
        return Result.success(centerDocumentService.batchAddUpload(file1, getToken(), docBsDocumentExtend, DocConstants.PERSON));
    }

    /**
     * 编辑
     */
    @PostMapping("updateUpload")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "编辑")
    public Result updateUpload(HttpServletRequest request, AddOrUpdateDocumentVO docBsDocumentExtend) throws Exception {
        MultipartHttpServletRequest request1 = (MultipartHttpServletRequest) request;
        List<MultipartFile> file11 = request1.getFiles("file1");
        List<MultipartFile> file12 = request1.getFiles("file2");
        docBsDocumentExtend.setDocType(DocConstants.PERSON);
        return Result.success(centerDocumentService.updateUpload(file11 != null && file11.size() > 0 ? file11.get(0) : null, file12, getToken(), docBsDocumentExtend));
    }

    /**
     * 删除文档
     */
    @PostMapping("delDoc")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "删除文档")
    public Result delDoc(Long busId) {
        centerDocumentService.delDoc(busId, DocConstants.PERSON, getToken());
        return Result.success(true);
    }

    /**
     * 文档名称重命名
     */
    @PostMapping("updateDocumentName")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "文档名称重命名")
    public Result<DocBsDocument> updateDocumentName(Long busId, Long folderId, String docName) {
        return Result.success(centerDocumentService.updateDocumentName(busId, folderId, docName, DocConstants.PERSON, getToken()));
    }

    /**
     * 文档名称重命名校验
     */
    @PostMapping("checkDocumentName")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "文档名称重命名校验")
    public Result<List<Map>> checkDocumentName(Long folderId, String[] docNames) {
        return Result.success(centerDocumentService.checkDocumentName(folderId, docNames));
    }


    /**
     * 删除提示
     */
    @PostMapping("delPrompt")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + DocLogsConstants.COMMON_DELETE)
    public Result<PromptDTO> delPrompt(@RequestBody Long[] busIds) {
        return Result.success(folderService.delPrompt(busIds, getToken(), DocConstants.PERSON));
    }

    /**
     * 文档详情
     */
    @PostMapping("getInfo")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "详情")
    public Result<DocBsDocumentDTO> getInfo(Long busId) {
        return Result.success(centerDocumentService.getInfo(busId, getToken().getId()));
    }

    /**
     * 获取企业库名称列表
     */
    @PostMapping("queryHouseList")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "获取文档标签树")
    public Result<List<DocSysHouseDTO>> queryHouseList() {
        List<DocSysHouseDTO> list = docCommonService.queryHouseList(getToken());
        return Result.success(list);
    }

    /**
     * 获取企业库目录
     */
    @PostMapping("queryFolderTree")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "获取企业库目录")
    public Result<List<DocBsDocumentDTO>> queryFolderTree(Long houseId) {
        List<DocBsDocumentDTO> list = centerFolderService.queryFolderTree(getToken(), houseId);
        return Result.success(list);
    }

    /**
     * 标签列表
     */
    @PostMapping("selectTag")
    @OperationLog(DocLogsConstants.SYS_TAG + DocLogsConstants.COMMON_GETLIST)
    public Result<List<DocSysTagDTO>> selectTag(String tagName) {
        return Result.success(docSysTagService.selectTag(tagName));
    }

    /**
     * 子级标签
     */
    @PostMapping("selectChild")
    @OperationLog(DocLogsConstants.SYS_TAG + "查询标签子级")
    public Result<List<DocSysTagDTO>> selectChild(Long tagId) {
        return Result.success(docSysTagService.selectChild(tagId));
    }


    /**
     * 上架到企业文档库
     */
    @PostMapping("upgradeCompany")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "上架企业文档库-保存")
    public Result upgradeCompany(@RequestBody UpgradeCompanyVO upgradeCompanyVo) {
        centerDocumentService.upgradeCompany(upgradeCompanyVo);
        return Result.success(true);
    }


    /**
     * 复用上级权限
     */
    @PostMapping("reuseAuth")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "上架企业文档库-复用上级权限")
    public Result<UserTeamDeptInitDTO> reuseAuth(Long parentId, Integer type) {
        return Result.success(docCommonService.reuseAuth(parentId, type));
    }

//    /**
//     * 移动到文件夹树列表
//     */
//    @PostMapping("moveFolderTree")
//    @OperationLog(DocLogsConstants.BS_DOCUMENT + "移动到文件夹树列表")
//    public Result<List<DocBsDocumentDTO>> moveFolderTree() {
//        List<DocBsDocumentDTO> ret = centerFolderService.moveFolderTree(getToken(), null, DocConstants.PERSON);
//        return Result.success(ret);
//    }


    /**
     * 移动文档
     */
    @PostMapping("moveDoc")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "移动文档")
    public Result moveDoc(Long busId, Long folderId) {
        centerDocumentService.moveDoc(getToken(), busId, folderId, DocConstants.PERSON);
        return Result.success(true);
    }

    /**
     * 删除文件夹
     */
    @PostMapping("delFolder")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "删除文件夹")
    public Result delFolder(Long folderId) {
        centerFolderService.delFolder(getToken(), folderId, DocConstants.PERSON);
        return Result.success(true);
    }

    /**
     * 新增文件夹
     */
    @PostMapping("addFolder")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "新增文件夹")
    public Result addFolder(@RequestBody DocBsDocumentDTO docBsDocument) {
        docBsDocument.setDocOwner(getToken().getId());
        docBsDocument.setDocCreator(getToken().getId());
        docBsDocument.setDocType(DocConstants.PERSON);
        centerFolderService.addFolder(getToken(), docBsDocument);
        return Result.success(true);
    }

    /**
     * 修改文件夹
     */
    @PostMapping("updateFolder")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "修改文件夹")
    public Result updateFolder(@RequestBody DocBsDocumentVO docBsDocument) {
        centerFolderService.updateFolder(getToken(), docBsDocument, DocConstants.PERSON);
        return Result.success(true);
    }


    /**
     * 获取组织团队用户列表
     */
    @PostMapping("getUserByDeptOrTeam")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "获取组织团队用户列表")
    public Result<UserTeamDeptDTO> getUserByDeptOrTeam(UserTeamDeptDTO extend, PageForm pageForm) {
        UserTeamDeptDTO ret = docCommonService.getUserByDeptOrTeam(extend, pageForm, getToken());
        return Result.success(ret);
    }

    /**
     * 彻底删除
     */
    @PostMapping("delDocOrFolder")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "彻底删除")
    public Result delDocOrFolder(@RequestBody Long[] recycles) {
        centerRecycleService.delDoc(recycles, getToken(), DocConstants.PERSON);
        return Result.success(true);
    }

    /**
     * 恢复
     */
    @PostMapping("recycleResume")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "恢复")
    public Result recycleResume(@RequestBody Long[] recycles) {
        centerRecycleService.recycleResume(recycles, getToken(), DocConstants.PERSON);
        return Result.success(true);
    }


    /**
     * 回收站列表
     */
    @PostMapping("recycleList")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "回收站列表")
    public Result<PageInfo<DocBsDocumentDTO>> recycleList(DocBsRecycleSearchVO docBsDocumentExtend, PageForm pageForm) {
        docBsDocumentExtend.setDocType(DocConstants.PERSON);
        PageInfo<DocBsDocumentDTO> ret = centerRecycleService.recycleList(getToken(), docBsDocumentExtend, pageForm);
        return Result.success(ret);
    }


    /**
     * 添加分享
     */
    @PostMapping("addShape")
    Result<DocBsShapeLinkDTO> addShape(@RequestBody DocBsShapeAddVO s) {
        return centerShapeService.addShape(s, getToken().getId(), getToken().getName());
    }

    /**
     * 添加收藏
     */
    @PostMapping("addCollection")
    public Result addCollection(Long[] docId) {
        return centerCollectionService.addCollection(docId, getToken().getId());
    }

    /**
     * 取消收藏
     */
    @PostMapping("cancelCollection")
    public Result cancelCollection(Long docId) {
        return centerCollectionService.cancelCollection(docId);
    }


    /**
     * 个人已使用容量
     */
    @PostMapping("usedCapacity")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "个人已使用容量")
    public Result<DocCapacityDTO> usedCapacity() {
        DocCapacityDTO ret = centerDocumentService.usedCapacity(getToken());
        return Result.success(ret);
    }


    /**
     * 获取文件夹的顺序号
     */
    @PostMapping("getFolderSeq")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + "获取文件夹的顺序号")
    public Result<Integer> getFolderSeq(Long parentId) {
        return Result.success(docCommonService.getFolderSeq(parentId, null, getToken(), DocConstants.PERSON));
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
