package com.sunyard.edm.service;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageInfo;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.constant.DocDictionaryKeyConstants;
import com.sunyard.edm.dto.DocBsCollectionDTO;
import com.sunyard.edm.mapper.DocBsCollectionMapper;
import com.sunyard.edm.mapper.DocBsDocumentMapper;
import com.sunyard.edm.mapper.DocSysTeamUserMapper;
import com.sunyard.edm.po.DocBsCollection;
import com.sunyard.edm.po.DocBsDocument;
import com.sunyard.edm.po.DocSysTeamUser;
import com.sunyard.edm.util.DocUtils;
import com.sunyard.edm.vo.DocBsCollectionVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.DictionaryApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysUserDTO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shiro.util.Assert;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author PJW 2022/12/16 10:25
 * @DESC 首页-我的收藏实现
 */
@Service
public class CenterCollectionService {

    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private DocBsDocumentMapper docBsDocumentMapper;
    @Resource
    private DocBsCollectionMapper docBsCollectionMapper;
    @Resource
    private DocSysTeamUserMapper docSysTeamUserMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private DictionaryApi sysDictionaryService;
    /**
     * 收藏列表
     */
    public Result query(DocBsCollectionVO c, PageForm p) {
        //如果没传文档类型则直接返回空
        if (CollectionUtils.isEmpty(c.getDocType())) {
            return Result.success(new PageInfo<>());
        }
        //校验当前用户是否有收藏
        List<DocBsCollection> docBsCollectionList = docBsCollectionMapper.selectList(new LambdaQueryWrapper<DocBsCollection>().eq(DocBsCollection::getUserId, c.getUserId()));
        if (CollectionUtils.isEmpty(docBsCollectionList)) {
            return Result.success(new PageInfo<>());
        }
        //拿到父级目录id集合
        List<Long> docIdList = docBsCollectionList.stream().map(DocBsCollection::getDocId).distinct().collect(Collectors.toList());
        List<DocBsDocument> documentList = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>().in(DocBsDocument::getBusId, docIdList));
        if (CollectionUtils.isEmpty(documentList)) {
            return Result.success(new PageInfo<>());
        }
        List<Long> parentIdList = documentList.stream().map(item -> {
            if (ObjectUtils.isEmpty(item.getParentId())) {
                if(ObjectUtils.isEmpty(item.getFolderId())){
                    return item.getBusId();
                }else {
                    return item.getFolderId();
                }
            }
            return item.getParentId();
        }).distinct().collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(c.getCollectionTimeDo())) {
            Date createEndDate = c.getCollectionTimeDo();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(createEndDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            //这是明天
            Date tomorrow = calendar.getTime();
            c.setCollectionTimeDo(tomorrow);
        }

        List<Long> idList = new ArrayList<>();
        idList.add(c.getUserId());
        idList.add(c.getDeptId());
        idList.add(c.getInstId());
        List<DocSysTeamUser> teamUserList = docSysTeamUserMapper.selectList(new LambdaQueryWrapper<DocSysTeamUser>().eq(DocSysTeamUser::getUserId, c.getUserId()));
        teamUserList.forEach(item -> idList.add(item.getTeamId()));
        if (CollectionUtils.isEmpty(idList)) {
            return Result.success(new PageInfo<>());
        }

        List<DocBsCollectionDTO> result = docBsCollectionMapper.searchListExtend(c.getUserId(), parentIdList, idList, c.getTagIdList(),
                c.getDocName(),c.getOwner(),c.getCollectionTimeTo(),c.getCollectionTimeDo(),c.getCollectionTimeSort(),
                c.getUpdateTimeSort()
        );
        if (CollectionUtils.isEmpty(result)) {
            return Result.success(new PageInfo<>());
        }

        List<DocBsCollectionDTO> collect = result.stream().filter(s -> s.getIsDeleted().equals(DocConstants.DELETED_NO)).collect(Collectors.toList());
        //如果‘文档格式’存在6
        if (c.getDocType().contains(Integer.valueOf(DocConstants.DOC_COMMON_SUFFIX_OTHER))) {
            List<String> allSuffixList = getAllSuffixList(c.getDocType());
            if (!CollectionUtils.isEmpty(allSuffixList)) {
                collect= collect.stream().filter(s-> !allSuffixList.contains(s.getDocSuffix())||s.getDocSuffix()==null).collect(Collectors.toList());
            }
        } else {
            List<String> suffixList = getSuffixList(c.getDocType());
            if(!CollectionUtils.isEmpty(suffixList)){
                collect=collect.stream().filter(s-> suffixList.contains(s.getDocSuffix())).collect(Collectors.toList());

            }
        }
        handleDocSize(collect);
        collect = handleUserName(collect, c.getOwner());
        //手动分页
        int startIndex = (p.getPageNum() - 1) * p.getPageSize();
        int endIndex = Math.min(startIndex + p.getPageSize(), collect.size());

        List<DocBsCollectionDTO> pageList = collect.subList(startIndex, endIndex);
        PageInfo<DocBsCollectionDTO> pageInfo = new PageInfo<>(pageList);
        pageInfo.setTotal(collect.size());
        pageInfo.setPageNum(p.getPageNum());
        pageInfo.setPageSize(p.getPageSize());

        return Result.success(pageInfo);

    }

    /**
     * 添加收藏
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#userId")
    public Result addCollection(Long[] docId, Long userId) {
        Assert.notEmpty(docId, "参数错误");
        //如果已经收藏，则进行返回
        Long aLong = docBsCollectionMapper.selectCount(new LambdaQueryWrapper<DocBsCollection>().in(DocBsCollection::getDocId, Arrays.asList(docId)).eq(DocBsCollection::getUserId, userId));
        Assert.isTrue(aLong.intValue() == 0, "存在收藏记录，无法收藏");
        List<DocBsCollection> docBsCollectionList = Arrays.asList(docId).stream().map(item -> {
            DocBsCollection docBsCollection = new DocBsCollection();
            docBsCollection.setCollectionId(snowflakeUtil.nextId());
            docBsCollection.setUserId(userId);
            docBsCollection.setDocId(item);
            return docBsCollection;
        }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(docBsCollectionList)) {
            return Result.success(true);
        }

        MybatisBatch<DocBsCollection> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, docBsCollectionList);
        MybatisBatch.Method<DocBsCollection> method = new MybatisBatch.Method<>(DocBsCollectionMapper.class);
        mybatisBatch.execute(method.insert());
        return Result.success(true);
    }

    /**
     * 取消收藏
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#docId")
    public Result cancelCollection(Long docId) {
        Assert.notNull(docId, "参数错误");
        docBsCollectionMapper.deleteById(docId);
        return Result.success(true);
    }

    /**
     * 根据传入的docType 拿到后缀list
     */
    private List<String> getSuffixList(List<Integer> docType) {
        List<String> suffixList = new ArrayList<>();
        Result<Map<String, String>> nameByKey = sysDictionaryService.searchValExtraMapByParentKey(DocDictionaryKeyConstants.DOC_COMMON_SUFFIX);
        Map<String, String> map = nameByKey.getData();
        docType.forEach(item -> {
            suffixList.addAll(Arrays.asList(map.get(String.valueOf(item)).split(",")));
        });
        return suffixList;
    }

    /**
     * 根据传入的docType，过滤掉当前后缀list
     */
    private List<String> getAllSuffixList(List<Integer> docType) {
        List<String> suffixList = new ArrayList<>();
        Result<Map<String, String>> nameByKey = sysDictionaryService.searchValExtraMapByParentKey(DocDictionaryKeyConstants.DOC_COMMON_SUFFIX);
        Map<String, String> map = nameByKey.getData();
        docType.forEach(item -> {
            map.remove(String.valueOf(item));
        });
        map.forEach((key, value) -> {
            suffixList.addAll(Arrays.asList(map.get(String.valueOf(key)).split(",")));

        });
        return suffixList;
    }

    /**
     * 计算文件大小
     */
    private void handleDocSize(List<DocBsCollectionDTO> docBsDocuments) {
        for (DocBsCollectionDTO documentExtend : docBsDocuments) {
            if (!ObjectUtils.isEmpty(documentExtend.getDocSize())) {
                documentExtend.setDocSizeStr(DocUtils.getFilseSize(Long.parseLong(documentExtend.getDocSize())));
            }
        }
    }

    /**
     * 处理所有者
     */
    private List<DocBsCollectionDTO> handleUserName(List<DocBsCollectionDTO> docBsDocuments, String owner){
        List<DocBsCollectionDTO> filteredDocuments = new ArrayList<>();
        Set<Long> collect = docBsDocuments.stream()
                .map(DocBsCollectionDTO::getDocOwnerName)
                .filter(Objects::nonNull)
                .map(Long::valueOf).collect(Collectors.toSet());
        Result<List<SysUserDTO>> userListByUserIds = userApi.getUserListByUserIds(collect.toArray(new Long[0]));
        List<SysUserDTO> data = userListByUserIds.getData();
        Map<Long, String> userMap = data.stream()
                    .collect(Collectors.toMap(SysUserDTO::getUserId, SysUserDTO::getName));

        docBsDocuments.forEach(item ->{
            if(!StringUtils.isEmpty(item.getDocOwnerName())){
                item.setDocOwnerName(userMap.get(Long.valueOf(item.getDocOwnerName())));
            }
        });
        if(StringUtils.isBlank(owner)) {
            filteredDocuments = docBsDocuments;
        } else {
            String finalOwner = owner.trim();
            filteredDocuments = docBsDocuments.stream()
                    .filter(doc -> doc.getDocOwnerName() != null &&
                            doc.getDocOwnerName().contains(finalOwner)
                    ).collect(Collectors.toList());
        }
        return filteredDocuments;
    }
}
