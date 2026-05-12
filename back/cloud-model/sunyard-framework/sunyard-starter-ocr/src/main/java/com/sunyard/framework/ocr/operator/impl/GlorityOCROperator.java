package com.sunyard.framework.ocr.operator.impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunyard.framework.common.util.UUIDUtils;
import com.sunyard.framework.common.util.date.DateUtils;
import com.sunyard.framework.ocr.config.properties.GlorityOcrProperties;
import com.sunyard.framework.ocr.dto.invoicevalidation.ValidateRequestParamDTO;
import com.sunyard.framework.ocr.dto.invoicevalidation.glority.GlorityInvoiceValidationDataDTO;
import com.sunyard.framework.ocr.dto.invoicevalidation.glority.GlorityInvoiceValidationResultDTO;
import com.sunyard.framework.ocr.dto.ocr.OCRRequestParamDTO;
import com.sunyard.framework.ocr.dto.ocr.OcrResultDTO;
import com.sunyard.framework.ocr.dto.ocr.glority.GlorityOCRResponseDTO;
import com.sunyard.framework.ocr.entity.InvoiceValidation;
import com.sunyard.framework.ocr.exception.CommonException;
import com.sunyard.framework.ocr.exception.NeedRetryException;
import com.sunyard.framework.ocr.operator.OcrOperator;
import com.sunyard.framework.ocr.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author PJW*/
@Slf4j
@Service
public class GlorityOCROperator implements OcrOperator {
    @Resource
    private GlorityOcrProperties glorityOcrProperties;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Resource(name = "typeCodePretaxAmountKeyNameMap")
    private Map<String, String> typeCodePretaxAmountKeyNameMap;
    @Resource(name = "validateResultCodeDesMap")
    private Map<Integer, String> validateResultCodeDesMap;
    @Resource(name = "invoiceTypeCodeNameMap")
    private Map<String, String> invoiceTypeCodeNameMap;

    private String getToken(long timestamp) {
        String token =
            DigestUtils.md5DigestAsHex((glorityOcrProperties.getAppKey() + "+" + timestamp + "+" + glorityOcrProperties.getAppSecret())
                .getBytes(StandardCharsets.UTF_8));
        return token;
    }

