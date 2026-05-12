package com.sunyard.ecm.api;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunyard.ecm.config.EcmApiFactoryConfig;
import com.sunyard.ecm.config.SunyardConfig;
import com.sunyard.ecm.constant.ApiConstants;
import com.sunyard.ecm.dto.*;
import com.sunyard.ecm.dto.split.FileUploadDTO;
import com.sunyard.ecm.dto.split.SplitUploadBigFileDTO;
import com.sunyard.ecm.dto.split.SplitUploadDTO;
import com.sunyard.ecm.dto.split.SplitUploadRecordDTO;
import com.sunyard.ecm.dto.split.SysFileApiDTO;
import com.sunyard.ecm.enums.ResultCodeApiEnum;
import com.sunyard.ecm.result.ResultApi;
import com.sunyard.ecm.util.BeanUtils;
import com.sunyard.ecm.util.FileUploadSplitUtils;
import com.sunyard.ecm.util.FileZipUtils;
import com.sunyard.ecm.vo.BusiDocDuplicateVO;
import com.sunyard.ecm.vo.EcmAppDefVO;
import com.sunyard.ecm.vo.EcmDelVO;
import com.sunyard.ecm.vo.QueryBusiInfoVO;
import com.sunyard.ecm.vo.QueryDataVO;
import com.sunyard.ecm.vo.SplitUploadVO;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.util.ZipUtils;
import com.sunyard.framework.common.util.encryption.AesUtils;
import com.sunyard.framework.common.util.encryption.RsaUtils;
import com.sunyard.framework.oauth.SunApiClient;
import com.sunyard.framework.oauth.methods.SunPost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 影像采集对外接口
 *
 * @author scm
 * @Description 影像采集
 * @since 2023/8/9 10:56
 */
@Component
@Slf4j
public class EcmApiImpl implements EcmApi {

    //todo 线程池定义有问题
    private static Executor taskExecutor;

    private String urlEncodeKey = "sunyard-abcdefghijklmnopq-123456";


    /**
     * 获取线程池
     *
     * @return
     */
    public Executor getTaskExecutor() {
        if (taskExecutor == null) {
            taskExecutor = BeanUtils.getBean(Executor.class);
        }
        return taskExecutor;
    }

    /**
     * 影像调阅
     *
     * @param ecmRootDataDTO 调阅入参
     * @return BusiInfoAndFileVO
     */
    @Override
    public ResultApi<EcmPageBaseInfoDTO> accessEcm(EcmRootDataDTO ecmRootDataDTO) {
        EcmBaseInfoDTO ecmBaseInfoDTO = ecmRootDataDTO.getEcmBaseInfoDTO();
        ecmBaseInfoDTO.setIsScan(ApiConstants.ONLY_SHOW);
        ecmRootDataDTO.setEcmBaseInfoDTO(ecmBaseInfoDTO);
        return scanOrUpdateEcmBase(ecmRootDataDTO);
    }

    @Override
    public ResultApi<EcmPageBaseInfoDTO> scanOrUpdateEcm(EcmRootDataDTO ecmRootDataDTO) {
        EcmBaseInfoDTO ecmBaseInfoDTO = ecmRootDataDTO.getEcmBaseInfoDTO();
        ecmBaseInfoDTO.setIsScan(ApiConstants.HAVE_CAPTURE);
        ecmRootDataDTO.setEcmBaseInfoDTO(ecmBaseInfoDTO);
        return scanOrUpdateEcmBase(ecmRootDataDTO);
    }


    @Override
    public ResultApi<EcmPageBaseInfoDTO> scanOrUpdateEcmIe(EcmRootDataDTO ecmRootDataDTO) {
        EcmBaseInfoDTO ecmBaseInfoDTO = ecmRootDataDTO.getEcmBaseInfoDTO();
        ecmBaseInfoDTO.setIsScan(ApiConstants.HAVE_CAPTURE);
        ecmRootDataDTO.setEcmBaseInfoDTO(ecmBaseInfoDTO);
        return scanOrUpdateEcmBaseIe(ecmRootDataDTO);
    }

    @Override
    public ResultApi<EcmPageBaseInfoDTO> scanOrUpdateEcmMobile(EcmRootDataDTO ecmRootDataDTO) {
        EcmBaseInfoDTO ecmBaseInfoDTO = ecmRootDataDTO.getEcmBaseInfoDTO();
        if (ApiConstants.HAVE_CAPTURE.equals(ecmBaseInfoDTO.getIsScan())) {
            ecmBaseInfoDTO.setIsScan(ApiConstants.HAVE_CAPTURE);
        } else {
            ecmBaseInfoDTO.setIsScan(ApiConstants.ONLY_SHOW);
        }
        ecmRootDataDTO.setEcmBaseInfoDTO(ecmBaseInfoDTO);
        return scanOrUpdateEcmMobileBase(ecmRootDataDTO);
    }

    /**
     * 影像采集或修改
     *
     * @param ecmRootDataDTO 采集入参
     * @return BusiInfoAndFileVO
     */
    public ResultApi<EcmPageBaseInfoDTO> scanOrUpdateEcmMobileBase(EcmRootDataDTO ecmRootDataDTO) {
        //请求客户端
        Map<String, String> map = getConnectBase(ecmRootDataDTO, ApiConstants.ACCESSECM_MOBILE);
        String connect = map.get(ApiConstants.RETURN_RESPONSESTR);
        ResultApi dto1 = JSONObject.parseObject(connect, ResultApi.class);
        String s = JSONObject.toJSONString(dto1.getData());
        EcmPageBaseInfoDTO dto = JSONObject.parseObject(s, EcmPageBaseInfoDTO.class);
//        dto.setAccessToken(map.get(ApiConstants.REQUEST_TOKENSTR));
        dto.setRole(ecmRootDataDTO.getEcmBaseInfoDTO().getRoleCode());
        SunyardConfig bean = BeanUtils.getBean(SunyardConfig.class);
        dto.setAppId(bean.getAppId());
        try {
            //跳转路径解密
            dto.setPageUrl(RsaUtils.decrypt(dto.getPageUrl()));
        } catch (Exception e) {
            log.error("跳转页面加密有误");
        }
//        dto.setPageUrl("http://127.0.0.1:8090/sunIcmsMobile/mobileTerminal");
        // 创建StringBuilder用于构建参数
        StringBuilder params = new StringBuilder();
        params.append("sun=true")
                .append("&token=").append(dto.getAccessToken())
                .append("&role=").append(dto.getRole())
                .append("&appId=").append(dto.getAppId())
//                .append("&flagId=").append(dto.getFlagId())
                .append("&nonce=").append(dto.getNonce())
                .append("&usercode=").append(ecmRootDataDTO.getEcmBaseInfoDTO().getUserCode())
                .append("&username=").append(ecmRootDataDTO.getEcmBaseInfoDTO().getUserName())
                .append("&orgCode=").append(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode())
                .append("&orgName=").append(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgName())
                .append("&isRetransmission=").append(ecmRootDataDTO.getEcmBaseInfoDTO().getIsRetransmission());

        if (StrUtil.equals(ApiConstants.SIGNAL_SCAN, ecmRootDataDTO.getEcmBaseInfoDTO().getOneBatch())) {
            // 单扫场景
            if (ecmRootDataDTO.getEcmBaseInfoDTO().getIsScan().equals(ApiConstants.HAVE_CAPTURE)) {
                // 采集场景
                params.append("&businessOperateType=1");
            } else {
                // 调阅场景
                params.append("&openType=view")
                        .append("&busIds=").append(ecmRootDataDTO.getEcmBusExtendDTOS().size())
                        .append("&businessOperateType=2");
            }
            if (2 == ecmRootDataDTO.getEcmBaseInfoDTO().getIsRetransmission()) {
                params.append("&openType=view");
            }
        } else {
            // 批扫场景
            params.append("&isDefaultShowDialog=1")
                    .append("&businessOperateType=0");
        }

        if ( ecmRootDataDTO.getEcmBusExtendDTOS() != null && !ecmRootDataDTO.getEcmBusExtendDTOS().isEmpty()) {
            EcmBusExtendDTO busExtendDTO = ecmRootDataDTO.getEcmBusExtendDTOS().get(0);
            if (StrUtil.isNotBlank(busExtendDTO.getDelegateType())) {
                params.append("&delegateType=").append(busExtendDTO.getDelegateType());
            }
            if (StrUtil.isNotBlank(busExtendDTO.getTypeBig())) {
                params.append("&typeBig=").append(busExtendDTO.getTypeBig());
            }
        }
        log.info("scanOrUpdateEcmMobileBase,params : {}",params);
        // 一次性拼接完整URL
        dto.setPageUrl(dto.getPageUrl() + "?sp=" + getUrlEncode(params.toString()));
        return ResultApi.success(dto);
    }

