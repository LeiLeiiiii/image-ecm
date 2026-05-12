package com.sunyard.ecm.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sunyard.ecm.constant.DocRightConstants;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.RoleConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmDocDefDTO;
import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmDocrightDefDTO;
import com.sunyard.ecm.mapper.EcmAppDefRelMapper;
import com.sunyard.ecm.mapper.EcmAppDocRelMapper;
import com.sunyard.ecm.mapper.EcmAppDocrightMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmDocDefRelMapper;
import com.sunyard.ecm.mapper.EcmDocDefRelVerMapper;
import com.sunyard.ecm.mapper.EcmDocrightDefMapper;
import com.sunyard.ecm.po.EcmAppDefRel;
import com.sunyard.ecm.po.EcmAppDocRel;
import com.sunyard.ecm.po.EcmAppDocright;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmDocDefRel;
import com.sunyard.ecm.po.EcmDocDefRelVer;
import com.sunyard.ecm.po.EcmDocrightDef;
import com.sunyard.ecm.util.RightCheckers;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import static com.sunyard.ecm.util.RightCheckers.RIGHT_CHECKER_MAP;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.mapper.EcmAppDefMapper;

/**
 * @author： zyl
 * @create： 2023/4/13 16:25
 * @desc: 静态树权限实现类
 */
@Slf4j
@Service
public class StaticTreePermissService {
    @Resource
    private EcmAppDefRelMapper ecmAppDefRelMapper;
    @Resource
    private EcmAppDocrightMapper ecmAppDocrightMapper;
    @Resource
    private EcmDocrightDefMapper ecmDocrightDefMapper;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmDocDefRelVerMapper ecmDocDefRelVerMapper;
    @Resource
    private EcmDocDefRelMapper ecmDocDefRelMapper;
    @Resource
    private EcmAppDocRelMapper ecmAppDocRelMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;

    /**
     * 根据角色获取doc权限（静态树）
     */
    public List<EcmDocrightDefDTO> roleDimLogic(String appCode, Integer rightVer, AccountTokenExtendDTO tokenExtend) {
        List<EcmDocrightDefDTO> docRightList = new ArrayList<>();
        //成功，获取角色id列表
        List<Long> roleIds = getRoleByToken(tokenExtend);
        if (CollectionUtils.isEmpty(roleIds)) {
            //没有关联角色，返回无权限列表
            docRightList = getEcmDocrightDefExtendsByRole(appCode, rightVer, null, tokenExtend.getUsername(), null, DocRightConstants.ROLE_DIM);
            return docRightList;
        }
        List<EcmDocrightDefDTO> allDocRightList;
        LambdaQueryWrapper<EcmAppDocright> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmAppDocright::getAppCode, appCode);
        // 改为当业务对应版本 之前是最新版本
        wrapper.eq(EcmAppDocright::getRightVer, rightVer);
        EcmAppDocright ecmAppDocright = ecmAppDocrightMapper.selectOne(wrapper);
        AssertUtils.isNull(ecmAppDocright, "该业务未设置当前权限版本");

