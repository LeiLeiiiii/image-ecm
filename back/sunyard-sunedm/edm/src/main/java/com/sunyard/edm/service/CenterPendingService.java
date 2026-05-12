package com.sunyard.edm.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shiro.util.Assert;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageInfo;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.constant.DocDictionaryKeyConstants;
import com.sunyard.edm.dto.DocBsCompanyGroundingDTO;
import com.sunyard.edm.mapper.DocBsDocFlowMapper;
import com.sunyard.edm.mapper.DocBsDocumentMapper;
import com.sunyard.edm.mapper.DocSysTeamUserMapper;
import com.sunyard.edm.po.DocBsDocFlow;
import com.sunyard.edm.po.DocBsDocument;
import com.sunyard.edm.po.DocSysTeamUser;
import com.sunyard.edm.util.DocUtils;
import com.sunyard.edm.vo.DocBsCompanyGroundingVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.module.system.api.DictionaryApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysUserDTO;

/**
 * @Author PJW 2022/12/26 15:34
 * @DESC 文档中心-未上架文档实现类
 */
@Service
public class CenterPendingService {
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private DocBsDocumentMapper docBsDocumentMapper;
    @Resource
    private DocSysTeamUserMapper docSysTeamUserMapper;
    @Resource
    private DictionaryApi dictionaryApi;
    @Resource
    private UserApi userApi;

