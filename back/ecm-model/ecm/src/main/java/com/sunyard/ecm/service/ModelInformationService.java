package com.sunyard.ecm.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.manager.StaticTreePermissService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmDocDefDTO;
import com.sunyard.ecm.dto.ecm.EcmDocPlaCheDTO;
import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.enums.StrategyConstantsEnum;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmAppDefRelMapper;
import com.sunyard.ecm.mapper.EcmAppDocRelMapper;
import com.sunyard.ecm.mapper.EcmBusiDocMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmDocDefRelMapper;
import com.sunyard.ecm.mapper.EcmDocDefRelVerMapper;
import com.sunyard.ecm.mapper.EcmDocPlagCheMapper;
import com.sunyard.ecm.mapper.EcmDtdDefMapper;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmAppDefRel;
import com.sunyard.ecm.po.EcmAppDocRel;
import com.sunyard.ecm.po.EcmBusiDoc;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmDocDefRel;
import com.sunyard.ecm.po.EcmDocDefRelVer;
import com.sunyard.ecm.po.EcmDocPlagChe;
import com.sunyard.ecm.po.EcmDtdDef;
import com.sunyard.ecm.vo.SysStrategyVO;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.redis.constant.TimeOutConstants;
import com.sunyard.module.system.api.DictionaryApi;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysDictionaryDTO;
import com.sunyard.module.system.api.dto.SysParamDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author： zyl
 * @create： 2023/4/13 16:38
 * @desc: 影像资料接口
 */
@Slf4j
@Service
public class ModelInformationService {
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmDocDefRelMapper ecmDocDefRelMapper;
    @Resource
    private EcmDocDefRelVerMapper ecmDocDefRelVerMapper;
    @Resource
    private EcmAppDocRelMapper ecmAppDocRelMapper;
    @Resource
    private EcmAppAttrMapper ecmAppAttrMapper;
    @Resource
    private EcmDtdDefMapper ecmDtdDefMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmBusiDocMapper ecmBusiDocMapper;
    @Resource
    private EcmAppDefRelMapper ecmAppDefRelMapper;
    @Resource
    private EcmDocPlagCheMapper ecmDocPlagCheMapper;
//    @Resource
//    private EcmDocDynaPlagMapper ecmDocDynaPlagMapper;
    @Resource
    private ParamApi paramApi;
    @Resource
    private UserApi userApi;
    @Resource
    private DictionaryApi dictionaryApi;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private StaticTreePermissService staticTreePermissService;

    /**
     * 新增资料类型
     */
    @Transactional(rollbackFor = Exception.class)
    public Result addInformationType(EcmDocDefDTO ecmDocDef, String userId) {
        AssertUtils.isNull(ecmDocDef.getDocCode(), "参数错误！");
        AssertUtils.isNull(ecmDocDef.getDocName(), "参数错误！");
        Result paramError = checkEcmDocDefExtend(ecmDocDef, true);
        if (paramError != null) {
            return paramError;
        }
        //处理顺序号
        EcmDocDef ecmDocDef1 = new EcmDocDef();
        BeanUtils.copyProperties(ecmDocDef, ecmDocDef1);
        ecmDocDef1.setMaxFiles(ecmDocDef.getMaxLen());
        ecmDocDef1.setMinFiles(ecmDocDef.getMinLen());
        dealDocSort(ecmDocDef1, ecmDocDef.getParent());
        ecmDocDef1.setCreateUser(userId);
        //查询加密情况
        /*Result<SysParamDTO> result = paramApi.searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString());
        SysParamDTO data = result.getData();
        String value = data.getValue();
        SysStrategyVO sysStrategyVO = JSONObject.parseObject(value, SysStrategyVO.class);
        if (sysStrategyVO.getEncryptStatus()) {
            ecmDocDef1.setIsEncrypt(IcmsConstants.YES_ENCRYPT);
        } else {
            ecmDocDef1.setIsEncrypt(IcmsConstants.NO_ENCRYPT);
        }*/
        ecmDocDefMapper.insert(ecmDocDef1);
        //插入资料类型定义必包表
        addEcmDocDefRel(ecmDocDef1);
        return Result.success(true);
        //        }
        //        return Result.error("无法作为父节点，该资料类型已关联文档", ResultCode.PARAM_ERROR);
    }

    private void addEcmDocDefRel(EcmDocDef ecmDocDef) {
        //插入前端传的父节点
        if (ObjectUtils.isEmpty(ecmDocDef.getParent())) {
            ecmDocDef.setParent(IcmsConstants.DOC_LEVEL_FIRST);
        }
        //插入父节点为自己
        EcmDocDefRel ecmDocDefRel1 = new EcmDocDefRel();
        ecmDocDefRel1.setDocCode(ecmDocDef.getDocCode());
        ecmDocDefRel1.setParent(ecmDocDef.getDocCode());
        ecmDocDefRelMapper.insert(ecmDocDefRel1);
        if (!StateConstants.ZERO.toString().equals(ecmDocDef.getParent())) {
            //该节点的父节点的所有父节点集合
            List<EcmDocDefRel> ecmDocDefRels = ecmDocDefRelMapper.selectList(null).stream()
                    .collect(Collectors.groupingBy(EcmDocDefRel::getDocCode))
                    .get(ecmDocDef.getParent());
            AssertUtils.isNull(ecmDocDefRels, "参数错误");
            for (EcmDocDefRel e : ecmDocDefRels) {
                EcmDocDefRel ecmDocDefRel2 = new EcmDocDefRel();
                ecmDocDefRel2.setDocCode(ecmDocDef.getDocCode());
                ecmDocDefRel2.setParent(e.getParent());
                //todo 改批量插入
                ecmDocDefRelMapper.insert(ecmDocDefRel2);
            }
        }
    }

