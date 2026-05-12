package com.sunyard.module.storage.api;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.storage.constant.ApiConstants;
import com.sunyard.module.storage.dto.ecm.EcmRequestBody;
import com.sunyard.module.storage.dto.ecm.EcmVtreeNode;
import com.sunyard.module.storage.dto.webservice.BusinessRespBean;
import com.sunyard.module.storage.dto.webservice.DocType;
import com.sunyard.module.storage.dto.webservice.ResponseBean;

/**
 * @author P-JWei
 * @date 2023/5/15 16:22 @title：
 * @description:
 */
@FeignClient(name = ApiConstants.NAME)
public interface StorageEcmBizApi {
    String PREFIX = ApiConstants.PREFIX + "/storageEcmBiz/";

    /**
     * 获取源0010连接通过用户名
     *
     * @param ecmRequestBody ecmRequestBody
     * @return Result EcmReceive
     */
    @PostMapping(PREFIX + "getResourceSydEcm0010ByUserName")
    Result getResourceSydEcm0010ByUserName(@RequestBody EcmRequestBody ecmRequestBody);

    /**
     * 修改资料类型名称
     *
     * @param docCode       资料类型号
     * @param docName       资料类型名称
     * @param parentDocCode 父级
     * @param actUserName   操作人
     * @return Result
     */
    @PostMapping(PREFIX + "updateSunEcmDocTypeSimple")
    Result updateSunEcmDocTypeSimple(@RequestParam("docCode") String docCode, @RequestParam("docName") String docName,
                                     @RequestParam("parentDocCode") String parentDocCode, @RequestParam("actUserName") String actUserName);

    /**
     * 往影像系统增加资料类型
     *
     * @param docCode     资料类型号
     * @param docName     资料类型名称
     * @param parent      父级资料类型
     * @param actUserName 操作人名称
     * @return Result 调用接口结果
     */
    @PostMapping(PREFIX + "addSunEcmDocTypes")
    Result addSunEcmDocTypes(@RequestParam("docCode") String docCode, @RequestParam("docName") String docName,
                             @RequestParam("parent") String parent, @RequestParam("actUserName") String actUserName);

    /**
     * 往影像系统增加业务类型的业务属性
     *
     * @param appCode      业务类型号
     * @param attrCode     业务属性号
     * @param attrName     业务属性名称
     * @param actUserName  操作人名称
     * @param isPrimaryKey 是否是主键
     * @param isShow       是否隐藏
     * @return Result 调用接口结果
     */
    @PostMapping(PREFIX + "addSunEcmAppAttr")
    Result addSunEcmAppAttr(@RequestParam("appCode") String appCode, @RequestParam("attrCode") String attrCode,
                            @RequestParam("attrName") String attrName, @RequestParam("actUserName") String actUserName,
                            @RequestParam("isPrimaryKey") Boolean isPrimaryKey, @RequestParam("isShow") Integer isShow);

    /**
     * 往影像系统的业务类型关联资料
     *
     * @param appCode  业务类型号
     * @param docCodes 资料类型号
     * @return Result 调用接口结果
     */
    @PostMapping(PREFIX + "sunEcmLinkTypeDocs")
    Result sunEcmLinkTypeDocs(@RequestParam("appCode") String appCode, @RequestParam("docCodes") String[] docCodes);

    /**
     * 往影像系统的业务类型增加角色资料权限
     *
     * @param appCode 业务类型号
     * @param num     (资料类型个数,初始1)
     * @return Result 调用接口结果
     */
    @PostMapping(PREFIX + "addSunEcmRoleRight")
    Result addSunEcmRoleRight(@RequestParam("appCode") String appCode, @RequestParam("num") Integer num);

    /**
     * 批量删除
     *
     * @param bean BusinessRespBean
     * @return Result
     */
    @PostMapping(PREFIX + "batchDelBusiness")
    Result<ResponseBean> batchDelBusiness(@RequestBody BusinessRespBean bean);

