package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.dto.ecm.EcmBusiInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiStatusDTO;
import com.sunyard.ecm.dto.ecm.EcmDestroyListDTO;
import com.sunyard.ecm.dto.ecm.statistics.EcmWorkStatisticsDTO;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.vo.BusiInfoVO;
import com.sunyard.ecm.vo.EcmStatisticsVO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author ty
 * @since 2023-4-18 9:43
 * @desc 业务信息接口
 */
public interface EcmBusiInfoMapper extends BaseMapper<EcmBusiInfo> {

    /**
     * 查询业务及业务类型信息
     *
     * @param busiId 业务ID
     * @return
     */
    EcmBusiInfoDTO selectWithAppTypeById(@Param("busiId") Long busiId);

    /**
     * 根据业务唯一主键 业务类型编码、业务索引号查询业务信息
     *
     * @param appCode
     * @param busiNo
     * @return
     */
    EcmBusiInfoDTO selectWithAppType(@Param("appCode") String appCode,
                                     @Param("busiNo") String busiNo);

    /**
     * 查询业务带业务类型信息列表
     *
     * @param busiIds 业务ID列表
     * @return
     */
    List<EcmBusiInfoDTO> selectWithAppTypeList(@Param("busiIds") List<Long> busiIds);

    /**
     * 业务数据查询
     * @param busiInfoVo
     * @param appCodeList
     * @param busiIds
     * @param pageSize
     * @param size
     * @return
     */
    List<EcmBusiInfoDTO> selecAppTypetList(@Param("busiInfoVo") BusiInfoVO busiInfoVo, @Param("appCodeList") List<String> appCodeList,
                                           @Param("busiIds") List<Long> busiIds, @Param("orgCodeList") List<String> orgCodeList);
    /**
     * 业务状态统计
     * @param busiInfoVo
     * @param appCodeList
     * @param busiIds
     * @return
     */
    List<EcmBusiStatusDTO> selecBusiStatusList(@Param("busiInfoVo") BusiInfoVO busiInfoVo, @Param("appCodeList") List<String> appCodeList,
                                               @Param("busiIds") List<Long> busiIds,@Param("orgCodeList") List<String> orgCodeList);

    /**
     * 业务数据查询
     * @param busiInfoVo
     * @param appCodeList
     * @param busiIds
     * @return
     */
    List<EcmBusiInfoDTO> selecAppTypetList2(@Param("busiInfoVo") BusiInfoVO busiInfoVo, @Param("appCodeList") List<String> appCodeList,
                                           @Param("busiIds") List<Long> busiIds);

    /**
     * 业务数据查询 数量
     * @param busiInfoVo
     * @param appCodeList
     * @param busiIds
     * @return
     */
    long selecetAppTypetListCounts(@Param("busiInfoVo") BusiInfoVO busiInfoVo, @Param("appCodeList") List<String> appCodeList,
                                            @Param("busiIds") List<Long> busiIds);
    /**
     * 详细数据查询
     * @param busiIdList
     * @return
     */
    List<EcmBusiInfoDTO> selectQueryDataList(@Param("busiIdList") List<Long> busiIdList);

    /**
     * 批量修改
     * @param ecmBusiInfoList
     */
    void updateBatchById(@Param("list") List<EcmBusiInfo> ecmBusiInfoList);

    /**
     * 查询业务数量
     * @param busiInfoVo
     * @param appCodeList
     * @param busiIds
     * @return
     */
    Long selecAppTypetCount(@Param("busiInfoVo") BusiInfoVO busiInfoVo, @Param("appCodeList") List<String> appCodeList,
                                           @Param("busiIds") List<Long> busiIds, @Param("orgCodeList") List<String> orgCodeList);

    /**
     * 根据ID查询业务信息（包含已删除）
     *
     * @param busiId
     * @return
     */
    EcmBusiInfo selectByIdWithDeleted(@Param("busiId") Long busiId);

    /**
     * 根据id更新(可将已删除状态更新为未删除)
     *
     * @param ecmBusiInfo
     * @return
     */
    int updateByIdWithNoDeleted(@Param("busiInfo") EcmBusiInfo ecmBusiInfo);

    /**
     * 根据业务ID删除
     * 注意：物理删除，慎用
     *
     * @param busiId
     * @return
     */
    int deleteByBusiId(@Param("busiId") Long busiId);

    /**
     * 根据业务ID批量删除
     * 注意：物理删除，慎用
     *
     * @param busiIdList
     * @return
     */
    int deleteBatchByBusiId(@Param("coll") Collection<Long> busiIdList);

    /**
     * 获取版本是否有使用
     * @param rightVer
     * @param appCode
     * @return
     */
    Long selectCountAll(@Param("rightVer") Integer rightVer, @Param("appCode") String appCode);

    /**
     * 查询上传人上传的文件总量
     * @param ecmStatisticsDTO
     * @return
     */
    List<EcmWorkStatisticsDTO> selectUploadNum(@Param("ecmStatisticsDTO") EcmStatisticsVO ecmStatisticsDTO);

    /**
     * 查询回收站删除数据的类型
     * @param appCode
     * @return
     */
    List<String> selectListInRecycle(String appCode);

    /**
     * 历史资料销毁查询
     * @return
     */
    List<EcmDestroyListDTO> selectListForDestroy(@Param("createTimeStart") String createTimeStart,
                                                 @Param("createTimeEnd") String createTimeEnd,
                                                 @Param("state") Integer state,
                                                 @Param("docCode") String docCode,
                                                 @Param("orgCode") String orgCode,
                                                 @Param("busiNo") String busiNo);

}
