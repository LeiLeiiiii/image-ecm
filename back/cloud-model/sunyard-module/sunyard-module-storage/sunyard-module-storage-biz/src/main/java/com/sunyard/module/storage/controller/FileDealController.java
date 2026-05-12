package com.sunyard.module.storage.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.storage.constant.LogsPrefixConstants;
import com.sunyard.module.storage.constant.StateConstants;
import com.sunyard.module.storage.dto.MixedPastingSplitDTO;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.dto.SysFilePictureRotatingDTO;
import com.sunyard.module.storage.dto.ecm.DownloadFileZip;
import com.sunyard.module.storage.service.FileDealService;
import com.sunyard.module.storage.vo.DownFileVO;
import com.sunyard.module.storage.vo.FileMergeVO;
import com.sunyard.module.storage.vo.FileSplitPdfVO;
import com.sunyard.module.storage.vo.PictureRotatingVO;

import lombok.extern.slf4j.Slf4j;

/**
 * 文件处理
 *
 * @author zyl
 * @since 2023/4/26 14:12
 */
@Slf4j
@RestController
@RequestMapping("storage/deal")
public class FileDealController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.MENU_FILE_DEAL + "->";
    @Resource
    private FileDealService fileDealService;

    /************************************************资源编辑******************************************************/
    /**
     * 图片编辑PC
     *
     * @param pictureRotatingVO pictureRotatingVO
     * @return Result
     */
    @OperationLog(BASELOG + "图片编辑PC")
    @PostMapping("updateImgToPc")
    public Result<List<SysFilePictureRotatingDTO>> updateImgToPc(@RequestBody PictureRotatingVO pictureRotatingVO) {
        return Result.success(fileDealService.updateImg(pictureRotatingVO, getToken()));
    }

    /**
     * 图片编辑APP
     *
     * @param pictureRotatingVO pictureRotatingVO
     * @return Result
     */
    @OperationLog(BASELOG + "图片编辑APP")
    @PostMapping("updateImgToApp")
    public Result<List<SysFilePictureRotatingDTO>> updateImgToApp(@RequestBody PictureRotatingVO pictureRotatingVO) {
        return Result.success(fileDealService.updateImg(pictureRotatingVO, getToken()));
    }

    /**
     * 图片预编辑
     *
     * @param pictureRotatingVO pictureRotatingVO
     * @return Result
     */
    @OperationLog(BASELOG + "图片预编辑")
    @PostMapping("pretreatUpdateImg")
    public Result<List<ResponseEntity<byte[]>>> pretreatUpdateImg(@RequestBody PictureRotatingVO pictureRotatingVO,
                                                                  HttpServletRequest request) {
        List<ResponseEntity<byte[]>> inputStreamList = fileDealService
                .pretreatUpdateImg(pictureRotatingVO, getToken(), request);
        return Result.success(inputStreamList);
    }

    /**
     * 图片旋转
     *
     * @param pictureRotatingVO pictureRotatingVO
     * @return Result
     */
    @OperationLog(BASELOG + "图片旋转")
    @PostMapping("picturesRotating")
    public Result<List<SysFilePictureRotatingDTO>> picturesRotating(@RequestBody PictureRotatingVO pictureRotatingVO) {
        return Result.success(fileDealService.picturesRotating(pictureRotatingVO, getToken()));
    }

    /**
     * 文件合并
     *
     * @param ecmMergeVo ecmMergeVo
     * @return Result
     */
    @OperationLog(BASELOG + "文件合并")
    @PostMapping("mergeFile")
    public Result<SysFileDTO> mergeFile(@RequestBody FileMergeVO ecmMergeVo) {
        return Result.success(fileDealService.mergeFile(ecmMergeVo, getToken()));
    }

    /**
     * 文件拆分
     *
     * @param ecmSplitPdfVo ecmSplitPdfVo
     * @return Result
     */
    @OperationLog(BASELOG + "文件拆分")
    @PostMapping("splitFile")
    public Result<List<SysFileDTO>> splitFile(@RequestBody FileSplitPdfVO ecmSplitPdfVo) {
        return Result.success(fileDealService.splitFile(ecmSplitPdfVo, getToken()));
    }

    /**
     * 混贴拆分
     *
     * @param mixedPastingSplitDTO mixedPastingSplitDTO
     * @return Result
     */
    @OperationLog(BASELOG + "混贴拆分")
    @PostMapping(value = "mixedPastingSplit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<List<SysFileDTO>> mixedPastingSplit(@RequestBody MixedPastingSplitDTO mixedPastingSplitDTO) {
        return Result.success(fileDealService.mixedPastingSplit(mixedPastingSplitDTO));
    }

    /************************************************资源删除******************************************************/

    /**
     * 删除文件
     *
     * @param fileId 文件id
     * @return Result
     */
    @OperationLog(BASELOG + "删除文件")
    @PostMapping(value = "deleteFile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<List<SysFileDTO>> deleteFile(Long fileId) {
        fileDealService.deleteFile(fileId);
        return Result.success();
    }

    /************************************************资源获取******************************************************/

    /**
     * 获取文件url
     *
     * @param id 文件id
     * @return Result
     */
    @OperationLog(BASELOG + "获取文件url")
    @PostMapping(value = "getFileUrl", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<String> getFileUrl(@RequestParam("id") Long id) {
        return Result.success(fileDealService.getFileUrl(id));
    }

    /**
     * @param request  请求头
     * @param response 响应头
     * @param fileId   文件ID
     * @return Result
     */
    @OperationLog(BASELOG + "根据文件id获取源文件，不做任务处理")
    @GetMapping("getFileByFileId")
    public Result getFileByFileId(HttpServletRequest request, HttpServletResponse response,
                                  Long fileId, Integer type) {
        fileDealService.getFileByFileId(request, response, fileId);
        return Result.success();
    }

    /**
     * 获取带水印的源文件
     *
     * @param fileId 文件id
     * @return Result
     */
    @GetMapping("getFileByFileIdByWater")
    @OperationLog(BASELOG + "获取带水印的源文件")
    public Result getFileByFileIdByWater(HttpServletRequest request, HttpServletResponse response,
                                         Long fileId, String username, String name, String orgCode,
                                         String orgName, String password) {
        String sessionId = request.getSession().getId();
        //如果使用外部系统做图像预览，比方说onlyoffice是无法获取到用户信息的，而这类往往是内部访问，因此可通过参数方式传递。
        AccountToken token = new AccountToken();
        token.setOut(false);
        token.setUsername(username);
        token.setName(name);
        token.setOrgCode(orgCode);
        token.setOrgName(orgName);
        fileDealService.getFileByFileIdByWater(sessionId, fileId, token, response, password);
        return Result.success();
    }

    /**
     * 创建输入流资源
     *
     * @param request  请求头
     * @param response 响应头
     * @param fileId   文件ID
     * @param type     操作类型 0-查看  1-打印  用于后续调取这个接口判断是否加水印
     * @return Result
     */
    @OperationLog(BASELOG + "创建输入流资源")
    @GetMapping("createInputStreamResources")
    public Result createInputStreamResources(HttpServletRequest request,
                                             HttpServletResponse response, Long fileId,
                                             Integer type, String password) {
        fileDealService.createInputStreamResources(request, response, fileId, getToken(), null,
                password);
        return Result.success();
    }

    /**
     * 获取图片缩略图
     *
     * @param request  请求头
     * @param response 响应头
     * @param fileId   文件ID
     * @param type     操作类型 0-查看  1-打印  用于后续调取这个接口判断是否加水印
     * @return Result
     */
    @OperationLog(BASELOG + "获取图片缩略图")
    @GetMapping("getThumbnail")
    public Result getThumbnail(HttpServletRequest request, HttpServletResponse response,
                               Long fileId, Integer type) {
        fileDealService.getThumbnail(request, response, fileId, getToken(), type);
        return Result.success();
    }

    /**
     * 获取文件输入流
     *
     * @param request  请求头
     * @param response 响应头
     * @param fileId   文件ID
     */
    @OperationLog(BASELOG + "获取文件输入流")
    @GetMapping("createInputStreamResourcesCache")
    public void createInputStreamResourcesCache(HttpServletRequest request,
                                                HttpServletResponse response, Long fileId,
                                                String password) {
        fileDealService.createInputStreamResources(request, response, fileId, getToken(),
                StateConstants.YES, password);
    }

    /**
     * 文件下载
     *
     * @param fileId 文件id
     * @param isPack 是否打包
     * @return Result
     */
    @OperationLog(BASELOG + "文件下载")
    @PostMapping("downFile")
    public Result<ResponseEntity<byte[]>> downFile(@RequestParam List<Long> fileId,
                                                   @RequestParam Integer isPack,
                                                   @RequestParam(required = false) String password) {
        AssertUtils.isNull(getToken(), "很遗憾，您没下载权限");
        String sessionId = request.getSession().getId();
        DownFileVO downFileVO = new DownFileVO();
        downFileVO.setFileId(fileId);
        downFileVO.setIsPack(isPack);
        downFileVO.setPassword(password);
        ResponseEntity<byte[]> responseEntity = null;
        if (isPack.equals(0)) {
            responseEntity = fileDealService.downFileByResponseEntity(sessionId, downFileVO,
                    getToken(), response);
        } else {
            fileDealService.downFile(sessionId, downFileVO, getToken(), response);
        }
        return Result.success(responseEntity);
    }

    /**
     * 文件下载
     *
     * @param fileId 文件id
     * @param isPack 是否打包
     * @return ResponseEntity
     */
    @OperationLog(BASELOG + "文件下载")
    @PostMapping("downFileByResponseEntity")
    public ResponseEntity<byte[]> downFileByResponseEntity(@RequestParam List<Long> fileId,
                                                           @RequestParam Integer isPack) {
        AssertUtils.isNull(getToken(), "很遗憾，您没下载权限");
        String sessionId = request.getSession().getId();
        DownFileVO downFileVO = new DownFileVO();
        downFileVO.setFileId(fileId);
        downFileVO.setIsPack(isPack);
        ResponseEntity<byte[]> responseEntity = fileDealService.downFileByResponseEntity(sessionId,
                downFileVO, getToken(), response);
        return responseEntity;
    }

    /**
     * 文件打印
     *
     * @param request  请求头
     * @param response 响应头
     * @param fileVO   文件ID
     * @return Result
     */
    @OperationLog(BASELOG + "文件打印")
    @PostMapping("printFile")
    public Result printFile(HttpServletRequest request, HttpServletResponse response,
                            @RequestBody DownFileVO fileVO) {
        fileDealService.printFile(request, response, fileVO.getFileId(), getToken(),
                fileVO.getPassword());
        return Result.success();
    }

    /**
     * 文件转图片打印
     *
     * @param request  请求头
     * @param response 响应头
     * @param fileVO   文件ID
     * @return Result
     */
    @OperationLog(BASELOG + "文件转图片打印")
    @PostMapping("printFileImage")
    public Result printFileImage(HttpServletRequest request, HttpServletResponse response,
                            @RequestBody DownFileVO fileVO) {
        fileDealService.printFileImage(request, response, fileVO.getFileId(), getToken(),
                fileVO.getPassword());
        return Result.success();
    }

    /**
     * 根据文件id预览文件(onlyOffice)
     *
     * @param fileId 请求头
     * @return Result
     */
    @OperationLog(BASELOG + "根据文件id预览文件(onlyOffice)")
    @PostMapping("documentView")
    public Result documentView(Long fileId, String password) {
        return fileDealService.documentView(fileId, password, getToken(), StateConstants.ZERO);
    }

    /**
     * 获取onlyOffice开关
     *
     * @return Result
     */
    @OperationLog(BASELOG + "获取onlyOffice开关")
    @PostMapping("getOnlyOfficeEnable")
    public Result getOnlyOfficeEnable() {
        return fileDealService.getOnlyOfficeEnable();
    }

    /**
     * 检测文件是否需要密码
     */
    @OperationLog(BASELOG + "检测文件是否需要密码")
    @PostMapping("checkIsPasswordProtected")
    public Result isPasswordProtected(@RequestParam List<Long> fileIds) {
        return fileDealService.isPasswordProtected(fileIds);
    }

    /**
     * 文件根据指定的zip格式批量下载
     */
    @OperationLog(BASELOG + "文件根据指定的zip格式批量下载")
    @PostMapping("batchDownloadZip")
    public ResponseEntity<FileSystemResource> batchDownloadZip(@RequestBody List<DownloadFileZip> downloadFileList,
                                                               HttpServletRequest request) {
        return fileDealService.batchDownloadZip(downloadFileList, request);
    }

    /**
     * pdf批注文件获取
     */
    @OperationLog(BASELOG + "pdf批注文件获取")
    @GetMapping("getPdfSplitFiles")
    public Result getPdfSplitFiles(HttpServletResponse response,
                                   String fileName, String requestPage, String fileMd5) {
        fileDealService.getPdfSplitFiles(response, fileName, requestPage, fileMd5);
        return Result.success();
    }

    /**
     * 发起异步批量压缩任务（返回压缩包路径）
     */
    @OperationLog(BASELOG + "发起异步批量压缩任务")
    @PostMapping("startBatchZip")
    public Result startBatchZip(
            @RequestBody List<DownloadFileZip> downloadFileList,
            HttpServletRequest request) {
        return fileDealService.startBatchZip(downloadFileList, request);
    }

    /**
     * 检测压缩包是否能正常下载
     */
    @PostMapping("checkZipSuccess")
    public Result checkZipSuccess(@RequestParam String zipFilePath, HttpServletRequest request) {
        return fileDealService.checkZipSuccess(zipFilePath, request);
    }

    /**
     * 根据压缩包路径下载文件
     */
    @GetMapping("downloadZip")
    public ResponseEntity<?> downloadZip(@RequestParam String zipFilePath, HttpServletRequest request) {
        return fileDealService.downloadZip(zipFilePath, request);
    }

}
