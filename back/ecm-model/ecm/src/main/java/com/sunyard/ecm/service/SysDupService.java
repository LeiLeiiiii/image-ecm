package com.sunyard.ecm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.sunyard.ecm.dto.ecm.EcmDocPlaRelNameDTO;
import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmPlagiarismStateDTO;
import com.sunyard.ecm.dto.ecm.TypeStateDTO;
import com.sunyard.ecm.manager.BusiCacheService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmDocPlagCheMapper;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmDocPlagChe;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.framework.redis.constant.TimeOutConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * @author ljw
 * @since 2025/2/21
 */
@Slf4j
@Service
public class SysDupService {

    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmDocPlagCheMapper ecmDocPlagCheMapper;
//    @Resource
//    private EcmDocDynaPlagMapper ecmDocDynaPlagMapper;
    @Resource
    private ModelInformationService modelInformationService;
    @Resource
    private BusiCacheService busiCacheService;

    /**
     * 更新静态文档查重配置
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStaticPlagiarismState(List<EcmPlagiarismStateDTO> vo) {
        if (!CollectionUtils.isEmpty(vo)) {
            //获取所有字段集合
            List<String> collect = vo.stream().map(EcmPlagiarismStateDTO::getDocCode)
                    .collect(Collectors.toList());
            //删除关联表中有关联的静态字段信息
            LambdaQueryWrapper<EcmDocPlagChe> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.in(EcmDocPlagChe::getDocCode, collect);
            ecmDocPlagCheMapper.delete(wrapper1);
            vo.forEach(relDocs -> {
                Integer queryType = relDocs.getQueryType();
                AssertUtils.isNull(queryType, "资料节点类型不能为空");
                if (queryType.equals(IcmsConstants.GLOBAL_PLAG_CHE_QUERY_CURR)) {
                    EcmDocPlagChe ecmDocPlagChe = new EcmDocPlagChe();
                    ecmDocPlagChe.setId(snowflakeUtil.nextId());
                    ecmDocPlagChe.setDocCode(relDocs.getDocCode());
                    ecmDocPlagChe.setFrameYear(relDocs.getFrameYear());
                    ecmDocPlagChe.setFileSimilarity(relDocs.getFileSimilarity());
//                    ecmDocPlagChe.setType(IcmsConstants.STATIC_TREE);
                    ecmDocPlagChe.setQueryType(queryType);
                    ecmDocPlagChe.setRelDocCode(relDocs.getDocCode());
//                    ecmDocPlagChe.setRelType(IcmsConstants.STATIC_TREE);
                    ecmDocPlagCheMapper.insert(ecmDocPlagChe);
                } else if (queryType.equals(IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ALL)) {
                    EcmDocPlagChe ecmDocPlagChe = new EcmDocPlagChe();
                    ecmDocPlagChe.setId(snowflakeUtil.nextId());
                    ecmDocPlagChe.setDocCode(relDocs.getDocCode());
                    ecmDocPlagChe.setRelDocCode(null);
                    ecmDocPlagChe.setFrameYear(relDocs.getFrameYear());
                    ecmDocPlagChe.setFileSimilarity(relDocs.getFileSimilarity());
//                    ecmDocPlagChe.setType(IcmsConstants.STATIC_TREE);
                    ecmDocPlagChe.setQueryType(queryType);
                    ecmDocPlagCheMapper.insert(ecmDocPlagChe);
                } else {
                    if (!CollectionUtils.isEmpty(relDocs.getRelDocCodes())) {
                        relDocs.getRelDocCodes().forEach(relDoc -> {
                            EcmDocPlagChe ecmDocPlagChe = new EcmDocPlagChe();
                            ecmDocPlagChe.setId(snowflakeUtil.nextId());
                            ecmDocPlagChe.setDocCode(relDocs.getDocCode());
                            ecmDocPlagChe.setRelDocCode(relDoc.getRelDoccode());
                            ecmDocPlagChe.setFrameYear(relDocs.getFrameYear());
                            ecmDocPlagChe.setFileSimilarity(relDocs.getFileSimilarity());
//                            ecmDocPlagChe.setType(IcmsConstants.STATIC_TREE);
//                            ecmDocPlagChe.setRelType(relDoc.getRelType());
                            ecmDocPlagChe.setQueryType(queryType);
                            //todo 改批量插入
                            ecmDocPlagCheMapper.insert(ecmDocPlagChe);
                        });
                    }
                }
            });
        }
        return true;
    }

    /**
     * 更新动态文档查重配置
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveDynamicPlagiarismState(List<EcmPlagiarismStateDTO> vo) {
        if (!CollectionUtils.isEmpty(vo)) {
            List<EcmPlagiarismStateDTO> inserts = new ArrayList<>();
            List<EcmPlagiarismStateDTO> updates = new ArrayList<>();
            vo.forEach(dynaDoc -> {
                if (dynaDoc.getId() == null) {
                    inserts.add(dynaDoc);
                } else {
                    updates.add(dynaDoc);
                }
            });

//            List<EcmDocDynaPlag> ecmDocDynaPlags = ecmDocDynaPlagMapper
//                    .selectList(new LambdaQueryWrapper<>());
//            List<Long> collect = ecmDocDynaPlags.stream().map(EcmDocDynaPlag::getId)
//                    .collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(updates)) {
                List<Long> collect1 = updates.stream().map(EcmPlagiarismStateDTO::getId)
                        .collect(Collectors.toList());
//                List<Long> collect11 = collect.stream().filter(s -> !collect1.contains(s))
//                        .collect(Collectors.toList());
//                if (!CollectionUtils.isEmpty(collect11)) {
//                    LambdaQueryWrapper<EcmDocDynaPlag> eq = new LambdaQueryWrapper<EcmDocDynaPlag>()
//                            .in(EcmDocDynaPlag::getId, collect11);
//                    ecmDocDynaPlagMapper.delete(eq);
//                }

                List<String> collect3 = updates.stream().map(EcmPlagiarismStateDTO::getDocCode)
                        .collect(Collectors.toList());
                LambdaQueryWrapper<EcmDocPlagChe> wrapper = new LambdaQueryWrapper<>();
                wrapper.in(EcmDocPlagChe::getDocCode, collect3);
                ecmDocPlagCheMapper.delete(wrapper);
                updates.forEach(this::updateDynamicPlagiarism);
            } else {
//                if (!CollectionUtils.isEmpty(collect)) {
//                    LambdaQueryWrapper<EcmDocDynaPlag> eq = new LambdaQueryWrapper<EcmDocDynaPlag>()
//                            .notIn(EcmDocDynaPlag::getId, collect);
//                    ecmDocDynaPlagMapper.delete(eq);
//                }
            }
            if (!CollectionUtils.isEmpty(inserts)) {
                insertDynamicPlagiarism(inserts);
            }
        } else {
            LambdaQueryWrapper<EcmDocPlagChe> wrapper = new LambdaQueryWrapper<>();
//            wrapper.eq(EcmDocPlagChe::getType, IcmsConstants.DYNAMIC_TREE);
            ecmDocPlagCheMapper.delete(wrapper);
//            List<EcmDocDynaPlag> ecmDocDynaPlags = ecmDocDynaPlagMapper
//                    .selectList(new LambdaQueryWrapper<>());
//            List<Long> collect = ecmDocDynaPlags.stream().map(EcmDocDynaPlag::getId)
//                    .collect(Collectors.toList());
//            ecmDocDynaPlagMapper.deleteBatchIds(collect);
        }
        return true;
    }

    /**
     * 查询查重资料类型树
     */
    public Result<List<EcmDocTreeDTO>> searchPlagiarismTypeTree(int state) {
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectChildren(null, null, null, state, null);
        List<EcmDocDef> allEcmDocDefs = ecmDocDefMapper.selectList(null);
        Map<String, List<EcmDocDef>> groupedByDocCode = allEcmDocDefs.stream()
                .collect(Collectors.groupingBy(EcmDocDef::getDocCode));
        LambdaQueryWrapper<EcmDocPlagChe> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(EcmDocPlagChe::getType, IcmsConstants.STATIC_TREE);
        wrapper.isNotNull(EcmDocPlagChe::getDocCode);
        List<EcmDocPlagChe> ecmDocPlagChes = ecmDocPlagCheMapper.selectList(wrapper);
        Map<String, List<EcmDocPlagChe>> relDocCodes = ecmDocPlagChes.stream()
                .collect(Collectors.groupingBy(EcmDocPlagChe::getDocCode));
        // 构建树结构
        List<EcmDocTreeDTO> ecmDocTreeDTOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ecmDocDefs)) {
            for (EcmDocDef ecmDocDef : ecmDocDefs) {
                EcmDocTreeDTO ecmDocTreeDTO = new EcmDocTreeDTO();
                BeanUtils.copyProperties(ecmDocDef, ecmDocTreeDTO);
                ecmDocTreeDTOS.add(ecmDocTreeDTO);
                // 递归提取父节点
                extractParent(ecmDocDef, ecmDocTreeDTOS, groupedByDocCode);
            }
            // 将 ecmDocTreeDTOS 转换为树形结构
            List<EcmDocTreeDTO> tree = buildTree(ecmDocTreeDTOS, relDocCodes, groupedByDocCode);
            return Result.success(tree);
        }
        return Result.success(ecmDocTreeDTOS);
    }

    //todo 未使用
    /**
     * 查询查重开关状态
     */
    public Result<List<EcmDocTreeDTO>> searchPlagiarismTypeTree() {
        List<EcmDocDef> allEcmDocDefs = ecmDocDefMapper.selectList(null);
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectChildren(null, null, null, null, null);
        Map<String, List<EcmDocDef>> groupedByDocCode = allEcmDocDefs.stream()
                .collect(Collectors.groupingBy(EcmDocDef::getDocCode));
        LambdaQueryWrapper<EcmDocPlagChe> wrapper = new LambdaQueryWrapper();
//        wrapper.eq(EcmDocPlagChe::getType, IcmsConstants.STATIC_TREE);
        wrapper.isNotNull(EcmDocPlagChe::getDocCode);
        List<EcmDocPlagChe> ecmDocPlagChes = ecmDocPlagCheMapper.selectList(wrapper);
        Map<String, List<EcmDocPlagChe>> relDocCodes = ecmDocPlagChes.stream()
                .collect(Collectors.groupingBy(EcmDocPlagChe::getDocCode));
        // 构建树结构
        List<EcmDocTreeDTO> ecmDocTreeDTOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ecmDocDefs)) {
            for (EcmDocDef ecmDocDef : ecmDocDefs) {
                EcmDocTreeDTO ecmDocTreeDTO = new EcmDocTreeDTO();
                BeanUtils.copyProperties(ecmDocDef, ecmDocTreeDTO);
                ecmDocTreeDTOS.add(ecmDocTreeDTO);
                // 递归提取父节点
                extractParent(ecmDocDef, ecmDocTreeDTOS, groupedByDocCode);
            }
            // 将 ecmDocTreeDTOS 转换为树形结构
            List<EcmDocTreeDTO> tree = buildTree(ecmDocTreeDTOS, relDocCodes, groupedByDocCode);
            return Result.success(tree);
        }
        return Result.success(ecmDocTreeDTOS);
    }

    /**
     * 删除动态文档查重配置
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteDynamicPlagiarism(String docCode) {
        AssertUtils.isNull(docCode, "资料代码不存在");
        LambdaQueryWrapper<EcmDocPlagChe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmDocPlagChe::getDocCode, docCode);
        ecmDocPlagCheMapper.delete(wrapper);
//        LambdaQueryWrapper<EcmDocDynaPlag> wrapper1 = new LambdaQueryWrapper<>();
//        wrapper1.eq(EcmDocDynaPlag::getDocCode, docCode);
//        ecmDocDynaPlagMapper.delete(wrapper1);
        return true;
    }

    /**
     * 查询动态资料
     */
    public Result<List<EcmPlagiarismStateDTO>> searchDynamicPlagiarism() {

        Map<String, List<EcmDocDef>> staticTypeTree = ecmDocDefMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(EcmDocDef::getDocCode));
