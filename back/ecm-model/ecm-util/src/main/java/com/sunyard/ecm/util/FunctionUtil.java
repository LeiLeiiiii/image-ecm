package com.sunyard.ecm.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sunyard.ecm.dto.AddBusiDTO;
import com.sunyard.ecm.dto.BusiDocDuplicateDTO;
import com.sunyard.ecm.dto.EcmBusExtendDTO;
import com.sunyard.ecm.dto.EcmBusiAttrDTO;
import com.sunyard.ecm.dto.EcmDownloadFileDTO;
import com.sunyard.ecm.dto.EcmDownloadFileOutDTO;
import com.sunyard.ecm.dto.EcmReuseBatchDTO;
import com.sunyard.ecm.dto.EcmRootDataDTO;
import com.sunyard.ecm.dto.EditBusiAttrOutDTO;
import com.sunyard.ecm.vo.BusiDocDuplicateTarVO;
import com.sunyard.ecm.vo.BusiDocDuplicateVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator.Feature.WRITE_XML_DECLARATION;

/**
 * 新老影像对接工具类,xml转json,json转xml等数据处理
 * @author yzy
 * @since 2025/02/17 10:55
 */
@Slf4j
public class FunctionUtil {

    /**
     * 将xml转为EcmRootDataDTO
     *
     * @param xml xml信息
     * @return ecmRootDataDTO
     * @throws
     */
    public static EcmRootDataDTO getEcmRootDataDTO(String xml) throws JsonProcessingException {
        XmlMapper xmlMapper =new XmlMapper();
        EcmRootDataDTO ecmRootDataDTO = xmlMapper.readValue(xml, EcmRootDataDTO.class);
        if(ecmRootDataDTO!=null&&! CollectionUtils.isEmpty(ecmRootDataDTO.getEcmBusExtendDTOS())){
            List<EcmBusExtendDTO> ecmBusExtendDTOS = ecmRootDataDTO.getEcmBusExtendDTOS();
            //暂时解决xml转jason后的空数据问题{}
            ecmBusExtendDTOS.removeIf(s -> areAllFieldsNull(s));
            for(EcmBusExtendDTO ecmBusExtendDTO : ecmBusExtendDTOS){
                Map<String, Object> extraFields = ecmBusExtendDTO.getExtraFields();
                List<EcmBusiAttrDTO> list = new ArrayList<>();
                extraFields.forEach((key, value) -> {
                    EcmBusiAttrDTO dto = new EcmBusiAttrDTO();
                    //暂时写死 入参xml中没传
                    dto.setAttrCode(key);
                    dto.setAppAttrValue(value.toString());
                    list.add(dto);
                });
                ecmBusExtendDTO.setEcmBusiAttrDTOList(list);
            }
        }
        return ecmRootDataDTO;
    }

    /**
     * 将xml转为EditBusiAttrOutDTO
     *
     * @param xml xml信息
     * @return BusiDocDuplicateDTO
     * @throws
     */
    public static EditBusiAttrOutDTO getEditBusiAttrOutDTO(String xml) throws JsonProcessingException {
        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.readValue(xml, EditBusiAttrOutDTO.class);
    }

    /**
     * 将xml转为EditBusiAttrOutDTO
     *
     * @param xml xml信息
     * @return BusiDocDuplicateDTO
     * @throws
     */
    public static EcmDownloadFileDTO getEcmDownloadFileDTO(String xml) throws JsonProcessingException {
        XmlMapper xmlMapper = new XmlMapper();
        EcmDownloadFileOutDTO ecmDownloadFileOutDTO = xmlMapper.readValue(xml, EcmDownloadFileOutDTO.class);
        EcmDownloadFileDTO ecmDownloadFileDTO = new EcmDownloadFileDTO();
        ecmDownloadFileDTO.setEcmBaseInfoDTO(ecmDownloadFileOutDTO.getEcmBaseInfoDTO());
        ecmDownloadFileDTO.setBusiNo(ecmDownloadFileOutDTO.getEcmDownloadMetaDataDTO().getEcmDownloadBatchDTO().getBusiNo());
        ecmDownloadFileDTO.setAppCode(ecmDownloadFileOutDTO.getEcmDownloadMetaDataDTO().getEcmDownloadBatchDTO().getAppCode());
        List<String> docNo = ecmDownloadFileOutDTO.getEcmDownloadMetaDataDTO().getDocNos();
        if(!CollectionUtils.isEmpty(docNo)){
            ecmDownloadFileDTO.setDocNo(docNo.size()>1?"":docNo.get(0));
        }
        return ecmDownloadFileDTO;
    }

