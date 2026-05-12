package com.sunyard.edm.controller;


import com.github.pagehelper.PageInfo;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.constant.DocLogsConstants;
import com.sunyard.edm.dto.DocBsDocumentDTO;
import com.sunyard.edm.dto.DocBsDocumentUserDTO;
import com.sunyard.edm.dto.DocBsShapeLinkDTO;
import com.sunyard.edm.dto.DocSysHouseDTO;
import com.sunyard.edm.dto.DocSysHouseUserDTO;
import com.sunyard.edm.dto.DocSysTagDTO;
import com.sunyard.edm.dto.HouseInfoDTO;
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
import com.sunyard.edm.service.SysLibraryService;
import com.sunyard.edm.service.SysTagService;
import com.sunyard.edm.vo.AddOrUpdateDocumentVO;
import com.sunyard.edm.vo.DocBsDocumentSearchVO;
import com.sunyard.edm.vo.DocBsDocumentVO;
import com.sunyard.edm.vo.DocBsRecycleSearchVO;
import com.sunyard.edm.vo.DocBsShapeAddVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.api.dto.SysUserDTO;
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
 * @desc 企业文档库
 *
 */
@RestController
@RequestMapping("center/ent")
public class CenterEntController extends BaseController {
    @Resource
    private SysCapacityService sysCapacityService;
    @Resource
    private CenterDocumentService centerDocumentService;
    @Resource
    private CenterFolderService centerFolderService;
    @Resource
    private SysLibraryService sysLibraryService;
    @Resource
    private CenterRecycleService centerRecycleService;
    @Resource
    private SysFolderService folderService;
    @Resource
    private SysTagService tagService;
    @Resource
    private CenterCommonService commonService;
    @Resource
    private CenterCollectionService centerCollectionService;
    @Resource
    private CenterShapeService centerShapeService;

    /**
     * 获取文档库列表
     *
     * @return
     */
    @PostMapping("qryHouseList")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "获取文档库")
    public Result<List<DocSysHouseDTO>> qryHouseList() {
        List<DocSysHouseDTO> list = commonService.queryHouseList(getToken());
        return Result.success(list);
    }

    /**
     * 文件夹树
     *
     * @param
     * @return
     */
    @PostMapping("tree")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + DocLogsConstants.COMMON_GETLIST)
    public Result<List<DocBsDocumentDTO>> tree(Long houseId) {
        List<DocBsDocumentDTO> ret = centerFolderService.getFolderTree(getToken(), houseId, DocConstants.COMPANY);
        return Result.success(ret);
    }


