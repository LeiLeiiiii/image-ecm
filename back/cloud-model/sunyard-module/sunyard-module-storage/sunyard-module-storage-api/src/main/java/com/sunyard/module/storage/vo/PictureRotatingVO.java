package com.sunyard.module.storage.vo;

import lombok.Data;

import java.util.List;

/**
 * @author： zyl
 * @Description：
 * @create： 2023/6/12 15:09
 */
@Data
public class PictureRotatingVO {

    /**
     * 业务批次号
     */
    private String busiBatchNo;

    /**
     * 存储设备id
     */
    private Long stEquipmentId;

    /**
     * 旋转信息
     */
    private List<PictureRotatingInfoVO> rotationAngle;

    /**
     * 是否加密
     */
    private Integer isEncrypt;
}
