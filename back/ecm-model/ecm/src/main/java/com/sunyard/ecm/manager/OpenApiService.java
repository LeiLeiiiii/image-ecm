package com.sunyard.ecm.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.annotation.LogManageAnnotation;
import com.sunyard.ecm.constant.BusiInfoConstants;
import com.sunyard.ecm.constant.DocRightConstants;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.PatternConstants;
import com.sunyard.ecm.constant.RedisConstants;
import com.sunyard.ecm.constant.RoleConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.AddBusiDTO;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.ecm.dto.EcmBaseInfoDTO;
import com.sunyard.ecm.dto.EcmBusExtendDTO;
import com.sunyard.ecm.dto.EcmBusiAttrDTO;
import com.sunyard.ecm.dto.EcmBusiAttrQueryDataDTO;
import com.sunyard.ecm.dto.EcmBusiInfoDataDTO;
import com.sunyard.ecm.dto.EcmPageBaseInfoDTO;
import com.sunyard.ecm.dto.EcmRootDataDTO;
import com.sunyard.ecm.dto.EcmRuleDataDTO;
import com.sunyard.ecm.dto.EcmSplitFileDTO;
import com.sunyard.ecm.dto.EcmUploadAllDTO;
import com.sunyard.ecm.dto.EcmUploadFileDTO;
import com.sunyard.ecm.dto.EcmUserDTO;
import com.sunyard.ecm.dto.EcmVTreeDataDTO;
import com.sunyard.ecm.dto.EditBusiAttrDTO;
import com.sunyard.ecm.dto.FileDTO;
import com.sunyard.ecm.dto.FileOcrCallBackDTO;
import com.sunyard.ecm.dto.FileOcrDTO;
import com.sunyard.ecm.dto.QueryBusiDTO;
import com.sunyard.ecm.dto.QueryDataTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiFileInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiStructureTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmDocrightDefDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmStructureTreeDTO;
import com.sunyard.ecm.dto.ecm.SysStrategyDTO;
import com.sunyard.ecm.dto.redis.EcmBusiDocRedisDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.dto.redis.UserBusiRedisDTO;
import com.sunyard.ecm.enums.EcmCheckAsyncTaskEnum;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmAppDocRelMapper;
import com.sunyard.ecm.mapper.EcmAppDocrightMapper;
import com.sunyard.ecm.mapper.EcmBusiDocMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmBusiMetadataMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmDocrightDefMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmAppDocRel;
import com.sunyard.ecm.po.EcmAppDocright;
import com.sunyard.ecm.po.EcmAsyncTask;
import com.sunyard.ecm.po.EcmBusiDoc;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmBusiMetadata;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmDocrightDef;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.service.ModelBusiService;
import com.sunyard.ecm.service.OperateCaptureService;
import com.sunyard.ecm.service.OperateFullQueryService;
import com.sunyard.ecm.service.SysStorageService;
import com.sunyard.ecm.service.SysStrategyService;
import com.sunyard.ecm.vo.AppTypeBusiVO;
import com.sunyard.ecm.vo.BusiDocDuplicateTarVO;
import com.sunyard.ecm.vo.BusiDocDuplicateVO;
import com.sunyard.ecm.vo.BusiInfoAndFileVO;
import com.sunyard.ecm.vo.DocFileNumVO;
import com.sunyard.ecm.vo.EcmBusiStorageListVO;
import com.sunyard.ecm.vo.EcmDelVO;
import com.sunyard.ecm.vo.FileInfoRedisEntityVO;
import com.sunyard.ecm.vo.FileInfoVO;
import com.sunyard.ecm.vo.MultiplexFileVO;
import com.sunyard.ecm.vo.QueryBusiFileVO;
import com.sunyard.ecm.vo.QueryBusiInfoVO;
import com.sunyard.ecm.vo.QueryDataFileVO;
import com.sunyard.ecm.vo.QueryDataVO;
import com.sunyard.ecm.vo.StatisticsDocFileNumVO;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.UUIDUtils;
import com.sunyard.framework.common.util.encryption.Md5Utils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.auth.api.OpenAuthApi;
import com.sunyard.module.system.api.DictionaryApi;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.dto.SysDictionaryDTO;
import com.sunyard.module.system.api.dto.SysParamDTO;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author lw
 * @date 2023/5/23
 * @describe 对外接口业务操作
 */
@Slf4j
@Service
public class OpenApiService {
    @Value("${icms.page.pageAddressKey:ECMS_CAPTURE_URL}")
    private String pageAddressKey;
    @Value("${storage.file.fileFullPath}")
    private String fileFullPath;
    @Value("${queryBusi.size:20}")
    private Integer queryBusiSize;
    @Value("${queryBusi.pdfExt:true}")
    private Boolean pdfExt;
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private EcmDocrightDefMapper ecmDocrightDefMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmAppAttrMapper ecmAppAttrMapper;
    @Resource
    private EcmAppDocrightMapper ecmAppDocrightMapper;
    @Resource
    private EcmBusiMetadataMapper ecmBusiMetadataMapper;
    @Resource
    private EcmBusiDocMapper ecmBusiDocMapper;
    @Resource
    private EcmAppDocRelMapper ecmAppDocRelMapper;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Resource
    private OpenAuthApi openAuthApi;
    @Resource
    private DictionaryApi dictionaryApi;
    @Resource
    private ParamApi paramApi;
    @Resource
    private SysStorageService sysStorageService;
    @Resource
    private BusiOperationService busiOperationService;
    @Resource
    private StaticTreePermissService staticTreePermissService;
    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private SysStrategyService sysStrategyService;
    @Resource
    private OperateFullQueryService operateFullQueryService;
    @Resource
    private CommonService commonService;
    @Resource
    private ModelBusiService modelBusiService;
    @Resource
    private OperateCaptureService operateCaptureService;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private AsyncTaskService asyncTaskService;
    @Resource
    private CaptureSubmitService captureSubmitService;

