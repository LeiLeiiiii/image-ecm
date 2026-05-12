package com.sunyard.edm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.edm.dto.DocBsHomeDTO;
import com.sunyard.edm.dto.DocBsShapeMeToDTO;
import com.sunyard.edm.dto.DocBsShapeToMeDTO;
import com.sunyard.edm.po.DocBsShape;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @Author PJW 2022/12/14 10:02
 */
public interface DocBsShapeMapper extends BaseMapper<DocBsShape> {


    /**
     * 查询分享+文档信息
     *
     * @param list
     * @return
     */
    List<DocBsShapeToMeDTO> queryToMe(@Param("list") List<Long> list,
                                      @Param("docName") String docName,
                                      @Param("userIds") List<Long> userIds,
                                      @Param("shapeTimeTo") Date shapeTimeTo,
                                      @Param("shapeTimeDo") Date shapeTimeDo,
                                      @Param("shapeState") Integer shapeState,
                                      @Param("date") Date date,
                                      @Param("shapeIdList") List<Long> shapeIdList,
                                      @Param("shapeTimeSort") String shapeTimeSort,
                                      @Param("invalidTimeSort") String invalidTimeSort);


    /**
     * 查询分享+文档信息
     *
     * @param userIdList
     * @param list
     * @return
     */
    List<DocBsShapeMeToDTO> queryMeTo(@Param("userIdList") List<Long> userIdList, @Param("list") List<Long> list,
                                      @Param("docName") String docName,
                                      @Param("sharer") String sharer,
                                      @Param("shapeTimeTo") Date shapeTimeTo,
                                      @Param("shapeTimeDo") Date shapeTimeDo,
                                      @Param("shapeState") Integer shapeState,
                                      @Param("date") Date date,
                                      @Param("shapeUserId") Long shapeUserId,
                                      @Param("shapeType") Integer ShapeType,
                                      @Param("shapeTimeSort") String shapeTimeSort,
                                      @Param("invalidTimeSort") String invalidTimeSort);

    /**
     * 查询首页分享通知
     *
     * @param shapeIdList
     * @return
     */
    List<DocBsHomeDTO> searchListHome(@Param("shapeIdList") List<Long> shapeIdList);

}
