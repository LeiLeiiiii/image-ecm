package com.sunyard.ecm.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunyard.afm.api.AntiFraudDetApi;
import com.sunyard.afm.api.dto.AfmDetImgDetDTO;
import com.sunyard.afm.api.dto.AfmDetUpdateDto;
import com.sunyard.ecm.annotation.LogManageAnnotation;
import com.sunyard.ecm.annotation.WebsocketNoticeAnnotation;
import com.sunyard.ecm.config.LocationParserEngineConfig;
import com.sunyard.ecm.config.properties.EcmOcrProperties;
import com.sunyard.ecm.config.properties.SunEcmProperties;
import com.sunyard.ecm.constant.BusiInfoConstants;
import com.sunyard.ecm.constant.DocRightConstants;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.RedisConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.AreaInfoDTO;
import com.sunyard.ecm.dto.EcmBaseInfoDTO;
import com.sunyard.ecm.dto.EcmBusExtendDTO;
import com.sunyard.ecm.dto.EcmFileInfoApiDTO;
import com.sunyard.ecm.dto.ecm.EcmAutoCheckListDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiStructureTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmDocrightDefDTO;
import com.sunyard.ecm.dto.ecm.EcmFileAutoDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmFileOcrDetailEsDTO;
import com.sunyard.ecm.dto.ecm.EcmFileOcrInfoEsDTO;
import com.sunyard.ecm.dto.ecm.EcmFileOcrInfoEsExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmIntelligentDetectionAgainDTO;
import com.sunyard.ecm.dto.ecm.EcmIntelligentDetectionDTO;
import com.sunyard.ecm.dto.ecm.EcmOcrIndentifyDTO;
import com.sunyard.ecm.dto.ecm.SysStrategyDTO;
import com.sunyard.ecm.dto.redis.EcmBusiDocRedisDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.dto.redis.UserBusiRedisDTO;
import com.sunyard.ecm.dto.split.SysFileApiDTO;
import com.sunyard.ecm.enums.EcmCheckAsyncTaskEnum;
import com.sunyard.ecm.enums.StrategyConstantsEnum;
import com.sunyard.ecm.es.EsEcmFile;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmAsyncTaskMapper;
import com.sunyard.ecm.mapper.EcmBusiDocMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmDestroyListMapper;
import com.sunyard.ecm.mapper.EcmDestroyTaskMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmDocPlagCheMapper;
import com.sunyard.ecm.mapper.EcmDtdAttrMapper;
import com.sunyard.ecm.mapper.EcmDtdDefMapper;
import com.sunyard.ecm.mapper.EcmFileAttrOperationMapper;
import com.sunyard.ecm.mapper.EcmFileExpireInfoMapper;
import com.sunyard.ecm.mapper.EcmFileHistoryMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.mapper.EcmFileLabelMapper;
import com.sunyard.ecm.mapper.es.EsEcmFileMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmAsyncTask;
import com.sunyard.ecm.po.EcmBusiDoc;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmDestroyList;
import com.sunyard.ecm.po.EcmDestroyTask;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmDocPlagChe;
import com.sunyard.ecm.po.EcmDtdAttr;
import com.sunyard.ecm.po.EcmDtdDef;
import com.sunyard.ecm.po.EcmFileAttrOperation;
import com.sunyard.ecm.po.EcmFileExpireInfo;
import com.sunyard.ecm.po.EcmFileHistory;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.po.EcmFileLabel;
import com.sunyard.ecm.service.ModelPermissionsService;
import com.sunyard.ecm.service.OperateFullQueryService;
import com.sunyard.ecm.service.SysStrategyService;
import com.sunyard.ecm.service.mq.RabbitMQProducer;
import com.sunyard.ecm.util.CheckDetectionUtils;
import com.sunyard.ecm.util.CommonHttpClientUtils;
import com.sunyard.ecm.util.CommonUtils;
import com.sunyard.ecm.util.EcmEsUtils;
import com.sunyard.ecm.util.FileSizeUtils;
import com.sunyard.ecm.util.Md5Utils;
import com.sunyard.ecm.vo.*;
import com.sunyard.ecm.websocket.WebSocketMessageDTO;
import com.sunyard.ecm.websocket.WebSocketMsgTypeEnum;
import com.sunyard.ecm.websocket.WebSocketService;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.ZipUtils;
import com.sunyard.framework.common.util.date.DateUtils;
import com.sunyard.framework.elasticsearch.vo.BaseAttachment;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.framework.ocr.dto.ocr.OcrResultDTO;
import com.sunyard.framework.ocr.util.RegenOcrUtils;
import com.sunyard.framework.redis.constant.TimeOutConstants;
import com.sunyard.module.storage.api.FileHandleApi;
import com.sunyard.module.storage.api.FileStorageApi;
import com.sunyard.module.storage.api.StorageEquipmentApi;
import com.sunyard.module.storage.dto.EquipmentDTO;
import com.sunyard.module.storage.dto.MixedPastingSplitDTO;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.vo.DownFileVO;
import com.sunyard.module.storage.vo.FileByteVO;
import com.sunyard.module.storage.vo.FileEcmMergeVO;
import com.sunyard.module.storage.vo.FileSplitPdfVO;
import com.sunyard.module.storage.vo.StEquipmentVO;
import com.sunyard.module.storage.vo.UploadListVO;
import com.sunyard.module.system.api.InstApi;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.dto.SysInstDTO;
import com.sunyard.module.system.api.dto.SysParamDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.dromara.easyes.core.conditions.update.LambdaEsUpdateWrapper;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import tech.spiro.addrparser.common.RegionInfo;
import tech.spiro.addrparser.parser.Location;
import tech.spiro.addrparser.parser.LocationParserEngine;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author： zyl
 * @Description：文件信息实现类
 * @create： 2023/5/6 15:46
 */
@Slf4j
@Service
public class FileInfoService {
    public final static List<String> IMGS = Arrays.asList("JPG", "jpg", "JPEG", "jpeg", "png", "PNG", "psd", "PSD", "bmp", "BMP","gif", "GIF");
    public final static List<String> DOCS = Arrays.asList("doc", "docx", "DOC", "DOCX","pdf","PDF","xls","XLS","xlsx","XLSX","txt","TXT","ppt","PPT","pptx","PPTX","wps","WPS","ofd","OFD","ini","INI","rtf","RTF","Xml","XML","xml","syd","SYD","html","HTML","xmind","XMIND");


    @Value("${spring.application.name:eam-acc-server}")
    private String application;
    @Value("${fileIndex:ecm_file_dev}")
    private String fileIndex;
    @Value("${storage.url:http://172.1.1.210:28083}")
    private String storageUrl;
    @Value("${pdf.default.max_size:50.0}")
    private Double pdfDefaultMaxSize;

    @Resource(name = "invoiceTypeCodeNameMap")
    Map<String, String> invoiceTypeCodeNameMap;
    @Resource
    private EcmEsUtils ecmEsUtils;
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private SnowflakeUtils snowflakeUtils;
    @Resource
    private EcmDestroyTaskMapper ecmDestroyTaskMapper;
    @Resource
    private EcmDestroyListMapper ecmDestroyListMapper;
    @Resource
    private EcmFileAttrOperationMapper ecmFileAttrOperationMapper;
    @Resource
    private EcmFileExpireInfoMapper ecmFileExpireInfoMapper;
    @Resource
    private EcmDocPlagCheMapper ecmDocPlagCheMapper;
    @Resource
    private EcmDtdAttrMapper ecmDtdAttrMapper;
    @Resource
    private EcmDtdDefMapper ecmDtdDefMapper;
    @Resource
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Resource
    private EcmBusiDocMapper ecmBusiDocMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmFileHistoryMapper ecmFileHistoryMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EsEcmFileMapper esEcmFileMapper;
    @Resource
    private EcmAsyncTaskMapper asyncTaskMapper;
    @Resource
    private EcmFileLabelMapper ecmFileLabelMapper;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private AntiFraudDetApi antiFraudDetApi;
    @Resource
    private FileStorageApi fileStorageApi;
    @Resource
    private FileHandleApi fileHandleApi;
    @Resource
    private StorageEquipmentApi equipmentApi;
    @Resource
    private ParamApi paramApi;
    @Resource
    private InstApi instApi;
    @Resource
    private LocationParserEngineConfig locationParserEngineService;
    @Resource
    private AsyncTaskService asyncTaskService;
    @Resource
    private SysStrategyService sysStrategyService;
    @Resource
    private TaskSwitchService taskSwitchService;
    @Resource
    private OperateFullQueryService operateFullQueryService;
    @Resource
    private WebSocketService webSocketService;
    @Resource
    private BusiOperationService busiOperationService;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private CommonService commonService;
    @Resource
    private FileCommentService fileCommentService;
    @Resource
    private SunEcmProperties sunEcmProperties;
    @Resource
    private EcmOcrProperties ecmOcrProperties;
    @Resource
    private CaptureSubmitService captureSubmitService;
    @Resource
    private RestHighLevelClient restHighLevelClient;
    @Resource
    private FileSplitService fileSplitService;
    @Resource
    private CheckDetectionService checkDetectionService;
    /**
     * 插入文件信息
     */
    @WebsocketNoticeAnnotation(busiId = "#ecmFileInfoDTO.busiId")
    @LogManageAnnotation("保存文件")
    public Result insertFileInfo(EcmFileInfoDTO ecmFileInfoDTO, AccountTokenExtendDTO token) {
        long l = System.currentTimeMillis();

        AssertUtils.isNull(ecmFileInfoDTO.getFileId(), "文件id不能为空");
        AssertUtils.isNull(ecmFileInfoDTO.getDocId(), "资料id不能为空");
        AssertUtils.isNull(ecmFileInfoDTO.getFileSort(), "文件顺序号不能为空");
        AssertUtils.isNull(ecmFileInfoDTO.getBusiId(), "业务id不能为空");
        AssertUtils.isNull(ecmFileInfoDTO.getBusiNo(), "业务编号不能为空");
        AssertUtils.isNull(ecmFileInfoDTO.getFormat(), "文件后缀名不能为空");
        AssertUtils.isNull(ecmFileInfoDTO.getSourceFileMd5(), "sourceFileMd5不能为空");
        AssertUtils.isNull(ecmFileInfoDTO.getAppCode(), "appTypeId不能为空");
        AssertUtils.isNull(ecmFileInfoDTO.getAppTypeName(), "业务类型名称不能为空");
        AssertUtils.isNull(ecmFileInfoDTO.getNewFileName(),"文件名称不能为空");
        //添加文件信息
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO1 = busiCacheService.getEcmBusiInfoRedisDTO(token, ecmFileInfoDTO.getBusiId());
        Integer treeType = ecmBusiInfoRedisDTO1.getTreeType();
        ecmFileInfoDTO = addFileInfoBack(token, ecmFileInfoDTO.getBusiId(), ecmFileInfoDTO.getDocId(), ecmFileInfoDTO, ecmBusiInfoRedisDTO1);
        ecmFileInfoDTO.setCreateUser(token.getUsername());
        ecmFileInfoDTO.setCreateUserName(token.getName());
        ecmFileInfoDTO.setFileMd5(ecmFileInfoDTO.getSourceFileMd5());
        ecmFileInfoDTO.setOrgName(token.getOrgName());
        Long fileId = snowflakeUtil.nextId();
        ecmFileInfoDTO.setFileId(fileId);
        long l1 = System.currentTimeMillis();
        log.info("文件上传1:{}", l1 - l);
        //混贴拆分后的文件信息
        List<SysFileDTO> sysFileDTOS = new ArrayList<>();
        //OCR识别成功与否
        boolean isOcrSuccess = true;
        //是否进行自动归类
        boolean isAutoGroup = false;
        //获取tasktype之前 先判断菜单是否开启
        List<String> enumConfigList = sysStrategyService.queryEcmEnumConfig();
        //判断自动分类是否开启，0：开启 1：关闭
        Integer automaticClassificationStatus = enumConfigList.contains(IcmsConstants.AUTOMATIC_CLASSIFICATION_STATUS)?IcmsConstants.ZERO:IcmsConstants.ONE;
        //只有图片和pdf才可以Ocr识别
        // 查询系统配置
        Map<Integer, Boolean> result = taskSwitchService.queryAllSwitches(ecmFileInfoDTO.getDocCode());
        if (IMGS.contains(ecmFileInfoDTO.getFormat()) || DOCS.contains(ecmFileInfoDTO.getFormat())) {
            //只有配置2的时候走睿征
            if (IMGS.contains(ecmFileInfoDTO.getFormat())&&IcmsConstants.AUTO_CLASS_METHOD_RZ.equals(ecmOcrProperties.getOcrType())) {
                //查询该业务下的单证是否配置OCR识别权限
                SysStrategyDTO vo = isAutoGroup(ecmFileInfoDTO.getAppCode());
                //OCR识别开关
                if (vo.getOcrConfigStatus()) {
                    //使用ocr读取发票信息
                    List<OcrResultDTO> ocrResultDTOS = getOcrResultDTOS(ecmFileInfoDTO, token);
                    if (!CollectionUtils.isEmpty(ocrResultDTOS)) {
                        //自动识别
                        sysFileDTOS = autoGroupNew(ecmFileInfoDTO, token, vo, sysFileDTOS,
                                ocrResultDTOS);
                    } else {
                        isOcrSuccess = false;
                    }
                    isAutoGroup = true;
                }
            } else if (IcmsConstants.AUTO_CLASS_METHOD_SUNYARD.equals(ecmOcrProperties.getOcrType()) && StrUtil.isNotEmpty(ecmFileInfoDTO.getDocCode())) {
                //内部OCR识别
                //采集页面用户自动采集配置,开了才走内部OCR,有值表示关闭了自动归类
                Boolean userResult = busiCacheService.hasAutoGroup(token.getUsername());
                //如果选中了资料节点则不进自动归类,选中了则不为UNCLASSIFIED_ID
                if (!userResult && IcmsConstants.UNCLASSIFIED_ID.equals(ecmFileInfoDTO.getDocCode()) && IcmsConstants.ZERO.equals(automaticClassificationStatus)) {
                    ecmFileInfoDTO = checkAutoGroupWithTimeout(ecmFileInfoDTO, token,ecmBusiInfoRedisDTO1);
                    //自动归类以后,用新归类的资料节点查询是否开启了翻拍模糊等检测
                    result = taskSwitchService.queryAllSwitches(ecmFileInfoDTO.getDocCode());
                    isAutoGroup = true;
                }

            }
        }
        FileInfoRedisDTO fileInfoRedisDTO = new FileInfoRedisDTO();
        if (CollectionUtils.isEmpty(sysFileDTOS)) {
            if (isAutoGroup) {
                //自动归类权限校验
                checkDocRightByAutoClassify(ecmFileInfoDTO, "自动归类导入文件", token, false, ecmBusiInfoRedisDTO1);
            } else {
                //自动归类权限校验
                //                checkDocRightByAutoClassify(ecmFileInfoDTO, "导入文件", token, false);
            }
            //没有进行混贴拆分
            //添加文件信息（持久化、redis、es）
            if (token.isOut()) {
                ecmFileInfoDTO.setOrgCode(token.getOrgCode());
            }
            fileInfoRedisDTO = saveFileInfo(ecmFileInfoDTO, token);
        }
        long l2 = System.currentTimeMillis();
        log.info("文件上传2:{}", l2 - l1);
        //判断是否是图片类型
        //单证识别
        SysStrategyDTO vo = isAutoGroup(ecmFileInfoDTO.getAppCode());
        if (IMGS.contains(ecmFileInfoDTO.getFormat())) {
            /*
            这里写配置检测,异步任务类型000000100九个字长,1位单证识别，2位自动转正，3模糊检测，4查重检测，5拆分合并，6位表示翻拍检测,7es,8反光,9缺角
            其中每位上 0表示无该类型，1表示处理中，2失败，3成功,对于模糊查重以及翻拍多2个状态，4表示排除异常，5表示确认异常
            如果开启了单证识别配置,则初始化taskType100000,把对应位置置为1即可
            */
            String taskType = CheckDetectionUtils.getTaskType(result, vo,enumConfigList);
            //如果未开启任何配置则不初始化
            if (!IcmsConstants.ASYNC_TASK_STATUS_INIT.equals(taskType)) {
                EcmAsyncTask ecmAsyncTask = new EcmAsyncTask();
                ecmAsyncTask.setTaskType(taskType);
                ecmAsyncTask.setBusiId(ecmFileInfoDTO.getBusiId());
                ecmAsyncTask.setFileId(ecmFileInfoDTO.getFileId());
                asyncTaskService.insert(ecmAsyncTask);
                //MQ处理智能检测
                checkDetectionService.checkDetectionByMq(ecmFileInfoDTO,ecmAsyncTask,taskType);
            }
        } else if (DOCS.contains(ecmFileInfoDTO.getFormat())){
            String taskType = getAfmTaskType(result,enumConfigList);
            if (!IcmsConstants.ASYNC_TASK_STATUS_INIT.equals(taskType)) {
                EcmAsyncTask ecmAsyncTask = new EcmAsyncTask();
                ecmAsyncTask.setTaskType(taskType);
                ecmAsyncTask.setBusiId(ecmFileInfoDTO.getBusiId());
                ecmAsyncTask.setFileId(ecmFileInfoDTO.getFileId());
                asyncTaskService.insert(ecmAsyncTask);
                //MQ处理智能检测
                checkDetectionService.checkDetectionByMq(ecmFileInfoDTO,ecmAsyncTask,taskType);
            }
        }
        //文件内容提取es
        long esStartTime = System.currentTimeMillis();
        operateFullQueryService.addEsFileInfo(fileInfoRedisDTO, token.getId());
        long esEndTime = System.currentTimeMillis();
        log.info("文件上传3:{}", esEndTime - esStartTime);
        long l3 = System.currentTimeMillis();
        log.info("文件上传总耗时:{}", l3 - l);
        if (isOcrSuccess) {
            return Result.success(ecmFileInfoDTO);
        } else {
            return Result.success(ecmFileInfoDTO, ResultCode.SUCCESS.getCode(), "OCR识别失败");
        }
    }

    public boolean judgeAfmText(EcmFileInfoDTO ecmFileInfoDTO,String context) {
        boolean flag = false;
        List list = searchIsRead(ecmFileInfoDTO.getFileId());
        if (!CollectionUtils.isEmpty(list)){
            EsEcmFile esEcmFile = (EsEcmFile) list.get(0);
            log.info("当前fileId:{}，获取ES对象esEcmFile:{}",ecmFileInfoDTO.getFileId(),esEcmFile);
            BaseAttachment attachment = esEcmFile.getAttachment();
            String content = attachment == null ? "":attachment.getContent();
            if (!StringUtils.isEmpty(content)) {
                ecmFileInfoDTO.setTextAll(content);
                ecmFileInfoDTO.setTextDet(true);
                flag = true;
            }
        }
        if (!flag && !StringUtils.isEmpty(context)){
            ecmFileInfoDTO.setTextAll(context);
            ecmFileInfoDTO.setTextDet(true);
            flag = true;
        }
        return  flag;
    }



    private String getAfmTaskType(Map<Integer, Boolean> result , List<String> enumConfigList) {
        String taskType = IcmsConstants.ASYNC_TASK_STATUS_INIT;
        //获取菜单状态  0:开启  1：关闭
        Integer duplicateStatus = enumConfigList.contains(IcmsConstants.DUPLICATE_CHECK_STATUS)?IcmsConstants.ZERO:IcmsConstants.ONE;
        if (!CollectionUtils.isEmpty(result)) {
            //判断是否开启文本查重
            boolean plagiarismText = result.get(IcmsConstants.PLAGIARISM_TEXT);
            if (plagiarismText && IcmsConstants.ZERO.equals(duplicateStatus)){
                taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_TEN,
                        EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            }
        }
        return taskType;
    }

    /**
     * 更新审核检测结果
     */
    @WebsocketNoticeAnnotation(busiId = "#ecmReviewDTO.busiId")
    public Result updateReviewDetection(EcmIntelligentDetectionDTO ecmReviewDTO) {
        AssertUtils.isNull(ecmReviewDTO.getFileId(), "文件ID不能为空");
        AssertUtils.isNull(ecmReviewDTO.getBusiId(), "业务ID不能为空");
        AssertUtils.isNull(ecmReviewDTO.getStatus(), "审核状态不能为空");

        // 获取要处理的类型列表（批量或单个）
        List<Integer> typesToProcess;
        if (ecmReviewDTO.getTypes() != null && !ecmReviewDTO.getTypes().isEmpty()) {
            typesToProcess = ecmReviewDTO.getTypes();
        } else {
            AssertUtils.isNull(ecmReviewDTO.getType(), "审核类型不能为空");
            typesToProcess = Collections.singletonList(ecmReviewDTO.getType());
        }

        // 验证所有类型是否支持审核
        for (Integer type : typesToProcess) {
            AssertUtils.isTrue(
                    !type.equals(IcmsConstants.TYPE_THREE) &&
                            !type.equals(IcmsConstants.TYPE_FOUR) &&
                            !type.equals(IcmsConstants.TYPE_SIX) &&
                            !type.equals(IcmsConstants.TYPE_EIGHT) &&
                            !type.equals(IcmsConstants.TYPE_NINE)&&
                            !type.equals(IcmsConstants.TYPE_TEN),
                    "审核失败，类型" + type + "不支持审核"
            );
        }

        // 从 Redis 获取任务
        EcmAsyncTask task = busiCacheService.getEcmAsyncTask(
                RedisConstants.BUSIASYNC_TASK_PREFIX + ecmReviewDTO.getBusiId(),
                ecmReviewDTO.getFileId().toString()
        );

        String currentTaskType = task.getTaskType();
        if (currentTaskType == null || currentTaskType.length() != IcmsConstants.LENGTH) {
            currentTaskType = IcmsConstants.ASYNC_TASK_STATUS_INIT;
        }

        String status = ecmReviewDTO.getStatus();
        char statusChar = status.charAt(0);
        String updatedTaskType = currentTaskType;

        // 批量处理所有类型
        for (Integer type : typesToProcess) {
            // 获取当前审核位置的状态
            int typeIndex = getTypeIndex(type);
            String currentStatus = String.valueOf(updatedTaskType.charAt(typeIndex));

            // 检查该位置的状态是否是待审核（'6'）
            AssertUtils.isTrue(
                    !EcmCheckAsyncTaskEnum.CHECK_FAILED.description().equals(currentStatus),
                    "审核失败：类型" + type + "只能对待审核状态进行审核"
            );

            // 更新当前类型的状态
            updatedTaskType = CheckDetectionUtils.updateStatus(updatedTaskType, type, statusChar);
        }

        // 更新任务状态
        task.setTaskType(updatedTaskType);
        asyncTaskService.updateEcmAsyncTask(task);

        return Result.success("审核成功，共处理" + typesToProcess.size() + "个类型");
    }


    /**
     * 重新尝试执行检测任务
     */
    public void retryTask(EcmIntelligentDetectionDTO retryDTO, AccountTokenExtendDTO token) {
        AssertUtils.isNull(retryDTO.getBusiId(), "业务ID不能为空");
        AssertUtils.isNull(retryDTO.getFileId(), "文件ID不能为空");
        AssertUtils.isNull(retryDTO.getType(), "重试类型不能为空");

        // 从Redis获取任务信息
        EcmAsyncTask task = busiCacheService.getEcmAsyncTask(
                RedisConstants.BUSIASYNC_TASK_PREFIX + retryDTO.getBusiId(),
                retryDTO.getFileId().toString());

        String taskType = task.getTaskType();
        String failedStatus = EcmCheckAsyncTaskEnum.FAILED.description();
        char processingChar = EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0);
        boolean hasUpdated = false;

