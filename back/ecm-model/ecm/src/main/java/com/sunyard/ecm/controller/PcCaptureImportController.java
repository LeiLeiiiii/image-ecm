package com.sunyard.ecm.controller;

import com.sunyard.ecm.annotation.TimeCalculateAnnotation;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.dto.AreaInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.manager.FileInfoService;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author： zyl
 * @create： 2023/5/6 15:42
 * @Desc：运营管理-影像导入
 */
@Api(tags = "运营管理-影像导入")
@RestController
@RequestMapping("pc/capture/import")
@TimeCalculateAnnotation
public class PcCaptureImportController extends BaseController {
    @Resource
    private FileInfoService fileInfoService;

    /**
     * 根据经纬度获取位置信息（省市区）
     */
    @ApiOperation("根据经纬度获取位置信息（省市区）")
    @OperationLog(LogsConstants.ICMS + "根据经纬度获取位置信息（省市区）")
    @PostMapping("getLocation")
    public Result<AreaInfoDTO> getLocation(@RequestBody Map<String, Double> map) {
        Double lon = map.get("lon");
        Double lat = map.get("lat");
        return fileInfoService.getLocation(lon, lat);
    }

    /**
     * OCR测试
     */
    @ApiOperation("OCR测试")
    @OperationLog(LogsConstants.ICMS + "OCR测试")
    @PostMapping("autoGroup")
    public Result autoGroup(String url) {
        return fileInfoService.autoGroup(url);
    }

    /**
     * 全部下载
     */
    @ApiOperation("查看影像属性-查看（元数据管理）")
    @OperationLog(LogsConstants.CAPTURE + "全部下载")
    @GetMapping("downFileAll")
    public void downfileall(Long markId, String docCode, Long busiId, HttpServletRequest request, HttpServletResponse response) {
        fileInfoService.downFileAll(markId, docCode, busiId, getToken(), request, response);
    }

    /**
     * 修改查重审核状态
     */
    @ApiOperation("修改异步任务查重审核状态")
    @OperationLog(LogsConstants.ICMS + "修改查重状态")
    @PostMapping("updateCheckRepeatStatus")
    public Result updateCheckRepeatStatus(@RequestBody EcmFileInfoDTO ecmFileInfoDTO) {
        return fileInfoService.updateCheckRepeatStatus(ecmFileInfoDTO);
    }

    /**
     * 获取查重影像数据
     */
    @ApiOperation("获取查重影像数据")
    @OperationLog(LogsConstants.ICMS + "获取查重影像数据")
    @PostMapping("getCheckRepeatImageFiles")
    public Result getCheckRepeatImageFiles(@RequestBody List<EcmFileInfoDTO> ecmFileInfoDTOList) {
        return fileInfoService.getCheckRepeatImageFiles(ecmFileInfoDTOList);
    }

}
