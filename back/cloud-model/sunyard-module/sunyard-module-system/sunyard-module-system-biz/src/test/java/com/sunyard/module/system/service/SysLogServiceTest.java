package com.sunyard.module.system.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunyard.module.system.mapper.SysLogMapper;
import com.sunyard.module.system.po.SysLog;

/**
 * 系统管理-日志管理
 *
 * @author wubingyang
 * @date 2021/7/20 21:44
 */
@ExtendWith(MockitoExtension.class)
public class SysLogServiceTest {
    @InjectMocks
    private SysLogService sysLogService;
    @Mock
    private SysLogMapper mapper;

    @Test
    public void select() {
        Mockito.when(mapper.selectById(Mockito.anyLong())).thenReturn(new SysLog());
        sysLogService.select(1l);
    }

}
