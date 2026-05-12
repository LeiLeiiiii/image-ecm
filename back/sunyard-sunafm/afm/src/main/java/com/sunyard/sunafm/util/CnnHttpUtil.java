package com.sunyard.sunafm.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.sunafm.po.AfmServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * python请求类
 */
@Slf4j
public class CnnHttpUtil {

    /**
     * 存特征统一掉用
     * @param url
     * @param param
     * @param file
     * @param server
     * @return
     */
    public static String saveFeature(String url, List<Map<String, Object>> param, List<MultipartFile> file, AfmServer server) {

        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            HttpPost uploadFile = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for(int i=0;i<file.size();i++){
                builder.addBinaryBody(
                        "files",
                        file.get(i).getInputStream(),
                        ContentType.APPLICATION_OCTET_STREAM,
                        file.get(i).getOriginalFilename()
                );
            }
            builder.addTextBody("params", JSONArray.toJSONString(param));
            builder.addTextBody("server", JSONArray.toJSONString(server));
            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);
            try (CloseableHttpResponse response = httpClient.execute(uploadFile)) {
                System.out.println(response.getStatusLine());
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    String result = EntityUtils.toString(responseEntity);
                    System.out.println(result);
                    return result;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
    /**
     * 字节转为file
     * @return
     * @throws IOException
     */
    public static File multipartFileToFile(String fileName,byte[] bytes) {
        String originalFilename = fileName;
        // 找到文件名中最后一个点的位置
        int lastIndex = originalFilename.lastIndexOf('.');

        // 提取文件的原始名称（不包含扩展名）
        String filenameWithoutExtension = originalFilename.substring(0, lastIndex);
        String extension = originalFilename.substring(lastIndex);
        File tempFile = null;
        try {
            tempFile = Files.createTempFile(filenameWithoutExtension, extension).toFile();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(bytes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return tempFile;

    }


    public static String getHttp(String url, String s) {
        String cnnStr;
        log.info("cnn:{}",s);
        HttpEntity execute = CommonHttpRequest
                .post(url)
                .body(s)
                .execute();
        cnnStr = CommonHttpRequest.getResponseString(execute);
        return cnnStr;
    }


    /**
     * 返回值处理
     */
    public static class CnnRetHandle {
        public final JSONObject jsonObject1;
        public final Boolean succ;

        public CnnRetHandle(JSONObject jsonObject1, Boolean succ) {
            this.jsonObject1 = jsonObject1;
            this.succ = succ;
        }
    }

    public static CnnHttpUtil.CnnRetHandle getCnnRetHandle(String cnnStr) {
        AssertUtils.isNull(cnnStr, "服务器连接失败，请稍后重试");
        JSONObject jsonObject1 = null;
        try {
            jsonObject1 = JSONObject.parseObject(cnnStr);
        } catch (Exception e) {
            AssertUtils.isNull(cnnStr, "服务器连接失败，请稍后重试");
        }
        AssertUtils.isNull(jsonObject1, "服务器连接失败，请稍后重试");
        Boolean succ = jsonObject1.getBoolean("succ");
        if (succ == null) {
            AssertUtils.isTrue(true, cnnStr);
        }
        CnnHttpUtil.CnnRetHandle result = new CnnHttpUtil.CnnRetHandle(jsonObject1, succ);
        return result;
    }

}
