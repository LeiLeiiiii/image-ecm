package com.sunyard.module.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.module.system.dto.SysPostListDTO;
import com.sunyard.module.system.mapper.SysDeptMapper;
import com.sunyard.module.system.mapper.SysInstMapper;
import com.sunyard.module.system.mapper.SysPostMapper;
import com.sunyard.module.system.mapper.SysPostUserMapper;
import com.sunyard.module.system.po.SysDept;
import com.sunyard.module.system.po.SysInst;
import com.sunyard.module.system.po.SysPost;
import com.sunyard.module.system.po.SysPostUser;
import com.sunyard.module.system.vo.SysPostVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 系统管理-岗位管理
 *
 * @Author wangmeiling 2025/9/5
 */
@Service
public class SysPostService {

    @Resource
    private SysPostMapper sysPostMapper;
    @Resource
    private SysPostUserMapper sysPostUserMapper;
    @Resource
    private SysInstMapper sysInstMapper;
    @Resource
    private SysDeptMapper sysDeptMapper;

    /**
     * 查询岗位
     *
     * @param sysPostListDTO
     * @return
     */
    public PageInfo<SysPost> search(SysPostListDTO sysPostListDTO) {
        // 校验
        Assert.notNull(sysPostListDTO.getInstId(),"参数不能为空");
        PageHelper.startPage(sysPostListDTO.getPageNum(), sysPostListDTO.getPageSize());
        List<SysPost> list = sysPostMapper.selectList(
                new LambdaQueryWrapper<SysPost>().likeRight(StringUtils.hasText(sysPostListDTO.getName()), SysPost::getName, sysPostListDTO.getName())
                        .eq(StringUtils.hasText(String.valueOf(sysPostListDTO.getInstId())), SysPost::getInstId, sysPostListDTO.getInstId()));
        return new PageInfo<>(list);
    }

    /**
     * 新增岗位
     *
     * @param sysPostVO
     * @param userId
     */
    public void add(SysPostVO sysPostVO, Long userId) {
        // 参数校验
        Assert.hasLength(sysPostVO.getName(),"参数不能为空");
        Assert.notNull(sysPostVO.getInstId(),"参数不能为空");
        Assert.hasLength(sysPostVO.getPostCode(),"参数不能为空");

        // 校验重复
        Long count = sysPostMapper.selectCount(new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getName, sysPostVO.getName())
                .eq(SysPost::getInstId, sysPostVO.getInstId()));

        Assert.isTrue(count < 1, "岗位创建失败,该岗位名字已存在");
        // 岗位新增
        SysPost sysPost = new SysPost();
        BeanUtils.copyProperties(sysPostVO, sysPost);
        sysPost.setPostId(null);
        sysPost.setCreateUser(userId.toString());
        sysPost.setUpdateUser(userId.toString());
        sysPostMapper.insert(sysPost);
    }

    /**
     * 修改岗位
     *
     * @param sysPostVO
     * @param userId
     */
    public void edit(SysPostVO sysPostVO, Long userId) {
        // 参数校验
        Assert.notNull(sysPostVO.getPostId(),"参数不能为空");
        Assert.notNull(sysPostVO.getInstId(),"参数不能为空");
        Assert.hasLength(sysPostVO.getName(),"参数不能为空");
        Assert.hasLength(sysPostVO.getPostCode(),"参数不能为空");

        // 名字重复
        Long count = sysPostMapper.selectCount(new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getName, sysPostVO.getName())
                .eq(SysPost::getInstId, sysPostVO.getInstId())
                .ne(SysPost::getPostId, sysPostVO.getPostId()));
        Assert.isTrue(count < 1, "岗位修改失败,该岗位名字已存在");

        // 岗位修改
        SysPost sysPost = new SysPost();
        BeanUtils.copyProperties(sysPostVO, sysPost);
        sysPost.setUpdateUser(userId.toString());
        sysPostMapper.updateById(sysPost);
    }

    /**
     * 删除岗位信息
     *
     * @param postId
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long postId) {
        Assert.notNull(postId, "参数不能为空");
        sysPostMapper.deleteById(postId);
        sysPostUserMapper.delete(new LambdaQueryWrapper<SysPostUser>().eq(SysPostUser::getPostId, postId));
    }

    /**
     * 查询所有岗位
     *
     *
     * @param instId
     * @param deptId
     * @return
     */
    public List<SysPost> searchAll(Long instId, Long deptId) {
        // 判断机构和部门
        if (null == deptId) {
            SysInst sysInst = sysInstMapper
                    .selectOne(new LambdaQueryWrapper<SysInst>()
                            .eq(SysInst::getInstId, instId)
                            .eq(SysInst::getNewlevel, 0));
            Assert.notNull(sysInst, "请选择正确的机构");
        } else {
            List<SysDept> sysDept = sysDeptMapper.selectList(
                    new LambdaQueryWrapper<SysDept>()
                            .eq(SysDept::getDeptId, deptId)
                            .orderByDesc(SysDept::getNewlevel));
            Assert.notNull(sysDept, "请选择正确的部门");
            instId = sysDept.get(0).getParentId();
        }
        return sysPostMapper.selectList(new LambdaQueryWrapper<SysPost>().eq(SysPost::getInstId, instId));
    }

    /**
     * 查询单个岗位详情
     *
     * @param postId
     * @return
     */
    public SysPost select(Long postId) {
        Assert.notNull(postId, "参数不能为空");
        return sysPostMapper.selectById(postId);
    }
}
