package com.sunyard.ecm.manager;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.service.OperateFullQueryService;
import com.sunyard.framework.common.util.AssertUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;


/**
 * @author rao
 * @date 2023/4/20
 * @describe 业务操作实现类
 */
@Service
public class BusiOperationService {
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private OperateFullQueryService operateFullQueryService;

    /**
     * 更新操作人信息
     */
    @Async("GlobalThreadPool")
    public void addOperation(Long busiId, Integer addComment, AccountTokenExtendDTO tokenExtend, String remark) {
        AssertUtils.isNull(busiId,"参数错误");
        String userId = tokenExtend.getUsername();
        // 更新影像业务信息表操作人信息
        Date date = new Date();
        LambdaUpdateWrapper<EcmBusiInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(EcmBusiInfo::getBusiId,busiId);
        wrapper.set(EcmBusiInfo::getUpdateUser,userId);
        wrapper.set(EcmBusiInfo::getUpdateTime, date);
        wrapper.set(EcmBusiInfo::getUpdateUserName,tokenExtend.getName());
        ecmBusiInfoMapper.update(null,wrapper);
        // 更新影像业务信息表操作人es信息
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = new EcmBusiInfoRedisDTO();
        ecmBusiInfoRedisDTO.setBusiId(busiId);
        ecmBusiInfoRedisDTO.setUpdateUserName(tokenExtend.getName());
        operateFullQueryService.editEsBusiInfo(ecmBusiInfoRedisDTO, tokenExtend.getUsername(), date);
    }

}
