package com.sunyard.ecm.service;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.redis.constant.TimeOutConstants;
import com.sunyard.module.system.api.DictionaryApi;
import com.sunyard.module.system.api.dto.SysDictionaryDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liwei
 * @since 2025/5/19
 * @desc 自动分类检测服务接口
 */
@Service
public class SysClassificationService {

    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private DictionaryApi dictionaryApi;

    /**
     * 查询资料类型树的自动分类状态
     * @param state   自动分类状态（0关闭，1开启）
     * @return 资料类型树
     */
    public Result<List<EcmDocTreeDTO>> searchAutoClassificationTypeTree(int state) {
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectChildren(null,null,null,null,state);
        List<EcmDocDef> allEcmDocDefs = ecmDocDefMapper.selectList(null);
        Map<String, List<EcmDocDef>> groupedByDocCode = allEcmDocDefs.stream()
                .collect(Collectors.groupingBy(EcmDocDef::getDocCode));
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
            List<EcmDocTreeDTO> tree = buildTree(ecmDocTreeDTOS);
            return Result.success(tree);
        }
        return Result.success(ecmDocTreeDTOS);
    }

    /**
     * 更新资料的自动分类状态
     * @param vo 需要更新的资料定义列表
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAutoClassificationState(List<EcmDocDef> vo) {
        if (!CollectionUtils.isEmpty(vo)){
            vo.forEach(ecmStateVo -> {
                LambdaUpdateWrapper<EcmDocDef> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(EcmDocDef::getDocCode,ecmStateVo.getDocCode())
                        .set(EcmDocDef::getIsAutoClassified,ecmStateVo.getIsAutoClassified());
                ecmDocDefMapper.update(null, wrapper);
                //修改缓存数据
                BusiCacheService busiCacheService = SpringUtil.getBean(BusiCacheService.class);
                EcmDocDef ecmDocDef= busiCacheService.getIntelligentProcessingEcmDocDef(ecmStateVo.getDocCode());
                ecmDocDef.setIsAutoClassified(ecmStateVo.getIsAutoClassified());
                busiCacheService.setDocInfo(ecmDocDef, TimeOutConstants.SEVEN_DAY);
            });
        }
        return true;
    }

    /**
     * 配置资料的自动分类标识
     * @param docCode 资料代码
     * @param autoClassificationId 自动分类标识
     * @return 更新后的资料对象
     */
    public Result<EcmDocDef> configureAutoClassificationSign(String docCode, String autoClassificationId) {

        AssertUtils.isNull(docCode, "资料代码为空");

        ecmDocDefMapper.update(null,new LambdaUpdateWrapper<EcmDocDef>()
                .eq(EcmDocDef::getDocCode,docCode)
                .set(EcmDocDef::getAutoClassificationId,autoClassificationId));
        //修改缓存数据
        BusiCacheService busiCacheService = SpringUtil.getBean(BusiCacheService.class);
        EcmDocDef ecmDocDef= busiCacheService.getIntelligentProcessingEcmDocDef(docCode);
        ecmDocDef.setAutoClassificationId(autoClassificationId);
        busiCacheService.setDocInfo(ecmDocDef,TimeOutConstants.SEVEN_DAY);
        return Result.success();
    }

    /**
     * 查询自动分类标识字典值集合
     */
    public Result<List<SysDictionaryDTO>> getAutoClassificationSigns() {
        // 查询字典数据
        Result<List<SysDictionaryDTO>> result = dictionaryApi.selectValueByParentKey(IcmsConstants.DICTIONARY_CODE, null);

        AssertUtils.isNull(result,"字典名不能为空");

        return Result.success(result.getData());
    }


    /**
     * 开关全部开或关
     * @param types
     * @param state
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Result updateAllCmdState(List<Integer> types, Integer state) {
        if (CollectionUtils.isEmpty(types)) {
            return Result.error("状态类型列表不能为空", 400);
        }

        LambdaUpdateWrapper<EcmDocDef> wrapper = new LambdaUpdateWrapper<>();

        // 遍历类型列表，批量设置需要更新的字段
        for (Integer type : types) {
            if (IcmsConstants.REGULARIZE.equals(type)) {
                wrapper.set(EcmDocDef::getIsRegularized, state);
            } else if (IcmsConstants.OBSCURE.equals(type)) {
                wrapper.set(EcmDocDef::getIsObscured, state);
            } else if (IcmsConstants.REFLECTIVE.equals(type)) {
                wrapper.set(EcmDocDef::getIsReflective, state);
            } else if (IcmsConstants.MISS_CORNER.equals(type)) {
                wrapper.set(EcmDocDef::getIsCornerMissing, state);
            } else if (IcmsConstants.REMAKE.equals(type)) {
                wrapper.set(EcmDocDef::getIsRemade, state);
            } else if (IcmsConstants.PLAGIARISM.equals(type)) {
                wrapper.set(EcmDocDef::getIsPlagiarism, state);
            } else if (IcmsConstants.AUTOMATIC_CLASSIFICATION.equals(type)) {
                wrapper.set(EcmDocDef::getIsAutoClassified, state);
            } else {
                return Result.error("存在不存在的配置类型：" + type, 500);
            }
        }

        // 执行批量更新
        ecmDocDefMapper.update(null, wrapper);

        // 更新缓存
        BusiCacheService busiCacheService = SpringUtil.getBean(BusiCacheService.class);
        busiCacheService.setDocInfoAll();

        return Result.success();
    }

    /**
     * 递归提取父节点
     * @param currentNode 当前节点
     * @param treeDTOS 树形节点列表
     * @param groupedByDocCode 按 DocCode 分组的所有节点
     */
    private void extractParent(EcmDocDef currentNode, List<EcmDocTreeDTO> treeDTOS, Map<String, List<EcmDocDef>> groupedByDocCode) {
        String parentCode = currentNode.getParent();
        if (!"0".equals(parentCode)) {
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
     * @param treeDTOS 所有节点列表
     * @return 树形结构的节点列表
     */
    private List<EcmDocTreeDTO> buildTree(List<EcmDocTreeDTO> treeDTOS) {
        Map<String, List<EcmDocTreeDTO>> childrenMap = treeDTOS.stream()
                .collect(Collectors.groupingBy(EcmDocTreeDTO::getParent));
        List<EcmDocTreeDTO> tree = new ArrayList<>();
        for (EcmDocTreeDTO node : treeDTOS) {
            if (IcmsConstants.DOC_LEVEL_FIRST.equals(node.getParent())) {
                String label = "(" + node.getDocCode() + ")" + node.getDocName();
                node.setLabel(label);
                node.setDocName(label);
                tree.add(node);
                addChildren(node, childrenMap);
            }
        }
        return tree;
    }

    /**
     * 将子节点添加到父节点
     * @param parentNode 父节点
     * @param childrenMap 子节点分组
     */
    private void addChildren(EcmDocTreeDTO parentNode, Map<String, List<EcmDocTreeDTO>> childrenMap) {
        List<EcmDocTreeDTO> children = childrenMap.get(parentNode.getDocCode());
        if (children != null) {
            parentNode.setChildren(children);
            for (EcmDocTreeDTO child : children) {
                String label = "(" + child.getDocCode() + ")" + child.getDocName();
                child.setLabel(label);
                child.setDocName(label);
                addChildren(child, childrenMap);
            }
        }
    }
}
