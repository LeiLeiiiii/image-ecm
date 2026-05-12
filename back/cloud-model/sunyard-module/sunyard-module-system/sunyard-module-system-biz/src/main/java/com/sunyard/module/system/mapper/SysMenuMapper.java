package com.sunyard.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.module.system.po.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 菜单权限表 Mapper 接口
 * </p>
 *
 * @author zhouleibin
 * @since 2021-08-16
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 得到菜单模块
     *
     * @param userId 用户id
     * @param type 类型
     * @return Result
     */
    List<SysMenu> getMenuByUserIdAll(@Param("userId") Long userId,@Param("type") Integer type);
    /**
     * 得到菜单模块
     * 
     * @param userId 用户id
     * @param type 类型
     * @return Result
     */
    List<SysMenu> getMenuByUserId(@Param("userId") Long userId,@Param("type") Integer type);
}