//    /**
//     * 子级文件夹
//     *
//     * @param busId 查询条件
//     * @return 子级文件夹
//     */updateHouse
//    @PostMapping("selectChildFolder")
//    @OperationLog(DocLogsConstants.SYS_DOCUMENT + "查询文件夹子级")
//    public Result<List<DocBsDocumentDTO>> selectChildFolder(Long busId) {
//        return Result.success(folderService.selectChild(busId, getToken()));
//    }

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
     * 文档列表
     *
     * @param docBsDocumentExtend 查询条件
     * @return
     */
    @PostMapping("search")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + DocLogsConstants.COMMON_GETLIST)
    public Result<PageInfo<DocBsDocumentDTO>> search(@RequestBody DocBsDocumentSearchVO docBsDocumentExtend) {
        docBsDocumentExtend.setDocType(DocConstants.COMPANY);
        docBsDocumentExtend.setShowFlag(false);
        PageForm pageForm = new PageForm();
        pageForm.setPageNum(docBsDocumentExtend.getPageNum());
        pageForm.setPageSize(docBsDocumentExtend.getPageSize());
        PageInfo<DocBsDocumentDTO> ret = centerDocumentService.search(getToken(), docBsDocumentExtend, pageForm);
        return Result.success(ret);
    }



    /**
     * 文档库列表
     *
     * @param docBsDocumentExtend 查询条件
     * @param pageForm            分页参时
     * @return
     */
    @PostMapping("searchNoFolder")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + DocLogsConstants.COMMON_GETLIST)
    public Result<PageInfo<DocBsDocumentDTO>> searchNoFolder(DocBsDocumentSearchVO docBsDocumentExtend, PageForm pageForm) {
        docBsDocumentExtend.setDocType(DocConstants.COMPANY);
        docBsDocumentExtend.setShowFlag(false);
        PageInfo<DocBsDocumentDTO> ret = centerDocumentService.searchNoFolder(getToken(), docBsDocumentExtend, pageForm);
        return Result.success(ret);
    }

    /**
     * 单个上传-用户搜索列表
     *
     * @param name
     * @return
     */
    @PostMapping("queryUserList")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "单个上传-用户搜索列表")
    public Result<List<SysUserDTO>> queryUserList(String name) {
        List<SysUserDTO> search = centerDocumentService.queryUserList(getToken(), name);
        return Result.success(search);
    }

    /**
     * 文件上传
     */
    @PostMapping("addUpload")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "单个上传包含文件上传")
    public Result addUpload(
            HttpServletRequest request, AddOrUpdateDocumentVO docBsDocumentExtend) throws Exception {
        System.out.println("0、进入文件上传接口," + System.currentTimeMillis());
        MultipartHttpServletRequest request1 = (MultipartHttpServletRequest) request;
        List<MultipartFile> file11 = request1.getFiles("file1");
        AssertUtils.isNull(file11, "参数错误");
        List<MultipartFile> file12 = request1.getFiles("file2");
        List<AddOrUpdateDocumentVO> list = centerDocumentService.addUpload(file11.get(0), file12, getToken(), docBsDocumentExtend, DocConstants.COMPANY);
        System.out.println("end、进入文件上传接口," + System.currentTimeMillis());
        return Result.success(list);
    }

    /**
     * 文件上传
     */
    @PostMapping("batchAddUpload")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "批量上传包含文件上传")
    public Result batchAddUpload(@RequestParam("file1") List<MultipartFile> file1, AddOrUpdateDocumentVO docBsDocumentExtend) throws Exception {
        return Result.success(centerDocumentService.batchAddUpload(file1, getToken(), docBsDocumentExtend, DocConstants.COMPANY));
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
        docBsDocumentExtend.setDocType(DocConstants.COMPANY);
        return Result.success(centerDocumentService.updateUpload(file11 != null && file11.size() > 0 ? file11.get(0) : null, file12, getToken(), docBsDocumentExtend));
    }

    /**
     * 删除
     *
     * @param houseIds
     * @return
     */
    @PostMapping("delHouse")
    @OperationLog(DocLogsConstants.LIBRARY + DocLogsConstants.COMMON_DELETE)
    public Result delHouse(@RequestBody Long[] houseIds) {
        sysLibraryService.delHouse(houseIds, getToken());
        return Result.success(true);
    }

    /**
     * 删除文档
     *
     * @return
     */
    @PostMapping("delDoc")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "删除文档")
    public Result delDoc(Long[] busId) {
        centerDocumentService.delDocBatch(busId, DocConstants.COMPANY, getToken());
        return Result.success(true);
    }

    /**
     * 文档名称重命名
     *
     * @return
     */
    @PostMapping("updateDocumentName")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "文档名称重命名")
    public Result<DocBsDocument> updateDocumentName(Long busId, Long folderId, String docName) {
        return Result.success(centerDocumentService.updateDocumentName(busId, folderId, docName, DocConstants.COMPANY, getToken()));
    }


    /**
     * 文档名称重命名校验
     *
     * @return
     */
    @PostMapping("checkDocumentName")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "文档名称重命名校验")
    public Result<List<Map>> checkDocumentName(Long folderId, String[] docNames) {
        return Result.success(centerDocumentService.checkDocumentName(folderId, docNames));
    }

    /**
     * 文档详情
     *
     * @return
     */
    @PostMapping("getInfo")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "详情")
    public Result<DocBsDocumentDTO> getInfo(Long busId) {
        return Result.success(centerDocumentService.getInfo(busId, getToken().getId()));
    }

    /**
     * 关联文档详情
     *
     * @return
     */
    @PostMapping("getAssociationInfo")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "关联文档详情")
    public Result<DocBsDocumentDTO> getAssociationInfo(Long busId) {
        return Result.success(centerDocumentService.getAssociationInfo(busId, getToken()));
    }

    /**
     * 关联文档-已关联
     *
     * @return
     */
    @PostMapping("getRelDocOn")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "关联文档-已关联")
    public Result<List<Long>> getRelDocOn(Long busId) {
        return Result.success(centerDocumentService.getRelDocOn(busId));
    }


    /**
     * 文档详情
     *
     * @return
     */
    @PostMapping("getInfoUser")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "文档详情-文档权限")
    public Result<Map> getInfoUser(Long busId, PageForm pageForm) {
        return Result.success(centerDocumentService.getInfoUser(busId, getToken(), pageForm));
    }


    /**
     * 标签设置
     *
     * @return
     */
    @PostMapping("setTag")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "标签设置")
    public Result setTag(@RequestBody AddOrUpdateDocumentVO vo) {
        centerDocumentService.setTag(vo, getToken());
        return Result.success(true);
    }

    /**
     * 权限设置
     *
     * @return
     */
    @PostMapping("setUserDept")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "权限设置")
    public Result setUserDept(@RequestBody AddOrUpdateDocumentVO vo) {
        centerDocumentService.setUserDept(vo, getToken());
        return Result.success(true);
    }

    /**
     * 关联文档
     *
     * @return
     */
    @PostMapping("relDoc")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "关联文档")
    public Result relDoc(@RequestBody AddOrUpdateDocumentVO vo) {
        vo.setDocType(DocConstants.COMPANY);
        centerDocumentService.relDoc(getToken(), vo);
        return Result.success(true);
    }


    /**
     * 下架
     *
     * @return
     */
    @PostMapping("soldOut")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "下架")
    public Result soldOut(Long[] busId) {
        centerDocumentService.soldOut(busId, getToken());
        return Result.success(true);
    }


    /**
     * 关联文档-文档列表
     *
     * @return
     */
    @PostMapping("relDocList")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "关联文档-文档列表")
    public Result<PageInfo<DocBsDocumentDTO>> relDocList(DocBsDocumentSearchVO vo, PageForm pageForm) {
        vo.setDocType(DocConstants.COMPANY);
        PageInfo<DocBsDocumentDTO> ret = centerDocumentService.relDocList(getToken(), vo, pageForm);
        return Result.success(ret);
    }

    /**
     * 获取文件夹的顺序号
     *
     * @param parentId 文件夹父级id
     * @return 顺序号
     */
    @PostMapping("getFolderSeq")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + "获取文件夹的顺序号")
    public Result<Integer> getFolderSeq(Long parentId, Long houseId) {
        return Result.success(commonService.getFolderSeq(parentId, houseId, getToken(), DocConstants.COMPANY));
    }


    /**
     * 获取文件夹的顺序号
     *
     * @return 顺序号
     */
    @PostMapping("getHouseSeq")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + "获取文档库的顺序号")
    public Result<Integer> getHouseSeq() {
        return Result.success(sysLibraryService.getFolderSeq());
    }


    /**
     * 标签列表
     *
     * @param tagName 查询条件
     * @return 标签列表
     */
    @PostMapping("selectTag")
    @OperationLog(DocLogsConstants.SYS_TAG + DocLogsConstants.COMMON_GETLIST)
    public Result<List<DocSysTagDTO>> selectTag(String tagName) {
        return Result.success(tagService.selectTag(tagName));
    }


    /**
     * 编辑时，标签的回显
     *
     * @param docId
     * @return 标签列表
     */
    @PostMapping("selectUpdateTag")
    @OperationLog(DocLogsConstants.SYS_TAG + "编辑时，标签的回显")
    public Result<Map> selectUpdateTag(Long docId) {
        return Result.success(tagService.selectUpdateTag(docId));
    }

    /**
     * 子级标签
     *
     * @param tagId 查询条件
     * @return 子级标签
     */
    @PostMapping("selectChild")
    @OperationLog(DocLogsConstants.SYS_TAG + "查询标签子级")
    public Result<List<DocSysTagDTO>> selectChild(Long tagId) {
        return Result.success(tagService.selectChild(tagId));
    }


    /**
     * 移动到文件夹树列表
     *
     * @return
     */
    @PostMapping("moveFolderTree")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "移动到文件夹树列表")
    public Result<List<DocBsDocumentDTO>> moveFolderTree(Long houseId) {
        List<DocBsDocumentDTO> ret = centerFolderService.moveFolderTree(getToken(), houseId, DocConstants.COMPANY);
        return Result.success(ret);
    }


    /**
     * 移动文档
     *
     * @return
     */
    @PostMapping("moveDoc")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "移动文档")
    public Result moveDoc(Long[] busId, Long folderId) {
        centerDocumentService.moveDocBatch(getToken(), busId, folderId, DocConstants.COMPANY);
        return Result.success(true);
    }

    /**
     * 删除文件夹
     *
     * @return
     */
    @PostMapping("delFolder")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "删除文件夹")
    public Result delFolder(Long folderId) {
        centerFolderService.delFolder(getToken(), folderId, DocConstants.COMPANY);
        return Result.success(true);
    }

    /**
     * 新增文件夹
     *
     * @return
     */
    @PostMapping("addFolder")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "新增文件夹")
    public Result addFolder(@RequestBody DocBsDocumentDTO docBsDocument) {
        docBsDocument.setDocOwner(getToken().getId());
        docBsDocument.setDocCreator(getToken().getId());
        docBsDocument.setDocType(DocConstants.COMPANY);
        centerFolderService.addFolder(getToken(), docBsDocument);
        return Result.success(true);
    }

    /**
     * 修改文件夹
     *
     * @return
     */
    @PostMapping("updateFolder")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "修改文件夹")
    public Result updateFolder(@RequestBody DocBsDocumentVO docBsDocument) {
        centerFolderService.updateFolder(getToken(), docBsDocument, DocConstants.COMPANY);
        return Result.success(true);
    }


    /**
     * 复用上级权限
     *
     * @param parentId 父级文件夹id/文档库id
     * @return 上级权限
     */
    @PostMapping("reuseAuth")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "复用上级权限")
    public Result<UserTeamDeptInitDTO> reuseAuth(Long parentId, Integer type) {
        return Result.success(commonService.reuseAuth(parentId, type));
    }

    /**
     * 获取组织团队用户列表
     *
     * @return
     */
    @PostMapping("getUserByDeptOrTeam")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "获取组织团队用户列表")
    public Result<UserTeamDeptDTO> getUserByDeptOrTeam(UserTeamDeptDTO extend, PageForm pageForm) {
        UserTeamDeptDTO ret = commonService.getUserByDeptOrTeam(extend, pageForm, getToken());
        return Result.success(ret);
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
     * 计算权限 机构 部门 团队 用户 的数量
     */
    @PostMapping("getTypeNum")
    @OperationLog(DocLogsConstants.LIBRARY + DocLogsConstants.COMMON_GETLIST)
    public Result<HouseInfoDTO> getTypeNum(Long houseId) {
        return Result.success(sysLibraryService.getTypeNum(houseId));
    }


    /**
     * 文件夹列表
     */
    @PostMapping("selectFolder")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + DocLogsConstants.COMMON_GETLIST)
    public Result<List<DocBsDocumentDTO>> selectFolder(Long houseId, String folderName) {
        return Result.success(folderService.selectFolder(houseId, folderName, getToken()));
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
     * 删除提示
     *
     * @param busIds 文件夹ids
     * @return 文件夹数量
     */
    @PostMapping("delPrompt")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + DocLogsConstants.COMMON_DELETE)
    public Result<PromptDTO> delPrompt(@RequestBody Long[] busIds) {
        return Result.success(folderService.delPrompt(busIds, getToken(), DocConstants.COMPANY));
    }