    /**
     * ocr并直接返回接口返回字符串
     *
     * @param
     * @return Result
     */
    @Override
    public String ocrResultAsString(String imageUrl, String imageData, String appKey, String ocrHost,
        String appSecret) {
        log.debug("{}ocr校验开始", imageUrl);
        String url = imageUrl;
        String host = ocrHost;
        MultiValueMap map = new LinkedMultiValueMap();
        long timestamp = System.currentTimeMillis() / 1000;
        String token = getToken(timestamp);
        map.add("app_key", appKey);
        map.add("timestamp", timestamp);
        map.add("token", token);
        if (StringUtils.isNotBlank(url)) {
            // url转base64模式,trm过来的url已经urlencode过,避免restTemplate再次encode,必须使用URI参数模式
            byte[] bytes = restTemplate.getForObject(URI.create(url), byte[].class);
            String base64 = null;
            try {
                base64 = new String(Base64.getEncoder().encode(bytes), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error("图片Base64加密异常", e);
                throw new CommonException("图片Base64加密异常");
            }
            map.add("image_data", base64);
        } else {
            // 直接base64模式
            map.add("image_data", imageData);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity requestEntity = new HttpEntity(map, headers);
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> result = restTemplate.exchange(host, HttpMethod.POST, requestEntity, String.class);
        long passTime = System.currentTimeMillis() - startTime;
        log.info("调OCR接口耗时:" + passTime);
        String resultBody = result.getBody();

        return resultBody;
        // return null;
    }

    /**
     * ocr并以List<OcrResult>形式返回结果
     *
     * @param
     * @return Result
     */
    @Override
    public List<OcrResultDTO> ocrResultAsDtoList(String imageUrl, String imageData, String appKey, String ocrHost,
        String appSecret) {
        GlorityOCRResponseDTO glorityOcrResponse = null;
        try {
            String resultString = ocrResultAsString(imageUrl, imageData, appKey, ocrHost, appSecret);
            if (!org.springframework.util.StringUtils.hasText(resultString)) {
                log.error("ocr接口返回数据为空:", imageUrl);
                throw new CommonException("ocr接口返回数据为空");
            }
            glorityOcrResponse = buildOCRResponse(resultString);
            if (!glorityOcrResponse.isSuccess() || glorityOcrResponse.getResponse() == null
                || glorityOcrResponse.getResponse().getData() == null
                || !new Integer("1").equals(glorityOcrResponse.getResponse().getData().getResult())
                || glorityOcrResponse.getResponse().getData().getIdentifyResults() == null) {
                return null;
            }
            List<OcrResultDTO> ocrResultDTOList = glorityOcrResponse.getResponse().getData().getIdentifyResults();
            if (ocrResultDTOList != null && ocrResultDTOList.size() > 0) {
                for (OcrResultDTO ocrResultDTO : ocrResultDTOList) {
                    String typeName = invoiceTypeCodeNameMap.get(ocrResultDTO.getType());
                    ocrResultDTO.setTypeName(typeName);
                    if (ocrResultDTO.getDetails() != null) {
                        // 根据不同发票构建识别码(部分发票没有发票号和发票代码,需要这么个参数作为唯一识别码)
                        String identifyCode = buildIdentifyCode(ocrResultDTO);
                        // 发票号
                        String number = (String)ocrResultDTO.getDetails().get("number");
                        if (StringUtils.isBlank(number)) {
                            number = identifyCode;
                        }

                        // 总金额
                        String total = (String)ocrResultDTO.getDetails().get("total");
                        ocrResultDTO.setIdentifyCode(identifyCode);
                        ocrResultDTO.setNumber(number);
                        ocrResultDTO.setTotal(total);
                    }

                }
            }
            return ocrResultDTOList;
        } catch (Exception e) {
            log.error("影像OCR识别异常:" + imageUrl, e);
            throw e;
        }
    }

    /**
     * @param buildOCRResponse
     * @return Result
     */
    private GlorityOCRResponseDTO buildOCRResponse(String buildOCRResponse) {
        GlorityOCRResponseDTO response = null;
        try {
            response = JSON.parseObject(buildOCRResponse, GlorityOCRResponseDTO.class);
        } catch (Exception e) {
            log.error("解析ocr接口返回结果异常", e);
        }
        return response;
    }

    /**
     * 验真并直接返回接口返回的字符串
     *
     * @param param
     * @return Result
     */

    @Override
    public String validate(ValidateRequestParamDTO param) {
        String host = glorityOcrProperties.getValidateHost() + "/v1/item/fapiao_validation";
        long timestamp = System.currentTimeMillis() / 1000;
        String token = getToken(timestamp);
        String code = param.getCode();
        String number = param.getNumber();
        String checkCode = param.getCheckCode();
        String paramCheckCode = checkCode;
        if (checkCode != null && checkCode.length() > 6) {
            // 校验码取后六位
            paramCheckCode = checkCode.substring(checkCode.length() - 6, checkCode.length());
        }
        String pretaxAmount = param.getPretaxAmount();
        String date = param.getDate();
        String type = param.getType();

        MultiValueMap map = new LinkedMultiValueMap();
        map.add("app_key", glorityOcrProperties.getAppKey());
        map.add("timestamp", timestamp);
        map.add("token", token);
        map.add("code", code);
        map.add("number", number);
        map.add("check_code", paramCheckCode);
        map.add("pretax_amount", pretaxAmount);
        map.add("date", date);
        map.add("type", type);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity requestEntity = new HttpEntity(map, headers);
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> result = null;
        try {
            result = restTemplate.exchange(host, HttpMethod.POST, requestEntity, String.class);
        } catch (Exception e) {
            log.error("调用验真接口异常", e);
            throw new CommonException("调用验真接口异常");
        }
        long passTime = System.currentTimeMillis() - startTime;
        log.info("调验真接口耗时:" + passTime);
        String resultBody = result.getBody();

        return resultBody;
    }

    /**
     * 解析校验结果并填充InvoiceValidation对应参数
     *
     * @param invoiceValidation
     */

    @Override
    public void analyseAndFillInvoiceValidation(InvoiceValidation invoiceValidation) {

        String resultString = invoiceValidation.getValidateData();
        if (invoiceValidation == null) {
            throw new CommonException("invoiceValidation参数为空");
        }
        if (StringUtils.isBlank(resultString)) {
            throw new CommonException("invoiceValidation的data参数为空");
        }
        GlorityInvoiceValidationResultDTO validationResult = null;
        try {
            validationResult = objectMapper.readValue(resultString, GlorityInvoiceValidationResultDTO.class);
        } catch (Exception e) {
            log.error("InvoiceValidationResult反序列化异常", e);
            throw new CommonException("InvoiceValidationResult反序列化异常");
        }

        Integer checkResult = 0;

        if (validationResult != null) {
            checkResult = validationResult.getResult();
        }
        if (checkResult == 1) {
            if (validationResult.getResponse() != null || validationResult.getResponse().getData() != null) {
                checkResult = validationResult.getResponse().getData().getResult();
            } else {
                checkResult = 0;
            }
        }
        // 前几级状态码仅作为接口调用是否成功的描述,data中的才是验真结果
        if (!new Integer("1").equals(checkResult)) {
            log.error("验真接口调用失败");
            throw new CommonException("验真接口调用失败");
        }

        GlorityInvoiceValidationDataDTO validationdata = validationResult.getResponse().getData();
        if (validationdata.getIdentifyResults() == null || validationdata.getIdentifyResults().isEmpty()
            || validationdata.getIdentifyResults().get(0).getValidation() == null) {
            log.error("验真接口返回数据异常");
            throw new CommonException("验真接口返回数据异常");
        }
        Integer validateCode = validationdata.getIdentifyResults().get(0).getValidation().getCode();
        String resultDes = validateResultCodeDesMap.get(validateCode);
        if (new Integer(10000).equals(validateCode)) {
            checkResult = 1;
        } else if (new Integer(10006).equals(validateCode)) {
            // 这类问题可能由于税局接口不稳导致,一般重试即可
            log.error("验真接口返回未知错误");
            throw new NeedRetryException("验真接口返回未知错误");
        } else if (new Integer(10003).equals(validateCode)) {
            log.error("验真次数超过限制");
            throw new CommonException("此票据验真次数超过限制,请明天再试");
        } else {
            checkResult = 0;
        }

        // 10001为"查无此票",可能为国税接口未同步成功,过一段时间后才可以查到,这类情况不用做md5,没有md5的数据不会保存到数据库做缓存
        if (!new Integer(10001).equals(validateCode)) {
            String code = invoiceValidation.getInvoiceCode();
            String number = invoiceValidation.getInvoiceNumber();
            String checkCode = invoiceValidation.getCheckCode();
            String pretaxAmount = invoiceValidation.getPretaxAmount();
            String date = invoiceValidation.getInvoiceDate();
            String type = invoiceValidation.getInvoiceType();

            // 把参数md5后作为识别码提升数据库查询效率,有可能撞车,所以真找到匹配数据的话会再校验一遍参数
            String md5 = md5FullString(code, number, checkCode, pretaxAmount, date, type);
            invoiceValidation.setParamMd5(md5);
        }

        // 如果验真返回字符串过长,尝试删掉items字段信息,如果还是不行,就不做记录
        try {
            if (resultString.length() > 3800) {
                Map<String, Object> details = validationdata.getIdentifyResults().get(0).getDetails();
                if (details != null && details.containsKey("items")) {
                    details.remove("items");

                    resultString = objectMapper.writeValueAsString(validationResult);
                }
                if (resultString.length() > 3800) {
                    resultString = "{\"des\":\"数据过长,无法记录\"}";
                }
            }
        } catch (Exception e) {
            resultString = "{\"des\":\"数据过长,无法记录\"}";
        }
        invoiceValidation.setValidateData(resultString);

        invoiceValidation.setResultDes(resultDes);
        invoiceValidation.setCheckResult(checkResult);
    }

    /**
     * 以验真参数构建查询用的md5字符串
     *
     * @param code
     * @param number
     * @param checkCode
     * @param pretaxAmount
     * @param date
     * @param type
     * @return Result
     */

    @Override
    public String md5FullString(String code, String number, String checkCode, String pretaxAmount, String date,
        String type) {
        String fullString = code + number + checkCode + pretaxAmount + date + type;
        // 把参数md5后作为识别码提升数据库查询效率,有可能撞车,所以真找到匹配数据的话会再校验一遍参数
        String md5 = null;
        try {
            md5 = DigestUtils.md5DigestAsHex(fullString.getBytes("UTF-8")).toUpperCase();
        } catch (UnsupportedEncodingException e) {
            log.error("MD5加密出错", e);
            throw new CommonException("MD5加密出错");
        }
        return md5;
    }

    @Override
    public InvoiceValidation validateAndGetInvoiceValidation(ValidateRequestParamDTO param) {

        String resultString = validate(param);

        InvoiceValidation invoiceValidation = new InvoiceValidation();
        invoiceValidation.setValidationId(UUIDUtils.generateUUID());
        invoiceValidation.setInvoiceCode(param.getCode());
        invoiceValidation.setInvoiceNumber(param.getNumber());
        invoiceValidation.setInvoiceType(param.getType());
        invoiceValidation.setCheckCode(param.getCheckCode());
        invoiceValidation.setPretaxAmount(param.getPretaxAmount());
        invoiceValidation.setInvoiceDate(param.getDate());
        invoiceValidation.setValidateData(resultString);
        // 解析校验结果
        try {
            analyseAndFillInvoiceValidation(invoiceValidation);
        } catch (NeedRetryException e) {
            // 重试一次
            resultString = validate(param);
            invoiceValidation.setValidateData(resultString);
            analyseAndFillInvoiceValidation(invoiceValidation);
            log.info("验真重试成功");
        }
        return invoiceValidation;
    }

    /**
     * 根据ocr结果构建票据唯一识别码
     *
     * @param ocrResultDTO
     * @return Result
     */
    @Override
    public String buildIdentifyCode(OcrResultDTO ocrResultDTO) {
        Map<String, Object> detail = ocrResultDTO.getDetails();
        String code = (String)detail.get("code");
        String number = (String)detail.get("number");
        String printNumber = (String)detail.get("print_number");
        String serialNumber = (String)detail.get("serial_number");
        String buyerTaxId = (String)detail.get("buyer_tax_id");

        String identifyCode = null;
        if (StringUtils.isNotBlank(code) && StringUtils.isNotBlank(number)) {
            identifyCode = code + number;
        } else if ("10503".equals(ocrResultDTO.getType())) {
            // 火车票
            identifyCode = serialNumber;
        } else if ("10506".equals(ocrResultDTO.getType())) {
            // 航空运输电子客票行程单
            identifyCode = number + printNumber;
        } else if ("20100".equals(ocrResultDTO.getType())) {
            // 小票时间戳化
            String receiptDate = (String)detail.get("date");
            String receiptTime = (String)detail.get("time");
            String receiptTotal = UUIDUtils.removePoint((String)detail.get("total"));
            String receiptType = (String)detail.get("type");
            try {
                String dateString = DateUtils.timeStampToDateTime(receiptDate + " " + receiptTime).toString();
                identifyCode = dateString + receiptTotal + receiptType;
            } catch (ParseException e) {
                log.error("日期转换异常", e);
                throw new CommonException("影像OCR识别失败");
            }

        } else if ("20105".equals(ocrResultDTO.getType())) {
            // 滴滴出行单时间戳化
            try {
                String ddPhone = (String)detail.get("phone");
                String ddTotal = UUIDUtils.removePoint((String)detail.get("total"));
                String dateStart = (String)detail.get("date_start");
                String dateEnd = (String)detail.get("date_end");
                String startTimeStamp = DateUtils.timeStampToDateTime(dateStart).toString();
                String endTimeStamp = DateUtils.timeStampToDateTime(dateEnd).toString();
                identifyCode = startTimeStamp + endTimeStamp + ddPhone + ddTotal;
            } catch (ParseException e) {
                log.error("日期转换异常", e);
                throw new CommonException("影像OCR识别失败");
            }

        } else if ("10902".equals(ocrResultDTO.getType())) {
            // 完税证明
            identifyCode = buyerTaxId + number;
        }
        return identifyCode;
    }

    /**
     * 判断此ocr结果是否有效
     *
     * @param ocrResultDTO
     * @return Result
     */
    @Override
    public String checkOcrResult(OcrResultDTO ocrResultDTO) {
        String result = null;
        if (ocrResultDTO == null) {
            log.warn("ocrResult为空");
            return "识别不到票据信息";
        }
        if (ocrResultDTO.getDetails() == null) {
            log.warn("ocrResult.getDetails为空");
            return "识别不到票据信息";
        }
        // 10900是个坑,只要是图片,没有被识别为其他几种有效的发票类型,都会默认归类为10090(其他可报销发票)
        if ("10900".equals(ocrResultDTO.getType())) {
            return "影像OCR识别失败";
        }
        if (StringUtils.isBlank(ocrResultDTO.getIdentifyCode())) {
            log.warn("无法构建有效IdentifyCode");
            return "异常票据";
        }
        return result;
    }

    /**
     * 以ocr结果构建验真参数
     *
     * @param ocrResultDTO
     * @return Result
     */
    @Override
    public ValidateRequestParamDTO buildValidateRequestParamByOcrResult(OcrResultDTO ocrResultDTO) {

        Map<String, Object> detailsMap = ocrResultDTO.getDetails();
        String checkCode = (String)detailsMap.get("check_code");
        String date = (String)detailsMap.get("date");
        String number = (String)detailsMap.get("number");
        String code = (String)detailsMap.get("code");
        String type = ocrResultDTO.getType();
        // 不同种类票据对税前金额的取值不一样
        String pretaxAmountKeyName = typeCodePretaxAmountKeyNameMap.get(ocrResultDTO.getType());
        String pretaxAmount = null;
        if (StringUtils.isNotBlank(pretaxAmountKeyName)) {
            pretaxAmount = (String)detailsMap.get(pretaxAmountKeyName);
        }

        ValidateRequestParamDTO validateRequestParam = new ValidateRequestParamDTO();
        validateRequestParam.setCheckCode(checkCode);
        validateRequestParam.setCode(code);
        validateRequestParam.setDate(date);
        validateRequestParam.setNumber(number);
        validateRequestParam.setType(type);
        validateRequestParam.setPretaxAmount(pretaxAmount);
        log.debug("验真参数：checkCode:{},code:{},date:{},number:{},type:{},pretaxAmount:{}", checkCode, code, date,
            number, type, pretaxAmount);
        return validateRequestParam;
    }

    @Override
    public String ocrAndValidate(OCRRequestParamDTO param) {
        return null;
    }
}