    /**
     * 文档库列表
     */
    public Result search(DocBsCompanyGroundingVO v, PageForm p) {
        Assert.notNull(v.getType(), "参数错误");
        //如果没传文档类型则直接返回空
        if (CollectionUtils.isEmpty(v.getDocType())) {
            return Result.success(new PageInfo<>());
        }

        if (!ObjectUtils.isEmpty(v.getUploadTimeDo())) {
            Date createEndDate = v.getUploadTimeDo();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(createEndDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date tomorrow = calendar.getTime();
            v.setUploadTimeDo(tomorrow);
        }
        if (!ObjectUtils.isEmpty(v.getLowerTimeDo())) {
            Date createEndDate = v.getLowerTimeDo();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(createEndDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date tomorrow = calendar.getTime();
            v.setLowerTimeDo(tomorrow);
        }
        Integer docStatus;
        if (0 == v.getType()) {
            //val传‘待上架’的文档状态集
            docStatus=DocConstants.DOC_STATUS_NOPUTAWAY;
        } else if (1 == v.getType()) {
            //val传‘已下架’的文档状态集
            docStatus=DocConstants.DOC_STATUS_OUT;
        } else {
            return Result.error("参数错误", ResultCode.PARAM_ERROR);
        }
        List<Long> userIdList = new ArrayList<>();
        userIdList.add(v.getUserId());
        userIdList.add(v.getDeptId());
        userIdList.add(v.getInstId());
        List<DocSysTeamUser> teamUserList = docSysTeamUserMapper.selectList(new LambdaQueryWrapper<DocSysTeamUser>().eq(DocSysTeamUser::getUserId, v.getUserId()));
        teamUserList.forEach(item -> userIdList.add(item.getTeamId()));
        if (CollectionUtils.isEmpty(userIdList)) {
            return Result.success(new PageInfo<>());
        }
        List<DocBsCompanyGroundingDTO> filterResult = new ArrayList<>();
        List<DocBsCompanyGroundingDTO> result = docBsDocumentMapper.queryGrounding(userIdList, v.getTagIdList(),
                docStatus,v.getDocName(),v.getUploadTimeTo(),v.getUploadTimeDo(),
                v.getLowerTimeTo(),v.getLowerTimeDo(),v.getUploadTimeSort(),v.getLowerTimeSort());
        if (!CollectionUtils.isEmpty(result)) {
            //如果‘文档格式’存在6
            if (v.getDocType().contains(Integer.valueOf(DocConstants.DOC_COMMON_SUFFIX_OTHER))) {
                List<String> allSuffixList = getAllSuffixList(v.getDocType());
                if (!CollectionUtils.isEmpty(allSuffixList)) {
                    result = result.stream().filter(r -> !allSuffixList.contains(r.getDocSuffix())).collect(Collectors.toList());
                }
            } else {
                List<String> suffixList = getSuffixList(v.getDocType());
                if (!CollectionUtils.isEmpty(suffixList)) {
                    result = result.stream().filter(r -> suffixList.contains(r.getDocSuffix())).collect(Collectors.toList());
                }
            }
            if(!CollectionUtils.isEmpty(result)){
                Set<Long> collect = result.stream()
                        .map(DocBsCompanyGroundingDTO::getDocOwner)
                        .collect(Collectors.toSet());
                Result<List<SysUserDTO>> userListByUserIds = userApi.getUserListByUserIds(collect.toArray(new Long[0]));
                List<SysUserDTO> data = userListByUserIds.getData();
                Map<Long, String> userMap = data.stream()
                        .collect(Collectors.toMap(SysUserDTO::getUserId, SysUserDTO::getName));
                for (DocBsCompanyGroundingDTO docBsCompanyGroundingDTO : result) {
                    Long docOwner = docBsCompanyGroundingDTO.getDocOwner();
                    if (userMap.containsKey(docOwner)) {
                        docBsCompanyGroundingDTO.setOwner(userMap.get(docOwner));
                    }
                }

                if(StringUtils.isBlank(v.getOwner())) {
                    filterResult = result;
                } else {
                    String owner = v.getOwner().trim();
                    filterResult = result.stream()
                            .filter(doc -> doc.getOwner() != null &&
                                    doc.getOwner().contains(owner)
                            ).collect(Collectors.toList());
                }
            }
        }
        handleDocSize(filterResult);
        //手动分页
        int startIndex = (p.getPageNum() - 1) * p.getPageSize();
        int endIndex = Math.min(startIndex + p.getPageSize(), filterResult.size());
        List<DocBsCompanyGroundingDTO> pageList = filterResult.subList(startIndex, endIndex);
        PageInfo<DocBsCompanyGroundingDTO> pageInfo = new PageInfo<>(pageList);
        pageInfo.setTotal(filterResult.size());
        pageInfo.setPageNum(p.getPageNum());
        pageInfo.setPageSize(p.getPageSize());
        return Result.success(pageInfo);
    }

    /**
     * 重新上架
     */
    public Result reGrounding(Long[] busIds, AccountToken token) {
        Assert.notEmpty(busIds, "参数错误");

        docBsDocumentMapper.update(null, new LambdaUpdateWrapper<DocBsDocument>()
                .set(DocBsDocument::getDocStatus, DocConstants.DOC_STATUS_PUTAWAY)
                .in(DocBsDocument::getBusId, busIds));
        //添加文档动态
        List<DocBsDocFlow> docBsDocFlows = new ArrayList<>();
        for (Long id : busIds) {
            DocBsDocFlow docBsDocFlow = new DocBsDocFlow();
            docBsDocFlow.setDocId(id);
            docBsDocFlow.setUserId(token.getId());
            docBsDocFlow.setFlowDate(new Date());
            docBsDocFlow.setFlowDescribe("重新上架");
            docBsDocFlow.setFlowType(DocConstants.FLOW_TYPE_ON_SHELF);
            docBsDocFlows.add(docBsDocFlow);
        }
        MybatisBatch<DocBsDocFlow> docBatchs = new MybatisBatch<>(sqlSessionFactory, docBsDocFlows);
        MybatisBatch.Method<DocBsDocFlow> docMethod = new MybatisBatch.Method<>(DocBsDocFlowMapper.class);
        docBatchs.execute(docMethod.insert());
        return Result.success(true);
    }

    /**
     * 根据传入的docType 拿到后缀list
     */
    private List<String> getSuffixList(List<Integer> docType) {
        List<String> suffixList = new ArrayList<>();
        Result<Map<String, String>> mapResult = dictionaryApi.searchValExtraMapByParentKey(DocDictionaryKeyConstants.DOC_COMMON_SUFFIX);
        Map<String, String> map = mapResult.getData();
        docType.forEach(item -> {
            suffixList.addAll(Arrays.asList(map.get(String.valueOf(item)).split(",")));
        });
        return suffixList;
    }

    /**
     * 根据传入的docType，过滤掉当前后缀list
     *
     * @return
     */
    private List<String> getAllSuffixList(List<Integer> docType) {
        List<String> suffixList = new ArrayList<>();
        Result<Map<String, String>> mapResult = dictionaryApi.searchValExtraMapByParentKey(DocDictionaryKeyConstants.DOC_COMMON_SUFFIX);
        Map<String, String> map = mapResult.getData();
        docType.forEach(item -> {
            map.remove(String.valueOf(item));
        });
        map.forEach((key, value) -> {
            suffixList.addAll(Arrays.asList(map.get(String.valueOf(key)).split(",")));

        });
        return suffixList;
    }

    /**
     * 计算size大小
     */
    private void handleDocSize(List<DocBsCompanyGroundingDTO> docBsDocuments) {
        for (DocBsCompanyGroundingDTO documentExtend : docBsDocuments) {
            if (!ObjectUtils.isEmpty(documentExtend.getDocSize())) {
                documentExtend.setDocSizeStr(DocUtils.getFilseSize(Long.parseLong(documentExtend.getDocSize())));
            }
        }
    }

}
