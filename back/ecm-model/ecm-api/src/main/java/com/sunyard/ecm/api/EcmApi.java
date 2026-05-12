package com.sunyard.ecm.api;

import com.sunyard.ecm.dto.*;
import com.sunyard.ecm.result.ResultApi;
import com.sunyard.ecm.vo.BusiDocDuplicateVO;
import com.sunyard.ecm.vo.EcmDelVO;
import com.sunyard.ecm.vo.QueryBusiInfoVO;
import com.sunyard.ecm.vo.QueryDataVO;

import java.util.List;

/**
 * 影像采集对外接口
 *
 * @author scm
 * @Description 影像采集
 * @since 2023/8/9 10:56
 */
public interface EcmApi {

    /**
     * 查询影像信息和影像资源信息
     *
     * @param ecmRootQueryDTO 查询入参
     * @return BusiInfoAndFileVO
     */
    ResultApi<List<QueryDataVO>> queryData(EcmRootDataDTO ecmRootQueryDTO);

    /**
     * 影像调阅
     *
     * @param ecmRootDataDTO 调阅入参
     * @return BusiInfoAndFileVO
     */
    ResultApi<EcmPageBaseInfoDTO> accessEcm(EcmRootDataDTO ecmRootDataDTO);

    /**
     * 影像采集或修改
     *
     * @param ecmRootDataDTO 采集入参
     * @return BusiInfoAndFileVO
     */
    ResultApi<EcmPageBaseInfoDTO> scanOrUpdateEcm(EcmRootDataDTO ecmRootDataDTO);

    /**
     * 影像采集或修改
     *
     * @param ecmRootDataDTO 采集入参
     * @return BusiInfoAndFileVO
     */
    ResultApi<EcmPageBaseInfoDTO> scanOrUpdateEcmIe(EcmRootDataDTO ecmRootDataDTO);
    /**
     * 影像移动端调阅
     *
     * @param ecmRootDataDTO 采集入参
     * @return BusiInfoAndFileVO
     */
    ResultApi<EcmPageBaseInfoDTO> scanOrUpdateEcmMobile(EcmRootDataDTO ecmRootDataDTO);

    /**
     * 影像复制接口
     *
     * @param fileInfoRedisEntityVo 文件复制入参
     */
    ResultApi busiDocDuplicate(BusiDocDuplicateVO fileInfoRedisEntityVo);

    /**
     * 影像删除
     *
     * @param vo 删除入参
     */
    ResultApi deleteFile(EcmDelVO vo);

    /**
     * 文件上传
     *
     * @return Boolean
     */
    ResultApi splitFile(UploadAllDTO splitDTO);

    /**
     * 文件下载
     *
     * @return Boolean
     */
    ResultApi downloadFile(EcmDownloadFileDTO ecmDownloadFileDTO);


    /**
     * 业务类型属性列表
     *
     * @return Boolean
     */
    ResultApi setBusiAttr(EditBusiAttrDTO editBusiAttrDTO);


    /**
     * ocr全文识别文本内容回传接口
     */
    ResultApi textCallback(FileOcrCallBackDTO fileOcrCallBackDTO);


    /**
     * 业务类型属性列表
     *
     * @return Boolean
     */
    ResultApi busiDeblock(EditBusiAttrDTO editBusiAttrDTO);

    /**
     * 获取业务类型及文档列表
     *
     */
    ResultApi<List<QueryBusiInfoVO>> queryBusi(QueryBusiDTO queryBusiDTO);

    /**
     * 影像业务校验
     * @param ecmBusiInfoDataDTO
     * @return
     */
    ResultApi ecmBusiInfoCheck(EcmBusiInfoDataDTO ecmBusiInfoDataDTO);

    /**
     * 影像资料统计
     * @param ecmBusiInfoDataDTO
     * @return
     */
    ResultApi statisticsDocFileNUm(EcmBusiInfoDataDTO ecmBusiInfoDataDTO);

    /**
     * 影像归档接口
     *
     * @param dto 文件复制入参
     */
    ResultApi busiArchive(QueryBusiDTO dto);
}