    //校验
    private Result checkEcmDocDefExtend(EcmDocDefDTO ecmDocDef, boolean b) {
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectList(null);
        //得到该节点同级的资料类型id 不包含本身
        List<String> docCodes = ecmDocDefRelMapper
                .selectList(new LambdaQueryWrapper<EcmDocDefRel>().eq(EcmDocDefRel::getParent, ecmDocDef.getParent()))
                .stream().map(EcmDocDefRel::getDocCode).collect(Collectors.toList()).stream()
                .filter(x -> !x.equals(ecmDocDef.getDocCode())).collect(Collectors.toList());
        //查询资料是否已经冠关联文档
        if (b) {
            //判断资料类型代码不能重复
            List<String> docCodes1 = ecmDocDefs.stream().map(EcmDocDef::getDocCode)
                    .collect(Collectors.toList());
            // 统一转为小写（或全大写）后判断包含关系
            if (!ObjectUtils.isEmpty(docCodes1)) {
                String targetDocCode = ecmDocDef.getDocCode();
                // 遍历集合中的每个元素，与目标值进行大小写不敏感比较
                boolean isDuplicate = docCodes1.stream()
                        .anyMatch(code -> code.equalsIgnoreCase(targetDocCode));
                if (isDuplicate) {
                    return Result.error("资料类型代码不能重复！", ResultCode.PARAM_ERROR);
                }
            }
        }
        //得到同级的资料类型名称
        List<String> docNames = ecmDocDefs.stream().filter(x -> docCodes.contains(x.getDocCode()))
                .collect(Collectors.toList()).stream().map(EcmDocDef::getDocName)
                .collect(Collectors.toList());
        if (docNames.contains(ecmDocDef.getDocName())) {
            return Result.error("同一资料类型复资料类型名称不可重复！", ResultCode.PARAM_ERROR);
        }
        //判断资料类型是否在最子级父级目录
       /* //isParent:0代表为资料类型，需校验;1为父级目录无需校验
        if (StateConstants.ZERO.equals(ecmDocDef.getIsParent())
                && !StateConstants.ZERO.toString().equals(ecmDocDef.getParent())) {
            String parent = ecmDocDef.getParent();
            List<EcmDocDef> parentEcmDocDefs = ecmDocDefMapper
                    .selectList(new LambdaQueryWrapper<EcmDocDef>().eq(EcmDocDef::getParent, parent).eq(EcmDocDef::getIsParent,
                            StateConstants.COMMON_ONE));
            if (!CollectionUtil.isEmpty(parentEcmDocDefs)) {
                return Result.error("资料类型只可在最子节点的“父级目录”下！", ResultCode.PARAM_ERROR);
            }
        }*/
        return null;
    }

    /**
     * 处理顺序号（顺序号重复：占用原来的，后面的都加一）
     */
    private void dealDocSort(EcmDocDef ecmDocDef, String parentId) {
        //查询该节点的父节点下的所有节点，用于顺序号的调整
        List<EcmDocDef> ecmDocDefRels = ecmDocDefMapper
                .selectList(new LambdaQueryWrapper<EcmDocDef>().eq(EcmDocDef::getParent, parentId));
        if (!CollectionUtils.isEmpty(ecmDocDefRels)) {
            //筛选出该档案父节点的所有子节点的资料类型id
            List<String> docCodes = ecmDocDefRels.stream().map(EcmDocDef::getDocCode)
                    .collect(Collectors.toList());
            //根据查出的资料类型id集合查询出该父节点下的所有的子节点信息
            List<EcmDocDef> ecmDocDefs = ecmDocDefMapper
                    .selectList(new LambdaQueryWrapper<EcmDocDef>().in(EcmDocDef::getDocCode, docCodes));
            List<Float> collect = ecmDocDefs.stream().map(EcmDocDef::getDocSort)
                    .collect(Collectors.toList());
            //如果顺序号存在，该父节点下的子节点的顺序号大于该节点的顺序号的节点顺序号加一
            if (collect.contains(ecmDocDef.getDocSort())) {
                for (EcmDocDef docDef : ecmDocDefs) {
                    if (docDef.getDocSort() >= ecmDocDef.getDocSort()) {
                        Float docSort = docDef.getDocSort();
                        docDef.setDocSort(++docSort);
                        ecmDocDefMapper.updateById(docDef);
                        //修改缓存
                        busiCacheService.setDocInfo(ecmDocDef, TimeOutConstants.SEVEN_DAY);
                    }
                }
            }
            //如果该顺序号为空并且是新增、自动递增
            if (ObjectUtils.isEmpty(ecmDocDef.getDocSort())) {
                ecmDocDef.setDocSort(docSortAutoIncrement(ecmDocDefs));
            }
        } else {
            ecmDocDef.setDocSort(1.0F);
        }
    }

    /**
     * 资料顺序号自动递增
     */
    private Float docSortAutoIncrement(List<EcmDocDef> list) {
        Float docSort = list.stream().map(EcmDocDef::getDocSort).collect(Collectors.toList())
                .stream().max(Comparator.comparing(x -> x)).orElse(null);
        if (ObjectUtils.isEmpty(docSort)) {
            docSort = 1.0F;
        } else {
            ++docSort;
        }
        return docSort;
    }

