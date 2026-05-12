package com.sunyard.module.storage.vo;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author： zyl
 * @Description：
 * @create： 2023/5/16 10:17
 */
@Data
public class PictureRotatingInfoVO {
    /**
     * 新文件id
     */
    private Long newFileId;

    /**
     * 方向,顺时针和逆时针,0顺时针,1逆时针
     */
    private Integer direction;

    /**
     * 文件id
     */
    private Long fileId;

    /**
     * 旋转角度
     */
    private Integer rotationAngle;

    /**
     * 淡化 0-1 0为原图
     */
    private Double downplay;

    /**
     * 亮化 0为默认原图
     */
    private Double brighten;

    /**
     * 锐化 0为默认原图
     */
    private Double sharpen;

    /**
     * 是否去黑边 true 是 false 否
     */
    private Boolean blackEdge;

    /**
     * 裁剪x坐标
     */
    private Integer x1;
    /**
     * 裁剪y坐标
     */
    private Integer y1;
    /**
     * 裁剪 宽
     */
    private Integer x2;
    /**
     * 裁剪高
     */
    private Integer y2;

    /**
     * 旋转时间戳
     */
    private String  rotateTime;

    /**
     * 水平镜像时间戳
     */
    private String horizontalMirrorTime;

    /**
     * 垂直镜像时间戳
     */
    private String verticallyMirrorTime;

    /**
     * 水平镜像(0:没用进行水平镜像, 1:进行水平镜像)
     */
    private Integer horizontalMirror;

    /**
     * 垂直镜像(0:没用进行垂直镜像, 1:进行垂直镜像)
     */
    private Integer mirrorVertically;

    /**
     * 是否纠偏 true 是 false 否
     */
    private Boolean corrective;

    /**
     * 获取crop
     * @return Result
     */
    public List<Integer> getCrop(){
        List<Integer> crop = new ArrayList();
        if (ObjectUtil.isNotEmpty(this.x1)){
            crop.add(this.x1);
            crop.add(this.y1);
            crop.add(this.x2-this.x1);
            crop.add(this.y2-this.y1);
        }
        return crop;
    }


    public Long getRotateTime() {
        return Long.parseLong(rotateTime);
    }

    public Long getVerticallyMirrorTime() {
        return Long.parseLong(verticallyMirrorTime);
    }

    public Long getHorizontalMirrorTime() {
        return Long.parseLong(horizontalMirrorTime);
    }
}
