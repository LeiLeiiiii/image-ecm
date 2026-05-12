package com.sunyard.edm.service;

import com.github.pagehelper.PageInfo;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.constant.DocDictionaryKeyConstants;
import com.sunyard.edm.constant.DocElasticsearchQueryTypeConstants;
import com.sunyard.edm.dto.DocFullTextFileDTO;
import com.sunyard.edm.es.EdmEsUtils;
import com.sunyard.edm.mapper.DocBsDocumentMapper;
import com.sunyard.edm.mapper.DocSysHouseMapper;
import com.sunyard.edm.mapper.DocUploadRecordMapper;
import com.sunyard.edm.po.DocBsDocument;
import com.sunyard.edm.po.DocSysHouse;
import com.sunyard.edm.po.DocUploadRecord;
import com.sunyard.edm.vo.AddOrUpdateDocumentVO;
import com.sunyard.edm.vo.DocBsDocumentSearchVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.date.DateUtils;
import com.sunyard.module.storage.api.FileHandleApi;
import com.sunyard.module.storage.vo.FileByteVO;
import com.sunyard.module.system.api.DictionaryApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysUserDTO;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author huronghao
 * @Type
 * @Desc  文档中心-全文检索实现类
 * @DATE 2022-12-21 14:19
 */
@Service
public class CenterQueryService {
    @Value("${path.temp-path:/home/temp/tmp}")
    private String tempPath;
    @Value("${elasticsearch.indexName:document}")
    private String index;
    @Resource
    private EdmEsUtils esUtil;
    @Resource
    private DocSysHouseMapper docSysHouseMapper;
    @Resource
    private DocUploadRecordMapper docUploadRecordMapper;
    @Resource
    private DocBsDocumentMapper docBsDocumentMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private DictionaryApi sysDictionaryService;
    @Resource
    private CenterCommonService docCommonService;
    @Resource
    private FileHandleApi fileHandleApi;