        // 根据任务类型进行检测重试
        if (IcmsConstants.TYPE_SIX.equals(retryDTO.getType())) {
            // 检查翻拍检测是否为失败状态
            int sixIndex = getTypeIndex(IcmsConstants.TYPE_SIX);
            AssertUtils.isTrue(
                    checkStatus(taskType, sixIndex, failedStatus),
                    "重试失败，翻拍检测当前状态不支持重试"
            );

            // 更新状态
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_SIX, processingChar);
            hasUpdated = true;
        } else if (IcmsConstants.TYPE_TWO.equals(retryDTO.getType())) {
            // 检查转正检测是否为失败状态
            int twoIndex = getTypeIndex(IcmsConstants.TYPE_TWO);
            AssertUtils.isTrue(
                    checkStatus(taskType, twoIndex, failedStatus),
                    "重试失败，转正检测当前状态不支持重试"
            );

            // 更新状态
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_TWO, processingChar);
            hasUpdated = true;
        } else if (IcmsConstants.TYPE_THREE_ALL.equals(retryDTO.getType())) {
            // 模糊检测
            int threeIndex = getTypeIndex(IcmsConstants.TYPE_THREE);
            if (!checkStatus(taskType, threeIndex, failedStatus)) {
                taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_THREE, processingChar);
                hasUpdated = true;
            }

            // 反光检测
            int eightIndex = getTypeIndex(IcmsConstants.TYPE_EIGHT);
            if (!checkStatus(taskType, eightIndex, failedStatus)) {
                taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_EIGHT, processingChar);
                hasUpdated = true;
            }

            // 缺角检测
            int nineIndex = getTypeIndex(IcmsConstants.TYPE_NINE);
            if (!checkStatus(taskType, nineIndex, failedStatus)) {
                taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_NINE, processingChar);
                hasUpdated = true;
            }

            // 检查是否有可重试的状态
            AssertUtils.isTrue(!hasUpdated, "重试失败，当前状态不支持重试");
        } else if (IcmsConstants.TYPE_FOUR.equals(retryDTO.getType())) {
            // 检查查重检测（类型4）是否为失败状态
            int fourIndex = getTypeIndex(IcmsConstants.TYPE_FOUR);
            AssertUtils.isTrue(
                    checkStatus(taskType, fourIndex, failedStatus),
                    "重试失败，查重检测当前状态不支持重试"
            );

            // 更新类型4的状态
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_FOUR, processingChar);
            hasUpdated = true;
        } else if (IcmsConstants.TYPE_ONE.equals(retryDTO.getType())) {
            // 检查单证识别检测（类型1）是否为失败状态
            int oneIndex = getTypeIndex(IcmsConstants.TYPE_ONE);
            AssertUtils.isTrue(
                    checkStatus(taskType, oneIndex, failedStatus),
                    "重试失败，单证识别检测当前状态不支持重试"
            );

            // 更新类型1的状态
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_ONE, processingChar);
            hasUpdated = true;
        }else if (IcmsConstants.TYPE_THREE.equals(retryDTO.getType())) {
            // 模糊检测单独重试
            int threeIndex = getTypeIndex(IcmsConstants.TYPE_THREE);
            AssertUtils.isTrue(
                    checkStatus(taskType, threeIndex, failedStatus),
                    "重试失败，模糊检测当前状态不支持重试"
            );
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_THREE, processingChar);
            hasUpdated = true;
        } else if (IcmsConstants.TYPE_EIGHT.equals(retryDTO.getType())) {
            // 反光检测单独重试
            int eightIndex = getTypeIndex(IcmsConstants.TYPE_EIGHT);
            AssertUtils.isTrue(
                    checkStatus(taskType, eightIndex, failedStatus),
                    "重试失败，反光检测当前状态不支持重试"
            );
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_EIGHT, processingChar);
            hasUpdated = true;
        } else if (IcmsConstants.TYPE_NINE.equals(retryDTO.getType())) {
            // 缺角检测单独重试
            int nineIndex = getTypeIndex(IcmsConstants.TYPE_NINE);
            AssertUtils.isTrue(
                    checkStatus(taskType, nineIndex, failedStatus),
                    "重试失败，缺角检测当前状态不支持重试"
            );
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_NINE, processingChar);
            hasUpdated = true;
        } else if(IcmsConstants.TYPE_TEN.equals(retryDTO.getType())){
            // 缺角检测单独重试
            int tenIndex = getTypeIndex(IcmsConstants.TYPE_TEN);
            AssertUtils.isTrue(
                    checkStatus(taskType, tenIndex, failedStatus),
                    "重试失败，文本查重当前状态不支持重试"
            );
            //文本查重重试 需要同时修改OCR和文本查重状态
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_TEN, processingChar);
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_SEVEN, processingChar);
            hasUpdated = true;
        } else {
            log.error("查询不到该重试状态: {}", retryDTO.getType());
            throw new IllegalArgumentException("不支持的重试类型");
        }

        // 只有状态有更新时才修改数据库
        task.setTaskType(taskType);
        asyncTaskService.updateEcmAsyncTask(task);

        // 走MQ处理
        EcmFileInfoDTO ecmFileInfoDTO = busiCacheService.getFileInfoRedisSingle(task.getBusiId(), task.getFileId());
        checkDetectionService.checkDetectionByMq(ecmFileInfoDTO, task, task.getTaskType());
    }

    private int getTypeIndex(Integer type) {
        return type-1;
    }

    /**
     * 检查指定类型的状态是否为失败
     */
    private boolean checkStatus(String taskType, int index, String failedStatus) {
        return !(index >= 0 && index < taskType.length()
                && failedStatus.charAt(0) == taskType.charAt(index));
    }

    /**
     * 执行自动识别检测的重试操作
     */
    private void retrypDocOCRDetection(EcmAsyncTask task, Long newFileId,
                                       AccountTokenExtendDTO token) {
        //从Redis中获取文件数据
        FileInfoRedisDTO fileInfoRedisSingle = busiCacheService
                .getFileInfoRedisSingle(task.getBusiId(), task.getFileId());
        EcmFileInfoDTO ecmFileInfoDTO = new EcmFileInfoDTO();
        AssertUtils.isNull(fileInfoRedisSingle, "找不到该笔业务");
        BeanUtils.copyProperties(fileInfoRedisSingle,ecmFileInfoDTO);
        ecmFileInfoDTO.setFileId(task.getFileId());
        ecmFileInfoDTO.setNewFileId(newFileId);
        SysStrategyDTO vo = isAutoGroup(ecmFileInfoDTO.getAppCode());
        //处理服务
        String updatedTaskType = handleDocOcr(ecmFileInfoDTO, token, task.getTaskType(), vo);
        // 更新任务状态
        task.setTaskType(updatedTaskType);
        asyncTaskService.updateEcmAsyncTask(task);
    }

    /**
     * 执行翻拍检测的重试操作
     */
    private void retryRemakeDetection(EcmAsyncTask task, Long newFileId,
                                      AccountTokenExtendDTO token) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("file_url",
                storageUrl + "/storage/deal/getFileByFileId?fileId=" + newFileId);
        requestBody.put("threshold", 1000000);
        //处理服务
        String updatedTaskType = remakeHandle(task.getFileId(), task.getTaskType(), requestBody);

        // 更新任务状态
        task.setTaskType(updatedTaskType);
        asyncTaskService.updateEcmAsyncTask(task);
    }

    /**
     * 执行转正检测的重试操作
     */
    private void retryRegularizeDetection(EcmAsyncTask task, Long newFileId,
                                          AccountTokenExtendDTO token) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("file_url",
                storageUrl + "/storage/deal/getFileByFileId?fileId=" + newFileId);
        requestBody.put("threshold", 1000000);
        //处理服务
        //从Redis中获取文件数据
        FileInfoRedisDTO fileInfoRedisSingle = busiCacheService
                .getFileInfoRedisSingle(task.getBusiId(), task.getFileId());
        EcmFileInfoDTO ecmFileInfoDTO = new EcmFileInfoDTO();
        AssertUtils.isNull(fileInfoRedisSingle, "找不到该笔业务");
        BeanUtils.copyProperties(fileInfoRedisSingle, ecmFileInfoDTO);
        String updatedTaskType = regularizeHandle(fileInfoRedisSingle.getFileId(),
                task.getTaskType(), requestBody, ecmFileInfoDTO, token);
        // 更新任务状态
        task.setTaskType(updatedTaskType);
        asyncTaskService.updateEcmAsyncTask(task);
    }

    /**
     * 执行模糊检测的重试操作
     */
    private void retryObscuredDetection(EcmAsyncTask task, Long newFileId,
                                        AccountTokenExtendDTO token) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("file_url",
                storageUrl + "/storage/deal/getFileByFileId?fileId=" + newFileId);
        requestBody.put("threshold", 10000);
        String updatedTaskType = obscureHandle(task.getFileId(), task.getTaskType(), requestBody);
        task.setTaskType(updatedTaskType);
        asyncTaskService.updateEcmAsyncTask(task);
    }

    /**
     * 执行查重检测的重试操作
     */
    private void retrypPlagiarismDetection(EcmAsyncTask task, Long newFileId) {
        String fileUrl = storageUrl + "/storage/deal/getFileByFileId?fileId=" + newFileId;
        //从Redis中获取文件数据
        FileInfoRedisDTO fileInfoRedisSingle = busiCacheService
                .getFileInfoRedisSingle(task.getBusiId(), task.getFileId());
        AssertUtils.isNull(fileInfoRedisSingle, "找不到该笔业务");
        String updatedTaskType = handleAfm(fileInfoRedisSingle, task.getTaskType(), fileUrl);
        task.setTaskType(updatedTaskType);
        asyncTaskService.updateEcmAsyncTask(task);
    }

    /**
     * 影像删除
     */
    @WebsocketNoticeAnnotation(busiId = "#vo.busiId")
    @LogManageAnnotation("删除文件")
    @Transactional(rollbackFor = Exception.class)
    public void deleteFileInfo(FileInfoVO vo, AccountTokenExtendDTO token) {
        AssertUtils.isTrue(IcmsConstants.SHOW_PAGE.equals(token.getIsShow()),"当前无权限,无法进行采集相关操作");
        AssertUtils.isNull(vo.getBusiId(), "参数错误");
        //文件编辑：待提交状态、处理失败状态可进行编辑
        if(!token.isOut()) {
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                    .getEcmBusiInfoRedisDTO(token, vo.getBusiId());
            Integer status = ecmBusiInfoRedisDTO.getStatus();
            if (!BusiInfoConstants.BUSI_STATUS_ZERO.equals(status) && !BusiInfoConstants.BUSI_STATUS_FOUR.equals(status)) {
                AssertUtils.isTrue(true, "原业务状态暂无编辑文件权限");
            }
        }
        vo.setCurentUserId(token.getUsername());
        vo.setUpdateTime(new Date());
        //校验他人操作权限;
        List<Long> fileIdList = new ArrayList<>();
        if (!ObjectUtils.isEmpty(vo.getFileId())) {
            fileIdList.add(vo.getFileId());
        }
        if (!CollectionUtils.isEmpty(vo.getFileIdList()) && ObjectUtils.isEmpty(vo.getFileId())) {
            fileIdList = vo.getFileIdList();
        }
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                vo.getBusiId());
        List<FileInfoRedisDTO> fileInfos = busiCacheService.getFileInfoRedis(vo.getBusiId());
        List<EcmDocrightDefDTO> docrightDefCommon = busiCacheService.getDocrightDefCommon(ecmBusiInfoRedisDTO, token);
        //需要检验的权限列表
        List<String> rights = new ArrayList<>();
        rights.add(DocRightConstants.OTHER_UPDATE);
        commonService.checkDocRight(rights, fileInfos, fileIdList,
                token.getUsername(),docrightDefCommon);
        if (!ObjectUtils.isEmpty(vo.getFileId())) {
            //删除单个影像文件`
            List<Long> fileIds = new ArrayList<>();
            fileIds.add(vo.getFileId());
            vo.setFileIdList(fileIds);
            deleteFileInfoByMore(vo, IcmsConstants.STATE,token);
        }
        if (!CollectionUtils.isEmpty(vo.getFileIdList()) && ObjectUtils.isEmpty(vo.getFileId())) {
            //删除多个影像文件
            deleteFileInfoByMore(vo, IcmsConstants.STATE,token);
        }
        busiOperationService.addOperation(vo.getBusiId(), IcmsConstants.ADD_BUSI, token,
                "修改文件-删除");
    }

    /**
     * 已删除列表中影像删除
     */
    @WebsocketNoticeAnnotation(busiId = "#vo.busiId")
    @LogManageAnnotation("已删除列表中删除文件")
    @Transactional(rollbackFor = Exception.class)
    public void deleteFileInfoTrue(FileInfoVO vo, AccountTokenExtendDTO token) {
        AssertUtils.isTrue(IcmsConstants.SHOW_PAGE.equals(token.getIsShow()),"当前无权限,无法进行采集相关操作");
        AssertUtils.isNull(vo.getBusiId(), "参数错误");
        //文件编辑：待提交状态、处理失败状态可进行编辑
        if(!token.isOut()) {
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                    .getEcmBusiInfoRedisDTO(token, vo.getBusiId());
            Integer status = ecmBusiInfoRedisDTO.getStatus();
            if (!BusiInfoConstants.BUSI_STATUS_ZERO.equals(status) && !BusiInfoConstants.BUSI_STATUS_FOUR.equals(status)) {
                AssertUtils.isTrue(true, "原业务状态暂无编辑文件权限");
            }
        }
        vo.setCurentUserId(token.getUsername());
        vo.setUpdateTime(new Date());
        //校验他人操作权限
        List<Long> fileIdList = new ArrayList<>();
        if (!ObjectUtils.isEmpty(vo.getFileId())) {
            fileIdList.add(vo.getFileId());
        }
        if (!CollectionUtils.isEmpty(vo.getFileIdList()) && ObjectUtils.isEmpty(vo.getFileId())) {
            fileIdList = vo.getFileIdList();
        }
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                vo.getBusiId());
        List<FileInfoRedisDTO> fileInfos = busiCacheService.getFileInfoRedis(vo.getBusiId());
        List<EcmDocrightDefDTO> docrightDefCommon = busiCacheService.getDocrightDefCommon(ecmBusiInfoRedisDTO, token);
        //需要检验的权限列表
        List<String> rights = new ArrayList<>();
        rights.add(DocRightConstants.OTHER_UPDATE);
        commonService.checkDocRight(rights, fileInfos, fileIdList,
                token.getUsername(), docrightDefCommon);
        if (!ObjectUtils.isEmpty(vo.getFileId())) {
            //删除单个影像文件
            List<Long> fileIds = new ArrayList<>();
            fileIds.add(vo.getFileId());
            vo.setFileIdList(fileIds);
            deleteFileInfoByMore(vo, IcmsConstants.IS_DELETED,token);
        }
        if (!CollectionUtils.isEmpty(vo.getFileIdList()) && ObjectUtils.isEmpty(vo.getFileId())) {
            //删除多个影像文件
            deleteFileInfoByMore(vo, IcmsConstants.IS_DELETED,token);
        }
        busiOperationService.addOperation(vo.getBusiId(), IcmsConstants.ADD_BUSI, token,
                "修改文件-删除");
    }

    /**
     * 影像恢复
     */
    @WebsocketNoticeAnnotation(busiId = "#vo.busiId")
    @LogManageAnnotation("还原文件")
    @Transactional(rollbackFor = Exception.class)
    public void restoreFileInfo(FileInfoVO vo, AccountTokenExtendDTO token) {
        AssertUtils.isTrue(IcmsConstants.SHOW_PAGE.equals(token.getIsShow()),"当前无权限,无法进行采集相关操作");
        AssertUtils.isNull(vo.getBusiId(), "参数错误");
        AssertUtils.isNull(vo.getOldDocCode(), "参数错误");
        vo.setCurentUserId(token.getUsername());
        vo.setUpdateTime(new Date());
        //数量验证
        checkNum(vo, token);
        if (!ObjectUtils.isEmpty(vo.getFileId())) {
            //恢复单个影像文件
            List<Long> fileIds = new ArrayList<>();
            fileIds.add(vo.getFileId());
            vo.setFileIdList(fileIds);
            restoreFileInfoByMore(vo);
            //如果恢复之后，该文件是加密的资料节点，则需要升级为加密文件。
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                    vo.getBusiId());
            if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType())) {
                //只处理静态树，动态树不支持加密
                List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService
                        .getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
                if (CollectionUtil.isNotEmpty(fileInfoRedisEntities)) {
                    List<FileInfoRedisDTO> collect = fileInfoRedisEntities.stream()
                            .filter(s -> s.getFileId().equals(vo.getFileId())
                                    && !IcmsConstants.UNCLASSIFIED_ID.equals(s.getDocCode()))
                            .collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(collect)) {
                        FileInfoRedisDTO fileInfoRedisDTO = collect.get(0);
                        //String docCode = fileInfoRedisDTO.getDocCode();
                        //EcmDocDef ecmDocDef = ecmDocDefMapper.selectById(docCode);
                        ArrayList<Long> objects = new ArrayList<>();
                        objects.add(fileInfoRedisDTO.getNewFileId());
                        SysStrategyVO sysStrategyVO = getSysStrategyVO();
                        //commonService.encryptFile(ecmDocDef.getIsEncrypt(), objects);
                        commonService.encryptFile(
                                sysStrategyVO.getEncryptStatus() ? IcmsConstants.YES_ENCRYPT
                                        : IcmsConstants.NO_ENCRYPT,
                                objects);
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(vo.getFileIdList()) && ObjectUtils.isEmpty(vo.getFileId())) {
            //恢复多个影像文件
            restoreFileInfoByMore(vo);
            //如果恢复之后，该文件是加密的资料节点，则需要升级为加密文件。
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                    vo.getBusiId());
            if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType())) {
                //只处理静态树，动态树不支持加密
                List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService
                        .getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
                if (CollectionUtil.isNotEmpty(fileInfoRedisEntities)) {
                    List<FileInfoRedisDTO> collect = fileInfoRedisEntities.stream()
                            .filter(s -> vo.getFileIdList().contains(s.getFileId())
                                    && !IcmsConstants.UNCLASSIFIED_ID.equals(s.getDocCode()))
                            .collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(collect)) {
                        List<String> collect1 = collect.stream().map(FileInfoRedisDTO::getDocId)
                                .collect(Collectors.toList());
                        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectBatchIds(collect1);
                        List<String> collect2 = ecmDocDefs.stream()
                                //filter(s -> IcmsConstants.YES_ENCRYPT.equals(s.getIsEncrypt()))
                                .map(EcmDocDef::getDocCode).collect(Collectors.toList());
                        List<Long> files = collect.stream()
                                .filter(s -> collect2.contains(s.getDocCode()))
                                .map(FileInfoRedisDTO::getNewFileId).collect(Collectors.toList());
                        SysStrategyVO sysStrategyVO = getSysStrategyVO();
                        commonService.encryptFile(
                                sysStrategyVO.getEncryptStatus() ? IcmsConstants.YES_ENCRYPT
                                        : IcmsConstants.NO_ENCRYPT,
                                files);
                    }
                }
            }
        }

        busiOperationService.addOperation(vo.getBusiId(), IcmsConstants.ADD_BUSI, token,
                "修改文件-恢复");
    }

    private SysStrategyVO getSysStrategyVO() {
        Result<SysParamDTO> sysParam = paramApi
                .searchValueByKey(StrategyConstantsEnum.OCR_STRATEGY.toString());
        SysParamDTO data = sysParam.getData();
        String value = data.getValue();
        return JSONObject.parseObject(value, SysStrategyVO.class);
    }

    private void checkNum(FileInfoVO vo, AccountTokenExtendDTO token) {
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                vo.getBusiId());
        if (ecmBusiInfoRedisDTO != null) {
            List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService
                    .getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
            //判断要恢复的文件恢复到原来的节点下会不会有相同的文件
            checkFileMd5(fileInfoRedisEntities, vo);
            //文件权限从关联版本获取最新权限
            List<EcmDocrightDefDTO> currentDocRight = busiCacheService
                    .getDocrightDefCommon(ecmBusiInfoRedisDTO, token);
            List<String> oldDocCode = vo.getOldDocCode();
            oldDocCode.forEach(pp -> {
                if (!IcmsConstants.UNCLASSIFIED_ID.equals(pp)) {
                    //筛选得到要归类到某个节点的权限
                    List<EcmDocrightDefDTO> collect = currentDocRight.stream()
                            .filter(p -> p.getDocCode().equals(pp)).collect(Collectors.toList());
                    EcmDocrightDefDTO ecmDocrightDefDTO = collect.get(StateConstants.ZERO);
                    //根据 docCod得到 docName
                    String docName = null;
                    Integer treeTpye;
                    if (!ObjectUtils.isEmpty(ecmBusiInfoRedisDTO.getTreeType())) {
                        //从redis中查静态树还是动态树
                        treeTpye = ecmBusiInfoRedisDTO.getTreeType();
                    } else {
                        //从数据库中查静态树还是动态树
                        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectById(vo.getBusiId());
                        treeTpye = ecmBusiInfo.getTreeType();
                    }
                    Integer maxLen = null;
                    if (IcmsConstants.STATIC_TREE.equals(treeTpye)) {
                        //静态树
                        EcmDocDef ecmDocDef = ecmDocDefMapper.selectById(pp);
                        AssertUtils.isNull(ecmDocDef, "参数错误");
                        docName = ecmDocDef.getDocName();
                        maxLen = ecmDocrightDefDTO.getMaxLen();
                        if (maxLen == null) {
                            maxLen = ecmDocDef.getMaxFiles();
                        }
                    } else {
                        //动态树
                        List<EcmBusiDoc> ecmBusiDocs = ecmBusiDocMapper
                                .selectList(new LambdaQueryWrapper<EcmBusiDoc>()
                                        .eq(EcmBusiDoc::getDocCode, pp).eq(EcmBusiDoc::getDocMark,
                                                IcmsConstants.DOC_MARK_STATIC_TREE));
                        AssertUtils.isNull(ecmBusiDocs, "参数错误");
                        docName = ecmBusiDocs.get(0).getDocName();
                        Map<String, List<EcmDocrightDefDTO>> collect1 = currentDocRight.stream()
                                .collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
                        List<EcmDocrightDefDTO> ecmDocrightDefDTOS = collect1.get(pp);
                        maxLen = ecmDocrightDefDTOS.get(0).getMaxLen();
                    }

                    AssertUtils.isTrue(
                            String.valueOf(StateConstants.ZERO)
                                    .equals(ecmDocrightDefDTO.getAddRight()),
                            "恢复失败，" + docName + "节点无新增权限！");
                    //判断节点最大上传大小
                    List<FileInfoRedisDTO> collect1 = fileInfoRedisEntities.stream()
                            .filter(p -> p.getDocCode().equals(pp)
                                    && StateConstants.ZERO.equals(p.getState()))
                            .collect(Collectors.toList());
                    int fileNum = collect1.size();
                    AssertUtils.isTrue(fileNum + 1 > maxLen, "恢复失败，超过[" + docName + "]节点最大上传大小！");
                }
            });
        }
    }

    private void checkFileMd5(List<FileInfoRedisDTO> fileInfoRedisEntities, FileInfoVO vo) {
        if (!ObjectUtils.isEmpty(vo.getFileId())) {
            //恢复单个影像文件
            List<Long> fileIds = new ArrayList<>();
            fileIds.add(vo.getFileId());
            vo.setFileIdList(fileIds);
        }
        vo.getFileIdList().forEach(pp -> {
            if (!CollectionUtils.isEmpty(fileInfoRedisEntities)) {
                //要恢复的文件信息
                List<FileInfoRedisDTO> restoreFileInfoList = fileInfoRedisEntities.stream()
                        .filter(p -> p.getFileId().equals(pp)).collect(Collectors.toList());
                AssertUtils.isNull(restoreFileInfoList, "参数错误");
                FileInfoRedisDTO restoreFileInfo = restoreFileInfoList.get(0);
                //得到要恢复到的节点
                String docCode = restoreFileInfo.getDocCode();
                if (!IcmsConstants.UNCLASSIFIED_ID.equals(docCode)) {
                    List<String> fileMd5 = fileInfoRedisEntities.stream()
                            .filter(p -> p.getDocCode().equals(docCode)
                                    && StateConstants.ZERO.equals(p.getState()))
                            .map(FileInfoRedisDTO::getFileMd5).collect(Collectors.toList());
                    AssertUtils.isTrue(fileMd5.contains(restoreFileInfo.getFileMd5()),
                            "恢复失败,[" + restoreFileInfo.getDocName() + "]节点中已存在要恢复的文件:"
                                    + restoreFileInfo.getNewFileName());
                }
            }
        });
    }

    /**
     * 新增文件关联业务信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Result insertFileInfoApi(EcmFileInfoApiDTO ecmFileInfoApiDTO) {
        long start = System.currentTimeMillis();
        com.sunyard.ecm.dto.EcmFileInfoDTO ecmFileInfoDTO = ecmFileInfoApiDTO.getEcmFileInfoDTO();
        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        EcmBaseInfoDTO ecmBaseInfoDTO = ecmFileInfoApiDTO.getAddBusiDTO().getEcmBaseInfoDTO();
        token.setRoleCodeList(ecmBaseInfoDTO.getRoleCode());
        token.setUsername(ecmBaseInfoDTO.getUserCode());
        token.setName(ecmBaseInfoDTO.getUserName());
        token.setOrgCode(ecmBaseInfoDTO.getOrgCode());
        token.setOrgName(ecmBaseInfoDTO.getOrgName());
        token.setOut(true);
        List<SysFileApiDTO> files = ecmFileInfoApiDTO.getFiles();
        files.parallelStream().forEach(sysFileDTO -> {
            ecmFileInfoDTO.setBusiId(ecmFileInfoDTO.getBusiId());
            if (IcmsConstants.STATIC_TREE.equals(Integer.valueOf(ecmFileInfoDTO.getTreeType()))) {
                ecmFileInfoDTO.setDocId(ecmFileInfoDTO.getDocCode());
            }
            ecmFileInfoDTO.setBusiBatchNo(UUID.randomUUID().toString());
            ecmFileInfoDTO.setFileReuse(0);
            if (ecmFileInfoDTO.getFileSort() == null) {
                ecmFileInfoDTO.setFileSort(0d);
            }
            ecmFileInfoDTO.setSize(sysFileDTO.getSize().toString());
            ecmFileInfoDTO.setDocFileSort(sysFileDTO.getFileDocSort());
            ecmFileInfoDTO.setNewFileLock(0);
            ecmFileInfoDTO.setState(0);
            ecmFileInfoDTO.setFileId(sysFileDTO.getId());
            ecmFileInfoDTO.setFileMd5(sysFileDTO.getFileMd5());
            ecmFileInfoDTO.setNewFileName(sysFileDTO.getOriginalFilename());
            ecmFileInfoDTO.setSize(String.valueOf(sysFileDTO.getSize()));
            ecmFileInfoDTO.setFormat(sysFileDTO.getExt());
            ecmFileInfoDTO.setSourceFileMd5(sysFileDTO.getSourceFileMd5());
            ecmFileInfoDTO.setAppCode(ecmFileInfoDTO.getAppCode());
            ecmFileInfoDTO.setAppTypeName(ecmFileInfoDTO.getAppTypeName());
            ecmFileInfoDTO.setBusiNo(ecmFileInfoDTO.getBusiNo());
            ecmFileInfoDTO.setBusiBatchNo(UUID.randomUUID().toString());
            ecmFileInfoDTO.setSize(sysFileDTO.getSize().toString());

            EcmFileInfoDTO fileInfoDTO = new EcmFileInfoDTO();
            BeanUtils.copyProperties(ecmFileInfoDTO, fileInfoDTO);
            fileInfoDTO.setBusiNo(ecmFileInfoDTO.getBusiNo());
            fileInfoDTO.setSize(Long.valueOf(ecmFileInfoDTO.getSize()));
            if (null == fileInfoDTO.getFileSort()) {
                fileInfoDTO.setFileSort(Double.valueOf(IcmsConstants.ZERO));
            }
            insertFileInfo(fileInfoDTO, token);
        });
        long end = System.currentTimeMillis();
        log.info("文件关联成功总时长：{}", end - start);
        //对外接口成功后发送mq
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,ecmFileInfoDTO.getBusiId());
        captureSubmitService.sendMQMessage(ecmBusiInfoRedisDTO);
        return Result.success();
    }

    /**
     * 新增文件关联业务信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Result insertFileListInfo(EcmFileInfoApiDTO ecmFileInfoApiDTO) {
        long start = System.currentTimeMillis();
        //设置token
        EcmBaseInfoDTO ecmBaseInfoDTO = ecmFileInfoApiDTO.getAddBusiDTO().getEcmBaseInfoDTO();
        AccountTokenExtendDTO token = new AccountTokenExtendDTO();
        token.setRoleCodeList(ecmBaseInfoDTO.getRoleCode());
        token.setUsername(ecmBaseInfoDTO.getUserCode());
        token.setName(ecmBaseInfoDTO.getUserName());
        token.setOrgCode(ecmBaseInfoDTO.getOrgCode());
        token.setOrgName(ecmBaseInfoDTO.getOrgName());
        token.setOut(true);
        //获取 资料类型
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectList(null);
        Map<String, List<EcmDocDef>> docCodeListMap = ecmDocDefs.stream()
                .collect(Collectors.groupingBy(EcmDocDef::getDocCode));
        //处理sdk的传值
        List<EcmFileInfoDTO> ecmFileInfoDTOS = handleEcmFileInfoApiDTO(ecmFileInfoApiDTO, token);
        //添加持久化数据库信息
        List<EcmFileInfo> ecmFileInfos = BeanUtil.copyToList(ecmFileInfoDTOS, EcmFileInfo.class);
        insertEcmFileInfos(ecmFileInfos);

        //ecmFileInfoMapper.insertEcm(ecmFileInfos);
        //添加业务操作记录
        //处理并添加标签
        List<EcmFileLabel> labels = new ArrayList<>();
        handleLabels(ecmFileInfoApiDTO, ecmFileInfos, labels);
        String remark = "对外接口批量添加文件:";
        busiOperationService.addOperation(ecmFileInfoApiDTO.getEcmFileInfoDTO().getBusiId(),
                IcmsConstants.ADD_FILE, token, remark);
        //生成文件历史集合、添加es信息
        List<EcmFileHistory> ecmFileHistoryList = new ArrayList<>();
        Date date = new Date();
        Map<Long, List<EcmFileLabel>> collect = labels.stream()
                .collect(Collectors.groupingBy(EcmFileLabel::getFileId));
        ecmFileInfoDTOS.forEach(p -> {
            EcmFileHistory ecmFileHistory = new EcmFileHistory();
            ecmFileHistory.setFileId(p.getFileId());
            ecmFileHistory.setBusiId(p.getBusiId());
            ecmFileHistory.setNewFileId(p.getNewFileId());
            ecmFileHistory.setNewFileSize(p.getSize());
            ecmFileHistory.setFileOperation(IcmsConstants.ADD_FILE_OPERATION_STRING);
            ecmFileHistory.setCreateUser(token.getUsername());
            ecmFileHistory.setCreateTime(date);
            ecmFileHistory.setNewFileExt(CommonUtils.getExt(p));
            ecmFileHistoryList.add(ecmFileHistory);
            //添加资料类型名称
            addDocTypeName(p, docCodeListMap);
            FileInfoRedisDTO fileInfoRedisDTO = new FileInfoRedisDTO();
            BeanUtils.copyProperties(p, fileInfoRedisDTO);
            fileInfoRedisDTO.setEcmFileLabels(collect.get(p.getFileId()));
            //添加es数据
            operateFullQueryService.addEsFileInfo(fileInfoRedisDTO, token.getId());
        });

        //批量新增文件历史
        insertBatchEcmFileHistory(ecmFileHistoryList);
        //更新redis
        addFileListByBusiInfoRedis(ecmFileInfoDTOS, ecmFileHistoryList, labels);
        long end = System.currentTimeMillis();
        //智能化检测处理
        String treeType=ecmBaseInfoDTO.getTypeTree();
        if(StringUtils.isEmpty(treeType)){
            //如果为空默认静态树
            treeType= String.valueOf(IcmsConstants.STATIC_TREE);
        }
        EcmBusExtendDTO ecmBusExtendDTO=ecmFileInfoApiDTO.getAddBusiDTO().getEcmBusExtendDTOS();
        handleIntelligentDetection(ecmFileInfoDTOS,treeType,ecmBusExtendDTO.getAppCode());
        log.info("文件关联成功总时长：{}", end - start);
        //对外接口成功后发送mq
        if (!CollectionUtils.isEmpty(ecmFileInfoDTOS)){
            for (EcmFileInfoDTO ecmFileInfoDTO : ecmFileInfoDTOS) {
                EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,ecmFileInfoDTO.getBusiId());
                captureSubmitService.sendMQMessage(ecmBusiInfoRedisDTO);
            }
        }
        return Result.success();
    }

    private void handleLabels(EcmFileInfoApiDTO ecmFileInfoApiDTO, List<EcmFileInfo> ecmFileInfos,
                              List<EcmFileLabel> labels) {
        List<SysFileApiDTO> files = ecmFileInfoApiDTO.getFiles();
        Map<Long, List<EcmFileInfo>> listMap = ecmFileInfos.stream()
                .collect(Collectors.groupingBy(EcmFileInfo::getNewFileId));

        Long busiId = ecmFileInfoApiDTO.getEcmFileInfoDTO().getBusiId();
        files.stream()
                .filter(sysFileApiDTO -> CollectionUtil.isNotEmpty(sysFileApiDTO.getFileLabels()))
                .forEach(sysFileApiDTO -> {
                    List<String> fileLabels = sysFileApiDTO.getFileLabels();
                    Long fileId = listMap.get(sysFileApiDTO.getId()).get(0).getFileId();
                    for (String label : fileLabels) {
                        Long id = snowflakeUtil.nextId();
                        EcmFileLabel ecmFileLabel = new EcmFileLabel();
                        ecmFileLabel.setId(id);
                        ecmFileLabel.setFileId(fileId);
                        ecmFileLabel.setBusiId(busiId);
                        ecmFileLabel.setLabelName(label);
                        //todo 改批量插入
                        ecmFileLabelMapper.insert(ecmFileLabel);
                        labels.add(ecmFileLabel);
                    }
                });

    }

    private void handleIntelligentDetection(List<EcmFileInfoDTO> ecmFileInfos,String treeType,String appCode) {
        SysStrategyDTO vo = isAutoGroup(appCode);
        ecmFileInfos.forEach(item->{
            if(IMGS.contains(item.getFormat())){
                Map<Integer, Boolean> result = taskSwitchService.queryAllSwitches(item.getDocCode());
                List<String> enumConfigList = sysStrategyService.queryEcmEnumConfig();
                String taskType = CheckDetectionUtils.getTaskType(result, vo, enumConfigList);
                EcmAsyncTask ecmAsyncTask = new EcmAsyncTask();
                ecmAsyncTask.setTaskType(taskType);
                ecmAsyncTask.setBusiId(item.getBusiId());
                ecmAsyncTask.setFileId(item.getFileId());
                asyncTaskService.insert(ecmAsyncTask);
                checkDetectionService.checkDetectionByMq(item,ecmAsyncTask,taskType);
            }
        });
    }

    private void insertEcmFileInfos(List<EcmFileInfo> ecmFileInfos) {
        MybatisBatch<EcmFileInfo> mybatisBatch = new MybatisBatch<>(sqlSessionFactory,
                ecmFileInfos);
        MybatisBatch.Method<EcmFileInfo> method = new MybatisBatch.Method<>(
                EcmFileInfoMapper.class);
        mybatisBatch.execute(method.insert());
    }

    private void insertBatchEcmFileHistory(List<EcmFileHistory> ecmFileHistoryList) {
        MybatisBatch<EcmFileHistory> mybatisBatch = new MybatisBatch<>(sqlSessionFactory,
                ecmFileHistoryList);
        MybatisBatch.Method<EcmFileHistory> method = new MybatisBatch.Method<>(
                EcmFileHistoryMapper.class);
        mybatisBatch.execute(method.insert());
    }

    /**
     * 根据经纬度获取位置信息（省市区）
     */
    public Result<AreaInfoDTO> getLocation(Double lon, Double lat) {
        if (ObjectUtils.isEmpty(lon) || ObjectUtils.isEmpty(lat)) {
            return Result.success();
        }
        LocationParserEngine engine = locationParserEngineService.getEngine();
        Location location = engine.parse(lon, lat);
        if (location != null) {
            // 省信息
            RegionInfo provInfo = location.getProv();
            // 省名称
            final String provName = provInfo.getName();
            // 市信息
            RegionInfo cityInfo = location.getCity();
            // 市名称
            final String cityName = cityInfo.getName();
            // 区信息
            RegionInfo districtInfo = location.getDistrict();
            // 区名称
            final String districtName = districtInfo.getName();
            return Result.success(new AreaInfoDTO().setProvName(provName).setCityName(cityName)
                    .setDistrictName(districtName));

        } else {
            return Result.success();
        }

    }

    /**
     * 异步下载文件
     */
    private void downloadFileById(EcmFileInfo file, String path, DownFileVO downFileVO) {
        String originalFilename = file.getNewFileName();
        String fileNameWithoutExtension = originalFilename.substring(0,
                originalFilename.lastIndexOf('.'));
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        // 将文件写到download/file.apk中

        downFileVO.setFileName(originalFilename);
        List<Long> objects = new LinkedList<>();
        objects.add(file.getNewFileId());
        downFileVO.setFileId(objects);
        Result<byte[]> down = fileStorageApi.down(downFileVO);
        if (down.isSucc()) {
            File file1 = new File(path);
            if (!file1.exists()) {
                file1.mkdirs();
            }

            String filePath = path + "/" + fileNameWithoutExtension + "_" + file.getNewFileId()
                    + fileExtension;
            try(FileOutputStream fos = new FileOutputStream(filePath)){
                fos.write(down.getData());
            } catch (Exception e) {
                log.error("异步下载文件异常",e);
            }
        }
    }

    /**
     * 文件名处理
     */
    public static File generateTargetFile(String path, String originalFileName, Long fileId) {
        String fileNameWithoutExtension = originalFileName.substring(0,
                originalFileName.lastIndexOf('.'));
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String newFileName = fileNameWithoutExtension + "_" + fileId + fileExtension;
        return new File(path + "/" + newFileName);
    }

    // 创建一个固定大小的线程池，这里设置为10个线程
    private final ExecutorService executor = new ThreadPoolExecutor(10, // 核心线程数
            10, // 最大线程数
            0L, // 空闲线程存活时间
            TimeUnit.MILLISECONDS, // 时间单位
            new LinkedBlockingQueue<Runnable>(), // 任务队列
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(10);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "fileDownLoad-" + threadNumber.getAndIncrement());
                }
            });

    /**
     * 全部下载
     */
    public void downFileAll(Long markId, String docCode, Long busiId, AccountTokenExtendDTO token,
                            HttpServletRequest request, HttpServletResponse response) {
        AssertUtils.isNull(token, "很遗憾，您没下载权限");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(busiId);
        if (!StringUtils.isEmpty(docCode)) {
            stringBuilder.append("_" + docCode);
        }
        stringBuilder.append("_" + DateUtils.dateTimeNow());
        String pathName = stringBuilder.toString();
        List<EcmFileInfo> ecmFileInfos = ecmFileInfoMapper
                .selectList(new LambdaQueryWrapper<EcmFileInfo>().eq(EcmFileInfo::getBusiId, busiId)
                        .eq(markId != null, EcmFileInfo::getMarkDocId, markId)
                        .eq(!StringUtils.isEmpty(docCode), EcmFileInfo::getDocCode, docCode));
        AssertUtils.isNull(ecmFileInfos, "暂无可下载的文件");
        DownFileVO vo = new DownFileVO();
        vo.setName(token.getName());
        vo.setUsername(token.getUsername());
        vo.setOrgCode(token.getOrgCode());
        vo.setOrgName(token.getOrgName());
        String sessionId = request.getSession().getId();
        vo.setSessionId(sessionId);
        vo.setIsPack(StateConstants.NO);
        //线程池
        CountDownLatch latch = new CountDownLatch(ecmFileInfos.size());
        for (EcmFileInfo file : ecmFileInfos) {
            executor.execute(() -> {
                downloadFileById(file, sunEcmProperties.getFileDownloadPath() + "/" + pathName, vo);
                latch.countDown();
            });
        }
        FileInputStream inputStream = null;
        try {
            latch.await();
            //所有线程都执行完毕，进行打包
            //打包
            String zipFileName = sunEcmProperties.getFileDownloadPath() + "/" + pathName + ".zip";
            String folderToCompress = sunEcmProperties.getFileDownloadPath() + "/" + pathName;
            //打成压缩包
            ZipUtils.toZip(folderToCompress, zipFileName);
            FileSizeUtils.loadFileBig(response, request, zipFileName);

        } catch (Exception e) {
            log.error("文件全部下载异常",e);
        } finally {

            try {
                if (null != inputStream) {
                    inputStream.close();
                    inputStream = null;
                }
            } catch (IOException e) {
                log.error("异常描述", e);
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    /**
     * 查询文档属性
     */
    public List<EcmDtdAttr> getOcrAttrList(Long dtdTypeId, AccountTokenExtendDTO token) {
        AssertUtils.isNull(dtdTypeId, "单证id不能为空");
        List<EcmDtdAttr> ecmDtdAttrs = ecmDtdAttrMapper.selectList(
                new LambdaUpdateWrapper<EcmDtdAttr>().eq(EcmDtdAttr::getDtdTypeId, dtdTypeId));
        return ecmDtdAttrs;
    }

    /**
     * 查询文档类型
     */
    public List<EcmDtdDef> searchFileAttrType(OcrResultVO vo, AccountTokenExtendDTO token) {
        AssertUtils.isNull(vo.getBusiId(), "业务id不能为空");
        List<EcmDtdDef> ecmDtdDefs = ecmDtdDefMapper.selectList(null);
        return ecmDtdDefs;
    }

    /**
     * 获取翻拍结果
     */
    public Result getReviewDetection(EcmIntelligentDetectionDTO ecmIntelligentDetectionDTO) {
        AssertUtils.isNull(ecmIntelligentDetectionDTO.getBusiId(), "业务id不能为空");
        AssertUtils.isNull(ecmIntelligentDetectionDTO.getFileId(), "文件id不能为空");

        EcmAsyncTask ecmAsyncTask = asyncTaskMapper.selectOne(new LambdaQueryWrapper<EcmAsyncTask>()
                .eq(EcmAsyncTask::getBusiId, ecmIntelligentDetectionDTO.getBusiId())
                .eq(EcmAsyncTask::getFileId, ecmIntelligentDetectionDTO.getFileId()));

        Map<String, Object> resultMap = new HashMap<>();
        if (ecmAsyncTask != null) {
            String taskType = ecmAsyncTask.getTaskType();
            StringBuilder sb = new StringBuilder(taskType);

            // 处理各类检测状态
            handleDetectionStatus(sb, IcmsConstants.TYPE_THREE - 1, "dim", resultMap);
            handleDetectionStatus(sb, IcmsConstants.TYPE_FOUR - 1, "duplicate", resultMap);
            handleDetectionStatus(sb, IcmsConstants.TYPE_SIX - 1, "remake", resultMap);
            handleDetectionStatus(sb, IcmsConstants.TYPE_EIGHT - 1, "reflective", resultMap);
            handleDetectionStatus(sb, IcmsConstants.TYPE_NINE - 1, "missCorner", resultMap);
            handleDetectionStatus(sb, IcmsConstants.TYPE_TEN - 1, "duplicateText", resultMap);
        }

        return Result.success(resultMap);
    }

    /**
     * 统一处理检测状态的方法
     * @param taskTypeBuilder 任务类型字符串构建器
     * @param index 状态索引位置
     * @param prefix 结果map中的键前缀
     * @param resultMap 结果map
     */
    private void handleDetectionStatus(StringBuilder taskTypeBuilder, int index,
                                       String prefix, Map<String, Object> resultMap) {
        // 确保索引不越界
        if (index < 0 || index >= taskTypeBuilder.length()) {
            resultMap.put(prefix + "StatusStr", "未知状态");
            resultMap.put(prefix, -1);
            return;
        }

        String status = String.valueOf(taskTypeBuilder.charAt(index));
        String statusStr;

        // 建立状态码与描述的映射关系
        Map<String, String> statusMapping = new HashMap<>();
        statusMapping.put(EcmCheckAsyncTaskEnum.CHECK_FAILED.description(), "待处理");
        statusMapping.put(EcmCheckAsyncTaskEnum.EXCLUDE_ANOMALY.description(), "已排除异常");
        statusMapping.put(EcmCheckAsyncTaskEnum.CONFIRM_ANOMALY.description(), "已确认异常");


        // 获取对应的状态描述
        if (statusMapping.containsKey(status)) {
            statusStr = statusMapping.get(status);
            resultMap.put(prefix + "StatusStr", statusStr);
        }
        try {
            resultMap.put(prefix, Integer.valueOf(status));
        } catch (NumberFormatException e) {
            resultMap.put(prefix, -1);
        }
    }

    /**
     *
     * @param busiId
     * @return
     */
    public Integer getCheckAutoGroupSize(Long busiId){
        Map<Object, Object> map = busiCacheService
                .getAllAutoGroup(RedisConstants.AUTO_CLASS_PENDING_TASK_LIST + busiId);
        return map.size();
    }
    /**
     * 自动归类识别检测
     */
    public Result getCheckAutoGroupList(Long busiId) {
        // 获取所有待处理任务列表
        Map<Object, Object> map = busiCacheService
                .getAllAutoGroup(RedisConstants.AUTO_CLASS_PENDING_TASK_LIST + busiId);

        // 创建结果列表
        List<EcmAutoCheckListDTO> list = new ArrayList<>(map.size());

        // 遍历每个待处理任务
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Long fileId = Long.parseLong(entry.getKey().toString());
            List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = (List<EcmBusiDocRedisDTO>) entry
                    .getValue();
            //按照资料节点顺序排序
            ecmBusiDocRedisDTOS = ecmBusiDocRedisDTOS.stream()
                    .sorted(Comparator.comparing(EcmBusiDocRedisDTO::getDocSort))
                    .collect(Collectors.toList());

            // 构建 docCodes 列表
            List<String> docCodes = ecmBusiDocRedisDTOS.stream().map(EcmBusiDocRedisDTO::getDocCode)
                    .collect(Collectors.toList());

            // 获取文件信息
            FileInfoRedisDTO fileInfoRedisDTO = busiCacheService.getFileInfoRedisSingle(busiId,
                    fileId);

            // 构建 EcmAutoCheckListDTO 对象并设置相关属性
            EcmAutoCheckListDTO ecmAutoCheckListDTO = new EcmAutoCheckListDTO();
            ecmAutoCheckListDTO.setDocCodes(docCodes);
            ecmAutoCheckListDTO.setNewFileId(fileInfoRedisDTO.getNewFileId());
            ecmAutoCheckListDTO.setFileId(fileInfoRedisDTO.getFileId());
            ecmAutoCheckListDTO.setFileName(fileInfoRedisDTO.getNewFileName());

            // 将对象添加到结果列表
            list.add(ecmAutoCheckListDTO);
        }

        // 返回成功结果
        return Result.success(list);
    }

    /**
     * 修改用户自动归类识别开关
     */
    public Result updateCheckAutoUserSelf(AccountTokenExtendDTO tokenExtendDTO) {
        String userName = tokenExtendDTO.getUsername();
        //判断redis是否有,有就删除，没有就新增
        Boolean flag = busiCacheService.hasAutoGroup(userName);
        if (flag) {
            busiCacheService.delAutoGroup(userName);
        } else {
            busiCacheService.setAutoGroup(userName, "true");
        }
        return Result.success();
    }

    /**
     * 获取查重结果
     */
    public Result getNoteRes(AccountTokenExtendDTO token, EcmIntelligentDetectionDTO ecmIntelligentDetectionDTO) {
        AfmDetImgDetDTO dto = new AfmDetImgDetDTO();
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                ecmIntelligentDetectionDTO.getBusiId());
        dto.setBusinessIndex(ecmBusiInfoRedisDTO.getBusiNo());
        dto.setBusinessType(ecmBusiInfoRedisDTO.getAppCode());
        dto.setInvoiceType(IcmsConstants.AFM_TYPE);
        dto.setSourceSys(IcmsConstants.ECM);
        FileInfoRedisDTO fileInfoRedisSingle = busiCacheService.getFileInfoRedisSingle(
                ecmIntelligentDetectionDTO.getBusiId(), ecmIntelligentDetectionDTO.getFileId());
        dto.setFileIndex(fileInfoRedisSingle.getFileId().toString());
        Result<Map> mapResult = new Result<>();
        if (IcmsConstants.FOUR.equals(ecmIntelligentDetectionDTO.getType())){
            mapResult = antiFraudDetApi.antiFraudDetRes(dto);
        }else if (IcmsConstants.TEN.equals(ecmIntelligentDetectionDTO.getType())){
            mapResult = antiFraudDetApi.antiFraudDetResByText(dto);
        }
        Map<String, Object> map = new HashMap<>();
        EcmFileInfo ecmFileInfo = new EcmFileInfo();
        BeanUtils.copyProperties(fileInfoRedisSingle, ecmFileInfo);
//        addDocRight(token, map, ecmFileInfo);
//        List<EcmDocrightDefDTO> docRightList = busiCacheService
//                .getDocrightDefCommon(ecmBusiInfoRedisDTO, token);
//        Map<String, List<EcmDocrightDefDTO>> docRightGroupedById = docRightList.stream()
//                .filter(p -> !ObjectUtils.isEmpty(p.getDocCode()))
//                .collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
//        List<EcmDocrightDefDTO> docrightDefExtendList = docRightGroupedById
//                .get(fileInfoRedisSingle.getDocCode());
//        map.put("docRight", docrightDefExtendList.get(0));
        map.put("orgCode", ecmBusiInfoRedisDTO.getOrgCode());
        fileInfoRedisSingle.setDocRight((EcmDocrightDefDTO) map.get("docRight"));
        //处理afm响应返回值
        processAfmResponse(mapResult, token);
        mapResult.getData().put("fileInfos", fileInfoRedisSingle);
        return Result.success(mapResult);
    }

    private void processAfmResponse(Result<Map> result, AccountTokenExtendDTO token) {
        if (result.isSucc()) {
            Map<String, ?> data = (Map<String, ?>) result.getData();
            Map<String, Object> dupList = (Map<String, Object>) data.get("dup");
            List<Map> similarityList = (List<Map>) dupList.get("similarityList");
            log.info(result.getData().toString());
            if (!CollectionUtils.isEmpty(similarityList)) {
                ArrayList<Long> longs = new ArrayList<>();

                ArrayList<Map> objects = new ArrayList<>();
                for (Map map : similarityList) {
                    long l = Long.parseLong(map.get("fileIndex").toString());
                    longs.add(l);
                }
                List<EcmFileInfo> ecmFileInfos = ecmFileInfoMapper.selectList(
                        new LambdaQueryWrapper<EcmFileInfo>().in(EcmFileInfo::getFileId, longs));
                if (CollectionUtils.isEmpty(ecmFileInfos)) {
                    dupList.put("similarityList", objects);
                    return;
                }
                Map<Long, List<EcmFileInfo>> collect = ecmFileInfos.stream()
                        .collect(Collectors.groupingBy(EcmFileInfo::getFileId));

                for (Map map : similarityList) {
                    //删除url
                    map.remove("fileFullPath");
                    map.remove("fileUrl");
                    //默认都已删除,影像中存在的数据改为影像的状态
                    map.put("state", "1");
                    //添加busiid
                    List<EcmFileInfo> ecmFileInfos1 = collect
                            .get(Long.parseLong(map.get("fileIndex").toString()));
                    if (!CollectionUtils.isEmpty(ecmFileInfos1)) {
                        EcmFileInfo fileInfoRedisSingle = ecmFileInfos1.get(0);
                        if (IcmsConstants.ONE.equals(fileInfoRedisSingle.getState())){
                            //已删除的不返回
                            continue;
                        }
                        map.put("busiId", fileInfoRedisSingle.getBusiId());
                        map.put("newFileId", fileInfoRedisSingle.getNewFileId());
                        map.put("fileId", fileInfoRedisSingle.getFileId());
                        map.put("state", fileInfoRedisSingle.getState());
                        //计算大小
                        calculateFileSize(map, fileInfoRedisSingle);
                        //添加docRight,orgCode ,权限需要修改到详情再查看
                        map.put("orgCode", fileInfoRedisSingle.getOrgCode());
                        objects.add(map);
                    }
                }
                dupList.put("similarityList", objects);
            }

        }
    }

    public Result addDocRight(AccountTokenExtendDTO token,
                              EcmIntelligentDetectionDTO ecmIntelligentDetectionDTO) {
        Map<String, Object> map = new HashMap<>();
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                ecmIntelligentDetectionDTO.getBusiId());
        List<EcmDocrightDefDTO> docRightList = busiCacheService
                .getDocrightDefCommon(ecmBusiInfoRedisDTO, token);
        FileInfoRedisDTO fileInfoRedisSingle = busiCacheService.getFileInfoRedisSingle(
                ecmIntelligentDetectionDTO.getBusiId(), ecmIntelligentDetectionDTO.getFileId());
        Map<String, List<EcmDocrightDefDTO>> docRightGroupedById = docRightList.stream()
                .filter(p -> !ObjectUtils.isEmpty(p.getDocCode()))
                .collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
        List<EcmDocrightDefDTO> docrightDefExtendList = docRightGroupedById
                .get(fileInfoRedisSingle.getDocCode());
        map.put("docRight", docrightDefExtendList.get(0));
        return Result.success(map);
    }

    private void calculateFileSize(Map map, EcmFileInfo fileInfoRedisSingle) {
        try {
            Double newFileSize = 0.00;
            String fileUnit = "";
            Long oldFileSize = fileInfoRedisSingle.getNewFileSize();
            if (oldFileSize > 0 && oldFileSize <= 1024 * 1024) {
                newFileSize = Math.ceil(oldFileSize.doubleValue() / 1024);
                fileUnit = IcmsConstants.FILE_UNIT_K;
            } else if (oldFileSize > 1024 * 1024 && oldFileSize <= 1024 * 1024 * 1024) {
                newFileSize = Double.valueOf(
                        String.format("%.3f", (oldFileSize.doubleValue() / (1024 * 1024))));
                fileUnit = IcmsConstants.FILE_UNIT_M;
            } else {
                newFileSize = Double.valueOf(
                        String.format("%.3f", (oldFileSize.doubleValue() / (1024 * 1024 * 1024))));
                fileUnit = IcmsConstants.FILE_UNIT_G;
            }
            map.put("fileSize", newFileSize.toString());
            map.put("fileUnit", fileUnit);
        } catch (NumberFormatException e) {
            log.error("单位转换有误：{}", e.getMessage());
        }
    }

    private boolean calculateFileSizeToM(Long oldFileSize) {
        boolean flag = false;
        try {
            Double newFileSize = Double.valueOf(
                    String.format("%.3f", (oldFileSize.doubleValue() / (1024 * 1024))));
            flag = newFileSize > pdfDefaultMaxSize;
        }catch (Exception e){
            log.error("转换错误：{}", e.getMessage());
        }
        return flag;
    }

    /**
     * 获取用户自动归类识别开关
     */
    public Result getCheckAutoUserSelfFlag(AccountTokenExtendDTO tokenExtendDTO) {
        String userName = tokenExtendDTO.getUsername();
        //判断redis是否有,有就删除，没有就新增,有Key表示关闭,无key表示开启
        Boolean flag = !busiCacheService.hasAutoGroup(userName);
        return Result.success(flag);
    }

    /**
     * 修改查重审核状态
     */
    public Result updateCheckRepeatStatus(EcmFileInfoDTO ecmFileInfoDTO) {
        try {
            AssertUtils.isNull(ecmFileInfoDTO.getFileId(), "文件id不能为空");
            AssertUtils.isNull(ecmFileInfoDTO.getBusiId(), "业务id不能为空");
            // status 4:排除查重异常  5：确认查重异常
            AssertUtils.isNull(ecmFileInfoDTO.getCheckRepeatStatus(), "查重审核状态不能为空");

            //查询task状态
            EcmAsyncTask ecmAsyncTask = busiCacheService.getEcmAsyncTask(
                    RedisConstants.BUSIASYNC_TASK_PREFIX + ecmFileInfoDTO.getBusiId(),
                    ecmFileInfoDTO.getFileId().toString());
            if (!ObjectUtils.isEmpty(ecmAsyncTask)) {
                String taskType = ecmAsyncTask.getTaskType();

                taskType = Objects.equals(ecmFileInfoDTO.getCheckRepeatStatus(),
                        IcmsConstants.TYPE_FOUR)
                                ? CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_FOUR,
                                        EcmCheckAsyncTaskEnum.EXCLUDE_ANOMALY.description()
                                                .charAt(0))
                                : CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_FOUR,
                                        EcmCheckAsyncTaskEnum.CONFIRM_ANOMALY.description()
                                                .charAt(0));
                ecmAsyncTask.setTaskType(taskType);

                asyncTaskService.updateEcmAsyncTask(ecmAsyncTask);
            }
        } catch (Exception e) {
            log.error("影像查重审核状态修改失败 : {}", e);
            return Result.error("影像查重审核状态异常", ResultCode.SYSTEM_ERROR);
        }

        return Result.success();
    }

    /**
     * 获取查重结果集
     */
    public Result getCheckRepeatImageFiles(List<EcmFileInfoDTO> ecmFileInfoDTO) {
        HashMap<String, Object> returnMap = new HashMap<>();
        try {
            for (EcmFileInfoDTO ecmFileInfo : ecmFileInfoDTO) {
                //数据校验
                AssertUtils.isNull(ecmFileInfo.getFileId(), "文件id不能为空");

                //获取查重结果
                List<Map> similarityList = getResultFromAfm(ecmFileInfo);

                EcmFileInfo ecmFileDetail = ecmFileInfoMapper.selectById(ecmFileInfo.getFileId());

                //赋值
                setFileInfoFailed(ecmFileDetail, ecmFileInfo);

                HashMap<String, Object> objectHashMap = new HashMap<>();
                objectHashMap.put("fileInfo", ecmFileInfoDTO);
                objectHashMap.put("similarList", similarityList);

                returnMap.put(ecmFileInfo.getFileId().toString(), objectHashMap);
            }

        } catch (Exception e) {
            log.error("获取查重结果集失败 : {}", e);
            return Result.error("获取查重结果集失败", ResultCode.SYSTEM_ERROR);
        }
        return Result.success(returnMap);
    }

    private List<Map> getResultFromAfm(EcmFileInfoDTO ecmFileInfoDTO) {
        List<Map> similarityList = new ArrayList<>();
        try {
            AfmDetImgDetDTO dto = new AfmDetImgDetDTO();
            dto.setInvoiceType(IcmsConstants.AFM_TYPE);
            dto.setSourceSys(IcmsConstants.ECM);
            dto.setFileIndex(ecmFileInfoDTO.getFileId().toString());

            //调用查重接口
            Result result = antiFraudDetApi.antiFraudDetRes(dto);
            if (result.isSucc()) {
                Map<String, ?> data = (Map<String, ?>) result.getData();
                //获取查重结果
                Map<String, ?> dupList = (Map<String, ?>) data.get("dup");
                similarityList = (List<Map>) dupList.get("similarityList");
            }
        } catch (Exception e) {
            log.error("获取查重结果集失败 : {}", e);
            throw new RuntimeException(e.getMessage(), e);
        }
        return similarityList;
    }

    private void setFileInfoFailed(EcmFileInfo ecmFileInfo, EcmFileInfoDTO ecmFileInfoDTO) {

        EcmDocDef ecmDocDef = ecmDocDefMapper.selectById(ecmFileInfo.getDocCode());
        ecmFileInfoDTO.setComment(ObjectUtils.isEmpty(ecmDocDef) ? "" : ecmDocDef.getDocName());
        ecmFileInfoDTO.setDocName(ObjectUtils.isEmpty(ecmDocDef) ? "" : ecmDocDef.getDocName());

        //赋值
        ecmFileInfoDTO.setNewFileSize(ecmFileInfo.getNewFileSize());
        ecmFileInfoDTO.setBusiId(ecmFileInfo.getBusiId());
        ecmFileInfoDTO.setOrgName(ecmFileInfo.getOrgName());
        ecmFileInfoDTO.setCreateUserName(ecmFileInfo.getCreateUserName());
        ecmFileInfoDTO.setCreateTime(ecmFileInfo.getCreateTime());
    }

    /**
     * 获取自动归类信息，如果有多个资料满足则返回资料树
     */
    private EcmFileInfoDTO checkAutoGroup(EcmFileInfoDTO ecmFileInfoDTO, AccountTokenExtendDTO tokenExtendDTO,EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        if(IMGS.contains(ecmFileInfoDTO.getFormat())){
            // 调用 OCR 识别
            JSONObject requestBody = createOcrRequestBody(ecmFileInfoDTO);
            try {
                long l3 = System.currentTimeMillis();
                String response = executePostRequest(sunEcmProperties.getAutoClassUrl(), requestBody);
                JSONObject result = JSONObject.parseObject(response);
                long l4 = System.currentTimeMillis();
                log.info("上传66总耗时:{}", l4 - l3);
                // 只有当成功时才继续执行
                if (result.getInteger("errorCode") == 0) {
                    JSONObject data = result.getJSONObject("result");
                    JSONArray categories = data.getJSONArray("categories");
                    for (int i = 0; i < categories.size(); i++) {
                        JSONObject jsonObject = categories.getJSONObject(i);
                        BigDecimal bigDecimal = jsonObject.getBigDecimal("score");
                        log.info("识别到的类型：" + jsonObject.toJSONString());
                        if (bigDecimal.compareTo(new BigDecimal(ecmOcrProperties.getAutoClassThread())) > 0) {
                            String classIds = jsonObject.getString("id");
                            // 获取该业务的资料树
                            List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = ecmBusiInfoRedisDTO
                                    .getEcmBusiDocRedisDTOS();

                            // 筛选开启了自动归类并且归类类型一致的数据
                            Map<String,EcmDocDef> docDefMap=busiCacheService.getDocInfoAll();
                            List<EcmBusiDocRedisDTO> filterDocs = filterDocsRecursively(
                                    ecmBusiDocRedisDTOS, classIds,docDefMap);

                            // 根据筛选出的资料类型决定如何处理
                            processFilteredDocs(filterDocs, ecmFileInfoDTO);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("ocr识别异常",e);
                //设置为未归类
                ecmFileInfoDTO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                ecmFileInfoDTO.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
                ecmFileInfoDTO.setComment("");
            }
            return ecmFileInfoDTO;
        }else{
            if(StringUtils.isEmpty(ecmFileInfoDTO.getContentFirstPage())){
                ecmFileInfoDTO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                ecmFileInfoDTO.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
                ecmFileInfoDTO.setComment("");
            }else{
                //文档类型自动分类
                JSONObject requestBody = new JSONObject();
                requestBody.put("text", ecmFileInfoDTO.getContentFirstPage());
                try {
                    long l3 = System.currentTimeMillis();
                    String response = executePostRequest(sunEcmProperties.getAutoClassDocUrl(), requestBody);
                    JSONObject result = JSONObject.parseObject(response);
                    long l4 = System.currentTimeMillis();
                    log.info("上传66总耗时:{}", l4 - l3);
                    // 只有当成功时才继续执行
                    if (result.getInteger("errorCode") == 0) {
                        JSONObject data = result.getJSONObject("result");
                        JSONArray categories = data.getJSONArray("categories");
                        for (int i = 0; i < categories.size(); i++) {
                            JSONObject jsonObject = categories.getJSONObject(i);
                            BigDecimal bigDecimal = jsonObject.getBigDecimal("score");
                            log.info("识别到的类型：" + jsonObject.toJSONString());
                            if (bigDecimal.compareTo(new BigDecimal(ecmOcrProperties.getAutoClassThread())) > 0) {
                                String classIds = jsonObject.getString("id");
                                // 获取该业务的资料树
                                List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = ecmBusiInfoRedisDTO
                                        .getEcmBusiDocRedisDTOS();

                                // 筛选开启了自动归类并且归类类型一致的数据
                                Map<String,EcmDocDef> docDefMap=busiCacheService.getDocInfoAll();
                                List<EcmBusiDocRedisDTO> filterDocs = filterDocsRecursively(
                                        ecmBusiDocRedisDTOS, classIds,docDefMap);

                                // 根据筛选出的资料类型决定如何处理
                                processFilteredDocs(filterDocs, ecmFileInfoDTO);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                    //设置为未归类
                    ecmFileInfoDTO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
                    ecmFileInfoDTO.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
                    ecmFileInfoDTO.setComment("");
                }
            }
            return ecmFileInfoDTO;
        }

    }

    private JSONObject createOcrRequestBody(EcmFileInfoDTO ecmFileInfoDTO) {
        JSONObject requestBody = new JSONObject();
        String fileUrl = storageUrl + "/storage/deal/getFileByFileId?fileId="
                + ecmFileInfoDTO.getNewFileId();
        requestBody.put("image", fileUrl);
        requestBody.put("topk", 1);
        return requestBody;
    }

    private void processFilteredDocs(List<EcmBusiDocRedisDTO> filterDocs,
                                     EcmFileInfoDTO ecmFileInfoDTO) {
        if (filterDocs.size() > 1) {
            Long busiId = ecmFileInfoDTO.getBusiId();
            // 如果筛选出的资料类型大于1，存入 Redis 中并归类到未分类中
            ecmFileInfoDTO.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
            ecmFileInfoDTO.setDocName(IcmsConstants.UNCLASSIFIED);
            ecmFileInfoDTO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
            ecmFileInfoDTO.setComment("");
            // 设置缓存
            busiCacheService.setAutoClassPendingTaskList(
                    RedisConstants.AUTO_CLASS_PENDING_TASK_LIST + ecmFileInfoDTO.getBusiId(),
                    ecmFileInfoDTO.getFileId().toString(), filterDocs);
            //通知前端当前待处理的数量
            Integer num = getCheckAutoGroupSize(busiId);
            WebSocketMessageDTO dto = new WebSocketMessageDTO();
            dto.setMsgType(WebSocketMsgTypeEnum.AUTO_CLASS_NUM.description());
            dto.setBuisIdList(Collections.singletonList(busiId.toString()));
            dto.setContentText(String.valueOf(num));
            webSocketService.pushMsg(dto);
        } else if (filterDocs.size() == 1) {
            // 如果筛选出的资料类型只有 1 个
            EcmBusiDocRedisDTO doc = filterDocs.get(0);
            ecmFileInfoDTO.setDocCode(doc.getDocCode());
            ecmFileInfoDTO.setDocName(doc.getDocName());
            ecmFileInfoDTO.setDocId(doc.getDocCode());
            ecmFileInfoDTO.setComment(doc.getDocName());//添加备注
        } else {
            // 如果没有找到匹配的资料类型，归类到未分类
            ecmFileInfoDTO.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
            ecmFileInfoDTO.setDocName(IcmsConstants.UNCLASSIFIED);
            ecmFileInfoDTO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
            ecmFileInfoDTO.setComment("");
        }
    }

    /**
     * 处理sdk的传值
     */
    private List<EcmFileInfoDTO> handleEcmFileInfoApiDTO(EcmFileInfoApiDTO ecmFileInfoApiDTO,
                                                         AccountTokenExtendDTO token) {
        List<EcmFileInfoDTO> ecmFileInfoDTOS = new ArrayList<>();
        com.sunyard.ecm.dto.EcmFileInfoDTO ecmFileInfoDTO = ecmFileInfoApiDTO.getEcmFileInfoDTO();

        List<SysFileApiDTO> files = ecmFileInfoApiDTO.getFiles();
        for (SysFileApiDTO sysFileDTO : files) {
            ecmFileInfoDTO.setBusiId(ecmFileInfoDTO.getBusiId());
            if (IcmsConstants.STATIC_TREE.equals(Integer.valueOf(ecmFileInfoDTO.getTreeType()))) {
                ecmFileInfoDTO.setDocId(ecmFileInfoDTO.getDocCode());
            }
            ecmFileInfoDTO.setBusiBatchNo(UUID.randomUUID().toString());
            ecmFileInfoDTO.setFileReuse(0);
            if (ecmFileInfoDTO.getFileSort() == null) {
                ecmFileInfoDTO.setFileSort(0d);
            }
            ecmFileInfoDTO.setSize(sysFileDTO.getSize().toString());
            ecmFileInfoDTO.setDocFileSort(sysFileDTO.getFileDocSort());
            ecmFileInfoDTO.setNewFileLock(0);
            ecmFileInfoDTO.setState(0);
            ecmFileInfoDTO.setFileId(sysFileDTO.getId());
            ecmFileInfoDTO.setFileMd5(sysFileDTO.getFileMd5());
            ecmFileInfoDTO.setNewFileName(sysFileDTO.getOriginalFilename());
            ecmFileInfoDTO.setSize(String.valueOf(sysFileDTO.getSize()));
            ecmFileInfoDTO.setFormat(sysFileDTO.getExt());
            ecmFileInfoDTO.setNewFileExt(sysFileDTO.getExt());
            ecmFileInfoDTO.setSourceFileMd5(sysFileDTO.getSourceFileMd5());
            ecmFileInfoDTO.setAppCode(ecmFileInfoDTO.getAppCode());
            ecmFileInfoDTO.setAppTypeName(ecmFileInfoDTO.getAppTypeName());
            ecmFileInfoDTO.setBusiNo(ecmFileInfoDTO.getBusiNo());
            ecmFileInfoDTO.setBusiBatchNo(UUID.randomUUID().toString());
            ecmFileInfoDTO.setSize(sysFileDTO.getSize().toString());
            ecmFileInfoDTO.setIsFilePassword(sysFileDTO.getIsFilePassword());
            EcmFileInfoDTO fileInfoDTO = new EcmFileInfoDTO();
            BeanUtils.copyProperties(ecmFileInfoDTO, fileInfoDTO);
            fileInfoDTO.setBusiNo(ecmFileInfoDTO.getBusiNo());
            fileInfoDTO.setSize(Long.valueOf(ecmFileInfoDTO.getSize()));
            fileInfoDTO.setNewFileSize(sysFileDTO.getSize());
            if (null == fileInfoDTO.getFileSort()) {
                fileInfoDTO.setFileSort(Double.valueOf(IcmsConstants.ZERO));
            }
            addFileInfo(token, fileInfoDTO.getBusiId(),
                    fileInfoDTO.getDocId(), fileInfoDTO);
            Long fileId = snowflakeUtil.nextId();
            fileInfoDTO.setFileId(fileId);
            ecmFileInfoDTOS.add(fileInfoDTO);
        }
        return ecmFileInfoDTOS;
    }

    /**
     * 合并文件前后插入文件信息
     */
    @WebsocketNoticeAnnotation(busiId = "#vo.busiId")
    @LogManageAnnotation("合并文件")
    @Transactional(rollbackFor = Exception.class)
    public Result mergInsertFileInfo(MergFileVO vo, AccountTokenExtendDTO token) {
        EcmFileInfoDTO ecmFileInfoDTO1 = vo.getEcmFileInfoDTO();
        ecmFileInfoDTO1.setDocCode(vo.getDocCode());
        //检查判空
        checkEmpt(vo, ecmFileInfoDTO1);
        //给合并后的文件赋值
        if (vo.getDocCode().equals(vo.getDocId())) {
            addFileInfo(token, vo.getBusiId(), vo.getDocCode(), ecmFileInfoDTO1);
        } else {
            addFileInfo(token, vo.getBusiId(),
                    vo.getDocId() == null ? vo.getDocCode() : vo.getDocId(), ecmFileInfoDTO1);
        }
        //合并后md5校验
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService
                .getFileInfoRedis(ecmFileInfoDTO1.getBusiId());
        if (!CollectionUtils.isEmpty(fileInfoRedis)) {
            Map<String, List<FileInfoRedisDTO>> collect = fileInfoRedis.stream()
                    .filter(s -> !s.getDocCode().equals(IcmsConstants.UNCLASSIFIED_ID)
                            && s.getState().equals(StateConstants.NOT_DELETE))
                    .collect(Collectors.groupingBy(EcmFileInfoDTO::getDocId));
            List<FileInfoRedisDTO> fileInfoRedisDTOS = collect.get(ecmFileInfoDTO1.getDocId());
            if (fileInfoRedisDTOS != null) {
                List<String> collect1 = fileInfoRedisDTOS.stream().map(FileInfoRedisDTO::getFileMd5)
                        .collect(Collectors.toList());
                if (collect1.contains(ecmFileInfoDTO1.getFileMd5())) {
                    AssertUtils.isTrue(true, "资料节点下已存在合并后的文件");
                }
            }
        }
        //将合并后的新文件插入文件信息表
        insertEcmFileInfo(ecmFileInfoDTO1, token);
        //需要新增的文件集合
        List<FileInfoRedisDTO> addFileInfoRedisDTOS = new ArrayList<>();
        String extension = CommonUtils.getExt(ecmFileInfoDTO1);
        //添加合并后的新文件的历史记录
        EcmFileHistory ecmMergeFileHistory = commonService.insertFileHistory(
                ecmFileInfoDTO1.getBusiId(), ecmFileInfoDTO1.getFileId(),
                ecmFileInfoDTO1.getNewFileId(), IcmsConstants.MERG_FILE_STRING,
                ecmFileInfoDTO1.getCreateUser(), ecmFileInfoDTO1.getSize(), extension);
        //处理要新增的redis信息
        FileInfoRedisDTO mergeFileInfoRedisDTO = handleRedisInfo(ecmFileInfoDTO1, token,
                ecmMergeFileHistory);
        addFileInfoRedisDTOS.add(mergeFileInfoRedisDTO);
        //新增合并后文件的es信息
        operateFullQueryService.addEsFileInfo(mergeFileInfoRedisDTO, token.getId());
        //删除合并前的文件
        //合并前的文件id集合
        List<Long> fileIdList = vo.getFileIdList();
        //处理合并前文件信息 用于更新数据库
        FileInfoVO fileInfoVO = handleMergeBeforeFileInfo(vo, token, fileIdList);
        //修改合并前文件信息 更新持久化数据库数据
        deleteFileInfoByMoreToDb(fileInfoVO, IcmsConstants.STATE);
        for (int i = 0; i < fileIdList.size(); i++) {
            AssertUtils.isNull(fileIdList.get(i), "文件id不能为空");
        }
        //更新redis数据(添加新的文件和删除和合并前的文件和修改合并前的文件信息)
        updateFileByBusiInfoRedis(addFileInfoRedisDTOS, fileIdList, token);
        //添加操作记录表
        busiOperationService.addOperation(vo.getBusiId(), IcmsConstants.EDIT_FILE_MERG, token,
                "修改文件-合并: " + vo.getNewFileNames());
        //发起智能化处理
        EcmAsyncTask ecmAsyncTask = new EcmAsyncTask();
        //修改taskType
        getTaskType(ecmFileInfoDTO1,ecmAsyncTask);
        ecmAsyncTask.setBusiId(ecmFileInfoDTO1.getBusiId());
        ecmAsyncTask.setFileId(ecmFileInfoDTO1.getFileId());
        asyncTaskMapper.insert(ecmAsyncTask);
        //存入map中后续发起智能化处理
        //智能化处理
//        checkDetectionByMq(ecmFileInfoDTO1,ecmAsyncTask,ecmAsyncTask.getTaskType());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 事务已提交，此时发 MQ 安全
                checkDetectionService.checkDetectionByMq(ecmFileInfoDTO1, ecmAsyncTask, ecmAsyncTask.getTaskType());
            }
        });
        return Result.success(true);
    }

    /**
     * 拆分文件前后插入文件信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#vo.busiId + '_' + #vo.newFileName")
    @WebsocketNoticeAnnotation(busiId = "#vo.busiId")
    public Result splitFile(SplitFileVO vo, AccountTokenExtendDTO token) {
        AssertUtils.isNull(vo.getBusiId(), "业务主键id不能为空");
        AssertUtils.isNull(vo.getEcmFileInfoDTO(), "被拆分的文件信息不能为空");
        AssertUtils.isNull(vo.getAppTypeName(), "业务类型名称不能为空");
        AssertUtils.isNull(vo.getBusiNo(), "业务编号不能为空");
        AssertUtils.isNull(vo.getEcmFileInfoDTO().getSize(), "业务编号不能为空");
        //文件编辑：待提交状态、处理失败状态可进行编辑
        if(!token.isOut()) {
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                    .getEcmBusiInfoRedisDTO(token, vo.getBusiId());
            Integer status = ecmBusiInfoRedisDTO.getStatus();
            if (!BusiInfoConstants.BUSI_STATUS_ZERO.equals(status) && !BusiInfoConstants.BUSI_STATUS_FOUR.equals(status)) {
                AssertUtils.isTrue(true, "原业务状态暂无编辑文件权限");
            }
        }
        FileSplitPdfVO fileSplitPdfVO = new FileSplitPdfVO();
        fileSplitPdfVO.setNewFileId(vo.getEcmFileInfoDTO().getNewFileId().toString());
        fileSplitPdfVO.setBusiBatchNo(vo.getBusiBatchNo());
        fileSplitPdfVO.setFilename(vo.getNewFileName());
        fileSplitPdfVO.setIsEncrypt(vo.getIsEncrypt());
        Long size = vo.getEcmFileInfoDTO().getSize();

        if (size > sunEcmProperties.getMaxSplitFileSize()) {
            String flagId = token.getFlagId();
            UserBusiRedisDTO userPageRedis = busiCacheService.getUserPageRedis(flagId, token);
            HashSet<Long> splitFileId = userPageRedis.getSplitFileId();
            if (CollectionUtil.isNotEmpty(splitFileId)) {
                AssertUtils.isTrue(splitFileId.contains(vo.getEcmFileInfoDTO().getNewFileId()),
                        "当前文件已处于拆分中，请耐心等待");
                splitFileId.add(vo.getEcmFileInfoDTO().getNewFileId());
                userPageRedis.setSplitFileId(splitFileId);
                busiCacheService.saveOrUpdateUser(flagId, userPageRedis);
            } else {
                //不存在则设置值；
                HashSet<Long> objects = new HashSet<>();
                objects.add(vo.getEcmFileInfoDTO().getNewFileId());
                userPageRedis.setSplitFileId(objects);
                busiCacheService.saveOrUpdateUser(flagId, userPageRedis);
            }
            //大于5m的文件异步拆分
            String key = RedisConstants.BUSIASYNC_TASK_PREFIX + vo.getBusiId();
            EcmAsyncTask ecmAsyncTask = busiCacheService.getEcmAsyncTask(key, vo.getEcmFileInfoDTO().getFileId().toString());
            String taskType;
            if(ecmAsyncTask==null){
                ecmAsyncTask = new EcmAsyncTask();
                taskType=IcmsConstants.ASYNC_TASK_STATUS_INIT;
                taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_FIVE,
                        EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
                ecmAsyncTask.setTaskType(taskType);
                ecmAsyncTask.setBusiId(vo.getBusiId());
                ecmAsyncTask.setFileId(vo.getEcmFileInfoDTO().getFileId());
                asyncTaskService.insert(ecmAsyncTask);
            }else{
                taskType=ecmAsyncTask.getTaskType();
                taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_FIVE,
                        EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
                ecmAsyncTask.setTaskType(taskType);
                asyncTaskService.updateEcmAsyncTask(ecmAsyncTask);
            }
            //往redis写入key,表示检测中,需要定时任务刷新
            busiCacheService.setNeedPushBusiSync(
                    RedisConstants.NEED_PUSH_BUSIASYNC_TASK_PREFIX + vo.getBusiId(),
                    IcmsConstants.DETECTING, TimeOutConstants.ONE_HOURS);
            //写入异步任务栏状态
            EcmAsyncTask finalEcmAsyncTask = ecmAsyncTask;
            executor.execute(() -> {
                fileSplitService.backSplitFileAsn(vo, token, fileSplitPdfVO, finalEcmAsyncTask);
            });

            return Result.success(null, StateConstants.SUCC, "异步拆分中，请耐心等待");
        } else {
            Result<Boolean> booleanResult = fileSplitService.backSplitFile(vo, token, fileSplitPdfVO);
            return booleanResult;
        }

    }

    /**
     * 合并文件(图片、文档类型文件合并为pdf并上传到OSS)
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<SysFileDTO> mergeFile(FileEcmMergeVO fileEcmMergeVO,
                                        AccountTokenExtendDTO token) {
        fileEcmMergeVO.setUserId(token.getId());
        if (fileEcmMergeVO.getFileIdList().size() > sunEcmProperties.getMaxMergeFileSize()) {
            //如果合并文件大于10个,则走异步
            String taskType = IcmsConstants.ASYNC_TASK_STATUS_INIT;
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_FIVE,
                    EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            List<EcmAsyncTask> insertEcmAsyncTaskList = new ArrayList<>();
            List<EcmAsyncTask> updateEcmAsyncTaskList = new ArrayList<>();
            EcmAsyncTask ecmAsyncTask = null;
            Long busiId = Long.valueOf(fileEcmMergeVO.getBusiBatchNo());
            for (Long fileId : fileEcmMergeVO.getFileIdList()) {
                EcmAsyncTask task = busiCacheService.getEcmAsyncTask(
                        RedisConstants.BUSIASYNC_TASK_PREFIX + busiId, fileId.toString());
                if (ObjectUtils.isEmpty(task)) {
                    ecmAsyncTask = new EcmAsyncTask();
                    ecmAsyncTask.setTaskType(taskType);
                    ecmAsyncTask.setBusiId(Long.valueOf(fileEcmMergeVO.getBusiBatchNo()));
                    ecmAsyncTask.setFileId(fileId);
                    insertEcmAsyncTaskList.add(ecmAsyncTask);
                } else {
                    //获取当前状态
                    String updateTaskType = task.getTaskType();
                    //如果当前处于异步合并状态则不允许
                    String status = String
                            .valueOf(updateTaskType.charAt(IcmsConstants.TYPE_FIVE - 1));
                    if (EcmCheckAsyncTaskEnum.PROCESSING.description().equals(status) || EcmCheckAsyncTaskEnum.IN_MQ.description().equals(status)) {
                        return Result.error(null, "所选文件已有异步合并任务!", StateConstants.FAIL);
                    }
                    //如果有文件目前有转正翻盘等异步任务处理中,则不处理
                    if (updateTaskType.contains(EcmCheckAsyncTaskEnum.PROCESSING.description()) || updateTaskType.contains(EcmCheckAsyncTaskEnum.IN_MQ.description())) {
                        return Result.error(null, "所选文件已有异步任务在执行中,请执行完毕以后再合并操作!",
                                StateConstants.FAIL);
                    }
                    updateTaskType = CheckDetectionUtils.updateStatus(updateTaskType, IcmsConstants.TYPE_FIVE,
                            EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
                    task.setTaskType(updateTaskType);
                    updateEcmAsyncTaskList.add(task);
                }
            }

            asyncTaskService.batchInsert(insertEcmAsyncTaskList);
            //往redis写入key,表示检测中,需要定时任务刷新
            busiCacheService.setNeedPushBusiSync(
                    RedisConstants.NEED_PUSH_BUSIASYNC_TASK_PREFIX + busiId,
                    IcmsConstants.DETECTING, TimeOutConstants.ONE_HOURS);
            //异步合并
            executor.execute(() -> {
                List<EcmAsyncTask> taskList = new ArrayList<>();
                taskList.addAll(insertEcmAsyncTaskList);
                taskList.addAll(updateEcmAsyncTaskList);
                backMergeFileAsn(fileEcmMergeVO, token, taskList);
            });
            return Result.success(null, StateConstants.SUCC, "异步合并中,请耐心等待");
        } else {
            //不走异步
            Result<SysFileDTO> result = fileHandleApi.mergeFile(fileEcmMergeVO);
            if (!result.isSucc()) {
                throw new SunyardException(result.getMsg());
            }
            return result;
        }

    }

    /**
     * 异步合并文件
     */
    @LogManageAnnotation("异步合并文件")
    @WebsocketNoticeAnnotation(busiId = "#vo.busiBatchNo")
    @Transactional(rollbackFor = Exception.class)
    public void backMergeFileAsn(FileEcmMergeVO vo, AccountTokenExtendDTO token,
                                 List<EcmAsyncTask> taskList) {
        try {
            Result<SysFileDTO> result = fileHandleApi.mergeFile(vo);
            if (!result.isSucc()) {
                throw new SunyardException(result.getMsg());
            }

            SysFileDTO sysFileDTO = result.getData();
            //处理完成以后调用mergInsertFileInfo
            //封装参数
            MergFileVO mergFileVO = new MergFileVO();
            mergFileVO.setBusiId(Long.valueOf(vo.getBusiBatchNo()));
            mergFileVO.setDocCode(vo.getDocCode());
            mergFileVO.setDocId(vo.getDocId());
            mergFileVO.setFileIdList(vo.getFileIdList());
            mergFileVO.setNewFileIdList(vo.getNewFileIdList());
            mergFileVO.setNewFileNames(vo.getNewFileNames());

            //处理ecmFileInfoDTO
            EcmFileInfoDTO ecmFileInfoDTO = JSONObject.parseObject(
                    JSONObject.toJSONString(vo.getEcmFileInfoDTO()), EcmFileInfoDTO.class);

            //设置合并后文件属性
            ecmFileInfoDTO.setFileId(sysFileDTO.getId());
            ecmFileInfoDTO.setFileMd5(sysFileDTO.getFileMd5());
            ecmFileInfoDTO.setNewFileName(sysFileDTO.getOriginalFilename());
            ecmFileInfoDTO.setFormat(sysFileDTO.getExt());
            ecmFileInfoDTO.setSize(sysFileDTO.getSize());

            mergFileVO.setEcmFileInfoDTO(ecmFileInfoDTO);
            //保存文件到ecm
            mergInsertFileInfo(mergFileVO, token);
            //删除合并之前文件的异步任务列表
            asyncTaskService.batchDelete(vo.getFileIdList(), Long.valueOf(vo.getBusiBatchNo()));
            EcmAsyncTask ecmAsyncTask = new EcmAsyncTask();
            String taskType = IcmsConstants.ASYNC_TASK_STATUS_INIT;
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_FIVE,
                    EcmCheckAsyncTaskEnum.MERGE_SUCCESS.description().charAt(0));
            //修改任务状态,拆分成功
            ecmAsyncTask.setTaskType(taskType);
            ecmAsyncTask.setBusiId(Long.valueOf(vo.getBusiBatchNo()));
            ecmAsyncTask.setFileId(sysFileDTO.getId());
            asyncTaskService.insert(ecmAsyncTask);
            //设置redis检测完成
            busiCacheService.setNeedPushBusiSync(
                    RedisConstants.NEED_PUSH_BUSIASYNC_TASK_PREFIX + vo.getBusiBatchNo(),
                    IcmsConstants.DETECTION_COMPLETE, TimeOutConstants.ONE_HOURS);
        } catch (Exception e) {
            batchUpdateFailTaskType(taskList);
            throw new SunyardException(e.getMessage());
        }
    }

    //开新事务避免回滚
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void batchUpdateFailTaskType(List<EcmAsyncTask> taskList) {
        taskList.forEach(item -> {
            String taskType = item.getTaskType();
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_FIVE,
                    EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
            item.setTaskType(taskType);
        });
        asyncTaskService.batchUpdateTask(taskList);
    }

    /**
     * 旋转文件前后插入文件信息
     */
    @Transactional(rollbackFor = Exception.class)
    @LogManageAnnotation("编辑文件")
    @WebsocketNoticeAnnotation(busiId = "#vo.busiId")
    public Result rotateInsertFileInfo(RotateFileVO vo, AccountTokenExtendDTO token) {
        List<EcmFileInfoDTO> resultList = new ArrayList<>();
        AssertUtils.isNull(vo.getBusiId(), "业务主键id不能为空");
        AssertUtils.isNull(vo.getEcmFileInfoExtendsNew(), "旋转后的文件信息集合不能为空");
        Date date = new Date();
        //文件编辑：待提交状态、处理失败状态可进行编辑
        if(!token.isOut()) {
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                    .getEcmBusiInfoRedisDTO(token, vo.getBusiId());
            Integer status = ecmBusiInfoRedisDTO.getStatus();
            if (!BusiInfoConstants.BUSI_STATUS_ZERO.equals(status) && !BusiInfoConstants.BUSI_STATUS_FOUR.equals(status)) {
                AssertUtils.isTrue(true, "原业务状态暂无编辑文件权限");
            }
        }
        for (EcmFileInfoDTO e : vo.getEcmFileInfoExtendsNew()) {
            e.setUpdateTime(date);
            e.setUpdateUser(token.getUsername());
            e.setUpdateUserName(token.getName());
            resultList.add(e);
        }
        //批量修改数据
        ecmFileInfoMapper.updateBatch(resultList);
        //更新redis数据(添加新的文件)
        String fileOperation = IcmsConstants.ROTATE_FILE_STRING;
        if(IcmsConstants.ONE.equals(vo.getRegularize())){
            fileOperation = IcmsConstants.REGULARIZE_FILE_STRING;
        }
        updateFileNameToRedisToFixImage(vo.getEcmFileInfoExtendsNew(), vo.getCreateUserName(),
                token,fileOperation);
        //添加业务操作记录表
        List<String> collect = vo.getEcmFileInfoExtendsNew().stream()
                .map(EcmFileInfoDTO::getNewFileName).collect(Collectors.toList());
        AssertUtils.isNull(collect, "旋转后的文件信息名称不能为空");
        busiOperationService.addOperation(vo.getBusiId(), IcmsConstants.EDIT_FILE_SPLIT, token,
                "修改文件-旋转: " + collect);
        return Result.success(resultList);
    }



    /**
     * 替换文件前后插入文件信息
     */
    @Transactional(rollbackFor = Exception.class)
    @LogManageAnnotation("编辑文件")
    @WebsocketNoticeAnnotation(busiId = "#vo.busiId")
    public Result replaceInsertFileInfo(RotateFileVO vo, AccountTokenExtendDTO token) {
        AssertUtils.isNull(vo.getBusiId(), "业务主键id不能为空");
        AssertUtils.isNull(vo.getEcmFileInfoExtendsNew(), "替换后的文件信息集合不能为空");
        List<Long> fileIdList = new ArrayList<>();
        vo.getEcmFileInfoExtendsNew().forEach(s->fileIdList.add(s.getOldFileId()));
        //校验权限
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                vo.getBusiId());
        //文件编辑：待提交状态、处理失败状态可进行编辑
        if(!token.isOut()) {
            Integer status = ecmBusiInfoRedisDTO.getStatus();
            if (!BusiInfoConstants.BUSI_STATUS_ZERO.equals(status) && !BusiInfoConstants.BUSI_STATUS_FOUR.equals(status)) {
                AssertUtils.isTrue(true, "原业务状态暂无编辑文件权限");
            }
        }
        List<FileInfoRedisDTO> fileInfos = busiCacheService.getFileInfoRedis(vo.getBusiId());
        List<EcmDocrightDefDTO> docrightDefCommon = busiCacheService.getDocrightDefCommon(ecmBusiInfoRedisDTO, token);
        //需要检验的权限列表
        List<String> rights = new ArrayList<>();
        rights.add(DocRightConstants.OTHER_UPDATE);
        rights.add(DocRightConstants.UPDATE_RIGHT);
        commonService.checkDocRight(rights, fileInfos, fileIdList,
                token.getUsername(), docrightDefCommon);
        Date date = new Date();
        Integer treeType = ecmBusiInfoRedisDTO.getTreeType();
        for (EcmFileInfoDTO e : vo.getEcmFileInfoExtendsNew()) {
            e.setUpdateTime(date);
            e.setUpdateUser(token.getUsername());
            //修改数据
            ecmFileInfoMapper.update(new LambdaUpdateWrapper<EcmFileInfo>()
                .eq(EcmFileInfo::getFileId,e.getOldFileId())
                .set(EcmFileInfo::getNewFileId,e.getFileId())
                .set(EcmFileInfo::getFileMd5,e.getFileMd5())
                .set(EcmFileInfo::getUpdateUser,e.getUpdateUser())
                .set(EcmFileInfo::getNewFileSize,e.getSize())
                .set(EcmFileInfo::getUpdateTime,e.getUpdateTime())
                .set(EcmFileInfo::getNewFileName,e.getNewFileName())
                .set(EcmFileInfo::getNewFileExt,e.getFormat()));
            ecmFileExpireInfoMapper.delete(new LambdaQueryWrapper<EcmFileExpireInfo>()
                .eq(EcmFileExpireInfo::getFileId,e.getOldFileId()));
        }
        //更新redis数据(添加新的文件)
        updateFileNameToRedisToFixImage(vo.getEcmFileInfoExtendsNew(), vo.getCreateUserName(),
                token,IcmsConstants.REPLACE_FILE_STRING);
        //智能化处理
        asyTask(vo, treeType);
        //添加业务操作记录表
        List<String> collect = vo.getEcmFileInfoExtendsNew().stream()
                .map(EcmFileInfoDTO::getNewFileName).collect(Collectors.toList());
        AssertUtils.isNull(collect, "替换后的文件信息名称不能为空");
        busiOperationService.addOperation(vo.getBusiId(), IcmsConstants.EDIT_FILE_SPLIT, token,
                "修改文件-旋转: " + collect);
        return Result.success(vo.getEcmFileInfoExtendsNew().get(0));
    }

    public void asyTask(RotateFileVO vo, Integer treeType) {
        //判断是否是图片类型
        for (EcmFileInfoDTO e : vo.getEcmFileInfoExtendsNew()) {
            if (IMGS.contains(e.getFormat())) {
            /*
            这里写配置检测,异步任务类型000000100九个字长,1位单证识别，2位自动转正，3模糊检测，4查重检测，5拆分合并，6位表示翻拍检测,7es,8反光,9缺角
            其中每位上 0表示无该类型，1表示处理中，2失败，3成功,对于模糊查重以及翻拍多2个状态，4表示排除异常，5表示确认异常
            如果开启了单证识别配置,则初始化taskType100000,把对应位置置为1即可
            */
                Map<Integer, Boolean> result = taskSwitchService.queryAllSwitches(e.getDocId());
                //单证识别
                SysStrategyDTO vo1 = isAutoGroup(e.getAppCode());
                List<String> enumConfigList = sysStrategyService.queryEcmEnumConfig();
                String taskType = CheckDetectionUtils.getTaskType(result, vo1, enumConfigList);
                //如果未开启任何配置则不初始化
                if (!IcmsConstants.ASYNC_TASK_STATUS_INIT.equals(taskType)) {
                    //物理删除之前的数据
                    asyncTaskMapper.deleteBatchByFileId(Collections.singletonList(e.getOldFileId()));
                    EcmAsyncTask ecmAsyncTask = new EcmAsyncTask();
                    ecmAsyncTask.setTaskType(taskType);
                    ecmAsyncTask.setBusiId(e.getBusiId());
                    ecmAsyncTask.setFileId(e.getOldFileId());
                    asyncTaskService.insert(ecmAsyncTask);
                    //查重新增文本查重
//                judgeAfmText(ecmFileInfoDTO);
                    //MQ处理智能检测
                    FileInfoRedisDTO fileInfoRedisSingle = busiCacheService
                            .getFileInfoRedisSingle(e.getBusiId(), e.getOldFileId());
                    checkDetectionService.checkDetectionByMq(fileInfoRedisSingle, ecmAsyncTask, taskType);
                }
            }
        }
    }

//    private void checkOtherUpdateForMove(MoveFileVO vo, AccountTokenExtendDTO token) {
//        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
//                vo.getBusiId());
//        List<FileInfoRedisDTO> fileInfos = busiCacheService.getFileInfoRedis(vo.getBusiId());
//        Map<Long, List<FileInfoRedisDTO>> dtos = new HashMap<>();
//        if (CollectionUtil.isNotEmpty(fileInfos)) {
//            dtos = fileInfos.stream().filter(s -> s.getState().equals(StateConstants.ZERO))
//                    .collect(Collectors.groupingBy(FileInfoRedisDTO::getFileId));
//        }
//        if (dtos.isEmpty()) {
//            return;
//        }
//        List<EcmDocrightDefDTO> docRightList = busiCacheService.getDocrightDefCommon(ecmBusiInfoRedisDTO, token);
//        Map<String, List<EcmDocrightDefDTO>> collect1 = docRightList.stream()
//                .filter(s -> StateConstants.NO.toString().equals(s.getOtherUpdate()))
//                .collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
//        if (collect1.isEmpty()) {
//            return;
//        }
//
//        List<Long> fileId = vo.getFileId();
//        List<FileInfoRedisDTO> fileInfoRedisDTOS = dtos.get(fileId.get(0));
//        if (collect1.containsKey(fileInfoRedisDTOS.get(0).getDocId())) {
//            if (!fileInfoRedisDTOS.isEmpty()
//                    && !fileInfoRedisDTOS.get(0).getCreateUser().equals(token.getUsername())) {
//                AssertUtils.isTrue(true, "操作失败,暂无修改他人影像文件权限");
//            }
//        }
//
//    }

    private void updateFileNameToRedisToFixImage(List<EcmFileInfoDTO> ecmFileInfoDTOList,
                                                 String createUserName,
                                                 AccountTokenExtendDTO token,String fileOperation) {
        if (CollectionUtils.isEmpty(ecmFileInfoDTOList)) {
            return;
        }
        FileInfoRedisDTO fileInfoRedisDTO1 = new FileInfoRedisDTO();
        Long busiId1 = ecmFileInfoDTOList.get(0).getBusiId();
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(busiId1);

        for (EcmFileInfoDTO e : ecmFileInfoDTOList) {
            String ext = CommonUtils.getExt(e);
            EcmFileHistory ecmFileHistory = saveFileHistory(e.getBusiId(), e.getOldFileId(),
                    e.getFileId(), fileOperation, token.getUsername(),
                    e.getSize(), ext);
            for (FileInfoRedisDTO fileInfoRedisDTO : fileInfoRedis) {
                if (fileInfoRedisDTO.getFileId().equals(e.getOldFileId())) {
                    busiId1 = e.getBusiId();
                    fileInfoRedisDTO.setNewFileId(e.getFileId());
                    fileInfoRedisDTO.setSize(e.getSize());
                    //批注数量清零
                    fileInfoRedisDTO.setFileCommentCount(IcmsConstants.ZERO);
                    //添加文件历史记录
                    //更新最新修改人
                    fileInfoRedisDTO.setUpdateUserName(token.getName());
                    fileInfoRedisDTO.setUpdateUser(ecmFileHistory.getCreateUser());
                    fileInfoRedisDTO.setUpdateTime(ecmFileHistory.getCreateTime());
                    //编辑文件，添加new标签标识(针对移动端功能)
                    fileInfoRedisDTO.setSignFlag(IcmsConstants.ONE);
                    //更新名称
                    fileInfoRedisDTO.setNewFileName(e.getNewFileName());
                    fileInfoRedisDTO.setFormat(e.getFormat());
                    fileInfoRedisDTO.setFileMd5(e.getFileMd5());
                    fileInfoRedisDTO.setSourceFileMd5(e.getFileMd5());
                    //兼容对外接口无系统用户情况
                    if (StrUtil.isNotBlank(createUserName)) {
                        fileInfoRedisDTO1.setUpdateUserName(createUserName);
                        fileInfoRedisDTO1.setUpdateUser(token.getUsername());
                    } else {
                        fileInfoRedisDTO1.setUpdateUserName(token.getName());
                        fileInfoRedisDTO1.setUpdateUser(token.getUsername());
                    }
                    fileInfoRedisDTO1.setUpdateTime(fileInfoRedisDTO.getUpdateTime());
                    if (CollectionUtils.isEmpty(fileInfoRedisDTO.getFileHistories())) {
                        List<EcmFileHistory> ecmFileHistories = new ArrayList<>();
                        ecmFileHistories.add(ecmFileHistory);
                        fileInfoRedisDTO.setFileHistories(ecmFileHistories);
                    } else {
                        List<EcmFileHistory> ecmFileHistories = fileInfoRedisDTO.getFileHistories();
                        ArrayList<EcmFileHistory> ecmFileHistories1 = new ArrayList<>(
                                ecmFileHistories);
                        ecmFileHistories1.add(ecmFileHistory);
                        fileInfoRedisDTO.setFileHistories(ecmFileHistories1);
                    }
                    if(IcmsConstants.REPLACE_FILE_STRING.equals(fileOperation)){
                        //替换自动设置为未到期
                        fileInfoRedisDTO.setIsExpired(StateConstants.ZERO);
                    }
                    //更新es信息
                    operateFullQueryService.editEsFileInfo(e.getOldFileId(),
                            fileInfoRedisDTO1.getUpdateUserName(),
                            fileInfoRedisDTO1.getUpdateTime(), e.getFileId(), null, null);
                    busiCacheService.updateFileInfoRedis(fileInfoRedisDTO);
                }

            }
        }

    }

    /**
     * 文件移动
     */
    @WebsocketNoticeAnnotation(busiId = "#moveFileVo.busiId")
    public Result moveFile(MoveFileVO moveFileVo, AccountTokenExtendDTO token) {
        AssertUtils.isNull(moveFileVo.getAllFileId(), "文件id集合不能为空");
        AssertUtils.isNull(moveFileVo.getFileId(), "被拖拽的文件的fileId不能为空");
        AssertUtils.isNull(moveFileVo.getBusiId(), "busiId集合不能为空");
        //文件编辑：待提交状态、处理失败状态可进行编辑
        if(!token.isOut()) {
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                    .getEcmBusiInfoRedisDTO(token, moveFileVo.getBusiId());
            Integer status = ecmBusiInfoRedisDTO.getStatus();
            if (!BusiInfoConstants.BUSI_STATUS_ZERO.equals(status) && !BusiInfoConstants.BUSI_STATUS_FOUR.equals(status)) {
                AssertUtils.isTrue(true, "原业务状态暂无编辑文件权限");
            }
        }
        //判断是否可被他人修
        EcmBusiInfoRedisDTO ecmBusiInfos = busiCacheService.getEcmBusiInfoRedisDTO(token,
                moveFileVo.getBusiId());
        List<FileInfoRedisDTO> fileInfos = busiCacheService.getFileInfoRedis(moveFileVo.getBusiId());
        List<EcmDocrightDefDTO> docrightDefCommon = busiCacheService.getDocrightDefCommon(ecmBusiInfos, token);
        //需要检验的权限列表
        List<String> rights = new ArrayList<>();
        rights.add(DocRightConstants.OTHER_UPDATE);
        commonService.checkDocRight(rights, fileInfos, moveFileVo.getFileId(),
                token.getUsername(), docrightDefCommon);
        //得到该业务下的所有文件
        List<EcmFileInfo> ecmFileInfos = new ArrayList<>();
        List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService
                .getFileInfoRedis(moveFileVo.getBusiId());
        if (CollectionUtils.isEmpty(fileInfoRedisEntities)) {
            ecmFileInfos = ecmFileInfoMapper.selectList(
                    new LambdaQueryWrapper<EcmFileInfo>().eq(EcmFileInfo::getBusiId, moveFileVo.getBusiId()));
        } else {
            ecmFileInfos = PageCopyListUtils.copyListProperties(fileInfoRedisEntities,
                    EcmFileInfo.class);
        }
        //根据fileId分组
        Map<Long, List<EcmFileInfo>> fileInfoMapByFileId = ecmFileInfos.stream()
                .collect(Collectors.groupingBy(EcmFileInfo::getFileId));
        List<Long> fileIdList = ecmFileInfos.stream().map(EcmFileInfo::getFileId)
                .collect(Collectors.toList());
        //求量集合的差集
        List<Long> difference = (List<Long>) org.apache.commons.collections4.CollectionUtils
                .subtract(moveFileVo.getFileId(), fileIdList);
        //得到被移动文件的资料节点
        if (!CollectionUtils.isEmpty(difference)) {
            return Result.error("文件信息有误", ResultCode.PARAM_ERROR);
        }
        String docId = fileInfoMapByFileId.get(moveFileVo.getFileId().get(StateConstants.ZERO))
                .get(0).getDocCode();
        //需要修改顺序号的集合
        List<EcmFileInfo> ecmFileInfoNew = new ArrayList<>();
        //上一个文件的DocId
        String upDocId = "1";
        //判断是推拽文件的下一个文件的标识
        Boolean a = false;
        //判断操作
        for (int index = 0; index < moveFileVo.getAllFileId().size(); index++) {
            List<EcmFileInfo> ecmFileInfos2 = fileInfoMapByFileId
                    .get(moveFileVo.getAllFileId().get(index));
            if (CollectionUtils.isEmpty(ecmFileInfos2)) {
                return Result.error("文件信息有误", ResultCode.PARAM_ERROR);
            }
            EcmFileInfo ecmFileInfo = ecmFileInfos2.get(StateConstants.ZERO);
            if (a && !ecmFileInfo.getDocCode().equals(docId)) {
                return Result.error("不能将该文件拖拽到其资料节点下", ResultCode.PARAM_ERROR);
            }
            a = moveFileVo.getFileId().contains(moveFileVo.getAllFileId().get(index))
                    && !upDocId.equals(docId);
            upDocId = ecmFileInfo.getDocCode();
        }

        if (a) {
            return Result.error("不能将该文件拖拽到其资料节点下", ResultCode.PARAM_ERROR);
        }
        //修改顺序号
        updateInfos(moveFileVo, token, fileInfoMapByFileId, docId, ecmFileInfoNew);
        //修改redis数据
        updateRedisInfo(moveFileVo, token, ecmFileInfoNew);
        return Result.success(true);
    }

    private void updateRedisInfo(MoveFileVO moveFileVo, AccountTokenExtendDTO token, List<EcmFileInfo> ecmFileInfoNew) {
        List<Long> moveFilesId = moveFileVo.getMoveFilesId();
        if (moveFilesId.size() > 1) {
            //多个排序
            Map<Long, List<EcmFileInfo>> collect = ecmFileInfoNew.stream()
                    .collect(Collectors.groupingBy(EcmFileInfo::getFileId));
            for (Long fileId : moveFilesId) {
                editDataToRedis(moveFileVo.getBusiId(), fileId,
                        collect.get(fileId).get(0).getFileSort(), token);
            }
        } else {
            editDataToRedis(moveFileVo.getBusiId(), moveFileVo.getFileId().get(0),
                    ecmFileInfoNew.get(0).getFileSort(), token);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateInfos(MoveFileVO moveFileVo, AccountTokenExtendDTO token, Map<Long, List<EcmFileInfo>> fileInfoMapByFileId, String docId, List<EcmFileInfo> ecmFileInfoNew) {
        //修改持久化数据
        List<Long> moveFilesId = moveFileVo.getMoveFilesId();
        try {
            if (moveFilesId.size() > 1) {
                //多个排序
                editDataListToDb(moveFileVo, token, fileInfoMapByFileId, ecmFileInfoNew);
            } else {
                //单个排序
                editDataToDb(moveFileVo, token, fileInfoMapByFileId, docId, ecmFileInfoNew);
            }
        } catch (Exception e) {
            log.error("修改数据异常",e);
            throw new SunyardException(ResultCode.SYSTEM_ERROR, "不能跨资料节点拖拽");
        }

    }

    /**
     * OCR测试
     */
    public Result autoGroup(String url) {
        if (ObjectUtils.isEmpty(url)) {
            return Result.success(true);
        }
        //使用ocr读取发票信息
        List<OcrResultDTO> ocrResults = RegenOcrUtils.ocrResultAsDtoList(url, null, ecmOcrProperties.getAppKey(), ecmOcrProperties.getOcrHost(),
                ecmOcrProperties.getAppSecret(), invoiceTypeCodeNameMap);
        return Result.success(ocrResults);
    }

    /**
     * 查看影像属性-识别（元数据管理）
     */
    public EcmFileOcrInfoEsExtendDTO readImageAttr(Long newFileId, Long busiId, String docCode, Long fileId, String appCode, AccountTokenExtendDTO token) {
        AssertUtils.isNull(newFileId, "newFileId不能为空");
        AssertUtils.isNull(busiId, "busiId不能为空");
        AssertUtils.isNull(docCode, "docCode不能为空");
        AssertUtils.isNull(appCode, "appCode不能为空");
        SysStrategyDTO sysStrategyDTO = sysStrategyService.queryConfig();
        //查询该业务下的单证是否配置OCR识别权限
        if (!sysStrategyDTO.getOcrConfigStatus()) {
            throw new SunyardException(ResultCode.NO_OPERATION_AUTH, "未开启OCR识别");
        }
        //查询文件信息
        Result<SysFileDTO> fileInfo = fileHandleApi.getFileInfo(newFileId);
        if (!fileInfo.isSucc()) {
            throw new SunyardException(fileInfo.getMsg());
        }
        SysFileDTO data = fileInfo.getData();
        //查询文件的设配信息
        StEquipmentVO stEquipmentVO = new StEquipmentVO();
        stEquipmentVO.setId(data.getEquipmentId());
        Result<EquipmentDTO> equipmentApiInfo = equipmentApi.getInfo(stEquipmentVO);
        if (!equipmentApiInfo.isSucc()) {
            throw new SunyardException(equipmentApiInfo.getMsg());
        }
        if (ObjectUtils.isEmpty(data)) {
            return null;
        }
        //使用ocr读取发票信息
        //文件大小如果大于50M 不能不进行OCR识别
        if (52428800L < data.getSize()) {
            throw new SunyardException("文件大小超过50M，不能进行OCR识别");
        }

        //得到该业务类型下的单证属性代码
        List<EcmFileAutoDTO> ecmFileAutoDTOS = new ArrayList<>();

        //手动识别不根据配置的业务类型来识别，而是识别所有的单证类型
        List<OcrResultDTO> ocrResults = null;
        List<EcmDtdAttr> ecmDtdAttrs = ecmDtdAttrMapper.selectList(new LambdaQueryWrapper<EcmDtdAttr>()
                .eq(EcmDtdAttr::getType,ecmOcrProperties.getOcrDocumentType()));
        if (!CollectionUtils.isEmpty(ecmDtdAttrs)) {
            List<EcmDtdDef> ecmDtdDefs = ecmDtdDefMapper.selectList(new LambdaQueryWrapper<EcmDtdDef>()
                    .eq(EcmDtdDef::getType,ecmOcrProperties.getOcrDocumentType()));
            Map<Long, List<EcmDtdDef>> collect1 = ecmDtdDefs.stream().collect(Collectors.groupingBy(EcmDtdDef::getDtdTypeId));
            for (EcmDtdAttr dtdAttr : ecmDtdAttrs) {
                EcmFileAutoDTO dto = new EcmFileAutoDTO();
                dto.setDtdAttrId(dtdAttr.getDtdAttrId());
                dto.setAttrCode(dtdAttr.getAttrCode());
                dto.setAttrName(dtdAttr.getAttrName());
                dto.setDtdTypeId(dtdAttr.getDtdTypeId());
                dto.setBusiId(busiId);
                dto.setRegex(dtdAttr.getRegex());
                List<EcmDtdDef> ecmDtdDefs1 = collect1.get(dtdAttr.getDtdTypeId());
                if (!CollectionUtils.isEmpty(ecmDtdDefs1)) {
                    dto.setDtdCode(ecmDtdDefs1.get(0).getDtdCode());
                    dto.setDtdName(ecmDtdDefs1.get(0).getDtdName());
                }
                ecmFileAutoDTOS.add(dto);
            }

            //如果单证属性查不出来，则没必要发起ocr接口。
            //获取文件 Base64
            if (IcmsConstants.TWO.equals(Integer.valueOf(ecmOcrProperties.getOcrDocumentType()))){
                String imageData = getFileBase64(newFileId, token, data);
                ocrResults = RegenOcrUtils.ocrResultAsDtoList(null, imageData, ecmOcrProperties.getAppKey(), ecmOcrProperties.getOcrHost(),
                        ecmOcrProperties.getAppSecret(), invoiceTypeCodeNameMap);
            }else if (IcmsConstants.ONE.equals(Integer.valueOf(ecmOcrProperties.getOcrDocumentType()))){
                //信雅达ocr识别
                //获取请求参数
                byte[] fileByte = getFileByte(newFileId, token, data);
                String params = getSunyardRequestParams(ecmDtdAttrs,ecmDtdDefs);
                ocrResults = RegenOcrUtils.ocrResultAsDtoList(ecmOcrProperties.getOcrHost(),fileByte,params,ecmOcrProperties.getOcrIgnoreClassId(),ecmOcrProperties.getOcrSplitLength());
            }

        }

        //存入es
        List<EcmFileOcrInfoEsDTO> ecmFileOcrInfoEsDTOS = addOneOcrFileInfoToEs(newFileId, busiId,
                null, ocrResults, null, ecmFileAutoDTOS, fileId,token);
        //将map集合转换成list返回
        EcmFileOcrInfoEsExtendDTO ecmFileOcrInfoEsExtendDTO = new EcmFileOcrInfoEsExtendDTO();
        if (!CollectionUtils.isEmpty(ecmFileOcrInfoEsDTOS)) {
            ecmFileOcrInfoEsExtendDTO.setIdentifyType(String.valueOf(IcmsConstants.ONE));
        } else {
            ecmFileOcrInfoEsExtendDTO.setIdentifyType(String.valueOf(IcmsConstants.TWO));
        }
        ecmFileOcrInfoEsExtendDTO.setEcmFileOcrInfoEsDTOList(ecmFileOcrInfoEsDTOS);
        return ecmFileOcrInfoEsExtendDTO;
    }

    private String getSunyardRequestParams(List<EcmDtdAttr> ecmDtdAttrs, List<EcmDtdDef> ecmDtdDefs) {
        // 使用 Map 存储结果：key=dtd_code, value=List<attr_name>
        Map<String, List<String>> result = new HashMap<>();

        // 先按 dtd_type_id 分组 attr_name
        Map<Long, List<String>> attrMap = new HashMap<>();
        for (EcmDtdAttr attr : ecmDtdAttrs) {
            Long key = attr.getDtdTypeId(); // 假设字段名为 dtdTypeId
            attrMap.computeIfAbsent(key, k -> new ArrayList<>()).add(attr.getAttrName());
        }

        // 遍历 ecmDtdDefs，构建最终结果
        for (EcmDtdDef def : ecmDtdDefs) {
            String dtdCode = def.getDtdCode(); // 例如 "0"
            Long dtdTypeId = def.getDtdTypeId(); // 用于关联属性

            List<String> attrNames = attrMap.get(dtdTypeId);
            if (attrNames != null && !attrNames.isEmpty()) {
                result.put(dtdCode, attrNames);
            }
        }

        // 转成 JSON 字符串
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new RuntimeException("JSON序列化失败", e);
        }
    }

    private String getFileBase64(Long newFileId, AccountTokenExtendDTO token, SysFileDTO data) {
        String imageData = null;
        FileByteVO fileByteVO = new FileByteVO();
        fileByteVO.setFileId(newFileId);
        fileByteVO.setEquipmentId(data.getEquipmentId());
        fileByteVO.setUsername(token.getUsername());
        fileByteVO.setName(token.getName());
        fileByteVO.setOrgCode(token.getOrgCode());
        fileByteVO.setOrgName(token.getOrgName());
        fileByteVO.setSessionId(UUID.randomUUID().toString());
        Result<byte[]> fileInputStream = fileHandleApi.getFileByteWater(fileByteVO);
        if (fileInputStream.isSucc()) {
            byte[] bytes = fileInputStream.getData();
            imageData = Base64.getEncoder().encodeToString(bytes);
        }
        return imageData;
    }

    private byte[] getFileByte(Long newFileId, AccountTokenExtendDTO token, SysFileDTO data) {
        byte[] bytes = null;
        try {
            FileByteVO fileByteVO = new FileByteVO();
            fileByteVO.setFileId(newFileId);
            fileByteVO.setEquipmentId(data.getEquipmentId());
            fileByteVO.setUsername(token.getUsername());
            fileByteVO.setName(token.getName());
            fileByteVO.setOrgCode(token.getOrgCode());
            fileByteVO.setOrgName(token.getOrgName());
            fileByteVO.setSessionId(UUID.randomUUID().toString());
            Result<byte[]> fileInputStream = fileHandleApi.getFileByteWater(fileByteVO);
            if (fileInputStream.isSucc()) {
                bytes = fileInputStream.getData();
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return bytes;
    }

    /**
     * 查看影像属性-保存（元数据管理）
     */
    public void saveImageAttr(OcrResultVO vo, AccountTokenExtendDTO token) {
        List<EcmFileOcrInfoEsDTO> newFileInfoList = vo.getEcmFileInfoEsDTO();
        AssertUtils.isNull(vo.getNewFileId(), "newFileId不能为空");
        AssertUtils.isNull(vo.getFileId(), "fileId不能为空");
        AssertUtils.isNull(newFileInfoList, "要修改的EcmFileInfoEsDTO不能为空");
        AssertUtils.isNull(vo.getBusiId(), "busiId不能为空");
        AssertUtils.isNull(vo.getNumber(), "number不能为空");

        // 1. 获取旧的属性信息（从ES查询历史数据）
        EcmFileOcrInfoEsExtendDTO oldAttrInfo = searshImageAttr(vo.getFileId());
        List<EcmFileOcrInfoEsDTO> oldFileInfoList = (oldAttrInfo != null && oldAttrInfo.getEcmFileOcrInfoEsDTOList() != null)
                ? oldAttrInfo.getEcmFileOcrInfoEsDTOList()
                : new ArrayList<>();

        // 2. 处理 DTD 类型编码和名称 set过滤重复值
        Set<String> dtdCode = new HashSet<>();
        Set<String> dtdTypeNames = new HashSet<>();
        EcmFileOcrInfoEsDTO newFileInfo = newFileInfoList.get(0);
        List<EcmDtdDef> dtdTypeNameList = newFileInfo.getDtdTypeName();
        if (!CollectionUtils.isEmpty(dtdTypeNameList)) {
            List<Long> dtdTypeIds = dtdTypeNameList.stream()
                    .map(EcmDtdDef::getDtdTypeId)
                    .collect(Collectors.toList());
            List<EcmDtdDef> ecmDtdDefs = ecmDtdDefMapper.selectBatchIds(dtdTypeIds);
            Map<Long, List<EcmDtdDef>> dtdDefMap = ecmDtdDefs.stream()
                    .collect(Collectors.groupingBy(EcmDtdDef::getDtdTypeId));

            for (EcmDtdDef dtdDef : dtdTypeNameList) {
                List<EcmDtdDef> matchedDtdDefs = dtdDefMap.get(dtdDef.getDtdTypeId());
                if (!CollectionUtils.isEmpty(matchedDtdDefs)) {
                    EcmDtdDef targetDtdDef = matchedDtdDefs.get(0);
                    dtdCode.add(targetDtdDef.getDtdCode());
                    dtdTypeNames.add(String.format("(%s)%s", targetDtdDef.getDtdCode(), targetDtdDef.getDtdName()));
                }
            }
        }

        // 3. 更新 ES 中的文件属性
        esEcmFileMapper.update(null,
                new LambdaEsUpdateWrapper<EsEcmFile>().indexName(fileIndex)
                        .set(EsEcmFile::getDtdCode, dtdCode.toString())
                        .set(EsEcmFile::getDtdTypeName, dtdTypeNames.toString())
                        .set(EsEcmFile::getOcrInfo, newFileInfoList)
                        .eq(EsEcmFile::getId, String.valueOf(vo.getFileId())));

        // 4. 生成并保存操作记录
        createFileAttrOperationRecord(String.valueOf(vo.getFileId()), token, oldFileInfoList, newFileInfoList);

        // 5. 原有业务操作日志
        busiOperationService.addOperation(vo.getBusiId(), IcmsConstants.ADD_COMMENT, token, "文件属性保存");
    }

    /**
     * 核心方法：创建文件属性操作记录（以单证类型为单位）
     */
    private void createFileAttrOperationRecord(String fileId, AccountTokenExtendDTO token,
                                               List<EcmFileOcrInfoEsDTO> oldFileInfoList,
                                               List<EcmFileOcrInfoEsDTO> newFileInfoList) {
        // 步骤1：将新旧数据解析为「dtdTypeId -> 单证类型详情」的Map
        Map<Long, EcmOcrIndentifyDTO> oldDtdMap = parseDtdToMap(oldFileInfoList);
        Map<Long, EcmOcrIndentifyDTO> newDtdMap = parseDtdToMap(newFileInfoList);

        // 步骤2：初始化操作详情容器
        Map<String, Object> changeDetails = new HashMap<>(); // 所有变更
        Map<String, Object> addDetails = new HashMap<>();    // 新增的单证类型
        Map<String, Object> deleteDetails = new HashMap<>(); // 删除的单证类型

        // 步骤3：获取所有单证类型的名称映射（用于记录显示）
        Map<Long, String> dtdTypeNameMap = getDtdTypeNameMap(oldDtdMap.keySet(), newDtdMap.keySet());

        // 步骤4：处理新增和修改的单证类型
        for (Map.Entry<Long, EcmOcrIndentifyDTO> newEntry : newDtdMap.entrySet()) {
            Long dtdTypeId = newEntry.getKey();
            EcmOcrIndentifyDTO newDtd = newEntry.getValue();
            EcmOcrIndentifyDTO oldDtd = oldDtdMap.get(dtdTypeId);

            String dtdName = dtdTypeNameMap.getOrDefault(dtdTypeId, "未知单证类型");

            if (oldDtd == null) {
                // 场景1：新增单证类型
                Map<String, Object> detail = buildAddDtdDetail(dtdTypeId, dtdName, newDtd);
                addDetails.put(dtdTypeId.toString(), detail);
//                changeDetails.put(dtdTypeId.toString(), detail);
            } else {
                // 场景2：修改单证类型，需要检查内部属性变化
                Map<String, Object> attrChanges = compareDtdAttributes(oldDtd, newDtd);
                if (!attrChanges.isEmpty()) {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("dtd_name", dtdName);
                    detail.put("op_type", 2); // 2=修改
                    detail.put("attr_changes", attrChanges);

                    changeDetails.put(dtdTypeId.toString(), detail);
                }
            }
        }

        // 步骤5：处理删除的单证类型
        for (Map.Entry<Long, EcmOcrIndentifyDTO> oldEntry : oldDtdMap.entrySet()) {
            Long dtdTypeId = oldEntry.getKey();
            if (!newDtdMap.containsKey(dtdTypeId)) {
                EcmOcrIndentifyDTO oldDtd = oldEntry.getValue();
                String dtdName = dtdTypeNameMap.getOrDefault(dtdTypeId, "未知单证类型");

                // 构建删除详情
                Map<String, Object> detail = new HashMap<>();
                detail.put("dtd_name", dtdName);
                detail.put("op_type", 3); // 3=删除
                detail.put("deleted_attributes", convertAttrsToMap(oldDtd.getAttr()));

                deleteDetails.put(dtdTypeId.toString(), detail);
            }
        }

        // 步骤6：无变更则不生成记录
        if (changeDetails.isEmpty() && addDetails.isEmpty() && deleteDetails.isEmpty()) {
            return;
        }

        // 步骤7：构建数据库实体并保存
        EcmFileAttrOperation operationRecord = new EcmFileAttrOperation();
        operationRecord.setFileId(fileId);
        operationRecord.setOperatorId(token.getUsername());
        operationRecord.setOperatorName(token.getName());
        operationRecord.setCreateTime(new Date());
        operationRecord.setRemark("文件属性保存");

        // 转换为JSON字符串
        operationRecord.setChangeDetails(changeDetails.isEmpty() ? null : JSONObject.toJSONString(changeDetails));
        operationRecord.setAddDetails(addDetails.isEmpty() ? null : JSONObject.toJSONString(addDetails));
        operationRecord.setDeleteDetails(deleteDetails.isEmpty() ? null : JSONObject.toJSONString(deleteDetails));

        // 保存到数据库
        ecmFileAttrOperationMapper.insert(operationRecord);
    }

    /**
     * 解析数据为「dtdTypeId -> 单证类型详情」的Map
     */
    private Map<Long, EcmOcrIndentifyDTO> parseDtdToMap(List<EcmFileOcrInfoEsDTO> fileInfoList) {
        Map<Long, EcmOcrIndentifyDTO> dtdMap = new HashMap<>();
        if (CollectionUtils.isEmpty(fileInfoList)) {
            return dtdMap;
        }

        for (EcmFileOcrInfoEsDTO fileInfo : fileInfoList) {
            List<EcmOcrIndentifyDTO> ocrIdentifyList = fileInfo.getOcrIdentifyInfo();
            if (CollectionUtils.isEmpty(ocrIdentifyList)) {
                continue;
            }

            for (EcmOcrIndentifyDTO identifyDTO : ocrIdentifyList) {
                Long dtdTypeId = identifyDTO.getDtdTypeId();
                if (dtdTypeId != null) {
                    dtdMap.put(dtdTypeId, identifyDTO);
                }
            }
        }
        return dtdMap;
    }

    /**
     * 获取单证类型ID与名称的映射关系
     */
    private Map<Long, String> getDtdTypeNameMap(Set<Long> oldDtdTypeIds, Set<Long> newDtdTypeIds) {
        Set<Long> allDtdTypeIds = new HashSet<>();
        allDtdTypeIds.addAll(oldDtdTypeIds);
        allDtdTypeIds.addAll(newDtdTypeIds);

        if (allDtdTypeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<EcmDtdDef> dtdDefs = ecmDtdDefMapper.selectBatchIds(allDtdTypeIds);
        Map<Long, String> nameMap = new HashMap<>();

        for (EcmDtdDef dtdDef : dtdDefs) {
            nameMap.put(dtdDef.getDtdTypeId(), dtdDef.getDtdName());
        }

        return nameMap;
    }

    /**
     * 构建新增单证类型的详情
     */
    private Map<String, Object> buildAddDtdDetail(Long dtdTypeId, String dtdName, EcmOcrIndentifyDTO newDtd) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("dtd_name", dtdName);
        detail.put("op_type", 1); // 1=新增
        detail.put("added_attributes", convertAttrsToMap(newDtd.getAttr()));
        return detail;
    }

    /**
     * 比较两个单证类型的属性变化
     */
    private Map<String, Object> compareDtdAttributes(EcmOcrIndentifyDTO oldDtd, EcmOcrIndentifyDTO newDtd) {
        Map<Long, EcmFileOcrDetailEsDTO> oldAttrMap = convertAttrListToMap(oldDtd.getAttr());
        Map<Long, EcmFileOcrDetailEsDTO> newAttrMap = convertAttrListToMap(newDtd.getAttr());

        Map<String, Object> changes = new HashMap<>();

        // 检查修改和新增的属性
        for (Map.Entry<Long, EcmFileOcrDetailEsDTO> newEntry : newAttrMap.entrySet()) {
            Long attrId = newEntry.getKey();
            EcmFileOcrDetailEsDTO newAttr = newEntry.getValue();
            EcmFileOcrDetailEsDTO oldAttr = oldAttrMap.get(attrId);

            if (oldAttr == null) {
                // 新增属性
                Map<String, Object> change = new HashMap<>();
                change.put("attr_name", newAttr.getLabel());
                change.put("old_value", null);
                change.put("new_value", newAttr.getValue());
                change.put("op_type", 1); // 1=新增

                changes.put(attrId.toString(), change);
            } else if (!Objects.equals(oldAttr.getValue(), newAttr.getValue())) {
                // 修改属性
                Map<String, Object> change = new HashMap<>();
                change.put("attr_name", newAttr.getLabel());
                change.put("old_value", oldAttr.getValue());
                change.put("new_value", newAttr.getValue());
                change.put("op_type", 2); // 2=修改

                changes.put(attrId.toString(), change);
            }
        }

        // 检查删除的属性
        for (Map.Entry<Long, EcmFileOcrDetailEsDTO> oldEntry : oldAttrMap.entrySet()) {
            Long attrId = oldEntry.getKey();
            if (!newAttrMap.containsKey(attrId)) {
                EcmFileOcrDetailEsDTO oldAttr = oldEntry.getValue();

                Map<String, Object> change = new HashMap<>();
                change.put("attr_name", oldAttr.getLabel());
                change.put("old_value", oldAttr.getValue());
                change.put("new_value", null);
                change.put("op_type", 3); // 3=删除

                changes.put(attrId.toString(), change);
            }
        }

        return changes;
    }

    /**
     * 将属性列表转换为Map（attrId -> 属性值）
     */
    private Map<Long, EcmFileOcrDetailEsDTO> convertAttrListToMap(List<EcmFileOcrDetailEsDTO> attrList) {
        Map<Long, EcmFileOcrDetailEsDTO> attrMap = new HashMap<>();
        if (CollectionUtils.isEmpty(attrList)) {
            return attrMap;
        }

        for (EcmFileOcrDetailEsDTO attr : attrList) {
            if (attr.getId() != null) {
                attrMap.put(attr.getId(), attr);
            }
        }
        return attrMap;
    }

    /**
     * 将属性列表转换为简单的键值对Map（用于记录）
     */
    private Map<String, Object> convertAttrsToMap(List<EcmFileOcrDetailEsDTO> attrList) {
        Map<String, Object> attrsMap = new HashMap<>();
        if (CollectionUtils.isEmpty(attrList)) {
            return attrsMap;
        }

        for (EcmFileOcrDetailEsDTO attr : attrList) {
            attrsMap.put(attr.getLabel(), attr.getValue());
        }
        return attrsMap;
    }

    /**
     * 查看影像属性-查看（元数据管理）
     */
    public EcmFileOcrInfoEsExtendDTO searshImageAttr(Long fileId) {
        AssertUtils.isNull(fileId, "fileId不能为空");
        List ecmFileInfo = searchIsRead(fileId);
        List<EcmFileOcrInfoEsDTO> list = new ArrayList<>();

        //判断是否识别过
        final Boolean[] isOcrIdentify = { false };
        if (!CollectionUtils.isEmpty(ecmFileInfo)) {
            //将map转为list
            Object p = ecmFileInfo.get(0);
            Map<String, Object> map = BeanUtil.beanToMap(p);
            //所有关联的文档类型
            if (!ObjectUtils.isEmpty(map.get("isOcrIdentify"))) {
                //判断是否识别过
                isOcrIdentify[0] = true;
            }
            List<EcmFileOcrInfoEsDTO> info = (List<EcmFileOcrInfoEsDTO>) map.get("ocrInfo");
            if (info != null) {
                List<EcmDtdAttr> ecmDtdAttrs = ecmDtdAttrMapper.selectList(null);
                if (!CollectionUtils.isEmpty(ecmDtdAttrs)) {
                    Map<Long, List<EcmDtdAttr>> collect = ecmDtdAttrs.stream()
                            .collect(Collectors.groupingBy(EcmDtdAttr::getDtdTypeId));
                    List<EcmDtdDef> ecmDtdDefs = ecmDtdDefMapper.selectList(null);
                    Map<Long, List<EcmDtdDef>> collect1 = ecmDtdDefs.stream()
                            .collect(Collectors.groupingBy(EcmDtdDef::getDtdTypeId));

                    for (EcmFileOcrInfoEsDTO ecmFileOcrInfoEsDTO : info) {
                        List<EcmOcrIndentifyDTO> ocrIdentifyInfo = ecmFileOcrInfoEsDTO
                                .getOcrIdentifyInfo();
                        if (CollectionUtils.isEmpty(ocrIdentifyInfo)) {
                            return null;
                        }
                        ArrayList<EcmOcrIndentifyDTO> ecmOcrIndentifyDTOS = new ArrayList<>();
                        ArrayList<EcmDtdDef> ecmDtdDefArrayList = new ArrayList<>();

                        for (EcmOcrIndentifyDTO dto : ocrIdentifyInfo) {
                            List<EcmFileOcrDetailEsDTO> objects = new ArrayList<>();
                            List<EcmDtdDef> ecmDtdDefs1 = collect1.get(dto.getDtdTypeId());
                            AssertUtils.isNull(ecmDtdDefs1, "单证类型不能为空");
                            List<EcmDtdAttr> ecmDtdAttrs1 = collect.get(dto.getDtdTypeId());
                            for (EcmDtdAttr e : ecmDtdAttrs1) {
                                EcmFileOcrDetailEsDTO stringStringHashMap = new EcmFileOcrDetailEsDTO();
                                stringStringHashMap.setLabel(e.getAttrName());
                                if (!CollectionUtils.isEmpty(dto.getAttr())) {
                                    for (EcmFileOcrDetailEsDTO map1 : dto.getAttr()) {
                                        if (map1.getId() != null
                                                && map1.getId().equals(e.getDtdAttrId())) {
                                            stringStringHashMap.setValue(map1.getValue());
                                            break;
                                        }
                                    }
                                } else {
                                    stringStringHashMap.setValue(null);
                                }
                                stringStringHashMap.setId(e.getDtdAttrId());
                                stringStringHashMap.setDtdTypeId(e.getDtdTypeId());
                                stringStringHashMap.setRegex(e.getRegex());
                                objects.add(stringStringHashMap);
                            }
                            EcmOcrIndentifyDTO dto1 = new EcmOcrIndentifyDTO();
                            dto1.setDtdTypeId(dto.getDtdTypeId());
                            dto1.setAttr(objects);
                            ecmDtdDefArrayList.add(ecmDtdDefs1.get(0));
                            ecmOcrIndentifyDTOS.add(dto1);
                        }
                        ecmFileOcrInfoEsDTO.setOcrIdentifyInfo(ecmOcrIndentifyDTOS);
                        ecmFileOcrInfoEsDTO.setDtdTypeName(ecmDtdDefArrayList);
                        list.add(ecmFileOcrInfoEsDTO);
                    }

                }
            }
        }

        EcmFileOcrInfoEsExtendDTO ecmFileOcrInfoEsExtendDTO = new EcmFileOcrInfoEsExtendDTO();
        if (isOcrIdentify[0] && !CollectionUtils.isEmpty(list)) {
            ecmFileOcrInfoEsExtendDTO.setIdentifyType(String.valueOf(IcmsConstants.ONE));
        } else if (isOcrIdentify[0] && CollectionUtils.isEmpty(list)) {
            ecmFileOcrInfoEsExtendDTO.setIdentifyType(String.valueOf(IcmsConstants.TWO));
        } else {
            ecmFileOcrInfoEsExtendDTO.setIdentifyType(String.valueOf(IcmsConstants.ZERO));
        }
        ecmFileOcrInfoEsExtendDTO.setEcmFileOcrInfoEsDTOList(list);
        return ecmFileOcrInfoEsExtendDTO;
    }

    private List searchIsRead(Long fileId) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //查询文件是否被识别过
        sourceBuilder.query(QueryBuilders.matchQuery("fileId", fileId));
        List<EsEcmFile> esEcmFiles = esEcmFileMapper
                .selectList(new LambdaEsQueryWrapper<EsEcmFile>().indexName(fileIndex)
                        .match(EsEcmFile::getFileId, fileId));
        return esEcmFiles;
    }

    /**
     * object转Map
     */
    private Map<String, Object> convertObjectToMap(Object obj) {
        Map<String, Object> map = new HashMap<>();

        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                String name = field.getName();
                Object value = field.get(obj);
                map.put(name, value);
            } catch (IllegalAccessException e) {
                // Handle exception
            }
        }
        return map;
    }

    private List<SysFileDTO> autoGroupNew(EcmFileInfoDTO ecmFileInfoDTO,
                                          AccountTokenExtendDTO token, SysStrategyDTO vo,
                                          List<SysFileDTO> sysFileDTOS,
                                          List<OcrResultDTO> ocrResults) {
        if (token.isOut()) {
            ecmFileInfoDTO.setOrgCode(token.getOrgCode());
        }
        /*//得到文件的文档属性-用于自动归类
        List<EcmFileAutoDTO> ecmFileAutoDTOS = ecmBusiDocMapper
                .selectDtdCode(ecmFileInfoDTO.getBusiId());

        //查询业务是静态树还是动态树
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectById(ecmFileInfoDTO.getBusiId());
        if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfo.getTreeType())) {
            ecmFileAutoDTOS = ecmBusiDocMapper.staticTreeselectDtdCode(ecmFileInfoDTO.getAppCode());
        } else {
            ecmFileAutoDTOS = ecmBusiDocMapper.selectDtdCode(ecmFileInfoDTO.getBusiId());
        }*/
        List<EcmFileAutoDTO> ecmFileAutoDTOS = new ArrayList<>();
        List<EcmDtdAttr> ecmDtdAttrs = ecmDtdAttrMapper.selectList(new LambdaQueryWrapper<EcmDtdAttr>()
                .eq(EcmDtdAttr::getType, ecmOcrProperties.getOcrDocumentType()));
        if (!CollectionUtils.isEmpty(ecmDtdAttrs)) {
            List<EcmDtdDef> ecmDtdDefs = ecmDtdDefMapper.selectList(new LambdaQueryWrapper<EcmDtdDef>()
                    .eq(EcmDtdDef::getType, ecmOcrProperties.getOcrDocumentType()));
            Map<Long, List<EcmDtdDef>> collect1 = ecmDtdDefs.stream().collect(Collectors.groupingBy(EcmDtdDef::getDtdTypeId));
            for (EcmDtdAttr dtdAttr : ecmDtdAttrs) {
                EcmFileAutoDTO dto = new EcmFileAutoDTO();
                dto.setDtdAttrId(dtdAttr.getDtdAttrId());
                dto.setAttrCode(dtdAttr.getAttrCode());
                dto.setAttrName(dtdAttr.getAttrName());
                dto.setDtdTypeId(dtdAttr.getDtdTypeId());
                dto.setBusiId(ecmFileInfoDTO.getBusiId());
                dto.setRegex(dtdAttr.getRegex());
                List<EcmDtdDef> ecmDtdDefs1 = collect1.get(dtdAttr.getDtdTypeId());
                if (!CollectionUtils.isEmpty(ecmDtdDefs1)) {
                    dto.setDtdCode(ecmDtdDefs1.get(0).getDtdCode());
                    dto.setDtdName(ecmDtdDefs1.get(0).getDtdName());
                }
                ecmFileAutoDTOS.add(dto);
            }
        }
        if (!CollectionUtils.isEmpty(ocrResults)) {
            Integer i = StateConstants.ZERO;
            //文件信息集合
            List<FileInfoRedisDTO> fileInfoRedisDTOList = new ArrayList<>();
            //判断是否有选择一个资料节点使用
            String docCode = ecmFileInfoDTO.getDocCode();
            for (OcrResultDTO o : ocrResults) {
                if (!CollectionUtils.isEmpty(sysFileDTOS) && sysFileDTOS.size() >= StateConstants.COMMON_TWO) {
                    //进行了混贴拆分
                    //添加原图
                    if (IcmsConstants.ZERO.equals(i)) {
                        //向es中添加OCR识别的信息
                        addOneOcrFileInfoToEs(ecmFileInfoDTO.getNewFileId(),
                                ecmFileInfoDTO.getBusiId(), null, ocrResults,
                                ecmFileInfoDTO.getFileExif(), ecmFileAutoDTOS,
                                ecmFileInfoDTO.getFileId(),token);
                        //添加文件信息（持久化、es）
                        //添加持久化数据库信息
                        saveDBAndESFileInfo(ecmFileInfoDTO, token, fileInfoRedisDTOList);
                    }
                    long fileId = snowflakeUtil.nextId();
                    ecmFileInfoDTO.setFileId(fileId);
                    ecmFileInfoDTO.setComment(
                            IcmsConstants.UNCLASSIFIED_ID.equals(ecmFileInfoDTO.getDocCode())
                                    ? ecmFileInfoDTO.getDocName()
                                    : null);
                    //向es中添加OCR识别的信息
                    addOneOcrFileInfoToEs(sysFileDTOS.get(i).getId(), ecmFileInfoDTO.getBusiId(), null, Collections.singletonList(o), ecmFileInfoDTO.getFileExif(), ecmFileAutoDTOS, fileId,token);
                    //添加文件信息（持久化、es）
                    saveDBAndESFileInfo(ecmFileInfoDTO, token, fileInfoRedisDTOList);
                }
                ++i;
            }
            //将发票信息存入es
            if (CollectionUtils.isEmpty(sysFileDTOS)) {
                //没有进行混贴拆分
                addOneOcrFileInfoToEs(ecmFileInfoDTO.getNewFileId(), ecmFileInfoDTO.getBusiId(),
                        null, ocrResults, ecmFileInfoDTO.getFileExif(), ecmFileAutoDTOS,
                        ecmFileInfoDTO.getFileId(),token);
            } else {
                //添加redis信息
                //                saveRedisInfo(fileInfoRedisDTOList);
            }
        }
        return sysFileDTOS;
    }

    /**
     * 向redis中插入多条数据
     */
    private void saveRedisInfo(List<FileInfoRedisDTO> fileInfoRedisDTOList) {

        if (CollectionUtil.isNotEmpty(fileInfoRedisDTOList)) {
            busiCacheService.updateFileInfoRedis(fileInfoRedisDTOList);
        }
    }

    /**
     * 用于循环插入文件信息时使用
     * 在循环插入文件信息时并且存在事物注解时，循环插入redis会出问题，将插入es信息提取出来
     * flag : 是否需要存储ES
     */
    private FileInfoRedisDTO saveDBAndESFileInfo(EcmFileInfoDTO ecmFileInfoDTO, AccountTokenExtendDTO token,
                                     List<FileInfoRedisDTO> fileInfoRedisDTOList) {
        //添加持久化数据库信息
        EcmFileHistory ecmFileHistory = saveDBInfo(ecmFileInfoDTO, token);
        //处理要新增的redis信息
        FileInfoRedisDTO fileInfoRedisDTO = handleRedisInfo(ecmFileInfoDTO, token, ecmFileHistory);
        fileInfoRedisDTOList.add(fileInfoRedisDTO);
        //添加es数
//        try {
//            operateFullQueryService.addEsFileInfo(fileInfoRedisDTO, token.getId());
//            log.info("修改es成功");
//        } catch (Exception e) {
//            log.error("添加es失败", e);
//        }
        return fileInfoRedisDTO;
    }

    /**
     * 处理要存入redis的文件信息
     */
    private FileInfoRedisDTO handleRedisInfo(EcmFileInfoDTO ecmFileInfoDTO,
                                             AccountTokenExtendDTO token,
                                             EcmFileHistory ecmFileHistory) {
        //添加新增的文件信息
        FileInfoRedisDTO fileInfoRedisDTO = new FileInfoRedisDTO();
        BeanUtils.copyProperties(ecmFileInfoDTO, fileInfoRedisDTO);
        //添加创建人、最近修改人、机构号
        if (token.isOut()) {
            fileInfoRedisDTO.setUpdateUser(fileInfoRedisDTO.getUpdateUser());
            fileInfoRedisDTO.setCreateUser(fileInfoRedisDTO.getCreateUser());
            fileInfoRedisDTO.setCreateUserName(fileInfoRedisDTO.getCreateUserName());
            fileInfoRedisDTO.setUpdateUserName(fileInfoRedisDTO.getUpdateUserName());
        }
        //添加资料类型名称
        addDocTypeName(fileInfoRedisDTO);
        //添加文件历史
        fileInfoRedisDTO.setFileHistories(Collections.singletonList(ecmFileHistory));
        //添加文件大小
        fileInfoRedisDTO.setNewFileSize(ecmFileInfoDTO.getSize());
        //添加期限
        fileInfoRedisDTO.setIsExpired(StateConstants.ZERO);
        return fileInfoRedisDTO;
    }

    @Transactional(rollbackFor = Exception.class)
    public EcmFileHistory saveDBInfo(EcmFileInfoDTO ecmFileInfoDTO, AccountTokenExtendDTO token) {
        EcmFileInfo ecmFileInfo = insertEcmFileInfo(ecmFileInfoDTO, token);
        ecmFileInfoDTO.setFileId(ecmFileInfo.getFileId());
        //新导入的文件有new标签标识（针对移动端）
        ecmFileInfoDTO.setSignFlag(IcmsConstants.ONE);
        //添加业务操作记录
        String remark = "添加文件:" + ecmFileInfoDTO.getNewFileName();
        busiOperationService.addOperation(ecmFileInfoDTO.getBusiId(), IcmsConstants.ADD_FILE,
                token, remark);
        //获取文件拓展名
        String extension = CommonUtils.getExt(ecmFileInfoDTO);
        //新增一条文件历史记录
        EcmFileHistory ecmFileHistory = commonService.insertFileHistory(ecmFileInfoDTO.getBusiId(),
                ecmFileInfoDTO.getFileId(), ecmFileInfoDTO.getNewFileId(),
                IcmsConstants.ADD_FILE_OPERATION_STRING, ecmFileInfoDTO.getCreateUser(),
                ecmFileInfoDTO.getSize(), extension);
        return ecmFileHistory;
    }

    /**
     * 插入文件信息表
     */
    private EcmFileInfo insertEcmFileInfo(EcmFileInfoDTO ecmFileInfoDTO,
                                          AccountTokenExtendDTO token) {
        EcmFileInfo ecmFileInfo = new EcmFileInfo();
        BeanUtils.copyProperties(ecmFileInfoDTO, ecmFileInfo);
        //添加机构号
        if (StrUtil.isBlank(ecmFileInfoDTO.getOrgCode())) {
            ecmFileInfoDTO.setOrgCode(addInstNo(token));
            ecmFileInfoDTO.setOrgName(token.getOrgName());
        }
        ecmFileInfo.setNewFileSize(ecmFileInfoDTO.getSize());
        ecmFileInfo.setCreateUser(token.getUsername());
        ecmFileInfo.setCreateUserName(token.getName());
        ecmFileInfo.setNewFileExt(CommonUtils.getExt(ecmFileInfoDTO));
        //todo
        ecmFileInfoMapper.insert(ecmFileInfo);
        return ecmFileInfo;
    }

    private List<OcrResultDTO> getOcrResultDTOS(EcmFileInfoDTO ecmFileInfoDTO,
                                                AccountTokenExtendDTO token) {
        //查询文件信息
        Result<SysFileDTO> fileInfo = fileHandleApi.getFileInfo(ecmFileInfoDTO.getNewFileId());
        if (!fileInfo.isSucc()) {
            throw new SunyardException(fileInfo.getMsg());
        }
        SysFileDTO data = fileInfo.getData();
        //查询文件的设配信息
        StEquipmentVO stEquipmentVO = new StEquipmentVO();
        stEquipmentVO.setId(data.getEquipmentId());
        Result<EquipmentDTO> equipmentApiInfo = equipmentApi.getInfo(stEquipmentVO);
        if (!equipmentApiInfo.isSucc()) {
            throw new SunyardException(equipmentApiInfo.getMsg());
        }
        if (ObjectUtils.isEmpty(data)) {
            return null;
        }
        //使用ocr读取发票信息
        List<OcrResultDTO> ocrResults = null;
        try {
            //使用ocr读取发票信息(type=10900时是无法识别的发票)
            //获取文件 Base64
            //文件大小如果大于50M 不能不进行OCR识别
            if (52428800L < data.getSize()) {
                return null;
            }
            if (IcmsConstants.TWO.equals(Integer.valueOf(ecmOcrProperties.getOcrDocumentType()))){
                String imageData = getFileBase64(ecmFileInfoDTO.getNewFileId(), token, data);
                ocrResults = RegenOcrUtils.ocrResultAsDtoList(null, imageData, ecmOcrProperties.getAppKey(), ecmOcrProperties.getOcrHost(),
                        ecmOcrProperties.getAppSecret(), invoiceTypeCodeNameMap);
            }else if (IcmsConstants.ONE.equals(Integer.valueOf(ecmOcrProperties.getOcrDocumentType()))){
                //信雅达ocr识别
                //获取请求参数
                byte[] fileByte = getFileByte(ecmFileInfoDTO.getNewFileId(), token, data);
                List<EcmDtdAttr> ecmDtdAttrs = ecmDtdAttrMapper.selectList(new LambdaQueryWrapper<EcmDtdAttr>()
                        .eq(EcmDtdAttr::getType, ecmOcrProperties.getOcrDocumentType()));
                List<EcmDtdDef> ecmDtdDefs = ecmDtdDefMapper.selectList(new LambdaQueryWrapper<EcmDtdDef>()
                        .eq(EcmDtdDef::getType, ecmOcrProperties.getOcrDocumentType()));
                String params = getSunyardRequestParams(ecmDtdAttrs, ecmDtdDefs);
                ocrResults = RegenOcrUtils.ocrResultAsDtoList(ecmOcrProperties.getOcrHost(), fileByte, params,ecmOcrProperties.getOcrIgnoreClassId(),ecmOcrProperties.getOcrSplitLength());
            }
        } catch (Exception e) {
            log.error("OCR识别失败,文件没有自动归类",e);
        }
        return ocrResults;
    }

    private List<SysFileDTO> mixedPastingSplit(EcmFileInfoDTO ecmFileInfoDTO,
                                               AccountTokenExtendDTO token,
                                               List<OcrResultDTO> ocrResults) {
        List<List<Integer>> regionList = new ArrayList<>();
        ocrResults.forEach(p -> {
            List<Integer> region = p.getRegion();
            //宽
            region.set(2, region.get(2) - region.get(0));
            //高
            region.set(3, region.get(3) - region.get(1));
            regionList.add(region);
        });
        MixedPastingSplitDTO mixedPastingSplitDTO = new MixedPastingSplitDTO();
        mixedPastingSplitDTO.setId(ecmFileInfoDTO.getNewFileId());
        mixedPastingSplitDTO.setRegionList(regionList);
        mixedPastingSplitDTO.setUserId(token.getId());
        //混贴拆分
        return fileHandleApi.mixedPastingSplit(mixedPastingSplitDTO).getData();
    }

    private void searchDocMd5Con(EcmFileInfoDTO ecmFileInfoDTO,
                                 List<EcmFileAutoDTO> ecmFileAutoDTOS1,
                                 AccountTokenExtendDTO token) {
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                ecmFileInfoDTO.getBusiId());
        if (ecmBusiInfoRedisDTO != null) {
            ecmFileInfoDTO.setDocId(null);
            for (EcmFileAutoDTO p : ecmFileAutoDTOS1) {
                List<FileInfoRedisDTO> fileInfoRedisDTOS = searchDocMd5(ecmFileInfoDTO,
                        p.getDocCode(), ecmBusiInfoRedisDTO);
                if (CollectionUtils.isEmpty(fileInfoRedisDTOS)) {
                    ecmFileInfoDTO.setDocId(p.getDocCode());
                    break;
                }
            }
            AssertUtils.isNull(ecmFileInfoDTO.getDocId(), ecmFileInfoDTO.getNewFileName() + "已存在");
        }
    }

    private List<FileInfoRedisDTO> searchDocMd5(EcmFileInfoDTO ecmFileInfoDTO, String docId,
                                                EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        if (!ObjectUtils.isEmpty(ecmBusiInfoRedisDTO)) {
            List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService
                    .getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
            if (!CollectionUtils.isEmpty(fileInfoRedisEntities)) {
                //查看该资料节点下是否有与该自动归类的文件的Md5相同的文件
                List<FileInfoRedisDTO> collect = fileInfoRedisEntities.stream()
                        .filter(p -> p.getDocCode().equals(docId)
                                && p.getFileMd5().equals(ecmFileInfoDTO.getSourceFileMd5())
                                && 0 == p.getState())
                        .collect(Collectors.toList());
                return collect;
            }
        }
        return new ArrayList<>();
    }

    private List<EcmFileOcrInfoEsDTO> addOneOcrFileInfoToEs(Long newFileId, Long busiId,
                                                            String docCode,
                                                            List<OcrResultDTO> ocrResults,
                                                            HashMap fileExif,
                                                            List<EcmFileAutoDTO> ecmFileAutoDTOS,
                                                            Long fileId,AccountTokenExtendDTO token) {
        log.info("OCR识别数据：{}", ocrResults);
        log.info(busiId + "业务下关联的单证属性：{}", ecmFileAutoDTOS);

        String id = String.valueOf(fileId);
        log.info("es中_id：{}", id);
        //判断文件信息是否已经存在es中
        boolean existsDocument = ecmEsUtils.isExistsDocument(fileIndex, "_doc", id);
        //判断文件的OCR识别出的信息是否存在
        List<EcmFileOcrInfoEsDTO> list = new ArrayList<>();
        Set<String> dtdCode = new HashSet<>();
        Set<String> dtdTypeNames = new HashSet<>();
        //处理OCR识别出的属性
        if (!CollectionUtils.isEmpty(ocrResults)) {
            List<EcmOcrIndentifyDTO> objectObjectHashMap = new ArrayList<>();
            List<EcmDtdDef> stringHashMap = new ArrayList<>();
            ocrResults.forEach(pp -> {
                Map<String, Object> jsonObject = new LinkedHashMap<>();
                Map<String, Object> details = pp.getDetails();
                //根据OCR识别返回的资料代码找到该资料下的绑定的单证属性
                Set<EcmFileAutoDTO> collect = ecmFileAutoDTOS.stream()
                        .filter(p -> p.getDtdCode().equals(pp.getType()))
                        .collect(Collectors.toSet());
                if (!ObjectUtils.isEmpty(docCode)) {
                    collect = collect.stream().filter(p -> p.getDocCode().equals(docCode))
                            .collect(Collectors.toSet());
                }

                log.info("业务下关联的单证属性与COR识别出的数据匹配的数据：{}", collect);
                if (!CollectionUtils.isEmpty(collect)) {
                    Map<Long, List<EcmFileAutoDTO>> collect1 = collect.stream()
                            .collect(Collectors.groupingBy(EcmFileAutoDTO::getDtdTypeId));
                    List<EcmDtdDef> ecmDtdDefs = ecmDtdDefMapper.selectBatchIds(collect1.keySet());
                    Map<Long, List<EcmDtdDef>> collect2 = ecmDtdDefs.stream()
                            .collect(Collectors.groupingBy(EcmDtdDef::getDtdTypeId));

                    //由于一个业务下可能会关联很多资料类型，不同的资料类型可以关联相同的单证类型，这就导致了collect重复，所以要随机选择一个资料类型来达到去重collect的效果
                    //根据OCR识别返回的资料代码找到该资料下的绑定的单证属性
                    int i = 1;
                    for (Long typeid : collect1.keySet()) {
                        List<EcmFileAutoDTO> ecmFileAutoDTOS1 = collect1.get(typeid);
                        List<EcmDtdDef> ecmDtdDefs1 = collect2.get(typeid);
                        if (!CollectionUtils.isEmpty(ecmDtdDefs1)) {
                            stringHashMap.add(ecmDtdDefs1.get(0));
                            EcmOcrIndentifyDTO dto = new EcmOcrIndentifyDTO();
                            dto.setDtdTypeId(typeid);
                            List<EcmFileOcrDetailEsDTO> jsonListMap = new ArrayList<>();
                            for (EcmFileAutoDTO e : ecmFileAutoDTOS1) {
                                EcmFileOcrDetailEsDTO object = new EcmFileOcrDetailEsDTO();
                                object.setLabel(e.getAttrName());
                                //根据配置的ocrtype进行区分 瑞真返回值根据code去匹配，信雅达OCR返回值根据name去匹配value
                                if (IcmsConstants.TWO.equals(Integer.valueOf(ecmOcrProperties.getOcrDocumentType()))){
                                    object.setValue((String) details.get(e.getAttrCode()));
                                }else if (IcmsConstants.ONE.equals(Integer.valueOf(ecmOcrProperties.getOcrDocumentType()))){
                                    object.setValue((String) details.get(e.getAttrName()));
                                }
                                object.setId(e.getDtdAttrId());
                                object.setDtdTypeId(e.getDtdTypeId());
                                object.setRegex(e.getRegex());
                                jsonListMap.add(object);
                                jsonObject.put(e.getAttrName(), details.get(e.getAttrCode()));
                            }
                            dto.setAttr(jsonListMap);
                            dtdCode.add(ecmDtdDefs1.get(0).getDtdCode());
                            dtdTypeNames.add(String.format("(%s)%s", ecmDtdDefs1.get(0).getDtdCode(), ecmDtdDefs1.get(0).getDtdName()));
                            objectObjectHashMap.add(dto);
                        }
                    }

                }
            });
            EcmFileOcrInfoEsDTO ecmFileInfoEsDTO = new EcmFileOcrInfoEsDTO();
            ecmFileInfoEsDTO.setOcrIdentifyInfo(objectObjectHashMap);
            ecmFileInfoEsDTO.setExif(String.valueOf(fileExif));
            ecmFileInfoEsDTO.setIdentifyType(String.valueOf(StateConstants.YES));
            ecmFileInfoEsDTO.setDtdTypeName(stringHashMap);
            list.add(ecmFileInfoEsDTO);
        }
        List<EcmFileOcrInfoEsDTO> oldFileInfoList=new ArrayList<>();
        //存入es
        if (!existsDocument) {
            //如果没有创建过则创建新的
            EsEcmFile baseFileObjEs = getBaseFileObjEs(fileId, "", "", null);
            baseFileObjEs.setDtdCode(ObjectUtils.isEmpty(dtdCode)?"":dtdCode.toString());
            baseFileObjEs.setDtdTypeName(ObjectUtils.isEmpty(dtdTypeNames)?"":dtdTypeNames.toString());
            baseFileObjEs.setOcrInfo(list);
            baseFileObjEs.setIsOcrIdentify(IcmsConstants.ONE.longValue());
            try {
                IndexRequest indexRequest = new IndexRequest(fileIndex).id(fileId+"");
                indexRequest.source(JSONObject.toJSONString(baseFileObjEs), XContentType.JSON);
                IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
                log.info("插入es结果 : {}", indexResponse);
//                esEcmFileMapper.insert(baseFileObjEs, fileIndex);
            } catch (Exception e) {
                log.error("esEcmFileMapper.insert报错",e);
                throw new RuntimeException(e.getMessage(), e);
            }
            log.info("成功添加OCR信息到es");
        } else {
            EcmFileOcrInfoEsExtendDTO oldAttrInfo = searshImageAttr(Long.valueOf(id));
            oldFileInfoList = (oldAttrInfo != null && oldAttrInfo.getEcmFileOcrInfoEsDTOList() != null)
                    ? oldAttrInfo.getEcmFileOcrInfoEsDTOList()
                    : new ArrayList<>();
            if(CollectionUtil.isNotEmpty(oldFileInfoList) && CollectionUtil.isNotEmpty(list)){
                EcmFileOcrInfoEsDTO dto = oldFileInfoList.get(0);
                EcmFileOcrInfoEsDTO ecmFileOcrInfoEsDTO = list.get(0);
                ecmFileOcrInfoEsDTO.getDtdTypeName().addAll(dto.getDtdTypeName());
                ecmFileOcrInfoEsDTO.getOcrIdentifyInfo().addAll(dto.getOcrIdentifyInfo());
            }
            esEcmFileMapper.update(null, new LambdaEsUpdateWrapper<EsEcmFile>().indexName(fileIndex)
                    .set(!ObjectUtils.isEmpty(dtdCode),EsEcmFile::getDtdCode, dtdCode.toString())
                    .set(!ObjectUtils.isEmpty(dtdTypeNames),EsEcmFile::getDtdTypeName, dtdTypeNames.toString())
                    .set(EsEcmFile::getOcrInfo, list)
                    .set(EsEcmFile::getIsOcrIdentify, IcmsConstants.ONE).eq(EsEcmFile::getId, id));
            log.info("成功修改OCR信息到es");

        }
        //存文件属性历史记录
        createFileAttrOperationRecord(id, token, oldFileInfoList, list);
        return list;
    }

    /**
     * 生成文件索引内容
     */
    private EsEcmFile getBaseFileObjEs(Long fileId, String fileName, String suffix,
                                       String base64FileContent) {
        EsEcmFile baseFileObjEs = new EsEcmFile();
        baseFileObjEs.setId(null == fileId ? null : fileId + "");
        baseFileObjEs.setBaseBizSource(application);
        baseFileObjEs.setBaseBizSourceId(fileId);
        //目前跟sourceId一致
        baseFileObjEs.setFileId(fileId + "");
        baseFileObjEs.setFileName(fileName);
        baseFileObjEs.setFileSuffix(suffix.toLowerCase());
        //如是文本文件提取赋值
        baseFileObjEs.setTitle("");
        baseFileObjEs.setAbstracts("");
        //如是图片视频文件提取赋值
        baseFileObjEs.setExif("");
        baseFileObjEs.setOcrInfo(null);
        //文件内容
        baseFileObjEs.setContent(base64FileContent);
        return baseFileObjEs;
    }

    private EcmFileInfoDTO addFileInfoBack(AccountTokenExtendDTO token, Long busiId, String docId, EcmFileInfoDTO e, EcmBusiInfoRedisDTO ecmBusiInfo) {
        AssertUtils.isNull(e.getFileReuse(), "是否复用字段（fileReuse）不能为空{是否复用（默认0，1:复用）}");
        AssertUtils.isNull(busiId, "业务主键（busiId）不能为空");
        //树节点
        if (ecmBusiInfo == null) {
            ecmBusiInfo = busiCacheService.getEcmBusiInfoRedisDTO(token, busiId);
        }
        List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = CommonUtils.flattenTreeToDFS(ecmBusiInfo.getEcmBusiDocRedisDTOS());
        e.setCreateUserName(token.getName());
        e.setCreateUser(token.getUsername());
        e.setCreateTime(new Date());
        e.setTreeType(ecmBusiInfo.getTreeType().toString());
        e.setRightVer(ecmBusiInfo.getRightVer());
        if (token.isOut()) {
            e.setOrgCode(token.getOrgCode());
        } else {
            e.setOrgCode(addInstNo(token));
        }
        if (StrUtil.isNotBlank(e.getDocFileSort())) {
            if (e.getDocFileSort().indexOf('_') != -1) {
                Long fileRealSort = Long.valueOf(e.getDocFileSort().substring(e.getDocFileSort().indexOf('_') + IcmsConstants.ONE));
                Long fileTime = Long.valueOf(e.getDocFileSort().substring(0, e.getDocFileSort().indexOf('_')));
                e.setFileSort((double) (fileRealSort * 10 + fileTime));
            }
        }
        e.setNewFileId(e.getFileId());
        e.setFileId(snowflakeUtil.nextId());
        e.setBusiId(busiId);
        e.setDocId(docId);
        e.setNewFileUrl(IcmsConstants.NEW_FILE_URL);
        e.setState(StateConstants.ZERO);
        if (IcmsConstants.DYNAMIC_TREE.equals(ecmBusiInfo.getTreeType())) {
            if (IcmsConstants.UNCLASSIFIED_ID.equals(docId)) {
                e.setDocCode(docId);
                e.setComment("");
                e.setDocName("未归类");
            } else {
                Map<String, List<EcmBusiDocRedisDTO>> collect = ecmBusiDocRedisDTOS.stream()
                        .collect(Collectors.groupingBy(dto -> dto.getDocId().toString()));
                List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS1 = collect.get(docId);
                if (!CollectionUtils.isEmpty(ecmBusiDocRedisDTOS1)) {
                    EcmBusiDocRedisDTO ecmBusiDoc = ecmBusiDocRedisDTOS1.get(0);
                    e.setDocCode(ecmBusiDoc.getDocCode());
                    e.setComment(ObjectUtils.isEmpty(ecmBusiDoc) ? "" : ecmBusiDoc.getDocName());
                    e.setDocName(ObjectUtils.isEmpty(ecmBusiDoc) ? "" : ecmBusiDoc.getDocName());
                }

            }

        } else {
            Map<String, List<EcmBusiDocRedisDTO>> collect = ecmBusiDocRedisDTOS.stream()
                    .collect(Collectors.groupingBy(EcmBusiDocRedisDTO::getDocCode));
            List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS1 = collect.get(docId);
            if (!CollectionUtils.isEmpty(ecmBusiDocRedisDTOS1)) {
                EcmBusiDocRedisDTO ecmDocDef = ecmBusiDocRedisDTOS1.get(0);
                e.setComment(ObjectUtils.isEmpty(ecmDocDef) ? "" : ecmDocDef.getDocName());
                e.setDocName(ObjectUtils.isEmpty(ecmDocDef) ? "" : ecmDocDef.getDocName());
            }
            e.setDocCode(docId);
        }
        if (null != e.getMarkDocId()) {
            List<EcmBusiDoc> ecmBusiDocs = ecmBusiInfo.getEcmBusiDocs();
            EcmBusiDoc ecmBusiDoc = null;
            if (!CollectionUtils.isEmpty(ecmBusiDocs)) {
                Map<Long, List<EcmBusiDoc>> collect = ecmBusiDocs.stream().collect(Collectors.groupingBy(EcmBusiDoc::getDocId));
                List<EcmBusiDoc> ecmBusiDocs1 = collect.get(e.getMarkDocId());
                if (!CollectionUtils.isEmpty(ecmBusiDocs1)) {
                    ecmBusiDoc = ecmBusiDocs1.get(0);
                }
            }
            //查询标记节点名称
            if (ecmBusiDoc == null) {
                ecmBusiDoc = ecmBusiDocMapper.selectById(e.getMarkDocId());
            }
            e.setComment(ecmBusiDoc.getDocName());
        }
        //添加文件exif 信息
        if (!CollectionUtils.isEmpty(e.getFileExif()) && e.getFileExif().get("GPSLongitude") != null
                && e.getFileExif().get("GPSLatitude") != null) {
            HashMap fileExif = e.getFileExif();
            //经度
            Object gpsLongitude = fileExif.get("GPSLongitude");
            //纬度
            Object gpsLatitude = fileExif.get("GPSLatitude");
            if (!ObjectUtils.isEmpty(gpsLongitude) && !ObjectUtils.isEmpty(gpsLatitude)) {
                String lon = gpsLongitude.toString();
                String lat = gpsLatitude.toString();
                Result<AreaInfoDTO> location = getLocation(formatCoordinate(lon),
                        formatCoordinate(lat));
                AreaInfoDTO data = location.getData();
                if(!ObjectUtils.isEmpty(data)) {
                    fileExif.put("provName", data.getProvName());
                    fileExif.put("cityName", data.getCityName());
                    fileExif.put("districtName", data.getDistrictName());
                    e.setFileExif(fileExif);
                }
            }
        }
        return e;
    }

    private EcmFileInfoDTO addFileInfo(AccountTokenExtendDTO token, Long busiId, String docId, EcmFileInfoDTO e) {
        return addFileInfoBack(token, busiId, docId, e, null);
    }

    /**
     * 经纬度格式
     * @param value
     * @return
     */
    private double formatCoordinate(String value) {
        try {
            return Double.parseDouble(value.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid coordinate format: " + value);
        }
    }

    private void deleteFileInfoByMore(FileInfoVO vo, String type,AccountTokenExtendDTO tokenExtendDTO) {
        //更新redis数据
        deleteFileInfoByMoreToRedis(vo, type);
        //更新持久化数据库数据
        deleteFileInfoByMoreToDb(vo, type);
        if (IcmsConstants.IS_DELETED.equals(type)) {
            //删除es数据
            deleteFileInfoByMoreToEs(vo);
            List<Long> fileIds=vo.getFileIdList();
            //彻底删除需要删除掉异步任务列表数据
            asyncTaskService.batchDelete(fileIds, vo.getBusiId());
            //删除带归类列表缓存数据
            fileIds.forEach(fileId->{
                busiCacheService.delAutoClassPendingTaskList(RedisConstants.AUTO_CLASS_PENDING_TASK_LIST + vo.getBusiId(),
                        fileId.toString());
            });
            //删除AFM中数据
//            vo.getFileIdList().forEach(item->{
//                AfmDetImgDetDTO dto = new AfmDetImgDetDTO();
//                dto.setSourceSys(IcmsConstants.ECM);
//                dto.setFileIndex(item.toString());
//                antiFraudDetApi.delFile(dto);
//            });
            changeAfmData(null,null,vo.getFileIdList(),IcmsConstants.TWO);
            //添加一条销毁记录
            EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectByIdWithDeleted(vo.getBusiId());
            addEcmDestroyTask(tokenExtendDTO, ecmBusiInfo);
        }
    }

    private void addEcmDestroyTask(AccountTokenExtendDTO tokenExtendDTO, EcmBusiInfo ecmBusiInfo) {
        EcmDestroyTask task = new EcmDestroyTask();
        long destroyId = snowflakeUtils.nextId();
        task.setId(destroyId);
        task.setDestroyType(IcmsConstants.DESTROY_TYPE_FOUR);
        task.setAppCode(ecmBusiInfo.getAppCode());
        task.setBusiCreateDate("");
        task.setOrgCode(ecmBusiInfo.getOrgCode());
        task.setCreateUser(tokenExtendDTO.getUsername());
        task.setStatus(IcmsConstants.DESTROY_STATUS_ONE);
        task.setOrgName(tokenExtendDTO.getOrgName());
        task.setCreateUserName(tokenExtendDTO.getName());
        EcmDestroyList destroyList = new EcmDestroyList();
        BeanUtils.copyProperties(ecmBusiInfo, destroyList);
        destroyList.setId(snowflakeUtils.nextId());
        destroyList.setDestroyTaskId(destroyId);
        Set<Long> busiIds =new HashSet<>();
        busiIds.add(destroyList.getBusiId());
        int size = ecmFileInfoMapper.selectWithDeleteByBusiId(busiIds).size();
        destroyList.setFileCount((long)size);
        ecmDestroyTaskMapper.insert(task);
        ecmDestroyListMapper.insert(destroyList);
    }

    private void deleteFileInfoByMoreToEs(FileInfoVO vo) {
        esEcmFileMapper.deleteBatchIds(vo.getFileIdList(), fileIndex);
    }

    private void deleteFileInfoByMoreToDb(FileInfoVO vo, String type) {
        if (IcmsConstants.STATE.equals(type)) {
            ecmFileInfoMapper.update(null, new UpdateWrapper<EcmFileInfo>()
                    .set("state", IcmsConstants.ONE).set("update_user", vo.getCurentUserId())
                    .set("update_time", vo.getUpdateTime()).in("file_id", vo.getFileIdList()));
        } else if (IcmsConstants.IS_DELETED.equals(type)) {
            //已删除列表删除
            ecmFileInfoMapper.deleteBatchIds(vo.getFileIdList());
        }
    }

    private void deleteFileInfoByMoreToRedis(FileInfoVO vo, String type) {
        List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService.getFileInfoRedis(vo.getBusiId(), vo.getFileIdList());
        //如果是已删除的删除操作,数据过滤出来删除掉
        if (IcmsConstants.IS_DELETED.equals(type) && !CollectionUtils.isEmpty(vo.getFileIdList())) {
            // 将文件ID列表转换为 Set，提升查找效率
            Set<Long> fileIdSet = new HashSet<>(vo.getFileIdList());
            fileInfoRedisEntities = fileInfoRedisEntities.stream()
                    // 过滤掉那些 ID 在 fileIdList 中的文件
                    .filter(file -> !fileIdSet.contains(file.getFileId()))
                    .collect(Collectors.toList());
            busiCacheService.delFileInfoRedisReal(vo.getBusiId(), vo.getFileIdList());
        }
        //走不显示操作
        else {
            fileInfoRedisEntities.stream().filter(p -> vo.getFileIdList().contains(p.getFileId()))
                    .forEach(p -> {
                        //已删除状态
                        p.setState(IcmsConstants.ONE);
                        p.setUpdateUser(vo.getCurentUserId());
                        p.setUpdateTime(vo.getUpdateTime());
                    });
        }
        //添加文件历史记录
        addFileHistory(vo.getFileIdList(), fileInfoRedisEntities, vo.getCurentUserId(),
                IcmsConstants.DELETE_FILE_STRING);
        busiCacheService.updateFileInfoRedis(fileInfoRedisEntities);
    }

    private void addFileHistory(List<Long> fileIdList, List<FileInfoRedisDTO> fileInfoRedisEntities,
                                String curentUserId, String deleteFile) {
        if (CollectionUtils.isEmpty(fileInfoRedisEntities)) {
            return;
        }
        for (FileInfoRedisDTO fileInfoRedisDTO : fileInfoRedisEntities) {
            if (fileIdList.contains(fileInfoRedisDTO.getFileId())) {
                String ext = CommonUtils.getExt(fileInfoRedisDTO);
                //新增一条文件历史记录
                EcmFileHistory ecmFileHistory = commonService.insertFileHistory(
                        fileInfoRedisDTO.getBusiId(), fileInfoRedisDTO.getFileId(),
                        fileInfoRedisDTO.getNewFileId(), deleteFile, curentUserId,
                        fileInfoRedisDTO.getSize(), ext);
                if (CollectionUtils.isEmpty(fileInfoRedisDTO.getFileHistories())) {
                    List<EcmFileHistory> ecmFileHistories = new ArrayList<>();
                    ecmFileHistories.add(ecmFileHistory);
                    fileInfoRedisDTO.setFileHistories(ecmFileHistories);
                    fileInfoRedisDTO.setUpdateTime(new Date());
                    fileInfoRedisDTO.setUpdateUserName(curentUserId);
                } else {
                    List<EcmFileHistory> fileHistories = fileInfoRedisDTO.getFileHistories();
                    ArrayList<EcmFileHistory> ecmFileHistories = new ArrayList<>(fileHistories);
                    ecmFileHistories.add(ecmFileHistory);
                    fileInfoRedisDTO.setFileHistories(ecmFileHistories);
                    fileInfoRedisDTO.setUpdateTime(new Date());
                    fileInfoRedisDTO.setUpdateUserName(curentUserId);
                }
            }
        }
    }

    /**
     * 更新缓存文件信息
     */
    private FileInfoRedisDTO updateFileByBusiInfoRedis(List<FileInfoRedisDTO> fileInfoRedisDTOList, List<Long> delFileIdList, AccountTokenExtendDTO token) {
        FileInfoRedisDTO fileInfoRedisDTO = null;
        if (CollectionUtil.isNotEmpty(fileInfoRedisDTOList)) {
            fileInfoRedisDTO = fileInfoRedisDTOList.get(0);
            busiCacheService.updateFileInfoRedis(fileInfoRedisDTO);
            if (!ObjectUtils.isEmpty(delFileIdList)) {
                List<FileInfoRedisDTO> fileInfoRedisDTOS = busiCacheService
                        .delFileInfoRedis(fileInfoRedisDTO.getBusiId(), delFileIdList);
                //添加文件历史记录
                addFileHistory(delFileIdList, fileInfoRedisDTOS, token.getName(),
                        IcmsConstants.MERGE_DELETE_FILE_STRING);
            }
        }
        return fileInfoRedisDTO;
    }

    private void addDocTypeName(FileInfoRedisDTO fileInfoRedisDTO) {
        if (ObjectUtils.isEmpty(fileInfoRedisDTO.getDocName())) {
            fileInfoRedisDTO.setDocName("未归类");
        }
        if (ObjectUtils.isEmpty(fileInfoRedisDTO.getDocCode())) {
            fileInfoRedisDTO.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
        }
        if (ObjectUtils.isEmpty(fileInfoRedisDTO.getComment())) {
            EcmDocDef ecmDocDef = ecmDocDefMapper.selectById(fileInfoRedisDTO.getDocCode());
            if (!ObjectUtils.isEmpty(ecmDocDef)) {
                fileInfoRedisDTO.setDocName(ecmDocDef.getDocName());
                fileInfoRedisDTO.setComment(ecmDocDef.getDocName());
            }
        }
    }

    /**
     * 添加资料类型名称
     */
    private void addDocTypeName(EcmFileInfoDTO fileInfoRedisDTO,
                                Map<String, List<EcmDocDef>> docCodeListMap) {
        if (ObjectUtils.isEmpty(fileInfoRedisDTO.getDocName())) {
            fileInfoRedisDTO.setDocName("未归类");
        }
        if (ObjectUtils.isEmpty(fileInfoRedisDTO.getDocCode())) {
            fileInfoRedisDTO.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
        }
        if (ObjectUtils.isEmpty(fileInfoRedisDTO.getComment())) {
            List<EcmDocDef> ecmDocDefs = docCodeListMap.get(fileInfoRedisDTO.getDocCode());
            EcmDocDef ecmDocDef = ObjectUtils.isEmpty(ecmDocDefs) ? null : ecmDocDefs.get(0);
            if (!ObjectUtils.isEmpty(ecmDocDef)) {
                fileInfoRedisDTO.setDocName(ecmDocDef.getDocName());
                fileInfoRedisDTO.setComment(ecmDocDef.getDocName());
            }
        }
    }

    private void restoreFileInfoByMore(FileInfoVO vo) {
        //更新持久化数据库数据
        restoreFileInfoByMoreToDb(vo);
        //更新redis数据
        restoreFileInfoByMoreToRedis(vo);
    }

    private void restoreFileInfoByMoreToDb(FileInfoVO vo) {
        ecmFileInfoMapper.update(null,
                new UpdateWrapper<EcmFileInfo>().set("state", IcmsConstants.ZERO)
                        .set("update_user", vo.getCurentUserId())
                        .set("update_time", vo.getUpdateTime()).in("file_id", vo.getFileIdList()));
    }

    private void restoreFileInfoByMoreToRedis(FileInfoVO vo) {
        List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService.getFileInfoRedis(vo.getBusiId(), vo.getFileIdList());
        fileInfoRedisEntities.stream().filter(p -> vo.getFileIdList().contains(p.getFileId())).forEach(p -> {
            //未删除状态
            p.setState(IcmsConstants.ZERO);
            p.setUpdateUser(vo.getCurentUserId());
            p.setUpdateTime(vo.getUpdateTime());
            //恢复文件去掉标注标识
            p.setFileCommentCount(null);
        });
        //添加文件历史记录
        addFileHistory(vo.getFileIdList(), fileInfoRedisEntities, vo.getCurentUserId(),
                IcmsConstants.RESTORE_FILE_STRING);
        busiCacheService.updateFileInfoRedis(fileInfoRedisEntities);
    }

    private void editDataToDb(MoveFileVO moveFileVo, AccountTokenExtendDTO token,
                              Map<Long, List<EcmFileInfo>> fileInfoMapByFileId, String docId,
                              List<EcmFileInfo> ecmFileInfoNew) {
        double i = 0d;
        double up = 0d;
        double down = 0d;
        List<Long> allFileId = moveFileVo.getAllFileId();
        for (int j = 0; j < allFileId.size(); j++) {
            List<EcmFileInfo> ecmFileInfos2 = fileInfoMapByFileId.get(allFileId.get(j));
            EcmFileInfo ecmFileInfo = ecmFileInfos2.get(StateConstants.ZERO);
            if (docId.equals(ecmFileInfo.getDocCode())
                    && allFileId.get(j).equals(moveFileVo.getFileId().get(0))) {
                if (j == 0) {
                    List<EcmFileInfo> ecmFileInfos = fileInfoMapByFileId.get(allFileId.get(j + 1));
                    BigDecimal subtract = BigDecimal.valueOf(ecmFileInfos.get(0).getFileSort()).subtract(BigDecimal.valueOf(1d));
                    i = subtract.setScale(4, RoundingMode.HALF_UP).doubleValue();
                    ecmFileInfo.setFileSort(i);
                    ecmFileInfoNew.add(ecmFileInfo);
                } else if (j == allFileId.size() - 1) {
                    List<EcmFileInfo> ecmFileInfos = fileInfoMapByFileId.get(allFileId.get(j - 1));
                    BigDecimal add = BigDecimal.valueOf(ecmFileInfos.get(0).getFileSort())
                            .add(BigDecimal.valueOf(1d));
                    i = add.setScale(4, RoundingMode.HALF_UP).doubleValue();
                    ecmFileInfo.setFileSort(i);
                    ecmFileInfoNew.add(ecmFileInfo);
                } else {
                    List<EcmFileInfo> ecmFileInfos3 = fileInfoMapByFileId.get(allFileId.get(j - 1));
                    up = ecmFileInfos3.get(0).getFileSort();
                    List<EcmFileInfo> ecmFileInfos4 = fileInfoMapByFileId.get(allFileId.get(j + 1));
                    down = ecmFileInfos4.get(0).getFileSort();
                    i = getSortFloat(up, down);
                    ecmFileInfo.setFileSort(i);
                    ecmFileInfoNew.add(ecmFileInfo);
                }
                //更新DB数据
                ecmFileInfoMapper.update(null,
                        new UpdateWrapper<EcmFileInfo>().set("file_sort", ecmFileInfo.getFileSort())
                                .set("update_user_name", token.getName())
                                .set("update_time", new Date())
                                .eq("file_id", moveFileVo.getFileId().get(0)));
                break;
            }
        }

    }

    private void editDataListToDb(MoveFileVO moveFileVo, AccountTokenExtendDTO token,
                                  Map<Long, List<EcmFileInfo>> fileInfoMapByFileId,
                                  List<EcmFileInfo> ecmFileInfoNew) {
        double i = 0;
        List<Long> allFileId = moveFileVo.getAllFileId();
        List<Long> moveFilesId = moveFileVo.getMoveFilesId();
        if (moveFilesId.get(0).equals(allFileId.get(0))) {
            //拖到第一个了
            int index = allFileId.indexOf(moveFilesId.get(moveFilesId.size() - 1));
            List<EcmFileInfo> ecmFileInfos = fileInfoMapByFileId.get(allFileId.get(index + 1));
            Double fileSort = ecmFileInfos.get(0).getFileSort();
            //判断是否一类
            List<EcmFileInfo> infos = fileInfoMapByFileId
                    .get(moveFilesId.get(moveFilesId.size() - 1));
            if (!ecmFileInfos.get(0).getDocCode().equals(infos.get(0).getDocCode())) {
                AssertUtils.isTrue(true, "不能跨资料节点拖拽");
            }
            BigDecimal subtract = BigDecimal.valueOf(fileSort).subtract(BigDecimal.valueOf(1d));
            double firstSort = subtract.doubleValue();
            //均分
            //BigDecimal div = BigDecimal.valueOf(1).divide(BigDecimal.valueOf(moveFilesId.size()),4,RoundingMode.DOWN);
            i = getAddDouble(moveFilesId, 1);
            updateSort(token, fileInfoMapByFileId, i, ecmFileInfoNew, moveFilesId, firstSort);
        } else if (moveFilesId.get(moveFilesId.size() - 1)
                .equals(allFileId.get(allFileId.size() - 1))) {
            //拖到最后一个了
            int index = allFileId.indexOf(moveFilesId.get(0));
            List<EcmFileInfo> ecmFileInfos = fileInfoMapByFileId.get(allFileId.get(index - 1));
            Double fileSort = ecmFileInfos.get(0).getFileSort();
            //判断是否一类
            List<EcmFileInfo> infos = fileInfoMapByFileId.get(moveFilesId.get(0));
            if (!ecmFileInfos.get(0).getDocCode().equals(infos.get(0).getDocCode())) {
                AssertUtils.isTrue(true, "不能跨资料节点拖拽");
            }
            double firstSort = fileSort;
            i = getAddDouble(moveFilesId, 1);
            updateSort(token, fileInfoMapByFileId, i, ecmFileInfoNew, moveFilesId, firstSort);
        } else {
            //拖到中间位置
            double up = 0d;
            double down = 0d;
            //取到拖拽文件位置前一个和后一个的fileSort
            int index1 = allFileId.indexOf(moveFilesId.get(0));
            List<EcmFileInfo> ecmFileInfo1 = fileInfoMapByFileId.get(allFileId.get(index1 - 1));
            up = ecmFileInfo1.get(0).getFileSort();
            int index2 = allFileId.indexOf(moveFilesId.get(moveFilesId.size() - 1));
            List<EcmFileInfo> ecmFileInfo2 = fileInfoMapByFileId.get(allFileId.get(index2 + 1));
            down = ecmFileInfo2.get(0).getFileSort();
            //判断是否一类
            List<EcmFileInfo> infos = fileInfoMapByFileId.get(moveFilesId.get(0));
            List<EcmFileInfo> infos1 = fileInfoMapByFileId
                    .get(moveFilesId.get(moveFilesId.size() - 1));
            if (!ecmFileInfo1.get(0).getDocCode().equals(ecmFileInfo2.get(0).getDocCode())) {
                AssertUtils.isTrue(true, "不能跨资料节点拖拽");
            }
            if (!ecmFileInfo1.get(0).getDocCode().equals(infos.get(0).getDocCode())
                    || !ecmFileInfo2.get(0).getDocCode().equals(infos1.get(0).getDocCode())) {
                AssertUtils.isTrue(true, "不能跨资料节点拖拽");
            }
            //取得前一个和后一个差值
            BigDecimal subtract = BigDecimal.valueOf(down).subtract(BigDecimal.valueOf(up));
            double v = subtract.doubleValue();
            //均分
            i = getAddDouble(moveFilesId, v);
            updateSort(token, fileInfoMapByFileId, i, ecmFileInfoNew, moveFilesId, up);
        }
    }

    private Double getAddDouble(List<Long> moveFilesId, double v) {
        BigDecimal div = BigDecimal.valueOf(v).divide(BigDecimal.valueOf(moveFilesId.size() + 1), 4,
                RoundingMode.DOWN);
        return div.doubleValue();
    }

    private void updateSort(AccountTokenExtendDTO token,
                            Map<Long, List<EcmFileInfo>> fileInfoMapByFileId, Double i,
                            List<EcmFileInfo> ecmFileInfoNew, List<Long> moveFilesId, double up) {
        for (Long fileId : moveFilesId) {
            List<EcmFileInfo> ecmFileInfoList = fileInfoMapByFileId.get(fileId);
            EcmFileInfo ecmFileInfo = ecmFileInfoList.get(0);
            ecmFileInfo.setFileSort(up + i);
            ecmFileInfoNew.add(ecmFileInfo);
            //更新redis数据
            ecmFileInfoMapper.update(null,
                    new UpdateWrapper<EcmFileInfo>().set("file_sort", ecmFileInfo.getFileSort())
                            .set("update_user_name", token.getName()).set("update_time", new Date())
                            .eq("file_id", fileId));
            up += i;
        }
    }

    private Double getSortFloat(double up, double down) {
        BigDecimal a = new BigDecimal(up);
        BigDecimal b = new BigDecimal(down);
        BigDecimal add = a.add(b);
        BigDecimal i = add.divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        return i.doubleValue();
    }

    private void editDataToRedis(Long busiId, Long fileId, Double i, AccountTokenExtendDTO token) {
        FileInfoRedisDTO fileInfoRedisDTO = busiCacheService.getFileInfoRedisSingle(busiId, fileId);
        fileInfoRedisDTO.setFileSort(i);
        fileInfoRedisDTO.setUpdateUserName(token.getName());
        fileInfoRedisDTO.setUpdateTime(new Date());
        busiCacheService.updateFileInfoRedis(fileInfoRedisDTO);
    }

    private void handleMiPaSpData(SysFileDTO sysFileDTO, EcmFileInfoDTO ecmFileInfoDTO, Integer i) {
        ecmFileInfoDTO.setNewFileId(sysFileDTO.getId());
        ecmFileInfoDTO.setFileMd5(sysFileDTO.getFileMd5());
        ecmFileInfoDTO.setSourceFileMd5(sysFileDTO.getSourceFileMd5());
        ecmFileInfoDTO.setNewFileName(sysFileDTO.getOriginalFilename());
        ecmFileInfoDTO.setFileSort(ecmFileInfoDTO.getFileSort() + i);
    }

    /**
     * 保存文件信息到数据库
     */
    private FileInfoRedisDTO saveFileInfo(EcmFileInfoDTO ecmFileInfoDTO, AccountTokenExtendDTO token) {

        List<FileInfoRedisDTO> fileInfoRedisDTOList = new ArrayList<>();
        //添加文件信息
        FileInfoRedisDTO fileInfoRedisDTO = saveDBAndESFileInfo(ecmFileInfoDTO, token, fileInfoRedisDTOList);
        //更新redis缓存中对应业务的文件信息数据
        updateFileByBusiInfoRedis(fileInfoRedisDTOList, new ArrayList<Long>(), token);
        return fileInfoRedisDTO;
    }

    private String addInstNo(AccountTokenExtendDTO token) {
        if (token.isOut()) {
            return token.getOrgCode();
        } else {
            Long instId = token.getInstId();
            //根据机构id获取机构号
            SysInstDTO sysInstDTO = instApi.getInstByInstId(instId).getData();
            AssertUtils.isNull(sysInstDTO, "机构号参数错误");
            token.setOrgName(sysInstDTO.getName());
            return sysInstDTO.getInstNo();
        }

    }

    public SysStrategyDTO isAutoGroup(String appCode) {
        //查询OCR识别总配置（0为未配置）
        SysStrategyDTO sysStrategyDTO = sysStrategyService.queryConfig();
        //OCR识别业务类型ID
        List<String> ocrConfigIds = searchChildren(sysStrategyDTO.getOcrConfigIds());
        if (!ocrConfigIds.contains(appCode)) {
            sysStrategyDTO.setOcrConfigStatus(false);
        }
        //混贴拆分业务类型ID
        List<String> splitIds = searchChildren(sysStrategyDTO.getSplitIds());
        if (!splitIds.contains(appCode)) {
            sysStrategyDTO.setSplitStatus(false);
        }
        return sysStrategyDTO;
    }

    private List<String> searchChildren(List<String> ocrIdentifyIds) {
        //查询所有
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectList(null);
        //根据父节点分组
        Map<String, List<EcmAppDef>> listMap = ecmAppDefs.stream()
                .collect(Collectors.groupingBy(EcmAppDef::getParent));
        List<String> allOcrIdentifyIds = new ArrayList();
        //将数据库存的id的所有子类存入一个新的数组
        addAllId(listMap, ocrIdentifyIds, allOcrIdentifyIds);
        return allOcrIdentifyIds;
    }

    private void addAllId(Map<String, List<EcmAppDef>> listMap, List<String> ocrIdentifyIds,
                          List<String> allOcrIdentifyIds) {
        if (!CollectionUtils.isEmpty(ocrIdentifyIds)) {
            for (String id : ocrIdentifyIds) {
                List<EcmAppDef> ecmAppDefs = listMap.get(id);
                if (CollectionUtils.isEmpty(ecmAppDefs)) {
                    allOcrIdentifyIds.add(id);
                } else {
                    //得到该子类的id
                    List<String> list = ecmAppDefs.stream().map(EcmAppDef::getAppCode)
                            .collect(Collectors.toList());
                    addAllId(listMap, list, allOcrIdentifyIds);
                }
            }
        }
    }

    /**
     * 不校验数量-由前段来校验
     */
    private void checkDocRightByAutoClassify(EcmFileInfoDTO ecmFileInfoDTO, String operation,
                                             AccountTokenExtendDTO token, boolean blag,
                                             EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        //获取redis缓存中的业务信息
        List<FileInfoRedisDTO> fileInfoRedisDTOList = new ArrayList<>();
        FileInfoRedisDTO fileInfoRedisDTO = new FileInfoRedisDTO();
        BeanUtils.copyProperties(ecmFileInfoDTO, fileInfoRedisDTO);
        fileInfoRedisDTO.setRightVer(ecmBusiInfoRedisDTO.getRightVer());
        fileInfoRedisDTO.setDocCode(ecmFileInfoDTO.getDocCode());
        fileInfoRedisDTOList.add(fileInfoRedisDTO);

        EcmBusiStructureTreeDTO ecmBusiStructureTreeDTO = new EcmBusiStructureTreeDTO();
        ecmBusiStructureTreeDTO.setDocCode(ecmFileInfoDTO.getDocCode());
        ecmBusiStructureTreeDTO.setName(ecmFileInfoDTO.getDocName());
        ecmBusiStructureTreeDTO.setBusiId(ecmFileInfoDTO.getBusiId());
        //文件权限从关联版本获取最新权限
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService
                .getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
        List<EcmDocrightDefDTO>  currentDocRight = busiCacheService.getDocrightDefCommon(ecmBusiInfoRedisDTO, token);
        commonService.checkDocRightTarget(fileInfoRedisDTOList, ecmBusiInfoRedisDTO,
                ecmBusiStructureTreeDTO, operation, blag, fileInfoRedis,currentDocRight);
    }

    /**
     * 图片编辑后插入历史记录
     */
    private EcmFileHistory saveFileHistory(Long busiId, Long fileId, Long newFileId,
                                           String fileOperation, String userId, Long fileSize,
                                           String ext) {
        AssertUtils.isNull(userId, "参数错误:token中的userName为空");
        EcmFileHistory ecmFileHistory = new EcmFileHistory();
        ecmFileHistory.setBusiId(busiId);
        ecmFileHistory.setFileId(fileId);
        ecmFileHistory.setNewFileSize(fileSize);
        ecmFileHistory.setNewFileId(newFileId);
        ecmFileHistory.setFileOperation(fileOperation);
        ecmFileHistory.setCreateUser(userId);
        ecmFileHistory.setNewFileExt(ext);
        ecmFileHistoryMapper.insert(ecmFileHistory);
        return ecmFileHistory;
    }

    /**
     * 将文件集合一次性添加到redis
     */
    private List<FileInfoRedisDTO> addFileListByBusiInfoRedis(List<EcmFileInfoDTO> ecmFileInfoDTO, List<EcmFileHistory> ecmFileHistoryList, List<EcmFileLabel> labels) {
        //将历史记录根据文件id分组
        Map<Long, List<EcmFileHistory>> listMap = ecmFileHistoryList.stream()
                .collect(Collectors.groupingBy(EcmFileHistory::getFileId));
        Map<Long, List<EcmFileLabel>> collect = labels.stream()
                .collect(Collectors.groupingBy(EcmFileLabel::getFileId));
        ecmFileInfoDTO.forEach(p -> {
            FileInfoRedisDTO fileInfoRedisDTO = new FileInfoRedisDTO();
            BeanUtils.copyProperties(p, fileInfoRedisDTO);
            //新增一条文件历史记录
            fileInfoRedisDTO.setFileHistories(listMap.get(p.getFileId()));
            fileInfoRedisDTO.setEcmFileLabels(collect.get(p.getFileId()));
            busiCacheService.updateFileInfoRedis(fileInfoRedisDTO);
        });

        return null;
    }

    private FileInfoVO handleMergeBeforeFileInfo(MergFileVO vo, AccountTokenExtendDTO token,
                                                 List<Long> fileIdList) {
        FileInfoVO fileInfoVO = new FileInfoVO();
        fileInfoVO.setFileIdList(fileIdList);
        fileInfoVO.setBusiId(vo.getBusiId());
        fileInfoVO.setCurentUserId(token.getUsername());
        fileInfoVO.setUpdateTime(new Date());
        return fileInfoVO;
    }

    private void checkEmpt(MergFileVO vo, EcmFileInfoDTO ecmFileInfoDTO1) {
        AssertUtils.isNull(ecmFileInfoDTO1, "合并后的文件信息不能为空");
        AssertUtils.isNull(vo.getFileIdList(), "要合并的文件id集合不能为空");
        AssertUtils.isNull(vo.getNewFileIdList(), "要合并的new文件id集合不能为空");
        AssertUtils.isNull(vo.getNewFileNames(), "newFileNames不能为空");
        AssertUtils.isNull(ecmFileInfoDTO1.getAppTypeName(), "业务类型名称不能为空");
        if (ecmFileInfoDTO1.getDocFileSort() != null && ecmFileInfoDTO1.getFileSort() == null) {
            if (ecmFileInfoDTO1.getDocFileSort().indexOf("_") == -1) {
                ecmFileInfoDTO1.setFileSort(Double.valueOf(ecmFileInfoDTO1.getDocFileSort()));
            }
        }
        AssertUtils.isNull(ecmFileInfoDTO1.getFileSort(), "fileSort不能为空");
        AssertUtils.isNull(ecmFileInfoDTO1.getDocFileSort(), "docFileSort不能为空");
    }

    /**
     * TODO 按照顺序依次执行，所有检测执行完毕以后调用ecm_async_task的相关插入操作
     * 上传文件检测处理
     */
    @Async("customTaskExecutor")
    public void checkDetection(EcmFileInfoDTO ecmFileInfoDTO, EcmAsyncTask ecmAsyncTask,
                               String taskType, AccountTokenExtendDTO token, SysStrategyDTO vo)
            throws IOException {
        JSONObject requestBody = new JSONObject();
        String fileUrl = storageUrl + "/storage/deal/getFileByFileId?fileId="
                + ecmFileInfoDTO.getNewFileId();
        requestBody.put("file_url", fileUrl);
        requestBody.put("threshold", 10000);
        //文档识别
        taskType = handleDocOcr(ecmFileInfoDTO, token, taskType, vo);
        //翻拍检测
        taskType = remakeHandle(ecmFileInfoDTO.getNewFileId(), taskType, requestBody);
        // 图像矫正
        taskType = regularizeHandle(ecmFileInfoDTO.getNewFileId(), taskType, requestBody,
                ecmFileInfoDTO, token);
        // 模糊检测
        taskType = obscureHandle(ecmFileInfoDTO.getNewFileId(), taskType, requestBody);
        //查重检测
        taskType = handleAfm(ecmFileInfoDTO, taskType, fileUrl);
        //修改异步任务表文件状态
        ecmAsyncTask.setTaskType(taskType);
        asyncTaskService.updateEcmAsyncTask(ecmAsyncTask);
        //设置redis检测完成
        busiCacheService.setNeedPushBusiSync(
                RedisConstants.NEED_PUSH_BUSIASYNC_TASK_PREFIX + ecmFileInfoDTO.getBusiId(),
                IcmsConstants.DETECTION_COMPLETE, TimeOutConstants.ONE_HOURS);
    }

    public String remakeHandle(Long fileId, String taskType, JSONObject requestBody) {
        try {
            if (taskType.charAt(IcmsConstants.TYPE_SIX-1) == EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0)) {
                String remakeResponse = executePostRequest(sunEcmProperties.getCheckDetectionUrl(), requestBody);
                log.info(fileId + "智能化处理翻拍结果：{}", remakeResponse);
                taskType = processRemakeResponse(remakeResponse, taskType);
            }
        } catch (Exception e) {
            log.error(fileId + ":处理翻拍检测异常", e);
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_SIX,
                    EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
        }
        return taskType;
    }

    public String reflectiveHandle(Long fileId, String taskType, JSONObject requestBody) {
        try {
            if (taskType.charAt(IcmsConstants.TYPE_EIGHT-1) == EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0)) {
                String reflectiveResponse = executePostRequest(sunEcmProperties.getCheckReflectiveUrl(), requestBody);
                log.info(fileId + "智能化处理反光结果：{}", reflectiveResponse);
                taskType = processReflectiveResponse(reflectiveResponse, taskType);
            }
        } catch (Exception e) {
            log.error(fileId + ":处理反光检测异常", e);
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_EIGHT,
                    EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
        }
        return taskType;
    }

    public String missCornerHandle(Long fileId, String taskType, JSONObject requestBody) {
        try {
            if (taskType.charAt(IcmsConstants.TYPE_NINE-1) == EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0)) {
                String missCornerResponse = executePostRequest(sunEcmProperties.getCheckMissCornerUrl(), requestBody);
                log.info(fileId + "智能化处理缺角结果：{}", missCornerResponse);
                taskType = processMissCornerResponse(missCornerResponse, taskType);
            }
        } catch (Exception e) {
            log.error(fileId + ":处理缺角检测异常", e);
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_NINE,
                    EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
        }
        return taskType;
    }

    public String specialHandle(Long fileId, String taskType, JSONObject requestBody,
                                EcmFileInfoDTO ecmFileInfoDTO, AccountTokenExtendDTO token) {
        log.info(fileId + "智能化处理特殊处理开始");
        try {
            //先缺角检测
            taskType=missCornerHandle(fileId,taskType,requestBody);
            //再转正处理
            taskType=regularizeHandle(fileId,taskType,requestBody,ecmFileInfoDTO,token);
        } catch (Exception e) {
            log.error(fileId + ":特殊处理异常", e);
        }
        return taskType;
    }

    public String regularizeHandle(Long fileId, String taskType, JSONObject requestBody,
                                    EcmFileInfoDTO ecmFileInfoDTO, AccountTokenExtendDTO token) {
        try {
            if (taskType.charAt(IcmsConstants.TYPE_TWO - 1) == EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0)) {
                String remakeResponse = executePostRequest(sunEcmProperties.getCheckRegularizeUrl(), requestBody);
                taskType = processRegularizeResponse(remakeResponse, taskType, ecmFileInfoDTO, token);
            }
        } catch (Exception e) {
            log.error(fileId + ":处理转正异常", e);
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_TWO,
                    EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
        }
        return taskType;
    }

    public String obscureHandle(Long fileId, String taskType, JSONObject requestBody) {
        try {
            if (taskType.charAt(IcmsConstants.TYPE_THREE-1) == EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0)) {
                String checkFuzzyResponse = executePostRequest(sunEcmProperties.getCheckFuzzyUrl(), requestBody);
                log.info(fileId + "智能化处理模糊结果：{}", checkFuzzyResponse);
                taskType = processFuzzyResponse(checkFuzzyResponse, taskType);
            }
        } catch (Exception e) {
            log.error(fileId + ":处理模糊检测异常", e);
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_THREE,
                    EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
        }
        return taskType;
    }

    /**
     * 查重检测
     */
    public String handleAfm(EcmFileInfoDTO ecmFileInfoDTO, String taskType, String fileUrl) {
        // 处理查重检测
        try {
            if (taskType.charAt(IcmsConstants.TYPE_FOUR - 1) == EcmCheckAsyncTaskEnum.PROCESSING
                    .description().charAt(0) || taskType.charAt(IcmsConstants.TYPE_TEN - 1) == EcmCheckAsyncTaskEnum.PROCESSING
                    .description().charAt(0)) {
                //业务类型资料类型不能为空
                AssertUtils.isNull(ecmFileInfoDTO.getAppCode(), "业务类型不能为空");
                AssertUtils.isNull(ecmFileInfoDTO.getAppTypeName(), "业务类型名称不能为空");
                AssertUtils.isNull(ecmFileInfoDTO.getDocCode(), "资料类型不能为空");
                AssertUtils.isNull(ecmFileInfoDTO.getDocName(), "资料类型名称不能为空");
                AfmDetImgDetDTO dto = new AfmDetImgDetDTO();
                dto.setBusinessIndex(ecmFileInfoDTO.getBusiNo());
                dto.setBusinessTypeCode(ecmFileInfoDTO.getAppCode());
                dto.setBusinessTypeName(ecmFileInfoDTO.getAppTypeName());
                dto.setMaterialTypeCode(ecmFileInfoDTO.getDocCode());
                dto.setBusinessType(ecmFileInfoDTO.getAppCode() + "#_sunyard#"
                        + ecmFileInfoDTO.getAppTypeName());
                dto.setMaterialType(
                        ecmFileInfoDTO.getDocCode() + "#_sunyard#" + ecmFileInfoDTO.getDocName());
                dto.setMaterialTypeName(ecmFileInfoDTO.getDocName());
                dto.setYear(ecmFileInfoDTO.getCreateTime().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate().getYear());
                dto.setFileUrl(fileUrl);
                dto.setFileMd5(ecmFileInfoDTO.getFileMd5());
                dto.setFileName(ecmFileInfoDTO.getNewFileName());
                dto.setUploadUserCode(ecmFileInfoDTO.getCreateUser());
                dto.setUploadUserName(ecmFileInfoDTO.getCreateUserName());
                dto.setSourceSys(IcmsConstants.ECM);
                dto.setInvoiceType(IcmsConstants.AFM_TYPE);
                dto.setFileIndex(ecmFileInfoDTO.getFileId().toString());
                dto.setFileToken("");
                dto.setFileText(ecmFileInfoDTO.getTextAll());

                HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
                //范围
                handleAfmQuery( ecmFileInfoDTO, objectObjectHashMap, dto);
                dto.setQueryExpr(JSONObject.toJSONString(objectObjectHashMap));
                Map map = new HashMap();
                map.put("docCode", ecmFileInfoDTO.getDocCode());
                map.put("appCode", ecmFileInfoDTO.getAppCode());
                map.put("appName", ecmFileInfoDTO.getAppTypeName());
                map.put("docName", ecmFileInfoDTO.getDocName());
                map.put("busiNo", ecmFileInfoDTO.getBusiNo());
                map.put("fileName", ecmFileInfoDTO.getNewFileName());
                map.put("year", dto.getYear());
                dto.setFileExif(JSONObject.toJSONString(map));
                log.info("ecmFileInfoDTO.isTextDet:{}",ecmFileInfoDTO.isTextDet());
                Result result = null;
                if (ecmFileInfoDTO.isTextDet()){
                    //文本查重
                    result = antiFraudDetApi.saveFeatureByTextNow(dto);
                }else {
                    result = antiFraudDetApi.antiFraudDetNow(dto);
                }
                log.info(ecmFileInfoDTO.getFileId() + "智能化处理查重结果：{}", JSONUtil.toJsonStr(result));
                //处理返回结果
                taskType =  handleAfmResult(result,taskType,ecmFileInfoDTO.isTextDet()? IcmsConstants.TYPE_TEN : IcmsConstants.FOUR);
            }
        } catch (Exception e) {
            log.error(ecmFileInfoDTO.getFileId() + ":查重异常", e);
            taskType = CheckDetectionUtils.updateStatus(taskType, ecmFileInfoDTO.isTextDet()? IcmsConstants.TYPE_TEN : IcmsConstants.FOUR,
                    EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
        }
        log.info("最终taskType:{}",taskType);
        return taskType;
    }

    private String handleAfmResult(Result result, String taskType ,Integer charType) {
        try {
            log.info("charType:{},当前tasktype:{}",charType,taskType);
            if (result != null && result.isSucc()) {
                Map<String, ?> data = (Map<String, ?>) result.getData();
                List<Map> similarityList = (List<Map>) data.get("similarityList");
                if (!CollectionUtils.isEmpty(similarityList)) {
                    //需要移除删除的文件
                    List<Long> collect1 = similarityList.stream()
                            .map(s -> Long.parseLong(s.get("fileIndex").toString()))
                            .collect(Collectors.toList());
                    Long l = ecmFileInfoMapper.selectCount(new LambdaQueryWrapper<EcmFileInfo>()
                            .in(EcmFileInfo::getFileId, collect1));
                    if (l > 0) {
                        taskType = CheckDetectionUtils.updateStatus(taskType, charType,
                                EcmCheckAsyncTaskEnum.CHECK_FAILED.description().charAt(0));
                    } else {
                        taskType = CheckDetectionUtils.updateStatus(taskType, charType,
                                EcmCheckAsyncTaskEnum.SUCCESS.description().charAt(0));
                    }
                } else {
                    taskType = CheckDetectionUtils.updateStatus(taskType, charType,
                            EcmCheckAsyncTaskEnum.SUCCESS.description().charAt(0));
                }
            } else {
                taskType = CheckDetectionUtils.updateStatus(taskType, charType,
                        EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
            }
        }catch (Exception e){
            log.error("查重状态修改异常：", e);
            throw  new RuntimeException("查重状态修改异常:"+e.getMessage());
        }
        return taskType;
    }

    public void handleAfmQuery(EcmFileInfoDTO ecmFileInfoDTO,
                                HashMap<Object, Object> objectObjectHashMap, AfmDetImgDetDTO dto) {
        //动态树
        List<EcmDocPlagChe> ecmDocPlagChes = ecmDocPlagCheMapper.selectList(
                new LambdaQueryWrapper<EcmDocPlagChe>()
                        .eq(EcmDocPlagChe::getDocCode, ecmFileInfoDTO.getDocCode()));
        if (!CollectionUtils.isEmpty(ecmDocPlagChes)) {
            //独立配置
            EcmDocPlagChe ecmDocPlagChe = ecmDocPlagChes.get(0);
            handleDocAfmPZ(ecmFileInfoDTO, ecmDocPlagChe.getFileSimilarity(),
                    ecmDocPlagChe.getFrameYear(), ecmDocPlagChe.getQueryType(), ecmDocPlagChes,
                    objectObjectHashMap, dto);
        } else {
            //全局配置
            List<EcmDocPlagChe> ecmDocPlagChesG = ecmDocPlagCheMapper
                    .selectList(new LambdaQueryWrapper<EcmDocPlagChe>()
                            .isNull(EcmDocPlagChe::getDocCode)
                            .isNull(EcmDocPlagChe::getRelDocCode));
            if (!CollectionUtils.isEmpty(ecmDocPlagChesG)) {
                EcmDocPlagChe ecmDocPlagChe = ecmDocPlagChesG.get(0);
                List<EcmDocPlagChe> ecmDocPlagChesList = new ArrayList<>();
                //判断如果queryType类型等于选中资料节点
                if (ecmDocPlagChe.getQueryType().equals(IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ADD)) {
                    ecmDocPlagChesList = ecmDocPlagCheMapper.selectList(new LambdaQueryWrapper<EcmDocPlagChe>()
                            .eq(EcmDocPlagChe::getQueryType,
                                    IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ADD)
                            .isNull(EcmDocPlagChe::getDocCode)
                            .isNotNull(EcmDocPlagChe::getRelDocCode));
                }
                handleDocAfmPZ(ecmFileInfoDTO, ecmDocPlagChe.getFileSimilarity(),
                        ecmDocPlagChe.getFrameYear(), ecmDocPlagChe.getQueryType(), ecmDocPlagChesList,
                        objectObjectHashMap, dto);
            }
        }
    }

    private static void handleDocAfmPZ(EcmFileInfoDTO ecmFileInfoDTO, Double fileSimilarity,
                                       Integer frameYear, Integer queryType,
                                       List<EcmDocPlagChe> ecmDocPlagChes,
                                       HashMap<Object, Object> objectObjectHashMap,
                                       AfmDetImgDetDTO dto) {

        List<Integer> lastNYears = CommonUtils.getLastNYears(frameYear);
        objectObjectHashMap.put("year", lastNYears);
        dto.setFileSimilarity(fileSimilarity / 100);
        if (IcmsConstants.GLOBAL_PLAG_CHE_QUERY_CURR.equals(queryType)) {
            //匹配当前资料节点
            ArrayList<String> strings = new ArrayList<>();
            strings.add(ecmFileInfoDTO.getDocCode());
            objectObjectHashMap.put("docCode", strings);
        } else if (IcmsConstants.GLOBAL_PLAG_CHE_QUERY_ADD.equals(queryType)) {
            //匹配指定资料节点
            ArrayList<String> strings = new ArrayList<>();
            strings.addAll(ecmDocPlagChes.stream().map(EcmDocPlagChe::getRelDocCode)
                    .collect(Collectors.toList()));
            if (!CollectionUtils.isEmpty(strings)) {
                objectObjectHashMap.put("docCode", strings);
            }
        }
    }

    /**
     * 文档识别
     */
    public String handleDocOcr(EcmFileInfoDTO ecmFileInfoDTO, AccountTokenExtendDTO token,
                               String taskType, SysStrategyDTO vo) {
        // 处理文档识别
        try {
            if (taskType.charAt(IcmsConstants.TYPE_ONE - 1) == EcmCheckAsyncTaskEnum.PROCESSING
                    .description().charAt(0)) {
                //使用ocr读取发票信息
                List<OcrResultDTO> ocrResultDTOS = getOcrResultDTOS(ecmFileInfoDTO, token);
                log.info(ecmFileInfoDTO.getFileId() + "文档识别OCR结果：{}", JSONUtil.toJsonStr(ocrResultDTOS));
                if (!CollectionUtils.isEmpty(ocrResultDTOS)) {
                    //自动识别
                    List<SysFileDTO> sysFileDTOS = new ArrayList<>();
                    autoGroupNew(ecmFileInfoDTO, token, vo, sysFileDTOS, ocrResultDTOS);
                }
                taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_ONE,
                        EcmCheckAsyncTaskEnum.SUCCESS.description().charAt(0));
            }
        } catch (Exception e) {
            log.error(ecmFileInfoDTO.getFileId() + ":文档识别异常", e);
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_ONE,
                    EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
        }
        return taskType;
    }

    private String getResponseString(CloseableHttpResponse response) throws IOException {
        if (response == null || response.getEntity() == null) {
            return null;
        }
        return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
    }
    
    /**
     * 执行 HTTP POST 请求
     * @param url         请求 URL
     * @param requestBody 请求体（JSON 格式）
     * @return 响应字符串
     * @throws IOException 如果请求失败
     */
    private String executePostRequest(String url, JSONObject requestBody) throws Exception {
        CloseableHttpClient httpClient = CommonHttpClientUtils.createHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8));
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return getResponseString(response);
        }
    }

    /**
     * 处理翻拍检测响应
     * @param responseString 响应字符串
     * @param taskType       任务类型
     * @return 更新后的任务类型
     */
    private String processRemakeResponse(String responseString, String taskType) {
        if (responseString != null && !responseString.trim().isEmpty()) {
            JSONObject jsonObject = JSONObject.parseObject(responseString);

            // 检查是否有 code 字段且 code 不为 200
            if (!jsonObject.containsKey("code")
                    || !ResultCode.SUCCESS.getCode().equals(jsonObject.getIntValue("code"))) {
                log.error("翻拍检测路径错误，code=" + jsonObject.getIntValue("code"));
                taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_SIX,
                        EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
            } else {
                // 检查 data 字段
                if (!jsonObject.containsKey("data")) {
                    taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_SIX,
                            EcmCheckAsyncTaskEnum.CHECK_FAILED.description().charAt(0));
                }

                JSONObject dataObject = jsonObject.getJSONObject("data");

                // 检查 class_ids 字段
                if (!dataObject.containsKey("class_ids")) {
                    taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_SIX,
                            EcmCheckAsyncTaskEnum.CHECK_FAILED.description().charAt(0));
                }

                int classIds = dataObject.getIntValue("class_ids");

                // 根据 class_ids 处理不同的状态
                if (classIds == IcmsConstants.ID_AUTHENTIC) {
                    taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_SIX,
                            EcmCheckAsyncTaskEnum.SUCCESS.description().charAt(0));
                } else if (classIds == IcmsConstants.ID_REMAKE) {
                    taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_SIX,
                            EcmCheckAsyncTaskEnum.CHECK_FAILED.description().charAt(0));
                } else {
                    taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_SIX,
                            EcmCheckAsyncTaskEnum.CHECK_FAILED.description().charAt(0));
                }
            }

        }
        return taskType;
    }

    /**
     * 处理图片转正响应
     * @param responseString 响应字符串
     * @param taskType       任务类型
     * @param ecmFileInfoDTO 文件实体类
     * @return 任务类型
     * @throws Exception
     */
    private String processRegularizeResponse(String responseString, String taskType,
                                             EcmFileInfoDTO ecmFileInfoDTO,
                                             AccountTokenExtendDTO token)
            throws Exception {
        if (responseString != null && !responseString.trim().isEmpty()) {
            JSONObject jsonObject = JSONObject.parseObject(responseString);
            if (jsonObject.getInteger("code") == 200) {
                String base64Data = jsonObject.getString("data");
                if (base64Data != null && base64Data.equals(IcmsConstants.NOT_DEAL)) {
                    taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_TWO,
                            EcmCheckAsyncTaskEnum.NOT_DEAL.description().charAt(0));
                } else {
                    byte[] bytes = Base64.getDecoder().decode(base64Data);
                    try {
                        //拿到上传后的id
                        Long id = uploadFile(bytes, ecmFileInfoDTO).getId();
                        String md5 = Md5Utils.calculateMD5(bytes);
                        //更新原文件id为oldFileId
                        ecmFileInfoDTO.setOldFileId(ecmFileInfoDTO.getFileId());
                        //更新文件id
                        ecmFileInfoDTO.setFileId(id);
                        //更新文件md5
                        ecmFileInfoDTO.setFileMd5(md5);
                        List<EcmFileInfoDTO> ecmFileInfoExtendsNew = new ArrayList<>();
                        ecmFileInfoExtendsNew.add(ecmFileInfoDTO);
                        RotateFileVO rotateFileVO = new RotateFileVO();
                        rotateFileVO.setEcmFileInfoExtendsNew(ecmFileInfoExtendsNew);
                        rotateFileVO.setBusiId(ecmFileInfoDTO.getBusiId());
                        rotateFileVO.setCreateUserName(ecmFileInfoDTO.getCreateUserName());
                        rotateFileVO.setRegularize(IcmsConstants.ONE);
                        AccountTokenExtendDTO accountTokenExtendDTO = new AccountTokenExtendDTO();
                        accountTokenExtendDTO.setUsername(ecmFileInfoDTO.getCreateUserName());
                        //插入文件信息
                        rotateInsertFileInfo(rotateFileVO, token);
                        taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_TWO,
                                EcmCheckAsyncTaskEnum.SUCCESS.description().charAt(0));
                    } catch (Exception e) {
                        log.error(ecmFileInfoDTO.getFileId() + ":处理转正异常", e);
                        taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_TWO,
                                EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
                    }
                }
            } else {
                taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_TWO,
                        EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
            }
        }
        return taskType;
    }

    /**
     * 处理模糊检测响应
     * @param responseString 响应字符串
     * @param taskType       任务类型
     * @return 更新后的任务类型
     */
    private String processFuzzyResponse(String responseString, String taskType) {
        if (responseString != null && !responseString.trim().isEmpty()) {
            JSONObject jsonObject = JSONObject.parseObject(responseString);
            Boolean isBlurry = jsonObject.getBoolean("data");
            //True表示模糊,则检测未通过
            taskType = isBlurry
                    ? CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_THREE,
                            EcmCheckAsyncTaskEnum.CHECK_FAILED.description().charAt(0))
                    : CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_THREE,
                            EcmCheckAsyncTaskEnum.SUCCESS.description().charAt(0));
        } else {
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_THREE,
                    EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
        }
        return taskType;
    }

    /**
     * 处理反光检测响应
     * @param responseString 响应字符串
     * @param taskType       任务类型
     * @return 更新后的任务类型
     */
    private String processReflectiveResponse(String responseString, String taskType) {
        if (responseString != null && !responseString.trim().isEmpty()) {
            JSONObject jsonObject = JSONObject.parseObject(responseString);
            Boolean isTrue = jsonObject.getBoolean("data");
            //True表示反光,则检测未通过
            taskType = isTrue
                    ? CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_EIGHT,
                    EcmCheckAsyncTaskEnum.CHECK_FAILED.description().charAt(0))
                    : CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_EIGHT,
                    EcmCheckAsyncTaskEnum.SUCCESS.description().charAt(0));
        } else {
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_EIGHT,
                    EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
        }
        return taskType;
    }

    /**
     * 处理缺角检测响应
     * @param responseString 响应字符串
     * @param taskType       任务类型
     * @return 更新后的任务类型
     */
    private String processMissCornerResponse(String responseString, String taskType) {
        if (responseString != null && !responseString.trim().isEmpty()) {
            JSONObject jsonObject = JSONObject.parseObject(responseString);
            Boolean isTrue = jsonObject.getBoolean("data");
            //True表示缺角,则检测未通过
            taskType = isTrue
                    ? CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_NINE,
                    EcmCheckAsyncTaskEnum.CHECK_FAILED.description().charAt(0))
                    : CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_NINE,
                    EcmCheckAsyncTaskEnum.SUCCESS.description().charAt(0));
        } else {
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_NINE,
                    EcmCheckAsyncTaskEnum.FAILED.description().charAt(0));
        }
        return taskType;
    }

    private SysFileApiDTO uploadFile(byte[] data, EcmFileInfoDTO ecmFileInfoDTO) {
        UploadListVO fileUploadDTO = new UploadListVO();
        fileUploadDTO.setFileByte(data);
        fileUploadDTO.setFileName(ecmFileInfoDTO.getOriginalFilename());
        EcmAppDef ecmAppDef=ecmAppDefMapper.selectById(ecmFileInfoDTO.getAppCode());
        fileUploadDTO.setStEquipmentId(ecmAppDef.getEquipmentId());
        fileUploadDTO.setUserId(0L);
        fileUploadDTO.setFileSource("OpenApi");
        fileUploadDTO.setMd5(ecmFileInfoDTO.getFileMd5());
        fileUploadDTO.setFileName(ecmFileInfoDTO.getNewFileName());
        //是否加密
        fileUploadDTO.setIsEncrypt(
                ecmFileInfoDTO.getIsEncrypt() == null ? 0 : ecmFileInfoDTO.getIsEncrypt());
        Result result = fileStorageApi.upload(fileUploadDTO);
        System.out.println("打印上传结果：" + JSONObject.toJSON(result));
        if (result.isSucc() && result.getData() != null) {
            SysFileDTO FileData = (SysFileDTO) result.getData();
            SysFileApiDTO sysFileApiDTO = new SysFileApiDTO();
            BeanUtil.copyProperties(FileData, sysFileApiDTO);
            return sysFileApiDTO;
        }
        return null;
    }

    /**
     * 自动归类递归筛选树,查找符合条件的资料
     */
    private List<EcmBusiDocRedisDTO> filterDocsRecursively(List<EcmBusiDocRedisDTO> docs,
                                                           String classIds,Map<String, EcmDocDef> docDefMap) {
        List<EcmBusiDocRedisDTO> result = new ArrayList<>();
        for (EcmBusiDocRedisDTO doc : docs) {
            // 递归获取符合条件的节点
            if (isValid(doc, classIds,docDefMap)) {
                result.add(doc); // 如果当前节点符合条件，则添加到结果列表中
            }

            // 递归查找该节点的子节点
            if (doc.getChildren() != null && !doc.getChildren().isEmpty()) {
                result.addAll(filterDocsRecursively(doc.getChildren(), classIds,docDefMap)); // 递归添加符合条件的子节点
            }
        }
        return result;
    }

    private boolean isValid(
            EcmBusiDocRedisDTO doc,
            String classIds,
            Map<String, EcmDocDef> docDefMap) { // 新增：传入缓存的Map
        // 直接从Map获取
        EcmDocDef ecmDocDef = docDefMap.get(doc.getDocCode());
        // 原有校验逻辑不变
        return ecmDocDef != null
                && ecmDocDef.getIsAutoClassified() != null
                && ecmDocDef.getIsAutoClassified() == 1
                && ecmDocDef.getAutoClassificationId() != null
                && Arrays.stream(ecmDocDef.getAutoClassificationId().split(","))
                .anyMatch(id -> id.equals(classIds));
    }

    /**
     * 重智能检测
     */
    @Transactional(rollbackFor = Exception.class)
    public void intelligentDetectionAgain(EcmIntelligentDetectionAgainDTO retryDTO,
                                          AccountTokenExtendDTO token) {
        AssertUtils.isNull(retryDTO.getBusiId(), "业务ID不能为空");
        AssertUtils.isNull(retryDTO.getFileId(), "文件ID不能为空");
        //查询对应资料开关
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, retryDTO.getBusiId());
        Map<Integer, Boolean> result = taskSwitchService.queryAllSwitches(retryDTO.getDocCode());
        if (ObjectUtils.isEmpty(result)) {
            log.info("重归类后未查到智能检测配置记录");
            return;
        }
        //文档识别开关
        SysStrategyDTO vo = isAutoGroup(retryDTO.getAppCode());
        // 从Redis获取任务信息
        EcmAsyncTask task = busiCacheService.getEcmAsyncTask(
                RedisConstants.BUSIASYNC_TASK_PREFIX + retryDTO.getBusiId(),
                retryDTO.getFileId().toString());
        if (task == null) {
            task = new EcmAsyncTask();
            task.setFileId(retryDTO.getFileId());
            task.setBusiId(retryDTO.getBusiId());
            task.setTaskType(IcmsConstants.ASYNC_TASK_STATUS_INIT);
            task.setCreateTime(new Date());
            asyncTaskService.insert(task);
        }
        String taskType = task.getTaskType();
        //根据开关跟传来的需要处理的类型判断
        if (retryDTO.getTypes().contains(IcmsConstants.TYPE_SIX)
                && result.get(IcmsConstants.REMAKE)) {
            //翻拍检测
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_SIX,
                    EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            task.setTaskType(taskType);
        }
        if (retryDTO.getTypes().contains(IcmsConstants.TYPE_TWO)
                && result.get(IcmsConstants.REGULARIZE)) {
            //转正检测
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_TWO,
                    EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            task.setTaskType(taskType);
        }
        if (retryDTO.getTypes().contains(IcmsConstants.TYPE_THREE)
                && result.get(IcmsConstants.OBSCURE)) {
            //模糊检测
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_THREE,
                    EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            task.setTaskType(taskType);
        }
        if (retryDTO.getTypes().contains(IcmsConstants.TYPE_EIGHT)
                && result.get(IcmsConstants.REFLECTIVE)) {
            //反光检测
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_EIGHT,
                    EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            task.setTaskType(taskType);
        }
        if (retryDTO.getTypes().contains(IcmsConstants.TYPE_NINE)
                && result.get(IcmsConstants.MISS_CORNER)) {
            //缺角检测
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_NINE,
                    EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            task.setTaskType(taskType);
        }
        if (retryDTO.getTypes().contains(IcmsConstants.TYPE_FOUR)
                && result.get(IcmsConstants.PLAGIARISM)) {
            //查重检测
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_FOUR,
                    EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            task.setTaskType(taskType);
        }
        if (retryDTO.getTypes().contains(IcmsConstants.TYPE_TEN)
                && result.get(IcmsConstants.PLAGIARISM_TEXT)) {
            //文本查重
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_TEN,
                    EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            task.setTaskType(taskType);
        }
        if (retryDTO.getTypes().contains(IcmsConstants.TYPE_ONE) && vo.getOcrConfigStatus()) {
            //文档识别检测
            taskType = CheckDetectionUtils.updateStatus(taskType, IcmsConstants.TYPE_ONE,
                    EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0));
            task.setTaskType(taskType);
        }
        //走MQ处理
        asyncTaskService.updateEcmAsyncTask(task);
        EcmFileInfoDTO ecmFileInfoDTO=busiCacheService.getFileInfoRedisSingle(task.getBusiId(),task.getFileId());
        checkDetectionService.checkDetectionByMq(ecmFileInfoDTO,task,task.getTaskType());

    }

    public EcmFileInfoDTO checkAutoGroupWithTimeout(EcmFileInfoDTO ecmFileInfoDTO, AccountTokenExtendDTO token,EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<EcmFileInfoDTO> task = () -> checkAutoGroup(ecmFileInfoDTO, token,ecmBusiInfoRedisDTO);

        Future<EcmFileInfoDTO> future = executor.submit(task);
        try {
            // 等待最多2秒，超时的话设置为未归类
            return future.get(sunEcmProperties.getCheckGroupTimeOut(), TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // 处理超时异常
            future.cancel(true); // 取消任务
            // 设置为未归类
            ecmFileInfoDTO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
            ecmFileInfoDTO.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
            ecmFileInfoDTO.setComment("");
            return ecmFileInfoDTO;
        } catch (InterruptedException | ExecutionException e) {
            // 处理其他异常
            Thread.currentThread().interrupt(); // 恢复中断状态
            future.cancel(true); // 取消任务
            // 设置为未归类
            ecmFileInfoDTO.setDocId(IcmsConstants.UNCLASSIFIED_ID);
            ecmFileInfoDTO.setDocCode(IcmsConstants.UNCLASSIFIED_ID);
            ecmFileInfoDTO.setComment("");
            return ecmFileInfoDTO;
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    // 强制关闭线程池
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

    public Result splitPdfToImage(SplitFileVO vo, AccountTokenExtendDTO token) {
        HashMap<String, Object> splitPdfImageMap = new HashMap<>();
        try {
            //校验
            AssertUtils.isNull(vo.getBusiId(), "业务主键id不能为空");
            AssertUtils.isNull(vo.getEcmFileInfoDTO(), "被拆分的文件信息不能为空");
            AssertUtils.isNull(vo.getAppTypeName(), "业务类型名称不能为空");
            AssertUtils.isNull(vo.getBusiNo(), "业务编号不能为空");
            AssertUtils.isNull(vo.getEcmFileInfoDTO().getSize(), "业务编号不能为空");
            AssertUtils.isNull(vo.getSplitPageNum(), "pdf文件页数不能为空");
            AssertUtils.isNull(vo.getSplitPageSize(), "pdf文件每页大小不能为空");

            FileSplitPdfVO fileSplitPdfVO = new FileSplitPdfVO();
            fileSplitPdfVO.setNewFileId(vo.getEcmFileInfoDTO().getNewFileId().toString());
            fileSplitPdfVO.setBusiBatchNo(vo.getBusiBatchNo());
            fileSplitPdfVO.setFilename(vo.getNewFileName());
            fileSplitPdfVO.setIsEncrypt(vo.getIsEncrypt());
            fileSplitPdfVO.setSplitPageNum(vo.getSplitPageNum());
            fileSplitPdfVO.setSplitPageSize(vo.getSplitPageSize());
            fileSplitPdfVO.setToken(JSONObject.toJSONString(token));
            fileSplitPdfVO.setFileTooLarge(calculateFileSizeToM(vo.getEcmFileInfoDTO().getNewFileSize()));
            fileSplitPdfVO.setFileMd5(vo.getEcmFileInfoDTO().getFileMd5());
            splitPdfImageMap = (HashMap<String, Object>)fileStorageApi.splitPdfFile(fileSplitPdfVO).getData();
            if (CollectionUtils.isEmpty(splitPdfImageMap)){
                return Result.error("pdf拆分异常",ResultCode.SYSTEM_ERROR);
            }
            boolean flag = (boolean) splitPdfImageMap.get("flag");
            if (!flag){
                return Result.error("当前pdf正在加载中，请稍后再试",ResultCode.SYSTEM_ERROR);
            }
            ArrayList<List<EcmFileCommentVO>> imagesCommentList = getAllPdfToImagesComment(vo, token);
            splitPdfImageMap.put("comment",imagesCommentList);
        }catch (Exception e){
            log.error("pdf文件拆分异常:{}", e.getMessage(), e);
            throw new SunyardException(ResultCode.SYSTEM_ERROR, e.toString());
        }
//        return Result.success(getResultData(splitPdfImageMap,imagesCommentList));
        return Result.success(splitPdfImageMap);
    }

    private Object getResultData(HashMap<String, Object> splitPdfImageMap, ArrayList<List<EcmFileCommentVO>> imagesCommentList) {
        List<String> splitPdfImageList = (List<String>)splitPdfImageMap.get("list");
        HashMap<String, Object> returnMap = new HashMap<>();
        ArrayList<Map<String, Object>> listMap = new ArrayList<>();
        for (int i = 0; i < splitPdfImageList.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("image", splitPdfImageList.get(i));
            map.put("comment", imagesCommentList.get(i));
            listMap.add(map);
        }
        returnMap.put("images", listMap);
        returnMap.put("total", splitPdfImageMap.get("total"));
        return returnMap;
    }

    /**
     * 获取注解
     * @param vo
     * @param token
     * @return
     */
    private ArrayList<List<EcmFileCommentVO>> getAllPdfToImagesComment(SplitFileVO vo, AccountTokenExtendDTO token) {
        ArrayList<List<EcmFileCommentVO>> commentList = new ArrayList<>();
        int startPage = vo.getSplitPageSize() * vo.getSplitPageNum();
        for (int i = startPage; i < startPage+vo.getSplitPageSize(); i++) {
            EcmCommentVO commentVO = new EcmCommentVO();
            commentVO.setBusiId(vo.getBusiId());
            commentVO.setFileId(vo.getEcmFileInfoDTO().getFileId());
            commentVO.setNewFileId(vo.getEcmFileInfoDTO().getNewFileId());
            commentVO.setNewFileName(vo.getNewFileName());
            commentVO.setFilePage(i);
            commentVO.setIsPdf(true);
            List<EcmFileCommentVO> comment = fileCommentService.getComment(commentVO, token);
            commentList.add(comment);
        }
        return commentList;
    }

    public void getTaskType(EcmFileInfoDTO ecmFileInfoDTO, EcmAsyncTask ecmAsyncTask) {
        List<String> enumConfigList = sysStrategyService.queryEcmEnumConfig();
        SysStrategyDTO vo = isAutoGroup(ecmFileInfoDTO.getAppCode());
        Map<Integer, Boolean> result = taskSwitchService.queryAllSwitches(ecmFileInfoDTO.getDocCode());
        if (IMGS.contains(ecmFileInfoDTO.getFormat())) {
            String taskType = CheckDetectionUtils.getTaskType(result, vo, enumConfigList);
            log.info("当前的format：{},taskType:{}",ecmFileInfoDTO.getFormat(),taskType);
            ecmAsyncTask.setTaskType(taskType);
        } else if (DOCS.contains(ecmFileInfoDTO.getFormat())){
            String taskType = getAfmTaskType(result, enumConfigList);
            ecmAsyncTask.setTaskType(taskType);
        }
    }

    public void processAfmData(EcmFileInfoDTO ecmFileInfoDTO, EcmAsyncTask ecmAsyncTask, String taskType){
        checkDetectionService.checkDetectionByMq(ecmFileInfoDTO,ecmAsyncTask,taskType);
    }

    public String updateTaskStatus(String status, Integer position, char newValue){
        return CheckDetectionUtils.updateStatus(status, position, newValue);
    }

    /**
     *
     * @param busiId
     * @param fileIds
     * @param type
     */
    @Async("GlobalThreadPool")
    public void changeAfmData(Long busiId, String busiNo , List<Long> fileIds,Integer type){
        AssertUtils.isTrue(ObjectUtils.isEmpty(type),"数据同步类型不能为空");
        if (IcmsConstants.ONE.equals(type)){
            AssertUtils.isTrue(ObjectUtils.isEmpty(busiId),"afm数据修改busiId不能为空");
            AssertUtils.isTrue(ObjectUtils.isEmpty(busiNo),"afm数据修改busiNo不能为空");
            //获取所有文件id
            fileIds = busiCacheService.getAllFileIds(busiId);
        }
        //判断文件是否查重过 筛选出查重过的数据
        List<String> filterFileIds = busiCacheService.filterExistingFileIds(fileIds, busiId);
        List<String> requestFileIds = filterFileIds.stream()
                .map(String::valueOf)
                .map(id -> IcmsConstants.ECM + "_" + id)
                .collect(Collectors.toList());
        //封装请求对象
        AfmDetUpdateDto updateDto = new AfmDetUpdateDto();
        updateDto.setBusinessIndex(busiNo);
        updateDto.setType(type);
        updateDto.setFileIds(requestFileIds);
        antiFraudDetApi.ecmToAfmDataSync(updateDto);
    }

    public boolean addEsFileInfoSync(EcmFileInfoDTO ecmFileInfoDTO,Long userId){
        return operateFullQueryService.addEsFileInfoSync(ecmFileInfoDTO,userId);
    }

    public String getFileContext(EcmFileInfoDTO ecmFileInfoDTO){
        return operateFullQueryService.getFileContent(ecmFileInfoDTO);
    }


    public int queryCountByBusiIdAndState(Long busiId,  int state) {
        QueryWrapper<EcmFileInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("busi_id", busiId);
        wrapper.eq("state", state);
        long count = ecmFileInfoMapper.selectCount(wrapper);
        return (int)count;
    }

    /**
     *  其他类型的文件进行重新智能化检测
     */
    @Transactional(rollbackFor = Exception.class)
    public void intelligentDetectionAgainByTextDup(EcmIntelligentDetectionAgainDTO retryDTO, AccountTokenExtendDTO token) {
        Map<Integer, Boolean> result = taskSwitchService.queryAllSwitches(retryDTO.getDocCode());
        List<String> enumConfigList = sysStrategyService.queryEcmEnumConfig();
        String taskType = getAfmTaskType(result ,enumConfigList);
        //开启了文本查重
        if (!IcmsConstants.ASYNC_TASK_STATUS_INIT.equals(taskType)) {
            EcmAsyncTask task = busiCacheService.getEcmAsyncTask(
                    RedisConstants.BUSIASYNC_TASK_PREFIX + retryDTO.getBusiId(),
                    retryDTO.getFileId().toString());
            if (task == null) {
                task = new EcmAsyncTask();
                task.setFileId(retryDTO.getFileId());
                task.setBusiId(retryDTO.getBusiId());
                task.setCreateTime(new Date());
                task.setTaskType(IcmsConstants.ASYNC_TASK_STATUS_INIT);
                asyncTaskService.insert(task);
            }
            task.setTaskType(taskType);
            asyncTaskService.updateEcmAsyncTask(task);
            EcmFileInfoDTO ecmFileInfoDTO=busiCacheService.getFileInfoRedisSingle(task.getBusiId(),task.getFileId());
            final EcmAsyncTask finalTask = task;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 事务已提交，此时发 MQ 安全
                    checkDetectionService.checkDetectionByMq(ecmFileInfoDTO,finalTask,finalTask.getTaskType());
                }
            });
        }
    }
}
