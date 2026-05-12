package com.sunyard.edm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.edm.dto.DocSysAnnounDTO;
import com.sunyard.edm.po.DocSysAnnoun;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author wt 2022/12/15 11:16
 * 公告Mapper
 */
public interface DocSysAnnounMapper extends BaseMapper<DocSysAnnoun> {
    /**
     * 查看公告
     *
     * @return
     */
    List<DocSysAnnounDTO> selectListAnnounExtend(
            @Param("relId1") Long relId1, @Param("type1") Integer type1,
            @Param("relId2") Long relId2, @Param("type2") Integer type2,
            @Param("relId3") Long relId3, @Param("type3") Integer type3,
            @Param("teams") List<Long> teams, @Param("type4") Integer type4);
}
