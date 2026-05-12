package com.sunyard.module.storage.ecm.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.conversion.XmlUtils;
import com.sunyard.framework.common.util.date.DateUtils;
import com.sunyard.framework.common.util.http.HttpUtils;
import com.sunyard.module.storage.api.StorageEcmBizApi;
import com.sunyard.module.storage.config.properties.StorageNonBankEcmProperties;
import com.sunyard.module.storage.dto.ecm.EcmBaseData;
import com.sunyard.module.storage.dto.ecm.EcmBatch;
import com.sunyard.module.storage.dto.ecm.EcmBatchBusiInfo;
import com.sunyard.module.storage.dto.ecm.EcmMetaData;
import com.sunyard.module.storage.dto.ecm.EcmPages;
import com.sunyard.module.storage.dto.ecm.EcmReceive;
import com.sunyard.module.storage.dto.ecm.EcmRequest;
import com.sunyard.module.storage.dto.ecm.EcmRequestBody;
import com.sunyard.module.storage.dto.ecm.EcmRequestBusiData;
import com.sunyard.module.storage.dto.ecm.EcmVtree;
import com.sunyard.module.storage.dto.ecm.EcmVtreeNode;
import com.sunyard.module.storage.dto.webservice.AppAttr;
import com.sunyard.module.storage.dto.webservice.AppAttrId;
import com.sunyard.module.storage.dto.webservice.AppType;
import com.sunyard.module.storage.dto.webservice.BusinessRespBean;
import com.sunyard.module.storage.dto.webservice.DocType;
import com.sunyard.module.storage.dto.webservice.ResponseBean;
import com.sunyard.module.storage.ecm.dto.EcmCopyData;
import com.sunyard.module.storage.ecm.dto.EcmCopyFromBatchData;
import com.sunyard.module.storage.ecm.dto.EcmCopyMetaData;
import com.sunyard.module.storage.ecm.dto.EcmCopyToBatchData;
import com.sunyard.module.storage.ecm.dto.QueryImgBusiData;
import com.sunyard.module.storage.ecm.service.SunEcmBusinessWebService;
import com.sunyard.module.storage.ecm.webservice.BusinessWebService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author P-JWei
 * @date 2023/5/15 16:40 @title：
 * @description:
 */
@Slf4j
@RestController
public class StorageEcmBizApiImpl implements StorageEcmBizApi {

    public static final String DEFAULT_DOC_CODE = "DAZL";
    @Resource
    private StorageNonBankEcmProperties storageNonBankEcmProperties;
    @Resource
    private SunEcmBusinessWebService sunEcmBusinessWebService;

    /**
     * @return Result
     * @ appCode
     * @ businessNo
     * @ userCode
     * @ userName
     */
    @Override
    public Result getResourceSydEcm0010ByUserName(EcmRequestBody ecmRequestBody) {
        String ip2 = storageNonBankEcmProperties.getIp2();
        String port2 = storageNonBankEcmProperties.getPort2();
        String key = storageNonBankEcmProperties.getKey();
        String serverParameter = storageNonBankEcmProperties.getServerParameter();
        String interfaceAddress = storageNonBankEcmProperties.getInterfaceAddress();

        String address = serverParameter + "/" + interfaceAddress;
        ecmRequestBody.setIp(ip2);
        ecmRequestBody.setPort(port2);
        ecmRequestBody.setKey(key);
        ecmRequestBody.setRoleNo(storageNonBankEcmProperties.getRole());
        ecmRequestBody.setAddress(address);
        return getResourceSydEcm0010(ecmRequestBody);
    }

    @Override
    public Result updateSunEcmDocTypeSimple(String docCode, String docName, String parentDocCode,
                                            String actUserName) {
        actUserName = actUserName == null ? "档案系统" : actUserName;
        if (ObjectUtils.isEmpty(docCode) || ObjectUtils.isEmpty(docName)
                || ObjectUtils.isEmpty(storageNonBankEcmProperties.getRole())) {
            return Result.success(false);
        }
        DocType docType = new DocType();
        docType.setDocCode(docCode);
        docType.setDocName(docName);
        docType.setParent(parentDocCode);
        docType.setModifyUser(actUserName);
        return this.updateSunEcmDocType(docType);
    }

    @Override
    public Result addSunEcmDocTypes(String docCode, String docName, String parent,
                                    String actUserName) {
        BusinessWebService businessWebService = sunEcmBusinessWebService
                .getBusinessWebServiceImplPort();
        BusinessRespBean businessRespBean = new BusinessRespBean();
        List<DocType> docTypeList = new ArrayList<>();
        DocType docType = new DocType();
        docType.setModifyUser(actUserName);
        docType.setCreateUser(actUserName);
        docType.setStatus(1);
        docType.setDocCode(docCode);
        docType.setDocName(docName);
        docType.setParent(parent);
        docTypeList.add(docType);
        String param = JSON.toJSONString(docTypeList);
        businessRespBean.setDocTypeList(param);
        ResponseBean respBean = businessWebService.addBusiness(businessRespBean);
        return Result.success("200".equals(respBean.getRespCode()));
    }

