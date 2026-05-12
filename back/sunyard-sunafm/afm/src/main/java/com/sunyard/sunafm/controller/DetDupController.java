package com.sunyard.sunafm.controller;

import com.github.pagehelper.PageInfo;
import com.sunyard.afm.api.dto.AfmDetImgDetDTO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.sunafm.dto.AfmDetOnlineFileDTO;
import com.sunyard.sunafm.service.DetDupService;
import com.sunyard.sunafm.vo.AfmDetOnlineListVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Map;


/**
 * @author P-JWei
 * @date 2024/3/7 15:58:13
 * @title
 * @description  在线检测/查重检测
 */
@RestController
@RequestMapping("det/dup")
public class DetDupController extends BaseController {

    @Resource
    private DetDupService detDupService;


    /**
     * 开始查重在线查重
     */
    @OperationLog("在线检测-查重检测-开始查重在线查重")
    @PostMapping("dupByFile")
    public Result<AfmDetImgDetDTO> dupByFile(MultipartFile file, String token, Long exifId) {
        return Result.success(detDupService.dupOnline(file,exifId,token,getToken()));
    }


    /**
     * 开始查重在线查重
     */
    @OperationLog("在线检测-查重检测-开始查重在线查重")
    @PostMapping("antiFraudDetResPage")
    public Result<Map> antiFraudDetResPage(AfmDetImgDetDTO dto, PageForm pageForm) {
        Map map = detDupService.antiFraudDetResPage(dto, pageForm);
        return Result.success(map);
    }

    /**
     * 开始查重在线查重-不分页
     */
    @OperationLog("在线检测-查重检测-开始查重在线查重-不分页")
    @PostMapping("antiFraudDetRes")
    public Result<Map> antiFraudDetRes(AfmDetImgDetDTO vo) {
        Map map = detDupService.antiFraudDetRes(vo);
        return Result.success(map);
    }

    /**
     * 选择文件
     */
    @OperationLog("在线检测-查重检测-开始查重在线查重-不分页")
    @PostMapping("queryChooseConditions")
    public Result<Map> queryChooseConditions() {
        return Result.success(detDupService.queryChooseConditions());
    }


    /**
     * 选择文件
     */
    @OperationLog("在线检测-查重检测-选择文件")
    @PostMapping("chooseFile")
    public Result<PageInfo<AfmDetOnlineFileDTO>> chooseFile(AfmDetOnlineListVO afmDetOnlineListVO, PageForm pageForm) {
        return Result.success(detDupService.chooseFile(afmDetOnlineListVO,pageForm));
    }


}
