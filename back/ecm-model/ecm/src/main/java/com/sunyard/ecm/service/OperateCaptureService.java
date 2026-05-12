package com.sunyard.ecm.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.annotation.WebsocketNoticeAnnotation;
import com.sunyard.ecm.constant.BusiInfoConstants;
import com.sunyard.ecm.constant.BusiLogConstants;
import com.sunyard.ecm.constant.DocRightConstants;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.RedisConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.ecm.dto.EcmBaseInfoDTO;
import com.sunyard.ecm.dto.EcmBusExtendDTO;
import com.sunyard.ecm.dto.EcmDownloadFileDTO;
import com.sunyard.ecm.dto.EcmPageBaseInfoDTO;
import com.sunyard.ecm.dto.EcmRootDataDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiStructureTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmDocrightDefDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmIntelligentDetectionAgainDTO;
import com.sunyard.ecm.dto.ecm.EcmStorageQueDTO;
import com.sunyard.ecm.dto.ecm.EcmStructureTreeDTO;
import com.sunyard.ecm.dto.redis.EcmBusiDocRedisDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.dto.redis.FileDocDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.dto.redis.UserBusiRedisDTO;
import com.sunyard.ecm.enums.EcmCheckAsyncTaskEnum;
import com.sunyard.ecm.enums.StrategyConstantsEnum;
import com.sunyard.ecm.es.EsEcmFile;
import com.sunyard.ecm.manager.AsyncBusiLogService;
import com.sunyard.ecm.manager.AsyncTaskService;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.manager.BusiOperationService;
import com.sunyard.ecm.manager.CaptureSubmitService;
import com.sunyard.ecm.manager.CommonService;
import com.sunyard.ecm.manager.FileInfoService;
import com.sunyard.ecm.manager.FileLabelService;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.manager.StaticTreePermissService;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmAppDocRelMapper;
import com.sunyard.ecm.mapper.EcmAppDocrightMapper;
import com.sunyard.ecm.mapper.EcmBusiDocMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmBusiMetadataMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmDocDefRelVerMapper;
import com.sunyard.ecm.mapper.EcmDocrightDefMapper;
import com.sunyard.ecm.mapper.EcmFileCommentMapper;
import com.sunyard.ecm.mapper.EcmFileExpireInfoMapper;
import com.sunyard.ecm.mapper.EcmFileHistoryMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.mapper.EcmFileLabelMapper;
import com.sunyard.ecm.mapper.SysBusiLogMapper;
import com.sunyard.ecm.mapper.es.EsEcmFileMapper;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmAppDocRel;
import com.sunyard.ecm.po.EcmAppDocright;
import com.sunyard.ecm.po.EcmAsyncTask;
import com.sunyard.ecm.po.EcmBusiDoc;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmBusiLog;
import com.sunyard.ecm.po.EcmBusiMetadata;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmDocDefRelVer;
import com.sunyard.ecm.po.EcmDocrightDef;
import com.sunyard.ecm.po.EcmFileComment;
import com.sunyard.ecm.po.EcmFileExpireInfo;
import com.sunyard.ecm.po.EcmFileHistory;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.po.EcmFileLabel;
import com.sunyard.ecm.util.CommonUtils;
import com.sunyard.ecm.util.EasyExcelUtils;
import com.sunyard.ecm.vo.AppTypeBusiVO;
import com.sunyard.ecm.vo.AppTypeVO;
import com.sunyard.ecm.vo.BatchImportFailureVO;
import com.sunyard.ecm.vo.BatchImportResultVO;
import com.sunyard.ecm.vo.BusiInfoVO;
import com.sunyard.ecm.vo.EcmsCaptureVO;
import com.sunyard.ecm.vo.FileInfoRedisEntityVO;
import com.sunyard.ecm.vo.FileInfoVO;
import com.sunyard.ecm.vo.MultiplexFileVO;
import com.sunyard.ecm.vo.SearchOptionVO;
import com.sunyard.ecm.vo.SearchVO;
import com.sunyard.ecm.vo.SysStrategyVO;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.UUIDUtils;
import com.sunyard.framework.common.util.date.DateUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.framework.spire.constant.OnlineConstants;
import com.sunyard.framework.spire.dto.FileCloudDTO;
import com.sunyard.framework.spire.util.OnlineUtils;
import com.sunyard.module.storage.api.FileHandleApi;
import com.sunyard.module.storage.api.FileStorageApi;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.vo.FileByteVO;
import com.sunyard.module.system.api.InstApi;
import com.sunyard.module.system.api.MenuApi;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.RoleApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysInstDTO;
import com.sunyard.module.system.api.dto.SysParamDTO;
import com.sunyard.module.system.api.dto.SysRoleDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.PropID;

/**
 * @author ty
 * @since 2023-4-24 15:38
 * @desc 影像采集实现类
 */
@Slf4j
@Service
public class OperateCaptureService {
    @Value("${fileIndex:ecm_file_dev}")
    private String fileIndex;
    @Value("${nodeType:1}")
    private String nodeType;
    private static final Lock lock = new ReentrantLock();
    private static final int NUMBER_LENGTH = 5;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
            //带时间的格式
            DateTimeFormatter.ofPattern("yyyy-M-d H:m:s"),      // 2026-1-1 9:5:3
            DateTimeFormatter.ofPattern("yyyy-M-d H:m"),         // 2026-1-1 9:5
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"),

