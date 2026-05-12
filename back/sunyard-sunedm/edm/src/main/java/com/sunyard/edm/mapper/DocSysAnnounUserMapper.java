package com.sunyard.edm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.edm.dto.DocBsHomeDTO;
import com.sunyard.edm.po.DocSysAnnounUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author wt 2022/12/15 11:16
 * 公告和用户关联Mapper
 */
public interface DocSysAnnounUserMapper extends BaseMapper<DocSysAnnounUser> {

    /**
     * 查询首页公告栏内容
     *
     * @param list
     * @return
     */
    List<DocBsHomeDTO> searchListHome(@Param("list") List<Long> list);
}
