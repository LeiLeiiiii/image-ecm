package com.sunyard.ecm.service.impl;
/*
 * Project: SunICM
 *
 * File Created at 2025/4/18
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sunyard.ecm.mapper.EcmAppDocrightMapper;
import com.sunyard.ecm.mapper.EcmDocrightDefMapper;
import com.sunyard.ecm.po.EcmAppDocright;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Leo
 * @Desc
 * @date 2025/4/18 12:36
 */
public class SysRoleServiceImplTest {
//    @InjectMocks
//    private SysRoleServiceImpl service;

    @Mock
    private EcmDocrightDefMapper ecmDocrightDefMapper;
    @Mock
    private EcmAppDocrightMapper ecmAppDocrightMapper;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getAppRightVerList() {
        List<EcmAppDocright> list = new ArrayList<>();
        EcmAppDocright ecmAppDocright = new EcmAppDocright();
        ecmAppDocright.setId(1L);
        list.add(ecmAppDocright);
//        Mockito.when(ecmAppDocrightMapper.selectList(Mockito.any(QueryWrapper.class))).thenReturn(list);
//        service.getAppRightVerList("1");
//        Mockito.when(ecmAppDocrightMapper.selectList(Mockito.any(QueryWrapper.class))).thenReturn(null);
//        service.getAppRightVerList("1");
    }
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/4/18 Leo creat
 */
