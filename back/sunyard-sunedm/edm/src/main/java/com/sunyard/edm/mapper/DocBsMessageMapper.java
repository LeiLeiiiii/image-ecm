package com.sunyard.edm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.edm.dto.DocBsHomeDTO;
import com.sunyard.edm.dto.DocBsMessageDTO;
import com.sunyard.edm.po.DocBsMessage;

/**
 * @Author PJW 2022/12/14 10:08
 */
public interface DocBsMessageMapper extends BaseMapper<DocBsMessage> {

    /**
     * 查询近30天的消息通知
     *
     * @param userId
     * @param userIdList
     * @return
     */
    List<DocBsMessageDTO> searchExtend(@Param("userId") Long userId, @Param("userIdList") List<Long> userIdList);

    /**
     * 查询首页消息通知
     *
     * @param userId
     * @return
     */
    List<DocBsHomeDTO> searchListHome(@Param("userId") Long userId);

}
