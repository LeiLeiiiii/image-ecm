package com.sunyard.mytool.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class KsXmlInfo {
    //文件夹信息
    private Date fileCreateTime;
    private Date fileModifyTime;
    private String fileCreateUser;
    private String fileModifyUser;

    //文件版本信息
    private List<VersionInfo> versionInfos;
}
