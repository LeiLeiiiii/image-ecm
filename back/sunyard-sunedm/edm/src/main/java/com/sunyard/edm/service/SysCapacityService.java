package com.sunyard.edm.service;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.lock.annotation.Lock4j;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.dto.SysParamDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author raochangmei
 * @date 11.15
 * @Desc 高级配置-容量上限实现类
 */
@Service
public class SysCapacityService {
    @Resource
    private ParamApi paramApi;
    /**
     * 查询
     */
    public ArrayList<HashMap> searchCapacity() {
        Result<SysParamDTO> sysParamDTOResult = paramApi.searchValueByKey(DocConstants.DOC_MAXIMUM_SIZE);
        SysParamDTO data = sysParamDTOResult.getData();
        List<SysParamDTO> sysMax = new ArrayList<>();
        if (data != null && DocConstants.DOC_STATUS_SET.equals(data.getStatus())){
            sysMax.add(data);
        }
        Result<SysParamDTO> sysParamDTOResult2 = paramApi.searchValueByKey( DocConstants.DOC_RECYCLE_DAY_KEY);
        SysParamDTO data2 = sysParamDTOResult2.getData();
        List<SysParamDTO> sysRecycle = new ArrayList<>();
        if (data2 != null && DocConstants.DOC_STATUS_SET.equals(data2.getStatus())){
            sysRecycle.add(data2);
        }
        Result<SysParamDTO> sysParamDTOResult3 = paramApi.searchValueByKey(  DocConstants.DOC_FOLDER_TREE_TYPE);
        SysParamDTO data3 = sysParamDTOResult3.getData();
        List<SysParamDTO> sysFolder = new ArrayList<>();
        if (data3 != null && DocConstants.DOC_STATUS_SET.equals(data3.getStatus())){
            sysFolder.add(data3);
        }
        ArrayList<HashMap> initList = getInitList();
        for (HashMap rule : initList) {
            if (rule.get("code").equals(DocConstants.DOC_MAXIMUM_SIZE)
                    && !CollectionUtils.isEmpty(sysMax)) {
                SysParamDTO sysFirst = sysMax.get(0);
                rule.put("value", sysFirst.getValue() + "G");
                rule.put("valueRel", sysFirst.getValue());
                rule.put("status", sysFirst.getStatus());
                rule.put("updatetime", new Date());
            } else if (rule.get("code").equals(DocConstants.DOC_RECYCLE_DAY_KEY)
                    && !CollectionUtils.isEmpty(sysRecycle)) {
                SysParamDTO sysSecond = sysRecycle.get(0);
                rule.put("value", sysSecond.getValue() + "天");
                rule.put("valueRel", sysSecond.getValue());
                rule.put("status", sysSecond.getStatus());
                rule.put("updatetime", new Date());
            } else if (rule.get("code").equals(DocConstants.DOC_FOLDER_TREE_TYPE)
                    && !CollectionUtils.isEmpty(sysFolder)) {
                SysParamDTO sysThird = sysFolder.get(0);
                String view = "仅展示有权限的文件夹";
                if (DocConstants.DOC_FOLDER_TREE_TYPE_ALL.equals(sysThird.getValue())) {
                    view = "展示所有文件夹";
                }
                rule.put("value", view);
                rule.put("valueRel", sysThird.getValue());
                rule.put("status", sysThird.getStatus());
                rule.put("updatetime", new Date());
            }
        }
        return initList;
    }


    /**
     * 修改配置
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#name")
    public void updateCapacity(String code, String value) {
        AssertUtils.isNull(code, "参数错误");
        AssertUtils.isNull(value, "参数错误");
        paramApi.updateValueByKey(code,value);
    }

    /**
     * 获取删除天数
     */
    public String getDelDay() {
        String day = "60";
        Result<SysParamDTO> sysParamDTOResult = paramApi.searchValueByKey("DOC_RECYCLE_DAY_KEY");
        SysParamDTO data = sysParamDTOResult.getData();
        List<SysParamDTO> sysRecycle = new ArrayList<>();
        if (data != null && DocConstants.DOC_STATUS_SET.equals(data.getStatus())){
            sysRecycle.add(data);
        }
        if (!CollectionUtils.isEmpty(sysRecycle)) {
            day = sysRecycle.get(0).getValue();
        }
        return day;
    }

    /**
     * 获取列表
     *
     */
    private ArrayList<HashMap> getInitList() {
        ArrayList<HashMap> objects = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>(DocConstants.SIXTEEN);
        map.put("index", 1);
        map.put("code", DocConstants.DOC_MAXIMUM_SIZE);
        map.put("name", "容量上限");
        map.put("value", null);
        map.put("valueRel", null);
        map.put("status", null);
        map.put("updatetime", null);
        HashMap<String, Object> map1 = new HashMap<>(DocConstants.SIXTEEN);
        map1.put("index", 2);
        map1.put("code", DocConstants.DOC_RECYCLE_DAY_KEY);
        map1.put("name", "回收站保留期限");
        map1.put("value", null);
        map.put("valueRel", null);
        map1.put("status", null);
        map1.put("updatetime", null);
        HashMap<String, Object> map2 = new HashMap<>(DocConstants.SIXTEEN);
        map2.put("index", 3);
        map2.put("code", DocConstants.DOC_FOLDER_TREE_TYPE);
        map2.put("name", "企业文件夹视图");
        map2.put("value", null);
        map.put("valueRel", null);
        map2.put("status", null);
        map2.put("updatetime", null);
        objects.add(map);
        objects.add(map1);
        objects.add(map2);
        return objects;
    }
}
