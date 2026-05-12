package com.sunyard.module.system.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.encryption.AppUtils;
import com.sunyard.framework.common.util.encryption.RsaUtils;
import com.sunyard.framework.common.util.encryption.Sm2Util;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.module.auth.api.OpenAuthApi;
import com.sunyard.module.system.constant.CachePrefixConstants;
import com.sunyard.module.system.constant.StateConstants;
import com.sunyard.module.system.dto.SysApiAuthDTO;
import com.sunyard.module.system.mapper.SysApiAuthMapper;
import com.sunyard.module.system.mapper.SysApiMapper;
import com.sunyard.module.system.mapper.SysApiSystemMapper;
import com.sunyard.module.system.po.SysApi;
import com.sunyard.module.system.po.SysApiAuth;
import com.sunyard.module.system.po.SysApiSystem;
import com.sunyard.module.system.vo.SysApiAuthVO;
import com.sunyard.module.system.vo.SysApiVO;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author P-JWei
 * @date 2023/7/31 14:01:15
 * @title
 * @description
 */
@Slf4j
@Service
public class SysConfigAuthService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private SysApiMapper sysApiMapper;
    @Resource
    private SysApiAuthMapper sysApiAuthMapper;
    @Resource
    private SysApiSystemMapper sysApiSystemMapper;
    @Resource
    private OpenAuthApi openAuthApi;

    /**
     * 查询
     * @param vo 查询obj
     * @param pageForm 分页参数
     * @return Result
     */
    public Result search(SysApiAuthVO vo, PageForm pageForm) {
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<SysApiSystem> list = sysApiSystemMapper.selectList(
                new LambdaQueryWrapper<SysApiSystem>().like(StringUtils.hasText(vo.getAppId()),
                        SysApiSystem::getAppId, vo.getAppId())
                        .like(StringUtils.hasText(vo.getSystemName()), SysApiSystem::getSystemName,
                                vo.getSystemName()));
        return Result.success(getSysApiAuthDTOListPageInfo(new PageInfo<>(list)));
    }

    /**
     * 查询apiList
     * @param vo 查询obj
     * @param pageForm 分页参数
     * @return Result
     */
    public Result searchApi(SysApiVO vo, PageForm pageForm) {
        PageHelper.startPage(pageForm.getPageNum(), pageForm.getPageSize());
        List<SysApi> sysApis = sysApiMapper.selectList(new LambdaQueryWrapper<SysApi>()
                .like(StringUtils.hasText(vo.getApiCode()), SysApi::getApiCode, vo.getApiCode())
                .like(StringUtils.hasText(vo.getApiName()), SysApi::getApiName, vo.getApiName())
                .eq(vo.getSystemType() != null, SysApi::getSystemType, vo.getSystemType())
                .orderByAsc(SysApi::getCreateTime));
        return Result.success(new PageInfo<>(sysApis));
    }

    /**
     * 新增对外api
     * @param vo 查询obj
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result addApi(SysApiVO vo) {
        Assert.notNull(vo.getApiCode(), "参数错误");
        Assert.notNull(vo.getApiName(), "参数错误");
        Assert.notNull(vo.getApiUrl(), "参数错误");
        Assert.notNull(vo.getSystemType(), "参数错误");
        //判断code是否存在
        List<SysApi> sysApis = sysApiMapper.selectList(
                new LambdaQueryWrapper<SysApi>().eq(SysApi::getApiCode, vo.getApiCode()));
        if (!CollectionUtils.isEmpty(sysApis)) {
            return Result.error("新增失败，存在相同编码接口", ResultCode.PARAM_ERROR);
        }
        SysApi sysApi = new SysApi();
        BeanUtils.copyProperties(vo, sysApi);
        sysApiMapper.insert(sysApi);
        return Result.success("");
    }

    /**
     * 修改对外api
     * @param vo 查询obj
     * @return Result
     */
    public Result updateApi(SysApiVO vo) {
        Assert.notNull(vo.getId(), "参数错误");
        Assert.notNull(vo.getApiName(), "参数错误");
        Assert.notNull(vo.getApiUrl(), "参数错误");
        SysApi sysApi = new SysApi();
        BeanUtils.copyProperties(vo, sysApi);
        sysApiMapper.updateById(sysApi);
        return Result.success("");
    }

    /**
     * 新增
     * @param vo 查询obj
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result add(SysApiAuthVO vo) {
        Assert.notNull(vo.getAppId(), "参数错误");
        Assert.notNull(vo.getAppSecret(), "参数错误");
        Assert.notNull(vo.getSystemName(), "参数错误");
        Assert.notNull(vo.getSystemReferer(), "参数错误");
        Assert.notNull(vo.getFormat(), "参数错误");
        Assert.notNull(vo.getCharset(), "参数错误");
        Assert.notNull(vo.getSignType(), "参数错误");
        Assert.notNull(vo.getPublicKey(), "参数错误");
        Assert.notNull(vo.getPrivateKey(), "参数错误");
        //判断appId是否重复
        Long aooIdCount = sysApiSystemMapper.selectCount(
                new LambdaQueryWrapper<SysApiSystem>().eq(SysApiSystem::getAppId, vo.getAppId()));
        Assert.isTrue(aooIdCount == 0, "存在重复AppId，无法添加");
        SysApiSystem system = new SysApiSystem();
        BeanUtils.copyProperties(vo, system);
        // 默认 0 已授权
        system.setStatus(0);
        system.setExpirationTime(new Date());
        sysApiSystemMapper.insert(system);
        return Result.success("");
    }

    /**
     * 修改
     * @param vo 查询obj
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result update(SysApiAuthVO vo) {
        Assert.notNull(vo.getId(), "参数错误");
        Assert.notNull(vo.getSystemName(), "参数错误");
        Assert.notNull(vo.getSystemReferer(), "参数错误");
        Assert.notNull(vo.getFormat(), "参数错误");
        Assert.notNull(vo.getCharset(), "参数错误");
        Assert.notNull(vo.getSignType(), "参数错误");
        sysApiSystemMapper.update(null,
                new LambdaUpdateWrapper<SysApiSystem>()
                        .set(SysApiSystem::getSystemName, vo.getSystemName())
                        .set(SysApiSystem::getSystemReferer, vo.getSystemReferer())
                        .set(SysApiSystem::getFormat, vo.getFormat())
                        .set(SysApiSystem::getCharset, vo.getCharset())
                        .set(SysApiSystem::getSignType, vo.getSignType())
                        .eq(SysApiSystem::getId, vo.getId()));
        return Result.success("");
    }

    /**
     * 删除
     * @param id id
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result del(Long[] id) {
        Assert.notEmpty(id, "参数错误");
        List<SysApiSystem> sysApiSystems = sysApiSystemMapper.selectBatchIds(Arrays.asList(id));
        sysApiSystems.stream().forEach(i -> {
            //调用认证服务，使token失效
            openAuthApi.revokeToken(i.getAppId());
        });
        //删除api系统表
        sysApiSystemMapper.deleteBatchIds(Arrays.asList(id));
        //删除关联表
        sysApiAuthMapper.delete(new LambdaQueryWrapper<SysApiAuth>().in(SysApiAuth::getAppId, id));
        return Result.success("");
    }

    /**
     * 删除对外api
     * @param apiId apiId
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result delApi(Long apiId) {
        Assert.notNull(apiId, "参数错误");
        //删除接口数据
        sysApiMapper.deleteById(apiId);
        //删除关联表数据
        sysApiAuthMapper
                .delete(new LambdaUpdateWrapper<SysApiAuth>().eq(SysApiAuth::getApiId, apiId));
        return Result.success();
    }

    /**
     * 取消授权
     * @param id id
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result cancelAuth(Long id) {
        Assert.notNull(id, "参数错误");
        SysApiSystem system = sysApiSystemMapper.selectById(id);
        system.setStatus(1);
        sysApiSystemMapper.updateById(system);
        //调用认证服务，使token失效，重新获取新的token
        openAuthApi.revokeToken(system.getAppId());
        return Result.success("");
    }

    /**
     * 开启授权
     * @param id id
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result startAuth(Long id) {
        Assert.notNull(id, "参数错误");
        SysApiSystem system = sysApiSystemMapper.selectById(id);
        system.setStatus(0);
        sysApiSystemMapper.updateById(system);
        return Result.success("");
    }

    /**
     * 获取AppId/AppSecret
     * @param appId appid
     * @return Result
     */
    public Result getAppId(String appId) {
        Map<String, Object> appIdMap = new HashMap<String, Object>(6);
        if (!StringUtils.hasText(appId)) {
            appId = AppUtils.getAppId();
        }
        String appSecret = AppUtils.getAppSecret(appId);
        appIdMap.put("appId", appId);
        appIdMap.put("appSecret", appSecret);
        return Result.success(appIdMap);
    }

    /**
     * 获取公私钥
     * @return Result
     */
    public Result getKey() {
        Map<String, Object> stringObjectMap = new HashMap<>(6);
        try {
            stringObjectMap = RsaUtils.generateRsaKeyPairs();
        } catch (Exception e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        }
        return Result.success(stringObjectMap);
    }

    /**
     * 获取api集
     * @param id id
     * @return Result
     */
    public Result getApiList(Long id) {
        Map<String, Object> map = new HashMap<>(6);
        //全部的api
        List<SysApi> sysApis = sysApiMapper.selectList(null);
        if (sysApis.size() == 0) {
            return Result.success(map);
        }
        //已经关联的api
        List<SysApiAuth> sysApiAuths = sysApiAuthMapper
                .selectList(new LambdaQueryWrapper<SysApiAuth>().eq(SysApiAuth::getAppId, id));
        if (sysApiAuths.size() == 0) {
            map.put("fromData", buildDataTree(sysApis));
            return Result.success(map);
        }
        List<Long> accApiIds = sysApiAuths.stream().map(SysApiAuth::getApiId)
                .collect(Collectors.toList());
        //已关联Api
        List<SysApi> accSysApis = sysApiMapper.selectBatchIds(accApiIds);
        //未关联Api
        List<SysApi> unAccSysApis = sysApis.stream().filter(
                apiA -> !accSysApis.stream().anyMatch(apiB -> apiB.getId().equals(apiA.getId())))
                .collect(Collectors.toList());
        map.put("fromData", buildDataTree(unAccSysApis));
        map.put("toData", buildDataTree(accSysApis));
        return Result.success(map);
    }

    /**
     * 关联接口
     * @param apiId apiId
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result assocApi(Long id, Long[] apiId) {
        Assert.notNull(id, "参数错误");
        sysApiAuthMapper.delete(new LambdaUpdateWrapper<SysApiAuth>().eq(SysApiAuth::getAppId, id));
        if (null != apiId && apiId.length > 0) {
            List<SysApiAuth> list = new ArrayList<>();
            Arrays.stream(apiId).forEach(i -> {
                SysApiAuth sysApiAuth = new SysApiAuth();
                sysApiAuth.setAppId(id);
                sysApiAuth.setApiId(i);
                list.add(sysApiAuth);
            });
            MybatisBatch<SysApiAuth> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, list);
            MybatisBatch.Method<SysApiAuth> method = new MybatisBatch.Method<>(
                    SysApiAuthMapper.class);
            mybatisBatch.execute(method.insert());
        }
        SysApiSystem system = sysApiSystemMapper.selectById(id);
        //调用认证服务，使token失效，重新获取新的token
        openAuthApi.revokeToken(system.getAppId());
        return Result.success("");
    }

    /**
     * 关闭/开启时间校验
     * @param apiId apiId
     * @param status 状态
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result timestampFilter(Long apiId, Integer status) {
        Assert.notNull(apiId, "参数错误");
        Assert.notNull(status, "参数错误");
        sysApiMapper.update(null, new LambdaUpdateWrapper<SysApi>()
                .set(SysApi::getIsTimestamp, status).eq(SysApi::getId, apiId));
        //更新redis的value
        List<SysApi> sysApis = sysApiMapper
                .selectList(new LambdaQueryWrapper<SysApi>().eq(SysApi::getIsTimestamp, 1));
        List<String> ignoreList = sysApis.stream().map(SysApi::getApiUrl)
                .collect(Collectors.toList());
        redisUtils.set(CachePrefixConstants.AUTH + "timestampIgnore", escapeUrl(ignoreList));
        return Result.success("");
    }

    /**
     * 关闭/开启referer校验
     * @param apiId apiId
     * @param status 状态
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result refererFilter(Long apiId, Integer status) {
        Assert.notNull(apiId, "参数错误");
        Assert.notNull(status, "参数错误");
        sysApiMapper.update(null, new LambdaUpdateWrapper<SysApi>()
                .set(SysApi::getIsReferer, status).eq(SysApi::getId, apiId));
        //更新redis的value
        List<SysApi> sysApis = sysApiMapper
                .selectList(new LambdaQueryWrapper<SysApi>().eq(SysApi::getIsReferer, 1));
        List<String> ignoreList = sysApis.stream().map(SysApi::getApiUrl)
                .collect(Collectors.toList());
        redisUtils.set(CachePrefixConstants.AUTH + "refererIgnore", escapeUrl(ignoreList));
        return Result.success("");
    }

    /**
     * 关闭/开启验签
     * @param apiId apiId
     * @param status 状态
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Result signFilter(Long apiId, Integer status) {
        Assert.notNull(apiId, "参数错误");
        Assert.notNull(status, "参数错误");
        sysApiMapper.update(null, new LambdaUpdateWrapper<SysApi>().set(SysApi::getIsSign, status)
                .eq(SysApi::getId, apiId));
        //更新redis的value
        List<SysApi> sysApis = sysApiMapper
                .selectList(new LambdaQueryWrapper<SysApi>().eq(SysApi::getIsSign, 1));
        List<String> ignoreList = sysApis.stream().map(SysApi::getApiUrl)
                .collect(Collectors.toList());
        redisUtils.set(CachePrefixConstants.AUTH + "signIgnore", escapeUrl(ignoreList));
        return Result.success("");
    }

    /**
     * 分页
     * @param sysApiSystemPageInfo 对象
     * @return result
     */
    private PageInfo<SysApiAuthDTO> getSysApiAuthDTOListPageInfo(PageInfo<SysApiSystem> sysApiSystemPageInfo) {
        PageInfo<SysApiAuthDTO> resultPage = new PageInfo<>();
        List<SysApiAuthDTO> sysApiAuthDTOList = PageCopyListUtils
                .copyListProperties(sysApiSystemPageInfo.getList(), SysApiAuthDTO.class);
        sysApiAuthDTOList.forEach(i -> i.setStatusStr(i.getStatus() == 0 ? "已授权" : "未授权"));
        BeanUtils.copyProperties(sysApiSystemPageInfo, resultPage);
        resultPage.setList(sysApiAuthDTOList);
        return resultPage;
    }

    /**
     * 构建树
     * list->Tree
     *
     * @param list apiList
     * @return Result
     */
    private List<Tree<Long>> buildDataTree(List<SysApi> list) {
        //额外三个添加对象，方便TreeUtil进行建树
        addParentObj(list);
        //把system_type字段当parent 基础parent = -1

        List<Tree<Long>> treeList = TreeUtil.build(list, -1L, new TreeNodeConfig(),
                (treeNode, tree) -> {
                    tree.setId(treeNode.getId());
                    tree.setParentId(treeNode.getSystemType().longValue());
                    tree.setName(treeNode.getApiName());
                    tree.putExtra("key", treeNode.getId());
                    tree.putExtra("label", treeNode.getApiName());
                });
        return treeList;

    }

    /**
     * 添加三个父节点对象
     *
     * @param list apilist
     */
    private void addParentObj(List<SysApi> list) {
        if (list.stream().anyMatch(i -> Objects.equals(i.getSystemType(),
                Integer.parseInt(StateConstants.SYSTEM.toString())))) {
            SysApi basicApi = new SysApi();
            basicApi.setId(StateConstants.SYSTEM);
            basicApi.setApiName("基础管理");
            basicApi.setSystemType(-1);
            list.add(basicApi);
        }
        if (list.stream().anyMatch(i -> Objects.equals(i.getSystemType(),
                Integer.parseInt(StateConstants.EAM.toString())))) {
            SysApi arcApi = new SysApi();
            arcApi.setId(StateConstants.EAM);
            arcApi.setApiName("综合档案");
            arcApi.setSystemType(-1);
            list.add(arcApi);
        }
        if (list.stream().anyMatch(i -> Objects.equals(i.getSystemType(),
                Integer.parseInt(StateConstants.ECM.toString())))) {
            SysApi ecmApi = new SysApi();
            ecmApi.setId(StateConstants.ECM);
            ecmApi.setApiName("影像模块");
            ecmApi.setSystemType(-1);
            list.add(ecmApi);
        }
        if (list.stream().anyMatch(i -> Objects.equals(i.getSystemType(),
                Integer.parseInt(StateConstants.EDM.toString())))) {
            SysApi ecmApi = new SysApi();
            ecmApi.setId(StateConstants.EDM);
            ecmApi.setApiName("文档系统");
            ecmApi.setSystemType(-1);
            list.add(ecmApi);
        }
        if (list.stream().anyMatch(i -> Objects.equals(i.getSystemType(),
                Integer.parseInt(StateConstants.AFM.toString())))) {
            SysApi ecmApi = new SysApi();
            ecmApi.setId(StateConstants.AFM);
            ecmApi.setApiName("反欺诈系统");
            ecmApi.setSystemType(-1);
            list.add(ecmApi);
        }
        if (list.stream().anyMatch(i -> Objects.equals(i.getSystemType(),
                Integer.parseInt(StateConstants.STORAGE.toString())))) {
            SysApi ecmApi = new SysApi();
            ecmApi.setId(StateConstants.STORAGE);
            ecmApi.setApiName("存储服务");
            ecmApi.setSystemType(-1);
            list.add(ecmApi);
        }
    }

    /**
     * Collection转义后转成json
     * @param collection 集合
     * @return Result
     */
    private String escapeUrl(Collection<String> collection) {
        List<String> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(collection)) {
            for (String i : collection) {
                list.add(i.replaceAll("\\*", ".*"));
            }
        }
        return JSON.toJSONString(list);
    }

    public Result getSm2Key() {
        Map<String, Object> stringObjectMap = new HashMap<>(6);
        try {
            stringObjectMap = Sm2Util.generateRsaKeyPairs();
        } catch (Exception e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        }
        return Result.success(stringObjectMap);
    }
}
