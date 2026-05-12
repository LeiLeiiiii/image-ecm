package com.sunyard.sunafm.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author pjw
 * @since 2024-09-30
 */
@Getter
@Setter
@TableName("afm_server")
public class AfmServer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 向量数据库服务名称
     */
    private String name;

    /**
     * 连接地址
     */
    private String host;

    /**
     * 端口
     */
    private String port;

    /**
     * 连接用户名
     */
    private String user;

    /**
     * 连接密码
     */
    private String password;

    /**
     * 向量数据库的名称
     */
    private String dbName;

    /**
     * 状态 0:可读，1:可读写
     */
    private Integer type;

    /**
     * 资料类型
     */
    private String docCode;

    /**
     * 0:不可使用；1:可使用
     */
    private Integer status;

    /**
     * 年份，如果是多年则，存放第一年的日期
     */
    private Integer year;

    /**
     * 集合排列的数量
     */
    private Integer num;

    /**
     * 集合内总条数
     */
    private Long collectionTotal;

    /**
     * 服务器对应集合名称
     */
    private String collectionName;

    /**
     * 服务器查重类型  0：图像查重服务器 1：文本查重服务器
     */
    private Integer serverType;
}
