package com.sunyard.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.module.system.api.dto.SysPostDTO;
import com.sunyard.module.system.po.SysPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 岗位mapper
 *
 * @author wml
 */
public interface SysPostMapper extends BaseMapper<SysPost> {

    /**
     * 获取用户下岗位
     *
     * @param userId
     * @return
     */
    List<SysPost> searchLinkedPost(@Param("userId") Long userId);

    /**
     * 多条件查询岗位信息
     *
     * @param sysPostDTO
     * @return
     */
    List<SysPostDTO> searchByCondition(@Param("sysPostDTO") SysPostDTO sysPostDTO);
}
