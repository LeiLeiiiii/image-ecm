package com.sunyard.mytool.until;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunyard.mytool.mapper.es.EsEcmFileMapper;
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
import java.util.*;
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

    public void getFileInfoByBytesSync(JSONObject jsonObject,String indexName,String id,String fileBytes){
        getFileInfoByBytes(jsonObject,indexName,id,fileBytes);
    }
}
