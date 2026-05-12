package com.sunyard.module.storage.ecm.api;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.module.storage.api.StorageEquipmentApi;
import com.sunyard.module.storage.dto.EquipmentDTO;
import com.sunyard.module.storage.manager.StorageEquipmentService;
import com.sunyard.module.storage.mapper.StEquipmentMapper;
import com.sunyard.module.storage.po.StEquipment;
import com.sunyard.module.storage.vo.EquipmentVO;
import com.sunyard.module.storage.vo.StEquipmentVO;

/**
 * 存储设备
 *
 * @author： zyl
 * @Description：
 * @create： 2023/6/14 10:31
 */
@RestController
public class StorageEquipmentApiImpl implements StorageEquipmentApi {

    @Resource
    private StorageEquipmentService storageEquipmentService;
    @Resource
    private StEquipmentMapper stEquipmentMapper;

    @Override
    public Result<List<EquipmentDTO>> getEquipmentList(EquipmentVO equipmentVO) {
        List<StEquipment> stEquipments = stEquipmentMapper.selectList(new LambdaQueryWrapper<StEquipment>()
                .eq(StringUtils.isNotBlank(equipmentVO.getEquipmentName()),
                        StEquipment::getEquipmentName, equipmentVO.getEquipmentName())
                .in(!CollectionUtils.isEmpty(equipmentVO.getIds()),StEquipment::getId,equipmentVO.getIds())
        );
        List<EquipmentDTO> sysFileDtoList = PageCopyListUtils.copyListProperties(stEquipments, EquipmentDTO.class);

        return  Result.success(sysFileDtoList);
    }

    @Override
    public Result query(StEquipmentVO vo) {
        PageForm page = new PageForm(vo.getPageNum(), vo.getPageSize());
        return Result.success(storageEquipmentService.query(vo,page));
    }

    @Override
    public Result add(StEquipmentVO vo) {
        StEquipment stEquipment = new StEquipment();
        BeanUtils.copyProperties(vo,stEquipment);
        storageEquipmentService.add(stEquipment);
        return Result.success();
    }

    @Override
    public Result update(StEquipmentVO vo) {
        StEquipment stEquipment = new StEquipment();
        BeanUtils.copyProperties(vo,stEquipment);
        storageEquipmentService.update(stEquipment);
        return Result.success();
    }

    @Override
    public Result del(StEquipmentVO vo) {
        StEquipment stEquipment = new StEquipment();
        BeanUtils.copyProperties(vo,stEquipment);
        storageEquipmentService.del(stEquipment);
        return Result.success();
    }

    @Override
    public Result<EquipmentDTO> getInfo(StEquipmentVO vo) {
        StEquipment stEquipment = new StEquipment();
        BeanUtils.copyProperties(vo,stEquipment);
        StEquipment equipment = storageEquipmentService.getInfo(stEquipment);
        EquipmentDTO equipmentDTO = new EquipmentDTO();
        BeanUtils.copyProperties(equipment,equipmentDTO);
        return Result.success(equipmentDTO);
    }

    @Override
    public Result testConnect(StEquipmentVO vo) {
        StEquipment stEquipment = new StEquipment();
        BeanUtils.copyProperties(vo,stEquipment);
        return storageEquipmentService.testConnect(stEquipment);
    }

    @Override
    public Result changeStatus(StEquipmentVO vo) {
        StEquipment stEquipment = new StEquipment();
        stEquipment.setId(vo.getId());
        stEquipment.setStatus(vo.getStatus());
        storageEquipmentService.update(stEquipment);
        return Result.success();
    }

}
