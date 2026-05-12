package com.sunyard.sunafm.service.api;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.afm.api.dto.AfmDetImgDetDTO;
import com.sunyard.afm.api.dto.AfmDetInvoiceDTO;
import com.sunyard.afm.api.dto.AfmDetPsDTO;
import com.sunyard.afm.api.dto.AfmDetUpdateDto;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.sunafm.constant.AfmConstant;
import com.sunyard.sunafm.manager.FalsifyService;
import com.sunyard.sunafm.manager.InvoiceService;
import com.sunyard.sunafm.mapper.AfmApiDataMapper;
import com.sunyard.sunafm.po.AfmApiData;
import com.sunyard.sunafm.service.RecordDupService;
import com.sunyard.sunafm.service.mq.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;


/**
 *
 * @author P-JWei
 * @date 2024/3/20 14:05:53
 * @title
 * @description 对外接口实现类
 */
@Slf4j
@Service
public class ApiService {
    @Resource
    private AfmApiDataMapper afmApiDataMapper;
    @Resource
    private MessageService messageService;
    @Resource
    private RecordDupService recordDupService;
    @Resource
    private FalsifyService falsifyService;
    @Resource
    private InvoiceService invoiceService;
    @Resource
    private SnowflakeUtils snowflakeUtil;

    @Value("${afminsert.num:500}")
    private Integer insertNum;
    /**
     * 调用查重检测
     */
    public void antiFraudDet(AfmDetImgDetDTO dto) {
        //存队列，异步执行
        publishMessage(dto,AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE);
    }

    /**
     * 获取查重结果
     */
    public Map antiFraudDetRes(AfmDetImgDetDTO dto) {
        Map result = new HashMap();
        //查重
        String substring = dto.getInvoiceType().substring(0, 2);
        if (AfmConstant.DO_DET_SAVE_AND_NOTE.equals(substring)) {
            Map map = recordDupService.antiFraudDetRes(dto,AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE);
            result.put("dup", map);
        } else {
            result.put("dup", "");
        }
        //ps检测
        String substring1 = dto.getInvoiceType().substring(2, 3);
        if (AfmConstant.YES.toString().equals(substring1)) {
            AfmDetPsDTO afmDetPsDTO = new AfmDetPsDTO();
            BeanUtils.copyProperties(dto, afmDetPsDTO);
            Map map = falsifyService.psResult(afmDetPsDTO);
            result.put("ps", map);
        } else {
            result.put("ps", "");
        }
        //发票
        String invoice = dto.getInvoiceType().substring(3, 6);
        if (AfmConstant.DO_INVOICE_NOT.equals(invoice)) {
            //不获取结果
            result.put("invoice", "");
        } else {
            AfmDetInvoiceDTO afmDetInvoiceDTO = new AfmDetInvoiceDTO();
            BeanUtils.copyProperties(dto, afmDetInvoiceDTO);
            String yz = dto.getInvoiceType().substring(3, 4);
            String cc = dto.getInvoiceType().substring(4, 5);
            String lx = dto.getInvoiceType().substring(5, 6);
            List<Map> invoiceResult = new ArrayList<>();
            if (AfmConstant.YES.toString().equals(yz)) {
                //拿验真结果
                Map map = invoiceService.verifyResult(afmDetInvoiceDTO);
                invoiceResult.add(map);
            }
            if (AfmConstant.YES.toString().equals(cc)) {
                //拿查重结果
                Map map = invoiceService.dupResult(afmDetInvoiceDTO);
                invoiceResult.add(map);
            }
            if (AfmConstant.YES.toString().equals(lx)) {
                //拿连续结果
                Map map = invoiceService.linkResult(afmDetInvoiceDTO);
                invoiceResult.add(map);
            }
            result.put("invoice", invoiceResult);
        }
        return result;
    }

    /**
     * 存特征
     */
    public void saveFeature(AfmDetImgDetDTO dto) {
        publishMessage(dto,AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE);

    }

    /**
     * 试试返回结果
     */
    public Map antiFraudDetNow(AfmDetImgDetDTO dto) {
        recordDupService.dupByUrl(dto);
        Map map = recordDupService.antiFraudDetRes(dto,AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE);
        return map;
    }