//    /**
//     * 加入回收站（删除）
//     *
//     * @return
//     */
//    @PostMapping("addRecycle")
//    @OperationLog(DocLogsConstants.BS_DOCUMENT + "加入回收站（删除）")
//    public Result addRecycle(@RequestBody Long[] busIds) {
//        centerDocumentService.addRecycle(getToken(), busIds, DocConstants.COMPANY);
//        return Result.success(true);
//    }

    /**
     * 彻底删除
     *
     * @return
     */
    @PostMapping("delDocOrFolder")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "彻底删除")
    public Result delDocOrFolder(@RequestBody Long[] recycles) {
        centerRecycleService.delDoc(recycles, getToken(), DocConstants.COMPANY);
        return Result.success(true);
    }

    /**
     * 恢复
     *
     * @return
     */
    @PostMapping("recycleResume")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "恢复")
    public Result recycleResume(@RequestBody Long[] recycles) {
        centerRecycleService.recycleResume(recycles, getToken(), DocConstants.COMPANY);
        return Result.success(true);
    }


    /**
     * 回收站列表
     *
     * @return
     */
    @PostMapping("recycleList")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "回收站列表")
    public Result<PageInfo<DocBsDocumentDTO>> recycleList(DocBsRecycleSearchVO docBsDocumentExtend, PageForm pageForm) {
        docBsDocumentExtend.setDocType(DocConstants.COMPANY);
        PageInfo<DocBsDocumentDTO> ret = centerRecycleService.recycleList(getToken(), docBsDocumentExtend, pageForm);
        return Result.success(ret);
    }


    /**
     * 添加分享
     *
     * @param s 添加分享bean
     */
    @PostMapping("addShape")
    public Result<DocBsShapeLinkDTO> addShape(@RequestBody DocBsShapeAddVO s) {
        return centerShapeService.addShape(s, getToken().getId(), getToken().getName());
    }


    /**
     * 添加收藏
     *
     * @param docId 文档id
     */
    @PostMapping("addCollection")
    public Result addCollection(Long[] docId) {
        return centerCollectionService.addCollection(docId, getToken().getId());
    }

    /**
     * 取消收藏
     *
     * @param docId 文档id
     */
    @PostMapping("cancelCollection")
    public Result cancelCollection(Long docId) {
        return centerCollectionService.cancelCollection(docId);
    }

    /**
     * 新增
     *
     * @param houseVo 查询条件
     * @return
     */
    @PostMapping("addHouse")
    @OperationLog(DocLogsConstants.LIBRARY + DocLogsConstants.COMMON_ADD)
    public Result addHouse(@RequestBody DocSysHouseDTO houseVo) {
        sysLibraryService.addHouse(houseVo);
        return Result.success(true);
    }

    /**
     * 查询文档库详情
     *
     * @param houseId
     * @return
     */
    @PostMapping("getInfoHouse")
    @OperationLog(DocLogsConstants.LIBRARY + DocLogsConstants.COMMON_GETLIST)
    public Result<HouseInfoDTO> getInfoHouse(Long houseId, PageForm pageForm) {
        return Result.success(sysLibraryService.getInfo(getToken(), houseId, pageForm));
    }


    /**
     * 修改
     *
     * @param houseVo 查询条件
     * @return
     */
    @PostMapping("updateHouse")
    @OperationLog(DocLogsConstants.LIBRARY + DocLogsConstants.COMMON_UPDATE)
    public Result updateHouse(@RequestBody DocSysHouseDTO houseVo) {
        sysLibraryService.updateHouse(houseVo, getToken());
        return Result.success(true);
    }


    /**
     * 文件夹详情
     *
     * @param busId    文件夹id
     * @param pageForm 分页参数
     * @return 文件夹详情
     */
    @PostMapping("getInfoFolder")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + DocLogsConstants.COMMON_GETINFO)
    public Result<DocBsDocumentDTO> getInfoFolder(Long busId, PageForm pageForm) {
        DocBsDocumentDTO infoFolder = folderService.getInfoFolder(busId, pageForm);
        return Result.success(infoFolder);
    }


    /**
     * 权限详情
     *
     * @param busId 文件夹id
     * @return 权限详情
     */
    @PostMapping("getInfoAuth")
    @OperationLog(DocLogsConstants.SYS_DOCUMENT + DocLogsConstants.COMMON_GETINFO)
    public Result<List<DocBsDocumentUserDTO>> getInfoAuth(Long busId) {
        List<DocBsDocumentUserDTO> infoAuth = folderService.getInfoAuth(busId);
        return Result.success(infoAuth);
    }


    /**
     * 回收站天数
     *
     * @return
     */
    @PostMapping("getDelDay")
    @OperationLog(DocLogsConstants.BS_DOCUMENT + "获取回收站天数")
    public Result<String> getDelDay() {
        return Result.success(sysCapacityService.getDelDay());
    }

}
