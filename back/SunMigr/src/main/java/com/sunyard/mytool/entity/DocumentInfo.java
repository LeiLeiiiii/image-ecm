package com.sunyard.mytool.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DocumentInfo {
    //文件夹信息
    private Date fileCreateTime;
    private Date fileModifyTime;
    private String fileCreateUser;
    private String fileModifyUser;

    //文件信息
    /*private Date versionCreateTime;
    private Date versionModifyTime;
    private String MD5;
    private String versionCreateUser;
    private String versionModifyUser;*/
}
