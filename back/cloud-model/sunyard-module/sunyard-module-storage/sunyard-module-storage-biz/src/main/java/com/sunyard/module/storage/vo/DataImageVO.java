package com.sunyard.module.storage.vo;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: sunams-c4.0.0.210331
 * @description: 影像目录树展示对象
 * @author: yey.he
 * @create: 2021-02-19 15:55
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(value = {"hibernateLazyInitializer","headler"})
public class DataImageVO implements Serializable {
    private String id;

    private String pId;

    private String name;

    private String type;

    private String icon;

    private Object src;

    private Boolean open;

    /**
     *
     * @return Result
     */
    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>(6);
        map.put("id",id);
        map.put("pId",pId);
        map.put("name",name);
        if (CharSequenceUtil.isNotBlank(type)){
            map.put("type",type);
        }
        if (CharSequenceUtil.isNotBlank(icon)){
            map.put("icon",icon);
        }
        if (src != null){
            map.put("src",src);
        }
        if (open!=null){
            map.put("open",open);
        }

        return map;
    }
}
