package com.sunyard.edm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.edm.dto.DocSysTagDTO;
import com.sunyard.edm.po.DocSysTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author huronghao
 * @Type
 * @Desc
 * @date 2022-12-14 9:13
 */
public interface DocSysTagMapper extends BaseMapper<DocSysTag> {

    /**
     * 获取标签树
     *
     * @param parentId 父级id
     * @return 获取标签树
     */
    List<DocSysTagDTO> searchTagTree(@Param("parentId") Long parentId);
}