            // === 纯日期格式 ===
            DateTimeFormatter.ofPattern("yyyy-M-d"),            // 通用：支持 2026/1/1, 2026-1-1, 2026.1.1
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd")
    );
    private static final long EXPIRE_TIME = 86400;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private SnowflakeUtils snowflakeUtils;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Resource
    private EcmFileCommentMapper ecmFileCommentMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmBusiDocMapper ecmBusiDocMapper;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmAppDocrightMapper ecmAppDocrightMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmAppAttrMapper ecmAppAttrMapper;
    @Resource
    private EcmBusiMetadataMapper ecmBusiMetadataMapper;
    @Resource
    private EcmDocrightDefMapper ecmDocRightDefMapper;
    @Resource
    private EcmDocDefRelVerMapper ecmDocDefRelVerMapper;
    @Resource
    private SysBusiLogMapper busiLogMapper;
    @Resource
    private EsEcmFileMapper esEcmFileMapper;
    @Resource
    private EcmAppDocRelMapper ecmAppDocRelMapper;
    @Resource
    private EcmFileExpireInfoMapper ecmFileExpireInfoMapper;
    @Resource
    private FileStorageApi fileStorageApi;
    @Resource
    private FileHandleApi fileHandleApi;
    @Resource
    private RoleApi roleApi;
    @Resource
    private InstApi instApi;
    @Resource
    private ParamApi paramApi;
    @Resource
    private UserApi userApi;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private SysStorageService sysStorageService;
    @Resource
    private BusiOperationService busiOperationService;
    @Resource
    private OperateFullQueryService operateFullQueryService;
    @Resource
    private CommonService commonService;
    @Resource
    private AsyncBusiLogService asyncBusiLogService;
    @Resource
    private StaticTreePermissService staticTreePermissService;
    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private AsyncTaskService asyncTaskService;
    @Resource
    private FileLabelService fileLabelService;
    @Resource
    private ModelBusiService modelBusiService;
    @Resource
    private ModelPermissionsService modelPermissionsService;
    @Resource
    private CaptureScanService captureScanService;
    @Resource
    private MenuApi menuApi;
    @Resource
    private CaptureSubmitService captureSubmitService;
    @Resource
    @Lazy
    private OpenApiService openApiService;

    /**
     * 影像文件重命名
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#vo.fileId + '_' + #vo.newName")
    @WebsocketNoticeAnnotation(busiId = "#vo.busiId")
    public void updateFileName(FileInfoVO vo) {
        AssertUtils.isNull(vo.getBusiId(), "参数错误");
        AssertUtils.isNull(vo.getFileId(), "参数错误");
        AssertUtils.isNull(vo.getNewName(), "备注不能为空");
        AssertUtils.isNull(vo.getCurentUserName(), "getToken().getName()不能为空");
        //修改持久化数据
        updateFileNameToDb(vo.getFileId(), vo.getNewName(), vo.getCurentUserName());
        //修改缓存数据
        updateFileNameToRedis(vo);
    }

    /**
     * 影像文件归类
     */
    @WebsocketNoticeAnnotation(msgType = "all")
    @Lock4j(keys = "#vo.busiId + '_' + #vo.fileIds")
    public void classifyIcmsFile(EcmsCaptureVO vo, AccountTokenExtendDTO token) {
        AssertUtils.isTrue(IcmsConstants.SHOW_PAGE.equals(token.getIsShow()),"当前无权限,无法进行采集相关操作");
        //参数校验
        checkPararmClassify(vo);
        //判断业务状态权限
        checkBusiStatus(vo, token);
        String docCode=vo.getDocNode().getDocCode();
        String appCode=vo.getDocNode().getAppCode();
        //验证是否可被他人修改
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, vo.getOldBusiId());
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId(), vo.getFileIds());
        List<EcmDocrightDefDTO> docrightDefCommon = busiCacheService.getDocrightDefCommon(ecmBusiInfoRedisDTO, token);
        //需要检验的权限列表
        List<String> rights = new ArrayList<>();
        rights.add(DocRightConstants.OTHER_UPDATE);
        commonService.checkDocRight(rights, fileInfoRedis, vo.getFileIds(), token.getUsername(), docrightDefCommon);
        //checkOtherUpdate(vo,token);
        //同资料节点md5校验
        checkMd5(vo, token);
        //权限校验
        for (String s : vo.getOldDocCode()) {
            if (vo.getDocNode().getType() == IcmsConstants.TREE_TYPE_BUSI) {
                //归类至另外一笔业务
                EcmBusiStructureTreeDTO ecmBusiStructureTreeDTO = null;
                if (!s.equals(IcmsConstants.UNCLASSIFIED_ID)) {
                    ecmBusiStructureTreeDTO = handleClassifyBusi(vo, token, vo.getDocNode().getChildren(), s, ecmBusiStructureTreeDTO);
                }
                if (ecmBusiStructureTreeDTO != null) {
                    vo.setDocNode(ecmBusiStructureTreeDTO);
                } else {
                    EcmBusiStructureTreeDTO dto = new EcmBusiStructureTreeDTO();
                    BeanUtils.copyProperties(dto, vo.getDocNode());
                    dto.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
                    dto.setName(IcmsConstants.UNCLASSIFIED);
                    dto.setType(IcmsConstants.TREE_TYPE_BUSI);
                    vo.setDocNode(dto);
                }
            } else {
                if (!vo.getDocNode().getDocCode().equals(s)) {
                    checkDocRightByClassify(vo, vo.getDocNode().getDocCode(), token);
                }
            }

        }
        List<String> docNames = vo.getFileInfoVOS().stream().map(FileDocDTO::getDocName).collect(Collectors.toList());
        docNames = docNames.stream().distinct().collect(Collectors.toList());
        updateClassifyInfos(vo, token, docNames);
        //异步将不加密的文件升级为加密文件
        //静态树处理，动态树不处理
        if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType())
                && CollectionUtil.isNotEmpty(fileInfoRedis)
                && !IcmsConstants.UNCLASSIFIED_ID.equals(vo.getDocNode().getDocCode())) {
            //EcmDocDef ecmDocDef = ecmDocDefMapper.selectById(vo.getDocNode().getDocCode());
            List<Long> files = fileInfoRedis.stream()
                    .filter(s -> vo.getFileIds().contains(s.getFileId()))
                    .map(FileInfoRedisDTO::getNewFileId)
                    .collect(Collectors.toList());
            Result<SysParamDTO> sysParam = paramApi.searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString());
            SysParamDTO data = sysParam.getData();
            String value = data.getValue();
            SysStrategyVO sysStrategyVO = JSONObject.parseObject(value, SysStrategyVO.class);
            commonService.encryptFile(sysStrategyVO.getEncryptStatus() ? IcmsConstants.YES_ENCRYPT : IcmsConstants.NO_ENCRYPT, files);
        }
        //查重,文档识别检测
        List<Integer> types=Arrays.asList(IcmsConstants.TYPE_ONE,IcmsConstants.TYPE_TWO,IcmsConstants.TYPE_THREE,IcmsConstants.TYPE_FOUR,IcmsConstants.TYPE_FIVE,IcmsConstants.TYPE_SIX,IcmsConstants.TYPE_SEVEN,IcmsConstants.TYPE_EIGHT,IcmsConstants.TYPE_NINE,IcmsConstants.TYPE_TEN);
        for (FileInfoRedisDTO file:fileInfoRedis) {
            //如果是手动处理待自动归类列表的数据,需要清掉对应的redis文件信息
            busiCacheService.delAutoClassPendingTaskList(RedisConstants.AUTO_CLASS_PENDING_TASK_LIST + vo.getDocNode().getBusiId(),
                    file.getFileId().toString());
            if (IcmsConstants.IMGS.contains(file.getFormat())) {
                EcmIntelligentDetectionAgainDTO dto = new EcmIntelligentDetectionAgainDTO();
                dto.setFileId(file.getFileId());
                dto.setBusiId(vo.getOldBusiId());
                dto.setNewFileId(file.getNewFileId());
                dto.setDocCode(docCode);
                dto.setAppCode(appCode);
                //文档以及查重检测
                dto.setTypes(types);
                fileInfoService.intelligentDetectionAgain(dto, token);
            }else if (IcmsConstants.DOCS.contains(file.getFormat())) {
                //除了图像类型其他发起文本查重
                EcmIntelligentDetectionAgainDTO dto = new EcmIntelligentDetectionAgainDTO();
                dto.setFileId(file.getFileId());
                dto.setBusiId(vo.getOldBusiId());
                dto.setNewFileId(file.getNewFileId());
                dto.setDocCode(docCode);
                dto.setAppCode(appCode);
                //文档以及查重检测
                dto.setTypes(types);
                fileInfoService.intelligentDetectionAgainByTextDup(dto, token);
            }
        }
    }

    public void checkBusiStatus(EcmsCaptureVO vo, AccountTokenExtendDTO token) {
        //文件编辑：待提交状态、处理失败状态可进行编辑
        if(!token.isOut()){
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                    .getEcmBusiInfoRedisDTO(token, vo.getOldBusiId());
            Integer status = ecmBusiInfoRedisDTO.getStatus();
            if(!BusiInfoConstants.BUSI_STATUS_ZERO.equals(status)&&!BusiInfoConstants.BUSI_STATUS_FOUR.equals(status)){
                AssertUtils.isTrue(true,"原业务状态暂无编辑文件权限");
            }
            //文件新增：0待提交状态、4处理失败状态、5已完结状态可进行新增
            if(!vo.getOldBusiId().equals(vo.getNewBusiId())){
                EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO1 = busiCacheService
                        .getEcmBusiInfoRedisDTO(token, vo.getNewBusiId());
                Integer status1 = ecmBusiInfoRedisDTO1.getStatus();
                if(!BusiInfoConstants.BUSI_STATUS_ZERO.equals(status1)&&!BusiInfoConstants.BUSI_STATUS_FOUR.equals(status1)&&
                        !BusiInfoConstants.BUSI_STATUS_FIVE.equals(status1)){
                    AssertUtils.isTrue(true,"目标业务状态暂无新增文件权限");
                }
            }
        }
    }

    private void updateClassifyInfos(EcmsCaptureVO vo, AccountTokenExtendDTO token, List<String> docNames) {
        //更新持久化数据
        classifyIcmsFileToDb(vo, token);
        //更新缓存数据
        classifyIcmsFileToRedis(vo, token);
        //更新es数据
        classifyIcmsFileToEs(vo, token);
        //保存归类日志
        asyncBusiLogService.saveClassifyLog(vo, token, docNames);
    }

    /**
       业务结构信息
     */
    private EcmBusiStructureTreeDTO handleClassifyBusi(EcmsCaptureVO vo, AccountTokenExtendDTO token, List<EcmBusiStructureTreeDTO> list, String docCode, EcmBusiStructureTreeDTO ecmBusiStructureTreeDTO) {
        if (CollectionUtils.isEmpty(list)) {
            return ecmBusiStructureTreeDTO;
        }
        for (EcmBusiStructureTreeDTO dto : list) {
            if (!CollectionUtils.isEmpty(dto.getChildren())) {
                ecmBusiStructureTreeDTO = handleClassifyBusi(vo, token, dto.getChildren(), docCode, ecmBusiStructureTreeDTO);
            } else {
                if (dto.getDocCode() != null && dto.getDocCode().equals(docCode)) {
                    checkDocRightByClassify(vo, docCode, token);
                    ecmBusiStructureTreeDTO = dto;
                    break;
                }
            }
        }
        return ecmBusiStructureTreeDTO;
    }

    /**
     * 校验md5
     */
    private void checkMd5(EcmsCaptureVO vo, AccountTokenExtendDTO token) {
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(vo.getNewBusiId());
        //校验勾选文件是否重复
        List<String> md5s = fileInfoRedis.stream().
                filter(s -> vo.getFileIds().contains(s.getFileId()) && s.getState().equals(StateConstants.NOT_DELETE)).
                map(FileInfoRedisDTO::getFileMd5).
                collect(Collectors.toList());
        HashSet<String> set = new HashSet<>(md5s);
        if (set.size() < md5s.size()) {
            AssertUtils.isTrue(true, "存在相同文件");
        }
        //过滤掉已删除文件
        List<String> collect = fileInfoRedis.stream()
                .filter(s -> (s.getDocCode().equals(vo.getDocNode().getDocCode()) && s.getState().equals(StateConstants.NOT_DELETE)))
                .map(FileInfoRedisDTO::getFileMd5)
                .collect(Collectors.toList());
        List<String> fileMd5List;
        if (vo.getOldBusiId().equals(vo.getNewBusiId())) {
            //同一笔业务归类
            fileMd5List = fileInfoRedis.stream()
                    .filter(s -> vo.getFileIds().contains(s.getFileId()))
                    .map(FileInfoRedisDTO::getFileMd5)
                    .collect(Collectors.toList());
        } else {
            //不同业务归类
            fileMd5List = fileInfoRedis
                    .stream().filter(s -> vo.getFileIds().contains(s.getFileId()))
                    .map(FileInfoRedisDTO::getFileMd5)
                    .collect(Collectors.toList());
        }
        fileMd5List.retainAll(collect);
        Boolean flag = true;
        if (vo.getOldBusiId().equals(vo.getNewBusiId())) {
            //同一笔业务下，同一个资料节点，不同标记允许归类
            List<String> oldDocCode = vo.getOldDocCode();
            oldDocCode.add(vo.getDocNode().getDocCode());
            HashSet<String> hashMap = new HashSet<>(oldDocCode);
            if (hashMap.size() == 1) {
                flag = false;
            }
        }
        if (flag) {
            AssertUtils.notNull(fileMd5List, "存在相同文件");
        }
    }

    /**
     * 校验资料节点权限
     */
    private void checkDocRightByClassify(EcmsCaptureVO vo, String docCode, AccountTokenExtendDTO token) {
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, vo.getNewBusiId());
        Integer maxLen = null;
        if (ecmBusiInfoRedisDTO != null) {
            if (IcmsConstants.DYNAMIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType())) {
                //动态树判断是否是最后一层级，如果不是最后一层级不允许拖拽
                List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = ecmBusiInfoRedisDTO.getEcmBusiDocRedisDTOS();
                checkTreeEndLevel(docCode, ecmBusiDocRedisDTOS);
                List<EcmDocrightDefDTO> docrightDefCommon = busiCacheService.getDocrightDefCommon(ecmBusiInfoRedisDTO, token);
                Map<String, List<EcmDocrightDefDTO>> collect = docrightDefCommon.stream().collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
                List<EcmDocrightDefDTO> ecmDocRightDefDTOList = collect.get(docCode);
                maxLen = ecmDocRightDefDTOList.get(0).getMaxLen();
            } else {
                //静态树校验文件格式权限
                EcmDocDef ecmDocDef = ecmDocDefMapper.selectById(docCode);
                List<Long> fileIds = vo.getFileIds();
                List<EcmFileInfo> ecmFileInfos = ecmFileInfoMapper.selectBatchIds(fileIds);
                for (EcmFileInfo fileInfo : ecmFileInfos) {
                    String suffix = FilenameUtils.getExtension(fileInfo.getNewFileName()).toLowerCase();
                    getAllSuffix(ecmDocDef, suffix, fileInfo.getNewFileSize());
                }
                maxLen = ecmDocDef.getMaxFiles();
            }
            if (maxLen == null) {
                maxLen = 1000;
            }
            //文件权限从关联版本获取最新权限
            List<EcmDocrightDefDTO> currentDocRight = busiCacheService.getDocrightDefCommon(ecmBusiInfoRedisDTO, token);
            //筛选得到要归类到某个节点的权限
            List<EcmDocrightDefDTO> collect = currentDocRight.stream().filter(p -> p.getDocCode().equals(docCode)).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)) {
                EcmDocrightDefDTO ecmDocrightDefDTO = collect.get(StateConstants.ZERO);
                AssertUtils.isTrue(String.valueOf(StateConstants.ZERO).equals(ecmDocrightDefDTO.getAddRight()), "归类失败，" + vo.getDocNode().getName() + "节点无新增权限！");
                List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService.getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
                if (!CollectionUtils.isEmpty(fileInfoRedisEntities)) {
                    //判断节点最大上传大小
                    List<FileInfoRedisDTO> collect1 = fileInfoRedisEntities.stream().filter(p -> p.getDocCode().equals(docCode) && StateConstants.ZERO.equals(p.getState())).collect(Collectors.toList());
                    int fileNum = collect1.size();
                    log.info("################# VO:{}", vo);
                    AssertUtils.isTrue(fileNum + vo.getFileIds().size() > maxLen, "归类失败，文件数量超过[" + vo.getDocNode().getName() + "]节点最大数量！");
                } else {
                    int fileNum = vo.getFileIds().size();
                    AssertUtils.isTrue(fileNum + vo.getFileIds().size() > maxLen, "归类失败，文件数量超过[" + vo.getDocNode().getName() + "]节点最大数量！");
                }
            } else {
                AssertUtils.isTrue(true, "归类失败，" + vo.getDocNode().getName() + "节点无新增权限！");
            }
        }
    }

    /**
     *
      获取文件权限
     */
    private static void getAllSuffix(EcmDocDef ecmDocDef, String suffix, Long newFileSize) {
        boolean allSuffixBase = getAllSuffixBase(ecmDocDef.getImgLimit(), suffix, newFileSize);
        if (allSuffixBase) {
            return;
        }
        boolean allSuffixBase1 = getAllSuffixBase(ecmDocDef.getOtherLimit(), suffix, newFileSize);
        if (allSuffixBase1) {
            return;
        }
        boolean allSuffixBase2 = getAllSuffixBase(ecmDocDef.getVideoLimit(), suffix, newFileSize);
        if (allSuffixBase2) {
            return;
        }
        boolean allSuffixBase3 = getAllSuffixBase(ecmDocDef.getAudioLimit(), suffix, newFileSize);
        if (allSuffixBase3) {
            return;
        }
        boolean allSuffixBase4 = getAllSuffixBase(ecmDocDef.getOfficeLimit(), suffix, newFileSize);
        if (allSuffixBase4) {
            return;
        }
        AssertUtils.isTrue(true, "归类失败，当前格式【" + suffix + "】不支持归入当前节点");

    }

    /**
     * 获取归类权限
     */
    private static boolean getAllSuffixBase(String imgLimit, String suffix, Long newFileSize) {
        if (StringUtils.isEmpty(imgLimit)) {
            return false;
        }
        JSONObject jsonObject = JSONObject.parseObject(imgLimit);
        Integer limitDisabel = jsonObject.getInteger("limit_disabel");
        if (limitDisabel.equals(StateConstants.NO)) {
            return false;
        }
        String limitFormat = jsonObject.getString("limit_format");
        String limitLength = jsonObject.getString("limit_length");

        if (StringUtils.isEmpty(limitFormat)) {
            return false;
        }
        String[] split = limitFormat.split(";");
        for (String s : split) {
            if (s.equals(suffix)) {
                AssertUtils.isTrue(Double.parseDouble(limitLength) < (newFileSize / 1000 / 1000), "归类失败，文件大小超过当前节点可上传的限定值");
                return true;
            }
        }
        return false;
    }

    /**
     * 校验节点等级
     */
    private void checkTreeEndLevel(String docCode, List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS) {
        ecmBusiDocRedisDTOS.forEach(m -> {
            if (m.getDocCode().equals(docCode)) {
                AssertUtils.notNull(m.getChildren(), "不允许归类至父级");
            } else {
                if (!CollectionUtils.isEmpty(m.getChildren())) {
                    checkTreeEndLevel(docCode, m.getChildren());
                }
            }
        });
    }

    /**
     * 新增采集
     */
    @Async("GlobalThreadPool")
    public String addCapture(List<String> appTypeIds, AccountTokenExtendDTO token) {
        AssertUtils.isNull(appTypeIds, "业务类型不能为空");
        checkAppDocright(appTypeIds);
        UserBusiRedisDTO userBusiEntity = new UserBusiRedisDTO();
        List<AppTypeBusiVO> appTypeBusiVOS = new ArrayList<>();
        for (String appCode : appTypeIds) {
            AppTypeBusiVO appTypeBusiVo = new AppTypeBusiVO();
            appTypeBusiVo.setAppCode(appCode);
            appTypeBusiVOS.add(appTypeBusiVo);
        }
        userBusiEntity.setAppType(appTypeIds);
        userBusiEntity.setUsercode(token.getUsername());
        userBusiEntity.setRelation(appTypeBusiVOS);
        //设置扫描类型
        userBusiEntity.setModelType(IcmsConstants.ADD_BUSI);
        //redis存储当前用户影像收集界面关联的业务类型id列表
        //生成页面唯一表示uuid
        String pageFlag = UUIDUtils.generateUUID();
        busiCacheService.saveOrUpdateUser(pageFlag, userBusiEntity);
        return pageFlag;
    }

    /**
     * 更新用户业务信息缓存
     */
    public String addCapturePage2(List<String> appTypeIds, AccountTokenExtendDTO token) {
        AssertUtils.isNull(appTypeIds, "业务类型不能为空");
        checkAppDocright2(appTypeIds);
        UserBusiRedisDTO userBusiEntity = busiCacheService.getUserPageRedis(token.getFlagId(), token);

        List<AppTypeBusiVO> appTypeBusiVOS = userBusiEntity.getRelation();
        if (CollectionUtils.isEmpty(appTypeBusiVOS)) {
            appTypeBusiVOS = new ArrayList<>();
        }
        for (String appCode : appTypeIds) {
            AppTypeBusiVO appTypeBusiVo = new AppTypeBusiVO();
            appTypeBusiVo.setAppCode(appCode);
            appTypeBusiVOS.add(appTypeBusiVo);
        }

        userBusiEntity.setUsercode(token.getUsername());
        userBusiEntity.setAppType(appTypeIds);
        userBusiEntity.setRelation(appTypeBusiVOS);
        //设置扫描类型
        userBusiEntity.setModelType(IcmsConstants.ADD_BUSI);
        //redis存储当前用户影像收集界面关联的业务类型id列表
        //生成页面唯一表示uuid
        busiCacheService.saveOrUpdateUser(token.getFlagId(), userBusiEntity);
        return token.getFlagId();
    }

    /**
     * 校验业务类型信息
     */
    private void checkAppDocright2(List<String> appTypeIds) {
        //获取所有业务类型信息
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectBatchIds(appTypeIds);
        if (CollectionUtils.isEmpty(ecmAppDefs)) {
            AssertUtils.isTrue(true, "业务类型信息丢失");
        }
    }
    /**
     * 校验业务类型信息
     */
    private void checkAppDocright(List<String> appTypeIds) {
        //获取所有业务类型信息
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectBatchIds(appTypeIds);
        if (CollectionUtils.isEmpty(ecmAppDefs)) {
            AssertUtils.isTrue(true, "业务类型信息丢失");
        }
    }

    /**
     * 新增采集
     */
    @Lock4j(keys = "#ecmBusiInfoExtend.busiNo + '_' + #token.id")
    public AppTypeVO addBusi(EcmBusiInfoRedisDTO ecmBusiInfoExtend, AccountTokenExtendDTO token) {
        //业务主属性是否允许为空
        judgeAppAttrListIsNull(ecmBusiInfoExtend);
        AppTypeVO appTypeVO = new AppTypeVO();
        ecmBusiInfoExtend.setModelType(IcmsConstants.TWO);
        ecmBusiInfoExtend.setStatus(BusiInfoConstants.BUSI_STATUS_ZERO);
        appTypeVO.setAppCode(ecmBusiInfoExtend.getAppCode());
        //新增采集
        List<String> appTypeIds = new ArrayList<>();
        appTypeIds.add(ecmBusiInfoExtend.getAppCode());
        //查询目前业务类型对应的 最新资料版本
        List<EcmAppDocright> ecmAppDocrights = ecmAppDocrightMapper.selectList(new LambdaQueryWrapper<EcmAppDocright>()
                .eq(EcmAppDocright::getAppCode, ecmBusiInfoExtend.getAppCode()).eq(EcmAppDocright::getRightNew, StateConstants.COMMON_ONE));
        AssertUtils.isTrue(CollectionUtils.isEmpty(ecmAppDocrights), "该资料类型无默认最新权限版本!");
        List<EcmDocDefRelVer> list = ecmDocDefRelVerMapper.selectList(new LambdaQueryWrapper<EcmDocDefRelVer>()
                .eq(EcmDocDefRelVer::getAppCode, ecmBusiInfoExtend.getAppCode()).eq(EcmDocDefRelVer::getRightVer, ecmAppDocrights.get(0).getRightVer()));
        AssertUtils.isTrue(CollectionUtils.isEmpty(list), "该业务类型未关联资料!");
        //1、只添加了业务类型，没有输入业务主属性
        if (CollectionUtils.isEmpty(ecmBusiInfoExtend.getAttrList())) {
            checkDocRightValue(ecmBusiInfoExtend.getAppCode(), token);
            addCapturePage2(appTypeIds, token);
        } else {
            List<EcmAppAttrDTO> attrList = ecmBusiInfoExtend.getAttrList();
            List<EcmAppAttrDTO> mainAttr = attrList.stream().filter(f -> IcmsConstants.ONE.equals(f.getIsKey())).collect(Collectors.toList());
            //2、业务主属性对应的值为空，则不存业务
            AssertUtils.isTrue(CollectionUtil.isEmpty(mainAttr), "该业务未配置业务主属性");
            String appAttrValue = mainAttr.get(0).getAppAttrValue();
            if (StrUtil.isBlank(appAttrValue)) {
                checkDocRightValue(ecmBusiInfoExtend.getAppCode(), token);
                addCapturePage2(appTypeIds, token);
            } else {
                //校验业务编号
                checkBusiNo(ecmBusiInfoExtend);
                //新增业务
                //校验该业务类型业务是否全部没有权限
                checkDocRightValue2(ecmBusiInfoExtend.getAppCode(), token, ecmAppDocrights.get(0));
                addCapturePage2(appTypeIds, token);
                ecmBusiInfoExtend.setPageFlag(ecmBusiInfoExtend.getPageFlag());
                //插入业务信息
                insertEcmBusiInfo(ecmBusiInfoExtend, token, ecmAppDocrights.get(0).getRightVer());

                /** 静态树资料不存储关联关系，组装树的时候根据业务权限版本获取 **/
//                //添加业务类型关联的资料类型静态树
                //redis添加采集页面业务相关信息
                busiCacheService.addBusiExtendInfoToRedis(ecmBusiInfoExtend, token, null);
                //redis更新用户-业务相关信息
                updateUserBusiToRedis(ecmBusiInfoExtend, token, null);
                //添加操作记录表
                busiOperationService.addOperation(ecmBusiInfoExtend.getBusiId(), IcmsConstants.ADD_BUSI, token, "新增业务");
                appTypeVO.setBusiId(ecmBusiInfoExtend.getBusiId());
                //添加到es
                operateFullQueryService.addEsBusiInfo(ecmBusiInfoExtend, token.getId());
                //关户设置权限
                checkCloseAccountPermission(attrList, token, true);
            }
        }
        //设置业务类型主索引
        appTypeVO.setPageFlag(ecmBusiInfoExtend.getPageFlag());
        setBusiMainIndex(appTypeVO);
        return appTypeVO;
    }

    private void judgeAppAttrListIsNull(EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        for (EcmAppAttrDTO ecmAppAttrDTO : ecmBusiInfoExtend.getAttrList()) {
            //是否是主键 是否可以为空
            if (IcmsConstants.ONE.equals(ecmAppAttrDTO.getIsKey()) && IcmsConstants.ZERO.equals(ecmAppAttrDTO.getIsNull())){
                AssertUtils.isNull(ecmAppAttrDTO.getAppAttrValue(), "请维护主键属性");
            }
        }

    }

    /**
     * 获取业务主索引
     */
    private void setBusiMainIndex(AppTypeVO appTypeVO) {
        LambdaQueryWrapper<EcmAppAttr> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmAppAttr::getAppCode, appTypeVO.getAppCode());
        wrapper.eq(EcmAppAttr::getIsKey, IcmsConstants.ONE);
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(wrapper);
        if (CollectionUtil.isNotEmpty(ecmAppAttrs)) {
            //获取主索引值
            EcmAppAttr ecmAppAttr = ecmAppAttrs.get(0);
            appTypeVO.setMainIndexName(ecmAppAttr.getAttrName());
            appTypeVO.setMainIndexValue("");
            List<EcmBusiMetadata> ecmBusiMetadata = ecmBusiMetadataMapper.selectList(new LambdaQueryWrapper<EcmBusiMetadata>()
                    .eq(EcmBusiMetadata::getAppAttrId, appTypeVO.getBusiId())
                    .eq(appTypeVO.getBusiId() != null, EcmBusiMetadata::getBusiId, appTypeVO.getBusiId()));
            if (CollectionUtil.isNotEmpty(ecmBusiMetadata)) {
                appTypeVO.setMainIndexValue(ecmBusiMetadata.get(0).getAppAttrVal());
            }
        }
    }

    /**
     * 校验业务编号
     */
    private void checkBusiNo(EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        //校验是否该业务类型该业务编号数据已经存在
        if (CollectionUtils.isEmpty(ecmBusiInfoExtend.getAttrList())) {
            AssertUtils.isTrue(CollectionUtils.isEmpty(ecmBusiInfoExtend.getAttrList()), "业务类型未关联属性");
        }
        List<EcmAppAttrDTO> attrList = ecmBusiInfoExtend.getAttrList();
        Optional<EcmAppAttrDTO> first = attrList.stream().filter(f -> IcmsConstants.ONE.equals(f.getIsKey())).findFirst();
        if (!first.isPresent()) {
            AssertUtils.isTrue(!first.isPresent(), "业务类型未定义主属性");
        }
        //拓展属性值校验
        for (EcmAppAttrDTO ecmAppAttrDTO : attrList) {
            //校验拓展属性中下拉选是否再默认值范围内
            if (IcmsConstants.THREE.equals(ecmAppAttrDTO.getInputType())){
                String listValue = ecmAppAttrDTO.getListValue();
                List<String> allowedValues = Arrays.stream(listValue.split(";"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                AssertUtils.isTrue(!StringUtils.isEmpty(ecmAppAttrDTO.getAppAttrValue())  && !allowedValues.contains(ecmAppAttrDTO.getAppAttrValue()), "【"+ecmAppAttrDTO.getAttrName()+"】"+"格式错误");
            }else if (IcmsConstants.TWO.equals(ecmAppAttrDTO.getInputType())){
                //日期类型的值校验
                try {
                    if (!StringUtils.isEmpty(ecmAppAttrDTO.getAppAttrValue())){
                        ecmAppAttrDTO.setAppAttrValue(parseAndFormat(ecmAppAttrDTO.getAppAttrValue()));
                    }
                }catch (Exception e){
                    AssertUtils.isTrue(true, e.getMessage());
                }
            }
        }
        EcmAppAttrDTO ecmAppAttrDTO = first.get();
        //根据主属性（业务编号）和业务类型id查询该业务编号是否已经存在
        List<EcmBusiInfo> busiInfos = ecmBusiInfoMapper.selectList(new LambdaQueryWrapper<EcmBusiInfo>()
                .eq(!ObjectUtils.isEmpty(ecmBusiInfoExtend.getAppCode()), EcmBusiInfo::getAppCode, ecmBusiInfoExtend.getAppCode())
                .eq(EcmBusiInfo::getBusiNo, ecmAppAttrDTO.getAppAttrValue()));
        if (CollectionUtils.isNotEmpty(busiInfos)) {
            AssertUtils.isTrue(CollectionUtils.isNotEmpty(busiInfos), "业务编号已存在");
        }
        /*//校验归档属性不能重复
        List<EcmAppAttrDTO> uniqueArchived = attrList.stream().filter(p -> IcmsConstants.ONE.equals(p.getIsArchived())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(uniqueArchived)) {
            List<EcmAppAttrDTO> filterAttr = uniqueArchived.stream()
                    .filter(p -> !ObjectUtils.isEmpty(p.getAppAttrValue()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(filterAttr)) {
                // appCode查询busiIds
                List busiIds = ecmBusiMetadataMapper.complexSelect(filterAttr, filterAttr.size(),
                        Collections.singletonList(ecmBusiInfoExtend.getAppCode()));
                if(CollectionUtils.isNotEmpty(busiIds)){
                    Long count = ecmBusiInfoMapper.selectCount(new LambdaQueryWrapper<EcmBusiInfo>().eq(EcmBusiInfo::getIsDeleted, IcmsConstants.ZERO).in(EcmBusiInfo::getBusiId, busiIds));
                    AssertUtils.isTrue(count.intValue() > 1, "归档属性已存在");
                }
            }
        }*/
    }

    /**
     * 校验业务类型对应资料是否没有配置资料权限
     */
    private void checkDocRightValue2(String appCode, AccountTokenExtendDTO tokenExtend, EcmAppDocright ecmAppDocright) {
        //先校验业务是否有关联资料，若没关联，则不允许新增
        SysRoleDTO data = null;
        List<Long> roleIds = new ArrayList<>();
        if (tokenExtend.isOut()) {
            //对外接口
            roleIds = tokenExtend.getRoleIdList();
        } else {
            //查找用户关联的角色id
            Result<List<Long>> result = userApi.getRoleListByUsername(tokenExtend.getUsername());
            if (result.isSucc() && CollectionUtils.isNotEmpty(result.getData())) {
                //成功，获取角色id列表
                roleIds = result.getData();
                List<String> roleIdsStr = roleIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
                Integer rightVer = ecmAppDocright.getRightVer();
                LambdaQueryWrapper<EcmDocrightDef> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(EcmDocrightDef::getDimType, IcmsConstants.ZERO);
                wrapper.eq(EcmDocrightDef::getAppCode, appCode);
                wrapper.eq(EcmDocrightDef::getRightVer, rightVer);
                wrapper.in(EcmDocrightDef::getRoleDimVal, roleIdsStr);
                List<EcmDocrightDef> ecmDocrightDefList = ecmDocRightDefMapper.selectList(wrapper);
                AssertUtils.isTrue(CollectionUtils.isEmpty(ecmDocrightDefList), "该用户未配置资料权限");
                //判断是否所有资料权限都为空
                checkAllDocRight(BeanUtil.copyToList(ecmDocrightDefList, EcmDocrightDefDTO.class), IcmsConstants.ONE);
            } else {
                AssertUtils.isTrue(ObjectUtils.isEmpty(roleIds), "当前用户未关联任何角色");
            }
        }
    }

    /**
     * 校验业务类型对应资料是否没有配置资料权限
     */
    private void checkDocRightValue(String appCode, AccountTokenExtendDTO tokenExtend) {
        //校验业务类型是否关联属性，没有关联则给出提示切不让新增成功
        LambdaQueryWrapper<EcmAppAttr> attrWrapper = new LambdaQueryWrapper<>();
        attrWrapper.eq(EcmAppAttr::getAppCode, appCode);
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(attrWrapper);
        AssertUtils.isTrue(CollectionUtil.isEmpty(ecmAppAttrs), "该业务类型未关联业务属性");
        //先校验业务是否有关联资料，若没关联，则不允许新增
        LambdaQueryWrapper<EcmDocDefRelVer> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(EcmDocDefRelVer::getAppCode, appCode);
        List<EcmDocDefRelVer> ecmAppDocRel = ecmDocDefRelVerMapper.selectList(relWrapper);
        AssertUtils.isTrue(CollectionUtils.isEmpty(ecmAppDocRel), "该业务类型未关联资料");
        SysRoleDTO data = null;
        UserBusiRedisDTO userPageRedis = busiCacheService.getUserPageRedis(tokenExtend.getFlagId(), tokenExtend);
        List<Long> roleIds = userPageRedis.getRoleIds();
        List<String> roleIdsStr = roleIds.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        //根据业务类型id获取当前资料权限版本
        LambdaQueryWrapper<EcmAppDocright> versionWrapper = new LambdaQueryWrapper<>();
        versionWrapper.eq(EcmAppDocright::getAppCode, appCode);
        versionWrapper.eq(EcmAppDocright::getRightNew, IcmsConstants.ONE);
        EcmAppDocright ecmAppDocright = ecmAppDocrightMapper.selectOne(versionWrapper);
        AssertUtils.isTrue(ObjectUtils.isEmpty(ecmAppDocright), "当前业务类型未配置最新版本信息");
        Integer rightVer = ecmAppDocright.getRightVer();
        LambdaQueryWrapper<EcmDocrightDef> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmDocrightDef::getDimType, IcmsConstants.ZERO);
        wrapper.eq(EcmDocrightDef::getAppCode, appCode);
        wrapper.eq(EcmDocrightDef::getRightVer, rightVer);
        wrapper.in(EcmDocrightDef::getRoleDimVal, roleIdsStr);
        List<EcmDocrightDef> ecmDocrightDefList = ecmDocRightDefMapper.selectList(wrapper);
        AssertUtils.isTrue(CollectionUtils.isEmpty(ecmDocrightDefList), "该用户未配置资料权限");
        //判断是否所有资料权限都为空
        checkAllDocRight(BeanUtil.copyToList(ecmDocrightDefList, EcmDocrightDefDTO.class), IcmsConstants.ONE);
    }

    /**
     * 判断具体资料权限(operationFlag：1-新增业务 0-查看业务树)
     */
    public List<String> checkAllDocRight(List<EcmDocrightDefDTO> ecmDocrightDefList, Integer operationFlag) {
        List<String> docCodeList = new ArrayList<>();
        if (CollectionUtils.isEmpty(ecmDocrightDefList)) {
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
            } else {
                docCount += 1;
                docCodeList.add(right.getDocCode());
            }
        }
        if (IcmsConstants.ONE.equals(operationFlag)) {
            AssertUtils.isTrue(docCount == docSize, "无新增权限");
        }
        return docCodeList;
    }


    /**
     * 新增业务
     */
    public List<EcmBusiInfo> checkPremissByBusi(EcmStructureTreeDTO ecmStructureTreeDTO, AccountTokenExtendDTO token) {
        List<EcmBusiInfo> redisDTOS = new ArrayList<>();
        List<String> busiNo = new ArrayList<>();

        //所有有权限的appcode列表,这里包含了所有的版本权限，需要过滤出指定的版本
        List<EcmDocrightDef> appCodeHaveByTokenAll = staticTreePermissService.getAppCodeHaveByTokenAll(null, token);
        Map<String, List<EcmDocrightDef>> collect = appCodeHaveByTokenAll.stream().collect(Collectors.groupingBy(EcmDocrightDef::getAppCode));
        //权限判断。
        List<String> busiIsDel = new ArrayList<>();
        List<EcmBusiInfo> ecmBusiInfoList = ecmBusiInfoMapper.selectList(new LambdaQueryWrapper<EcmBusiInfo>()
                .in(EcmBusiInfo::getBusiId, ecmStructureTreeDTO.getBusiIdList()));
        Map<Long, EcmBusiInfo> busiInfoMap = ecmBusiInfoList.stream()
                .collect(Collectors.toMap(
                        EcmBusiInfo::getBusiId,
                        Function.identity()
                ));
        Map<Long, List<EcmAppAttrDTO>> ecmAppAttrMap = busiCacheService.getAppAttrExtends(ecmStructureTreeDTO.getBusiIdList());
        for (Long id : ecmStructureTreeDTO.getBusiIdList()) {
//            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, id);
            EcmBusiInfo ecmBusiInfo = busiInfoMap.get(id);
            if (ecmBusiInfo == null){
                continue;
            }
            String appCode = ecmBusiInfo.getAppCode();
            List<EcmAppAttrDTO> appAttrDTOList = ecmAppAttrMap.get(id).stream().filter(item -> appCode.equalsIgnoreCase(item.getAppCode())).collect(Collectors.toList());
            checkCloseAccountPermission(appAttrDTOList, token, true);
            if (StateConstants.YES.equals(ecmBusiInfo.getIsDeleted())) {
                busiIsDel.add(ecmBusiInfo.getBusiNo());
                continue;
            }
            if (IcmsConstants.DYNAMIC_TREE.equals(ecmBusiInfo.getTreeType())) {
                //动态树
                //当动态树时，用户没有关联动态树功能时，提示暂无权限
                Boolean vtreeFromPcByRole = busiCacheService.getVtreeFromPcByRole(token.getId(), ecmStructureTreeDTO.getIsShow());
                if (!vtreeFromPcByRole) {
                    //没有权限
                    busiNo.add(ecmBusiInfo.getBusiNo());
                }
            } else {
                List<EcmDocrightDef> defs = collect.get(ecmBusiInfo.getAppCode());
                if (CollectionUtils.isEmpty(defs)) {
                    busiNo.add(ecmBusiInfo.getBusiNo());
                    continue;
                }

                Map<Integer, List<EcmDocrightDef>> collect1 = defs.stream().collect(Collectors.groupingBy(EcmDocrightDef::getRightVer));
                //当前业务所拥有的权限
                List<EcmDocrightDef> defs1 = collect1.get(ecmBusiInfo.getRightVer());
                if (CollectionUtils.isEmpty(defs1)) {
                    busiNo.add(ecmBusiInfo.getBusiNo());
                    continue;
                }
                List<String> collect2 = defs1.stream().map(EcmDocrightDef::getAppCode).collect(Collectors.toList());
                //静态树
                if (!collect2.contains(ecmBusiInfo.getAppCode())) {
                    busiNo.add(ecmBusiInfo.getBusiNo());
                }
            }
            redisDTOS.add(ecmBusiInfo);
        }
        if (!CollectionUtils.isEmpty(busiIsDel)) {
            AssertUtils.notNull(busiIsDel, "业务【" + String.join(",", busiIsDel) + "】已在回收站，无法打开");
        }
        if (!CollectionUtils.isEmpty(busiNo)) {
            AssertUtils.notNull(busiNo, "暂无【" + String.join(",", busiNo) + "】业务权限");
        }


        return redisDTOS;
    }

    /**
     * 获取业务列表
     */
    public UserBusiRedisDTO getBusiIdsByFlagId(AccountTokenExtendDTO token) {
        if (token.isOut()) {
            UserBusiRedisDTO userBusiRedisDTO = busiCacheService.getUserPageRedis(token.getFlagId(), token);
            if (userBusiRedisDTO != null) {
                if (CollectionUtil.isNotEmpty(userBusiRedisDTO.getBusiId())) {
                    UserBusiRedisDTO userBusiRedisDTO1 = new UserBusiRedisDTO();
                    userBusiRedisDTO1.setBusiId(userBusiRedisDTO.getBusiId());
                    userBusiRedisDTO1.setIsShow(userBusiRedisDTO.getIsShow());
                    userBusiRedisDTO1.setFlagId(token.getFlagId());
                    return userBusiRedisDTO1;
                }
            }
        }
        return null;
    }

    /**
     * 获取当前采集页面业务结构树里所展示的业务类型列表
     */
    public List<EcmAppDef> getAppTypeByCapture(AccountTokenExtendDTO token) {
        //获取当前用户影像收集关联的业务类型id和业务id todo
        UserBusiRedisDTO userBusiEntity = busiCacheService.getUserPageRedis(token.getId().toString(), token);
        List<String> appTypeIds = userBusiEntity.getAppType();
        if (CollectionUtils.isEmpty(appTypeIds)) {
            return Collections.emptyList();
        }
        List<EcmAppDef> appDefs = ecmAppDefMapper.selectList(new LambdaQueryWrapper<EcmAppDef>()
                .select(EcmAppDef::getAppCode, EcmAppDef::getAppName)
                .in(EcmAppDef::getAppCode, appTypeIds));
        return appDefs;
    }

    /**
     * 获取业务类型的业务属性列表
     */
    public Map getAppAttrList(String appCode, Long busiId, AccountTokenExtendDTO token) {
        AssertUtils.isNull(appCode, "参数错误");
        Map map = new HashMap();
        List<EcmAppAttr> appAttrs = ecmAppAttrMapper.selectList(new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, appCode));
        if (CollectionUtils.isEmpty(appAttrs)) {
            return Collections.emptyMap();
        }
        List<EcmAppAttrDTO> ecmAppAttrDTOS = PageCopyListUtils.copyListProperties(appAttrs, EcmAppAttrDTO.class);
        ecmAppAttrDTOS = ecmAppAttrDTOS.stream().sorted(Comparator.comparing(EcmAppAttrDTO::getAttrSort)).collect(Collectors.toList());
        //获取业务属性值
        getBusiAttrValue(ecmAppAttrDTOS, busiId);
        //主属性排到第一个位置
        Collections.sort(ecmAppAttrDTOS, Comparator.comparingInt(EcmAppAttrDTO::getIsKey).reversed());
        map.put("attrList", ecmAppAttrDTOS);
        List<SearchVO> list = new ArrayList<>();
        for (EcmAppAttr ecmAppAttr : appAttrs) {
            SearchVO vo = new SearchVO();
            vo.setCode(ecmAppAttr.getAttrCode());
            vo.setLabel(ecmAppAttr.getAttrName());
            if (BusiInfoConstants.DATE_TYPE.equals(ecmAppAttr.getInputType())) {
                vo.setType("date");
                vo.setPlaceholder("请选择" + ecmAppAttr.getAttrName());
            } else if (BusiInfoConstants.SELECT_TYPE.equals(ecmAppAttr.getInputType())) {
                String selectNodes = ecmAppAttr.getListValue();
                String[] split = selectNodes.split(";");
                List<SearchOptionVO> voList = new ArrayList<>();
                for (String spli : split) {
                    SearchOptionVO optionVo = new SearchOptionVO();
                    optionVo.setLabel(spli);
                    optionVo.setCode(spli);
                    optionVo.setValue(spli);
                    voList.add(optionVo);
                }
                vo.setType("select");
                vo.setPlaceholder("请选择" + ecmAppAttr.getAttrName());
                vo.setOption(voList);
            } else {
                vo.setPlaceholder("请输入" + ecmAppAttr.getAttrName());
            }
            vo.setIsNull(ecmAppAttr.getIsNull());
            vo.setAttrFlag(true);
            vo.setAppAttrId(ecmAppAttr.getAppAttrId());
            vo.setIsKey(ecmAppAttr.getIsKey());
            vo.setRegex(ecmAppAttr.getRegex());
            vo.setAttrSort(ecmAppAttr.getAttrSort());
            vo.setIsArchived(ecmAppAttr.getIsArchived());
            getDefaultKeyValue(token, ecmAppAttr, appCode, vo);
            list.add(vo);
        }
        //按照属性顺序排序
        list = list.stream().sorted(Comparator.comparing(SearchVO::getAttrSort)).collect(Collectors.toList());
        //主属性排到第一个位置
        Collections.sort(list, Comparator.comparingInt(SearchVO::getIsKey).reversed());
        map.put("tableList", list);
        return map;
    }
    /**
     * 业务树自动生成主索引流水号：当前登录用户所属机构+用户选择的业务类型所属父节点代码+当前操作日期YYYYMMDD+五位随机数字
     * @param token
     * @param ecmAppAttr
     * @param appCode
     * @param vo
     */
    private void getDefaultKeyValue(AccountTokenExtendDTO token, EcmAppAttr ecmAppAttr, String appCode, SearchVO vo) {
        if(token !=null && StateConstants.COMMON_ONE.equals(ecmAppAttr.getIsKey())) {
            String defaultKeyValue = getBusiIndexDefault(token, appCode);
            vo.setKeyValue(defaultKeyValue);
        }
    }

    private String getBusiIndexDefault(AccountTokenExtendDTO token, String appCode) {
        String defaultKeyValue = "";
        try {
            // 查询用户选择的业务类型所属父节点代码
            LambdaQueryWrapper<EcmAppDef> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(EcmAppDef::getAppCode, appCode);
            EcmAppDef ecmAppDef = ecmAppDefMapper.selectOne(queryWrapper);
            StringBuffer buffer = new StringBuffer();
            buffer.append(RedisConstants.DEFAULT_BUSI_NO_PREFIX_KEY);
            String orgCode = token.getOrgCode();
            buffer.append(orgCode);
            String parentAppCode = ecmAppDef.getParent();
            if (StateConstants.PARENT_APP_CODE_DEFAULT.equals(ecmAppDef.getParent())) {
                parentAppCode = appCode;
            }
            buffer.append(parentAppCode);
            buffer = generateUniqueCode(buffer);
            String defaultKeyValueStr = buffer.toString();
            int index = defaultKeyValueStr.indexOf(RedisConstants.DEFAULT_BUSI_NO_PREFIX_KEY);
            defaultKeyValue = defaultKeyValueStr.substring(index + RedisConstants.DEFAULT_BUSI_NO_PREFIX_KEY.length());
        } catch (Exception e) {
            log.error("获取默认业务主索引失败", e);
        }
        return defaultKeyValue;
    }

    private StringBuffer generateUniqueCode(StringBuffer prefix) {
        String date = sdf.format(new Date());
        prefix.append(date);
        String code = null;
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            long count = redisUtils.incr(prefix.toString(), EXPIRE_TIME);
            code = String.format("%0" + NUMBER_LENGTH + "d", count);
        } finally {
            lock.unlock();
        }
        return prefix.append(code);
    }
    /**
     * 业务属性值
     */
    private void getBusiAttrValue(List<EcmAppAttrDTO> ecmAppAttrDTOS, Long busiId) {
        if (CollectionUtils.isNotEmpty(ecmAppAttrDTOS)) {
            for (EcmAppAttrDTO ecmAppAttrDTO : ecmAppAttrDTOS) {
                List<EcmBusiMetadata> ecmBusiMetadata = ecmBusiMetadataMapper.selectList(new LambdaQueryWrapper<EcmBusiMetadata>().eq(EcmBusiMetadata::getAppAttrId, ecmAppAttrDTO.getAppAttrId()).eq(EcmBusiMetadata::getBusiId, busiId));
                ecmAppAttrDTO.setAppAttrValue("");
                if (CollectionUtils.isNotEmpty(ecmBusiMetadata)) {
                    ecmAppAttrDTO.setAppAttrValue(ecmBusiMetadata.get(0).getAppAttrVal());
                }
            }
        }
    }

    /**
     * 获取业务结构树
     */
    public List<EcmBusiStructureTreeDTO> getBusiStructureTree(String userId, EcmStructureTreeDTO ecmStructureTreeDTO, AccountTokenExtendDTO token) {
        //静态树
        Integer treeType = IcmsConstants.STATIC_TREE;
        //业务id集合
        List<Long> busiIdList = ecmStructureTreeDTO.getBusiIdList();
        //token.isOut() 是否是第三方跳转过来的,默认否
        if(IcmsConstants.ONE == ecmStructureTreeDTO.getIsClassify()) {
            busiIdList = getBusiIdListByOut(ecmStructureTreeDTO, token, busiIdList);
        }
        //查询有效业务
        if (!CollectionUtils.isEmpty(busiIdList)) {
            LambdaQueryWrapper<EcmBusiInfo> queryWrapper = new LambdaQueryWrapper<>();
            List<EcmBusiInfo> ecmBusiInfos = ecmBusiInfoMapper.selectList(queryWrapper.in(EcmBusiInfo::getBusiId, busiIdList));
            List<Long> collect = ecmBusiInfos.stream().map(EcmBusiInfo::getBusiId).collect(Collectors.toList());
            busiIdList = busiIdList.stream().filter(s->collect.contains(s)).collect(Collectors.toList());
        }
        List<EcmBusiStructureTreeDTO> treeExtendList = new ArrayList<>();
        //新增没有输入主索引值 空树
        if (CollectionUtil.isEmpty(busiIdList)) {
            //新建第一次
            List<EcmBusiStructureTreeDTO> emptyList = getEcmBusiStructureTreeDTOS(ecmStructureTreeDTO, token, treeExtendList, treeType);
            if (emptyList != null) {
                return emptyList;
            }
        } else {
            //更新
            List<EcmBusiStructureTreeDTO> treeExtendList1 = getEcmBusiStructureTreeDTOS(ecmStructureTreeDTO, token, treeExtendList, treeType, busiIdList);
            if (treeExtendList1 != null) {
                return treeExtendList1;
            }
        }
        generateaName(treeExtendList);
        return treeExtendList;
    }

    /**
     * 业务id集合
     */
    private List<Long> getBusiIdListByOut(EcmStructureTreeDTO ecmStructureTreeDTO, AccountTokenExtendDTO token, List<Long> busiIdList) {
        String flagId = null;
        if (token.getFlagId() != null) {
            flagId = token.getFlagId();
        } else {
            flagId = ecmStructureTreeDTO.getPageFlag();
            token.setFlagId(flagId);
        }
        if (!CollectionUtils.isEmpty(ecmStructureTreeDTO.getBusiIdList())) {
            return ecmStructureTreeDTO.getBusiIdList();
        }
        UserBusiRedisDTO userBusiRedisDTO = busiCacheService.getUserPageRedis(flagId, token);
        if (userBusiRedisDTO != null) {
            if (CollectionUtil.isNotEmpty(userBusiRedisDTO.getBusiId())) {
                ecmStructureTreeDTO.setIsShow(userBusiRedisDTO.getIsShow());
                if (CollectionUtils.isEmpty(busiIdList)) {
                    ecmStructureTreeDTO.setBusiIdList(userBusiRedisDTO.getBusiId());
                    busiIdList = userBusiRedisDTO.getBusiId();
                }
            }
        }

        return busiIdList;
    }

    /**
     * 名字生成
     */
    private void generateaName(List<EcmBusiStructureTreeDTO> ecmDocTreeDTOS) {
        for (EcmBusiStructureTreeDTO ecmDocTreeDTO : ecmDocTreeDTOS) {
            switch (ecmDocTreeDTO.getType()) {
                case 1:
                    //业务类型
                    ecmDocTreeDTO.setName("(" + ecmDocTreeDTO.getAppCode() + ")" + ecmDocTreeDTO.getName());
                    ecmDocTreeDTO.setAppTypeName(ecmDocTreeDTO.getName());
                    break;
                case 2:
                    //业务类型
                    ecmDocTreeDTO.setAppTypeName("(" + ecmDocTreeDTO.getAppCode() + ")" + ecmDocTreeDTO.getAppTypeName());
                    break;
                case 3:
                    //资料
                    ecmDocTreeDTO.setName("(" + ecmDocTreeDTO.getDocCode() + ")" + ecmDocTreeDTO.getName());
                    break;
                default:
                    break;
            }
            if (!CollectionUtils.isEmpty(ecmDocTreeDTO.getChildren())) {
                generateaName(ecmDocTreeDTO.getChildren());
            }
        }
    }

    /**
     * 获取业务机构集合
     */
    private List<EcmBusiStructureTreeDTO> getEcmBusiStructureTreeDTOS(EcmStructureTreeDTO ecmStructureTreeDTO, AccountTokenExtendDTO token,
                                                                      List<EcmBusiStructureTreeDTO> treeExtendList, Integer treeType, List<Long> busiIdList) {
        List<EcmBusiInfoRedisDTO> ecmBusiInfoRedisDTOList = new ArrayList<>();
        List<EcmBusiInfoRedisDTO> nowEcmBusiInfoRedisDTOList = new ArrayList<>();

        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = new EcmBusiInfoRedisDTO();
        //去重
        Set<String> apps = new HashSet<>();
        busiIdList = busiIdList.stream().distinct().collect(Collectors.toList());
        //是否加密
        Result<SysParamDTO> result = paramApi.searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString());
        SysParamDTO data = result.getData();
        String value = data.getValue();
        SysStrategyVO sysStrategyVO = JSONObject.parseObject(value, SysStrategyVO.class);
        Integer isEncrypt = IcmsConstants.NO_ENCRYPT;
        List<String> appCodes = new ArrayList<>();
        List<EcmBusiInfoRedisDTO> ecmBusiInfoRedisDTOListTemp = new ArrayList<>();
        //有业务数据
        for (Long busiId : busiIdList) {
            ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, busiId);
            checkCloseAccountPermission(ecmBusiInfoRedisDTO.getAttrList(), token, true);
            //设置
            if (!ObjectUtils.isEmpty(ecmBusiInfoRedisDTO)) {
                //压缩换实时数据
                String appCode = ecmBusiInfoRedisDTO.getAppCode();
                appCodes.add(appCode);
                ecmBusiInfoRedisDTO.setTreeDataFlag(true);
                ecmBusiInfoRedisDTOListTemp.add(ecmBusiInfoRedisDTO);
                apps.add(appCode);
            }
            if (CollectionUtils.isEmpty(ecmBusiInfoRedisDTOListTemp)) {
                return treeExtendList;
            }
        }

        List<EcmAppDef> ecmAppDefs1 = ecmAppDefMapper.selectList(new LambdaQueryWrapper<EcmAppDef>().in(EcmAppDef::getAppCode, appCodes));
        Map<String, List<EcmAppDef>> collect1 = ecmAppDefs1.stream().collect(Collectors.groupingBy(EcmAppDef::getAppCode));
        for(EcmBusiInfoRedisDTO s:ecmBusiInfoRedisDTOListTemp){
            List<EcmAppDef> ecmBusiInfoRedisDTOS = collect1.get(s.getAppCode());
            if(CollectionUtils.isNotEmpty(ecmBusiInfoRedisDTOS)){
                EcmAppDef d = ecmBusiInfoRedisDTOS.get(0);
                s.setResiz(d.getResize());
                s.setIsQulity(d.getIsResize());
                s.setQulity(d.getQulity());
            }
            ecmBusiInfoRedisDTOList.add(s);
            if (ecmStructureTreeDTO.getNowBusiIdList()!=null && ecmStructureTreeDTO.getNowBusiIdList().contains(s.getBusiId())){
                nowEcmBusiInfoRedisDTOList.add(s);
            }
        }

        //无业务数据，只有业务类型空壳    新增时有 ，编辑时没有
        if (StrUtil.isNotBlank(ecmStructureTreeDTO.getPageFlag())) {
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO1 = new EcmBusiInfoRedisDTO();
//            //从缓存里面拿当前用户需要采集的业务数据
            //从缓存里面拿当前用户需要采集的业务数据
            UserBusiRedisDTO userBusiData = busiCacheService.getUserPageRedis(ecmStructureTreeDTO.getPageFlag(), token);
            List<String> appTypeIdList = userBusiData.getAppType();
            if (CollectionUtils.isEmpty(appTypeIdList)) {
                return Collections.emptyList();
            }
            String appCode = appTypeIdList.get(0);
            EcmAppDef ecmAppDef = ecmAppDefMapper.selectById(appCode);
            BeanUtils.copyProperties(ecmAppDef, ecmBusiInfoRedisDTO1);
            ecmBusiInfoRedisDTO1.setAppTypeName(ecmAppDef.getAppName());
            ecmBusiInfoRedisDTO1.setEquipmentId(ecmAppDef.getEquipmentId());
//            //只有业务类型空壳，无数据
            ecmBusiInfoRedisDTO1.setTreeDataFlag(false);
            ecmBusiInfoRedisDTOList.add(ecmBusiInfoRedisDTO1);
        }

        //同一笔业务类型去重
        List<EcmBusiInfoRedisDTO> distinctUsers = new ArrayList<>();
        for (EcmBusiInfoRedisDTO ecmBusiInfoRedisDTOs : ecmBusiInfoRedisDTOList) {
            if (!distinctUsers.contains(ecmBusiInfoRedisDTOs)) {
                distinctUsers.add(ecmBusiInfoRedisDTOs);
            }
        }
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectBatchIds(apps);
        Map<String, List<EcmAppDef>> collect = ecmAppDefs.stream().sorted(Comparator.comparing(EcmAppDef::getAppSort).reversed()).collect(Collectors.groupingBy(EcmAppDef::getAppCode));
