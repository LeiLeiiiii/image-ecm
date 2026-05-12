package com.sunyard.ecm.oldToNew.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sunyard.ecm.bean.BatchCountResponseBean;
import com.sunyard.ecm.bean.BatchInfoRespBean;
import com.sunyard.ecm.bean.Ecm0010ResponseBean;
import com.sunyard.ecm.bean.Ecm006ResponseBean;
import com.sunyard.ecm.bean.ImageDownloadRespBean;
import com.sunyard.ecm.bean.NodeResponseBean;
import com.sunyard.ecm.bean.PageResponseBean;
import com.sunyard.ecm.bean.ResponseBean;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.*;
import com.sunyard.ecm.dto.split.SysFileApiDTO;
import com.sunyard.ecm.exception.OldToNewException;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.oldToNew.constant.ApiConstants;
import com.sunyard.ecm.oldToNew.socket.util.MD5Util;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.manager.OpenCaptureService;
import com.sunyard.ecm.service.OperateCaptureService;
import com.sunyard.ecm.manager.FileInfoService;
import com.sunyard.ecm.util.FunctionUtil;
import com.sunyard.ecm.util.GetTokenUtils;
import com.sunyard.ecm.util.ParamDecrypt;
import com.sunyard.ecm.vo.*;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.ZipUtils;
import com.sunyard.module.storage.api.FileStorageApi;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.vo.DownFileVO;
import com.sunyard.module.storage.vo.UploadListVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;


/**
 * @author yzy
 * @since 2025/02/17 10:55
 */
@Slf4j
@Service
public class ApiService {

    /**
     * appId，联系本系统获取
     */
    @Value("${oldToNew.appId:KXy0rX9I}")
    private String appId;
    /**
     * appSecret，联系本系统获取
     */
    @Value("${oldToNew.appSecret:a102333e710ef4011543f4440c6b3a7abb0cbb8d}")
    private String appSecret;
    /**
     * accessToken获取地址，目前写死可改到nacos上
     */
    @Value("${oldToNew.accessTokenUrl:/web-api/auth/getOutApiToken}")
    private String accessTokenUrl;

    /**
     * 创建对象时不指定ip，默认本地
     */
    @Value("${oldToNew.ip:http://172.1.1.210:8080}")
    private String ip;
    @Value("${oldToNew.storageIp:http://172.1.1.210:28083/api/storage/deal/shardingDownFile}")
    private String storageIp;
    @Value("${oldToNew.downloadUrl:http://172.1.1.210:8040/web-api/ecm/api/ecms/oldToNew/downloadFile?fileName=}")
    private String downloadUrl;
    @Value("${oldToNew.filePath:/home/files}")
    private String filePath;
    @Resource
    private OpenApiService openApiService;
    @Resource
    private OpenCaptureService openCaptureService;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private FileStorageApi fileStorageApi;
    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private OperateCaptureService operateCaptureService;
    @Resource
    private EcmAppAttrMapper ecmAppAttrMapper;

