package com.sunyard.ecm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.ecm.constant.DocRightConstants;
import com.sunyard.ecm.dto.ecm.EcmDocrightDefDTO;
import com.sunyard.ecm.mapper.EcmAppDocrightMapper;
import com.sunyard.ecm.mapper.EcmDocrightDefMapper;
import com.sunyard.ecm.po.EcmAppDocright;
import com.sunyard.ecm.po.EcmDocrightDef;
import com.sunyard.ecm.vo.EcmAppDefAttrVO;
import com.sunyard.ecm.vo.SysRoleVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ty
 * @since 2023-4-23 10:37
 * @desc 系统角色实现类
 */
@Service
public class SysRoleService {
    @Resource
    private EcmDocrightDefMapper ecmDocrightDefMapper;
    @Resource
    private EcmAppDocrightMapper ecmAppDocrightMapper;


    /**
     * 获取当前角色关联用户列表
     */
    public Result getDocRightList(SysRoleVO sysRoleVo) {
        //校验参数
        checkParam(sysRoleVo);
        List<EcmDocrightDef> docrightDefList = ecmDocrightDefMapper.selectList(new LambdaQueryWrapper<EcmDocrightDef>()
                .eq(EcmDocrightDef::getDimType, DocRightConstants.ROLE_DIM)
                .eq(EcmDocrightDef::getRoleDimVal, sysRoleVo.getRoleId().toString())
                .eq(EcmDocrightDef::getAppCode, sysRoleVo.getAppCode())
                .eq(EcmDocrightDef::getRightVer, sysRoleVo.getRightVer()));
        if (CollectionUtils.isEmpty(docrightDefList)) {
            return Result.success(Collections.emptyList());
        }
        List<EcmDocrightDefDTO> docrightDefExtendList = PageCopyListUtils.copyListProperties(docrightDefList, EcmDocrightDefDTO.class);
//        ecmDocRightService.addDocFileTypeList(docrightDefExtendList);
        return Result.success(docrightDefExtendList);
    }

    /**
     * 获取业务类型的权限版本号列表
     */
    public Result getAppRightVerList(String appCode) {
        AssertUtils.isNull(appCode, "参数错误");
        List<EcmAppDocright> appDocrights = ecmAppDocrightMapper.selectList(new LambdaQueryWrapper<EcmAppDocright>()
                .eq(EcmAppDocright::getAppCode, appCode));
        if (CollectionUtils.isEmpty(appDocrights)) {
            return Result.success(Collections.emptyList());
        }
        List<Integer> rightVerList = appDocrights.stream().map(EcmAppDocright::getRightVer).collect(Collectors.toList());
        return Result.success(rightVerList);
    }

    /**
     * 根据节点 id 集合过滤树形结构
     *
     * @param nodeList 节点列表
     * @param idList   需要保留的节点 id 集合
     * @return
     */
    private List<EcmAppDefAttrVO> filterTree(List<EcmAppDefAttrVO> nodeList, List<String> idList) {
        List<EcmAppDefAttrVO> filteredList = new ArrayList<>();
        for (EcmAppDefAttrVO node : nodeList) {
            // 如果节点 id 在需要保留的节点 id 集合中，则将该节点及其子节点加入新的节点列表
            if (idList.contains(node.getAppCode())) {
                if (!CollectionUtils.isEmpty(node.getChildren())) {
                    node.setChildren(filterTree(node.getChildren(), idList));
                }
                filteredList.add(node);
            } else {
                // 如果节点 id 不在需要保留的节点 id 集合中，则将该节点及其子节点移除
                if (!CollectionUtils.isEmpty(node.getChildren())) {
                    node.setChildren(filterTree(node.getChildren(), idList));
                    if (!CollectionUtils.isEmpty(node.getChildren())) {
                        filteredList.add(node);
                    }
                }
            }
        }
        return filteredList;
    }

    private void checkParam(SysRoleVO sysRoleVo) {
        AssertUtils.isNull(sysRoleVo.getRoleId(), "参数错误");
        AssertUtils.isNull(sysRoleVo.getAppCode(), "业务类型不能为空");
        AssertUtils.isNull(sysRoleVo.getRightVer(), "版本不能为空");
    }
}