//
        EcmBusiInfoRedisDTO finalEcmBusiInfoRedisDTO = ecmBusiInfoRedisDTO;

        if (sysStrategyVO.getEncryptStatus()) {
            isEncrypt = IcmsConstants.YES_ENCRYPT;
        }
        //添加是否偏离矫正
        Boolean isFlat = searchIsFlat(StrategyConstantsEnum.OCR_STRATEGY.toString());
        for (EcmBusiInfoRedisDTO s : distinctUsers) {
            //判断是否只有业务类型，无资料节点
            if (CollectionUtil.isEmpty(s.getEcmBusiDocRedisDTOS())) {
                EcmBusiStructureTreeDTO ecmBusiStructureTreeDTO = BeanUtil.copyProperties(s, EcmBusiStructureTreeDTO.class);
                ecmBusiStructureTreeDTO.setChildren(new ArrayList<>());
                ecmBusiStructureTreeDTO.setType(IcmsConstants.TREE_TYPE_APPCODE);
                ecmBusiStructureTreeDTO.setId(s.getAppCode());
                ecmBusiStructureTreeDTO.setHaveNodeData(false);
                ecmBusiStructureTreeDTO.setIsFlat(isFlat);
                treeExtendList.add(ecmBusiStructureTreeDTO);
                continue;
            }
            //添加业务类型节点
            EcmBusiStructureTreeDTO appTypeNode = new EcmBusiStructureTreeDTO();
            appTypeNode.setAppCode(s.getAppCode());
            appTypeNode.setId(s.getAppCode());
            appTypeNode.setType(IcmsConstants.TREE_TYPE_APPCODE);
            appTypeNode.setName(s.getAppTypeName());
            appTypeNode.setAppTypeName(s.getAppTypeName());
            appTypeNode.setPid(StateConstants.ZERO.toString());
            if (!CollectionUtils.isEmpty(collect.get(s.getAppCode()))) {
                appTypeNode.setEquipmentId(collect.get(s.getAppCode()).get(0).getEquipmentId());
                appTypeNode.setName(collect.get(s.getAppCode()).get(0).getAppName());
                appTypeNode.setAppTypeName(collect.get(s.getAppCode()).get(0).getAppName());
            }
            //统计业务类型下文件数量a
            appTypeNode.setFileCount(StateConstants.ZERO);
            appTypeNode.setTreeType(treeType);
            appTypeNode.setModelType(IcmsConstants.ONE);
            appTypeNode.setCreateUser(finalEcmBusiInfoRedisDTO.getCreateUser());
            appTypeNode.setCreateTime(finalEcmBusiInfoRedisDTO.getCreateTime());
            appTypeNode.setIsFlat(isFlat);
            appTypeNode.setHaveNodeData(true);
            appTypeNode.setIsEncrypt(isEncrypt);
            appTypeNode.setEquipmentId(!ObjectUtils.isEmpty(s.getEquipmentId()) ? s.getEquipmentId() : null);
            appTypeNode.setStatus(StringUtils.isEmpty(s.getStatus())?BusiInfoConstants.BUSI_STATUS_ZERO:s.getStatus());
            treeExtendList.add(appTypeNode);
        }

        //上面代码是把业务类型加进去了，没有统计业务数量
        //构建树
        if (IcmsConstants.ONE.equals(ecmStructureTreeDTO.getIsShowAll())){
            getTreeNodeWithoutDetails(treeExtendList, busiIdList, token,isEncrypt, isFlat);
            if (!nowEcmBusiInfoRedisDTOList.isEmpty() && nowEcmBusiInfoRedisDTOList.size() > 0) {
                getTreeNode(token, treeExtendList, nowEcmBusiInfoRedisDTOList, treeType, collect, ecmStructureTreeDTO.getIsShow(), isEncrypt, isFlat, ecmStructureTreeDTO.getDocCode());
            }
        }else if (ObjectUtils.isEmpty(ecmStructureTreeDTO.getIsShowAll())){
            getTreeNode(token, treeExtendList, ecmBusiInfoRedisDTOList, treeType, collect, ecmStructureTreeDTO.getIsShow(), isEncrypt, isFlat, ecmStructureTreeDTO.getDocCode());
        }
        return treeExtendList;
    }

    private void getTreeNodeWithoutDetails(List<EcmBusiStructureTreeDTO> treeExtendList, List<Long> busiIdList, AccountTokenExtendDTO token, Integer isEncrypt, Boolean isFlat) {
        //业务信息
        List<EcmBusiInfo> ecmBusiInfos = ecmBusiInfoMapper.selectList(new LambdaQueryWrapper<EcmBusiInfo>()
                .in(EcmBusiInfo::getBusiId, busiIdList));
        Map<Long, List<EcmBusiInfo>> busiInfoMaps = ecmBusiInfos.stream().collect(Collectors.groupingBy(EcmBusiInfo::getBusiId));
        Map<String, List<EcmBusiInfo>> appCodeMaps  = ecmBusiInfos.stream().collect(Collectors.groupingBy(EcmBusiInfo::getAppCode));
        //文件信息
        List<EcmFileInfo> ecmFileInfos = ecmFileInfoMapper.selectList(new LambdaQueryWrapper<EcmFileInfo>()
                .in(EcmFileInfo::getBusiId, busiIdList)
                .eq(EcmFileInfo::getState, IcmsConstants.ZERO));
        Map<Long, List<EcmFileInfo>> fileInfoMaps = ecmFileInfos.stream().collect(Collectors.groupingBy(EcmFileInfo::getBusiId));

        for (EcmBusiStructureTreeDTO ecmBusiStructureTreeDTO : treeExtendList) {
            List<EcmBusiStructureTreeDTO> list = new ArrayList<>();
            String appCode = ecmBusiStructureTreeDTO.getAppCode();
            //根据appcode可以查到业务信息
            List<EcmBusiInfo> ecmBusiInfoList = appCodeMaps.get(appCode);
            //业务类型文件数量
            ecmBusiStructureTreeDTO.setFileCount(ecmBusiInfoList.size());

            for (EcmBusiInfo ecmBusiInfo : ecmBusiInfoList) {
                EcmBusiStructureTreeDTO busiNode = new EcmBusiStructureTreeDTO();
                busiNode.setId(ecmBusiInfo.getBusiId().toString());
                busiNode.setAppCode(ecmBusiInfo.getAppCode());
                busiNode.setName(ecmBusiInfo.getBusiNo());
                busiNode.setType(IcmsConstants.TREE_TYPE_BUSI);
                busiNode.setBusiNo(ecmBusiInfo.getBusiNo());
                busiNode.setAppTypeName(ecmBusiStructureTreeDTO.getAppTypeName());
                busiNode.setPid(ecmBusiStructureTreeDTO.getId());
                busiNode.setPName(ecmBusiStructureTreeDTO.getName());
                busiNode.setBusiId(ecmBusiInfo.getBusiId());
                busiNode.setTreeType(ecmBusiInfo.getTreeType());
                busiNode.setIsResize(ecmBusiStructureTreeDTO.getIsQulity());
                busiNode.setResize(ecmBusiStructureTreeDTO.getResize());
                busiNode.setQulity(ecmBusiStructureTreeDTO.getQulity());
                busiNode.setCreateUser(ecmBusiInfo.getCreateUser());
                busiNode.setCreateTime(ecmBusiInfo.getCreateTime());
                busiNode.setEquipmentId(ecmBusiStructureTreeDTO.getEquipmentId());
                busiNode.setIsFlat(isFlat);
                busiNode.setMd5List(new ArrayList<>());
                busiNode.setIsEncrypt(isEncrypt);
                busiNode.setIsParent(StateConstants.ZERO);
                busiNode.setStatus(StringUtils.isEmpty(ecmBusiStructureTreeDTO.getStatus())?BusiInfoConstants.BUSI_STATUS_ZERO:ecmBusiStructureTreeDTO.getStatus());
                //业务节点返回所有数据操作权限（和未归类权限一致）
                busiNode.setDocRight(addUnclassifyNodeDocRight());
                //文件信息
                List<EcmFileInfo> ecmFileInfoList = fileInfoMaps.get(ecmBusiInfo.getBusiId());
                busiNode.setFileCount(ecmFileInfoList != null && !ecmFileInfoList.isEmpty() ? ecmFileInfoList.size() : IcmsConstants.ZERO );
                list.add(busiNode);
            }

            ecmBusiStructureTreeDTO.setChildren(list);

        }

    }

    //构建树
    private void getTreeNode(AccountTokenExtendDTO token, List<EcmBusiStructureTreeDTO> appCodeTreeExtendList, List<EcmBusiInfoRedisDTO> ecmBusiInfoRedisDTOList, Integer treeType, Map<String, List<EcmAppDef>> collect, Integer show, Integer isEncrypt, Boolean isFlat, String docCodeDestroy) {
        //分组
        Map<String, List<EcmBusiInfoRedisDTO>> groupedByAppType = ecmBusiInfoRedisDTOList.stream().filter(s -> s.getAppCode() != null).collect(Collectors.groupingBy(EcmBusiInfoRedisDTO::getAppCode));

        //获取资料节点的加密情况
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectList(null);
        Map<String, List<EcmDocDef>> docMaps = ecmDocDefs.stream().collect(Collectors.groupingBy(EcmDocDef::getDocCode));


        //获取压缩配置
        //构建树
        for (EcmBusiStructureTreeDTO appTypeNode : appCodeTreeExtendList) {
            if (!appTypeNode.getHaveNodeData()) {
                continue;
            }
            List<EcmBusiStructureTreeDTO> children = appTypeNode.getChildren();
            if (children == null || children.isEmpty()) {
                children = new ArrayList<>();
            }
            Map<Long, List<EcmBusiStructureTreeDTO>> listMap = children.stream().collect(Collectors.groupingBy(EcmBusiStructureTreeDTO::getBusiId));

            EcmAppDef ecmAppDef = collect.get(appTypeNode.getAppCode()).get(0);

            //这是一笔业务
            List<EcmBusiInfoRedisDTO> busiBranchs = groupedByAppType.get(appTypeNode.getId());
            if (!CollectionUtils.isEmpty(busiBranchs)) {

//                List<EcmBusiStructureTreeDTO> busiChildren = new ArrayList<>();
                for (EcmBusiInfoRedisDTO busiBranch : busiBranchs) {
                    treeType = busiBranch.getTreeType();
                    busiBranch.setEquipmentId(ecmAppDef.getEquipmentId());
                    if (CollectionUtil.isEmpty(busiBranch.getEcmBusiDocRedisDTOS()) && busiBranch.getTreeDataFlag().equals(false)) {
                        continue;
                    }
                    EcmBusiStructureTreeDTO busiNode = new EcmBusiStructureTreeDTO();
                    Integer index = -1;
                    if (!listMap.isEmpty() && listMap.containsKey(busiBranch.getBusiId())){
                        busiNode=listMap.get(busiBranch.getBusiId()).get(0);
                        index = children.indexOf(listMap.get(busiBranch.getBusiId()).get(0));
                    }
                    busiNode.setAppCode(busiBranch.getAppCode());
                    busiNode.setId(busiBranch.getBusiId().toString());
                    busiNode.setType(IcmsConstants.TREE_TYPE_BUSI);
                    busiNode.setName(busiBranch.getBusiNo());
                    busiNode.setAttrList(CollectionUtils.isEmpty(busiBranch.getAttrList()) ? new ArrayList<>() : busiBranch.getAttrList());
                    //主属性排到第一个位置
                    if (!ObjectUtils.isEmpty(busiBranch.getAttrList())) {
                        for (EcmAppAttrDTO s : busiBranch.getAttrList()) {
                            if (s != null && s.getIsKey() == null) {
                                s.setIsKey(StateConstants.ZERO);
                            }
                        }
                        Collections.sort(busiBranch.getAttrList(), Comparator.comparingInt(EcmAppAttrDTO::getIsKey).reversed());
                    }
                    busiNode.setBusiNo(busiBranch.getBusiNo());
                    busiNode.setAppTypeName(busiBranch.getAppTypeName());
                    busiNode.setPid(appTypeNode.getId());
                    busiNode.setPName(appTypeNode.getName());
                    busiNode.setBusiId(busiBranch.getBusiId());
                    busiNode.setTreeType(busiBranch.getTreeType());
                    busiNode.setIsResize(busiBranch.getIsQulity());
                    busiNode.setResize(busiBranch.getResiz());
                    busiNode.setQulity(busiBranch.getQulity());
                    busiNode.setCreateUser(busiBranch.getCreateUser());
                    busiNode.setCreateTime(appTypeNode.getCreateTime());
                    busiNode.setEquipmentId(ecmAppDef.getEquipmentId());
                    busiNode.setIsFlat(isFlat);
                    busiNode.setMd5List(new ArrayList<>());
                    busiNode.setIsEncrypt(isEncrypt);
                    busiNode.setIsParent(StateConstants.ZERO);
                    busiNode.setStatus(StringUtils.isEmpty(busiBranch.getStatus())?BusiInfoConstants.BUSI_STATUS_ZERO:busiBranch.getStatus());
                    //业务节点返回所有数据操作权限（和未归类权限一致）
                    busiNode.setDocRight(addUnclassifyNodeDocRight());
                    //设置业务树类型
                    busiNode.setTreeType(busiBranch.getTreeType());
                    busiNode.setCreateUserName(busiBranch.getCreateUserName());
                    List<EcmBusiStructureTreeDTO> ecmBusiStructureTreeDTOS = new ArrayList<>();
                    Map<String, List<FileInfoRedisDTO>> groupedFileByDocId = new HashMap<>();
                    Map<Long, List<FileInfoRedisDTO>> groupedFileByMark = new HashMap<>();
                    List<FileInfoRedisDTO> delListFile = new ArrayList<>();
                    Map<String, List<EcmBusiDoc>> groupedByDocMark = new HashMap<>();
                    Map<String, List<EcmDocrightDefDTO>> groupedByDocRight = new HashMap<>();
                    Map<String, List<EcmBusiDocRedisDTO>> collect1 = null;
                    List<FileInfoRedisDTO> unList = new ArrayList<>();
//                    if (busiBranch.getTotalFileSize() < maxFileSize) {
                    //小数据量走redis统计文件数量
                    List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(busiBranch.getBusiId());
                    if (CollectionUtils.isEmpty(fileInfoRedis)) {
                        //统计业务下文件数量
                        busiNode.setFileCount(StateConstants.ZERO);
                    } else {
                        //为删除的文件
                        List<FileInfoRedisDTO> fileInfoRedisEntities = fileInfoRedis;
                        List<FileInfoRedisDTO> docList = new ArrayList<>();
                        List<FileInfoRedisDTO> markList = new ArrayList<>();
                        List<FileInfoRedisDTO> nodel = new ArrayList<>();
                        for (FileInfoRedisDTO p : fileInfoRedisEntities) {
                            if (IcmsConstants.ZERO.equals(p.getState())) {
                                nodel.add(p);
                                if (p.getDocCode() != null) {
                                    if (IcmsConstants.UNCLASSIFIED_ID.equals(p.getDocCode())) {
                                        unList.add(p);
                                    } else if (p.getMarkDocId() != null) {
                                        markList.add(p);
                                    }
                                    docList.add(p);

                                }
                            } else {
                                delListFile.add(p);
                            }

                        }
                        busiNode.setFileCount(nodel.size());
                        groupedFileByDocId = docList.stream().collect(Collectors.groupingBy(EcmFileInfoDTO::getDocCode));
                        groupedFileByMark = markList.stream().collect(Collectors.groupingBy(EcmFileInfoDTO::getMarkDocId));
                    }
//                    }

                    if (!CollectionUtils.isEmpty(busiBranch.getEcmBusiDocs())) {
                        List<EcmBusiDoc> ecmBusiDocs = busiBranch.getEcmBusiDocs();
                        groupedByDocMark = ecmBusiDocs.stream().sorted(Comparator.comparing(EcmBusiDoc::getDocSort)).collect(Collectors.groupingBy(EcmBusiDoc::getDocCode));
                    }

                    //获取资料权限 并集
                    //获取版本权限
                    List<EcmDocrightDefDTO> currentDocRight = busiCacheService.getDocrightDefCommon(busiBranch, token);
                    if (!CollectionUtils.isEmpty(currentDocRight)) {
                        groupedByDocRight = currentDocRight.stream().collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
                    }
                    //设置资料节点加密
//                    setDocEnrcypt(treeType, collect1, busiBranch);
                    //获取资料节点
                    List<EcmBusiStructureTreeDTO> docTypeNode = getDocTypeNode(ecmBusiStructureTreeDTOS,
                            busiBranch.getEcmBusiDocRedisDTOS(), groupedFileByDocId,
                            busiBranch, groupedByDocMark, treeType, groupedByDocRight,
                            isFlat, isEncrypt, groupedFileByMark, docMaps, token, collect1);
                    Set<String> docCode = new HashSet<>();

                    docCode.add(IcmsConstants.UNCLASSIFIED_ID);
                    //去掉没有子节点的父节点
                    filterTree(docTypeNode, null, docCode);
                    //如果对外接口要求显示指定的节点，则这里需要将其他节点过滤掉
                    UserBusiRedisDTO userBusiData = busiCacheService.getUserPageRedis(token.getFlagId(), token);
                    if (userBusiData.getDocCodeShow() != null) {
                        Map<Long, List<String>> docCodeShow = userBusiData.getDocCodeShow();
                        List<String> strings = docCodeShow.get(busiBranch.getBusiId());
                        if (CollectionUtil.isNotEmpty(strings)) {
                            docTypeNode = CommonUtils.isshowNode(strings, docTypeNode);
                        }
                    }
                    //隐藏节点的特殊处理
                    if (IcmsConstants.NODETYPPE_NOSHOW.equals(nodeType)) {
//                        //隐藏带锁的节点
                        docTypeNode = CommonUtils.removeTreeLock(docTypeNode);
                        Integer to = 0;
                        for (EcmBusiStructureTreeDTO dto : docTypeNode) {
                            to = to + dto.getFileCount();
                        }
                        to = to + (unList != null ? unList.size() : 0);
                        busiNode.setFileCount(to);
                    }


                    //添加未归类资料节点
                    if (!CollectionUtils.isEmpty(unList)) {
                        addUnclassifiedDocNode(docTypeNode, busiBranch, treeType, appTypeNode.getIsEncrypt(), unList, isFlat, collect1);
                    }
                    //添加已删除资料节点
                    addDeletedDocNode(docTypeNode, busiBranch, treeType, busiBranch.getAppCode(), token, show, delListFile);
                    //影像销毁清单中跳转过来需要过滤树
                    if(StringUtils.hasText(docCodeDestroy)){
                        docTypeNode = filterByDocCodeDestroy(docTypeNode, docCodeDestroy);
                        AssertUtils.isNull(docTypeNode, "参数错误,资料节点过滤业务树为空");
                        busiNode.setFileCount(docTypeNode.get(0).getFileCount());
                    }
                    busiNode.setChildren(docTypeNode);
                    if (index < 0) {
                        children.add(busiNode);
                    } else {
                        children.set(index, busiNode);
                    }
                }
                appTypeNode.setChildren(children);
                //添加业务总数文件数
                appTypeNode.setFileCount(appTypeNode.getChildren().size());
            }
        }
    }
    /**
     * 过滤树结构，只保留包含指定docCode的子树
     * @param docCode 要查找的资料类型id
     * @return 过滤后的树列表
     */
    public static List<EcmBusiStructureTreeDTO> filterByDocCodeDestroy(List<EcmBusiStructureTreeDTO> ecmBusiStructureTreeDTO, String docCode) {
        List<EcmBusiStructureTreeDTO> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(ecmBusiStructureTreeDTO)) {
            return result;
        }

        for (EcmBusiStructureTreeDTO node : ecmBusiStructureTreeDTO) {
            EcmBusiStructureTreeDTO filteredNode = filterNode(node, docCode);
            if (filteredNode != null) {
                result.add(filteredNode);
            }
        }
        return result;
    }

    private static EcmBusiStructureTreeDTO filterNode(EcmBusiStructureTreeDTO node, String docCode) {
        // 如果当前节点的docCode匹配，返回
        if (docCode.equals(node.getDocCode())) {
            return node;
        }
        if (CollectionUtils.isEmpty(node.getChildren())) {
            return null;
        }
        // 递归过滤子节点
        List<EcmBusiStructureTreeDTO> filteredChildren = new ArrayList<>();
        for (EcmBusiStructureTreeDTO child : node.getChildren()) {
            EcmBusiStructureTreeDTO filteredChild = filterNode(child, docCode);
            if (filteredChild != null) {
                filteredChildren.add(filteredChild);
            }
        }
        //覆盖子节点，只保留筛选出的数据
        if (!filteredChildren.isEmpty()) {
            node.setChildren(filteredChildren);
            node.setFileCount(filteredChildren.get(0).getFileCount());
            return node;
        }

        return null;
    }

    /**
     * 获取业务结构集合
     */
    private List<EcmBusiStructureTreeDTO> getEcmBusiStructureTreeDTOS(EcmStructureTreeDTO ecmStructureTreeDTO, AccountTokenExtendDTO token,
                                                                      List<EcmBusiStructureTreeDTO> treeExtendList,
                                                                      Integer treeType) {
        List<EcmBusiInfoRedisDTO> ecmBusiInfoRedisDTOList = new ArrayList<>();
        //获取用户
        SysUserDTO user = null;
        SysInstDTO inst = null;
        if (token.isOut()) {
            user = new SysUserDTO();
            user.setLoginName(token.getUsername());
            user.setName(token.getName());
            inst = new SysInstDTO();
            inst.setInstNo(token.getOrgCode());
        } else {
            Result<List<SysUserDTO>> userListByUsernames = userApi.getUserListByUsernames(new String[]{token.getUsername()});
            user = userListByUsernames.getData().get(0);
            //获取机构
            Result<SysInstDTO> instByInstId = instApi.getInstByInstId(user.getInstId());
            inst = instByInstId.getData();
        }
        //从缓存里面拿当前用户需要采集的业务数据
        UserBusiRedisDTO userBusiRedisDTO = busiCacheService.getUserPageRedis(ecmStructureTreeDTO.getPageFlag(), token);
        List<Long> busiIds = userBusiRedisDTO.getBusiId();
        List<String> appTypeIds = userBusiRedisDTO.getAppType();
        if (CollectionUtils.isEmpty(appTypeIds)) {
            return Collections.emptyList();
        }
        //添加业务类型节点
        Result<SysParamDTO> result = paramApi.searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString());
        SysParamDTO data = result.getData();
        String value = data.getValue();
        SysStrategyVO sysStrategyVO = JSONObject.parseObject(value, SysStrategyVO.class);
        Integer isEncrypt = IcmsConstants.NO_ENCRYPT;
        if (sysStrategyVO.getEncryptStatus()) {
            isEncrypt = IcmsConstants.YES_ENCRYPT;
        }
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectBatchIds(appTypeIds);
        Boolean isFlat = searchIsFlat(StrategyConstantsEnum.OCR_STRATEGY.toString());
        for (EcmAppDef ecmAppDef : ecmAppDefs) {
            EcmBusiStructureTreeDTO appTypeNode = new EcmBusiStructureTreeDTO();
            appTypeNode.setId(ecmAppDef.getAppCode());
            appTypeNode.setType(StateConstants.COMMON_ONE);
            appTypeNode.setName(ecmAppDef.getAppName());
            appTypeNode.setPid(StateConstants.ZERO.toString());
            //统计业务类型下文件数量
            appTypeNode.setFileCount(StateConstants.ZERO);
            appTypeNode.setTreeType(treeType);
            appTypeNode.setCreateUser(ecmAppDef.getCreateUser());
            appTypeNode.setCreateTime(ecmAppDef.getCreateTime());
            appTypeNode.setModelType(userBusiRedisDTO.getModelType());
            appTypeNode.setEquipmentId(ecmAppDef.getEquipmentId());
            appTypeNode.setQueueName(ecmAppDef.getQueueName());
            appTypeNode.setAppCode(ecmAppDef.getAppCode());
            appTypeNode.setIsFlat(isFlat);
            appTypeNode.setAppTypeName(ecmAppDef.getAppName());
            appTypeNode.setIsEncrypt(isEncrypt);
            appTypeNode.setEquipmentId(!ObjectUtils.isEmpty(ecmAppDef.getEquipmentId()) ? ecmAppDef.getEquipmentId() : null);
            appTypeNode.setStatus(BusiInfoConstants.BUSI_STATUS_ZERO);
            treeExtendList.add(appTypeNode);
        }
        if (CollectionUtils.isEmpty(busiIds)) {
            return treeExtendList;
        }
        for (Long busiId2 : busiIds) {
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, busiId2);
            if (!ObjectUtils.isEmpty(ecmBusiInfoRedisDTO)) {
                //压缩换实时数据
                String appCode = ecmBusiInfoRedisDTO.getAppCode();
                EcmAppDef ecmAppDef = ecmAppDefMapper.selectOne(new LambdaQueryWrapper<EcmAppDef>().eq(EcmAppDef::getAppCode, appCode));
                ecmBusiInfoRedisDTO.setResiz(ecmAppDef.getResize());
                ecmBusiInfoRedisDTO.setQulity(ecmAppDef.getQulity());
                saveBusiLog(user, inst, ecmBusiInfoRedisDTO);
                ecmBusiInfoRedisDTOList.add(ecmBusiInfoRedisDTO);
            }
        }
        if (CollectionUtils.isEmpty(ecmBusiInfoRedisDTOList)) {
            return treeExtendList;
        }

        return null;
    }


    /**
     * 若父节点下没有子节点，则不显示父节点
     */
    private EcmBusiStructureTreeDTO filterTree(List<EcmBusiStructureTreeDTO> tree, EcmBusiStructureTreeDTO pnode, Set<String> docCode) {
        List<EcmBusiStructureTreeDTO> toRemove = new ArrayList<>();
        int i = 0;
        if (CollectionUtils.isEmpty(tree)) {
            return null;
        }
        for (EcmBusiStructureTreeDTO node : tree) {
            if (CollectionUtil.isNotEmpty(node.getChildren())) {
                EcmBusiStructureTreeDTO ecmBusiStructureTreeDTO = filterTree(node.getChildren(), node, docCode);
                List<EcmBusiStructureTreeDTO> collect = node.getChildren().stream().filter(s -> !s.isLock()).collect(Collectors.toList());
                node.setLock(CollectionUtils.isEmpty(collect));
                if (ecmBusiStructureTreeDTO != null) {
                    i++;
                    docCode.add(ecmBusiStructureTreeDTO.getDocCode());
                    toRemove.add(ecmBusiStructureTreeDTO);
                }
            } else {
                if (null != node.getNodeType() && IcmsConstants.ZERO.equals(node.getNodeType())) {
//                    // 如果没有子节点，则将该节点添加到待移除列表
                    for (EcmBusiStructureTreeDTO dto : toRemove) {
                        docCode.add(dto.getDocCode());
                    }
                    toRemove.add(node);
                    i++;
                }
                if (IcmsConstants.ONE.equals(node.getNodeType())) {
                    //叶子节点
                    EcmDocrightDefDTO right = node.getDocRight();
                    if (!ObjectUtils.isEmpty(right) && (StrUtil.equals(IcmsConstants.ONE.toString(), right.getAddRight())
                            || StrUtil.equals(IcmsConstants.ONE.toString(), right.getDeleteRight())
                            || StrUtil.equals(IcmsConstants.ONE.toString(), right.getUpdateRight())
                            || StrUtil.equals(IcmsConstants.ONE.toString(), right.getReadRight())
                            || StrUtil.equals(IcmsConstants.ONE.toString(), right.getThumRight())
                            || StrUtil.equals(IcmsConstants.ONE.toString(), right.getPrintRight())
                            || StrUtil.equals(IcmsConstants.ONE.toString(), right.getDownloadRight()))) {
                    } else {
                        docCode.add(node.getDocCode());
                        toRemove.add(node);
                    }
                }
            }

        }
        for (EcmBusiStructureTreeDTO dto : toRemove) {
            docCode.add(dto.getDocCode());
            dto.setLock(true);
        }
        if (i == tree.size()) {
            docCode.add(pnode.getDocCode());
            return pnode;
        }

        // 移除所有待移除的节点
        return null;
    }

    /**
     * 保存批扫新增业务日志
     */
    private void saveBusiLog(SysUserDTO user, SysInstDTO inst, EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        String appCode = ecmBusiInfoRedisDTO.getAppCode();
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectById(appCode);
        // 保存业务日志
        EcmBusiLog ecmBusiLog = new EcmBusiLog();
        ecmBusiLog.setAppCode(ecmAppDef.getAppCode());
        ecmBusiLog.setAppName(ecmAppDef.getAppName());
        ecmBusiLog.setOrgCode(inst.getInstNo());
        ecmBusiLog.setOperatorId(user.getLoginName());
        ecmBusiLog.setOperator(user.getName());
        ecmBusiLog.setOperateContent("新增业务：" + ecmBusiInfoRedisDTO.getBusiNo());
        ecmBusiLog.setBusiNo(ecmBusiInfoRedisDTO.getBusiNo());
        busiLogMapper.insert(ecmBusiLog);
    }

    /**
     * 是否偏离矫正
     */
    private Boolean searchIsFlat(String key) {
        //获取配置信息
        Result<SysParamDTO> pageReturnResult = paramApi.searchValueByKey(key);
        if (!pageReturnResult.isSucc()) {
            throw new SunyardException(pageReturnResult.getMsg());
        }
        SysParamDTO data = pageReturnResult.getData();
        String value = data.getValue();
        SysStrategyVO sysStrategyVO = JSONObject.parseObject(value, SysStrategyVO.class);

        return sysStrategyVO.getOcrFlatStatus();
    }

    /**
     * 获取设备名称
     */
    private String getEquipmentNameById(Long equipmentId) {
        String equipmentName = "";
        Result<List<EcmStorageQueDTO>> storageDeviceList = sysStorageService.getStorageDeviceList();
        List<EcmStorageQueDTO> data = storageDeviceList.getData();
        if (CollectionUtils.isNotEmpty(data)) {
            Optional<EcmStorageQueDTO> first = data.stream().filter(f -> f.getId().equals(equipmentId)).findFirst();
            if (first.isPresent()) {
                EcmStorageQueDTO ecmStorageQueDTO = first.get();
                equipmentName = ecmStorageQueDTO.getEquipmentName();
            }
        }
        return equipmentName;
    }

    /**
     * 影像文件列表
     */
    public PageInfo searchEcmsFileList(EcmsCaptureVO vo, AccountTokenExtendDTO token) {
        AssertUtils.isNull(vo.getBusiId(), "业务ID不能为空!");//
        Long busiId = vo.getBusiId();
        // redis中取出当前业务下所有的影像文件
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, busiId);
        //校验关户权限
        checkCloseAccountPermission(ecmBusiInfoRedisDTO.getAttrList(), token, true);
        // 文件权限用当前业务版本最新权限
        List<FileInfoRedisDTO> resultFiles = new LinkedList<>();
        //走redis
        return searchMinFile(vo, token, busiId, resultFiles, ecmBusiInfoRedisDTO);
    }

    /**
     * 影像文件待审核列表
     */
    public PageInfo searchEcmsFileListInCheckFailed(EcmsCaptureVO vo, AccountTokenExtendDTO token) {
        AssertUtils.isNull(vo.getBusiId(), "业务ID不能为空!");//
        Long busiId = vo.getBusiId();
        // redis中取出当前业务下所有的影像文件
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, busiId);
        // 文件权限用当前业务版本最新权限
        List<FileInfoRedisDTO> resultFiles = new LinkedList<>();
        //走redis
        return searchAllFileInCheckFailed(vo, token, busiId, resultFiles, ecmBusiInfoRedisDTO);
    }

    /**
     * 获取文件列表
     */
    private PageInfo searchMinFile(EcmsCaptureVO vo, AccountTokenExtendDTO token, Long busiId,
                                   List<FileInfoRedisDTO> resultFiles, EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        // 1. 获取文件列表并初始化
        List<FileInfoRedisDTO> fileInfos = busiCacheService.getFileInfoRedis(busiId);
        if (CollUtil.isEmpty(fileInfos)) {
            return new PageInfo<>(Collections.emptyList());
        }
        Map<Long, Boolean> abnormalResultMarkingMap = new HashMap<>();
        Map<Long, String> taskTypeMap = new HashMap<>();
        Map<Object, Object> redisMap = redisUtils.hmget(RedisConstants.BUSIASYNC_TASK_PREFIX + busiId);

        // 2. 处理RedisMap（仅处理'2'相关任务类型，无额外筛选）
        processRedisMapCommon(redisMap, abnormalResultMarkingMap, taskTypeMap);

        // 3. 影像筛选（公共逻辑）
        fileInfos = filterResultFiles(vo, fileInfos);

        // 4. 处理文件到结果集（公共逻辑）
        List<FileInfoRedisDTO> delNode = new LinkedList<>();
        List<FileInfoRedisDTO> markNode = new LinkedList<>();
        processFileInfosToNodes(fileInfos, abnormalResultMarkingMap, taskTypeMap, resultFiles, delNode, markNode);

        // 5. 按DocId筛选结果（公共逻辑）
        resultFiles = filterByDocId(vo, resultFiles, delNode, markNode, ecmBusiInfoRedisDTO);

        // 6. 排序处理（公共逻辑）
        resultFiles = processSorting(resultFiles, ecmBusiInfoRedisDTO, vo);

        // 7. 特殊排序（公共逻辑）
        if (!ObjectUtils.isEmpty(vo.getSortType())) {
            resultFiles = sortData(resultFiles, vo.getSortField(), vo.getSortType());
        }

        // 8. 结果文件后续处理（公共逻辑）
        resultFiles = processResultFiles(resultFiles, ecmBusiInfoRedisDTO, token);

        // 9. 填充信息
        resultFiles = statusResultFiles(resultFiles, ecmBusiInfoRedisDTO);

        // 10. 分页处理（公共逻辑）
        return processPagination(resultFiles, vo);
    }

    /**
     * 获取待审核文件列表
     */
    private PageInfo searchAllFileInCheckFailed(EcmsCaptureVO vo, AccountTokenExtendDTO token, Long busiId,
                                                List<FileInfoRedisDTO> resultFiles, EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        // 1. 获取文件列表并初始化
        List<FileInfoRedisDTO> fileInfos = busiCacheService.getFileInfoRedis(busiId);
        if (CollUtil.isEmpty(fileInfos)) {
            return new PageInfo<>(Collections.emptyList());
        }
        Map<Long, Boolean> abnormalResultMarkingMap = new HashMap<>();
        Map<Long, String> taskTypeMap = new HashMap<>();
        Map<Object, Object> redisMap = redisUtils.hmget(RedisConstants.BUSIASYNC_TASK_PREFIX + busiId);

        // 2. 处理RedisMap
        Set<Long> fileIdsWithType6 = new HashSet<>();
        processRedisMapCommon(redisMap, abnormalResultMarkingMap, taskTypeMap); // 复用公共逻辑
        // 收集包含待审核的文件ID
        for (Map.Entry<Object, Object> entry : redisMap.entrySet()) {
            Long fileId = Long.parseLong(String.valueOf(entry.getKey()));
            EcmAsyncTask task = (EcmAsyncTask) entry.getValue();
            if (task.getTaskType() != null && task.getTaskType().contains("6")) {
                fileIdsWithType6.add(fileId);
            }
        }
        // 仅保留包含待审核的文件
        fileInfos = fileInfos.stream()
                .filter(f -> fileIdsWithType6.contains(f.getFileId()))
                .collect(Collectors.toList());

        // 3. 影像筛选（公共逻辑）
        fileInfos = filterResultFiles(vo, fileInfos);

        // 4. 处理文件到结果集（公共逻辑）
        List<FileInfoRedisDTO> delNode = new LinkedList<>();
        List<FileInfoRedisDTO> markNode = new LinkedList<>();
        processFileInfosToNodes(fileInfos, abnormalResultMarkingMap, taskTypeMap, resultFiles, delNode, markNode);

        // 5. 按DocId筛选结果（公共逻辑）
        resultFiles = filterByDocId(vo, resultFiles, delNode, markNode, ecmBusiInfoRedisDTO);

        // 6. 排序处理（公共逻辑）
        resultFiles = processSorting(resultFiles, ecmBusiInfoRedisDTO, vo);

        // 7. 特殊排序（公共逻辑）
        if (!ObjectUtils.isEmpty(vo.getSortType())) {
            resultFiles = sortData(resultFiles, vo.getSortField(), vo.getSortType());
        }

        // 8. 结果文件后续处理（公共逻辑）
        resultFiles = processResultFiles(resultFiles, ecmBusiInfoRedisDTO, token);

        // 9. 分页处理（公共逻辑）
        return processPagination(resultFiles, vo);
    }

    /**
     * 处理RedisMap，填充异常标记和任务类型映射（公共逻辑：处理包含'2'相关的任务类型）
     */
    private void processRedisMapCommon(Map<Object, Object> redisMap,
                                       Map<Long, Boolean> abnormalResultMarkingMap,
                                       Map<Long, String> taskTypeMap) {
        for (Map.Entry<Object, Object> entry : redisMap.entrySet()) {
            Long fileId = Long.parseLong(String.valueOf(entry.getKey()));
            EcmAsyncTask ecmAsyncTask = (EcmAsyncTask) entry.getValue();

            // 判断taskType是否包含失败/检查失败（'2'相关枚举）
            boolean abnormalResultMarking = ecmAsyncTask.getTaskType() != null
                    && (ecmAsyncTask.getTaskType().contains(EcmCheckAsyncTaskEnum.FAILED.description())
                    || ecmAsyncTask.getTaskType().contains(EcmCheckAsyncTaskEnum.CHECK_FAILED.description()));
            abnormalResultMarkingMap.put(fileId, abnormalResultMarking);
            taskTypeMap.put(fileId, ecmAsyncTask.getTaskType());
        }
    }

    /**
     * 处理文件列表到结果集（公共逻辑：敏感信息清空、状态分类）
     */
    private void processFileInfosToNodes(List<FileInfoRedisDTO> fileInfos,
                                         Map<Long, Boolean> abnormalResultMarkingMap,
                                         Map<Long, String> taskTypeMap,
                                         List<FileInfoRedisDTO> resultFiles,
                                         List<FileInfoRedisDTO> delNode,
                                         List<FileInfoRedisDTO> markNode) {
        for (FileInfoRedisDTO s : fileInfos) {
            // 清空敏感信息
            s.setBucketName(null);
            s.setFilePath(null);
            s.setObjectKey(null);
            s.setUrl(null);
            // 赋值异常结果标记
            Boolean abnormalResultMarking = abnormalResultMarkingMap.getOrDefault(s.getFileId(), false);
            s.setAbnormalResultMarking(abnormalResultMarking);
            // 赋值任务类型
            s.setTaskType(taskTypeMap.get(s.getFileId()));
            s.setDocFileSort(s.getFileSort().toString());
            // 按状态分类
            if (IcmsConstants.ZERO.equals(s.getState())) {
                resultFiles.add(s);
                if (s.getMarkDocId() != null) {
                    markNode.add(s);
                }
            } else {
                delNode.add(s);
            }
        }
    }

    /**
     * 根据DocId筛选结果集（公共逻辑：删除节点/标记节点筛选）
     */
    private List<FileInfoRedisDTO> filterByDocId(EcmsCaptureVO vo,
                                                 List<FileInfoRedisDTO> resultFiles,
                                                 List<FileInfoRedisDTO> delNode,
                                                 List<FileInfoRedisDTO> markNode,
                                                 EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        if (ObjectUtils.isEmpty(vo.getDocId())) {
            return resultFiles;
        }
        if (IcmsConstants.DELETED_CODE.equals(vo.getDocId())) {
            return delNode; // 已删除节点
        }
        // 按标记节点筛选
        if (ObjectUtils.isEmpty(vo.getMarkDocId())) {
            Set<String> allChildIds = getDocAllTree(vo, ecmBusiInfoRedisDTO);
            return allChildIds.isEmpty() ? resultFiles :
                    resultFiles.stream().filter(p -> allChildIds.contains(p.getDocCode())).collect(Collectors.toList());
        } else {
            return markNode.stream()
                    .filter(p -> vo.getDocId().equals(p.getDocCode()))
                    .filter(p -> vo.getMarkDocId().equals(p.getMarkDocId()))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 处理排序（公共逻辑：静态/动态树排序）
     */
    private List<FileInfoRedisDTO> processSorting(List<FileInfoRedisDTO> resultFiles,
                                                  EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO,
                                                  EcmsCaptureVO vo) {
        if (CollUtil.isEmpty(resultFiles)) {
            return resultFiles;
        }
        if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType())) {
            return doSort(resultFiles, commonService.getDocList1(ecmBusiInfoRedisDTO.getAppCode(), ecmBusiInfoRedisDTO.getRightVer()));
        } else if (IcmsConstants.DYNAMIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType())) {
            List<EcmBusiDoc> ecmBusiDocs = ecmBusiDocMapper.selectList(new LambdaQueryWrapper<EcmBusiDoc>().eq(EcmBusiDoc::getBusiId, vo.getBusiId()));
            List<EcmBusiDocRedisDTO> docRedisDTOs = PageCopyListUtils.copyListProperties(ecmBusiDocs, EcmBusiDocRedisDTO.class);
            return doSort(resultFiles, docRedisDTOs);
        }
        return resultFiles;
    }

    /**
     * 处理结果文件后续逻辑（公共逻辑：敏感信息清理、格式转换等）
     */
    private List<FileInfoRedisDTO> processResultFiles(List<FileInfoRedisDTO> resultFiles,
                                                      EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO,
                                                      AccountTokenExtendDTO token) {
        if (CollUtil.isEmpty(resultFiles)) {
            return resultFiles;
        }
        // 资料名称为空处理
        resultFiles.forEach(s -> {
            if (s.getDocName() == null) s.setDocName("");
        });
        // 文件格式转小写
        resultFiles.forEach(file -> {
            if (file.getFormat() != null) file.setFormat(file.getFormat().toLowerCase());
        });
        // 计算文件大小
        resultFiles.forEach(file -> {
            if (file.getSize() != null) calculateFileSize(file);
        });
        // 添加权限列表
        return andDocRightList(ecmBusiInfoRedisDTO, resultFiles, token);
    }

    /**
     * 填充信息
     * @param resultFiles
     * @param ecmBusiInfoRedisDTO
     * @return
     */
    private List<FileInfoRedisDTO> statusResultFiles(List<FileInfoRedisDTO> resultFiles,
                                                      EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO){
        if (CollUtil.isEmpty(resultFiles)) {
            return resultFiles;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        resultFiles.forEach(file -> {
            file.setStatus(StringUtils.isEmpty(ecmBusiInfoRedisDTO.getStatus())?IcmsConstants.ZERO:ecmBusiInfoRedisDTO.getStatus());
            Integer isExpired = file.getIsExpired();
            String isExpiredStr;
            if (isExpired == null) {
                isExpiredStr = "";
            } else {
                isExpiredStr = IcmsConstants.ZERO.equals(isExpired)
                        ? (file.getExpireDate() != null ? sdf.format(file.getExpireDate()) : "")
                        : (IcmsConstants.ONE.equals(isExpired) ? "文件已到期" : "");
            }
            file.setIsExpiredStr(isExpiredStr);
        });
        return resultFiles;
    }

    /**
     * 处理分页（公共逻辑：分页计算与封装）
     */
    private PageInfo processPagination(List<FileInfoRedisDTO> resultFiles, EcmsCaptureVO vo) {
        Page page = new Page();
        int total = resultFiles.size();
        page.setTotal(total);
        // 分页处理
        if (IcmsConstants.ZERO.equals(vo.getShowAll())) {
            int start = (vo.getPageNum() - 1) * vo.getPageSize();
            int end = Math.min(start + vo.getPageSize(), total);
            resultFiles = resultFiles.subList(start, end);
        }
        page.addAll(resultFiles);
        return new PageInfo<>(page);
    }

    /**
     * 获取资料树集合
     */
    private Set<String> getDocAllTree(EcmsCaptureVO vo, EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        Set<String> allChildIds = new HashSet<>();
        if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType())) {

            if (!ObjectUtils.isEmpty(vo.getDocCode())) {
                if (IcmsConstants.UNCLASSIFIED_ID.equals(vo.getDocCode())) {
                    allChildIds.add(IcmsConstants.UNCLASSIFIED_ID);
                } else {
                    //查询该业务该版本选中资料节点及其子节点所
                    LambdaQueryWrapper<EcmDocDefRelVer> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(EcmDocDefRelVer::getAppCode, ecmBusiInfoRedisDTO.getAppCode());
                    wrapper.eq(EcmDocDefRelVer::getRightVer, ecmBusiInfoRedisDTO.getRightVer());
                    List<EcmDocDefRelVer> ecmDocDefRelVerList = ecmDocDefRelVerMapper.selectList(wrapper);
                    if (CollectionUtil.isNotEmpty(ecmDocDefRelVerList)) {
                        List<EcmDocDefRelVer> filteredList = filterByDocCode(ecmDocDefRelVerList, vo.getDocCode());
                        List<String> collect = filteredList.stream().map(EcmDocDefRelVer::getDocCode).collect(Collectors.toList());
                        allChildIds.addAll(collect);
                    }
                }
            } else {
                ///业务资料调整前逻辑
                List<EcmDocTreeDTO> ecmDocTreeDTOS = modelBusiService.searchOldRelevanceInformation1(ecmBusiInfoRedisDTO.getAppCode(), ecmBusiInfoRedisDTO.getRightVer());
                for (EcmDocTreeDTO s : ecmDocTreeDTOS) {
                    allChildIds.add(s.getDocCode());
                }
            }
        } else {
            allChildIds = getAllChildIdsByTree(vo.getDocId(),ecmBusiInfoRedisDTO);
        }
        return allChildIds;
    }

    /**
     * 递归获取子节点
     */
    private static List<EcmDocDefRelVer> filterByDocCode(List<EcmDocDefRelVer> list, String docCode) {
        List<EcmDocDefRelVer> filteredList = new ArrayList<>();
        for (EcmDocDefRelVer node : list) {
            if (node.getDocCode().equals(docCode)) {
                filteredList.add(node);
                addChildren(list, node, filteredList);
            }
        }
        return filteredList;
    }

    /**
     * 递归添加子节点
     */
    private static void addChildren(List<EcmDocDefRelVer> list, EcmDocDefRelVer parent, List<EcmDocDefRelVer> filteredList) {
        for (EcmDocDefRelVer node : list) {
            if (node.getParent().equals(parent.getDocCode())) {
                filteredList.add(node);
                addChildren(list, node, filteredList);
            }
        }
    }

    /**
     * 计算文件大小
     */
    private void calculateFileSize(FileInfoRedisDTO file) {
        try {
            Double newFileSize = 0.00;
            String fileUnit = "";
            Long oldFileSize = file.getSize() == null ? file.getNewFileSize() : file.getSize();
            if (oldFileSize > 0 && oldFileSize <= 1024 * 1024) {
                newFileSize = Math.ceil(oldFileSize.doubleValue() / 1024);
                fileUnit = IcmsConstants.FILE_UNIT_K;
            } else if (oldFileSize > 1024 * 1024 && oldFileSize <= 1024 * 1024 * 1024) {
                newFileSize = Double.valueOf(String.format("%.3f", (oldFileSize.doubleValue() / (1024 * 1024))));
                fileUnit = IcmsConstants.FILE_UNIT_M;
            } else {
                newFileSize = Double.valueOf(String.format("%.3f", (oldFileSize.doubleValue() / (1024 * 1024 * 1024))));
                fileUnit = IcmsConstants.FILE_UNIT_G;
            }
            file.setFileUnit(fileUnit);
            file.setFileSize(newFileSize.toString());
        } catch (NumberFormatException e) {
            log.error("单位转换有误：{}", e.getMessage());
        }
    }

    /**
     * 文件排序
     */
    public List<FileInfoRedisDTO> doSort(List<FileInfoRedisDTO> resultFiles, List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS) {
        List<FileInfoRedisDTO> finalResultFiles = new ArrayList<>();
        //提取未归类影像文件
        List<FileInfoRedisDTO> unclassifiedFiles = resultFiles.stream()
                .filter(p -> IcmsConstants.UNCLASSIFIED_ID.equals(p.getDocCode()))
                .sorted(Comparator.comparing(EcmFileInfoDTO::getFileSort))
                .collect(Collectors.toList());
        //提取资料类型id
        List<String> docCodes = resultFiles.stream()
                .filter(p -> !IcmsConstants.UNCLASSIFIED_ID.equals(p.getDocId()))
                .filter(p -> !ObjectUtils.isEmpty(p.getDocCode()))
                .map(EcmFileInfoDTO::getDocCode).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(docCodes)) {
            Map<String, List<FileInfoRedisDTO>> fileGroupedByDocTypeId = resultFiles.stream()
                    .filter(p -> !IcmsConstants.UNCLASSIFIED_ID.equals(p.getDocId()))
                    .filter(p -> !ObjectUtils.isEmpty(p.getDocCode()))
                    .collect(Collectors.groupingBy(EcmFileInfoDTO::getDocCode));
            for (EcmBusiDocRedisDTO docNode : ecmBusiDocRedisDTOS) {
                List<FileInfoRedisDTO> leafValues = getLeafValues(docNode, fileGroupedByDocTypeId);
                finalResultFiles.addAll(leafValues);
            }
        }
        finalResultFiles.addAll(unclassifiedFiles);
        return finalResultFiles;
    }


    /**
     * BFS 有序获取叶子节点数据
     */
    private List<FileInfoRedisDTO> getLeafValues(EcmBusiDocRedisDTO root, Map<String, List<FileInfoRedisDTO>> fileGroupedByDocTypeId) {
        // 空树返回空列表
        if (ObjectUtils.isEmpty(root)) {
            return new ArrayList<>();
        }
        List<FileInfoRedisDTO> result = new ArrayList<>();
        Deque<EcmBusiDocRedisDTO> queue = new LinkedList<>();

        // 将根节点加入队列
        queue.offer(root);
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                EcmBusiDocRedisDTO node = queue.poll();
                List<EcmBusiDocRedisDTO> children = node.getChildren();
                if (CollectionUtils.isEmpty(children)) {
                    List<FileInfoRedisDTO> fileInfoRedisEntities = fileGroupedByDocTypeId.get(node.getDocCode());
                    if (!CollectionUtils.isEmpty(fileInfoRedisEntities)) {
                        //文件有序
                        fileInfoRedisEntities = fileInfoRedisEntities.stream().sorted(Comparator.comparing(EcmFileInfoDTO::getFileSort)).collect(Collectors.toList());
                        result.addAll(fileInfoRedisEntities);
                    }
                } else {
                    // 非叶子节点
                    Collections.reverse(children);
                    for (EcmBusiDocRedisDTO child : children) {
                        queue.offerFirst(child);
                    }
                }
            }
        }

        return result;
    }

    /**
     * BFS 有序获取叶子节点数据
     */
    private List<EcmFileInfo> getLeafValues2(EcmBusiDocRedisDTO root, Map<String, List<EcmFileInfo>> fileGroupedByDocTypeId) {
        // 空树返回空列表
        if (ObjectUtils.isEmpty(root)) {
            return new ArrayList<>();
        }
        List<EcmFileInfo> result = new ArrayList<>();
        Deque<EcmBusiDocRedisDTO> queue = new LinkedList<>();

        // 将根节点加入队列
        queue.offer(root);
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                EcmBusiDocRedisDTO node = queue.poll();
                List<EcmBusiDocRedisDTO> children = node.getChildren();
                if (CollectionUtils.isEmpty(children)) {
                    List<EcmFileInfo> fileInfoRedisEntities = fileGroupedByDocTypeId.get(node.getDocCode());
                    if (!CollectionUtils.isEmpty(fileInfoRedisEntities)) {
                        //文件有序
                        fileInfoRedisEntities = fileInfoRedisEntities.stream().sorted(Comparator.comparing(EcmFileInfo::getFileSort)).collect(Collectors.toList());
                        result.addAll(fileInfoRedisEntities);
                    }
                } else {
                    // 非叶子节点
                    Collections.reverse(children);
                    for (EcmBusiDocRedisDTO child : children) {
                        queue.offerFirst(child);
                    }
                }
            }
        }

        return result;
    }


    /**
     * 复用弹框搜素-资料下拉列表(全部最子级)
     */
    public List<FileInfoRedisDTO> andDocRightList(EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO, List<FileInfoRedisDTO> resultFiles, AccountTokenExtendDTO token) {
        List<EcmDocrightDefDTO> docRightList = busiCacheService.getDocrightDefCommon(ecmBusiInfoRedisDTO, token);

        Map<String, List<FileInfoRedisDTO>> collect = new HashMap<>();
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
        if (CollectionUtil.isNotEmpty(fileInfoRedis)) {
            collect = fileInfoRedis.stream()
                    .filter(s -> s.getState().equals(StateConstants.ZERO))
                    .collect(Collectors.groupingBy(FileInfoRedisDTO::getDocCode));
        }
        Map<String, List<EcmDocrightDefDTO>> docRightGroupedById = docRightList.stream()
                .filter(p -> !ObjectUtils.isEmpty(p.getDocCode()))
                .collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));

        for (FileInfoRedisDTO resultFile : resultFiles) {
            List<EcmDocrightDefDTO> docrightDefExtendList = docRightGroupedById.get(resultFile.getDocCode());
            List<FileInfoRedisDTO> fileInfoRedisDTOS = collect.get(resultFile.getDocCode());
            EcmDocrightDefDTO ecmDocrightDefDTO = new EcmDocrightDefDTO();
            if (!CollectionUtils.isEmpty(docrightDefExtendList)) {
                //默认值
                resultFile.setDocRight(ecmDocrightDefDTO);
                resultFile.setShowType(2);
                EcmDocrightDefDTO right = docrightDefExtendList.get(0);
                if (StrUtil.equals(IcmsConstants.ONE.toString(), right.getThumRight())) {
                    //有缩略图权限
                    resultFile.setShowType(1);
                }
                if (StrUtil.equals(IcmsConstants.ONE.toString(), right.getDeleteRight())
                        || StrUtil.equals(IcmsConstants.ONE.toString(), right.getUpdateRight())
                        || StrUtil.equals(IcmsConstants.ONE.toString(), right.getReadRight())
                        || StrUtil.equals(IcmsConstants.ONE.toString(), right.getThumRight())
                        || StrUtil.equals(IcmsConstants.ONE.toString(), right.getPrintRight())
                        || StrUtil.equals(IcmsConstants.ONE.toString(), right.getDownloadRight())
                        || StrUtil.equals(IcmsConstants.ONE.toString(), right.getOtherUpdate())) {
                } else {
                    resultFile.setFileMd5(null);
                    resultFile.setNewFileId(0L);
                    resultFile.setShowType(0);
                }
                if (CollectionUtil.isNotEmpty(fileInfoRedisDTOS)) {
                    docrightDefExtendList.get(0).setFileCount(fileInfoRedisDTOS.size());
                } else {
                    docrightDefExtendList.get(0).setFileCount(0);
                }
                resultFile.setDocRight(docrightDefExtendList.get(0));
            } else {
                if (IcmsConstants.UNCLASSIFIED_ID.equals(resultFile.getDocCode())) {
                    //文件属于未归类-文件操作权限打开
                    EcmDocrightDefDTO docrightDefExtend = getDocRightAllOpen();
                    resultFile.setDocRight(docrightDefExtend);
                } else {
                    EcmDocrightDefDTO docrightDefExtend = getDocRightAllOpen();
                    resultFile.setDocRight(docrightDefExtend);
                    resultFile.setFileMd5(null);
                    resultFile.setNewFileId(0L);
                    resultFile.setShowType(0);
                }
            }
        }

        UserBusiRedisDTO userBusiData = busiCacheService.getUserPageRedis(token.getFlagId(), token);
        if (userBusiData.getDocCodeShow() != null) {
            Map<Long, List<String>> docCodeShow = userBusiData.getDocCodeShow();
            List<String> strings = docCodeShow.get(ecmBusiInfoRedisDTO.getBusiId());
            if (CollectionUtil.isNotEmpty(strings)) {
                resultFiles = resultFiles.stream().filter(s -> strings.contains(s.getDocCode())).collect(Collectors.toList());
            }
        }

        if (IcmsConstants.NODETYPPE_NOSHOW.equals(nodeType)) {
            ArrayList<FileInfoRedisDTO> objects = new ArrayList<>();
            //隐藏带锁的节点
            for (FileInfoRedisDTO resultFile : resultFiles) {
                if (!IcmsConstants.FILETYPE_LOCK.equals(resultFile.getShowType())) {
                    objects.add(resultFile);
                }
            }
            return objects;
        }
        return resultFiles;
    }


    /**
     * 复用公用类
     */
    @WebsocketNoticeAnnotation(msgType = "all")
    @Lock4j(keys = "#ecmBusiDocExtend.sourceBusiId")
    public Result multiplexFile(FileInfoRedisEntityVO ecmBusiDocExtend, AccountTokenExtendDTO token, Boolean b) {
        AssertUtils.isTrue(IcmsConstants.SHOW_PAGE.equals(token.getIsShow()),"当前无权限,无法进行采集相关操作");
        List<MultiplexFileVO> multiplexFileVO = ecmBusiDocExtend.getMultiplexFileVO();
        AssertUtils.isNull(multiplexFileVO, "选中要复用的资料节点信息不能为空");
        AssertUtils.isNull(ecmBusiDocExtend.getSourceBusiId(), "源业务id不能为空");
        AssertUtils.isNull(ecmBusiDocExtend.getSourceDocId(), "源资料节点id不能为空");
        AssertUtils.isNull(ecmBusiDocExtend.getSourceAppTypeName(), "源资料节点的业务类型名称不能为空");
        AssertUtils.isNull(ecmBusiDocExtend.getSourceAppCode(), "源资料节点的业务类型代码不能为空");
        AssertUtils.isNull(ecmBusiDocExtend.getSourceBusiNo(), "源资料节点的业务主索引不能为空");
        //文件新增：0待提交状态、4处理   失败状态、5已完结状态可进行新增
        if(!token.isOut()){
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                    .getEcmBusiInfoRedisDTO(token, ecmBusiDocExtend.getSourceBusiId());
            Integer status = ecmBusiInfoRedisDTO.getStatus();
            if(!BusiInfoConstants.BUSI_STATUS_ZERO.equals(status)&&!BusiInfoConstants.BUSI_STATUS_FOUR.equals(status)&&
                    !BusiInfoConstants.BUSI_STATUS_FIVE.equals(status)){
                AssertUtils.isTrue(true,"当前业务状态暂无新增文件权限");
            }
        }
        List<FileInfoRedisDTO> ecmFileInfos = busiCacheService.getFileInfoRedis(ecmBusiDocExtend.getSourceBusiId());
        ecmFileInfos = ecmFileInfos.stream()
                .filter(s -> StateConstants.ZERO.equals(s.getState()))
                .collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(ecmBusiDocExtend.getSourceDocId())
                && !ecmBusiDocExtend.getSourceDocId().equals(ecmBusiDocExtend.getSourceBusiId().toString())) {
            ecmFileInfos = ecmFileInfos.stream()
                    .filter(s -> ecmBusiDocExtend.getSourceDocId().equals(s.getDocCode()))
                    .collect(Collectors.toList());
        }
