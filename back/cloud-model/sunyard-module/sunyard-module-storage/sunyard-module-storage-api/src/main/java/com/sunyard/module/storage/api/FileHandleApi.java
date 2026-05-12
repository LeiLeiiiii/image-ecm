package com.sunyard.module.storage.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.storage.constant.ApiConstants;
import com.sunyard.module.storage.dto.MixedPastingSplitDTO;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.vo.FileByteVO;
import com.sunyard.module.storage.vo.FileEcmMergeVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 文件处理类
 *
 * @author zyl
 * @Description 文件相关操作
 * @since 2024/4/29 10:06
 */
@FeignClient(value = ApiConstants.NAME)
public interface FileHandleApi {
    String PREFIX = ApiConstants.PREFIX + "/fileHandle/";

    /**
     * 获取文件地址
     *
     * @param id 文件id
     * @return Result
     */
    @PostMapping(value = PREFIX + "getFileUrl", consumes = MediaType.APPLICATION_JSON_VALUE)
    Result<String> getFileUrl(@RequestParam("id") Long id);

    /**
     * 文件信息
     *
     * @param id 文件id集合
     * @return Result
     */
    @PostMapping(PREFIX + "getFileInfo")
    Result<SysFileDTO> getFileInfo(@RequestParam("id") Long id);

    /**
     * 文件详情列表
     *
     * @param fileIds 文件id集合
     * @return Result
     */
    @PostMapping(PREFIX + "details")
    Result<List<SysFileDTO>> details(@RequestBody List<Long> fileIds);

    /**
     * 混贴拆分
     *
     * @param mixedPastingSplitDTO 要拆分的文件信息
     * @return Result
     */
    @PostMapping(PREFIX + "mixedPastingSplit")
    Result<List<SysFileDTO>> mixedPastingSplit(@RequestBody MixedPastingSplitDTO mixedPastingSplitDTO);

    /**
     * 获得文件byte带水印
     *
     * @param fileByteVO 文件byte对象
     * @return Result
     */
    @PostMapping(value = PREFIX + "getFileByteWater", consumes = MediaType.APPLICATION_JSON_VALUE)
    Result<byte[]> getFileByteWater(@RequestBody FileByteVO fileByteVO);

    /**
     * 加密文件
     *
     * @param stFileIds 文件id
     * @return Result
     */
    @PostMapping(PREFIX + "encryptStFile")
    Result encryptStFile(@RequestParam("stFileIds") List<Long> stFileIds);

    /**
     * 获得原文件byte无水印
     *
     * @param fileByteVO 文件byte对象
     * @return Result
     */
    @PostMapping(value = PREFIX + "getFileByte", consumes = MediaType.APPLICATION_JSON_VALUE)
    Result<byte[]> getFileBytes(@RequestBody FileByteVO fileByteVO);

    /**
     * 文件合并
     *
     * @param ecmMergeVo 文件对象
     * @return Result
     */
    @PostMapping(value = PREFIX + "mergeFile", consumes = MediaType.APPLICATION_JSON_VALUE)
    Result<SysFileDTO> mergeFile(@RequestBody FileEcmMergeVO ecmMergeVo);


}
