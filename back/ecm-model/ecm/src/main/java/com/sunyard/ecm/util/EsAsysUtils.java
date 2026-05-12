package com.sunyard.ecm.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunyard.ecm.config.properties.EcmOcrProperties;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.enums.EcmCheckAsyncTaskEnum;
import com.sunyard.ecm.es.EsEcmFile;
import com.sunyard.ecm.manager.FileInfoService;
import com.sunyard.ecm.mapper.es.EsEcmFileMapper;
import com.sunyard.framework.elasticsearch.util.PdfBoxAndPoiUtils;
import com.sunyard.framework.elasticsearch.vo.BaseAttachment;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.dromara.easyes.core.conditions.update.LambdaEsUpdateWrapper;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EsAsysUtils {
    public final static List<String> IMGS = Arrays.asList("jpg", "jpeg", "gif", "bmp", "tif", "png");
    private final static List<String> suffixArr = Arrays.asList("txt", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "jpg", "png", "jpeg");

    @Value("${storage.url:http://172.1.1.210:28083}")
    private String fileFullPath;

    @Value("${fileIndex:ecm_file_dev}")
    private String fileIndex;

    @Resource
    private EsEcmFileMapper esEcmFileMapper;

    @Resource
    private RestHighLevelClient restHighLevelClient;
    @Resource
    @Lazy
    private FileInfoService fileInfoService;

    @Resource
    private EcmOcrProperties ecmOcrProperties;
    /**
     * 用pdfbox解析文件代替管道
     *
     * @param jsonObject json格式文件对象
     */
    @Async("GlobalThreadPool")
    public Future<Void> getFileInfoByBytes(JSONObject jsonObject, String indexName, String id, String fileBytes) {
        // 获取文件byte[]
        String fileName1 = jsonObject.getString("fileName");
        if (fileBytes != null && !"".equals(fileBytes)) {
            try {
                String fileName = jsonObject.getString("fileName");
                // 将获取到的bytes解码
                byte[] bytes = Base64.getDecoder().decode(fileBytes);
                File tempFile = null;
                // 找到文件名中最后一个点的位置
                int lastIndex = fileName.lastIndexOf('.');
                // 提取文件的原始名称（不包含扩展名）
                String filenameWithoutExtension = fileName.substring(0, lastIndex);
                String extension = fileName.substring(lastIndex);
                // 创建临时文件用于读取
                tempFile = Files.createTempFile(filenameWithoutExtension, extension).toFile();
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(bytes);
                fos.close();
                // 获取文件信息
                getAttachmentInfo(jsonObject, tempFile,extension);
                tempFile.delete();
                UpdateRequest updateRequest = new UpdateRequest(indexName, id)
                        .doc(jsonObject.toJSONString(), XContentType.JSON); // 不使用版本控制
//.doc("fileBytes", "").doc("content", "")
                UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
                log.info("UpdateResponse: {}", updateResponse);
            } catch (Exception e) {
                log.error("elasticsearch getFileInfoByBytes:{}"+fileName1, e.getMessage(), e);
            }
        } else {
            log.info("============没有获取到文件bytes============");
        }
        return new AsyncResult<>(null);
    }

    /**
     * 获取文件信息并存入json
     *
     * @param jsonObject json格式文件对象
     * @param tempFile 文件对象
     */
    private void getAttachmentInfo(JSONObject jsonObject, File tempFile,String ext) throws IOException {
        //PdfBox解析文件信息
        String context = "";
        try {
            context = getFileContext(tempFile, ext);
        } catch (Exception e) {
            context="";
            log.info("解析文件失败");
        }
        JSONObject attachment=new JSONObject();
        attachment.put("date",new Date());
        attachment.put("contentType",ext);
        attachment.put("author","");
        attachment.put("language","");
        attachment.put("content",context);
        attachment.put("contentLength","");
        jsonObject.put("attachment",attachment);
        //删除多余元素
        jsonObject.remove("fileBytes");
        jsonObject.remove("content");
        log.debug("获取到的文件信息为:   "+attachment.toJSONString());
    }

    public String getFileContext(File tempFile, String ext) {
        String context = "";
        try {
            switch (ext) {
                case ".pdf":
                    context = PdfBoxAndPoiUtils.parserPDF(tempFile);
                    break;
                case ".txt":
                    context = PdfBoxAndPoiUtils.parserTxt(tempFile);
                    break;
                case ".docx":
                case ".doc":
                    context = PdfBoxAndPoiUtils.parseDocxAndDoc(tempFile);
                    break;
                case ".xls":
                case ".xlsx":
                    context = PdfBoxAndPoiUtils.parseXlsAndXlsx(tempFile);
                    break;
//                case ".jpg":
//                case ".jpeg":
//                case ".gif":
//                case ".bmp":
//                case ".tif":
//                case ".png":
//                    context = executePostRequest(getOcrUrl, createOcrRequestBody(jsonObject.getString("newFileId")));
//                    break;
                default:
                    context = "";
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return context;
    }

    public String updateEsImgContext(EcmFileInfoDTO ecmFileInfoDTO, String taskType, String fileUrl, AccountTokenExtendDTO token) {
        String context = "";
        try {
            if(IMGS.contains(ecmFileInfoDTO.getFormat())){
                if (taskType.charAt(6) == EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0)) {
                    context = executePostRequest(ecmOcrProperties.getGetOcrUrl(), createOcrRequestBody(ecmFileInfoDTO.getNewFileId().toString()));
                    if(context!=null){
                        updateEs(ecmFileInfoDTO, context);
                        taskType = updateStatus(taskType, IcmsConstants.TYPE_SEVEN,
                                EcmCheckAsyncTaskEnum.SUCCESS.description().charAt(0));
                    }else {
                        taskType = updateStatus(taskType, IcmsConstants.TYPE_SEVEN,
                                EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
                    }
                }
            }else if (suffixArr.contains(ecmFileInfoDTO.getFormat())){
                log.info("进行文档类型全文索引识别:{}",ecmFileInfoDTO.getFileId());
                //文档类型
                boolean flag = fileInfoService.addEsFileInfoSync(ecmFileInfoDTO, token.getId());
                context = fileInfoService.getFileContext(ecmFileInfoDTO);
                if (flag){
                    taskType = updateStatus(taskType, IcmsConstants.TYPE_SEVEN,
                            EcmCheckAsyncTaskEnum.SUCCESS.description().charAt(0));
                }else {
                    taskType = updateStatus(taskType, IcmsConstants.TYPE_SEVEN,
                            EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
                }
            } else {
                taskType = updateStatus(taskType, IcmsConstants.TYPE_SEVEN,
                        EcmCheckAsyncTaskEnum.SUCCESS.description().charAt(0));
            }
        } catch (Exception e) {
            taskType = updateStatus(taskType, IcmsConstants.TYPE_SEVEN,
                    EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
        }
        //文本查重
        try {
            if (taskType.charAt(IcmsConstants.TYPE_TEN - 1) == EcmCheckAsyncTaskEnum.PROCESSING
                    .description().charAt(0)) {
                log.info("当前taskType :{}", taskType);
                //获取文本信息
                boolean flag = fileInfoService.judgeAfmText(ecmFileInfoDTO,context);
                if (flag && taskType.charAt(IcmsConstants.TYPE_SEVEN - 1) == EcmCheckAsyncTaskEnum.SUCCESS
                        .description().charAt(0)) {
                    //ocr识别成功
                    taskType = fileInfoService.handleAfm(ecmFileInfoDTO, taskType, fileUrl);
                } else if (taskType.charAt(IcmsConstants.TYPE_SEVEN - 1) == EcmCheckAsyncTaskEnum.FAILED
                        .description().charAt(0)){
                    //OCR识别失败，文本查重直接置为失败
                    taskType = updateStatus(taskType, IcmsConstants.TYPE_TEN,
                            EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
                } else if (!flag && taskType.charAt(IcmsConstants.TYPE_SEVEN - 1) == EcmCheckAsyncTaskEnum.SUCCESS
                        .description().charAt(0)){
                    // OCR识别成功，未识别出文本信息，直接置为失败
                    taskType = updateStatus(taskType, IcmsConstants.TYPE_TEN,
                            EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
                }
            }
        } catch (Exception e) {
            log.error(ecmFileInfoDTO.getFileId() + ":文本查重异常", e);
            taskType = updateStatus(taskType, IcmsConstants.TYPE_TEN,
                    EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
        }
        return taskType;

    }

    private void updateEs(EcmFileInfoDTO ecmFileInfoDTO, String context) {
        //识别成功并存入es
        BaseAttachment attachment=new BaseAttachment();
        attachment.setDate(new Date());
        attachment.setContentType(ecmFileInfoDTO.getFormat());
        attachment.setContent(context);
        esEcmFileMapper.update(
                null,
                new LambdaEsUpdateWrapper<EsEcmFile>()
                        .indexName(fileIndex)
                        .set(EsEcmFile::getAttachment, attachment) // 更新整个 attachment 对象
                        .eq(EsEcmFile::getId, String.valueOf(ecmFileInfoDTO.getFileId()))
        );
    }

    /**
     * 更新 RemakeStatus 指定位置的值
     * @param status   原始状态字符串
     * @param position 需要更新的位置
     * @param newValue
     * @return 更新后的状态字符串
     */
    private String updateStatus(String status, Integer position, char newValue) {
        if (position < 1 || position > IcmsConstants.LENGTH) {
            throw new IllegalArgumentException(
                    String.format("只能更新 1 到 %d 位", IcmsConstants.LENGTH)
            );
        }
        StringBuilder sb = new StringBuilder(status);
        sb.setCharAt(position - 1, newValue);
        return sb.toString();

    }

    private String executePostRequest(String url, JSONObject requestBody) throws Exception {
        String content = "";
        CloseableHttpClient httpClient = CommonHttpClientUtils.createHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8));
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            if (response == null || response.getEntity() == null) {
                return null;
            }
            // 获取响应内容
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            // 解析JSON响应
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            // 获取result对象
            JSONObject result = jsonResponse.getJSONObject("result");
            // 获取ocrResults数组
            JSONArray ocrResults = result.getJSONArray("ocrResults");

            // 遍历OCR结果
            List<String> allRecTexts = new ArrayList<>();
            for (int i = 0; i < ocrResults.size(); i++) {
                JSONObject ocrResult = ocrResults.getJSONObject(i);
                JSONObject prunedResult = ocrResult.getJSONObject("prunedResult");

                // 获取rec_texts数组
                JSONArray recTexts = prunedResult.getJSONArray("rec_texts");
                log.info("当前ocr识别出来的为：{}",recTexts);

                // 将rec_texts转换为List<String>
                for (int j = 0; j < recTexts.size(); j++) {
                    allRecTexts.add(recTexts.getString(j));
                }
                content = allRecTexts.stream().collect(Collectors.joining("\n"));
            }
            log.info("当前ocr转换后的为：{}",content);
            return content;
        }
    }

    private JSONObject createOcrRequestBody(String fileId) {
        JSONObject requestBody = new JSONObject();
        String fileUrl = fileFullPath+"/storage/deal/getFileByFileId?fileId="+fileId;
        requestBody.put("file", fileUrl);
        requestBody.put("fileType", 1);
        return requestBody;
    }

    public void getFileInfoByBytesSync(JSONObject jsonObject,String indexName,String id,String fileBytes){
        getFileInfoByBytes(jsonObject,indexName,id,fileBytes);
    }
}