    @Override
    public Result addSunEcmAppAttr(String appCode, String attrCode, String attrName,
                                   String actUserName, Boolean isPrimaryKey, Integer isShow) {
        BusinessWebService businessWebService = sunEcmBusinessWebService
                .getBusinessWebServiceImplPort();
        BusinessRespBean businessRespBean = new BusinessRespBean();
        AppAttr appAttr = new AppAttr();
        AppAttrId id = new AppAttrId();
        id.setAppCode(appCode);
        id.setAttrCode(attrCode);
        appAttr.setId(id);
        appAttr.setAttrName(attrName);
        appAttr.setInputType(1);
        appAttr.setStatus(1);
        appAttr.setIsKey(isPrimaryKey ? 1 : 0);
        appAttr.setIsNull(1);
        appAttr.setIsShow(isShow);
        appAttr.setCreateUser(actUserName);
        List<AppAttr> appAttrs = new ArrayList<>();
        appAttrs.add(appAttr);
        businessRespBean.setAppAttrList(JSON.toJSONString(appAttrs));
        ResponseBean respBean = businessWebService.addBusiness(businessRespBean);
        if (!"200".equals(respBean.getRespCode())) {
            log.debug("同步影像类型：{}", respBean.getRespMsg());
        }
        return Result.success("200".equals(respBean.getRespCode()));
    }

    @Override
    public Result sunEcmLinkTypeDocs(String appCode, String[] docCodes) {
        if (null == docCodes || docCodes.length == 0) {
            return Result.success(false);
        }
        BusinessWebService businessWebService = sunEcmBusinessWebService
                .getBusinessWebServiceImplPort();
        BusinessRespBean businessRespBean = new BusinessRespBean();

        List<String> docStr = new ArrayList<>();
        for (int i = 0; i < docCodes.length; i++) {
            docStr.add("" + i + "=" + docCodes[i]);
        }
        businessRespBean.setAppCode(appCode);
        businessRespBean.setDocList(JSON.toJSONString(docStr));
        ResponseBean respBean = businessWebService.addBusiness(businessRespBean);
        return Result.success("200".equals(respBean.getRespCode()));
    }

