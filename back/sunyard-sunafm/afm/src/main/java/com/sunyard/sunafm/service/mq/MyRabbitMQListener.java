package com.sunyard.sunafm.service.mq;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.rabbitmq.client.Channel;
import com.sunyard.afm.api.dto.AfmDetImgDetDTO;
import com.sunyard.afm.api.dto.AfmDetInvoiceDTO;
import com.sunyard.afm.api.dto.AfmDetPsDTO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.sunafm.constant.AfmConstant;
import com.sunyard.sunafm.dto.AfmDetOnlineResultDetailsDTO;
import com.sunyard.sunafm.manager.FalsifyService;
import com.sunyard.sunafm.manager.InvoiceService;
import com.sunyard.sunafm.mapper.AfmApiDataMapper;
import com.sunyard.sunafm.po.AfmApiData;
import com.sunyard.sunafm.service.RecordDupService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * 监听器：监听消息队列并进行消费
 */
@Slf4j
@Component
public class MyRabbitMQListener {
    @Resource
    private RecordDupService recordDupService;
    @Resource
    private FalsifyService falsifyService;
    @Resource
    private InvoiceService invoiceService;
    @Resource
    private AfmApiDataMapper afmApiDataMapper;

    @Resource(name = "GlobalThreadPool")
    private Executor executor;

