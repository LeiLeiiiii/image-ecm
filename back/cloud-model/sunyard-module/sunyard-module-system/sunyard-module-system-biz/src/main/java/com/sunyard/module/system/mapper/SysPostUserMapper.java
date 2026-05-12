package com.sunyard.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.module.system.api.dto.SysPostDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.dto.SysPostUserListDTO;
import com.sunyard.module.system.po.SysPostUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 岗位用户关联mapper
 *
 * @author wml
 */
public interface SysPostUserMapper extends BaseMapper<SysPostUser> {

    /**
     * 多条件查询岗位下关联的用户信息
     *
     * @param sysPostDTO
     * @return
     */
    List<SysUserDTO> searchPostUserByCondition(@Param("sysPostDTO") SysPostDTO sysPostDTO);

    /**
     * 根据userIds查询岗位名称id
     *
     * @param list
     * @return
     */
    List<SysPostUserListDTO> selectListByUserIdList(List<Long> list);
}
