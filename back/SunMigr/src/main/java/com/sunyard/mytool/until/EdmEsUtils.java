package com.sunyard.mytool.until;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;


@Slf4j
@Component
public class EdmEsUtils {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /***/
    public boolean indexBase(String index, String id, Map<String, Object> jsonMap) {
        try {
            IndexRequest indexRequest = new IndexRequest(index);
            if (null != id) {
                indexRequest = new IndexRequest(index).id(id);
            }
            indexRequest.source(jsonMap);
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest,
                    RequestOptions.DEFAULT);
            log.info("indexResponse : {}", indexResponse);
            return indexResponse != null;
        } catch (IOException e) {
            log.error("elasticsearch indexBase错误:{}", e.getMessage(), e);
            throw new RuntimeException("elasticsearch indexBase错误",e);
        }
    }

    /***/
    public boolean indexFullTextFile(String indexName, String id, Object file) {
        try {
            IndexRequest indexRequest = new IndexRequest(indexName).id(id);
            String filestr = JSON.toJSONString(file);
            JSONObject jsonObject = JSON.parseObject(filestr);
            // 用Tika工具解析文件代替pipeline
            getFileInfoByBytes(jsonObject);
            indexRequest.source(jsonObject.toJSONString(), XContentType.JSON);
            //indexRequest.setPipeline("attachment");
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest,
                    RequestOptions.DEFAULT);
            log.info("indexResponse : {}", indexResponse);
            return indexResponse != null;
        } catch (Exception e) {
            log.error("elasticsearch indexFullTextFile错误:{}", e.getMessage(), e);
            throw new RuntimeException("elasticsearch indexFullTextFile错误",e);
        }
    }

    /**
     * 用tika解析文件代替管道
     *
     * @param jsonObject json格式文件对象
     */
    private void getFileInfoByBytes(JSONObject jsonObject) {
        // 获取文件内容
        String content = jsonObject.getString("content");
        if (content != null && !"".equals(content)) {
            try {
                String fileName = jsonObject.getString("docName");
                // 将获取到的bytes解码
                byte[] bytes = Base64.getDecoder().decode(content.getBytes());
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
                log.error("elasticsearch getFileInfoByBytes:{}", e.getMessage(), e);
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
    private void getAttachmentInfo(JSONObject jsonObject, File tempFile,String ext) {
        String context;
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
                default:
                    context = "";
                    break;
            }
        } catch (Exception e) {
            context="";
            log.info("解析文件失败",e);  //todo 记录数据库
        }
        JSONObject attachment=new JSONObject();
        attachment.put("date",new Date());
        attachment.put("contentType",ext);
        attachment.put("author","");
        attachment.put("language","");
        attachment.put("content",context);
        attachment.put("contentLength","");
        jsonObject.put("attachment",attachment);
        jsonObject.remove("content");
        log.debug("获取到的文件信息为:   "+attachment.toJSONString());
    }


    /**
     * 删除指定索引下指定文档数据
     *
     * @param indexName
     * @param id
     * @return
     */
    public boolean delFullTextFile(String indexName, String id) {
        try {
            DeleteRequest deleteRequest = new DeleteRequest(indexName).id(id);
            DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest,
                    RequestOptions.DEFAULT);
            log.info("deleteResponse : {}", deleteResponse);
            return deleteResponse != null;
        } catch (Exception e) {
            log.error("elasticsearch delFullTextFile错误:{}", e.getMessage(), e);
            throw new RuntimeException("elasticsearch delFullTextFile错误", e);
        }
    }

    /**
     * 删除指定索引下的文档信息
     *
     * @param indexName
     * @return
     */
    public boolean delAllFullTextFile(String indexName) {
        try {
            DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);
            request.setQuery(QueryBuilders.matchAllQuery());
            BulkByScrollResponse response = restHighLevelClient.deleteByQuery(request,
                    RequestOptions.DEFAULT);
            log.info("response : {}", response);
            return response != null;
        } catch (Exception e) {
            log.error("elasticsearch delAllFullTextFile错误:{}", e.getMessage(), e);
            throw new RuntimeException("elasticsearch delAllFullTextFile错误", e);
        }
    }

    /***/
    public boolean updateByQuery(UpdateByQueryRequest updateByQuery) {
        try {
            BulkByScrollResponse response = restHighLevelClient.updateByQuery(updateByQuery,
                    RequestOptions.DEFAULT);
            log.info("BulkByScrollResponse : {}", response);
            return response.getStatus().getUpdated() > 0 ? true : false;
        } catch (IOException e) {
            log.error("elasticsearch updateByQuery错误:{}", e.getMessage(), e);
            return false;
        }

    }

    /***/
    public <T> T select(SearchSourceBuilder searchSourceBuilder, Class<T> classz) {
        try {
            log.debug("searchSourceBuilder: {}", searchSourceBuilder);
            // 获取类注解
            Document documentAnnotation = classz.getAnnotation(Document.class);
            String indexName = documentAnnotation.indexName();
            SearchRequest searchRequest = new SearchRequest();
            if (null != indexName) {
                searchRequest.indices(indexName);
            }
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest,
                    RequestOptions.DEFAULT);
            SearchHit[] hits = searchResponse.getHits().getHits();
            T t = null;
            for (SearchHit hit : hits) {
                t = JSONObject.parseObject(JSONObject.toJSONString(hit.getSourceAsMap()), classz);
            }
            return t;
        } catch (Exception e) {
            log.error("elasticsearch select错误:{}", e.getMessage(), e);
            throw new RuntimeException("elasticsearch select错误", e);
        }
    }

    /***/
    public <T> List searchAll(SearchSourceBuilder searchSourceBuilder, String indexName) {
        try {
            // 获取类注解
//            Document documentAnnotation = classz.getAnnotation(Document.class);
//            String indexName = documentAnnotation.indexName();
            SearchRequest searchRequest = new SearchRequest();
            if (null != indexName) {
                searchRequest.indices(indexName);
            }
            Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1));
            searchRequest.scroll(scroll);
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = restHighLevelClient.search(searchRequest,
                    RequestOptions.DEFAULT);
            String scrollId = searchResponse.getScrollId();
            SearchHit[] hits = searchResponse.getHits().getHits();
            List list = new ArrayList<>();
            while (!ObjectUtils.isEmpty(hits)) {
                for (SearchHit hit : hits) {
                    Map<String, Object> sourceMap = hit.getSourceAsMap();
                    for (Map.Entry<String, HighlightField> entry : hit.getHighlightFields()
                            .entrySet()) {
                        String key = entry.getKey();
                        if (sourceMap.containsKey(key)) {
                            Text[] fragments = entry.getValue().getFragments();
                            sourceMap.put(key, transTextArrayToString(fragments));
                        }
                        if (entry.getKey().contains("attachment")) {
                            String attachmentKey = entry.getKey().split("\\.")[1];
                            Text[] fragments = entry.getValue().fragments();
                            Map<String, Object> attachmentMap = (Map<String, Object>) sourceMap
                                    .get("attachment");
                            attachmentMap.put(attachmentKey, transTextArrayToString(fragments));
                        }
                    }
                    list.add(hit.getSourceAsMap());
                }
                SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId);
                searchScrollRequest.scroll(scroll);
                SearchResponse searchScrollResponse = restHighLevelClient
                        .scroll(searchScrollRequest, RequestOptions.DEFAULT);
                scrollId = searchScrollResponse.getScrollId();
                hits = searchScrollResponse.getHits().getHits();
            }
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            return list;
        } catch (Exception e) {
            log.error("elasticsearch searchAll错误:{}", e.getMessage(), e);
            throw new RuntimeException("elasticsearch searchAll错误", e);
        }
    }

    private String transTextArrayToString(Text[] fragments) {
        if (null == fragments) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for (Text fragment : fragments) {
            buffer.append(fragment.string());
        }
        return buffer.toString();
    }

    /**
     * 数量统计
     *
     * @param classz
     * @param <T>
     * @return
     */
    public <T> long count(QueryBuilder queryBuilder, Class<T> classz) {
        try {
            log.debug("queryBuilder: {}", queryBuilder);
            // 获取类注解
            Document documentAnnotation = classz.getAnnotation(Document.class);
            String indexName = documentAnnotation.indexName();
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

    /*
    public <T> String createIndex(Object obj, Class<T> classz) {
        // 获取类注解
        Document documentAnnotation = classz.getAnnotation(Document.class);
        String indexName = documentAnnotation.indexName();
        try {
            Field[] fields = classz.getDeclaredFields();
            String id = null;
            for (Field field : fields) {
                Id idAnnotation = field.getAnnotation(Id.class);
                if (null != idAnnotation) {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (null != value) {
                        id = value.toString();
                        break;
                    }
                }
            }
            IndexRequest indexRequest = new IndexRequest(indexName);
            if (null != id) {
                indexRequest.id(id);
            }
            indexRequest.source(JSON.toJSONString(obj), XContentType.JSON);
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest,
                    RequestOptions.DEFAULT);
            return indexResponse.getId();
        } catch (Exception e) {
            log.error("elasticsearch createIndex错误:{}", e.toString());
            throw new InBizException(ResultCode.SYSTEM_ERROR, "elasticsearch createIndex错误");
        }
    }
    */

    //==========================================索引操作=================================================
    /**
     * 方法描述: 创建索引,若索引不存在且创建成功,返回true,若同名索引已存在,返回false
     *
     * @param: [index] 索引名称
     * @return: boolean
     * @author: gxf
     * @date: 2021年07月27日
     * @time: 11:01 上午
     */
    /*
    public static boolean insertIndex(String index) {
     //创建索引请求
     CreateIndexRequest request = new CreateIndexRequest(index);
     //执行创建请求IndicesClient,请求后获得响应
     try {
         CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
         return response != null;
     } catch (Exception e) {
         e.printStackTrace();
     }
     return false;
    }
    
    
    *//**
     * 方法描述: 判断索引是否存在,若存在返回true,若不存在或出现问题返回false
     *
     * @param: [index] 索引名称
     * @return: boolean
     * @author: gxf
     * @date: 2021年07月27日
     * @time: 11:09 上午
     */
    /*
    public static boolean isExitsIndex(String index) {
     GetIndexRequest request = new GetIndexRequest(index);
     try {
         return client.indices().exists(request, RequestOptions.DEFAULT);
     } catch (Exception e) {
         e.printStackTrace();
     }
     return false;
    }
    
    
    *//*
     * 方法描述: 删除索引,删除成功返回true,删除失败返回false
     * @param: [index] 索引名称
     * @return: boolean
     * @author: gxf
     * @date: 2021年07月27日
     * @time: 11:23 上午
     *//*
                               public static boolean deleteIndex(String index) {
                                DeleteIndexRequest request = new DeleteIndexRequest(index);
                                try {
                                    AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
                                    return response.isAcknowledged();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return false;
                               }
                        
                        
                               //==========================================文档操作(新增,删除,修改)=================================================
                        
                               *//**
     * 方法描述: 新增/修改文档信息
     *
     * @param index 索引
     * @param id    文档id
     * @param data  数据
     * @return: boolean
     * @author: gxf
     * @date: 2021年07月27日
     * @time: 10:34 上午
     */
    /*
    public static boolean insertOrUpdateDocument(String index, String id, Object data) {
     try {
         IndexRequest request = new IndexRequest(index);
         request.timeout(TIME_VALUE_SECONDS);
         if (id != null && id.length() > 0) {
             request.id(id);
         }
         request.source(JSON.toJSONString(data), XContentType.JSON);
         IndexResponse response = client.index(request, RequestOptions.DEFAULT);
         String status = response.status().toString();
         if (RESPONSE_STATUS_CREATED.equals(status) || RESPONSE_STATUS_OK.equals(status)) {
             return true;
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
     return false;
    }
    */

    /**
     * 方法描述: 更新文档信息
     *
     * @param index 索引
     * @param id    文档id
     * @param data  数据
     * @return: boolean
     * @author: gxf
     * @date: 2021年07月27日
     * @time: 10:34 上午
     */
    /*
    public static boolean updateDocument(String index, String id, Object data) {
     try {
         UpdateRequest request = new UpdateRequest(index, id);
         request.doc(JSON.toJSONString(data), XContentType.JSON);
         UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
         String status = response.status().toString();
         if (RESPONSE_STATUS_OK.equals(status)) {
             return true;
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
     return false;
    }
    
    *//**
     * 方法描述:删除文档信息
     *
     * @param index 索引
     * @param id    文档id
     * @return: boolean
     * @author: gxf
     * @date: 2021年07月27日
     * @time: 10:33 上午
     */
    /*
    public static boolean deleteDocument(String index, String id) {
     try {
         DeleteRequest request = new DeleteRequest(index, id);
         DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
         String status = response.status().toString();
         if (RESPONSE_STATUS_OK.equals(status)) {
             return true;
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
     return false;
    }
    
    
    *//**
     * 方法描述: 小数据量批量新增
     *
     * @param index    索引
     * @param dataList 数据集 新增修改需要传递
     * @param timeout  超时时间 单位为秒
     * @return: boolean
     * @author: gxf
     * @date: 2021年07月27日
     * @time: 10:31 上午
     */
    /*
    public static boolean simplePatchInsert(String index, List<Object> dataList, long timeout) {
     try {
         BulkRequest bulkRequest = new BulkRequest();
         bulkRequest.timeout(TimeValue.timeValueSeconds(timeout));
         if (dataList != null && dataList.size() > 0) {
             for (Object obj : dataList) {
                 bulkRequest.add(
                         new IndexRequest(index)
                                 .source(JSON.toJSONString(obj), XContentType.JSON)
                 );
             }
             BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
             if (!response.hasFailures()) {
                 return true;
             }
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
     return false;
    }
    
    *//**
     * 功能描述:
     * @param index 索引名称
     * @param idList 需要批量删除的id集合
     * @return : boolean
     * @author : gxf
     * @date : 2021/6/30 1:22
     */
    /*
    public static boolean patchDelete(String index, List<String> idList) {
     BulkRequest request = new BulkRequest();
     for (String id:idList) {
         request.add(new DeleteRequest().index(index).id(id));
     }
     try {
         BulkResponse response = EsUtil.client.bulk(request, RequestOptions.DEFAULT);
         return !response.hasFailures();
     } catch (Exception e) {
         e.printStackTrace();
     }
     return false;
    }
    
    
    //==========================================文档操作(查询)=================================================
    
    *//**
     * 方法描述: 判断文档是否存在
     *
     * @param index 索引
     * @param id    文档id
     * @return: boolean
     * @author: gxf
     * @date: 2021年07月27日
     * @time: 10:36 上午
     */
    /*
    public static boolean isExistsDocument(String index, String id) {
     return isExistsDocument(index, DEFAULT_TYPE, id);
    }
    
    
    *//**
     * 方法描述: 判断文档是否存在
     *
     * @param index 索引
     * @param type  类型
     * @param id    文档id
     * @return: boolean
     * @author: gxf
     * @date: 2021年07月27日
     * @time: 10:36 上午
     */
    public boolean isExistsDocument(String index, String type, String id) {
        GetRequest request = new GetRequest(index, type, id);
        try {
            GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
            return response.isExists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /*
    public static boolean isExistsDocument(String index, String type, String id) {
     GetRequest request = new GetRequest(index, type, id);
     try {
         GetResponse response = client.get(request, RequestOptions.DEFAULT);
         return response.isExists();
     } catch (Exception e) {
         e.printStackTrace();
     }
     return false;
    }
    
    
    *//**
     * 方法描述: 根据id查询文档
     *
     * @param index 索引
     * @param id    文档id
     * @param clazz 转换目标Class对象
     * @return 对象
     * @author: gxf
     * @date: 2021年07月27日
     * @time: 10:36 上午
     */
    /*
    public static <T> T selectDocumentById(String index, String id, Class<T> clazz) {
     return selectDocumentById(index, DEFAULT_TYPE, id, clazz);
    }
    
    
    *//**
     * 方法描述: 根据id查询文档
     *
     * @param index 索引
     * @param type  类型
     * @param id    文档id
     * @param clazz 转换目标Class对象
     * @return 对象
     * @author: gxf
     * @date: 2021年07月27日
     * @time: 10:35 上午
     */
    /*
    public static <T> T selectDocumentById(String index, String type, String id, Class<T> clazz) {
     try {
         type = type == null || type.equals("") ? DEFAULT_TYPE : type;
         GetRequest request = new GetRequest(index, type, id);
         GetResponse response = client.get(request, RequestOptions.DEFAULT);
         if (response.isExists()) {
             Map<String, Object> sourceAsMap = response.getSourceAsMap();
             return dealObject(sourceAsMap, clazz);
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
     return null;
    }
    
    
    *//**
     * 方法描述:（筛选条件）获取数据集合
     *
     * @param index         索引
     * @param sourceBuilder 请求条件
     * @param clazz         转换目标Class对象
     * @return: java.util.List<T>
     * @author: gxf
     * @date: 2021年07月27日
     * @time: 10:35 上午
     */
    /*
    public static <T> List<T> selectDocumentList(String index, SearchSourceBuilder sourceBuilder, Class<T> clazz) {
     try {
         SearchRequest request = new SearchRequest(index);
         if (sourceBuilder != null) {
             // 返回实际命中数
             sourceBuilder.trackTotalHits(true);
             request.source(sourceBuilder);
         }
         SearchResponse response = client.search(request, RequestOptions.DEFAULT);
         if (response.getHits() != null) {
             List<T> list = new ArrayList<>();
             SearchHit[] hits = response.getHits().getHits();
             for (SearchHit documentFields : hits) {
                 Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
                 list.add(dealObject(sourceAsMap, clazz));
             }
             return list;
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
     return null;
    }
    
    
    *//**
     * 方法描述:（筛选条件）获取数据
     *
     * @param index         索引
     * @param sourceBuilder 请求条
     * @return: java.util.List<T>
     * @author: gxf
     * @date: 2021年07月27日
     * @time: 10:35 上午
     */
    /*
    public static SearchResponse selectDocument(String index, SearchSourceBuilder sourceBuilder) {
     try {
         SearchRequest request = new SearchRequest(index);
         if (sourceBuilder != null) {
             // 返回实际命中数
             sourceBuilder.trackTotalHits(true);
             sourceBuilder.size(10000);
             request.source(sourceBuilder);
         }
         return client.search(request, RequestOptions.DEFAULT);
     } catch (Exception e) {
         e.printStackTrace();
     }
     return null;
    }
    
    
    *//**
     * 方法描述: 筛选查询,返回使用了<span style='color:red'></span>处理好的数据.
     *
     * @param: index 索引名称
     * @param: sourceBuilder sourceBuilder对象
     * @param: clazz 需要返回的对象类型.class
     * @param: highLight 需要表现的高亮匹配字段
     * @return: java.util.List<T>
     * @author: gxf
     * @date: 2021年07月27日
     * @time: 6:39 下午
     */
    /*
    public static <T> List<T> selectDocumentListHighLight(String index, SearchSourceBuilder sourceBuilder, Class<T> clazz, String highLight) {
     try {
         SearchRequest request = new SearchRequest(index);
         if (sourceBuilder != null) {
             // 返回实际命中数
             sourceBuilder.trackTotalHits(true);
             //高亮
             HighlightBuilder highlightBuilder = new HighlightBuilder();
             highlightBuilder.field(highLight);
             highlightBuilder.requireFieldMatch(false);//多个高亮关闭
             highlightBuilder.preTags("<span style='color:red'>");
             highlightBuilder.postTags("</span>");
             sourceBuilder.highlighter(highlightBuilder);
             request.source(sourceBuilder);
         }
         SearchResponse response = client.search(request, RequestOptions.DEFAULT);
         if (response.getHits() != null) {
             List<T> list = new ArrayList<>();
             for (SearchHit documentFields : response.getHits().getHits()) {
                 Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
                 HighlightField title = highlightFields.get(highLight);
                 Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
                 if (title != null) {
                     Text[] fragments = title.fragments();
                     String n_title = "";
                     for (Text fragment : fragments) {
                         n_title += fragment;
                     }
                     sourceAsMap.put(highLight, n_title);//高亮替换原来的内容
                 }
                 list.add(dealObject(sourceAsMap, clazz));
             }
             return list;
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
     return null;
    }
    
    *//**
     * 方法描述: 返回索引内所有内容,返回SearchResponse对象,需要自己解析,不对数据封装
     * @param: index 索引名称
     * @return: SearchResponse
     * @author: gxf
     * @date: 2021/6/30
     * @time: 1:28 上午
     */
    /*
    public static SearchResponse queryAllData(String index){
     //创建搜索请求对象
     SearchRequest request = new SearchRequest(index);
     //构建查询的请求体
     SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
     //查询所有数据
     sourceBuilder.query(QueryBuilders.matchAllQuery());
     request.source(sourceBuilder);
     try {
         return client.search(request, RequestOptions.DEFAULT);
     } catch (IOException e) {
         e.printStackTrace();
     }
     return null;
    }
    
    
    *//**
     * 方法描述: 返回索引内所有内容,返回指定类型
     * @param: index 索引名称
     * @param: clazz 需要接受转换的对象类型
     * @return: java.util.List<T>
     * @author: gxf
     * @date: 2021/6/30
     * @time: 1:32 上午
     *//*
                               public static <T> List<T> queryAllData(String index, Class<T> clazz){
                                //创建搜索请求对象
                                SearchRequest request = new SearchRequest(index);
                                //构建查询的请求体
                                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                                //查询所有数据
                                sourceBuilder.query(QueryBuilders.matchAllQuery());
                                request.source(sourceBuilder);
                                try {
                                    SearchResponse response = client.search(request, RequestOptions.DEFAULT);
                                    if (response.getHits() != null) {
                                        List<T> list = new ArrayList<>();
                                        SearchHit[] hits = response.getHits().getHits();
                                        for (SearchHit documentFields : hits) {
                                            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
                                            list.add(dealObject(sourceAsMap, clazz));
                                        }
                                        return list;
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                               }*/

}