    /**
     * 影像查询对外接口
     */
    public List<QueryDataVO> queryData(EcmRootDataDTO ecmRootDataDTO) {
        log.info("影像查询调阅对外接口传入参数：{}", JSONUtil.toJsonStr(ecmRootDataDTO));
        //校验基本信息
        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        EcmBaseInfoDTO baseInfoDTO = ecmRootDataDTO.getEcmBaseInfoDTO();
        token.setUsername(baseInfoDTO.getUserCode());
        token.setName(baseInfoDTO.getUserName());
        token.setOrgName(baseInfoDTO.getOrgName());
        token.setOrgCode(baseInfoDTO.getOrgCode());
        token.setRoleCodeList(baseInfoDTO.getRoleCode());
        token.setOut(true);
        AccountTokenExtendDTO token1 = checkBusBase(ecmRootDataDTO, token, true);
        baseInfoDTO.setUserName(token1.getName());
        baseInfoDTO.setOrgName(token1.getOrgName());
        ecmRootDataDTO.setEcmBaseInfoDTO(baseInfoDTO);
        List<QueryDataVO> busiAndFileVOS = new ArrayList<>();
        //获取扩展信息
        List<EcmBusExtendDTO> extendDTOS = ecmRootDataDTO.getEcmBusExtendDTOS();
        extendDTOS.forEach(dto -> {
            //获取业务类型Id
            String appCode = dto.getAppCode();
            //获取业务Id
            EcmBusiInfo ecmBusiInfo = getBusiId(dto, appCode);
            QueryDataVO vo = new QueryDataVO();
            BeanUtils.copyProperties(ecmBusiInfo, vo);
            //获取属性
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token1,
                    ecmBusiInfo.getBusiId());

            ArrayList<EcmBusiAttrQueryDataDTO> objects1 = new ArrayList<>();
            for (EcmAppAttrDTO appAttrDTO : ecmBusiInfoRedisDTO.getAttrList()) {
                EcmBusiAttrQueryDataDTO attrDTO = new EcmBusiAttrQueryDataDTO();
                attrDTO.setAttrCode(appAttrDTO.getAttrCode());
                attrDTO.setAppAttrValue(appAttrDTO.getAppAttrValue());
                objects1.add(attrDTO);
            }

            vo.setAttrList(objects1);
            ArrayList<QueryDataFileVO> objects = new ArrayList<>();
            List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService
                    .getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
            if (CollectionUtil.isEmpty(fileInfoRedisEntities)) {
                fileInfoRedisEntities = new ArrayList<>();
            }
            //已删除数据不返回
            if (CollectionUtil.isNotEmpty(dto.getFileIds())) {
                fileInfoRedisEntities = fileInfoRedisEntities.stream()
                        .filter(s -> dto.getFileIds().contains(s.getFileId()))
                        .filter(s -> StateConstants.NO.equals(s.getState()))
                        .collect(Collectors.toList());
            } else if (CollectionUtil.isNotEmpty(dto.getEcmDocCodes())) {
                fileInfoRedisEntities = fileInfoRedisEntities.stream()
                        .filter(s -> dto.getEcmDocCodes().contains(s.getDocCode()))
                        .filter(s -> StateConstants.NO.equals(s.getState()))
                        .collect(Collectors.toList());

            } else {
                fileInfoRedisEntities = fileInfoRedisEntities.stream()
                        .filter(s -> StateConstants.NO.equals(s.getState()))
                        .collect(Collectors.toList());

            }

            //根据资料节点归类
            Map<String, List<FileInfoRedisDTO>> collect = fileInfoRedisEntities.stream()
                    .collect(Collectors.groupingBy(FileInfoRedisDTO::getDocCode));
            //树节点过滤,根据权限走
            List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = ecmBusiInfoRedisDTO
                    .getEcmBusiDocRedisDTOS();

            if (dto.getTypeQuery() == null
                    || IcmsConstants.TYPEQUERY_QL.equals(dto.getTypeQuery())) {
                //全量
            } else if (IcmsConstants.TYPEQUERY_ROLE.equals(dto.getTypeQuery())) {
                //根据角色过滤
                List<EcmDocrightDefDTO> currentDocRight = busiCacheService
                        .getEcmDocrightDefDTOS(token, ecmBusiInfoRedisDTO);
                if (!CollectionUtils.isEmpty(currentDocRight)) {
                    Map<String, List<EcmDocrightDefDTO>> collect1 = currentDocRight.stream()
                            .collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
                    //文件过滤
                    ArrayList<String> objects2 = new ArrayList<>();
                    ecmBusiDocRedisDTOS = setTreeDoc(ecmBusiDocRedisDTOS, collect1, objects2);
                    fileInfoRedisEntities = fileInfoRedisEntities.stream()
                            .filter(s -> objects2.contains(s.getDocCode()))
                            .collect(Collectors.toList());
                } else {
                    ecmBusiDocRedisDTOS = new ArrayList<>();
                    fileInfoRedisEntities = new ArrayList<>();
                }

            } else if (IcmsConstants.TYPEQUERY_DWD.equals(dto.getTypeQuery())) {
                //根据多维度过滤
                List<EcmDocrightDefDTO> currentDocRight = commonService.dealRuleData(dto, token,
                        ecmBusiInfoRedisDTO.getRightVer());
                if (!CollectionUtils.isEmpty(currentDocRight)) {
                    Map<String, List<EcmDocrightDefDTO>> collect1 = currentDocRight.stream()
                            .collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
                    //文件过滤
                    ArrayList<String> objects2 = new ArrayList<>();
                    ecmBusiDocRedisDTOS = setTreeDoc(ecmBusiDocRedisDTOS, collect1, objects2);
                    fileInfoRedisEntities = fileInfoRedisEntities.stream()
                            .filter(s -> objects2.contains(s.getDocCode()))
                            .collect(Collectors.toList());
                } else {
                    ecmBusiDocRedisDTOS = new ArrayList<>();
                    fileInfoRedisEntities = new ArrayList<>();
                }
            }

            if (CollectionUtil.isNotEmpty(ecmBusiDocRedisDTOS)) {
                ArrayList<QueryDataTreeDTO> queryDataTreeDTOS = handleTree(collect,
                        ecmBusiDocRedisDTOS);
                vo.setEcmBusiDocRedisDTOS(queryDataTreeDTOS);
                busiCacheService.getAllTaskType(fileInfoRedisEntities,ecmBusiInfoRedisDTO.getBusiId());
                for (FileInfoRedisDTO dto1 : fileInfoRedisEntities) {
                    QueryDataFileVO queryDataFileVo = new QueryDataFileVO();
                    StringBuffer buf2 = new StringBuffer();
                    buf2.append(fileFullPath).append("api/").append(IcmsConstants.NEW_FILE_URL)
                            .append("?").append("fileId").append("=").append(dto1.getNewFileId());
                    queryDataFileVo.setFileFullPath(buf2.toString());
                    StringBuffer sourceUrl = new StringBuffer();
                    sourceUrl.append(fileFullPath).append("api/").append(IcmsConstants.DOWNLOAD_FILE_URL)
                            .append("?").append("fileId").append("=").append(dto1.getNewFileId()).append("&").append("fileName").append("=").
                            append(dto1.getNewFileName());
                    queryDataFileVo.setSourceFileDownloadPath(sourceUrl.toString());
                    queryDataFileVo.setExt(dto1.getFormat());
                    queryDataFileVo.setFileName(dto1.getNewFileName());
                    queryDataFileVo.setDocCode(dto1.getDocCode());
                    queryDataFileVo
                            .setFileId(dto1.getFileId() != null ? dto1.getFileId() + "" : null);
                    queryDataFileVo.setFileSort(dto1.getFileSort());
                    //资源请求返回结果 新加查重标识字段
                    if (dto1.getTaskType() != null){
                        queryDataFileVo.setDupImgState(getAfmResult(dto1.getTaskType().charAt(IcmsConstants.TYPE_FOUR - 1)));
                        queryDataFileVo.setDupTextState(getAfmResult(dto1.getTaskType().charAt(IcmsConstants.TYPE_TEN - 1)));
                    }else {
                        //未开启
                        queryDataFileVo.setDupImgState(IcmsConstants.THREE);
                        queryDataFileVo.setDupTextState(IcmsConstants.THREE);
                    }
                    queryDataFileVo.setCreateUserName(dto1.getCreateUserName());
                    queryDataFileVo.setCreateTime(dto1.getCreateTime());
                    queryDataFileVo.setUpdateUserName(dto1.getUpdateUserName());
                    queryDataFileVo.setUpdateTime(dto1.getUpdateTime());
                    objects.add(queryDataFileVo);
                }

                vo.setFileInfoRedisEntities(objects);
                busiAndFileVOS.add(vo);
            } else {
                vo.setEcmBusiDocRedisDTOS(null);
                busiAndFileVOS.add(vo);
            }
        });
        return busiAndFileVOS;
    }

    private Integer getAfmResult(char afmChar) {
        //默认未开启查重
        int result = IcmsConstants.THREE;

        if (afmChar == EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0)) {
            result = IcmsConstants.ZERO;
        } else if (afmChar == EcmCheckAsyncTaskEnum.CHECK_FAILED.description().charAt(0)
                || afmChar == EcmCheckAsyncTaskEnum.CONFIRM_ANOMALY.description().charAt(0)) {
            result = IcmsConstants.ONE;
        } else if (afmChar == EcmCheckAsyncTaskEnum.SUCCESS.description().charAt(0)
                || afmChar == EcmCheckAsyncTaskEnum.EXCLUDE_ANOMALY.description().charAt(0)) {
            result = IcmsConstants.TWO;
        }
        return result;
    }

    private List<EcmBusiDocRedisDTO> setTreeDoc(List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS,
                                                Map<String, List<EcmDocrightDefDTO>> collect1,
                                                ArrayList<String> objects2) {
        List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOSHandle = new ArrayList<>();
        for (EcmBusiDocRedisDTO doc : ecmBusiDocRedisDTOS) {
            if (CollectionUtil.isNotEmpty(doc.getChildren())) {
                List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS1 = setTreeDoc(doc.getChildren(),
                        collect1, objects2);
                if (CollectionUtil.isNotEmpty(ecmBusiDocRedisDTOS1)) {
                    doc.setChildren(ecmBusiDocRedisDTOS1);
                    ecmBusiDocRedisDTOSHandle.add(doc);
                }
            } else {
                if (!org.apache.commons.collections4.CollectionUtils
                        .isEmpty(collect1.get(doc.getDocCode()))) {

                    List<String> docCodeList = checkAllDocRight(collect1.get(doc.getDocCode()));
                    if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(docCodeList)) {
                        //添加资料权限
                        objects2.add(doc.getDocCode());
                        ecmBusiDocRedisDTOSHandle.add(doc);
                    }

                }
            }
        }
        return ecmBusiDocRedisDTOSHandle;
    }

    public List<String> checkAllDocRight(List<EcmDocrightDefDTO> ecmDocrightDefList) {
        List<String> docCodeList = new ArrayList<>();
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(ecmDocrightDefList)) {
            return docCodeList;
        }
        int docSize = ecmDocrightDefList.size();
        int docCount = IcmsConstants.ZERO;
        for (EcmDocrightDefDTO right : ecmDocrightDefList) {
            if (StrUtil.equals(IcmsConstants.ONE.toString(), right.getAddRight())
                    || StrUtil.equals(IcmsConstants.ONE.toString(), right.getDeleteRight())
                    || StrUtil.equals(IcmsConstants.ONE.toString(), right.getUpdateRight())
                    || StrUtil.equals(IcmsConstants.ONE.toString(), right.getReadRight())
                    || StrUtil.equals(IcmsConstants.ONE.toString(), right.getThumRight())
                    || StrUtil.equals(IcmsConstants.ONE.toString(), right.getPrintRight())
                    || StrUtil.equals(IcmsConstants.ONE.toString(), right.getDownloadRight())) {
                docCount += 1;
                docCodeList.add(right.getDocCode());
            }
        }
        return docCodeList;
    }

    private ArrayList<QueryDataTreeDTO> handleTree(Map<String, List<FileInfoRedisDTO>> collect,
                                                   List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS) {
        ArrayList<QueryDataTreeDTO> objects1 = new ArrayList<>();
        for (EcmBusiDocRedisDTO dto1 : ecmBusiDocRedisDTOS) {
            if (StateConstants.COMMON_ONE.equals(dto1.getDocMark())) {
                continue;
            }
            QueryDataTreeDTO dto2 = new QueryDataTreeDTO();
            if (dto1.getDocCode() != null) {
                List<FileInfoRedisDTO> fileInfoRedisDTOS = collect.get(dto1.getDocCode());
                dto2.setDocCode(dto1.getDocCode());
                dto2.setFileCount(dto1.getFileCount());
                if (CollectionUtil.isNotEmpty(fileInfoRedisDTOS)) {
                    dto2.setFileCount(fileInfoRedisDTOS.size());
                    List<String> collect1 = fileInfoRedisDTOS.stream()
                            .map(FileInfoRedisDTO::getFileMd5).collect(Collectors.toList());
                    //                    dto2.setMd5List(collect1);
                }
                dto2.setName(dto1.getDocName());
                objects1.add(dto2);
            }
            if (CollectionUtil.isNotEmpty(dto1.getChildren())) {
                ArrayList<QueryDataTreeDTO> queryDataTreeDTOS = handleTree(collect,
                        dto1.getChildren());
                dto2.setChildren(queryDataTreeDTOS);
            }
        }
        return objects1;
    }

    private AccountTokenExtendDTO checkBusBase(EcmRootDataDTO ecmRootDataDTO,
                                               AccountTokenExtendDTO token, boolean isApi) {
        if (ecmRootDataDTO == null) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "参数有误");
        }
        if (ecmRootDataDTO.getEcmBaseInfoDTO() == null) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "用户信息不能为空");
        }

        EcmBaseInfoDTO ecmBaseInfoDTO = ecmRootDataDTO.getEcmBaseInfoDTO();
        if (CollectionUtil.isEmpty(ecmRootDataDTO.getEcmBusExtendDTOS())) {
            ecmBaseInfoDTO.setOneBatch(IcmsConstants.BATCH_SCAN);
        } else {
            ecmBaseInfoDTO.setOneBatch(IcmsConstants.SIGNAL_SCAN);
        }
        ecmRootDataDTO.setEcmBaseInfoDTO(ecmBaseInfoDTO);
        AccountTokenExtendDTO accountTokenExtendDTO = busiCacheService
                .checkUser(ecmRootDataDTO.getEcmBaseInfoDTO(), token);
        handleCheckBusi(ecmRootDataDTO.getEcmBusExtendDTOS(), isApi);
        return accountTokenExtendDTO;
    }

    private void handleCheckBusi(List<EcmBusExtendDTO> ecmBusExtendDTOS, boolean isApi) {
        if (!CollectionUtils.isEmpty(ecmBusExtendDTOS)) {
            //校验主键
            List<String> collect = ecmBusExtendDTOS.stream().map(EcmBusExtendDTO::getAppCode)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(collect)) {
                throw new SunyardException(ResultCode.NO_DATA_AUTH, "业务不存在!");
            }
            List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(
                    new LambdaQueryWrapper<EcmAppAttr>().in(EcmAppAttr::getAppCode, collect)
                            .eq(EcmAppAttr::getIsKey, StateConstants.YES));
            Map<String, List<EcmAppAttr>> collect1 = ecmAppAttrs.stream()
                    .collect(Collectors.groupingBy(EcmAppAttr::getAppCode));
            for (EcmBusExtendDTO dto : ecmBusExtendDTOS) {
                List<EcmAppAttr> ecmAppAttrs1 = collect1.get(dto.getAppCode());
                if (CollectionUtils.isEmpty(ecmAppAttrs1)) {
                    throw new SunyardException(ResultCode.PARAM_ERROR,
                            "业务类型appCode：" + dto.getAppCode() + "不存在");
                }
                EcmAppAttr ecmAppAttr = ecmAppAttrs1.get(0);
                List<EcmBusiAttrDTO> ecmBusiAttrDTOList = dto.getEcmBusiAttrDTOList();
                List<EcmBusiAttrDTO> collect2 = ecmBusiAttrDTOList.stream()
                        .filter(s -> ecmAppAttr.getAttrCode().equals(s.getAttrCode()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(collect2)
                        || StrUtil.isBlank(collect2.get(0).getAppAttrValue())) {
                    throw new SunyardException(ResultCode.NO_DATA_AUTH, "业务主索引不能为空!");
                }
                dto.setBusiNo(collect2.get(0).getAppAttrValue());

                String regex = ecmAppAttr.getRegex();
                if (isApi) {
                    //pc端不判断主索引是否符合规范，适配auto_业务
                    if (!StringUtils.isEmpty(regex)) {
                        //校验
                        Pattern p = Pattern.compile(regex);
                        Matcher m = p.matcher(dto.getBusiNo());
                        if (!m.matches()) {
                            throw new SunyardException(ResultCode.NO_DATA_AUTH, "业务主索引的定义不符合规则!");
                        }
                    } else {
                        //校验
                        Pattern p = Pattern.compile(PatternConstants.NUMBER_AND_ZM);
                        Matcher m = p.matcher(dto.getBusiNo());
                        if (!m.matches()) {
                            throw new SunyardException(ResultCode.NO_DATA_AUTH, "业务主索引的定义不符合规则!");
                        }
                    }
                }

                //校验动态树结构是否正确
                if (!CollectionUtils.isEmpty(dto.getEcmVTreeDataDTOS())) {
                    List<EcmVTreeDataDTO> ecmVTreeDataDTOS = dto.getEcmVTreeDataDTOS();
//                    HashMap<String, List<String>> objectObjectHashMap = new HashMap<>();
//                    handleCheckTree(ecmVTreeDataDTOS, objectObjectHashMap);
//                    List<String> list = objectObjectHashMap.get("child");
//                    List<String> parents = objectObjectHashMap.get("parents");
//                    list.addAll(parents);
//                    Set<String> collect3 = list.stream().collect(Collectors.toSet());
//                    if (list.size() != collect3.size()) {
//                        throw new SunyardException(ResultCode.PARAM_ERROR, "动态树数据有误:存在相同的资料节点代码!");
//                    }

                    // 校验动态树结构
                    validateTree(ecmVTreeDataDTOS);

                }
            }

        }
    }

    /**
     * 动态树校验
     * 1. 检查同一父节点下是否有重复 docCode
     * 2. 检查所有叶子节点的 docCode 是否在数据库中存在
     * @param treeData
     */
    public void validateTree(List<EcmVTreeDataDTO> treeData) {

        // 1. 检查是否有重复 docCode
        if (!checkDuplicateDocCodes(treeData)) {
            throw new SunyardException(ResultCode.PARAM_ERROR, "动态树数据有误:存在相同的资料节点代码!");
        }
        // 2. 检查叶子节点是否在数据库中存在
        Set<String> leafDocCodes = collectLeafDocCodes(treeData);

        //是否在redis中存在
        for (String docCode : leafDocCodes) {
            if (ObjectUtils.isEmpty(busiCacheService.getIntelligentProcessingEcmDocDef(docCode))){
                throw new SunyardException(ResultCode.PARAM_ERROR, "动态树数据有误:叶子节点未在系统中配置!");
            }
        }

    }

    public boolean checkDuplicateDocCodes(List<EcmVTreeDataDTO> treeData) {
        for (EcmVTreeDataDTO node : treeData) {
            if (!checkNode(node)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkNode(EcmVTreeDataDTO node) {
        // 检查当前节点的子节点是否有重复 docCode
        if (node.getEcmVTreeDataDTOS() != null && !node.getEcmVTreeDataDTOS().isEmpty()) {
            Set<String> childDocCodes = new HashSet<>();
            for (EcmVTreeDataDTO child : node.getEcmVTreeDataDTOS()) {
                if (childDocCodes.contains(child.getDocCode())) {
                    // 发现重复
                    return false;
                }
                childDocCodes.add(child.getDocCode());
            }

            // 递归检查子节点
            for (EcmVTreeDataDTO child : node.getEcmVTreeDataDTOS()) {
                if (!checkNode(child)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取所有的叶子节点的DocCode
     */
    public Set<String> collectLeafDocCodes(List<EcmVTreeDataDTO> treeData) {
        Set<String> leafDocCodes = new HashSet<>();
        for (EcmVTreeDataDTO node : treeData) {
            collectLeafDocCodes(node, leafDocCodes);
        }
        return leafDocCodes;
    }

    public void collectLeafDocCodes(EcmVTreeDataDTO node, Set<String> leafDocCodes) {
        if (node.getEcmVTreeDataDTOS() == null || node.getEcmVTreeDataDTOS().isEmpty()) {
            // 是叶子节点
            leafDocCodes.add(node.getDocCode());
        } else {
            // 不是叶子节点
            for (EcmVTreeDataDTO child : node.getEcmVTreeDataDTOS()) {
                collectLeafDocCodes(child, leafDocCodes);
            }
        }
    }

    private void handleCheckTree(List<EcmVTreeDataDTO> ecmVTreeDataDTOS,
                                 HashMap<String, List<String>> objectObjectHashMap) {
        ecmVTreeDataDTOS.forEach(s -> {
            if (Objects.equals(s.getChildFlag(), StateConstants.YES.toString())) {
                //是否是子节点
                //如果为子节点，则后一层级不应该有值
                if (!CollectionUtils.isEmpty(s.getEcmVTreeDataDTOS())) {
                    throw new SunyardException(ResultCode.PARAM_ERROR, "动态树数据有误:子节点未最后一层级!");
                }
                List<String> list = objectObjectHashMap.get("child");
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(s.getDocCode());
                objectObjectHashMap.put("child", list);
            } else {
                if (CollectionUtils.isEmpty(s.getEcmVTreeDataDTOS())) {
                    throw new SunyardException(ResultCode.PARAM_ERROR, "动态树数据有误：请传递完整的树结构!");
                } else {
                    List<String> list = objectObjectHashMap.get("parents");
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(s.getDocCode());
                    objectObjectHashMap.put("parents", list);
                    handleCheckTree(s.getEcmVTreeDataDTOS(), objectObjectHashMap);
                }
            }
        });
    }

    /**
     * 影像扫描或修改对外接口
     */
    public EcmPageBaseInfoDTO businessDataService(EcmRootDataDTO ecmRootDataDTO, boolean isApi) {
        log.info("影像扫描对外接口传入参数：{}", JSONUtil.toJsonStr(ecmRootDataDTO));
        EcmPageBaseInfoDTO userBaseInfo = getEcmPageBaseInfoDTO(ecmRootDataDTO, isApi);

        // 批扫返回采集页面url地址和token
        //返回页面地址
        Result result = returnScanPage(new Result());
        String pageUrl = result.getData().toString();
        try {
            if (isApi) {
                //对外接口，获取token
                Result<String> tokenJwt = openAuthApi.getTokenJwt(ecmRootDataDTO.getFlagId());
                if (tokenJwt.isSucc()) {
                    userBaseInfo.setAccessToken(tokenJwt.getData());
//                    pageUrl = RsaUtils.encrypt(pageUrl);
                } else {
                    log.error("jwt权限校验失败");
                }
                //生成Url唯一标识
                String nonce = UUIDUtils.generateUUID();
                userBaseInfo.setNonce(nonce);
                busiCacheService.setUrlOnceNonce(nonce,userBaseInfo.getFlagId());
                //对外接口置空
                userBaseInfo.setFlagId(null);
            } else {
                //Pc端
                pageUrl = "/sunIcms/#/imageContentManagement";
            }
        } catch (Exception e) {
            log.error("跳转页面加密有误");
        }
        userBaseInfo.setPageUrl(pageUrl);
        return userBaseInfo;
    }

    /**
     * 统一处理
     */
    private EcmPageBaseInfoDTO getEcmPageBaseInfoDTO(EcmRootDataDTO ecmRootDataDTO, boolean isApi) {
        //获取基本信息
        EcmBaseInfoDTO baseInfoDTO = ecmRootDataDTO.getEcmBaseInfoDTO();
        //获取操作员ID
        EcmPageBaseInfoDTO userBaseInfo = new EcmPageBaseInfoDTO();
        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        token.setUsername(baseInfoDTO.getUserCode());
        token.setName(baseInfoDTO.getUserName());
        token.setOrgName(baseInfoDTO.getOrgName());
        token.setOrgCode(baseInfoDTO.getOrgCode());
        token.setRoleCodeList(baseInfoDTO.getRoleCode());
        if (isApi) {
            token.setOut(true);
        } else {
            token.setOut(false);
        }
        token = busiCacheService.checkUser(baseInfoDTO, token);
        baseInfoDTO.setUserName(token.getName());
        baseInfoDTO.setOrgName(token.getOrgName());
        ecmRootDataDTO.setEcmBaseInfoDTO(baseInfoDTO);
        //判断是批扫还是单扫(0-批扫 1-单扫)
        String oneBatch = baseInfoDTO.getOneBatch();
        if (StrUtil.equals(StateConstants.COMMON_ONE.toString(), oneBatch)) {
            //校验基本信息
            handleCheckBusi(ecmRootDataDTO.getEcmBusExtendDTOS(), isApi);
            //校验动态树业务权限
            checkBusiness(ecmRootDataDTO, token);

            //处理动态树结构
            handleVTreeStructure(ecmRootDataDTO,token);

            List<EcmBusiInfo> busiIdList = new ArrayList<>();
            List<String> appCode = new ArrayList<>();
            //判断业务是否存在
            List<EcmBusExtendDTO> addExtendDtos = new ArrayList<>();
            List<EcmBusExtendDTO> updateExtendDtos = new ArrayList<>();
            List<EcmBusExtendDTO> ecmBusExtendDTOS = ecmRootDataDTO.getEcmBusExtendDTOS();
            List<EcmBusiInfo> checkDto = new ArrayList<>();
            List<EcmBusiInfo> checkDtoCheck = new ArrayList<>();
            List<EcmBusExtendDTO> addExtendDtosCheck = new ArrayList<>();
            for (EcmBusExtendDTO dto : ecmBusExtendDTOS) {
                EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper
                        .selectOne(new LambdaQueryWrapper<EcmBusiInfo>()
                                .eq(EcmBusiInfo::getBusiNo, dto.getBusiNo())
                                .eq(EcmBusiInfo::getAppCode, dto.getAppCode()));
                if (ecmBusiInfo == null) {
                    addExtendDtos.add(dto);
                } else {
                    //设置树类型
                    dto.setTypeTree(String.valueOf(ecmBusiInfo.getTreeType()));
                    ecmRootDataDTO.getEcmBaseInfoDTO().setTypeTree(String.valueOf(ecmBusiInfo.getTreeType()));
                    updateExtendDtos.add(dto);
                    checkDto.add(ecmBusiInfo);
                }
                if (CollectionUtil.isEmpty(dto.getEcmRuleDataDTO())) {
                    if (ecmBusiInfo == null) {
                        addExtendDtosCheck.add(dto);
                    } else {
                        checkDtoCheck.add(ecmBusiInfo);
                    }
                }
                appCode.add(dto.getAppCode());
            }

            //无权限的数据报错 --角色校验
            List<String> busiNoPermissFromLive = getBusiNoPermissFromLive(token, checkDtoCheck,
                    addExtendDtosCheck);
            if (!CollectionUtils.isEmpty(busiNoPermissFromLive)) {
                throw new SunyardException(ResultCode.NO_DATA_AUTH,
                        "暂无【" + String.join(",", busiNoPermissFromLive) + "】业务权限！");
            }

            if (IcmsConstants.DYNAMIC_TREE.toString()
                    .equals(ecmRootDataDTO.getEcmBaseInfoDTO().getTypeTree())) {
                //校验动态树
                checkTreeLevel(updateExtendDtos, checkDto);
            }

            Map<Long, List<EcmDocrightDefDTO>> map = new HashMap<>();
            if (addExtendDtos.size() != IcmsConstants.ZERO) {
                ecmRootDataDTO.setEcmBusExtendDTOS(addExtendDtos);
                //新增业务
                List<EcmBusiInfo> ecmBusiInfos = addBusinessInfo(ecmRootDataDTO, token, map, isApi);
                //                List<Long> collect1 = ecmBusiInfos.stream().map(EcmBusiInfo::getBusiId).collect(Collectors.toList());
                busiIdList.addAll(ecmBusiInfos);
            }
            if (updateExtendDtos.size() != IcmsConstants.ZERO) {
                ecmRootDataDTO.setEcmBusExtendDTOS(updateExtendDtos);
                //更新业务
                List<EcmBusiInfo> list = updateBusiness(ecmRootDataDTO, token, map, checkDto);
                busiIdList.addAll(list);
            }

            handleSinCapture(ecmRootDataDTO, busiIdList, token, userBaseInfo, appCode, map);
        } else {
            UserBusiRedisDTO userBusiEntity = new UserBusiRedisDTO();
            userBusiEntity.setUsercode(ecmRootDataDTO.getEcmBaseInfoDTO().getUserCode());
            userBusiEntity.setUsername(ecmRootDataDTO.getEcmBaseInfoDTO().getUserName());
            userBusiEntity.setRole(ecmRootDataDTO.getEcmBaseInfoDTO().getRoleCode());
            userBusiEntity.setOrg(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode());
            userBusiEntity.setOrgName(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgName());

            //生成页面唯一表示uuid
            String pageFlag = UUIDUtils.generateUUID();
            userBaseInfo.setFlagId(pageFlag);
            ecmRootDataDTO.setFlagId(pageFlag);
            busiCacheService.saveOrUpdateUser(pageFlag, userBusiEntity);
        }
        return userBaseInfo;
    }

    private void handleVTreeStructure(EcmRootDataDTO ecmRootDataDTO, AccountTokenExtendDTO token) {

        //如果不是动态树则不处理
        List<EcmBusExtendDTO> ecmBusExtendDTOS = ecmRootDataDTO.getEcmBusExtendDTOS();
        for (EcmBusExtendDTO dto : ecmBusExtendDTOS) {
            //如果传入vtree报文则不进行后续处理
            if (dto.getEcmVTreeDataDTOS() != null){
                continue;
            }
            EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper
                    .selectOne(new LambdaQueryWrapper<EcmBusiInfo>()
                            .eq(EcmBusiInfo::getBusiNo, dto.getBusiNo())
                            .eq(EcmBusiInfo::getAppCode, dto.getAppCode()));
            if (ecmBusiInfo == null || !IcmsConstants.DYNAMIC_TREE.equals(ecmBusiInfo.getTreeType())) {
                continue;
            }
            //动态树则拼VTree
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, ecmBusiInfo.getBusiId());
            List<EcmBusiDocRedisDTO> busiDocRedisDTOS = ecmBusiInfoRedisDTO.getEcmBusiDocRedisDTOS();
            //转换
            changeVTreeType(busiDocRedisDTOS,dto);
        }
    }

    private void changeVTreeType(List<EcmBusiDocRedisDTO> busiDocRedisDTOS, EcmBusExtendDTO dto) {
        List<EcmVTreeDataDTO> treeDataDTOS = dto.getEcmVTreeDataDTOS();
        Map<String, EcmVTreeDataDTO> map = treeDataDTOS.stream()
                .collect(Collectors.toMap(EcmVTreeDataDTO::getDocCode, Function.identity()));
        ArrayList<EcmVTreeDataDTO> ecmVTreeDataDTOS = new ArrayList<>();
        for (EcmBusiDocRedisDTO redisDTO : busiDocRedisDTOS) {
            EcmVTreeDataDTO vTreeDataDTO = convertToVTreeData(redisDTO,map);
            ecmVTreeDataDTOS.add(vTreeDataDTO);
        }
        dto.setEcmVTreeDataDTOS(ecmVTreeDataDTOS);
    }

    private EcmVTreeDataDTO convertToVTreeData(EcmBusiDocRedisDTO source, Map<String, EcmVTreeDataDTO> map) {
        if (source == null) {
            return null;
        }
        EcmVTreeDataDTO ecmVTreeDataDTO = new EcmVTreeDataDTO();
        ecmVTreeDataDTO.setDocCode(source.getDocCode());
        ecmVTreeDataDTO.setDocName(source.getDocName());
        if (map.containsKey(source.getDocCode())){
            ecmVTreeDataDTO.setDocRight(map.get(source.getDocCode()).getDocRight());
        }else {
            ecmVTreeDataDTO.setDocRight("");
        }
        ecmVTreeDataDTO.setDocOrder(String.valueOf(source.getDocSort()));
//        ecmVTreeDataDTO.setMaxPages();
//        ecmVTreeDataDTO.setMinPages();

        if (source.getChildren() != null && !source.getChildren().isEmpty()){
            ecmVTreeDataDTO.setEcmVTreeDataDTOS(
                    source.getChildren().stream()
                            .map(source1 -> convertToVTreeData(source1, map))
                            .collect(Collectors.toList())
            );
        }else {
            ecmVTreeDataDTO.setEcmVTreeDataDTOS(new ArrayList<>());
        }

        return ecmVTreeDataDTO;
    }


    private void handleSinCapture(EcmRootDataDTO ecmRootDataDTO, List<EcmBusiInfo> busiIdList,
                                  AccountTokenExtendDTO token, EcmPageBaseInfoDTO userBaseInfo,
                                  List<String> appCode, Map<Long, List<EcmDocrightDefDTO>> map) {
        if (CollectionUtils.isEmpty(appCode)) {
            return;
        }
        List<Long> collect = busiIdList.stream().map(EcmBusiInfo::getBusiId)
                .collect(Collectors.toList());

        UserBusiRedisDTO userBusiRedisDTO = handleRedisUser(ecmRootDataDTO, token, appCode,
                busiIdList);
        userBaseInfo.setBusiId(collect);
        userBaseInfo.setFlagId(ecmRootDataDTO.getFlagId());
        userBusiRedisDTO.setUsercode(ecmRootDataDTO.getEcmBaseInfoDTO().getUserCode());
        userBusiRedisDTO.setUsername(ecmRootDataDTO.getEcmBaseInfoDTO().getUserName());
        userBusiRedisDTO.setRole(ecmRootDataDTO.getEcmBaseInfoDTO().getRoleCode());
        userBusiRedisDTO.setOrg(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode());
        userBusiRedisDTO.setOrgName(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgName());
        userBusiRedisDTO.setBusiId(collect);
        userBusiRedisDTO.setAppType(appCode);
        userBusiRedisDTO.setIsShow(ecmRootDataDTO.getEcmBaseInfoDTO().getIsScan());
        //如果是动态树需要将这一个会话的权限写入redis中
        userBusiRedisDTO.setDocRightList(map);

        busiCacheService.saveOrUpdateUser(ecmRootDataDTO.getFlagId(), userBusiRedisDTO);
        log.info("结束设置redisUser时间，用时：{}",
                RedisConstants.USER_BUSI_PREFIX + ecmRootDataDTO.getFlagId());
    }

    /**
     * @param token
     * @param checkDto      修改
     * @param addExtendDtos 新增
     */
    private List<String> getBusiNoPermissFromLive(AccountTokenExtendDTO token,
                                                  List<EcmBusiInfo> checkDto,
                                                  List<EcmBusExtendDTO> addExtendDtos) {
        //已存在的业务进行判断
        List<String> busiNo = new ArrayList<>();
        //所有有权限的appcode列表,这里包含了所有的版本权限，需要过滤出指定的版本
        List<EcmDocrightDef> appCodeHaveByTokenAll = staticTreePermissService
                .getAppCodeHaveByTokenAll(null, token);
        Map<String, List<EcmDocrightDef>> collect = appCodeHaveByTokenAll.stream()
                .collect(Collectors.groupingBy(EcmDocrightDef::getAppCode));
        if (CollectionUtil.isNotEmpty(checkDto)) {
            //权限判断。
            for (EcmBusiInfo ecmBusiInfo : checkDto) {
                Long id = ecmBusiInfo.getBusiId();
                EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                        .getEcmBusiInfoRedisDTO(token, id);
                if (IcmsConstants.DYNAMIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType())) {
                    //对外接口、动态树不处理
                } else {
                    List<EcmDocrightDef> defs = collect.get(ecmBusiInfoRedisDTO.getAppCode());
                    if (org.apache.commons.collections4.CollectionUtils.isEmpty(defs)) {
                        busiNo.add(ecmBusiInfoRedisDTO.getBusiNo());
                        continue;
                    }

                    Map<Integer, List<EcmDocrightDef>> collect1 = defs.stream()
                            .collect(Collectors.groupingBy(EcmDocrightDef::getRightVer));
                    //当前业务所拥有的权限
                    List<EcmDocrightDef> defs1 = collect1.get(ecmBusiInfoRedisDTO.getRightVer());
                    if (CollectionUtils.isEmpty(defs1)) {
                        busiNo.add(ecmBusiInfoRedisDTO.getBusiNo());
                        continue;
                    }
                    List<String> collect2 = defs1.stream().map(EcmDocrightDef::getAppCode)
                            .collect(Collectors.toList());
                    //静态树
                    if (!collect2.contains(ecmBusiInfoRedisDTO.getAppCode())) {
                        busiNo.add(ecmBusiInfoRedisDTO.getBusiNo());
                    }
                }
            }
        }

        if (CollectionUtil.isNotEmpty(addExtendDtos)) {
            //最新版本
            List<EcmAppDocright> ecmAppDocrights = ecmAppDocrightMapper
                    .selectList(new LambdaQueryWrapper<EcmAppDocright>()
                            .eq(EcmAppDocright::getRightNew, StateConstants.YES));
            Map<String, List<EcmAppDocright>> collect3 = ecmAppDocrights.stream()
                    .collect(Collectors.groupingBy(EcmAppDocright::getAppCode));

            //不存在业务校验权限
            for (EcmBusExtendDTO ecmBusExtendDTO : addExtendDtos) {
                //对外接口、动态书不处理，静态树进行权限判断
                if (CollectionUtil.isEmpty(ecmBusExtendDTO.getEcmVTreeDataDTOS())) {
                    //静态树
                    List<EcmDocrightDef> defs = collect.get(ecmBusExtendDTO.getAppCode());
                    List<EcmAppDocright> ecmAppDocrights1 = collect3
                            .get(ecmBusExtendDTO.getAppCode());
                    if (CollectionUtils.isEmpty(defs)
                            || CollectionUtils.isEmpty(ecmAppDocrights1)) {
                        busiNo.add(ecmBusExtendDTO.getBusiNo());
                        continue;
                    }
                    Map<Integer, List<EcmDocrightDef>> collect4 = defs.stream()
                            .collect(Collectors.groupingBy(EcmDocrightDef::getRightVer));
                    //当前业务所拥有的权限
                    List<EcmDocrightDef> defs1 = collect4
                            .get(ecmAppDocrights1.get(0).getRightVer());
                    if (CollectionUtils.isEmpty(defs1)) {
                        busiNo.add(ecmBusExtendDTO.getBusiNo());
                        continue;
                    }
                    List<String> collect2 = defs1.stream().map(EcmDocrightDef::getAppCode)
                            .collect(Collectors.toList());
                    //静态树
                    if (!collect2.contains(ecmBusExtendDTO.getAppCode())) {
                        busiNo.add(ecmBusExtendDTO.getBusiNo());
                    }
                }
            }
        }
        return busiNo;
    }

    /**
     * 校验原节点结构
     */
    private void checkTreeLevel(List<EcmBusExtendDTO> updateExtendDtos,
                                List<EcmBusiInfo> checkDto) {
        //校验动态树是否修改了父子层级关系，将原本的子级作为别人子级的父级，则报错。
        if (!CollectionUtils.isEmpty(checkDto)) {
            Map<String, List<EcmBusExtendDTO>> collect = updateExtendDtos.stream()
                    .collect(Collectors.groupingBy(EcmBusExtendDTO::getAppCode));
            Map<String, List<EcmBusiInfo>> collect1 = checkDto.stream()
                    .collect(Collectors.groupingBy(EcmBusiInfo::getAppCode));
            List<Long> collect2 = checkDto.stream().map(EcmBusiInfo::getBusiId)
                    .collect(Collectors.toList());
            List<EcmBusiDoc> ecmBusiDocs = ecmBusiDocMapper
                    .selectList(new LambdaQueryWrapper<EcmBusiDoc>()
                            .eq(EcmBusiDoc::getDocMark, StateConstants.NO)
                            .in(EcmBusiDoc::getBusiId, collect2));
            if (CollectionUtils.isEmpty(ecmBusiDocs)) {
                return;
            }
            Map<Long, List<EcmBusiDoc>> collect3 = ecmBusiDocs.stream()
                    .collect(Collectors.groupingBy(EcmBusiDoc::getBusiId));

            for (String str : collect.keySet()) {
                List<EcmBusExtendDTO> ecmBusExtendDTOS1 = collect.get(str);
                Map<String, List<EcmBusExtendDTO>> collect4 = ecmBusExtendDTOS1.stream()
                        .collect(Collectors.groupingBy(EcmBusExtendDTO::getBusiNo));
                List<EcmBusiInfo> ecmBusiInfos = collect1.get(str);
                for (EcmBusiInfo busiInfo : ecmBusiInfos) {
                    List<EcmBusExtendDTO> ecmBusExtendDTOS2 = collect4.get(busiInfo.getBusiNo());
                    if (!CollectionUtils.isEmpty(ecmBusExtendDTOS2)) {
                        EcmBusExtendDTO dto = ecmBusExtendDTOS2.get(0);
                        List<EcmVTreeDataDTO> ecmVTreeDataDTOS = dto.getEcmVTreeDataDTOS();

                        EcmBusiDocRedisDTO ecmBusiDocRedisDTOS = new EcmBusiDocRedisDTO();
                        List<EcmBusiDoc> ecmBusiDocs1 = collect3.get(busiInfo.getBusiId());
                        List<String> collect5 = ecmBusiDocs1.stream().map(EcmBusiDoc::getDocCode)
                                .collect(Collectors.toList());
                        ArrayList<EcmBusiDocRedisDTO> objects = new ArrayList<>();
                        for (EcmBusiDoc ecmBusiDoc : ecmBusiDocs1) {
                            EcmBusiDocRedisDTO dto1 = new EcmBusiDocRedisDTO();
                            BeanUtils.copyProperties(ecmBusiDoc, dto1);
                            objects.add(dto1);
                        }
                        handleChidle(objects, busiInfo.getBusiId(), ecmBusiDocRedisDTOS);
                        List<EcmBusiDocRedisDTO> children = ecmBusiDocRedisDTOS.getChildren();
                        handleCheckChild(ecmVTreeDataDTOS, collect5, children);
                    }
                }
            }
        }
    }

    private void handleCheckChild(List<EcmVTreeDataDTO> ecmVTreeDataDTOS, List<String> collect5,
                                  List<EcmBusiDocRedisDTO> children) {
        List<String> collect6 = children.stream().map(EcmBusiDocRedisDTO::getDocCode)
                .collect(Collectors.toList());
        Map<String, List<EcmBusiDocRedisDTO>> collect = children.stream()
                .collect(Collectors.groupingBy(EcmBusiDocRedisDTO::getDocCode));
        for (EcmVTreeDataDTO vTreeDataDTO : ecmVTreeDataDTOS) {
            if (collect5.contains(vTreeDataDTO.getDocCode())) {
                AssertUtils.isTrue(!collect6.contains(vTreeDataDTO.getDocCode()),
                        "资料树结构有误，与原节点结构不匹配");
            }
            if (!CollectionUtils.isEmpty(vTreeDataDTO.getEcmVTreeDataDTOS())) {
                List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = collect
                        .get(vTreeDataDTO.getDocCode());
                if (!CollectionUtils.isEmpty(ecmBusiDocRedisDTOS)) {
                    handleCheckChild(vTreeDataDTO.getEcmVTreeDataDTOS(), collect5,
                            ecmBusiDocRedisDTOS.get(0).getChildren());

                }
            }
        }
    }

    private void handleChidle(List<EcmBusiDocRedisDTO> ecmBusiDocs1, Long parentId,
                              EcmBusiDocRedisDTO ecmBusiDocRedisDTOS) {
        List<EcmBusiDocRedisDTO> collect = ecmBusiDocs1.stream()
                .filter(s -> s.getParentId().equals(parentId)).collect(Collectors.toList());
        ecmBusiDocRedisDTOS.setChildren(collect);
        if (!CollectionUtils.isEmpty(collect)) {
            for (EcmBusiDocRedisDTO ecmBusiDoc : collect) {
                handleChidle(ecmBusiDocs1, ecmBusiDoc.getDocId(), ecmBusiDoc);
            }

        }

    }

    /**
     * 影像扫描或修改对外接口
     */
    @Transactional(rollbackFor = Exception.class)
    public EcmPageBaseInfoDTO businessDataServiceMobile(EcmRootDataDTO ecmRootDataDTO) {
        log.info("影像扫描对外接口传入参数：{}", JSONUtil.toJsonStr(ecmRootDataDTO));
        EcmPageBaseInfoDTO userBaseInfo = getEcmPageBaseInfoDTO(ecmRootDataDTO, true);
        // 批扫返回采集页面url地址和token
        Result result = new Result();
        //返回页面地址
        //sysParam接口调用
        Result<SysParamDTO> pageReturnResult = paramApi
                .searchValueByKey(IcmsConstants.ECMS_MOBILE_CAPTURE_PATH);
        log.info("调用采集页面返回数据：{}", pageReturnResult);
        if (!pageReturnResult.isSucc()) {
            throw new SunyardException(pageReturnResult.getMsg());
        }
        SysParamDTO data = pageReturnResult.getData();
        //对外接口，获取token
        Result<String> tokenJwt = openAuthApi.getTokenJwt(ecmRootDataDTO.getFlagId());
        if (tokenJwt.isSucc()) {
            userBaseInfo.setAccessToken(tokenJwt.getData());
            String url = data.getValue();
//            try {
//                url = RsaUtils.encrypt(url);
//            } catch (Exception e) {
//                log.error("跳转页面加密有误");
//            }
            result.setData(url);
            userBaseInfo.setPageUrl(result.getData().toString());
        } else {
            AssertUtils.isTrue(true, "token获取失败");
        }
        //生成Url唯一标识
        String nonce = UUIDUtils.generateUUID();
        userBaseInfo.setNonce(nonce);
        busiCacheService.setUrlOnceNonce(nonce,userBaseInfo.getFlagId());
        return userBaseInfo;
    }

    /**
     * 修改影像批次
     *
     * @param ecmRootDataDTO
     * @param token
     * @param map
     * @param checkDto
     */
    private List<EcmBusiInfo> updateBusiness(EcmRootDataDTO ecmRootDataDTO,
                                             AccountTokenExtendDTO token,
                                             Map<Long, List<EcmDocrightDefDTO>> map,
                                             List<EcmBusiInfo> checkDto) {
        List<EcmBusExtendDTO> ecmBusExtendDTOS = ecmRootDataDTO.getEcmBusExtendDTOS();
        EcmBaseInfoDTO baseInfoDTO=ecmRootDataDTO.getEcmBaseInfoDTO();
        //是否补传
        Integer isRetransmission = baseInfoDTO.getIsRetransmission();
        ArrayList<EcmBusiInfo> list = new ArrayList<>();
        //2、修改业务信息
        for (EcmBusExtendDTO busExtendDTO : ecmBusExtendDTOS) {
            //静态树
            List<EcmBusiInfo> collect = checkDto.stream()
                    .filter(s -> s.getBusiNo().equals(busExtendDTO.getBusiNo())
                            && s.getAppCode().equals(busExtendDTO.getAppCode()))
                    .collect(Collectors.toList());
            EcmBusiInfo ecmBusiInfo = collect.get(0);
            String typeTree = busExtendDTO.getTypeTree();
            if (busExtendDTO.getTypeTree() == null) {
                typeTree = ecmRootDataDTO.getEcmBaseInfoDTO().getTypeTree();
            }

            if (IcmsConstants.STATIC_TREE.toString().equals(typeTree)) {
                //设置机构号
                busExtendDTO.setOrgCode(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode());
                updateStaticTreeDataInfo(busExtendDTO, token, map, ecmBusiInfo, isRetransmission);
            } else {
                //设置机构号
                busExtendDTO.setOrgCode(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode());
                //动态树
                updateDynamicTreeData(busExtendDTO, token, ecmRootDataDTO, map, isRetransmission);
            }

            list.add(ecmBusiInfo);

        }

        return list;
    }

    /**
     * 修改动态树资料数据
     */
    @LogManageAnnotation("编辑业务")
    public void updateDynamicTreeData(EcmBusExtendDTO busExtendDTO, AccountTokenExtendDTO token,
                                      EcmRootDataDTO ecmRootDataDTO,
                                      Map<Long, List<EcmDocrightDefDTO>> map,
                                      Integer isRetransmission) {
        CompletableFuture<String> appCodeFuture = CompletableFuture
                .supplyAsync(() -> saveCaptureInfo(busExtendDTO.getAppCode(), token));
        CompletableFuture<Void> task = appCodeFuture.thenApplyAsync(appCode -> {
            EcmBusiInfoRedisDTO ecmBusiInfoExtend = new EcmBusiInfoRedisDTO();
            ecmBusiInfoExtend.setBusiNo(busExtendDTO.getBusiNo());
            ecmBusiInfoExtend.setAppCode(appCode);
            EcmBusiInfo ecmBusiInfo = updateEcmBusiInfo(ecmBusiInfoExtend, token,
                    IcmsConstants.DYNAMIC_TREE);
            ecmBusiInfoExtend = busiCacheService.getEcmBusiInfoRedisDTO(token,
                    ecmBusiInfo.getBusiId());
            List<EcmDocrightDefDTO> docrightDefExtends = null;
            if (CollectionUtil.isNotEmpty(busExtendDTO.getEcmVTreeDataDTOS())
                    && busExtendDTO.getEcmRuleDataDTO() == null) {
                // 更新动态树资料
                updateDynamicDoc(ecmBusiInfoExtend, busExtendDTO.getEcmVTreeDataDTOS(), token);
                docrightDefExtends = dealTreeDocRight(busExtendDTO, ecmBusiInfo.getBusiId());
            } else {
                docrightDefExtends = busiCacheService.vTreeLogic(ecmBusiInfoExtend, token);
            }
//            ecmBusiInfoExtend.setDocRightList(docrightDefExtends);
            if(IcmsConstants.ONE.equals(isRetransmission)||IcmsConstants.TWO.equals(isRetransmission)){
                operateCaptureService.dealRetransmission(isRetransmission, docrightDefExtends);
            }
            map.put(ecmBusiInfo.getBusiId(), docrightDefExtends);
            Long l = ecmFileInfoMapper.selectCount(new LambdaQueryWrapper<EcmFileInfo>()
                    .eq(EcmFileInfo::getBusiId, ecmBusiInfo.getBusiId()));
            ecmBusiInfoExtend.setTotalFileSize(l);
            busiCacheService.saveAndUpate(ecmBusiInfoExtend);
            // 添加操作记录表
            busiOperationService.addOperation(ecmBusiInfoExtend.getBusiId(),
                    IcmsConstants.ADD_BUSI, token, "更新业务");
            return null;
        });
        try {
            task.get(); // 等待任务完成
        } catch (InterruptedException | ExecutionException e) {
            log.error("修改动态树资料数据",e);
        }
    }

    /**
     * 更新资料
     */
    private void updateDynamicDoc(EcmBusiInfoRedisDTO ecmBusiInfoExtend,
                                  List<EcmVTreeDataDTO> ecmVTreeDataDTOS,
                                  AccountTokenExtendDTO token) {
        if (CollectionUtil.isEmpty(ecmVTreeDataDTOS)) {
            return;
        }
        //根据业务类型和资料代码查询资料是否更新
        LambdaQueryWrapper<EcmBusiDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmBusiDoc::getBusiId, ecmBusiInfoExtend.getBusiId());
        List<EcmBusiDoc> ecmBusiDocs = ecmBusiDocMapper.selectList(wrapper);
        List<String> collect = ecmBusiDocs.stream().map(EcmBusiDoc::getDocCode)
                .collect(Collectors.toList());

        //调用新增资料节点方法
        saveEcmBusiDoc(ecmBusiInfoExtend, ecmVTreeDataDTOS, collect, token);
    }

    /**
     * 修改资料动态树
     */
    private void updateEcmBusiDoc(Long busiId, List<EcmVTreeDataDTO> ecmVTreeDataDTOS) {
        //更新资料
        for (EcmVTreeDataDTO ecmVTreeDataDTO : ecmVTreeDataDTOS) {
            //根据业务类型和资料代码查询资料是否更新
            LambdaQueryWrapper<EcmBusiDoc> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EcmBusiDoc::getBusiId, busiId);
            wrapper.eq(EcmBusiDoc::getDocCode, ecmVTreeDataDTO.getDocCode());
            List<EcmBusiDoc> ecmBusiDocs = ecmBusiDocMapper.selectList(wrapper);
            if (CollectionUtil.isNotEmpty(ecmBusiDocs)) {
                //数据应该唯一，查集合取一个防止脏数据
                EcmBusiDoc ecmBusiDoc = ecmBusiDocs.get(0);
                BeanUtil.copyProperties(ecmVTreeDataDTO, ecmBusiDoc);
                ecmBusiDocMapper.updateById(ecmBusiDoc);
            }
        }
    }

    /**
     * 更新静态树数据
     */
    @LogManageAnnotation("编辑业务")
    private void updateStaticTreeDataInfo(EcmBusExtendDTO busExtendDTO, AccountTokenExtendDTO token,
                                          Map<Long, List<EcmDocrightDefDTO>> map,
                                          EcmBusiInfo ecmBusiInfo,
                                          Integer isRetransmission) {
        EcmBusiInfoRedisDTO ecmBusiInfoExtend = new EcmBusiInfoRedisDTO();
        //映射基础信息
        ecmBusiInfoExtend.setBusiNo(busExtendDTO.getBusiNo());
        ecmBusiInfoExtend.setAppCode(busExtendDTO.getAppCode());
        ecmBusiInfoExtend.setOrgCode(busExtendDTO.getOrgCode());
        ecmBusiInfoExtend.setUpdateUser(token.getUsername());
        ecmBusiInfoExtend.setRightVer(ecmBusiInfo.getRightVer());
        if (CollectionUtil.isNotEmpty(busExtendDTO.getEcmBusiAttrDTOList())) {
            List<EcmAppAttrDTO> ecmAppAttrDTOS = new ArrayList<>();
            List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper
                    .selectList(new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode,
                            busExtendDTO.getAppCode()));
            Map<String, List<EcmAppAttr>> collect = ecmAppAttrs.stream()
                    .collect(Collectors.groupingBy(EcmAppAttr::getAttrCode));

            for (EcmBusiAttrDTO dto : busExtendDTO.getEcmBusiAttrDTOList()) {
                List<EcmAppAttr> ecmAppAttrs1 = collect.get(dto.getAttrCode());
                if (CollectionUtil.isEmpty(ecmAppAttrs1)) {
                    continue;
                }
                EcmAppAttrDTO ecmAppAttrDTO = new EcmAppAttrDTO();
                BeanUtils.copyProperties(ecmAppAttrs1.get(0), ecmAppAttrDTO);
                ecmAppAttrDTO.setAttrCode(dto.getAttrCode());
                ecmAppAttrDTO.setAppAttrValue(dto.getAppAttrValue());
                ecmAppAttrDTO.setAppCode(ecmBusiInfoExtend.getAppCode());
                ecmAppAttrDTO.setAttrName(
                        StringUtils.isEmpty(dto.getAttrName()) ? ecmAppAttrs1.get(0).getAttrName()
                                : dto.getAttrName());
                ecmAppAttrDTO.setAppAttrId(ecmAppAttrs1.get(0).getAppAttrId());
                ecmAppAttrDTOS.add(ecmAppAttrDTO);
            }
            ecmBusiInfoExtend.setAttrList(ecmAppAttrDTOS);
        }
        //更新扩展字段
        operateCaptureService.editBusi(ecmBusiInfoExtend, token, busExtendDTO, map, isRetransmission);
    }

    /**
     * 获取采集页面
     */
    public Result returnScanPage(Result result) {
        //sysParam接口调用
        Result<SysParamDTO> pageReturnResult = paramApi.searchValueByKey(pageAddressKey);
        log.info("调用采集页面返回数据：{}", pageReturnResult);
        if (!pageReturnResult.isSucc()) {
            throw new SunyardException(pageReturnResult.getMsg());
        }
        SysParamDTO data = pageReturnResult.getData();
        String url = data.getValue();
        result.setData(url);
        return result;
    }

    /**
     * 新增业务
     */
    public List<EcmBusiInfo> addBusi(EcmRootDataDTO ecmRootDataDTO, AccountTokenExtendDTO token) {
        //获取用户ID用于token
        AssertUtils.isNull(ecmRootDataDTO, "添加业务参数不能为空");
        log.info(JSONObject.toJSONString(ecmRootDataDTO));
        List<EcmBusiInfo> busiIdList = new ArrayList<>();
        List<EcmBusExtendDTO> addExtendDtos = new ArrayList<>();
        List<EcmBusExtendDTO> ecmBusExtendDTOS = ecmRootDataDTO.getEcmBusExtendDTOS();
        for (EcmBusExtendDTO dto : ecmBusExtendDTOS) {
            addExtendDtos.add(dto);
        }
        Map<Long, List<EcmDocrightDefDTO>> map = new HashMap<>();
        if (addExtendDtos.size() != IcmsConstants.ZERO) {
            ecmRootDataDTO.setEcmBusExtendDTOS(addExtendDtos);
            //新增业务
            busiIdList = addBusinessInfo(ecmRootDataDTO, token, map, true);
        }
        return busiIdList;
    }

    /**
     * 校验数据
     */
    private void checkBusiness(EcmRootDataDTO ecmRootDataDTO, AccountTokenExtendDTO token) {
        //默认静态树
        ecmRootDataDTO.getEcmBaseInfoDTO().setTypeTree(IcmsConstants.STATIC_TREE.toString());
        //获取业务拓展信息
        List<EcmBusExtendDTO> busiDataList = ecmRootDataDTO.getEcmBusExtendDTOS();
        if (CollectionUtil.isNotEmpty(busiDataList)) {
            busiDataList.stream().forEach(b -> {
                //判断业务类型和业务编号是否为空
                if (StrUtil.isBlank(b.getAppCode())) {
                    throw new SunyardException("业务数据有误");
                }
                //动态树
                List<EcmVTreeDataDTO> ecmVTreeDataDTOS = b.getEcmVTreeDataDTOS();
                //动态树资料权限获取
                if (CollectionUtil.isNotEmpty(ecmVTreeDataDTOS)) {
                    //重新赋值
                    ecmRootDataDTO.getEcmBaseInfoDTO()
                            .setTypeTree(IcmsConstants.DYNAMIC_TREE.toString());
                    ecmVTreeDataDTOS.stream().forEach(t -> {
                        String right = t.getDocRight();
                        if (StrUtil.isBlank(right)) {
                            throw new SunyardException(ResultCode.NO_DATA_AUTH, "动态树权限获取失败!");
                        }
                    });
                }
                //判断是否为业务多维度
                List<EcmRuleDataDTO> ecmRuleDataDTO = b.getEcmRuleDataDTO();
                String s = commonService.getRuleDataSort(ecmRuleDataDTO);

                //多维度
                if (ecmRuleDataDTO != null) {
                    //查询资料权限定义表
                    LambdaQueryWrapper<EcmDocrightDef> wrapper = new LambdaQueryWrapper<>();
                    //获取对应业务类型且为多维度的资料权限
                    wrapper.eq(EcmDocrightDef::getRoleDimVal, s);
                    wrapper.eq(EcmDocrightDef::getAppCode, b.getAppCode());
                    wrapper.eq(EcmDocrightDef::getDimType, StateConstants.COMMON_ONE);
                    List<EcmDocrightDef> ecmDocrightDefs = ecmDocrightDefMapper.selectList(wrapper);
                    if (CollectionUtil.isEmpty(ecmDocrightDefs)) {
                        throw new SunyardException(ResultCode.NO_DATA_AUTH, "暂无权限!");
                    }
                }
            });
        }
    }

    /**
     * 添加业务信息
     */
    public List<EcmBusiInfo> addBusinessInfo(EcmRootDataDTO ecmRootDataDTO,
                                             AccountTokenExtendDTO token,
                                             Map<Long, List<EcmDocrightDefDTO>> map,
                                             boolean isApi) {
        //新增需要做参数校验
        token = checkBusBase(ecmRootDataDTO, token, isApi);
        EcmBaseInfoDTO ecmBaseInfoDTO = ecmRootDataDTO.getEcmBaseInfoDTO();
        ecmBaseInfoDTO.setUserName(token.getName());
        ecmBaseInfoDTO.setOrgName(token.getOrgName());
        ecmRootDataDTO.setEcmBaseInfoDTO(ecmBaseInfoDTO);
        List<EcmBusiInfo> busiIdList = new ArrayList<>();
        List<EcmBusExtendDTO> ecmBusExtendDTOS = ecmRootDataDTO.getEcmBusExtendDTOS();
        for (EcmBusExtendDTO ecmBusExtendDTO : ecmBusExtendDTOS) {
            //静态树（系统中已有相关业务建模）
            if (IcmsConstants.STATIC_TREE.toString()
                    .equals(ecmRootDataDTO.getEcmBaseInfoDTO().getTypeTree())) {
                //设置机构号
                ecmBusExtendDTO.setOrgCode(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode());
                List<EcmBusiInfo> ecmBusiInfos = dealStaticTree(ecmBusExtendDTO, token);
                List<Long> busiIds = ecmBusiInfos.stream().map(EcmBusiInfo::getBusiId)
                        .collect(Collectors.toList());

                EcmAppDocright ecmAppDef = ecmAppDocrightMapper
                        .selectOne(new LambdaQueryWrapper<EcmAppDocright>()
                                .eq(EcmAppDocright::getRightNew, StateConstants.YES)
                                .eq(EcmAppDocright::getAppCode, ecmBusExtendDTO.getAppCode()));
                for (Long id : busiIds) {
                    //静态树多维度权限写
                    List<EcmDocrightDefDTO> ecmDocrightDefDTOS;
                    if(!ObjectUtils.isEmpty(ecmBusExtendDTO.getEcmRuleDataDTO())){
                        ecmDocrightDefDTOS = commonService.dealRuleData(ecmBusExtendDTO, token, ecmAppDef.getRightVer());
                    }else{
                         ecmDocrightDefDTOS = staticTreePermissService
                                .roleDimLogic2(ecmBusExtendDTO.getAppCode(), ecmAppDef.getRightVer(),
                                        token);
                    }
                    map.put(id, ecmDocrightDefDTOS);
                }

                busiIdList.addAll(ecmBusiInfos);
            } else {
                //动态树资料权限或者多维度权限（需要根据传入信息手动建模）
                //设置机构号
                ecmBusExtendDTO.setOrgCode(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode());
                //处理动态树
                EcmBusiInfo ecmBusiInfo = dealDynamicTree(ecmBusExtendDTO, token, map);
                //业务ID集合
                busiIdList.add(ecmBusiInfo);
            }
        }
        return busiIdList;
    }

    /**
     * 处理静态树
     */
    private List<EcmBusiInfo> dealStaticTree(EcmBusExtendDTO ecmBusExtendDTO,
                                             AccountTokenExtendDTO token) {
        List<EcmBusiInfo> list = new ArrayList<>();
        //新增业务
        EcmBusiInfo ecmBusiInfo = saveStaticTreeDataInfo(ecmBusExtendDTO, token);

        list.add(ecmBusiInfo);
        return list;
    }

    private UserBusiRedisDTO handleRedisUser(EcmRootDataDTO ecmRootDataDTO,
                                             AccountTokenExtendDTO token, List<String> appCode,
                                             List<EcmBusiInfo> ecmBusiInfoList) {
        //设置用户信息
        UserBusiRedisDTO userBusiRedisDTO = new UserBusiRedisDTO();
        List<Long> busiId = new ArrayList<>();
        List<EcmBusiInfo> ecmBusiInfos = new ArrayList<>();
        Map<Long, List<String>> map = new HashMap<>();
        Map<String, Map<String, List<EcmBusiInfo>>> collect2 = ecmBusiInfoList.stream()
                .collect(Collectors.groupingBy(EcmBusiInfo::getAppCode,
                        Collectors.groupingBy(EcmBusiInfo::getBusiNo)));

        for (EcmBusExtendDTO s : ecmRootDataDTO.getEcmBusExtendDTOS()) {
            Map<String, List<EcmBusiInfo>> stringListMap = collect2.get(s.getAppCode());
            if (stringListMap != null) {
                List<EcmBusiInfo> ecmBusiInfos1 = stringListMap.get(s.getBusiNo());
                if (!CollectionUtils.isEmpty(ecmBusiInfos1)) {
                    busiId.add(ecmBusiInfos1.get(0).getBusiId());
                    ecmBusiInfos.add(ecmBusiInfos1.get(0));
                    map.put(ecmBusiInfos1.get(0).getBusiId(), s.getEcmDocCodes());
                }
            }
        }
        if (!CollectionUtil.isEmpty(map.keySet())) {
            userBusiRedisDTO.setDocCodeShow(map);
        }
        userBusiRedisDTO.setAppType(appCode);
        userBusiRedisDTO.setBusiId(busiId);
        ArrayList<AppTypeBusiVO> objects = new ArrayList<>();

        Map<String, List<EcmBusiInfo>> collect = ecmBusiInfos.stream()
                .collect(Collectors.groupingBy(EcmBusiInfo::getAppCode));
        for (String s : collect.keySet()) {
            AppTypeBusiVO appTypeBusiVo = new AppTypeBusiVO();
            appTypeBusiVo.setAppCode(s);
            List<EcmBusiInfo> ecmBusiInfos1 = collect.get(s);
            List<Long> collect1 = ecmBusiInfos1.stream().map(EcmBusiInfo::getBusiId)
                    .collect(Collectors.toList());
            appTypeBusiVo.setBusiIds(collect1);
            objects.add(appTypeBusiVo);
        }
        userBusiRedisDTO.setRelation(objects);
        String pageFlag = UUIDUtils.generateUUID();

        //新增当前用户影像收集关联的业务类型id和业务id缓存
        if (ecmRootDataDTO != null) {
            userBusiRedisDTO.setUsercode(ecmRootDataDTO.getEcmBaseInfoDTO().getUserCode());
            userBusiRedisDTO.setUsername(ecmRootDataDTO.getEcmBaseInfoDTO().getUserName());
            userBusiRedisDTO.setOrg(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode());
            userBusiRedisDTO.setOrgName(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgName());
            userBusiRedisDTO.setRole(ecmRootDataDTO.getEcmBaseInfoDTO().getRoleCode());
            ecmRootDataDTO.setFlagId(pageFlag);
        }
        return userBusiRedisDTO;
    }

    /**
     * 保存业务基本信息
     */
    private EcmBusiInfo saveEcmBusiInfo(EcmBusiInfoRedisDTO ecmBusiInfoExtend,
                                        AccountTokenExtendDTO token, Integer treeTypeFlag) {
        AssertUtils.isNull(ecmBusiInfoExtend.getBusiNo(), "业务编号不能为空");
        AssertUtils.isNull(ecmBusiInfoExtend.getAppCode(), "业务类型不能为空");
        //判断业务编号是否已存在
        if (Objects.equals(treeTypeFlag, IcmsConstants.STATIC_TREE)) {
            List<EcmBusiInfo> busiInfos = ecmBusiInfoMapper
                    .selectList(new LambdaQueryWrapper<EcmBusiInfo>()
                            .eq(EcmBusiInfo::getAppCode, ecmBusiInfoExtend.getAppCode())
                            .eq(EcmBusiInfo::getBusiNo, ecmBusiInfoExtend.getBusiNo()));
            AssertUtils.isTrue(CollectionUtil.isNotEmpty(busiInfos), "业务编号已存在");
        }
        //获取当前业务类型所对应的权限版本
        List<EcmAppDocright> appDocrights = ecmAppDocrightMapper
                .selectList(new LambdaQueryWrapper<EcmAppDocright>()
                        .eq(EcmAppDocright::getAppCode, ecmBusiInfoExtend.getAppCode())
                        .eq(EcmAppDocright::getRightNew, StateConstants.COMMON_ONE));
        if (IcmsConstants.STATIC_TREE.equals(treeTypeFlag)
                && CollectionUtils.isEmpty(appDocrights)) {
            AssertUtils.isTrue(true, "无法新增业务，该业务类型未配置业务资料权限版本");
        }
        //创建人
        ecmBusiInfoExtend.setOrgCode(token.getOrgCode());
        ecmBusiInfoExtend.setOrgName(token.getOrgName());
        ecmBusiInfoExtend.setCreateUser(token.getUsername());
        ecmBusiInfoExtend.setCreateUserName(token.getName());
        //静态树
        if (IcmsConstants.STATIC_TREE.equals(treeTypeFlag)) {
            ecmBusiInfoExtend.setTreeType(IcmsConstants.STATIC_TREE);
            //资料权限版本
            ecmBusiInfoExtend.setRightVer(appDocrights.get(0).getRightVer());
        } else {
            //动态树,动态树默认版本都为0
            ecmBusiInfoExtend.setRightVer(IcmsConstants.ZERO);
            ecmBusiInfoExtend.setTreeType(IcmsConstants.DYNAMIC_TREE);
        }

        EcmBusiInfo ecmBusiInfo = new EcmBusiInfo();
        BeanUtils.copyProperties(ecmBusiInfoExtend, ecmBusiInfo);
        ecmBusiInfo.setBusiId(snowflakeUtil.nextId());
        ecmBusiInfoExtend.setBusiId(ecmBusiInfo.getBusiId());
        ecmBusiInfoMapper.insert(ecmBusiInfo);
        //        EcmBusiInfo busiInfo = ecmBusiInfoMapper.selectOne(new LambdaQueryWrapper<EcmBusiInfo>()
        //                .eq(EcmBusiInfo::getBusiNo, ecmBusiInfoExtend.getBusiNo())
        //                .eq(EcmBusiInfo::getAppCode, ecmBusiInfoExtend.getAppCode()));
        //        ecmBusiInfoExtend.setBusiId(busiInfo.getBusiId());
        //        BeanUtils.copyProperties(busiInfo, ecmBusiInfo);
        return ecmBusiInfo;
    }

    /**
     * 保存业务基本信息
     */
    private EcmBusiInfo updateEcmBusiInfo(EcmBusiInfoRedisDTO ecmBusiInfoExtend,
                                          AccountTokenExtendDTO token, Integer treeTypeFlag) {
        AssertUtils.isNull(ecmBusiInfoExtend.getBusiNo(), "业务编号不能为空");
        AssertUtils.isNull(ecmBusiInfoExtend.getAppCode(), "业务类型不能为空");
        //获取当前业务类型所对应的权限版本
        List<EcmAppDocright> appDocrights = ecmAppDocrightMapper
                .selectList(new LambdaQueryWrapper<EcmAppDocright>()
                        .eq(EcmAppDocright::getAppCode, ecmBusiInfoExtend.getAppCode())
                        .eq(EcmAppDocright::getRightNew, StateConstants.COMMON_ONE));
        if (IcmsConstants.STATIC_TREE.equals(treeTypeFlag)
                && CollectionUtils.isEmpty(appDocrights)) {
            AssertUtils.isTrue(true, "无法修改业务，该业务类型未配置业务资料权限版本");
        }
        //创建人
        ecmBusiInfoExtend.setOrgCode(token.getOrgCode());
        ecmBusiInfoExtend.setOrgName(token.getOrgName());
        ecmBusiInfoExtend.setCreateUser(token.getUsername());
        ecmBusiInfoExtend.setCreateUserName(token.getName());
        //静态树
        if (IcmsConstants.STATIC_TREE.equals(treeTypeFlag)) {
            ecmBusiInfoExtend.setTreeType(IcmsConstants.STATIC_TREE);
            //资料权限版本
            ecmBusiInfoExtend.setRightVer(appDocrights.get(0).getRightVer());
        } else {
            //动态树,动态树默认版本都为0
            ecmBusiInfoExtend.setRightVer(IcmsConstants.ZERO);
            ecmBusiInfoExtend.setTreeType(IcmsConstants.DYNAMIC_TREE);
        }

        EcmBusiInfo ecmBusiInfo = new EcmBusiInfo();
        BeanUtils.copyProperties(ecmBusiInfoExtend, ecmBusiInfo);
        commonService.updateEcmBusiInfo(ecmBusiInfo, token.getUsername());
        EcmBusiInfo busiInfo = ecmBusiInfoMapper.selectOne(new LambdaQueryWrapper<EcmBusiInfo>()
                .eq(EcmBusiInfo::getBusiNo, ecmBusiInfoExtend.getBusiNo())
                .eq(EcmBusiInfo::getAppCode, ecmBusiInfoExtend.getAppCode()));
        ecmBusiInfoExtend.setBusiId(busiInfo.getBusiId());
        BeanUtils.copyProperties(busiInfo, ecmBusiInfo);
        return ecmBusiInfo;
    }

    /**
     * 保存业务属性
     */
//    @Transactional(rollbackFor = Exception.class)
    private void saveOrUpdateEcmBusiMetadata(EcmBusiInfoRedisDTO ecmBusiInfoExtend,
                                             Integer operateFlag) {
        List<EcmAppAttrDTO> appAttrs = ecmBusiInfoExtend.getAttrList();
        if (CollectionUtils.isEmpty(appAttrs)) {
            return;
        }
        for (EcmAppAttrDTO extend : appAttrs) {
            //需要根据业务属性代码和业务属性值查业务属性id
            String appCode = ecmBusiInfoExtend.getAppCode();
            String appAttrValue = extend.getAttrCode();
            LambdaQueryWrapper<EcmAppAttr> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EcmAppAttr::getAppCode,appCode).eq(EcmAppAttr::getAttrCode,appAttrValue);
            List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(wrapper);
            if (CollectionUtil.isNotEmpty(ecmAppAttrs)) {
                EcmAppAttr ecmAppAttr = ecmAppAttrs.get(0);
                EcmBusiMetadata ecmBusiMetadata = new EcmBusiMetadata();
                if (IcmsConstants.OPERATE_FLAG_ADD.equals(operateFlag)) {
                    ecmBusiMetadata.setBusiId(ecmBusiInfoExtend.getBusiId());
                    ecmBusiMetadata.setAppAttrId(ecmAppAttr.getAppAttrId());
                    ecmBusiMetadata.setAppAttrVal(extend.getAppAttrValue());
                    //todo 改批量插入
                    ecmBusiMetadataMapper.insert(ecmBusiMetadata);
                } else {
                    LambdaUpdateWrapper<EcmBusiMetadata> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(EcmBusiMetadata::getBusiId, ecmBusiInfoExtend.getBusiId());
                    updateWrapper.eq(EcmBusiMetadata::getAppAttrId, ecmAppAttr.getAppAttrId());
                    ecmBusiMetadata.setAppAttrVal(extend.getAppAttrValue());
                    ecmBusiMetadataMapper.update(ecmBusiMetadata, updateWrapper);
                }

            }
        }
    }

    /**
     * 新增业务信息
     */
    @LogManageAnnotation("新增业务")
    private EcmBusiInfo saveStaticTreeDataInfo(EcmBusExtendDTO busExtendDTO,
                                               AccountTokenExtendDTO token) {
        //根据业务类型Code获取业务类型id
        String appCode = saveCaptureInfo(busExtendDTO.getAppCode(), token);
        EcmAppDef ecmAppDef = ecmAppDefMapper
                .selectOne(new LambdaQueryWrapper<EcmAppDef>().eq(EcmAppDef::getAppCode, appCode));
        EcmBusiInfoRedisDTO ecmBusiInfoExtend = new EcmBusiInfoRedisDTO();
        //映射基础信息
        ecmBusiInfoExtend.setBusiNo(busExtendDTO.getBusiNo());
        ecmBusiInfoExtend.setAppCode(appCode);
        ecmBusiInfoExtend.setAppTypeName(ecmAppDef.getAppName());
        ecmBusiInfoExtend.setOrgCode(busExtendDTO.getOrgCode());
        ecmBusiInfoExtend.setCreateTime(new Date());
        ecmBusiInfoExtend.setStatus(BusiInfoConstants.BUSI_STATUS_ZERO);
        ecmBusiInfoExtend.setDelegateType(busExtendDTO.getDelegateType());
        ecmBusiInfoExtend.setTypeBig(busExtendDTO.getTypeBig());
        ecmBusiInfoExtend.setSourceSystem(busExtendDTO.getSourceSystem());
        log.info("静态树保存业务信息 - delegateType: {}, typeBig: {}, sourceSystem: {}",
                busExtendDTO.getDelegateType(),
                busExtendDTO.getTypeBig(),
                busExtendDTO.getSourceSystem());
        EcmBusiInfo ecmBusiInfo = saveEcmBusiInfo(ecmBusiInfoExtend, token,
                IcmsConstants.STATIC_TREE);
        List<EcmBusiAttrDTO> attrDTOList = busExtendDTO.getEcmBusiAttrDTOList();
        List<EcmAppAttrDTO> ecmAppAttrDTOS = new ArrayList<>();
        for (EcmBusiAttrDTO ecmBusiAttrDTO : attrDTOList) {
            EcmAppAttr ecmAppAttr = null;
            if (ecmBusiAttrDTO.getAppAttrId() != null) {
                ecmAppAttr = ecmAppAttrMapper.selectById(ecmBusiAttrDTO.getAppAttrId());
            } else {
                ecmAppAttr = ecmAppAttrMapper.selectOne(
                        new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, appCode)
                                .eq(EcmAppAttr::getAttrCode, ecmBusiAttrDTO.getAttrCode()));
            }
            if (ecmAppAttr == null) {
                throw new SunyardException(ResultCode.PARAM_ERROR, "无指定业务属性");
            }
            EcmAppAttrDTO ecmAppAttrDTO = new EcmAppAttrDTO();
            BeanUtils.copyProperties(ecmAppAttr, ecmAppAttrDTO);
            BeanUtils.copyProperties(ecmBusiAttrDTO, ecmAppAttrDTO);
            ecmAppAttrDTO.setAppAttrId(ecmAppAttr.getAppAttrId());
            ecmAppAttrDTO.setAttrName(ecmAppAttr.getAttrName());
            ecmBusiAttrDTO.setAttrName(ecmAppAttr.getAttrName());
            ecmAppAttrDTOS.add(ecmAppAttrDTO);
        }
        ecmBusiInfoExtend.setAttrList(ecmAppAttrDTOS);
        //todo es添加业务属性
        operateFullQueryService.addEsBusiInfo(ecmBusiInfoExtend, token.getId());
        saveOrUpdateEcmBusiMetadata(ecmBusiInfoExtend, IcmsConstants.OPERATE_FLAG_ADD);
        //添加业务类型关联的资料类型静态树
        List<EcmDocTreeDTO> ecmDocTreeDTOS = modelBusiService
                .searchOldRelevanceInformation(appCode);
        ecmDocTreeDTOS = ecmDocTreeDTOS.stream()
                .sorted(Comparator.comparing(EcmDocTreeDTO::getDocSort))
                .collect(Collectors.toList());
        List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = copyTree(ecmDocTreeDTOS);
        ecmBusiInfoExtend.setEcmBusiDocRedisDTOS(ecmBusiDocRedisDTOS);
        //redis添加采集页面业务相关信息
        busiCacheService.addBusiExtendInfoToRedis(ecmBusiInfoExtend, token, busExtendDTO);
        //redis更新用户-业务相关信息
        //        ecmCaptureService.updateUserBusiToRedis(ecmBusiInfoExtend, token,ecmRootDataDTO);
        //添加操作记录表
        busiOperationService.addOperation(ecmBusiInfoExtend.getBusiId(), IcmsConstants.ADD_BUSI,
                token, "新增业务");
        return ecmBusiInfo;
    }

    /**
     * 组装资料树
     */
    private List<EcmBusiDocRedisDTO> copyTree(List<EcmDocTreeDTO> sourceTree) {
        List<EcmBusiDocRedisDTO> copiedTree = new ArrayList<>();
        if (sourceTree != null) {
            for (EcmDocTreeDTO sourceNode : sourceTree) {
                EcmBusiDocRedisDTO copiedNode = new EcmBusiDocRedisDTO();
                // 复制节点属性，可以根据需要复制其他属性
                BeanUtil.copyProperties(sourceNode, copiedNode);
                // 设置type属性
                copiedNode.setNodeType(sourceNode.getType());
                //                copiedNode.setDocId(sourceNode.getDocCode());
                List<EcmDocTreeDTO> children = sourceNode.getChildren();
                // 递归复制子节点
                copiedNode.setChildren(
                        BeanUtil.copyToList(copyTree(children), EcmBusiDocRedisDTO.class));
                copiedTree.add(copiedNode);
            }
        }
        return copiedTree;
    }

    /**
     * 新增采集
     */
    private String saveCaptureInfo(String appCode, AccountTokenExtendDTO token) {
        //        //根据业务类型code获取业务类型Id
        //        LambdaQueryWrapper<EcmAppDef> wrapper = new LambdaQueryWrapper<>();
        //        wrapper.eq(StrUtil.isNotBlank(appCode), EcmAppDef::getAppCode, appCode);
        //        //使用selectList避免脏数据，防止手动输入业务类型id重复时报错
        //        List<EcmAppDef> ecmAppDefList = ecmAppDefMapper.selectList(wrapper);
        //        if (CollectionUtil.isEmpty(ecmAppDefList)) {
        //            return null;
        //        }
        //        EcmAppDef ecmAppDef = ecmAppDefList.get(0);
        //        String appCode1 = ecmAppDef.getAppCode();
        //新增采集
        operateCaptureService.addCapture(new ArrayList<>(Arrays.asList(appCode)), token);
        return appCode;
    }

    /**
     * 处理动态树或者多维度（数据来源外部）
     */
    @LogManageAnnotation("新增业务")
    public EcmBusiInfo dealDynamicTree(EcmBusExtendDTO busExtendDTO, AccountTokenExtendDTO token,
                                       Map<Long, List<EcmDocrightDefDTO>> map) {
        //新增采集
        String appCode = saveCaptureInfo(busExtendDTO.getAppCode(), token);
        EcmAppDef ecmAppDef = ecmAppDefMapper
                .selectOne(new LambdaQueryWrapper<EcmAppDef>().eq(EcmAppDef::getAppCode, appCode));
        EcmBusiInfoRedisDTO ecmBusiInfoExtend = new EcmBusiInfoRedisDTO();
        //映射基础信息
        ecmBusiInfoExtend.setBusiNo(busExtendDTO.getBusiNo());
        ecmBusiInfoExtend.setAppCode(appCode);
        ecmBusiInfoExtend.setAppTypeName(ecmAppDef.getAppName());
        ecmBusiInfoExtend.setOrgCode(busExtendDTO.getOrgCode());
        ecmBusiInfoExtend.setCreateTime(new Date());
        ecmBusiInfoExtend.setDelegateType(busExtendDTO.getDelegateType());
        ecmBusiInfoExtend.setTypeBig(busExtendDTO.getTypeBig());
        ecmBusiInfoExtend.setSourceSystem(busExtendDTO.getSourceSystem());
        log.info("动态书保存业务信息 - delegateType: {}, typeBig: {}, sourceSystem: {}",
                busExtendDTO.getDelegateType(),
                busExtendDTO.getTypeBig(),
                busExtendDTO.getSourceSystem());
        EcmBusiInfo ecmBusiInfo = saveEcmBusiInfo(ecmBusiInfoExtend, token,
                IcmsConstants.DYNAMIC_TREE);
        ecmBusiInfoExtend.setBusiId(ecmBusiInfo.getBusiId());
        //业务属性设置
        List<EcmBusiAttrDTO> attrDTOList = busExtendDTO.getEcmBusiAttrDTOList();
        List<EcmAppAttrDTO> ecmAppAttrDTOS = new ArrayList<>();
        for (EcmBusiAttrDTO ecmBusiAttrDTO : attrDTOList) {
            LambdaQueryWrapper<EcmAppAttr> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EcmAppAttr::getAppCode, appCode).eq(EcmAppAttr::getAttrCode,
                    ecmBusiAttrDTO.getAttrCode());
            EcmAppAttr ecmAppAttr = ecmAppAttrMapper.selectOne(wrapper);
            AssertUtils.isNull(ecmAppAttr,
                    "该业务类型未定义属性：" + "【" + ecmBusiAttrDTO.getAttrCode() + "】");
            EcmAppAttrDTO ecmAppAttrDTO = new EcmAppAttrDTO();
            BeanUtils.copyProperties(ecmAppAttr, ecmAppAttrDTO);
            ecmAppAttrDTO.setAttrName(ecmAppAttr.getAttrName());
            ecmAppAttrDTO.setAppAttrValue(ecmBusiAttrDTO.getAppAttrValue());
            ecmAppAttrDTOS.add(ecmAppAttrDTO);
        }
        ecmBusiInfoExtend.setAttrList(ecmAppAttrDTOS);
        //插入es
        operateFullQueryService.addEsBusiInfo(ecmBusiInfoExtend, token.getId());
        saveOrUpdateEcmBusiMetadata(ecmBusiInfoExtend, IcmsConstants.OPERATE_FLAG_ADD);
        //动态树传入资料和权限
        List<EcmDocrightDefDTO> docrightDefExtends = null;
        if (CollectionUtil.isNotEmpty(busExtendDTO.getEcmVTreeDataDTOS())) {
            //动态树资料入库
            saveEcmBusiDoc(ecmBusiInfoExtend, busExtendDTO.getEcmVTreeDataDTOS(), new ArrayList<>(),
                    token);
            //isAdd用于判断是否是分片上传
            Integer isAdd = busExtendDTO.getIsAdd();
            if (IcmsConstants.ONE.equals(isAdd)) {
                //文件上传动态树资料权限绑定
                docrightDefExtends = dealTreeDocRightUpload(ecmBusiInfo);
            } else {
                //动态树资料权限绑定
                docrightDefExtends = dealTreeDocRight(busExtendDTO, ecmBusiInfo.getBusiId());
            }
        }
        //数据存入Redis