//        List<EcmDocDynaPlag> ecmDocDynaPlags = ecmDocDynaPlagMapper.selectList(null);
//        Map<String, List<EcmDocDynaPlag>> dynaTypeTree = ecmDocDynaPlags.stream()
//                .collect(Collectors.groupingBy(EcmDocDynaPlag::getDocCode));
        List<EcmDocPlagChe> ecmDocPlagChes = ecmDocPlagCheMapper
                .selectList(new LambdaQueryWrapper<EcmDocPlagChe>()
//                        .eq(EcmDocPlagChe::getType, IcmsConstants.DYNAMIC_TREE)
                        .isNotNull(EcmDocPlagChe::getDocCode));
        if (!CollectionUtils.isEmpty(ecmDocPlagChes)) {
            Map<String, List<EcmDocPlagChe>> relDocCodes = ecmDocPlagChes.stream()
                    .collect(Collectors.groupingBy(EcmDocPlagChe::getDocCode));
            List<EcmPlagiarismStateDTO> dtos = new ArrayList<>();
//            if (!CollectionUtils.isEmpty(ecmDocDynaPlags)) {
//                ecmDocDynaPlags.forEach(ecmDocDynaPlag -> {
//                    EcmPlagiarismStateDTO dto = new EcmPlagiarismStateDTO();
//                    if (!CollectionUtils.isEmpty(relDocCodes)) {
//                        List<EcmDocPlagChe> docNames = relDocCodes.get(ecmDocDynaPlag.getDocCode());
//                        if (!CollectionUtils.isEmpty(docNames)) {
//                            List<String> relNames = new ArrayList<>();
//                            docNames.forEach(docName -> {
//                                if (docName.getQueryType()
//                                        .equals(IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ALL)) {
//                                    String relName = "查询范围:所有类型";
//                                    relNames.add(relName);
//                                } else {
//                                    Integer relType = docName.getRelType();
//                                    if (IcmsConstants.STATIC_TREE.equals(relType)) {
//                                        if (!CollectionUtils.isEmpty(
//                                                staticTypeTree.get(docName.getRelDocCode()))) {
//                                            String relDocName = staticTypeTree
//                                                    .get(docName.getRelDocCode()).get(0)
//                                                    .getDocName();
//                                            relNames.add(relDocName);
//                                        }
//
//                                    } else if (IcmsConstants.DYNAMIC_TREE.equals(relType)) {
//                                        if (!CollectionUtils.isEmpty(
//                                                dynaTypeTree.get(docName.getRelDocCode()))) {
//                                            String relDocName = dynaTypeTree
//                                                    .get(docName.getRelDocCode()).get(0)
//                                                    .getDocName();
//                                            relNames.add(relDocName);
//                                        }
//
//                                    } else {
//                                        log.error("关联资料类型有误或不存在");
//                                    }
//                                }
//                            });
//                            dto.setFrameYear(docNames.get(0).getFrameYear());
//                            dto.setFileSimilarity(docNames.get(0).getFileSimilarity());
//                            dto.setDocType(IcmsConstants.DYNAMIC_TREE);
//                            dto.setRelDocNames(relNames);
//                        }
//
//                    }
//                    dto.setId(ecmDocDynaPlag.getId());
//                    dto.setDocCode(ecmDocDynaPlag.getDocCode());
//                    dto.setIsPlagiarism(ecmDocDynaPlag.getIsPlagiarism());
//                    dto.setDocName(ecmDocDynaPlag.getDocName());
//                    dtos.add(dto);
//                });
//                return Result.success(dtos);
//            }
        }
        return Result.success();
    }

    /**
     * 查询全部类型资料
     */
    public List<EcmDocTreeDTO> searchAllDoc() {
        List<EcmDocTreeDTO> staticDoc = modelInformationService.searchInformationTypeTree(null)
                .getData();
        List<EcmPlagiarismStateDTO> dynaDocs = searchDynamicPlagiarism().getData();
        List<EcmDocTreeDTO> ecmDynaTreeDTOS = new ArrayList<>();

        if (!CollectionUtils.isEmpty(dynaDocs)) {
            dynaDocs.forEach(dynaDoc -> {
                EcmDocTreeDTO ecmDocTreeDTO = new EcmDocTreeDTO();
                ecmDocTreeDTO.setDocCode(dynaDoc.getDocCode());
                ecmDocTreeDTO.setDocName(dynaDoc.getDocName());
                ecmDocTreeDTO.setDocType(IcmsConstants.DYNAMIC_TREE);
                ecmDynaTreeDTOS.add(ecmDocTreeDTO);
            });
        }
        if (!CollectionUtils.isEmpty(ecmDynaTreeDTOS)) {
            staticDoc.addAll(ecmDynaTreeDTOS);
        }
        return staticDoc;
    }

    /**
     * 更新静态资料开关状态
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStaticState(List<EcmDocDef> vo) {
        if (!CollectionUtils.isEmpty(vo)) {
            List<String> docCodes = vo.stream().map(EcmDocDef::getDocCode)
                    .collect(Collectors.toList());
            LambdaQueryWrapper<EcmDocDef> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(EcmDocDef::getDocCode, docCodes);
            List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectList(queryWrapper);
            // 按照 docCode 分组
            Map<String, List<EcmDocDef>> groupedByDocCode = ecmDocDefs.stream()
                    .collect(Collectors.groupingBy(EcmDocDef::getDocCode));
            vo.forEach(ecmStateVo -> {
                LambdaUpdateWrapper<EcmDocDef> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(EcmDocDef::getDocCode, ecmStateVo.getDocCode())
                        .set(ecmStateVo.getIsPlagiarism() != null ,EcmDocDef::getIsPlagiarism, ecmStateVo.getIsPlagiarism())
                        .set(ecmStateVo.getIsPlagiarismText() != null,EcmDocDef::getIsPlagiarismText, ecmStateVo.getIsPlagiarismText());
                ecmDocDefMapper.update(null, wrapper);
                EcmDocDef docDef=groupedByDocCode.get(ecmStateVo.getDocCode()).get(0);
                if (ecmStateVo.getIsPlagiarism() != null) {
                    docDef.setIsPlagiarism(ecmStateVo.getIsPlagiarism());
                }else if (ecmStateVo.getIsPlagiarismText() != null) {
                    docDef.setIsPlagiarismText(ecmStateVo.getIsPlagiarismText());
                }
                busiCacheService.setDocInfo(docDef,
                        TimeOutConstants.SEVEN_DAY);
            });
        }
        return true;
    }

    /**
     * 更新动态资料开关状态
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDynaState(String docCode, int state) {
//        LambdaUpdateWrapper<EcmDocDynaPlag> wrapper = new LambdaUpdateWrapper<>();
//        wrapper.eq(EcmDocDynaPlag::getDocCode, docCode).set(EcmDocDynaPlag::getIsPlagiarism, state);
//        ecmDocDynaPlagMapper.update(wrapper);
        return true;
    }

    /**
     * 取消定制
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean CancelCustomize(String docCode, int type) {
//        AssertUtils.isNull(type, "资料类型不能为空");
        AssertUtils.isNull(docCode, "资料id不能为空");
        LambdaQueryWrapper<EcmDocPlagChe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmDocPlagChe::getDocCode, docCode);
        ecmDocPlagCheMapper.delete(wrapper);
        return true;
    }

    /**
     * 查询筛选数据
     */
    public Result<List<EcmDocTreeDTO>> searchSift(List<String> docCodes, Integer state, List<TypeStateDTO> typeStates) {
        //        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectChildren(null,null,null,state);
        //查询所有数据
        List<EcmDocDef> allEcmDocDefs = ecmDocDefMapper.selectList(null);
        //如果传来的docCodes为空,则默认查全部
        if (CollectionUtils.isEmpty(docCodes)) {
            docCodes = allEcmDocDefs.stream().map(EcmDocDef::getDocCode)
                    .collect(Collectors.toList());
        }
        //根据docCode进行分组
        Map<String, List<EcmDocDef>> groupedByDocCode = allEcmDocDefs.stream()
                .collect(Collectors.groupingBy(EcmDocDef::getDocCode));
        //根据parentCode进行分组
        Map<String, List<EcmDocDef>> groupedByParentCode = allEcmDocDefs.stream()
                .collect(Collectors.groupingBy(EcmDocDef::getParent));
        LambdaQueryWrapper<EcmDocPlagChe> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(EcmDocPlagChe::getType, IcmsConstants.STATIC_TREE);
        wrapper.isNotNull(EcmDocPlagChe::getDocCode);
        List<EcmDocPlagChe> ecmDocPlagChes = ecmDocPlagCheMapper.selectList(wrapper);
        List<EcmDocDef> filteredLeafNodes = new ArrayList<>();
        //获取关联code
        Map<String, List<EcmDocPlagChe>> relDocCodes = ecmDocPlagChes.stream()
                .collect(Collectors.groupingBy(EcmDocPlagChe::getDocCode));
        docCodes.forEach(docCode -> {
            getFilteredLeafNodesFromChildren(groupedByDocCode.get(docCode).get(0),
                    filteredLeafNodes, groupedByParentCode);
        });
        //获取最子节点集合
        List<EcmDocDef> ecmDocDefs = getLastNodes(filteredLeafNodes, state);
        // 构建树结构
        List<EcmDocTreeDTO> ecmDocTreeDTOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ecmDocDefs)) {
            for (EcmDocDef ecmDocDef : ecmDocDefs) {
                EcmDocTreeDTO ecmDocTreeDTO = new EcmDocTreeDTO();
                BeanUtils.copyProperties(ecmDocDef, ecmDocTreeDTO);
                ecmDocTreeDTOS.add(ecmDocTreeDTO);
                // 递归提取父节点
                extractParent(ecmDocDef, ecmDocTreeDTOS, groupedByDocCode);
            }
            // 将 ecmDocTreeDTOS 转换为树形结构
            List<EcmDocTreeDTO> tree = buildTree(ecmDocTreeDTOS, relDocCodes, groupedByDocCode);
            //过滤条件进行筛选
            for (TypeStateDTO typeState : typeStates) {
                tree = getResultTree(tree,typeState.getState(),typeState.getType());
            }
            return Result.success(tree);
        }
        return Result.success(ecmDocTreeDTOS);
    }

    private List<EcmDocTreeDTO> getResultTree(List<EcmDocTreeDTO> tree, Integer state, Integer type) {
        List<EcmDocTreeDTO> result = new ArrayList<>();
        if (Objects.isNull(state)) {
            for (EcmDocTreeDTO node : tree) {
                calculateParentPlagiarism(node, type);
                result.add(node);
            }
        } else {

            for (EcmDocTreeDTO node : tree) {
                if (isLeafNode(node)) {
                    // 叶子节点：直接判断是否满足条件
                    if (matchesCondition(node, state, type)) {
                        result.add(node);
                    }
                } else {
                    // 先计算，再过滤子集
                    calculateParentPlagiarism(node, type);
                    //得到过滤后的子节点列表
                    List<EcmDocTreeDTO> filteredChildren = getResultTree(node.getChildren(), state, type);
                    // 保留满足条件的子节点
                    node.setChildren(new ArrayList<>(filteredChildren));
                    // 获取父节点计算后的值
                    Integer parentValue = type.equals(4) ? node.getIsPlagiarism() : node.getIsPlagiarismText();
                    boolean shouldIncludeParent = parentValue != null && (parentValue.equals(state) || parentValue == 2);
                    if (shouldIncludeParent) {
                        result.add(node);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 计算父节点的 isPlagiarism 或 isPlagiarismText 值（基于子节点）
     */
    private void calculateParentPlagiarism(EcmDocTreeDTO parentNode, Integer type) {
        List<EcmDocTreeDTO> children = parentNode.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }

        boolean allOne = true;
        boolean allZero = true;

        for (EcmDocTreeDTO child : children) {
            Integer value = type.equals(4) ? child.getIsPlagiarism() : child.getIsPlagiarismText();
            if (value == null) continue;

            if (value == 1) {
                allZero = false;
            } else if (value == 0) {
                allOne = false;
            } else {
                allOne = false;
                allZero = false;
            }
        }

        if (allOne) {
            if (type.equals(IcmsConstants.FOUR)) {
                parentNode.setIsPlagiarism(IcmsConstants.ONE);
            } else {
                parentNode.setIsPlagiarismText(IcmsConstants.ONE);
            }
        } else if (allZero) {
            if (type.equals(IcmsConstants.FOUR)) {
                parentNode.setIsPlagiarism(IcmsConstants.ZERO);
            } else {
                parentNode.setIsPlagiarismText(IcmsConstants.ZERO);
            }
        } else {
            if (type.equals(IcmsConstants.FOUR)) {
                parentNode.setIsPlagiarism(IcmsConstants.TWO);
            } else {
                parentNode.setIsPlagiarismText(IcmsConstants.TWO);
            }
        }
    }

    /**
     * 判断是否为叶子节点
     */
    private boolean isLeafNode(EcmDocTreeDTO node) {
        return node.getChildren() == null || node.getChildren().isEmpty();
    }

    /**
     * 检查节点是否匹配条件
     */
    private boolean matchesCondition(EcmDocTreeDTO node, Integer state, Integer type) {
        Integer value = type.equals(4) ? node.getIsPlagiarism() : node.getIsPlagiarismText();
        return value != null && value.equals(state);
    }

    /**
     * 从当前节点的子节点递归查找符合条件的最子节点
     */
    private void getFilteredLeafNodesFromChildren(EcmDocDef currentNode,
                                                  List<EcmDocDef> filteredLeafNodes,
                                                  Map<String, List<EcmDocDef>> groupedByParentCode) {
        //获取当前节点的所有子节点
        List<EcmDocDef> childrenNodes = groupedByParentCode.get(currentNode.getDocCode());
        if (childrenNodes != null && !childrenNodes.isEmpty()) {
            for (EcmDocDef child : childrenNodes) {
                if (groupedByParentCode.get(child.getDocCode()) == null) {
                    if (!filteredLeafNodes.contains(child)) {
                        filteredLeafNodes.add(child);
                    }
                } else {
                    // 递归检查子节点的子节点
                    getFilteredLeafNodesFromChildren(child, filteredLeafNodes, groupedByParentCode);
                }
            }
        } else {
            if (!filteredLeafNodes.contains(currentNode)) {
                filteredLeafNodes.add(currentNode);
            }
        }
    }

    /**
     *  获取最子节点集合
     */
    private List<EcmDocDef> getLastNodes(List<EcmDocDef> filteredLeafNodes, Integer state) {
        if (state == null) {
            return filteredLeafNodes;
        }
        return filteredLeafNodes.stream().collect(Collectors.groupingBy(EcmDocDef::getIsPlagiarism))
                .get(state);
    }

    /**
     * 递归提取父节点
     * @param currentNode      当前节点
     * @param treeDTOS         树形节点列表
     * @param groupedByDocCode 按 DocCode 分组的所有节点
     */
    private void extractParent(EcmDocDef currentNode, List<EcmDocTreeDTO> treeDTOS,
                               Map<String, List<EcmDocDef>> groupedByDocCode) {
        String parentCode = currentNode.getParent();
        if (!IcmsConstants.DOC_LEVEL_FIRST.equals(parentCode)) {
            List<EcmDocDef> parentNodes = groupedByDocCode.get(parentCode);
            if (parentNodes != null && !parentNodes.isEmpty()) {
                EcmDocDef parentNode = parentNodes.get(0); // 取第一个父节点
                EcmDocTreeDTO parentDTO = new EcmDocTreeDTO();
                BeanUtils.copyProperties(parentNode, parentDTO);
                // 防止重复添加
                if (!treeDTOS.contains(parentDTO)) {
                    treeDTOS.add(parentDTO);
                }
                // 递归处理父节点的父节点
                extractParent(parentNode, treeDTOS, groupedByDocCode);
            }
        }
    }

    /**
     * 构建树结构
     * @param treeDTOS         所有节点列表
     * @return 树形结构的节点列表
     */
    private List<EcmDocTreeDTO> buildTree(List<EcmDocTreeDTO> treeDTOS,
                                          Map<String, List<EcmDocPlagChe>> relDocCodes,
                                          Map<String, List<EcmDocDef>> groupedByDocCode) {
        Map<String, List<EcmDocTreeDTO>> childrenMap = treeDTOS.stream()
                .collect(Collectors.groupingBy(EcmDocTreeDTO::getParent));
        List<EcmDocTreeDTO> tree = new ArrayList<>();
        for (EcmDocTreeDTO node : treeDTOS) {
            if (IcmsConstants.DOC_LEVEL_FIRST.equals(node.getParent())) {
                String label = "(" + node.getDocCode() + ")" + node.getDocName();
                node.setLabel(label);
                node.setDocName(label);
                tree.add(node);
                addChildren(node, childrenMap, relDocCodes, groupedByDocCode);
            }
        }
        return tree;
    }

    /**
     * 将子节点添加到父节点
     * @param parentNode       父节点
     * @param childrenMap      子节点分组
     */
    private void addChildren(EcmDocTreeDTO parentNode, Map<String, List<EcmDocTreeDTO>> childrenMap,
                             Map<String, List<EcmDocPlagChe>> relDocCodes,
                             Map<String, List<EcmDocDef>> groupedByDocCode) {
        List<EcmDocTreeDTO> children = childrenMap.get(parentNode.getDocCode());
        if (!CollectionUtils.isEmpty(children)) {
            parentNode.setChildren(children);
            for (EcmDocTreeDTO child : children) {
                String label = "(" + child.getDocCode() + ")" + child.getDocName();
                child.setLabel(label);
                child.setDocName(label);
                addChildren(child, childrenMap, relDocCodes, groupedByDocCode);
            }
        } else {
//            Map<String, List<EcmDocDynaPlag>> dynaType = ecmDocDynaPlagMapper.selectList(null)
//                    .stream().collect(Collectors.groupingBy(EcmDocDynaPlag::getDocCode));
            // 当没有子节点时，表示当前节点是最子节点，插入其他数据
            modelInformationService.insertAdditionalData(parentNode, relDocCodes, groupedByDocCode);
        }
    }

    /**
     * 更新动态资料查重
     */
    private void updateDynamicPlagiarism(EcmPlagiarismStateDTO ecmStateVo) {
//        LambdaUpdateWrapper<EcmDocDynaPlag> wrapper = new LambdaUpdateWrapper<>();
//        wrapper.eq(EcmDocDynaPlag::getId, ecmStateVo.getId()).set(EcmDocDynaPlag::getIsPlagiarism,
//                ecmStateVo.getIsPlagiarism());
//        ecmDocDynaPlagMapper.update(null, wrapper);
        Integer queryType = ecmStateVo.getQueryType();
        if (queryType != null) {
            if (IcmsConstants.GLOBAL_PLAG_CHE_QUERY_CURR.equals(queryType)) {
                EcmDocPlagChe ecmDocPlagChe = new EcmDocPlagChe();
                ecmDocPlagChe.setId(snowflakeUtil.nextId());
                ecmDocPlagChe.setDocCode(ecmStateVo.getDocCode());
                ecmDocPlagChe.setRelDocCode(ecmStateVo.getDocCode());
                ecmDocPlagChe.setFrameYear(ecmStateVo.getFrameYear());
                ecmDocPlagChe.setFileSimilarity(ecmStateVo.getFileSimilarity());
//                ecmDocPlagChe.setType(IcmsConstants.DYNAMIC_TREE);
                ecmDocPlagChe.setQueryType(queryType);
                ecmDocPlagCheMapper.insert(ecmDocPlagChe);
            } else if (IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ALL.equals(queryType)) {
                EcmDocPlagChe ecmDocPlagChe = new EcmDocPlagChe();
                ecmDocPlagChe.setId(snowflakeUtil.nextId());
                ecmDocPlagChe.setDocCode(ecmStateVo.getDocCode());
                ecmDocPlagChe.setFrameYear(ecmStateVo.getFrameYear());
                ecmDocPlagChe.setFileSimilarity(ecmStateVo.getFileSimilarity());
//                ecmDocPlagChe.setType(IcmsConstants.DYNAMIC_TREE);
                ecmDocPlagChe.setQueryType(queryType);
                ecmDocPlagCheMapper.insert(ecmDocPlagChe);
            } else {
                if (!CollectionUtils.isEmpty(ecmStateVo.getRelDocCodes())) {
                    ecmStateVo.getRelDocCodes().forEach(docCode -> {
                        EcmDocPlagChe ecmDocPlagChe = new EcmDocPlagChe();
                        ecmDocPlagChe.setId(snowflakeUtil.nextId());
                        ecmDocPlagChe.setDocCode(ecmStateVo.getDocCode());
                        ecmDocPlagChe.setRelDocCode(docCode.getRelDoccode());
                        ecmDocPlagChe.setFrameYear(ecmStateVo.getFrameYear());
                        ecmDocPlagChe.setFileSimilarity(ecmStateVo.getFileSimilarity());
//                        ecmDocPlagChe.setType(IcmsConstants.DYNAMIC_TREE);
                        ecmDocPlagChe.setQueryType(queryType);
//                        ecmDocPlagChe.setRelType(docCode.getRelType());
                        //todo 改批量插入
                        ecmDocPlagCheMapper.insert(ecmDocPlagChe);
                    });
                }

            }
        }

    }

    /**
     * 新增动态资料查重
     */
    private void insertDynamicPlagiarism(List<EcmPlagiarismStateDTO> vo) {
        vo.forEach(ecmStateVo -> {
            Integer queryType = ecmStateVo.getQueryType();
            //            AssertUtils.isNull(queryType, "资料节点类型不能为空");
//            EcmDocDynaPlag ecmDocDynaPlag = new EcmDocDynaPlag();
//            ecmDocDynaPlag.setId(snowflakeUtil.nextId());
//            ecmDocDynaPlag.setDocName(ecmStateVo.getDocName());
//            ecmDocDynaPlag.setDocCode(ecmStateVo.getDocCode());
//            ecmDocDynaPlag.setIsPlagiarism(ecmStateVo.getIsPlagiarism());
            //todo 改批量插入
//            ecmDocDynaPlagMapper.insert(ecmDocDynaPlag);
            if (ecmStateVo.getQueryType() != null) {
                if (IcmsConstants.GLOBAL_PLAG_CHE_QUERY_CURR.equals(queryType)) {
                    EcmDocPlagChe ecmDocPlagChe = new EcmDocPlagChe();
                    ecmDocPlagChe.setId(snowflakeUtil.nextId());
                    ecmDocPlagChe.setDocCode(ecmStateVo.getDocCode());
                    ecmDocPlagChe.setRelDocCode(ecmStateVo.getDocCode());
                    ecmDocPlagChe.setFrameYear(ecmStateVo.getFrameYear());
                    ecmDocPlagChe.setFileSimilarity(ecmStateVo.getFileSimilarity());
//                    ecmDocPlagChe.setType(IcmsConstants.DYNAMIC_TREE);
                    ecmDocPlagChe.setQueryType(queryType);
                    ecmDocPlagCheMapper.insert(ecmDocPlagChe);
                } else if (IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ALL.equals(queryType)) {
                    EcmDocPlagChe ecmDocPlagChe = new EcmDocPlagChe();
                    ecmDocPlagChe.setId(snowflakeUtil.nextId());
                    ecmDocPlagChe.setDocCode(ecmStateVo.getDocCode());
                    ecmDocPlagChe.setFrameYear(ecmStateVo.getFrameYear());
                    ecmDocPlagChe.setFileSimilarity(ecmStateVo.getFileSimilarity());
//                    ecmDocPlagChe.setType(IcmsConstants.DYNAMIC_TREE);
                    ecmDocPlagChe.setQueryType(queryType);
                    ecmDocPlagCheMapper.insert(ecmDocPlagChe);
                } else if (IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ADD.equals(queryType)) {
                    if (!CollectionUtils.isEmpty(ecmStateVo.getRelDocCodes())) {
                        ecmStateVo.getRelDocCodes().forEach(docCode -> {
                            EcmDocPlagChe ecmDocPlagChe = new EcmDocPlagChe();
                            ecmDocPlagChe.setId(snowflakeUtil.nextId());
                            ecmDocPlagChe.setDocCode(ecmStateVo.getDocCode());
                            ecmDocPlagChe.setRelDocCode(docCode.getRelDoccode());
                            ecmDocPlagChe.setFrameYear(ecmStateVo.getFrameYear());
                            ecmDocPlagChe.setFileSimilarity(ecmStateVo.getFileSimilarity());
//                            ecmDocPlagChe.setType(IcmsConstants.DYNAMIC_TREE);
                            ecmDocPlagChe.setQueryType(queryType);
//                            ecmDocPlagChe.setRelType(docCode.getRelType());
                            //todo 改批量插入
                            ecmDocPlagCheMapper.insert(ecmDocPlagChe);
                        });
                    }

                }
            }

        });
    }

    /**
     * 更新全局配置
     */
    public Result updateGlobalplagiarism(EcmDocTreeDTO dto) {

        EcmDocPlagChe ecmDocPlagChe1 = ecmDocPlagCheMapper
                .selectOne(new LambdaQueryWrapper<EcmDocPlagChe>()
//                        .eq(EcmDocPlagChe::getType, IcmsConstants.GLOBAL_TREE)
                        .isNull(EcmDocPlagChe::getDocCode).isNull(EcmDocPlagChe::getRelDocCode));
        if (ecmDocPlagChe1 != null) {
            //更新
            ecmDocPlagCheMapper.update(new LambdaUpdateWrapper<EcmDocPlagChe>()
                    .set(dto.getFileSimilarity() != null, EcmDocPlagChe::getFileSimilarity,
                            dto.getFileSimilarity())
                    .set(dto.getQueryType() != null, EcmDocPlagChe::getQueryType,
                            dto.getQueryType())
                    .set(dto.getFrameYear() != null, EcmDocPlagChe::getFrameYear,
                            dto.getFrameYear())
                    .eq(EcmDocPlagChe::getId, ecmDocPlagChe1.getId()));
            if (IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ADD.equals(dto.getQueryType())) {
                //线衫后插
                ecmDocPlagCheMapper.delete(new LambdaQueryWrapper<EcmDocPlagChe>()
                        .eq(EcmDocPlagChe::getQueryType, IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ADD)
//                        .eq(EcmDocPlagChe::getType, IcmsConstants.GLOBAL_TREE)
                        .isNotNull(EcmDocPlagChe::getRelDocCode).isNull(EcmDocPlagChe::getDocCode));
            }
        } else {
            //新增
            EcmDocPlagChe ecmDocPlagChe = new EcmDocPlagChe();
//            ecmDocPlagChe.setType(IcmsConstants.GLOBAL_TREE);
            ecmDocPlagChe.setQueryType(dto.getQueryType());
            ecmDocPlagChe.setFrameYear(dto.getFrameYear());
            ecmDocPlagChe.setFileSimilarity(dto.getFileSimilarity());
            ecmDocPlagChe.setId(snowflakeUtil.nextId());
            ecmDocPlagCheMapper.insert(ecmDocPlagChe);
        }

        if (IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ADD.equals(dto.getQueryType())
                && !CollectionUtils.isEmpty(dto.getRelDocCodes())) {
            dto.getRelDocCodes().forEach(docCode -> {
                EcmDocPlagChe ecmDocPlagChe = new EcmDocPlagChe();
                ecmDocPlagChe.setRelDocCode(docCode.getRelDoccode());
//                ecmDocPlagChe.setType(IcmsConstants.GLOBAL_TREE);
                ecmDocPlagChe.setQueryType(IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ADD);
                ecmDocPlagChe.setFrameYear(dto.getFrameYear());
                ecmDocPlagChe.setFileSimilarity(dto.getFileSimilarity());
                ecmDocPlagChe.setId(snowflakeUtil.nextId());
//                ecmDocPlagChe.setRelType(docCode.getRelType());
                //todo 改批量插入
                ecmDocPlagCheMapper.insert(ecmDocPlagChe);
            });
        }

        return Result.success();
    }

    /**
     * 查询全局配置
     */
    public Result<EcmDocTreeDTO> searGlobalplagiarism() {
        //全局配置只有一条
        List<EcmDocPlagChe> ecmDocPlagChes = ecmDocPlagCheMapper
                .selectList(new LambdaQueryWrapper<EcmDocPlagChe>()
//                        .eq(EcmDocPlagChe::getType, IcmsConstants.GLOBAL_TREE)
                        .isNull(EcmDocPlagChe::getDocCode).isNull(EcmDocPlagChe::getRelDocCode));
        EcmDocPlagChe ecmDocPlagChe1 = null;
        if (!CollectionUtils.isEmpty(ecmDocPlagChes)) {
            ecmDocPlagChe1 = ecmDocPlagChes.get(0);
        }
        List<EcmDocPlagChe> ecmDocPlagChes1 = ecmDocPlagCheMapper
                .selectList(new LambdaQueryWrapper<EcmDocPlagChe>()
//                        .eq(EcmDocPlagChe::getType, IcmsConstants.GLOBAL_TREE)
                        .isNotNull(EcmDocPlagChe::getRelDocCode)
                        .eq(EcmDocPlagChe::getQueryType, IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ADD));
        return getEcmDocTreeDTOResult(ecmDocPlagChe1, ecmDocPlagChes1);
    }

    /**
     * 查询查重配置
     */
    private Result<EcmDocTreeDTO> getEcmDocTreeDTOResult(EcmDocPlagChe ecmDocPlagChe1, List<EcmDocPlagChe> ecmDocPlagChes1) {
        EcmDocTreeDTO map = new EcmDocTreeDTO();
        if (ecmDocPlagChe1 != null) {
            map.setFileSimilarity(ecmDocPlagChe1.getFileSimilarity());
            map.setQueryType(ecmDocPlagChe1.getQueryType());
            map.setFrameYear(ecmDocPlagChe1.getFrameYear());
        }

        if (!CollectionUtils.isEmpty(ecmDocPlagChes1)) {
            //指定节点配置
            List<EcmDocPlaRelNameDTO> relNames = new ArrayList<>();
            ecmDocPlagChes1.forEach(ecmDocPlagChe -> {
                EcmDocPlaRelNameDTO dto = new EcmDocPlaRelNameDTO();
//                dto.setRelType(ecmDocPlagChe.getRelType());
                dto.setRelDoccode(ecmDocPlagChe.getRelDocCode());
                EcmDocDef ecmDocDef = ecmDocDefMapper.selectOne(new LambdaQueryWrapper<EcmDocDef>()
                        .eq(EcmDocDef::getDocCode, ecmDocPlagChe.getRelDocCode()));
                dto.setRelDocName(ecmDocDef.getDocName());
                relNames.add(dto);
            });
            map.setRelDocCodes(relNames);
        } else {
            map.setRelDocCodes(new ArrayList<>());
        }

        List<EcmDocTreeDTO> ecmDocTreeDTOS = searchAllDoc();
        map.setAll(ecmDocTreeDTOS);
        //回显只需展示
        return Result.success(map);
    }

    /**
     * 更新静态资料查重配置
     */
    public Result<EcmDocTreeDTO> queryPlagiarismState(String docCode, String type) {
        AssertUtils.isNull(docCode, "docCode不能为空");

//        if (StringUtils.isEmpty(type)) {
//            return Result.success();
//        }
        List<EcmDocPlagChe> ecmDocPlagChes = ecmDocPlagCheMapper
                .selectList(new LambdaQueryWrapper<EcmDocPlagChe>()
                        .eq(EcmDocPlagChe::getDocCode, docCode));
        if (CollectionUtils.isEmpty(ecmDocPlagChes)) {
            return Result.success();
        }
        EcmDocPlagChe ecmDocPlagChe1 = ecmDocPlagChes.get(0);
        List<EcmDocPlagChe> ecmDocPlagChes1 = ecmDocPlagCheMapper.selectList(
                new LambdaQueryWrapper<EcmDocPlagChe>().eq(EcmDocPlagChe::getDocCode, docCode)
//                        .eq(EcmDocPlagChe::getType, type).isNotNull(EcmDocPlagChe::getRelDocCode)
                        .eq(EcmDocPlagChe::getQueryType, IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ADD));
        return getEcmDocTreeDTOResult(ecmDocPlagChe1, ecmDocPlagChes1);
    }
}
