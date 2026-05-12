package com.sunyard.module.storage.api;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.storage.constant.ApiConstants;
import com.sunyard.module.storage.dto.EquipmentDTO;
import com.sunyard.module.storage.vo.EquipmentVO;
import com.sunyard.module.storage.vo.StEquipmentVO;

/**
 * 存储设备
 * 
 * @author PJW
 */
@FeignClient(value = ApiConstants.NAME)
public interface StorageEquipmentApi {
    String PREFIX = ApiConstants.PREFIX + "/storageEquipment/";

    /**
     * 获取设备列表
     *
     * @param equipmentVO 存储obj
     * @return result
     */
    @PostMapping(PREFIX + "getEquipmentList")
    Result<List<EquipmentDTO>> getEquipmentList(@RequestBody EquipmentVO equipmentVO);

    /**
     * 获取存储设备列表
     *
     * @param stEquipment 存储obj
     * @return result
     */
    @PostMapping(PREFIX + "query")
    Result query(@RequestBody StEquipmentVO stEquipment);

    /**
     * 新建存储设备
     *
     * @param stEquipment 存储obj
     * @return result
     */
    @PostMapping(PREFIX + "add")
    Result add(@RequestBody StEquipmentVO stEquipment);

    /**
     * 修改存储设备
     *
     * @param stEquipment 存储obj
     * @return result
     */
    @PostMapping(PREFIX + "update")
    Result update(@RequestBody StEquipmentVO stEquipment);

    /**
     * 删除存储设备
     *
     * @param stEquipment 存储obj
     * @return result
     */
    @PostMapping(PREFIX + "del")
    Result del(@RequestBody StEquipmentVO stEquipment);

    /**
     * 获取存储设备详情
     *
     * @param stEquipment 存储obj
     * @return result
     */
    @PostMapping(PREFIX + "getInfo")
    Result<EquipmentDTO> getInfo(@RequestBody StEquipmentVO stEquipment);

    /**
     * 测试连接
     *
     * @param stEquipment 存储obj
     * @return result
     */
    @PostMapping(PREFIX + "testConnect")
    Result testConnect(@RequestBody StEquipmentVO stEquipment);

    /**
     * 是否启用
     * 
     * @param stEquipment 存储obj
     * @return Result
     */
    @PostMapping(PREFIX + "changeStatus")
    Result changeStatus(@RequestBody StEquipmentVO stEquipment);
}