    @Override
    public Result addSunEcmRoleRight(String appCode, Integer num) {
        String role = storageNonBankEcmProperties.getRole();

        BusinessWebService businessWebService = sunEcmBusinessWebService
                .getBusinessWebServiceImplPort();
        BusinessRespBean businessRespBean = new BusinessRespBean();
        List<String> docRights = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            docRights.add("C,R,U,D,T,1000,0");
        }
        businessRespBean.setAppCode(appCode);
        businessRespBean.setRoleCode(role);
        businessRespBean.setRightList(JSON.toJSONString(docRights));
        ResponseBean respBean = businessWebService.addBusiness(businessRespBean);
        return Result.success("200".equals(respBean.getRespCode()));
    }

    @Override
    public Result<ResponseBean> batchDelBusiness(BusinessRespBean bean) {
        BusinessWebService businessWebService = sunEcmBusinessWebService
                .getBusinessWebServiceImplPort();
        //        JSON.toJSONString(businessWebService.delBusiness(bean));
        ResponseBean responseBean = businessWebService.delBusiness(bean);
        return Result.success(responseBean);
    }

    @Override
    public Result<ResponseBean> batchAddBusiness(BusinessRespBean bean) {
        BusinessWebService businessWebService = sunEcmBusinessWebService
                .getBusinessWebServiceImplPort();
        //        String jsonString = JSON.toJSONString(businessWebService.addBusiness(bean));
        ResponseBean responseBean = businessWebService.addBusiness(bean);
        return Result.success(responseBean);
    }

    @Override
    public Result getDefaultDocCode() {
        return Result.success(DEFAULT_DOC_CODE);
    }

    @Override
    public Result updateSunEcmTypeSimple(String appCode, String appName, String parentAppCode,
                                         String actUserName) {
        String docCode = storageNonBankEcmProperties.getDocCode();
        String docName = storageNonBankEcmProperties.getDocName();
        String role = storageNonBankEcmProperties.getRole();

        actUserName = actUserName == null ? "档案系统" : actUserName;
        if (ObjectUtils.isEmpty(docCode) || ObjectUtils.isEmpty(docName)
                || ObjectUtils.isEmpty(role)) {
            return Result.success(false);
        }
        AppType appType = new AppType();
        appType.setAppCode(appCode);
        appType.setAppName(appName);
        appType.setParent(parentAppCode);
        appType.setModifyTime(DateUtils.getNowDate());
        appType.setModifyUser(actUserName);
        return this.updateSunEcmAppType(appType);
    }

    @Override
    public Result deleteSunEcmAppAttr(String appCode, String attrCode) {
        BusinessWebService businessWebService = sunEcmBusinessWebService
                .getBusinessWebServiceImplPort();
        BusinessRespBean businessRespBean = new BusinessRespBean();
        AppAttr appAttr = new AppAttr();
        AppAttrId id = new AppAttrId();
        id.setAppCode(appCode);
        id.setAttrCode(attrCode);
        appAttr.setId(id);
        List<AppAttr> appAttrs = new ArrayList<>();
        appAttrs.add(appAttr);
        businessRespBean.setAppAttrList(JSON.toJSONString(appAttrs));
        ResponseBean respBean = businessWebService.delBusiness(businessRespBean);
        return Result.success("200".equals(respBean.getRespCode()));
    }

    @Override
    public Result addAttachToEum(String arcNo, String fileId, String fileNo, List<String> pages,
                                 String sysParamValue, String arcTypeNo, String arcTypeName,
                                 List<EcmVtreeNode> vtreeNodes) {
        String referer = storageNonBankEcmProperties.getReferer();
        String organCode = storageNonBankEcmProperties.getOrganCode();
        String role = storageNonBankEcmProperties.getRole();
        String ip2 = storageNonBankEcmProperties.getIp2();
        String port2 = storageNonBankEcmProperties.getPort2();
        String key = storageNonBankEcmProperties.getKey();
        String serverParameter = storageNonBankEcmProperties.getServerParameter();
        String interfaceAddress = storageNonBankEcmProperties.getInterfaceAddress();

        String address = serverParameter + "/" + interfaceAddress;
        EcmCopyData ecmCopyData = new EcmCopyData();
        // 来源
        EcmCopyFromBatchData ecmCopyFromBatchData = new EcmCopyFromBatchData();
        ecmCopyFromBatchData.setAppCode(sysParamValue);
        ecmCopyFromBatchData.setBusiNo(fileId);
        EcmPages ecmPages = new EcmPages();
        ecmPages.setPageid(pages);

        ecmCopyFromBatchData.setEcmPageids(ecmPages);

        // 是否自动添加为归类节点
        EcmVtree vtree = new EcmVtree();
        // List<EcmVtreeNode> vtreeNodes = getVtreeNodes(materialsExtendList, false, false);
        vtree.setAppCode(arcTypeNo);
        vtree.setAppName(arcTypeName);
        vtree.setNodes(deleteArcId(vtreeNodes));
        // 目标
        EcmCopyMetaData ecmCopyMetaData = new EcmCopyMetaData();
        ecmCopyMetaData.setEcmCopyFromBatchData(ecmCopyFromBatchData);
        EcmCopyToBatchData ecmCopyToBatchData = new EcmCopyToBatchData();
        ecmCopyToBatchData.setAppCode(arcTypeNo);
        ecmCopyToBatchData.setAppName(arcTypeName);
        ecmCopyToBatchData.setBusiNo(arcNo);
        ecmCopyToBatchData.setImageType(fileNo);
        ecmCopyToBatchData.setEcmVtree(vtree);

        ecmCopyMetaData.setEcmCopyToBatchData(ecmCopyToBatchData);
        ecmCopyMetaData.setPageCopy(pages.size());
        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setUserCode(referer);
        ecmBaseData.setUserName(referer);
        ecmBaseData.setOrgCode(organCode);
        ecmBaseData.setOrgName(organCode);
        ecmBaseData.setRoleCode(role);

        ecmCopyData.setEcmMetaData(ecmCopyMetaData);
        ecmCopyData.setEcmBaseData(ecmBaseData);
        // 拼接请求参数
        EcmRequestBusiData ecmRequestBusiData = new EcmRequestBusiData();
        ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_COPY);
        ecmRequestBusiData.setFormat(EcmRequestBusiData.FORMAT_XML);
        ecmRequestBusiData.setBusiData(ecmCopyData);

        // 组装请求
        EcmRequest ecmRequest = new EcmRequest();
        ecmRequest.setIp(ip2);
        ecmRequest.setPort(port2);
        ecmRequest.setAddress(address);
        ecmRequest.setKey(key);
        ecmRequest.setEcmRequestBusiData(ecmRequestBusiData);

        String resourceAccessUrl = ecmRequest.getRequestUrl();
        HttpUtils httpUtil = HttpUtils.init();
        httpUtil.setHeader("Referer", referer);
        Map<String, String> result = httpUtil.post(resourceAccessUrl);
        log.info("影像返回：{},{}", JSON.toJSONString(result), resourceAccessUrl);
        return "200".equals(result.get("statusCode"))
                ? Result.success(JSON
                        .toJSONString(XmlUtils.unmarshal(result.get("result"), EcmReceive.class)))
                : null;
    }

    /**
     * @return Result
     * @ ip       影像系统IP
     * @ port     影像系统端口
     * @ address  影像接口地址
     * @ key      影像授权密钥
     * @ appCode  业务类型
     * @ busiNo   批次号
     * @ userCode 操作人ID
     * @ userName 操作人姓名
     * @ roleCode 角色
     */
    @Override
    public Result getResourceSydEcm0010(EcmRequestBody ecmRequestBody) {
        String ip = storageNonBankEcmProperties.getIp();
        String port = storageNonBankEcmProperties.getPort();
        String referer = storageNonBankEcmProperties.getReferer();
        String organCode = storageNonBankEcmProperties.getOrganCode();
        String key = storageNonBankEcmProperties.getKey();

        // 拼接xml参数
        QueryImgBusiData queryImgBusiData = new QueryImgBusiData();
        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setUserCode(ecmRequestBody.getUserId());
        ecmBaseData.setUserName(ecmRequestBody.getUserName());
        ecmBaseData.setOrgCode(organCode);
        ecmBaseData.setOrgName("总公司");
        ecmBaseData.setRoleCode(ecmRequestBody.getRoleNo());
        queryImgBusiData.setEcmBaseData(ecmBaseData);

        EcmMetaData ecmMetaData = new EcmMetaData();
        EcmBatch metaDataBatch = new EcmBatch();
        metaDataBatch.setAppCode(ecmRequestBody.getAppCode());
        metaDataBatch.setBusiNo(ecmRequestBody.getBusiNo());
        metaDataBatch.setAppName("业务");
        ecmMetaData.addMetaDataBatch(metaDataBatch);
        queryImgBusiData.setEcmMetaData(ecmMetaData);

        // 拼接请求参数
        EcmRequestBusiData ecmRequestBusiData = new EcmRequestBusiData();
        ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_RESOURCE_ACCESS);
        ecmRequestBusiData.setFormat(EcmRequestBusiData.FORMAT_XML);
        ecmRequestBusiData.setBusiData(queryImgBusiData);
        // 组装请求
        EcmRequest ecmRequest = new EcmRequest();
        if (ObjectUtils.isEmpty(ecmRequestBody.getIp())) {
            ecmRequest.setIp(ip);
        } else {
            ecmRequest.setIp(ecmRequestBody.getIp());
        }
        if (ObjectUtils.isEmpty(ecmRequestBody.getPort())) {
            ecmRequest.setPort(port);
        } else {
            ecmRequest.setPort(ecmRequestBody.getPort());
        }

        ecmRequest.setAddress(ecmRequestBody.getAddress());
        if (ObjectUtils.isEmpty(ecmRequestBody.getKey())) {
            ecmRequest.setKey(key);
        } else {
            ecmRequest.setKey(ecmRequestBody.getKey());
        }
        ecmRequest.setEcmRequestBusiData(ecmRequestBusiData);
        String resourceAccessUrl = ecmRequest.getRequestUrl();
        HttpUtils httpUtil = HttpUtils.init();
        httpUtil.setHeader("Referer", referer);
        Map<String, String> result = httpUtil.post(resourceAccessUrl);
        log.info("查询影像返回：{}", JSON.toJSONString(result));
        return "200".equals(result.get("statusCode"))
                ? Result.success(JSON
                        .toJSONString(XmlUtils.unmarshal(result.get("result"), EcmReceive.class)))
                : null;
    }

    /**
     * 修改影像资料类型
     *
     * @param docType
     * @return Result
     */
    @Override
    public Result updateSunEcmDocType(DocType docType) {
        BusinessWebService businessWebService = sunEcmBusinessWebService
                .getBusinessWebServiceImplPort();
        BusinessRespBean businessRespBean = new BusinessRespBean();
        List<DocType> list = new ArrayList<>();
        list.add(docType);
        businessRespBean.setDocTypeList(JSON.toJSONString(list));
        ResponseBean respBean = businessWebService.modBusiness(businessRespBean);
        return Result.success("200".equals(respBean.getRespCode()));
    }

    /**
     * @param ecmRequestBody
     * @return Result
     */
    @Override
    public Result getScanImgInTreeUrlDynamicEnt(EcmRequestBody ecmRequestBody) {
        String organCode = storageNonBankEcmProperties.getOrganCode();
        String role = storageNonBankEcmProperties.getRole();
        String ip2 = storageNonBankEcmProperties.getIp2();
        String port2 = storageNonBankEcmProperties.getPort2();
        String key = storageNonBankEcmProperties.getKey();
        String serverParameter = storageNonBankEcmProperties.getServerParameter();
        String interfaceAddress = storageNonBankEcmProperties.getInterfaceAddress();

        String address = serverParameter + "/" + interfaceAddress;
        return getScanImgInTreeUrlEnt(ip2, port2, address, key, organCode,
                ecmRequestBody.getAppCode(), ecmRequestBody.getBusiNo(), ecmRequestBody.getUserId(),
                ecmRequestBody.getUserName(), role, ecmRequestBody.getVtreeNodes(),
                ecmRequestBody.getAppAttrs(), ecmRequestBody.getBusiTitle(),
                ecmRequestBody.getIsStatic(), ecmRequestBody.getIsScanning(),
                ecmRequestBody.getArcNo());
    }

    @Override
    public Result getQueryImgInTreeUrlEnt(EcmRequestBody ecmRequestBody) {
        String organCode = storageNonBankEcmProperties.getOrganCode();
        String role = storageNonBankEcmProperties.getRole();
        String ip2 = storageNonBankEcmProperties.getIp2();
        String port2 = storageNonBankEcmProperties.getPort2();
        String key = storageNonBankEcmProperties.getKey();
        String serverParameter = storageNonBankEcmProperties.getServerParameter();
        String interfaceAddress = storageNonBankEcmProperties.getInterfaceAddress();

        String address = serverParameter + "/" + interfaceAddress;
        return getQueryImgInTreeUrlEnt(ip2, port2, address, key, organCode,
                ecmRequestBody.getAppCode(), ecmRequestBody.getBusiNo(), ecmRequestBody.getUserId(),
                ecmRequestBody.getUserName(), role, ecmRequestBody.getVtreeNodes(),
                ecmRequestBody.getAppAttrs(), ecmRequestBody.getBusiTitle(),
                ecmRequestBody.getIsStatic(), ecmRequestBody.getArcNo());
    }

    @Override
    public Result getBatchQueryImgInTreeUrlEnt(EcmRequestBody ecmRequestBody) {
        String organCode = storageNonBankEcmProperties.getOrganCode();
        String role = storageNonBankEcmProperties.getRole();
        String ip2 = storageNonBankEcmProperties.getIp2();
        String port2 = storageNonBankEcmProperties.getPort2();
        String key = storageNonBankEcmProperties.getKey();
        String serverParameter = storageNonBankEcmProperties.getServerParameter();
        String interfaceAddress = storageNonBankEcmProperties.getInterfaceAddress();

        String address = serverParameter + "/" + interfaceAddress;
        return getBatchQueryImgInTreeUrlEnt(ip2, port2, address, key, organCode,
                ecmRequestBody.getUserId(), ecmRequestBody.getUserName(), role,
                ecmRequestBody.getIsStatic(), ecmRequestBody.getIsScan(),
                ecmRequestBody.getIsScanning(), ecmRequestBody.getEcmBatchBusiInfoList());
    }

    private Result getQueryImgInTreeUrlEnt(String ip, String port, String address, String key,
                                           String orgCode, String appCode, String busiNo,
                                           String userId, String userName, String roleNo,
                                           List<EcmVtreeNode> vtreeNodes,
                                           Map<String, String> temMap, String busiTitle,
                                           Integer isStatic, String arcNo) {

        EcmVtree vtree = new EcmVtree();
        vtree.setNodes(deleteArcId(vtreeNodes));
        if (isStatic == 0) {
            return getSunEcmUrlEnt(ip, port, address, key, orgCode, appCode, busiNo, userId,
                    userName, roleNo, vtree, temMap, busiTitle, false, 1, null, arcNo);
        } else {
            return getQueryImgUrl(ip, port, address, key, orgCode, appCode, busiNo, userId,
                    userName, roleNo, temMap, busiTitle, null);
        }
    }

    private Result getScanImgInTreeUrlEnt(String ip, String port, String address, String key,
                                          String orgCode, String appCode, String busiNo,
                                          String userId, String userName, String roleNo,
                                          List<EcmVtreeNode> vtreeNodes, Map<String, String> temMap,
                                          String busiTitle, Integer isStatic, Integer isScanning,
                                          String arcNo) {
        // 拼接vtree参数
        EcmVtree vtree = new EcmVtree();
        vtree.setNodes(deleteArcId(vtreeNodes));
        if (isStatic == 0) {
            return getSunEcmUrlEnt(ip, port, address, key, orgCode, appCode, busiNo, userId,
                    userName, roleNo, vtree, temMap, busiTitle, true, isScanning, null, arcNo);
        } else {
            return getScanImgUrl(ip, port, address, key, orgCode, appCode, busiNo, userId, userName,
                    roleNo, temMap, busiTitle, isScanning);
        }
    }

    public Result getQueryImgUrl(String ip, String port, String address, String key, String orgCode,
                                 String appCode, String busiNo, String userId, String userName,
                                 String roleNo, Map<String, String> appAttrs, String busiTitle,
                                 String version) {
        // 拼接xml参数
        QueryImgBusiData queryImgBusiData = new QueryImgBusiData();
        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setComCode(orgCode);
        ecmBaseData.setOrgCode(orgCode);
        ecmBaseData.setUserCode(userId);
        ecmBaseData.setUserName(userName);
        ecmBaseData.setRoleCode(roleNo);
        ecmBaseData.setOrgName("档案系统");
        queryImgBusiData.setEcmBaseData(ecmBaseData);
        EcmMetaData ecmMetaData = new EcmMetaData();
        EcmBatch metaDataBatch = new EcmBatch();
        metaDataBatch.setAppCode(appCode);
        metaDataBatch.setBusiNo(busiNo);
        metaDataBatch.setBusinessNo(busiNo);
        metaDataBatch.setBusiTitle(busiTitle);
        metaDataBatch.setQueryVer(version);
        ecmMetaData.addMetaDataBatch(metaDataBatch);
        queryImgBusiData.setEcmMetaData(ecmMetaData);
        if (!ObjectUtils.isEmpty(appAttrs)) {
            for (String attrName : appAttrs.keySet()) {
                metaDataBatch.addAttr(attrName, appAttrs.get(attrName));
            }
        }

        // 拼接请求参数
        EcmRequestBusiData ecmRequestBusiData = new EcmRequestBusiData();
        ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_QUERY);
        ecmRequestBusiData.setFormat(EcmRequestBusiData.FORMAT_XML);
        ecmRequestBusiData.setBusiData(queryImgBusiData);

        // 组装请求
        EcmRequest ecmRequest = new EcmRequest();
        ecmRequest.setIp(ip);
        ecmRequest.setPort(port);
        ecmRequest.setAddress(address);
        ecmRequest.setKey(key);
        ecmRequest.setEcmRequestBusiData(ecmRequestBusiData);
        return Result.success(ecmRequest.getRequestUrl());
    }

    private Result getSunEcmUrlEnt(String ip, String port, String address, String key,
                                   String orgCode, String appCode, String busiNo, String userId,
                                   String userName, String roleNo, EcmVtree vtree,
                                   Map<String, String> appAttrs, String busiTitle, boolean isScan,
                                   Integer isScanning, String version, String arcNo) {
        // 拼接xml参数
        QueryImgBusiData queryImgBusiData = new QueryImgBusiData();
        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setComCode(orgCode);
        ecmBaseData.setOrgCode(orgCode);
        ecmBaseData.setUserCode(userId);
        ecmBaseData.setUserName(userName);
        ecmBaseData.setRoleCode(roleNo);
        ecmBaseData.setOrgName("档案系统");
        queryImgBusiData.setEcmBaseData(ecmBaseData);
        EcmMetaData ecmMetaData = new EcmMetaData();
        EcmBatch metaDataBatch = new EcmBatch();
        metaDataBatch.setAppCode(appCode);
        metaDataBatch.setBusiNo(busiNo);
        metaDataBatch.setBusinessNo(busiNo);
        metaDataBatch.setBusiArcno(arcNo);
        metaDataBatch.setBusiTitle(busiTitle);
        metaDataBatch.setQueryVer(version);
        // 2022-05-06 pjw 是否使用ES
        metaDataBatch.setIsToEs("1");
        if (!ObjectUtils.isEmpty(appAttrs)) {
            for (String attrName : appAttrs.keySet()) {
                metaDataBatch.addAttr(attrName, appAttrs.get(attrName));
            }
        }

        ecmMetaData.addMetaDataBatch(metaDataBatch);
        queryImgBusiData.setEcmMetaData(ecmMetaData);
        if (!ObjectUtils.isEmpty(vtree)) {
            metaDataBatch.setEcmVtree(vtree);
        }
        // 拼接请求参数
        EcmRequestBusiData ecmRequestBusiData = new EcmRequestBusiData();
        if (isScan) {
            if (isScanning == 0) {
                ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_SCAN_W);
            } else {
                ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_SCAN);
            }
        } else {
            ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_QUERY);
        }
        ecmRequestBusiData.setFormat(EcmRequestBusiData.FORMAT_XML);
        ecmRequestBusiData.setBusiData(queryImgBusiData);

        // 组装请求
        EcmRequest ecmRequest = new EcmRequest();
        ecmRequest.setIp(ip);
        ecmRequest.setPort(port);
        ecmRequest.setAddress(address);
        ecmRequest.setKey(key);
        ecmRequest.setEcmRequestBusiData(ecmRequestBusiData);
        return Result.success(ecmRequest.getRequestUrl());
    }

    public Result getScanImgUrl(String ip, String port, String address, String key, String orgCode,
                                String appCode, String busiNo, String userId, String userName,
                                String roleNo, Map<String, String> appAttrs, String busiTitle,
                                Integer isScanning) {
        // 拼接xml参数
        QueryImgBusiData queryImgBusiData = new QueryImgBusiData();
        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setComCode(orgCode);
        ecmBaseData.setOrgCode(orgCode);
        ecmBaseData.setUserCode(userId);
        ecmBaseData.setUserName(userName);
        ecmBaseData.setRoleCode(roleNo);
        ecmBaseData.setOrgName("档案系统");
        queryImgBusiData.setEcmBaseData(ecmBaseData);
        EcmMetaData ecmMetaData = new EcmMetaData();
        EcmBatch metaDataBatch = new EcmBatch();
        metaDataBatch.setAppCode(appCode);
        metaDataBatch.setBusiNo(busiNo);
        metaDataBatch.setBusinessNo(busiNo);
        metaDataBatch.setBusiTitle(busiTitle);
        // 2022-05-06 PJW 是否使用ES
        metaDataBatch.setIsToEs("1");
        if (!ObjectUtils.isEmpty(appAttrs)) {
            /// 暂时注释
            // Iterator<String> keyIter = appAttrs.keySet().iterator();
            // while (keyIter.hasNext()){
            // String attrName = keyIter.next();
            // metaDataBatch.addAttr(attrName,appAttrs.get(attrName));
            // }
            for (String attrName : appAttrs.keySet()) {
                metaDataBatch.addAttr(attrName, appAttrs.get(attrName));
            }
        }
        ecmMetaData.addMetaDataBatch(metaDataBatch);
        queryImgBusiData.setEcmMetaData(ecmMetaData);

        // 拼接请求参数
        EcmRequestBusiData ecmRequestBusiData = new EcmRequestBusiData();
        if (isScanning == 0) {
            ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_SCAN_W);
        } else {
            ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_SCAN);
        }
        ecmRequestBusiData.setFormat(EcmRequestBusiData.FORMAT_XML);
        ecmRequestBusiData.setBusiData(queryImgBusiData);

        // 组装请求
        EcmRequest ecmRequest = new EcmRequest();
        ecmRequest.setIp(ip);
        ecmRequest.setPort(port);
        ecmRequest.setAddress(address);
        ecmRequest.setKey(key);
        ecmRequest.setEcmRequestBusiData(ecmRequestBusiData);

        return Result.success(ecmRequest.getRequestUrl());
    }

    /**
     * 动态树模式 获取扫描影像接口地址
     *
     * @return Result
     */
    @Override
    public Result getSunEcmUrl(EcmRequestBody ecmRequestBody) {
        String organCode = storageNonBankEcmProperties.getOrganCode();
        String role = storageNonBankEcmProperties.getRole();
        String ip2 = storageNonBankEcmProperties.getIp2();
        String port2 = storageNonBankEcmProperties.getPort2();
        String key = storageNonBankEcmProperties.getKey();
        String serverParameter = storageNonBankEcmProperties.getServerParameter();
        String interfaceAddress = storageNonBankEcmProperties.getInterfaceAddress();

        String address = serverParameter + "/" + interfaceAddress;
        // 拼接xml参数
        QueryImgBusiData queryImgBusiData = new QueryImgBusiData();
        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setComCode(organCode);
        ecmBaseData.setOrgCode(organCode);
        ecmBaseData.setUserCode(ecmRequestBody.getUserId());
        ecmBaseData.setUserName(ecmRequestBody.getUserName());
        if (ObjectUtils.isEmpty(ecmRequestBody.getRoleNo())) {
            ecmBaseData.setRoleCode(role);
        } else {
            ecmBaseData.setRoleCode(ecmRequestBody.getRoleNo());
        }
        ecmBaseData.setOrgName("档案系统");
        queryImgBusiData.setEcmBaseData(ecmBaseData);
        EcmMetaData ecmMetaData = new EcmMetaData();
        EcmBatch metaDataBatch = new EcmBatch();
        metaDataBatch.setAppCode(ecmRequestBody.getAppCode());
        metaDataBatch.setBusiNo(ecmRequestBody.getBusiNo());
        metaDataBatch.setBusinessNo(ecmRequestBody.getBusiNo());
        metaDataBatch.setBusiTitle(ecmRequestBody.getBusiTitle());
        metaDataBatch.setQueryVer(ecmRequestBody.getVersion());
        // 2022-05-06 pjw 是否使用ES/
        metaDataBatch.setIsToEs("1");
        if (!ObjectUtils.isEmpty(ecmRequestBody.getAppAttrs())) {
            for (String attrName : ecmRequestBody.getAppAttrs().keySet()) {
                metaDataBatch.addAttr(attrName, ecmRequestBody.getAppAttrs().get(attrName));
            }
        }

        ecmMetaData.addMetaDataBatch(metaDataBatch);
        queryImgBusiData.setEcmMetaData(ecmMetaData);
        if (!ObjectUtils.isEmpty(ecmRequestBody.getVtree())) {
            metaDataBatch.setEcmVtree(ecmRequestBody.getVtree());
        }
        // 拼接请求参数
        EcmRequestBusiData ecmRequestBusiData = new EcmRequestBusiData();
        if (ecmRequestBody.getIsScan()) {
            if (ecmRequestBody.getIsScanning() == 0) {
                ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_SCAN_W);
            } else {
                ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_SCAN);
            }
        } else {
            ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_QUERY);
        }
        ecmRequestBusiData.setFormat(EcmRequestBusiData.FORMAT_XML);
        ecmRequestBusiData.setBusiData(queryImgBusiData);

        // 组装请求
        EcmRequest ecmRequest = new EcmRequest();
        ecmRequest.setIp(ip2);
        ecmRequest.setPort(port2);
        ecmRequest.setAddress(address);
        ecmRequest.setKey(key);
        ecmRequest.setEcmRequestBusiData(ecmRequestBusiData);
        return Result.success(ecmRequest.getRequestUrl());
    }

    /**
     * 修改影像业务类型
     *
     * @param appType 业务类型信息
     * @return Result 调用接口结果
     */
    private Result updateSunEcmAppType(AppType appType) {
        BusinessWebService businessWebService = sunEcmBusinessWebService
                .getBusinessWebServiceImplPort();
        BusinessRespBean businessRespBean = new BusinessRespBean();
        List<AppType> list = new ArrayList<>();
        list.add(appType);
        businessRespBean.setAppTypeList(JSON.toJSONString(list));
        ResponseBean respBean = businessWebService.modBusiness(businessRespBean);
        return Result.success("200".equals(respBean.getRespCode()));
    }

    private List<EcmVtreeNode> deleteArcId(List<EcmVtreeNode> list) {
        for (EcmVtreeNode vtreeNode : list) {
            vtreeNode.setArcId(null);
            if (!ObjectUtils.isEmpty(vtreeNode.getNodes())) {
                deleteArcId(vtreeNode.getNodes());
            }
        }
        return list;
    }

    private Result getBatchQueryImgInTreeUrlEnt(String ip, String port, String address, String key,
                                                String orgCode, String userId, String userName,
                                                String roleNo, Integer isStatic, Boolean isScan,
                                                Integer isScanning,
                                                List<EcmBatchBusiInfo> ecmBatchBusiInfoList) {

        if (isStatic == 0) {
            return getBatchSunEcmUrlEnt(ip, port, address, key, orgCode, userId, userName, roleNo,
                    isStatic, isScan, isScanning, ecmBatchBusiInfoList);
        } else {
            return getBatchQueryImgUrl(ip, port, address, key, orgCode, userId, userName, roleNo,
                    isStatic, isScan, isScanning, ecmBatchBusiInfoList);
        }
    }

    private Result getBatchQueryImgUrl(String ip, String port, String address, String key,
                                       String orgCode, String userId, String userName,
                                       String roleNo, Integer isStatic, Boolean isScan,
                                       Integer isScanning,
                                       List<EcmBatchBusiInfo> ecmBatchBusiInfoList) {
        // 拼接xml参数
        QueryImgBusiData queryImgBusiData = new QueryImgBusiData();
        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setComCode(orgCode);
        ecmBaseData.setOrgCode(orgCode);
        ecmBaseData.setUserCode(userId);
        ecmBaseData.setUserName(userName);
        ecmBaseData.setRoleCode(roleNo);
        ecmBaseData.setOrgName("档案系统");
        queryImgBusiData.setEcmBaseData(ecmBaseData);
        EcmMetaData ecmMetaData = new EcmMetaData();
        for (EcmBatchBusiInfo ecmBatchBusiInfo : ecmBatchBusiInfoList) {
            EcmBatch ecmBatch = getEcmBatch(ecmBatchBusiInfo);
            ecmMetaData.addMetaDataBatch(ecmBatch);
        }
        //静态树
        ecmMetaData.setBatch(null);
        queryImgBusiData.setEcmMetaData(ecmMetaData);
        // 拼接请求参数
        EcmRequestBusiData ecmRequestBusiData = new EcmRequestBusiData();
        ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_QUERY);
        ecmRequestBusiData.setFormat(EcmRequestBusiData.FORMAT_XML);
        ecmRequestBusiData.setBusiData(queryImgBusiData);

        // 组装请求
        EcmRequest ecmRequest = new EcmRequest();
        ecmRequest.setIp(ip);
        ecmRequest.setPort(port);
        ecmRequest.setAddress(address);
        ecmRequest.setKey(key);
        ecmRequest.setEcmRequestBusiData(ecmRequestBusiData);
        return Result.success(ecmRequest.getRequestUrl());
    }

    private Result getBatchSunEcmUrlEnt(String ip, String port, String address, String key,
                                        String orgCode, String userId, String userName,
                                        String roleNo, Integer isStatic, Boolean isScan,
                                        Integer isScanning,
                                        List<EcmBatchBusiInfo> ecmBatchBusiInfoList) {
        // 拼接xml参数
        QueryImgBusiData queryImgBusiData = new QueryImgBusiData();
        EcmBaseData ecmBaseData = new EcmBaseData();
        ecmBaseData.setComCode(orgCode);
        ecmBaseData.setOrgCode(orgCode);
        ecmBaseData.setUserCode(userId);
        ecmBaseData.setUserName(userName);
        ecmBaseData.setRoleCode(roleNo);
        ecmBaseData.setOrgName("档案系统");
        queryImgBusiData.setEcmBaseData(ecmBaseData);
        EcmMetaData ecmMetaData = new EcmMetaData();
        for (EcmBatchBusiInfo ecmBatchBusiInfo : ecmBatchBusiInfoList) {
            EcmBatch ecmBatch = getEcmBatch(ecmBatchBusiInfo);
            ecmMetaData.addMetaDataBatch(ecmBatch);
        }
        queryImgBusiData.setEcmMetaData(ecmMetaData);

        // 拼接请求参数
        EcmRequestBusiData ecmRequestBusiData = new EcmRequestBusiData();
        if (isScan) {
            if (isScanning == 0) {
                ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_SCAN_W);
            } else {
                ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_SCAN);
            }
        } else {
            ecmRequestBusiData.setCode(EcmRequestBusiData.CODE_QUERY);
        }
        ecmRequestBusiData.setFormat(EcmRequestBusiData.FORMAT_XML);
        ecmRequestBusiData.setBusiData(queryImgBusiData);

        // 组装请求
        EcmRequest ecmRequest = new EcmRequest();
        ecmRequest.setIp(ip);
        ecmRequest.setPort(port);
        ecmRequest.setAddress(address);
        ecmRequest.setKey(key);
        ecmRequest.setEcmRequestBusiData(ecmRequestBusiData);
        return Result.success(ecmRequest.getRequestUrl());
    }

    /**
     * 获取档案批次信息
     * @param ecmBatchBusiInfo
     * @return
     */
    private EcmBatch getEcmBatch(EcmBatchBusiInfo ecmBatchBusiInfo) {
        EcmVtree vtree = new EcmVtree();
        vtree.setNodes(deleteArcId(ecmBatchBusiInfo.getVtreeNodes()));
        EcmBatch metaDataBatch = new EcmBatch();
        metaDataBatch.setAppCode(ecmBatchBusiInfo.getAppCode());
        metaDataBatch.setBusiNo(ecmBatchBusiInfo.getBusiNo());
        metaDataBatch.setBusinessNo(ecmBatchBusiInfo.getBusiNo());
        metaDataBatch.setBusiArcno(ecmBatchBusiInfo.getArcNo());
        metaDataBatch.setBusiTitle(ecmBatchBusiInfo.getBusiTitle());
        metaDataBatch.setQueryVer(null);
        if (!ObjectUtils.isEmpty(vtree)) {
            metaDataBatch.setEcmVtree(vtree);
        }
        // 2022-05-06 pjw 是否使用ES
        metaDataBatch.setIsToEs("1");
        Map<String, String> appAttrs = ecmBatchBusiInfo.getAppAttrs();
        if (!ObjectUtils.isEmpty(appAttrs)) {
            for (String attrName : appAttrs.keySet()) {
                metaDataBatch.addAttr(attrName, appAttrs.get(attrName));
            }
        }
        return metaDataBatch;
    }
}
