package com.sunyard.module.system.weaver.service;

import java.util.List;

import javax.annotation.Resource;

import com.sunyard.module.system.weaver.bo.OaPost;
import com.sunyard.module.system.weaver.bo.QueryPostRequest;
import com.sunyard.module.system.weaver.bo.QueryPostResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.sunyard.module.system.config.properties.SystemWeaverProperties;
import com.sunyard.module.system.weaver.bo.OaBaseRequest;
import com.sunyard.module.system.weaver.bo.OaDept;
import com.sunyard.module.system.weaver.bo.OaInst;
import com.sunyard.module.system.weaver.bo.OaUser;
import com.sunyard.module.system.weaver.bo.QueryDeptRequest;
import com.sunyard.module.system.weaver.bo.QueryDeptResponse;
import com.sunyard.module.system.weaver.bo.QueryInstRequest;
import com.sunyard.module.system.weaver.bo.QueryInstResponse;
import com.sunyard.module.system.weaver.bo.QueryUserRequest;
import com.sunyard.module.system.weaver.bo.QueryUserResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouleibin
 * @Desc 泛微同步接口实现类
 */
@Slf4j
@Service
public class WeaverOaService {

    @Resource
    private SystemWeaverProperties systemWeaverProperties;
    @Resource
    private RestTemplate template;

    public List<OaInst> queryInstData(QueryInstRequest request) {
        QueryInstResponse response = this.call(request, QueryInstResponse.class);
        if (!ObjectUtils.isEmpty(response) && response.getCode().equals("1") && response.getData().getDataList().size() > 0) {
            return response.getData().getDataList();
        }
        return null;
    }

    public List<OaDept> queryDeptData(QueryDeptRequest request) {
        QueryDeptResponse response = this.call(request, QueryDeptResponse.class);
        if (!ObjectUtils.isEmpty(response) && response.getCode().equals("1") && response.getData().getDataList().size() > 0) {
            return response.getData().getDataList();
        }
        return null;
    }

    public List<OaUser> queryUserData(QueryUserRequest request) {
        QueryUserResponse response = this.call(request, QueryUserResponse.class);
        if (!ObjectUtils.isEmpty(response) && response.getCode().equals("1") && response.getData().getDataList().size() > 0) {
            return response.getData().getDataList();
        }
        return null;
    }

    public List<OaPost> queryPostData(QueryPostRequest request) {
        QueryPostResponse response = this.call(request, QueryPostResponse.class);
        if (!ObjectUtils.isEmpty(response) && response.getCode().equals("1") && response.getData().getDataList().size() > 0) {
            return response.getData().getDataList();
        }
        return null;
    }

    /**
     * @param request
     * @param responseClass
     * @return T
     * @despciption
     */
    private <T> T call(OaBaseRequest request, Class<T> responseClass) {
        setRequestBaseInfo(request);
        String url = request.getUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = request.getHttpEntity(headers);
        try {
            log.debug("调用泛微OA接口，请求接口地址：[{}]", url);
            log.info("调用泛微OA接口，请求数据：[{}]", JSON.toJSONString(entity));
            ResponseEntity<T> response = template.postForEntity(url, entity, responseClass);
            log.info("调用泛微OA接口，应答数据：[{}]", JSON.toJSONString(response));
            return response == null ? null : response.getBody();
        } catch (Exception e) {
            log.error("调用泛微OA接口，错误", e);
            return null;
        }
    }

    /**
     * @param request void
     * @despciption
     */
    private void setRequestBaseInfo(OaBaseRequest request) {
        request.setBaseUrl(systemWeaverProperties.getUrlPrefix());
    }

}