//        ecmBusiInfoExtend.setDocRightList(docrightDefExtends);
        //处理压缩参数
        boolean isCompress = ObjectUtils.isEmpty(busExtendDTO.getIsCompress());
        boolean compressSize = ObjectUtils.isEmpty(busExtendDTO.getCompressSize());
        boolean compressValue = ObjectUtils.isEmpty(busExtendDTO.getCompressValue());
        if (isCompress || compressSize || compressValue) {
            //获取压缩比
            EcmAppDef redisZip = busiCacheService.getRedisZip(busExtendDTO.getAppCode());
            ecmBusiInfoExtend.setIsQulity(isCompress ? redisZip.getIsResize()
                    : Integer.valueOf(busExtendDTO.getIsCompress()));
            ecmBusiInfoExtend.setQulity(compressSize ? redisZip.getQulity()
                    : Float.valueOf(busExtendDTO.getCompressValue()));
            ecmBusiInfoExtend.setResiz(compressValue ? redisZip.getResize()
                    : Integer.valueOf(busExtendDTO.getCompressSize()));
        }
        map.put(ecmBusiInfo.getBusiId(), docrightDefExtends);
        busiCacheService.saveAndUpate(ecmBusiInfoExtend);
        //添加操作记录表
        busiOperationService.addOperation(ecmBusiInfoExtend.getBusiId(), IcmsConstants.ADD_BUSI,
                token, "新增业务");
        return ecmBusiInfo;
    }

    /**
     * 动态树资料权限绑定
     */
    private List<EcmDocrightDefDTO> dealTreeDocRight(EcmBusExtendDTO busExtendDTO,
                                                     Long ecmBusiInfo) {
        List<EcmDocrightDefDTO> list = new ArrayList<>();
        LambdaQueryWrapper<EcmBusiDoc> busiDocWrapper = new LambdaQueryWrapper<>();
        busiDocWrapper.eq(EcmBusiDoc::getBusiId, ecmBusiInfo);
        List<EcmBusiDoc> ecmBusiDocs = ecmBusiDocMapper.selectList(busiDocWrapper);
        if (CollectionUtil.isEmpty(ecmBusiDocs)) {
            return Collections.emptyList();
        }

        ArrayList<EcmVTreeDataDTO> ecmVTreeDataDTOS = new ArrayList<>();
        getLeafRight(busExtendDTO.getEcmVTreeDataDTOS(), ecmVTreeDataDTOS);
        Map<String, List<EcmVTreeDataDTO>> collect = ecmVTreeDataDTOS.stream()
                .collect(Collectors.groupingBy(EcmVTreeDataDTO::getDocCode));
        //动态树资料关联资料权限存入
        for (EcmBusiDoc ecmBusiDoc : ecmBusiDocs) {
            EcmDocrightDefDTO extend = new EcmDocrightDefDTO();
            extend.setDocName(ecmBusiDoc.getDocName());
            extend.setDocCode(ecmBusiDoc.getDocCode());
            List<EcmVTreeDataDTO> ecmVTreeDataDTOS1 = collect.get(ecmBusiDoc.getDocCode());
            if (!CollectionUtils.isEmpty(ecmVTreeDataDTOS1)) {
                EcmVTreeDataDTO treeDataDTO = ecmVTreeDataDTOS1.get(0);
                if (treeDataDTO.getMaxPages() != null) {
                    extend.setMaxLen(Integer.parseInt(treeDataDTO.getMaxPages()));
                } else {
                    extend.setMaxLen(DocRightConstants.ONE_THOUSAND);
                }
                if (treeDataDTO.getMinPages() != null) {
                    extend.setMinLen(Integer.parseInt(treeDataDTO.getMinPages()));
                } else {
                    extend.setMaxLen(0);
                }
                judgeRight(extend, treeDataDTO.getDocRight());
                list.add(extend);
            }
        }
        return list;
    }

    private void getLeafRight(List<EcmVTreeDataDTO> ecmVTreeDataDTOS,
                              List<EcmVTreeDataDTO> ecmVTreeDataDTO) {
        for (EcmVTreeDataDTO treeDataDTO : ecmVTreeDataDTOS) {
            if (!CollectionUtils.isEmpty(treeDataDTO.getEcmVTreeDataDTOS())) {
                ecmVTreeDataDTO.add(treeDataDTO);
                getLeafRight(treeDataDTO.getEcmVTreeDataDTOS(), ecmVTreeDataDTO);
            } else {
                ecmVTreeDataDTO.add(treeDataDTO);
            }
        }
    }

    private List<EcmDocrightDefDTO> dealTreeDocRightUpload(EcmBusiInfo ecmBusiInfo) {
        List<EcmDocrightDefDTO> list = new ArrayList<>();
        LambdaQueryWrapper<EcmBusiDoc> busiDocWrapper = new LambdaQueryWrapper<>();
        busiDocWrapper.eq(EcmBusiDoc::getBusiId, ecmBusiInfo.getBusiId());
        busiDocWrapper.eq(EcmBusiDoc::getDocCode, 0L);
        List<EcmBusiDoc> ecmBusiDocs = ecmBusiDocMapper.selectList(busiDocWrapper);
        ecmBusiDocs.removeIf(ecmBusiDoc -> Objects.equals(ecmBusiDoc.getParentId(),
                ecmBusiDoc.getBusiId().toString()));
        if (CollectionUtil.isEmpty(ecmBusiDocs)) {
            return Collections.emptyList();
        }
        //动态树资料关联资料权限存入
        for (EcmBusiDoc ecmBusiDoc : ecmBusiDocs) {
            EcmDocrightDefDTO extend = new EcmDocrightDefDTO();
            String docCode = ecmBusiDoc.getDocCode();
            LambdaQueryWrapper<EcmDocDef> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(EcmDocDef::getDocCode, docCode);
            EcmDocDef ecmDocDef = ecmDocDefMapper.selectOne(queryWrapper);
            extend.setDocCode(ecmDocDef.getDocCode());
            extend.setDocName(ecmBusiDoc.getDocName());
            extend.setPrintRight(IcmsConstants.ONE.toString());
            extend.setDownloadRight(IcmsConstants.ONE.toString());
            extend.setAddRight(IcmsConstants.ONE.toString());
            extend.setDeleteRight(IcmsConstants.ONE.toString());
            extend.setUpdateRight(IcmsConstants.ONE.toString());
            extend.setThumRight(IcmsConstants.ONE.toString());
            extend.setReadRight(IcmsConstants.ONE.toString());
            extend.setOtherUpdate(IcmsConstants.ONE.toString());
            list.add(extend);
        }
        return list;
    }

    /**
     * 比较权限
     */
    private void judgeRight(EcmDocrightDefDTO extend, String right) {
        extend.setAddRight("0");
        extend.setReadRight("0");
        extend.setUpdateRight("0");
        extend.setPrintRight("0");
        extend.setThumRight("0");
        extend.setDownloadRight("0");
        extend.setDeleteRight("0");
        extend.setOtherUpdate("0");
        if (StrUtil.isNotBlank(right)) {
            char[] chars = right.toUpperCase().toCharArray();
            for (char c : chars) {
                if ('C' == c) {
                    extend.setAddRight("1");
                } else if ('R' == c) {
                    extend.setReadRight("1");
                } else if ('U' == c) {
                    extend.setUpdateRight("1");
                } else if ('S' == c) {
                    extend.setDownloadRight("1");
                } else if ('P' == c) {
                    extend.setPrintRight("1");
                } else if ('T' == c) {
                    extend.setThumRight("1");
                } else if ('D' == c) {
                    extend.setDeleteRight("1");
                } else if ('O' == c) {
                    extend.setOtherUpdate("1");
                }
            }
        }
    }

    /**
     * 保存动态文档树及树形父子关系
     */
    private void saveEcmBusiDoc(EcmBusiInfoRedisDTO ecmBusiInfoExtend,
                                List<EcmVTreeDataDTO> ecmVTreeDataDTOS,
                                List<String> ecmVTreeDataDTOS1, AccountTokenExtendDTO token) {
        if (CollectionUtils.isEmpty(ecmVTreeDataDTOS)) {
            return;
        }
        Long busiId = ecmBusiInfoExtend.getBusiId();
        //TODO处理排序问题
        processVTreeNode(ecmVTreeDataDTOS);
        //外部动态树根节点的父节点设置为业务ID
        saveVTreeDoc(ecmVTreeDataDTOS, busiId, busiId, token, ecmVTreeDataDTOS1, ecmBusiInfoExtend);
        //构建资料类型动态树（动态树资料节点入库后）
        LambdaQueryWrapper<EcmBusiDoc> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmBusiDoc::getBusiId, busiId);
        List<EcmBusiDoc> ecmBusiDocs = ecmBusiDocMapper.selectList(queryWrapper);
        EcmBusiDocRedisDTO dto = new EcmBusiDocRedisDTO();
        dto.setDocId(busiId);
        //组装动态树
        commonService.buildDynTree(dto, ecmBusiDocs);
        ecmBusiInfoExtend.setEcmBusiDocRedisDTOS(dto.getChildren());
    }

    private void processVTreeNode(List<EcmVTreeDataDTO> treeNodes) {
        if (treeNodes == null || treeNodes.isEmpty()) {
            return;
        }
        //根节点设置为1
        treeNodes.get(0).setDocOrder("1");
        //递归处理后续子节点
        handleVTreeOrder(treeNodes);

    }


    private void handleVTreeOrder(List<EcmVTreeDataDTO> treeNodes) {
        if (treeNodes == null || treeNodes.isEmpty()) {
            return;
        }
        //根节点设置为
        for (EcmVTreeDataDTO node : treeNodes) {
            // 处理当前节点的子节点
            List<EcmVTreeDataDTO> children = node.getEcmVTreeDataDTOS();
            if (children != null && !children.isEmpty()) {
                // 为子节点设置order
                for (int i = 0; i < children.size(); i++) {
                    children.get(i).setDocOrder(String.valueOf(i + 1)); // order从1开始
                }

                // 递归处理子节点
                handleVTreeOrder(children);
            }
        }
    }

    private void saveVTreeDoc(List<EcmVTreeDataDTO> ecmVTreeDataDTOS, Long busiId, Long parentId,
                              AccountTokenExtendDTO token, List<String> ecmVTreeDataDTOS1,
                              EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        for (EcmVTreeDataDTO vTreeDataDTO : ecmVTreeDataDTOS) {
            if (CollectionUtil.isEmpty(vTreeDataDTO.getEcmVTreeDataDTOS())) {
                if (!ecmVTreeDataDTOS1.contains(vTreeDataDTO.getDocCode())) {
                    buildDocTree(busiId, vTreeDataDTO, parentId, IcmsConstants.ONE, token,
                            ecmBusiInfoExtend);
                } else {
                    //更新
                    ecmBusiDocMapper.update(null,
                            new LambdaUpdateWrapper<EcmBusiDoc>()
                                    .set(EcmBusiDoc::getDocName, vTreeDataDTO.getDocName())
                                    .eq(EcmBusiDoc::getDocCode, vTreeDataDTO.getDocCode())
                                    .eq(EcmBusiDoc::getBusiId, busiId));
                }
            } else {
                Long aLong = null;
                if (!ecmVTreeDataDTOS1.contains(vTreeDataDTO.getDocCode())) {
                    aLong = buildDocTree(busiId, vTreeDataDTO, parentId, IcmsConstants.ZERO, token,
                            ecmBusiInfoExtend);
                } else {
                    LambdaQueryWrapper<EcmBusiDoc> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(EcmBusiDoc::getBusiId, busiId);
                    queryWrapper.eq(EcmBusiDoc::getDocCode, vTreeDataDTO.getDocCode());
                    List<EcmBusiDoc> ecmBusiDocs = ecmBusiDocMapper.selectList(queryWrapper);
                    if (!CollectionUtils.isEmpty(ecmBusiDocs)) {
                        aLong = ecmBusiDocs.get(0).getDocId();
                        if (vTreeDataDTO.getDocName() != null && !vTreeDataDTO.getDocName()
                                .equals(ecmBusiDocs.get(0).getDocName())) {
                            //更新
                            ecmBusiDocMapper.update(null,
                                    new LambdaUpdateWrapper<EcmBusiDoc>()
                                            .set(EcmBusiDoc::getDocName, vTreeDataDTO.getDocName())
                                            .eq(EcmBusiDoc::getDocCode, vTreeDataDTO.getDocCode())
                                            .eq(EcmBusiDoc::getBusiId, busiId));
                        }
                    }

                }
                saveVTreeDoc(vTreeDataDTO.getEcmVTreeDataDTOS(), busiId, aLong, token,
                        ecmVTreeDataDTOS1, ecmBusiInfoExtend);
            }
        }

        //        //非叶子节点
        //        List<EcmVTreeDataDTO> parentDocTree = ecmVTreeDataDTOS.stream().filter(s -> s.getChildFlag().equals("0")).collect(Collectors.toList());
        //        //叶子节点
        //        List<EcmVTreeDataDTO> childDocTree = ecmVTreeDataDTOS.stream().filter(s -> !s.getChildFlag().equals("0")).collect(Collectors.toList());
        //        if (!CollectionUtil.isEmpty(parentDocTree)) {
        //            for (EcmVTreeDataDTO treeDataDTO : parentDocTree) {
        //                //非叶子节点入库
        //                String docCode = buildDocTree(busiId, treeDataDTO, parentId, IcmsConstants.ZERO, token);
        //                //获取子节点
        //                List<EcmVTreeDataDTO> ecmVTreeDataChildDTOS = treeDataDTO.getEcmVTreeDataDTOS();
        //                if (!CollectionUtils.isEmpty(ecmVTreeDataChildDTOS)) {
        //                    saveVTreeDoc(ecmVTreeDataChildDTOS, busiId, docCode, token);
        //                }
        //            }
        //        }
        //        if (!CollectionUtil.isEmpty(childDocTree)) {
        //            for (EcmVTreeDataDTO ecmVTreeDataDTO : childDocTree) {
        //                buildDocTree(busiId, ecmVTreeDataDTO, parentId, IcmsConstants.ONE, token);
        //            }
        //        }
    }

    /**
     * 资料数据入库
     */
    private Long buildDocTree(Long busiId, EcmVTreeDataDTO treeDataDTO, Long parentId, Integer leaf,
                              AccountTokenExtendDTO token, EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        EcmBusiDoc ecmBusiDoc = new EcmBusiDoc();
        String docCode = treeDataDTO.getDocCode();
        String docName = treeDataDTO.getDocName();
        ecmBusiDoc.setBusiId(busiId);
        //外部动态树
        ecmBusiDoc.setDocCode(docCode);
        ecmBusiDoc.setDocName(docName);
        //资料顺序外部数据为定义，暂时设定默认值
        ecmBusiDoc.setDocSort(Float.valueOf(treeDataDTO.getDocOrder()));
        ecmBusiDoc.setDocMark(StateConstants.ZERO);
        //        EcmBusiDocRel ecmBusiDocRel = new EcmBusiDocRel();
        ecmBusiDoc.setParentId(parentId);

        //动态树资料节点表
        ecmBusiDocMapper.insert(ecmBusiDoc);

        //如果父级节点，并且父级杰顿存在文件，需要将文件重归类至未归类中
        EcmBusiDoc ecmBusiDoc1 = ecmBusiDocMapper.selectById(parentId);
        if (ecmBusiDoc1 != null) {
            //放入未归类中
            int update = ecmFileInfoMapper.update(null,
                    new LambdaUpdateWrapper<EcmFileInfo>()
                            .set(EcmFileInfo::getDocCode, IcmsConstants.UNCLASSIFIED_ID)
                            .eq(EcmFileInfo::getDocCode, ecmBusiDoc1.getDocCode())
                            .eq(EcmFileInfo::getBusiId, busiId));
            if (update > 0) {
                //刷新redis
                List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService
                        .getFileInfoRedis(busiId);
                fileInfoRedisEntities.forEach(s -> {
                    if (s.getDocCode().equals(ecmBusiDoc1.getDocCode())
                            && s.getBusiId().equals(busiId)) {
                        s.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
                        s.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                    }
                });
                busiCacheService.updateFileInfoRedis(fileInfoRedisEntities);
            }

        }

        //刷新redis
        return ecmBusiDoc.getDocId();
    }

    //获取业务ID
    private EcmBusiInfo getBusiId(EcmBusExtendDTO dto, String appCode) {
        //业务主索引
        String businessNo = dto.getBusiNo();
        //获取业务ID
        LambdaQueryWrapper<EcmBusiInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmBusiInfo::getBusiNo, businessNo).eq(EcmBusiInfo::getAppCode, appCode);
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectOne(wrapper);
        if (Objects.isNull(ecmBusiInfo)) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "当前业务不存在!");
        }
        return ecmBusiInfo;
    }

    //获取业务类型ID
    private String getAppTypeId(EcmBusExtendDTO dto) {
        //通过业务类型代码获取业务类型ID\校验是否存在
        String appCode = dto.getAppCode();
        LambdaQueryWrapper<EcmAppDef> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmAppDef::getAppCode, appCode);
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectOne(queryWrapper);
        if (Objects.isNull(ecmAppDef)) {
            throw new SunyardException("业务类型代码有误");
        }
        return ecmAppDef.getAppCode();
    }

    //动态树过滤数据设置权限
    private void vTreeFilterInfoAndSetRight(EcmBusExtendDTO dto,
                                            BusiInfoAndFileVO busiInfoAndFileInfo) {
        //获取动态树节点
        //        List<EcmVTreeDataDTO> vTreeDto = dto.getEcmVTreeDataDTOS();
        Map<String, String> map = new HashMap<>();
        //用map保存指定查询的资料类型节点ID和对应权限
        //        getLeafNodeRight(vTreeDto, map);
        //过滤业务资料树，保留指定查询的资料节点
        List<EcmBusiStructureTreeDTO> treeDTOList = busiInfoAndFileInfo
                .getEcmBusiStructureTreeDTOList();
        treeDTOList.forEach(t -> filterVTree(t, map));
        //过滤文件,保留指定资料节点下文件
        List<FileInfoRedisDTO> fileList = busiInfoAndFileInfo.getFileList();
        setFilesList(map, fileList);
    }

    private void getLeafNodeRight(List<EcmVTreeDataDTO> vTreeDto, Map<String, String> map) {
        vTreeDto.forEach(v -> {
            if (!Objects.equals(v.getChildFlag(), IcmsConstants.ONE.toString())) {
                List<EcmVTreeDataDTO> ecmVTreeDataDTOS = v.getEcmVTreeDataDTOS();
                getLeafNodeRight(ecmVTreeDataDTOS, map);
            } else {
                LambdaQueryWrapper<EcmDocDef> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(EcmDocDef::getDocCode, v.getDocCode());
                EcmDocDef e = ecmDocDefMapper.selectOne(queryWrapper);
                map.put(e.getDocCode(), v.getDocRight());
            }
        });
    }

    //处理文件
    private void setFilesList(Map<String, String> map, List<FileInfoRedisDTO> fileList) {
        //过滤掉非指定资料类型下的文件
        List<FileInfoRedisDTO> removeList = new ArrayList<>();
        for (FileInfoRedisDTO fileInfoRedisDTO : fileList) {
            String docCode = fileInfoRedisDTO.getDocCode();
            if (filterFiles(docCode, map)) {
                removeList.add(fileInfoRedisDTO);
            }
        }
        if (!CollectionUtils.isEmpty(removeList)) {
            fileList.removeAll(removeList);
        }
        //处理文件权限
        fileList.forEach(a -> {
            EcmDocrightDefDTO docRight = a.getDocRight();
            String docCode = a.getDocCode();
            setRight(map, docCode, docRight);
        });
    }

    //判断文件是否需要过滤
    private Boolean filterFiles(String docCode, Map<String, String> map) {
        //根据资料类型Id获取实体
        LambdaQueryWrapper<EcmDocDef> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmDocDef::getDocCode, docCode);
        EcmDocDef ecmDocDef = ecmDocDefMapper.selectOne(queryWrapper);
        //若不匹配指定资料类型ID
        if (!ObjectUtils.isEmpty(ecmDocDef)) {
            if (!map.containsKey(docCode)) {
                //若不为叶子节点
                if (!Objects.equals(ecmDocDef.getParent(), IcmsConstants.DOC_LEVEL_FIRST)) {
                    //递归获取到根节点，且不匹配指定资料类型ID
                    if (filterFiles(ecmDocDef.getParent(), map)) {
                        return true;
                    }
                    //递归获取上一级
                    filterFiles(ecmDocDef.getParent(), map);
                }
                //若不匹配指定资料类型ID，且为根节点，移除
                return Objects.equals(ecmDocDef.getParent(), IcmsConstants.DOC_LEVEL_FIRST);
            }
        }
        //匹配则不用移除
        return false;
    }

    //过滤业务资料树
    private void filterVTree(com.sunyard.ecm.dto.ecm.EcmBusiStructureTreeDTO t,
                             Map<String, String> map) {
        //传入业务资料树的批次节点，获取该批次中树的子节点
        List<EcmBusiStructureTreeDTO> children = t.getChildren();
        if (children == null) {
            return;
        }
        //遍历子节点
        for (int i = 0; i < children.size(); i++) {
            //判断资料类型ID是否为空，确定是否需要递归查询业务类型子节点
            if (children.get(i).getDocCode() == null) {
                //传入业务资料树子节点递归查询
                filterVTree(children.get(i), map);
            } else {
                //资料类型ID不为空，说明为资料类型节点
                //叶子节点判断
                if (Objects.equals(children.get(i).getNodeType(), IcmsConstants.ZERO)) {
                    //若不为叶子节点，不对应指定的资料类型则递归获取资料类型子节点
                    if (!map.containsKey(children.get(i).getDocCode())) {
                        filterVTree(children.get(i), map);
                    }
                    //若不为叶子节点，且该资料类型节点下子节点长度为0，则移除该资料类型节点
                    if (children.get(i).getChildren().size() == IcmsConstants.ZERO) {
                        children.remove(i);
                        i--;
                    } else {
                        //若为不为叶子节点，且对应指定资料类型，则为该类型下所有叶子节点设置权限
                        setNodeRight(children, i, map);
                    }
                } else {
                    EcmBusiStructureTreeDTO dto = children.get(i);
                    Long docId = Long.parseLong(dto.getId());
                    LambdaQueryWrapper<EcmBusiDoc> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(EcmBusiDoc::getDocId, docId);
                    //                    EcmBusiDoc ecmBusiDoc = ecmBusiDocMapper.selectOne(wrapper);
                    //                    String docCode = ecmBusiDoc.getDocCode();
                    //                    LambdaQueryWrapper<EcmDocDef> queryWrapper = new LambdaQueryWrapper<>();
                    //                    queryWrapper.eq(EcmDocDef::getDocCode, docCode);
                    //                    EcmDocDef ecmDocDef = ecmDocDefMapper.selectOne(queryWrapper);
                    //                    dto.setDocCode(ecmDocDef.getDocCode());
                    //若为叶子节点，不对应指定资料类型则移除该叶子节点
                    if (!map.containsKey(dto.getDocCode())) {
                        children.remove(children.get(i));
                        i--;
                    } else {
                        //若为叶子节点，且对应指定资料类型，则设置资料权限
                        String docCode1 = dto.getDocCode();
                        //获取原资料权限
                        EcmDocrightDefDTO docRight = dto.getDocRight();
                        setRight(map, docCode1, docRight);
                    }
                }
            }
        }
    }

    //获取指定资料类型的叶子节点且设置权限
    private void setNodeRight(List<com.sunyard.ecm.dto.ecm.EcmBusiStructureTreeDTO> children, int i,
                              Map<String, String> map) {
        com.sunyard.ecm.dto.ecm.EcmBusiStructureTreeDTO treeDTO = children.get(i);
        List<com.sunyard.ecm.dto.ecm.EcmBusiStructureTreeDTO> nodes = treeDTO.getChildren();
        if (nodes != null) {
            for (int j = 0; j < nodes.size(); j++) {
                if (nodes.get(j).getChildren() != null) {
                    setNodeRight(nodes.get(j).getChildren(), j, map);
                } else {
                    String docCode = nodes.get(j).getDocCode();
                    //获取原资料权限
                    EcmDocrightDefDTO docRight = nodes.get(j).getDocRight();
                    setRight(map, docCode, docRight);
                }
            }
        }
    }

    //根据动态树节点设置权限
    private void setRight(Map<String, String> map, String docCode, EcmDocrightDefDTO docRight) {
        //获取动态树中资料权限
        String right = map.get(docCode);
        //为空则说明动态树指定的资料节点不为叶子节点
        if (right == null) {
            //获取指定资料节点下的所有叶子节点，判断传入的资料类型节点（docCode）是否匹配
            Set<String> ids = map.keySet();
            for (String id : ids) {
                List<String> docCodes = new ArrayList<>();
                List<EcmDocDef> ecmDocDefs = getDocDefs(id);
                setDocTypeIds(docCodes, ecmDocDefs);
                if (docCodes.contains(docCode)) {
                    right = map.get(id);
                }
            }
        }
        docRight.setAddRight("0");
        docRight.setReadRight("0");
        docRight.setUpdateRight("0");
        docRight.setDeleteRight("0");
        docRight.setThumRight("0");
        docRight.setDownloadRight("0");
        docRight.setPrintRight("0");
        docRight.setOtherUpdate("0");
        assert right != null;
        char[] chars = right.toUpperCase().toCharArray();
        for (char c : chars) {
            if ('C' == c) {
                docRight.setAddRight("1");
            } else if ('R' == c) {
                docRight.setReadRight("1");
            } else if ('U' == c) {
                docRight.setUpdateRight("1");
            } else if ('D' == c) {
                docRight.setDeleteRight("1");
            } else if ('T' == c) {
                docRight.setThumRight("1");
            } else if ('S' == c) {
                docRight.setDownloadRight("1");
            } else if ('P' == c) {
                docRight.setPrintRight("1");
            } else if ('O' == c) {
                docRight.setOtherUpdate("1");
            }
        }
    }

    //获取资料类型节点下的所有叶子节点ID
    private void setDocTypeIds(List<String> docCodes, List<EcmDocDef> ecmDocDefs) {
        ecmDocDefs.forEach(e -> {
            List<EcmDocDef> docDefs = getDocDefs(e.getDocCode());
            if (docDefs.size() == IcmsConstants.ZERO) {
                docCodes.add(e.getDocCode());
                docCodes.add(e.getParent());
            } else {
                setDocTypeIds(docCodes, docDefs);
            }
        });
    }

    //获取父ID为k的资料类型
    private List<EcmDocDef> getDocDefs(String k) {
        LambdaQueryWrapper<EcmDocDef> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmDocDef::getParent, k);
        return ecmDocDefMapper.selectList(queryWrapper);
    }

    //设置资料类型叶子节点权限
    private void setDimDocRight(List<com.sunyard.ecm.dto.ecm.EcmBusiStructureTreeDTO> treeDTOList,
                                EcmDocrightDefDTO ecmDocrightDefDTO) {
        treeDTOList.forEach(t -> {
            if (t.getChildren() != null) {
                setDimDocRight(t.getChildren(), ecmDocrightDefDTO);
            } else {
                t.setDocRight(ecmDocrightDefDTO);
            }
        });
    }

    //将file 文件转换成FileItem 文件 便于转换成  MultipartFile 文件
    private static FileItem createFileItem(File file, String fileName) {
        String filePath = file.getPath();
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        String textFieldName = "file";
        int num = filePath.lastIndexOf(".");
        FileItem item = factory.createItem(textFieldName, "multipart/form-data", true, fileName);

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = item.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // 记录异常或抛出特定业务异常
            throw new RuntimeException("创建FileItem失败", e);
        }

        return item;
    }

    //判断文件是否需要压缩
    private void zipMethod(String path, MultipartFile file, EcmSplitFileDTO ecmSplitFileDTO,
                           String fileName, String appCode)
            throws IOException {
        //判断是否需要压缩
        SysStrategyDTO sysStrategyDTO = sysStrategyService.queryConfig();
        if (sysStrategyDTO.getZipStatus()) {
            Integer zipBound = sysStrategyDTO.getZipBound();
            long size = ((long) zipBound * zipBound * IcmsConstants.THREE);
            if (file.getSize() > size) {
                Integer zipScale = sysStrategyDTO.getZipScale();
                double zipQuality = zipScale / IcmsConstants.HUNDRED.doubleValue();
                StringBuilder stringBuilder = new StringBuilder(fileName);
                int i = stringBuilder.indexOf(".");
                stringBuilder.insert(i, "_".concat(zipScale.toString()).concat("%"));
                String zipName = stringBuilder.toString();
                File zipFile = new File(path, zipName);
                Thumbnails.of(file.getInputStream()).scale(IcmsConstants.ONE.floatValue())
                        .outputQuality(zipQuality / 2.4D).toFile(zipFile);
                FileItem fileItem = createFileItem(zipFile, zipName);
                MultipartFile zipMulFile = new CommonsMultipartFile(fileItem);
                byte[] zipBytes = IoUtil.readBytes(zipMulFile.getInputStream());
                String zipMd5 = Md5Utils.calculateMD5(zipBytes);
                ecmSplitFileDTO.setFileName(zipName);
                ecmSplitFileDTO.setIdentifier(zipMd5);
                ecmSplitFileDTO.setTotalSize(zipMulFile.getSize());
            }
        } else {
            EcmAppDef ecmAppDef = ecmAppDefMapper.selectById(appCode);
            Integer resiz = ecmAppDef.getResize();
            Float quality = ecmAppDef.getQulity();
            if (resiz != null && quality != null) {
                long size = ((long) resiz * resiz * IcmsConstants.THREE);
                if (file.getSize() > size) {
                    double zipQuality = quality / IcmsConstants.HUNDRED.doubleValue();
                    StringBuilder stringBuilder = new StringBuilder(fileName);
                    int i = stringBuilder.indexOf(".");
                    stringBuilder.insert(i, "_".concat(quality.toString()).concat("%"));
                    String zipName = stringBuilder.toString();
                    File zipFile = new File(path, zipName);
                    Thumbnails.of(file.getInputStream()).scale(IcmsConstants.ONE.floatValue())
                            .outputQuality(zipQuality / 2.4D).toFile(zipFile);
                    FileItem fileItem = createFileItem(zipFile, zipName);
                    MultipartFile zipMulFile = new CommonsMultipartFile(fileItem);
                    byte[] zipBytes = IoUtil.readBytes(zipMulFile.getInputStream());
                    String zipMd5 = Md5Utils.calculateMD5(zipBytes);
                    ecmSplitFileDTO.setFileName(zipName);
                    ecmSplitFileDTO.setIdentifier(zipMd5);
                    ecmSplitFileDTO.setTotalSize(zipMulFile.getSize());
                }
            }
        }
    }

    //设置设备ID
    private void setEquipment(EcmSplitFileDTO ecmSplitFileDTO, String appCode) {
        PageInfo<EcmBusiStorageListVO> pageInfo = sysStorageService
                .getBusiStorageList(new PageForm());
        long total = pageInfo.getTotal();
        PageInfo<EcmBusiStorageListVO> busiStorageList = sysStorageService
                .getBusiStorageList(new PageForm(IcmsConstants.ONE, Math.toIntExact(total)));
        List<EcmBusiStorageListVO> storageLists = busiStorageList.getList();
        storageLists.forEach(s -> {
            if (s.getAppCode().equals(appCode)) {
                ecmSplitFileDTO.setEquipmentId(s.getEquipmentId());
            }
        });
    }

    /**
     * 组装动态树
     */
    private List<EcmDocTreeDTO> getDocTreeDTO(Map<String, List<EcmBusiDoc>> collect,
                                              String parentName, String parentId,
                                              List<EcmDocTreeDTO> list, List<String> parentIds) {
        List<EcmDocTreeDTO> ecmDocTreeDTOS = new ArrayList<>();
        //遍历map，
        collect.forEach((k, v) -> {
            final Integer[] j = { 0 };
            //判断传入的父ID是否与已有节点的父ID相同
            if (parentId.equals(k)) {
                //for循环创建资料树节点
                for (EcmBusiDoc e : v) {
                    EcmDocTreeDTO ecmDocTreeDTO = new EcmDocTreeDTO();
                    ecmDocTreeDTO.setDocCode(e.getDocCode());
                    ecmDocTreeDTO.setDocCode(e.getDocCode());
                    ecmDocTreeDTO.setId(e.getDocCode());
                    ecmDocTreeDTO.setLabel(e.getDocName());
                    ecmDocTreeDTO.setDocName(e.getDocName());
                    ecmDocTreeDTO.setDocSort(e.getDocSort());
                    ecmDocTreeDTO.setParent(parentId);
                    ecmDocTreeDTO.setParentName(parentName);
                    //查询该子节点的子节点列表
                    final Integer[] i = { 0 };
                    collect.forEach((k1, v1) -> {
                        //判断其他元素是否有父ID与当前元素的资料ID相同，说明当前元素为其父节点
                        if (k1.equals(e.getDocCode())) {
                            //递归获取子节点
                            List<EcmDocTreeDTO> docTreeDTOS = getDocTreeDTO(collect, e.getDocName(),
                                    e.getDocCode(), list, parentIds);
                            ecmDocTreeDTO.setChildren(docTreeDTOS);
                            ecmDocTreeDTO.setType(RoleConstants.ONE);
                            i[0] = 1;
                        }
                    });
                    //若成立，说明无父节点，当前元素为叶子节点
                    if (i[0] == 0) {
                        if (!parentIds.contains(e.getDocCode())) {
                            //结束本次循环
                            j[0] = 1;
                            ecmDocTreeDTO.setType(RoleConstants.ZERO);
                            ecmDocTreeDTOS.add(ecmDocTreeDTO);
                        }
                    }
                    if (!ObjectUtils.isEmpty(ecmDocTreeDTO.getChildren())) {
                        //list为完整树结构，作为结果值返回使用
                        list.add(ecmDocTreeDTO);
                        //ecmDocTreeDTOS为树结构其中一条完整分支，用于组装为完整树结构，作为过渡值返回使用
                        ecmDocTreeDTOS.add(ecmDocTreeDTO);
                        j[0] = 0;
                    }
                }
            }
        });
        return ecmDocTreeDTOS;
    }

    private void getChildBusiDoc(String docCode, List<EcmBusiDoc> ecmBusiDocs, Long busiId) {
        LambdaQueryWrapper<EcmBusiDoc> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmBusiDoc::getParentId, docCode).eq(EcmBusiDoc::getBusiId, busiId);
        List<EcmBusiDoc> ecmBusiChildDocs = ecmBusiDocMapper.selectList(queryWrapper);
        if (!Objects.isNull(ecmBusiChildDocs)) {
            ecmBusiDocs.addAll(ecmBusiChildDocs);
            ecmBusiChildDocs.forEach(e -> {
                getChildBusiDoc(e.getDocCode(), ecmBusiDocs, busiId);
            });
        }
    }

    /**
     * 平铺动态树结构
     */
    public List<EcmVTreeDataDTO> flattenTree(List<EcmVTreeDataDTO> list) {
        List<EcmVTreeDataDTO> flatList = new ArrayList<>();
        for (EcmVTreeDataDTO node : list) {
            flatList.add(node);
            if (CollectionUtil.isNotEmpty(node.getEcmVTreeDataDTOS())) {
                flatList.addAll(flattenTree(node.getEcmVTreeDataDTOS()));
            }
        }
        return flatList;
    }

    /**
     * 校验重复文件
     */
    private List<EcmFileInfo> checkRepeatFile(Long busiId, String docCode) {
        //根据busiId和docNo获取该类型已经上传过的文件
        LambdaQueryWrapper<EcmFileInfo> fileWrapper = new LambdaQueryWrapper<>();
        fileWrapper.eq(EcmFileInfo::getBusiId, busiId);
        fileWrapper.eq(EcmFileInfo::getDocCode, docCode);
        List<EcmFileInfo> ecmFileInfos = ecmFileInfoMapper.selectList(fileWrapper);
        return ecmFileInfos;
    }

    /**
     * 影像复制
     */
    public Result busiDocDuplicate(BusiDocDuplicateVO busiDocDuplicateVo) {
        AssertUtils.isNull(busiDocDuplicateVo.getEcmBusExtendDTO(), "源业务不能为空");
        AssertUtils.isNull(busiDocDuplicateVo.getEcmBusExtendDTO().getEcmBusExtendDTOS(),
                "源业务不能为空");
        AssertUtils.isNull(
                busiDocDuplicateVo.getEcmBusExtendDTO().getEcmBusExtendDTOS().getBusiNo(),
                "源业务编号不能为空");
        AssertUtils.isNull(
                busiDocDuplicateVo.getEcmBusExtendDTO().getEcmBusExtendDTOS().getAppCode(),
                "源业务类型不能为空");

        AssertUtils.isNull(busiDocDuplicateVo.getBusiDocDuplicateVos(), "目标业务不能为空");

        //校验源是否存在，不存在则报错
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper
                .selectOne(new LambdaQueryWrapper<EcmBusiInfo>()
                        .eq(EcmBusiInfo::getBusiNo,
                                busiDocDuplicateVo.getEcmBusExtendDTO().getEcmBusExtendDTOS()
                                        .getBusiNo())
                        .eq(EcmBusiInfo::getAppCode, busiDocDuplicateVo.getEcmBusExtendDTO()
                                .getEcmBusExtendDTOS().getAppCode()));

        AssertUtils.isNull(ecmBusiInfo, "源业务不存在");

        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        token.setUsername(
                busiDocDuplicateVo.getEcmBusExtendDTO().getEcmBaseInfoDTO().getUserCode());
        token.setName(busiDocDuplicateVo.getEcmBusExtendDTO().getEcmBaseInfoDTO().getUserName());
        token.setOrgName(busiDocDuplicateVo.getEcmBusExtendDTO().getEcmBaseInfoDTO().getOrgName());
        token.setOrgCode(busiDocDuplicateVo.getEcmBusExtendDTO().getEcmBaseInfoDTO().getOrgCode());
        token.setRoleCodeList(
                busiDocDuplicateVo.getEcmBusExtendDTO().getEcmBaseInfoDTO().getRoleCode());
        token.setOut(true);
        //校验用户信息
        token = busiCacheService
                .checkUser(busiDocDuplicateVo.getEcmBusExtendDTO().getEcmBaseInfoDTO(), token);
        EcmBaseInfoDTO ecmBaseInfoDTO = busiDocDuplicateVo.getEcmBusExtendDTO().getEcmBaseInfoDTO();
        ecmBaseInfoDTO.setUserName(token.getName());
        ecmBaseInfoDTO.setOrgName(token.getOrgName());
        busiDocDuplicateVo.getEcmBusExtendDTO().setEcmBaseInfoDTO(ecmBaseInfoDTO);

        //校验业务是否存在（目标），不存在则创建
        List<BusiDocDuplicateTarVO> busiDocDuplicateVos = busiDocDuplicateVo
                .getBusiDocDuplicateVos();
        //校验基本信息
        ArrayList<EcmBusExtendDTO> arrayList = new ArrayList();
        for (BusiDocDuplicateTarVO vo : busiDocDuplicateVos) {
            arrayList.add(vo.getEcmBusExtendDTO().getEcmBusExtendDTOS());
        }
        //校验业务
        handleCheckBusi(arrayList, true);
        EcmRootDataDTO ecmRootDataDTO = new EcmRootDataDTO();
        ecmRootDataDTO
                .setEcmBaseInfoDTO(busiDocDuplicateVo.getEcmBusExtendDTO().getEcmBaseInfoDTO());
        ecmRootDataDTO.setEcmBusExtendDTOS(arrayList);
        //校验动态树业务权限
        checkBusiness(ecmRootDataDTO, null);

        //新增业务
        List<EcmBusExtendDTO> addExtendDtos = new ArrayList<>();
        List<EcmBusiInfo> updates = new ArrayList<>();
        for (EcmBusExtendDTO dto : arrayList) {
            EcmBusiInfo ecmBusiInfo1 = ecmBusiInfoMapper
                    .selectOne(new LambdaQueryWrapper<EcmBusiInfo>()
                            .eq(EcmBusiInfo::getBusiNo, dto.getBusiNo())
                            .eq(EcmBusiInfo::getAppCode, dto.getAppCode()));
            if (ecmBusiInfo1 == null) {
                addExtendDtos.add(dto);
            } else {
                updates.add(ecmBusiInfo1);
            }
        }

        List<Long> list = new ArrayList<>();
        Map<Long, List<EcmDocrightDefDTO>> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(addExtendDtos)) {
            ecmRootDataDTO.setEcmBusExtendDTOS(addExtendDtos);
            //新增业务
            List<EcmBusiInfo> ecmBusiInfos = addBusinessInfo(ecmRootDataDTO, token, map, true);
            list = ecmBusiInfos.stream().map(EcmBusiInfo::getBusiId).collect(Collectors.toList());
        }

        List<Long> collect2 = updates.stream().map(EcmBusiInfo::getBusiId)
                .collect(Collectors.toList());
        list.addAll(collect2);
        //源
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService
                .getFileInfoRedis(ecmBusiInfo.getBusiId());
        fileInfoRedis = fileInfoRedis.stream().filter(s -> StateConstants.NO.equals(s.getState()))
                .collect(Collectors.toList());

        List<String> tarMary = new ArrayList<>();
        //静态树方式
        List<String> ecmDocCodes = busiDocDuplicateVo.getEcmBusExtendDTO().getEcmBusExtendDTOS()
                .getEcmDocCodes();

        //指定复制目录
        if (CollectionUtil.isNotEmpty(ecmDocCodes)) {
            tarMary = ecmDocCodes;
            fileInfoRedis = fileInfoRedis.stream().filter(s -> ecmDocCodes.contains(s.getDocCode()))
                    .collect(Collectors.toList());
        }
        if (CollectionUtil.isNotEmpty(fileInfoRedis) && CollectionUtil.isNotEmpty(list)) {
            Map mapRet = getMuliCopyFileResult(ecmBusiInfo, token, arrayList, list, fileInfoRedis, tarMary);
            if (mapRet.keySet().size() > 0) {
                //对外接口成功后发送mq
                if (CollectionUtil.isNotEmpty(fileInfoRedis)){
                    for (FileInfoRedisDTO fileInfoRedisDTO : fileInfoRedis) {
                        Long busiId = fileInfoRedisDTO.getBusiId();
                        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,busiId);
                        captureSubmitService.sendMQMessage(ecmBusiInfoRedisDTO);
                    }
                }
                return Result.success(mapRet);
            }
        }
        //动态树方式
        return Result.success(true);
    }


    /**
     * 对外接口复用公共类
     * @param ecmBusiInfo
     * @param token
     * @param arrayList
     * @param list
     * @param fileInfoRedis
     * @param tarMary
     * @return
     */
    private Map getMuliCopyFileResult(EcmBusiInfo ecmBusiInfo, AccountTokenExtendDTO token, ArrayList<EcmBusExtendDTO> arrayList, List<Long> list, List<FileInfoRedisDTO> fileInfoRedis, List<String> tarMary) {
        Map<String, Map<String, List<EcmBusExtendDTO>>> collect5 = arrayList.stream()
                .collect(Collectors.groupingBy(EcmBusExtendDTO::getAppCode,
                        Collectors.groupingBy(EcmBusExtendDTO::getBusiNo)));
        Map<String, List<FileInfoRedisDTO>> collect1 = fileInfoRedis.stream()
                .collect(Collectors.groupingBy(FileInfoRedisDTO::getDocCode));
        //当存在需要复用的文件时，走复用逻辑
        Map mapRet = new HashMap();
        for (Long tarBusi : list) {
            List<Long> objects = new ArrayList<>();
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                    .getEcmBusiInfoRedisDTO(token, tarBusi);
            List<EcmBusExtendDTO> ecmBusExtendDTOS = collect5
                    .get(ecmBusiInfoRedisDTO.getAppCode()).get(ecmBusiInfoRedisDTO.getBusiNo());
            if (CollectionUtil.isEmpty(ecmBusExtendDTOS)) {
                continue;
            }
            EcmBusExtendDTO ecmBusExtendDTO = ecmBusExtendDTOS.get(0);
            if (CollectionUtil.isNotEmpty(ecmBusExtendDTO.getEcmDocCodes())
                    && ecmBusExtendDTO.getEcmDocCodes().size() == 1) {
                //如果目标节点已经指定的话，则将所有文件归入改节点下，即将源的文件doc改为改节点
                for (FileInfoRedisDTO fileInfoRedisDTO : fileInfoRedis) {
                    fileInfoRedisDTO.setDocCode(ecmBusExtendDTO.getEcmDocCodes().get(0));
                }
                //重新分組
                collect1 = fileInfoRedis.stream()
                        .collect(Collectors.groupingBy(FileInfoRedisDTO::getDocCode));
            }

            List<FileInfoRedisDTO> fileInfoRedis1 = busiCacheService.getFileInfoRedis(tarBusi);
            fileInfoRedis1 = fileInfoRedis1.stream()
                    .filter(s -> StateConstants.NO.equals(s.getState()))
                    .collect(Collectors.toList());

            Map<String, List<FileInfoRedisDTO>> collect = fileInfoRedis1.stream()
                    .collect(Collectors.groupingBy(FileInfoRedisDTO::getDocCode));
            List<String> tglist = new ArrayList<>();

            for (String s : collect.keySet()) {
                //目标
                List<FileInfoRedisDTO> fileInfoRedisDTOS = collect.get(s);
                //源
                List<FileInfoRedisDTO> fileInfoRedisDTOS1 = collect1.get(s);
                if (CollectionUtil.isNotEmpty(fileInfoRedisDTOS)
                        && CollectionUtil.isNotEmpty(fileInfoRedisDTOS1)) {
                    List<String> collect3 = fileInfoRedisDTOS.stream()
                            .map(FileInfoRedisDTO::getFileMd5).collect(Collectors.toList());
                    List<String> collect4 = fileInfoRedisDTOS1.stream()
                            .map(FileInfoRedisDTO::getFileMd5).collect(Collectors.toList());
                    collect3.retainAll(collect4);
                    tglist.addAll(collect3);
                }
            }

            if (CollectionUtil.isNotEmpty(tglist)) {
                //需要跳过的文件（同名文件，无需复用）
                List<String> finalTglist = tglist;
                fileInfoRedis = fileInfoRedis.stream()
                        .filter(s -> !finalTglist.contains(s.getFileMd5()))
                        .collect(Collectors.toList());
                if (CollectionUtil.isEmpty(fileInfoRedis)) {
                    //动态树方式
                    continue;
                }
            }
            List<String> targe = new ArrayList<>();
            getDocCodeTree(ecmBusiInfoRedisDTO.getEcmBusiDocRedisDTOS(), targe);
            FileInfoRedisEntityVO vo = new FileInfoRedisEntityVO();
            vo.setSourceBusiId(tarBusi);
            vo.setSourceAppCode(ecmBusiInfoRedisDTO.getAppCode());
            vo.setSourceBusiNo(ecmBusiInfoRedisDTO.getBusiNo());
            //对于对外接口来说，这个时源
            List<MultiplexFileVO> source = new ArrayList<>();
            MultiplexFileVO multiplexFileVO = new MultiplexFileVO();
            multiplexFileVO.setTargetDocId(tarMary);
            multiplexFileVO.setTargetBusiId(ecmBusiInfo.getBusiId());
            multiplexFileVO.setTargetDocTypeId(tarMary);
            source.add(multiplexFileVO);
            vo.setMultiplexFileVO(source);
            List<FileInfoRedisDTO> collect3 = fileInfoRedis1.stream()
                    .filter(s -> IcmsConstants.UNCLASSIFIED_ID.equals(s.getDocCode()))
                    .collect(Collectors.toList());
            List<FileInfoRedisDTO> objects1 = new ArrayList<>();

            if (CollectionUtil.isNotEmpty(collect3)) {
                List<String> collect4 = collect3.stream().map(FileInfoRedisDTO::getFileMd5)
                        .collect(Collectors.toList());

                for (FileInfoRedisDTO fileInfoRedisDTO : fileInfoRedis) {
                    //得到被复用文件的资料代码code
                    String docCode = fileInfoRedisDTO.getDocCode();
                    //根据资料类型id得docId:知道了docId就知道了该文件要复用到那个资料节点上
                    AssertUtils.isNull(docCode, "参数错误");
                    if (!targe.contains(docCode)) {
                        //不一致则放入未归类，判断未归类是否存在相同文件
                        if (!collect4.contains(fileInfoRedisDTO.getFileMd5())) {
                            objects1.add(fileInfoRedisDTO);
                        }
                    } else {
                        objects1.add(fileInfoRedisDTO);
                    }
                }
            } else {
                objects1.addAll(fileInfoRedis);
            }
            if (CollectionUtil.isEmpty(objects1)) {
                continue;
            }

            List<EcmFileInfoDTO> ecmFileInfoDTOS = operateCaptureService.muliCopyFileSave(vo,
                    token, objects1, targe, objects);
            mapRet.put(tarBusi, ecmFileInfoDTOS.size());
        }
        return mapRet;
    }

    private static void getDocCodeTree(List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS,
                                       List<String> tarMary) {
        for (EcmBusiDocRedisDTO ecmBusiDocRedisDTO : ecmBusiDocRedisDTOS) {
            if (CollectionUtils.isEmpty(ecmBusiDocRedisDTO.getChildren())) {
                tarMary.add(ecmBusiDocRedisDTO.getDocCode());
            } else {
                getDocCodeTree(ecmBusiDocRedisDTO.getChildren(), tarMary);
            }
        }
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFileByBusiId(EcmDelVO vo, AccountTokenExtendDTO token) {
        FileInfoVO vo1 = new FileInfoVO();
        fileInfoService.deleteFileInfo(vo1, token);
    }

    /**
     * 业务属性回写
     */
    public Result setBusiAttr(EditBusiAttrDTO editBusiAttrDTO) {
        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        token.setUsername(editBusiAttrDTO.getEcmBaseInfoDTO().getUserCode());
        token.setName(editBusiAttrDTO.getEcmBaseInfoDTO().getUserName());
        token.setOut(true);
        token = busiCacheService.checkUser(editBusiAttrDTO.getEcmBaseInfoDTO(), token);
        EcmUserDTO ecmBaseInfoDTO = editBusiAttrDTO.getEcmBaseInfoDTO();
        ecmBaseInfoDTO.setUserName(token.getName());
        ecmBaseInfoDTO.setOrgName(token.getOrgName());
        editBusiAttrDTO.setEcmBaseInfoDTO(ecmBaseInfoDTO);
        String busiNo = editBusiAttrDTO.getBusiNo();
        //获取业务ID，业务类型ID
        LambdaQueryWrapper<EcmBusiInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmBusiInfo::getAppCode, editBusiAttrDTO.getAppCode());
        queryWrapper.eq(EcmBusiInfo::getBusiNo, busiNo);
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectOne(queryWrapper);
        AssertUtils.isNull(ecmBusiInfo, "当前业务不存在");
        /*        EcmAppAttr ecmAppAttr1 = ecmAppAttrMapper.selectOne(new LambdaQueryWrapper<EcmAppAttr>()
                .eq(EcmAppAttr::getIsKey, StateConstants.YES)
                .eq(EcmAppAttr::getAppCode, editBusiAttrDTO.getAppCode()));
        
            String appValue = null;
        for (EcmBusiAttrDTO s : editBusiAttrDTO.getEcmBusiAttrDTOList()) {
            if (ecmAppAttr1.getAttrCode().equals(s.getAttrCode())) {
                appValue = s.getAppAttrValue();
                break;
            }
        }
        if (!ObjectUtils.isEmpty(appValue)) {
            EcmBusiMetadata ecmBusiMetadata = ecmBusiMetadataMapper.selectOne(new LambdaQueryWrapper<EcmBusiMetadata>()
                    .eq(EcmBusiMetadata::getAppAttrVal, appValue)
                    .eq(EcmBusiMetadata::getAppAttrId, ecmAppAttr1.getAppAttrId())
                    .eq(EcmBusiMetadata::getBusiId, ecmBusiInfo.getBusiId()));
            AssertUtils.notNull(ecmBusiMetadata, "当前业务主索引已存在");
        }*/

        Long busiId = ecmBusiInfo.getBusiId();
        String appCode = ecmBusiInfo.getAppCode();
        List<EcmBusiAttrDTO> ecmBusiAttrDTOList = editBusiAttrDTO.getEcmBusiAttrDTOList();
        List<String> attrCodes = ecmBusiAttrDTOList.stream().map(EcmBusiAttrDTO::getAttrCode)
                .collect(Collectors.toList());
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(
                new QueryWrapper<EcmAppAttr>().in("attr_code", attrCodes).eq("app_code", appCode));
        List<EcmAppAttrDTO> ecmAppAttrDTOS = new ArrayList<>();
        String newBusiNo = busiNo;
        //设置业务属性ID
        for (EcmBusiAttrDTO e : ecmBusiAttrDTOList) {
            for (EcmAppAttr ecmAppAttr : ecmAppAttrs) {
                if (ecmAppAttr.getAttrCode().equals(e.getAttrCode())) {
                    switch (ecmAppAttr.getInputType()) {
                        case 2:
                            //日期
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            dateFormat.setLenient(false);
                            try {
                                dateFormat.parse(e.getAppAttrValue());
                            } catch (Exception exception) {
                                AssertUtils.isNull(null, "对不起,时间框值格式有误!");
                            }
                            break;
                        case 3:
                            //选择框
                            String listValue = ecmAppAttr.getListValue();
                            String[] split = listValue.split(";");
                            if (!ObjectUtils.isEmpty(split)) {
                                boolean contains = Arrays.asList(split)
                                        .contains(e.getAppAttrValue());
                                AssertUtils.isTrue(!contains, "对不起,选择框值有误!");
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                }
            }
            AssertUtils.isNull(e.getAppAttrValue(), "业务属性值不可为空");
            AssertUtils.isTrue(e.getAppAttrValue().length() > 128, "对不起,业务属性值过长!");
            AssertUtils.isNull(e.getAttrCode(), "业务属性代码不可为空");
            String attrCode = e.getAttrCode();
            LambdaQueryWrapper<EcmAppAttr> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EcmAppAttr::getAttrCode, attrCode).eq(EcmAppAttr::getAppCode, appCode);
            EcmAppAttr ecmAppAttr = ecmAppAttrMapper.selectOne(wrapper);
            AssertUtils.isTrue(ObjectUtils.isEmpty(ecmAppAttr), "对不起,业务属性代码有误!");
            e.setAppAttrId(ecmAppAttr.getAppAttrId());
            EcmAppAttrDTO ecmAppAttrDTO = new EcmAppAttrDTO();
            BeanUtils.copyProperties(e, ecmAppAttrDTO);
            ecmAppAttrDTOS.add(ecmAppAttrDTO);
            if (IcmsConstants.ONE.equals(ecmAppAttr.getIsKey())) {
                newBusiNo = e.getAppAttrValue();
                List<EcmBusiInfo> ecmBusiInfos = ecmBusiInfoMapper
                        .selectList(new QueryWrapper<EcmBusiInfo>().ne("busi_id", busiId)
                                .eq("app_code", appCode).eq("busi_no", e.getAppAttrValue()));
                AssertUtils.isTrue(!CollectionUtils.isEmpty(ecmBusiInfos), "对不起,当前业务类型主索引已存在!");
            }
        }
        EcmBusiInfoRedisDTO ecmBusiInfoExtend = new EcmBusiInfoRedisDTO();
        ecmBusiInfoExtend.setAttrList(ecmAppAttrDTOS);
        ecmBusiInfoExtend.setBusiId(busiId);
        ecmBusiInfoExtend.setBusiNo(newBusiNo);
        ecmBusiInfoExtend.setCreateUser(token.getUsername());
        ecmBusiInfoExtend.setUpdateUserName(token.getName());
        ecmBusiInfoExtend.setUpdateUser(token.getUsername());
        //校验参数
        checkParams(editBusiAttrDTO);
        //更新数据库
        Date updateTime=new Date();
        updateBusiInfoToDb(ecmBusiInfoExtend, token, updateTime);
        //更新redis缓存
        updateBusiInfoToRedis(ecmBusiInfoExtend, token,updateTime);
        //更新ES
        operateFullQueryService.editEsBusiInfo(ecmBusiInfoExtend, token.getUsername(), updateTime);
        return Result.success(true);
    }

    /**
     * 业务状态修改
     */
    public Result busiDeblock(EditBusiAttrDTO editBusiAttrDTO) {
        AssertUtils.isNull(editBusiAttrDTO.getBusiNo(), "参数错误");
        AssertUtils.isNull(editBusiAttrDTO.getAppCode(), "参数错误");
        AssertUtils.isNull(editBusiAttrDTO.getStatus(), "参数错误");
        AssertUtils.isNull(editBusiAttrDTO.getEcmBaseInfoDTO(), "参数错误");
        String busiNo = editBusiAttrDTO.getBusiNo();
        String appCode = editBusiAttrDTO.getAppCode();
        //获取业务ID，业务类型ID
        LambdaQueryWrapper<EcmBusiInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmBusiInfo::getAppCode, appCode);
        queryWrapper.eq(EcmBusiInfo::getBusiNo, busiNo);
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectOne(queryWrapper);
        AssertUtils.isNull(ecmBusiInfo, "当前业务不存在");
        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        token.setUsername(editBusiAttrDTO.getEcmBaseInfoDTO().getUserCode());
        token.setName(editBusiAttrDTO.getEcmBaseInfoDTO().getUserName());
        token.setOut(true);
        token = busiCacheService.checkUser(editBusiAttrDTO.getEcmBaseInfoDTO(), token);
        //校验状态
        Integer status = editBusiAttrDTO.getStatus();
        checkStatus(status, ecmBusiInfo);
        ecmBusiInfo.setStatus(status);
        if(!ObjectUtils.isEmpty(editBusiAttrDTO.getErrNo())){
            AssertUtils.isTrue(!editBusiAttrDTO.getErrNo().equals(IcmsConstants.ZERO) &&
                                    !editBusiAttrDTO.getErrNo().equals(IcmsConstants.ONE) , "errNo值不合法");
            ecmBusiInfo.setErrNo(editBusiAttrDTO.getErrNo());
            // 校验长度
            String remark = editBusiAttrDTO.getRemark();
            if (!ObjectUtils.isEmpty(remark)) {
                byte[] bytes = remark.getBytes(StandardCharsets.UTF_8);
                remark = bytes.length > 255 ? new String(bytes, 0, 255, StandardCharsets.UTF_8) : remark;
            }
            ecmBusiInfo.setRemark(remark);
        }
        //更新db
        ecmBusiInfoMapper.updateById(ecmBusiInfo);
        //添加操作记录表
        busiOperationService.addOperation(ecmBusiInfo.getBusiId(), IcmsConstants.EDIT_BUSI,
                token, "编辑业务信息");
        //更新redis
        busiCacheService.updateRedisBusiStatus(ecmBusiInfo, token, status);
        return Result.success(true);
    }

    private void checkStatus(Integer status, EcmBusiInfo ecmBusiInfo) {
        //1、若状态为：待提交 或 处理失败------可修改为》已提交；
        //2、若状态为：已提交-------可修改为》已受理 或 已办结 或 已作废；
        //3、若状态为：已受理-------可修改为》已办结 或 已作废；
        // 0待提交 1 已提交  2已受理（处理中）3 已作废 4 处理失败 5 已完结
        Integer busiStatus = ecmBusiInfo.getStatus();
        List<Integer> statusList = new ArrayList<>();
        statusList.add(BusiInfoConstants.BUSI_STATUS_ZERO);
        statusList.add(BusiInfoConstants.BUSI_STATUS_ONE);
        statusList.add(BusiInfoConstants.BUSI_STATUS_TWO);
        statusList.add(BusiInfoConstants.BUSI_STATUS_THREE);
        statusList.add(BusiInfoConstants.BUSI_STATUS_FOUR);
        statusList.add(BusiInfoConstants.BUSI_STATUS_FIVE);
        AssertUtils.isTrue(!statusList.contains(status),"参数不合法");
        if(BusiInfoConstants.BUSI_STATUS_ZERO.equals(busiStatus)||
                BusiInfoConstants.BUSI_STATUS_FOUR.equals(busiStatus)){
            AssertUtils.isTrue(!BusiInfoConstants.BUSI_STATUS_ONE.equals(status),"不可修改为此状态");
        }
        if(BusiInfoConstants.BUSI_STATUS_ONE.equals(busiStatus)){
            AssertUtils.isTrue(!BusiInfoConstants.BUSI_STATUS_TWO.equals(status)&&
                    !BusiInfoConstants.BUSI_STATUS_FIVE.equals(status)&&
                    !BusiInfoConstants.BUSI_STATUS_THREE.equals(status),"不可修改为此状态");
        }
        if(BusiInfoConstants.BUSI_STATUS_TWO.equals(busiStatus)){
            AssertUtils.isTrue(!BusiInfoConstants.BUSI_STATUS_FIVE.equals(status)&&
                    !BusiInfoConstants.BUSI_STATUS_THREE.equals(status),"不可修改为此状态");
        }
    }

    /**
     * 扫描修改业务
     */
    public EcmPageBaseInfoDTO scanOrUpdateEcmPc(AccountTokenExtendDTO token) {
        //批扫
        EcmRootDataDTO ecmRootDataDTO = new EcmRootDataDTO();
        EcmBaseInfoDTO baseInfoDTO = new EcmBaseInfoDTO();
        baseInfoDTO.setUserName(token.getName());
        baseInfoDTO.setUserCode(token.getUsername());
        if (!CollectionUtil.isEmpty(token.getRoleCodeList())) {
            baseInfoDTO.setRoleCode(token.getRoleCodeList());
        }
        baseInfoDTO.setOrgName(token.getOrgName());
        baseInfoDTO.setOrgCode(token.getOrgCode());
        baseInfoDTO.setOneBatch(IcmsConstants.BATCH_SCAN);
        ecmRootDataDTO.setEcmBaseInfoDTO(baseInfoDTO);
        EcmPageBaseInfoDTO ecmPageBaseInfoDTO = businessDataService(ecmRootDataDTO, false);
        return ecmPageBaseInfoDTO;
    }

    /**
     *单扫
     */
    public EcmPageBaseInfoDTO singleCapture(EcmStructureTreeDTO ecmStructureTreeDTO,
                                            AccountTokenExtendDTO token) {
        //单扫
        if (org.apache.commons.collections4.CollectionUtils
                .isEmpty(ecmStructureTreeDTO.getBusiIdList())) {
            return null;
        }
        List<EcmBusiInfo> redisDTOS = operateCaptureService
                .checkPremissByBusi(ecmStructureTreeDTO, token);
        EcmRootDataDTO ecmRootDataDTO = new EcmRootDataDTO();
        EcmBaseInfoDTO baseInfoDTO = new EcmBaseInfoDTO();
        baseInfoDTO.setUserName(token.getName());
        baseInfoDTO.setUserCode(token.getUsername());
        if (!CollectionUtil.isEmpty(token.getRoleCodeList())) {
            baseInfoDTO.setRoleCode(token.getRoleCodeList());
        }
        baseInfoDTO.setOrgName(token.getOrgName());
        baseInfoDTO.setOrgCode(token.getOrgCode());
        baseInfoDTO.setOneBatch(IcmsConstants.ONE.toString());
        ecmRootDataDTO.setEcmBaseInfoDTO(baseInfoDTO);

        ArrayList<EcmBusExtendDTO> arrayList = new ArrayList();
        ArrayList<String> appCode = new ArrayList();
        Map map = new HashMap();
        ArrayList<EcmBusiInfo> busiInfos = new ArrayList();
        for (EcmBusiInfo ecmBusiInfoRedisDTO1 : redisDTOS) {
            EcmBusExtendDTO dto = new EcmBusExtendDTO();
            dto.setAppCode(ecmBusiInfoRedisDTO1.getAppCode());
            dto.setTypeTree(ecmBusiInfoRedisDTO1.getTreeType().toString());
//            List<EcmAppAttrDTO> attrList = ecmBusiInfoRedisDTO1.getAttrList();
            List<EcmAppAttrDTO> attrList = busiCacheService.getAppAttrExtends(ecmBusiInfoRedisDTO1.getAppCode(), ecmBusiInfoRedisDTO1.getBusiId());
            ArrayList<EcmBusiAttrDTO> objects = new ArrayList<>();
            for (EcmAppAttrDTO ecmAppAttrDTO : attrList) {
                EcmBusiAttrDTO dto1 = new EcmBusiAttrDTO();
                dto1.setAttrCode(ecmAppAttrDTO.getAttrCode());
                dto1.setAppAttrValue(ecmAppAttrDTO.getAppAttrValue());
                objects.add(dto1);
            }
            dto.setEcmBusiAttrDTOList(objects);
            arrayList.add(dto);
            appCode.add(ecmBusiInfoRedisDTO1.getAppCode());
            List<EcmDocrightDefDTO> ecmDocrightDefDTOS = null;
            if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfoRedisDTO1.getTreeType())) {
                ecmDocrightDefDTOS = commonService.dealRuleData(dto, token,
                        ecmBusiInfoRedisDTO1.getRightVer());
            } else {
                //动态树
                EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = new EcmBusiInfoRedisDTO();
                ecmBusiInfoRedisDTO.setBusiId(ecmBusiInfoRedisDTO1.getBusiId());
                ecmBusiInfoRedisDTO.setTreeType(ecmBusiInfoRedisDTO1.getTreeType());
                ecmBusiInfoRedisDTO.setAppCode(ecmBusiInfoRedisDTO1.getAppCode());
                busiCacheService.addDocTypeTreeToEcmBusiDoc(ecmBusiInfoRedisDTO);
                ecmDocrightDefDTOS = busiCacheService.vTreeLogic(ecmBusiInfoRedisDTO, token);
            }

            map.put(ecmBusiInfoRedisDTO1.getBusiId(), ecmDocrightDefDTOS);
