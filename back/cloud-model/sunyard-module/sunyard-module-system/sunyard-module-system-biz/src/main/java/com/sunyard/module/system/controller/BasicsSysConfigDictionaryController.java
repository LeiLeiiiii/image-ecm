package com.sunyard.module.system.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.po.SysDictionary;
import com.sunyard.module.system.service.SysDictionaryService;

/**
 * 通用管理/系统初始化/字典表配置
 *
 * @author wubingyang
 * @date 2021/7/21 9:00
 */
@RestController
@RequestMapping("basics/sysConfig/dictionary")
public class BasicsSysConfigDictionaryController {
    private static final String BASELOG = LogsPrefixConstants.MENU_SYSTEM + "-字典配置->";
    @Resource
    private SysDictionaryService service;

    /**
     * 新增字典
     *
     * @param sysDictionary 字典obj
     * @return Result
     */
    @OperationLog(BASELOG + "新增字典")
    @PostMapping("addDictionary")
    public Result addDictionary(@RequestBody SysDictionary sysDictionary) {
        service.addDictionary(sysDictionary);
        return Result.success(true);
    }

    /**
     * 修改字典
     *
     * @param sysDictionary 字典obj
     * @return Result
     */
    @OperationLog(BASELOG + "修改字典")
    @PostMapping("updateDictionary")
    public Result updateDictionary(@RequestBody SysDictionary sysDictionary) {
        service.updateDictionary(sysDictionary);
        return Result.success(true);
    }

    /**
     * 字典详情
     *
     * @param sysDictionary 字典obj
     * @return Result
     */
    @OperationLog(BASELOG + "字典详情")
    @PostMapping("getInfoDictionary")
    public Result getInfoDictionary(SysDictionary sysDictionary) {
        return Result.success(service.getInfoDictionary(sysDictionary));
    }

    /**
     * 获取字典树
     * @return com.sunyard.framework.common.result.Result
     * @author haod.liu
     * @date 2024/7/5 上午9:51
     */
    @OperationLog(BASELOG + "获取字典树")
    @PostMapping("getDictionaryTree")
    public Result getDictionaryTree(SysDictionary sysDictionary) {
        return service.getDictionaryTree(sysDictionary);
    }

    /**
     * 根据字典id查询字典属性及下属value
     * @return com.sunyard.framework.common.result.Result
     * @author haod.liu
     * @date 2024/7/5 上午9:51
     */
    @OperationLog(BASELOG + "查询字典属性")
    @PostMapping("getDicInfo")
    public Result getDicInfo(Long id) {
        return service.getDicInfo(id);
    }

    /**
     * 新增字典
     * @return com.sunyard.framework.common.result.Result
     * @author haod.liu
     * @date 2024/7/5 上午9:51
     */
    @OperationLog(BASELOG + "新增字典")
    @PostMapping("addDicKey")
    public Result addDicKey(@RequestBody SysDictionary sysDictionary) {
        return service.addDicKey(sysDictionary);
    }

    /**
     * 新增字典数据
     * @return com.sunyard.framework.common.result.Result
     * @author haod.liu
     * @date 2024/7/5 上午9:51
     */
    @OperationLog(BASELOG + "新增字典数据")
    @PostMapping("addDicValue")
    public Result addDicValue(@RequestBody SysDictionary sysDictionary) {
        return service.addDicValue(sysDictionary);
    }

    /**
     * 修改字典
     * @return com.sunyard.framework.common.result.Result
     * @author haod.liu
     * @date 2024/7/5 上午9:51
     */
    @OperationLog(BASELOG + "修改字典")
    @PostMapping("updateDicKey")
    public Result updateDicKey(@RequestBody SysDictionary sysDictionary) {
        return service.updateDicKey(sysDictionary);
    }

    /**
     * 修改字典数据
     * @return com.sunyard.framework.common.result.Result
     * @author haod.liu
     * @date 2024/7/5 上午9:51
     */
    @OperationLog(BASELOG + "修改字典数据")
    @PostMapping("updateDicValue")
    public Result updateDicValue(@RequestBody SysDictionary sysDictionary) {
        return service.updateDicValue(sysDictionary);
    }

    /**
     * 删除字典
     * @return com.sunyard.framework.common.result.Result
     * @author haod.liu
     * @date 2024/7/5 上午9:51
     */
    @OperationLog(BASELOG + "删除字典")
    @PostMapping("deleteDicKey")
    public Result deleteDicKey(Long id) {
        return service.deleteDicKey(id);
    }

    /**
     * 删除字典数据
     * @return com.sunyard.framework.common.result.Result
     * @author haod.liu
     * @date 2024/7/5 上午9:51
     */
    @OperationLog(BASELOG + "删除字典数据")
    @PostMapping("deleteDicValue")
    public Result deleteDicValue(Long id) {
        return service.deleteDicValue(id);
    }

    /**
     * 查列表-ui
     *
     * @param sysDictionary 字典obj
     * @param pageForm      分页参数
     * @return Result
     */
    @OperationLog(BASELOG + "查列表-ui")
    @PostMapping("getListDictionary")
    public Result getListDictionary(SysDictionary sysDictionary, PageForm pageForm) {
        return Result.success(service.getListDictionary(sysDictionary, pageForm));
    }

    /**
     * 查列表只查父级
     *
     * @param sysDictionary 字典obj
     * @return Result
     */
    @OperationLog(BASELOG + "查列表只查父级")
    @PostMapping("getListParent")
    public Result getListParent(SysDictionary sysDictionary) {
        return Result.success(service.getListParent(sysDictionary));
    }

    /**
     * 查列表只查子级
     *
     * @param key        key
     * @param systemCode 系统分类
     * @return Result
     */
    @OperationLog(BASELOG + "查列表只查子级")
    @PostMapping("selectValueByKey")
    public Result selectValueByKey(String key, Integer systemCode) {
        return Result.success(service.getDictionaryAll(key, systemCode));
    }
}