    /**
     * url参数加密
     */
    private String getUrlEncode(String str) {
        String sp = "";
        try {
            // 1. 先对原始字符串进行AES加密
            String encryptedStr = AesUtils.encrypt(str.getBytes(StandardCharsets.UTF_8), urlEncodeKey);
            // 2. 对加密后的字符串进行URL编码
            sp = URLEncoder.encode(encryptedStr, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            log.error("URL编码失败，不支持的编码格式", e);
        } catch (Exception e) {
            log.error("AES加密参数失败", e);
        }
        return sp;
    }


    /**
     * 影像采集或修改
     *
     * @param ecmRootDataDTO 采集入参
     * @return BusiInfoAndFileVO
     */
    public ResultApi<EcmPageBaseInfoDTO> scanOrUpdateEcmBase(EcmRootDataDTO ecmRootDataDTO) {
        if (CollectionUtil.isEmpty(ecmRootDataDTO.getEcmBusExtendDTOS())) {
            ecmRootDataDTO.getEcmBaseInfoDTO().setOneBatch(ApiConstants.BATCH_SCAN);
        } else {
            ecmRootDataDTO.getEcmBaseInfoDTO().setOneBatch(ApiConstants.SIGNAL_SCAN);
        }
        //请求客户端
        Map<String, String> map = getConnectBase(ecmRootDataDTO, ApiConstants.ACCESSECM);
        String connect = map.get(ApiConstants.RETURN_RESPONSESTR);
        ResultApi dto1 = JSONObject.parseObject(connect, ResultApi.class);
        String s = JSONObject.toJSONString(dto1.getData());
        EcmPageBaseInfoDTO dto = JSONObject.parseObject(s, EcmPageBaseInfoDTO.class);
//        dto.setAccessToken(map.get(ApiConstants.REQUEST_TOKENSTR));
        dto.setRole(ecmRootDataDTO.getEcmBaseInfoDTO().getRoleCode());
        SunyardConfig bean = BeanUtils.getBean(SunyardConfig.class);
        dto.setAppId(bean.getAppId());
        try {
            //跳转路径解密
            dto.setPageUrl(RsaUtils.decrypt(dto.getPageUrl()));
        } catch (Exception e) {
            log.error("跳转页面加密有误");
        }
//        dto.setPageUrl("http://127.0.0.1:8040/sunIcms/#/imageContentManagement");
        // 构建基础参数
        StringBuilder params = new StringBuilder();
        params.append("sun=true")
                .append("&token=").append(dto.getAccessToken())
                .append("&role=").append(dto.getRole())
                .append("&appId=").append(dto.getAppId())
//                .append("&flagId=").append(dto.getFlagId())
                .append("&nonce=").append(dto.getNonce())
                .append("&usercode=").append(ecmRootDataDTO.getEcmBaseInfoDTO().getUserCode())
                .append("&username=").append(ecmRootDataDTO.getEcmBaseInfoDTO().getUserName())
                .append("&orgCode=").append(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode())
                .append("&orgName=").append(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgName())
                .append("&isRetransmission=").append(ecmRootDataDTO.getEcmBaseInfoDTO().getIsRetransmission());

        if (StrUtil.equals(ApiConstants.SIGNAL_SCAN, ecmRootDataDTO.getEcmBaseInfoDTO().getOneBatch())) {
            // 单扫特有参数
            params.append("&busiId=").append(dto.getBusiId())
                    .append("&isDefaultShowDialog=0");

            if (ecmRootDataDTO.getEcmBaseInfoDTO().getIsScan().equals(ApiConstants.HAVE_CAPTURE)) {
                // 采集
                params.append("&pageNonce=").append(dto.getNonce());
            } else {
                // 调阅
                params.append("&openType=view")
                        .append("&busIds=").append(ecmRootDataDTO.getEcmBusExtendDTOS().size())
                        .append("&pageNonce=").append(dto.getNonce());
            }
            if (2 == ecmRootDataDTO.getEcmBaseInfoDTO().getIsRetransmission()) {
                params.append("&openType=view");
            }
        } else {
            // 批扫特有参数
            params.append("&isDefaultShowDialog=1")
                    .append("&businessOperateType=0")
                    .append("&pageNonce=").append(dto.getNonce());
        }
        if ( ecmRootDataDTO.getEcmBusExtendDTOS() != null && !ecmRootDataDTO.getEcmBusExtendDTOS().isEmpty()) {
            EcmBusExtendDTO busExtendDTO = ecmRootDataDTO.getEcmBusExtendDTOS().get(0);
//            StringBuilder urlBuilder = new StringBuilder(params);
            if (StrUtil.isNotBlank(busExtendDTO.getDelegateType())) {
                params.append("&delegateType=").append(busExtendDTO.getDelegateType());
            }
            if (StrUtil.isNotBlank(busExtendDTO.getTypeBig())) {
                params.append("&typeBig=").append(busExtendDTO.getTypeBig());
            }
        }
        log.info("scanOrUpdateEcmBase,params : {}",params);
        // 拼接完整URL
        dto.setPageUrl(dto.getPageUrl() + "?sp=" + getUrlEncode(params.toString()));
        return ResultApi.success(dto);
    }

    public ResultApi<EcmPageBaseInfoDTO> scanOrUpdateEcmBaseIe(EcmRootDataDTO ecmRootDataDTO) {
        if (CollectionUtil.isEmpty(ecmRootDataDTO.getEcmBusExtendDTOS())) {
            ecmRootDataDTO.getEcmBaseInfoDTO().setOneBatch(ApiConstants.BATCH_SCAN);
        } else {
            ecmRootDataDTO.getEcmBaseInfoDTO().setOneBatch(ApiConstants.SIGNAL_SCAN);
        }
        //请求客户端
        Map<String, String> map = getConnectBase(ecmRootDataDTO, ApiConstants.ACCESSECM);
        String connect = map.get(ApiConstants.RETURN_RESPONSESTR);
        ResultApi dto1 = JSONObject.parseObject(connect, ResultApi.class);
        String s = JSONObject.toJSONString(dto1.getData());
        EcmPageBaseInfoDTO dto = JSONObject.parseObject(s, EcmPageBaseInfoDTO.class);
//        dto.setAccessToken(map.get(ApiConstants.REQUEST_TOKENSTR));
        dto.setRole(ecmRootDataDTO.getEcmBaseInfoDTO().getRoleCode());
        SunyardConfig bean = BeanUtils.getBean(SunyardConfig.class);
        dto.setAppId(bean.getAppId());
        try {
            //跳转路径解密
            dto.setPageUrl(RsaUtils.decrypt(dto.getPageUrl()));
        } catch (Exception e) {
            log.error("跳转页面加密有误");
        }
        dto.setPageUrl("http://127.0.0.1:3000");
//        dto.setPageUrl("http://127.0.0.1:8040/sunIcms/#/imageContentManagement");
        // 构建基础参数
        StringBuilder params = new StringBuilder();
        params.append("sun=true")
                .append("&token=").append(dto.getAccessToken())
                .append("&role=").append(dto.getRole())
                .append("&appId=").append(dto.getAppId())
//                .append("&flagId=").append(dto.getFlagId())
                .append("&nonce=").append(dto.getNonce())
                .append("&usercode=").append(ecmRootDataDTO.getEcmBaseInfoDTO().getUserCode())
                .append("&username=").append(ecmRootDataDTO.getEcmBaseInfoDTO().getUserName())
                .append("&orgCode=").append(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode())
                .append("&orgName=").append(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgName());

        if (StrUtil.equals(ApiConstants.SIGNAL_SCAN, ecmRootDataDTO.getEcmBaseInfoDTO().getOneBatch())) {
            // 单扫特有参数
            params.append("&busiId=").append(dto.getBusiId())
                    .append("&isDefaultShowDialog=0");

            if (ecmRootDataDTO.getEcmBaseInfoDTO().getIsScan().equals(ApiConstants.HAVE_CAPTURE)) {
                // 采集
                params.append("&pageNonce=").append(dto.getNonce());
            } else {
                // 调阅
                params.append("&openType=view")
                        .append("&busIds=").append(ecmRootDataDTO.getEcmBusExtendDTOS().size())
                        .append("&pageNonce=").append(dto.getNonce());
            }
            if (2 == ecmRootDataDTO.getEcmBaseInfoDTO().getIsRetransmission()) {
                params.append("&openType=view");
            }
        } else {
            // 批扫特有参数
            params.append("&isDefaultShowDialog=1")
                    .append("&businessOperateType=0")
                    .append("&pageNonce=").append(dto.getNonce());
        }

        // 拼接完整URL
        dto.setPageUrl(dto.getPageUrl() + "?" + params.toString());
        return ResultApi.success(dto);
    }

    private <T> String getConnect(T t, String uri) {
        Map<String, String> map = getConnectBase(t, uri);
        return map.get(ApiConstants.RETURN_RESPONSESTR);
    }

    private <T> String getConnect(T t, String uri, int num) {
        Map<String, String> map = getConnectBase(t, uri, num);
        return map.get(ApiConstants.RETURN_RESPONSESTR);
    }

    <T> Map<String, String> getConnectBase(T t, String uri) {
        SunyardConfig bean = BeanUtils.getBean(SunyardConfig.class);
        //请求客户端
        SunApiClient sunApiClient = EcmApiFactoryConfig.getSunApiClient(bean.getAppId(), bean.getAppSecret(), bean.getBaseUrl());
        //请求参数
        SunPost sunPost = new SunPost("application/json", bean.getBaseUrl() + uri, bean.getPrivateKey(), t, bean.getReferer(), bean.getBaseUrl());
        //拿到response对象
        String httpResponseStr = null;
        try {
            httpResponseStr = sunApiClient.executeResponseToString(sunPost);
        } catch (IOException e) {
            log.error("sunApiClient执行异常", e);
        }
        ResultApi dto1 = JSONObject.parseObject(httpResponseStr, ResultApi.class);
        if (!dto1.isSucc()) {
            throw new SunyardException(dto1.getMsg() != null ? dto1.getMsg() : "系统繁忙");
        }
        HashMap<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put(ApiConstants.REQUEST_TOKENSTR, sunApiClient.getAccessToken());
        objectObjectHashMap.put(ApiConstants.RETURN_RESPONSESTR, httpResponseStr);
        return objectObjectHashMap;
    }


    <T> Map<String, String> getConnectBase(T t, String uri, int num) {
        SunyardConfig bean = BeanUtils.getBean(SunyardConfig.class);
        //请求客户端
        SunApiClient sunApiClient = EcmApiFactoryConfig.getSunApiClient(bean.getAppId(), bean.getAppSecret(), bean.getBaseUrl());
        //请求参数
        SunPost sunPost = new SunPost("application/json", bean.getBaseUrl() + uri, bean.getPrivateKey(), t, bean.getReferer(), bean.getBaseUrl());
        //拿到response对象
        String httpResponseStr = null;
        try {
            httpResponseStr = sunApiClient.executeResponseToString(sunPost);
        } catch (IOException e) {
            log.error("sunApiClient执行异常", e);
        }
        ResultApi dto1 = JSONObject.parseObject(httpResponseStr, ResultApi.class);
        if (!dto1.isSucc()) {
            if (dto1.getMsg().equals(ApiConstants.OSS_FAIL_SIZE)) {
                if (num < 3) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    num++;
                    getConnectBase(t, uri, num);
                }
            } else {
                throw new SunyardException(dto1.getMsg() != null ? dto1.getMsg() : "系统繁忙");
            }
        }
        HashMap<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put(ApiConstants.REQUEST_TOKENSTR, sunApiClient.getAccessToken());
        objectObjectHashMap.put(ApiConstants.RETURN_RESPONSESTR, httpResponseStr);
        return objectObjectHashMap;
    }


