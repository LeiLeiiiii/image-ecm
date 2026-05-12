package com.sunyard.module.system.weaver.service;

import java.util.List;

import javax.annotation.Resource;

import com.sunyard.module.system.weaver.bo.OaPost;
import com.sunyard.module.system.weaver.bo.QueryPostRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.conversion.JsonUtils;
import com.sunyard.module.system.controller.BaseController;
import com.sunyard.module.system.task.service.SyncInstDeptUserService;
import com.sunyard.module.system.weaver.bo.OaDept;
import com.sunyard.module.system.weaver.bo.OaInst;
import com.sunyard.module.system.weaver.bo.OaUser;
import com.sunyard.module.system.weaver.bo.QueryDeptRequest;
import com.sunyard.module.system.weaver.bo.QueryInstRequest;
import com.sunyard.module.system.weaver.bo.QueryUserRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * 基础管理/通用管理/组织管理
 *
 * @Author PJW 2021/7/5 16:50
 */
@Slf4j
@RestController
@RequestMapping("weaver")
public class WeaverTestController extends BaseController {

    @Resource
    private WeaverOaService service;
    @Resource
    private SyncInstDeptUserService syncInstDeptUserService;
    @GetMapping("inst")
    public Result inst() {
        QueryInstRequest request = new QueryInstRequest();
        request.setCurpage(1);
        request.setPagesize(99999);
        List<OaInst> list = service.queryInstData(request);
        log.debug(JsonUtils.toJSONString(list));
        return Result.success(true);
    }

    @GetMapping("dept")
    public Result dept() {
        QueryDeptRequest request = new QueryDeptRequest();
        request.setCurpage(1);
        request.setPagesize(99999);
        List<OaDept> list = service.queryDeptData(request);
        log.debug(JsonUtils.toJSONString(list));
        return Result.success(true);
    }

    @GetMapping("user")
    public Result user() {
        QueryUserRequest request = new QueryUserRequest();
        request.setCurpage(1);
        request.setPagesize(99999);
        List<OaUser> list = service.queryUserData(request);
        log.debug(JsonUtils.toJSONString(list));
        return Result.success(true);
    }

    @GetMapping("post")
    public Result post() {
        // 岗位同步
        QueryPostRequest postRequest = new QueryPostRequest();
        postRequest.setCurpage(1);
        postRequest.setPagesize(99999);
        List<OaPost> postResults = service.queryPostData(postRequest);
        if (!ObjectUtils.isEmpty(postResults)) {
            syncInstDeptUserService.addSysLdapMiddle(postResults, 3);
            syncInstDeptUserService.upPostData();
        }
        return Result.success(true);
    }

    @GetMapping("sync")
    public Result sync() {
        syncInstDeptUserService.synchronizationInstDeptUser();
        return Result.success(true);
    }
}