    /*todo 使用config 去配置     @Resource(BUSI_LOG_TASK_EXE）用异步线程池*/
    private final ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    public ApiService(@Qualifier("customTaskExecutor") ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /**
     * 影像采集调阅
     *
     * @param request 调阅入参
     */
    public EcmPageBaseInfoDTO accessEcmOldECM0001(HttpServletRequest request, HttpServletResponse response) {
        try {
            EcmRootDataDTO vo = getEcmRootDataDTOEcm0001(request);
            EcmPageBaseInfoDTO dto = openApiService.businessDataService(vo, true);
            getReturnEcmPageBaseInfoDTOEcm0002(dto, vo);
            response.sendRedirect(dto.getPageUrl());
            //openURL(dto.getPageUrl());
            return null;
        } catch (Exception e) {
            log.error("处理异常", e);
            throw new OldToNewException(e.getMessage());
        }
    }

    /**
     * 影像查询调阅
     *
     * @param request 调阅入参
     */
    public EcmPageBaseInfoDTO accessEcmOldECM0002(HttpServletRequest request, HttpServletResponse response) {
        try {
            EcmRootDataDTO vo = getEcmRootDataDTOEcm0002(request);
            EcmPageBaseInfoDTO dto = openApiService.businessDataService(vo, true);
            getReturnEcmPageBaseInfoDTOEcm0002(dto, vo);
            response.sendRedirect(dto.getPageUrl());
            //openURL(dto.getPageUrl());
            return dto;
        } catch (Exception e) {
            log.error("处理异常", e);
            throw new OldToNewException(e.getMessage());
        }
    }

    /**
     * 移动端影像查询
     *
     * @param request 调阅入参
     */
    public EcmPageBaseInfoDTO accessEcmOldECM10004(HttpServletRequest request, HttpServletResponse response) {
        try {
            EcmRootDataDTO vo = getEcmRootDataDTOEcm0002(request);
            EcmPageBaseInfoDTO dto = openApiService.businessDataServiceMobile(vo);
            getReturnMobieEcmPageBaseInfo(dto, vo);
            response.sendRedirect(dto.getPageUrl());
            return dto;
        } catch (Exception e) {
            log.error("处理异常", e);
            throw new OldToNewException(e.getMessage());
        }
    }

    /**
     * 移动端影像采集
     *
     * @param request 调阅入参
     */
    public EcmPageBaseInfoDTO accessEcmOldECM10003(HttpServletRequest request, HttpServletResponse response) {
        try {
            EcmRootDataDTO vo = getEcmRootDataDTOEcm0001(request);
            EcmPageBaseInfoDTO dto = openApiService.businessDataServiceMobile(vo);
            getReturnMobieEcmPageBaseInfo(dto, vo);
            response.sendRedirect(dto.getPageUrl());
            return dto;
        } catch (Exception e) {
            log.error("处理异常", e);
            throw new OldToNewException(e.getMessage());
        }
    }

    /**
     * 影像资源请求接口
     *
     * @param request 入参
     */
    public String accessEcmOldECM0010(HttpServletRequest request) {
        try {
            EcmRootDataDTO dto = getEcmRootDataDTOEcm0010(request, "0010");
            List<QueryDataVO> result = openApiService.queryData(dto);
            getReturnEcm0010(result);
            return getEcm0010Bean(result);
        } catch (Exception e) {
            log.error("处理异常", e);
            throw new OldToNewException(e.getMessage());
        }
    }


    private String getEcm0010Bean(List<QueryDataVO> result) {
        Ecm0010ResponseBean resBean = new Ecm0010ResponseBean();
        resBean.setCode(200);
        resBean.setMsg("影像统计成功");
        List<PageResponseBean> pages = new ArrayList<>();
        for (QueryDataVO vo : result) {
            for (QueryDataFileVO fileVO : vo.getFileInfoRedisEntities()) {
                PageResponseBean page = new PageResponseBean();
                page.setDocCode(fileVO.getDocCode());
                page.setFileId(fileVO.getFileId());
                page.setFileName(fileVO.getFileName());
                page.setFileUrl(fileVO.getFileFullPath());
                page.setFileSort(String.format("%.0f", fileVO.getFileSort()));
                page.setFileNo(fileVO.getFileId());
                //拼接缩略图url
                String oldPath = "api/" + IcmsConstants.NEW_FILE_URL;
                String newPath = IcmsConstants.THUMBNAIL_URL;
                String fileFullPath = fileVO.getFileFullPath();
                // 替换 URL 中的指定部分
                String newUrl = fileFullPath.replace(oldPath, newPath);
                page.setThumUrl(newUrl);
                pages.add(page);
            }
        }
        resBean.setPages(pages);
        return FunctionUtil.toXml(resBean);
    }

    /**
     * 影像资料统计接口
     *
     * @param request 入参
     */
    public String accessEcmOldECM0006(HttpServletRequest request) {
        try {
            String xml = getXml(request);
            XmlMapper xmlMapper = new XmlMapper();
            EcmBusiInfoDataDTO ecmBusiInfoDataDTO = xmlMapper.readValue(xml, EcmBusiInfoDataDTO.class);

            Result result = openApiService.statisticsDocFileNUm(ecmBusiInfoDataDTO);

            String ecm006Bean = getEcm006Bean((StatisticsDocFileNumVO) result.getData());
            return ecm006Bean;
        } catch (Exception e) {
            log.error("处理异常", e);
            throw new OldToNewException(e.getMessage());
        }
    }

    private String getEcm006Bean(StatisticsDocFileNumVO queryDataVO) {
        Ecm006ResponseBean resBean = new Ecm006ResponseBean();
        Integer count = queryDataVO.getCount();
        if (count == 0) {
            resBean.setCode(200);
            resBean.setMsg("影像统计成功,没有该批次的影像信息");
            BatchCountResponseBean countResponseBean = new BatchCountResponseBean();
            countResponseBean.setCount(0);
            resBean.setBatchCountResponseBean(countResponseBean);
        } else {
            resBean.setCode(200);
            resBean.setMsg("影像统计成功");
            List<NodeResponseBean> list = new ArrayList<>();
            List<DocFileNumVO> ecmBusiDocRedisDTOS = queryDataVO.getDocFileNumList();
            getNodesBean(list, ecmBusiDocRedisDTOS);
            BatchCountResponseBean countResponseBean = new BatchCountResponseBean();
            countResponseBean.setCount(count);
            countResponseBean.setNodeResponseBeans(list);
            resBean.setBatchCountResponseBean(countResponseBean);
        }
        return FunctionUtil.toXml(resBean);
    }

    private void getNodesBean(List<NodeResponseBean> list, List<DocFileNumVO> ecmBusiDocRedisDTOS) {
        if (CollectionUtil.isEmpty(ecmBusiDocRedisDTOS)) {
            return;
        }
        for (DocFileNumVO queryDataTreeDTO : ecmBusiDocRedisDTOS) {
            if (queryDataTreeDTO.getFileSum() != 0) {
                NodeResponseBean nodeBean = new NodeResponseBean();
                nodeBean.setNodeName(queryDataTreeDTO.getDocName());
                nodeBean.setNodeId(queryDataTreeDTO.getDocCode());
                nodeBean.setImgNum(queryDataTreeDTO.getFileSum());
                list.add(nodeBean);
            }
        }
    }


    /**
     * 资源校验
     *
     * @param request 入参
     */
    public String accessEcmOldECM0013(HttpServletRequest request) {
        BatchInfoRespBean batchInfoRespBean = new BatchInfoRespBean();
        try {
            String xml = getXml(request);
            XmlMapper xmlMapper = new XmlMapper();
            EcmBusiInfoDataDTO ecmBusiInfoDataDTO = xmlMapper.readValue(xml, EcmBusiInfoDataDTO.class);
            Result result = openApiService.ecmBusiInfoCheck(ecmBusiInfoDataDTO);
            if (result.getCode().equals(ResultCode.SUCCESS.getCode())) {
                batchInfoRespBean.setRespCode("200");
                batchInfoRespBean.setRespMsg("批次存在!");
            } else {
                batchInfoRespBean.setRespCode("400");
                batchInfoRespBean.setRespMsg("批次不存在!");
            }
        } catch (Exception e) {
            log.error("影像业务校验接口出现异常！", e);
            batchInfoRespBean.setRespCode("400");
            batchInfoRespBean.setRespMsg("影像业务校验失败!" + e.getMessage());
        }
        return batchInfoRespBean.toXml();
    }

    /**
     * 影像删除
     *
     * @param request 入参
     */
    public String accessEcmOldECM0025(HttpServletRequest request) {
        ResponseBean responseBean = new ResponseBean();
        try {
            EcmRootDataDTO vo = getCommonEcmRootDataDTO(request);
            //封装删除数据
            EcmDelVO ecmDelVO = new EcmDelVO();
            ecmDelVO.setEcmBaseInfoDTO(vo.getEcmBaseInfoDTO());
            //暂时只处理单笔批次
            EcmBusExtendDTO ecmBusExtendDTO = vo.getEcmBusExtendDTOS().get(0);
            ecmDelVO.setAppCode(ecmBusExtendDTO.getAppCode());
            ecmDelVO.setBusiNo(ecmBusExtendDTO.getBusiNo());
            Map<String, Object> extraFields = ecmBusExtendDTO.getExtraFields();
            Object pages = extraFields.get("PAGES");
            List<Long> fileIds = new ArrayList<>();
            List<String> docNo = new ArrayList<>();
            ecmDelVO.setFileIdList(fileIds);
            ecmDelVO.setDocNo(docNo);
            if (!ObjectUtils.isEmpty(pages)) {
                Map<String, Map> pagesMap = (Map) pages;
                Map<String, Object> node = pagesMap.get("NODE");
                if (!ObjectUtils.isEmpty(node)) {
                    Object docCode = node.get("ID");
                    if (!ObjectUtils.isEmpty(docCode)) {
                        docNo.add(docCode.toString());
                    }
                }
                Object page = node.get("PAGE");
                if (page instanceof Map) {
                    // 处理 pages 是 Map 的情况
                    Map<String, String> pageMap = (Map) page;
                    String fileId = pageMap.get("PAGEID");
                    fileIds.add(Long.parseLong(fileId));
                } else if (page instanceof List) {
                    // 处理 pages 是 List 的情况
                    List<Map<String, String>> pagesList = (List) page;
                    pagesList.stream().map(pageMap -> pageMap.get("PAGEID")).filter(Objects::nonNull).map(Long::parseLong).forEach(fileIds::add);
                }
            }
            Result result = openCaptureService.deleteFileByBusiOrDoc(ecmDelVO);
            responseBean.setCode(result.getCode());
            responseBean.setMsg(result.getMsg());
        } catch (Exception e) {
            log.error("处理异常", e);
            throw new OldToNewException(e.getMessage());
        }
        return FunctionUtil.toXml(responseBean);
    }


    /**
     * 下载接口
     *
     * @param request 入参
     */
    public String accessEcmOldECM0009(HttpServletRequest request) {
        ImageDownloadRespBean bean = new ImageDownloadRespBean();
        try {
            EcmDownloadFileDTO vo = getEcmDownloadFileDTO(request);
            String s = downloadFile(vo);
            //Result result=ecmOpenApiService.setBusiAttr(vo);
            bean.setRespCode("200");
            bean.setRespMsg("资源下载成功");
            bean.setImage_zip_url(s);
        } catch (Exception e) {
            log.error("处理异常", e);
            throw new OldToNewException(e.getMessage());
        }
        return bean.toXml();
    }

    private String downloadFile(EcmDownloadFileDTO ecmDownloadFileDTO) {
        //判空
        AssertUtils.isNull(ecmDownloadFileDTO.getAppCode(), "业务类型不能为空");
        AssertUtils.isNull(ecmDownloadFileDTO.getBusiNo(), "业务编号不能为空");
        Result fileInfoByBusiOrDoc = operateCaptureService.getFileInfoByBusiOrDoc(ecmDownloadFileDTO);
        Map map = (Map) fileInfoByBusiOrDoc.getData();
        String appname = (String) map.get("appname");
        String list = (String) map.get("fileList");
        List<EcmDownloadByFileIdDTO> filesByBusiOrDoc = JSONArray.parseArray(list, EcmDownloadByFileIdDTO.class);
        if (CollectionUtil.isEmpty(filesByBusiOrDoc)) {
            AssertUtils.isTrue(true, "业务无对应文件");
        }
        long l = System.currentTimeMillis();
        return getDownloadFileReturn(ecmDownloadFileDTO, l, filesByBusiOrDoc, appname);
    }

    /**
     * 索引回写接口
     *
     * @param request 入参
     */
    public String accessEcmOldECM0014(HttpServletRequest request) {
        ResponseBean responseBean = new ResponseBean();
        try {
            EditBusiAttrDTO vo = getEditBusiAttrDTO(request);
            Result result = openApiService.setBusiAttr(vo);
            responseBean.setCode(result.getCode());
            responseBean.setMsg(result.getMsg());
        } catch (Exception e) {
            log.error("处理异常", e);
            throw new OldToNewException(e.getMessage());
        }
        return FunctionUtil.toXml(responseBean);
    }

    /**
     * 影像跨业务复制
     *
     * @param request 入参
     */
    public String accessEcmOldECM0026(HttpServletRequest request) {
        ResponseBean responseBean = new ResponseBean();
        try {
            BusiDocDuplicateVO vo = getCommonBusiDocDuplicateVO(request);
            Result result = openApiService.busiDocDuplicate(vo);
            responseBean.setCode(result.getCode());
            responseBean.setMsg(result.getMsg());
        } catch (Exception e) {
            log.error("处理异常", e);
            throw new OldToNewException(e.getMessage());
        }
        return FunctionUtil.toXml(responseBean);
    }

    /**
     * 获取业务全局配置
     *
     * @param ecmBusExtendDTOS 入参
     */
    public EcmBusExtendDTO getCompressParams(EcmBusExtendDTO ecmBusExtendDTOS) {
        EcmAppDef ecmAppDef = busiCacheService.getRedisZip(ecmBusExtendDTOS.getAppCode());
        //是否压缩
        Integer isQulity = ecmAppDef.getIsResize();
        //压缩比(大小)
        Integer resiz = ecmAppDef.getResize();
        //压缩质量
        Float qulity = ecmAppDef.getQulity();
        ecmBusExtendDTOS.setIsCompress(isQulity.toString());
        ecmBusExtendDTOS.setCompressSize(resiz.toString());
        ecmBusExtendDTOS.setCompressValue(qulity.toString());
        return ecmBusExtendDTOS;
    }

    /**
     * 文件校验及上传
     */
    public EcmBusiFileInfoDTO checkBusiAndFile(UploadFileDTO dto, UploadAllDTO splitDTO) {
        EcmBusiFileInfoDTO ecmBusiFileInfoDTO = new EcmBusiFileInfoDTO();
        EcmBusExtendDTO ecmBusExtendDTOS = splitDTO.getEcmRootDataDTO().getEcmBusExtendDTOS();
        //计算文件的md5,并压缩文件
        Map<String, FileAndSortDTO> md5s = MD5Util.getMd5(dto.getFileAndSortDTOS(), ecmBusExtendDTOS);
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
            //文件关联业务
            associationBusi(files, ecmFileInfoDTO, splitDTO.getEcmRootDataDTO());
            ecmBusiFileInfoDTO.setRepeatFileMd5List(repeatFileList);
            ecmBusiFileInfoDTO.setEcmFileInfoDTO(ecmFileInfoDTO);
            ecmBusiFileInfoDTO.setSaveFileSucc(files);
        }
        return ecmBusiFileInfoDTO;
    }