    <T> Map<String, String> getConnectBaseFormData(T t, String uri) {
        SunyardConfig bean = BeanUtils.getBean(SunyardConfig.class);
        //请求客户端
        SunApiClient sunApiClient = EcmApiFactoryConfig.getSunApiClient(bean.getAppId(), bean.getAppSecret(), bean.getBaseUrl());
        //请求参数
        SunPost sunPost = new SunPost("multipart/form-data", bean.getBaseUrl() + uri, bean.getPrivateKey(), t, bean.getReferer(), bean.getBaseUrl());
        //拿到response对象
        String httpResponseStr = null;
        try {
            httpResponseStr = sunApiClient.executeResponseToString(sunPost);
        } catch (IOException e) {
            log.error("sunApiClient执行异常", e);
        }
        ResultApi dto1 = JSONObject.parseObject(httpResponseStr, ResultApi.class);
        if (!dto1.isSucc()) {
            throw new SunyardException(dto1.getMsg() != null ? dto1.getMsg() : "系统繁忙");
        }
        HashMap<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put(ApiConstants.REQUEST_TOKENSTR, sunApiClient.getAccessToken());
        objectObjectHashMap.put(ApiConstants.RETURN_RESPONSESTR, httpResponseStr);
        return objectObjectHashMap;
    }

    <T> InputStream getConnectFile(T t, String uri, Long rangeStart) {
        SunyardConfig bean = BeanUtils.getBean(SunyardConfig.class);
        //请求客户端
        SunApiClient sunApiClient = EcmApiFactoryConfig.getSunApiClient(bean.getAppId(), bean.getAppSecret(), bean.getBaseUrl());
        //请求参数

        SunPost sunPost = new SunPost(bean.getBaseUrl() + uri, bean.getPrivateKey(), t, bean.getReferer(), bean.getBaseUrl());
        sunPost.setHeader("Range", "byte=" + rangeStart + "-");
        try {
            CloseableHttpResponse execute = sunApiClient.executeResponse(sunPost);
            return execute.getEntity().getContent();
        } catch (IOException e) {
            log.error("sunApiClient执行异常", e);
        }
        return null;
    }

    /**
     * 影像查询
     *
     * @param ecmRootQueryDTO 查询入参
     * @return BusiInfoAndFileVO
     */
    @Override
    public ResultApi<List<QueryDataVO>> queryData(@RequestBody EcmRootDataDTO ecmRootQueryDTO) {
        SunyardConfig bean = BeanUtils.getBean(SunyardConfig.class);
        Map<String, String> map = getConnectBase(ecmRootQueryDTO, ApiConstants.QUERYECM);
        String connect = map.get(ApiConstants.RETURN_RESPONSESTR);
        String token = map.get(ApiConstants.REQUEST_TOKENSTR);

        ResultApi dto1 = JSONObject.parseObject(connect, ResultApi.class);
        String s = JSONObject.toJSONString(dto1.getData());
        log.debug(s);
        List<QueryDataVO> busiInfoAndFileVOS = JSONArray.parseArray(s, QueryDataVO.class);
        for (QueryDataVO vo : busiInfoAndFileVOS) {
            if (CollectionUtil.isEmpty(vo.getFileInfoRedisEntities())) {
                continue;
            }
            vo.getFileInfoRedisEntities().forEach(m -> {
                m.setFileFullPath(m.getFileFullPath() + "&token=" + token + "&appid=" + bean.getAppId());
                m.setSourceFileDownloadPath(m.getSourceFileDownloadPath() + "&token=" + token + "&appid=" + bean.getAppId());
//                m.setFileFullPathCacheThumbnail(m.getFileFullPathCacheThumbnail()+"&token= "+token+"&appid="+bean.getAppId());
            });
        }
        return ResultApi.success(busiInfoAndFileVOS);
    }

