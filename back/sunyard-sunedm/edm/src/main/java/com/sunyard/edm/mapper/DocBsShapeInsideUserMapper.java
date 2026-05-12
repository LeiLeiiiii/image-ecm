package com.sunyard.edm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.edm.dto.DocBsShapeAcceptDTO;
import com.sunyard.edm.po.DocBsShapeInsideUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author PJW 2022/12/14 10:03
 */
public interface DocBsShapeInsideUserMapper extends BaseMapper<DocBsShapeInsideUser> {

    /**
     * 批量插入
     *
     * @param list
     * @return
     */
    int insertBatch(@Param("list") List<DocBsShapeInsideUser> list);

    /**
     * 根据shapeId查询分享对象
     *
     * @param shapeId
     * @return
     */
    List<DocBsShapeAcceptDTO> searchListByShapeId(@Param("shapeId") Long shapeId);
}
