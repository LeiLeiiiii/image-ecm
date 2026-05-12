package com.sunyard.module.storage.controller.api;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.sunyard.framework.log.annotation.ApiLog;
import com.sunyard.module.storage.constant.LogsPrefixConstants;
import com.sunyard.module.storage.constant.StateConstants;
import com.sunyard.module.storage.controller.BaseController;
import com.sunyard.module.storage.dto.FileDownDTO;
import com.sunyard.module.storage.dto.MixedPastingSplitDTO;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.dto.SysFilePictureRotatingDTO;
import com.sunyard.module.storage.service.FileDealService;
import com.sunyard.module.storage.vo.DownFileVO;
import com.sunyard.module.storage.vo.FileMergeVO;
import com.sunyard.module.storage.vo.FileSplitPdfVO;
import com.sunyard.module.storage.vo.FileUpdateVo;
import com.sunyard.module.storage.vo.PictureRotatingVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * 文件处理
 *
 * @author zyl
 * @since 2023/4/26 14:12
 */
@Slf4j
@RestController
@RequestMapping("api/storage/deal")
public class FileDealOpenController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.API_FILE_DEAL + "->";
    @Resource
    private FileDealService fileDealService;

    /**
     * 图片编辑PC
     *
     * @param pictureRotatingVO pictureRotatingVO
     * @return Result
     */
    @ApiLog(BASELOG + "图片编辑PC")
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
    @ApiLog(BASELOG + "图片编辑APP")
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
    @ApiLog(BASELOG + "图片预编辑")
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
    @ApiLog(BASELOG + "图片旋转")
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
    @ApiLog(BASELOG + "文件合并")
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
    @ApiLog(BASELOG + "文件拆分")
    @PostMapping("splitFile")
    public Result<List<SysFileDTO>> splitFile(@RequestBody FileSplitPdfVO ecmSplitPdfVo) {
        return Result.success(fileDealService.splitFile(ecmSplitPdfVo, getToken()));
    }

    /**
     * 创建输入流资源
     * @param request 请求头
     * @param response 响应头
     * @param vo vo
     * @return Result
     */
    @ApiLog(BASELOG + "创建输入流资源")
    @GetMapping("createInputStreamResources")
    public Result createInputStreamResources(HttpServletRequest request,
                                             HttpServletResponse response, FileUpdateVo vo) {
        fileDealService.createInputStreamResources(request, response, vo.getFileId(), getToken(),
                null, vo.getPassword());
        return Result.success();
    }

    /**
     * 获取图片缩略图
     * @param request 请求头
     * @param response 响应头
     * @param vo vo
     * @return Result
     */
    @ApiLog(BASELOG + "获取图片缩略图")
    @GetMapping("getThumbnail")
    public Result getThumbnail(HttpServletRequest request, HttpServletResponse response,
                               FileUpdateVo vo) {
        fileDealService.getThumbnail(request, response, vo.getFileId(), getToken(), vo.getType());
        return Result.success();
    }

    /**
     *
     * @param request 请求头
     * @param response 响应头
     * @param vo vo
     * @return Result
     */
    @ApiLog(BASELOG + "创建输入流资源")
    @GetMapping("createInputStreamResourcesCache")
    public Result createInputStreamResourcesCache(HttpServletRequest request,
                                                  HttpServletResponse response, FileUpdateVo vo) {
        fileDealService.createInputStreamResources(request, response, vo.getFileId(), getToken(),
                StateConstants.YES, vo.getPassword());
        return Result.success();
    }

    /**
     * 文件下载
     * @param fileId 文件id
     * @param isPack 是否打包
     * @return Result
     */
    @ApiLog(BASELOG + "文件下载")
    @PostMapping("downFile")
    public Result<ResponseEntity<byte[]>> downFile(@RequestParam List<Long> fileId,
                                                   @RequestParam Integer isPack,
                                                   @RequestParam(required = false) String password) {
        return downFileBase(fileId, isPack, password);
    }

    /**
     * Get方式文件下载，为了解决对外接口header中无法存token的问题，get方式可从url中获取token
     * @param fileId 文件id
     * @param isPack 是否打包
     * @return Result
     */
    @ApiLog(BASELOG + "Get方式文件下载")
    @GetMapping("downFileGet")
    public Result<ResponseEntity<byte[]>> downFileGet(@RequestParam List<Long> fileId,
                                                      @RequestParam Integer isPack,
                                                      @RequestParam(required = false) String password) {
        return downFileBase(fileId, isPack, password);
    }

    private Result<ResponseEntity<byte[]>> downFileBase(List<Long> fileId, Integer isPack,
                                                        String password) {
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
     * 文件分段下载
     *
     * @param downFileVO downFileVO
     * @return Result
     */
    @ApiLog(BASELOG + "文件分段下载")
    @PostMapping("shardingDownFile")
    public Result shardingDownFile(@RequestBody DownFileVO downFileVO) {
        AssertUtils.isNull(getToken(), "很遗憾，您没下载权限");
        fileDealService.shardingDownFile(downFileVO, getToken(), request, response);
        return Result.success();
    }

    /**
     * 文件打印
     *
     * @param
     * @return Result
     */
    @ApiLog(BASELOG + "文件打印")
    @PostMapping("printFile")
    public Result printFile(HttpServletRequest request, HttpServletResponse response,
                            @RequestBody DownFileVO fileVO) {
        fileDealService.printFile(request, response, fileVO.getFileId(), getToken(),
                fileVO.getPassword());
        return Result.success();
    }

    /**
     * 获取带水印的源文件
     *
     * @param fileId 文件id
     * @return Result
     */
    @ApiLog(BASELOG + "获取带水印的源文件")
    @GetMapping("getFileByFileIdByWater")
    public Result getFileByFileIdByWater(HttpServletRequest request, HttpServletResponse response,
                                         Long fileId, String username, String name, String orgCode,
                                         String orgName, String password) {
        AssertUtils.isNull(getToken(), "很遗憾，您没下载权限");
        String sessionId = request.getSession().getId();
        //如果使用外部系统做图像预览，比方说onlyoffice是无法获取到用户信息的，而这类往往是内部访问，因此可通过参数方式传递。
        AccountToken token = new AccountToken();
        token.setOut(true);
        token.setUsername(username);
        token.setName(name);
        token.setOrgCode(orgCode);
        token.setOrgName(orgName);
        fileDealService.getFileByFileIdByWater(sessionId, fileId, token, response, password);
        return Result.success();
    }

    /**
     * 获取文件url
     *
     * @param id id
     * @return Result
     */
    @ApiLog(BASELOG + "获取文件url")
    @PostMapping(value = "getFileUrl", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<String> getFileUrl(@RequestParam("id") Long id) {
        return Result.success(fileDealService.getFileUrl(id));
    }

    /**
     * 混贴拆分
     * @param mixedPastingSplitDTO mixedPastingSplitDTO
     * @return Result
     */
    @ApiLog(BASELOG + "混贴拆分")
    @PostMapping(value = "mixedPastingSplit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<List<SysFileDTO>> mixedPastingSplit(@RequestBody MixedPastingSplitDTO mixedPastingSplitDTO) {
        return Result.success(fileDealService.mixedPastingSplit(mixedPastingSplitDTO));
    }

    /**
     * 创建输入流资源
     * @param file 文件对象
     */
    @ApiLog(BASELOG + "创建输入流资源")
    @PostMapping("createInputStreamResourcesCacheApi")
    public void createInputStreamResourcesCacheApi(@RequestBody FileDownDTO file) {
        fileDealService.createInputStreamResourcesCacheApi(request, response, file.getFileId(),
                file.getUserName(), file.getUserPhone(), file.getInstName(), file.getInstPhone(),
                file.getPassword());
    }

    /**
     * 根据文件id预览文件(onlyOffice)
     *
     * @param fileId  请求头
     * @return Result
     */
    @ApiLog(BASELOG + "根据文件id预览文件(onlyOffice)")
    @PostMapping("documentView")
    public Result documentView(Long fileId, String password) {
        return fileDealService.documentView(fileId, password, getToken(),
                StateConstants.COMMON_ONE);
    }

    /**
     * 获取onlyOffice开关
     *
     * @return Result
     */
    @ApiLog(BASELOG + "获取onlyOffice开关")
    @PostMapping("getOnlyOfficeEnable")
    public Result getOnlyOfficeEnable() {
        return fileDealService.getOnlyOfficeEnable();
    }

    @ApiLog(BASELOG + "无水印文件下载")
    @GetMapping("downloadFile")
    public ResponseEntity<StreamingResponseBody> downloadFile(
            @RequestParam Long fileId,
            @RequestParam String fileName) {
       return fileDealService.downloadFile(fileId, fileName);
    }
}
