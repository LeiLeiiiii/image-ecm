package com.sunyard.module.system.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.service.SysConfigAuthService;
import com.sunyard.module.system.vo.SysApiAuthVO;
import com.sunyard.module.system.vo.SysApiVO;

/**
 * 通用管理/系统初始化/接入授权
 * @author P-JWei
 * @date 2023/7/31 11:30:02
 * @title 系统管理/接入授权
 * @description
 */
@RestController
@RequestMapping("basics/sysConfig/auth")
public class BasicsSysConfigAuthController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.MENU_SYSTEM + "-接入授权->";
    @Resource
    private SysConfigAuthService sysConfigAuthService;

    /**
     * 查询
     * @param vo 查询obj
     * @param pageForm 分页参数
     * @return result
     */
    @OperationLog(BASELOG + "查询")
    @PostMapping("search")
    public Result search(SysApiAuthVO vo, PageForm pageForm) {
        return sysConfigAuthService.search(vo, pageForm);
    }

    /**
     * 查询apiList
     * @param vo 查询obj
     * @param pageForm 分页参数
     * @return result
     */
    @OperationLog(BASELOG + "查询apiList")
    @PostMapping("searchApi")
    public Result searchApi(SysApiVO vo, PageForm pageForm) {
        return sysConfigAuthService.searchApi(vo, pageForm);
    }

    /**
     * 新增对外api
     * @param vo 新增obj
     * @return result
     */
    @OperationLog(BASELOG + "新增对外api")
    @PostMapping("addApi")
    public Result addApi(SysApiVO vo) {
        return sysConfigAuthService.addApi(vo);
    }

    /**
     * 修改对外api
     * @param vo 修改obj
     * @return result
     */
    @OperationLog(BASELOG + "修改对外api")
    @PostMapping("updateApi")
    public Result updateApi(SysApiVO vo) {
        return sysConfigAuthService.updateApi(vo);
    }

    /**
     * 新增
     * @param vo 新增obj
     * @return result
     */
    @OperationLog(BASELOG + "新增")
    @PostMapping("add")
    public Result add(SysApiAuthVO vo) {
        return sysConfigAuthService.add(vo);
    }

    /**
     * 修改
     * @param vo 修改obj
     * @return result
     */
    @OperationLog(BASELOG + "修改")
    @PostMapping("update")
    public Result update(SysApiAuthVO vo) {
        return sysConfigAuthService.update(vo);
    }

    /**
     * 删除
     * @param id 授权id
     * @return result
     */
    @OperationLog(BASELOG + "删除")
    @PostMapping("del")
    public Result del(Long[] id) {
        return sysConfigAuthService.del(id);
    }

    /**
     * 删除对外api apiId
     * @return result
     */
    @OperationLog(BASELOG + "删除对外api")
    @PostMapping("delApi")
    public Result delApi(Long apiId) {
        return sysConfigAuthService.delApi(apiId);
    }

    /**
     * 取消授权
     * @param id 授权id
     * @return result
     */
    @PostMapping("cancelAuth")
    @OperationLog(BASELOG + "取消授权")
    public Result cancelAuth(Long id) {
        return sysConfigAuthService.cancelAuth(id);
    }

    /**
     * 开启授权
     * @param id 授权id
     * @return result
     */
    @OperationLog(BASELOG + "开启授权")
    @PostMapping("startAuth")
    public Result startAuth(Long id) {
        return sysConfigAuthService.startAuth(id);
    }

    /**
     * 获取AppId/AppSecret
     * @param appId appId
     * @return result
     */
    @OperationLog(BASELOG + "获取AppId/AppSecret")
    @PostMapping("getAppId")
    public Result getAppId(String appId) {
        return sysConfigAuthService.getAppId(appId);
    }

    /**
     * 获取公私钥
     * @return result
     */
    @OperationLog(BASELOG + "获取公私钥")
    @PostMapping("getKey")
    public Result getKey() {
        return sysConfigAuthService.getKey();
    }

    /**
     * 获取SM2公私钥
     * @return result
     */
    @OperationLog(BASELOG + "获取Sm2公私钥")
    @PostMapping("getSm2Key")
    public Result getSm2Key() {
        return sysConfigAuthService.getSm2Key();
    }


    /**
     * 获取api集
     * @param id 授权id
     * @return result
     */
    @OperationLog(BASELOG + "获取api集")
    @PostMapping("getApiList")
    public Result getApiList(Long id) {
        return sysConfigAuthService.getApiList(id);
    }

    /**
     * 关联接口
     * @param appId appId
     * @param apiId apiId集
     * @return result
     */
    @OperationLog(BASELOG + "关联接口")
    @PostMapping("assocApi")
    public Result assocApi(Long appId, Long[] apiId) {
        return sysConfigAuthService.assocApi(appId, apiId);
    }

    /**
     * 关闭/开启时间校验
     * @param apiId apiId
     * @param status 状态 0开启 1关闭
     * @return result
     */
    @OperationLog(BASELOG + "关闭/开启时间校验")
    @PostMapping("timestampFilter")
    public Result timestampFilter(Long apiId, Integer status) {
        return sysConfigAuthService.timestampFilter(apiId, status);
    }

    /**
     * 关闭/开启referer校验
     * @param apiId apiId
     * @param status 状态 0开启 1关闭
     * @return result
     */
    @OperationLog(BASELOG + "关闭/开启referer校验")
    @PostMapping("refererFilter")
    public Result refererFilter(Long apiId, Integer status) {
        return sysConfigAuthService.refererFilter(apiId, status);
    }

    /**
     * 关闭/开启验签
     * @param apiId apiId
     * @param status 状态 0开启 1关闭
     * @return result
     */
    @OperationLog(BASELOG + "关闭/开启验签")
    @PostMapping("signFilter")
    public Result signFilter(Long apiId, Integer status) {
        return sysConfigAuthService.signFilter(apiId, status);
    }
}
