package com.sunyard.module.system.api;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.enums.LoginTypeEnum;
import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysMenuDTO;
import com.sunyard.module.system.api.dto.SysUserAdminDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.po.SysInst;
import com.sunyard.module.system.po.SysUser;
import com.sunyard.module.system.po.SysUserAdmin;
import com.sunyard.module.system.service.OrgInstService;
import com.sunyard.module.system.service.OrgUserService;

/**
 * user模块对内提供的controller
 *
 * @Author PJW 2023/2/15 15:35
 */
@RestController
public class AuthUserApiImpl implements AuthUserApi {

    @Resource
    private OrgUserService service;
    @Resource
    private OrgInstService orgInstService;

    /**
     * 普通登录使用需要返回密码，不对DTO进行数据过滤
     * */
    @Override
    public Result<List<SysUserDTO>> search(@RequestParam("loginName") String loginName) {
        List<SysUser> poList = service.search(loginName);
        //因list的size只会为1
        List<SysUserDTO> listDto = poList.stream().map(po -> {
            SysUserDTO dto = new SysUserDTO();
            SysInst sysInst = orgInstService.select(po.getInstId());
            BeanUtils.copyProperties(po, dto);
            dto.setInstName(sysInst.getName());
            return dto;
        }).collect(Collectors.toList());
        return Result.success(listDto);
    }

    /**
    * 登录使用需要返回密码，不对DTO进行数据过滤
    * */
    @Override
    public Result<List<SysUserAdminDTO>> adminInfoSearch(@RequestParam("loginName") String loginName) {
        List<SysUserAdmin> poList = service.adminInfoSearch(loginName);
        //因list的size只会为1
        List<SysUserAdminDTO> listDto = poList.stream().map(po -> {
            SysUserAdminDTO dto = new SysUserAdminDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(listDto);
    }

    /**
     * 登录使用需要返回密码，不对DTO进行数据过滤
     * */
    @Override
    public Result<List<SysUserDTO>> ldapLogin(@RequestParam("loginName") String loginName,
                                              @RequestParam("password") String pw) {
        return Result.success(service.ldapLogin(loginName, pw));
    }

    @Override
    public Result<Boolean> changeStatusByUserName(String userName, Integer status, LoginTypeEnum loginTypeEnum) {
        service.changeStatusByUserName(userName, status, loginTypeEnum);
        return Result.success(true);
    }

    @Override
    public Result<List<SysMenuDTO>> getRoleByUserId(Long userId) {
        return Result.success(service.getRoleByUserId(userId));
    }

    @Override
    public Result<Boolean> unlockByUserName(String userName, LoginTypeEnum loginTypeEnum) {
        service.unlockByUserName(userName, loginTypeEnum);
        return Result.success(true);
    }

    @Override
    public Result<String> getUserMailByUserName(String username) {
        return service.getUserMailByUserName(username);
    }

    @Override
    public Result<Boolean> sendPwdMailCode(String username) {
        return service.sendMailCode(username);
    }

    @Override
    public Result checkMailCodeByUserName(String username, String code) {
        return service.checkMailCode(username, code);
    }

    @Override
    public Result updatePwdByUserName(String username, String newPwd, String code) {
        return service.updatePwd(username, newPwd, code);
    }

    @Override
    public Result updatePwdByUserId(String userId, String newPwd) {
        return service.updatePwdByUserId(userId,newPwd);
    }

}
