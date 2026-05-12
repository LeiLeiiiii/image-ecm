package com.sunyard.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.module.system.api.dto.SysRoleUserDTO;
import com.sunyard.module.system.dto.SysRoleUserListDTO;
import com.sunyard.module.system.po.SysRoleUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhouleibin
 * @since 2021-08-16
 */
public interface SysRoleUserMapper extends BaseMapper<SysRoleUser> {
    /**
     * 获取列表，带用户名称
     * @param  roleIds 条件构造器
     * @return 角色-用户obj
     */
    List<SysRoleUserDTO> selectListAddUserName(
            @Param("roleIds") List<Long> roleIds,
            @Param("relateUserName") String relateUserName
    );

    /**
     * 获取关联的角色名称id
     * @param list
     * @return
     */
    List<SysRoleUserListDTO> selectListByUserIdList(@Param("list") List<Long> list);
}
