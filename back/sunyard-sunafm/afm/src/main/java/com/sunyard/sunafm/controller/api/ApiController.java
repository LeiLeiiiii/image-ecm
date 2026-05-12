package com.sunyard.sunafm.controller.api;


import com.alibaba.fastjson.JSONArray;
import com.sunyard.afm.api.dto.AfmDetImgDetDTO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.sunafm.constant.AfmConstant;
import com.sunyard.sunafm.controller.BaseController;
import com.sunyard.sunafm.service.api.ApiService;
import com.sunyard.sunafm.service.CommonService;
import com.sunyard.sunafm.service.RecordDupService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Description: 对外接口-外部接口
 * @Date: 2023/8/14
 */
@RestController
@RequestMapping("/api")
public class ApiController extends BaseController {

    @Resource
    private ApiService apiService;
    @Resource
    private RecordDupService recordDupService;
    @Resource
    private CommonService commonService;
    /**
     * 发起反欺诈检测接口
     */
    @PostMapping("antiFraudDetNowBase64")
    public Result<Map> antiFraudDetNowBase64(@RequestBody AfmDetImgDetDTO vo) {
        return Result.success(recordDupService.antiFraudDetNowBase64(vo));
    }

    /**
     * 自动切换服务器
     */
    @PostMapping("queryServer")
    public Result queryServer() {
        commonService.queryServer();
        return Result.success();
    }

    /**
     * 发起反欺诈检测接口
     */
    @PostMapping("saveFeatureNow")
    public Result saveFeatureNow(String json, List<MultipartFile> fileList1) {
        List<AfmDetImgDetDTO> dtos = JSONArray.parseArray(json, AfmDetImgDetDTO.class);
        apiService.saveFeatureNow(fileList1,dtos);
        return Result.success();
    }

    /**
     * 发起反欺诈检测接口
     */
    @PostMapping("antiFraudDetNow")
    public Result<Map> antiFraudDetNow(@RequestBody AfmDetImgDetDTO vo) {
        vo.setBusinessType(vo.getBusinessTypeCode() + AfmConstant.SUFF + vo.getBusinessTypeName());
        vo.setMaterialType(vo.getMaterialTypeCode() + AfmConstant.SUFF + vo.getMaterialTypeName());
        return Result.success(apiService.antiFraudDetNow(vo));
    }


    /**
     * 发起反欺诈检测接口
     */
    @PostMapping("antiFraudDet")
    public Result antiFraudDet(@RequestBody AfmDetImgDetDTO dto) {
        apiService.antiFraudDet(dto);
        return Result.success();
    }

    /**
     * 获取反欺诈结果接口
     */
    @PostMapping("antiFraudDetRes")
    public Result<Map> antiFraudDetRes(@RequestBody AfmDetImgDetDTO dto) {
        return Result.success(apiService.antiFraudDetRes(dto));
    }


    @PostMapping("delFile")
    public Result delFile(@RequestBody AfmDetImgDetDTO dto){
        recordDupService.delFile(dto);
        return Result.success();
    }

}
