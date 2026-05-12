package com.sunyard.framework.onlyoffice;


import com.sunyard.framework.onlyoffice.dto.FwFileConfig;
import com.sunyard.framework.onlyoffice.dto.edit.FwFileUser;


/**
 * @author 朱山成
 */
public interface FwFileHandlerConfigFactory {


    /**
     * 文件初始化onlyOffice必须信息
     * @param user 用户信息  id+姓名
     * @param fileUrl  文件地址
     * @param mode  mode
     * @param key      文件唯一标识
     * @param fileName 文件名称
     * @return Result onlyOffice必须信息
     */
    FwFileConfig buildInitConfig(FwFileUser user, String fileUrl, String mode, String key, String fileName);
}
