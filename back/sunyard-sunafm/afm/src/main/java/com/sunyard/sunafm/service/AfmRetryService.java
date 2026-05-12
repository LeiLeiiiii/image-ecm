package com.sunyard.sunafm.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunyard.afm.api.dto.AfmDetImgDetDTO;
import com.sunyard.sunafm.mapper.AfmApiDataMapper;
import com.sunyard.sunafm.po.AfmApiData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Service
@EnableAsync
public class AfmRetryService {

    @Value("${retry.retryNum:3}")
    private int retryNum;

    @Resource
    private AfmApiDataMapper afmApiDataMapper;
    @Resource
    private RecordDupService recordDupService;

    @Async("checkRepeatExecutor")
    public void checkRepeatRetryAsyn(AfmApiData afmApiData) {
        try {
            //设置修改时间
            afmApiData.setUpdateTime(new Date());

            log.info("当前处理的数据为：" + afmApiData.getId());
            AfmDetImgDetDTO dto = getRequestBody(afmApiData.getRequestParams());
            int i = (afmApiData.getRetryNum() != null ? afmApiData.getRetryNum() : 0) + 1;
            try {
                recordDupService.dupByUrl(dto);
                afmApiData.setRetryNum(i);
                afmApiData.setStatus(4);
                afmApiData.setErrorMsg("重试成功！");
                log.info("当前线程" + Thread.currentThread().getName() + "修改 " + afmApiData.getId() + " 数据库成功,状态为 ： " + afmApiData.getStatus() + ";重试次数为:" + afmApiData.getRetryNum());
            } catch (Exception e) {
                e.printStackTrace();
                afmApiData.setRetryNum(i);
                if (afmApiData.getRetryNum() > retryNum) {
                    afmApiData.setStatus(3);
                }
                afmApiData.setErrorMsg(afmApiData.getErrorMsg() + e.getMessage());
                log.info("当前线程" + Thread.currentThread().getName() + "修改 " + afmApiData.getId() + " 数据库成功,状态为 ： " + afmApiData.getStatus() + ";重试次数为:" + afmApiData.getRetryNum());
            }

            //更新状态
            afmApiDataMapper.update(null,
                    new LambdaUpdateWrapper<AfmApiData>()
                            .set(AfmApiData::getUpdateTime, afmApiData.getUpdateTime())
                            .set(AfmApiData::getStatus, afmApiData.getStatus())
                            .set(AfmApiData::getErrorMsg,afmApiData.getErrorMsg())
                            .set(AfmApiData::getRetryNum,afmApiData.getRetryNum())
                            .eq(AfmApiData::getId, afmApiData.getId()));
        } catch (Exception e) {
            log.error("重试查重失败！,{}", e.getMessage());
        }
    }

    private AfmDetImgDetDTO getRequestBody(String apiPara) {
        AfmDetImgDetDTO dto = new AfmDetImgDetDTO();
        try {
            //json转换成请求对象
            ObjectMapper objectMapper = new ObjectMapper();
            dto = objectMapper.readValue(apiPara, AfmDetImgDetDTO.class);
            log.info("当前请求对象为:{}", dto);
        } catch (Exception e) {
            log.error("json转换对象失败 : {}", e.getMessage());
        }

        return dto;
    }
}
