package com.sunyard.ecm.constant;

/**
 * @author ty
 * @since 2023-4-17 15:22
 * @Desc 资料权限常量配置
 */
public class DocRightConstants {
    // 通用常量
    public final static Integer ZERO = 0;
    public final static Integer ONE = 1;
    public final static Integer ONE_THOUSAND = 1000;

    //空白版本
    public final static Integer BLANK_VER = 0;
    //复用已有版本
    public final static Integer EXISTING_VER = 1;

    //维度类型：0角色维度，1业务多维度
    public final static Integer ROLE_DIM = 0;
    public final static Integer LOT_DIM = 1;

    /**
     * 权限字段
     */
    //修改
    public final static String UPDATE_RIGHT = "UPDATE_RIGHT";
    //他人修改
    public final static String OTHER_UPDATE = "OTHER_UPDATE";
}
