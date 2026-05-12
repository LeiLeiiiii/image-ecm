package com.sunyard.edm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.edm.dto.DocBsDocumentUserDTO;
import com.sunyard.edm.po.DocSysHouseUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author wt 2022/12/15 11:16
 * 文档库和用户Mapper
 */
public interface DocSysHouseUserMapper extends BaseMapper<DocSysHouseUser> {

    /**
     * 文档库权限
     *
     * @param parentId
     * @return 返回值
     */
    List<DocBsDocumentUserDTO> selectInfo(@Param("parentId") Long parentId);
}