    /**
     * queues 指定监听已有的哪个消费队列
     */
    @RabbitListener(queues = AfmConstant.QUEUE_NAMES, concurrency = "${sunyard.corePoolSize}")
    public void onAfmMessage(Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            String message1 = new String(message.getBody(), "UTF-8");
            log.info("消费消息: " + message + " | 线程: " + Thread.currentThread().getName());
            AfmApiData afmApiData = null;
            try {
                // 1. 校验消息体
                if (StringUtils.isEmpty(message1)) {
                    if (channel.isOpen()) {
                        channel.basicReject(deliveryTag, false);  // 不重新入队
                    }
                    return;
                }
                if (!message1.contains("materialTypeCode")) {
                    afmApiData = afmApiDataMapper.selectById(Long.parseLong(message1));
                    if (afmApiData == null || StringUtils.isEmpty(afmApiData.getRequestParams())) {
                        log.info("不存在当前消息对应的数据：{}", message1);
                        if (channel.isOpen()) {
                            channel.basicReject(deliveryTag, false);
                        }
                        return;
                    }
                }
                handleAfm(message1, afmApiData);
                if (channel.isOpen()) {
                    channel.basicAck(deliveryTag, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                //更新消息状态
                if (afmApiData != null) {
                    afmApiDataMapper.update(null,
                            new LambdaUpdateWrapper<AfmApiData>()
                                    .set(AfmApiData::getUpdateTime, new Date())
                                    .set(AfmApiData::getStatus, AfmConstant.OPEN_API_STATUS_ERROR)
                                    .set(AfmApiData::getErrorMsg, e.getMessage().length() > 450 ? e.getMessage().substring(0, 450) : e.getMessage())
                                    .eq(AfmApiData::getId, afmApiData.getId()));
                }
                if (channel.isOpen()) {
                    channel.basicReject(deliveryTag, false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (channel.isOpen()) {
                    channel.basicReject(deliveryTag, false);
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void handleAfm(String message1, AfmApiData afmApiData) throws IllegalAccessException, InvocationTargetException {
        AfmDetImgDetDTO afmDetImgDetDTO = null;
        if (afmApiData == null) {
            afmDetImgDetDTO = JSON.parseObject(message1, AfmDetImgDetDTO.class);
        } else {
            afmDetImgDetDTO = JSON.parseObject(afmApiData.getRequestParams(), AfmDetImgDetDTO.class);
        }
        if (AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE.equals(afmApiData.getType())){
            //影像查重
            imageAntiFraudDet(afmDetImgDetDTO,afmApiData,message1);
        }else if (AfmConstant.TEXT_ANTI_FRAUD_DET_TYPE.equals(afmApiData.getType())){
            //文本查重
            textAntiFraudDet(afmDetImgDetDTO,afmApiData,message1);
        }

    }

    private void textAntiFraudDet(AfmDetImgDetDTO afmDetImgDetDTO, AfmApiData afmApiData, String message1) {
        log.info("当前文本查重处理数据为:{}", afmDetImgDetDTO.getFileName());
        //查重检测
        String substring = afmDetImgDetDTO.getInvoiceType().substring(0, 2);
        if (AfmConstant.DO_DET_SAVE.equals(substring)) {
            //仅对传入文件做特征保存
            log.info("substring：{}，当前操作仅对传入文件做特征保存",substring);
            recordDupService.saveFeatureByText(afmDetImgDetDTO);
        }else if (AfmConstant.DO_DET_SAVE_AND_NOTE.equals(substring)){
            //查询查重结果
            log.info("substring：{}，对传入文件做查重处理",substring);
            recordDupService.dupByUrlByText(afmDetImgDetDTO);
        }
        //更新消息状态
        afmApiDataMapper.update(null,
                new LambdaUpdateWrapper<AfmApiData>()
                        .set(AfmApiData::getUpdateTime, new Date())
                        .set(AfmApiData::getStatus, AfmConstant.OPEN_API_STATUS_SUCC)
                        .eq(AfmApiData::getId, afmApiData.getId()));
    }

    private void imageAntiFraudDet(AfmDetImgDetDTO afmDetImgDetDTO, AfmApiData afmApiData, String message1) {

        log.info("保存特征，目前处理的是:{}", afmDetImgDetDTO.getFileName());
        //查重检测
        String substring = afmDetImgDetDTO.getInvoiceType().substring(0, 2);
        if (AfmConstant.DO_DET_SAVE.equals(substring)) {
            //仅对传入文件做特征保存
            log.info("仅对传入文件做特征保存");
            recordDupService.saveFeature(afmDetImgDetDTO);
        } else if (AfmConstant.DO_DET_SAVE_AND_NOTE.equals(substring)) {
            //对传入文件做查重并且保存特征
            //查询查重结果
            log.info("对传入文件做查重并且保存特征");
//            AfmDetImgDetResDTO dto = new AfmDetImgDetResDTO();
//            BeanUtils.copyProperties(dto, afmDetImgDetDTO);
            recordDupService.dupByUrl(afmDetImgDetDTO);
        }
        //ps检测
        String substring1 = afmDetImgDetDTO.getInvoiceType().substring(2, 3);
        if (AfmConstant.YES.toString().equals(substring1)) {
            AfmDetPsDTO dto = JSON.parseObject(message1, AfmDetPsDTO.class);
            falsifyService.detPs(dto);
        }
        //发票验真
        String substring3 = afmDetImgDetDTO.getInvoiceType().substring(3, 6);
        if (AfmConstant.DO_INVOICE_NOT.equals(substring3)) {
            //都不做检测
        } else {
            AfmDetInvoiceDTO dto = JSON.parseObject(message1, AfmDetInvoiceDTO.class);
            Result<Long> result = invoiceService.uploadInvoice(dto);
            Integer verify = null;
            List<AfmDetOnlineResultDetailsDTO> invoiceDup = null;
            List<AfmDetOnlineResultDetailsDTO> invoiceLink = null;
            if (AfmConstant.SUCC_CODE.equals(result.getCode().toString())) {
                if (AfmConstant.YES.toString().equals(substring3.substring(0, 1))) {
                    //做验真
                    verify = invoiceService.invoiceVerify(result.getData()).getData();
                }
                if (AfmConstant.YES.toString().equals(substring3.substring(1, 2))) {
                    //做查重
                    invoiceDup = invoiceService.invoiceDup(result.getData(), dto.getFileToken()).getData();
                }
                if (AfmConstant.YES.toString().equals(substring3.substring(2, 3))) {
                    //做连续
                    invoiceLink = invoiceService.invoiceLink(result.getData(), dto.getFileToken()).getData();
                }
                //拿到结果存入记录表
                invoiceService.record(dto, result.getData(), verify, invoiceDup, invoiceLink);
            }
        }
        //通知
        if (StringUtils.isNotBlank(afmDetImgDetDTO.getBackUrl())) {

        }
        //更新消息状态
        afmApiDataMapper.update(null,
                new LambdaUpdateWrapper<AfmApiData>()
                        .set(AfmApiData::getUpdateTime, new Date())
                        .set(AfmApiData::getStatus, AfmConstant.OPEN_API_STATUS_SUCC)
                        .eq(AfmApiData::getId, afmApiData.getId()));
    }
}