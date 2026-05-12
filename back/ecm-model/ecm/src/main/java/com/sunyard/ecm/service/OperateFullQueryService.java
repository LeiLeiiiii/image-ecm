package com.sunyard.ecm.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import cn.smallbun.screw.core.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.constant.BusiInfoConstants;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.RedisConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.ecm.dto.ecm.EcmDocrightDefDTO;
import com.sunyard.ecm.dto.ecm.EcmDtdAttrSearchDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.es.EcmBusiInfoEsDTO;
import com.sunyard.ecm.dto.es.EcmFileInfoEsDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.enums.EcmCheckAsyncTaskEnum;
import com.sunyard.ecm.enums.EcmElasticsearchQueryTypeEnum;
import com.sunyard.ecm.es.EsEcmBusi;
import com.sunyard.ecm.es.EsEcmFile;
import com.sunyard.ecm.manager.AsyncTaskService;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.manager.StaticTreePermissService;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.mapper.EcmAppDocrightMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmDocrightDefMapper;
import com.sunyard.ecm.mapper.EcmDtdAttrMapper;
import com.sunyard.ecm.mapper.EcmDtdDefMapper;
import com.sunyard.ecm.mapper.EcmEsAsyncTaskMapper;
import com.sunyard.ecm.mapper.es.EsEcmBusiMapper;
import com.sunyard.ecm.mapper.es.EsEcmFileMapper;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.po.EcmAppDocright;
import com.sunyard.ecm.po.EcmAsyncTask;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmDocrightDef;
import com.sunyard.ecm.po.EcmDtdAttr;
import com.sunyard.ecm.po.EcmDtdDef;
import com.sunyard.ecm.po.EcmEsAsyncTask;
import com.sunyard.ecm.po.EcmFileLabel;
import com.sunyard.ecm.util.EcmEsUtils;
import com.sunyard.ecm.util.TimeUtils;
import com.sunyard.ecm.vo.EcmSearchVO;
import com.sunyard.ecm.vo.SearchOptionVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.conversion.JsonUtils;
import com.sunyard.framework.common.util.date.DateUtils;
import com.sunyard.module.storage.api.FileHandleApi;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.vo.FileByteVO;
import com.sunyard.module.system.api.InstApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysOrgDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.dromara.easyes.common.constants.BaseEsConstants;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.core.conditions.update.LambdaEsUpdateWrapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zyl
 * @since 2023/8/7 9:58
 * @Description 影像全文检索实现类
 */
@Slf4j
@Service
public class OperateFullQueryService {
    private final List suffixArr = new ArrayList(Arrays.asList("txt", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "jpg", "png", "jpeg"));
    // 锁获取时间超时为30秒
    private final Long acquireTimeout = 60 * 1000L;
    // 锁自动失效时间为10秒
    private final Long expire = 30 * 1000L;
    @Value("${spring.application.name:eam-acc-server}")
    private String application;
    @Value("${bizIndex:ecm_busi_dev}")
    private String bizIndex;
    @Value("${fileIndex:ecm_file_dev}")
    private String fileIndex;
    @Value("${filePipeline}")
    private String filePipeline;
    @Resource
    private EcmEsUtils ecmEsUtils;
    @Resource
    private EcmAppDocrightMapper ecmAppDocrightMapper;
    @Resource
    private EcmDocrightDefMapper ecmDocRightDefMapper;
    @Resource
    private EcmAppAttrMapper ecmAppAttrMapper;
    @Resource
    private EcmDtdDefMapper ecmDtdDefMapper;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmDtdAttrMapper ecmDtdAttrMapper;
    @Resource
    private EsEcmFileMapper esEcmFileMapper;
    @Resource
    private EsEcmBusiMapper esEcmBusiMapper;
    @Resource
    private EcmEsAsyncTaskMapper ecmEsAsyncTaskMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private FileHandleApi fileHandleApi;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private StaticTreePermissService staticTreePermissService;
    @Resource
    private LockTemplate lockTemplate;
    @Resource
    private InstApi instApi;
    @Resource
    private AsyncTaskService asyncTaskService;

    /**
     * 全文检索
     */
    public Result search(EcmSearchVO ecmSearchVO, PageForm pageForm, AccountTokenExtendDTO token) {
        long start = System.currentTimeMillis();
        AssertUtils.isNull(ecmSearchVO.getPageNum(), "PageNum不能为空");
        AssertUtils.isNull(ecmSearchVO.getPageSize(), "PageNum不能为空");
        AssertUtils.isNull(ecmSearchVO.getType(), "type:查询类型不能为空");
        //判断是否查询条件什么都没输(有查询条件返回false)
        Boolean a = isHasQueryCriteria(ecmSearchVO);
        if (ObjectUtils.isEmpty(ecmSearchVO.getOrgCodeList())){
            //没有则取当前用户的机构号
            getAllOrgCodes(ecmSearchVO,token);
        }
        //如果没有传入appCode 默认查询当前权限下所以appCode的值
        if (ObjectUtils.isEmpty(ecmSearchVO.getAppCode())){
            getAllAppCodes(ecmSearchVO,token);
        }
        if (ObjectUtils.isEmpty(ecmSearchVO.getDocCode())){
            getallDocCodes(ecmSearchVO,token);
        }
        //去掉关键字限制
        //&& !ObjectUtils.isEmpty(ecmSearchVO.getKey())
        if (!a ) {
            //高亮分页查询
            boolean b = StateConstants.ZERO.equals(ecmSearchVO.getType().getCode());
            PageInfo pageInfo = new PageInfo();
            List<Map<String, Object>> result = new ArrayList<>();
            long count = 0;
            if (b) {
                //业务查询
                long startTime = System.currentTimeMillis();
                result = busiSearch(ecmSearchVO, StateConstants.ZERO);
                log.info("业务查询用时"+(System.currentTimeMillis()-startTime)+"毫秒");
            } else {
                //文档查询
                List<Map<String, String>> appAttrMap = ecmSearchVO.getAppAttrMap();
                String key = ecmSearchVO.getKey();
                if (!CollectionUtils.isEmpty(appAttrMap)) {
                    //筛选业务类型属性,先进行业务查询,然后筛选出busiId集合
                    ecmSearchVO.setKey(null);
                    List<Map<String, Object>> mapList = busiSearch(ecmSearchVO, StateConstants.ZERO);
                    List<Long> busiIdList = mapList.stream()
                            .map(map -> ((Number) map.get("busiId")).longValue()) // 强制转换为长整型
                            .collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(busiIdList)) {
                        return Result.success(pageInfo);
                    }
                    //筛选条件
                    ecmSearchVO.setBusiIdList(busiIdList);
                }
                ecmSearchVO.setKey(key);
                //拼接查询wrapper
                QueryBuilder query = getQueryBuilder(ecmSearchVO);
              /*  List<String> fieldList = ecmSearchVO.getType().getFieldList();
                LambdaEsUpdateWrapper<EsEcmFile> ecmfiledevWrapper = new LambdaEsUpdateWrapper<EsEcmFile>()
                        .multiMatchQuery(!ObjectUtils.isEmpty(ecmSearchVO.getKey()), ecmSearchVO.getKey(), fieldList.toArray(new String[fieldList.size()]))
                        .match(!ObjectUtils.isEmpty(ecmSearchVO.getAppCode()), EsEcmFile::getAppCode, ecmSearchVO.getAppCode())
                        .match(!ObjectUtils.isEmpty(ecmSearchVO.getBusiNo()), EsEcmFile::getBusiNo, ecmSearchVO.getBusiNo())
                        .match(!ObjectUtils.isEmpty(ecmSearchVO.getBusiNo()), EsEcmFile::getBusiNo, ecmSearchVO.getBusiNo())
                        .match(!ObjectUtils.isEmpty(ecmSearchVO.getDocCode()), EsEcmFile::getDocCode, ecmSearchVO.getDocCode())
                        .match(!ObjectUtils.isEmpty(ecmSearchVO.getDtdCode()), EsEcmFile::getDtdCode, ecmSearchVO.getDtdCode())
                        .match(!ObjectUtils.isEmpty(ecmSearchVO.getCreatUserName()), EsEcmFile::getCreatUserName, ecmSearchVO.getCreatUserName())
                        .match(!ObjectUtils.isEmpty(ecmSearchVO.getUpdateUserName()), EsEcmFile::getUpdateUserName, ecmSearchVO.getUpdateUserName())
                        .between(!ObjectUtils.isEmpty(ecmSearchVO.getCreateStartDate()) && !ObjectUtils.isEmpty(ecmSearchVO.getCreateEndDate()), EsEcmFile::getCreateDate,
                                ecmSearchVO.getCreateStartDate(), ecmSearchVO.getCreateEndDate())
                        .between(!ObjectUtils.isEmpty(ecmSearchVO.getUpdateEndDate()) && !ObjectUtils.isEmpty(ecmSearchVO.getUpdateStartDate()), EsEcmFile::getUpdateTime,
                                ecmSearchVO.getUpdateStartDate(), ecmSearchVO.getUpdateEndDate());

                //拼接高亮条件，因easy-es不支持嵌套属性高亮配置。所以得自己拼接高亮条件
                SearchSourceBuilder searchSourceBuilder = esEcmFileMapper.getSearchSourceBuilder(ecmfiledevWrapper);*/
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(query);
                HighlightBuilder highlightBuilder = getHighlightBuilder(ecmSearchVO.getType());
                searchSourceBuilder.highlighter(highlightBuilder);
          /*      searchSourceBuilder.timeout(TimeValue.timeValueMinutes(1L));
                searchSourceBuilder.sort("_score", SortOrder.DESC);*/
                searchSourceBuilder.from((pageForm.getPageNum() - 1) * pageForm.getPageSize());
                searchSourceBuilder.size(pageForm.getPageSize());
                long startTime = System.currentTimeMillis();
                //进行查询
                SearchResponse ecmfiledev = esEcmFileMapper.search(new LambdaEsQueryWrapper<EsEcmFile>()
                        .indexName(fileIndex)
                        .setSearchSourceBuilder(searchSourceBuilder)
                );
                log.info("文档查询用时"+(System.currentTimeMillis()-startTime)+"毫秒");
                count = ecmEsUtils.count(query, fileIndex);
                SearchHit[] hits = ecmfiledev.getHits().getHits();
                long startTime1 = System.currentTimeMillis();
                for (SearchHit item : hits) {
                    EsEcmFile esEcmFile = JSON.parseObject(item.getSourceAsString(), EsEcmFile.class);
                    ecmEsUtils.setNestedHighlightFieldValues(item.getHighlightFields(), esEcmFile);
                    result.add(BeanUtil.beanToMap(esEcmFile));
                }
                log.info("文档查询查询结果封装result用时"+(System.currentTimeMillis()-startTime1)+"毫秒");
                //筛选文档属性
                long startTime2 = System.currentTimeMillis();
                /*List<Map<String, String>> dtdAttrMap = ecmSearchVO.getDtdAttrMap();
                if (!ObjectUtils.isEmpty(dtdAttrMap)) {
                    handleDtdAttr(result, dtdAttrMap);
                }
                log.info("文档查询查询结果筛选文档属性用时"+(System.currentTimeMillis()-startTime2)+"毫秒");*/

            }
            //手动分页
            int total = result.size();
            pageInfo.setPageSize(pageForm.getPageSize());
            pageInfo.setPageNum(pageForm.getPageNum());
            int startIndex = (ecmSearchVO.getPageNum() - 1) * ecmSearchVO.getPageSize();
            int endIndex = Math.min(startIndex + ecmSearchVO.getPageSize(), total);
            List<Map<String, Object>> maps = new ArrayList<>();
            if(b){
                pageInfo.setTotal(total);
                maps = result.subList(startIndex, endIndex);
            }else {
                if(count>10000){
                    pageInfo.setTotal(10000);
                }else {
                    pageInfo.setTotal(count);
                }
                maps = result;
            }
            maps.forEach(map -> {
                if (!ObjectUtils.isEmpty(map.get("appTypeName")) && !ObjectUtils.isEmpty(map.get("appCode"))) {
                    String appTypeName = map.get("appTypeName").toString();
                    String appCode = map.get("appCode").toString();
                    map.put("appTypeName", "(" + appCode + ")" + appTypeName);
                }
            });
            pageInfo.setList(maps);
            long startTime = System.currentTimeMillis();
            //添加查看权限（readRight：0 无权限; readRight：1 有权限）
            addPermission(pageInfo.getList(), ecmSearchVO.getType().getCode(), token.getUsername());
            //处理高亮
            handleMap(pageInfo, ecmSearchVO.getType().getCode());
            log.info("查询结果添加权限和处理高亮用时"+(System.currentTimeMillis()-startTime)+"毫秒");
            log.info("查询总耗时"+(System.currentTimeMillis()-start)+"毫秒");
            return Result.success(pageInfo);
        }

