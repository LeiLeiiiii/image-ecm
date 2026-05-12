package com.sunyard.module.system.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.storage.api.StorageEquipmentApi;
import com.sunyard.module.storage.vo.StEquipmentVO;
import com.sunyard.module.system.constant.LogsPrefixConstants;

/**
 * 基础管理/存储管理/存储设备
 * @author P-JWei
 * @date 2023/9/22 17:37:16
 * @title 用户审计controller
 * @description
 */
@RestController
@RequestMapping("basics/storage/equipment")
public class BasicsStorageEquipmentController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.MENU_STORAGE + "-存储设备->";
    @Resource
    private StorageEquipmentApi equipmentApi;

    /**
     * 获取存储设备列表
     * @param stEquipment 存储obj
     * @return result
     */
    @OperationLog(BASELOG + "获取存储设备列表")
    @PostMapping("query")
    public Result query(StEquipmentVO stEquipment) {
        return equipmentApi.query(stEquipment);
    }

    /**
     * 新建存储设备
     * @param stEquipment 存储obj
     * @return result
     */
    @OperationLog(BASELOG + "新建存储设备")
    @PostMapping("add")
    public Result add(@RequestBody StEquipmentVO stEquipment) {
        stEquipment.setCreateUser(getToken().getId());
        return equipmentApi.add(stEquipment);
    }

    /**
     * 修改存储设备
     * @param stEquipment 存储obj
     * @return result
     */
    @OperationLog(BASELOG + "修改存储设备")
    @PostMapping("update")
    public Result update(@RequestBody StEquipmentVO stEquipment) {
        return equipmentApi.update(stEquipment);
    }

    /**
     * 删除存储设备
     * @param stEquipment 存储obj
     * @return result
     */
    @OperationLog(BASELOG + "删除存储设备")
    @PostMapping("del")
    public Result del(@RequestBody StEquipmentVO stEquipment) {
        return equipmentApi.del(stEquipment);
    }

    /**
     * 获取存储设备详情
     * @param stEquipment 存储obj
     * @return result
     */
    @OperationLog(BASELOG + "获取存储设备详情")
    @PostMapping("getInfo")
    public Result getInfo(@RequestBody StEquipmentVO stEquipment) {
        return equipmentApi.getInfo(stEquipment);
    }

    /**
     * 测试连接
     * @param stEquipment 存储obj
     * @return result
     */
    @OperationLog(BASELOG + "测试连接")
    @PostMapping("testConnect")
    public Result testConnect(@RequestBody StEquipmentVO stEquipment) {
        return equipmentApi.testConnect(stEquipment);
    }

    /**
     * 是否启用
     * @param stEquipment 存储obj
     * @return Result
     */
    @OperationLog(BASELOG + "是否启用")
    @PostMapping("changeStatus")
    public Result changeStatus(@RequestBody StEquipmentVO stEquipment) {
        return equipmentApi.changeStatus(stEquipment);
    }
}
