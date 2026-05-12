package com.sunyard.ecm.es;

import com.sunyard.ecm.dto.ecm.EcmFileOcrInfoEsDTO;
import com.sunyard.framework.elasticsearch.vo.BaseAttachment;
import lombok.Data;
import org.dromara.easyes.annotation.HighLight;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.IndexName;
import org.dromara.easyes.annotation.rely.IdType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author P-JWei
 * @date 2023/12/15 13:49:51
 * @title
 * @description ES文件数据
 */
@Data
@IndexName
public class EsEcmFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @IndexId(type = IdType.CUSTOMIZE)
    private String id;

    /**
     * 业务id
     */
    private Long busiId;

    /**
     * 业务号
     */
    @HighLight(preTag = "<span style=\'color:red\'>", postTag = "</span>", fragmentSize = 2000, numberOfFragments = 5)
    private String busiNo;

    /**
     * 业务类型Code
     */
    private String appCode;

    /**
     * 业务类型名称
     */
    private String appTypeName;

    /**
     * 创建人名
     */
    @HighLight(preTag = "<span style=\'color:red\'>", postTag = "</span>", fragmentSize = 2000, numberOfFragments = 5)
    private String creatUserName;

    /**
     * 创建日期
     */
    private Date createDate;

    /**
     * 修改人名
     */
    @HighLight(preTag = "<span style=\'color:red\'>", postTag = "</span>", fragmentSize = 2000, numberOfFragments = 5)
    private String updateUserName;

    /**
     * 修改日期
     */
    private Date updateTime;

    /**
     * 资料code
     */
    private String docCode;

    /**
     * 资料类型名称
     */
    private String docTypeName;

    /**
     * 文档类型代码
     */
    private String dtdCode;

    /**
     * 文档类型名称
     */
    private String dtdTypeName;

    /**
     * 文件格式
     */
    private String format;

    /**
     * 是否ocr处理
     */
    private Long isOcrIdentify;

    /**
     * 新文件id
     */
    private Long newFileId;
    /**
     * 新文件id
     */
    private String fileLabel;

    /**
     * 文件byte[]
     */
    private byte[] fileBytes;

    /**
     * 业务来源（内部服务模块系统号：acc\ent\ecm...）
     */
    private String baseBizSource;

    /**
     * 业务流水号（由业务系统生成）
     */
    private Long baseBizSourceId;

    /**
     * 文件id
     */
    private String fileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件后缀
     */
    private String fileSuffix;

    /**
     * 标题（文本类文件可提取赋值）
     */
    @HighLight(preTag = "<span style=\'color:red\'>", postTag = "</span>",fragmentSize = 100,numberOfFragments = 5)
    private String title;

    /**
     * 摘要（文本类文件可提取赋值）
     */
    private String abstracts;

    /**
     * 可交换图像文件格式（图片视频类可提取赋值）
     */
    private String exif;

    /**
     * ocr识别信息（图片视频类可提取赋值）
     */
    private List<EcmFileOcrInfoEsDTO> ocrInfo;

    /**
     * 文件转化成base64编码后所有的内容。
     * (
     * 文本文件在走ES，会通过attachment管道
     * 预处理成指定的信息。
     * )
     */
    @HighLight(preTag = "<span style='color:red'>", postTag = "</span>",fragmentSize = 100,numberOfFragments = 5)
    private String content;

    /**
     * 文件具体信息
     */
    private BaseAttachment attachment;

    /**
     * 文件大小
     */
    private Long newFileSize;

    /**
     * 机构号
     */
    private String orgCode;
}
