package com.sunyard.ecm.manager;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.EcmBaseInfoDTO;
import com.sunyard.ecm.dto.EcmDownloadFileDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author WJJ
 * @desc 影像采集实现公共类
 * @since 2025-5-15
 */
@Slf4j
@Service
public class CaptureService {
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private BusiCacheService busiCacheService;


    /**
     * 根据业务或者资料获取文件列表
     */
    public Result getFileInfoByBusiOrDoc(EcmDownloadFileDTO ecmDownloadFileDTO) {
        AccountTokenExtendDTO accountTokenExtendDTO = busiCacheService.checkUser(ecmDownloadFileDTO.getEcmBaseInfoDTO(), null);
        EcmBaseInfoDTO ecmBaseInfoDTO = ecmDownloadFileDTO.getEcmBaseInfoDTO();
        ecmBaseInfoDTO.setUserName(accountTokenExtendDTO.getName());
        ecmBaseInfoDTO.setOrgName(accountTokenExtendDTO.getOrgName());
        ecmDownloadFileDTO.setEcmBaseInfoDTO(ecmBaseInfoDTO);
        AssertUtils.isNull(ecmDownloadFileDTO.getAppCode(), "参数有误");
        AssertUtils.isNull(ecmDownloadFileDTO.getBusiNo(), "参数有误");
        //根据业务编号查询业务
        String appNo = ecmDownloadFileDTO.getAppCode();
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectList(new LambdaQueryWrapper<EcmAppDef>().eq(EcmAppDef::getAppCode, appNo));
        AssertUtils.isNull(ecmAppDefs, "参数有误");
        String busiNo = ecmDownloadFileDTO.getBusiNo();
        String docNo = ecmDownloadFileDTO.getDocNo();
        LambdaQueryWrapper<EcmBusiInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmBusiInfo::getBusiNo, busiNo).eq(EcmBusiInfo::getAppCode, ecmAppDefs.get(0).getAppCode());
        List<EcmBusiInfo> ecmBusiInfos = ecmBusiInfoMapper.selectList(wrapper);
        AssertUtils.isNull(ecmBusiInfos, "业务数据不存在");
        List<FileInfoRedisDTO> newFileList = new ArrayList<>();
        EcmBusiInfo ecmBusiInfo = ecmBusiInfos.get(0);
        Long busiId = ecmBusiInfo.getBusiId();
        //是否指定了fileId
        //是否指定了资料节点
        //都没有全部文件
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(busiId);
        if (CollectionUtil.isNotEmpty(fileInfoRedis)) {
            if (!CollectionUtils.isEmpty(ecmDownloadFileDTO.getFiles())) {
                newFileList = fileInfoRedis.stream().filter(s -> ecmDownloadFileDTO.getFiles().contains(s.getNewFileId())).collect(Collectors.toList());
            } else if (StrUtil.isBlank(docNo)) {
                newFileList = fileInfoRedis.stream().filter(s -> StateConstants.NO.equals(s.getState())).collect(Collectors.toList());
            } else {
                newFileList = fileInfoRedis.stream().filter(s -> StateConstants.NO.equals(s.getState()) && docNo.equals(s.getDocCode())).collect(Collectors.toList());
            }
        }
        AssertUtils.isNull(newFileList, "目标节点无文件");
        //根据newFileList查询存储那边文件完整信息
        Map map = new HashMap();
        map.put("appcode", ecmBusiInfo.getAppCode());
        map.put("appname", ecmAppDefs.get(0).getAppName());
        map.put("busino", ecmBusiInfo.getBusiNo());
        map.put("fileList", JSONObject.toJSONString(newFileList));
        return Result.success(map);
    }
}