    /**
     * 时间戳转化为时间格式。
     */
    public static String handleTimePoke(String time) {
        Long timeLong = Long.parseLong(time);
        //要转换的时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        try {
            date = sdf.parse(sdf.format(timeLong));
            return sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 文档检索 待调整。
     */
    public Result search(DocElasticsearchQueryTypeConstants type, DocBsDocumentSearchVO docVo, AccountToken token, PageForm pageForm) {
        AssertUtils.isNull(docVo.getKey(), "请输入搜索关键字");

        QueryBuilder query = getQueryBuilder(docVo, type);
//        TermQueryBuilder docName = QueryBuilders.termQuery("attachment.content", docVo.getKey());
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        for (String filed : type.getFieldList()) {
            //highlightBuilder.field(filed);
            if(filed.equals("attachment.content")){
                highlightBuilder.field(new HighlightBuilder.Field("attachment.content")
                        .highlighterType("fvh"));
            }else {
                highlightBuilder.field(filed);
            }
        }

        //多个高亮显示
        highlightBuilder.requireFieldMatch(true);
        highlightBuilder.preTags("<span style=\'color:blue\'>");
        highlightBuilder.postTags("</span>");
        //最大高亮分片数
        highlightBuilder.fragmentSize(200);
        //从第一个分片获取高亮片段
        highlightBuilder.numOfFragments(3);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(query);
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.timeout(TimeValue.timeValueMinutes(1L));
        searchSourceBuilder.sort("_score", SortOrder.DESC);
        searchSourceBuilder.from((pageForm.getPageNum() - 1) * pageForm.getPageSize());
        searchSourceBuilder.size(pageForm.getPageSize());
        List list = esUtil.searchByPage(searchSourceBuilder, index);
        long count = esUtil.count(query, index);
        //权限处理 文件路径处理
        handleAuthAndPath(list, token);
        PageInfo<Object> pageInfo = new PageInfo<>();
        pageInfo.setList(list);
        pageInfo.setPageNum(pageForm.getPageNum());
        pageInfo.setPageSize(pageForm.getPageSize());
        if(count>10000){
            pageInfo.setTotal(10000);
        }else {
            pageInfo.setTotal(count);
        }
        return Result.success(pageInfo);
    }

    /**
     * es 添加数据。
     */
    @Async("GlobalThreadPool")
    public void addFullTextPath(AddOrUpdateDocumentVO docBsDocumentExtend) {
        AssertUtils.isNull(docBsDocumentExtend.getBusId(), "id:文档id不能为空");
        AssertUtils.isNull(docBsDocumentExtend.getDocName(), "name:文档名不能为空");
        AssertUtils.isNull(docBsDocumentExtend.getDocSuffix(), "suffix:文档后缀不能为空");
        String name = docBsDocumentExtend.getDocName();
        DocUploadRecord docUploadRecord = new DocUploadRecord();
        docUploadRecord.setBusId(docBsDocumentExtend.getBusId());
        docUploadRecord.setIndexName(index);

        try {
            //调用存储服务的获取文件字节流接口
            FileByteVO fileByteVO = new FileByteVO();
            fileByteVO.setFileId(docBsDocumentExtend.getFileId());
            Result<byte[]> fileByteResult = fileHandleApi.getFileBytes(fileByteVO);
            byte[] bytes;
            if (!Objects.isNull(fileByteResult) &&fileByteResult.isSucc()){
                bytes = fileByteResult.getData();
            } else {
                bytes = null;
            }
            AssertUtils.isTrue(null == bytes, "获得源文件bytes失败");
            String base64FileContent = Base64.getEncoder().encodeToString(bytes);
            AssertUtils.isNull(base64FileContent, "base64FileContent:文件内容不能为空");
            Result<SysUserDTO> result = userApi.getUserByUserId(docBsDocumentExtend.getDocOwner());
            SysUserDTO sysUser = result.getData();
            //查询所有者
            Result<List<SysUserDTO>> userListByUserIds = userApi.getUserListByUserIds(new Long[]{docBsDocumentExtend.getDocOwner()});
            AssertUtils.isNull(userListByUserIds.getData(), "参数错误，存在未知用户！");
            DocFullTextFileDTO docFullTextFileDTO = new DocFullTextFileDTO();
            docFullTextFileDTO.setDocName(name);
            docFullTextFileDTO.setSuffix(docBsDocumentExtend.getDocSuffix());
            docFullTextFileDTO.setContent(base64FileContent);
            docFullTextFileDTO.setBusId(docBsDocumentExtend.getBusId());
            //标签
            docFullTextFileDTO.setTagIds(docBsDocumentExtend.getTagIds());
            docFullTextFileDTO.setDocOwnerStr(userListByUserIds.getData().get(0).getName());
            //附件名
            if (!CollectionUtils.isEmpty(docBsDocumentExtend.getAttchList())) {
                //获取所有附件名称
                List<String> attchNameList = docBsDocumentExtend.getAttchList().stream().map(DocBsDocument::getDocName).collect(Collectors.toList());
                docFullTextFileDTO.setAttchName(attchNameList);
            }
            if (ObjectUtils.isEmpty(docFullTextFileDTO.getCreatTime())) {
                docFullTextFileDTO.setCreatTime(new Date());
            }
            docFullTextFileDTO.setUpdateTime(new Date());
            esUtil.indexFullTextFile(index, docBsDocumentExtend.getBusId().toString(), docFullTextFileDTO);
            docUploadRecord.setIsSucceed(DocConstants.SUCCEED);
            docUploadRecordMapper.insert(docUploadRecord);
        } catch (Exception e) {
            docUploadRecord.setIsSucceed(DocConstants.FAIL);
            String exMsg = e.getMessage();
            docUploadRecord.setExceptionMsg(exMsg != null && exMsg.length() > 4000 ? exMsg.substring(0, 4000) : exMsg);
            docUploadRecordMapper.insert(docUploadRecord);
        }
    }

    /**
     * es 删除数据。
     */
    public void delFullText(String id) {
        AssertUtils.isNull(id, "id:文档id不能为空");
        esUtil.delFullTextFile(index, id);
    }

    /**
     * es 编辑数据。
     */
    public void updateFullText(AddOrUpdateDocumentVO vo) {
        for (Long id : vo.getBusIds()) {
            //要更新的内容
            HashMap<String, Object> params = new HashMap<>(DocConstants.SIXTEEN);
            params.put("tagIds", vo.getTagIds());
            //更新es里面的标签集合
            UpdateByQueryRequest updateByQueryRequest = new UpdateByQueryRequest(index);
            updateByQueryRequest.setQuery(QueryBuilders.termQuery("_id", id));
            updateByQueryRequest.setScript(new Script(ScriptType.INLINE, "painless", "ctx._source.tagIds = params.tagIds", params));
            esUtil.updateByQuery(updateByQueryRequest);
        }

    }

    /**
     * 得到分词查询的条件
     */
    private QueryBuilder getQueryBuilder(DocBsDocumentSearchVO docVo, DocElasticsearchQueryTypeConstants type) {
        QueryBuilder query = null;
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //key
        if (!ObjectUtils.isEmpty(docVo.getKey())) {
            //改为不拆字查询
            List<String> fieldList = type.getFieldList();
            BoolQueryBuilder must2Bool = QueryBuilders.boolQuery().should(QueryBuilders
                    .multiMatchQuery(docVo.getKey(), fieldList.toArray(new String[fieldList.size()])).type(MultiMatchQueryBuilder.Type.PHRASE));
            query = boolQuery.must(must2Bool);
        }

        //文档类型
        if (!ObjectUtils.isEmpty(docVo.getDictionSuffix())) {
            List<String> suffixList = new ArrayList<>();
            List<String> split = Arrays.asList(docVo.getDictionSuffix().split(","));
            Result<Map<String, String>> nameByKey = sysDictionaryService.searchValExtraMapByParentKey(DocDictionaryKeyConstants.DOC_COMMON_SUFFIX);
            Map<String, String> map = nameByKey.getData();
            //包含六
            if (split.contains(DocConstants.DOC_COMMON_SUFFIX_OTHER)) {
                split.forEach(item -> map.remove(String.valueOf(item)));
                map.forEach((key, value) -> suffixList.addAll(Arrays.asList(map.get(String.valueOf(key)).split(","))));
                TermsQueryBuilder must = QueryBuilders.termsQuery("suffix", suffixList);
                query = boolQuery.mustNot(must);
            } else {
                split.forEach(item -> suffixList.addAll(Arrays.asList(map.get(String.valueOf(item)).split(","))));
                TermsQueryBuilder must = QueryBuilders.termsQuery("suffix", suffixList);
                query = boolQuery.must(must);
            }
        }
        //所有者
        if (!ObjectUtils.isEmpty(docVo.getDocOwnerStr())) {
            MatchQueryBuilder must = QueryBuilders.matchQuery("docOwnerStr", "*" + docVo.getDocOwnerStr() + "*");
//            WildcardQueryBuilder must = QueryBuilders.wildcardQuery("docOwnerStr", "*" + docVo.getDocOwnerStr() + "*");
            query = boolQuery.must(must);
        }
        //标签
        if (!ObjectUtils.isEmpty(docVo.getTagId())) {
            MatchQueryBuilder must = QueryBuilders.matchQuery("tagIds", docVo.getTagId());
            query = boolQuery.must(must);
        }
        //上传时间
        if (!ObjectUtils.isEmpty(docVo.getCreateStartDate()) && !ObjectUtils.isEmpty(docVo.getCreateEndDate())) {
            RangeQueryBuilder must = QueryBuilders.rangeQuery("creatTime").gte(DateUtils.getDayBeginTime(docVo.getCreateStartDate()).getTime()).lte(DateUtils.getDayEndTime(docVo.getCreateEndDate()).getTime());
            query = boolQuery.must(must);
        }
        //更新时间
        /*if (!ObjectUtils.isEmpty(docVo.getUpdateEndDate()) && !ObjectUtils.isEmpty(docVo.getUpdateStartDate())) {

            RangeQueryBuilder must = QueryBuilders.rangeQuery("updateTime").gte(DateUtils.getDayBeginTime(docVo.getUpdateStartDate()).getTime()).lte(DateUtils.getDayEndTime(docVo.getUpdateEndDate()).getTime());
            query = boolQuery.must(must);
        }*/
        //附件名称
        if (!ObjectUtils.isEmpty(docVo.getAttchName())) {
            MatchQueryBuilder must = QueryBuilders.matchQuery("attchName", "*" + docVo.getAttchName() + "*");
            ///WildcardQueryBuilder must = QueryBuilders.wildcardQuery("attchName", "*" + docVo.getAttchName() + "*");
            query = boolQuery.must(must);
        }
        return query;
    }

    /**
     * 权限处理 文件路径处理
     */
    private void handleAuthAndPath(List<Map> list, AccountToken token) {
        for (Map map : list) {
            List<Long> busIds = new ArrayList<>();
            String busId = map.get("busId").toString();
            if (busId.contains("<span style='color:blue'>")) {
                busId = busId.substring(busId.indexOf("'>"), busId.indexOf("</"));
                busId = busId.substring(busId.indexOf(">") + 1);
            }
            DocBsDocument doc = docBsDocumentMapper.selectById(busId);
            busIds.add(doc.getBusId());
            //获取文档库
            DocSysHouse docSysHouse = null;
            String folderIdAllStr;
            if (!ObjectUtils.isEmpty(doc.getHouseId())) {
                docSysHouse = docSysHouseMapper.selectById(doc.getHouseId());
            }
            //文档全路径
            if (!ObjectUtils.isEmpty(docSysHouse)) {
                folderIdAllStr = docSysHouse.getHouseName() + "/" + docCommonService.handleFolderAll(doc.getFolderId());
            } else {
                folderIdAllStr = docCommonService.handleFolderAll(doc.getFolderId());
            }
            //权限
            Integer permissType = docCommonService.getCommonPermissDocOrFolder(token, busIds);
            Map attachment = (HashMap) map.get("attachment");
            if (ObjectUtils.isEmpty(permissType)) {
                if (!ObjectUtils.isEmpty(attachment.get("content"))) {
                    //没权限显示......
                    Integer begin = attachment.get("content").toString().indexOf("<span style='color:blue'>");
                    Integer end = attachment.get("content").toString().indexOf("</span>");
                    if (!DocConstants.INDEX.equals(begin) && !DocConstants.INDEX.equals(end)) {
                        String content = attachment.get("content").toString().substring(begin, end);
                        attachment.put("content", "......" + content + "</span>" + "......" + content + "</span>" + "......");
                    }
                }
                map.put("permissType", false);
            } else {
                if (!ObjectUtils.isEmpty(attachment.get("content")) && !attachment.get("content").toString().contains("<span style='color:blue'>") && attachment.get("content").toString().length() > DocConstants.THREE_HUNDDRED.intValue()) {
                    String content = attachment.get("content").toString().substring(DocConstants.ZERO, DocConstants.THREE_HUNDDRED);
                    attachment.put("content", content + "......");
                }
                map.put("permissType", true);
            }
            map.put("folderIdAllStr", folderIdAllStr);

            map.put("creatTime", handleTimePoke(map.get("creatTime").toString()));
        }
    }

}
