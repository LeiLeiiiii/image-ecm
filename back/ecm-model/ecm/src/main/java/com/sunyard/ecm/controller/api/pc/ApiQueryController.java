package com.sunyard.ecm.controller.api.pc;

import com.sunyard.ecm.annotation.TimeCalculateAnnotation;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.controller.BaseController;
import com.sunyard.ecm.service.OperateQueryService;
import com.sunyard.ecm.vo.FileInfoVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.ApiLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author lw
 * @since: 2023/8/14
 * @Desc: 对外接口-资源获取
 */
@Api(tags = "对外接口-资源获取")
@RestController
@RequestMapping("api/query")
@TimeCalculateAnnotation
public class ApiQueryController extends BaseController {

    @Resource
    private OperateQueryService operateQueryService;


    /**
     * 查看影像文件信息 基本信息+EXIF
     */
    @ApiOperation("查看影像文件信息")
    @ApiLog(LogsConstants.QUERY + "查看影像文件信息")
    @PostMapping("getFileInfo")
    public Result getFileExifInfo(@RequestBody FileInfoVO fileInfoVO) {
        operateQueryService.getFileInfo(fileInfoVO.getBusiId(), fileInfoVO.getFileId(), getToken());
        return Result.success(true);
    }
}
