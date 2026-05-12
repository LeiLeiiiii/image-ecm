package com.sunyard.module.storage.api;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.storage.constant.StateConstants;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.ecmbank.service.StorageBankEcmFileService;
import com.sunyard.module.storage.vo.DownFileVO;
import com.sunyard.module.storage.vo.FileByteVO;

/**
 * 文件处理类(银行)
 * @author P-JWei
 * @date 2024/5/22 14:47:18
 * @title
 * @description
 */
@RestController
public class FileHandleBankApiImpl implements FileHandleBankApi {

    @Resource
    private StorageBankEcmFileService storageBackEcmFileService;

    @Override
    public Result<List<SysFileDTO>> getBackEcmFiles(String contentId, String createDate) {
        return storageBackEcmFileService.queryFileListByContentId(contentId, createDate);
    }

    @Override
    public Result<byte[]> getFileByte(FileByteVO fileByteVO) {
        DownFileVO downFileVO = new DownFileVO();
        List<Long> ids = new ArrayList<>();
        ids.add(fileByteVO.getFileId());
        downFileVO.setFileId(ids)
                .setIsPack(StateConstants.COMMON_TWO)
                .setOrgCode(fileByteVO.getSessionId())
                .setOrgName(fileByteVO.getUsername());
        return Result.success(storageBackEcmFileService.downloadFiles(downFileVO));
    }

}
