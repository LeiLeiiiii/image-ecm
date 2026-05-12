package com.sunyard.module.storage.api;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.FileUtils;
import com.sunyard.module.storage.constant.ImgsConstants;
import com.sunyard.module.storage.constant.StateConstants;
import com.sunyard.module.storage.dto.MixedPastingSplitDTO;
import com.sunyard.module.storage.dto.StFileDTO;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.manager.StFileService;
import com.sunyard.module.storage.service.FileDealService;
import com.sunyard.module.storage.vo.FileByteVO;
import com.sunyard.module.storage.vo.FileEcmMergeVO;
import com.sunyard.module.storage.vo.FileMergeVO;
import com.sunyard.module.storage.vo.WaterMarkConfigVO;

import lombok.extern.slf4j.Slf4j;

/**
 * 文件处理类
 * @author zyl
 * @Description
 * @since 2024/4/29 10:13
 */
@Slf4j
@RestController
public class FileHandleApiImpl implements FileHandleApi {

    @Resource
    private FileDealService fileDealService;
    @Resource
    private StFileService stFileService;

    @Override
    public Result<String> getFileUrl(@RequestParam("id") Long id) {
        return Result.success(fileDealService.getFileUrl(id));
    }

    @Override
    public Result<List<SysFileDTO>> details(@RequestBody List<Long> fileIds) {
        return Result.success(fileDealService.details(fileIds));
    }

    @Override
    public Result<SysFileDTO> getFileInfo(Long id) {
        return Result.success(fileDealService.getFileInfo(id));
    }

    @Override
    public Result<List<SysFileDTO>> mixedPastingSplit(MixedPastingSplitDTO mixedPastingSplitDTO) {
        return Result.success(fileDealService.mixedPastingSplit(mixedPastingSplitDTO));
    }

    @Override
    public Result<byte[]> getFileByteWater(FileByteVO fileByteVO) {
        WaterMarkConfigVO waterMarkConfigVO = null;
        String sessionId = null;
        //开启水印才需要的参数
        if (!Objects.isNull(fileByteVO.getOpenFlag()) && fileByteVO.getOpenFlag().equals(StateConstants.COMMON_ONE)){
            AccountToken accountToken = new AccountToken();
            accountToken.setName(fileByteVO.getName());
            accountToken.setUsername(fileByteVO.getUsername());
            accountToken.setOrgCode(fileByteVO.getOrgCode());
            accountToken.setOrgName(fileByteVO.getOrgName());
            sessionId = fileByteVO.getSessionId();
            AssertUtils.isNull(accountToken, "参数错误: 用户信息(token)不能为空");
            AssertUtils.isNull(sessionId, "参数错误: 会话id(sessionId)不能为空");
            // 获取水印配置
            waterMarkConfigVO = fileDealService.getWaterMarkConfig(ImgsConstants.WATERMARK_TYPE_SHOW, accountToken);
        }
        Long fileId = fileByteVO.getFileId();
        AssertUtils.isNull(fileId, "参数错误: 文件id(fileId)不能为空");
        StFileDTO stFileDTO = stFileService.selectFileDTO(fileId);
        AssertUtils.isNull(stFileDTO, "文件id(fileId)查询文件为空");
        stFileDTO.setPassword(fileByteVO.getPassword());
        InputStream inputStream = fileDealService.getInputStream(fileId, sessionId, stFileDTO, waterMarkConfigVO);
        byte[] fileBytes = FileUtils.read(inputStream);
        return Result.success(fileBytes);
    }

    @Override
    public Result encryptStFile(List<Long> stFileIds) {
        return fileDealService.encryptStFile(stFileIds);
    }


    @Override
    public Result<byte[]> getFileBytes(FileByteVO fileByteVO) {
        Long fileId = fileByteVO.getFileId();
        AssertUtils.isNull(fileId, "参数错误: 文件id(fileId)不能为空");
        StFileDTO stFile = stFileService.selectFileDTO(fileId);
        InputStream inputStream = fileDealService.getInitialInputStream(fileId, stFile);
        byte[] fileBytes = FileUtils.read(inputStream);
        return Result.success(fileBytes);
    }

    @Override
    public Result<SysFileDTO> mergeFile(FileEcmMergeVO ecmMergeVo) {
        AccountToken accountToken=new AccountToken();
        accountToken.setId(ecmMergeVo.getUserId());
        FileMergeVO fileMergeVO=new FileMergeVO();
        BeanUtils.copyProperties(ecmMergeVo,fileMergeVO);
        fileMergeVO.setFileIdList(ecmMergeVo.getNewFileIdList());
        return Result.success(fileDealService.mergeFile(fileMergeVO, accountToken));
    }
}
