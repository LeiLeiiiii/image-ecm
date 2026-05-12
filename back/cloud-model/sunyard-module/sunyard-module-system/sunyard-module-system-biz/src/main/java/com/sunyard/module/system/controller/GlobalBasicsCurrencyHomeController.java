package com.sunyard.module.system.controller;

import javax.annotation.Resource;

import com.sunyard.framework.common.constant.LoginEncryptionConstant;
import com.sunyard.framework.common.util.encryption.Sm2Util;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.encryption.RsaUtils;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.service.ProfileService;
import com.sunyard.module.system.service.SysConfigWaterMarkService;

/**
 * @author P-JWei
 * @date 2024/1/19 10:10:35
 * @title
 * @description
 */
@RestController
@RequestMapping("global")
public class GlobalBasicsCurrencyHomeController extends BaseController{
    private static final String BASELOG = LogsPrefixConstants.GLOBAL_HOME + "->";
    @Resource
    private SysConfigWaterMarkService sysConfigWaterMarkService;
    @Resource
    private ProfileService profileService;


    /**
     * 查询配置信息
     * @return Result
     */
    @OperationLog(BASELOG + "查询配置信息")
    @PostMapping("selectWaterMark")
    public Result selectWaterMark(){
        return Result.success(sysConfigWaterMarkService.selectWaterMark());
    }

    /**
     * 用户修改密码
     *
     * @param oldPwd   老密码
     * @param newPwd   新密码
     * @param mailCode 邮箱验证码
     * @return result
     * @throws Exception 异常
     */
    @OperationLog(BASELOG + "用户修改密码")
    @PostMapping("updatePwd")
    public Result updatePwd(String oldPwd, String newPwd, String mailCode) throws Exception {
        Integer loginEncryption = 1;
        switch (loginEncryption) {
            case LoginEncryptionConstant.RSA_LOGIN_TYPE:
                oldPwd = RsaUtils.decrypt(oldPwd);
                newPwd = RsaUtils.decrypt(newPwd);
                break;
            default:
                oldPwd = Sm2Util.decrypt(LoginEncryptionConstant.ENCRYPTED_PREFIX + oldPwd);
                newPwd = Sm2Util.decrypt(LoginEncryptionConstant.ENCRYPTED_PREFIX + newPwd);
        }
        return profileService.updateUserPwd(getToken().getId(), oldPwd, newPwd, mailCode);
    }

    /**
     * 获取邮箱验证码(供修改密码时使用)
     *
     * @return result
     */
    @OperationLog(BASELOG + "获取邮箱验证码(供修改密码时使用)")
    @PostMapping("getEmailCode")
    public Result getEmailCode() {
        return profileService.getEmailCode(getToken().getUsername());
    }
}
