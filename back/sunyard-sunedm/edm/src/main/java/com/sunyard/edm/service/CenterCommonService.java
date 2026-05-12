package com.sunyard.edm.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.sunyard.edm.dto.DocBsDocumentSearchDTO;
import com.sunyard.edm.dto.ExtendPageDTO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.constant.DocDictionaryKeyConstants;
import com.sunyard.edm.dto.DocBsDocumentDTO;
import com.sunyard.edm.dto.DocBsDocumentUserDTO;
import com.sunyard.edm.dto.DocSysHouseDTO;
import com.sunyard.edm.dto.DocSysTeamDTO;
import com.sunyard.edm.dto.UserTeamDeptDTO;
import com.sunyard.edm.dto.UserTeamDeptInitDTO;
import com.sunyard.edm.mapper.DocBsCollectionMapper;
import com.sunyard.edm.mapper.DocBsDocFlowMapper;
import com.sunyard.edm.mapper.DocBsDocumentMapper;
import com.sunyard.edm.mapper.DocBsDocumentTreeMapper;
import com.sunyard.edm.mapper.DocBsDocumentUserMapper;
import com.sunyard.edm.mapper.DocSysHouseMapper;
import com.sunyard.edm.mapper.DocSysHouseUserMapper;
import com.sunyard.edm.mapper.DocSysTeamMapper;
import com.sunyard.edm.mapper.DocSysTeamUserMapper;
import com.sunyard.edm.po.DocBsCollection;
import com.sunyard.edm.po.DocBsDocFlow;
import com.sunyard.edm.po.DocBsDocument;
import com.sunyard.edm.po.DocBsDocumentTree;
import com.sunyard.edm.po.DocBsDocumentUser;
import com.sunyard.edm.po.DocSysHouse;
import com.sunyard.edm.po.DocSysHouseUser;
import com.sunyard.edm.po.DocSysTeam;
import com.sunyard.edm.po.DocSysTeamUser;
import com.sunyard.edm.util.DocUtils;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.module.system.api.DeptApi;
import com.sunyard.module.system.api.DictionaryApi;
import com.sunyard.module.system.api.InstApi;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysDeptDTO;
import com.sunyard.module.system.api.dto.SysDictionaryDTO;
import com.sunyard.module.system.api.dto.SysInstDTO;
import com.sunyard.module.system.api.dto.SysOrgDTO;
import com.sunyard.module.system.api.dto.SysParamDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;

/**
 * @Author PJW 2022/12/14 10:05
 * @DESC 通用方法实现类
 */
@Service
public class CenterCommonService {
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private DocSysTeamUserMapper docSysTeamUserMapper;
    @Resource
    private DocSysHouseMapper docSysHouseMapper;
    @Resource
    private DocBsDocumentUserMapper docBsDocumentUserMapper;
    @Resource
    private DocSysHouseUserMapper docSysHouseUserMapper;
    @Resource
    private DocSysTeamMapper docSysTeamMapper;
    @Resource
    private DocBsCollectionMapper docBsCollectionMapper;
    @Resource
    private DocBsDocumentMapper docBsDocumentMapper;
    @Resource
    private DocBsDocumentTreeMapper docBsDocumentTreeMapper;
    @Resource
    private DocBsDocFlowMapper docBsDocFlowMapper;
    @Resource
    private ParamApi paramApi;
    @Resource
    private UserApi userApi;
    @Resource
    private InstApi instApi;
    @Resource
    private DeptApi deptApi;
    @Resource
    private DictionaryApi sysDictionaryService;
    /**
     * 后缀处理
     */
    private List<String> handleSuffix(List<String> strings, Map<String, SysDictionaryDTO> stringSysDictionaryMap) {
        ArrayList<String> ret = new ArrayList();
        for (String dic : strings) {
            if (dic.equals(DocConstants.DOC_COMMON_SUFFIX_OTHER)) {
                continue;
            }
            SysDictionaryDTO sysDictionary = stringSysDictionaryMap.get(dic);
            if (sysDictionary != null && !StringUtils.isEmpty(sysDictionary.getDicExtra())) {
                String[] split = sysDictionary.getDicExtra().split(",");
                ret.addAll(Arrays.asList(split.clone()));
            }
        }
        return ret;
    }


    /**
     * 后缀处理
     */
    private List<String> handleSuffixAll(Map<String, SysDictionaryDTO> stringSysDictionaryMap) {
        ArrayList<String> ret = new ArrayList();
        Set<String> strings = stringSysDictionaryMap.keySet();
        for (String dic : strings) {
            if (dic.equals(DocConstants.DOC_COMMON_SUFFIX_OTHER)) {
                continue;
            }
            SysDictionaryDTO sysDictionary = stringSysDictionaryMap.get(dic);
            String[] split = sysDictionary.getDicExtra().split(",");
            ret.addAll(Arrays.asList(split.clone()));
        }
        return ret;
    }

    /**
     * 后缀统一处理查询处理
     */
    public <T> void handleSuffixSearch(String suffix, DocBsDocumentSearchDTO searchDTO) {
        List<String> list = Arrays.asList(suffix.split(","));
        Result<Map<String, SysDictionaryDTO>> mapResult = sysDictionaryService.selectDictionByKeyMap(DocDictionaryKeyConstants.DOC_COMMON_SUFFIX, null);
        Map<String, SysDictionaryDTO> stringSysDictionaryMap = mapResult.getData();
        List<String> list1 = handleSuffixAll(stringSysDictionaryMap);
        List<String> strings = handleSuffix(list, stringSysDictionaryMap);
        list1.removeIf(s -> list.contains(s));
        // @todo  注意下面这一行
        if (list.contains(DocConstants.DOC_COMMON_SUFFIX_OTHER)) {
            searchDTO.setContains(true);
            searchDTO.setSuffixSize(list.size());
            searchDTO.setSuffixAllList(list1);

           /* if (list.size() == 1) {
                like.and(wp -> wp.isNull("a.doc_suffix").or().notIn("a.doc_suffix", list1));
            } else {*/
            //文件夹放在其他
            List<String> collect = new ArrayList();
            stringSysDictionaryMap.values().stream().filter(s -> !StringUtils.isEmpty(s.getDicExtra())).forEach(s -> {
                collect.addAll(Arrays.asList(s.getDicExtra().split(",")));
            });
            collect.removeIf(s -> strings.contains(s));
            searchDTO.setDicExtraList(collect);
               /* if (!CollectionUtils.isEmpty(collect)) {
                    like.and(wp -> wp.notIn(!org.apache.shiro.util.CollectionUtils.isEmpty(collect), "a.doc_suffix", collect)
                            .or().isNull("a.doc_suffix").or().notIn("a.doc_suffix", list1));
                }*/
            /* }*/
        } else {
            searchDTO.setType(DocConstants.FOLDER);
            searchDTO.setSuffixList(strings);
            /*like.ne("a.type", DocConstants.FOLDER);
            if (!CollectionUtils.isEmpty(strings)) {
                like.in("a.doc_suffix", strings);
            }*/
        }

    }