    @Override
    public ResultApi busiDocDuplicate(BusiDocDuplicateVO fileInfoRedisEntityVo) {
        String connect = getConnect(fileInfoRedisEntityVo, ApiConstants.BUSIDOCDUPLICATE);
        ResultApi resultApi = JSONObject.parseObject(connect, ResultApi.class);
        return resultApi;
    }


    @Override
    public ResultApi busiArchive(QueryBusiDTO queryBusiDTO) {
        String connect = getConnect(queryBusiDTO, ApiConstants.BUSIARCHIVE);
        ResultApi resultApi = JSONObject.parseObject(connect, ResultApi.class);
        return resultApi;
    }

    /**
     * 影像删除
     *
     * @param vo 删除入参
     */
    @Override
    public ResultApi deleteFile(@RequestBody EcmDelVO vo) {
        String connect = getConnect(vo, ApiConstants.DELETEFILE);
        ResultApi resultApi = JSONObject.parseObject(connect, ResultApi.class);
        return resultApi;
    }


    /**
     * 是否允许上传
     *
     * @param dto
     * @return
     */
    ResultApi checkFile(EcmUploadAllDTO dto) {
        String connect = getConnect(dto, ApiConstants.CHECKFILE);
        return JSONObject.parseObject(connect, ResultApi.class);
    }


    /**
     * 获取上传进度
     *
     * @param md5
     * @param isEncrypt
     * @return
     */
    ResultApi<SplitUploadDTO> taskInfo(String md5, Integer isEncrypt) {
        SplitUploadBigFileDTO splitUploadBigFileDTO = new SplitUploadBigFileDTO();
        splitUploadBigFileDTO.setIdentifier(md5);
        splitUploadBigFileDTO.setIsEncrypt(isEncrypt);
        String connect = getConnect(splitUploadBigFileDTO, ApiConstants.SPLITUPLOAD);
        ResultApi resultApi = JSONObject.parseObject(connect, ResultApi.class);
        SplitUploadDTO splitUploadDTO = new SplitUploadDTO();
        if (resultApi.isSucc() && resultApi.getData() != null) {
            Object data = resultApi.getData();
            splitUploadDTO = JSONObject.parseObject(data.toString(), SplitUploadDTO.class);
        }
        return ResultApi.success(splitUploadDTO);
    }

    /**
     * 获取上传信息（上传进度、预签名上传url、文件信息）
     *
     * @param ecmFileInfoDTO
     * @param fileDTO
     * @return
     */
    ResultApi<SplitUploadDTO> getUploadInfo(EcmFileInfoDTO ecmFileInfoDTO, FileDTO fileDTO) {
        SplitUploadVO param = new SplitUploadVO();
        param.setIdentifier(fileDTO.getSourceFileMd5());
        param.setEquipmentId(ecmFileInfoDTO.getEquipmentId());
        param.setBusiBatchNo(ecmFileInfoDTO.getBusiBatchNo());
        param.setFileName(fileDTO.getFile().getName());
        param.setTotalSize(fileDTO.getFile().getSize());
        param.setChunkSize(5 * 1024 * 1024L);
        param.setSourceFileMd5(fileDTO.getSourceFileMd5());
        //加密
        param.setIsEncrypt(ecmFileInfoDTO.getIsEncrypt() == null ? 0 : ecmFileInfoDTO.getIsEncrypt());
        log.info("分片加密参数：{}", param.getIsEncrypt());
        //区分移动端和pc端，移动端传值为0，pc端传值为1
        param.setType("1");
        //是否是对外接口
        param.setIsOpen(true);
        //使用配置时用户信息
        param.setUserId(0L);
        param.setFileSource("SDK");
        String connect = getConnect(param, ApiConstants.SPLITUPLOADINFO);
        ResultApi resultApi = JSONObject.parseObject(connect, ResultApi.class);
        SplitUploadDTO splitUploadDTO = new SplitUploadDTO();
        if (resultApi.isSucc() && resultApi.getData() != null) {
            Object data = resultApi.getData();
            splitUploadDTO = JSONObject.parseObject(data.toString(), SplitUploadDTO.class);
        }
        return ResultApi.success(splitUploadDTO);
    }

    /**
     * 创建一个上传任务
     *
     * @param param
     * @return
     */
    ResultApi<SplitUploadDTO> initTask(EcmSplitFileDTO param) {
        SplitUploadDTO splitUploadDTO = null;
        String connect = getConnect(param, ApiConstants.INITASK);
        ResultApi resultApi = JSONObject.parseObject(connect, ResultApi.class);
        if (resultApi.isSucc() && resultApi.getData() != null) {
            Object data = resultApi.getData();
            splitUploadDTO = JSONUtil.toBean(data.toString(), SplitUploadDTO.class);

        }
        return ResultApi.success(splitUploadDTO);
    }

    /**
     * 获取每个分片的预签名上传地址
     *
     * @param identifier
     * @param partNumber
     * @param id
     * @return
     */
    ResultApi<String> preSignUploadUrl(String identifier, Integer partNumber, Long id) {
        Map<String, Object> param = new HashMap();
        param.put("identifier", identifier);
        param.put("partNumber", partNumber);
        param.put("type", "1");
        param.put("id", id);
        String connect = getConnect(param, ApiConstants.PRESIGNUPLOADURL);
        ResultApi resultApi = JSONObject.parseObject(connect, ResultApi.class);
        if (resultApi.isSucc() && resultApi.getData() != null) {
            Object data = resultApi.getData();
            return ResultApi.success(data.toString());
        }
        return ResultApi.success(null);
    }

    /**
     * 合并分片
     *
     * @param identifier
     * @param id
     * @return
     */
    ResultApi<String> merge(String identifier, Long id, Long equipmentId) {
        Map<String, Object> param = new HashMap();
        param.put("identifier", identifier);
        param.put("isFlat", false);
        param.put("id", id);
        param.put("equipmentId", equipmentId);
        String connect = getConnect(param, ApiConstants.MERGE, ApiConstants.AGAIN_NUM);
        ResultApi resultApi = JSONObject.parseObject(connect, ResultApi.class);
        if (resultApi.isSucc() && resultApi.getData() != null) {
            Object data = resultApi.getData();
            return ResultApi.success(data.toString());
        }
        return ResultApi.success(null);
    }

    /**
     * 插入文件信息
     *
     * @param ecmFileInfoApiDTO
     * @return
     */
    ResultApi insertFileInfo(EcmFileInfoApiDTO ecmFileInfoApiDTO) {
        String connect = getConnect(ecmFileInfoApiDTO, ApiConstants.INSERTFILEINFOBACK);
        return ResultApi.success(connect);
    }

