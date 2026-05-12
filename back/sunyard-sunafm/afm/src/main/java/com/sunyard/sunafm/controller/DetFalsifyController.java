package com.sunyard.sunafm.controller;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.sunafm.dto.AfmDetOnlinePsDetDTO;
import com.sunyard.sunafm.service.DetFalsifyService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author P-JWei
 * @date 2024/3/7 15:58:27
 * @title
 * @description  在线检测/篡改检测
 */
@RestController
@RequestMapping("det/falsify")
public class DetFalsifyController extends BaseController {

    @Resource
    private DetFalsifyService detFalsifyService;

    /**
     * 开始检测
     */
    @OperationLog("在线检测-篡改检测-开始检测")
    @PostMapping("det")
    public Result<AfmDetOnlinePsDetDTO> det(MultipartFile file, Long exifId, String token) {
        return detFalsifyService.det(file, exifId,token, getToken().getId());
    }

}
