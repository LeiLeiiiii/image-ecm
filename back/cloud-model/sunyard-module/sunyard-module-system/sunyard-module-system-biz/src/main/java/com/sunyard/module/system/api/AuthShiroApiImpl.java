package com.sunyard.module.system.api;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysMenuDTO;
import com.sunyard.module.system.mapper.SysMenuMapper;
import com.sunyard.module.system.po.SysMenu;
import com.sunyard.module.system.service.OrgInstService;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author PJW 2023/2/15 17:21
 */
@RestController
public class AuthShiroApiImpl implements AuthShiroApi {

    @Resource
    private OrgInstService orgInstService;
    @Resource
    private SysMenuMapper sysMenuMapper;

    @Override
    public Result<List<SysMenuDTO>> searchShiroPaths() {
        List<SysMenu> list = orgInstService.searchShiroPaths();
        List<SysMenuDTO> listDto = list.stream().map(po -> {
            SysMenuDTO dto = new SysMenuDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(listDto);
    }

    @Override
    public Result<String> getMenuPathByPerms(String perms) {
        List<SysMenu> sysMenus = sysMenuMapper.selectList(new LambdaUpdateWrapper<SysMenu>().eq(SysMenu::getPerms, perms));
        if(CollectionUtils.isEmpty(sysMenus)){
            return Result.success("");
        }
        return Result.success(sysMenus.get(0).getPath());
    }

}
