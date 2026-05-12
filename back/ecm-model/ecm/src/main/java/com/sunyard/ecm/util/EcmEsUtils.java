package com.sunyard.ecm.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author zhouleibin
 * @date 2021/11/5 16:46
 * @Desc
 */
@Slf4j
@Component
public class EcmEsUtils {

    @Resource
    private RestHighLevelClient restHighLevelClient;
    @Resource
    private EsAsysUtils esAsysUtils;

    /**
     * 添加文本文件，走管道
     *
     * @param pipeline 管道
     * @param id id
     * @param file 文件对象
     * @param indexName 索引名称
     * @return Result
     */
    public boolean indexPipeline(String pipeline, String id, Object file, String indexName,boolean flag) {
        try {
            IndexRequest indexRequest = new IndexRequest(indexName).id(id);
            String filestr = JSON.toJSONString(file);
            JSONObject jsonObject = JSON.parseObject(filestr);
            String fileBytes = jsonObject.getString("fileBytes");
            jsonObject.remove("fileBytes");
            jsonObject.remove("content");
            indexRequest.source(jsonObject.toJSONString(), XContentType.JSON);
            /*indexRequest.setPipeline(pipeline);*/
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            // 用pdfbox工具解析文件代替pipeline
            if (flag){
                esAsysUtils.getFileInfoByBytesSync(jsonObject,indexName,id,fileBytes);
            }else {
                Future<Void> future = esAsysUtils.getFileInfoByBytes(jsonObject, indexName, id, fileBytes);
                future.get();
            }
            log.info("indexResponse : {}", indexResponse);
            return indexResponse != null;
        } catch (Exception e) {
            log.error("elasticsearch indexPipeline:{}", e.getMessage(), e);
            throw new SunyardException(ResultCode.SYSTEM_BUSY_ERROR, e.toString());
        }
    }

    public String getFileContext(byte[] bytes, String fileName){
        String fileContext = "";
        try {
            // 将获取到的bytes解码
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
            fileContext = esAsysUtils.getFileContext(tempFile, extension);
        }catch (Exception e){
            log.error("获取文本信息失败:{}", e.getMessage(), e);
        }
        return fileContext;
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
            log.error("elasticsearch indexBase:{}", e.getMessage(), e);
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
            log.error(e.toString());
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
                log.error(e.toString());
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
                log.error(e.toString());
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
                log.error(e.toString());
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 数量统计
     *
     * @param indexName
     * @return
     */
    public long count(QueryBuilder queryBuilder, String indexName) {
        try {
            log.debug("queryBuilder: {}", queryBuilder);
            CountRequest countRequest = new CountRequest();
            if (null != indexName) {
                countRequest.indices(indexName);
            }

            countRequest.query(queryBuilder);
            CountResponse countResponse = restHighLevelClient.count(countRequest,
                    RequestOptions.DEFAULT);
            long count = countResponse.getCount();
            return count;
        } catch (Exception e) {
            log.error("elasticsearch count错误:{}", e.getMessage(), e);
            return 0;
        }
    }

}