        ArrayList<String> objects = new ArrayList<>();
        roleIds.forEach(s -> {
            objects.add(s + "");
        });
        //带上关联资料版本
        allDocRightList = getEcmDocrightDefExtendsByRole(appCode, rightVer, objects, tokenExtend.getUsername(), null, DocRightConstants.ROLE_DIM);
        return allDocRightList;
    }

    /**
     * 根据token获取角色id
     */
    public List<Long> getRoleByToken(AccountTokenExtendDTO tokenExtend) {
        if (CollectionUtils.isEmpty(tokenExtend.getRoleIdList())) {
            SysUserDTO dto = new SysUserDTO();
            dto.setLoginName(tokenExtend.getUsername());
            dto.setRoleCodeList(tokenExtend.getRoleCodeList());
            dto.setInstNo(tokenExtend.getOrgCode());
            Result<SysUserDTO> sysUserDTOResult;
            if(tokenExtend.isOut()) {
                 sysUserDTOResult = userApi.checkUserSpecial(dto);
            }else{
                sysUserDTOResult = userApi.checkUser(dto);
            }
            if (sysUserDTOResult.isSucc() && sysUserDTOResult.getData() != null) {
                return sysUserDTOResult.getData().getRoleIdList();
            }
        } else {
            return tokenExtend.getRoleIdList();
        }
        AssertUtils.isNull(tokenExtend.getRoleIdList(), "角色配置有误");
        return null;
//        if(!CollectionUtils.isEmpty(tokenExtend.getRoleIdList())){
//            return tokenExtend.getRoleIdList();
//        }
//        List<Long> roleIds = null;
//        List<Long> resultRoleIds = new ArrayList<>();
//        if (tokenExtend.isOut()) {
//            if(tokenExtend.getRoleIdList()==null){
//                SysRoleDTO sysRoleDTO = new SysRoleDTO();
//                sysRoleDTO.setRoleCode(tokenExtend.getRoleCode());
////                sysRoleDTO.setSystemCode(StateConstants.COMMON_ONE.toString());
//                sysRoleDTO.setPageSize(0);
//                Result result = roleApi.getRoleByRoleCode(tokenExtend.getRoleCode(),3);
//                if (result.isSucc()) {
//                    //成功，获取角色id列表
////                    PageInfo pageInfo = JSONObject.parseObject(JSONObject.toJSONString(result.getData()), PageInfo.class);
//                    SysRoleDTO dto = JSONObject.parseObject(JSONObject.toJSONString(result.getData()), SysRoleDTO.class);
////                    roleIds = list.stream().map(SysRoleDTO::getRoleId).collect(Collectors.toList());
//                    resultRoleIds.add(dto.getRoleId());
//                    roleIds = resultRoleIds;
//                }
//
//            }else{
//                resultRoleIds.add(tokenExtend.getRoleId());
//                roleIds = resultRoleIds;
//            }
//        } else {
//            //查找用户关联的角色id
//            Result<List<Long>> result = userApi.getRoleListByUsername(tokenExtend.getUsername());
//            if (result.isSucc()) {
//                roleIds = result.getData();
//            }
//        }
//        return roleIds;
    }

    /**
     * 根据角色获取doc权限（静态树）
     */
    public List<EcmDocrightDefDTO> roleDimLogic2(String appCode, Integer rightVer, AccountTokenExtendDTO tokenExtend) {
        List<EcmDocrightDefDTO> docRightList = new ArrayList<>();
        //成功，获取角色id列表
        List<Long> roleIds = null;

        if (tokenExtend.isOut()) {
            roleIds= tokenExtend.getRoleIdList();
        } else {
            //查找用户关联的角色id
            Result<List<Long>> result = userApi.getRoleListByUsername(tokenExtend.getUsername());
            if (!result.isSucc()) {
                //失败， 获取用户关联角色列表
                return docRightList;
            }
            roleIds = result.getData();
        }
        if (CollectionUtils.isEmpty(roleIds)) {
            //没有关联角色，返回无权限列表
            docRightList = getEcmDocrightDefExtendsByRole1(appCode, rightVer, null, tokenExtend.getUsername(), null);
            return docRightList;
        }
        List<EcmDocrightDefDTO> allDocRightList;
        ArrayList<String> objects = new ArrayList<>();
        roleIds.forEach(s -> {
            objects.add(s + "");
        });
        allDocRightList = getEcmDocrightDefExtendsByRole1(appCode, rightVer, objects, tokenExtend.getUsername(), null);
        return allDocRightList;
    }

    /**
     * 获取权限
     */
    public List<EcmDocrightDefDTO> getEcmDocrightDefExtendsByRole(String appCode, Integer rightVer, List<String> roleId, String username, Integer operateFlag, Integer lotDim) {
        List<String> docCodes;
        List<EcmDocDef> ecmDocDefs;
        List<EcmAppDocRel> appDocRels = ecmAppDocRelMapper.selectList(new QueryWrapper<EcmAppDocRel>()
                .eq("app_code", appCode)
                .eq("type", IcmsConstants.ONE)
                .orderByAsc("doc_sort"));
        //无关联资料类型直接返回
        if (org.springframework.util.CollectionUtils.isEmpty(appDocRels)) {
            return Collections.emptyList();
        }
        docCodes = appDocRels.stream().map(EcmAppDocRel::getDocCode).collect(Collectors.toList());
        ecmDocDefs = ecmDocDefMapper.selectList(new QueryWrapper<EcmDocDef>()
                .in("doc_code", docCodes));
        // 根据 appDocRels 中元素的顺序对 ecmDocDefs 进行排序
        ecmDocDefs = sortedByDoc(ecmDocDefs, appDocRels);
        //没有选择角色给出空模版
        List<EcmDocrightDefDTO> docrightDefExtendList = getDocRightDefEmptyTemplate(ecmDocDefs, appCode, rightVer);
        if (ObjectUtils.isEmpty(roleId)) {
            return docrightDefExtendList;
        }
        List<EcmDocrightDefDTO> emptyList = getEcmDocrightDefDTOS(appCode, rightVer, docrightDefExtendList, roleId,lotDim);

        // operateFlag 区分建模和其他 暂时一直未null 不走逻辑
        if (IcmsConstants.ONE.equals(operateFlag)) {
            docrightDefExtendList.forEach(s -> {
                s.setRoleDimVal(roleId.get(0));
            });
            //空白模版中若有已经定义过的资料权限，则赋值到对应资料
            if (CollectionUtil.isNotEmpty(docrightDefExtendList) && CollectionUtil.isNotEmpty(emptyList)) {
                Map<String, List<EcmDocrightDefDTO>> groupByDocTypeId = emptyList.stream().collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
                docrightDefExtendList.stream().forEach(s -> {
                    List<EcmDocrightDefDTO> ecmDocrightDefList = groupByDocTypeId.get(s.getDocCode());
                    if (CollectionUtil.isNotEmpty(ecmDocrightDefList)) {
                        EcmDocrightDefDTO ecmDocrightDef = ecmDocrightDefList.get(0);
                        if (s.getDocCode().equals(ecmDocrightDef.getDocCode())) {
                            BeanUtil.copyProperties(ecmDocrightDef, s);

                        }
                    }
                });
            }
//            addDocFileTypeListWithFileType(docrightDefExtendList, username);
        }
//        else {
////            //添加允许上传文件类型列表
//            docrightDefExtendList = BeanUtil.copyToList(docrightDefList,EcmDocrightDefDTO.class);
//            addDocFileTypeList(docrightDefExtendList);
//        }
        return docrightDefExtendList;
    }

    /**
     * 获取结构树
     */
    public List<EcmDocTreeDTO> searchOldRelevanceInformationTreeNew(String parentId, String parentName, Map<String, List<EcmDocDefDTO>> groupingByParent, List<String> docCodes, List<EcmDocTreeDTO> list1, Map<String, List<EcmAppDocRel>> collect) {
        List<EcmDocTreeDTO> ecmDocTreeDTOS = new ArrayList<>();
        //得到该子节点的类的信息
        groupingByParent.forEach((k, v) -> {
            final Integer[] j = {0};
            if (parentId.equals(k)) {
                for (EcmDocDefDTO e : v) {
                    //得到该子节点的类的信息
                    EcmDocTreeDTO ecmDocTreeDTO = new EcmDocTreeDTO();
                    ecmDocTreeDTO.setDocCode(e.getDocCode());
                    ecmDocTreeDTO.setDocCode(e.getDocCode());
                    ecmDocTreeDTO.setId(e.getDocCode());
                    ecmDocTreeDTO.setLabel(e.getDocName());
                    ecmDocTreeDTO.setDocName(e.getDocName());
                    ecmDocTreeDTO.setDocSort(e.getDocSort());
                    //下面代码若不注释，资料建模顺序改变，已关联资料不会改变
//                    List<EcmAppDocRel> ecmAppDocRels = collect.get(e.getDocCode());
//                    if (!ObjectUtils.isEmpty(ecmAppDocRels)) {
//                        ecmDocTreeDTO.setDocSort(Float.valueOf(ecmAppDocRels.get(StateConstants.ZERO).getDocSort()));
//                    }
                    ecmDocTreeDTO.setParent(parentId);
                    ecmDocTreeDTO.setParentName(parentName);
                    ecmDocTreeDTO.setIsParent(e.getIsParent());
                    //查询该子节点的子节点列表
                    final Integer[] i = {0};
                    groupingByParent.forEach((k1, v1) -> {
                        if (k1.equals(e.getDocCode())) {
                            List<EcmDocTreeDTO> ecmDocTreeExtends1 = searchOldRelevanceInformationTreeNew(e.getDocCode(), e.getDocName(), groupingByParent, docCodes, list1, collect);
                            ecmDocTreeDTO.setChildren(ecmDocTreeExtends1);
                            ecmDocTreeDTO.setType(RoleConstants.ZERO);
                            i[0] = 1;
                        }
                    });
                    if (i[0] == 0) {
                        if (docCodes.contains(e.getDocCode())) {
                            j[0] = 1;
                            ecmDocTreeDTO.setType(RoleConstants.ONE);
                        }
                    }
                    if (!com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(ecmDocTreeDTO.getChildren()) || j[0] == 1) {
                        ecmDocTreeDTOS.add(ecmDocTreeDTO);
                        list1.add(ecmDocTreeDTO);
                        j[0] = 0;
                    }
                }
            }
        });
        return ecmDocTreeDTOS;
    }


    /**
     * 获取所以角色权限的 并集
     */
    public List<EcmDocrightDefDTO> getEcmDocrightDefDTOS(String appCode, Integer rightVer, List<EcmDocrightDefDTO> docrightDefExtendList, List<String> strings, Integer lotDim) {
        //查询已经配置了权限的资料
        List<EcmDocrightDef> docrightDefList = ecmDocrightDefMapper.selectList(new QueryWrapper<EcmDocrightDef>()
                .eq("dim_type", lotDim)
                .in("role_dim_val", strings)
                .eq("app_code", appCode)
                .eq("right_ver", rightVer));
        if (org.springframework.util.CollectionUtils.isEmpty(docrightDefList)) {
            return Collections.emptyList();
        }
        Map<String, List<EcmDocrightDef>> collect = docrightDefList.stream().collect(Collectors.groupingBy(EcmDocrightDef::getDocCode));

        for (EcmDocrightDefDTO ecmDocrightDefDTO : docrightDefExtendList) {
            List<EcmDocrightDef> ecmDocrightDefs = collect.get(ecmDocrightDefDTO.getDocCode());
            if (!CollectionUtils.isEmpty(ecmDocrightDefs)) {
                ecmDocrightDefs.forEach(s -> {
                    if (StateConstants.YES.toString().equals(s.getDeleteRight())) {
                        ecmDocrightDefDTO.setDeleteRight(StateConstants.YES.toString());
                    }
                    if (StateConstants.YES.toString().equals(s.getAddRight())) {
                        ecmDocrightDefDTO.setAddRight(StateConstants.YES.toString());
                    }
                    if (StateConstants.YES.toString().equals(s.getDownloadRight())) {
                        ecmDocrightDefDTO.setDownloadRight(StateConstants.YES.toString());
                    }
                    if (StateConstants.YES.toString().equals(s.getPrintRight())) {
                        ecmDocrightDefDTO.setPrintRight(StateConstants.YES.toString());
                    }
                    if (StateConstants.YES.toString().equals(s.getReadRight())) {
                        ecmDocrightDefDTO.setReadRight(StateConstants.YES.toString());
                    }
                    if (StateConstants.YES.toString().equals(s.getThumRight())) {
                        ecmDocrightDefDTO.setThumRight(StateConstants.YES.toString());
                    }
                    if (StateConstants.YES.toString().equals(s.getUpdateRight())) {
                        ecmDocrightDefDTO.setUpdateRight(StateConstants.YES.toString());
                    }
                    if (StateConstants.YES.toString().equals(s.getOtherUpdate())) {
                        ecmDocrightDefDTO.setOtherUpdate(StateConstants.YES.toString());
                    }
                    //设置多维度配置的资料最大最小上传数
                    ecmDocrightDefDTO.setMaxLen(s.getMaxLen());
                    ecmDocrightDefDTO.setMinLen(s.getMinLen());
                    ecmDocrightDefDTO.setEnableLenLimit(s.getEnableLenLimit());
                });
            }
        }
        return null;
    }


    private List<EcmDocDef> sortedByDoc(List<EcmDocDef> ecmDocDefs, List<EcmAppDocRel> appDocRels) {
        List<EcmDocDef> sortedResult = new ArrayList<>();
        Map<String, List<EcmDocDef>> groupedByDocTypeId = ecmDocDefs.stream().collect(Collectors.groupingBy(EcmDocDef::getDocCode));
        for (EcmAppDocRel appDocRel : appDocRels) {
            List<EcmDocDef> ecmDocDefs1 = groupedByDocTypeId.get(appDocRel.getDocCode());
            if (!org.springframework.util.CollectionUtils.isEmpty(ecmDocDefs1)) {
                sortedResult.add(ecmDocDefs1.get(0));
            }
        }
        return sortedResult;
    }

    /**
     * 配置资料权限空白模版
     */
    public List<EcmDocrightDefDTO> getDocRightDefEmptyTemplate(List<EcmDocDef> ecmDocDefs, String appCode, Integer rightVer) {
        List<EcmDocrightDefDTO> docrightDefExtendList = new ArrayList<>();
        for (EcmDocDef def : ecmDocDefs) {
            EcmDocrightDefDTO docrightDefExtend = new EcmDocrightDefDTO();
//            docrightDefExtend.setRoleDimVal(ObjectUtils.isEmpty(roleId) ? "" : roleId.toString());
            docrightDefExtend.setAppCode(appCode);
            docrightDefExtend.setRightVer(rightVer);
            docrightDefExtend.setDimType(DocRightConstants.ROLE_DIM);
            docrightDefExtend.setIsUse(DocRightConstants.ONE);
            docrightDefExtend.setDocCode(def.getDocCode());
            docrightDefExtend.setDocName(def.getDocName());
            docrightDefExtend.setAddRight(DocRightConstants.ZERO.toString());
            docrightDefExtend.setDeleteRight(DocRightConstants.ZERO.toString());
            docrightDefExtend.setUpdateRight(DocRightConstants.ZERO.toString());
            docrightDefExtend.setReadRight(DocRightConstants.ZERO.toString());
            docrightDefExtend.setThumRight(DocRightConstants.ZERO.toString());
            docrightDefExtend.setPrintRight(DocRightConstants.ZERO.toString());
            docrightDefExtend.setDownloadRight(DocRightConstants.ZERO.toString());
            docrightDefExtend.setOtherUpdate(DocRightConstants.ZERO.toString());
//            docrightDefExtend.setMaxPages(DocRightConstants.ONE_THOUSAND);
//            docrightDefExtend.setMinPages(DocRightConstants.ZERO);
            docrightDefExtend.setEnableLenLimit(IcmsConstants.ZERO.toString());
            docrightDefExtendList.add(docrightDefExtend);
        }
        return docrightDefExtendList;
    }




    private List<EcmDocrightDefDTO> getEcmDocrightDefExtendsByRole1(String appCode, Integer rightVer, List<String> roleId, String username, Integer operateFlag) {
        List<String> docCodes = new ArrayList<>();
        List<EcmDocDefRelVer> ecmDocDefs = new ArrayList<>();
        List<EcmAppDocRel> ecmAppDocRels = ecmAppDocRelMapper.selectList(new QueryWrapper<EcmAppDocRel>()
                .eq("app_code", appCode)
                .eq("type", IcmsConstants.ONE));
        if (org.springframework.util.CollectionUtils.isEmpty(ecmAppDocRels)) {
            return Collections.emptyList();
        }
        docCodes = ecmAppDocRels.stream().map(EcmAppDocRel::getDocCode).collect(Collectors.toList());

        ecmDocDefs = ecmDocDefRelVerMapper.selectList(new QueryWrapper<EcmDocDefRelVer>()
                .in("doc_code", docCodes)
                .eq("app_code", appCode)
                .eq("right_ver", rightVer)
                .orderByAsc("doc_sort"));
        if (org.springframework.util.CollectionUtils.isEmpty(ecmDocDefs)) {
            return Collections.emptyList();
        }
        // 根据 appDocRels 中元素的顺序对 ecmDocDefs 进行排序
        //没有选择角色给出空模版
        List<EcmDocrightDefDTO> docrightDefExtendList = getDocRightDefEmptyTemplate1(ecmDocDefs, appCode, rightVer);
        if (ObjectUtils.isEmpty(roleId)) {
            return docrightDefExtendList;
        }
        List<EcmDocrightDefDTO> emptyList = getEcmDocrightDefDTOS(appCode, rightVer, docrightDefExtendList, roleId, DocRightConstants.ROLE_DIM);


        // operateFlag 区分建模和其他 暂时一直未null 不走逻辑
        if (IcmsConstants.ONE.equals(operateFlag)) {
            docrightDefExtendList.forEach(s -> {
                s.setRoleDimVal(roleId.get(0));
            });
            //空白模版中若有已经定义过的资料权限，则赋值到对应资料
            if (CollectionUtil.isNotEmpty(docrightDefExtendList) && CollectionUtil.isNotEmpty(emptyList)) {
                Map<String, List<EcmDocrightDefDTO>> groupByDocTypeId = emptyList.stream().collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
                docrightDefExtendList.stream().forEach(s -> {
                    List<EcmDocrightDefDTO> ecmDocrightDefList = groupByDocTypeId.get(s.getDocCode());
                    if (CollectionUtil.isNotEmpty(ecmDocrightDefList)) {
                        EcmDocrightDefDTO ecmDocrightDef = ecmDocrightDefList.get(0);
                        if (s.getDocCode().equals(ecmDocrightDef.getDocCode())) {
                            BeanUtil.copyProperties(ecmDocrightDef, s);

                        }
                    }
                });
            }
        }
        return docrightDefExtendList;
    }


    private List<EcmDocrightDefDTO> getDocRightDefEmptyTemplate1(List<EcmDocDefRelVer> ecmDocDefs, String appCode, Integer rightVer) {
        List<EcmDocrightDefDTO> docrightDefExtendList = new ArrayList<>();
        for (EcmDocDefRelVer def : ecmDocDefs) {
            EcmDocrightDefDTO docrightDefExtend = new EcmDocrightDefDTO();
            docrightDefExtend.setAppCode(appCode);
            docrightDefExtend.setRightVer(rightVer);
            docrightDefExtend.setDimType(DocRightConstants.ROLE_DIM);
            docrightDefExtend.setIsUse(DocRightConstants.ONE);
            docrightDefExtend.setDocCode(def.getDocCode());
            docrightDefExtend.setDocName(def.getDocName());
            docrightDefExtend.setAddRight(DocRightConstants.ZERO.toString());
            docrightDefExtend.setDeleteRight(DocRightConstants.ZERO.toString());
            docrightDefExtend.setUpdateRight(DocRightConstants.ZERO.toString());
            docrightDefExtend.setReadRight(DocRightConstants.ZERO.toString());
            docrightDefExtend.setThumRight(DocRightConstants.ZERO.toString());
            docrightDefExtend.setPrintRight(DocRightConstants.ZERO.toString());
            docrightDefExtend.setDownloadRight(DocRightConstants.ZERO.toString());
            docrightDefExtend.setOtherUpdate(DocRightConstants.ZERO.toString());
            docrightDefExtend.setEnableLenLimit(IcmsConstants.ZERO.toString());
//            docrightDefExtend.setMaxPages(DocRightConstants.ONE_THOUSAND);
//            docrightDefExtend.setMinPages(DocRightConstants.ZERO);
            docrightDefExtendList.add(docrightDefExtend);
        }
        return docrightDefExtendList;
    }

    /**
     * 查询当前是否拥有权限
     */
    public Set<String> getAppCodeHaveByToken(String appCode, AccountTokenExtendDTO token, String right) {
        List<EcmDocrightDef> ecmDocrightDefs = getAppCodeHaveByTokenAll(appCode, token);

        Map<String, List<EcmDocrightDef>> collect1 = ecmDocrightDefs.stream().collect(Collectors.groupingBy(EcmDocrightDef::getAppCode));
        //最新版本
        List<EcmAppDocright> ecmAppDocrights = ecmAppDocrightMapper.selectList(new LambdaQueryWrapper<EcmAppDocright>()
                .eq(EcmAppDocright::getRightNew,StateConstants.YES)
                .eq(StrUtil.isNotBlank(appCode), EcmAppDocright::getAppCode, appCode));
        List<EcmDocrightDef> defs = new ArrayList<EcmDocrightDef>();

        for(EcmAppDocright ecmAppDocright:ecmAppDocrights){
            List<EcmDocrightDef> ecmDocrightDefs1 = collect1.get(ecmAppDocright.getAppCode());
            if(CollectionUtils.isEmpty(ecmDocrightDefs1)){
                continue;
            }
            Map<Integer, List<EcmDocrightDef>> collect = ecmDocrightDefs1.stream().collect(Collectors.groupingBy(EcmDocrightDef::getRightVer));
            List<EcmDocrightDef> ecmDocrightDefs2 = collect.get(ecmAppDocright.getRightVer());
            if (CollectionUtils.isEmpty(ecmDocrightDefs2)) {
                continue;
            } else {
                if (!hasRight(ecmDocrightDefs2, right)) {
                    continue;
                }
            }
            defs.addAll(ecmDocrightDefs2);
        }

        //最新版本的权限
        Set<String> collect = defs.stream().map(EcmDocrightDef::getAppCode).collect(Collectors.toSet());

        //为了构建完整的树，需要将父级添加进去
        List<EcmAppDefRel> ecmAppDefRels = null;
        if (CollectionUtil.isNotEmpty(collect)){
            ecmAppDefRels = ecmAppDefRelMapper.selectList(new LambdaQueryWrapper<EcmAppDefRel>().in(EcmAppDefRel::getAppCode, collect));
        }
        if(CollectionUtils.isEmpty(ecmAppDefRels)){
            return collect;
        }else{
            Set<String> collect2 = ecmAppDefRels.stream().map(EcmAppDefRel::getParent).collect(Collectors.toSet());
            collect.addAll(collect2);
            return collect;
        }
    }

    /**
     * 获取有权限的资料类型
     */
    public Set<String> getDocCodeHaveByToken(String appCode, AccountTokenExtendDTO token, String right) {
        List<EcmDocrightDef> ecmDocrightDefs = getAppCodeHaveByReadToken(appCode, token);

        Map<String, List<EcmDocrightDef>> collect1 = ecmDocrightDefs.stream().collect(Collectors.groupingBy(EcmDocrightDef::getAppCode));
        //最新版本
        List<EcmAppDocright> ecmAppDocrights = ecmAppDocrightMapper.selectList(new LambdaQueryWrapper<EcmAppDocright>()
                .eq(EcmAppDocright::getRightNew,StateConstants.YES)
                .eq(StrUtil.isNotBlank(appCode), EcmAppDocright::getAppCode, appCode));
        List<EcmDocrightDef> defs = new ArrayList<EcmDocrightDef>();

        for(EcmAppDocright ecmAppDocright:ecmAppDocrights){
            List<EcmDocrightDef> ecmDocrightDefs1 = collect1.get(ecmAppDocright.getAppCode());
            if(CollectionUtils.isEmpty(ecmDocrightDefs1)){
                continue;
            }
            Map<Integer, List<EcmDocrightDef>> collect = ecmDocrightDefs1.stream().collect(Collectors.groupingBy(EcmDocrightDef::getRightVer));
            List<EcmDocrightDef> ecmDocrightDefs2 = collect.get(ecmAppDocright.getRightVer());
            if (CollectionUtils.isEmpty(ecmDocrightDefs2)) {
                continue;
            } else {
                if (!hasRight(ecmDocrightDefs2, right)) {
                    continue;
                }
            }
            defs.addAll(ecmDocrightDefs2);
        }

        //最新版本的权限
        Set<String> docCodeSet = defs.stream().map(EcmDocrightDef::getDocCode).collect(Collectors.toSet());

        //为了构建完整的树，需要将父级添加进去
        List<EcmDocDefRel> ecmDocDefRels = null;
        if (CollectionUtil.isNotEmpty(docCodeSet)){
            ecmDocDefRels = ecmDocDefRelMapper.selectList(new LambdaQueryWrapper<EcmDocDefRel>().in(EcmDocDefRel::getDocCode, docCodeSet));
        }
        if(CollectionUtils.isEmpty(ecmDocDefRels)){
            return docCodeSet;
        }else{
            Set<String> collect2 = ecmDocDefRels.stream().map(EcmDocDefRel::getParent).collect(Collectors.toSet());
            docCodeSet.addAll(collect2);
            return docCodeSet;
        }
    }

    /**
     * 判断是否有查看权限
     */
    private boolean isHavereadRight(List<EcmDocrightDef> ecmDocrightDefs2) {
        List<EcmDocrightDef> collect2 = ecmDocrightDefs2.stream().filter(p -> (StateConstants.YES.toString().equals(p.getReadRight()) || StateConstants.YES.toString().equals(p.getDownloadRight()))).collect(Collectors.toList());
        return CollectionUtils.isEmpty(collect2);
    }

    /**
     * 判断是否有权限
     * @param defs
     * @param right
     * @return
     */
    private boolean hasRight(List<EcmDocrightDef> defs, String right) {
        if (StringUtils.isEmpty(right)) {
            return true;
        }
        RightCheckers.RightChecker checker = RIGHT_CHECKER_MAP.get(right);
        if (checker == null) {
            return false;
        }
        return defs != null && defs.stream().anyMatch(checker::check);
    }

    /**
     * 获取当前用户所拥有的权限树
     */
    public List<EcmDocrightDef> getAppCodeHaveByTokenAll(String appCode, AccountTokenExtendDTO token) {
        //根据角色，查询处拥有权限的树
        List<Long> roleIds = getRoleByToken(token);
        if(CollectionUtils.isEmpty(roleIds)){
            return null;
        }
        List<String> roleIdsStr = roleIds.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        List<EcmDocrightDef> ecmDocrightDefs = ecmDocrightDefMapper.selectList(new LambdaQueryWrapper<EcmDocrightDef>()
                .in(EcmDocrightDef::getRoleDimVal, roleIdsStr)
                .eq(StrUtil.isNotBlank(appCode), EcmDocrightDef::getAppCode, appCode)
                .and(s -> s.eq(EcmDocrightDef::getDeleteRight, StateConstants.YES.toString())
                        .or()
                        .eq(EcmDocrightDef::getAddRight, StateConstants.YES.toString())
                        .or()
                        .eq(EcmDocrightDef::getDownloadRight, StateConstants.YES.toString())
                        .or()
                        .eq(EcmDocrightDef::getPrintRight, StateConstants.YES.toString())
                        .or()
                        .eq(EcmDocrightDef::getThumRight, StateConstants.YES.toString())
                        .or()
                        .eq(EcmDocrightDef::getReadRight, StateConstants.YES.toString())
                        .or()
                        .eq(EcmDocrightDef::getUpdateRight, StateConstants.YES.toString())
                )
        );
        return ecmDocrightDefs;
    }

    /**
     * 获取当前用户所拥有查看权限的权限树
     */
    public List<EcmDocrightDef> getAppCodeHaveByReadToken(String appCode, AccountTokenExtendDTO token) {
        //根据角色，查询处拥有权限的树
        List<Long> roleIds = getRoleByToken(token);
        if (CollectionUtils.isEmpty(roleIds)) {
            return null;
        }
        List<String> roleIdsStr = roleIds.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        List<EcmDocrightDef> ecmDocrightDefs = ecmDocrightDefMapper.selectList(new LambdaQueryWrapper<EcmDocrightDef>()
                .in(EcmDocrightDef::getRoleDimVal, roleIdsStr)
                .eq(StrUtil.isNotBlank(appCode), EcmDocrightDef::getAppCode, appCode)
                .and(s -> s.eq(EcmDocrightDef::getDeleteRight, StateConstants.YES.toString())
                        .or()
                        .eq(EcmDocrightDef::getAddRight, StateConstants.YES.toString())
                        .or()
                        .eq(EcmDocrightDef::getDownloadRight, StateConstants.YES.toString())
                        .or()
                        .eq(EcmDocrightDef::getPrintRight, StateConstants.YES.toString())
                        .or()
                        .eq(EcmDocrightDef::getThumRight, StateConstants.YES.toString())
                        .or()
                        .eq(EcmDocrightDef::getReadRight, StateConstants.YES.toString())
                        .or()
                        .eq(EcmDocrightDef::getUpdateRight, StateConstants.YES.toString())
                )
        );
        return ecmDocrightDefs;
    }

    public List<String> getAppCodesWithAddOrUpdatePerm(AccountTokenExtendDTO token) {
        List<EcmAppDef> allApps = ecmAppDefMapper.selectList(null);
        List<String> allAppCodes = allApps.stream().map(EcmAppDef::getAppCode).collect(Collectors.toList());

        // 获取有权限的appcode
        Set<String> addPermApps = getAppCodeHaveByToken(null, token, "add");
        Set<String> updatePermApps = getAppCodeHaveByToken(null, token, "update");

        // 合并两个权限集合
        java.util.Set<String> union = new java.util.HashSet<>();
        if (addPermApps != null) union.addAll(addPermApps);
        if (updatePermApps != null) union.addAll(updatePermApps);

        // 返回系统中存在的app code，同时保留allAppCodes的顺序
        return allAppCodes.stream()
                .filter(union::contains)
                .collect(Collectors.toList());
    }
}
