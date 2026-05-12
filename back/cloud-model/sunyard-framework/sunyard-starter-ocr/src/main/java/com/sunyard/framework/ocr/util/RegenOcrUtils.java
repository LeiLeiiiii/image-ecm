package com.sunyard.framework.ocr.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.util.UUIDUtils;
import com.sunyard.framework.common.util.date.DateUtils;
import com.sunyard.framework.ocr.dto.ocr.OcrResultDTO;
import com.sunyard.framework.ocr.dto.ocr.glority.GlorityOCRResponseDTO;
import com.sunyard.framework.ocr.exception.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 睿真OCR识别对外工具类
 *
 * @author： zyl @Description： @create： 2023/5/26 10:40
 */
@Slf4j
public class RegenOcrUtils {

    /**
     * ocr并直接返回接口返回字符串
     *
     * @param imageUrl 图片url
     * @param imageData 图片数据
     * @param appKey appKey
     * @param ocrHost ocrHost
     * @param appSecret appSecret
     * @return String
     */
    public static String ocrResultAsString(String imageUrl, String imageData, String appKey, String ocrHost,
        String appSecret) {
        String url = imageUrl;
        String host = ocrHost;
        HttpPost httpPost = new HttpPost(host);
        long timestamp = System.currentTimeMillis() / 1000;
        String token =
            DigestUtils.md5DigestAsHex((appKey + "+" + timestamp + "+" + appSecret).getBytes(StandardCharsets.UTF_8));

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("app_key", appKey));
        params.add(new BasicNameValuePair("timestamp", Long.toString(timestamp)));
        params.add(new BasicNameValuePair("token", token));
        params.add(new BasicNameValuePair("extract_level", "1"));
        String resultBody = "";
        long startTime = System.currentTimeMillis();
        String base64 = null;
        if (StringUtils.isNotBlank(url)) {
            try {
                base64 = OcrFileUtils.getBase64(url);
            } catch (Exception e) {
                log.error("图片Base64加密异常:",e);
                throw new CommonException("图片Base64加密异常");
            }
        } else {
            // 直接base64模式
            base64 = imageData;
        }
        try {
            params.add(new BasicNameValuePair("image_data", base64));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            resultBody = responseBody;
            // 测试数据result
        } catch (Exception e) {
            log.error("获取图片异常:",e);
            throw new CommonException("异常:", e.getMessage());
        }
        return resultBody;
    }

    /**
     * ocr并以List<OcrResult>形式返回结果
     *
     * @param imageUrl 图片url
     * @param imageData 图片数据
     * @param appKey appKey
     * @param ocrHost ocrHost
     * @param appSecret appSecret
     * @param invoiceTypeCodeNameMap 发票code
     * @return List
     */

    public static List<OcrResultDTO> ocrResultAsDtoList(String imageUrl, String imageData, String appKey,
        String ocrHost, String appSecret, Map<String, String> invoiceTypeCodeNameMap) {
        GlorityOCRResponseDTO glorityOcrResponse = null;
        try {
            String resultString = ocrResultAsString(imageUrl, imageData, appKey, ocrHost, appSecret);
            log.info("ocr识别结果：{}",resultString);
            //婚帖查分的
//            String resultString = "{\"id\":\"ad71b3cb7330485e914c7ff4699ccb73\",\"result\":1,\"message\":\"success\",\"response\":{\"data\":{\"version\":\"20201023\",\"timestamp\":\"1733456945\",\"message\":\"success\",\"id\":\"ad71b3cb7330485e914c7ff4699ccb73\",\"sha1\":\"c0fae5fc98005eda50758ce919566826e932859b\",\"time_cost\":\"4667\",\"result\":1,\"identify_results\":[{\"details\":{\"1\":\"\"},\"type\":\"1000021\",\"name\":\"5\",\"description\":\"机构贷抵押物清单\",\"orientation\":0,\"page\":0,\"region\":[1118,83,1400,919],\"extra\":{\"text-lines\":[[\"Tewo.u\",\"TA69*0:\"],[\"S\",\"T\",\"U\",\"存\",\"根B\",\"16-006-03265\",\"200140003403\"],[\"北京市出租汽车专用发票\"],[\"BEIJING TAX! SPECIAL IVOICE\"],[\"发票联\"],[\"INVOICE\"],[\"111601881161\"],[\"40302685\",\"审机测\"],[\"0357\"],[\"67366666\"],[\"车号 京\",\"B.V1759\"],[\"Taxl No.\"],[\"证号\",\"310131\"],[\"Certificate No.\"],[\"日期\",\"2019-01-15\"],[\"机\",\"打\",\"发\",\"票\",\"手\",\"写\",\"无\",\"效\",\"Date\",\"20:37-20:59\",\"2.30\",\"14.6\",\"00:02:42\"],[\"状态\",\"State\",\"0\"],[\"¥\",\"41.80\"],[\"燃油附加费\",\"¥\",\"1.00\"],[\"Fuel oll surcharge\"],[\"¥\",\"0.00\"],[\"¥\",\"43.00\"],[\"卡号\"],[\"Card N.o.\",\"----\"],[\"卡原额\"],[\"Previous Gard Balance\",\"卡余额\"],[\"Card Balance\"],[\"密 码\"],[\"Password\"]]},\"regions\":null,\"image_size\":[1679,1048]},{\"details\":{\"bank_name\":\"18-883-81143\",\"card_type\":\"\",\"purpose\":\"\",\"card_number\":\"\",\"effective_date\":\"\",\"name\":\"05070716\",\"unionpay_mark\":\"\"},\"type\":\"101022\",\"name\":\"80501\",\"description\":\"专用引擎银行卡\",\"orientation\":0,\"page\":0,\"region\":[794,87,1083,912],\"image_size\":[1679,1048]}]}}}";
//           单张的
//            String resultString = "{\"id\":\"8d5cc975651746a0b90f63d1b61325ac\",\"result\":1,\"message\":\"success\",\"response\":{\"data\":{\"version\":\"20201023\",\"timestamp\":\"1732865818\",\"message\":\"success\",\"id\":\"8d5cc975651746a0b90f63d1b61325ac\",\"sha1\":\"2fab59730334e4f5fa4d5ecb5d16561a0db13d23\",\"time_cost\":\"1773\",\"result\":1,\"identify_results\":[{\"details\":{\"bill_number\":\"\",\"bill_code\":\"52529764341\",\"date\":\"2020-03-20\",\"total\":\"\",\"check_code\":\"\",\"total_words\":\"\",\"personal_account_payment\":\"\",\"payer\":\"\",\"electronic_mark\":\"电子\",\"title\":\"航空运输电子客票行程单\",\"social_credit_code\":\"\",\"hospital\":\"\",\"overall_amount\":\"\",\"inpatient_number\":\"\",\"medical_record_number\":\"\",\"cash_payment\":\"\",\"inpatient_department\":\"\",\"admission_date\":\"\",\"discharge_date\":\"\",\"prepaid_amount\":\"\",\"supplementary_amount\":\"\",\"refund_amount\":\"\",\"kind\":\"\",\"outpatient_number\":\"\",\"medical_insurance_number\":\"\",\"visit_date\":\"\",\"medical_institution_type\":\"\",\"medical_insurance_type\":\"\",\"gender\":\"\",\"other_payments\":\"\",\"personal_expense\":\"\",\"personal_payment\":\"\",\"items\":[]},\"type\":\"102021\",\"name\":\"medical_invoice\",\"description\":\"电子医疗票据\",\"orientation\":0,\"page\":0,\"region\":[0,0,767,500],\"extra\":{\"text-lines\":[[\"航空运输电子客票行程单\",\"IERAtVEEI OFE TICXET\",\"FOR AR TRANS1 DRT\",\"印制序号:\",\"SERIALNLUMBER\",\"5252976434 1\"],[\"**机表St*\",\"刘广\",\"08\",\"效维份设件号有地5\",\"120823198906212032\",\"滨eerI\",\"o/不得签转/退改收费\"],[\"MB\",\"HKBB\",\"贰运人\",\"CR\",\"装机可\",\"18\",\"维等国\",\"0.0n\",\"日鲁\",\"B\",\"时\",\"TMT\",\"备照/花\",\"10\",\"医基生效计期\",\"东о\",\"有效提文日期\",\"细n\",\"(电费日\",\"结mk\",\"付\"],[\"BJM0N\",\"湛江\",\"里0\",\"上海虹桥\",\"厘币\",\"VOID\",\"10\",\"T2\",\"VOID\",\"医\",\"浙\",\"FM9404\",\"1\",\"2020\",\"08\",\"27\",\"11:30\",\"T\",\"g0k\",\"联\",\"¿\",\"M\",\"*\"],[\"¿a地G50m\",\"110\",\"釉址\",\"CNY\",\"615.00CN\",\"11511233\",\"50.00\",\"*牲*\",\"合W\",\"CNY\",\"665.00\",\"联\",\"见\"],[\"乌子票面\",\"17ot30\",\"7812325141434\",\"643\",\"鲁三信息\",\"W-awn\",\"保险费\",\"SBINF\",\"Xx×\",\"e\"],[\"销售单位代号\",\"SHA777\",\"*TC\",\"08677247\",\"填万综位\",\"8BBY\",\"阿斯兰航空服务(上海)有限公司\",\"质日期\",\"1\",\"2020-03-20\"],[\"400-81卡88\",\"M 服务热线:\",\"验名网址:\",\"WWWTRAVEL5KYCQ\",\"船信验置:\",\"及送JP生10669018\",\"建表海车机确区有大址省集和城是中;程记\",\"Wees ulsrduntsnewnwrwo\"]],\"title\":[\"航空运输电子客票行程单\"]},\"regions\":{\"bill_code\":[550,126,673,142],\"date\":[587,377,668,388],\"title\":[298,107,456,124]},\"image_size\":[767,500]}]}}}";
            if (!org.springframework.util.StringUtils.hasText(resultString)) {
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
            throw e;
        }
    }

    /**
     *  请求信雅达OCR
     * @param imageUrl
     * @param imageData
     * @param params
     * @return
     */
    public static List<OcrResultDTO> ocrResultAsDtoList(String imageUrl, byte[] imageData, String params , String classids , String lens) {
        final Integer OCR_SUCCESS_CODE = 200;
        try {
            String resultString = sunyardImageOcr(imageUrl, imageData, params, classids, lens);
            if (!org.springframework.util.StringUtils.hasText(resultString)) {
                throw new CommonException("OCR接口返回数据为空");
            }
            JSONObject outerResponse = JSON.parseObject(resultString);
            // 校验code
            if (!outerResponse.containsKey("code") ||
                    !OCR_SUCCESS_CODE.equals(outerResponse.getInteger("code"))) {
                String errorMsg = outerResponse.getString("msg");
                throw new CommonException("OCR服务返回错误: " + (errorMsg != null ? errorMsg : "未知错误"));
            }
            // 获取数据
            String dataStr = outerResponse.getString("data");
            if (!org.springframework.util.StringUtils.hasText(dataStr)) {
                throw new CommonException("OCR返回的 data 字段为空");
            }
            // 解析内层 data
            JSONObject innerData = JSON.parseObject(dataStr);
            OcrResultDTO dto = new OcrResultDTO();
            dto.setType(innerData.getString("class_ids"));

            Map<String, Object> entityMap = new HashMap<>();
            // 使用现有的 regions 字段存储坐标
            Map<String, Object> regionsMap = new HashMap<>();
            JSONArray detailArray = innerData.getJSONArray("detail");

            if (detailArray != null && !detailArray.isEmpty()) {
                for (int i = 0; i < detailArray.size(); i++) {
                    JSONObject detailItem = detailArray.getJSONObject(i);
                    if (detailItem == null) continue;
                    //封装返回值
                    for (String fieldName : detailItem.keySet()) {
                        JSONArray entities = detailItem.getJSONArray(fieldName);
                        if (entities != null && !entities.isEmpty()) {
                            JSONObject firstEntity = entities.getJSONObject(0);
                            if (firstEntity != null) {
                                String text = firstEntity.getString("text");
                                if (text != null) {
                                    entityMap.put(fieldName, text);
                                }
                                // 2. 提取坐标（新增）
                                JSONArray coordinates = firstEntity.getJSONArray("coordinates");
                                if (coordinates != null) {
                                    List<List<Integer>> coordList = new ArrayList<>();
                                    for (int j = 0; j < coordinates.size(); j++) {
                                        JSONArray point = coordinates.getJSONArray(j);
                                        List<Integer> pointList = new ArrayList<>();
                                        pointList.add(point.getInteger(0));
                                        pointList.add(point.getInteger(1));
                                        coordList.add(pointList);
                                    }
                                    regionsMap.put(fieldName, coordList);
                                }
                            }
                        }
                    }
                }
            }
            dto.setDetails(entityMap);
            dto.setRegions(regionsMap);
            return Collections.singletonList(dto);
        } catch (Exception e) {
            throw new RuntimeException("解析OCR结果失败", e);
        }
    }

    /**
     * 构建ocr响应对象
     *
     * @param buildOCRResponse ocr结果String
     * @return GlorityOCRResponseDTO
     */
    private static GlorityOCRResponseDTO buildOCRResponse(String buildOCRResponse) {
        GlorityOCRResponseDTO response = null;
        try {
            response = JSON.parseObject(buildOCRResponse, GlorityOCRResponseDTO.class);
        } catch (Exception e) {
        }
        return response;
    }

    /**
     * 根据ocr结果构建票据唯一识别码
     *
     * @param ocrResultDTO ocrResultDTO
     * @return String
     */

    public static String buildIdentifyCode(OcrResultDTO ocrResultDTO) {
        Map<String, Object> detail = ocrResultDTO.getDetails();
        String code = (String)detail.get("code");
        String number = (String)detail.get("number");
        // 航空印刷序号
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
                log.error("系统异常",e);
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
                log.error("系统异常",e);
                throw new CommonException("影像OCR识别失败");
            }

        } else if ("10902".equals(ocrResultDTO.getType())) {
            // 完税证明
            identifyCode = buyerTaxId + number;
        }
        return identifyCode;
    }
    /**
     * sunyard ocr识别
     */
    private static String sunyardImageOcr(String url, byte[] imageBytes, String params, String classids, String lens) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        try {
            ByteArrayBody fileBody = new ByteArrayBody(
                    imageBytes,
                    ContentType.IMAGE_JPEG,
                    "image.jpg"
            );
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addTextBody("params", params, ContentType.create("text/plain", StandardCharsets.UTF_8)) // 明确指定 UTF-8 编码
                    .addTextBody("classids", classids, ContentType.create("text/plain", StandardCharsets.UTF_8)) // 新增参数
                    .addTextBody("lens", lens, ContentType.create("text/plain", StandardCharsets.UTF_8))         // 新增参数
                    .addPart("file", fileBody)
                    .build();
            httpPost.setEntity(reqEntity);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                return EntityUtils.toString(responseEntity, "UTF-8");
            }
        } catch (Exception e) {
            throw new RuntimeException("sunyard ocr服务失败",e);
        } finally {
            try {
                httpClient.close();
            } catch (Exception ignored) {}
        }
    }
}
