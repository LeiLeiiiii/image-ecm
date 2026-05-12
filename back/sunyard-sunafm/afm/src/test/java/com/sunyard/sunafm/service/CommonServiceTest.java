//package com.sunyard.sunafm.service;
//
//import com.sunyard.framework.common.result.Result;
//import com.sunyard.module.system.api.DictionaryApi;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//
///**
// */
//@ExtendWith(MockitoExtension.class)
//public class CommonServiceTest {
//    @InjectMocks
//    private CommonService commonService;
//    @Mock
//    private DictionaryApi dictionaryApi;
//
//    @Test
//    public void getAfmSource() {
//        Mockito.when(dictionaryApi.getDescByCode(Mockito.anyString())).thenReturn(new Result<>());
//        commonService.getAfmSource();
//    }
//
//}
