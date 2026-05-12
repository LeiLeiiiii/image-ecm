package com.sunyard.ecm.service;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.redis.constant.TimeOutConstants;
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
 * @desc 转正配置服务接口
 */
@Service
public class SysObverseService {

    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    /**
     *查询转正资料类型树
     */
    public Result<List<EcmDocTreeDTO>> searchRegularizeTypeTree(int state) {
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectChildren(null,state,null,null,null);
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
     * 更新文档转正配置状态
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRegularizedState(List<EcmDocDef> vo) {
        if (!CollectionUtils.isEmpty(vo)){
            List<String> docCodes = vo.stream()
                    .map(EcmDocDef::getDocCode)
                    .collect(Collectors.toList());
            // 按照 docCode 分组
            Map<String, List<EcmDocDef>> groupedByDocCode = getGroupedByDocCode(docCodes);
            vo.forEach(ecmStateVo -> {
                LambdaUpdateWrapper<EcmDocDef> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(EcmDocDef::getDocCode,ecmStateVo.getDocCode())
                        .set(EcmDocDef::getIsRegularized, ecmStateVo.getIsRegularized());
                ecmDocDefMapper.update(null, wrapper);
                BusiCacheService busiCacheService = SpringUtil.getBean(BusiCacheService.class);
                EcmDocDef docDef=groupedByDocCode.get(ecmStateVo.getDocCode()).get(0);
                docDef.setIsRegularized(ecmStateVo.getIsRegularized());
                busiCacheService.setDocInfo(docDef, TimeOutConstants.SEVEN_DAY);
            });
        }
        return true;
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

    /**
     * 根据docCode进行分组
     */
    private  Map<String, List<EcmDocDef>> getGroupedByDocCode(List<String> docCodes){
        LambdaQueryWrapper<EcmDocDef> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(EcmDocDef::getDocCode,docCodes);
        List<EcmDocDef> ecmDocDefs=ecmDocDefMapper.selectList(queryWrapper);
        // 按照 docCode 分组
        return ecmDocDefs.stream()
                .collect(Collectors.groupingBy(EcmDocDef::getDocCode));
    }
}
