package com.sunyard.module.system.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysInstDTO;
import com.sunyard.module.system.po.SysInst;
import com.sunyard.module.system.service.OrgInstService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author PJW 2023/2/15 17:21
 */
@RestController
public class OrgApiImpl implements OrgApi {

    @Resource
    private OrgInstService orgInstService;

    @Override
    public Result<List<SysInstDTO>> searchInst(Long instId) {
        List<SysInst> poList = orgInstService.search(instId);
        List<SysInstDTO> dtoList = poList.stream().map(po -> {
            SysInstDTO dto = new SysInstDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dtoList);
    }

    @Override
    public Result<List<SysInstDTO>> getInstAll() {
        List<SysInst> poList = orgInstService.getInstAll();
        List<SysInstDTO> dtoList = poList.stream().map(po -> {
            SysInstDTO dto = new SysInstDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dtoList);
    }

}
