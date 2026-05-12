package com.sunyard.afm.api;
/*
 * Project: Sunyard
 *
 * File Created at 2023/5/6
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import com.sunyard.afm.api.dto.AfmDetImgDetDTO;
import com.sunyard.afm.api.dto.AfmDetUpdateDto;
import com.sunyard.afm.constant.ApiConstants;
import com.sunyard.framework.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @author Leo
 * @Desc
 * @date 2023/5/6 10:20
 */
@FeignClient(name = ApiConstants.NAME)
public interface AntiFraudDetApi {

    String PREFIX = ApiConstants.PREFIX + "/antiFraudDet/";
    /**
     * 获取反欺诈结果接口
     *
     * @return
     */
    @PostMapping(PREFIX+"saveFeature")
    Result saveFeature(@RequestBody AfmDetImgDetDTO dto);

    /**
     * 发起反欺诈检测接口
     *
     * @return
     */
    @PostMapping(PREFIX+"antiFraudDet")
    Result antiFraudDet(@RequestBody AfmDetImgDetDTO dto);

    /**
     * 反欺诈实时接口
     *
     * @return
     */
    @PostMapping(PREFIX+"antiFraudDetNow")
    Result antiFraudDetNow(@RequestBody AfmDetImgDetDTO dto);

    @PostMapping(PREFIX+"delFile")
    Result delFile(@RequestBody AfmDetImgDetDTO dto);

    /**
     * 获取反欺诈结果接口
     *
     * @return
     */
    @PostMapping(PREFIX+"antiFraudDetRes")
    Result<Map> antiFraudDetRes(@RequestBody AfmDetImgDetDTO dto);

    @PostMapping(PREFIX+"antiFraudDetList")
    Result antiFraudDetList(@RequestBody List<AfmDetImgDetDTO> dto);

    @PostMapping(PREFIX+"saveFeatureByText")
    Result saveFeatureByText(@RequestBody AfmDetImgDetDTO dto);

    @PostMapping(PREFIX+"saveFeatureByTextNow")
    Result saveFeatureByTextNow(@RequestBody AfmDetImgDetDTO dto);

    @PostMapping(PREFIX+"antiFraudDetResByText")
    Result<Map> antiFraudDetResByText(@RequestBody AfmDetImgDetDTO dto);

    @PostMapping(PREFIX+"ecmToAfmDataSync")
    Result ecmToAfmDataSync(@RequestBody AfmDetUpdateDto dto);
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/5/6 Leo creat
 */
