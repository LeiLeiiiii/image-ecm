package com.sunyard.module.storage.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.storage.constant.ApiConstants;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.vo.FileByteVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 文件处理类(银行)
 * @author P-JWei
 * @date 2024/5/22 14:41:57
 * @title
 * @description
 */
@FeignClient(value = ApiConstants.NAME)
public interface FileHandleBankApi {

    String PREFIX = ApiConstants.PREFIX + "/fileHandleBank/";

    /**
     * 查询银行影像文件
     * @param contentId 内容id
     * @param createDate 创建日期
     * @return Result
     */
    @PostMapping(PREFIX + "getBackEcmFiles")
    Result<List<SysFileDTO>> getBackEcmFiles(@RequestParam("contentId")String contentId, @RequestParam("createDate")String createDate);

    /**
     * 获得文件byte
     *
     * @param fileByteVO 文件byte对象
     * @return Result
     */
    @PostMapping(value = PREFIX + "getFileByte", consumes = MediaType.APPLICATION_JSON_VALUE)
    Result<byte[]> getFileByte(@RequestBody FileByteVO fileByteVO);

}