//            EcmBusiInfo ecmBusiInfo = new EcmBusiInfo();
//            BeanUtils.copyProperties(ecmBusiInfoRedisDTO1, ecmBusiInfo);
            busiInfos.add(ecmBusiInfoRedisDTO1);
        }

        ecmRootDataDTO.setEcmBusExtendDTOS(arrayList);
        EcmPageBaseInfoDTO userBaseInfo = new EcmPageBaseInfoDTO();

        handleSinCapture(ecmRootDataDTO, busiInfos, token, userBaseInfo, appCode, map);
        Result result = returnScanPage(new Result());
        String pageUrl = result.getData().toString();
        try {
            //Pc端
            pageUrl = "/sunIcms/#/imageContentManagement";
        } catch (Exception e) {
            log.error("跳转页面加密有误");
        }
        userBaseInfo.setPageUrl(pageUrl);

        //异步加载缓存
        operateCaptureService.getBusiInfoToRedis(ecmStructureTreeDTO,token);

        return userBaseInfo;
    }

    /**
     *更新数据库
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateBusiInfoToDb(EcmBusiInfoRedisDTO ecmBusiInfoExtend,
                                   AccountTokenExtendDTO token, Date updateTime) {
        //更新业务信息表
        ecmBusiInfoMapper.update(null,
                new UpdateWrapper<EcmBusiInfo>().set("busi_no", ecmBusiInfoExtend.getBusiNo())
                        .set("update_user", token.getUsername())
                        .set("update_user_name", token.getName()).set("update_time", updateTime)
                        .eq("busi_id", ecmBusiInfoExtend.getBusiId()));
        //更新业务属性值表
        if (!org.apache.commons.collections4.CollectionUtils
                .isEmpty(ecmBusiInfoExtend.getAttrList())) {
            //删除属性值关联表
            List<EcmAppAttrDTO> attrList = ecmBusiInfoExtend.getAttrList();
            List<Long> appAttrIdList = attrList.stream().map(EcmAppAttrDTO::getAppAttrId)
                    .collect(Collectors.toList());
            LambdaQueryWrapper<EcmBusiMetadata> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(EcmBusiMetadata::getBusiId, ecmBusiInfoExtend.getBusiId());
            deleteWrapper.in(EcmBusiMetadata::getAppAttrId, appAttrIdList);
            ecmBusiMetadataMapper.delete(deleteWrapper);
            attrList.stream().forEach(s -> {
                EcmBusiMetadata ecmBusiMetadata = new EcmBusiMetadata();
                BeanUtils.copyProperties(s, ecmBusiMetadata);
                ecmBusiMetadata.setAppAttrVal(s.getAppAttrValue());
                ecmBusiMetadata.setBusiId(ecmBusiInfoExtend.getBusiId());
                //todo 改批量插入
                ecmBusiMetadataMapper.insert(ecmBusiMetadata);
            });
        }
        //添加操作记录表
        busiOperationService.addOperation(ecmBusiInfoExtend.getBusiId(), IcmsConstants.EDIT_BUSI,
                token, "编辑业务信息");
    }

    private Date updateBusiInfoToRedis(EcmBusiInfoRedisDTO ecmBusiInfoExtend,
                                       AccountTokenExtendDTO token,Date updateTime) {
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                ecmBusiInfoExtend.getBusiId());
        //更新业务编号
        ecmBusiInfoRedisDTO.setBusiNo(ecmBusiInfoExtend.getBusiNo());
        //更新就近修改人
        ecmBusiInfoRedisDTO.setUpdateUser(token.getUsername());
        //更新最近修改时间
        ecmBusiInfoRedisDTO.setUpdateTime(updateTime);
        if (!CollectionUtils.isEmpty(ecmBusiInfoExtend.getAttrList())) {
            List<EcmAppAttrDTO> attrDTOList = ecmBusiInfoExtend.getAttrList();
            List<EcmAppAttrDTO> attrList = ecmBusiInfoRedisDTO.getAttrList();
            for (EcmAppAttrDTO appAttrDTO : attrList) {
                for (EcmAppAttrDTO ecmAppAttrDTO : attrDTOList) {
                    if (Objects.equals(ecmAppAttrDTO.getAttrCode(), appAttrDTO.getAttrCode())) {
                        //更新业务属性值
                        appAttrDTO.setAppAttrValue(ecmAppAttrDTO.getAppAttrValue());
                    }
                }
            }
        }
        busiCacheService.saveAndUpate(ecmBusiInfoRedisDTO);
        return ecmBusiInfoRedisDTO.getUpdateTime();
    }

    private void checkParams(EditBusiAttrDTO editBusiAttrDTO) {
        AssertUtils.isNull(editBusiAttrDTO.getBusiNo(), "参数错误");
        AssertUtils.isNull(editBusiAttrDTO.getAppCode(), "参数错误");
        String busiNo = editBusiAttrDTO.getBusiNo();
        String appCode = editBusiAttrDTO.getAppCode();
        LambdaQueryWrapper<EcmAppDef> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmAppDef::getAppCode, appCode);
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectOne(wrapper);
        if (ecmAppDef == null) {
            throw new SunyardException("业务类型不存在");
        }
        LambdaQueryWrapper<EcmBusiInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmBusiInfo::getBusiNo, busiNo).eq(EcmBusiInfo::getAppCode,
                ecmAppDef.getAppCode());
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectOne(queryWrapper);
        if (ecmBusiInfo == null) {
            throw new SunyardException("业务不存在");
        }
        AssertUtils.isNull(editBusiAttrDTO.getEcmBusiAttrDTOList(), "参数错误");
        List<EcmBusiAttrDTO> busiAttrs = editBusiAttrDTO.getEcmBusiAttrDTOList();
        List<EcmBusiAttrDTO> uniquePrimaryKey = busiAttrs.stream()
                .filter(p -> IcmsConstants.ONE.equals(p.getIsKey())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(uniquePrimaryKey)) {
            EcmBusiAttrDTO attrByBusiNo = uniquePrimaryKey.get(0);
            AssertUtils.isNull(attrByBusiNo.getAppAttrValue(), attrByBusiNo.getAttrName() + "不能为空");
            //业务编号不能重复
            Long count = ecmBusiInfoMapper.selectCount(
                    new QueryWrapper<EcmBusiInfo>().eq("is_deleted", IcmsConstants.ZERO)
                            .eq("busi_no", busiNo).ne("busi_id", ecmBusiInfo.getBusiId()));
            AssertUtils.isTrue(count.intValue() > 0, "业务编号已存在");
        }
        //判断业务代码是否存在
        List<String> attrCodeList = busiAttrs.stream().map(EcmBusiAttrDTO::getAttrCode)
                .collect(Collectors.toList());
        LambdaQueryWrapper<EcmAppAttr> attrWrapper = new LambdaQueryWrapper<>();
        attrWrapper.eq(EcmAppAttr::getAppCode, ecmAppDef.getAppCode());
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(attrWrapper);
        List<String> ecmAppAttrCodes = ecmAppAttrs.stream().map(EcmAppAttr::getAttrCode)
                .collect(Collectors.toList());
        if (attrCodeList.size() > ecmAppAttrs.size()) {
            throw new SunyardException("业务属性数量超过上限");
        }
        ecmAppAttrCodes.retainAll(attrCodeList);
        if (ecmAppAttrCodes.size() != attrCodeList.size()) {
            throw new SunyardException("业务代码错误");
        }
    }

    /**
     * 校验业务是否存在
     */
    private EcmBusiInfoDTO checkBusi(EcmUploadAllDTO dto, Integer treeType) {
        EcmBusiInfoDTO ecmBusiInfoDTO = new EcmBusiInfoDTO();
        EcmBusExtendDTO ecmBusExtendDTO = dto.getEcmRootDataDTO().getEcmBusExtendDTOS();
        //如果传了业务属性，则取业务属性中的主属性作为业务主索引值
        //        List<EcmBusiAttrDTO> ecmBusiAttrDTOList = ecmBusExtendDTO.getEcmBusiAttrDTOList();
        //        if (CollectionUtil.isNotEmpty(ecmBusiAttrDTOList)) {
        //            //获取业务主索引
        //            List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, ecmBusExtendDTO.getAppCode()));
        //            AssertUtils.isTrue(CollectionUtil.isEmpty(ecmAppAttrs), "该业务类型未关联属性");
        //            EcmAppAttr ecmAppAttr = ecmAppAttrs.stream().filter(f -> f.getIsKey().equals(StateConstants.YES)).findFirst().get();
        //            EcmBusiAttrDTO ecmBusiAttrDTO = ecmBusiAttrDTOList.stream().filter(k -> k.getAttrCode().equals(ecmAppAttr.getAttrCode())).findFirst().get();
        //            ecmBusExtendDTO.setBusiNo(ecmBusiAttrDTO.getAppAttrValue());
        //        }
        String appCode = ecmBusExtendDTO.getAppCode();
        String businessNo = ecmBusExtendDTO.getBusiNo();
        LambdaQueryWrapper<EcmAppDef> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(EcmAppDef::getAppCode, appCode);
        //业务类型code唯一
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectById(appCode);
        if (ecmAppDef != null) {
            String appCode1 = ecmAppDef.getAppCode();
            LambdaQueryWrapper<EcmBusiInfo> busiWrapper = new LambdaQueryWrapper<>();
            busiWrapper.eq(EcmBusiInfo::getAppCode, appCode1);
            busiWrapper.eq(EcmBusiInfo::getBusiNo, businessNo);
            EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectOne(busiWrapper);
            //            AssertUtils.isTrue((ecmBusiInfo == null && treeType.equals(IcmsConstants.STATIC_TREE)), "静态树业务不存在");
            if (ecmBusiInfo == null) {
                //业务不存在，但是业务类型一定存在
                ecmBusiInfoDTO.setAppTypeName(ecmAppDef.getAppName());
                ecmBusiInfoDTO.setEquipmentId(ecmAppDef.getEquipmentId());
                return ecmBusiInfoDTO;
            }
            ecmBusiInfoDTO = BeanUtil.copyProperties(ecmBusiInfo, EcmBusiInfoDTO.class);
            ecmBusiInfoDTO.setAppTypeName(ecmAppDef.getAppName());
            ecmBusiInfoDTO.setEquipmentId(ecmAppDef.getEquipmentId());
            ecmBusiInfoDTO.setBusiBatchNo(businessNo);
        }
        return ecmBusiInfoDTO;
    }

    /**
     * 校验资料文件
     */
    public Result checkFile(EcmUploadAllDTO dto) {
        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        EcmBaseInfoDTO ecmBaseInfoDTO = dto.getEcmRootDataDTO().getEcmBaseInfoDTO();
        token.setRoleCodeList(ecmBaseInfoDTO.getRoleCode());
        token.setUsername(ecmBaseInfoDTO.getUserCode());
        token.setName(ecmBaseInfoDTO.getUserName());
        token.setOrgCode(ecmBaseInfoDTO.getOrgCode());
        token.setOrgName(ecmBaseInfoDTO.getOrgName());
        token.setOut(true);
        long l1 = System.currentTimeMillis();
        token = busiCacheService.checkUser(ecmBaseInfoDTO, token);
        ecmBaseInfoDTO.setUserName(token.getName());
        ecmBaseInfoDTO.setOrgName(token.getOrgName());
        AddBusiDTO ecmRootDataDTO2 = dto.getEcmRootDataDTO();
        ecmRootDataDTO2.setEcmBaseInfoDTO(ecmBaseInfoDTO);
        dto.setEcmRootDataDTO(ecmRootDataDTO2);
        long l2 = System.currentTimeMillis();
        log.debug("校验用户时间：" + (l2 - l1));
        checkAttrKey(dto);

        List<EcmUploadFileDTO> splitDTO = dto.getSplitDTO();
        String appCode = dto.getEcmRootDataDTO().getEcmBusExtendDTOS().getAppCode();
        AssertUtils.isNull(appCode, "业务类型编号不能为空");
        String busiNo = dto.getEcmRootDataDTO().getEcmBusExtendDTOS().getBusiNo();
        AssertUtils.isNull(busiNo, "业务编号不能为空");
        EcmBusiFileInfoDTO ecmBusiFileInfoDTO = new EcmBusiFileInfoDTO();
        EcmFileInfoDTO ecmFileInfoDTO = new EcmFileInfoDTO();
        List<FileDTO> matchFileList = new ArrayList<>();
        List<FileDTO> repeatFile = new ArrayList<>();
        Integer treeType = null;
        //判断业务树类型
        List<EcmVTreeDataDTO> ecmVTreeDataDTOS = dto.getEcmRootDataDTO().getEcmBusExtendDTOS()
                .getEcmVTreeDataDTOS();
        List<EcmRuleDataDTO> ecmRuleDataDTO = dto.getEcmRootDataDTO().getEcmBusExtendDTOS()
                .getEcmRuleDataDTO();
        //静态树(业务系统没传动态树和多维度)
        if (CollectionUtil.isEmpty(ecmVTreeDataDTOS) && CollectionUtil.isEmpty(ecmRuleDataDTO)) {
            ecmBaseInfoDTO.setTypeTree(IcmsConstants.ZERO.toString());
            treeType = IcmsConstants.STATIC_TREE;
        } else {
            ecmBaseInfoDTO.setTypeTree(IcmsConstants.ONE.toString());
            //取出动态树资料
            treeType = IcmsConstants.DYNAMIC_TREE;
        }

        //处理本节点业务
        EcmUploadFileDTO ecmUploadFileDTO = splitDTO.stream()
                .filter(f -> f.getDocNo().contains(dto.getDocNo())).findFirst().get();
        //校验业务是否存在
        EcmBusiInfoDTO ecmBusiInfo = checkBusi(dto, treeType);
        if (ecmBusiInfo.getBusiId() == null) {
            //业务不存在，新建业务
            AddBusiDTO ecmRootDataDTO = dto.getEcmRootDataDTO();
            EcmRootDataDTO ecmRootDataDTO1 = BeanUtil.copyProperties(ecmRootDataDTO,
                    EcmRootDataDTO.class);
            //新建业务
            List<EcmBusiInfo> ecmBusiInfos = addBusi(ecmRootDataDTO1, token);
            EcmBusiInfo busiId = ecmBusiInfos.get(0);
            ecmBusiInfo.setBusiId(busiId.getBusiId());
            ecmBusiInfo.setAppCode(appCode);
            ecmBusiInfo.setBusiNo(busiNo);
            ecmBusiInfo.setAppTypeName(ecmBusiInfo.getAppTypeName());
            ecmBusiInfo.setEquipmentId(ecmBusiInfo.getEquipmentId());

            ecmBusiInfo.setRightVer(busiId.getRightVer());
        } else {
            //如果是动态树且存在需要更新新redis中数据
            if (treeType.equals(IcmsConstants.DYNAMIC_TREE)) {
                Map<Long, List<EcmDocrightDefDTO>> maps = new HashMap<>();
                EcmRootDataDTO ecmRootDataDTO = BeanUtil.copyProperties(dto.getEcmRootDataDTO(),
                        EcmRootDataDTO.class);
                updateDynamicTreeData(dto.getEcmRootDataDTO().getEcmBusExtendDTOS(), token,
                        ecmRootDataDTO, maps, null);
            }
        }
        BeanUtil.copyProperties(ecmBusiInfo, ecmFileInfoDTO);
        ecmFileInfoDTO.setTreeType(treeType.toString());
        Map<String, Object> map = fileToUnclassified(treeType, ecmUploadFileDTO, ecmFileInfoDTO,
                ecmVTreeDataDTOS);
        if (MapUtil.isNotEmpty(map)) {
            Boolean unClassifyFlag = (Boolean) map.get("unClassifyFlag");
            //未匹配到，文件放到未归类
            if (unClassifyFlag) {
                matchFileList.addAll(ecmUploadFileDTO.getFileAndSortDTOS());
                ecmBusiFileInfoDTO.setMatchFileList(matchFileList);
            } else {
                //默认有新增权限，过滤重复文件
                //根据资料id获取已经上传过的文件md5
                List<EcmFileInfo> ecmFileInfos = checkRepeatFile(ecmBusiInfo.getBusiId(),
                        ecmUploadFileDTO.getDocNo());
                //需要过滤掉已经上传过的文件md5
                List<String> md5List = ecmFileInfos.stream().map(EcmFileInfo::getFileMd5)
                        .collect(Collectors.toList());
                List<FileDTO> fileAndSortDTOS = ecmUploadFileDTO.getFileAndSortDTOS();
                List<FileDTO> repeatFileList = fileAndSortDTOS.stream()
                        .filter(f -> md5List.contains(f.getFileMd5())).collect(Collectors.toList());
                repeatFile.addAll(repeatFileList);
                //没有重复的文件
                List<FileDTO> noRepeatFileList = fileAndSortDTOS.stream()
                        .filter(f -> !md5List.contains(f.getFileMd5()))
                        .collect(Collectors.toList());
                //校验文件格式（去掉格式校验）
                List<FileDTO> matchFile = noRepeatFileList;
                matchFileList.addAll(matchFile);
            }
            ecmBusiFileInfoDTO.setMatchFileList(matchFileList);
            ecmBusiFileInfoDTO.setRepeatFileMd5List(repeatFile);
            ecmBusiFileInfoDTO.setEcmFileInfoDTO(ecmFileInfoDTO);
        }
        return Result.success(JSONObject.toJSONString(ecmBusiFileInfoDTO),
                ResultCode.SUCCESS.getCode(), null);
    }

    private void checkAttrKey(EcmUploadAllDTO dto) {
        //校验主键
        String appCode1 = dto.getEcmRootDataDTO().getEcmBusExtendDTOS().getAppCode();
        if (Strings.isBlank(appCode1)) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "业务不存在!");
        }
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(
                new LambdaQueryWrapper<EcmAppAttr>().in(EcmAppAttr::getAppCode, appCode1)
                        .eq(EcmAppAttr::getIsKey, StateConstants.YES));
        if (CollectionUtils.isEmpty(ecmAppAttrs)) {
            throw new SunyardException(ResultCode.PARAM_ERROR,
                    "业务类型appCode的主键：" + appCode1 + "不存在");
        }
        EcmAppAttr ecmAppAttr = ecmAppAttrs.get(0);
        List<EcmBusiAttrDTO> ecmBusiAttrDTOList = dto.getEcmRootDataDTO().getEcmBusExtendDTOS()
                .getEcmBusiAttrDTOList();
        List<EcmBusiAttrDTO> collect2 = ecmBusiAttrDTOList.stream()
                .filter(s -> ecmAppAttr.getAttrCode().equals(s.getAttrCode()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect2)
                || StrUtil.isBlank(collect2.get(0).getAppAttrValue())) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "业务主索引不能为空!");
        }
        dto.getEcmRootDataDTO().getEcmBusExtendDTOS().setBusiNo(collect2.get(0).getAppAttrValue());
    }

    /**
     * 判断文件是否到未归类
     */
    private Map<String, Object> fileToUnclassified(Integer treeType, EcmUploadFileDTO fileDTO,
                                                   EcmFileInfoDTO ecmFileInfoDTO,
                                                   List<EcmVTreeDataDTO> dynamicTreeData) {
        Map<String, Object> map = new HashMap<>();
        if (StrUtil.isNotBlank(fileDTO.getDocNo())) {
            String docNo = fileDTO.getDocNo();
            if (treeType.equals(IcmsConstants.STATIC_TREE)) {
                ecmFileInfoDTO.setDocCode(docNo);
                ecmFileInfoDTO.setDocId(fileDTO.getDocNo());
                //获取业务当前版本
                LambdaQueryWrapper<EcmAppDocright> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(EcmAppDocright::getAppCode, ecmFileInfoDTO.getAppCode());
                wrapper.eq(EcmAppDocright::getRightVer, ecmFileInfoDTO.getRightVer());
                EcmAppDocright ecmAppDocright = ecmAppDocrightMapper.selectOne(wrapper);
                if (ecmAppDocright != null) {
                    //获取关联资料
                    LambdaQueryWrapper<EcmAppDocRel> relWrapper = new LambdaQueryWrapper<>();
                    relWrapper.eq(EcmAppDocRel::getAppCode, ecmFileInfoDTO.getAppCode());
                    relWrapper.eq(EcmAppDocRel::getDocCode, docNo);
                    relWrapper.eq(EcmAppDocRel::getType, IcmsConstants.ONE);
                    EcmAppDocRel ecmAppDocRels = ecmAppDocRelMapper.selectOne(relWrapper);
                    if (ecmAppDocRels == null) {
                        ecmFileInfoDTO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                        ecmFileInfoDTO.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
                        map.put("unClassifyFlag", true);
                    } else {
                        //静态树文件类型允许所有文件类型
                        List<String> existFileTypeList = getMatchFileTypeList();
                        //默认有新增权限
                        map.put("unClassifyFlag", false);
                        map.put("existFileTypeList", existFileTypeList);
                    }

                }
            } else {
                if (CollectionUtil.isNotEmpty(dynamicTreeData)) {
                    //平铺树
                    List<EcmVTreeDataDTO> ecmVTreeDataDTOS1 = flattenTree(dynamicTreeData);
                    Optional<EcmVTreeDataDTO> first = ecmVTreeDataDTOS1.stream()
                            .filter(f -> f.getDocCode().equals(docNo)).findFirst();
                    if (first.isPresent()) {
                        //取出动态树docId
                        LambdaQueryWrapper<EcmBusiDoc> wrapper = new LambdaQueryWrapper<>();
                        wrapper.eq(EcmBusiDoc::getBusiId, ecmFileInfoDTO.getBusiId());
                        wrapper.eq(EcmBusiDoc::getDocCode, docNo);
                        EcmBusiDoc ecmBusiDoc = ecmBusiDocMapper.selectOne(wrapper);
                        if (ecmBusiDoc != null) {
                            ecmFileInfoDTO.setDocCode(docNo);
                            ecmFileInfoDTO.setDocId(ecmBusiDoc.getDocId().toString());
                            map.put("unClassifyFlag", false);
                            List<String> existFileTypeList = getMatchFileTypeList();
                            map.put("existFileTypeList", existFileTypeList);
                        }
                    } else {
                        ecmFileInfoDTO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                        ecmFileInfoDTO.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
                        map.put("unClassifyFlag", true);
                    }
                }
            }
        } else {
            ecmFileInfoDTO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
            ecmFileInfoDTO.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
            map.put("unClassifyFlag", true);
        }
        return map;
    }

    /**
     * 获取允许上传的文件类型
     */
    private List<String> getMatchFileTypeList() {
        List<String> objects = new ArrayList<>();

        //获取文件配置的所有类型
        Result<Map<String, List<SysDictionaryDTO>>> dictionaryAll = dictionaryApi
                .getDictionaryAll(IcmsConstants.FILE_TYPE_DIC, null);
        if (dictionaryAll.isSucc()) {
            Map<String, List<SysDictionaryDTO>> data = dictionaryAll.getData();
            List<SysDictionaryDTO> sysDictionaryDTOS = data.get(IcmsConstants.FILE_TYPE_DIC);

            List<String> collect = sysDictionaryDTOS.stream().map(SysDictionaryDTO::getValue)
                    .collect(Collectors.toList());
            Set<String> resultSet = new HashSet<>();
            for (String item : collect) {
                if (StringUtils.isEmpty(item)) {
                    continue;
                }
                JSONObject jsonObject = JSONObject.parseObject(item);
                String limitFormat = jsonObject.getString("limit_format");
                String[] items = limitFormat.split(";");
                for (String subItem : items) {
                    resultSet.add(subItem);
                }
            }
            return new ArrayList<>(resultSet);
        }
        return objects;

        //
        //        EcmDocrightDef ecmDocrightDef = (EcmDocrightDef)object;
        //        List<EcmFileTypeDef> ecmFileTypeDefs = new ArrayList<>();
        //        List<String> resultList = new ArrayList<>();
        //        if(IcmsConstants.STATIC_TREE.equals(treeType)){
        //            LambdaQueryWrapper<EcmDocFileTypeLimit> fileTypeRelWrapper = new LambdaQueryWrapper<>();
        //            fileTypeRelWrapper.eq(EcmDocFileTypeLimit::getDocrightId,ecmDocrightDef.getDocrightId());
        //            List<EcmDocFileTypeLimit> ecmDocFileTypeLimits = ecmDocFileTypeLimitMapper.selectList(fileTypeRelWrapper);
        //            if (CollectionUtil.isNotEmpty(ecmDocFileTypeLimits)) {
        //                List<Long> fileTypeList = ecmDocFileTypeLimits.stream().map(EcmDocFileTypeLimit::getFileTypeId).collect(Collectors.toList());
        //                ecmFileTypeDefs = ecmFileTypeDefMapper.selectBatchIds(fileTypeList);
        //            }
        //        }else {
        //            //动态树获取所有业务类型
        //            ecmFileTypeDefs = ecmFileTypeDefMapper.selectList(null);
        //        }
        //        if(CollectionUtil.isNotEmpty(ecmFileTypeDefs)){
        //            List<String> fileType = ecmFileTypeDefs.stream().map(EcmFileTypeDef::getFileTypeCode).collect(Collectors.toList());
        //            Set<String> resultSet = new HashSet<>();
        //            // 遍历List中的每个元素
        //            for (String item : fileType) {
        //                // 使用分号;拆分元素，并添加到Set中
        //                String[] items = item.split(";");
        //                for (String subItem : items) {
        //                    resultSet.add(subItem);
        //                }
        //            }
        //            resultList = new ArrayList<>(resultSet);
        //        }
        //        return resultList;
    }

    /**
     * 校验文件类型
     */
    private Map<String, List<FileDTO>> checkFileType(List<FileDTO> fileDTOS,
                                                     List<String> fileFormatList) {
        Map<String, List<FileDTO>> resultMap = new HashMap<>();
        List<FileDTO> matchFileList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(fileFormatList)) {
            for (FileDTO fileDTO : fileDTOS) {
                if (null != fileDTO.getFile()) {
                    String fileName = fileDTO.getFile().getName();
                    String format = fileName.substring(fileName.lastIndexOf(".") + 1);
                    if (fileFormatList.contains(format)) {
                        matchFileList.add(fileDTO);
                    } else {
                        AssertUtils.isTrue(true, fileName + "文件格式不允许");
                        //                        noMatchFileList.add(fileDTO);
                    }
                }
            }
        }
        resultMap.put("matchFileList", matchFileList);
        //        resultMap.put("noMatchFileList", noMatchFileList);
        return resultMap;
    }


    /**
     *  对外接口，文本查重功能
     */
    public Result extractFileTextDup(FileOcrCallBackDTO fileOcrCallBackDTO) {
        log.info("文本查重对外接口传入参数：{}", JSONUtil.toJsonStr(fileOcrCallBackDTO));
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectOne(new LambdaQueryWrapper<EcmBusiInfo>()
                .eq(EcmBusiInfo::getBusiNo, fileOcrCallBackDTO.getBusiNo())
                .eq(EcmBusiInfo::getAppCode,fileOcrCallBackDTO.getAppCode()));
        if (ecmBusiInfo == null) {
            throw new SunyardException("当前业务不存在");
        }
        String key = RedisConstants.BUSIASYNC_TASK_PREFIX + ecmBusiInfo.getBusiId();
        //校验基本信息
        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        EcmBaseInfoDTO baseInfoDTO = fileOcrCallBackDTO.getEcmBaseInfoDTO();
        token.setUsername(baseInfoDTO.getUserCode());
        token.setName(baseInfoDTO.getUserName());
        token.setOrgName(baseInfoDTO.getOrgName());
        token.setOrgCode(baseInfoDTO.getOrgCode());
        token.setRoleCodeList(baseInfoDTO.getRoleCode());
        token.setOut(true);
        AccountTokenExtendDTO token1 = busiCacheService
                .checkUser(baseInfoDTO, token);
        baseInfoDTO.setUserName(token1.getName());
        baseInfoDTO.setOrgName(token1.getOrgName());
        fileOcrCallBackDTO.setEcmBaseInfoDTO(baseInfoDTO);
        List<FileOcrDTO> fileOcrDtos = fileOcrCallBackDTO.getFileOcrDtos();
        if (CollectionUtils.isEmpty(fileOcrDtos)){
            return Result.success();
        }
        for (FileOcrDTO fileOcrDto : fileOcrDtos) {
            //获取异步任务
            EcmAsyncTask ecmAsyncTask = busiCacheService.getEcmAsyncTask(key, fileOcrDto.getFileId().toString());
            String taskType = !ObjectUtils.isEmpty(ecmAsyncTask) ? ecmAsyncTask.getTaskType() : "000000000";
            log.info("文本查重当前业务id:{}，文件id：{}，获取异步任务为：{}",fileOcrCallBackDTO.getBusiNo(),fileOcrDto.getFileId().toString(),taskType);

            EcmFileInfo ecmFileInfo = ecmFileInfoMapper.selectOne(new LambdaQueryWrapper<EcmFileInfo>()
                    .eq(EcmFileInfo::getFileId, fileOcrDto.getFileId())
                    .eq(EcmFileInfo::getBusiId, ecmBusiInfo.getBusiId()));
            if (ObjectUtils.isEmpty(ecmAsyncTask)){
                //创建异步任务
                ecmAsyncTask = new EcmAsyncTask();
                ecmAsyncTask.setTaskType(taskType);
                ecmAsyncTask.setBusiId(ecmBusiInfo.getBusiId());
                ecmAsyncTask.setFileId(fileOcrDto.getFileId());
                asyncTaskService.insert(ecmAsyncTask);
            }
            //修改异步任务状态
            taskType = fileInfoService.updateTaskStatus(taskType, IcmsConstants.TYPE_FOUR,
                    EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            ecmAsyncTask.setTaskType(taskType);
            asyncTaskService.updateEcmAsyncTask(ecmAsyncTask);
            //封装dto
            EcmFileInfoDTO ecmFileInfoDTO = copyPramsToDto(fileOcrCallBackDTO,fileOcrDto,ecmFileInfo,ecmBusiInfo);
            //放入mq
            fileInfoService.processAfmData(ecmFileInfoDTO,ecmAsyncTask,ecmAsyncTask.getTaskType());
        }
        return Result.success();
    }

    private EcmFileInfoDTO copyPramsToDto(FileOcrCallBackDTO fileOcrCallBackDTO, FileOcrDTO fileOcrDto, EcmFileInfo ecmFileInfo, EcmBusiInfo ecmBusiInfo) {
        EcmFileInfoDTO ecmFileInfoDTO = new EcmFileInfoDTO();
        ecmFileInfoDTO.setTextDet(true);
        ecmFileInfoDTO.setBusiNo(fileOcrCallBackDTO.getBusiNo());
        ecmFileInfoDTO.setAppCode(fileOcrCallBackDTO.getAppCode());
        ecmFileInfoDTO.setAppTypeName(fileOcrCallBackDTO.getAppCode());
        ecmFileInfoDTO.setTextAll(fileOcrDto.getTextAll());
        ecmFileInfoDTO.setDocCode(ecmFileInfo.getDocCode());
        ecmFileInfoDTO.setDocName(ecmFileInfo.getDocCode());
        ecmFileInfoDTO.setFileMd5(ecmFileInfo.getFileMd5());
        ecmFileInfoDTO.setCreateUser(ecmFileInfo.getCreateUser());
        ecmFileInfoDTO.setCreateUserName(ecmFileInfo.getCreateUserName());
        ecmFileInfoDTO.setOrgCode(ecmFileInfo.getOrgCode());
        ecmFileInfoDTO.setOrgName(ecmFileInfo.getOrgName());
        ecmFileInfoDTO.setTreeType(String.valueOf(ecmBusiInfo.getTreeType()));
        ecmFileInfoDTO.setBusiId(ecmBusiInfo.getBusiId());
        ecmFileInfoDTO.setFileId(fileOcrDto.getFileId());
        ecmFileInfoDTO.setNewFileId(ecmFileInfo.getNewFileId());
        ecmFileInfoDTO.setCreateTime(ecmFileInfo.getCreateTime());
        ecmFileInfoDTO.setNewFileName(ecmFileInfo.getNewFileName());
        return ecmFileInfoDTO;
    }

    /**
     * 获取业务类型及文档列表
     */
    public List<QueryBusiInfoVO> queryBusi(QueryBusiDTO queryBusiDTO) {
        log.info("影像查询调阅对外接口传入参数：{}", JSONUtil.toJsonStr(queryBusiDTO));
        if (queryBusiDTO == null) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "参数有误");
        }
        if (queryBusiDTO.getEcmBaseInfoDTO() == null) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "用户信息不能为空");
        }
        //校验基本信息
        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        EcmBaseInfoDTO baseInfoDTO = queryBusiDTO.getEcmBaseInfoDTO();
        token.setUsername(baseInfoDTO.getUserCode());
        token.setName(baseInfoDTO.getUserName());
        token.setOrgName(baseInfoDTO.getOrgName());
        token.setOrgCode(baseInfoDTO.getOrgCode());
        token.setRoleCodeList(baseInfoDTO.getRoleCode());
        token.setOut(true);
        AccountTokenExtendDTO token1 = busiCacheService
                .checkUser(baseInfoDTO, token);
        baseInfoDTO.setUserName(token1.getName());
        baseInfoDTO.setOrgName(token1.getOrgName());
        queryBusiDTO.setEcmBaseInfoDTO(baseInfoDTO);
        //业务编码
        String appCode = queryBusiDTO.getAppCode();
        //业务属性过滤
        List<Long> busiIds = checkBusiAttr(queryBusiDTO);
        List<QueryBusiInfoVO> busiInfoVOS = new ArrayList<>();
        //获取业务ID
        LambdaQueryWrapper<EcmBusiInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(!CollectionUtils.isEmpty(busiIds),EcmBusiInfo::getBusiId, busiIds)
                .eq(EcmBusiInfo::getAppCode, appCode)
                .ge(!ObjectUtils.isEmpty(queryBusiDTO.getStartBusiCreate()), EcmBusiInfo::getCreateTime, queryBusiDTO.getStartBusiCreate())
                .le(!ObjectUtils.isEmpty(queryBusiDTO.getEndBusiCreate()), EcmBusiInfo::getCreateTime, queryBusiDTO.getEndBusiCreate());
        PageHelper.startPage(IcmsConstants.ONE,queryBusiSize);
        List<EcmBusiInfo> ecmBusiInfos = ecmBusiInfoMapper.selectList(wrapper);
        if (CollectionUtil.isEmpty(ecmBusiInfos)) {
            return new ArrayList<>();
        }
        //取得文件表中所有重复的文件md5
        String ext ="";
        if(pdfExt){
            //是否限制pdf格式
            ext=IcmsConstants.EXT_PDF;
        }
        List<Long> busiIdList = ecmBusiInfos.stream().map(EcmBusiInfo::getBusiId).collect(Collectors.toList());
        List<String> dupMd5List = ecmFileInfoMapper.searchDupMd5List(ext,busiIdList);
        for(EcmBusiInfo info : ecmBusiInfos){
            QueryBusiInfoVO vo = new QueryBusiInfoVO();
            BeanUtils.copyProperties(info, vo);
            //获取属性
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token1,
                    info.getBusiId());

            ArrayList<EcmBusiAttrQueryDataDTO> objects1 = new ArrayList<>();
            for (EcmAppAttrDTO appAttrDTO : ecmBusiInfoRedisDTO.getAttrList()) {
                EcmBusiAttrQueryDataDTO attrDTO = new EcmBusiAttrQueryDataDTO();
                attrDTO.setAttrCode(appAttrDTO.getAttrCode());
                attrDTO.setAppAttrValue(appAttrDTO.getAppAttrValue());
                objects1.add(attrDTO);
            }
            //set属性列表
            vo.setAttrList(objects1);
            ArrayList<QueryBusiFileVO> objects = new ArrayList<>();
            List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService
                    .getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
            if (CollectionUtil.isEmpty(fileInfoRedisEntities)) {
                fileInfoRedisEntities = new ArrayList<>();
            }
            fileInfoRedisEntities = fileInfoRedisEntities.stream()
                    .filter(s -> StateConstants.NO.equals(s.getState()))
                    .collect(Collectors.toList());
            Map<String, List<FileInfoRedisDTO>> collect = fileInfoRedisEntities.stream()
                    .collect(Collectors.groupingBy(FileInfoRedisDTO::getDocCode));
            List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = ecmBusiInfoRedisDTO
                    .getEcmBusiDocRedisDTOS();
            if (CollectionUtil.isNotEmpty(ecmBusiDocRedisDTOS)) {
                ArrayList<QueryDataTreeDTO> queryDataTreeDTOS = handleTree(collect,
                        ecmBusiDocRedisDTOS);
                vo.setEcmBusiDocRedisDTOS(queryDataTreeDTOS);
                //set文件信息
                for (FileInfoRedisDTO dto1 : fileInfoRedisEntities) {
                    QueryBusiFileVO queryBusiFileVO = new QueryBusiFileVO();
                    String url =fileFullPath+"storage/deal/getFileByFileId?fileId="+dto1.getNewFileId();
                    queryBusiFileVO.setFilePath(url);
                    queryBusiFileVO
                            .setFileId(dto1.getFileId() != null ? dto1.getFileId() + "" : null);
                    queryBusiFileVO.setIsDuplicate(dupMd5List.contains(dto1.getFileMd5()));
                    objects.add(queryBusiFileVO);
                }

                vo.setFileInfoRedisEntities(objects);
                busiInfoVOS.add(vo);
            } else {
                vo.setEcmBusiDocRedisDTOS(null);
                busiInfoVOS.add(vo);
            }
        }
        //对外接口成功后发送mq
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,ecmBusiInfos.get(0).getBusiId());
        captureSubmitService.sendMQMessage(ecmBusiInfoRedisDTO);
        return busiInfoVOS;
    }

    private List<Long> checkBusiAttr(QueryBusiDTO queryBusiDTO) {
        List<Long> busiIds = new ArrayList<>();
        List<EcmBusiAttrDTO> ecmBusiAttrDTOList = queryBusiDTO.getQueryCriteria();
        if (CollectionUtil.isNotEmpty(ecmBusiAttrDTOList)) {
            List<EcmAppAttrDTO> ecmAppAttrDTOS = PageCopyListUtils.copyListProperties(ecmBusiAttrDTOList, EcmAppAttrDTO.class);
            List<EcmAppAttrDTO> filterAttr = ecmAppAttrDTOS.stream()
                    .filter(p -> !ObjectUtils.isEmpty(p.getAppAttrValue()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(filterAttr)) {
                // appCode查询busiIds
                busiIds = ecmBusiMetadataMapper.complexSelect(filterAttr, filterAttr.size(),
                        Collections.singletonList(queryBusiDTO.getAppCode()));
            }
        }
        return busiIds;
    }
    public Result ecmBusiInfoCheck(EcmBusiInfoDataDTO ecmBusiInfoDataDTO) {
        log.info("影像业务校验对外接口传入参数：{}", JSONUtil.toJsonStr(ecmBusiInfoDataDTO));
        //校验基本信息
        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        EcmUserDTO baseInfoDTO = ecmBusiInfoDataDTO.getEcmUserDTO();
        token.setUsername(baseInfoDTO.getUserCode());
        token.setName(baseInfoDTO.getUserName());
        token.setOrgName(baseInfoDTO.getOrgName());
        token.setOrgCode(baseInfoDTO.getOrgCode());
        token.setRoleCodeList(baseInfoDTO.getRoleCode());
        token.setOut(true);
        busiCacheService.checkUser(baseInfoDTO, token);
        com.sunyard.ecm.dto.EcmBusiInfoDTO ecmBusiInfo = ecmBusiInfoDataDTO.getEcmBusiInfoDTO();
        if (Objects.isNull(ecmBusiInfo)) {
            throw new SunyardException(ResultCode.PARAM_ERROR, "业务信息不能为空!");
        }
        String appCode = ecmBusiInfo.getAppCode();
        if (StrUtil.isBlank(appCode)) {
            throw new SunyardException(ResultCode.PARAM_ERROR, "业务类型代码不能为空!");
        }
        String busiNo = ecmBusiInfo.getBusiNo();
        if (StrUtil.isBlank(busiNo)) {
            throw new SunyardException(ResultCode.PARAM_ERROR, "业务主索引不能为空!");
        }
        EcmBusiInfoDTO ecmBusiInfoDTO = ecmBusiInfoMapper.selectWithAppType(appCode, busiNo);
        if (Objects.isNull(ecmBusiInfoDTO)) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "当前业务不存在!");
        }
        return Result.success(null,ResultCode.SUCCESS.getCode(), "当前业务存在!");
    }

    public Result statisticsDocFileNUm(EcmBusiInfoDataDTO ecmBusiInfoDataDTO) {
        log.info("影像资料统计接口传入参数：{}", JSONUtil.toJsonStr(ecmBusiInfoDataDTO));
        //校验基本信息
        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        EcmUserDTO baseInfoDTO = ecmBusiInfoDataDTO.getEcmUserDTO();
        token.setUsername(baseInfoDTO.getUserCode());
        token.setName(baseInfoDTO.getUserName());
        token.setOrgName(baseInfoDTO.getOrgName());
        token.setOrgCode(baseInfoDTO.getOrgCode());
        token.setRoleCodeList(baseInfoDTO.getRoleCode());
        token.setOut(true);
        busiCacheService.checkUser(baseInfoDTO, token);
        com.sunyard.ecm.dto.EcmBusiInfoDTO ecmBusiInfo = ecmBusiInfoDataDTO.getEcmBusiInfoDTO();
        if (Objects.isNull(ecmBusiInfo)) {
            throw new SunyardException(ResultCode.PARAM_ERROR, "业务信息不能为空!");
        }
        String appCode = ecmBusiInfo.getAppCode();
        if (StrUtil.isBlank(appCode)) {
            throw new SunyardException(ResultCode.PARAM_ERROR, "业务类型代码不能为空!");
        }
        String busiNo = ecmBusiInfo.getBusiNo();
        if (StrUtil.isBlank(busiNo)) {
            throw new SunyardException(ResultCode.PARAM_ERROR, "业务主索引不能为空!");
        }
        EcmBusiInfoDTO ecmBusiInfoDTO = ecmBusiInfoMapper.selectWithAppType(appCode, busiNo);
        if (Objects.isNull(ecmBusiInfoDTO)) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "当前业务不存在!");
        }
        Long busiId = ecmBusiInfoDTO.getBusiId();
        // 查询该业务下总共有多少文件
        int count = fileInfoService.queryCountByBusiIdAndState(busiId, 0);
        // 查询该业务下各个资料下有多少文件
        DocFileNumVO docFileNumVO = new DocFileNumVO();
        List<DocFileNumVO> list = ecmFileInfoMapper.selectDocFileNums(busiId);
        StatisticsDocFileNumVO statisticsDocFileNumVO = new StatisticsDocFileNumVO();
        statisticsDocFileNumVO.setCount(count);
        statisticsDocFileNumVO.setDocFileNumList(list);

        return Result.success(statisticsDocFileNumVO,ResultCode.SUCCESS.getCode(), "操作成功!");
    }

    /**
     * 自动归档
     */
    public Result busiArchive(QueryBusiDTO queryBusiDTO) {
        AssertUtils.isNull(queryBusiDTO.getAppCode(), "业务类型不能为空");
        AssertUtils.isNull(queryBusiDTO.getBusiNo(),"业务编号不能为空");

        //校验源是否存在，不存在则报错
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper
                .selectOne(new LambdaQueryWrapper<EcmBusiInfo>()
                        .eq(EcmBusiInfo::getBusiNo,queryBusiDTO.getBusiNo())
                        .eq(EcmBusiInfo::getAppCode, queryBusiDTO.getAppCode()));
        AssertUtils.isNull(ecmBusiInfo, "业务不存在");
        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        token.setUsername(queryBusiDTO.getEcmBaseInfoDTO().getUserCode());
        token.setName(queryBusiDTO.getEcmBaseInfoDTO().getUserName());
        token.setOrgName(queryBusiDTO.getEcmBaseInfoDTO().getOrgName());
        token.setOrgCode(queryBusiDTO.getEcmBaseInfoDTO().getOrgCode());
        token.setRoleCodeList(queryBusiDTO.getEcmBaseInfoDTO().getRoleCode());
        token.setOut(true);
        //校验用户信息
        token = busiCacheService
                .checkUser(queryBusiDTO.getEcmBaseInfoDTO(), token);
        EcmBaseInfoDTO ecmBaseInfoDTO = queryBusiDTO.getEcmBaseInfoDTO();
        ecmBaseInfoDTO.setUserName(token.getName());
        ecmBaseInfoDTO.setOrgName(token.getOrgName());
        ecmBaseInfoDTO.setTypeTree(IcmsConstants.STATIC_TREE.toString());
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                ecmBusiInfo.getBusiId());
        List<EcmAppAttrDTO> attrList = ecmBusiInfoRedisDTO.getAttrList();
        List<EcmAppAttrDTO> isArchivedAttr = attrList.stream()
                .filter(s -> IcmsConstants.ONE.equals(s.getIsArchived())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(isArchivedAttr) || !StringUtils.hasText(isArchivedAttr.get(0).getAppAttrValue())){
            AssertUtils.isTrue(true, "业务不存在归档属性");
        }
        EcmAppAttrDTO attrDTO = isArchivedAttr.get(0);
        //归档属性值
        String appAttrValue = attrDTO.getAppAttrValue();
        //查询业务关联归档业务
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectById(ecmBusiInfoRedisDTO.getAppCode());
        AssertUtils.isNull(ecmAppDef,"业务编号不存在");
        if(!StateConstants.COMMON_ONE.equals(ecmAppDef.getIsApiArchived())
                || !StringUtils.hasText(ecmAppDef.getArchiveAppCode())){
            AssertUtils.isTrue(true,"业务未关联归档信息");
        }
        //根据关联归档业务编号和归档属性值定位业务
        String archiveAppCode = ecmAppDef.getArchiveAppCode();
        //校验归档属性不能重复
        // appCode查询busiIds
        /*List<EcmAppAttr> attrs = ecmAppAttrMapper.selectList(
                new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, archiveAppCode));
        List<EcmAppAttr> attrs1 = attrs.stream()
                .filter(p -> StateConstants.COMMON_ONE.equals(p.getIsArchived())).collect(Collectors.toList());
        AssertUtils.isTrue(CollectionUtils.isEmpty(attrs1),"关联归档业务未配置归档属性信息");
        List<EcmAppAttrDTO> attrDTOS = PageCopyListUtils.copyListProperties(attrs1, EcmAppAttrDTO.class);
        attrDTOS.get(0).setAppAttrValue(appAttrValue);
        List<Long> busiIds = ecmBusiMetadataMapper.complexSelect(attrDTOS, attrDTOS.size(),
                Collections.singletonList(archiveAppCode));*/
        List<Long> busiIds = new ArrayList<>();
        EcmBusiInfo archiveBusi = ecmBusiInfoMapper
                .selectOne(new LambdaQueryWrapper<EcmBusiInfo>()
                                .eq(EcmBusiInfo::getBusiNo, appAttrValue)
                                .eq(EcmBusiInfo::getAppCode, archiveAppCode));
        EcmBusExtendDTO addExtendDtos = new EcmBusExtendDTO();
        if(archiveBusi == null){
            EcmRootDataDTO ecmRootDataDTO = new EcmRootDataDTO();
            ecmRootDataDTO
                    .setEcmBaseInfoDTO(queryBusiDTO.getEcmBaseInfoDTO());
            //新建业务
            addExtendDtos.setAppCode(archiveAppCode);
            //生成busiNo
           /* String busiNo = IcmsConstants.AUTO_BUSI_PREFIX + System.currentTimeMillis() + generateSixDigitRandomNumber();
            busiNo = busiNo.replace("_","");*/
            addExtendDtos.setBusiNo(appAttrValue);
            //塞入属性信息
            List<EcmBusiAttrDTO> ecmBusiAttrDTOS = new ArrayList<>();
            List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(
                    new LambdaQueryWrapper<EcmAppAttr>().in(EcmAppAttr::getAppCode, archiveAppCode));
            for(EcmAppAttr ecmAppAttr : ecmAppAttrs){
                if(IcmsConstants.ONE.equals(ecmAppAttr.getIsKey())){
                    EcmBusiAttrDTO attr = new EcmBusiAttrDTO();
                    attr.setAttrCode(ecmAppAttr.getAttrCode());
                    attr.setAppAttrValue(appAttrValue);
                    ecmBusiAttrDTOS.add(attr);
                }/*else if(IcmsConstants.ONE.equals(ecmAppAttr.getIsArchived())){
                    EcmBusiAttrDTO attr = new EcmBusiAttrDTO();
                    attr.setAttrCode(ecmAppAttr.getAttrCode());
                    attr.setAppAttrValue(appAttrValue);
                    ecmBusiAttrDTOS.add(attr);
                }*/
            }
            addExtendDtos.setEcmBusiAttrDTOList(ecmBusiAttrDTOS);
            Map<Long, List<EcmDocrightDefDTO>> map = new HashMap<>();
            ecmRootDataDTO.setEcmBusExtendDTOS(Collections.singletonList(addExtendDtos));
            //新增业务
            List<EcmBusiInfo> ecmBusiInfos = addBusinessInfo(ecmRootDataDTO, token, map, true);
            List<Long> list = ecmBusiInfos.stream().map(EcmBusiInfo::getBusiId).collect(Collectors.toList());
            busiIds.addAll(list);
        }else {
            //已有业务
            busiIds.add(archiveBusi.getBusiId());
            EcmBusiInfoRedisDTO redisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                    busiIds.get(0));
            addExtendDtos.setAppCode(redisDTO.getAppCode());
            addExtendDtos.setBusiNo(redisDTO.getBusiNo());
            List<EcmAppAttrDTO> attrList1 = redisDTO.getAttrList();
            List<EcmBusiAttrDTO> ecmBusiAttrDTOS = PageCopyListUtils.copyListProperties(attrList1, EcmBusiAttrDTO.class);
            addExtendDtos.setEcmBusiAttrDTOList(ecmBusiAttrDTOS);
        }
        //源文件
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService
                .getFileInfoRedis(ecmBusiInfo.getBusiId());
        fileInfoRedis = fileInfoRedis.stream().filter(s -> StateConstants.NO.equals(s.getState()))
                .collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(fileInfoRedis) && CollectionUtil.isNotEmpty(busiIds)) {
            ArrayList<EcmBusExtendDTO> arrayList = new ArrayList();
            arrayList.add(addExtendDtos);
            List<String> tarMary = new ArrayList<>();
            Map mapRet = getMuliCopyFileResult(ecmBusiInfo, token, arrayList, busiIds, fileInfoRedis, tarMary);
            if (mapRet.keySet().size() > 0) {
                return Result.success(mapRet);
            }
        }
        return Result.success(true);
    }


    /**
     * 生成6位随机数
     */
    private static String generateSixDigitRandomNumber() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        // Generate 6 random digits
        for (int i = 0; i < 6; i++) {
            // Generate a random digit between 0 and 9
            int randomDigit = secureRandom.nextInt(10);
            sb.append(randomDigit);
        }

        return sb.toString();
    }
}
