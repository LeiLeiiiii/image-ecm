package com.sunyard.mytool.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.SysUser;
import com.sunyard.mytool.mapper.db.sys.SysUserMapper;
import com.sunyard.mytool.service.sys.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Value("${unknownUserId}")
    private Long unknownUserId;
    @Value("${unknownUserLoginName}")
    private String unknownUserLoginName;
    @Value("${unknownUserName}")
    private String unknownUserName;

    /**
     * 获取用户
     */
    public SysUser selectSysUser(String loginName) {
        LambdaQueryWrapper<SysUser> sysUserQW = new LambdaQueryWrapper<>();
        sysUserQW.eq(SysUser::getLoginName, loginName);
        List<SysUser> sysUserList = baseMapper.selectList(sysUserQW);
        return sysUserList.isEmpty() ? null : sysUserList.get(0);
    }

    /**
     * 获取用户
     * @param loginName
     * @return
     */
    @Override
    public SysUser handleUser(String loginName) {
        LambdaQueryWrapper<SysUser> sysUserQW = new LambdaQueryWrapper<>();
        sysUserQW.eq(SysUser::getLoginName, loginName);
        List<SysUser> sysUserList = baseMapper.selectList(sysUserQW);
        if (sysUserList.isEmpty()){
            SysUser sysUser = new SysUser();
            sysUser.setUserId(unknownUserId);
            sysUser.setLoginName(unknownUserLoginName);
            sysUser.setName(unknownUserName);
            return sysUser;
        }else {
            return sysUserList.get(0);
        }
    }
}