    /**
     * 将xml转为BusiDocDuplicateVO
     *
     * @param xml xml信息
     * @return BusiDocDuplicateDTO
     * @throws
     */
    public static BusiDocDuplicateVO getBusiDocDuplicateVO(String xml) throws JsonProcessingException {
        XmlMapper xmlMapper =new XmlMapper();
        BusiDocDuplicateDTO busiDocDuplicateDTO = xmlMapper.readValue(xml, BusiDocDuplicateDTO.class);
        EcmReuseBatchDTO fromBatch=busiDocDuplicateDTO.getEcmReuseMetaDataDTO().getFromBatch();
        List<EcmReuseBatchDTO> toBatchs=busiDocDuplicateDTO.getEcmReuseMetaDataDTO().getToBatch();
        //来源节点
        BusiDocDuplicateVO busiDocDuplicateVO=new BusiDocDuplicateVO();
        List<BusiDocDuplicateTarVO> busiDocDuplicateVos=new ArrayList<>();
        AddBusiDTO ecmBusExtendDTO=new AddBusiDTO();
        busiDocDuplicateVO.setBusiDocDuplicateVos(busiDocDuplicateVos);
        busiDocDuplicateVO.setEcmBusExtendDTO(ecmBusExtendDTO);
        //来源节点属性
        EcmBusExtendDTO ecmBusExtendDTOS=new EcmBusExtendDTO();
        ecmBusExtendDTO.setEcmBusExtendDTOS(ecmBusExtendDTOS);
        //源节点赋值基础属性及业务属性
        ecmBusExtendDTO.setEcmBaseInfoDTO(busiDocDuplicateDTO.getEcmBaseInfoDTO());
        ecmBusExtendDTOS.setAppCode(fromBatch.getAppCode());
        ecmBusExtendDTOS.setBusiNo(fromBatch.getBusiNo());
        ecmBusExtendDTOS.setAppName(fromBatch.getAppName());
        List<String> fromDocNos=new ArrayList<>();
        //如果来源批次中的资料编码不为空则取来源，反之取外层的
        if(!ObjectUtils.isEmpty(fromBatch.getDocNo())){
            fromDocNos.add(fromBatch.getDocNo());
            //这里给的是目标批次的DocNo
            ecmBusExtendDTOS.setEcmDocCodes(fromDocNos);
        }else{
            List<String> docNos=busiDocDuplicateDTO.getEcmReuseMetaDataDTO().getDocNos();
            if (CollectionUtils.isEmpty(docNos)){
                docNos=new ArrayList<>();
            }
            fromDocNos.addAll(docNos);
            ecmBusExtendDTOS.setEcmDocCodes(fromDocNos);
        }
        //封装目标批次,注意目标批次的docNo是来源的，与PC端的相反
        List<BusiDocDuplicateTarVO> busiDocDuplicateTarVOS=new ArrayList<>();
        for(EcmReuseBatchDTO toBatch:toBatchs) {
            BusiDocDuplicateTarVO busiDocDuplicateTarVO = new BusiDocDuplicateTarVO();
            AddBusiDTO addBusiDTOTarget = new AddBusiDTO();
            busiDocDuplicateTarVO.setEcmBusExtendDTO(addBusiDTOTarget);
            EcmBusExtendDTO ecmBusExtendDTOTarget = new EcmBusExtendDTO();
            addBusiDTOTarget.setEcmBusExtendDTOS(ecmBusExtendDTOTarget);
            if(toBatch.getDocNo()==null){
                ecmBusExtendDTOTarget.setEcmDocCodes(fromDocNos);
            }else{
                ecmBusExtendDTOTarget.setEcmDocCodes(Collections.singletonList(toBatch.getDocNo()));
            }
            ecmBusExtendDTOTarget.setAppCode(toBatch.getAppCode());
            ecmBusExtendDTOTarget.setBusiNo(toBatch.getBusiNo());
            busiDocDuplicateTarVOS.add(busiDocDuplicateTarVO);
            busiDocDuplicateVO.setBusiDocDuplicateVos(busiDocDuplicateTarVOS);
        }
        return busiDocDuplicateVO;
    }



    public static boolean areAllFieldsNull(Object obj) {
        if (obj == null) return true;

        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !field.isAnnotationPresent(Transient.class)) {
                field.setAccessible(true); // 设置为可访问
                try {
                    Object value = field.get(obj);
                    if (value != null) {
                        return false; // 如果任何一个字段不为 null，则返回 false
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access field", e);
                }
            }
        }
        return true; // 所有字段都为 null 时返回 true
    }

    /**
     * 将结果转为xml
     *
     * @param bean
     * @return xml xml信息
     * @throws
     */
    public static String toXml(Object bean){
        // 创建 XmlMapper 实例
        XmlMapper xmlMapper = new XmlMapper();
        String xml="";
        // 启用美化输出
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 确保输出 XML 声明
        xmlMapper.configure(WRITE_XML_DECLARATION, true);
        // 将 DTO 对象转换为 XML 字符串
        try {
             xml = xmlMapper.writeValueAsString(bean);
        }catch (Exception e){
            log.error("转xml响应失败", e);
            throw new RuntimeException("转xml响应失败", e);
        }
        return xml;
    }
}