    /**
     * 所有者字符串转换
     */
    public void handleOwnStr(List<DocBsDocumentDTO> docBsDocuments) {
        if (CollectionUtils.isEmpty(docBsDocuments)) {
            return;
        }

        List<Long> owns = docBsDocuments.stream().filter(s -> s.getDocOwner() != null)
                .map(DocBsDocumentDTO::getDocOwner).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(owns)) {
            return;
        }
        Result<List<SysUserDTO>> sysUsers = userApi.getUserListByUserIds(owns.toArray(new Long[owns.size()]));
        Map<Long, List<SysUserDTO>> collect = sysUsers.getData().stream().collect(Collectors.groupingBy(SysUserDTO::getUserId));
        for (DocBsDocumentDTO documentExtend : docBsDocuments) {
            if (documentExtend.getDocOwner() != null) {
                List<SysUserDTO> sysUsers1 = collect.get(documentExtend.getDocOwner());
                if (CollectionUtils.isEmpty(sysUsers1)) {
                    continue;
                }
                documentExtend.setDocOwnerStr(sysUsers1.get(0).getName());
            }
        }
    }

    /**
     * 获取权限树
     */
    public UserTeamDeptDTO getUserByDeptOrTeam(UserTeamDeptDTO extend, PageForm pageForm, AccountToken token) {
        UserTeamDeptDTO userTeamDeptDTO = new UserTeamDeptDTO();

        //组织
        if (DocConstants.INST.equals(extend.getType())) {
            handleSetDeptInst(token, userTeamDeptDTO);
        }

        //团队列表
        if (DocConstants.TEAM.equals(extend.getType())) {
            PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
            List<DocSysTeam> docSysTeams = docSysTeamMapper.selectList(new LambdaQueryWrapper<DocSysTeam>()
                    .like(!ObjectUtils.isEmpty(extend.getName()),DocSysTeam::getTeamName, extend.getName()));
            PageInfo<DocSysTeamDTO> pageInfo = PageCopyListUtils.getPageInfo(new PageInfo<>(docSysTeams), DocSysTeamDTO.class);
            List<DocSysTeamDTO> sysTeamExtendList = pageInfo.getList();
            sysTeamExtendList.forEach(s -> {
                s.setRelId(s.getTeamId());
                s.setRelType(DocConstants.TEAM);
            });
            userTeamDeptDTO.setTeamPageInfo(pageInfo);
        }


        //用户列表
        if (DocConstants.USER.equals(extend.getType())) {
            Result<List<SysUserDTO>> userDetailByName = userApi.getUserDetailByName(extend.getName());
            List<SysUserDTO> instId = userDetailByName.getData().stream()
                    .filter(user -> token.getInstId().equals(user.getInstId()))
                    .collect(Collectors.toList());
            int startIndex = (pageForm.getPageNum() - 1) * pageForm.getPageSize();
            int endIndex = Math.min(startIndex + pageForm.getPageSize(), instId.size());
            List<SysUserDTO> sysUserDTOS = PageCopyListUtils.copyListProperties(instId.stream()
                    .skip(startIndex)
                    .limit(endIndex - startIndex)
                    .collect(Collectors.toList()), SysUserDTO.class);
            PageInfo<SysUserDTO> pageInfo = new PageInfo<>();
            pageInfo.setList(sysUserDTOS);
            pageInfo.setPageNum(pageForm.getPageNum());
            pageInfo.setPageSize(pageForm.getPageSize());
            pageInfo.setTotal(instId.size());
            //处理权限选择时所属组织展示
            List<SysUserDTO> sysUserList = pageInfo.getList();
            for (SysUserDTO user : sysUserList) {
                user.setRelType(DocConstants.USER);
                user.setRelId(user.getUserId());
                Result<SysInstDTO> sysInstList = instApi.getInstByInstId(user.getInstId());
                Result<List<SysDeptDTO>> sysDeptList = deptApi.selectByIds((new Long[]{user.getDeptId()}));
                String instStr = "";
                String deptStr;
                if (!ObjectUtils.isEmpty(sysInstList.getData())) {
                    instStr = sysInstList.getData().getNameLevel();
                }
                if (!CollectionUtils.isEmpty(sysDeptList.getData())) {
                    deptStr = sysDeptList.getData().get(0).getNameLevel();
                    instStr = instStr.replaceAll("-", "/") + "/" + deptStr.replaceAll("-", "/");
                }
                user.setOrganization(instStr.replaceAll("-", "/"));
            }

            userTeamDeptDTO.setUserPageInfo(pageInfo);
        }

        return userTeamDeptDTO;
    }

    private void handleSetDeptInst(AccountToken token, UserTeamDeptDTO userTeamDeptDTO) {
        Set<SysOrgDTO> list = new HashSet<>();
        Result<List<SysOrgDTO>> instList = instApi.searchInstTree(token.getInstId());
        Result<List<SysOrgDTO>> deptList = deptApi.searchDeptTree(token.getInstId());
        List<Long> collect = instList.getData().stream().map(SysOrgDTO::getInstId).collect(Collectors.toList());

        instList.getData().forEach(s -> {
            if (!collect.contains(s.getParentId())) {
                s.setParentId(DocConstants.ZERO.longValue());
            }
            s.setType(DocConstants.INST);
            s.setRelType(DocConstants.INST);
        });
        deptList.getData().forEach(s -> {
            s.setType(DocConstants.DEPT);
            s.setRelType(DocConstants.DEPT);
        });

        list.addAll(instList.getData());
        list.addAll(deptList.getData());
        userTeamDeptDTO.setSysOrgExtends(new ArrayList<>(list));
    }

    /**
     * 根据文件夹id获取全目录
     */
    public String handleFolderAll(Long folderId) {
        docBsDocumentMapper.selectById(folderId);
        List<DocBsDocumentTree> fatherIds = docBsDocumentTreeMapper.selectList(new LambdaQueryWrapper<DocBsDocumentTree>()
                .eq(DocBsDocumentTree::getDocId, folderId));

        //所有父级目录
        List<Long> collect1 = fatherIds.stream().map(DocBsDocumentTree::getFatherId).collect(Collectors.toList());
        List<DocBsDocument> folders = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>().in(DocBsDocument::getBusId, collect1));
        if (!CollectionUtils.isEmpty(folders)) {
            // 泛型为AuditBO的对象list
            folders.sort(Comparator.comparing(DocBsDocument::getFolderLevel));
            StringBuffer allFolder = new StringBuffer();
            folders.forEach(s -> {
                allFolder.append("/");
                allFolder.append(s.getDocName());
            });
            String s = allFolder.toString();
            String substring = s.substring(1, s.length());
            return substring;
        }
        return null;
    }

    /**
     * 通过文件后缀，判断文件属于那种类型，文档格式、图片格式、视频格式...
     */
    public void handleSuffixToDic(List<DocBsDocumentDTO> docBsDocuments) {
        List<DocBsDocumentDTO> collect = docBsDocuments.stream().filter(s -> s.getType().equals(DocConstants.DOCUMENT)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            return;
        }
        Result<Map<String, SysDictionaryDTO>> mapResult = sysDictionaryService.selectDictionByKeyMap(DocDictionaryKeyConstants.DOC_COMMON_SUFFIX, null);
        Map<String, SysDictionaryDTO> stringSysDictionaryMap = mapResult.getData();
        Set<String> strings1 = stringSysDictionaryMap.keySet();
        Map<String, SysDictionaryDTO> map = new HashMap<>(DocConstants.SIXTEEN);
        for (String s : strings1) {
            if (s != null && !DocConstants.DOC_COMMON_SUFFIX_OTHER.equals(s)) {
                SysDictionaryDTO sysDictionary = stringSysDictionaryMap.get(s);
                String[] split = sysDictionary.getDicExtra().split(",");
                for (String s1 : split) {
                    map.put(s1, sysDictionary);
                }
            }
        }
        for (DocBsDocumentDTO documentExtend : docBsDocuments) {
            if (documentExtend.getType().equals(DocConstants.DOCUMENT)) {
                SysDictionaryDTO sysDictionary = map.get(documentExtend.getDocSuffix());
                documentExtend.setDocSuffixType(sysDictionary);
            }
        }
    }

    /**
     * 根据配置，获取文档失效日期
     */
    public Date getRecycleDateByParam() {
        Result<SysParamDTO> sysParamDTOResult = paramApi.searchValueByKey(DocConstants.DOC_RECYCLE_DAY_KEY);
        SysParamDTO sysParam = sysParamDTOResult.getData();
        int day = 60;
        if (sysParam != null && !StringUtils.isEmpty(sysParam.getValue())) {
            try {
                day = Integer.parseInt(sysParam.getValue());
            } catch (Exception e) {

            }
        }
        Date date = DateUtils.addDays(new Date(), day);
        return date;
    }


    /**
     * 处理回收档案数据
     */
    public void handleRecycleDoc(List<Long> docs, Date recycleDate, AccountToken token) {
        //3、文档加入回收站
        docBsDocumentMapper.update(null, new LambdaUpdateWrapper<DocBsDocument>()
                .set(DocBsDocument::getRecycleStatus, DocConstants.RECYCLE_STATUS_RECOVERED)
                .set(DocBsDocument::getRecycleDate, recycleDate)
                .eq(DocBsDocument::getType, DocConstants.DOCUMENT)
                .in(DocBsDocument::getBusId, docs));

        //4、附件加入回收站
        docBsDocumentMapper.update(null, new LambdaUpdateWrapper<DocBsDocument>()
                .set(DocBsDocument::getRecycleStatus, DocConstants.RECYCLE_STATUS_RECOVERED)
                .set(DocBsDocument::getRecycleDate, recycleDate)
                .eq(DocBsDocument::getType, DocConstants.FILE)
                .in(DocBsDocument::getRelDoc, docs));

        //添加文档动态
        List<DocBsDocFlow> docBsDocFlows = new ArrayList<>();
        for (Long id : docs) {
            DocBsDocFlow docBsDocFlow = new DocBsDocFlow();
            docBsDocFlow.setDocId(id);
            docBsDocFlow.setUserId(token.getId());
            docBsDocFlow.setFlowDate(new Date());
            docBsDocFlow.setFlowDescribe("删除");
            docBsDocFlow.setFlowType(DocConstants.FLOW_TYPE_DEL);
            docBsDocFlows.add(docBsDocFlow);
        }
        MybatisBatch<DocBsDocFlow> docBsShapeMybatisBatch = new MybatisBatch<>(sqlSessionFactory, docBsDocFlows);
        MybatisBatch.Method<DocBsDocFlow> docBsShapeMethod = new MybatisBatch.Method<>(DocBsDocFlowMapper.class);
        docBsShapeMybatisBatch.execute(docBsShapeMethod.insert());
    }

    /**
     * 获取当前用户可查看的文档库列表
     */
    public List<DocSysHouseDTO> queryHouseList(AccountToken token) {
        List<Long> teams = getTeamListByUser(token);
        List<DocSysHouseDTO> list = docSysHouseMapper.selectListExtend(token.getInstId(),DocConstants.INST,
                token.getDeptId(),DocConstants.DEPT,token.getId(),DocConstants.USER,teams,DocConstants.TEAM);
        List<DocSysHouseDTO> re = new ArrayList<>();
        if (!CollectionUtils.isEmpty(list)) {
            //根据权限级别倒叙，并且根据busid分组，获取分组后的第一条数据，即，权限最大的数据
            Map<Long, List<DocSysHouseDTO>> collect = list.stream()
                    .sorted(Comparator.comparing(DocSysHouseDTO::getPermissType, Comparator.nullsLast(Integer::compareTo)).reversed())
                    .collect(Collectors.groupingBy(DocSysHouse::getHouseId));
            collect.keySet().forEach(s -> re.add(collect.get(s).get(0)));
            //查询按顺序号排序
            list = re.stream()
                    .sorted(Comparator.comparing(DocSysHouseDTO::getHouseSeq, Comparator.nullsLast(Long::compareTo))
                            .thenComparing(DocSysHouseDTO::getCreateTime)).collect(Collectors.toList());
        }
        return list;
    }

    /**
     * 多权限数据过滤
     */
    public List<DocBsDocumentDTO> getPermissMax(List<DocBsDocumentDTO> ret) {
        //过滤
        List<DocBsDocumentDTO> re = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ret)) {
            //根据权限级别倒叙，并且根据busid分组，获取分组后的第一条数据，即，权限最大的数据
            Map<Long, List<DocBsDocumentDTO>> collect = ret.stream()
                    .sorted(Comparator.comparing(DocBsDocumentDTO::getPermissType, Comparator.nullsLast(Integer::compareTo)).reversed())
                    .collect(Collectors.groupingBy(DocBsDocument::getBusId));
            collect.keySet().forEach(s -> re.add(collect.get(s).get(0)));
        }
        return re;
    }

    /**
     * 查询文件夹列表
     */
    public <T> void queryFolderList(AccountToken token, ExtendPageDTO extendPageDTO, Boolean showFlag, String tableName) {
        List<Long> teams = getTeamListByUser(token);
        if (showFlag != null && showFlag) {
            ArrayList<Integer> permissType = new ArrayList<>();
            permissType.add(DocConstants.DOC_COMMON_PERMISSION_TYPE_EDIT);
            permissType.add(DocConstants.DOC_COMMON_PERMISSION_TYPE_MANAGE);
            extendPageDTO.setPermissType(permissType);
            //仅展示有管理权限和编辑权限的文档
           /* queryWrapper.and(m ->
                    m.or(s -> s.eq("u.rel_id", token.getInstId()).eq("u.type", DocConstants.INST).in("u.permiss_type", permissType))
                            .or(s -> s.eq("u.rel_id", token.getDeptId()).eq("u.type", DocConstants.DEPT).in("u.permiss_type", permissType))
                            .or(s -> s.eq("u.rel_id", token.getId()).eq("u.type", DocConstants.USER).in("u.permiss_type", permissType))
                            .or(!CollectionUtils.isEmpty(teams), s -> s.in("u.rel_id", teams).eq("u.type", DocConstants.TEAM).in("u.permiss_type", permissType)));
*/

        }/* else {
            //获取当前登陆用户所在的机构、部门、团队
            queryWrapper.and(m -> m.or(s -> s.eq("u.rel_id", token.getInstId())
                            .eq("u.type", DocConstants.INST))
                    .or(s -> s.eq("u.rel_id", token.getDeptId()).eq("u.type", DocConstants.DEPT))
                    .or(s -> s.eq("u.rel_id", token.getId()).eq("u.type", DocConstants.USER))
                    .or(!CollectionUtils.isEmpty(teams), s -> s.in("u.rel_id", teams).eq("u.type", DocConstants.TEAM)));
        }*/
        extendPageDTO.setInstId(token.getInstId());
        extendPageDTO.setInst(DocConstants.INST);
        extendPageDTO.setDeptId(token.getDeptId());
        extendPageDTO.setDept(DocConstants.DEPT);
        extendPageDTO.setTokenId(token.getId());
        extendPageDTO.setUser(DocConstants.USER);
        extendPageDTO.setRelIds(teams);
        extendPageDTO.setTeam(DocConstants.TEAM);
    }


    /**
     * 获取指定文档库是否有权限，有什么权限
     */
    private Integer getPermissByTokenHouse(AccountToken token, Long houseId) {
        List<Long> teams = getTeamListByUser(token);
        LambdaQueryWrapper<DocSysHouseUser> queryWrapper = new LambdaQueryWrapper<>();
        //获取当前登陆用户所在的机构、部门、团队
        queryWrapper.and(m -> m.or(s -> s.eq(DocSysHouseUser::getRelId, token.getInstId())
                        .eq(DocSysHouseUser::getType, DocConstants.INST))
                .or(s -> s.eq(DocSysHouseUser::getRelId, token.getDeptId()).eq(DocSysHouseUser::getType, DocConstants.DEPT))
                .or(s -> s.eq(DocSysHouseUser::getRelId, token.getId()).eq(DocSysHouseUser::getType, DocConstants.USER))
                .or(!CollectionUtils.isEmpty(teams), s -> s.in(DocSysHouseUser::getRelId, teams).eq(DocSysHouseUser::getType, DocConstants.TEAM)));
        queryWrapper.eq(DocSysHouseUser::getHouseId, houseId);
        List<DocSysHouseUser> docSysHouseUsers = docSysHouseUserMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(docSysHouseUsers)) {
            Map<Long, List<DocSysHouseUser>> collect1 = docSysHouseUsers.stream().collect(Collectors.groupingBy(DocSysHouseUser::getHouseId));
            List<Integer> list = new ArrayList<>();
            for (Long id : collect1.keySet()) {
                List<Integer> collect = collect1.get(id).stream()
                        .sorted(Comparator.comparing(DocSysHouseUser::getPermissType).reversed())
                        .map(DocSysHouseUser::getPermissType)
                        .collect(Collectors.toList());
                list.add(collect.get(0));
            }
            Collections.sort(list);
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * 文档库是否拥有编辑以上的权限
     */
    public void isEditPermissHouse(AccountToken token, Long houseId) {
        Integer house = getPermissByTokenHouse(token, houseId);
        AssertUtils.isNull(house, "暂无权限");
        AssertUtils.isTrue(house < DocConstants.DOC_COMMON_PERMISSION_TYPE_EDIT, "暂无权限");
    }



    /**
     * 检验是否有管理权限，如果只有查看权限、编辑权限则直接报暂无权限错误，返回。
     */
    public void isMangePermiss(AccountToken token, Long busId) {
        List<Long> longs = new ArrayList<>();
        longs.add(busId);
        Integer commonPermissDocOrFolder = getCommonPermissDocOrFolder(token, longs);
        AssertUtils.isNull(commonPermissDocOrFolder, "暂无权限");
        AssertUtils.isTrue(!commonPermissDocOrFolder.equals(DocConstants.DOC_COMMON_PERMISSION_TYPE_MANAGE), "暂无权限");
    }

    /**
     * 检验是否有管理权限，如果只有查看权限、编辑权限则直接报暂无权限错误，返回。
     */
    public void isMangePermiss(AccountToken token, Long[] busId) {
        Integer commonPermissDocOrFolder = getCommonPermissDocOrFolder(token, Arrays.asList(busId));
        AssertUtils.isNull(commonPermissDocOrFolder, "暂无权限");
        AssertUtils.isTrue(!commonPermissDocOrFolder.equals(DocConstants.DOC_COMMON_PERMISSION_TYPE_MANAGE), "暂无权限");
    }

    /**
     * 是否有编辑以上的权限
     */
    public void isEditPermiss(AccountToken token, List<Long> list) {
        Integer permissByTokenDoc = getCommonPermissDocOrFolder(token, list);
        AssertUtils.isNull(permissByTokenDoc, "暂无权限");
        AssertUtils.isTrue(permissByTokenDoc.equals(DocConstants.DOC_COMMON_PERMISSION_TYPE_LOOK), "暂无权限");
    }

    /**
     * 计算文件大小
     */
    public void handleFileSize(DocBsDocument document) {
        //附件大小
        List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>().eq(DocBsDocument::getRelDoc, document.getBusId()));
        Long fileSize = document.getDocSize();
        for (DocBsDocument document1 : docBsDocuments) {
            fileSize += document1.getDocSize();
        }
        //需要将容量从个人的地方移除
        handleFolderSize(0 - fileSize, document.getFolderId());
    }

    /**
     * 计算文件大小
     */
    public void handleFolderSize(Long folderSize, Long folderId) {
        if (folderId == null) {
            return;
        }
        List<DocBsDocumentTree> docId = docBsDocumentTreeMapper.selectList(new LambdaQueryWrapper<DocBsDocumentTree>()
                .eq(DocBsDocumentTree::getDocId, folderId));
        if (!CollectionUtils.isEmpty(docId)) {
            List<Long> collect1 = docId.stream().map(DocBsDocumentTree::getFatherId).collect(Collectors.toList());
            List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectBatchIds(collect1);

            for (DocBsDocument docBsDocument : docBsDocuments) {
                long l = docBsDocument.getDocSize() == null ? 0 + folderSize : docBsDocument.getDocSize() + folderSize;
                if (l < 0) {
                    l = 0;
                }
                docBsDocumentMapper.update(null, new LambdaUpdateWrapper<DocBsDocument>()
                        //文件夹原本的大小加上需要新增的大小
                        .set(DocBsDocument::getDocSize, l)
                        .eq(DocBsDocument::getBusId, docBsDocument.getBusId()));
            }
        }
    }

    /**
     * 校验容量是否达上限
     */
    public void checkCapacity(AccountToken token) {
        //个人有容量的限制
        Result<SysParamDTO> sysParamDTOResult = paramApi.searchValueByKey(DocConstants.DOC_MAXIMUM_SIZE);
        SysParamDTO sysParam = sysParamDTOResult.getData();
        String value = "100";
        if (sysParam != null) {
            value = sysParam.getValue();
        }
        //总量
        long l1 = Long.parseLong(value) * DocConstants.FILESIZE * DocConstants.FILESIZE * DocConstants.FILESIZE;

        List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                .eq(DocBsDocument::getDocType, DocConstants.PERSON)
                .eq(DocBsDocument::getType, DocConstants.FOLDER)
                .isNull(DocBsDocument::getFolderId)
                .eq(DocBsDocument::getDocOwner, token.getId())
        );
        //所有的容量-回收站的容量，就是正常使用的容量
        Long aLong1 = handleCapacity(docBsDocuments);
        AssertUtils.isTrue(aLong1 >= l1, "容量达超上限。");
    }

    /**
     * 计算文件大小
     */
    public Long handleCapacity(List<DocBsDocument> docBsDocuments) {
        if (CollectionUtils.isEmpty(docBsDocuments)) {
            return 0L;
        }
        Long used = 0L;
        for (DocBsDocument document : docBsDocuments) {
            used += document.getDocSize() == null ? 0 : document.getDocSize();
        }

        return used;
    }

    /**
     * 检验是否有编辑权限，如果只有查看权限则直接报暂无权限错误，返回。
     */
    public void isEditPermiss(AccountToken token, Long busId) {
        List<Long> longs = new ArrayList<>();
        longs.add(busId);
        Integer permissByTokenDoc = getCommonPermissDocOrFolder(token, longs);
        AssertUtils.isNull(permissByTokenDoc, "暂无权限");
        AssertUtils.isTrue(permissByTokenDoc.equals(DocConstants.DOC_COMMON_PERMISSION_TYPE_LOOK), "暂无权限");
    }

    /**
     * 获取指定文件夹或者文档是否有权限，有什么权限
     */
    public Integer getCommonPermissDocOrFolder(AccountToken token, List<Long> docId) {
        //多选或者单选的数据修改或者删除鉴权所用
        List<Long> teams = getTeamListByUser(token);
        LambdaQueryWrapper<DocBsDocumentUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //获取当前登陆用户所在的机构、部门、团队
        lambdaQueryWrapper.and(m ->
                m.or(s -> s.eq(DocBsDocumentUser::getRelId, token.getInstId()).eq(DocBsDocumentUser::getType, DocConstants.INST))
                        .or(s -> s.eq(DocBsDocumentUser::getRelId, token.getDeptId()).eq(DocBsDocumentUser::getType, DocConstants.DEPT))
                        .or(s -> s.eq(DocBsDocumentUser::getRelId, token.getId()).eq(DocBsDocumentUser::getType, DocConstants.USER))
                        .or(!CollectionUtils.isEmpty(teams), s -> s.in(DocBsDocumentUser::getRelId, teams).eq(DocBsDocumentUser::getType, DocConstants.TEAM)));
        lambdaQueryWrapper.in(DocBsDocumentUser::getDocId, docId);
        List<DocBsDocumentUser> docBsDocumentUsers = docBsDocumentUserMapper.selectList(lambdaQueryWrapper);
        if (!CollectionUtils.isEmpty(docBsDocumentUsers)) {
            Map<Long, List<DocBsDocumentUser>> collect1 = docBsDocumentUsers.stream().collect(Collectors.groupingBy(DocBsDocumentUser::getDocId));
            List<Integer> list = new ArrayList<>();
            for (Long id : collect1.keySet()) {
                List<Integer> collect = collect1.get(id).stream()
                        .sorted(Comparator.comparing(DocBsDocumentUser::getPermissType, Comparator.nullsLast(Integer::compareTo)).reversed())
                        .map(DocBsDocumentUser::getPermissType)
                        .collect(Collectors.toList());
                list.add(collect.get(0));
            }
            Collections.sort(list);
            return list.get(0);
        } else {
            return null;
        }
    }

    private List<Long> getTeamListByUser(AccountToken token) {
        List<DocSysTeamUser> users = docSysTeamUserMapper.selectList(new LambdaQueryWrapper<DocSysTeamUser>().eq(DocSysTeamUser::getUserId, token.getId()));
        return users.stream().map(DocSysTeamUser::getTeamId).collect(Collectors.toList());
    }


    /**
     * 文件夹获取顺序号
     */
    public Integer getFolderSeq(Long parentId, Long houseId, AccountToken token, Integer code) {
        LambdaQueryWrapper<DocBsDocument> lambdaQueryWrapper = new LambdaQueryWrapper<DocBsDocument>()
                .eq(ObjectUtils.isEmpty(parentId), DocBsDocument::getFolderLevel, DocConstants.ZERO)
                .eq(!ObjectUtils.isEmpty(parentId), DocBsDocument::getParentId, parentId)
                .eq(DocBsDocument::getRecycleStatus, DocConstants.RECYCLE_STATUS_NORMAL)
                .eq(DocBsDocument::getType, DocConstants.FOLDER)
                .eq(!ObjectUtils.isEmpty(houseId), DocBsDocument::getHouseId, houseId)
                .orderByDesc(DocBsDocument::getDocSeq);
        lambdaQueryWrapper.eq(DocBsDocument::getDocType, code);
        if (DocConstants.PERSON.equals(code)) {
            lambdaQueryWrapper.eq(DocBsDocument::getDocOwner, token.getId());
        }
        List<DocBsDocument> docBsDocumentList = docBsDocumentMapper.selectList(lambdaQueryWrapper);

        //如果为空 返回1 不为空返回最大顺序号 个数加1
        if (CollectionUtils.isEmpty(docBsDocumentList)) {
            return DocConstants.ONE.intValue();
        } else {
            return docBsDocumentList.size() + 1;
        }
    }

    /**
     * 文件夹加入回收站
     *
     */
    public void handleRecycleFolder(Long folderId, Date date, AccountToken token) {
        //1、获取所有子文件夹
        List<DocBsDocumentTree> parentId = docBsDocumentTreeMapper.selectList(new LambdaQueryWrapper<DocBsDocumentTree>()
                .eq(DocBsDocumentTree::getFatherId, folderId));
        List<Long> folderIds = parentId.stream().map(DocBsDocumentTree::getDocId).collect(Collectors.toList());

        //2、获取所有子文件夹和当前文件夹中的文档。
        List<DocBsDocument> docIdList = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                .eq(DocBsDocument::getType, DocConstants.DOCUMENT)
                .in(DocBsDocument::getFolderId, folderIds));

        if (!CollectionUtils.isEmpty(docIdList)) {
            List<Long> docs = docIdList.stream().map(DocBsDocument::getBusId).collect(Collectors.toList());
            handleRecycleDoc(docs, date, token);
        }
        //5、文件夹加入回收站
        docBsDocumentMapper.update(null, new LambdaUpdateWrapper<DocBsDocument>()
                .set(DocBsDocument::getRecycleDate, date)
                .set(DocBsDocument::getRecycleStatus, DocConstants.RECYCLE_STATUS_RECOVERED)
                .in(DocBsDocument::getBusId, folderIds));
    }


    /**
     * 复用上级权限
     */
    public UserTeamDeptInitDTO reuseAuth(Long parentId, Integer type) {
        AssertUtils.isNull(type, "参数错误！");
        List<DocBsDocumentUserDTO> docBsDocumentUserDTOList = null;
        if (DocConstants.FOLDER.equals(type)) {
            //文件夹
            AssertUtils.isNull(parentId, "参数错误！");
            docBsDocumentUserDTOList = docBsDocumentUserMapper.selectListExtend(parentId,DocConstants.ZERO);
            for (DocBsDocumentUserDTO s : docBsDocumentUserDTOList) {
                SysUserDTO data = userApi.getUserByUserId(s.getRelId()).getData();
                SysInstDTO data1 = instApi.getInstByInstId(s.getRelId()).getData();
                SysDeptDTO data2 = deptApi.selectById(s.getRelId()).getData();
                if (null != data) {
                    s.setUserIdStr(data.getName());
                }
                if (null != data1) {
                    s.setInstIdStr(data1.getName());
                }
                if (null != data2) {
                    s.setDeptIdStr(data2.getName());
                }
            }
            if (CollectionUtils.isEmpty(docBsDocumentUserDTOList)) {
                //文件夹权限表为空  表示是文档库id
                docBsDocumentUserDTOList = docSysHouseUserMapper.selectInfo(parentId);
                for (DocBsDocumentUserDTO s : docBsDocumentUserDTOList) {
                    SysUserDTO data = userApi.getUserByUserId(s.getRelId()).getData();
                    SysInstDTO data1 = instApi.getInstByInstId(s.getRelId()).getData();
                    SysDeptDTO data2 = deptApi.selectById(s.getRelId()).getData();
                    if (null != data) {
                        s.setUserIdStr(data.getName());
                    }
                    if (null != data1) {
                        s.setInstIdStr(data1.getName());
                    }
                    if (null != data2) {
                        s.setDeptIdStr(data2.getName());
                    }
                }
            }

        } else if (DocConstants.DOCUMENT.equals(type)) {
            //文档
            docBsDocumentUserDTOList = docBsDocumentUserMapper.selectListExtend( parentId, DocConstants.ZERO);
            for (DocBsDocumentUserDTO s : docBsDocumentUserDTOList) {
                SysUserDTO data = userApi.getUserByUserId(s.getRelId()).getData();
                SysInstDTO data1 = instApi.getInstByInstId(s.getRelId()).getData();
                SysDeptDTO data2 = deptApi.selectById(s.getRelId()).getData();
                if (null != data) {
                    s.setUserIdStr(data.getName());
                }
                if (null != data1) {
                    s.setInstIdStr(data1.getName());
                }
                if (null != data2) {
                    s.setDeptIdStr(data2.getName());
                }
            }
        }
        UserTeamDeptInitDTO ret = new UserTeamDeptInitDTO();

        if (!CollectionUtils.isEmpty(docBsDocumentUserDTOList)) {
            //三个列表的处理
            handleInit(docBsDocumentUserDTOList, ret);

            handleRel(docBsDocumentUserDTOList);
            ret.setDocBsDocumentUserDTOList(docBsDocumentUserDTOList);
        }

        return ret;
    }

    /**
     * 返回值处理
     *
     */
    public List<DocBsDocumentDTO> getChildren(List<DocBsDocumentDTO> ret) {
        if (CollectionUtils.isEmpty(ret)) {
            return null;
        }
        ret.sort(Comparator.comparing(DocBsDocument::getFolderLevel));
        Map<Long, List<DocBsDocumentDTO>> collect = ret.stream().filter(s -> s.getParentId() != null).collect(Collectors.groupingBy(DocBsDocumentDTO::getParentId));
        Map<Integer, List<DocBsDocumentDTO>> collect2 = ret.stream().collect(Collectors.groupingBy(DocBsDocumentDTO::getFolderLevel));
        List<DocBsDocumentDTO> list2 = collect2.get(ret.get(0).getFolderLevel());
        for (DocBsDocumentDTO documentExtend : list2) {
            //第一层
            handleChildren(collect, documentExtend);
        }
        return list2;

    }

    private void handleChildren(Map<Long, List<DocBsDocumentDTO>> collect1, DocBsDocumentDTO documentExtend) {
        List<DocBsDocumentDTO> list = collect1.get(documentExtend.getBusId());
        if (CollectionUtils.isEmpty(list)) {
            return;
        } else {
            documentExtend.setChildren(list);
            for (DocBsDocumentDTO d : list) {
                handleChildren(collect1, d);
            }
        }
    }


    /**
     * 给relId转中文
     */
    public void handleRel(List<DocBsDocumentUserDTO> docBsDocumentUserDTOList) {
        for (DocBsDocumentUserDTO docBsDocumentUserDTO : docBsDocumentUserDTOList) {
            docBsDocumentUserDTO.setRelType(docBsDocumentUserDTO.getType());
            if (DocConstants.USER.equals(docBsDocumentUserDTO.getType())) {
                //用户
                docBsDocumentUserDTO.setRelIdStr(docBsDocumentUserDTO.getUserIdStr());
            } else if (DocConstants.DEPT.equals(docBsDocumentUserDTO.getType())) {
                //部门
                docBsDocumentUserDTO.setRelIdStr(docBsDocumentUserDTO.getDeptIdStr());
            } else if (DocConstants.INST.equals(docBsDocumentUserDTO.getType())) {
                //机构
                docBsDocumentUserDTO.setRelIdStr(docBsDocumentUserDTO.getInstIdStr());
            } else if (DocConstants.TEAM.equals(docBsDocumentUserDTO.getType())) {
                //团队
                docBsDocumentUserDTO.setRelIdStr(docBsDocumentUserDTO.getTeamIdStr());
            }
        }
    }

    /**
     * 分页处理权限
     */
    public void getPermissMaxPage(AccountToken token, List<DocBsDocumentDTO> docBsDocuments) {
        if (CollectionUtils.isEmpty(docBsDocuments)) {
            return;
        }
        List<Long> collect = docBsDocuments.stream().map(DocBsDocumentDTO::getBusId).collect(Collectors.toList());
        LambdaQueryWrapper<DocBsDocumentUser> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(DocBsDocumentUser::getDocId, collect);
        List<Long> teams = getTeamListByUser(token);
        //获取当前登陆用户所在的机构、部门、团队
        queryWrapper1.and(m -> m.or(s -> s.eq(  DocBsDocumentUser::getRelId, token.getInstId())
                .eq(DocBsDocumentUser::getType, DocConstants.INST))
                .or(s -> s.eq(DocBsDocumentUser::getRelId, token.getDeptId()).eq(DocBsDocumentUser::getType, DocConstants.DEPT))
                .or(s -> s.eq(DocBsDocumentUser::getRelId, token.getId()).eq(DocBsDocumentUser::getType, DocConstants.USER))
                .or(!CollectionUtils.isEmpty(teams), s -> s.in(DocBsDocumentUser::getRelId, teams).eq( DocBsDocumentUser::getType, DocConstants.TEAM)));

        List<DocBsDocumentUser> docBsDocumentUsers = docBsDocumentUserMapper.selectList(queryWrapper1);
        Map<Long, List<DocBsDocumentUser>> collect1 = docBsDocumentUsers.stream().collect(Collectors.groupingBy(DocBsDocumentUser::getDocId));
        for (DocBsDocumentDTO documentExtend : docBsDocuments) {
            if (!CollectionUtils.isEmpty(collect1.get(documentExtend.getBusId()))) {
                List<Integer> integerList = collect1.get(documentExtend.getBusId()).stream()
                        .sorted(Comparator.comparing(DocBsDocumentUser::getPermissType).reversed())
                        .map(DocBsDocumentUser::getPermissType)
                        .collect(Collectors.toList());
                documentExtend.setPermissType(integerList.get(0));
            }
        }
    }

    /**
     * 计算文件大小
     */
    public void handleDocSize(List<DocBsDocumentDTO> docBsDocuments) {
        for (DocBsDocumentDTO documentExtend : docBsDocuments) {
            Long docSize = documentExtend.getDocSize();
            documentExtend.setDocSizeStr(DocUtils.getFilseSize(docSize));
        }
    }


    /**
     * 文件收藏
     */
    public void handleCollection(List<DocBsDocumentDTO> docBsDocuments, Long userId) {
        if (CollectionUtils.isEmpty(docBsDocuments)) {
            return;
        }
        List<Long> collect = docBsDocuments.stream().map(DocBsDocumentDTO::getBusId).collect(Collectors.toList());
        List<DocBsCollection> docId = docBsCollectionMapper.selectList(new LambdaQueryWrapper<DocBsCollection>().in(DocBsCollection::getDocId, collect));
        if (CollectionUtils.isEmpty(docId)) {
            return;
        }
        //拿到当前用户的收藏集合
        Map<Long, List<DocBsCollection>> userMap = docId.stream().collect(Collectors.groupingBy(DocBsCollection::getUserId));
        List<DocBsCollection> docBsCollectionList = userMap.get(userId);
        if (!CollectionUtils.isEmpty(docBsCollectionList)) {
            Map<Long, List<DocBsCollection>> collect1 = docBsCollectionList.stream().collect(Collectors.groupingBy(DocBsCollection::getDocId));
            for (DocBsDocumentDTO documentExtend : docBsDocuments) {
                List<DocBsCollection> docBsCollections = collect1.get(documentExtend.getBusId());
                //判断当前用户是否进行了收藏
                if (!CollectionUtils.isEmpty(docBsCollections)) {
                    documentExtend.setCollectioned(DocConstants.ONE);
                    documentExtend.setCollectionId(docBsCollections.get(0).getCollectionId());
                }
            }
        }
    }

    /**
     * 文件夹收藏
     */
    public void handleCollectionFolder(List<DocBsDocumentDTO> docBsDocuments, Long userId) {
        if (CollectionUtils.isEmpty(docBsDocuments)) {
            return;
        }
        //查询出当前用户收藏集合
        List<DocBsCollection> collectionList = docBsCollectionMapper.selectList(new LambdaQueryWrapper<DocBsCollection>().eq(DocBsCollection::getUserId, userId));
        if (CollectionUtils.isEmpty(collectionList)) {
            return;
        }
        //拿到收藏的doc集合，用于判断是否进行了收场
        List<Long> docIdList = collectionList.stream().map(DocBsCollection::getDocId).collect(Collectors.toList());
        //拿到docId：收藏对象map集合，用于获取collectionId
        Map<Long, List<DocBsCollection>> docIdMap = collectionList.stream().collect(Collectors.groupingBy(DocBsCollection::getDocId));
        setCollection(docBsDocuments, docIdList, docIdMap);
    }

    public void setCollection(List<DocBsDocumentDTO> docBsDocuments, List<Long> docIdList, Map<Long, List<DocBsCollection>> docIdMap) {
        for (DocBsDocumentDTO documentExtend : docBsDocuments) {
            if (!CollectionUtils.isEmpty(documentExtend.getChildren())) {
                setCollection(documentExtend.getChildren(), docIdList, docIdMap);
            }
            //判断当前用户是否进行了收藏
            if (docIdList.contains(documentExtend.getBusId())) {
                documentExtend.setCollectioned(DocConstants.ONE);
                documentExtend.setCollectionId(docIdMap.get(documentExtend.getBusId()).get(0).getCollectionId());
            }

        }
    }

    private void handleInit(List<DocBsDocumentUserDTO> docBsDocumentUserDTOList, UserTeamDeptInitDTO ret) {
        //三个列表的处理
        Map<Integer, List<DocBsDocumentUserDTO>> collect = docBsDocumentUserDTOList.stream().collect(Collectors.groupingBy(DocBsDocumentUserDTO::getType));
        //机构
        List<DocBsDocumentUserDTO> instList = collect.get(DocConstants.INST) == null ? new ArrayList<>() : collect.get(DocConstants.INST);
        List<DocBsDocumentUserDTO> deptList = collect.get(DocConstants.DEPT) == null ? new ArrayList<>() : collect.get(DocConstants.DEPT);
        List<DocBsDocumentUserDTO> userList = collect.get(DocConstants.USER) == null ? new ArrayList<>() : collect.get(DocConstants.USER);
        List<DocBsDocumentUserDTO> teamList = collect.get(DocConstants.TEAM) == null ? new ArrayList<>() : collect.get(DocConstants.TEAM);
        instList.addAll(deptList);
        if (!CollectionUtils.isEmpty(instList)) {
            List<Long> collect1 = instList.stream().map(DocBsDocumentUserDTO::getRelId).collect(Collectors.toList());
            ret.setInstDeptList(collect1);
        }

        if (!CollectionUtils.isEmpty(userList)) {
            List<Long> collect1 = userList.stream().map(DocBsDocumentUserDTO::getRelId).collect(Collectors.toList());
            ret.setUserList(collect1);
        }

        if (!CollectionUtils.isEmpty(teamList)) {
            List<Long> collect1 = teamList.stream().map(DocBsDocumentUserDTO::getRelId).collect(Collectors.toList());
            ret.setTeamList(collect1);
        }
    }
}