//        //选中的业务的资料节点
        //查到源节点写所有的md5集合
        List<String> sourceMd5 = ecmFileInfos.stream()
                .filter(dto->StateConstants.NO.equals(dto.getState()))
                .map(FileInfoRedisDTO::getFileMd5)
                .collect(Collectors.toList());
        Map<String, List<FileInfoRedisDTO>> sourceMd5ListMap = ecmFileInfos.stream().collect(Collectors.groupingBy(FileInfoRedisDTO::getFileMd5));
        //所选中的所有目标节点docCode集合
        List<String> targetDocCodeList = new ArrayList<>();
        multiplexFileVO.forEach(p -> {
            List<String> targetDocTypeId = p.getTargetDocTypeId();
            targetDocCodeList.addAll(targetDocTypeId);
        });
        //选中的源节点docCode集合
        List<String> sourceDocCodeList = new ArrayList<>();
        //查询得到要复用的文件信息
        List<FileInfoRedisDTO> fileInfoRedisDTOList = getFileInfoRedisDTOS(ecmBusiDocExtend, sourceDocCodeList, token);

        List<String> collect1 = fileInfoRedisDTOList.stream().filter(s->!IcmsConstants.UNCLASSIFIED_ID.equals(s.getDocCode()))
                                    .map(s -> s.getFileMd5()).collect(Collectors.toList());
        int mostFrequent = CommonUtils.findMostFrequent(collect1);
        AssertUtils.isTrue(mostFrequent > 1, "当前复用文件中，存在重复的文件");
        AssertUtils.isNull(fileInfoRedisDTOList, "无可复用文件");
        //查到目标节点写所有的md5集合
        List<String> targetMd5 = fileInfoRedisDTOList.stream().map(FileInfoRedisDTO::getFileMd5).collect(Collectors.toList());
        Map<String, List<FileInfoRedisDTO>> collect = fileInfoRedisDTOList.stream().collect(Collectors.groupingBy(FileInfoRedisDTO::getFileMd5));
        //求两集合的交集 判断是否有文件重复
        ArrayList<String> intersection = (ArrayList) CollectionUtils.intersection(sourceMd5, targetMd5);
        List<Long> listFileId = new ArrayList<>();
        List<String> list = new ArrayList<>();
        if (!ObjectUtils.isEmpty(intersection)) {
            for (String a : intersection) {
                List<FileInfoRedisDTO> fileInfoRedisEntities = collect.get(a);
                List<FileInfoRedisDTO> ecmFileInfos1 = sourceMd5ListMap.get(a);
                if (!CollectionUtils.isEmpty(fileInfoRedisEntities) && !CollectionUtils.isEmpty(ecmFileInfos1)) {
                    for (FileInfoRedisDTO fileInfoRedisDTO1 : fileInfoRedisEntities) {
                        for (FileInfoRedisDTO ecmFileInfo : ecmFileInfos1) {
                            if (fileInfoRedisDTO1.getDocCode().equals(ecmFileInfo.getDocCode())
                                    && !IcmsConstants.UNCLASSIFIED_ID.equals(fileInfoRedisDTO1.getDocCode())) {
                                listFileId.add(fileInfoRedisDTO1.getFileId());
                                break;
                            }
                        }
                    }
                }
            }
        }
        //判断选中的节点有无与被复用的资料节点相同的节点 -如果没有没有相同节点那么就不需要进行权限校验
        if (!IcmsConstants.UNCLASSIFIED_ID.equals(ecmBusiDocExtend.getSourceDocId())) {
            //权限校验
            for (String s : sourceDocCodeList) {
                ecmBusiDocExtend.setSourceDocTypeId(s);
                if (b) {
                    checkDocRightByMultiplex(fileInfoRedisDTOList, ecmBusiDocExtend, "复用", token);
                }
            }
        }

        List<EcmFileInfoDTO> fileInfoRedisDTOS = muliCopyFileSave(ecmBusiDocExtend, token,
                fileInfoRedisDTOList, sourceDocCodeList, listFileId);
        list = fileInfoRedisDTOS.stream().map(EcmFileInfoDTO::getNewFileName).collect(Collectors.toList());
        return Result.success("成功复用" + fileInfoRedisDTOS.size() + "个文件,文件名:" + list);
    }


    /**
     * 影像复用
     * @param ecmBusiDocExtend     目标
     * @param token
     * @param fileInfoRedisDTOList 源
     * @param sourceDocCodeList    目标的
     * @param listFileId
     */
    public List<EcmFileInfoDTO> muliCopyFileSave(FileInfoRedisEntityVO ecmBusiDocExtend,
                                                 AccountTokenExtendDTO token,
                                                 List<FileInfoRedisDTO> fileInfoRedisDTOList,
                                                 List<String> sourceDocCodeList,
                                                 List<Long> listFileId) {
        List<EcmFileInfoDTO> fileInfoRedisDTOS = new ArrayList<>();
        List<EcmFileInfo> ecmFileInfos1 = new ArrayList<>();
        List<FileInfoRedisDTO> entryFiles = new ArrayList();
        List<EcmFileHistory> ecmFileHistoryList = new ArrayList<>();
        List<EcmFileLabel> ecmFileLabels =new ArrayList<>();
        List<EcmAsyncTask> ecmAsyncTasks = new ArrayList<>();
        List<EcmFileExpireInfo> ecmFileExpireInfos =new ArrayList<>();
        HashMap<EcmAsyncTask,FileInfoRedisDTO> intelligentHashMap = new HashMap<>();
        for (FileInfoRedisDTO fileInfoRedisDTO : fileInfoRedisDTOList) {
            //备注
            String newComment = "";
            //得到被复用文件的资料代码code
            String docCode = fileInfoRedisDTO.getDocCode();
            //根据资料类型id得docId:知道了docId就知道了该文件要复用到那个资料节点上
            AssertUtils.isNull(docCode, "参数错误");
            if (!sourceDocCodeList.contains(docCode)) {
                //没有有相同的节点，需要复用到进未归类中
                docCode = IcmsConstants.UNCLASSIFIED_ID;
            } else {
                entryFiles.add(fileInfoRedisDTO);
                //有相同节点，复用其备注
                newComment = fileInfoRedisDTO.getComment();
            }
            if (listFileId.contains(fileInfoRedisDTO.getFileId())) {
                //已存在的文件跳过
                continue;
            }
            EcmFileInfo ecmFileInfo = new EcmFileInfo();
            BeanUtils.copyProperties(fileInfoRedisDTO, ecmFileInfo);
            ecmFileInfo.setFileId(snowflakeUtils.nextId());
            ecmFileInfo.setDocCode(docCode);
            ecmFileInfo.setBusiId(ecmBusiDocExtend.getSourceBusiId());
            ecmFileInfo.setNewFileSize(fileInfoRedisDTO.getSize());
            ecmFileInfo.setCreateUser(token.getUsername());
            ecmFileInfo.setCreateUserName(token.getName());
            ecmFileInfo.setNewFileExt(getExt(fileInfoRedisDTO));
            ecmFileInfo.setFileReuse(IcmsConstants.ONE);
            //将复用后生成的新文件插入文件历史记录表
            EcmFileHistory ecmFileHistory = new EcmFileHistory();
            ecmFileHistory.setFileId(ecmFileInfo.getFileId());
            ecmFileHistory.setBusiId(ecmFileInfo.getBusiId());
            ecmFileHistory.setNewFileSize(ecmFileInfo.getNewFileSize());
            ecmFileHistory.setNewFileId(ecmFileInfo.getNewFileId());
            ecmFileHistory.setFileOperation(IcmsConstants.ADD_FILE_OPERATION_STRING + "(复用" + fileInfoRedisDTO.getBusiNo() + "业务)");
            ecmFileHistory.setCreateUser(token.getUsername());
            ecmFileHistory.setCreateTime(new Date());
            // 提取文件扩展名
            String ext = getExt(fileInfoRedisDTO);
            ecmFileHistory.setNewFileExt(ext);
            ecmFileHistoryList.add(ecmFileHistory);
            //获取复用的文件ID
            Long fileId=fileInfoRedisDTO.getFileId();
            Long busiId=fileInfoRedisDTO.getBusiId();
            //复用智能化检测
//            EcmAsyncTask ecmAsyncTask= asyncTaskService.getEcmAsyncTask(busiId,fileId);
//            if(ecmAsyncTask!=null){
//                ecmAsyncTask.setFileId(ecmFileInfo.getFileId());
//                ecmAsyncTask.setBusiId(ecmFileInfo.getBusiId());
//                ecmAsyncTask.setCreateTime(new Date());
//                ecmAsyncTask.setUpdateTime(new Date());
//                ecmAsyncTasks.add(ecmAsyncTask);
//            }
            //复用文件标签
            List<EcmFileLabel> ecmFileLabels1 = fileLabelService.queryLabels(busiId, fileId);
            if(CollectionUtils.isNotEmpty(ecmFileLabels1)){
                ecmFileLabels1.forEach(s->{
                    s.setId(snowflakeUtils.nextId());
                    s.setFileId(ecmFileInfo.getFileId());
                    s.setBusiId(ecmFileInfo.getBusiId());
                });
                ecmFileLabels.addAll(ecmFileLabels1);
            }
            //复用文件期限
            List<EcmFileExpireInfo> ecmFileExpireInfos1 = ecmFileExpireInfoMapper.selectList(
                    new LambdaQueryWrapper<EcmFileExpireInfo>().eq(EcmFileExpireInfo::getFileId,fileId));
            if(CollectionUtils.isNotEmpty(ecmFileExpireInfos1)){
                ecmFileExpireInfos1.forEach(s->{
                    s.setId(snowflakeUtils.nextId());
                    s.setFileId(ecmFileInfo.getFileId());
                });
                ecmFileExpireInfos.addAll(ecmFileExpireInfos1);
                //期限存入redis
                fileInfoRedisDTO.setIsExpired(ecmFileExpireInfos1.get(0).getIsExpired());
                fileInfoRedisDTO.setExpireDate(ecmFileExpireInfos1.get(0).getExpireDate());
            }
            fileInfoRedisDTO.setFileId(ecmFileInfo.getFileId());
            fileInfoRedisDTO.setBusiId(ecmFileInfo.getBusiId());
            fileInfoRedisDTO.setDocId(docCode);
            fileInfoRedisDTO.setDocCode(docCode);
            fileInfoRedisDTO.setComment(newComment);
            fileInfoRedisDTO.setDocName(newComment);
            fileInfoRedisDTO.setAppTypeName(ecmBusiDocExtend.getSourceAppTypeName());
            fileInfoRedisDTO.setAppCode(ecmBusiDocExtend.getSourceAppCode());
            fileInfoRedisDTO.setBusiNo(ecmBusiDocExtend.getSourceBusiNo());
            fileInfoRedisDTO.setEcmFileLabels(ecmFileLabels1);
            ecmFileInfo.setComment(newComment);
            ecmFileInfos1.add(ecmFileInfo);
            String remark = "复用新增文件：" + fileInfoRedisDTO.getNewFileName();
            busiOperationService.addOperation(fileInfoRedisDTO.getBusiId(), IcmsConstants.MULTIPLEX_FILE, token, remark);
            fileInfoRedisDTOS.add(fileInfoRedisDTO);
            //添加es中文件信息
            operateFullQueryService.addEsFileInfo(fileInfoRedisDTO, token.getId());
            if (FileInfoService.IMGS.contains(fileInfoRedisDTO.getFormat()) || FileInfoService.DOCS.contains(fileInfoRedisDTO.getFormat())){
                //智能化处理
                EcmAsyncTask ecmAsyncTask = new EcmAsyncTask();
                log.info("复用 ----  当前format：{}",fileInfoRedisDTO.getFormat());
                //修改taskType
                fileInfoService.getTaskType(fileInfoRedisDTO,ecmAsyncTask);
                ecmAsyncTask.setBusiId(ecmFileInfo.getBusiId());
                ecmAsyncTask.setFileId(ecmFileInfo.getFileId());
                ecmAsyncTasks.add(ecmAsyncTask);
                //存入map中后续发起智能化处理
                intelligentHashMap.put(ecmAsyncTask,fileInfoRedisDTO);
            }
        }
        saveInfos(ecmBusiDocExtend, token, fileInfoRedisDTOS, ecmFileInfos1, ecmFileHistoryList, ecmFileLabels, ecmAsyncTasks,intelligentHashMap,ecmFileExpireInfos);
        //被复用的资料节点
        //异步将不加密的文件升级为加密文件
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, ecmBusiDocExtend.getSourceBusiId());
        if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType()) && CollectionUtil.isNotEmpty(entryFiles)) {
            //只处理静态树，动态树不支持文件加密
            Set<String> collect2 = entryFiles.stream().map(FileInfoRedisDTO::getDocCode).collect(Collectors.toSet());
            List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectBatchIds(collect2);
            //获取需要加密的资料节点
            List<String> collect3 = ecmDocDefs.stream()
                    .map(EcmDocDef::getDocCode)
                    .collect(Collectors.toList());
            //过滤出需要加密的文件
            List<Long> files = entryFiles.stream()
                    .filter(s -> collect3.contains(s.getDocCode()) && !IcmsConstants.UNCLASSIFIED_ID.equals(s.getDocCode()))
                    .map(FileInfoRedisDTO::getNewFileId)
                    .collect(Collectors.toList());
            Result<SysParamDTO> sysParam = paramApi.searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString());
            SysParamDTO data = sysParam.getData();
            String value = data.getValue();
            SysStrategyVO sysStrategyVO = JSONObject.parseObject(value, SysStrategyVO.class);
            commonService.encryptFile(sysStrategyVO.getEncryptStatus() ? IcmsConstants.YES_ENCRYPT : IcmsConstants.NO_ENCRYPT, files);
        }
        return fileInfoRedisDTOS;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveInfos(FileInfoRedisEntityVO ecmBusiDocExtend, AccountTokenExtendDTO token, List<EcmFileInfoDTO> fileInfoRedisDTOS, List<EcmFileInfo> ecmFileInfos1, List<EcmFileHistory> ecmFileHistoryList, List<EcmFileLabel> ecmFileLabels, List<EcmAsyncTask> ecmAsyncTasks, HashMap<EcmAsyncTask, FileInfoRedisDTO> intelligentHashMap, List<EcmFileExpireInfo> ecmFileExpireInfos) {
        if (!CollectionUtils.isEmpty(ecmFileInfos1)) {
            insertEcmFileInfos(ecmFileInfos1);
            //更新redis缓存中对应业务的文件信息数据
            updateFileByBusiInfoRedisBatch(fileInfoRedisDTOS, ecmFileHistoryList);
        }
        if (CollectionUtil.isNotEmpty(ecmFileHistoryList)) {
            //批量插入历史记录表
            insertEcmFileHistorys(ecmFileHistoryList);
        }
        if (CollectionUtil.isNotEmpty(ecmAsyncTasks)) {
            ecmAsyncTasks.forEach(s->{
                asyncTaskService.insert(s);
            });
        }
        if (CollectionUtil.isNotEmpty(ecmFileLabels)) {
            //批量插入文件标签表
            insertEcmFileLables(ecmFileLabels);
        }
        if (CollectionUtil.isNotEmpty(ecmFileExpireInfos)){
            //批量插入文件期限表
            insertEcmFileExpireInfos(ecmFileExpireInfos);
        }
        if (intelligentHashMap != null && !intelligentHashMap.isEmpty()){
            for (Map.Entry<EcmAsyncTask, FileInfoRedisDTO> redisDTOEntry : intelligentHashMap.entrySet()) {
                EcmAsyncTask ecmAsyncTask = redisDTOEntry.getKey();
                FileInfoRedisDTO fileInfoRedisDTO = redisDTOEntry.getValue();
                //智能化处理
                fileInfoService.processAfmData(fileInfoRedisDTO,ecmAsyncTask,ecmAsyncTask.getTaskType());
            }
        }
        //保存复用文件日志
        asyncBusiLogService.saveRepeatLog(ecmBusiDocExtend, token);
    }

    /**
     * 批量插入资料文件属性信息
     */
    private void insertEcmFileLables(List<EcmFileLabel> ecmFileLabels) {
        MybatisBatch<EcmFileLabel> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, ecmFileLabels);
        MybatisBatch.Method<EcmFileLabel> method = new MybatisBatch.Method<>(EcmFileLabelMapper.class);
        mybatisBatch.execute(method.insert());
    }

    /**
     * 批量插入资料文件属性信息
     */
    private void insertEcmFileExpireInfos(List<EcmFileExpireInfo> ecmFileExpireInfos) {
        MybatisBatch<EcmFileExpireInfo> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, ecmFileExpireInfos);
        MybatisBatch.Method<EcmFileExpireInfo> method = new MybatisBatch.Method<>(EcmFileExpireInfoMapper.class);
        mybatisBatch.execute(method.insert());
    }

    /**
     * 批量插入影像文件历史版本信息
     */
    private void insertEcmFileHistorys(List<EcmFileHistory> ecmFileHistoryList) {
        MybatisBatch<EcmFileHistory> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, ecmFileHistoryList);
        MybatisBatch.Method<EcmFileHistory> method = new MybatisBatch.Method<>(EcmFileHistoryMapper.class);
        mybatisBatch.execute(method.insert());
    }
    /**
     * 批量插入影像文件信息
     */
    private void insertEcmFileInfos(List<EcmFileInfo> ecmFileInfos1) {
        MybatisBatch<EcmFileInfo> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, ecmFileInfos1);
        MybatisBatch.Method<EcmFileInfo> method = new MybatisBatch.Method<>(EcmFileInfoMapper.class);
        mybatisBatch.execute(method.insert());
    }

    /**
     * 提取文件扩展名
     */
    private String getExt(FileInfoRedisDTO fileInfoRedisDTO) {
        int lastIndex = fileInfoRedisDTO.getNewFileName().lastIndexOf('.');
        return fileInfoRedisDTO.getNewFileName().substring(lastIndex + 1);
    }

    /**
     * 权限校验
     */
    private void checkDocRightByMultiplex(List<FileInfoRedisDTO> fileInfoRedisDTOList, FileInfoRedisEntityVO ecmBusiDocExtend, String operation, AccountTokenExtendDTO token) {
        if (CollectionUtils.isEmpty(fileInfoRedisDTOList)) {
            return;
        }
        List<FileInfoRedisDTO> collect = fileInfoRedisDTOList.stream().filter(s -> s.getDocCode() != null && s.getDocCode().equals(ecmBusiDocExtend.getSourceDocTypeId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            return;
        }
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, ecmBusiDocExtend.getSourceBusiId());
        EcmBusiStructureTreeDTO ecmBusiStructureTreeDTO = new EcmBusiStructureTreeDTO();
        ecmBusiStructureTreeDTO.setDocCode(ecmBusiDocExtend.getSourceDocTypeId());
        ecmBusiStructureTreeDTO.setName(fileInfoRedisDTOList.get(0).getDocName());
        ecmBusiStructureTreeDTO.setBusiId(ecmBusiDocExtend.getSourceBusiId());
        //文件权限从关联版本获取最新权限
        List<EcmDocrightDefDTO> currentDocRight = busiCacheService.getDocrightDefCommon(ecmBusiInfoRedisDTO, token);
//        ecmBusiInfoRedisDTO.setDocRightList(currentDocRight);
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
        commonService.checkDocRightTarget(fileInfoRedisDTOList, ecmBusiInfoRedisDTO, ecmBusiStructureTreeDTO, operation, true, fileInfoRedis, currentDocRight);
    }

    /**
     * 复用弹框搜素
     */
    public PageInfo multiplexFileSearch(BusiInfoVO busiInfoVo, AccountTokenExtendDTO token) {
        List<Long> busiIds = new ArrayList<>();
        // 如果不传任何查询条件值默认返回空列表
        if (ObjectUtils.isEmpty(busiInfoVo.getAppCode()) && ObjectUtils.isEmpty(busiInfoVo.getBusiNo()) && ObjectUtils.isEmpty(busiInfoVo.getCreateUser())
                && ObjectUtils.isEmpty(busiInfoVo.getCreateTimeStart()) && ObjectUtils.isEmpty(busiInfoVo.getCreateTimeEnd()) && CollectionUtils.isEmpty(busiInfoVo.getAttrList())) {
            return new PageInfo();
        }
        //创建人条件检索
        //业务属性条件检索
        if (!CollectionUtils.isEmpty(busiInfoVo.getAttrList())) {
            AssertUtils.isNull(busiInfoVo.getAppCode(), "业务类型不能为空");
            List<EcmAppAttrDTO> filterAttr = busiInfoVo.getAttrList().stream().filter(p -> !ObjectUtils.isEmpty(p.getAppAttrValue())).collect(Collectors.toList());
            if (!org.springframework.util.CollectionUtils.isEmpty(filterAttr)) {
                busiIds = ecmBusiMetadataMapper.complexSelect(filterAttr, filterAttr.size(), Collections.singletonList(busiInfoVo.getAppCode()));
                busiIds.add(-Long.MAX_VALUE);
            }
        } else {
            AssertUtils.isNull(busiInfoVo.getBusiNo(), "业务主索引不能为空");
        }
        //搜索
//        PageHelper.startPage(busiInfoVo.getPageNum(), busiInfoVo.getPageSize());
        Integer pageSize = (busiInfoVo.getPageSize());
        Integer size = (busiInfoVo.getPageNum() - 1) * pageSize;
        //查询出该角色拥有查看权限的业务类型代码
        List<String> appCodeList;
        if (ObjectUtils.isEmpty(busiInfoVo.getAppCode())) {
            Set<String> appCodeHaveByToken = staticTreePermissService.getAppCodeHaveByToken(null, token, "read");
//            List<String> collect = ecmAppDefMapper.selectList(null).stream().map(EcmAppDef::getAppCode).collect(Collectors.toList());
            appCodeList = new ArrayList<>(appCodeHaveByToken);
        } else {
            appCodeList = Collections.singletonList(busiInfoVo.getAppCode());
        }
        //所以拥有权限的业务
        PageHelper.startPage(busiInfoVo.getPageNum(), busiInfoVo.getPageSize());
        List<EcmBusiInfoDTO> ecmBusiInfos = ecmBusiInfoMapper.selecAppTypetList2(busiInfoVo, appCodeList, busiIds);
        if (CollectionUtil.isEmpty(ecmBusiInfos)) {
            return new PageInfo();
        }
        PageInfo pageInfo = new PageInfo<>(ecmBusiInfos);
        long l = ecmBusiInfoMapper.selecetAppTypetListCounts(busiInfoVo, appCodeList, busiIds);
        pageInfo.setTotal(l);
        return pageInfo;
    }

    private void getListByTree(List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS,List<EcmBusiDocRedisDTO> all){
        for(EcmBusiDocRedisDTO dto:ecmBusiDocRedisDTOS){

            if(!CollectionUtils.isEmpty(dto.getChildren())){
                getListByTree(dto.getChildren(),all);
            }else{
                dto.setIsParent(StateConstants.NO);
                all.add(dto);
            }
        }
    }

    /**
     * 复用弹框搜索-资料下拉列表(全部最子级)
     */
    public Result multiplexFileSearchDoc(BusiInfoVO busiInfoVo, AccountTokenExtendDTO token) {
        AssertUtils.isNull(busiInfoVo.getBusiId(), "业务主键id不可为空!");
        List<EcmBusiStructureTreeDTO> ecmBusiStructureTreeDTOS = new ArrayList<>();
        EcmStructureTreeDTO ecmStructureTreeDTO = new EcmStructureTreeDTO();
        List<Long> busiIdList = new ArrayList<>();
        busiIdList.add(busiInfoVo.getBusiId());
        ecmStructureTreeDTO.setBusiIdList(busiIdList);
        ecmStructureTreeDTO.setPageFlag(busiInfoVo.getPageFlag());
        ecmStructureTreeDTO.setIsDeleted(IcmsConstants.ZERO);
//        List<EcmBusiStructureTreeDTO> busiStructureTree = getBusiStructureTree(token.getUsername(), ecmStructureTreeDTO, token);
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO1 = busiCacheService.getEcmBusiInfoRedisDTO(token, busiInfoVo.getBusiId());
        //获取权限列表
        List<EcmDocrightDefDTO> ecmDocRightDefs=busiCacheService.getDocrightDefCommon(ecmBusiInfoRedisDTO1,token);
        Set<String> validDocCodeSet = ecmDocRightDefs.stream()
                .filter(Objects::nonNull)
                .filter(def -> StateConstants.YES.toString().equals(def.getReadRight()))  // 过滤readRight等于1的元素
                .map(EcmDocrightDefDTO::getDocCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if(ecmBusiInfoRedisDTO1==null){
            return  Result.success();
        }
            List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = ecmBusiInfoRedisDTO1.getEcmBusiDocRedisDTOS();
            ArrayList<EcmBusiDocRedisDTO> all = new ArrayList<>();
            getListByTree(ecmBusiDocRedisDTOS,all);
            List<EcmBusiDocRedisDTO> collect = all.stream().filter(s -> StateConstants.NO.equals(s.getIsParent())).collect(Collectors.toList());
            List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(busiInfoVo.getBusiId());
            Map<String, List<FileInfoRedisDTO>>  collect1 = new HashMap<>();
            Integer unclassCount = null;
            if(!CollectionUtils.isEmpty(fileInfoRedis)){
                collect1 = fileInfoRedis.stream().filter(s->StateConstants.NO.equals(s.getState())).collect(Collectors.groupingBy(FileInfoRedisDTO::getDocCode));
                List<FileInfoRedisDTO> fileInfoRedisDTOS = collect1.get(IcmsConstants.UNCLASSIFIED_ID);
                if(!CollectionUtils.isEmpty(fileInfoRedisDTOS)){
                    unclassCount = fileInfoRedisDTOS.size();
                }
            }

        for(EcmBusiDocRedisDTO ecmBusiDocRedisDTO: collect) {
            String currentDocCode = ecmBusiDocRedisDTO.getDocCode();
            // 判断当前docCode是否在有效Set集合中
            if (validDocCodeSet.contains(currentDocCode)) {
                EcmBusiStructureTreeDTO ecmStructureTreeDTOs = new EcmBusiStructureTreeDTO();
                ecmStructureTreeDTOs.setAppCode(ecmBusiInfoRedisDTO1.getAppCode());
                ecmStructureTreeDTOs.setId(currentDocCode);
                ecmStructureTreeDTOs.setBusiId(busiInfoVo.getBusiId());
                ecmStructureTreeDTOs.setDocCode(currentDocCode);
                ecmStructureTreeDTOs.setName(ecmBusiDocRedisDTO.getDocName());
                ecmStructureTreeDTOs.setFileCount(0);

                if(!CollectionUtils.isEmpty(collect1.get(currentDocCode))){
                    ecmStructureTreeDTOs.setFileCount(collect1.get(currentDocCode).size());
                }
                //添加文件信息
                ecmStructureTreeDTOs.setResultFiles(collect1.get(currentDocCode));
                ecmBusiStructureTreeDTOS.add(ecmStructureTreeDTOs);
            }
        }

            if(unclassCount!=null){
                EcmBusiStructureTreeDTO und = new EcmBusiStructureTreeDTO();
                und.setAppCode(ecmBusiInfoRedisDTO1.getAppCode());
                und.setId(IcmsConstants.UNCLASSIFIED_ID);
                und.setBusiId(busiInfoVo.getBusiId());
                und.setFileCount(unclassCount);
                und.setName(IcmsConstants.UNCLASSIFIED);
                und.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
                und.setResultFiles(collect1.get(IcmsConstants.UNCLASSIFIED_ID));
                ecmBusiStructureTreeDTOS.add(und);
            }


//        if (!CollectionUtils.isEmpty(busiStructureTree)) {
//            //得到该业务资料树
//            List<EcmBusiStructureTreeDTO> children = busiStructureTree.get(StateConstants.ZERO).getChildren();
//            //判断是否有查看该资料的权限
//            checkDocReadRight(children);
//            //一个业务的children中只显示叶子节点
//            //叶子节点集合
//            //业务下所有的资料节点
//            if (!CollectionUtils.isEmpty(children)) {
//                List<EcmBusiStructureTreeDTO> children1 = children.get(StateConstants.ZERO).getChildren();
//                //递归得到叶子节点集合
//                handleEcmBusiStructureTreeDTO(children1, ecmBusiStructureTreeDTOS);
//                //将叶子节点复制给业务的children
//                children.get(StateConstants.ZERO).setChildren(ecmBusiStructureTreeDTOS);
//            }
//        }

        return Result.success(ecmBusiStructureTreeDTOS);
    }

    /**
     * 获取上传大小
     */
    public Result getUploadChunkSize() {
        return fileStorageApi.getUploadChunkSize();
    }

    /**
     * 编辑业务
     */
    @WebsocketNoticeAnnotation(busiId = "#ecmBusiInfoExtend.busiId")
    public void editBusi(EcmBusiInfoRedisDTO ecmBusiInfoExtend, AccountTokenExtendDTO token, EcmBusExtendDTO busExtendDTO, Map<Long, List<EcmDocrightDefDTO>> map, Integer isRetransmission) {
        AssertUtils.isTrue(IcmsConstants.SHOW_PAGE.equals(token.getIsShow()),"当前无权限,无法进行采集相关操作");
        //校验入参数
        checkPararmEditBusi(ecmBusiInfoExtend);
        //多维度或角色
        List<EcmDocrightDefDTO> ecmDocrightDefDTOS = commonService.dealRuleData(busExtendDTO, token, ecmBusiInfoExtend.getRightVer());
        if (map != null) {
            //如果是补传把权限处理为只有新增权限
            if(IcmsConstants.ONE.equals(isRetransmission)||IcmsConstants.TWO.equals(isRetransmission)){
                dealRetransmission(isRetransmission, ecmDocrightDefDTOS);
            }
            map.put(ecmBusiInfoExtend.getBusiId(), ecmDocrightDefDTOS);
        }
//        ecmBusiInfoExtend.setDocRightList(ecmDocrightDefDTOS);
        //更新redis缓存数据
        Date updateTime = updateBusiInfoToRedis(ecmBusiInfoExtend, token);
        if (updateTime != null) {
            //更新持久化数据库数据
            updateBusiInfoToDb(ecmBusiInfoExtend, token, updateTime);
            //更新es数据
            //添加到es
            ecmBusiInfoExtend.setUpdateUserName(token.getName());
            operateFullQueryService.editEsBusiInfo(ecmBusiInfoExtend, token.getUsername(), updateTime);
            //更新反欺诈的数据
            fileInfoService.changeAfmData(ecmBusiInfoExtend.getBusiId(),ecmBusiInfoExtend.getBusiNo(),null,IcmsConstants.ONE);
        }
        // 检查关户状态下的角色权限
        checkCloseAccountPermission(ecmBusiInfoExtend.getAttrList(), token, false);
    }

    /**
     * 检查已关户状态下的角色权限
     * @param attrList 业务属性列表
     * @param token 当前用户token
     * @param flag 是否抛异常
     */
    private void checkCloseAccountPermission(List<EcmAppAttrDTO> attrList, AccountTokenExtendDTO token, boolean flag) {
        // 过滤出 GHZT 属性
        List<EcmAppAttrDTO> ghzt = attrList.stream()
                .filter(item -> item.getAttrCode() != null && item.getAttrCode().equalsIgnoreCase(IcmsConstants.GHZT))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(ghzt)) {
            String appAttrValue = ghzt.get(0).getAppAttrValue();
            if (IcmsConstants.CLOSE_STATE_STR.equals(appAttrValue)) {
                checkCloseAccountRolePermission(ghzt.get(0).getAppCode(), token, flag);
            }
        }
    }

    /**
     * 检查已关户业务是否有权限查看
     * @param appCode 业务类型 code
     * @param token 当前登录用户 token
     * @param flag 是否抛异常
     */
    private void checkCloseAccountRolePermission(String appCode, AccountTokenExtendDTO token, boolean flag) {
        // 查询当前业务配置的角色
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectById(appCode);
        String roleIdsStr = ecmAppDef.getRoleIds();

        // 如果数据库没有配置角色，直接无权限
        if (StrUtil.isEmpty(roleIdsStr)) {
            redisUtils.del(RedisConstants.USER_BUSI_PREFIX + token.getFlagId());
            if (flag) {
                throw new SunyardException(ResultCode.NO_READ_AUTH, "当前已关户,暂无权限查看!");
            }
        } else {
            // 拆分数据库角色 ID 字符串为 List<Long>
            List<Long> configRoleIds = Arrays.stream(roleIdsStr.split(","))
                    .map(String::trim)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            // 当前登录用户的角色 ID 列表
            List<Long> userRoleIds = token.getRoleIdList();

            // 判断用户角色是否包含至少一个配置的角色
            boolean hasPermission = configRoleIds.stream()
                    .anyMatch(userRoleIds::contains);

            // 如果不包含任何一个配置的角色
            if (!hasPermission) {
                redisUtils.del(RedisConstants.USER_BUSI_PREFIX + token.getFlagId());
                if (flag) {
                    throw new SunyardException(ResultCode.NO_READ_AUTH, "当前已关户,暂无权限查看!");
                }
            }
        }
    }

    /**
     * 编辑业务
     */
    @WebsocketNoticeAnnotation(busiId = "#ecmBusiInfoExtend.busiId")
    public void editBusi(EcmBusiInfoRedisDTO ecmBusiInfoExtend, AccountTokenExtendDTO token) {
        editBusi(ecmBusiInfoExtend, token, null, null, null);
    }

    /**
     * 删除业务类型节点
     */
    //@WebsocketNoticeAnnotation
    @Transactional(rollbackFor = Exception.class)
    public void deleteAppTypeNode(String appCode, AccountTokenExtendDTO token) {
        String userId = token.getUsername();
        AssertUtils.isNull(appCode, "参数错误");
        AssertUtils.isNull(userId, "参数错误");
        //todo
        UserBusiRedisDTO userBusiRedisDTO = busiCacheService.getUserPageRedis(userId, token);
        //过滤出要删除的appTypeId
        List<String> filterAppTypeIds = userBusiRedisDTO.getAppType().stream().filter(p -> !p.equals(appCode)).collect(Collectors.toList());
        //需要清除的busiIds
        List<Long> delBusiIds = new ArrayList<>();
        List<AppTypeBusiVO> filterAppTypeBusiVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(userBusiRedisDTO.getRelation())) {
            for (AppTypeBusiVO appTypeBusiVo : userBusiRedisDTO.getRelation()) {
                if (appTypeBusiVo.getAppCode().equals(appCode)) {
                    if (!CollectionUtils.isEmpty(appTypeBusiVo.getBusiIds())) {
                        delBusiIds.addAll(appTypeBusiVo.getBusiIds());
                    }
                } else {
                    filterAppTypeBusiVOS.add(appTypeBusiVo);
                }
            }
        }
        //过滤出要删除的busiIds
        List<Long> filterBusiIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(userBusiRedisDTO.getBusiId())) {
            filterBusiIds = userBusiRedisDTO.getBusiId().stream().filter(p -> !delBusiIds.contains(p)).collect(Collectors.toList());
        }
        if (!CollectionUtils.isEmpty(delBusiIds)) {
            //删除对应的业务
            captureScanService.deleteBatch(delBusiIds, token);
        }
        //更新用户对应的redis缓存
        userBusiRedisDTO.setUsercode(userId);
        userBusiRedisDTO.setAppType(filterAppTypeIds);
        userBusiRedisDTO.setBusiId(filterBusiIds);
        userBusiRedisDTO.setRelation(filterAppTypeBusiVOS);
        //todo
        busiCacheService.saveOrUpdateUser(userId, userBusiRedisDTO);
    }

    /**
     * redis更新用户-业务相关信息
     */
    public UserBusiRedisDTO updateUserBusiToRedis(EcmBusiInfoRedisDTO ecmBusiInfoExtend, AccountTokenExtendDTO token, EcmRootDataDTO ecmRootDataDTO) {
        //获取当前用户影像收集关联的业务类型id和业务id缓存
        if (ecmBusiInfoExtend.getPageFlag() == null) {
            String pageFlag = UUIDUtils.generateUUID();
            ecmBusiInfoExtend.setPageFlag(pageFlag);
        }
        UserBusiRedisDTO userBusiEntity = busiCacheService.getUserPageRedis(ecmBusiInfoExtend.getPageFlag(), token);
        if (userBusiEntity != null) {
            if (CollectionUtils.isEmpty(userBusiEntity.getBusiId())) {
                List<Long> busiId = new ArrayList<>();
                busiId.add(ecmBusiInfoExtend.getBusiId());
                userBusiEntity.setBusiId(busiId);
            } else {
                List<Long> busiId = userBusiEntity.getBusiId();
                //校验当前添加的业务id是否存在，存在报提醒
                if (!busiId.contains(ecmBusiInfoExtend.getBusiId())) {
                    busiId.add(ecmBusiInfoExtend.getBusiId());
                    userBusiEntity.setBusiId(busiId);
                }
            }
            //添加业务类型
            if (CollectionUtils.isEmpty(userBusiEntity.getAppType())) {
                List<String> appcode = new ArrayList<>();
                appcode.add(ecmBusiInfoExtend.getAppCode());
                userBusiEntity.setAppType(appcode);
            } else {
                List<String> apps = userBusiEntity.getAppType();
                //校验当前添加的业务id是否存在，存在报提醒
                if (!apps.contains(ecmBusiInfoExtend.getAppCode())) {
                    apps.add(ecmBusiInfoExtend.getAppCode());
                    userBusiEntity.setAppType(apps);
                }
            }

            //添加业务类型对应业务的关联关系
            if (CollectionUtils.isEmpty(userBusiEntity.getRelation())) {
                List<AppTypeBusiVO> appTypeBusiVOS = new ArrayList<>();
                AppTypeBusiVO appTypeBusiVo = new AppTypeBusiVO();
                appTypeBusiVo.setAppCode(ecmBusiInfoExtend.getAppCode());
                List<Long> busiIds = new ArrayList<>();
                busiIds.add(ecmBusiInfoExtend.getBusiId());
                appTypeBusiVo.setBusiIds(busiIds);
                appTypeBusiVOS.add(appTypeBusiVo);
                userBusiEntity.setRelation(appTypeBusiVOS);
            } else {
                for (AppTypeBusiVO appTypeBusiVo : userBusiEntity.getRelation()) {
                    if (appTypeBusiVo.getAppCode().equals(ecmBusiInfoExtend.getAppCode())) {
                        List<Long> busiIds = appTypeBusiVo.getBusiIds();
                        if (CollectionUtils.isEmpty(busiIds)) {
                            busiIds = new ArrayList<>();
                        }
                        busiIds.add(ecmBusiInfoExtend.getBusiId());
                        appTypeBusiVo.setBusiIds(busiIds);
                    }
                }
            }


            if (ecmRootDataDTO != null) {
                userBusiEntity.setUsercode(ecmRootDataDTO.getEcmBaseInfoDTO().getUserCode());
                userBusiEntity.setUsername(ecmRootDataDTO.getEcmBaseInfoDTO().getUserName());
                userBusiEntity.setRole(ecmRootDataDTO.getEcmBaseInfoDTO().getRoleCode());
                userBusiEntity.setOrg(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode());
                userBusiEntity.setOrgName(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgName());
                ecmRootDataDTO.setFlagId(token.getUsername() + ecmBusiInfoExtend.getPageFlag());
            }
            Map<Long, List<EcmDocrightDefDTO>> docRightList = userBusiEntity.getDocRightList();
            List<EcmDocrightDefDTO> ecmDocrightDefDTOS = busiCacheService.getEcmDocrightDefDTOS(token, ecmBusiInfoExtend);
            if (docRightList != null && !CollectionUtils.isEmpty(docRightList.keySet())) {
                docRightList.put(ecmBusiInfoExtend.getBusiId(), ecmDocrightDefDTOS);
            } else {
                docRightList = new HashMap<>();
                docRightList.put(ecmBusiInfoExtend.getBusiId(), ecmDocrightDefDTOS);
            }
            userBusiEntity.setDocRightList(docRightList);
            //权限
            //更新当前用户影像收集关联的业务类型id和业务id缓存
            busiCacheService.saveOrUpdateUser(ecmBusiInfoExtend.getPageFlag(), userBusiEntity);
            return userBusiEntity;
        } else {
            UserBusiRedisDTO userBusiRedisDTO = new UserBusiRedisDTO();
            List<String> appCode = new ArrayList<>();
            List<Long> busiId = new ArrayList<>();
            appCode.add(ecmBusiInfoExtend.getAppCode());
            busiId.add(ecmBusiInfoExtend.getBusiId());
            userBusiRedisDTO.setAppType(appCode);
            userBusiRedisDTO.setBusiId(busiId);
            //添加业务类型对应业务的关联关系
            List<AppTypeBusiVO> appTypeBusiVOS = new ArrayList<>();
            AppTypeBusiVO appTypeBusiVo = new AppTypeBusiVO();
            appTypeBusiVo.setAppCode(ecmBusiInfoExtend.getAppCode());
            List<Long> busiIds = new ArrayList<>();
            busiIds.add(ecmBusiInfoExtend.getBusiId());
            appTypeBusiVo.setBusiIds(busiIds);
            appTypeBusiVOS.add(appTypeBusiVo);
            userBusiRedisDTO.setRelation(appTypeBusiVOS);
            //新增当前用户影像收集关联的业务类型id和业务id缓存
            if (ecmRootDataDTO != null) {
                userBusiRedisDTO.setUsercode(ecmRootDataDTO.getEcmBaseInfoDTO().getUserCode());
                userBusiRedisDTO.setUsername(ecmRootDataDTO.getEcmBaseInfoDTO().getUserName());
                userBusiRedisDTO.setOrg(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgCode());
                userBusiRedisDTO.setOrgName(ecmRootDataDTO.getEcmBaseInfoDTO().getOrgName());
                userBusiRedisDTO.setRole(ecmRootDataDTO.getEcmBaseInfoDTO().getRoleCode());
                ecmRootDataDTO.setFlagId(token.getUsername() + ecmBusiInfoExtend.getPageFlag());
            }
            busiCacheService.saveOrUpdateUser(ecmBusiInfoExtend.getPageFlag(), userBusiRedisDTO);
            return userBusiRedisDTO;

        }
    }

    /**
     * 获取业务属性信息根据业务编号
     */
    public Object getBusiAttrInfo(String appCode, String busiNo) {
        HashMap<Object, Object> map = new HashMap<>();
        if (ObjectUtils.isEmpty(appCode) || ObjectUtils.isEmpty(busiNo)) {
            return null;
        }
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectOne(new LambdaQueryWrapper<EcmBusiInfo>().eq(EcmBusiInfo::getAppCode, appCode).eq(EcmBusiInfo::getBusiNo, busiNo));
        if (ObjectUtils.isEmpty(ecmBusiInfo)) {
            return null;
        }
        //获取业务属性和值列表
        List<EcmAppAttrDTO> appAttrExtends = busiCacheService.getAppAttrExtends(appCode, ecmBusiInfo.getBusiId());
        if (CollectionUtils.isEmpty(appAttrExtends)) {
            return null;
        }
        appAttrExtends.forEach(p -> map.put(p.getAttrCode(), p.getAppAttrValue()));
        return map;
    }

    /**
     * 获取影像业务属性
     */
    public EcmAppAttr getAppMainAttr(String appCode) {
        if (ObjectUtils.isEmpty(appCode)) {
            return null;
        }
        LambdaQueryWrapper<EcmAppAttr> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(EcmAppAttr::getAppCode,appCode).eq(EcmAppAttr::getIsKey,IcmsConstants.ONE);
        EcmAppAttr ecmAppAttr=ecmAppAttrMapper.selectOne(lambdaQueryWrapper);
        return ecmAppAttr;
    }

    /**
     * 影像复用确认API
     */
    public void onlineByFileId(HttpServletResponse response, Long fileId) {
        AssertUtils.isNull(fileId, "参数错误");
        List<Long> fileIds = new ArrayList<>();
        fileIds.add(fileId);
        Result<List<SysFileDTO>> details = fileHandleApi.details(fileIds);
        if (!details.isSucc()) {
            throw new SunyardException(details.getMsg());
        }
        List<SysFileDTO> fileDetails = details.getData();
        AssertUtils.isNull(fileDetails, "文件丢失");

        FileCloudDTO fileCloudDTO = new FileCloudDTO();
        fileCloudDTO.setOriginalFilename(fileDetails.get(0).getOriginalFilename());
        fileCloudDTO.setUrl(fileDetails.get(0).getUrl());
        fileCloudDTO.setExt(fileDetails.get(0).getExt());
        //在线预览(暂无水印)
        OnlineUtils.online(response, fileCloudDTO, OnlineConstants.WATER_FLAG_NO, null, "utf-8");
    }

    /**
     * 获取压缩包内文件树，结构与 getBusiStructureTree 一致，便于前端复用同一树组件。
     *
     * @param fileId 影像文件id
     * @param token  登录态
     * @return 树根列表，仅一个根节点（压缩包），其 children 为顶层目录/文件
     */
    public List<EcmBusiStructureTreeDTO> getArchiveStructureTree(Long fileId, AccountTokenExtendDTO token) {
        AssertUtils.isNull(fileId, "fileId不能为空");
        EcmFileInfo ecmFileInfo = ecmFileInfoMapper.selectOne(new LambdaQueryWrapper<EcmFileInfo>()
                .eq(EcmFileInfo::getFileId, fileId));
        List<Long> fileIds = new ArrayList<>();
        fileIds.add(ecmFileInfo.getNewFileId());
        Result<List<SysFileDTO>> details = fileHandleApi.details(fileIds);
        if (!details.isSucc()) {
            throw new SunyardException(details.getMsg());
        }
        List<SysFileDTO> fileDetails = details.getData();
        AssertUtils.isNull(fileDetails, "文件丢失");
        SysFileDTO file = fileDetails.get(0);
        String archiveFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "压缩包";
        String ext = file.getExt() != null ? file.getExt().toLowerCase() : "";
        String lowerName = archiveFileName.toLowerCase();
        
        // 判断文件格式:支持 zip, rar, gzip (gz), 7z
        String formatType = null;
        if (lowerName.endsWith(".zip") || "zip".equals(ext)) {
            formatType = "zip";
        } else if (lowerName.endsWith(".rar") || "rar".equals(ext)) {
            formatType = "rar";
        } else if (lowerName.endsWith(".gz") || lowerName.endsWith(".gzip") || "gz".equals(ext) || "gzip".equals(ext)) {
            formatType = "gzip";
        } else if (lowerName.endsWith(".7z") || "7z".equals(ext)) {
            formatType = "7z";
        } else {
            throw new SunyardException("仅支持ZIP、RAR、GZIP、7Z格式压缩包，当前文件格式不支持");
        }

        FileByteVO vo = new FileByteVO();
        vo.setFileId(file.getId());
        Result<byte[]> bytesResult = fileHandleApi.getFileBytes(vo);
        if (!bytesResult.isSucc() || bytesResult.getData() == null) {
            throw new SunyardException("无法读取压缩包内容");
        }
        byte[] archiveBytes = bytesResult.getData();
        if (archiveBytes.length < 4) {
            throw new SunyardException("文件过小或已损坏，无法解析");
        }

        Map<String, EcmBusiStructureTreeDTO> pathToNode = new LinkedHashMap<>();
        EcmBusiStructureTreeDTO root = new EcmBusiStructureTreeDTO();
        root.setId("root");
        root.setName(archiveFileName);
        root.setType(IcmsConstants.TREE_TYPE_ARCHIVE_ROOT);
        root.setNodeType(0);
        root.setPid(StateConstants.ZERO.toString());
        root.setChildren(new ArrayList<>());
        pathToNode.put("", root);

        try {
            if ("zip".equals(formatType)) {
                parseZipArchive(archiveBytes, pathToNode, root);
            } else if ("gzip".equals(formatType)) {
                parseGzipArchive(archiveBytes, pathToNode, root, archiveFileName);
            } else if ("rar".equals(formatType)) {
                // 直接解析 RAR 文件
                parseRarArchive(archiveBytes, pathToNode, root);
            } else if ("7z".equals(formatType)) {
                parse7zArchive(archiveBytes, pathToNode, root);
            }
        } catch (ZipException ex) {
            throw new SunyardException("文件不是有效的" + formatType.toUpperCase() + "格式或已损坏，请确认该文件为" + formatType.toUpperCase() + "压缩包");
        } catch (SevenZipException ex) {
            throw new SunyardException("RAR/7Z文件解析失败：" + (ex.getMessage() != null ? ex.getMessage() : "未知错误") + "，可能是文件加密或格式不支持");
        } catch (Exception ex) {
            throw new SunyardException("解析压缩包失败：" + (ex.getMessage() != null ? ex.getMessage() : "未知错误"));
        }

        return Collections.singletonList(root);
    }

    /**
     * 解析ZIP格式压缩包
     */
    private void parseZipArchive(byte[] zipBytes, Map<String, EcmBusiStructureTreeDTO> pathToNode, 
                                  EcmBusiStructureTreeDTO root) throws Exception {
        // 验证ZIP文件头
        int m1 = zipBytes[0] & 0xFF;
        int m2 = zipBytes[1] & 0xFF;
        if (m1 != 'P' || m2 != 'K') {
            throw new SunyardException("文件不是有效的ZIP格式或已损坏（可能为加密存储），请确认该文件为ZIP压缩包");
        }
        
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                String path = e.getName().replace('\\', '/');
                path = path.substring(path.indexOf("/")+1);
                if (path.isEmpty()) {
                    zis.closeEntry();
                    continue;
                }
                boolean isDir = e.isDirectory() || path.endsWith("/");
                if (isDir) {
                    path = path.replaceAll("/+$", "");
                }
                if (path.isEmpty()) {
                    zis.closeEntry();
                    continue;
                }
                String[] parts = path.split("/");
                String parentPath = parts.length == 1 ? "" : path.substring(0, path.lastIndexOf('/'));
                String name = parts[parts.length - 1];

                ensureArchiveParentPath(pathToNode, parentPath, root);

                EcmBusiStructureTreeDTO node = new EcmBusiStructureTreeDTO();
                node.setId(path);
                node.setName(name);
                node.setType(isDir ? IcmsConstants.TREE_TYPE_ARCHIVE_DIR : IcmsConstants.TREE_TYPE_ARCHIVE_FILE);
                node.setNodeType(isDir ? 0 : 1);
                node.setPid(parentPath.isEmpty() ? "archive_root" : parentPath);
                node.setChildren(isDir ? new ArrayList<>() : null);

                pathToNode.put(path, node);
                EcmBusiStructureTreeDTO parent = parentPath.isEmpty() ? root : pathToNode.get(parentPath);
                if (parent != null && parent.getChildren() != null) {
                    parent.getChildren().add(node);
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * 解析GZIP格式压缩包
     * GZIP通常压缩单个文件，如果是.tar.gz则需要先解压gzip再解压tar
     */
    private void parseGzipArchive(byte[] gzipBytes, Map<String, EcmBusiStructureTreeDTO> pathToNode, 
                                   EcmBusiStructureTreeDTO root, String originalFileName) throws Exception {
        // 验证GZIP文件头
        int m1 = gzipBytes[0] & 0xFF;
        int m2 = gzipBytes[1] & 0xFF;
        if (m1 != 0x1f || m2 != 0x8b) {
            throw new SunyardException("文件不是有效的GZIP格式或已损坏");
        }
        
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(gzipBytes))) {
            // 检查是否是tar.gz格式
            if (originalFileName.toLowerCase().endsWith(".tar.gz") || originalFileName.toLowerCase().endsWith(".tgz")) {
                // 解析tar归档
                try (TarArchiveInputStream tis = new TarArchiveInputStream(gis)) {
                    ArchiveEntry entry;
                    while ((entry = tis.getNextEntry()) != null) {
                        String path = entry.getName().replace('\\', '/');
                        if (path.isEmpty()) {
                            continue;
                        }
                        boolean isDir = entry.isDirectory() || path.endsWith("/");
                        if (isDir) {
                            path = path.replaceAll("/+$", "");
                        }
                        if (path.isEmpty()) {
                            continue;
                        }
                        String[] parts = path.split("/");
                        String parentPath = parts.length == 1 ? "" : path.substring(0, path.lastIndexOf('/'));
                        String name = parts[parts.length - 1];

                        ensureArchiveParentPath(pathToNode, parentPath, root);

                        EcmBusiStructureTreeDTO node = new EcmBusiStructureTreeDTO();
                        node.setId(path);
                        node.setName(name);
                        node.setType(isDir ? IcmsConstants.TREE_TYPE_ARCHIVE_DIR : IcmsConstants.TREE_TYPE_ARCHIVE_FILE);
                        node.setNodeType(isDir ? 0 : 1);
                        node.setPid(parentPath.isEmpty() ? "archive_root" : parentPath);
                        node.setChildren(isDir ? new ArrayList<>() : null);

                        pathToNode.put(path, node);
                        EcmBusiStructureTreeDTO parent = parentPath.isEmpty() ? root : pathToNode.get(parentPath);
                        if (parent != null && parent.getChildren() != null) {
                            parent.getChildren().add(node);
                        }
                    }
                }
            } else {
                // 纯gzip文件，只包含一个解压后的文件
                String decompressedFileName = originalFileName;
                if (decompressedFileName.toLowerCase().endsWith(".gz")) {
                    decompressedFileName = decompressedFileName.substring(0, decompressedFileName.length() - 3);
                } else if (decompressedFileName.toLowerCase().endsWith(".gzip")) {
                    decompressedFileName = decompressedFileName.substring(0, decompressedFileName.length() - 5);
                }
                
                EcmBusiStructureTreeDTO node = new EcmBusiStructureTreeDTO();
                node.setId(decompressedFileName);
                node.setName(decompressedFileName);
                node.setType(IcmsConstants.TREE_TYPE_ARCHIVE_FILE);
                node.setNodeType(1);
                node.setPid("archive_root");
                node.setChildren(null);

                pathToNode.put(decompressedFileName, node);
                root.getChildren().add(node);
            }
        }
    }

    /**
     * 解析 7z 格式压缩包
     * 使用 sevenzipjbinding 库解析 7z 文件并构建目录树
     * 
     * @param archiveBytes 7z 文件的字节数组
     * @param pathToNode 路径到节点的映射
     * @param root 根节点
     */
    private void parse7zArchive(byte[] archiveBytes, Map<String, EcmBusiStructureTreeDTO> pathToNode,
                                 EcmBusiStructureTreeDTO root) throws Exception {
        // 验证 7z 文件头 (7z 魔数: 37 7A BC AF 27 1C)
        if (archiveBytes.length < 6) {
            throw new SunyardException("文件过小，不是有效的 7z 格式");
        }
        if ((archiveBytes[0] & 0xFF) != 0x37 || (archiveBytes[1] & 0xFF) != 0x7A ||
            (archiveBytes[2] & 0xFF) != 0xBC || (archiveBytes[3] & 0xFF) != 0xAF ||
            (archiveBytes[4] & 0xFF) != 0x27 || (archiveBytes[5] & 0xFF) != 0x1C) {
            throw new SunyardException("文件不是有效的 7z 格式或已损坏");
        }
        
        File temp7zFile = null;
        IInArchive archive = null;
        RandomAccessFile randomAccessFile = null;
        
        try {
            // 创建临时 7z 文件（sevenzipjbinding 需要从文件读取）
            temp7zFile = File.createTempFile("temp_7z_", ".7z");
            Files.write(temp7zFile.toPath(), archiveBytes);
            
            // 验证临时文件大小
            long fileSize = temp7zFile.length();
            if (fileSize != archiveBytes.length) {
                log.error("临时 7z 文件写入异常: 预期 {} 字节，实际 {} 字节", archiveBytes.length, fileSize);
                throw new SunyardException("7z 文件写入临时文件时发生错误");
            }
            
            log.info("开始使用 sevenzipjbinding 解析 7z 文件，大小: {} 字节", fileSize);
            
            // 打开 7z 归档
            randomAccessFile = new RandomAccessFile(temp7zFile, "r");
            archive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
            
            // 遍历所有条目
            int itemCount = archive.getNumberOfItems();
            for (int i = 0; i < itemCount; i++) {
                String path = (String) archive.getProperty(i, PropID.PATH);
                Boolean isDir = (Boolean) archive.getProperty(i, PropID.IS_FOLDER);

                //文件夹格式直接跳过
                if (isDir){
                    continue;
                }

                if (path == null || path.isEmpty()) {
                    continue;
                }
                path = path.substring(path.indexOf("\\")+1);

                // 标准化路径分隔符
                path = path.replace('\\', '/');
                if (isDir != null && isDir) {
                    path = path.replaceAll("/+$", "");
                }
                if (path.isEmpty()) {
                    continue;
                }
                
                String[] parts = path.split("/");
                String parentPath = parts.length == 1 ? "" : path.substring(0, path.lastIndexOf('/'));
                String name = parts[parts.length - 1];
                
                ensureArchiveParentPath(pathToNode, parentPath, root);
                
                EcmBusiStructureTreeDTO node = new EcmBusiStructureTreeDTO();
                node.setId(path);
                node.setName(name);
                node.setType(isDir != null && isDir ? IcmsConstants.TREE_TYPE_ARCHIVE_DIR : IcmsConstants.TREE_TYPE_ARCHIVE_FILE);
                node.setNodeType(isDir != null && isDir ? 0 : 1);
                node.setPid(parentPath.isEmpty() ? "archive_root" : parentPath);
                node.setChildren(isDir != null && isDir ? new ArrayList<>() : null);
                
                pathToNode.put(path, node);
                EcmBusiStructureTreeDTO parent = parentPath.isEmpty() ? root : pathToNode.get(parentPath);
                if (parent != null && parent.getChildren() != null) {
                    parent.getChildren().add(node);
                }
            }
            
            log.info("sevenzipjbinding 解析 7z 文件成功，共处理 {} 个条目", itemCount);
            
        } finally {
            // 释放资源
            if (archive != null) {
                try {
                    archive.close();
                } catch (Exception e) {
                    log.warn("关闭 7z 归档失败", e);
                }
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (Exception e) {
                    log.warn("关闭 7z 文件失败", e);
                }
            }
            // 清理临时文件
            if (temp7zFile != null && temp7zFile.exists()) {
                try {
                    Files.delete(temp7zFile.toPath());
                } catch (Exception e) {
                    log.warn("删除临时 7z 文件失败: {}", temp7zFile.getAbsolutePath(), e);
                }
            }
        }
    }

    /**
     * 解析 RAR 格式压缩包
     * 使用 sevenzipjbinding 库直接解析 RAR 文件并构建目录树
     * 
     * @param rarBytes RAR 文件的字节数组
     * @param pathToNode 路径到节点的映射
     * @param root 根节点
     */
    private void parseRarArchive(byte[] rarBytes, Map<String, EcmBusiStructureTreeDTO> pathToNode,
                                 EcmBusiStructureTreeDTO root) throws Exception {
        File tempRarFile = null;
        IInArchive archive = null;
        RandomAccessFile randomAccessFile = null;
        
        try {
            // 创建临时 RAR 文件（sevenzipjbinding 需要从文件读取）
            tempRarFile = File.createTempFile("temp_rar_", ".rar");
            Files.write(tempRarFile.toPath(), rarBytes);
            
            // 打开 RAR 归档
            randomAccessFile = new RandomAccessFile(tempRarFile, "r");
            archive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
            
            // 检查是否加密
            for (int i = 0; i < archive.getNumberOfItems(); i++) {
                Object isEncrypted = archive.getProperty(i, PropID.ENCRYPTED);
                if (isEncrypted instanceof Boolean && (Boolean) isEncrypted) {
                    throw new SunyardException("RAR文件已加密，不支持解析");
                }
            }
            
            // 遍历所有条目
            int itemCount = archive.getNumberOfItems();
            for (int i = 0; i < itemCount; i++) {
                String path = (String) archive.getProperty(i, PropID.PATH);
                Boolean isDir = (Boolean) archive.getProperty(i, PropID.IS_FOLDER);
                //文件夹直接跳过
                if (isDir){
                    continue;
                }
                
                if (path == null || path.isEmpty()) {
                    continue;
                }
                path = path.substring(path.indexOf("\\")+1);

                // 标准化路径分隔符
                path = path.replace('\\', '/');
                if (isDir != null && isDir) {
                    path = path.replaceAll("/+$", "");
                }
                if (path.isEmpty()) {
                    continue;
                }
                
                String[] parts = path.split("/");
                String parentPath = parts.length == 1 ? "" : path.substring(0, path.lastIndexOf('/'));
                String name = parts[parts.length - 1];
                
                ensureArchiveParentPath(pathToNode, parentPath, root);
                
                EcmBusiStructureTreeDTO node = new EcmBusiStructureTreeDTO();
                node.setId(path);
                node.setName(name);
                node.setType(isDir != null && isDir ? IcmsConstants.TREE_TYPE_ARCHIVE_DIR : IcmsConstants.TREE_TYPE_ARCHIVE_FILE);
                node.setNodeType(isDir != null && isDir ? 0 : 1);
                node.setPid(parentPath.isEmpty() ? "archive_root" : parentPath);
                node.setChildren(isDir != null && isDir ? new ArrayList<>() : null);
                
                pathToNode.put(path, node);
                EcmBusiStructureTreeDTO parent = parentPath.isEmpty() ? root : pathToNode.get(parentPath);
                if (parent != null && parent.getChildren() != null) {
                    parent.getChildren().add(node);
                }
            }
            
            log.info("sevenzipjbinding 解析 RAR 文件成功，共处理 {} 个条目", itemCount);
            
        } finally {
            // 释放资源
            if (archive != null) {
                try {
                    archive.close();
                } catch (Exception e) {
                    log.warn("关闭 RAR 归档失败", e);
                }
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (Exception e) {
                    log.warn("关闭 RAR 文件失败", e);
                }
            }
            // 清理临时文件
            if (tempRarFile != null && tempRarFile.exists()) {
                try {
                    Files.delete(tempRarFile.toPath());
                } catch (Exception e) {
                    log.warn("删除临时 RAR 文件失败: {}", tempRarFile.getAbsolutePath(), e);
                }
            }
        }
    }

    private void ensureArchiveParentPath(Map<String, EcmBusiStructureTreeDTO> pathToNode, String parentPath,
                                         EcmBusiStructureTreeDTO root) {
        if (parentPath.isEmpty() || pathToNode.containsKey(parentPath)) {
            return;
        }
        int last = parentPath.lastIndexOf('/');
        String grand = last < 0 ? "" : parentPath.substring(0, last);
        String name = last < 0 ? parentPath : parentPath.substring(last + 1);
        ensureArchiveParentPath(pathToNode, grand, root);
        EcmBusiStructureTreeDTO parent = new EcmBusiStructureTreeDTO();
        parent.setId(parentPath);
        parent.setName(name);
        parent.setType(IcmsConstants.TREE_TYPE_ARCHIVE_DIR);
        parent.setNodeType(0);
        parent.setPid(grand.isEmpty() ? "archive_root" : grand);
        parent.setChildren(new ArrayList<>());
        pathToNode.put(parentPath, parent);
        EcmBusiStructureTreeDTO grandNode = grand.isEmpty() ? root : pathToNode.get(grand);
        if (grandNode != null && grandNode.getChildren() != null) {
            grandNode.getChildren().add(parent);
        }
    }

    /**
     * 获取资料权限
     */
    private EcmDocrightDefDTO getDocRightAllOpen() {
        EcmDocrightDefDTO docrightDefExtend = new EcmDocrightDefDTO();
        docrightDefExtend.setAddRight(IcmsConstants.ONE.toString());
        docrightDefExtend.setReadRight(IcmsConstants.ONE.toString());
        docrightDefExtend.setUpdateRight(IcmsConstants.ONE.toString());
        docrightDefExtend.setDeleteRight(IcmsConstants.ONE.toString());
        docrightDefExtend.setThumRight(IcmsConstants.ONE.toString());
        docrightDefExtend.setPrintRight(IcmsConstants.ONE.toString());
        docrightDefExtend.setDownloadRight(IcmsConstants.ONE.toString());
        docrightDefExtend.setOtherUpdate(IcmsConstants.ONE.toString());
        return docrightDefExtend;
    }

    /**
      获取资料集合
     */
    private Set<String> getAllChildIdsByTree(String parentId, EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        if (IcmsConstants.UNCLASSIFIED_ID.equals(parentId)) {
            HashSet<String> objects = new HashSet<>();
            objects.add(IcmsConstants.UNCLASSIFIED_ID);
            return objects;
        } else if (IcmsConstants.DELETED_CODE.equals(parentId)) {
            HashSet<String> objects = new HashSet<>();
            objects.add(IcmsConstants.DELETED_CODE + "");
            return objects;
        }
        List<EcmBusiDocRedisDTO> ecmBusiDocRels = getAllSubNodeIds(ecmBusiInfoRedisDTO.getEcmBusiDocRedisDTOS(), Long.parseLong(parentId));
        if (!CollectionUtils.isEmpty(ecmBusiDocRels)) {
            List<Long> collect = ecmBusiDocRels.stream().map(EcmBusiDocRedisDTO::getDocId).collect(Collectors.toList());
            List<EcmBusiDoc> ecmBusiDocs = ecmBusiDocMapper.selectList(new LambdaQueryWrapper<EcmBusiDoc>().in(EcmBusiDoc::getDocId, collect));
            Set<String> collect1 = ecmBusiDocs.stream().map(EcmBusiDoc::getDocCode).collect(Collectors.toSet());
            return collect1;
        } else {
            //标记
            EcmBusiDoc ecmBusiDoc = ecmBusiDocMapper.selectById(parentId);
            if (ecmBusiDoc != null) {
                HashSet<String> objects = new HashSet<>();
                objects.add(ecmBusiDoc.getDocCode());
                return objects;
            }
            return null;
        }

    }


    /**
     * 根据指定节点ID获取该节点及其下所有子节点ID
     *
     * @param treeList    树结构列表
     * @param targetDocId 目标节点ID
     * @return 包含目标节点及其所有子节点ID的列表
     */
    private List<EcmBusiDocRedisDTO> getAllSubNodeIds(List<EcmBusiDocRedisDTO> treeList, Long targetDocId) {
        List<EcmBusiDocRedisDTO> result = new ArrayList<>();

        // 遍历树结构
        for (EcmBusiDocRedisDTO node : treeList) {
            // 如果找到目标节点
            if (node.getDocId().equals(targetDocId)) {
                // 添加当前节点ID
                result.add(node);
                // 递归添加所有子节点ID
                addAllChildrenIds(node, result);
                return result;
            }

            // 如果当前节点有子节点，递归查找
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                List<EcmBusiDocRedisDTO> subResult = getAllSubNodeIds(node.getChildren(), targetDocId);
                if (!subResult.isEmpty()) {
                    return subResult;
                }
            }
        }

        return result;
    }

    /**
     * 递归添加所有子节点ID
     * @param node 当前节点
     * @param idList ID列表
     */
    private void addAllChildrenIds(EcmBusiDocRedisDTO node, List<EcmBusiDocRedisDTO> idList) {
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (EcmBusiDocRedisDTO child : node.getChildren()) {
                idList.add(child);
                addAllChildrenIds(child, idList);
            }
        }
    }

    /**
     * 更新业务信息
     */
    public void updateBusiInfoToDb(EcmBusiInfoRedisDTO ecmBusiInfoExtend, AccountTokenExtendDTO token, Date updateTime) {
        List<EcmBusiMetadata> insertEcmBusiMetadatas = new ArrayList<>();
        List<EcmBusiMetadata> updateEcmBusiMetadatas = new ArrayList<>();
        //更新业务属性值表
        if (!CollectionUtils.isEmpty(ecmBusiInfoExtend.getAttrList())) {
            //删除属性值关联表
            List<EcmAppAttrDTO> attrList = ecmBusiInfoExtend.getAttrList();
            List<EcmBusiMetadata> ecmBusiMetadata1 = ecmBusiMetadataMapper.selectList(new LambdaQueryWrapper<EcmBusiMetadata>()
                    .eq(EcmBusiMetadata::getBusiId, ecmBusiInfoExtend.getBusiId()));
            Map<Long, List<EcmBusiMetadata>> collect = ecmBusiMetadata1.stream().collect(Collectors.groupingBy(EcmBusiMetadata::getAppAttrId));
            for (EcmAppAttrDTO s : attrList) {
                List<EcmBusiMetadata> ecmBusiMetadata2 = collect.get(s.getAppAttrId());
                if (CollectionUtil.isEmpty(ecmBusiMetadata2)) {
                    EcmBusiMetadata ecmBusiMetadata = new EcmBusiMetadata();
                    ecmBusiMetadata.setBusiId(ecmBusiInfoExtend.getBusiId());
                    ecmBusiMetadata.setAppAttrId(s.getAppAttrId());
                    ecmBusiMetadata.setAppAttrVal(s.getAppAttrValue());

                    insertEcmBusiMetadatas.add(ecmBusiMetadata);
                    continue;
                }
                EcmBusiMetadata ecmBusiMetadata3 = ecmBusiMetadata2.get(0);
                if (ecmBusiMetadata3.getAppAttrVal() != null && !ecmBusiMetadata3.getAppAttrVal().equals(s.getAppAttrValue())) {
                    ecmBusiMetadata3.setAppAttrVal(s.getAppAttrValue());
//                    ecmBusiMetadataMapper.updateById(ecmBusiMetadata3);
                    updateEcmBusiMetadatas.add(ecmBusiMetadata3);
                }
            }
        }
        saveAndUpdateInfos(ecmBusiInfoExtend, token, updateTime, insertEcmBusiMetadatas, updateEcmBusiMetadatas);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveAndUpdateInfos(EcmBusiInfoRedisDTO ecmBusiInfoExtend, AccountTokenExtendDTO token, Date updateTime, List<EcmBusiMetadata> insertEcmBusiMetadatas, List<EcmBusiMetadata> updateEcmBusiMetadatas) {
        //更新业务信息表
        ecmBusiInfoMapper.update(null, new UpdateWrapper<EcmBusiInfo>()
                .set("busi_no", ecmBusiInfoExtend.getBusiNo()).set("update_user", token.getUsername())
                .set("update_time", updateTime).set("update_user_name", token.getName())
                .eq("busi_id", ecmBusiInfoExtend.getBusiId()));

        MybatisBatch<EcmBusiMetadata> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, insertEcmBusiMetadatas);
        MybatisBatch.Method<EcmBusiMetadata> method = new MybatisBatch.Method<>(EcmBusiMetadataMapper.class);
        mybatisBatch.execute(method.insert());
        updateEcmBusiMetadatas.forEach(s->{
            ecmBusiMetadataMapper.updateById(s);
        });
        //添加操作记录表
        busiOperationService.addOperation(ecmBusiInfoExtend.getBusiId(), IcmsConstants.EDIT_BUSI, token, "编辑业务信息");
    }

    /**
     * 修改缓存数据
     */
    private Date updateBusiInfoToRedis(EcmBusiInfoRedisDTO ecmBusiInfoExtend, AccountTokenExtendDTO token) {
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, ecmBusiInfoExtend.getBusiId());
        //更新业务编号
        ecmBusiInfoRedisDTO.setBusiNo(ecmBusiInfoExtend.getBusiNo());
        //更新就近修改人
        ecmBusiInfoRedisDTO.setUpdateUser(token.getUsername());
        ecmBusiInfoRedisDTO.setUpdateUserName(token.getName());
        //更新最近修改时间
        ecmBusiInfoRedisDTO.setUpdateTime(new Date());