    /**
     * 批量新增文件类型、资料类型、属性
     *
     * @param bean BusinessRespBean
     * @return Result
     */
    @PostMapping(PREFIX + "batchAddBusiness")
    Result<ResponseBean> batchAddBusiness(@RequestBody BusinessRespBean bean);

    /**
     * 获取默认文档code
     *
     * @return Result
     */
    @PostMapping(PREFIX + "getDefaultDocCode")
    Result getDefaultDocCode();

    /**
     * 修改业务类型的类型名称和父级
     *
     * @param appCode       业务类型号
     * @param appName       类型名称
     * @param parentAppCode 父级业务类型
     * @param actUserName   操作人
     * @return Result 操作结果
     */
    @PostMapping(PREFIX + "updateSunEcmTypeSimple")
    Result updateSunEcmTypeSimple(@RequestParam("appCode") String appCode, @RequestParam("appName") String appName,
                                  @RequestParam("parentAppCode") String parentAppCode, @RequestParam("actUserName") String actUserName);

    /**
     * 删除档案业务属性
     *
     * @param appCode  业务类型
     * @param attrCode 业务属性代号
     * @return Result
     */
    @PostMapping(PREFIX + "deleteSunEcmAppAttr")
    Result deleteSunEcmAppAttr(@RequestParam("appCode") String appCode, @RequestParam("attrCode") String attrCode);

    /**
     * 跨业务复制
     *
     * @param arcNo
     * @param fileId
     * @param fileNo
     * @param pages
     * @param sysParamValue
     * @param arcTypeNo
     * @param arcTypeName
     * @param vtreeNodes
     * @return Result
     */
    @PostMapping(PREFIX + "addAttachToEum")
    Result addAttachToEum(@RequestParam("arcNo") String arcNo, @RequestParam("fileId") String fileId,
                          @RequestParam("fileNo") String fileNo, @RequestParam("pages") List<String> pages,
                          @RequestParam("sysParamValue") String sysParamValue, @RequestParam("arcTypeNo") String arcTypeNo,
                          @RequestParam("arcTypeName") String arcTypeName, @RequestBody List<EcmVtreeNode> vtreeNodes);

    /**
     * getResourceSydEcm0010
     *
     * @param ecmRequestBody ecmRequestBody
     * @return Result
     */
    @PostMapping(PREFIX + "getResourceSydEcm0010")
    Result getResourceSydEcm0010(@RequestBody EcmRequestBody ecmRequestBody);

    /**
     * 修改影像资料类型
     *
     * @param docType docType
     * @return Result
     */
    @PostMapping(PREFIX + "updateSunEcmDocType")
    Result updateSunEcmDocType(@RequestBody DocType docType);

    /**
     * getScanImgInTreeUrlDynamicEnt
     *
     * @param ecmRequestBody ecmRequestBody
     * @return Result
     */
    @PostMapping(PREFIX + "getScanImgInTreeUrlDynamicEnt")
    Result getScanImgInTreeUrlDynamicEnt(@RequestBody EcmRequestBody ecmRequestBody);

    /**
     * getQueryImgInTreeUrlEnt
     *
     * @param ecmRequestBody ecmRequestBody
     * @return Result
     */
    @PostMapping(PREFIX + "getQueryImgInTreeUrlEnt")
    Result getQueryImgInTreeUrlEnt(@RequestBody EcmRequestBody ecmRequestBody);

    /**
     * 批量查询
     *
     * @param ecmRequestBody ecmRequestBody
     * @return Result
     */
    @PostMapping(PREFIX + "getBatchQueryImgInTreeUrlEnt")
    Result getBatchQueryImgInTreeUrlEnt(@RequestBody EcmRequestBody ecmRequestBody);


    /**
     * 动态树 vtree  静态树 vtree为null
     *
     * @param ecmRequestBody
     * @return Result
     */
    @PostMapping(PREFIX + "getSunEcmUrl")
    Result getSunEcmUrl(@RequestBody EcmRequestBody ecmRequestBody);

}
