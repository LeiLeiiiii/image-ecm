package com.sunyard.ecm.service;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.redis.constant.TimeOutConstants;
import com.sunyard.module.system.api.DictionaryApi;
import com.sunyard.module.system.api.dto.SysDictionaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liwei
 * @since 2025/5/19
 * @desc 文件格式配置服务接口
 */
@Slf4j
@Service
public class SysFileTypeService {

    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private DictionaryApi dictionaryApi;
    /**
     * 获取文件格式
     */
    public Result<Map<String, List<SysDictionaryDTO>>> getFileTypeByDic(String limitFormat) {
        Result<Map<String, List<SysDictionaryDTO>>> dictionaryAll = dictionaryApi.getDictionaryAll(IcmsConstants.FILE_TYPE_DIC, null);
        if(org.apache.commons.lang3.StringUtils.isNotBlank(limitFormat)){
            Map<String, List<SysDictionaryDTO>> data = dictionaryAll.getData();
            List<SysDictionaryDTO> sysDictionaryDTOS = data.get(IcmsConstants.FILE_TYPE_DIC);
            List<SysDictionaryDTO> ret = new ArrayList<>();
            for(SysDictionaryDTO sysDictionaryDTO:sysDictionaryDTOS){
                JSONObject jsonObject = JSONObject.parseObject(sysDictionaryDTO.getValue());
                if(jsonObject==null){
                    continue;
                }
                String allFormat = jsonObject.getString("limit_format");
                if(!StringUtils.isEmpty(allFormat)&&allFormat.contains(limitFormat)){
                    ret.add(sysDictionaryDTO);
                }
            }
            data.put(IcmsConstants.FILE_TYPE_DIC, ret);
            dictionaryAll.setData(data);
        }
        return dictionaryAll;
    }

    /**
     * 更新文件类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateFileType(SysDictionaryDTO dto) {
        AssertUtils.isNull(dto.getDicKey(),"参数错误");
        AssertUtils.isNull(dto.getValue(),"参数错误");
        dictionaryApi.updateValueByKey(dto.getDicKey(),dto.getValue());
        //更新资料节点
        JSONObject jsonObject1 = JSONObject.parseObject(dto.getValue());
        String limitFormat1 = jsonObject1.getString("limit_format");

        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectList(null);

        List<EcmDocDef> ecmDocDefs1 = new ArrayList<>();
        for(EcmDocDef docDef:ecmDocDefs){
            if(dto.getDicKey().equals(IcmsConstants.ECMS_COMMON_FILETYPE_IMG)){
                String imgLimit = docDef.getImgLimit();
                String s = handleUpdate(docDef, imgLimit, limitFormat1);
                docDef.setImgLimit(s);
                ecmDocDefs1.add(docDef);
            }else if(dto.getDicKey().equals(IcmsConstants.ECMS_COMMON_FILETYPE_OFFICE)){
                String imgLimit = docDef.getOfficeLimit();
                String s = handleUpdate(docDef, imgLimit, limitFormat1);
                docDef.setOfficeLimit(s);
                ecmDocDefs1.add(docDef);
            }else if(dto.getDicKey().equals(IcmsConstants.ECMS_COMMON_FILETYPE_SP)){
                String imgLimit = docDef.getVideoLimit();
                String s = handleUpdate(docDef, imgLimit, limitFormat1);
                docDef.setVideoLimit(s);
                ecmDocDefs1.add(docDef);
            }else if(dto.getDicKey().equals(IcmsConstants.ECMS_COMMON_FILETYPE_YP)){
                String imgLimit = docDef.getAudioLimit();
                String s = handleUpdate(docDef, imgLimit, limitFormat1);
                docDef.setAudioLimit(s);
                ecmDocDefs1.add(docDef);
            }else if(dto.getDicKey().equals(IcmsConstants.ECMS_COMMON_FILETYPE_OTHER)){
                String imgLimit = docDef.getOtherLimit();
                String s = handleUpdate(docDef, imgLimit, limitFormat1);
                docDef.setOtherLimit(s);
                ecmDocDefs1.add(docDef);
            }
        }

        for(EcmDocDef docDef:ecmDocDefs1){
            ecmDocDefMapper.updateById(docDef);
            BusiCacheService busiCacheService = SpringUtil.getBean(BusiCacheService.class);
            busiCacheService.setDocInfo(docDef, TimeOutConstants.SEVEN_DAY);
        }
    }

    /**
     * 更新文件类型
     */
    private static String handleUpdate(EcmDocDef docDef, String imgLimit, String limitFormat1) {
        if(StringUtils.isEmpty(imgLimit)){
            return imgLimit;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(imgLimit);
            if(jsonObject!=null){
                String limitFormat = jsonObject.getString("limit_format");
                if(limitFormat==null||!limitFormat.equals(limitFormat1)){
                    jsonObject.put("limit_format", limitFormat1);
                    return jsonObject.toJSONString();
                }
            }
        }catch (Exception e){
            log.error(docDef.getDocCode()+"资料节点数据有误");
        }
        return null;
    }
}
