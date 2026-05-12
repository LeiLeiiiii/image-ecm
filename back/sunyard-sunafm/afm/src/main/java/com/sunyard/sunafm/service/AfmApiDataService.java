package com.sunyard.sunafm.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.sunafm.constant.AfmConstant;
import com.sunyard.sunafm.mapper.AfmApiDataMapper;
import com.sunyard.sunafm.po.AfmApiData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zyl
 * @description
 * @since 2025/3/28
 */
@Slf4j
@Service
public class AfmApiDataService {

    private List<AfmApiData> retryList = new ArrayList<>();

    @Resource
    private AfmRetryService afmRetryService;

    @Value("${retry.status:2}")
    private String retryStatus;

    @Value("${retry.limitnum:100}")
    private String limitnum;

    @Resource
    private AfmApiDataMapper afmApiDataMapper;

    public void checkRepeatRetry() {
        //没处理完就直接return掉
        if (retryList.size() != 0) {
            return;
        }
        List<String> list = Arrays.asList(retryStatus.split(","));
        retryList = afmApiDataMapper.selectList(new LambdaUpdateWrapper<AfmApiData>()
                        .in(AfmApiData::getStatus, list)
                        .last("LIMIT " + limitnum)
        );
        if (!CollectionUtils.isEmpty(retryList)) {
            log.info("需要重试的数据有{}个", retryList.size());
            for (AfmApiData afmApiData : retryList) {
                afmRetryService.checkRepeatRetryAsyn(afmApiData);
            }
            //清空集合数据
            retryList.clear();
        }else{
            log.info("没有需要重试的数据");
        }


        // 计算前一天的开始时间（00:00:00）和结束时间（23:59:59）
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusMinutes(AfmConstant.FAILURE_TIME_MESSAGE+1);

        // 查询前一天状态为 INIT 的数据，并限制返回条数
        List<AfmApiData> afmApiData = afmApiDataMapper.selectList(
                new LambdaUpdateWrapper<AfmApiData>()
                        .lt(AfmApiData::getCreateTime, twentyFourHoursAgo)
                        .eq(AfmApiData::getStatus, AfmConstant.OPEN_API_STATUS_INIT)
                        .last("LIMIT " + limitnum)
        );

        if(!CollectionUtils.isEmpty(afmApiData)){
            log.info("需要重跑的数据有{}个", afmApiData.size());
            for(AfmApiData afmApiData1 : afmApiData){
                afmRetryService.checkRepeatRetryAsyn(afmApiData1);
            }
        }
    }

}
