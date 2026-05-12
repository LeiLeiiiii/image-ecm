package com.sunyard.module.storage.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.storage.constant.ApiConstants;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.vo.DownFileVO;
import com.sunyard.module.storage.vo.FileSplitPdfVO;
import com.sunyard.module.storage.vo.SplitUploadBigFileVo;
import com.sunyard.module.storage.vo.SplitUploadVO;
import com.sunyard.module.storage.vo.UploadListVO;
import com.sunyard.module.storage.vo.UploadSplitVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

/**
 * 文件存储
 *
 * @author： zyl
 * @Desc: 文件上传、取消上传、下载、删除
 * @create： 2023/6/14 10:32
 */
@FeignClient(value = ApiConstants.NAME)
public interface FileStorageApi {
    String PREFIX = ApiConstants.PREFIX + "/fileStorage/";

    /**
     * 批量上传，并返回文件的信息
     *
     * @param file 文件对象
     * @return Result
     */
    @PostMapping(PREFIX + "uploadBatch")
    Result<List<SysFileDTO>> uploadBatch(@RequestBody List<UploadListVO> file);

    /**
     * 文件上传，并返回文件的信息
     *
     * @param file 文件对象
     * @return Result
     */
    @PostMapping(PREFIX + "upload")
    Result<SysFileDTO> upload(@RequestBody UploadListVO file);

    /**
     * 批量删除文件
     *
     * @param ids 文件id集合
     * @return Result
     */
    @PostMapping(PREFIX + "delBatch")
    Result delBatch(@RequestParam("ids") List<Long> ids);

    /**
     * 拆分
     * @param ecmSplitPdfVo
     * @return
     */
    @PostMapping(PREFIX + "splitFile")
    Result<List<SysFileDTO>> splitFile(@RequestBody FileSplitPdfVO ecmSplitPdfVo);

    /**
     * pdf文件拆分成base64
     * @param ecmSplitPdfVo
     * @return
     */
    @PostMapping(PREFIX + "splitPdfFile")
    Result splitPdfFile(@RequestBody FileSplitPdfVO ecmSplitPdfVo);

    /**
     * 取消上传
     *
     * @param md5           文件md5
     * @param stEquipmentId 存储设备id
     * @return Result
     */
    @PostMapping(PREFIX + "cancelUpload")
    Result cancelUpload(@RequestParam("md5") String md5, @RequestParam("stEquipmentId") String stEquipmentId);

    /**
     * 文件下载
     *
     * @param downFileVO 文件对象
     * @return Result
     */
    @PostMapping(PREFIX + "down")
    Result<byte[]> down(@RequestBody DownFileVO downFileVO);

    /**
     * 获取上传分片大小
     * @return
     */
    @PostMapping(PREFIX + "getUploadChunkSize")
    Result getUploadChunkSize();

    /**
     * 获取上传任务信息，没有就创建一个
     * @param splitUploadVO
     * @return
     */
    @PostMapping(PREFIX + "getUploadInfo")
    Result getUploadInfo(@RequestBody SplitUploadVO splitUploadVO);

    /**
     * 分片文件上传
     * @param uploadSplitVO
     * @return
     */
    @PostMapping(PREFIX + "uploadSplit")
    Result uploadSplit(@RequestBody UploadSplitVO uploadSplitVO);

    /**
     * 合并分片文件
     * @param vo
     * @return
     */
    @PostMapping(PREFIX + "mergeSplit")
    Result mergeSplit(@RequestBody SplitUploadBigFileVo vo);
}