        return Result.success(true);
    }

    private void getallDocCodes(EcmSearchVO ecmSearchVO, AccountTokenExtendDTO token) {
        Set<String> docCodeSet = staticTreePermissService.getDocCodeHaveByToken("", token, "");
        //未归类无权限控制
        docCodeSet.add(IcmsConstants.UNCLASSIFIED_ID);
        ecmSearchVO.setDocCodeSet(docCodeSet);
    }

    private void getAllAppCodes(EcmSearchVO ecmSearchVO, AccountTokenExtendDTO token) {
        Set<String> collect = staticTreePermissService.getAppCodeHaveByToken("", token, "");
        ecmSearchVO.setAppCodeSet(collect);
    }

    private void getAllOrgCodes(EcmSearchVO ecmSearchVO, AccountTokenExtendDTO token) {
        Result<List<SysOrgDTO>> listResult = instApi.searchInstTree(token.getInstId());
        List<SysOrgDTO> dataList = listResult.getData();
        if (dataList != null) {
            List<String> orgCodeList = dataList.stream()
                    .filter(Objects::nonNull) // 过滤掉 null 元素（可选）
                    .map(SysOrgDTO::getInstNo) // 提取 instNo
                    .filter(Objects::nonNull) // 过滤掉 instNo 为 null 的情况（可选）
                    .collect(Collectors.toList());
            ecmSearchVO.setOrgCodeList(orgCodeList);
        }
    }

    /**
     * 查询标签
     */
    public Result searchLabel(EcmSearchVO ecmSearchVO,PageForm pageForm, AccountTokenExtendDTO token) {
        if (!ObjectUtils.isEmpty(ecmSearchVO.getKey())) {
            MatchPhraseQueryBuilder matchQuery = QueryBuilders.matchPhraseQuery("fileLabel",  ecmSearchVO.getKey());
            // 创建布尔查询
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//            boolQuery.should(matchQuery);
            boolQuery.must(matchQuery);
            //设置appCode的值
            getAllAppCodes(ecmSearchVO,token);
            if (ecmSearchVO.getAppCodeSet() != null && !ecmSearchVO.getAppCodeSet().isEmpty()) {
                List<String> validAppCodes = ecmSearchVO.getAppCodeSet().stream()
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList());
                if (!validAppCodes.isEmpty()) {
                    // 使用 termsQuery + .keyword 实现多值精确匹配
                    boolQuery.must(QueryBuilders.termsQuery("appCode.keyword", validAppCodes));
                }
            }
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(boolQuery);
            HighlightBuilder highlightBuilder = getHighlightBuilder(ecmSearchVO.getType());
            sourceBuilder.highlighter(highlightBuilder);
            //进行查询
            SearchResponse ecmfiledev = esEcmFileMapper.search(new LambdaEsQueryWrapper<EsEcmFile>()
                    .indexName(fileIndex)
                    .setSearchSourceBuilder(sourceBuilder)
            );
            List<Map<String, Object>> result = new ArrayList<>();
            SearchHit[] hits = ecmfiledev.getHits().getHits();
            for (SearchHit item : hits) {
                EsEcmFile esEcmFile = JSON.parseObject(item.getSourceAsString(), EsEcmFile.class);
                ecmEsUtils.setNestedHighlightFieldValues(item.getHighlightFields(), esEcmFile);
                result.add(BeanUtil.beanToMap(esEcmFile));
            }
            //手动分页
            PageInfo pageInfo =new PageInfo();
            pageInfo.setPageSize(pageForm.getPageSize());
            pageInfo.setPageNum(pageForm.getPageNum());
            List<Map<String, Object>> maps = new ArrayList<>();
            long count = ecmEsUtils.count(boolQuery, fileIndex);
            if (count > 10000) {
                pageInfo.setTotal(10000);
            } else {
                pageInfo.setTotal(count);
            }
            maps = result;

            maps.forEach(map -> {
                if (!ObjectUtils.isEmpty(map.get("appTypeName")) && !ObjectUtils.isEmpty(map.get("appCode"))) {
                    String appTypeName = map.get("appTypeName").toString();
                    String appCode = map.get("appCode").toString();
                    map.put("appTypeName", "(" + appCode + ")" + appTypeName);
                }
            });
            pageInfo.setList(maps);
            //添加查看权限（readRight：0 无权限; readRight：1 有权限）
            addPermission(pageInfo.getList(), ecmSearchVO.getType().getCode(), token.getUsername());
            //处理高亮
            handleMap(pageInfo, ecmSearchVO.getType().getCode());
            return Result.success(pageInfo);
        }
        return Result.success(true);
    }

    /**
     * 单证检索
     */
    public Result searchDtd(EcmSearchVO ecmSearchVO, PageForm pageForm, AccountTokenExtendDTO token,Integer source) {
        AssertUtils.isNull(ecmSearchVO.getPageNum(), "PageNum不能为空");
        AssertUtils.isNull(ecmSearchVO.getPageSize(), "PageNum不能为空");
        //判断是否查询条件什么都没输(有查询条件返回false)
        if(IcmsConstants.ZERO.equals(source)){//0代表全文检索处查询，不输条件直接返回
            Boolean a = isHasQueryCriteria(ecmSearchVO);
            if (a) {
                return Result.success(true);
            }
        }
        if (ObjectUtils.isEmpty(ecmSearchVO.getOrgCodeList())){
            //没有则取当前用户的机构号
            getAllOrgCodes(ecmSearchVO,token);
        }
        if (ObjectUtils.isEmpty(ecmSearchVO.getAppCode())){
            getAllAppCodes(ecmSearchVO,token);
        }
        if (ObjectUtils.isEmpty(ecmSearchVO.getDocCode())){
            getallDocCodes(ecmSearchVO,token);
        }
        //文档查询
        List<Map<String, String>> appAttrMap = ecmSearchVO.getAppAttrMap();
        if (!CollectionUtils.isEmpty(appAttrMap)) {
            //筛选业务类型属性,先进行业务查询,然后筛选出busiId集合
            ecmSearchVO.setKey(null);
            List<Map<String, Object>> mapList = busiSearch(ecmSearchVO, StateConstants.ZERO);
            List<Long> busiIdList = mapList.stream()
                    .map(map -> ((Number) map.get("busiId")).longValue()) // 强制转换为长整型
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(busiIdList)) {
                return Result.success();
            }
            //筛选条件
            ecmSearchVO.setBusiIdList(busiIdList);
        }
        ecmSearchVO.setSearchSource(IcmsConstants.ONE);  //1代表是单独单证查询页面
        //拼接查询wrapper
        QueryBuilder query;
        //source 0代表全文检索处查询，1为单证查询菜单
        if(IcmsConstants.ZERO.equals(source)){
            query = getQueryBuilder(ecmSearchVO);
        }else{
            query = getQueryBuilderForDtd(ecmSearchVO);
        }
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(query);
//        HighlightBuilder highlightBuilder = getHighlightBuilder(ecmSearchVO.getType());
//        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.from((pageForm.getPageNum() - 1) * pageForm.getPageSize());
        searchSourceBuilder.size(pageForm.getPageSize());
        searchSourceBuilder.sort("createDate", SortOrder.DESC);
        //进行查询
        SearchResponse ecmfiledev = esEcmFileMapper.search(new LambdaEsQueryWrapper<EsEcmFile>()
                .indexName(fileIndex)
                .setSearchSourceBuilder(searchSourceBuilder)
        );
        List<Map<String, Object>> result = new ArrayList<>();
        SearchHit[] hits = ecmfiledev.getHits().getHits();
        for (SearchHit item : hits) {
            EsEcmFile esEcmFile = JSON.parseObject(item.getSourceAsString(), EsEcmFile.class);
            // ecmEsUtils.setNestedHighlightFieldValues(item.getHighlightFields(), esEcmFile);
            result.add(BeanUtil.beanToMap(esEcmFile));
        }
        //手动分页
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageSize(pageForm.getPageSize());
        pageInfo.setPageNum(pageForm.getPageNum());
        List<Map<String, Object>> maps = new ArrayList<>();
        long count = ecmEsUtils.count(query, fileIndex);
        if (count > 10000) {
            pageInfo.setTotal(10000);
        } else {
            pageInfo.setTotal(count);
        }
        maps = result;

        maps.forEach(map -> {
            if (!ObjectUtils.isEmpty(map.get("appTypeName")) && !ObjectUtils.isEmpty(map.get("appCode"))) {
                String appTypeName = map.get("appTypeName").toString();
                String appCode = map.get("appCode").toString();
                map.put("appTypeName", "(" + appCode + ")" + appTypeName);
            }
        });
        pageInfo.setList(maps);
        //添加查看权限（readRight：0 无权限; readRight：1 有权限）
        addPermission(pageInfo.getList(), IcmsConstants.ONE, token.getUsername());
        //处理高亮
