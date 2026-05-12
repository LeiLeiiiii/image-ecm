package com.sunyard.module.system.api;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysDictionaryDTO;
import com.sunyard.module.system.constant.ApiConstants;

/**
 * @Author raochangmei 2022-05-25
 */
@FeignClient(name = ApiConstants.NAME)
public interface DictionaryApi {
    String PREFIX = ApiConstants.PREFIX + "/dictionary/";

    /**
     * 根据字典表名获取SysDictionary对象
     *
     * @param dickey key
     * @return Result
     */
    @PostMapping(PREFIX + "getDictionary")
    Result<SysDictionaryDTO> getDictionary(@RequestParam(value = "dicKey", required = false) String dickey);

    /**
     * 获取key:code value:Desc map集合
     *
     * @param dicKey key
     * @return Result
     */
    @PostMapping(PREFIX + "getDescByCode")
    Result<Map<String, String>> getDescByCode(@RequestParam(value = "dicKey", required = false) String dicKey);

    /**
     * 根据一级key 获取到所有二级key 的 key code map集合
     *
     * @param dicKey key
     * @return Result
     */
    @PostMapping(PREFIX + "getDicValByDicKey")
    Result<Map<String, String>> getDicValByDicKey(@RequestParam(value = "dicKey", required = false) String dicKey);

    /**
     * 获取字段值通过父级key
     * @param key 父级key
     * @param systemCode 系统code
     * @return Result 字典集
     */
    @PostMapping(PREFIX + "selectValueByParentKey")
    Result<List<SysDictionaryDTO>> selectValueByParentKey(@RequestParam(value = "key", required = false) String key,
                                                          @RequestParam(value = "systemCode", required = false) Integer systemCode);

    /**
     * 获取所有字典
     * @param key 字典key
     * @param systemCode 系统code
     * @return Result 字典值
     */
    @PostMapping(PREFIX + "getDictionaryAll")
    Result<Map<String, List<SysDictionaryDTO>>> getDictionaryAll(@RequestParam(value = "key", required = false) String key,
                                                                 @RequestParam(value = "systemCode", required = false) Integer systemCode);

    /**
     * 获取字典描述
     * @param dicKey 字典key
     * @return Result 字典集
     */
    @PostMapping(PREFIX + "getDescByKey")
    Result<Map<String, String>> getDescByKey(@RequestParam(value = "dicKey", required = false) String dicKey);

    /**
     * 获取字典名称
     * @param dicKey 字典key
     * @return Result 字典集
     */
    @PostMapping(PREFIX + "getNameByKey")
    Result<Map<String, String>> getNameByKey(@RequestParam(value = "dicKey", required = false) String dicKey);

    /**
     * 获取字典列表
     * @param keys 字典key
     * @return Result 字典集
     */
    @PostMapping(PREFIX + "getDictionaryList")
    Result<List<SysDictionaryDTO>> getDictionaryList(@RequestParam(value = "keys", required = false) String[] keys);

    /**
     * 根据key，获取字典对象
     *
     * @param key key
     * @param systemCode 系统code
     * @return Result 字典
     */
    @PostMapping(PREFIX + "selectDictionByKeyMap")
    Result<Map<String, SysDictionaryDTO>> selectDictionByKeyMap(@RequestParam(value = "key", required = false) String key, @RequestParam(value = "systemCode", required = false) Integer systemCode);

    /**
     * 获取字典拓展值
     * @param dicKey 字典key
     * @return Result 字典
     */
    @PostMapping(PREFIX + "searchValExtraMapByParentKey")
    Result<Map<String, String>> searchValExtraMapByParentKey(@RequestParam(value = "dicKey", required = false) String dicKey);

    /**
     * 根据key更新字典值
     */
    @PostMapping(PREFIX + "updateValueByKey")
    Result<Map<String, String>> updateValueByKey(@RequestParam(value = "dicKey") String dicKey, @RequestParam(value = "dicVal", required = false) String dicVal);

    /**
     * 根据系统code获取
     */
    @PostMapping(PREFIX + "selectSystemDictionary")
    Result<List<Map<String, String>>> selectSystemDictionary(@RequestParam(value = "systemCode") Integer systemCode);
}