    /**
     * 文件分片
     *
     * @return Boolean
     */
    @Override
    public ResultApi splitFile(UploadAllDTO splitDTO) {
        //若资料节点下无文件，则排除该资料节点
        List<UploadFileDTO> splitDTO1 = splitDTO.getSplitDTO();
        List<UploadFileDTO> filteredData = new ArrayList<>();
        for (UploadFileDTO item : splitDTO1) {
            List<FileAndSortDTO> fileAndSortDTOS = item.getFileAndSortDTOS();
            boolean containsFile1 = false;
            for (FileAndSortDTO subitem : fileAndSortDTOS) {
                if (subitem.getMultipartFile() != null) {
                    containsFile1 = true;
                    break;
                }
            }
            if (containsFile1) {
                filteredData.add(item);
            }
        }
        splitDTO.setSplitDTO(filteredData);

        EcmBusExtendDTO ecmBusExtendDTOS = splitDTO.getEcmRootDataDTO().getEcmBusExtendDTOS();
        //判断该业务是否压缩
        String isCompress = splitDTO.getEcmRootDataDTO().getEcmBusExtendDTOS().getIsCompress();
        if (StrUtil.isNotBlank(isCompress) && isCompress.equals(ApiConstants.COMPRESS.toString())) {
            //获取业务类型压缩配置
            ecmBusExtendDTOS = getCompressParams(splitDTO.getEcmRootDataDTO().getEcmBusExtendDTOS());
        } else {
            ecmBusExtendDTOS.setIsCompress(ApiConstants.NOCOMPRESS.toString());
        }
        splitDTO.getEcmRootDataDTO().setEcmBusExtendDTOS(ecmBusExtendDTOS);
        List<EcmBusiFileInfoDTO> result = Collections.synchronizedList(new ArrayList<>());
        for (UploadFileDTO dto : splitDTO.getSplitDTO()) {
            EcmBusiFileInfoDTO ecmBusiFileInfoDTO = checkBusiAndFile(dto, splitDTO);
            result.add(ecmBusiFileInfoDTO);
        }
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<SysFileApiDTO> succ = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(result)) {
            //重复文件信息返回
            for (EcmBusiFileInfoDTO ecmBusiFileInfoDTO : result) {
                if (CollectionUtil.isNotEmpty(ecmBusiFileInfoDTO.getRepeatFileMd5List())) {
                    Map<String, Object> map = new HashMap<>();
                    List<String> fileNameList = null;
                    String docCode = ecmBusiFileInfoDTO.getEcmFileInfoDTO().getDocCode();
                    List<FileDTO> repeatFileList = ecmBusiFileInfoDTO.getRepeatFileMd5List();
                    fileNameList = repeatFileList.stream().map(FileDTO::getFile).map(MultipartFile::getOriginalFilename)
                            .collect(Collectors.toList());
                    map.put("docCode", docCode);
                    map.put("repeatFile", fileNameList);
                    resultList.add(map);
                }
                succ.addAll(ecmBusiFileInfoDTO.getSaveFileSucc());
            }
        }
        Map<String, Object> mapRet = new HashMap<>();
        mapRet.put("succFile", succ);
        mapRet.put("errorFile", resultList);
        if (CollectionUtil.isNotEmpty(resultList)) {
            return ResultApi.success(JSONUtil.toJsonStr(mapRet), ResultCodeApiEnum.BUSI_FILEREPEAT);
        }
        return ResultApi.success(JSONUtil.toJsonStr(mapRet), ResultCodeApiEnum.SUCCESS);

    }

    /**
     * 文件校验
     *
     * @param splitDTO
     * @param dto
     * @return
     */
    private EcmBusiFileInfoDTO checkBusiAndFile(UploadFileDTO dto, UploadAllDTO splitDTO) {
        EcmBusiFileInfoDTO ecmBusiFileInfoDTO = new EcmBusiFileInfoDTO();
        EcmBusExtendDTO ecmBusExtendDTOS = splitDTO.getEcmRootDataDTO().getEcmBusExtendDTOS();
        //计算文件的md5,并压缩文件
        Map<String, FileAndSortDTO> md5s = FileUploadSplitUtils.getMd5(dto.getFileAndSortDTOS(), ecmBusExtendDTOS);
        Set<String> strings = md5s.keySet();
        dto.setMd5List(strings);
        dto.setMd5s(md5s);
        Set<String> strings1 = md5s.keySet();
        if (strings1.size() != dto.getFileAndSortDTOS().size()) {
            throw new RuntimeException("同一批次存在相同文件");
        }
        EcmUploadAllDTO ecmUploadAllDTO = BeanUtil.copyProperties(splitDTO, EcmUploadAllDTO.class);
        ecmUploadAllDTO.setDocNo(dto.getDocNo());
        ecmUploadAllDTO.setEcmRootDataDTO(splitDTO.getEcmRootDataDTO());
        //校验文件是否允许上传
        //1、包含业务不存在新建业务
        //2、静态树获取角色对应资料权限，判断是否有新增权限，获取资料允许上传文件类型，判断传入文件是否符合该类型
        //3、动态树获取传入权限，判断是否有新增权限，默认给全部文件类型
        Map<String, Object> checkByFile = getCheckByFile(ecmUploadAllDTO);
        if (CollectionUtil.isNotEmpty(checkByFile)) {
            EcmFileInfoDTO ecmFileInfoDTO = (EcmFileInfoDTO) checkByFile.get("ecmFileInfoDTO");
            if (ecmFileInfoDTO != null) {
                ecmFileInfoDTO.setCreateUser(splitDTO.getEcmRootDataDTO().getEcmBaseInfoDTO().getUserCode());
                ecmFileInfoDTO.setCreateUserName(splitDTO.getEcmRootDataDTO().getEcmBaseInfoDTO().getUserName());
                ecmFileInfoDTO.setOrgCode(splitDTO.getEcmRootDataDTO().getEcmBaseInfoDTO().getOrgCode());
                ecmFileInfoDTO.setOrgName(splitDTO.getEcmRootDataDTO().getEcmBaseInfoDTO().getOrgName());
                //业务批次号
                ecmFileInfoDTO.setBusiBatchNo(UUID.fastUUID().toString());
            } else {
                throw new RuntimeException("业务信息有误");
            }
            //允许上传的文件md5列表
            List<FileDTO> matchFileList = (List<FileDTO>) checkByFile.get("matchFileList");
            List<FileDTO> repeatFileList = (List<FileDTO>) checkByFile.get("repeatFile");
            ArrayList<FileAndSortDTO> fileAndSortDTOS = new ArrayList<>();
            repeatFileList.forEach(s -> {
                s.setFile(md5s.get(s.getSourceFileMd5()).getMultipartFile());
            });
            //文件上传
            List<SysFileApiDTO> files = fileUploadToStorage(ecmFileInfoDTO, matchFileList, md5s);
            //关联文件标签
            Map<String, List<FileDTO>> collect = matchFileList.stream().collect(Collectors.groupingBy(FileDTO::getFileMd5));
            files.forEach(sysFileApiDTO -> {
                List<FileDTO> fileDTOS = collect.get(sysFileApiDTO.getFileMd5());
                if (CollectionUtil.isNotEmpty(fileDTOS.get(0).getFileLabels())) {
                    sysFileApiDTO.setFileLabels(fileDTOS.get(0).getFileLabels());
                }
            });
            //文件关联业务
            associationBusi(files, ecmFileInfoDTO, splitDTO.getEcmRootDataDTO());
            ecmBusiFileInfoDTO.setRepeatFileMd5List(repeatFileList);
            ecmBusiFileInfoDTO.setEcmFileInfoDTO(ecmFileInfoDTO);
            ecmBusiFileInfoDTO.setSaveFileSucc(files);
        }
        return ecmBusiFileInfoDTO;
    }

    /**
     * 文件上传到存储服务
     *
     * @param matchFileList
     * @param md5s
     * @return
     */
    private List<SysFileApiDTO> fileUploadToStorage(EcmFileInfoDTO ecmFileInfoDTO, List<FileDTO> matchFileList, Map<String, FileAndSortDTO> md5s) {
        List<SysFileApiDTO> resultList = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(matchFileList.size());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        long time = System.currentTimeMillis();
        for (int i = 0; i < matchFileList.size(); i++) {
            FileDTO fileDTO = matchFileList.get(i);
            FileAndSortDTO fileAndSortDTO = md5s.get(fileDTO.getFileMd5());
            fileDTO.setFile(fileAndSortDTO.getMultipartFile());
            if (fileDTO.getFile() == null) {
                latch.countDown();
                continue;
            }
            long length = fileDTO.getFile().getSize();
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                if (length < 5 * 1024 * 1024) {
//                    log.info("=====不分片======");
                    SysFileApiDTO sysFileApiDTO = processSingleFileUpload(ecmFileInfoDTO, fileDTO);
                    resultList.add(sysFileApiDTO);
                } else {
                    log.info("=====走分片======");
                    try {
                        SplitUploadDTO splitUploadDTO = getUploadInfo(ecmFileInfoDTO, fileDTO).getData();
                        SplitUploadRecordDTO taskRecord = splitUploadDTO.getTaskRecord();
                        if (splitUploadDTO.getFinished()) {
                            taskRecord.setFileDocSort(time + "_" + fileDTO.getFileSort());
                        } else {
                            List<String> urlList = splitUploadDTO.getUrlList();
                            uploadSplit(taskRecord, urlList, fileDTO.getFile(), ecmFileInfoDTO.getEquipmentId());
                            merge(taskRecord.getSourceFileMd5(), taskRecord.getId(), ecmFileInfoDTO.getEquipmentId());
                            taskRecord.setFileDocSort(time + "_" + fileDTO.getFileSort());
                            SysFileApiDTO sysFileDTO = BeanUtil.copyProperties(taskRecord, SysFileApiDTO.class);
                            resultList.add(sysFileDTO);
                        }
                    } catch (Exception e) {
                        log.error("文件上传存储服务失败", e);
                    }
                }
                latch.countDown();
            }, getTaskExecutor());
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("sunApiClient执行异常", e);
        }
        return resultList;
    }


    /**
     * @param ecmFileInfoDTO
     * @param fileDTO
     * @return
     */
    private SysFileApiDTO processSingleFileUpload(EcmFileInfoDTO ecmFileInfoDTO, FileDTO fileDTO) {
        FileUploadDTO fileUploadDTO = new FileUploadDTO();
        fileUploadDTO.setFile(fileDTO.getFile());
        fileUploadDTO.setFileName(fileDTO.getFile().getOriginalFilename());
        fileUploadDTO.setStEquipmentId(ecmFileInfoDTO.getEquipmentId());
        fileUploadDTO.setUserId(0L);
        fileUploadDTO.setFileSource("OpenApi");
        fileUploadDTO.setMd5(fileDTO.getFileMd5());
        //是否加密
        fileUploadDTO.setIsEncrypt(ecmFileInfoDTO.getIsEncrypt() == null ? 0 : ecmFileInfoDTO.getIsEncrypt());
//            log.info("不分片加密参数：{}", fileUploadDTO.getIsEncrypt());
        Map<String, String> connectBaseFormData = getConnectBaseFormData(fileUploadDTO, ApiConstants.FILEUPLOAD);
        String responseStr = connectBaseFormData.get("responseStr");
        ResultApi resultApi = JSONObject.parseObject(JSONUtil.toJsonStr(responseStr), ResultApi.class);

        if (resultApi.isSucc() && resultApi.getData() != null) {
            Object data = resultApi.getData();
            return JSONObject.parseObject(data.toString(), SysFileApiDTO.class);
        }

        return null;
    }

    /**
     * 获取业务全局配置
     *
     * @param ecmBusExtendDTOS
     * @return
     */
    private EcmBusExtendDTO getCompressParams(EcmBusExtendDTO ecmBusExtendDTOS) {
        String connect = getConnect(ecmBusExtendDTOS, ApiConstants.APPTYPEINFO);
        ResultApi resultApi = JSONObject.parseObject(connect, ResultApi.class);
        Object data = resultApi.getData();
        EcmAppDefVO ecmAppDefVO = JSONObject.parseObject(data.toString(), EcmAppDefVO.class);
        //是否压缩
        Integer isQulity = ecmAppDefVO.getIsQulity();
        //压缩比(大小)
        Integer resiz = ecmAppDefVO.getResiz();
        //压缩质量
        Float qulity = ecmAppDefVO.getQulity();
        ecmBusExtendDTOS.setIsCompress(isQulity.toString());
        ecmBusExtendDTOS.setCompressSize(resiz.toString());
        ecmBusExtendDTOS.setCompressValue(qulity.toString());
        return ecmBusExtendDTOS;
    }


    /**
     * 文件下载
     *
     * @return BusiInfoAndFileVO
     */
    private InputStream downloadFileCon(EcmDownloadByFileIdDTO fileId, EcmDownloadFileDTO ecmDownloadFileDTO, Long rangeStart) {
        Map map = new HashMap();
        List<Long> list = new ArrayList<>();
        list.add(fileId.getNewFileId());
        map.put("fileId", list);
        // map.put("fileId", fileId.getId());
        map.put("userName", ecmDownloadFileDTO.getEcmBaseInfoDTO().getUserName());
        map.put("instName", ecmDownloadFileDTO.getEcmBaseInfoDTO().getOrgName());
        //非打包方式
        map.put("isPack", 0);
        //非断点下载方式
        // InputStream connectFile = getConnectFile(map, ApiConstants.FILEDOWNLOAD,null);
        //断点下载方式
        InputStream connectFile = getConnectFile(map, ApiConstants.SHARDINGDOWNLOAD, rangeStart);
        return connectFile;
    }


    /**
     * 文件下载
     *
     * @return BusiInfoAndFileVO
     */
    private Map getFilesByBusiOrDoc(EcmDownloadFileDTO ecmDownloadFileDTO) {
        String connect = getConnect(ecmDownloadFileDTO, ApiConstants.FILELIST);
        ResultApi dto1 = JSONObject.parseObject(connect, ResultApi.class);
        String s = JSONObject.toJSONString(dto1.getData());
        Map map = JSONObject.parseObject(s, Map.class);
        return map;
    }


    @Override
    public ResultApi downloadFile(EcmDownloadFileDTO ecmDownloadFileDTO) {
        //判空
        if (ObjectUtils.isEmpty(ecmDownloadFileDTO.getAppCode())) {
            return ResultApi.error("业务类型不能为空", ResultCodeApiEnum.PARAM_ERROR);
        }
        //判空
        if (ObjectUtils.isEmpty(ecmDownloadFileDTO.getBusiNo())) {
            return ResultApi.error("业务编号不能为空", ResultCodeApiEnum.PARAM_ERROR);
        }

        if (ObjectUtils.isEmpty(ecmDownloadFileDTO.getPath())) {
            return ResultApi.error("下载的位置不能为空", ResultCodeApiEnum.PARAM_ERROR);
        }
        //根据业务编号获取所有的文件id；
        Map map = getFilesByBusiOrDoc(ecmDownloadFileDTO);
        String appname = (String) map.get("appname");
        String list = (String) map.get("fileList");
        List<EcmDownloadByFileIdDTO> filesByBusiOrDoc = JSONArray.parseArray(list, EcmDownloadByFileIdDTO.class);
        if (CollectionUtil.isEmpty(filesByBusiOrDoc)) {
            return ResultApi.error("业务无对应文件", ResultCodeApiEnum.DATA_FAILED);
        }

        return getDownloadFileReturn(ecmDownloadFileDTO, ecmDownloadFileDTO.getBusiNo(), filesByBusiOrDoc, appname);

    }

    @Override
    public ResultApi setBusiAttr(EditBusiAttrDTO editBusiAttrDTO) {
        String connect = getConnect(editBusiAttrDTO, ApiConstants.ATTRLIST);
        ResultApi dto1 = JSONObject.parseObject(connect, ResultApi.class);
        return dto1;
    }

    @Override
    public ResultApi busiDeblock(EditBusiAttrDTO editBusiAttrDTO) {
        String connect = getConnect(editBusiAttrDTO, ApiConstants.EDITBUSISTATUS);
        ResultApi dto1 = JSONObject.parseObject(connect, ResultApi.class);
        return dto1;
    }

    @Override
    public ResultApi<List<QueryBusiInfoVO>> queryBusi(QueryBusiDTO queryBusiDTO) {
        SunyardConfig bean = BeanUtils.getBean(SunyardConfig.class);
        Map<String, String> map = getConnectBase(queryBusiDTO, ApiConstants.QUERYBUSI);
        String connect = map.get(ApiConstants.RETURN_RESPONSESTR);
        String token = map.get(ApiConstants.REQUEST_TOKENSTR);

        ResultApi dto1 = JSONObject.parseObject(connect, ResultApi.class);
        String s = JSONObject.toJSONString(dto1.getData());
        log.debug(s);
        List<QueryBusiInfoVO> busiInfoAndFileVOS = JSONArray.parseArray(s, QueryBusiInfoVO.class);
        for (QueryBusiInfoVO vo : busiInfoAndFileVOS) {
            if (CollectionUtil.isEmpty(vo.getFileInfoRedisEntities())) {
                continue;
            }
            vo.getFileInfoRedisEntities().forEach(m -> {
                m.setFilePath(m.getFilePath() + "&token=" + token + "&appid=" + bean.getAppId());
            });
        }
        return ResultApi.success(busiInfoAndFileVOS);
    }

    /**
     * ocr全文识别文本内容回传接口
     *
     * @param fileOcrCallBackDTO
     * @return
     */
    @Override
    public ResultApi textCallback(FileOcrCallBackDTO fileOcrCallBackDTO) {
        try {
            if (ObjectUtils.isEmpty(fileOcrCallBackDTO.getFileOcrDtos())) {
                ResultApi.error("带查重文件列表不能为空", ResultCodeApiEnum.PARAM_ERROR);
            }
            if (ObjectUtils.isEmpty(fileOcrCallBackDTO.getAppCode())) {
                ResultApi.error("业务类型代码不能为空", ResultCodeApiEnum.PARAM_ERROR);
            }
            if (ObjectUtils.isEmpty(fileOcrCallBackDTO.getBusiNo())) {
                ResultApi.error("业务主索引不能为空", ResultCodeApiEnum.PARAM_ERROR);
            }
            if (ObjectUtils.isEmpty(fileOcrCallBackDTO.getEcmBaseInfoDTO())) {
                ResultApi.error("操作人员基本信息不能为空", ResultCodeApiEnum.PARAM_ERROR);
            }

            getConnectBase(fileOcrCallBackDTO, ApiConstants.TEXTANTIFRAUDDET);
            return ResultApi.success(null);
        } catch (Exception e) {
            log.error("sunApiClient执行异常", e);
            return ResultApi.error(e.getMessage(), ResultCodeApiEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 影像业务校验
     *
     * @param ecmBusiInfoDataDTO
     * @return
     */
    @Override
    public ResultApi ecmBusiInfoCheck(EcmBusiInfoDataDTO ecmBusiInfoDataDTO) {
        try {
            if (ObjectUtils.isEmpty(ecmBusiInfoDataDTO.getEcmUserDTO())) {
                ResultApi.error("操作人员基本信息不能为空", ResultCodeApiEnum.PARAM_ERROR);
            }
            EcmBusiInfoDTO ecmBusiInfoDTO = ecmBusiInfoDataDTO.getEcmBusiInfoDTO();
            if (ObjectUtils.isEmpty(ecmBusiInfoDTO)) {
                ResultApi.error("业务信息不能为空", ResultCodeApiEnum.PARAM_ERROR);
            }

            if (ObjectUtils.isEmpty(ecmBusiInfoDTO.getAppCode())) {
                ResultApi.error("业务类型代码不能为空", ResultCodeApiEnum.PARAM_ERROR);
            }
            if (ObjectUtils.isEmpty(ecmBusiInfoDTO.getBusiNo())) {
                ResultApi.error("业务主索引不能为空", ResultCodeApiEnum.PARAM_ERROR);
            }
            Map<String, String> map = getConnectBase(ecmBusiInfoDataDTO, ApiConstants.ECMBUSIINFOCHECK);

            String connect = map.get(ApiConstants.RETURN_RESPONSESTR);
            ResultApi dto = JSONObject.parseObject(connect, ResultApi.class);
            String s = JSONObject.toJSONString(dto.getData());
            log.info("影像业务校验结果:{}", s);
            return dto;
        } catch (Exception e) {
            log.error("影像业务校验出现异常", e);
            return ResultApi.error(e.getMessage(), ResultCodeApiEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 影像资料统计
     *
     * @param ecmBusiInfoDataDTO
     * @return
     */
    @Override
    public ResultApi statisticsDocFileNUm(EcmBusiInfoDataDTO ecmBusiInfoDataDTO) {
        try {
            if (ObjectUtils.isEmpty(ecmBusiInfoDataDTO.getEcmUserDTO())) {
                ResultApi.error("操作人员基本信息不能为空", ResultCodeApiEnum.PARAM_ERROR);
            }
            EcmBusiInfoDTO ecmBusiInfoDTO = ecmBusiInfoDataDTO.getEcmBusiInfoDTO();
            if (ObjectUtils.isEmpty(ecmBusiInfoDTO)) {
                ResultApi.error("业务信息不能为空", ResultCodeApiEnum.PARAM_ERROR);
            }

            if (ObjectUtils.isEmpty(ecmBusiInfoDTO.getAppCode())) {
                ResultApi.error("业务类型代码不能为空", ResultCodeApiEnum.PARAM_ERROR);
            }
            if (ObjectUtils.isEmpty(ecmBusiInfoDTO.getBusiNo())) {
                ResultApi.error("业务主索引不能为空", ResultCodeApiEnum.PARAM_ERROR);
            }
            Map<String, String> map = getConnectBase(ecmBusiInfoDataDTO, ApiConstants.STATISTICSDOCFILENUM);
            String connect = map.get(ApiConstants.RETURN_RESPONSESTR);
            ResultApi dto = JSONObject.parseObject(connect, ResultApi.class);
            String s = JSONObject.toJSONString(dto.getData());
            log.info("影像资料统计结果:{}", s);
            return dto;
        } catch (Exception e) {
            log.error("影像资料统计出现异常", e);
            return ResultApi.error(e.getMessage(), ResultCodeApiEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 下载返回值处理
     *
     * @param ecmDownloadFileDTO
     * @param l
     * @param filesByBusiOrDoc
     * @param appname
     * @return
     */
    private ResultApi getDownloadFileReturn(EcmDownloadFileDTO ecmDownloadFileDTO, String l, List<EcmDownloadByFileIdDTO> filesByBusiOrDoc, String appname) {
        try {
            String folderToCompress = ecmDownloadFileDTO.getPath() + "/" + l;
            File file2 = new File(folderToCompress);
            if (file2.exists()) {
                FileUtils.deleteDirectory(file2);

            }
            CountDownLatch latch = new CountDownLatch(filesByBusiOrDoc.size());
            for (EcmDownloadByFileIdDTO file : filesByBusiOrDoc) {
                Executor taskExecutor = BeanUtils.getBean(Executor.class);
                taskExecutor.execute(() -> {
                    downloadFileById(file, ecmDownloadFileDTO.getPath() + "/" + l, ecmDownloadFileDTO, latch);
                    latch.countDown();

                });
            }

            latch.await();

            FileZipUtils.printXmlByDownload(ecmDownloadFileDTO, filesByBusiOrDoc, ecmDownloadFileDTO.getPath() + "/" + l + "/busiDownload.xml", appname);
            //所有线程都执行完毕，进行打包
            if (ecmDownloadFileDTO.getIsPack().equals(ApiConstants.IS_PACK)) {

                //打包
                String zipFileName = ecmDownloadFileDTO.getPath() + "/" + l + ".zip";
                File file = new File(zipFileName);
                if (file.exists()) {
                    com.sunyard.framework.common.util.FileUtils.deleteFile(zipFileName);
                }
                //打成压缩包
                ZipUtils.toZip(folderToCompress, zipFileName);
                //此处为了兼容打压缩包也断点下载，所以不删除文件夹
                FileUtils.deleteDirectory(new File(folderToCompress));
            }
            //封装返回值
            return ResultApi.success(null);
        } catch (Exception e) {
            log.error("下载文件失败", e);
            return ResultApi.error(e.getMessage(), ResultCodeApiEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 异步下载文件
     *
     * @param file
     * @param path
     * @param ecmDownloadFileDTO
     * @param latch
     */
    private void downloadFileById(EcmDownloadByFileIdDTO file, String path, EcmDownloadFileDTO ecmDownloadFileDTO, CountDownLatch latch) {
        String originalFilename = file.getNewFileName();
        String fileNameWithoutExtension = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        // 已经下载文件长度
        long alreadySize = 0;
        // 将文件写到download/file.apk中
        File existFile = new File(path + "/" + fileNameWithoutExtension + "_" + file.getNewFileId() + fileExtension);
        // 如果存在，说明原来下载过，不过可能没有下载完
        if (existFile.exists()) {
            // 如果文件存在，就获取当前文件的大小
            alreadySize = existFile.length();
        }
        //Range开始范围
        Long rangeStart = alreadySize;
        //下载，rangeStart为断点下载的开始大小
        InputStream inputStream = downloadFileCon(file, ecmDownloadFileDTO, rangeStart);
        File file1 = new File(path);
        if (!file1.exists()) {
            file1.mkdirs();
        }
//        //存入指定磁盘
        File targetFile = generateTargetFile(path + "/", file.getNewFileName(), file.getNewFileId());
        FileUploadSplitUtils.inputStreamToFile(inputStream, targetFile);
    }


    /**
     * 文件名处理
     *
     * @param path
     * @param originalFileName
     * @return
     */
    //todo 全局方法？
    public static File generateTargetFile(String path, String originalFileName, Long fileId) {
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String newFileName = fileId + fileExtension;
        return new File(path + "/" + newFileName);
    }


    /**
     * 文件上传校验
     *
     * @param splitDTO
     * @return
     */
    private Map<String, Object> getCheckByFile(EcmUploadAllDTO splitDTO) {
        Map<String, Object> map = new HashMap<>();
        //调用ecm文件校验
        ResultApi resultApi = checkFile(splitDTO);
        List<FileDTO> noRightFileTypeList = new ArrayList<>();
        List<FileDTO> repeatFile = new ArrayList<>();
        List<FileDTO> matchFileList = new ArrayList<>();
        EcmFileInfoDTO ecmFileInfoDTO = new EcmFileInfoDTO();
        Object data = resultApi.getData();
        EcmBusiFileInfoDTO resultData = JSONObject.parseObject(data.toString(), EcmBusiFileInfoDTO.class);
        ecmFileInfoDTO = resultData.getEcmFileInfoDTO();
        repeatFile = resultData.getRepeatFileMd5List();
        noRightFileTypeList = resultData.getFileTypeNoRightList();
        matchFileList = resultData.getMatchFileList();
        map.put("ecmFileInfoDTO", ecmFileInfoDTO);
        map.put("noRightFileTypeList", noRightFileTypeList);
        map.put("repeatFile", repeatFile);
        map.put("matchFileList", matchFileList);
        return map;
    }

    /**
     * @param taskRecord 已上传的分片信息
     * @param urlList    所有分片的上传地址
     * @param file       文件
     */
    private void uploadSplit(SplitUploadRecordDTO taskRecord, List<String> urlList, MultipartFile file, Long equipmentId) {
        //已上传的分片信息
        //分片大小
        Long chunkSize = taskRecord.getChunkSize();
        CountDownLatch latch = new CountDownLatch(urlList.size());
//        for (Integer partNumber = 1; partNumber <= chunkNum; partNumber++) {
//            if (partNumberList.contains(partNumber)) {
//                //该分片已经上传过
//            } else {
//                //没上传过的分片进行上传
//                //1、获取上传路径
//                String url = urlList.get(partNumber-1);
//                //2、请求得到的上传路径将分片上传
//                Integer finalPartNumber = partNumber;
//                getTaskExecutor().execute(() -> {
//                    upload(url, file, finalPartNumber, chunkSize,equipmentId,taskRecord);
//                    latch.countDown();
//                });
//            }
//        }
        for (String s : urlList) {
            getTaskExecutor().execute(() -> {
                String partNumber = FileUploadSplitUtils.getParameterValue(s, "partNumber");
                Integer finalPartNumber = Integer.parseInt(partNumber);
                upload(s, file, finalPartNumber, chunkSize, equipmentId, taskRecord);
                latch.countDown();
            });
        }
        try {
            // 等待所有任务执行完毕
            latch.await();
        } catch (InterruptedException e) {
            log.error("分片上传失败", e);
        }
    }


    /**
     * 文件分片上传
     *
     * @param url
     * @param file
     * @param partNumber
     * @param chunkSize
     * @param equipmentId
     * @param taskRecord
     */
    private void upload(String url, MultipartFile file, Integer partNumber, Long chunkSize, Long equipmentId, SplitUploadRecordDTO taskRecord) {
        try {
            byte[] bytes = fileSplit(file, partNumber, chunkSize);
//            InputStream inputStream = new ByteArrayInputStream(bytes);
            SplitUploadVO splitUploadVO = new SplitUploadVO();
            splitUploadVO.setEquipmentId(equipmentId);
//            splitUploadVO.setInputStream(inputStream);
            splitUploadVO.setPartNumber(partNumber);
            splitUploadVO.setFileName(taskRecord.getFilename());
            splitUploadVO.setFileId(taskRecord.getId());
            splitUploadVO.setBusiBatchNo(taskRecord.getBusiBatchNo());
            splitUploadVO.setKey(taskRecord.getObjectKey());
            splitUploadVO.setPartSize(Long.valueOf(bytes.length));
            splitUploadVO.setUploadId(taskRecord.getUploadId());
            splitUploadVO.setIdentifier(taskRecord.getFileMd5());
            splitUploadVO.setBytes(bytes);

            Map<String, String> connectBaseFormData = getConnectBase(splitUploadVO, ApiConstants.UPLOADSPLITS);
            String responseStr = connectBaseFormData.get("responseStr");
            ResultApi resultApi = JSONObject.parseObject(JSONUtil.toJsonStr(responseStr), ResultApi.class);

            if (!(resultApi.isSucc() && resultApi.getData() != null)) {
                throw new SunyardException(resultApi.getMsg() != null ? resultApi.getMsg() : "分片上传文件出错");
            }
        } catch (Exception e) {
            log.error("上传异常", e);
        }
    }


    /**
     * 文件分片
     *
     * @param file
     * @param partNumber
     * @param chunkSize
     * @return
     */
    private byte[] fileSplit(MultipartFile file, Integer partNumber, long chunkSize) {
        long start = (partNumber - 1) * chunkSize;
        long end = Math.min(start + chunkSize, file.getSize());
        try (InputStream inputStream = file.getInputStream()) {
            int chunkSizeInt = (int) (end - start);
            byte[] buffer = new byte[chunkSizeInt];
            inputStream.skip(start);
            int bytesRead = inputStream.read(buffer);
            if (bytesRead > 0) {
                return buffer;
            }
        } catch (IOException e) {
            log.error("文件分片异常", e);
        }
        return null;
    }


    /**
     * 文件关联业务信息并入库
     *
     * @param files
     * @param ecmFileInfoDTO
     */
    private void associationBusi(List<SysFileApiDTO> files, EcmFileInfoDTO ecmFileInfoDTO, AddBusiDTO addBusiDTO) {
        EcmFileInfoApiDTO ecmFileInfoApiDTO = new EcmFileInfoApiDTO();
        ecmFileInfoDTO.setFileSource("OpenApi");
        ecmFileInfoApiDTO.setEcmFileInfoDTO(ecmFileInfoDTO);
        ecmFileInfoApiDTO.setAddBusiDTO(addBusiDTO);
        ecmFileInfoApiDTO.setFiles(files);
        if (CollectionUtil.isNotEmpty(files)) {
            insertFileInfo(ecmFileInfoApiDTO);
        }
    }


}
