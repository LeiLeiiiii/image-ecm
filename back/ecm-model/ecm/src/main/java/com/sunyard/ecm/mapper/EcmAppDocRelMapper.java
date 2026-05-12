package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.dto.ecm.EcmAppDocRelInfoDTO;
import com.sunyard.ecm.po.EcmAppDocRel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ty
 * @since 2023-4-18 9:43
 * @desc 业务关联资料接口
 */
public interface EcmAppDocRelMapper extends BaseMapper<EcmAppDocRel> {
    /**
     * @param list
     * @return
     */
    int insertList(@Param("list") List list);

    /**
     * 获取资料类型code
     * @param appCode
     * @return
     */
    List<EcmAppDocRelInfoDTO> selectListByAppTypeId(@Param("appCode") String appCode,@Param("busiId") Long busiId);
}