//        handleMap(pageInfo, ecmSearchVO.getType().getCode());
        return Result.success(pageInfo);
    }


    /**
     * 得到分词查询的条件
     */
    private QueryBuilder getQueryBuilder(EcmSearchVO docVo) {
        QueryBuilder query = null;
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        query = getQueryBuilderPublic(docVo, query, boolQuery);
        if (!ObjectUtils.isEmpty(docVo.getAppCode())) {
            MatchQueryBuilder must = QueryBuilders.matchQuery("appCode", docVo.getAppCode());
            query = boolQuery.must(must);
        }
        if (!ObjectUtils.isEmpty(docVo.getBusiNo())) {
            MatchQueryBuilder must = QueryBuilders.matchQuery("busiNo", docVo.getBusiNo());
            query = boolQuery.must(must);
        }
        // 多值 orgCode 精准匹配
        if (!ObjectUtils.isEmpty(docVo.getOrgCodeList())) {
            List<String> validOrgCodes = docVo.getOrgCodeList().stream()
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            if (!validOrgCodes.isEmpty()) {
                query = boolQuery.must(QueryBuilders.termsQuery("orgCode.keyword", validOrgCodes));
            }
        }
        //如果没有传入appCode，docCode那么默认查询当前角色权限下的业务类型和资料类型
        if (!ObjectUtils.isEmpty(docVo.getAppCodeSet())){
            List<String> validAppCodes = docVo.getAppCodeSet().stream()
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            if (!validAppCodes.isEmpty()) {
                boolQuery.must(QueryBuilders.termsQuery("appCode.keyword", validAppCodes));
            }
        }
        if (!ObjectUtils.isEmpty(docVo.getDocCodeSet())){
            List<String> validDocCodes = docVo.getDocCodeSet().stream()
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            if (!validDocCodes.isEmpty()) {
                boolQuery.must(QueryBuilders.termsQuery("docCode.keyword", validDocCodes));
            }
        }
        return query;
    }

    private QueryBuilder getQueryBuilderPublic(EcmSearchVO docVo, QueryBuilder query, BoolQueryBuilder boolQuery) {
        //key
        if (!ObjectUtils.isEmpty(docVo.getKey())) {
            //改为不拆字查询
            List<String> fieldList = docVo.getType().getFieldList();
            BoolQueryBuilder must2Bool = QueryBuilders.boolQuery().should(QueryBuilders
                    .multiMatchQuery(docVo.getKey(), fieldList.toArray(new String[fieldList.size()])).type(MultiMatchQueryBuilder.Type.PHRASE));
            query = boolQuery.must(must2Bool);
        }
        if (!ObjectUtils.isEmpty(docVo.getDocCode())) {
            TermsQueryBuilder must = QueryBuilders.termsQuery("docCode", docVo.getDocCode());
            query = boolQuery.must(must);
        }
        if (!ObjectUtils.isEmpty(docVo.getBusiIdList())) {
            TermsQueryBuilder must = QueryBuilders.termsQuery("busiId", docVo.getBusiIdList());
            query = boolQuery.must(must);
        }
        if (!ObjectUtils.isEmpty(docVo.getSearchSource()) && docVo.getSearchSource().equals(IcmsConstants.ONE)) {
            if (!ObjectUtils.isEmpty(docVo.getDtdCode())) {
                // dtdCode不为空：正常匹配查询
                MatchQueryBuilder must = QueryBuilders.matchQuery("dtdCode", docVo.getDtdCode());
                query = boolQuery.must(must);
            } else {
                // dtdCode为空：查询dtdCode字段存在（不为空）的文档
                ExistsQueryBuilder must = QueryBuilders.existsQuery("dtdCode");
                query = boolQuery.must(must);
            }
        } else {
            // searchSource不为1或为空：保持原有逻辑
            if (!ObjectUtils.isEmpty(docVo.getDtdCode())) {
                MatchQueryBuilder must = QueryBuilders.matchQuery("dtdCode", docVo.getDtdCode());
                query = boolQuery.must(must);
            }
        }
        if (!ObjectUtils.isEmpty(docVo.getCreatUserName())) {
            MatchQueryBuilder must = QueryBuilders.matchQuery("creatUserName", docVo.getCreatUserName());
            query = boolQuery.must(must);
        }
        if (!ObjectUtils.isEmpty(docVo.getUpdateUserName())) {
            MatchQueryBuilder must = QueryBuilders.matchQuery("updateUserName", docVo.getUpdateUserName());
            query = boolQuery.must(must);
        }

        //上传时间
        if (!ObjectUtils.isEmpty(docVo.getCreateStartDate()) && !ObjectUtils.isEmpty(docVo.getCreateEndDate())) {
            RangeQueryBuilder must = QueryBuilders.rangeQuery("createDate").gte(DateUtils.getDayBeginTime(docVo.getCreateStartDate()).getTime()).lte(DateUtils.getDayEndTime(docVo.getCreateEndDate()).getTime());
            query = boolQuery.must(must);
        }
        //更新时间
        if (!ObjectUtils.isEmpty(docVo.getUpdateEndDate()) && !ObjectUtils.isEmpty(docVo.getUpdateStartDate())) {
            RangeQueryBuilder must = QueryBuilders.rangeQuery("updateTime").gte(DateUtils.getDayBeginTime(docVo.getUpdateStartDate()).getTime()).lte(DateUtils.getDayEndTime(docVo.getUpdateEndDate()).getTime());
            query = boolQuery.must(must);
        }
        //文档属性
        if (!ObjectUtils.isEmpty(docVo.getDtdAttrMap())) {
            for (Map<String, String> pp : docVo.getDtdAttrMap()) {
                String targetId = pp.get("id");
                String targetValue = pp.get("value");
                // 跳过空值
                if (ObjectUtils.isEmpty(targetId) || ObjectUtils.isEmpty(targetValue)) {
                    continue;
                }
                // 单个 (id, value) 联合匹配的嵌套查询
                NestedQueryBuilder singleConditionQuery = QueryBuilders.nestedQuery(
                        "ocrInfo.ocrIdentifyInfo.attr",
                        QueryBuilders.boolQuery()
                                .must(QueryBuilders.termQuery("ocrInfo.ocrIdentifyInfo.attr.id", targetId))
                                .must(QueryBuilders.termQuery("ocrInfo.ocrIdentifyInfo.attr.value", targetValue)),
                        ScoreMode.None
                );

                query = boolQuery.must(singleConditionQuery);
            }
        }
        return query;
    }

    /**
     * 得到分词查询的条件
     */
    private QueryBuilder getQueryBuilderForDtd(EcmSearchVO docVo) {
        QueryBuilder query = null;
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //key
        query = getQueryBuilderPublic(docVo, query, boolQuery);
        if (!ObjectUtils.isEmpty(docVo.getAppCode())) {
            TermsQueryBuilder must = QueryBuilders.termsQuery("appCode", docVo.getAppCode());
            query = boolQuery.must(must);
        }
        if (!ObjectUtils.isEmpty(docVo.getBusiNo())) {
            TermsQueryBuilder must = QueryBuilders.termsQuery("busiNo", docVo.getBusiNo());
            query = boolQuery.must(must);
        }
        return query;
    }
    /**
     * 查询es业务信息
     */
    private List<Map<String, Object>>  busiSearch(EcmSearchVO ecmSearchVO, Integer type) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<EsEcmBusi> ecmbusidev = esEcmBusiMapper.selectList(new LambdaEsQueryWrapper<EsEcmBusi>()
                .indexName(bizIndex)
                .multiMatchQuery(!ObjectUtils.isEmpty(ecmSearchVO.getKey()), ecmSearchVO.getKey(), Operator.OR, 0, BaseEsConstants.DEFAULT_BOOST,
                        EsEcmBusi::getBusiNo, EsEcmBusi::getCreatUserName, EsEcmBusi::getUpdateUserName, EsEcmBusi::getAppAttrs)
                .match(!ObjectUtils.isEmpty(ecmSearchVO.getCreatUserName()), EsEcmBusi::getCreatUserName, "*" + ecmSearchVO.getCreatUserName() + "*")
                .match(!ObjectUtils.isEmpty(ecmSearchVO.getUpdateUserName()), EsEcmBusi::getUpdateUserName, "*" + ecmSearchVO.getUpdateUserName() + "*")
                .match(!ObjectUtils.isEmpty(ecmSearchVO.getAppCode()), EsEcmBusi::getAppCode, "*" + ecmSearchVO.getAppCode() + "*")
                .match(!ObjectUtils.isEmpty(ecmSearchVO.getBusiNo()), EsEcmBusi::getBusiNo, "*" + ecmSearchVO.getBusiNo() + "*")
                .in(!ObjectUtils.isEmpty(ecmSearchVO.getBusiIdList()), EsEcmBusi::getBusiId, ecmSearchVO.getBusiIdList())
                .eq(EsEcmBusi::getIsDeleted, StateConstants.NOT_DELETE)
                .isNotNull(EsEcmBusi::getIsDeleted)
                // 添加时间范围筛选
                .ge(!ObjectUtils.isEmpty(ecmSearchVO.getCreateStartDate()), EsEcmBusi::getCreateDate, ecmSearchVO.getCreateStartDate()) // 大于等于开始时间
                .le(!ObjectUtils.isEmpty(ecmSearchVO.getCreateEndDate()), EsEcmBusi::getCreateDate, ecmSearchVO.getCreateEndDate()) // 小于等于结束时间
                .in(!ObjectUtils.isEmpty(ecmSearchVO.getOrgCodeList()),  EsEcmBusi::getOrgCode, ecmSearchVO.getOrgCodeList())
                .in(!ObjectUtils.isEmpty(ecmSearchVO.getAppCodeSet()),EsEcmBusi::getAppCode, ecmSearchVO.getAppCodeSet())
        );
        for (EsEcmBusi item : ecmbusidev) {
            result.add(BeanUtil.beanToMap(item));
        }
        // 筛选业务类型属性
        handleAppAttr(result, ecmSearchVO.getAppAttrMap());
        return result;
    }
    /**
     * 添加es业务信息
     */
    @Async("GlobalThreadPool")
    public Result addEsBusiInfo(EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO, Long userId) {
        try {
            //得到要存到es中的业务信息
            EcmBusiInfoEsDTO ecmBusiInfoEsDTO = getEcmBusiInfoEsDTO(ecmBusiInfoRedisDTO);
            //创建es异步任务
            saveEsAsyncTask(ecmBusiInfoRedisDTO.getBusiId(),userId);
            //上传到es
            Boolean flag = uploadEs(ecmBusiInfoRedisDTO.getBusiId(), ecmBusiInfoEsDTO, StateConstants.ZERO, userId, false,null);

            //更新任务状态
            if (flag){
                updateEsTaskStatus(ecmBusiInfoRedisDTO.getBusiId(), IcmsConstants.ONE, "");
            }else {
                updateEsTaskStatus(ecmBusiInfoRedisDTO.getBusiId(), IcmsConstants.TWO, "");
            }
        }catch (Exception e){
            // 4. 任何异常都标记为失败
            updateEsTaskStatus(ecmBusiInfoRedisDTO.getBusiId(), IcmsConstants.TWO, e.getMessage()); // 记录异常信息
            log.error("ES 异步任务失败，busiId: {}", ecmBusiInfoRedisDTO.getBusiId(), e);
        }
        return null;
    }

    /**
     * 添加es文件信息
     */
    @Async("GlobalThreadPool")
    public void addEsFileInfo(EcmFileInfoDTO ecmFileInfoDTO, Long userId) {
        AssertUtils.isNull(ecmFileInfoDTO.getNewFileId(), "参数错误，新文件id不能为空");
        //得到要存到es中的业务信息
        EcmFileInfoEsDTO ecmFileInfoEsDTO = getEcmFileInfoEsDTO(ecmFileInfoDTO);
        //上传到es
        uploadEs(ecmFileInfoDTO.getFileId(), ecmFileInfoEsDTO, StateConstants.COMMON_ONE, userId,false,ecmFileInfoDTO.getBusiId());
    }
    /**
     * 编辑es业务信息
     */
    @Async("GlobalThreadPool")
    public Result editEsBusiInfo(EcmBusiInfoRedisDTO ecmBusiInfoExtend, String userId, Date updateTime) {
        ecmBusiInfoExtend.setUpdateUser(userId);
        ecmBusiInfoExtend.setUpdateTime(updateTime);
        EcmBusiInfoEsDTO ecmBusiInfoEsDTO1 = getEcmBusiInfoEsDTO(ecmBusiInfoExtend);
        // 查询条件
        esEcmBusiMapper.update(null, new LambdaEsUpdateWrapper<EsEcmBusi>()
                .indexName(bizIndex)
                .set(!ObjectUtils.isEmpty(ecmBusiInfoEsDTO1.getAppAttrs()), EsEcmBusi::getAppAttrs, ecmBusiInfoEsDTO1.getAppAttrs())
                .set(EsEcmBusi::getUpdateTime, ObjectUtils.isEmpty(ecmBusiInfoEsDTO1.getUpdateTime())
                        ? System.currentTimeMillis() : ecmBusiInfoEsDTO1.getUpdateTime().getTime())
                .set(EsEcmBusi::getUpdateUserName, ecmBusiInfoEsDTO1.getUpdateUserName())
                .eq(EsEcmBusi::getBusiId, ecmBusiInfoEsDTO1.getBusiId())
        );
        return null;
    }
    /**
     * 编辑es文件信息
     */
    @Async("GlobalThreadPool")
    public void editEsFileInfo(Long id, String updateUserName, Date updateTime, Long newFileId, String docCode, String newFileName) {
        AssertUtils.isNull(updateUserName, "参数错误，修改人名称为空");
        //得到资料名称
        EcmDocDef ecmDocDef = ecmDocDefMapper.selectById(docCode);
        esEcmFileMapper.update(null, new LambdaEsUpdateWrapper<EsEcmFile>()
                        .indexName(fileIndex)
                        .set(EsEcmFile::getUpdateTime, updateTime.getTime())
                        .set(EsEcmFile::getUpdateUserName, updateUserName)
                        .set(!ObjectUtils.isEmpty(newFileId), EsEcmFile::getNewFileId, newFileId)
                        .set(!ObjectUtils.isEmpty(docCode), EsEcmFile::getDocCode, docCode)
                        .set(!ObjectUtils.isEmpty(docCode), EsEcmFile::getDocTypeName, ObjectUtils.isEmpty(ecmDocDef) ? "未归类" : ecmDocDef.getDocName())
                        .set(!ObjectUtils.isEmpty(newFileName), EsEcmFile::getFileName, newFileName)
                        .eq(EsEcmFile::getId, id)
        );
    }


    /**
     * 查询资料类型树
     */
    public List<Tree<String>> searchDocTypeTree(AccountTokenExtendDTO token, String appCode) {
        Set<String> docCodeSet = staticTreePermissService.getDocCodeHaveByToken(appCode, token, "");
        if (docCodeSet != null && docCodeSet.isEmpty()){
            return new ArrayList<>();
        }
        //查询所有资料类型
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectList(new LambdaQueryWrapper<EcmDocDef>()
                .in(CollectionUtil.isNotEmpty(docCodeSet),EcmDocDef::getDocCode,docCodeSet));
        TreeNodeConfig config = new TreeNodeConfig();
        List<Tree<String>> build = TreeUtil.build(ecmDocDefs, "0", config, (treeNode, tree) -> {
            tree.putExtra("id", treeNode.getDocCode());
            tree.putExtra("parentId", treeNode.getParent());
            tree.putExtra("lable", treeNode.getDocName());
            tree.putExtra("isParent", treeNode.getIsParent());
        });
        return build;
    }

    /**
     * 影像单证类型
     */
    public List<EcmDtdDef> searchDtdType(String docCode) {
//        if (ObjectUtils.isEmpty(docCode)) {
            return ecmDtdDefMapper.selectList(null);
//        }
//        return ecmDtdDocRelMapper.selectDtdListByDocTypeId(docCode);

    }

    /**
     * 查询文档类型属性
     */
    public HashMap<String, Object> searchDtdTypeAttr(Long dtdTypeId, AccountTokenExtendDTO token) {
        List<EcmDtdAttr> ecmDtdAttrs = ecmDtdAttrMapper.selectList(new LambdaQueryWrapper<EcmDtdAttr>()
                .eq(EcmDtdAttr::getDtdTypeId, dtdTypeId));
        HashMap<String, Object> map = new HashMap<>();
        Map<String, List<EcmDtdAttr>> ecmDtdAttrMap = ecmDtdAttrs.stream().collect(Collectors.groupingBy(EcmDtdAttr::getAttrCode)).entrySet().stream()
                .filter(p -> p.getValue().size() == StateConstants.COMMON_ONE)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        List<EcmDtdAttr> ecmDtdAttrList = ecmDtdAttrMap.entrySet().stream().map(p -> p.getValue().get(StateConstants.ZERO)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ecmDtdAttrList)) {
            return null;
        }
        List<EcmDtdAttrSearchDTO> searchDTOList = new ArrayList<>();
        for (EcmDtdAttr ecmDtdAttr : ecmDtdAttrList) {
            EcmDtdAttrSearchDTO dto = new EcmDtdAttrSearchDTO();
            if (BusiInfoConstants.DATE_TYPE.equals(ecmDtdAttr.getInputType())) {
                dto.setType("date");
                dto.setPlaceholder("请选择" + ecmDtdAttr.getAttrName());
            } else if (BusiInfoConstants.SELECT_TYPE.equals(ecmDtdAttr.getInputType())) {
                String selectNodes = ecmDtdAttr.getListValue();
                String[] split = selectNodes.split(";");
                List<SearchOptionVO> voList = new ArrayList<>();
                for (String spli : split) {
                    SearchOptionVO optionVo = new SearchOptionVO();
                    optionVo.setLabel(spli);
                    optionVo.setCode(spli);
                    optionVo.setValue(spli);
                    voList.add(optionVo);
                }
                dto.setType("select");
                dto.setPlaceholder("请选择" + ecmDtdAttr.getAttrName());
                dto.setOption(voList);
            } else {
                dto.setPlaceholder("请输入" + ecmDtdAttr.getAttrName());
            }
            dto.setCode(ecmDtdAttr.getAttrCode());
            dto.setLabel(ecmDtdAttr.getAttrName() + "：");
            dto.setAppAttrId(ecmDtdAttr.getDtdAttrId());
            searchDTOList.add(dto);
        }
        map.put("searchList", searchDTOList);
        return map;
    }

    /**
     * 查询单个影像文件详情
     *
     */
    public FileInfoRedisDTO searchOneEcmsFile(Long busiId, Long fileId, AccountTokenExtendDTO token) {
        //校验入参数
        AssertUtils.isNull(busiId, "参数错误");
        AssertUtils.isNull(fileId, "参数错误");
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, busiId);
        //得到该业务下的文件所有信息
        FileInfoRedisDTO fileInfoRedisDTO = busiCacheService.getFileInfoRedisSingle(ecmBusiInfoRedisDTO.getBusiId(),fileId);
        if (fileInfoRedisDTO==null) {
            return null;
        }
        //文件后缀改为小写
        fileInfoRedisDTO.setFormat(fileInfoRedisDTO.getFormat().toLowerCase());
        //计算文件大小
        calculateFileSize(fileInfoRedisDTO);
        getDocRight(token, ecmBusiInfoRedisDTO, fileInfoRedisDTO);
        return fileInfoRedisDTO;
    }

    /**
     * 获取文件权限
     * @param token
     * @param ecmBusiInfoRedisDTO
     * @param fileInfoRedisDTO
     */
    public void getDocRight(AccountTokenExtendDTO token, EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO, FileInfoRedisDTO fileInfoRedisDTO) {
        List<EcmDocrightDefDTO> docRightList = staticTreePermissService.roleDimLogic2(ecmBusiInfoRedisDTO.getAppCode(), ecmBusiInfoRedisDTO.getRightVer(), token);
        Map<String, List<EcmDocrightDefDTO>> docRightGroupedById = docRightList.stream()
                .filter(p -> !ObjectUtils.isEmpty(p.getDocCode()))
                .collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
        List<EcmDocrightDefDTO> docrightDefExtendList = docRightGroupedById.get(fileInfoRedisDTO.getDocCode());
        if (!CollectionUtils.isEmpty(docrightDefExtendList)) {
            fileInfoRedisDTO.setDocRight(docrightDefExtendList.get(0));
        } else {
            if (IcmsConstants.UNCLASSIFIED_ID.equals(fileInfoRedisDTO.getDocCode())) {
                //文件属于未归类-文件操作权限打开
                EcmDocrightDefDTO docrightDefExtend = getDocRightAllOpen();
                fileInfoRedisDTO.setDocRight(docrightDefExtend);
            } else {
                EcmDocrightDefDTO docrightDefExtend = getDocRightAllOpen();
                fileInfoRedisDTO.setDocRight(docrightDefExtend);
                fileInfoRedisDTO.setFileMd5(null);
                fileInfoRedisDTO.setNewFileId(0L);
                fileInfoRedisDTO.setShowType(0);
            }
        }
    }

    /**
     * 获取资料权限
     */
    private EcmDocrightDefDTO getDocRightAllOpen() {
        EcmDocrightDefDTO docrightDefExtend = new EcmDocrightDefDTO();
        docrightDefExtend.setAddRight(IcmsConstants.ONE.toString());
        docrightDefExtend.setReadRight(IcmsConstants.ONE.toString());
        docrightDefExtend.setUpdateRight(IcmsConstants.ONE.toString());
        docrightDefExtend.setDeleteRight(IcmsConstants.ONE.toString());
        docrightDefExtend.setThumRight(IcmsConstants.ONE.toString());
        docrightDefExtend.setPrintRight(IcmsConstants.ONE.toString());
        docrightDefExtend.setDownloadRight(IcmsConstants.ONE.toString());
        docrightDefExtend.setOtherUpdate(IcmsConstants.ONE.toString());
        return docrightDefExtend;
    }

    /**
     * 计算文件大小
     *
     */
    private void calculateFileSize(FileInfoRedisDTO file) {
        try {
            Double newFileSize = 0.00;
            String fileUnit = "";
            Long oldFileSize = file.getSize();
            if (oldFileSize > 0 && oldFileSize <= 1024 * 1024) {
                newFileSize = Math.ceil(oldFileSize.doubleValue() / 1024);
                fileUnit = IcmsConstants.FILE_UNIT_K;
            } else if (oldFileSize > 1024 * 1024 && oldFileSize <= 1024 * 1024 * 1024) {
                newFileSize = Double.valueOf(String.format("%.1f", (oldFileSize.doubleValue() / (1024 * 1024))));
                fileUnit = IcmsConstants.FILE_UNIT_M;
            } else {
                newFileSize = Math.ceil(oldFileSize.doubleValue() / (1024 * 1024 * 1024));
                fileUnit = IcmsConstants.FILE_UNIT_G;
            }
            file.setFileUnit(fileUnit);
            file.setFileSize(newFileSize + fileUnit);
        } catch (NumberFormatException e) {
            log.error("单位转换有误：{}", e.getMessage());
        }
    }

    /**
     * 获取高亮信息
     */
    private HighlightBuilder getHighlightBuilder(EcmElasticsearchQueryTypeEnum vo) {
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        for (String filed : vo.getFieldList()) {
            if(IcmsConstants.ES_FVH.equals(filed)){
                highlightBuilder.field(new HighlightBuilder.Field(IcmsConstants.ES_FVH)
                        .highlighterType("fvh"));
            }else {
                highlightBuilder.field(filed);
            }
        }
        //多个高亮显示
        highlightBuilder.requireFieldMatch(true);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        //最大高亮分片数
        highlightBuilder.fragmentSize(100);
        //从第一个分片获取高亮片段
        highlightBuilder.numOfFragments(3);
        return highlightBuilder;
    }

    /**
     * 对列表数据的过滤处理
     */
    private void handleDtdAttr(List<Map<String, Object>> list, List<Map<String, String>> dtdAttrMap) {
        if (!CollectionUtils.isEmpty(list)) {
            //得到所有的文档类型属性
            List<EcmDtdAttr> ecmDtdAttrs = ecmDtdAttrMapper.selectList(null);
            //将所有的文档类型属性根据dtd_attr_id分组
            Map<Long, List<EcmDtdAttr>> collect = ecmDtdAttrs.stream().collect(Collectors.groupingBy(EcmDtdAttr::getDtdAttrId));
            Iterator<Map<String, Object>> iterator = list.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> map = iterator.next();
                final boolean[] shouldRemove = {true};
                // 获取 ocrInfoString
                String ocrInfoNewString = (String) map.get("ocrInfo");
                if (!ObjectUtils.isEmpty(ocrInfoNewString)) {
                    Map<String, Map<String, Object>> ocrInfoNew = getStringObjectMap(ocrInfoNewString);
                    ocrInfoNew.forEach((k, v) -> {
                        List<Map<String, String>> ocrIdentifyInfo = (List<Map<String, String>>) v.get("ocrIdentifyInfo");
                        Iterator<Map<String, String>> ocrIdentifyIterator = ocrIdentifyInfo.iterator();
                        while (ocrIdentifyIterator.hasNext()) {
                            Map<String, String> p = ocrIdentifyIterator.next();
                            if (!ObjectUtils.isEmpty(p.get("value"))) {
                                Iterator<Map<String, String>> dtdAttrIterator = dtdAttrMap.iterator();
                                while (dtdAttrIterator.hasNext()) {
                                    Map<String, String> pp = dtdAttrIterator.next();
                                    if (p.get("id").equals(pp.get("id")) && p.get("value").contains(pp.get("value"))) {
                                        shouldRemove[0] = false;
                                    }
                                }
                            }
                            List<EcmDtdAttr> ecmDtdAttrList = collect.get(Long.valueOf(p.get("id")));
                            AssertUtils.isNull(ecmDtdAttrList, "参数错误");
                            if (StateConstants.ZERO.equals(ecmDtdAttrList.get(StateConstants.ZERO).getIsShow())) {
                                ocrIdentifyIterator.remove();
                            }
                        }
                    });
                }
                // 如果需要移除，则从列表中移除当前元素
                if (shouldRemove[0]) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 获取map数据
     */
    private void handleMap(PageInfo pageInfo, Integer type) {
        List<Map<String, Object>> list = pageInfo.getList();
        if (!CollectionUtils.isEmpty(list)) {
            if (StateConstants.ZERO.equals(type)) {
                //查询所有的业务类型属性
                List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(null);
                Iterator<Map<String, Object>> iterator = list.iterator();
                while (iterator.hasNext()) {
                    Map<String, Object> map = iterator.next();
                    String appAttrs = (String) map.get("appAttrs");
                    boolean b = appAttrs.contains("<span style='color:red'>");
                    if (!ObjectUtils.isEmpty(appAttrs) && b) {
                        if (!ObjectUtils.isEmpty(appAttrs)) {
                            //判断是否展示业务类型属性，不展示的就删掉 todo
                            appAttrs = handleAppAttrIsShow(appAttrs, String.valueOf(map.get("appCode")), ecmAppAttrs);
                            map.put("appAttrs", appAttrs);
                        }
                    }
                }
            } else {
                for (Map map : list) {
                    String ocrInfoNewString = JSON.toJSONString(map.get("ocrInfo"));
                    if (!ObjectUtils.isEmpty(ocrInfoNewString)) {
                        //Map<String, Map<String, Object>> dataList = getStringObjectMap(ocrInfoNewString);
                        map.put("ocrInfo", ocrInfoNewString);
                    }
                }
            }

        }
    }

    /**
     * 将JSON字符串解析为嵌套的Map结构
     */
    private Map<String, Map<String, Object>> getStringObjectMap(String ocrInfoNewString) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Map<String, Object>> dataList = new HashMap<>();
        try {
            dataList = objectMapper.readValue(ocrInfoNewString, new TypeReference<Map<String, Map<String, Object>>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("json处理异常",e);
        }
        return dataList;
    }

    /**
     * 判断是否展示业务类型属性，不展示的就删掉
     */
    private String handleAppAttrIsShow(String stringStringMap, String appCode, List<EcmAppAttr> ecmAppAttrs) {
        //去除高亮
        String replace = stringStringMap.replace("<span style='color:red'>", "").replace("</span>", "");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            //去除高亮后的数据转为List<Map<String, Object>>方便处理数据
            List<Map<String, Object>> dataList = objectMapper.readValue(replace, new TypeReference<List<Map<String, Object>>>() {
            });
            List<Map<String, Object>> dataList1 = objectMapper.readValue(stringStringMap, new TypeReference<List<Map<String, Object>>>() {
            });
            List<Map<String, Object>> dataList2= new ArrayList<>();
            Iterator<Map<String, Object>> mapIterator = dataList1.iterator();
            while (mapIterator.hasNext()) {
                //处理错误高亮
                Map<String, Object> map = mapIterator.next();
                Map<String, Object> map1 = new HashMap<>();
                for (String key : map.keySet()) {
                    String newKey = key.replace("<span style='color:red'>", "").replace("</span>", "");
                    map1.put(newKey,map.get(key));
                }
                dataList2.add(map1);
            }
            //筛选出业务业务类型下的业务类型属性并根据业务类型属性分组
            Map<Long, List<EcmAppAttr>> collect = ecmAppAttrs.stream().filter(p -> p.getAppCode().equals(appCode)).collect(Collectors.groupingBy(EcmAppAttr::getAppAttrId));
            if (!ObjectUtils.isEmpty(collect)) {
                Iterator<Map<String, Object>> iterator = dataList.iterator();
                Iterator<Map<String, Object>> iterator1 = dataList2.iterator();
                while (iterator.hasNext()) {
                    iterator1.next();
                    Map<String, Object> p = iterator.next();
                    String appAttrIdSt = String.valueOf(p.get("id"));
                    Long appAttrId = Long.valueOf(appAttrIdSt);
                    List<EcmAppAttr> ecmAppAttrs1 = collect.get(appAttrId);
                    if (!CollectionUtils.isEmpty(ecmAppAttrs1) && StateConstants.ZERO.equals(ecmAppAttrs1.get(StateConstants.ZERO).getQueryShow())) {
                        //如果queryShow是0则移除
                        iterator.remove();
                        iterator1.remove();
                    }
                }
            }
            return JsonUtils.toJSONString(dataList2);
        } catch (JsonProcessingException e) {
            log.error("json处理异常",e);
        }
        return null;
    }


    /**
     * 判断该es中存的业务类型属性是不是前端传过来的值
     *
     */
    private boolean handleAppAttrs(List<Map<String, Object>> dataList, List<Map<String, String>> attrMap) {
        //es中的数据与前端选择的业务类型属性匹配次数
        int num = StateConstants.ZERO;
        for (Map<String, Object> p : dataList) {
            //去除id的高亮，然后再比较
            String appAttrIdSt = String.valueOf(p.get("id")).replace("<span style='color:red'>", "").replace("</span>", "");
            String value = String.valueOf(p.get("value"));
            String inputType = String.valueOf(p.get("inputType"));
            Iterator<Map<String, String>> iterator = attrMap.iterator();
            while (iterator.hasNext()) {
                Map<String, String> map = iterator.next();
                if(map.get("id").equals(appAttrIdSt)) {
                    String type = map.get("type");
                    // 查询查询条件是否是时间类型，是则逗号分割时间
                    if("date".equals(type)) {
                        if(StringUtils.isNotBlank(String.valueOf(p.get("value"))) && StringUtils.isNotBlank(map.get("value")) && BusiInfoConstants.DATE_TYPE.toString().equals(inputType)){
                            String valueStr = String.valueOf(p.get("value")).replace("<span style='color:red'>", "").replace("</span>", "");
                            String[] attrTimeValueList = map.get("value").split(",");
                            if(TimeUtils.isInInterval(valueStr,attrTimeValueList[0],attrTimeValueList[1])){
                                ++num;
                            }
                        }
                    } else {
                        if (value.contains(map.get("value"))) {
                            ++num;
                        }
                    }
                }
            }
        }
        return attrMap.size() != num;
    }

    /**
     * 获取ES文件下信息
     */
    private EcmFileInfoEsDTO getEcmFileInfoEsDTO(EcmFileInfoDTO ecmFileInfoDTO) {
        AssertUtils.isNull(ecmFileInfoDTO.getFileId(), "参数错误，文件id不能为空");
        AssertUtils.isNull(ecmFileInfoDTO.getBusiId(), "参数错误");
        AssertUtils.isNull(ecmFileInfoDTO.getNewFileId(), "参数错误");
        EcmFileInfoEsDTO ecmFileInfoEsDTO = new EcmFileInfoEsDTO();
        ecmFileInfoEsDTO.setOrgCode(ecmFileInfoDTO.getOrgCode())
                .setFileName(ecmFileInfoDTO.getNewFileName())
                .setNewFileId(ecmFileInfoDTO.getNewFileId())
                .setFileId(ecmFileInfoDTO.getFileId())
                .setDocTypeName(ecmFileInfoDTO.getDocName())
                .setDocCode(ecmFileInfoDTO.getDocCode())
                .setFormat(ecmFileInfoDTO.getFormat())
                .setFileExif(ecmFileInfoDTO.getFileExif())
                .setNewFileSize(ecmFileInfoDTO.getNewFileSize())
                .setBusiId(ecmFileInfoDTO.getBusiId())
                .setBusiNo(ecmFileInfoDTO.getBusiNo())
                .setAppCode(ecmFileInfoDTO.getAppCode())
                .setAppTypeName(ecmFileInfoDTO.getAppTypeName())
                .setCreatUserName(ecmFileInfoDTO.getCreateUserName())
                .setUpdateUserName(ecmFileInfoDTO.getUpdateUserName())
                .setUpdateTime(ecmFileInfoDTO.getUpdateTime())
                .setCreateDate(ecmFileInfoDTO.getCreateTime());
        List<EcmFileLabel> ecmFileLabels = ecmFileInfoDTO.getEcmFileLabels();
        if(!CollectionUtils.isEmpty(ecmFileLabels)){
            List<String> lables =new ArrayList<>();
            for(EcmFileLabel label : ecmFileLabels){
                lables.add(label.getLabelName());
            }
            ecmFileInfoEsDTO.setFileLabel(lables);
        }
        return ecmFileInfoEsDTO;
    }

    /**
     * 添加业务信息到es
     */
    private Boolean uploadEs(Long id, Object object, Integer type, Long userId,boolean isSync,Long busiId) {
        boolean flag = false;
        LockInfo lockInfo = null;
        JSONObject ecmJson = JsonUtils.parseObject(object);
        Map map = JsonUtils.parseObject(JsonUtils.toJSONString(ecmJson), Map.class);
        if (StateConstants.ZERO.equals(type)) {
            EcmBusiInfoEsDTO ecmBusiInfoEsDTO = (EcmBusiInfoEsDTO) object;
            EsEcmBusi esEcmBusi = new EsEcmBusi();
            BeanUtils.copyProperties(ecmBusiInfoEsDTO, esEcmBusi);
            esEcmBusi.setId(esEcmBusi.getBusiId() + "");
            esEcmBusi.setBaseBizSourceId(esEcmBusi.getBusiId());
            esEcmBusi.setBaseBizSource(application);
            esEcmBusi.setBaseCreateTime(new Date());
            esEcmBusi.setBaseCreateUser(userId);
            esEcmBusiMapper.insert(esEcmBusi, bizIndex);
            flag = true;
            log.info("成功添加业务信息到es:{}", map);
        } else {
            try {
                //判断文件信息是否已经存在es中
                lockInfo = lockTemplate.lock(RedisConstants.LOCK_ES_FILE + id, expire, acquireTimeout);
                if (lockInfo != null) {
                    boolean doc = ecmEsUtils.isExistsDocument(fileIndex, "_doc", id + "");
                    if (doc) {
                        //使用脚本script 完成部分字段的更新
                        final String[] scriptCode = {" "};
                        final Integer[] a = {StateConstants.COMMON_ONE};
                        Map<String, Object> scriptParams = new HashMap<>();
                        //将实体类中的属性提取出，批量新增到es中
                        map.forEach((k, v) -> {
                            scriptCode[StateConstants.ZERO] = scriptCode[StateConstants.ZERO] + "ctx._source." + k + "=params." + "newValue" + a[StateConstants.ZERO] + ";";
                            scriptParams.put("newValue" + a[StateConstants.ZERO], v);
                            a[StateConstants.ZERO]++;
                        });
                        EcmFileInfoEsDTO esEcmFile = (EcmFileInfoEsDTO) object;
                        log.info("isSync : {} , doc : {},esEcmFile : {}",isSync,doc,esEcmFile);
                        String suffix = FilenameUtils.getExtension(esEcmFile.getFileName());
                        EsEcmFile baseFileObjEs = getBaseFileObjEsWithoutContext(id, esEcmFile.getFileName(), suffix,esEcmFile.getNewFileSize());
                        BeanUtils.copyProperties(esEcmFile, baseFileObjEs);
                        //设置fileExif
                        baseFileObjEs.setExif(JSON.toJSONString(esEcmFile.getFileExif()));
                        esEcmFileMapper.updateById(baseFileObjEs, fileIndex);
                        flag = true;
                        lockTemplate.releaseLock(lockInfo);
                        log.info("成功修改文件信息到es:{},id:{}", map, id);
                    } else {
                        EcmFileInfoEsDTO esEcmFile = (EcmFileInfoEsDTO) object;
                        log.info("isSync : {} , doc : {},esEcmFile : {}",isSync,doc,esEcmFile);
                        String suffix = FilenameUtils.getExtension(esEcmFile.getFileName());
                        Long newFileSize = esEcmFile.getNewFileSize();
                        //判断文件后缀来决定是否走管道
                        if (suffixArr.contains(suffix)) {
                            SysFileDTO sysFileDTO = fileHandleApi.getFileInfo(esEcmFile.getNewFileId()).getData();
                            String base64FileContent = Base64.getEncoder().encodeToString(getEsFileInfo(sysFileDTO));
                            EsEcmFile baseFileObjEs = getBaseFileObjEs(id, esEcmFile.getFileName(), suffix, base64FileContent,newFileSize);
                            BeanUtils.copyProperties(esEcmFile, baseFileObjEs);
                            //设置fileExif,并且把id设为null，id是easy-es才用的上得属性，因管道插入不走easy-es。所以得置空
                            baseFileObjEs.setId(null);
                            baseFileObjEs.setExif(JSON.toJSONString(esEcmFile.getFileExif()));
                            //查询获取文件byte[]
                            byte[] esFileInfo = getInitialEsFileInfo(sysFileDTO);
                            baseFileObjEs.setFileBytes(esFileInfo);
                            if(!CollectionUtils.isEmpty(esEcmFile.getFileLabel())){
                                baseFileObjEs.setFileLabel(JSON.toJSONString(esEcmFile.getFileLabel()));
                            }
                            ecmEsUtils.indexPipeline(filePipeline, id + "", baseFileObjEs, fileIndex,isSync);
                        } else {
                            EsEcmFile baseFileObjEs = getBaseFileObjEs(id, esEcmFile.getFileName(), suffix, null,newFileSize);
                            BeanUtils.copyProperties(esEcmFile, baseFileObjEs);
                            //设置fileExif
                            baseFileObjEs.setExif(JSON.toJSONString(esEcmFile.getFileExif()));
                            esEcmFileMapper.insert(baseFileObjEs, fileIndex);
                        }
                        flag = true;
                        lockTemplate.releaseLock(lockInfo);
                        log.info("成功添加文件信息到es:{}", map);
                    }
                }
                //更新文件es任务的状态
                updateEsFileTask(id,busiId,flag);
            }catch (Exception e){
                log.error("添加文件信息到es失败:{}", map, e);
                updateEsFileTask(id,busiId,false);
            }finally {
                if (lockInfo != null){
                    lockTemplate.releaseLock(lockInfo);
                }
            }
        }
        return flag;
    }

    private void updateEsFileTask(Long fileId, Long busiId, boolean flag) {
        String key = RedisConstants.BUSIASYNC_TASK_PREFIX + busiId;
        LockInfo lockInfo = null;
        try {
            // 加锁
            lockInfo = lockTemplate.lock(key + fileId, expire, acquireTimeout);
            EcmAsyncTask ecmAsyncTask =new EcmAsyncTask();
            if (lockInfo != null) {
                //修改异步任务为处理中
                ecmAsyncTask = busiCacheService.getEcmAsyncTask(key, fileId.toString());
                String taskType = ecmAsyncTask.getTaskType();
                char status = flag ? EcmCheckAsyncTaskEnum.SUCCESS.description().charAt(0) : EcmCheckAsyncTaskEnum.FAILED.description().charAt(0);
                taskType=updateStatus(taskType, Collections.singletonList(IcmsConstants.TYPE_ELEVEN), status);
                ecmAsyncTask.setTaskType(taskType);
                //设置是否失败状态
                int isFailStatus = flag && !taskType.contains(String.valueOf(IcmsConstants.TWO)) ? IcmsConstants.ONE : IcmsConstants.TWO;
                ecmAsyncTask.setIsFail(isFailStatus);
                asyncTaskService.updateEcmAsyncTask(ecmAsyncTask);
                //修改完毕释放锁
                lockTemplate.releaseLock(lockInfo);
            }
        }catch (Exception e){
            log.error("文件es状态更新失败,",  e);
        } finally {
            if (lockInfo != null){
                lockTemplate.releaseLock(lockInfo);
            }
        }

    }

    /**
     * 更新 RemakeStatus 指定位置的值
     * @param status   原始状态字符串
     * @param positions 需要更新的位置
     * @param newValue
     * @return 更新后的状态字符串
     */
    private String updateStatus(String status, List<Integer> positions, char newValue) {
        StringBuilder sb = new StringBuilder(status);
        positions.forEach(position->{
            if (position < 1 || position > IcmsConstants.LENGTH) {
                throw new IllegalArgumentException(
                        String.format("只能更新 1 到 %d 位", IcmsConstants.LENGTH)
                );
            }
            sb.setCharAt(position - 1, newValue);
        });
        return sb.toString();
    }

    /**
     * 生成文件索引内容
     *
     */
    private EsEcmFile getBaseFileObjEs(Long fileId, String fileName, String suffix, String base64FileContent,Long fileSize) {
        EsEcmFile baseFileObjEs = new EsEcmFile();
        baseFileObjEs.setId(null == fileId ? null : fileId + "");
        baseFileObjEs.setBaseBizSource(application);
        baseFileObjEs.setBaseBizSourceId(fileId);
        //目前跟sourceId一致
        baseFileObjEs.setFileId(fileId + "");
        baseFileObjEs.setFileName(fileName);
        baseFileObjEs.setFileSuffix(suffix.toLowerCase());
        //如是文本文件提取赋值
        baseFileObjEs.setTitle("");
        baseFileObjEs.setAbstracts("");
        //文件内容
        baseFileObjEs.setContent(base64FileContent);
        //文件大小
        baseFileObjEs.setNewFileSize(fileSize);
        return baseFileObjEs;
    }

    /**
     * 生成文件索引内容
     */
    private EsEcmFile getBaseFileObjEsWithoutContext(Long fileId, String fileName, String suffix,Long fileSize) {
        EsEcmFile baseFileObjEs = new EsEcmFile();
        baseFileObjEs.setId(null == fileId ? null : fileId + "");
        baseFileObjEs.setBaseBizSource(application);
        baseFileObjEs.setBaseBizSourceId(fileId);
        //目前跟sourceId一致
        baseFileObjEs.setFileId(fileId + "");
        baseFileObjEs.setFileName(fileName);
        baseFileObjEs.setFileSuffix(suffix.toLowerCase());
        //如是文本文件提取赋值
        baseFileObjEs.setTitle("");
        baseFileObjEs.setAbstracts("");
        //文件大小
        baseFileObjEs.setNewFileSize(fileSize);
        return baseFileObjEs;
    }

    /**
     * 获取文件byte[]
     *
     */
    private byte[] getEsFileInfo(SysFileDTO sysFileDTO) {
        FileByteVO fileByteVO = new FileByteVO();
        fileByteVO.setFileId(sysFileDTO.getId());
        fileByteVO.setOpenFlag(StateConstants.NO);
        //调用存储服务的获取文件字节流接口
        Result<byte[]> fileByteWater = fileHandleApi.getFileByteWater(fileByteVO);
        if (!Objects.isNull(fileByteWater) &&fileByteWater.isSucc()){
            return fileByteWater.getData();
        }
        return new byte[0];
    }

    /**
     * 获取原文件byte[]
     *
     */
    private byte[] getInitialEsFileInfo(SysFileDTO sysFileDTO) {
        FileByteVO fileByteVO = new FileByteVO();
        fileByteVO.setFileId(sysFileDTO.getId());
        //调用存储服务的获取文件字节流接口
        Result<byte[]> fileByteWater = fileHandleApi.getFileBytes(fileByteVO);
        if (!Objects.isNull(fileByteWater) &&fileByteWater.isSucc()){
            return fileByteWater.getData();
        }
        return new byte[0];
    }
    /**
     * 获取ES业务信息参数
     *
     */
    private EcmBusiInfoEsDTO getEcmBusiInfoEsDTO(EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        EcmBusiInfoEsDTO ecmBusiInfoEsDTO = new EcmBusiInfoEsDTO();
        String appAttrs = null;
        //处理数据：给creatUserName和appAttrs赋值
        //业务类型的属性
        List<Map<String, String>> mapList = new ArrayList<>();
        List<EcmAppAttrDTO> attrList = ecmBusiInfoExtend.getAttrList();
        if (!org.apache.commons.collections4.CollectionUtils.isEmpty(attrList)) {
            attrList.forEach(p -> {
                Map<String, String> appAttrIdMap = new HashMap<>();
                appAttrIdMap.put("id", String.valueOf(p.getAppAttrId()));
                appAttrIdMap.put("label", p.getAttrName());
                appAttrIdMap.put("value", p.getAppAttrValue());
                appAttrIdMap.put("inputType", String.valueOf(p.getInputType()));
                mapList.add(appAttrIdMap);
            });
            appAttrs = JsonUtils.toJSONString(mapList);
        }
        String creatUserName = ecmBusiInfoExtend.getCreateUserName() == null ? ecmBusiInfoExtend.getCreateUser() : ecmBusiInfoExtend.getCreateUserName();
        String updateUserName = ecmBusiInfoExtend.getUpdateUserName() == null ? ecmBusiInfoExtend.getUpdateUser() : ecmBusiInfoExtend.getUpdateUserName();
        ecmBusiInfoEsDTO.setOrgCode(ecmBusiInfoExtend.getOrgCode())
                .setBusiNo(ecmBusiInfoExtend.getBusiNo())
                .setBusiId(ecmBusiInfoExtend.getBusiId())
                .setAppCode(ecmBusiInfoExtend.getAppCode())
                .setAppTypeName(ecmBusiInfoExtend.getAppTypeName())
                .setAppAttrs(appAttrs)
                .setAppAttrMap(mapList)
                .setCreatUserName(creatUserName)
                .setUpdateUserName(updateUserName)
                .setUpdateTime(ecmBusiInfoExtend.getUpdateTime())
                .setCreateDate(ecmBusiInfoExtend.getCreateTime())
                .setIsDeleted(StateConstants.NO);
        return ecmBusiInfoEsDTO;
    }
    /**
     * 检查所有字段是否均为 null
     */
    private Boolean isHasQueryCriteria(EcmSearchVO ecmSearchVO) {
        EcmSearchVO ecmSearchVO1 = new EcmSearchVO();
        BeanUtils.copyProperties(ecmSearchVO, ecmSearchVO1);
        ecmSearchVO1.setPageNum(null);
        ecmSearchVO1.setPageSize(null);
        ecmSearchVO1.setType(null);
        return Arrays.stream(ecmSearchVO1.getClass().getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .allMatch(field -> {
                    try {
                        return ObjectUtils.isEmpty(field.get(ecmSearchVO1));
                    } catch (IllegalAccessException e) {
                        log.error("判断field是否为空异常",e);
                        return false;
                    }
                });
    }


    /**
     * 添加查看权限（readRight：0 无权限; readRight：1 有权限）
     */
    private void addPermission(List<Map<String, Object>> result, Integer code, String userId) {
        for (Map<String, Object> map : result) {
            if (StateConstants.ZERO.equals(code)) {
                //业务查询
                determinePermissions(userId, map);
            } else {
                //文档查询
                if (!ObjectUtils.isEmpty(map.get("docCode"))) {
                    if (!(String.valueOf(StateConstants.COMMON_TWO).equals(map.get("docCode")))) {
                        determinePermissions(userId, map);
                    } else {
                        //未归类文件都可以查看
                        map.put("readRight", StateConstants.COMMON_ONE);
                    }
                }
            }
        }
    }

    /**
     * 判断权限
     *
     */
    private void determinePermissions(String userId, Map<String, Object> map) {
        EcmAppDocright ecmAppDocright = getEcmAppDocright(userId, map);
        if (ObjectUtils.isEmpty(ecmAppDocright)) {
            //当前账号的角色没用查看权限
            map.put("readRight", StateConstants.ZERO);
            return;
        }
        //查看当前角色对应的业务类型、资料类型权限
        List<EcmDocrightDef> ecmDocrightDefList = getEcmDocrightDefs(map);
        if (CollectionUtils.isEmpty(ecmDocrightDefList)) {
            //当前账号的角色没用查看权限
            map.put("readRight", StateConstants.ZERO);
            return;
        }
        //判断是否所有资料权限的查看权限都为空
        Boolean a = checkAllDocRight(ecmDocrightDefList);
        if (a) {
            //当前账号的角色有查看权限
            map.put("readRight", StateConstants.COMMON_ONE);
        } else {
            //当前账号的角色没有查看权限
            map.put("readRight", StateConstants.ZERO);
        }
    }
    /**
     * 获取影像资料权限定义
     */
    private List<EcmDocrightDef> getEcmDocrightDefs(Map<String, Object> map) {
        LambdaQueryWrapper<EcmDocrightDef> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmDocrightDef::getDimType, IcmsConstants.ZERO);
        wrapper.eq(EcmDocrightDef::getAppCode, map.get("appCode"));
        List<EcmDocrightDef> ecmDocrightDefList = ecmDocRightDefMapper.selectList(wrapper);
        return ecmDocrightDefList;
    }
    /**
     * 获取影像业务资料权限版本
     */
    private EcmAppDocright getEcmAppDocright(String userId, Map<String, Object> map) {
        //查询当前用户的角色
        List<Long> roleIds = userApi.getRoleListByUsername(userId).getData();
        AssertUtils.isNull(roleIds, "参数错误，当前用户角色有误");
        //根据业务类型id获取当前资料权限版本
        LambdaQueryWrapper<EcmAppDocright> versionWrapper = new LambdaQueryWrapper<>();
        versionWrapper.eq(EcmAppDocright::getAppCode, map.get("appCode"));
        versionWrapper.eq(EcmAppDocright::getRightNew, IcmsConstants.ONE);
        EcmAppDocright ecmAppDocright = ecmAppDocrightMapper.selectOne(versionWrapper);
        return ecmAppDocright;
    }
    /*
      检查节点权限
     */
    private Boolean checkAllDocRight(List<EcmDocrightDef> ecmDocrightDefList) {
        Boolean a = false;
        for (EcmDocrightDefDTO right : BeanUtil.copyToList(ecmDocrightDefList, EcmDocrightDefDTO.class)) {
            if (StrUtil.equals(IcmsConstants.ONE.toString(), right.getReadRight())) {
                //当前账号的角色有查看权限
                a = true;
            }
        }
        return a;
    }

    /**
     * 筛选业务类型属性
     *
     */
    private void handleAppAttr(List<Map<String, Object>> list, List<Map<String, String>> attrMap) {
        if (!CollectionUtils.isEmpty(list) && !CollectionUtils.isEmpty(attrMap)) {
            //构建迭代器用于删除不符合要求的数据
            Iterator<Map<String, Object>> iterator = list.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> map = iterator.next();
                //获取es中存的业务类型属性
                String appAttrs = (String) map.get("appAttrs");
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    List<Map<String, Object>> dataList = objectMapper.readValue(appAttrs, new TypeReference<List<Map<String, Object>>>() {
                    });
                    if (!ObjectUtils.isEmpty(appAttrs)) {
                        //筛选业务属性
                        if (handleAppAttrs(dataList, attrMap)) {
                            iterator.remove();
                        }
                    } else {
                        iterator.remove();
                    }
                } catch (JsonProcessingException e) {
                    log.error("JSON处理异常",e);
                }
            }
        }
    }

    public boolean addEsFileInfoSync(EcmFileInfoDTO ecmFileInfoDTO,Long userId){
        AssertUtils.isNull(ecmFileInfoDTO.getNewFileId(), "参数错误，新文件id不能为空");
        log.info("ecmFileInfoDTO : {}", ecmFileInfoDTO);
        //得到要存到es中的业务信息
        EcmFileInfoEsDTO ecmFileInfoEsDTO = getEcmFileInfoEsDTO(ecmFileInfoDTO);
        //上传到es
        return uploadEs(ecmFileInfoDTO.getFileId(), ecmFileInfoEsDTO, StateConstants.COMMON_ONE, userId,true,ecmFileInfoDTO.getBusiId());
    }

    public String getFileContent(EcmFileInfoDTO ecmFileInfoDTO){
        SysFileDTO sysFileDTO = fileHandleApi.getFileInfo(ecmFileInfoDTO.getNewFileId()).getData();
        //查询获取文件byte[]
        byte[] esFileInfo = getInitialEsFileInfo(sysFileDTO);
        return ecmEsUtils.getFileContext(esFileInfo,ecmFileInfoDTO.getNewFileName());
    }


    /**
     * 添加es任务
     *
     * @param busiId
     * @param userId
     */
    private void saveEsAsyncTask(Long busiId, Long userId) {
        EcmEsAsyncTask task = new EcmEsAsyncTask();
        task.setBusiId(busiId);
        task.setStatus(IcmsConstants.ZERO);
        task.setRetryCount(IcmsConstants.ZERO);
        task.setTokenId(userId);

        try {
            ecmEsAsyncTaskMapper.insert(task);
        } catch (Exception e) {
            log.debug("ES 任务已存在，busiId: {}", busiId);
        }
    }
    /**
     * 更新es任务
     */
    private void updateEsTaskStatus(Long busiId, Integer status, String failReason) {
        EcmEsAsyncTask task = new EcmEsAsyncTask();
        task.setBusiId(busiId);
        task.setStatus(status);

        // 只有失败时才记录原因（避免覆盖之前的错误信息）
        if (IcmsConstants.TWO.equals(status) && StringUtils.isNotBlank(failReason)) {
            // 截断过长的异常信息（防止 DB 字段溢出）
            task.setFailReason(failReason.length() > 500 ? failReason.substring(0, 500) : failReason);
        }

        ecmEsAsyncTaskMapper.update(
                task,
                new LambdaUpdateWrapper<EcmEsAsyncTask>()
                        .eq(EcmEsAsyncTask::getBusiId, busiId)
        );
    }

}