    /**
     * 编辑资料类型
     */
    @Transactional(rollbackFor = Exception.class)
    public Result editInformationType(EcmDocDefDTO ecmDocDef, String userId) {
        Result paramError = checkEcmDocDefExtend(ecmDocDef, false);
        if (paramError != null) {
            return paramError;
        }
        AssertUtils.isNull(ecmDocDef.getDocCode(), "参数错误！");
        AssertUtils.isNull(ecmDocDef.getDocName(), "参数错误！");
        EcmDocDef ecmDocDef1 = new EcmDocDef();
        BeanUtils.copyProperties(ecmDocDef, ecmDocDef1);
        ecmDocDef1.setMaxFiles(ecmDocDef.getMaxLen());
        ecmDocDef1.setMinFiles(ecmDocDef.getMinLen());
        //        dealDocSort(ecmDocDef1, ecmDocDef.getParent());
        ecmDocDef1.setUpdateUser(userId);
        //是否加密
        /*Result<SysParamDTO> result = paramApi.searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString());
        SysParamDTO data = result.getData();
        String value = data.getValue();
        SysStrategyVO sysStrategyVO = JSONObject.parseObject(value, SysStrategyVO.class);
        if (sysStrategyVO.getEncryptStatus()) {
            ecmDocDef1.setIsEncrypt(IcmsConstants.YES_ENCRYPT);
        } else {
            ecmDocDef1.setIsEncrypt(IcmsConstants.NO_ENCRYPT);
        }*/
        //更新文件类型

        Result<Map<String, List<com.sunyard.module.system.api.dto.SysDictionaryDTO>>> dictionaryAll = dictionaryApi
                .getDictionaryAll(IcmsConstants.FILE_TYPE_DIC, null);
        ecmDocDef1.setImgLimit(handleFileType(ecmDocDef1.getImgLimit(), dictionaryAll,
                IcmsConstants.ECMS_COMMON_FILETYPE_IMG));
        ecmDocDef1.setOfficeLimit(handleFileType(ecmDocDef1.getOfficeLimit(), dictionaryAll,
                IcmsConstants.ECMS_COMMON_FILETYPE_OFFICE));
        ecmDocDef1.setAudioLimit(handleFileType(ecmDocDef1.getAudioLimit(), dictionaryAll,
                IcmsConstants.ECMS_COMMON_FILETYPE_YP));
        ecmDocDef1.setVideoLimit(handleFileType(ecmDocDef1.getVideoLimit(), dictionaryAll,
                IcmsConstants.ECMS_COMMON_FILETYPE_SP));
        ecmDocDef1.setOtherLimit(handleFileType(ecmDocDef1.getOtherLimit(), dictionaryAll,
                IcmsConstants.ECMS_COMMON_FILETYPE_OTHER));
        ecmDocDefMapper.updateById(ecmDocDef1);
        //修改缓存
        busiCacheService.setDocInfo(ecmDocDef1, TimeOutConstants.SEVEN_DAY);
        //修改父节点
        //删除原来的闭包关系
        ecmDocDefRelMapper
                .delete(new LambdaQueryWrapper<EcmDocDefRel>().eq(EcmDocDefRel::getDocCode, ecmDocDef.getDocCode()));
        //重新插入闭包关系
        addEcmDocDefRel(ecmDocDef1);
        return Result.success(true);
    }

    private String handleFileType(String limit,
                                  Result<Map<String, List<com.sunyard.module.system.api.dto.SysDictionaryDTO>>> dictionaryAll,
                                  String dickey) {
        if (!StringUtils.isEmpty(limit)) {
            JSONObject jsonObject = JSONObject.parseObject(limit);
            List<com.sunyard.module.system.api.dto.SysDictionaryDTO> sysDictionaryDTOS = dictionaryAll
                    .getData().get(IcmsConstants.FILE_TYPE_DIC);
            Map<String, List<SysDictionaryDTO>> collect = sysDictionaryDTOS.stream()
                    .collect(Collectors.groupingBy(SysDictionaryDTO::getDicKey));
            List<SysDictionaryDTO> sysDictionaryDTOS1 = collect.get(dickey);
            String value1 = sysDictionaryDTOS1.get(0).getValue();
            JSONObject jsonObject1 = JSONObject.parseObject(value1);
            Object limitFormat = jsonObject1.get("limit_format");
            jsonObject.put("limit_format", limitFormat);
            return jsonObject.toJSONString();
        }
        return null;
    }

    /**
     * 删除资料类型
     */
    @Transactional(rollbackFor = Exception.class)
    public Result deleteInformationType(EcmDocDefDTO ecmDocDefDTO) {
        AssertUtils.isNull(ecmDocDefDTO.getDocCode(), "资料类型id不能为空");
        String docCode = ecmDocDefDTO.getDocCode();
        List<EcmAppDocRel> ecmAppDocRel = ecmAppDocRelMapper
                .selectList(new LambdaQueryWrapper<EcmAppDocRel>().eq(EcmAppDocRel::getDocCode, docCode));
        //查资料类型下下有无子资料
        if (CollectionUtil.isNotEmpty(ecmDocDefDTO.getParents())) {
            List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectList(
                    new LambdaQueryWrapper<EcmDocDef>().in(EcmDocDef::getDocCode, ecmDocDefDTO.getParents()));
            List<EcmDocDef> collect = ecmDocDefs.stream()
                    .filter(ecmDocDef -> StateConstants.ZERO.equals(ecmDocDef.getIsParent()))
                    .collect(Collectors.toList());
            if (collect.size() > StateConstants.ZERO) {
                return Result.error("无法删除，该目录下存在子节点", ResultCode.PARAM_ERROR);
            }
        }
        //查询该资料是否被业务关联
        List<EcmBusiDoc> ecmBusiDocs = ecmBusiDocMapper
                .selectList(new LambdaQueryWrapper<EcmBusiDoc>().eq(EcmBusiDoc::getDocCode, docCode));
        if (!CollectionUtils.isEmpty(ecmBusiDocs)) {
            return Result.error("无法删除，该资料已被被业务关联", ResultCode.PARAM_ERROR);
        }
        //删除子集
        List<String> delCodes = new ArrayList<>();
        delCodes.add(docCode);
        delCodes.addAll(ecmDocDefDTO.getParents());
        if (ObjectUtils.isEmpty(ecmAppDocRel)) {
            for (String code : delCodes) {
                //cancelRelevanceDtd(code);
                ecmDocDefMapper.deleteById(code);
                ecmDocDefRelMapper.delete(new LambdaQueryWrapper<EcmDocDefRel>().eq(EcmDocDefRel::getDocCode, code));
            }
            return Result.success(true);
        } else {
            return Result.error("无法删除，该资料类型已被关联", ResultCode.PARAM_ERROR);
        }

    }

