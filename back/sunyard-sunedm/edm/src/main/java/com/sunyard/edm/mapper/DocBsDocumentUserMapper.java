package com.sunyard.edm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.edm.dto.DocBsDocumentUserDTO;
import com.sunyard.edm.po.DocBsDocumentUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author raochangmei
 * @since 2021-12-14
 */
public interface DocBsDocumentUserMapper extends BaseMapper<DocBsDocumentUser> {

    /**
     * 获取用户、部门、团队、组织扩展
     *
     * @return
     */
    List<DocBsDocumentUserDTO> selectListExtend(@Param("docId") Long docId,@Param("isDeleted") Integer isDeleted);
}
