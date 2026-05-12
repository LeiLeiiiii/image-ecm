package com.sunyard.module.storage.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

import com.sunyard.module.storage.vo.SplitUploadBigFileVo;
import com.sunyard.module.storage.vo.SplitUploadVO;
import com.sunyard.module.storage.vo.UploadSplitVO;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.FileUtils;
import com.sunyard.module.storage.config.properties.StorageUploadProperties;
import com.sunyard.module.storage.constant.StateConstants;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.ecmbank.service.StorageBankEcmFileService;
import com.sunyard.module.storage.service.FileDealService;
import com.sunyard.module.storage.service.SplitUploadTaskService;
import com.sunyard.module.storage.vo.DownFileVO;
import com.sunyard.module.storage.vo.FileSplitPdfVO;
import com.sunyard.module.storage.vo.UploadListVO;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import lombok.extern.slf4j.Slf4j;

/**
 * 文件存储
 *
 * @author： zyl
 * @create： 2023/6/14 10:31
 */
@Slf4j
@RestController
public class FileStorageApiImpl implements FileStorageApi {

    @Resource
    private FileDealService fileDealService;
    @Resource
    private SplitUploadTaskService splitUploadTaskService;
    @Resource
    private StorageBankEcmFileService storageBackEcmFileService;
    @Resource
    private StorageUploadProperties storageUploadProperties;

    @Override
    public Result<List<SysFileDTO>> uploadBatch(List<UploadListVO> file) {
        if (!CollectionUtils.isEmpty(file)) {
            if (!ObjectUtils.isEmpty(file.get(0).getType()) && StateConstants.COMMON_ONE.equals(file.get(0).getType())) {
                //银行影像上传
                return storageBackEcmFileService.backEcmUploadFile(file);
            } else if (!ObjectUtils.isEmpty(file.get(0).getType()) && StateConstants.COMMON_TWO.equals(file.get(0).getType())) {
                //银行影像补传
                return storageBackEcmFileService.supplementaryFile(file);
            }
        }
        List<SysFileDTO> sysFileDtoList = new ArrayList<>();
        file.parallelStream().forEach(p -> {
            SysFileDTO sysFileDTO = splitUploadTaskService.useS3Upload(p.getFileByte(), p.getUserId(), p.getStEquipmentId(), p.getFileName(), p.getFileSource(), p.getMd5(), p.getIsEncrypt());
            sysFileDtoList.add(sysFileDTO);
        });
        return Result.success(sysFileDtoList);
    }

    @Override
    public Result<SysFileDTO> upload(UploadListVO file) {
        return Result.success(splitUploadTaskService.useS3Upload(file.getFileByte(), file.getUserId(), file.getStEquipmentId(), file.getFileName(), file.getFileSource(), file.getMd5(), file.getIsEncrypt()));
    }

    @Override
    public Result delBatch(List<Long> ids) {
        List<SysFileDTO> details = fileDealService.details(ids);
        AssertUtils.isNull(details, "参数错误");
        for (SysFileDTO sysFileDTO : details) {
            try {
                fileDealService.deleteFile(sysFileDTO.getId());
            } catch (Exception e) {
                log.error("删除文件失败",e);
                throw new RuntimeException(e);
            }
        }
        return Result.success();
    }

    @Override
    public Result<List<SysFileDTO>> splitFile(FileSplitPdfVO ecmSplitPdfVo){
        AccountToken accountToken = JSONObject.parseObject(ecmSplitPdfVo.getToken(), AccountToken.class);
        return Result.success(fileDealService.splitFile(ecmSplitPdfVo,accountToken));
    }

    @Override
    public Result splitPdfFile(FileSplitPdfVO ecmSplitPdfVo) {
        AccountToken accountToken = JSONObject.parseObject(ecmSplitPdfVo.getToken(), AccountToken.class);
        return Result.success(fileDealService.splitPdfFile(ecmSplitPdfVo,accountToken));
    }

    @Override
    public Result cancelUpload(String md5, String stEquipmentId) {
        splitUploadTaskService.cancelFileUpload(md5, stEquipmentId);
        return Result.success();
    }

    @Override
    public Result<byte[]> down(DownFileVO downFileVO) {
        if (!ObjectUtils.isEmpty(downFileVO.getType()) && StateConstants.COMMON_ONE.equals(downFileVO.getType())) {
            return Result.success(storageBackEcmFileService.downloadFiles(downFileVO));
        } else {
            AccountToken accountToken = new AccountToken();
            accountToken.setName(downFileVO.getName());
            accountToken.setUsername(downFileVO.getUsername());
            accountToken.setOrgCode(downFileVO.getOrgCode());
            accountToken.setOrgName(downFileVO.getOrgName());
            String zipPath = null;
            if (downFileVO.getIsPack().equals(StateConstants.COMMON_ONE)){
                zipPath = storageUploadProperties.getFileDownTemp() + System.currentTimeMillis() + ".zip";
            }
            InputStream inputStream = fileDealService.downFileByInputStream(downFileVO.getSessionId(), downFileVO, accountToken, zipPath);
            byte[] fileBytes = FileUtils.read(inputStream);
            // 删除downFileByInputStream方法中产生的临时文件
            if (zipPath!=null){
                try {
                    FileUtil.del(zipPath);
                } catch (IORuntimeException e) {
                    log.error("文件删除失败", e);
                    throw new RuntimeException(e);
                }
            }
            return Result.success(fileBytes);
        }
    }

    @Override
    public Result getUploadChunkSize() {
        return Result.success(storageUploadProperties.getChunkSize());
    }

    @Override
    public Result getUploadInfo(SplitUploadVO splitUploadVO) {
        AccountToken token = new AccountToken();
        token.setId(splitUploadVO.getUserId());
        return Result.success(splitUploadTaskService.getUploadInfo(splitUploadVO, token));
    }

    @Override
    public Result uploadSplit(UploadSplitVO uploadSplitVO) {
        uploadSplitVO.setInputStream(new ByteArrayInputStream(uploadSplitVO.getBytes()));
        return Result.success(splitUploadTaskService.uploadSplit(uploadSplitVO));
    }

    @Override
    public Result mergeSplit(SplitUploadBigFileVo vo) {
        return Result.success(splitUploadTaskService.merge(vo.getIdentifier(),
                vo.getIsFlat(), vo.getId(), vo.getEquipmentId()));
    }
}

