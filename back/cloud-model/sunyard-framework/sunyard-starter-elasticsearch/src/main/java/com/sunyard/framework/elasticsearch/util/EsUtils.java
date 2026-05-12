package com.sunyard.framework.elasticsearch.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.ResultCode;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouleibin
 * @date 2021/11/5 16:46
 * @Desc
 */
@Slf4j
@Component
public class EsUtils {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 添加文本文件，走管道
     *
     * @param pipeline 管道
     * @param id id
     * @param file 文件对象
     * @param indexName 索引名称
     * @return Result
     */
    public boolean indexPipeline(String pipeline, String id, Object file, String indexName) {
        try {
            IndexRequest indexRequest = new IndexRequest(indexName).id(id);
            String filestr = JSON.toJSONString(file);
            JSONObject jsonObject = JSON.parseObject(filestr);
            // 用pdfbox工具解析文件代替pipeline
            getFileInfoByBytes(jsonObject);
            indexRequest.source(jsonObject.toJSONString(), XContentType.JSON);
            /*indexRequest.setPipeline(pipeline);*/
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            log.info("indexResponse : {}", indexResponse);
            return indexResponse != null;
        } catch (Exception e) {
            log.error("elasticsearch indexPipeline", e);
            throw new SunyardException(ResultCode.SYSTEM_BUSY_ERROR, e.toString());
        }
    }

    /**
     * 用pdfbox解析文件代替管道
     *
     * @param jsonObject json格式文件对象
     */
    private void getFileInfoByBytes(JSONObject jsonObject) {
        // 获取文件byte[]
        String fileBytes = jsonObject.getString("fileBytes");
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
            } catch (Exception e) {
                log.error("elasticsearch getFileInfoByBytes", e);
            }
        } else {
            log.info("============没有获取到文件bytes============");
        }

    }

    /**
     * 获取文件信息并存入json
     *
     * @param jsonObject json格式文件对象
     * @param tempFile 文件对象
     */
    private void getAttachmentInfo(JSONObject jsonObject, File tempFile,String ext) throws IOException {
        //PdfBox解析文件信息
        String context;
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
            default:
                context = "";
                break;
        }
        JSONObject attachment=new JSONObject();
        attachment.put("date",new Date());
        attachment.put("contentType",ext);
        attachment.put("author","");
        attachment.put("language","");
        attachment.put("content",context);
        attachment.put("contentLength","");
        jsonObject.put("attachment",attachment);
        jsonObject.remove("fileBytes");
        log.debug("获取到的文件信息为:   "+attachment.toJSONString());
    }

    /**
     * 添加索引记录
     *
     * @param id id
     * @param obj 文件对象
     * @param indexName 索引名称
     * @return Result
     */
    public boolean indexObj(String id, Object obj, String indexName) {
        try {
            IndexRequest indexRequest = new IndexRequest(indexName);
            if (null != id) {
                indexRequest = new IndexRequest(indexName).id(id);
            }
            String filestr = JSON.toJSONString(obj);
            indexRequest.source(filestr, XContentType.JSON);
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            log.info("indexResponse : {}", indexResponse);
            return indexResponse != null;
        } catch (IOException e) {
            log.error("elasticsearch indexBase",e);
            throw new SunyardException(ResultCode.SYSTEM_BUSY_ERROR, e.toString());
        }
    }

    /**
     * 方法描述: 判断文档是否存在
     *
     * @param index 索引
     * @param type 类型
     * @param id 文档id
     * @return Result: boolean
     */
    public boolean isExistsDocument(String index, String type, String id) {
        GetRequest request = new GetRequest(index, id);
        try {
            GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
            return response.isExists();
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置高亮属性
     *
     * @param highlightFields 高亮字段
     * @param entity 对象
     * @throws IllegalAccessException 异常
     */
    public <T> void setNestedHighlightFieldValues(Map<String, HighlightField> highlightFields, T entity) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(entity);

        for (Map.Entry<String, HighlightField> entry : highlightFields.entrySet()) {
            String fieldName = entry.getKey();
            HighlightField highlightField = entry.getValue();

            String[] nestedFields = fieldName.split("\\.");
            if (nestedFields.length > 1) {
                Object nestedObject = getNestedObject(entity, nestedFields);
                if (nestedObject != null) {
                    String finalFieldName = nestedFields[nestedFields.length - 1];
                    setNestedField(nestedObject, finalFieldName, getHighlightedValue(highlightField));
                }
            } else {
                Field field = getField(entity.getClass(), fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    beanWrapper.setPropertyValue(fieldName, getHighlightedValue(highlightField));
                }
            }
        }
    }

    /**
     * 获取高亮值
     * 
     * @param highlightField 高亮属性
     * @return Object
     */
    private Object getHighlightedValue(HighlightField highlightField) {
        Text[] fragments = highlightField.fragments();
        if (fragments != null && fragments.length > 0) {
            return fragments[0].string();
        }
        return null;
    }

    /**
     * 获取字段
     * 
     * @param clazz class对象
     * @param fieldName 字段名
     * @return T
     */
    private <T> Field getField(Class<T> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            try {
                return clazz.getSuperclass().getDeclaredField(fieldName);
            } catch (NoSuchFieldException x) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 获取对象对应的值
     * 
     * @param entity 实体类
     * @param nestedFields 属性集
     * @return Object
     */
    private <T> Object getNestedObject(T entity, String[] nestedFields) {
        Object nestedObject = entity;
        for (int i = 0; i < nestedFields.length - 1; i++) {
            String field = nestedFields[i];
            Field nestedField = getField(nestedObject.getClass(), field);
            nestedField.setAccessible(true);
            try {
                nestedObject = nestedField.get(nestedObject);
            } catch (IllegalAccessException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        }
        return nestedObject;
    }

    /**
     * 设置对象对应的值
     * 
     * @param nestedObject 属性集
     * @param fieldName 属性名
     * @param value 值
     */
    private void setNestedField(Object nestedObject, String fieldName, Object value) {
        Field field = getField(nestedObject.getClass(), fieldName);
        if (field != null) {
            field.setAccessible(true);
            try {
                field.set(nestedObject, value);
            } catch (IllegalAccessException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        }
    }

}