    /**
     *
     */
    public void saveFeatureNow(List<MultipartFile> fileList1, List<AfmDetImgDetDTO> vo) {
        recordDupService.saveFeatureList(fileList1,vo,AfmConstant.IMAGE_ANTI_FRAUD_DET_TYPE);
    }

    /**
     *  删除文件
     */
    public void delFile(AfmDetImgDetDTO dto) {
        recordDupService.delFile(dto);

    }

    private void publishMessage(AfmDetImgDetDTO dto,Integer type) {
        if(dto.getBase64()!=null){
            AssertUtils.isTrue(true,"异步不支持base64方式");
        }
        //校验参数
        recordDupService.checkParam(dto);
        String s1 = JSONObject.toJSONString(dto);
        AfmApiData data = new AfmApiData();
        data.setStatus(AfmConstant.OPEN_API_STATUS_INIT);
        data.setId(snowflakeUtil.nextId());
        data.setRequestParams(s1);
        data.setType(type);
        afmApiDataMapper.insert(data);
        messageService.publish(data.getId().toString());
        log.info("结束推送至队列：{}", dto.getFileIndex());
    }

    public void antiFraudDetList(List<AfmDetImgDetDTO> dtos) {
        List<AfmApiData> dtos1 = getAfmApiData(dtos);
        long l = System.currentTimeMillis();
        for (AfmApiData dto : dtos1) {
            messageService.publish(dto.getId().toString());
            log.info("结束推送至队列,apiId：{}", dto.getId());
        }
        log.info("结束批量推送消息，总耗时：{}",System.currentTimeMillis()-l);

    }


    @Transactional(rollbackFor = Exception.class)
    public List<AfmApiData> getAfmApiData(List<AfmDetImgDetDTO> dtos) {
        List<AfmApiData> dtos1 = Collections.synchronizedList(new ArrayList<>());
        for (AfmDetImgDetDTO dto : dtos) {
            if (dto.getBase64() != null) {
                AssertUtils.isTrue(true, "异步不支持base64方式");
            }
            //校验参数
            recordDupService.checkParam(dto);
            String s1 = JSONObject.toJSONString(dto);
            AfmApiData data = new AfmApiData();
            data.setStatus(AfmConstant.OPEN_API_STATUS_INIT);
            data.setRequestParams(s1);
            data.setId(snowflakeUtil.nextId());
            data.setRetryNum(0);
            data.setIsDeleted(0);
            dtos1.add(data);
        }

        // 没500条一次提交
        if (dtos1.size() > insertNum) {
            int batchSize = insertNum;
            for (int i = 0; i < dtos1.size(); i += batchSize) {
                int end = Math.min(i + batchSize, dtos1.size());
                List<AfmApiData> batch = dtos1.subList(i, end);
                afmApiDataMapper.insertBatch(batch);
            }
        } else {
            afmApiDataMapper.insertBatch(dtos1);
        }

        return dtos1;
    }

    public void antiFraudDetByText(AfmDetImgDetDTO dto) {
        //存队列，异步执行
        publishMessage(dto,AfmConstant.TEXT_ANTI_FRAUD_DET_TYPE);
    }

    public Map antiFraudDetResByText(AfmDetImgDetDTO dto) {
        Map result = new HashMap();
        //查重
        String substring = dto.getInvoiceType().substring(0, 2);
        if (AfmConstant.DO_DET_SAVE_AND_NOTE.equals(substring)) {
            Map map = recordDupService.antiFraudDetResByText(dto);
            result.put("dup", map);
        } else {
            result.put("dup", "");
        }
        return result;
    }

    public Map saveFeatureByTextNow(AfmDetImgDetDTO dto) {
        recordDupService.dupByUrlByText(dto);
        Map map = recordDupService.antiFraudDetRes(dto,AfmConstant.TEXT_ANTI_FRAUD_DET_TYPE);
        return map;
    }

    public void ecmToAfmDataSync(AfmDetUpdateDto dto) {
        recordDupService.ecmToAfmDataSync(dto);
    }
}