    /**
     * 文件上传校验
     */
    private Map<String, Object> getCheckByFile(EcmUploadAllDTO splitDTO) {
        Map<String, Object> map = new HashMap<>();
        //调用ecm文件校验
        Result result = checkFile(splitDTO);
        List<FileDTO> noRightFileTypeList = new ArrayList<>();
        List<FileDTO> repeatFile = new ArrayList<>();
        List<FileDTO> matchFileList = new ArrayList<>();
        EcmFileInfoDTO ecmFileInfoDTO = new EcmFileInfoDTO();
        Object data = result.getData();
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
     * 是否允许上传
     */
    private Result checkFile(EcmUploadAllDTO dto) {
        return openApiService.checkFile(dto);
    }

    /**
     * 文件上传到存储服务
     */
    private List<SysFileApiDTO> fileUploadToStorage(EcmFileInfoDTO ecmFileInfoDTO, List<FileDTO> matchFileList, Map<String, FileAndSortDTO> md5s) {
        List<SysFileApiDTO> resultList = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(matchFileList.size());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < matchFileList.size(); i++) {
            FileDTO fileDTO = matchFileList.get(i);
            FileAndSortDTO fileAndSortDTO = md5s.get(fileDTO.getFileMd5());
            fileDTO.setFile(fileAndSortDTO.getMultipartFile());
            if (fileDTO.getFile() == null) {
                latch.countDown();
                continue;
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                SysFileApiDTO sysFileApiDTO = null;
                try {
                    sysFileApiDTO = processSingleFileUpload(ecmFileInfoDTO, fileDTO);
                } catch (IOException e) {
                    log.error("处理异常", e);
                    throw new RuntimeException(e);
                }
                resultList.add(sysFileApiDTO);
                latch.countDown();
            }, taskExecutor);
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("处理异常", e);
        }
        return resultList;
    }

    /**
     * 文件上传
     */
    private SysFileApiDTO processSingleFileUpload(EcmFileInfoDTO ecmFileInfoDTO, FileDTO fileDTO) throws IOException {
        UploadListVO fileUploadDTO = new UploadListVO();
        fileUploadDTO.setFileByte(fileDTO.getFile().getBytes());
        fileUploadDTO.setFileName(fileDTO.getFile().getOriginalFilename());
        fileUploadDTO.setStEquipmentId(ecmFileInfoDTO.getEquipmentId());
        fileUploadDTO.setUserId(0L);
        fileUploadDTO.setFileSource("OpenApi");
        fileUploadDTO.setMd5(fileDTO.getFileMd5());
        //是否加密
        fileUploadDTO.setIsEncrypt(ecmFileInfoDTO.getIsEncrypt() == null ? 0 : ecmFileInfoDTO.getIsEncrypt());
        Result result = fileStorageApi.upload(fileUploadDTO);
        System.out.println("打印上传结果：" + JSONObject.toJSON(result));
        if (result.isSucc() && result.getData() != null) {
            SysFileDTO data = (SysFileDTO) result.getData();
            SysFileApiDTO sysFileApiDTO = new SysFileApiDTO();
            BeanUtil.copyProperties(data, sysFileApiDTO);
            return sysFileApiDTO;
        }
        return null;
    }

    /**
     * 文件关联业务信息并入库
     */
    private void associationBusi(List<SysFileApiDTO> files, EcmFileInfoDTO ecmFileInfoDTO, AddBusiDTO addBusiDTO) {
        EcmFileInfoApiDTO ecmFileInfoApiDTO = new EcmFileInfoApiDTO();
        ecmFileInfoApiDTO.setEcmFileInfoDTO(ecmFileInfoDTO);
        ecmFileInfoApiDTO.setAddBusiDTO(addBusiDTO);
        ecmFileInfoApiDTO.setFiles(files);
        if (CollectionUtil.isNotEmpty(files)) {
            insertFileInfo(ecmFileInfoApiDTO);
        }
    }

    /**
     * 插入文件信息
     */
    private Result insertFileInfo(EcmFileInfoApiDTO ecmFileInfoApiDTO) {
        return fileInfoService.insertFileListInfo(ecmFileInfoApiDTO);
    }


    private void getReturnEcmPageBaseInfoDTOEcm0002(EcmPageBaseInfoDTO dto, EcmRootDataDTO ecmRootDataDTO) {
//        String accessToken = GetTokenUtils.getAccessTokenByResponse(ip,accessTokenUrl,appId,appSecret);
//        dto.setAccessToken(accessToken);
        dto.setRole(ecmRootDataDTO.getEcmBaseInfoDTO().getRoleCode());
        dto.setAppId(appId);
        try {
            //跳转路径解密
            dto.setPageUrl(dto.getPageUrl());
        } catch (Exception e) {
            log.error("跳转页面加密有误");
        }
//        dto.setPageUrl("http://127.0.0.1:8040/sunIcms/#/imageContentManagement");
        if (StrUtil.equals(ApiConstants.SIGNAL_SCAN, ecmRootDataDTO.getEcmBaseInfoDTO().getOneBatch())) {
            //单扫
            dto.setPageUrl(dto.getPageUrl() + "?sun=true&token=" + dto.getAccessToken() + "&role=" + dto.getRole() + "&appId="
                    + dto.getAppId() + "&nonce=" + dto.getNonce() + "&usercode=" + ecmRootDataDTO.getEcmBaseInfoDTO().getUserCode() + "&username=" + ecmRootDataDTO.getEcmBaseInfoDTO().getUserName()
                    + "&orgCode=" + ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode() + "&orgName=" + ecmRootDataDTO.getEcmBaseInfoDTO().getOrgName() + "&busiId=" + dto.getBusiId() + "&isDefaultShowDialog=0");
            if (ecmRootDataDTO.getEcmBaseInfoDTO().getIsScan().equals(ApiConstants.HAVE_CAPTURE)) {
                //采集
                dto.setPageUrl(dto.getPageUrl() + "&pageNonce=" + dto.getNonce());
            } else {
                //调阅
                dto.setPageUrl(dto.getPageUrl() + "&openType=view&busIds=" + ecmRootDataDTO.getEcmBusExtendDTOS().size() + "&pageNonce=" + dto.getNonce());
            }
        } else {
            //批扫
            dto.setPageUrl(dto.getPageUrl() + "?sun=true&token=" + dto.getAccessToken() + "&role=" + dto.getRole() + "&appId="
                    + dto.getAppId() + "&nonce=" + dto.getNonce() + "&usercode=" + ecmRootDataDTO.getEcmBaseInfoDTO().getUserCode() + "&username=" + ecmRootDataDTO.getEcmBaseInfoDTO().getUserName()
                    + "&orgCode=" + ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode() + "&orgName=" + ecmRootDataDTO.getEcmBaseInfoDTO().getOrgName() + "&isDefaultShowDialog=1&businessOperateType=0" + "&pageNonce=" + dto.getNonce());

        }
    }


    private void getReturnMobieEcmPageBaseInfo(EcmPageBaseInfoDTO dto, EcmRootDataDTO ecmRootDataDTO) {
        dto.setRole(ecmRootDataDTO.getEcmBaseInfoDTO().getRoleCode());
        dto.setAppId(appId);
        try {
            //跳转路径解密
            dto.setPageUrl(dto.getPageUrl());
        } catch (Exception e) {
            log.error("跳转页面加密有误");
        }
        if (StrUtil.equals(ApiConstants.SIGNAL_SCAN, ecmRootDataDTO.getEcmBaseInfoDTO().getOneBatch())) {
            //单扫
            dto.setPageUrl(dto.getPageUrl() + "?sun=true&token=" + dto.getAccessToken() + "&role=" + dto.getRole() + "&appId="
                    + dto.getAppId() + "&nonce=" + dto.getNonce() + "&usercode=" + ecmRootDataDTO.getEcmBaseInfoDTO().getUserCode() + "&username=" + ecmRootDataDTO.getEcmBaseInfoDTO().getUserName()
                    + "&orgCode=" + ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode() + "&orgName=" + ecmRootDataDTO.getEcmBaseInfoDTO().getOrgName());
            if (ecmRootDataDTO.getEcmBaseInfoDTO().getIsScan().equals(ApiConstants.HAVE_CAPTURE)) {
                //采集
                dto.setPageUrl(dto.getPageUrl() + "&businessOperateType=1");
            } else {
                //调阅
                dto.setPageUrl(dto.getPageUrl() + "&openType=view&busIds=" + ecmRootDataDTO.getEcmBusExtendDTOS().size() + "&businessOperateType=2");
            }
        } else {
            //批扫
            dto.setPageUrl(dto.getPageUrl() + "?sun=true&token=" + dto.getAccessToken() + "&role=" + dto.getRole() + "&appId="
                    + dto.getAppId() + "&nonce=" + dto.getNonce() + "&usercode=" + ecmRootDataDTO.getEcmBaseInfoDTO().getUserCode() + "&username=" + ecmRootDataDTO.getEcmBaseInfoDTO().getUserName()
                    + "&orgCode=" + ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode() + "&orgName=" + ecmRootDataDTO.getEcmBaseInfoDTO().getOrgName() + "&isDefaultShowDialog=1&businessOperateType=0");

        }
    }

    private void getReturnEcm0010(List<QueryDataVO> result) {
        String accessToken = GetTokenUtils.getAccessTokenByResponse(ip, accessTokenUrl, appId, appSecret);
        for (QueryDataVO vo : result) {
            if (CollectionUtil.isEmpty(vo.getFileInfoRedisEntities())) {
                continue;
            }
            vo.getFileInfoRedisEntities().forEach(m -> {
                m.setFileFullPath(m.getFileFullPath() + "&token=" + accessToken + "&appid=" + appId);
            });
        }
    }

    private EcmRootDataDTO getEcmRootDataDTOEcm0010(HttpServletRequest request, String ecmCode) throws Exception {
        //获取基础ecmRootDataDTO
        EcmRootDataDTO ecmRootDataDTO = null;
        if ("0010".equals(ecmCode)) {
            ecmRootDataDTO = getEcm0010DTO(request);
        } else {
            ecmRootDataDTO = getCommonEcmRootDataDTO(request);
        }
        //对ecmRootDataDTO数据进行详细封装
        EcmBaseInfoDTO dto = new EcmBaseInfoDTO();
        if (ecmRootDataDTO == null || ecmRootDataDTO.getEcmBaseInfoDTO() == null) {
            throw new SunyardException("用户信息不能为空");
        }
        BeanUtils.copyProperties(ecmRootDataDTO.getEcmBaseInfoDTO(), dto);
        //必传 todo 必传参数需要有校验，不能给默认值
//        dto.setOrgCode(ecmRootQueryDTO.getEcmBaseInfoDTO().getOrgCode()!=null?ecmRootQueryDTO.getEcmBaseInfoDTO().getOrgCode():"jg01");
//        dto.setOrgName(ecmRootQueryDTO.getEcmBaseInfoDTO().getOrgName()!=null?ecmRootQueryDTO.getEcmBaseInfoDTO().getOrgName():"机构01");
//        dto.setUserCode(ecmRootQueryDTO.getEcmBaseInfoDTO().getUserCode()!=null?ecmRootQueryDTO.getEcmBaseInfoDTO().getUserCode():"user01");
//        dto.setUserName(ecmRootQueryDTO.getEcmBaseInfoDTO().getUserName()!=null?ecmRootQueryDTO.getEcmBaseInfoDTO().getUserName():"小饶同学");
//        dto.setRoleCode(ecmRootQueryDTO.getEcmBaseInfoDTO().getRoleCode()!=null?ecmRootQueryDTO.getEcmBaseInfoDTO().getRoleCode():"bbb");
//        //默认单扫
        dto.setOneBatch(ecmRootDataDTO.getEcmBaseInfoDTO().getOneBatch() != null ? ecmRootDataDTO.getEcmBaseInfoDTO().getOneBatch() : "1");
        ecmRootDataDTO.setEcmBaseInfoDTO(dto);
        if ("0006".equals(ecmCode)) {
            getEcmbusiAttrForEcm0006(ecmRootDataDTO);
        } else {
            getEcmbusiAttrForEcm0013(ecmRootDataDTO);
        }
        return ecmRootDataDTO;
    }

    private void getEcmbusiAttrForEcm0013(EcmRootDataDTO ecmRootDataDTO) {
        EcmBusExtendDTO ecmBusExtendDTO = ecmRootDataDTO.getEcmBusExtendDTOS().get(0);
        List<EcmBusiAttrDTO> ecmBusiAttrDTOList = new ArrayList<>();
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(new LambdaQueryWrapper<EcmAppAttr>()
                .eq(EcmAppAttr::getAppCode, ecmBusExtendDTO.getAppCode()));
        List<EcmAppAttr> collect = ecmAppAttrs.stream().filter(p -> StateConstants.COMMON_ONE.equals(p.getIsKey())).collect(Collectors.toList());
        EcmBusiAttrDTO ecmBusiAttrDTO = new EcmBusiAttrDTO();
        //暂时写死 入参xml中没传
        ecmBusiAttrDTO.setAttrCode(collect.get(0).getAttrCode());
        ecmBusiAttrDTO.setAppAttrValue(ecmBusExtendDTO.getBusiNo());
        ecmBusiAttrDTOList.add(ecmBusiAttrDTO);
        ecmBusExtendDTO.setEcmBusiAttrDTOList(ecmBusiAttrDTOList);
    }

    private void getEcmbusiAttrForEcm0002(EcmRootDataDTO ecmRootDataDTO) {
        for (EcmBusExtendDTO ecmBusExtendDTO : ecmRootDataDTO.getEcmBusExtendDTOS()) {
            List<EcmBusiAttrDTO> ecmBusiAttrDTOList = new ArrayList<>();
            List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(new LambdaQueryWrapper<EcmAppAttr>()
                    .eq(EcmAppAttr::getAppCode, ecmBusExtendDTO.getAppCode()));
            List<EcmAppAttr> collect = ecmAppAttrs.stream().filter(p -> StateConstants.COMMON_ONE.equals(p.getIsKey())).collect(Collectors.toList());
            EcmBusiAttrDTO ecmBusiAttrDTO = new EcmBusiAttrDTO();
            //暂时写死 入参xml中没传
            ecmBusiAttrDTO.setAttrCode(collect.get(0).getAttrCode());
            ecmBusiAttrDTO.setAppAttrValue(ecmBusExtendDTO.getBusiNo());
            ecmBusiAttrDTOList.add(ecmBusiAttrDTO);
            ecmBusExtendDTO.setEcmBusiAttrDTOList(ecmBusiAttrDTOList);
        }
    }

    private void getEcmbusiAttrForEcm0006(EcmRootDataDTO ecmRootDataDTO) {
        EcmBusExtendDTO ecmBusExtendDTO = ecmRootDataDTO.getEcmBusExtendDTOS().get(0);
        List<EcmBusiAttrDTO> ecmBusiAttrDTOList = new ArrayList<>();
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(new LambdaQueryWrapper<EcmAppAttr>()
                .eq(EcmAppAttr::getAppCode, ecmBusExtendDTO.getAppCode()));
        List<EcmAppAttr> collect = ecmAppAttrs.stream().filter(p -> StateConstants.COMMON_ONE.equals(p.getIsKey())).collect(Collectors.toList());
        EcmBusiAttrDTO ecmBusiAttrDTO = new EcmBusiAttrDTO();
        //主索引
        ecmBusiAttrDTO.setAttrCode(collect.get(0).getAttrCode());
        ecmBusiAttrDTO.setAppAttrValue(ecmBusExtendDTO.getBusiNo());
        ecmBusiAttrDTOList.add(ecmBusiAttrDTO);
        //拓展索引
        Map<String, Object> extraFields = ecmBusExtendDTO.getExtraFields();
        extraFields.forEach((key, value) -> {
            EcmBusiAttrDTO dto = new EcmBusiAttrDTO();
            //暂时写死 入参xml中没传
            dto.setAttrCode(key);
            dto.setAppAttrValue(value.toString());
            ecmBusiAttrDTOList.add(dto);
        });

        ecmBusExtendDTO.setEcmBusiAttrDTOList(ecmBusiAttrDTOList);
    }


    private EcmRootDataDTO getEcmRootDataDTOEcm0002(HttpServletRequest request) throws Exception {
        //获取基础ecmRootDataDTO
        EcmRootDataDTO ecmRootDataDTO = getCommonEcmRootDataDTO(request);
        //对ecmRootDataDTO数据进行详细封装
        EcmRootDataDTO dataDTO = new EcmRootDataDTO();
        EcmBaseInfoDTO dto = ecmRootDataDTO.getEcmBaseInfoDTO();
        dto.setOneBatch(StrUtil.isNotBlank(dto.getOneBatch()) ? ecmRootDataDTO.getEcmBaseInfoDTO().getOneBatch() : "1");
        //非必传
        dto.setTypeTree(ecmRootDataDTO.getEcmBaseInfoDTO().getTypeTree());
        dto.setOneBatch(ecmRootDataDTO.getEcmBaseInfoDTO().getOneBatch());
        //查看
        dto.setIsScan(ApiConstants.ONLY_SHOW);
        //未使用：缓存机构、默认压缩大小、业务控制参数
        dataDTO.setEcmBaseInfoDTO(dto);
        dataDTO.setEcmBusExtendDTOS(ecmRootDataDTO.getEcmBusExtendDTOS());
        if (CollectionUtil.isEmpty(dataDTO.getEcmBusExtendDTOS())) {
            dataDTO.getEcmBaseInfoDTO().setOneBatch(ApiConstants.BATCH_SCAN);
        } else {
            dataDTO.getEcmBaseInfoDTO().setOneBatch(ApiConstants.SIGNAL_SCAN);
        }
        getEcmbusiAttrForEcm0002(dataDTO);
        return dataDTO;
    }

    private EcmRootDataDTO getEcmRootDataDTOEcm0001(HttpServletRequest request) throws Exception {
        //获取基础ecmRootDataDTO
        EcmRootDataDTO ecmRootDataDTO = getCommonEcmRootDataDTO(request);
        //对ecmRootDataDTO数据进行详细封装
        EcmRootDataDTO dataDTO = new EcmRootDataDTO();
        EcmBaseInfoDTO dto = ecmRootDataDTO.getEcmBaseInfoDTO();
        dto.setOneBatch(StrUtil.isNotBlank(dto.getOneBatch()) ? ecmRootDataDTO.getEcmBaseInfoDTO().getOneBatch() : "1");
//        EcmBaseInfoDTO dto = getEcmBaseInfoDTO(ecmRootDataDTO);
        //非必传
        dto.setOrgCode(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode());
        dto.setTypeTree(ecmRootDataDTO.getEcmBaseInfoDTO().getTypeTree());
        dto.setOneBatch(ecmRootDataDTO.getEcmBaseInfoDTO().getOneBatch());
        //采集
        dto.setIsScan(ApiConstants.HAVE_CAPTURE);
        //未使用：缓存机构、默认压缩大小、业务控制参数
        dataDTO.setEcmBaseInfoDTO(dto);
        dataDTO.setEcmBusExtendDTOS(ecmRootDataDTO.getEcmBusExtendDTOS());
        if (CollectionUtil.isEmpty(dataDTO.getEcmBusExtendDTOS())) {
            dataDTO.getEcmBaseInfoDTO().setOneBatch(ApiConstants.BATCH_SCAN);
        } else {
            dataDTO.getEcmBaseInfoDTO().setOneBatch(ApiConstants.SIGNAL_SCAN);
        }
        getEcmbusiAttrForEcm0002(dataDTO);
        return dataDTO;
    }

    /**
     * 通用xml转EcmRootDataDTO
     *
     * @param request 调阅入参
     */
    private EcmRootDataDTO getCommonEcmRootDataDTO(HttpServletRequest request) throws Exception {
        String xml = getXml(request);
        return FunctionUtil.getEcmRootDataDTO(xml);
    }

    /**
     * 通用xml转EcmRootDataDTO
     *
     * @param request 调阅入参
     */
    private EcmRootDataDTO getEcm0010DTO(HttpServletRequest request) throws Exception {
        String xml = getXml(request);
        XmlMapper xmlMapper = new XmlMapper();
        EcmRootDataDTO ecmRootDataDTO = new EcmRootDataDTO();
        BusiQueryDataDTO busiQueryDataDTO = xmlMapper.readValue(xml, BusiQueryDataDTO.class);
        ecmRootDataDTO.setEcmBaseInfoDTO(busiQueryDataDTO.getEcmBaseInfoDTO());
        List<EcmBusExtendDTO> list = new ArrayList();
        EcmBusExtendDTO ecmBusExtendDTO = busiQueryDataDTO.getEcmQueryMetaDataDTO().getEcmBusExtendDTO();
        ecmBusExtendDTO.setFileIds(busiQueryDataDTO.getEcmQueryMetaDataDTO().getPageIds());
        ecmBusExtendDTO.setEcmDocCodes(busiQueryDataDTO.getEcmQueryMetaDataDTO().getDocNos());
        list.add(ecmBusExtendDTO);
        ecmRootDataDTO.setEcmBusExtendDTOS(list);
        return ecmRootDataDTO;
    }


    /**
     * 通用xml转BusiDocDuplicateVO
     *
     * @param request 调阅入参
     */
    private EcmDownloadFileDTO getEcmDownloadFileDTO(HttpServletRequest request) throws Exception {
        String xml = getXml(request);
        EcmDownloadFileDTO ecmDownloadFileDTO = FunctionUtil.getEcmDownloadFileDTO(xml);
        ecmDownloadFileDTO.setIsPack(StateConstants.COMMON_ONE);
        //todo  什么问题？？
        ecmDownloadFileDTO.setPath(filePath);
        return ecmDownloadFileDTO;
    }

    /**
     * 通用xml转BusiDocDuplicateVO
     *
     * @param request 调阅入参
     */
    private EditBusiAttrDTO getEditBusiAttrDTO(HttpServletRequest request) throws Exception {
        String xml = getXml(request);
        EditBusiAttrOutDTO editBusiAttrOutDTO = FunctionUtil.getEditBusiAttrOutDTO(xml);
        EditBusiAttrDTO dto = new EditBusiAttrDTO();
        dto.setEcmBaseInfoDTO(editBusiAttrOutDTO.getEcmBaseInfoDTO());
        if (!CollectionUtils.isEmpty(editBusiAttrOutDTO.getEcmBusExtendDTOS())) {
            dto.setAppCode(editBusiAttrOutDTO.getEcmBusExtendDTOS().get(0).getAppCode());
            dto.setBusiNo(editBusiAttrOutDTO.getEcmBusExtendDTOS().get(0).getBusiNo());
        }
        Map<String, Object> extraFields = editBusiAttrOutDTO.getExtraFields();
        List<EcmBusiAttrDTO> list = new ArrayList<>();
        Object data = extraFields.get("WRITEBACK_META_DATA");
        Map<String, Object> map = (Map) data;
        map.forEach((key, value) -> {
            if ("BUSI_NO".equals(key)) {
                List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(new LambdaQueryWrapper<EcmAppAttr>()
                        .eq(EcmAppAttr::getAppCode, dto.getAppCode()));
                List<EcmAppAttr> collect = ecmAppAttrs.stream().filter(p -> StateConstants.COMMON_ONE.equals(p.getIsKey())).collect(Collectors.toList());
                EcmBusiAttrDTO ecmBusiAttrDTO = new EcmBusiAttrDTO();
                //暂时写死 入参xml中没传
                ecmBusiAttrDTO.setAttrCode(collect.get(0).getAttrCode());
                ecmBusiAttrDTO.setAppAttrValue(value.toString());
                list.add(ecmBusiAttrDTO);
            } else {
                EcmBusiAttrDTO ecmBusiAttrDTO = new EcmBusiAttrDTO();
                //暂时写死 入参xml中没传
                ecmBusiAttrDTO.setAttrCode(key);
                ecmBusiAttrDTO.setAppAttrValue(value.toString());
                list.add(ecmBusiAttrDTO);
            }
        });
        dto.setEcmBusiAttrDTOList(list);
        return dto;
    }

    /**
     * 通用xml转BusiDocDuplicateVO
     *
     * @param request 调阅入参
     */
    private BusiDocDuplicateVO getCommonBusiDocDuplicateVO(HttpServletRequest request) throws Exception {
        String xml = getXml(request);
        BusiDocDuplicateVO busiDocDuplicateVO = FunctionUtil.getBusiDocDuplicateVO(xml);
        //需要查库补全业务属性List
        List<BusiDocDuplicateTarVO> busiDocDuplicateVos = busiDocDuplicateVO.getBusiDocDuplicateVos();
        for (BusiDocDuplicateTarVO ecmBusExtendDTO : busiDocDuplicateVos) {
            //赋值扩展属性
            setEcmBusiAttrDTOList(ecmBusExtendDTO.getEcmBusExtendDTO().getEcmBusExtendDTOS());
        }
        return busiDocDuplicateVO;
    }

    /**
     * 赋值扩展属性中的业务属性
     *
     * @param ecmBusExtendDTO 调阅入参
     */
    public void setEcmBusiAttrDTOList(EcmBusExtendDTO ecmBusExtendDTO) {
        Map<String, String> map = (Map<String, String>) operateCaptureService.getBusiAttrInfo(ecmBusExtendDTO.getAppCode(), ecmBusExtendDTO.getBusiNo());
        //将索引值赋进去
        List<EcmBusiAttrDTO> ecmBusiAttrDTOList = new ArrayList<>();
        ecmBusExtendDTO.setEcmBusiAttrDTOList(ecmBusiAttrDTOList);
        if (!ObjectUtils.isEmpty(map)) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                EcmBusiAttrDTO ecmBusiAttrDTO = new EcmBusiAttrDTO();
                ecmBusiAttrDTO.setAttrCode(entry.getKey());
                ecmBusiAttrDTO.setAppAttrValue(entry.getValue());
                ecmBusiAttrDTOList.add(ecmBusiAttrDTO);
            }
        } else {
            //不存在的业务直接给业务主索引
            EcmAppAttr ecmAppAttr = operateCaptureService.getAppMainAttr(ecmBusExtendDTO.getAppCode());
            if (ObjectUtils.isEmpty(ecmAppAttr)) {
                throw new RuntimeException("业务类型错误");
            }
            EcmBusiAttrDTO ecmBusiAttrDTO = new EcmBusiAttrDTO();
            ecmBusiAttrDTO.setAttrCode(ecmAppAttr.getAttrCode());
            ecmBusiAttrDTO.setAppAttrValue(ecmBusExtendDTO.getBusiNo());
            ecmBusiAttrDTOList.add(ecmBusiAttrDTO);
        }
    }

    /**
     * 下载返回值处理
     */
    private String getDownloadFileReturn(EcmDownloadFileDTO ecmDownloadFileDTO, long l, List<EcmDownloadByFileIdDTO> filesByBusiOrDoc, String appname) {
        try {
            String folderToCompress = ecmDownloadFileDTO.getPath() + "/" + l;
            File file2 = new File(folderToCompress);
            if (file2.exists()) {
                FileUtils.deleteDirectory(file2);

            }
            CountDownLatch latch = new CountDownLatch(filesByBusiOrDoc.size());
            for (EcmDownloadByFileIdDTO file : filesByBusiOrDoc) {
                //Executor taskExecutor = BeanUtils.getBean(Executor.class);
                taskExecutor.execute(() -> {
                    downloadFileById(file, ecmDownloadFileDTO.getPath() + "/" + l, ecmDownloadFileDTO, latch);
                    latch.countDown();

                });
            }

            latch.await();

            printXmlByDownload(ecmDownloadFileDTO, filesByBusiOrDoc, ecmDownloadFileDTO.getPath() + "/" + l + "/busiDownload.xml", appname);
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
            //l + ".zip"为文件名 拼接下载接口路径
            return downloadUrl + l + ".zip";
        } catch (Exception e) {
            log.error("处理异常", e);
            return null;
        }
    }

    //todo static
    public static void printXmlByDownload(EcmDownloadFileDTO ecmDownloadFileDTO,
                                          List<EcmDownloadByFileIdDTO> filesByBusiOrDoc,
                                          String pathxml,
                                          String appname) {
        try (FileOutputStream fos = new FileOutputStream(pathxml)) {
            XMLStreamWriter writer = null;
            try {
                // 创建XMLStreamWriter
                writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fos, "UTF-8");

                // 写入XML内容（和之前相同）
                writer.writeStartDocument("UTF-8", "1.0");
                writer.writeStartElement("root");

                // 写入DocInfo子元素
                writer.writeStartElement("DocInfo");
                writer.writeStartElement("APP_CODE");
                writer.writeCharacters(ecmDownloadFileDTO.getAppCode());
                writer.writeEndElement(); // 结束APP_CODE

                writer.writeStartElement("APP_NAME");
                writer.writeCharacters(appname);
                writer.writeEndElement(); // 结束APP_NAME

                writer.writeStartElement("BUSI_NO");
                writer.writeCharacters(ecmDownloadFileDTO.getBusiNo());
                writer.writeEndElement(); // 结束BUSI_NO
                writer.writeEndElement(); // 结束DocInfo

                // 写入PAGES子元素
                writer.writeStartElement("PAGES");
                for (EcmDownloadByFileIdDTO d : filesByBusiOrDoc) {
                    writer.writeStartElement("PAGE");
                    writer.writeAttribute("DOC_CODE", d.getDocCode());
                    writer.writeAttribute("DOC_NAME", d.getDocName());
                    writer.writeAttribute("FILE_ID", d.getFileId().toString());
                    writer.writeAttribute("FILE_NAME", d.getNewFileName());
                    writer.writeAttribute("PAGE_URL", d.getNewFileId() + "." + d.getFormat());
                    writer.writeAttribute("CREATE_USER", d.getCreateUser());
                    writer.writeEndElement(); // 结束PAGE
                }
                writer.writeEndElement(); // 结束PAGES

                writer.writeEndElement(); // 结束root
                writer.writeEndDocument();

            } finally {
                // 手动关闭XMLStreamWriter（确保资源释放）
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (XMLStreamException e) {
                        log.error("处理异常", e);
                    }
                }
            }

        } catch (Exception e) {
            log.error("处理异常", e);
        }
    }


    /**
     * 异步下载文件
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
        inputStreamToFile(inputStream, targetFile);
    }

    /**
     * 输入流转文件
     */
    //todo static
    public static void inputStreamToFile(InputStream ins, File file) {
        BufferedOutputStream bos = null;
        BufferedInputStream bis = new BufferedInputStream(ins);
        try {
            //设置为true表示追加
            bos = new BufferedOutputStream(new FileOutputStream(file, true));
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = bis.read(buffer, 0, 8192)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException e) {
                }
                ins = null;
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                }
                bos = null;
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                }
                bis = null;
            }
        }
    }

    /**
     * 文件下载
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
        InputStream connectFile = null;
        //断点下载方式
        DownFileVO vo = new DownFileVO();
        vo.setFileId(list);
        vo.setIsPack(0);
        vo.setUsername(ecmDownloadFileDTO.getEcmBaseInfoDTO().getUserName());
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url = storageIp;
            HttpPost httpPost = new HttpPost(url);
            // 设置请求头
            httpPost.setHeader("Content-Type", "application/json");
            // 设置请求体
            StringEntity entity = new StringEntity(JSON.toJSONString(vo), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            CloseableHttpResponse execute = httpClient.execute(httpPost);
            connectFile = execute.getEntity().getContent();
            return connectFile;
        } catch (IOException e) {
            log.error("处理异常", e);
        }
        //InputStream connectFile = getConnectFile(map, ApiConstants.SHARDINGDOWNLOAD, rangeStart);
        return null;
    }

    /**
     * 文件名处理
     */
    //todo static
    public static File generateTargetFile(String path, String originalFileName, Long fileId) {
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String newFileName = fileId + fileExtension;
        return new File(path + "/" + newFileName);
    }

    private String getXml(HttpServletRequest request) {
        Map<String, String[]> requestParams = request.getParameterMap();
        String data = "";
        Map<String, Object> decode = new HashMap<>();
        if (requestParams.get("data") == null) {
            data = requestParams.keySet().iterator().next();
        } else {
            data = requestParams.get("data")[0];
        }
        if ("".equals(data)) {
            throw new SunyardException("参数错误");
        }
        //解密获取xml信息并转换为EcmRootDataDTO
        try {
            decode = ParamDecrypt.getDecodeParam(data);
        } catch (Exception e) {
            throw new SunyardException("参数错误");
        }
        String xml = decode.get("xml").toString();
        return xml;
    }

    /**
     * 下载
     */
    public void download(String fileName, HttpServletRequest request, HttpServletResponse response) {
        File file = new File(filePath + "/" + fileName);

        // 设置响应内容类型
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

        // 设置响应内容长度
        response.setContentLength((int) file.length());

        // 读取文件并写入响应
        try (FileInputStream inStream = new FileInputStream(file);
             OutputStream outStream = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        } catch (FileNotFoundException e) {
            log.error("处理异常", e);
        } catch (IOException e) {
            log.error("处理异常", e);
        }
    }

}
