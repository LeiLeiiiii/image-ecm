package com.sunyard.sunafm.api;


import com.sunyard.afm.api.AntiFraudDetApi;
import com.sunyard.afm.api.dto.AfmDetImgDetDTO;
import com.sunyard.afm.api.dto.AfmDetUpdateDto;
import com.sunyard.framework.common.result.Result;
import com.sunyard.sunafm.service.api.ApiService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author： zyl
 * @Description： 反欺诈检测
 * @create： 2023/6/14 10:31
 */
@RestController
public class AntiFraudDetApiImpl implements AntiFraudDetApi {
    @Resource
    private ApiService apiService;

    @Override
    public Result saveFeature(AfmDetImgDetDTO dto) {
        apiService.saveFeature(dto);
        return Result.success();
    }

    @Override
    public Result antiFraudDet(AfmDetImgDetDTO dto) {
        apiService.antiFraudDet(dto);
        return Result.success();
    }

    @Override
    public Result antiFraudDetNow(AfmDetImgDetDTO dto) {
        return Result.success(apiService.antiFraudDetNow(dto));
    }

    @Override
    public Result delFile(AfmDetImgDetDTO dto) {
        apiService.delFile(dto);
        return Result.success();
    }

    @Override
    public Result<Map> antiFraudDetRes(AfmDetImgDetDTO dto) {
        return Result.success(apiService.antiFraudDetRes(dto));
    }

    @Override
    public Result antiFraudDetList(List<AfmDetImgDetDTO> dto) {
        apiService.antiFraudDetList(dto);
        return Result.success();
    }

    @Override
    public Result saveFeatureByText(AfmDetImgDetDTO dto) {
        apiService.antiFraudDetByText(dto);
        return Result.success();
    }

    @Override
    public Result saveFeatureByTextNow(AfmDetImgDetDTO dto) {
        return Result.success(apiService.saveFeatureByTextNow(dto));
    }

    @Override
    public Result<Map> antiFraudDetResByText(AfmDetImgDetDTO dto) {
        return Result.success(apiService.antiFraudDetResByText(dto));
    }
    @Override
    public Result ecmToAfmDataSync(AfmDetUpdateDto dto) {
        apiService.ecmToAfmDataSync(dto);
        return Result.success();
    }
}
