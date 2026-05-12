package com.sunyard.ecm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.mapper.EcmBusiMetadataMapper;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.po.EcmBusiMetadata;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author WJJ
 * @date 2025/5/15
 * @describe 影像查询实现类
 */
@Service
public class CaptureQueryService {
    @Resource
    private EcmAppAttrMapper ecmAppAttrMapper;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private EcmBusiMetadataMapper ecmBusiMetadataMapper;

    /**
     * 属性按钮-获取属性信息
     */
    public List<EcmAppAttrDTO> getBusiAttrInfo(String appCode, Long busiId, AccountTokenExtendDTO token) {
        AssertUtils.isNull(appCode, "参数错误");
        AssertUtils.isNull(busiId, "参数错误");
        //先看缓存有没，没有走持久化数据库
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, busiId);
        if (ecmBusiInfoRedisDTO==null) {
            //走持久化数据库
            List<EcmAppAttr> appAttrs = ecmAppAttrMapper.selectList(new LambdaQueryWrapper<EcmAppAttr>()
                    .eq(EcmAppAttr::getAppCode, appCode));
            if (CollectionUtils.isEmpty(appAttrs)) {
                return Collections.emptyList();
            }
            List<EcmAppAttrDTO> ecmAppAttrDTOS = PageCopyListUtils.copyListProperties(appAttrs, EcmAppAttrDTO.class);
            List<EcmBusiMetadata> ecmBusiMetadata = ecmBusiMetadataMapper.selectList(new LambdaQueryWrapper<EcmBusiMetadata>()
                    .eq(EcmBusiMetadata::getBusiId, busiId));
            if (CollectionUtils.isEmpty(ecmBusiMetadata)) {
                return ecmAppAttrDTOS;
            }
            Map<Long, List<EcmBusiMetadata>> groupedByAttr = ecmBusiMetadata.stream().collect(Collectors.groupingBy(EcmBusiMetadata::getAppAttrId));
            for (EcmAppAttrDTO ecmAppAttrDTO : ecmAppAttrDTOS) {
                List<EcmBusiMetadata> busiMetadata = groupedByAttr.get(ecmAppAttrDTO.getAppAttrId());
                if (!CollectionUtils.isEmpty(busiMetadata)) {
                    ecmAppAttrDTO.setAppAttrValue(busiMetadata.get(0).getAppAttrVal());
                }
            }
            return ecmAppAttrDTOS;
        } else {
            //走缓存
            if (CollectionUtils.isEmpty(ecmBusiInfoRedisDTO.getAttrList())) {
                return Collections.emptyList();
            }
            return ecmBusiInfoRedisDTO.getAttrList();
        }
    }
}
