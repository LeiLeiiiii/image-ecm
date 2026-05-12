package com.sunyard.module.system.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysUserAdminDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.service.OrgInstService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author PJW 2023/2/15 17:21
 */
@RestController
public class AuthOrgApiImpl implements AuthOrgApi {

    @Resource
    private OrgInstService orgInstService;

    @Override
    public Result<SysUserAdminDTO> selectSuperOrg(@RequestParam("userId") Long id) {
        return Result.success(orgInstService.selectSuperOrg(id));
    }

    @Override
    public Result<SysUserDTO> selectOrg(@RequestParam("userId") Long userId) {
        return Result.success(orgInstService.selectOrg(userId));
    }
}
