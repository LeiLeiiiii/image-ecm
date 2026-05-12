package com.sunyard.mytool.entity;

import lombok.Data;

import java.util.Date;
@Data
public class VersionInfo {
    //文件信息
    private String versionTag;   // V_1, V_2, V_3
    private String versionNumber;   // 1, 2, 3
    private Date versionCreateTime;
    private Date versionModifyTime;
    private String MD5;
    private String versionCreateUser;
    private String versionModifyUser;
    //是否是当前版本
    private boolean current;
}
