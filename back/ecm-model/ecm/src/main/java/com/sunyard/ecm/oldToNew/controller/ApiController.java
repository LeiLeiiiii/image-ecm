package com.sunyard.ecm.oldToNew.controller;


import com.sunyard.ecm.bean.ResponseBean;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.exception.OldToNewException;
import com.sunyard.ecm.oldToNew.service.ApiService;
import com.sunyard.ecm.util.FunctionUtil;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.ApiLog;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *     老影像切换接口
 *     对接老影像的接口打到这里,然后数据处理中转后调用微服务影像接口
 *
 * @author yzy
 * @since 2025/02/17 10:55
 */
@RestController
@RequestMapping("/api/ecms/oldToNew")
public class ApiController  {


    @Resource
    private ApiService apiService;

    //影像查询调阅,ECM0002
    @RequestMapping("ECM0002")
    @ApiLog(LogsConstants.OLD_TO_NEW + "影像查询调阅" )
    public Result ECM0002(HttpServletRequest request,HttpServletResponse response) {
        return Result.success(apiService.accessEcmOldECM0002(request,response));
    }

    //影像查询调阅,ECM0001
    @RequestMapping("ECM0001")
    @ApiLog(LogsConstants.OLD_TO_NEW + "影像扫描修改" )
    public Result ECM0001(HttpServletRequest request,HttpServletResponse response) {
        return Result.success(apiService.accessEcmOldECM0001(request,response));
    }

    //移动影像查询调阅,ECM10004
    @RequestMapping("ECM10004")
    @ApiLog(LogsConstants.OLD_TO_NEW + "移动端影像查询调阅" )
    public Result ECM10004(HttpServletRequest request,HttpServletResponse response) {
        return Result.success(apiService.accessEcmOldECM10004(request,response));
    }

    //移动影像采集调阅,ECM10003
    @RequestMapping("ECM10003")
    @ApiLog(LogsConstants.OLD_TO_NEW + "移动端影像采集调阅" )
    public Result ECM10003(HttpServletRequest request,HttpServletResponse response) {
        return Result.success(apiService.accessEcmOldECM10003(request,response));
    }

    //影像资源请求接口ECM0010
    @RequestMapping("ECM0010")
    @ApiLog(LogsConstants.OLD_TO_NEW + "影像资源请求接口" )
    public String ECM0010(HttpServletRequest request) {
        return apiService.accessEcmOldECM0010(request);
    }

    //资源校验
    @RequestMapping("ECM0013")
    @ApiLog(LogsConstants.OLD_TO_NEW + "影像校验接口" )
    public String ECM0013(HttpServletRequest request) {
        return apiService.accessEcmOldECM0013(request);
    }

    //资源统计接口
    @RequestMapping("ECM0006")
    @ApiLog(LogsConstants.OLD_TO_NEW + "资源统计接口" )
    public String ECM0006(HttpServletRequest request) {
        return apiService.accessEcmOldECM0006(request);
    }

    //下载接口
    @RequestMapping("ECM0009")
    @ApiLog(LogsConstants.OLD_TO_NEW + "下载接口" )
    public String ECM0009(HttpServletRequest request) {
        return apiService.accessEcmOldECM0009(request);
    }


    //索引回写接口
    @RequestMapping("ECM0014")
    @ApiLog(LogsConstants.OLD_TO_NEW + "属性回写" )
    public String ECM0014(HttpServletRequest request) {
        return apiService.accessEcmOldECM0014(request);
    }

    //影像复制接口,与后台基本一致，可以共用
    @RequestMapping(value = "ECM0023",produces = "application/xml")
    @ApiLog(LogsConstants.OLD_TO_NEW + "影像复制接口" )
    public String ECM0023(HttpServletRequest request) {
        return apiService.accessEcmOldECM0026(request);
    }

    //影像删除
    @RequestMapping("ECM0025")
    @ApiLog(LogsConstants.OLD_TO_NEW + "影像删除" )
    public String ECM0025(HttpServletRequest request) {
        return apiService.accessEcmOldECM0025(request);
    }

    //影像跨业务复制接口（后台）
    @RequestMapping("ECM0026")
    @ApiLog(LogsConstants.OLD_TO_NEW + "影像跨业务复制接口（后台）" )
    public String ECM0026(HttpServletRequest request) {
        return apiService.accessEcmOldECM0026(request);
    }


    @RequestMapping("downloadFile")
    public void download(String fileName,HttpServletRequest request, HttpServletResponse response) {
        apiService.download(fileName,request,response);
    }

    // 在 Controller 内部捕获自定义异常
    @ExceptionHandler(OldToNewException.class)
    public String handleCustomException(OldToNewException ex) {
        // 处理异常并返回特定的消息
        ResponseBean res=new ResponseBean(500,ex.getMessage());
        return FunctionUtil.toXml(res);
    }
}
