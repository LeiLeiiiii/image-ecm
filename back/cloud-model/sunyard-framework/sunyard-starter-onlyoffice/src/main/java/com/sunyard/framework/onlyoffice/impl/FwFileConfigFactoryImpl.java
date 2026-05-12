package com.sunyard.framework.onlyoffice.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.sunyard.framework.onlyoffice.FwFileHandlerConfigFactory;
import com.sunyard.framework.onlyoffice.constant.StateConstants;
import com.sunyard.framework.onlyoffice.dto.FwAddressUrlConfig;
import com.sunyard.framework.onlyoffice.dto.FwDocumentConfig;
import com.sunyard.framework.onlyoffice.dto.FwEditorConfig;
import com.sunyard.framework.onlyoffice.dto.FwFileConfig;
import com.sunyard.framework.onlyoffice.dto.FwPlugins;
import com.sunyard.framework.onlyoffice.dto.documentfunction.FwDocumentEditPermission;
import com.sunyard.framework.onlyoffice.dto.documentfunction.FwDocumentInfo;
import com.sunyard.framework.onlyoffice.dto.documentfunction.FwDocumentPermission;
import com.sunyard.framework.onlyoffice.dto.documentfunction.FwDocumentViewPermission;
import com.sunyard.framework.onlyoffice.dto.documentfunction.FwSharingSettings;
import com.sunyard.framework.onlyoffice.dto.edit.FwFileCustomization;
import com.sunyard.framework.onlyoffice.dto.edit.FwFileUser;
import com.sunyard.framework.onlyoffice.tools.FileUtil;
import com.sunyard.framework.onlyoffice.tools.JWTUtil;
import com.sunyard.framework.onlyoffice.tools.OnlyOfficeUtil;

import cn.hutool.core.date.DateUtil;

/**
 * @author PJW
 * @BelongsProject: leaf-onlyoffice
 * @BelongsPackage: com.ideayp.leaf.onlyoffice.dto
 */
@Component
public class FwFileConfigFactoryImpl implements FwFileHandlerConfigFactory {

    @Resource
    private FwAddressUrlConfig fwAddressUrlConfig;
    @Resource
    private FwDocumentViewPermission fwDocumentViewPermission;
    @Resource
    private FwDocumentEditPermission fwDocumentEditPermission;
    @Resource
    private FwFileCustomization fwFileCustomization;
    @Resource
    private FwPlugins fwPlugins;
    /**
     * 初始化 only office 最基础的信息必要数据
     *
     * @param fileUrl  可访问的url路径
     * @param mode     打开方式
     * @param key      唯一标示符 20个字符以内
     * @param fileName 文件名称
     * @return Result 配置信息
     */
    @Override
    public FwFileConfig buildInitConfig(FwFileUser user, String fileUrl, String mode, String key, String fileName) {
        Map<String, Object> map = new HashMap<>(6);
        FwFileConfig fwFileConfigDTO = new FwFileConfig();
        // 1、文档类型
        fwFileConfigDTO.setDocumentType(OnlyOfficeUtil.getDocumentType(fileName));
        map.put("documentType", fwFileConfigDTO.getDocumentType());
        map.put("type", fwFileConfigDTO.getType());
        // 2、onlyoffice的 api位置
        fwFileConfigDTO.setDocServiceApiUrl(fwAddressUrlConfig.getDocService() + StateConstants.DOC_API_URL);
        // ========文档类型=============
        String typePart = FileUtil.getFileExtension(fileName);
        FwDocumentConfig fileDocument = FwDocumentConfig.builder()
                // 文件名
                .title(fileName)
                // 扩展名
                .fileType(typePart)
                // 可访问的url
                .url(fileUrl)
                // 唯一有标示符
                .key(key)
                .info(getDocumentInfo(user))
                //.permissions(buildPermission(mode))
                .build();
        if (mode.equals(StateConstants.VIEW)) {
            FwDocumentPermission fwDocumentPermission = new FwDocumentPermission();
            BeanUtils.copyProperties(fwDocumentViewPermission, fwDocumentPermission);
            fileDocument.setPermissions(fwDocumentPermission);
        } else {
            FwDocumentPermission fwDocumentPermission = new FwDocumentPermission();
            BeanUtils.copyProperties(fwDocumentEditPermission, fwDocumentPermission);
            fileDocument.setPermissions(fwDocumentPermission);
        }
        fwFileConfigDTO.setDocument(fileDocument);
        String jsonString2 = JSON.toJSONString(fileDocument);
        map.put("document", jsonString2);
        // ==========编辑配置===============
        String callBackUrl = fwAddressUrlConfig.getLocalhostAddress() + fwAddressUrlConfig.getCallBackUrl();
        FwEditorConfig fwEditorConfig = new FwEditorConfig();
        fwEditorConfig.setCallbackUrl(callBackUrl);
        fwEditorConfig.setMode(mode);
        fwEditorConfig.setFileCustomization(fwFileCustomization);
        fwEditorConfig.setUser(user);
        fwEditorConfig.setFwPlugins(fwPlugins);
        fwFileConfigDTO.setFwEditorConfig(fwEditorConfig);
        String jsonString = JSON.toJSONString(fwEditorConfig);
        map.put("editorConfig", jsonString);
        if (!ObjectUtils.isEmpty(fwAddressUrlConfig.getSecret())) {
            String token = JWTUtil.createToken(map, fwAddressUrlConfig.getSecret());
            fwFileConfigDTO.setToken(token);
        }
        return fwFileConfigDTO;
    }


    private FwDocumentInfo getDocumentInfo(FwFileUser user) {
        FwSharingSettings fwSharingSettings = new FwSharingSettings();
        fwSharingSettings.setPermissions(new String[]{"Full Access"});
        fwSharingSettings.setUser(user.getName());
        fwSharingSettings.setIsLink(true);
        FwDocumentInfo info = FwDocumentInfo.builder()
                .created(DateUtil.formatDateTime(new Date()))
                .fwSharingSettings(Collections.singletonList(fwSharingSettings)).build();
        return info;
    }

/*
    private DocumentPermission buildPermission(String mode) {
        if (mode.equals(StateConstants.VIEW)) {
            return LoadConfigUtil.getViewPermission();
        }
        return LoadConfigUtil.getEditPermission();
    }

    private FileCustomization getFileCustomization() {
        return LoadConfigUtil.getCustomization();
    }


    private Plugins getPlugins() {
        return LoadConfigUtil.getPlugins();
    }
*/


}
