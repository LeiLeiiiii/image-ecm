package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.dto.ecm.EcmDtdAttrDTO;
import com.sunyard.ecm.po.EcmDocDef;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ty
 * @since 2023-4-18 9:43
 * @desc 业务资料定义接口
 */
public interface EcmDocDefMapper extends BaseMapper<EcmDocDef> {
    /**
     * @param identifyCode
     * @return
     */
    List<EcmDtdAttrDTO> selectDtdAttr(@Param("identifyCode") String identifyCode);

    List<EcmDocDef> selectChildren(@Param("isRemade") Integer isRemade,@Param("isRegularized") Integer isRegularized,@Param("isObscured") Integer isObscured,@Param("isPlagiarism") Integer isPlagiarism,@Param("isAutoClassified") Integer isAutoClassified);
}