    //todo 缺少注释
    public Result<EcmDocDefDTO> searchInformationType(String docCode, String parentId,
                                                      String parentName) {
        EcmDocDefDTO ecmDocDefDTO = new EcmDocDefDTO();
        EcmDocDef ecmDocDef = ecmDocDefMapper.selectById(docCode);
        AssertUtils.isNull(ecmDocDef, "资料节点不存在");
        BeanUtils.copyProperties(ecmDocDef, ecmDocDefDTO);
        ecmDocDefDTO.setMinLen(ecmDocDef.getMinFiles());
        ecmDocDefDTO.setMaxLen(ecmDocDef.getMaxFiles());
        List<EcmDocDefRel> parents = ecmDocDefRelMapper
                .selectList(new LambdaQueryWrapper<EcmDocDefRel>().eq(EcmDocDefRel::getDocCode, docCode));
        List<String> collect = parents.stream().filter(s -> !s.equals(0L))
                .map(EcmDocDefRel::getParent).collect(Collectors.toList());
        ecmDocDefDTO.setParents(collect);
        String directParentId = ecmDocDef.getParent();
        if (directParentId != null && !"0".equals(directParentId)) {
            ecmDocDefDTO.setParent(directParentId);
            EcmDocDef parentDef = ecmDocDefMapper.selectById(directParentId);
            ecmDocDefDTO.setParentName(parentDef != null ? parentDef.getDocName() : null);
        } else {
            ecmDocDefDTO.setParent("0");
            ecmDocDefDTO.setParentName("无");
        }
        return Result.success(getUserNames(ecmDocDef, ecmDocDefDTO));
    }

    private EcmDocDefDTO getUserNames(EcmDocDef ecmDocDef, EcmDocDefDTO ecmDocDefDTO) {
        String createUser = ecmDocDef.getCreateUser();
        String updateUser = ecmDocDef.getUpdateUser();
        List<String> userIds = new ArrayList<>();
        if (!ObjectUtils.isEmpty(createUser)) {
            userIds.add(createUser);
        }
        if (!ObjectUtils.isEmpty(updateUser)) {
            userIds.add(updateUser);
        }
        Result<List<SysUserDTO>> result = userApi
                .getUserListByUsernames(userIds.toArray(new String[0]));
        List<SysUserDTO> list = new ArrayList<>();
        if (result.isSucc()) {
            list = result.getData();
        } else {
            throw new SunyardException(result.getMsg());
        }
        if (!ObjectUtils.isEmpty(list)) {
            for (SysUserDTO e : list) {
                if (!ObjectUtils.isEmpty(ecmDocDef.getCreateUser())) {
                    if (e.getLoginName().equals(ecmDocDef.getCreateUser())) {
                        ecmDocDefDTO.setCreateUserName(e.getName());
                    }
                }
                if (!ObjectUtils.isEmpty(ecmDocDef.getUpdateUser())) {
                    if (e.getLoginName().equals(ecmDocDef.getUpdateUser())) {
                        ecmDocDefDTO.setUpdateUserName(e.getName());
                    }
                }
            }
        }
        SysStrategyVO sysStrategyVO = getSysStrategyVO();
        if (!sysStrategyVO.getEncryptStatus()) {
            ecmDocDefDTO.setIsEncryptStr("不加密");
        } else {
            ecmDocDefDTO.setIsEncryptStr("加密");
        }

        return ecmDocDefDTO;
    }

