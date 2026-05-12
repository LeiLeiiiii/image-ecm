package com.sunyard.mytool.service.sys;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.SysUser;


public interface SysUserService extends IService<SysUser> {


    /**
     * 获取用户
     */
    SysUser selectSysUser(String loginName);


    /**
     * 获取创建用户
     */
    SysUser handleUser(String loginName);
}