//        ecmBusiInfoRedisDTO.setDocRightList(ecmBusiInfoExtend.getDocRightList());
        if (!CollectionUtils.isEmpty(ecmBusiInfoExtend.getAttrList())) {
            //更新业务属性列表
            ecmBusiInfoRedisDTO.setAttrList(ecmBusiInfoExtend.getAttrList());
        }
        Long l = ecmFileInfoMapper.selectCount(new LambdaQueryWrapper<EcmFileInfo>()
                .eq(EcmFileInfo::getBusiId, ecmBusiInfoRedisDTO.getBusiId()));
        ecmBusiInfoRedisDTO.setTotalFileSize(l);
        busiCacheService.saveAndUpate(ecmBusiInfoRedisDTO);
        return ecmBusiInfoRedisDTO.getUpdateTime();
    }

    /**
     * 校验入参数
     */
    public void checkPararmEditBusi(EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        if (ecmBusiInfoExtend.getBusiId() != null) {
            checkEditBusi(ecmBusiInfoExtend);
        } else {
            //适用于对外接口
            AssertUtils.isNull(ecmBusiInfoExtend.getAppCode(), "参数错误");
            AssertUtils.isNull(ecmBusiInfoExtend.getBusiNo(), "参数错误");
            EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectOne(new LambdaQueryWrapper<EcmBusiInfo>().eq(EcmBusiInfo::getAppCode, ecmBusiInfoExtend.getAppCode()).eq(EcmBusiInfo::getBusiNo, ecmBusiInfoExtend.getBusiNo()));

            AssertUtils.isNull(ecmBusiInfo, "参数错误");
            ecmBusiInfoExtend.setBusiId(ecmBusiInfo.getBusiId());
            checkEditBusi(ecmBusiInfoExtend);
        }

    }

    /**
     * 校验参数
     */
    private void checkEditBusi(EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        AssertUtils.isNull(ecmBusiInfoExtend.getBusiId(), "参数错误");
        List<EcmAppAttrDTO> appAttrs = ecmBusiInfoExtend.getAttrList();
        if (CollectionUtils.isEmpty(appAttrs)) {
            return;
        }
        List<EcmAppAttrDTO> uniquePrimaryKey = appAttrs.stream().filter(p -> IcmsConstants.ONE.equals(p.getIsKey())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(uniquePrimaryKey)) {
            EcmAppAttrDTO attrByBusiNo = uniquePrimaryKey.get(0);
            AssertUtils.isNull(attrByBusiNo.getAppAttrValue(), attrByBusiNo.getAttrName() + "不能为空");
            ecmBusiInfoExtend.setBusiNo(attrByBusiNo.getAppAttrValue());
            ecmBusiInfoExtend.setAppCode(attrByBusiNo.getAppCode());
            EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectById(ecmBusiInfoExtend.getBusiId());
            if (!ecmBusiInfo.getBusiNo().equals(ecmBusiInfoExtend.getBusiNo())) {
                //业务编号不能重复
                Long count = ecmBusiInfoMapper.selectCount(new LambdaQueryWrapper<EcmBusiInfo>().eq(EcmBusiInfo::getIsDeleted, IcmsConstants.ZERO).eq(EcmBusiInfo::getAppCode, ecmBusiInfoExtend.getAppCode()).eq(EcmBusiInfo::getBusiNo, ecmBusiInfoExtend.getBusiNo()).ne(EcmBusiInfo::getBusiId, ecmBusiInfoExtend.getBusiId()));
                AssertUtils.isTrue(count.intValue() > 0, "业务编号已存在");
            }
        }
        /*List<EcmAppAttrDTO> uniqueArchived = appAttrs.stream().filter(p -> IcmsConstants.ONE.equals(p.getIsArchived())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(uniqueArchived)) {
            List<EcmAppAttrDTO> filterAttr = uniqueArchived.stream()
                    .filter(p -> !ObjectUtils.isEmpty(p.getAppAttrValue()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(filterAttr)) {
                // appCode查询busiIds
                List busiIds = ecmBusiMetadataMapper.complexSelect(filterAttr, filterAttr.size(),
                        Collections.singletonList(ecmBusiInfoExtend.getAppCode()));
                if(CollectionUtils.isNotEmpty(busiIds)) {
                    Long count = ecmBusiInfoMapper.selectCount(new LambdaQueryWrapper<EcmBusiInfo>().eq(EcmBusiInfo::getIsDeleted, IcmsConstants.ZERO).in(EcmBusiInfo::getBusiId, busiIds));
                    AssertUtils.isTrue(count.intValue() > 1, "归档属性已存在");
                }
            }
        }*/

    }

    /**
     * 过滤影像文件列表
     *
     */
    private List<FileInfoRedisDTO> filterResultFiles(EcmsCaptureVO vo, List<FileInfoRedisDTO> resultFiles) {
        if (CollUtil.isEmpty(resultFiles)) {
            return resultFiles;
        }
        //文件标签
        if (!ObjectUtils.isEmpty(vo.getLabelNames()) || !ObjectUtils.isEmpty(vo.getLabelIds())) {
            List<EcmFileLabel> collect = resultFiles.stream().filter(file->!CollectionUtils.isEmpty(file.getEcmFileLabels())).map(FileInfoRedisDTO::getEcmFileLabels).flatMap(List::stream).collect(Collectors.toList());

            Set<EcmFileLabel> all = new HashSet<>();
            if(!ObjectUtils.isEmpty(vo.getLabelNames()) ){
                List<EcmFileLabel> collect1 = collect.stream().filter(s -> vo.getLabelNames().contains(s.getLabelName())).collect(Collectors.toList());
                all.addAll(collect1);
            }
            if(!ObjectUtils.isEmpty(vo.getLabelIds()) ){
                List<EcmFileLabel> collect1 = collect.stream().filter(s -> s.getLabelId()!=null&&vo.getLabelIds().contains(s.getLabelId())).collect(Collectors.toList());
                all.addAll(collect1);
            }

            if(all.size()>0){
                Set<Long> collect1 = all.stream().map(EcmFileLabel::getFileId).collect(Collectors.toSet());
                resultFiles = resultFiles.stream()
                        .filter(p -> collect1.contains(p.getFileId()))
                        .collect(Collectors.toList());
            }else{
                resultFiles = new ArrayList<>();
            }
        }
        //文件名称
        if (!ObjectUtils.isEmpty(vo.getFileName())) {
            resultFiles = resultFiles.stream()
                    .filter(p -> p.getNewFileName().contains(vo.getFileName()))
                    .collect(Collectors.toList());
        }
        //采集日期
        if (!ObjectUtils.isEmpty(vo.getCaptureTimeStart()) && !ObjectUtils.isEmpty(vo.getCaptureTimeEnd())) {
            Date dayBeginTime = DateUtils.getDayBeginTime(vo.getCaptureTimeStart());
            Date dayEndTime = DateUtils.getDayEndTime(vo.getCaptureTimeEnd());
            resultFiles = resultFiles.stream()
                    .filter(p -> p.getCreateTime().after(dayBeginTime) && p.getCreateTime().before(dayEndTime))
                    .collect(Collectors.toList());
        }
        //采集人
        if (!ObjectUtils.isEmpty(vo.getCaptureUser())) {
            //采集人id列表
            List<String> userIds = new ArrayList<>();
            Result<List<SysUserDTO>> result = userApi.getUserDetailByName(vo.getCaptureUser());
            userIds.addAll(result.getData().stream().map(SysUserDTO::getLoginName).collect(Collectors.toList()));
            userIds.add(String.valueOf(-Long.MAX_VALUE));
            resultFiles = resultFiles.stream()
                    .filter(p -> userIds.contains(p.getCreateUser()))
                    .collect(Collectors.toList());
        }
        //有无批注
        if (!ObjectUtils.isEmpty(vo.getIsComment())) {
            //有批注的影像文件id列表
            List<Long> fileIdsByComment = new ArrayList<>();
            List<EcmFileComment> fileComments = ecmFileCommentMapper.selectList(new LambdaUpdateWrapper<EcmFileComment>()
                    .eq(EcmFileComment::getBusiId, vo.getBusiId()));
            if (CollUtil.isNotEmpty(fileComments)) {
                fileIdsByComment = fileComments.stream()
                        .map(EcmFileComment::getFileId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            fileIdsByComment.add(-Long.MAX_VALUE);
            List<Long> finalFileIdsByComment = fileIdsByComment;
            resultFiles = resultFiles.stream()
                    .filter(p -> StateConstants.ZERO.equals(vo.getIsComment()) != finalFileIdsByComment.contains(p.getFileId()))
                    .collect(Collectors.toList());
        }
        //是否到期
        if (!ObjectUtils.isEmpty(vo.getIsExpire())) {
            resultFiles = resultFiles.stream()
                    .filter(p -> p.getIsExpired().equals(vo.getIsExpire()))
                    .collect(Collectors.toList());
        }

        return resultFiles;
    }

    /**
     * 添加已删除资料节点
     */
    public void addDeletedDocNode(List<EcmBusiStructureTreeDTO> docTypeNode, EcmBusiInfoRedisDTO busiBranch, Integer treeType, String appCode, AccountTokenExtendDTO token, Integer show, List<FileInfoRedisDTO> delListFile) {
//        判断删除菜单权限
        EcmBusiStructureTreeDTO node = getDelNode(busiBranch, treeType, appCode, token, show, delListFile);
        if (node == null) {
            return;
        }
        docTypeNode.add(node);
    }

    /**
     *判断删除菜单权限
     */
    public EcmBusiStructureTreeDTO getDelNode(EcmBusiInfoRedisDTO busiBranch, Integer treeType, String appCode, AccountTokenExtendDTO token, Integer show, List<FileInfoRedisDTO> delListFile) {
        boolean flag = false;
        try {
            flag = checkDeleteMenuRight(appCode, busiBranch, token, show);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        if (!flag) {
            //无需添加已删除节点
            return null;
        }
        EcmBusiStructureTreeDTO node = new EcmBusiStructureTreeDTO();
        node.setId(IcmsConstants.DELETED_CODE);
        node.setDocCode(IcmsConstants.DELETED_CODE);
        node.setName(IcmsConstants.DELETED);
        node.setType(IcmsConstants.SIX);
        node.setNodeType(StateConstants.COMMON_ONE);
        node.setPid(busiBranch.getBusiId().toString());
        node.setPName(busiBranch.getBusiNo());
        node.setBusiNo(busiBranch.getBusiNo());
        node.setBusiId(busiBranch.getBusiId());
        node.setTreeType(treeType);
        node.setStatus(busiBranch.getStatus());
        if (CollectionUtils.isEmpty(delListFile)) {
            node.setFileCount(IcmsConstants.ZERO);
        } else {
            //统计资料节点下文件数量
            node.setFileCount(delListFile.size());
        }
        return node;
    }

    /**
     * 判断该用户对应业务类型是否有删除权限
     */
    private boolean checkDeleteMenuRight(String appCode, EcmBusiInfoRedisDTO busiBranch, AccountTokenExtendDTO token, Integer show) {
        //根据版本和业务类型代码获取该业务类型关联的角色
        List<Long> newRoleIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(token.getRoleIdList())) {
            newRoleIds = token.getRoleIdList();
        } else {
            if (token.isOut()) {
                newRoleIds = token.getRoleIdList();
            } else {
                //根据用户id获取角色
                Result<List<Long>> roleIdListResult = userApi.getRoleListByUsername(token.getUsername());
                if (!roleIdListResult.isSucc() || CollectionUtil.isEmpty(roleIdListResult.getData())) {
                    return false;
                }
                //用户角色
                List<Long> userRoleIdList = roleIdListResult.getData();
                //取出该业务类型所有的角色
                for (Long roleId : userRoleIdList) {
                    newRoleIds.add(roleId);
                }
            }
        }

        if (CollectionUtil.isEmpty(newRoleIds)) {
            return false;
        }
        Result menuByRoleId = roleApi.getMenuByRoleId(newRoleIds);
        List<String> resultList = new ArrayList<>();
        if (menuByRoleId.isSucc()) {
            List<LinkedHashMap> menuList = (List) menuByRoleId.getData();
            // 遍历List<LinkHashMap>
            for (LinkedHashMap<String, Object> mapResult : menuList) {
                for (Map.Entry<String, Object> entry : mapResult.entrySet()) {
                    if ("perms".equals(entry.getKey())) {
                        resultList.add(entry.getValue().toString());
                    }
                }

            }
        }
        if (CollectionUtil.isNotEmpty(resultList)) {
            if (IcmsConstants.SHOW_PAGE.equals(show)) {
                //查看页面
                return resultList.contains("searchViewDeleted");
            } else if (IcmsConstants.CAPTURE_PAGE.equals(show)) {
                //采集页面
                return resultList.contains("captureViewDeleted");
            }
        }

        return false;
    }

    /**
     * 添加未归类资料节点
     */
    private void addUnclassifiedDocNode(List<EcmBusiStructureTreeDTO> docTypeNode, EcmBusiInfoRedisDTO busiBranch,
                                        Integer treeType, Integer isEncrypt, List<FileInfoRedisDTO> collectByUnclassified, Boolean isFlat, Map<String, List<EcmBusiDocRedisDTO>> collect1) {

        EcmBusiStructureTreeDTO node = new EcmBusiStructureTreeDTO();
        //添加是否偏离矫正
        node.setEquipmentId(busiBranch.getEquipmentId());
        node.setAppTypeName(busiBranch.getAppTypeName());
        node.setAppCode(busiBranch.getAppCode());
        node.setId(IcmsConstants.UNCLASSIFIED_ID);
        node.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
        node.setName(IcmsConstants.UNCLASSIFIED);
        node.setType(IcmsConstants.FIVE);
        node.setNodeType(StateConstants.COMMON_ONE);
        node.setPid(busiBranch.getBusiId().toString());
        node.setPName(busiBranch.getBusiNo());
        node.setBusiNo(busiBranch.getBusiNo());
        node.setBusiId(busiBranch.getBusiId());
        node.setIsEncrypt(isEncrypt);
        node.setStatus(busiBranch.getStatus());
        node.setTreeType(treeType);
        node.setIsResize(busiBranch.getIsQulity());
        node.setResize(busiBranch.getResiz());
        node.setQulity(busiBranch.getQulity());
        node.setDocRight(addUnclassifyNodeDocRight());
        node.setIsFlat(isFlat);
        if (!CollectionUtils.isEmpty(collectByUnclassified)) {
            //统计资料节点下文件数量
            node.setFileCount(collectByUnclassified.size());
            //添加md5list
            List<String> md5List = collectByUnclassified.stream().map(EcmFileInfoDTO::getFileMd5).distinct().collect(Collectors.toList());
            node.setMd5List(md5List);
            docTypeNode.add(StateConstants.ZERO,node);
        }

    }

    /**
     * 未归类节点添加所有资料操作权限
     */
    public EcmDocrightDefDTO addUnclassifyNodeDocRight() {
        EcmDocrightDefDTO docrightDefExtend = new EcmDocrightDefDTO();
        docrightDefExtend.setDimType(DocRightConstants.ROLE_DIM);
        docrightDefExtend.setIsUse(DocRightConstants.ONE);
        docrightDefExtend.setAddRight(DocRightConstants.ONE.toString());
        docrightDefExtend.setDeleteRight(DocRightConstants.ONE.toString());
        docrightDefExtend.setUpdateRight(DocRightConstants.ONE.toString());
        docrightDefExtend.setReadRight(DocRightConstants.ONE.toString());
        docrightDefExtend.setThumRight(DocRightConstants.ONE.toString());
        docrightDefExtend.setPrintRight(DocRightConstants.ONE.toString());
        docrightDefExtend.setDownloadRight(DocRightConstants.ONE.toString());
        docrightDefExtend.setOtherUpdate(DocRightConstants.ONE.toString());
        return docrightDefExtend;
    }

    /**
     * 更新缓存数据
     */
    private void classifyIcmsFileToRedis(EcmsCaptureVO vo, AccountTokenExtendDTO token) {
        if (vo.getOldBusiId().equals(vo.getNewBusiId())) {
            //相同的业务
            EcmBusiInfoRedisDTO dto = busiCacheService.getEcmBusiInfoRedisDTO(token, vo.getOldBusiId());
            if (dto != null) {
                List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(vo.getOldBusiId());
                //资料节点
                List<Long> fileIds = vo.getFileIds();
                List<FileInfoRedisDTO> collect = fileInfoRedis.stream().filter(s -> fileIds.contains(s.getFileId())).collect(Collectors.toList());
                //修改文件节点的资料关联类型
                //老节点
                Map<String, List<FileInfoRedisDTO>> collect1 = collect.stream().collect(Collectors.groupingBy(FileInfoRedisDTO::getDocCode));
                Map<Long, List<FileInfoRedisDTO>> collect2 = collect.stream().collect(Collectors.groupingBy(FileInfoRedisDTO::getFileId));

                List<FileInfoRedisDTO> fileInfoRedisEntities = collect;
                for (FileInfoRedisDTO dto1 : fileInfoRedisEntities) {
                    List<FileInfoRedisDTO> fileInfoRedisDTOS = collect2.get(dto1.getFileId());
                    if (!CollectionUtils.isEmpty(fileInfoRedisDTOS)) {
                        dto1.setDocCode(vo.getDocNode().getDocCode());
                        dto1.setDocId(vo.getDocNode().getDocCode());
                        if (IcmsConstants.FOUR.equals(vo.getDocNode().getType())) {
                            dto1.setMarkDocId(Long.parseLong(vo.getDocNode().getId()));
                        } else {
                            dto1.setMarkDocId(null);
                        }
                        dto1.setComment(vo.getDocNode().getName());
                        dto1.setDocName(vo.getDocNode().getName());
                        dto1.setUpdateUserName(token.getName());
                        dto1.setUpdateTime(new Date());
                    }
                }
                busiCacheService.updateFileInfoRedis(fileInfoRedisEntities);
                List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = dto.getEcmBusiDocRedisDTOS();
                //需要减少的节点
                Set<String> strings = collect1.keySet();
                for (EcmBusiDocRedisDTO dto1 : ecmBusiDocRedisDTOS) {
                    if (strings.contains(dto1.getDocCode())) {
                        dto1.setFileCount(dto1.getFileCount() - collect1.get(dto1.getDocCode()).size());
                    } else if (vo.getDocNode().getDocCode().equals(dto1.getDocCode())) {
                        dto1.setFileCount(dto1.getFileCount() + collect.size());
                    }
                }
                dto.setEcmBusiDocRedisDTOS(ecmBusiDocRedisDTOS);
                busiCacheService.saveAndUpate(dto);
            }

        } else {
            //不同的业务
            EcmBusiInfoRedisDTO dto = busiCacheService.getEcmBusiInfoRedisDTO(token, vo.getOldBusiId());
            if (dto != null) {
                List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(vo.getOldBusiId());
                List<Long> fileIds = vo.getFileIds();
                //修改文件节点的资料关联类型
                //老节点
                List<FileInfoRedisDTO> collect = fileInfoRedis.stream().filter(s -> fileIds.contains(s.getFileId())).collect(Collectors.toList());
                //删
                Map<String, List<FileInfoRedisDTO>> collect1 = collect.stream().collect(Collectors.groupingBy(FileInfoRedisDTO::getDocCode));
                Map<Long, List<FileInfoRedisDTO>> collect2 = collect.stream().collect(Collectors.groupingBy(FileInfoRedisDTO::getFileId));

                List<FileInfoRedisDTO> fileInfoRedisEntities = fileInfoRedis;
                ArrayList<FileInfoRedisDTO> add = new ArrayList<>();
                ArrayList<Long> del = new ArrayList<>();
                for (FileInfoRedisDTO dto1 : fileInfoRedisEntities) {
                    List<FileInfoRedisDTO> fileInfoRedisDTOS = collect2.get(dto1.getFileId());
                    if (!CollectionUtils.isEmpty(fileInfoRedisDTOS)) {
                        del.add(dto1.getFileId());
                        dto1.setAppCode(vo.getDocNode().getAppCode());
                        dto1.setBusiId(vo.getNewBusiId());
                        dto1.setDocCode(vo.getDocNode().getDocCode());
                        if (IcmsConstants.FOUR.equals(vo.getDocNode().getType())) {
                            dto1.setMarkDocId(Long.parseLong(vo.getDocNode().getId()));
                        } else {
                            dto1.setMarkDocId(null);
                        }
                        dto1.setComment(vo.getDocNode().getName());
                        dto1.setDocId(vo.getDocNode().getId());
                        dto1.setUpdateUserName(token.getName());
                        dto1.setUpdateTime(new Date());
                        add.add(dto1);
                    }
                }
                busiCacheService.delFileInfoRedisReal(vo.getOldBusiId(), del);

                //改数量
                List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = dto.getEcmBusiDocRedisDTOS();
                //需要减少的节点
                Set<String> strings = collect1.keySet();
                for (EcmBusiDocRedisDTO dto1 : ecmBusiDocRedisDTOS) {
                    if (strings.contains(dto1.getDocCode())) {
                        dto1.setFileCount(dto1.getFileCount() - collect1.get(dto1.getDocCode()).size());
                    }
                }
                dto.setEcmBusiDocRedisDTOS(ecmBusiDocRedisDTOS);
                busiCacheService.saveAndUpate(dto);

                EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, vo.getNewBusiId());
                busiCacheService.updateFileInfoRedis(add);
                List<EcmBusiDocRedisDTO> collect3 = ecmBusiInfoRedisDTO.getEcmBusiDocRedisDTOS().stream().filter(s -> vo.getDocNode().equals(s.getDocCode())).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(collect3)) {
                    List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS1 = ecmBusiInfoRedisDTO.getEcmBusiDocRedisDTOS();
                    for (EcmBusiDocRedisDTO dto1 : ecmBusiDocRedisDTOS1) {
                        if (dto1.getDocCode().equals(dto1.getDocCode())) {
                            dto1.setFileCount(dto1.getFileCount() + add.size());
                        }
                    }
                    ecmBusiInfoRedisDTO.setEcmBusiDocRedisDTOS(ecmBusiDocRedisDTOS1);
                }
                busiCacheService.saveAndUpate(ecmBusiInfoRedisDTO);

            }
        }
    }

    /**
     * 文件归类
     */
    @Transactional(rollbackFor = Exception.class)
    public void classifyIcmsFileToDb(EcmsCaptureVO vo, AccountTokenExtendDTO token) {
        if (!ObjectUtils.isEmpty(vo.getDocNode())) {
            if (IcmsConstants.THREE.equals(vo.getDocNode().getType())) {
                //归类到对应的资料节点下
                ecmFileInfoMapper.update(null, new UpdateWrapper<EcmFileInfo>()
                        .set("busi_id", vo.getNewBusiId())
                        .set("doc_code", vo.getDocNode().getDocCode())
                        .set("mark_doc_id", null)
                        .set("\"COMMENT\"", vo.getDocNode().getName())
                        .set("update_time", new Date())
                        .set("update_user", token.getUsername())
                        .set("update_user_name", token.getName())
                        .in("file_id", vo.getFileIds()));
            } else if (IcmsConstants.FOUR.equals(vo.getDocNode().getType())) {
                //归类到资料标记节点
                ecmFileInfoMapper.update(null, new UpdateWrapper<EcmFileInfo>()
                        .set("busi_id", vo.getNewBusiId()).set("doc_code", vo.getDocNode().getDocCode())
                        .set("mark_doc_id", vo.getDocNode().getId())
                        .set("\"COMMENT\"", vo.getDocNode().getName())
                        .set("update_time", new Date()).set("update_user", token.getUsername())
                        .set("update_user_name", token.getName())
                        .in("file_id", vo.getFileIds()));
            } else if (IcmsConstants.TWO.equals(vo.getDocNode().getType())) {
                //归类到未归类里
                ecmFileInfoMapper.update(null, new UpdateWrapper<EcmFileInfo>()
                        .set("busi_id", vo.getNewBusiId()).set("doc_code", IcmsConstants.UNCLASSIFIED_ID)
                        .set("mark_doc_id", null).set("\"COMMENT\"", IcmsConstants.UNCLASSIFIED)
                        .set("update_time", new Date()).set("update_user", token.getUsername())
                        .set("update_user_name", token.getName())
                        .in("file_id", vo.getFileIds()));
            } else if (IcmsConstants.TREE_TYPE_BUSI.equals(vo.getDocNode().getType())) {
                //归类到未归类里
                ecmFileInfoMapper.update(null, new UpdateWrapper<EcmFileInfo>()
                        .set("busi_id", vo.getNewBusiId()).set("doc_code", vo.getDocNode().getDocCode())
                        .set("mark_doc_id", vo.getDocNode().getId()).set("\"COMMENT\"", vo.getDocNode().getName())
                        .set("update_time", new Date()).set("update_user", token.getUsername())
                        .set("update_user_name", token.getName())
                        .in("file_id", vo.getFileIds()));
            }
        } else {
            //归类到未归类里
            ecmFileInfoMapper.update(null, new UpdateWrapper<EcmFileInfo>()
                    .set("busi_id", vo.getNewBusiId()).set("doc_code", IcmsConstants.UNCLASSIFIED_ID)
                    .set("mark_doc_id", null).set("\"COMMENT\"", IcmsConstants.UNCLASSIFIED)
                    .set("update_time", new Date()).set("update_user", token.getUsername())
                    .set("update_user_name", token.getName())
                    .in("file_id", vo.getFileIds()));
        }
        //添加操作记录表
        busiOperationService.addOperation(vo.getOldBusiId(), IcmsConstants.ADD_BUSI, token, "业务归类");
    }

    /**
     * 编辑es文件信息
     */
    private void classifyIcmsFileToEs(EcmsCaptureVO vo, AccountTokenExtendDTO token) {
        List<Long> fileIds = vo.getFileIds();
        //得到修改人名称
        String userName = token.getName();
        if (!CollectionUtils.isEmpty(fileIds)) {
            String finalUserName = userName;
            fileIds.forEach(p -> {
                operateFullQueryService.editEsFileInfo(p, finalUserName, new Date(), null, vo.getDocNode().getDocCode(), null);
            });
        }
    }

    /**
     * 修改缓存信息
     */
    private void updateFileNameToRedis(FileInfoVO vo) {
        String newName = vo.getNewName();
        FileInfoRedisDTO fileInfoRedisSingle = busiCacheService.getFileInfoRedisSingle(vo.getBusiId(), vo.getFileId());
        fileInfoRedisSingle.setComment(newName);
        fileInfoRedisSingle.setNewFileName(newName);
        fileInfoRedisSingle.setUpdateTime(new Date());
        fileInfoRedisSingle.setUpdateUserName(vo.getCurentUserName());
        busiCacheService.updateFileInfoRedis(fileInfoRedisSingle);
    }

    /**
     * 修改影像文件信息表文件名
     */
    private void updateFileNameToDb(Long fileId, String newName, String curentUserName) {
        //修改影像文件信息表文件名
        ecmFileInfoMapper.update(null, new UpdateWrapper<EcmFileInfo>()
                .set("comment", newName)
                .set("new_file_name", newName)
                .set("update_time", new Date())
                .set("update_user_name", curentUserName)
                .eq("file_id", fileId));
    }

    /**
     * 更新redis缓存中的业务信息
     */
    private void updateFileByBusiInfoRedisBatch(List<EcmFileInfoDTO> ecmFileInfoDTO, List<EcmFileHistory> ecmFileHistoryList) {
        //处理文件历史
        Map<Long, List<EcmFileHistory>> collect = ecmFileHistoryList.stream().collect(Collectors.groupingBy(EcmFileHistory::getFileId));
        //获取redis缓存中的业务信息
        ecmFileInfoDTO.forEach(p -> {
            //文件历史
            List<EcmFileHistory> ecmFileHistories = collect.get(p.getFileId());
            //添加新增的文件信息
            FileInfoRedisDTO fileInfoRedisDTO = new FileInfoRedisDTO();
            BeanUtils.copyProperties(p, fileInfoRedisDTO);
            fileInfoRedisDTO.setDocCode(p.getDocCode());
            fileInfoRedisDTO.setFileHistories(ecmFileHistories);
            busiCacheService.saveFileInfoRedis(fileInfoRedisDTO);
        });
    }

    /**
     * 获取资料节点
     *
     * @param ecmBusiStructureTreeDTOS 缓存中的业务结构树
     * @param ecmBusiDocRedisDTOS      缓存里面的资料
     * @param groupedFileByDocId       缓存中的资料文件
     * @param busiBranch               单笔业务
     * @param groupedByDocMark         资料标记
     * @param treeType                 树类型
     * @param groupedByDocRight        资料权限
     */
    private List<EcmBusiStructureTreeDTO> getDocTypeNode(List<EcmBusiStructureTreeDTO> ecmBusiStructureTreeDTOS, List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS,
                                                         Map<String, List<FileInfoRedisDTO>> groupedFileByDocId, EcmBusiInfoRedisDTO busiBranch,
                                                         Map<String, List<EcmBusiDoc>> groupedByDocMark, Integer treeType, Map<String, List<EcmDocrightDefDTO>> groupedByDocRight,
                                                         Boolean isFlat, Integer isEncrypt, Map<Long, List<FileInfoRedisDTO>> groupedFileByMark, Map<String, List<EcmDocDef>> docMaps,
                                                         AccountTokenExtendDTO token, Map<String, List<EcmBusiDocRedisDTO>> collect1) {

        //校验若资料没有除文件外的任意权限，则该资料节点不显示（不拼装到树中）
        List<String> docCodeList = checkAllDocRight(groupedByDocRight.get(busiBranch.getBusiId()), IcmsConstants.ZERO);
        //下面这里不能要，不然就没版本的概念了
        ecmBusiDocRedisDTOS = ecmBusiDocRedisDTOS.stream().sorted(Comparator.comparing(EcmBusiDocRedisDTO::getDocSort)).collect(Collectors.toList());
        for (EcmBusiDocRedisDTO doc : ecmBusiDocRedisDTOS) {
            EcmBusiStructureTreeDTO node = new EcmBusiStructureTreeDTO();
            if (isEncrypt != null) {
                node.setIsEncrypt(isEncrypt);
            } else {
                node.setIsEncrypt(StateConstants.NO);
            }
            node.setAppCode(busiBranch.getAppCode());
            node.setAppTypeName(busiBranch.getAppTypeName());
            node.setId(doc.getDocId() != null ? doc.getDocId().toString() : doc.getDocCode());
            node.setDocCode(doc.getDocCode());
            node.setName(doc.getDocName());
            node.setType(IcmsConstants.TREE_TYPE_DOCCODE);
            node.setNodeType(CollUtil.isEmpty(doc.getChildren()) ? StateConstants.COMMON_ONE : StateConstants.ZERO);
            node.setPid(doc.getParent() + "");
            node.setPName(doc.getParentName());
            node.setBusiNo(busiBranch.getBusiNo());
            node.setBusiId(busiBranch.getBusiId());
            node.setTreeType(busiBranch.getTreeType());
            node.setIsResize(busiBranch.getIsQulity());
            node.setResize(busiBranch.getResiz());
            node.setQulity(busiBranch.getQulity());
            node.setEquipmentId(busiBranch.getEquipmentId());
            node.setIsFlat(isFlat);
            node.setIsParent(doc.getIsParent());
            node.setStatus(StringUtils.isEmpty(busiBranch.getStatus())?BusiInfoConstants.BUSI_STATUS_ZERO:busiBranch.getStatus());
            //资料权限
            if (IcmsConstants.STATIC_TREE.equals(treeType)) {
                List<EcmDocDef> ecmDocDefs = docMaps.get(node.getId());
                if (!CollectionUtils.isEmpty(ecmDocDefs)) {
                    EcmDocDef ecmDocDef = ecmDocDefs.get(0);
                    //静态树
                    node.setImgLimit(ecmDocDef.getImgLimit());
                    node.setAudioLimit(ecmDocDef.getAudioLimit());
                    node.setOfficeLimit(ecmDocDef.getOfficeLimit());
                    node.setVideoLimit(ecmDocDef.getVideoLimit());
                    node.setOtherLimit(ecmDocDef.getOtherLimit());
                    //如果是对外接口把最大最小值改为多维度权限配置
                    EcmDocrightDefDTO ecmDocrightDefDTO = Optional.ofNullable(groupedByDocRight.get(ecmDocDef.getDocCode()))
                            .filter(list -> !list.isEmpty())
                            .map(list -> list.get(0))
                            .orElse(null);
                    //先取权限,取不到并且未启用取资料定义表
                    if (!ObjectUtils.isEmpty(ecmDocrightDefDTO)&&IcmsConstants.ONE.toString().equals(ecmDocrightDefDTO.getEnableLenLimit())) {
                        //优先用 ecmDocrightDefDTO 的 maxLen：不为 null 则用，为 null 则取 ecmDocDef 的 maxFiles
                        node.setMaxLen(ecmDocrightDefDTO.getMaxLen() != null
                                ? ecmDocrightDefDTO.getMaxLen()
                                : ecmDocDef.getMaxFiles());

                        //优先用 ecmDocrightDefDTO 的 minLen：不为 null 则用，为 null 则取 ecmDocDef 的 minFiles
                        node.setMinLen(ecmDocrightDefDTO.getMinLen() != null
                                ? ecmDocrightDefDTO.getMinLen()
                                : ecmDocDef.getMinFiles());
                    } else {
                        //若 ecmDocrightDefDTO 为空，直接取 ecmDocDef 的配置
                        node.setMaxLen(ecmDocDef.getMaxFiles());
                        node.setMinLen(ecmDocDef.getMinFiles());
                    }
                }
            } else {
                if (groupedByDocRight.get(node.getDocCode()) == null) {
                    //动态树
                    node.setMaxLen(DocRightConstants.ONE_THOUSAND);
                    node.setMinLen(0);
                } else {
                    //动态树
                    node.setMaxLen(groupedByDocRight.get(node.getDocCode()).get(0).getMaxLen());
                    node.setMinLen(groupedByDocRight.get(node.getDocCode()).get(0).getMinLen());
                }

            }

            //是否加锁
            if (CollectionUtils.isNotEmpty(docCodeList)) {
                if (docCodeList.contains(node.getDocCode())) {
                    node.setLock(true);
                }
            }
            //资料权限，树的权限包括文件类型，增删改查和文件数量
            EcmDocrightDefDTO ecmDocrightDefDTO = setTreePermiss(treeType, groupedByDocRight, docMaps, node, token);
            //统计资料节点下文件数量
            if (CollectionUtils.isEmpty(groupedFileByDocId.get(doc.getDocCode()))) {
                node.setFileCount(StateConstants.ZERO);
                //添加md5list
                node.setMd5List(new ArrayList<>());
            } else {
                node.setFileCount(groupedFileByDocId.get(doc.getDocCode()).size());
                //添加md5list
                List<String> md5List = groupedFileByDocId.get(doc.getDocCode()).stream().map(EcmFileInfoDTO::getFileMd5).distinct().collect(Collectors.toList());
                node.setMd5List(md5List);
            }

            if (!CollectionUtils.isEmpty(doc.getChildren())) {
                //非叶子节点
                node.setChildren(new ArrayList<>());

                getDocTypeNode(node.getChildren(), doc.getChildren(),
                        groupedFileByDocId, busiBranch,
                        groupedByDocMark, treeType,
                        groupedByDocRight, isFlat,
                        isEncrypt, groupedFileByMark, docMaps, token, collect1);
                //更新文件数量
                int totalFileCount = node.getChildren().stream().mapToInt(EcmBusiStructureTreeDTO::getFileCount).sum();
                node.setFileCount(totalFileCount);
            } else {
                //动态树把docId的值赋给docCode(兼容标记节点保存)
                //叶子节点
                if (!ObjectUtils.isEmpty(groupedByDocMark)) {
                    //添加资料标记节点
                    //资料权限，树的权限包括文件类型，增删改查和文件数量
                    addDocMarkNode(node, groupedByDocMark, groupedFileByMark, treeType, ecmDocrightDefDTO, isFlat, isEncrypt, busiBranch);
                }
            }
            ecmBusiStructureTreeDTOS.add(node);
        }

        return ecmBusiStructureTreeDTOS;
    }

    /**
     * 设置资料节点权限
     */
    private static EcmDocrightDefDTO setTreePermiss(Integer treeType, Map<String, List<EcmDocrightDefDTO>> groupedByDocRight, Map<String, List<EcmDocDef>> docMaps, EcmBusiStructureTreeDTO node, AccountTokenExtendDTO token) {
        if (IcmsConstants.STATIC_TREE.equals(treeType)) {
            List<EcmDocDef> ecmDocDefs = docMaps.get(node.getId());
            List<EcmDocrightDefDTO> ecmDocrightDefDTOS=groupedByDocRight.get(node.getDocCode());
            if (!CollectionUtils.isEmpty(ecmDocDefs)) {
                EcmDocDef ecmDocDef = ecmDocDefs.get(0);
                //静态树
                node.setImgLimit(ecmDocDef.getImgLimit());
                node.setAudioLimit(ecmDocDef.getAudioLimit());
                node.setOfficeLimit(ecmDocDef.getOfficeLimit());
                if(!CollectionUtils.isEmpty(ecmDocrightDefDTOS)){
                    EcmDocrightDefDTO ecmDocrightDefDTO=ecmDocrightDefDTOS.get(0);
                    if(IcmsConstants.ONE.toString().equals(ecmDocrightDefDTO.getEnableLenLimit())) {
                        node.setMaxLen(ecmDocrightDefDTO.getMaxLen());
                        node.setMinLen(ecmDocrightDefDTO.getMinLen());
                    }
                }else {
                    node.setMaxLen(ecmDocDef.getMaxFiles());
                    node.setMinLen(ecmDocDef.getMinFiles());
                }
                node.setVideoLimit(ecmDocDef.getVideoLimit());
                node.setOtherLimit(ecmDocDef.getOtherLimit());
            }
        } else {
            if (CollectionUtils.isEmpty(groupedByDocRight.keySet())) {
                //界面打开动态树，默认上传大小限制为0到1000
                node.setMaxLen(DocRightConstants.ONE_THOUSAND);
                node.setMinLen(0);
            } else {
                //动态树
                if (groupedByDocRight.get(node.getDocCode()) != null) {
                    node.setMaxLen(groupedByDocRight.get(node.getDocCode()).get(0).getMaxLen());
                    node.setMinLen(groupedByDocRight.get(node.getDocCode()).get(0).getMinLen());
                } else {
                    node.setMaxLen(DocRightConstants.ONE_THOUSAND);
                    node.setMinLen(0);
                }

            }
        }
        EcmDocrightDefDTO ecmDocrightDefDTO = new EcmDocrightDefDTO();
        if (!CollectionUtils.isEmpty(groupedByDocRight.get(node.getDocCode()))) {
//                ecmDocrightDefDTO = handleDocRight(groupedByDocRight.get(doc.getDocCode()).get(0));
            //添加资料权限
            node.setDocRight(groupedByDocRight.get(node.getDocCode()).get(0));
            ecmDocrightDefDTO = groupedByDocRight.get(node.getDocCode()).get(0);
        } else {
            if (!token.isOut() && IcmsConstants.DYNAMIC_TREE.equals(treeType)) {
                //pc端打开动态树，直接展示所有权限
                ecmDocrightDefDTO.setThumRight(StateConstants.YES.toString());
                ecmDocrightDefDTO.setAddRight(StateConstants.YES.toString());
                ecmDocrightDefDTO.setUpdateRight(StateConstants.YES.toString());
                ecmDocrightDefDTO.setPrintRight(StateConstants.YES.toString());
                ecmDocrightDefDTO.setReadRight(StateConstants.YES.toString());
                ecmDocrightDefDTO.setDownloadRight(StateConstants.YES.toString());
                ecmDocrightDefDTO.setDeleteRight(StateConstants.YES.toString());
                ecmDocrightDefDTO.setOtherUpdate(StateConstants.YES.toString());
                ecmDocrightDefDTO.setDocCode(node.getDocCode());
                node.setLock(false);
                node.setDocRight(ecmDocrightDefDTO);
            } else {
                ecmDocrightDefDTO.setThumRight(StateConstants.NO.toString());
                ecmDocrightDefDTO.setAddRight(StateConstants.NO.toString());
                ecmDocrightDefDTO.setUpdateRight(StateConstants.NO.toString());
                ecmDocrightDefDTO.setPrintRight(StateConstants.NO.toString());
                ecmDocrightDefDTO.setReadRight(StateConstants.NO.toString());
                ecmDocrightDefDTO.setDownloadRight(StateConstants.NO.toString());
                ecmDocrightDefDTO.setDeleteRight(StateConstants.NO.toString());
                ecmDocrightDefDTO.setOtherUpdate(StateConstants.NO.toString());
                ecmDocrightDefDTO.setDocCode(node.getDocCode());
                node.setLock(true);
                node.setDocRight(ecmDocrightDefDTO);
            }
        }

        return ecmDocrightDefDTO;
    }

    /**
     * 添加资料标记节点
     */
    public void addDocMarkNode(EcmBusiStructureTreeDTO node, Map<String, List<EcmBusiDoc>> groupedByDocMark, Map<Long, List<FileInfoRedisDTO>> groupedFileByDocId,
                               Integer treeType, EcmDocrightDefDTO ecmDocrightDefDTO, Boolean isFlat, Integer isEncrypt, EcmBusiInfoRedisDTO busiBranch) {
        List<EcmBusiDoc> docMarks = groupedByDocMark.get(node.getDocCode());
        if (CollectionUtils.isEmpty(docMarks)) {
            return;
        }
        List<EcmBusiStructureTreeDTO> children = new ArrayList<>();
        for (EcmBusiDoc mark : docMarks) {
            //添加是否偏离矫正
            EcmBusiStructureTreeDTO markNode = new EcmBusiStructureTreeDTO();
            markNode.setDocFileSize(node.getFileCount());
            markNode.setImgLimit(node.getImgLimit());
            markNode.setAudioLimit(node.getAudioLimit());
            markNode.setOfficeLimit(node.getOfficeLimit());
            markNode.setVideoLimit(node.getVideoLimit());
            markNode.setOtherLimit(node.getOtherLimit());
            markNode.setMaxLen(node.getMaxLen());
            markNode.setMinLen(node.getMinLen());
            markNode.setLock(node.isLock());
            markNode.setId(mark.getDocId().toString());
            markNode.setName(mark.getDocName());
            markNode.setType(IcmsConstants.TREE_TYPE_DOCMARK);
            markNode.setPid(node.getId());
            markNode.setPName(node.getName());
            markNode.setBusiNo(node.getBusiNo());
            markNode.setBusiId(node.getBusiId());
            markNode.setTreeType(treeType);
            markNode.setDocCode(mark.getDocCode());
            markNode.setAppCode(node.getAppCode());
            markNode.setEquipmentId(node.getEquipmentId());
            markNode.setAppTypeName(node.getAppTypeName());
            markNode.setIsFlat(isFlat);
            markNode.setIsEncrypt(isEncrypt);
            markNode.setFileCount(StateConstants.ZERO);
            markNode.setStatus(node.getStatus());
            //统计资料标记节点下文件数量
            if (groupedFileByDocId != null) {
                List<FileInfoRedisDTO> fileInfoRedisDTOS = groupedFileByDocId.get(mark.getDocId());
                if (CollectionUtils.isNotEmpty(fileInfoRedisDTOS)) {
                    List<FileInfoRedisDTO> collect = fileInfoRedisDTOS.stream()
                            .filter(p -> !ObjectUtils.isEmpty(p.getMarkDocId())
                                    && mark.getDocId().equals(p.getMarkDocId())
                                    && StateConstants.NO.equals(p.getState()))
                            .collect(Collectors.toList());

                    markNode.setFileCount(CollectionUtils.isEmpty(fileInfoRedisDTOS) ? StateConstants.ZERO : collect.size());
                    markNode.setMd5List(CollectionUtils.isEmpty(fileInfoRedisDTOS)
                            ? new ArrayList<>()
                            : fileInfoRedisDTOS.stream().filter(p -> !ObjectUtils.isEmpty(p.getMarkDocId()) && mark.getDocId().equals(p.getMarkDocId()))
                            .map(EcmFileInfoDTO::getFileMd5)
                            .distinct()
                            .collect(Collectors.toList())
                    );
                }
            } else {
                markNode.setFileCount(StateConstants.ZERO);
                markNode.setMd5List(new ArrayList<>());
            }
            markNode.setDocRight(ecmDocrightDefDTO);
            children.add(markNode);
        }
        node.setChildren(children);
    }

    /**
     * 批次属性扩展表插入数据
     */
    private void insertEcmBusiMetadata(EcmBusiInfoRedisDTO ecmBusiInfoExtend) {

    }

    /**
     * 数据库插入影像业务信息
     */
    private EcmBusiInfoRedisDTO insertEcmBusiInfo(EcmBusiInfoRedisDTO ecmBusiInfoExtend, AccountTokenExtendDTO token, Integer rightVer) {
        AssertUtils.isNull(ecmBusiInfoExtend.getAppCode(), "业务类型不能为空");
        //唯一主键不能为空
        List<EcmAppAttrDTO> appAttrs = ecmBusiInfoExtend.getAttrList();
        AssertUtils.isNull(appAttrs, "该业务类型属性未配置");
        List<EcmAppAttrDTO> uniquePrimaryKey = appAttrs.stream().filter(p -> IcmsConstants.ONE.equals(p.getIsKey())).collect(Collectors.toList());
        AssertUtils.isNull(uniquePrimaryKey, "该业务类型未定义业务主键");
        AssertUtils.isTrue(uniquePrimaryKey.size() > 1, "该业务类型存在多个业务主键");
        ecmBusiInfoExtend.setBusiNo(uniquePrimaryKey.get(0).getAppAttrValue());
        List<EcmBusiInfo> busiInfos = ecmBusiInfoMapper.selectList(new LambdaQueryWrapper<EcmBusiInfo>()
                .eq(!ObjectUtils.isEmpty(ecmBusiInfoExtend.getAppCode()), EcmBusiInfo::getAppCode, ecmBusiInfoExtend.getAppCode())
                .eq(EcmBusiInfo::getBusiNo, ecmBusiInfoExtend.getBusiNo()));
        AssertUtils.isTrue(!CollectionUtils.isEmpty(busiInfos), "业务编号已存在");
        //创建人
        ecmBusiInfoExtend.setCreateUser(token.getUsername());
        //静态树
        ecmBusiInfoExtend.setTreeType(StateConstants.ZERO);
        //资料权限版本
        ecmBusiInfoExtend.setRightVer(rightVer);
        EcmBusiInfo ecmBusiInfo = new EcmBusiInfo();
        BeanUtils.copyProperties(ecmBusiInfoExtend, ecmBusiInfo);
        ecmBusiInfo.setCreateUserName(token.getName());
        //添加机构号
        addInstNo(token, ecmBusiInfo);
        insertDB(appAttrs, ecmBusiInfo);
        ecmBusiInfoExtend.setBusiId(ecmBusiInfo.getBusiId());
        ecmBusiInfoExtend.setCreateTime(ecmBusiInfo.getCreateTime());
        ecmBusiInfoExtend.setOrgCode(ecmBusiInfo.getOrgCode());
        ecmBusiInfoExtend.setCreateUserName(ecmBusiInfo.getCreateUserName());
        return ecmBusiInfoExtend;
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertDB(List<EcmAppAttrDTO> appAttrs, EcmBusiInfo ecmBusiInfo) {
        ecmBusiInfoMapper.insert(ecmBusiInfo);
        //添加
        for (EcmAppAttrDTO extend : appAttrs) {
            if (!ObjectUtils.isEmpty(extend.getAppAttrValue())) {
                EcmBusiMetadata ecmBusiMetadata = new EcmBusiMetadata();
                ecmBusiMetadata.setBusiId(ecmBusiInfo.getBusiId());
                ecmBusiMetadata.setAppAttrId(extend.getAppAttrId());
                ecmBusiMetadata.setAppAttrVal(extend.getAppAttrValue());
                //todo 改批量插入
                ecmBusiMetadataMapper.insert(ecmBusiMetadata);
            }
        }
    }

    /**
     * 添加机构信息
     */
    private void addInstNo(AccountTokenExtendDTO token, EcmBusiInfo ecmBusiInfo) {
        //外部接口机构从参数中取
        if (token.isOut()) {
            ecmBusiInfo.setOrgCode(token.getOrgCode());
            ecmBusiInfo.setOrgName(token.getOrgName());
            return;
        }
        //根据用户id获取机构id
        List<String> userIds = new ArrayList<>();
        Long instId = null;
        if (!ObjectUtils.isEmpty(token.getUsername())) {
            userIds.add(token.getUsername());
        }
        Map<String, List<SysUserDTO>> groupedByUserId = modelPermissionsService.getUserListByUserIds(userIds);
        //添加创建人名称
        if (!ObjectUtils.isEmpty(token.getId())) {
            if (!org.springframework.util.CollectionUtils.isEmpty(groupedByUserId.get(token.getUsername()))) {
                instId = groupedByUserId.get(token.getUsername()).get(0).getInstId();
            }
        }
        //根据机构id获取机构号
        SysInstDTO sysInstDTO = instApi.getInstByInstId(instId).getData();
        AssertUtils.isNull(sysInstDTO, "参数错误");
        ecmBusiInfo.setOrgCode(sysInstDTO.getInstNo());
        ecmBusiInfo.setOrgName(sysInstDTO.getName());
    }

    /**
     * 检查参数
     */
    private void checkPararmClassify(EcmsCaptureVO vo) {
        AssertUtils.isNull(vo.getFileIds(), "请先选中需要操作的文件");
        AssertUtils.isNull(vo.getOldBusiId(), "参数错误");
        AssertUtils.isNull(vo.getNewBusiId(), "参数错误");
        AssertUtils.isNull(vo.getOldDocCode(), "参数错误");
        if (!ObjectUtils.isEmpty(vo.getDocNode())) {
            if (IcmsConstants.TREE_TYPE_DOCCODE.equals(vo.getDocNode().getType())) {
                AssertUtils.isTrue(IcmsConstants.ZERO.equals(vo.getDocNode().getNodeType()), "只能归类到最底层资料节点下");
            }
            if (IcmsConstants.TREE_TYPE_BUSI.equals(vo.getDocNode().getType())) {
                AssertUtils.isTrue(vo.getNewBusiId().equals(vo.getOldBusiId()), "请指定资料类型");
            }
        }

        if (vo.getOldBusiId().equals(vo.getNewBusiId())) {
            if (vo.getDocNode().getDocCode().equals(IcmsConstants.UNCLASSIFIED_ID)) {
                AssertUtils.isTrue(true, "不能归入同一业务下的未归类节点");
            }
        }
    }

    /**
     * 检查全限
     */
    private void checkDocReadRight(List<EcmBusiStructureTreeDTO> children) {
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        children.forEach(p -> {
            if (!ObjectUtils.isEmpty(p.getChildren())) {
                checkDocReadRight(p.getChildren());
            } else {
                if (!(!ObjectUtils.isEmpty(p.getDocRight()) && !ObjectUtils.isEmpty(p.getDocRight().getReadRight()) && String.valueOf(StateConstants.COMMON_ONE).equals(p.getDocRight().getReadRight()))) {
                    p = null;
                }
            }
        });
    }

    /**
     * 获取文件信息
     */
    private List<FileInfoRedisDTO> getFileInfoRedisDTOS(FileInfoRedisEntityVO ecmBusiDocExtend, List<String> sourceDocCodeList, AccountTokenExtendDTO token) {
        //要复用的文件集合
        final List<FileInfoRedisDTO>[] fileInfoRedisDTO = new List[]{new ArrayList<>()};
        //选中要复用的资料节点信息
        List<MultiplexFileVO> multiplexFileVO = ecmBusiDocExtend.getMultiplexFileVO();
        //判断选中的节点中是否有被复用的节点有没有交集
        final Boolean[] isHaveIntersection = {false};
        for (MultiplexFileVO p : multiplexFileVO) {
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, p.getTargetBusiId());
//            List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
            List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedisByFileIds(ecmBusiInfoRedisDTO.getBusiId(),p.getTargetFileId());
            if (!CollectionUtils.isEmpty(fileInfoRedis)) {
                //筛选出在目标节点id下且没删除的文件
                fileInfoRedisDTO[0].addAll(fileInfoRedis.stream().filter(pp -> p.getTargetDocTypeId().contains(pp.getDocCode()) && StateConstants.ZERO.equals(pp.getState())).collect(Collectors.toList()));
            } else {
                fileInfoToDb(fileInfoRedisDTO[0], p);
            }
            // 从持久化数据库中查节点关系
            EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectById(ecmBusiDocExtend.getSourceBusiId());
            AssertUtils.isNull(ecmBusiInfo, "参数错误");
            //查询该业务是静态树还是动态树
            if (IcmsConstants.DYNAMIC_TREE.equals(ecmBusiInfo.getTreeType())) {
                //动态树
                //查看选中的节点中是否有被复用的节点相同的节点 没有则文件进入未归类，有则进入相应节点下
                List<EcmBusiDoc> ecmBusiDocs = ecmBusiDocMapper.selectList(new LambdaQueryWrapper<EcmBusiDoc>().eq(EcmBusiDoc::getBusiId, ecmBusiDocExtend.getSourceBusiId()));
                AssertUtils.isNull(ecmBusiDocs, "数据异常");
                // 判断是不是资料节点
                List<String> list = Collections.singletonList(ecmBusiDocExtend.getSourceDocId());
                //递归得到所选节点下资料节点中的叶子节点
                List<String> finalSourceDocTypeIdList = new ArrayList<>();
                List<String> finalSourceDocTypeCodeList = new ArrayList<>();
                List<String> list1 = handleDocTypeIdList(list, ecmBusiDocs, finalSourceDocTypeIdList, finalSourceDocTypeCodeList);
                sourceDocCodeList.addAll(list1);
            } else {
                //静态树
                //根据busiId得到app_code
                String appCode = ecmBusiInfo.getAppCode();
                //根据app_code得到app_code关联的doc_code
                LambdaQueryWrapper<EcmAppDocRel> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(EcmAppDocRel::getAppCode, appCode);
                queryWrapper.eq(EcmAppDocRel::getType, IcmsConstants.ONE);
                List<EcmAppDocRel> ecmAppDocRels = ecmAppDocRelMapper.selectList(queryWrapper);
                List<String> docCodeList = ecmAppDocRels.stream().map(EcmAppDocRel::getDocCode).collect(Collectors.toList());

                List<EcmDocDefRelVer> ecmDocDefRelVers = ecmDocDefRelVerMapper.selectList(new LambdaQueryWrapper<EcmDocDefRelVer>()
                        .in(EcmDocDefRelVer::getDocCode, docCodeList)
                        .eq(EcmDocDefRelVer::getAppCode, appCode)
                        .eq(EcmDocDefRelVer::getRightVer, ecmBusiInfo.getRightVer()));
                //筛选出叶子节点
                //帅选出选中的叶子节点
                List<String> targetDocTypeId = p.getTargetDocTypeId();
//                List<String> collect = ecmAppDocRels.stream().filter(pp -> IcmsConstants.ONE.equals(pp.getType())).filter(f -> targetDocTypeId.contains(f.getDocCode())).map(EcmAppDocRelVer::getDocCode).collect(Collectors.toList());
                List<String> collect = ecmDocDefRelVers.stream().filter(f -> targetDocTypeId.contains(f.getDocCode())).map(EcmDocDefRelVer::getDocCode).collect(Collectors.toList());

                sourceDocCodeList.addAll(collect);
            }
            //判断选中的节点中是否有被复用的节点有没有交集
            ArrayList<String> intersection = (ArrayList<String>) CollectionUtils.intersection(sourceDocCodeList, p.getTargetDocTypeId());
            if (!CollectionUtils.isEmpty(intersection)) {
                //没交集就复用到未归类中
                isHaveIntersection[0] = true;
            }
        }
        if (!isHaveIntersection[0]) {
            ecmBusiDocExtend.setSourceDocId(IcmsConstants.UNCLASSIFIED_ID);
        }

        return fileInfoRedisDTO[0];
    }

    /**
     * 添加文件信息
     */
    private void fileInfoToDb(List<FileInfoRedisDTO> fileInfoRedisDTOS1, MultiplexFileVO p) {
        List<EcmFileInfo> ecmFileInfos = ecmFileInfoMapper.selectList(new LambdaQueryWrapper<EcmFileInfo>()
                .eq(EcmFileInfo::getBusiId, p.getTargetBusiId())
                .in(!CollectionUtils.isEmpty(p.getTargetDocId()), EcmFileInfo::getDocCode, p.getTargetDocId())
                .eq(EcmFileInfo::getState, StateConstants.ZERO));
        if (CollectionUtils.isEmpty(ecmFileInfos)) {
            fileInfoRedisDTOS1.addAll(new ArrayList<>());
        } else {
            List<Long> newFileId = ecmFileInfos.stream().map(EcmFileInfo::getNewFileId).collect(Collectors.toList());
            //从存储服务查询文件信息详情
            List<SysFileDTO> sysFileDTOList = fileHandleApi.details(newFileId).getData();
            AssertUtils.isNull(sysFileDTOList, "参数错误， 存储服务查询文件信息为空");
            Map<Long, List<SysFileDTO>> listMap = sysFileDTOList.stream().collect(Collectors.groupingBy(SysFileDTO::getId));
            //从es中查询 文件EXIF
            QueryBuilder query = null;
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            MatchQueryBuilder must = QueryBuilders.matchQuery("busiId", +p.getTargetBusiId());
            query = boolQuery.must(must);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(query);
            List<EsEcmFile> list = esEcmFileMapper.selectList(new LambdaEsQueryWrapper<EsEcmFile>()
                    .indexName(fileIndex)
                    .match(EsEcmFile::getBusiId, p.getTargetBusiId()));
            //
            List<FileInfoRedisDTO> fileInfoRedisDTOS = PageCopyListUtils.copyListProperties(ecmFileInfos, FileInfoRedisDTO.class);
            fileInfoRedisDTOS.forEach(pp -> {
                //赋值fileExif
                list.forEach(ppp -> {
                    if (ppp.getFileId().equals(String.valueOf(pp.getFileId()))) {
                        pp.setFileExif(JSON.parseObject(ppp.getExif(), HashMap.class));
                    }
                });
                //赋值文件信息
                List<SysFileDTO> sysFileDTOS = listMap.get(pp.getNewFileId());
                if (!CollectionUtils.isEmpty(sysFileDTOS)) {
                    //文件后缀
                    pp.setFormat(sysFileDTOS.get(IcmsConstants.ZERO).getExt());
                    //文件大小
                    pp.setSize(sysFileDTOS.get(IcmsConstants.ZERO).getSize());
                    calculateFileSize(pp);
                }
            });
            fileInfoRedisDTOS1.addAll(fileInfoRedisDTOS);
        }

    }

    /**
     * 递归得到所选节点下资料节点中的叶子节点
     *
     * @param list        节点信息
     * @param ecmBusiDocs 源业务节点下所有节点信息
     */
    private List<String> handleDocTypeIdList(List<String> list, List<EcmBusiDoc> ecmBusiDocs, List<String> finalSourceDocTypeIdList, List<String> finalSourceDocTypeCodeList) {
        //处理ecmBusiDocs数据
        Map<Long, List<EcmBusiDoc>> collect = ecmBusiDocs.stream().collect(Collectors.groupingBy(EcmBusiDoc::getParentId));
        Map<Long, List<EcmBusiDoc>> collect2 = ecmBusiDocs.stream().collect(Collectors.groupingBy(EcmBusiDoc::getDocId));
        list.forEach(p -> {
            List<EcmBusiDoc> ecmBusiDocs1 = collect.get(Long.parseLong(p));
            // 查看是不是父节点
            if (CollectionUtils.isEmpty(ecmBusiDocs1)) {
                finalSourceDocTypeIdList.add(p);
                List<EcmBusiDoc> ecmBusiDocs2 = collect2.get(Long.parseLong(p));
                if (!CollectionUtils.isEmpty(ecmBusiDocs2)) {
                    finalSourceDocTypeCodeList.add(ecmBusiDocs2.get(0).getDocCode());
                }
            } else {
                //是父节点，得到其子节点的docId
                List<String> collect1 = new ArrayList<>();
                for (EcmBusiDoc doc : ecmBusiDocs1) {
                    collect1.add(doc.getDocId().toString());
                }
                //ecmBusiDocs1.stream().map(s -> collect1.add(s.getDocCode()));
                handleDocTypeIdList(collect1, ecmBusiDocs, finalSourceDocTypeIdList, finalSourceDocTypeCodeList);
            }
        });

        return finalSourceDocTypeCodeList;
    }

    /**
     *递归得到叶子节点集合
     */
    private void handleEcmBusiStructureTreeDTO(List<EcmBusiStructureTreeDTO> children, List<EcmBusiStructureTreeDTO> ecmBusiInfoListList) {
        if (!CollectionUtils.isEmpty(children)) {
            //判断该资料节点是不是叶子节点
            children.forEach(p -> {
                //没有子节点
                boolean b = CollectionUtils.isEmpty(p.getChildren()) && !IcmsConstants.FOUR.equals(p.getType()) && !IcmsConstants.SIX.equals(p.getType());
                boolean b1 = !CollectionUtils.isEmpty(p.getChildren()) && IcmsConstants.FOUR.equals(p.getChildren().get(0).getType()) && !IcmsConstants.SIX.equals(p.getType());
                if (b || b1) {
                    ecmBusiInfoListList.add(p);
                } else {
                    handleEcmBusiStructureTreeDTO(p.getChildren(), ecmBusiInfoListList);
                }
            });
        }

    }

    /**
     *获取业务或者资料节点对应的文件信息
     */
    public Result getFileInfoByBusiOrDoc(EcmDownloadFileDTO ecmDownloadFileDTO) {
        AccountTokenExtendDTO accountTokenExtendDTO = busiCacheService.checkUser(ecmDownloadFileDTO.getEcmBaseInfoDTO(), null);
        EcmBaseInfoDTO ecmBaseInfoDTO = ecmDownloadFileDTO.getEcmBaseInfoDTO();
        ecmBaseInfoDTO.setUserName(accountTokenExtendDTO.getName());
        ecmBaseInfoDTO.setOrgName(accountTokenExtendDTO.getOrgName());
        ecmDownloadFileDTO.setEcmBaseInfoDTO(ecmBaseInfoDTO);
        AssertUtils.isNull(ecmDownloadFileDTO.getAppCode(), "参数有误");
        AssertUtils.isNull(ecmDownloadFileDTO.getBusiNo(), "参数有误");
        //根据业务编号查询业务
        String appNo = ecmDownloadFileDTO.getAppCode();
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectList(new LambdaQueryWrapper<EcmAppDef>().eq(EcmAppDef::getAppCode, appNo));
        AssertUtils.isNull(ecmAppDefs, "参数有误");
        String busiNo = ecmDownloadFileDTO.getBusiNo();
        String docNo = ecmDownloadFileDTO.getDocNo();
        LambdaQueryWrapper<EcmBusiInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmBusiInfo::getBusiNo, busiNo).eq(EcmBusiInfo::getAppCode, ecmAppDefs.get(0).getAppCode());
        List<EcmBusiInfo> ecmBusiInfos = ecmBusiInfoMapper.selectList(wrapper);
        AssertUtils.isNull(ecmBusiInfos, "业务数据不存在");
        List<FileInfoRedisDTO> newFileList = new ArrayList<>();
        EcmBusiInfo ecmBusiInfo = ecmBusiInfos.get(0);
        Long busiId = ecmBusiInfo.getBusiId();
        //是否指定了fileId
        //是否指定了资料节点
        //都没有全部文件
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(busiId);
        if (CollectionUtil.isNotEmpty(fileInfoRedis)) {
            if (!CollectionUtils.isEmpty(ecmDownloadFileDTO.getFiles())) {
                newFileList = fileInfoRedis.stream().filter(s -> ecmDownloadFileDTO.getFiles().contains(s.getNewFileId())).collect(Collectors.toList());
            } else if (StrUtil.isBlank(docNo)) {
                newFileList = fileInfoRedis.stream().filter(s -> StateConstants.NO.equals(s.getState())).collect(Collectors.toList());
            } else {
                newFileList = fileInfoRedis.stream().filter(s -> StateConstants.NO.equals(s.getState()) && docNo.equals(s.getDocCode())).collect(Collectors.toList());
            }
        }
        AssertUtils.isNull(newFileList, "目标节点无文件");
        //根据newFileList查询存储那边文件完整信息
        Map map = new HashMap();
        map.put("appcode", ecmBusiInfo.getAppCode());
        map.put("appname", ecmAppDefs.get(0).getAppName());
        map.put("busino", ecmBusiInfo.getBusiNo());
        map.put("fileList", JSONObject.toJSONString(newFileList));
        return Result.success(map);
    }

    /**
     * 排序
     *
     * @param data      排序的集合
     * @param sortField 排序方式
     * @param sortOrder 顺序或倒叙
     */
    private List<FileInfoRedisDTO> sortData(List<FileInfoRedisDTO> data, String sortField, String sortOrder) {
        Comparator<FileInfoRedisDTO> comparator = null;

        // 根据排序字段选择比较器
        if ("newFileName".equals(sortField)) {
            comparator = Comparator.comparing(FileInfoRedisDTO::getNewFileName);
        } else if ("createUser".equals(sortField)) {
            comparator = Comparator.comparing(FileInfoRedisDTO::getCreateUser);
        } else if ("createTime".equals(sortField)) {
            comparator = Comparator.comparing(FileInfoRedisDTO::getCreateTime);
        } else if ("updateTime".equals(sortField)) {
            comparator = Comparator.comparing(FileInfoRedisDTO::getUpdateTime, Comparator.nullsFirst(Comparator.naturalOrder()));
        }

        // 根据排序顺序调整比较器
        if ("desc".equals(sortOrder)) {
            comparator = comparator.reversed();
        }

        // 排序并返回结果
        return data.stream().sorted(comparator).collect(Collectors.toList());
    }

    /**
     * 查看文件记录日志
     */
    public void viewFileLog(Long busiId , String fileName, Integer size,AccountTokenExtendDTO token){
        AssertUtils.isNull(busiId,"参数错误");
        EcmBusiInfoDTO ecmBusiInfoDTO = ecmBusiInfoMapper.selectWithAppTypeById(busiId);
        AssertUtils.isNull(ecmBusiInfoDTO,"参数错误");
        EcmBusiLog ecmBusiLog = new EcmBusiLog();
        ecmBusiLog.setBusiNo(ecmBusiInfoDTO.getBusiNo())
                .setAppName(ecmBusiInfoDTO.getAppTypeName())
                .setAppCode(ecmBusiInfoDTO.getAppCode())
                .setOrgCode(ecmBusiInfoDTO.getOrgCode())
                .setOperatorId(token.getUsername())
                .setOperator(token.getName())
                .setOperatorType(BusiLogConstants.OPERATION_TYPE_FIVE)
                .setOperateContent("查看文件" + ":" + fileName+"等"+size+"个文件");
        busiLogMapper.insert(ecmBusiLog);
    }

    /**
     * 根据UrlNoce换取flageId
     * @param nonce
     * @return
     */
    public String getFlagIdByUrlnonce(String nonce) {
        String flagId = busiCacheService.getUrlOnceNonce(nonce);
        AssertUtils.isNull(flagId,"url失效请重新获取");
        //清理缓存
        busiCacheService.delUrlOnceNonce(nonce);
        return flagId;
    }

    public Result parseExcelToBusi(MultipartFile file, AccountTokenExtendDTO token) {
        AssertUtils.isNull(file, "传入文件不能为空");
        AssertUtils.isTrue(EasyExcelUtils.isExcelCompletelyEmpty(file),"当前导入文件为空，请重新上传");

        EcmPageBaseInfoDTO ecmPageBaseInfoDTO = openApiService.scanOrUpdateEcmPc(token);
        String flagId = ecmPageBaseInfoDTO.getFlagId();
        BatchImportResultVO batchImportResultVO = new BatchImportResultVO();

        //统计数量
        int totalCount = 0;
        int successCount = 0;
        int failCount = 0;
        List<BatchImportFailureVO> failureVOList = new ArrayList<>();
        try {
            List<Map<String, Object>> result = EasyExcelUtils.parseExcel(file.getInputStream());

            for (Map<String, Object> stringObjectMap : result) {

                String appCode = (String) stringObjectMap.get("appCode");
                String appName = (String) stringObjectMap.get("appName");
                EcmAppDef ecmAppDef = ecmAppDefMapper
                        .selectOne(new LambdaQueryWrapper<EcmAppDef>()
                                //这里必须用collect!=null,因为还有一种情况是需要校验权限，但是查到的权限是null,即无权限的情况
                                .in(!StringUtils.isEmpty(appCode), EcmAppDef::getAppCode, appCode));
                //获取attrDto  每一个appCode只获取一次 排序好
                List<EcmAppAttr> appAttrs = ecmAppAttrMapper.selectList(new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, appCode));
                if (org.springframework.util.CollectionUtils.isEmpty(appAttrs)) {
                    log.info("appCode: {} 没有业务属性", appCode);
                }
                List<EcmAppAttrDTO> ecmAppAttrDTOS = PageCopyListUtils.copyListProperties(appAttrs, EcmAppAttrDTO.class);
                ecmAppAttrDTOS = ecmAppAttrDTOS.stream().sorted(Comparator.comparing(EcmAppAttrDTO::getAttrSort)).collect(Collectors.toList());
                //主属性排到第一个位置
                Collections.sort(ecmAppAttrDTOS, Comparator.comparingInt(EcmAppAttrDTO::getIsKey).reversed());
                List<List<String>> defalutValueList = (List<List<String>>) stringObjectMap.get("data");
                for (int i = 0; i < defalutValueList.size(); i++) {
                    List<String> strings = defalutValueList.get(i);
                    for (int l = 0; l < strings.size(); l++) {
                        if (l == 0 && StringUtils.isEmpty(strings.get(l))) {
                            // 获取 主索引
                            String busiIndex = getBusiIndexDefault(token, appCode);
                            ecmAppAttrDTOS.get(l).setAppAttrValue(busiIndex);
                        } else {
                            //excel 传入值了
                            String value = StringUtils.isEmpty(strings.get(l)) ? "" : strings.get(l);
                            ecmAppAttrDTOS.get(l).setAppAttrValue(value);
                        }
                    }
                    // 封装对象，请求addbusi接口 添加业务
                    EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = new EcmBusiInfoRedisDTO();
                    ecmBusiInfoRedisDTO.setAppCode(appCode);
                    ecmBusiInfoRedisDTO.setAttrList(ecmAppAttrDTOS);
                    ecmBusiInfoRedisDTO.setEquipmentId(ecmAppDef.getEquipmentId());
                    ecmBusiInfoRedisDTO.setPageFlag(flagId);
                    token.setFlagId(flagId);
                    try {
                        addBusi(ecmBusiInfoRedisDTO,token);
                        successCount++;
                    }catch (Exception e){
                        log.error("创建业务信息失败",e);
                        failCount++;
                        BatchImportFailureVO importFailureVO = BatchImportFailureVO.builder()
                                .appName(appName)
                                .busiIndex(ecmAppAttrDTOS.get(0).getAppAttrValue())
                                .reason(e.getMessage())
                                .build();
                        failureVOList.add(importFailureVO);
                    }
                    //把拓展属性值全部设置为空
                    ecmAppAttrDTOS.forEach(
                            ecmAppAttrDTO -> ecmAppAttrDTO.setAppAttrValue("")
                    );
                    totalCount++;
                }
            }
            //处理完之后设置值
            batchImportResultVO.setTotal(totalCount);
            batchImportResultVO.setFailures(failureVOList);
            batchImportResultVO.setSuccessCount(successCount);
            batchImportResultVO.setFailCount(failCount);
        } catch (Exception e) {
            log.error("批量导入失败",e);
        }
        return Result.success(batchImportResultVO);
    }



    public String parseAndFormat(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("日期字符串不能为空");
        }

        String trimmed = dateStr.trim();

        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(trimmed, formatter);
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException ignored) {
                // 继续尝试下一个格式
            }
        }
        throw new IllegalArgumentException("日期格式错误");
    }

    /**
     * 异步加载缓存
     */
    @Async("GlobalThreadPool")
    public void  getBusiInfoToRedis(EcmStructureTreeDTO ecmStructureTreeDTO, AccountTokenExtendDTO token){
        for (Long id : ecmStructureTreeDTO.getBusiIdList()) {
            //业务缓存
            busiCacheService.getEcmBusiInfoRedisDTO(token, id);
            //文件缓存
            busiCacheService.getFileInfoRedis(id);
        }
    }

    /**
     * 处理补传权限
     * type = 1：新增 + 修改
     * type = 2：仅新增
     */
    public void dealRetransmission(Integer type, List<EcmDocrightDefDTO> ecmDocrightDefDTOS){
        if (CollUtil.isNotEmpty(ecmDocrightDefDTOS)) {
            ecmDocrightDefDTOS.forEach(dto -> {
                dto.setAddRight("0");
                dto.setReadRight("0");
                dto.setUpdateRight("0");
                dto.setDeleteRight("0");
                dto.setThumRight("0");
                dto.setPrintRight("0");
                dto.setDownloadRight("0");
                dto.setOtherUpdate("0");

                if (type == 1) {
                    // type=1：新增 + 修改
                    dto.setAddRight("1");
                    dto.setUpdateRight("1");
                    dto.setThumRight("1");
                } else if (type == 2) {
                    // type=2：与1一致
                    dto.setAddRight("1");
                    dto.setUpdateRight("1");
                    dto.setThumRight("1");
                }
            });
        }
    }

}