    private SysStrategyVO getSysStrategyVO() {
        Result<SysParamDTO> sysParam = paramApi
                .searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString());
        SysParamDTO data = sysParam.getData();
        String value = data.getValue();
        return JSONObject.parseObject(value, SysStrategyVO.class);
    }

    /**
     * 查询资料类型树
     */
    public Result<List<EcmDocTreeDTO>> searchInformationTypeTree(String docCode) {
        List<EcmDocTreeDTO> ecmDocTreeDTOS = new ArrayList<>();
        //使用分组优化树
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectList(null);
        getSearchInformationResult(docCode, ecmDocTreeDTOS, ecmDocDefs);
        return Result.success(ecmDocTreeDTOS);
    }

    /**
     * 查询父级目录树
     */
    public Result<List<EcmDocTreeDTO>> searchInformationParentTypeTree(String docCode) {
        List<EcmDocTreeDTO> ecmDocTreeDTOS = new ArrayList<>();
        //使用分组优化树
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper
                .selectList(new LambdaQueryWrapper<EcmDocDef>().eq(EcmDocDef::getIsParent, IcmsConstants.ONE));
        getSearchInformationResult(docCode, ecmDocTreeDTOS, ecmDocDefs);
        return Result.success(ecmDocTreeDTOS);
    }

    private void getSearchInformationResult(String docCode, List<EcmDocTreeDTO> ecmDocTreeDTOS,
                                            List<EcmDocDef> ecmDocDefs) {
        Map<String, List<EcmDocDef>> groupingByParent = ecmDocDefs.stream()
                .collect(Collectors.groupingBy(EcmDocDef::getParent));
        groupingByParent.values()
                .forEach(list -> list.sort(Comparator.comparing(EcmDocDef::getDocSort)));
        Map<String, List<EcmDocDef>> groupedByDocCode = ecmDocDefs.stream()
                .collect(Collectors.groupingBy(EcmDocDef::getDocCode));
        LambdaQueryWrapper<EcmDocPlagChe> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(EcmDocPlagChe::getType, IcmsConstants.STATIC_TREE);
        wrapper.isNotNull(EcmDocPlagChe::getDocCode);
        List<EcmDocPlagChe> ecmDocPlagChes = ecmDocPlagCheMapper.selectList(wrapper);
        Map<String, List<EcmDocPlagChe>> relDocCodes = ecmDocPlagChes.stream()
                .collect(Collectors.groupingBy(EcmDocPlagChe::getDocCode));
        Result<List<SysDictionaryDTO>> dictionaryResult = dictionaryApi
                .selectValueByParentKey(IcmsConstants.DICTIONARY_CODE, null);
//        Map<String, List<EcmDocDynaPlag>> dynaType = ecmDocDynaPlagMapper.selectList(null).stream()
//                .collect(Collectors.groupingBy(EcmDocDynaPlag::getDocCode));
        informationTypeTreeNew(ecmDocTreeDTOS, StateConstants.ZERO.toString(), "无", groupingByParent, docCode, relDocCodes,
                groupedByDocCode, dictionaryResult);
        generateDocName(ecmDocTreeDTOS);
    }

    /**
     * 名字生成
     */
    private void generateDocName(List<EcmDocTreeDTO> ecmDocTreeDTOS) {
        for (EcmDocTreeDTO ecmDocTreeDTO : ecmDocTreeDTOS) {
            String label = "(" + ecmDocTreeDTO.getDocCode() + ")" + ecmDocTreeDTO.getDocName();
            ecmDocTreeDTO.setLabel(label);
            ecmDocTreeDTO.setDocName(label);
            if (!CollectionUtils.isEmpty(ecmDocTreeDTO.getChildren())) {
                generateDocName(ecmDocTreeDTO.getChildren());
            }
        }
    }

    /**
     * 获取版本结构树
     */
    public Map getDocTreeByVerAndAppCode(String appCode, Integer rightVer) {
        Map map = new HashMap();
        map.put("isNew", false);
        if (rightVer == null) {
            map.put("isNew", true);
            Result<List<EcmDocTreeDTO>> listResult = searchInformationTypeTree(null);
            map.put("tree", listResult.getData());
            return map;
        }
        //查权限版本
        List<EcmAppDocRel> appDocRels = ecmAppDocRelMapper
                .selectList(new LambdaQueryWrapper<EcmAppDocRel>().eq(EcmAppDocRel::getAppCode, appCode)
                        .eq(EcmAppDocRel::getType, IcmsConstants.ONE).orderByAsc(EcmAppDocRel::getDocSort));
        if (CollectionUtil.isEmpty(appDocRels)) {
            map.put("isNew", true);
            Result<List<EcmDocTreeDTO>> listResult = searchInformationTypeTree(null);
            map.put("tree", listResult.getData());
            return map;
        }
        List<String> codes = appDocRels.stream().map(EcmAppDocRel::getDocCode)
                .collect(Collectors.toList());
        List<EcmDocDefRelVer> ecmDocDefRelVers = ecmDocDefRelVerMapper
                .selectList(new LambdaQueryWrapper<EcmDocDefRelVer>().in(EcmDocDefRelVer::getDocCode, codes)
                        .eq(EcmDocDefRelVer::getAppCode, appCode).eq(EcmDocDefRelVer::getRightVer, rightVer).orderByAsc(EcmDocDefRelVer::getDocSort));
        //无关联资料类型直接返回
        if (CollectionUtil.isEmpty(ecmDocDefRelVers)) {
            map.put("isNew", true);
            Result<List<EcmDocTreeDTO>> listResult = searchInformationTypeTree(null);
            map.put("tree", listResult.getData());
        } else {
            //关联树
            List<String> docCodeList = ecmDocDefRelVers.stream().map(EcmDocDefRelVer::getDocCode)
                    .collect(Collectors.toList());
            //            Map<String, List<EcmDocDefRelVer>> collect = ecmDocDefRelVers.stream().collect(Collectors.groupingBy(EcmDocDefRelVer::getDocCode));
            List<EcmDocDef> ecmDocDefs = ecmDocDefMapper
                    .selectList(new LambdaQueryWrapper<EcmDocDef>().in(EcmDocDef::getDocCode, docCodeList));
            ecmDocDefs = ecmDocDefs.stream().sorted(Comparator.comparing(EcmDocDef::getDocSort))
                    .collect(Collectors.toList());
            //完整树
            List<EcmDocDef> ecmDocDefsAll = ecmDocDefMapper.selectList(
                    new LambdaQueryWrapper<EcmDocDef>().orderByAsc(EcmDocDef::getDocSort));
            List<EcmDocDefDTO> ecmDocDefDTOS = PageCopyListUtils.copyListProperties(ecmDocDefsAll,
                    EcmDocDefDTO.class);
            //            ecmDocDefDTOS.forEach(s -> {
            //                if(!CollectionUtils.isEmpty(collect.get(s.getDocCode()))){
            //                    s.setDocSort(Float.parseFloat(collect.get(s.getDocCode()).get(0).getDocSort().toString()));
            //                }
            //            });
            //            ecmDocDefDTOS = ecmDocDefDTOS.stream().sorted(Comparator.comparing(EcmDocDefDTO::getDocSort)).collect(Collectors.toList());

            Map<String, List<EcmDocDefDTO>> parentListMap1 = ecmDocDefDTOS.stream()
                    .collect(Collectors.groupingBy(EcmDocDefDTO::getParent));

            //根据自己反查
            List<String> docCodes = ecmDocDefs.stream().map(EcmDocDef::getDocCode)
                    .collect(Collectors.toList());
            List<EcmDocTreeDTO> retTree = staticTreePermissService
                    .searchOldRelevanceInformationTreeNew(StateConstants.ZERO.toString(), "无",
                            parentListMap1, docCodes, new ArrayList<>(), null);
            map.put("tree", retTree);
        }
        return map;
    }

    /**
     * 拖拽树
     *
     * @param sourceId       目标要修改的资料类型id
     * @param upSort         要插入的上一个节点的顺序号
     * @param downSort       要插入的下一个节点的顺序号
     * @param sourceParentId 源父节点id
     * @param targetParentId 目标父节点id
     */
    @Transactional(rollbackFor = Exception.class)
    public Result dragTree(String sourceId, Float sourceSort, Float upSort, Float downSort,
                           String sourceParentId, String targetParentId, Integer type,
                           AccountTokenExtendDTO token) {
        AssertUtils.isNull(sourceId, "要修改的资料类型不可为空");
        if (ObjectUtils.isEmpty(upSort)) {
            upSort = 0F;
        }
        if (ObjectUtils.isEmpty(downSort)) {
            sourceSort = ++upSort;
        }
        if (ObjectUtils.isEmpty(upSort) && ObjectUtils.isEmpty(downSort)) {
            sourceSort = 1F;
        }
        if (!ObjectUtils.isEmpty(upSort) && !ObjectUtils.isEmpty(downSort)) {
            sourceSort = (upSort + downSort) / 2;
        }

        if (type == 1) {
            //判断是否可以移动到改位置
            //查询资料是否已经冠关联文档
            ecmDocDefMapper.update(null,
                    new UpdateWrapper<EcmDocDef>().set("doc_sort", sourceSort)
                            .set("parent", targetParentId).set("update_user", token.getUsername())
                            .set("update_time", new Date()).eq("doc_code", sourceId));
            //更新缓存
            EcmDocDef ecmDocDef = ecmDocDefMapper.selectById(sourceId);
            if (!sourceParentId.equals(targetParentId)) {
                ecmDocDefRelMapper.update(null, new UpdateWrapper<EcmDocDefRel>()
                        .set("parent", targetParentId).eq("doc_code", sourceId));
            }
            busiCacheService.setDocInfo(ecmDocDef, TimeOutConstants.SEVEN_DAY);
        } else if (type == 2) {
            UpdateWrapper<EcmAppDef> updateWrapper = new UpdateWrapper<EcmAppDef>()
                    .set("app_sort", sourceSort).set("update_user", token.getUsername())
                    .set("update_time", new Date()).eq("app_code", sourceId);
            if (!sourceParentId.equals(targetParentId)) {
                updateWrapper.set("parent", targetParentId);
                //判断是否可以移动到改位置
                //查询有没有关联资料
                List<EcmAppDocRel> ecmAppDocRels = ecmAppDocRelMapper.selectList(
                        new LambdaQueryWrapper<EcmAppDocRel>().eq(EcmAppDocRel::getAppCode, targetParentId));
                //查询有没有关联属性
                List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper
                        .selectList(new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, targetParentId));
                if (!ObjectUtils.isEmpty(ecmAppDocRels)) {
                    return Result.error("无法作为父节点，该业务类型已关联资料", ResultCode.PARAM_ERROR);
                }
                if (!ObjectUtils.isEmpty(ecmAppAttrs)) {
                    return Result.error("该业务类型已经关联了属性，无法作为父节点！", ResultCode.PARAM_ERROR);
                }
            }
            ecmAppDefMapper.update(null, updateWrapper);
            //清除闭包表数据及重建
            rebuildEcmAppDefRel(sourceId, targetParentId);
        } else {
            ecmDtdDefMapper.update(null, new UpdateWrapper<EcmDtdDef>().set("dtd_sort", sourceSort)
                    .eq("dtd_type_id", sourceId));
        }
        return Result.success(true);
    }

    private List<EcmDocTreeDTO> informationTypeTreeNew(List<EcmDocTreeDTO> ecmDocTreeDTOS,
                                                       String parentId, String parentName,
                                                       Map<String, List<EcmDocDef>> groupingByParent,
                                                       String docCode,
                                                       Map<String, List<EcmDocPlagChe>> relDocCodes,
                                                       Map<String, List<EcmDocDef>> groupedByDocCode,
                                                       Result<List<SysDictionaryDTO>> dictionaryResult) {

        //得到该子节点的类的信息
        groupingByParent.forEach((k, v) -> {
            if (parentId.equals(k)) {
                for (EcmDocDef e : v) {
                    //得到该子节点的类的信息
                    EcmDocTreeDTO ecmDocTreeDTO = new EcmDocTreeDTO();
                    ecmDocTreeDTO.setDocCode(e.getDocCode());
                    ecmDocTreeDTO.setDocName(e.getDocName());
                    ecmDocTreeDTO.setLabel(e.getDocName());
                    ecmDocTreeDTO.setId(e.getDocCode());
                    ecmDocTreeDTO.setDocSort(e.getDocSort());
                    ecmDocTreeDTO.setParent(parentId);

                    ecmDocTreeDTO.setDisabled(
                            !ObjectUtils.isEmpty(docCode) && e.getDocCode().equals(docCode));
                    ecmDocTreeDTO.setParentName(parentName);
                    ecmDocTreeDTO.setIsParent(e.getIsParent());

                    ecmDocTreeDTO.setIsPlagiarism(e.getIsPlagiarism());
                    ecmDocTreeDTO.setIsPlagiarismText(e.getIsPlagiarismText());
                    ecmDocTreeDTO.setIsRemade(e.getIsRemade());
                    ecmDocTreeDTO.setIsRegularized(e.getIsRegularized());
                    ecmDocTreeDTO.setIsObscured(e.getIsObscured());
                    ecmDocTreeDTO.setDocType(IcmsConstants.STATIC_TREE);
                    ecmDocTreeDTO.setIsAutoClassified(e.getIsAutoClassified());
                    ecmDocTreeDTO.setIsReflective(e.getIsReflective());
                    ecmDocTreeDTO.setIsCornerMissing(e.getIsCornerMissing());

                    if (e.getAutoClassificationId() != null
                            && !e.getAutoClassificationId().isEmpty()) {
                        //多选拆分
                        String[] classificationIds = e.getAutoClassificationId().split(",");

                        StringBuilder autoClassificationNames = new StringBuilder();

                        if (dictionaryResult != null && dictionaryResult.getData() != null) {
                            for (String classificationId : classificationIds) {
                                for (SysDictionaryDTO sysDictionaryDTO : dictionaryResult
                                        .getData()) {
                                    // 遍历字典数据，查找与 classificationId 匹配的项
                                    if (sysDictionaryDTO.getDicVal().equals(classificationId)) {
                                        if (autoClassificationNames.length() > 0) {
                                            autoClassificationNames.append(",");
                                        }
                                        autoClassificationNames
                                                .append(sysDictionaryDTO.getRemark());
                                        break;
                                    }
                                }
                            }
                        }

                        // 设置拼接后的名称字符串
                        ecmDocTreeDTO.setAutoClassificationName(autoClassificationNames.toString());
                    }
                    ecmDocTreeDTO.setAutoClassificationId(e.getAutoClassificationId());

                    if (!IcmsConstants.IS_PARENT.equals(e.getIsParent())) {
                        insertAdditionalData(ecmDocTreeDTO, relDocCodes, groupedByDocCode);
                    }
                    //查询该子节点的子节点列表
                    final Integer[] i = { 0 };
                    groupingByParent.forEach((k1, v1) -> {
                        if (k1.equals(e.getDocCode())) {
                            List<EcmDocTreeDTO> ecmDocTreeExtends2 = new ArrayList<>();
                            List<EcmDocTreeDTO> ecmDocTreeExtends1 = informationTypeTreeNew(
                                    ecmDocTreeExtends2, e.getDocCode(), e.getDocName(),
                                    groupingByParent, docCode, relDocCodes, groupedByDocCode,
                                    dictionaryResult);
                            ecmDocTreeDTO.setChildren(ecmDocTreeExtends1);
                            ecmDocTreeDTO.setType(StateConstants.COMMON_ONE);
                            i[0] = 1;
                        }
                    });
                    if (i[0] == 0) {
                        ecmDocTreeDTO.setType(StateConstants.ZERO);
                    }
                    ecmDocTreeDTOS.add(ecmDocTreeDTO);

                }
            }
        });
        return ecmDocTreeDTOS;
    }

    /**
     * 在子节点插入其他数据
     */
    public void insertAdditionalData(EcmDocTreeDTO leafNode,
                                     Map<String, List<EcmDocPlagChe>> relDocCodes,
                                     Map<String, List<EcmDocDef>> groupedByDocCode) {
        List<EcmDocPlagChe> ecmDocPlagChes = relDocCodes.get(leafNode.getDocCode());

        List<String> relDocNames = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ecmDocPlagChes)) {
            ecmDocPlagChes.forEach(ecmDocPlagChe -> {
                if (ecmDocPlagChe.getQueryType().equals(IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ALL)) {
                    String relName = "查询范围:所有类型";
                    relDocNames.add(relName);
                } else {
//                    if (ecmDocPlagChe.getRelType().equals(IcmsConstants.STATIC_TREE)) {
//                        String relName = groupedByDocCode.get(ecmDocPlagChe.getRelDocCode()).get(0)
//                                .getDocName();
//                        relDocNames.add(relName);
//                    } else if (ecmDocPlagChe.getRelType().equals(IcmsConstants.DYNAMIC_TREE)) {
//                        String relName = dynaType.get(ecmDocPlagChe.getRelDocCode()).get(0)
//                                .getDocName();
//                        relDocNames.add(relName);
//                    } else {
//                        log.error("资料类型有误或不存在");
//                    }
                }
            });
        }
        EcmDocPlaCheDTO additionalChild = new EcmDocPlaCheDTO();
        additionalChild.setRelDocNames(relDocNames);
        if (!CollectionUtils.isEmpty(ecmDocPlagChes)) {
            additionalChild.setFrameYear(ecmDocPlagChes.get(0).getFrameYear());
            additionalChild.setFileSimilarity(ecmDocPlagChes.get(0).getFileSimilarity());
            additionalChild.setQueryType(ecmDocPlagChes.get(0).getQueryType());
        }
        leafNode.setPlagiarismCheckPolicy(additionalChild);
    }

    //todo 缺少注释
    public Result<List<EcmDocTreeDTO>> searchDynamicPlagiarism() {
        List<EcmDocTreeDTO> ecmDocTreeDTOS = new ArrayList<>();
        //使用分组优化树
//        List<EcmDocDynaPlag> ecmDocDynaPlags = ecmDocDynaPlagMapper.selectList(null);

        //        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectList(null);
        getSearchInformationResult2(null, ecmDocTreeDTOS);
        return Result.success(ecmDocTreeDTOS);
    }

    private void getSearchInformationResult2(String docCode, List<EcmDocTreeDTO> ecmDocTreeDTOS) {
        LambdaQueryWrapper<EcmDocPlagChe> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(EcmDocPlagChe::getType, IcmsConstants.DYNAMIC_TREE);
        wrapper.isNotNull(EcmDocPlagChe::getDocCode);
        List<EcmDocPlagChe> ecmDocPlagChes = ecmDocPlagCheMapper.selectList(wrapper);

        Map<String, List<EcmDocPlagChe>> list = ecmDocPlagChes.stream()
                .collect(Collectors.groupingBy(EcmDocPlagChe::getDocCode));
        List<EcmDocDef> ecmDocDefs1 = ecmDocDefMapper
                .selectList(new LambdaQueryWrapper<EcmDocDef>());
        Map<String, List<EcmDocDef>> collect = ecmDocDefs1.stream()
                .collect(Collectors.groupingBy(EcmDocDef::getDocCode));
//        for (EcmDocDynaPlag docDynaPlag : ecmDocDefs) {
//
//            EcmDocTreeDTO dto = new EcmDocTreeDTO();
//            dto.setId(docDynaPlag.getId().toString());
//            dto.setDocName(docDynaPlag.getDocName());
//            dto.setDocCode(docDynaPlag.getDocCode());
//            dto.setLabel(docDynaPlag.getDocName());
//            dto.setDocType(IcmsConstants.DYNAMIC_TREE);
//            dto.setIsPlagiarism(docDynaPlag.getIsPlagiarism());
//            List<EcmDocPlagChe> ecmDocPlagChes2 = list.get(docDynaPlag.getDocCode());
//            if (!CollectionUtils.isEmpty(ecmDocPlagChes2)) {
//                EcmDocPlagChe ecmDocPlagChe = ecmDocPlagChes2.get(0);
//                dto.setQueryType(ecmDocPlagChe.getQueryType());
//                dto.setFrameYear(ecmDocPlagChe.getFrameYear());
//                dto.setFileSimilarity(ecmDocPlagChe.getFileSimilarity());
//                Map<String, List<EcmDocPlagChe>> relDocCodes = ecmDocPlagChes2.stream().filter(
//                        s -> IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ADD.equals(s.getQueryType()))
//                        .collect(Collectors.groupingBy(EcmDocPlagChe::getDocCode));
//                List<EcmDocPlagChe> ecmDocPlagChes1 = relDocCodes.get(dto.getDocCode());
//                if (!CollectionUtils.isEmpty(ecmDocPlagChes1)) {
//                    ArrayList<EcmDocPlaRelNameDTO> objects = new ArrayList<>();
//                    for (EcmDocPlagChe docPlagChe : ecmDocPlagChes1) {
//                        EcmDocPlaRelNameDTO dto1 = new EcmDocPlaRelNameDTO();
//                        dto1.setRelDoccode(docPlagChe.getRelDocCode());
//                        List<EcmDocDef> ecmDocDefs2 = collect.get(docPlagChe.getRelDocCode());
//                        if (!CollectionUtils.isEmpty(ecmDocDefs2)) {
//                            EcmDocDef ecmDocDef = ecmDocDefs2.get(0);
//                            dto1.setRelDocName(ecmDocDef.getDocName());
//                        }
//                        dto1.setRelType(docPlagChe.getRelType());
//                        objects.add(dto1);
//                    }
//                    dto.setRelDocCodes(objects);
//                }
//            }
//            ecmDocTreeDTOS.add(dto);
//
//        }
    }

    /**
     * 重建闭包表数据
     */
    public void rebuildEcmAppDefRel(String appCode, String parentCode) {
        ecmAppDefRelMapper.delete(new LambdaQueryWrapper<EcmAppDefRel>().eq(EcmAppDefRel::getAppCode, appCode));
        //插入子节点与父节点的关系
        EcmAppDefRel ecmAppDefRel11 = new EcmAppDefRel();
        ecmAppDefRel11.setAppCode(appCode);
        ecmAppDefRel11.setParent(parentCode);
        ecmAppDefRelMapper.insert(ecmAppDefRel11);
        if (!StateConstants.PARENT_APP_CODE_DEFAULT.equals(parentCode)) {
            //父节点的所有父节点集合
            List<EcmAppDefRel> ecmAppDefRels = ecmAppDefRelMapper.selectList(null).stream()
                    .collect(Collectors.groupingBy(EcmAppDefRel::getAppCode)).get(parentCode);
            AssertUtils.isNull(ecmAppDefRels, "参数错误");
            for (EcmAppDefRel e : ecmAppDefRels) {
                EcmAppDefRel ecmAppDefRel1 = new EcmAppDefRel();
                ecmAppDefRel1.setAppCode(appCode);
                ecmAppDefRel1.setParent(e.getParent());
                //todo 改批量插入
                ecmAppDefRelMapper.insert(ecmAppDefRel1);
            }
        }
    }
}
