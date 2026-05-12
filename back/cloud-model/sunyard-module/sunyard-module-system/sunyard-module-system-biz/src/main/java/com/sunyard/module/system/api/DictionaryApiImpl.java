package com.sunyard.module.system.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysDictionaryDTO;
import com.sunyard.module.system.po.SysDictionary;
import com.sunyard.module.system.service.SysDictionaryService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典表
 *
 * @Author raochangmei 2022-05-25
 */
@RestController
public class DictionaryApiImpl implements DictionaryApi {

    @Resource
    private SysDictionaryService sysDictionaryService;

    @Override
    public Result<SysDictionaryDTO> getDictionary(String dickey) {
        SysDictionaryDTO dto = new SysDictionaryDTO();
        BeanUtils.copyProperties(sysDictionaryService.getDictionary(dickey), dto);
        return Result.success(dto);
    }

    @Override
    public Result<Map<String, String>> getDescByCode(String dicKey) {
        return Result.success(sysDictionaryService.getDescByCode(dicKey));
    }

    @Override
    public Result<Map<String, String>> getDicValByDicKey(String dicKey) {
        return Result.success(sysDictionaryService.getDicValByDicKey(dicKey));
    }

    @Override
    public Result<List<SysDictionaryDTO>> selectValueByParentKey(String key, Integer systemCode) {
        List<SysDictionary> poList = sysDictionaryService.selectValueByParentKey(key, systemCode);
        List<SysDictionaryDTO> listDto = poList.stream().map(po -> {
            SysDictionaryDTO dto = new SysDictionaryDTO();
            BeanUtils.copyProperties(po, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(listDto);
    }

    @Override
    public Result<Map<String, List<SysDictionaryDTO>>> getDictionaryAll(String key, Integer systemCode) {
        return Result.success(sysDictionaryService.getDictionaryAll(key, systemCode));
    }

    @Override
    public Result<List<SysDictionaryDTO>> getDictionaryList(String[] keys) {
        return Result.success(sysDictionaryService.getDictionaryList(keys));
    }

    @Override
    public Result<Map<String, SysDictionaryDTO>> selectDictionByKeyMap(String key, Integer systemCode) {
        return Result.success(sysDictionaryService.selectDictionByKeyMap(key,systemCode));
    }

    @Override
    public Result<Map<String, String>> searchValExtraMapByParentKey(String dicKey) {
        Map<String, String> map = sysDictionaryService.searchValExtraMapByParentKey(dicKey);
        return Result.success(map);
    }

    @Override
    public Result<Map<String, String>> getDescByKey(String dicKey) {
        Map<String, String> descByKey = sysDictionaryService.getDescByKey(dicKey);
        return Result.success(descByKey);
    }

    @Override
    public Result<Map<String, String>> getNameByKey(String dicKey) {
        Map<String, String> nameByKey = sysDictionaryService.getNameByKey(dicKey);
        return Result.success(nameByKey);
    }

    @Override
    public Result<Map<String, String>> updateValueByKey(String dicKey, String dicVal) {
        sysDictionaryService.updateValueByKey(dicKey,dicVal);
        return Result.success();
    }

    @Override
    public Result<List<Map<String, String>>> selectSystemDictionary(Integer systemCode) {
        return Result.success(sysDictionaryService.selectSystemDictionary(systemCode));
    }


}
