package com.sunyard.ecm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.SysLabelTreeDTO;
import com.sunyard.ecm.mapper.SysLabelMapper;
import com.sunyard.ecm.po.EcmSysLabel;
import com.sunyard.ecm.vo.SysLabelVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//todo 待补充
@Slf4j
@Service
public class SysLabelService {
    @Resource
    private SnowflakeUtils snowflakeUtils;
    @Resource
    private SysLabelMapper sysLabelMapper;

    /**
     * 获取标签树结构
     */
    public List<SysLabelTreeDTO> getLabelStructureTree() {

        LambdaQueryWrapper<EcmSysLabel> queryWrapper = new LambdaQueryWrapper<>();
        List<EcmSysLabel> ecmSysLabels = sysLabelMapper.selectList(queryWrapper);

        return buildLabelTree(ecmSysLabels);
    }

    /**
     * 获取标签
     */
    private List<SysLabelTreeDTO> buildLabelTree(List<EcmSysLabel> ecmSysLabels) {
        List<SysLabelTreeDTO> labelTree = new ArrayList<>();

        // 遍历标签，构建树结构
        for (EcmSysLabel label : ecmSysLabels) {
            if (label.getParentId() == null) {
                // 根标签
                SysLabelTreeDTO parentNode = new SysLabelTreeDTO();
                parentNode.setLabelId(label.getLabelId());
                parentNode.setLabelName(label.getLabelName());

                // 查找该标签的子标签
                List<SysLabelTreeDTO> children = buildChildren(label.getLabelId(), ecmSysLabels);
                parentNode.setChildren(children);

                labelTree.add(parentNode);
            }
        }

        return labelTree;
    }

    /**
     *
     * 递归获取子标签
     */
    private List<SysLabelTreeDTO> buildChildren(Long parentId, List<EcmSysLabel> ecmSysLabels) {
        List<SysLabelTreeDTO> children = new ArrayList<>();

        for (EcmSysLabel label : ecmSysLabels) {
            if (parentId.equals(label.getParentId())) {
                SysLabelTreeDTO child = new SysLabelTreeDTO();
                child.setLabelId(label.getLabelId());
                child.setLabelName(label.getLabelName());
                child.setParentId(label.getParentId());

                // 递归获取子标签
                List<SysLabelTreeDTO> subChildren = buildChildren(label.getLabelId(), ecmSysLabels);
                child.setChildren(subChildren);

                children.add(child);
            }
        }

        return children;
    }

    /**
     * 获取标签详情
     * @param labelId 标签id
     * @return 标签详情
     */
    public Result getLabelDetails(Long labelId) {
        // 根据标签id查询标签详情
        EcmSysLabel ecmSysLabel = sysLabelMapper.selectById(labelId);
        return Result.success(ecmSysLabel);
    }

    /**
     * 新增标签
     * @param sysLabelVO 标签数据
     * @param token
     * @return 操作结果
     */
    public Result addLabel(SysLabelVO sysLabelVO, AccountTokenExtendDTO token) {

        AssertUtils.isNull(sysLabelVO.getLabelName(), "标签名称不能为空");

        Long count = sysLabelMapper.selectCount(new LambdaQueryWrapper<EcmSysLabel>()
                .eq(EcmSysLabel::getLabelName, sysLabelVO.getLabelName())
                .and(wrapper -> wrapper
                        .eq(EcmSysLabel::getParentId, IcmsConstants.ZERO)
                        .or().isNull(EcmSysLabel::getParentId)
                        .or().eq(EcmSysLabel::getParentId, sysLabelVO.getParentId())
                ));

        AssertUtils.isTrue(count != IcmsConstants.LONG_ZERO, "同一父级下标签名称不能重复");


        // 使用雪花ID生成新的 labelId
        Long labelId = snowflakeUtils.nextId();

        EcmSysLabel ecmSysLabel = new EcmSysLabel();

        // 使用生成的雪花ID作为 labelId
        ecmSysLabel.setLabelId(labelId);
        ecmSysLabel.setLabelName(sysLabelVO.getLabelName());
        ecmSysLabel.setLastLevel(sysLabelVO.getLastLevel());
        ecmSysLabel.setParentId(sysLabelVO.getParentId());
        ecmSysLabel.setCreateUser(token.getUsername());
        ecmSysLabel.setCreateTime(new Date());

        // 插入操作
        sysLabelMapper.insert(ecmSysLabel);

        return Result.success("操作成功");
    }

    /**
     * 编辑标签
     * @param sysLabelVO 标签数据
     * @return 操作结果
     */
    public Result editLabel(SysLabelVO sysLabelVO, AccountTokenExtendDTO token) {

        AssertUtils.isNull(sysLabelVO.getLabelId(), "标签id为空");

        Long count = sysLabelMapper.selectCount(new LambdaQueryWrapper<EcmSysLabel>()
                .eq(EcmSysLabel::getLabelName, sysLabelVO.getLabelName())
                .and(wrapper -> wrapper
                        .eq(EcmSysLabel::getParentId, IcmsConstants.ZERO)
                        .or().isNull(EcmSysLabel::getParentId)
                        .or().eq(EcmSysLabel::getParentId, sysLabelVO.getParentId())
                )
                .ne(EcmSysLabel::getLabelId, sysLabelVO.getLabelId())
        );

        AssertUtils.isTrue(count != IcmsConstants.LONG_ZERO, "同一父级下标签名称不能重复");


        LambdaUpdateWrapper<EcmSysLabel> updateWrapper = new LambdaUpdateWrapper<>();
        // 根据标签ID查找标签
        updateWrapper.eq(EcmSysLabel::getLabelId, sysLabelVO.getLabelId());

        // 更新标签的各个字段
        updateWrapper.set(EcmSysLabel::getLabelName, sysLabelVO.getLabelName());
        updateWrapper.set(EcmSysLabel::getParentId, sysLabelVO.getParentId());
        updateWrapper.set(EcmSysLabel::getLastLevel, sysLabelVO.getLastLevel());
        updateWrapper.set(EcmSysLabel::getUpdateUser, token.getUsername());
        updateWrapper.set(EcmSysLabel::getUpdateTime, new Date());

        sysLabelMapper.update(updateWrapper);

        return Result.success("操作成功");
    }

    /**
     * 删除标签
     * @param labelId 标签id
     * @return 操作结果
     */
    public Result deleteLabel(EcmSysLabel labelId) {

        AssertUtils.isNull(labelId, "标签id为空");

        sysLabelMapper.deleteById(labelId);

        return Result.success("操作成功");
    }
}

