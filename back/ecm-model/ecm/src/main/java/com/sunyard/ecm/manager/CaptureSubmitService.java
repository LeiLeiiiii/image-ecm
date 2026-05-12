package com.sunyard.ecm.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sunyard.ecm.annotation.WebsocketNoticeAnnotation;
import com.sunyard.ecm.config.MqConfig;
import com.sunyard.ecm.constant.BusiInfoConstants;
import com.sunyard.ecm.constant.BusiLogConstants;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.RedisConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmAppDefDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmDocrightDefDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmMobileParamsDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.dto.redis.UserBusiRedisDTO;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmAIBridgeSyncMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmBusiMetadataMapper;
import com.sunyard.ecm.mapper.EcmBusiVersionMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.mapper.SysBusiLogMapper;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmAIBridgeSync;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmBusiLog;
import com.sunyard.ecm.po.EcmBusiMetadata;
import com.sunyard.ecm.po.EcmBusiVersion;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.service.OperateFullQueryService;
import com.sunyard.ecm.vo.AppTypeBusiVO;
import com.sunyard.ecm.vo.EcmCommentVO;
import com.sunyard.ecm.vo.EcmSubmitVO;
import com.sunyard.framework.common.constant.LoginEncryptionConstant;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.UUIDUtils;
import com.sunyard.framework.common.util.encryption.RsaUtils;
import com.sunyard.framework.common.util.encryption.Sm2Util;
import com.sunyard.framework.mq.util.MqProducerUtils;
import com.sunyard.framework.redis.constant.TimeOutConstants;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysParamDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author： ty
 * @since： 2023/5/24 14:27
 * @desc 影像采集提交实现类
 */
@Slf4j
@Service
public class CaptureSubmitService {
    @Value("${spring.rabbitmq.addresses:}")
    private String addresses;
    @Value("${spring.rabbitmq.host:}")
    private String host;
    @Value("${spring.rabbitmq.port:5672}")
    private Integer port;
    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;
    @Value("${queues.timeout:43200000}")
    private String queuesTimeout;
    @Resource
    private RedisUtils redisUtil;
    @Resource
    private MqConfig mqConfig;
    @Resource
    private EcmBusiVersionMapper ecmBusiVersionMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Resource
    private EcmAppAttrMapper ecmAppAttrMapper;
    @Resource
    private EcmBusiMetadataMapper ecmBusiMetadataMapper;
    @Resource
    private SysBusiLogMapper busiLogMapper;
    @Resource
    private ParamApi paramApi;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private OperateFullQueryService operateFullQueryService;
    @Resource
    private BusiOperationService busiOperationService;
    @Resource
    private EcmAIBridgeSyncMapper ecmAIBridgeSyncMapper;
    @Resource
    private UserApi userApi;

    /**
     * 影像提交
     */
    @WebsocketNoticeAnnotation(msgType = "all")
    @Transactional(rollbackFor = Exception.class)
    public void submit(AccountTokenExtendDTO userId, EcmSubmitVO ecmSubmitVO) {
        // 参数校验
        AssertUtils.isNull(ecmSubmitVO.getDelegateType(), "流程类型ID不能为空");
        AssertUtils.isNull(ecmSubmitVO.getDelegateTypeName(), "流程类型NAME不能为空");
        AssertUtils.isNull(ecmSubmitVO.getTypeBig(), "业务类型ID不能为空");
        AssertUtils.isNull(ecmSubmitVO.getTypeBigName(), "业务类型NAME不能为空");

        List<Long> busiIdList = ecmSubmitVO.getBusiIdList();
        //页面变动表示，用来判断是否添加历史轨迹记录
        Integer changeFlag = ecmSubmitVO.getChangeFlag();

        // 获取用户ID进行AI桥接校验
        String userCode = getUserCode(userId);
        log.info("当前的userCode : {}", userCode);
        // 校验用户是否在AI桥接表中存在（放在最前面）
        checkAIBridgeUser(userCode);

        Map<Long, List<EcmDocrightDefDTO>> docRightList = null;
        UserBusiRedisDTO userBusiRedisDTO = busiCacheService.getUserPageRedis(userId.getFlagId(), userId);
        if (userBusiRedisDTO != null) {
            if (!ObjectUtils.isEmpty(userBusiRedisDTO.getBusiId())) {
                busiIdList = userBusiRedisDTO.getBusiId();
                //查询有效业务
                LambdaQueryWrapper<EcmBusiInfo> queryWrapper = new LambdaQueryWrapper<>();
                List<EcmBusiInfo> ecmBusiInfos = ecmBusiInfoMapper.selectList(queryWrapper.in(EcmBusiInfo::getBusiId, busiIdList));
                busiIdList = ecmBusiInfos.stream().map(EcmBusiInfo::getBusiId).collect(Collectors.toList());
            }
            docRightList = userBusiRedisDTO.getDocRightList();
        }
        AssertUtils.isNull(busiIdList, "无法提交，业务信息不存在");
        AssertUtils.isNull(userId, "参数错误");
        for (Long busiId : busiIdList) {
            List<EcmDocrightDefDTO> ecmDocrightDefDTOS = ObjectUtils.isEmpty(docRightList) ? new ArrayList<>() : docRightList.get(busiId);

            checkSingleBusi(userId, busiId, ecmDocrightDefDTOS, changeFlag, ecmSubmitVO);
        }

    }

    /**
     * 获取用户ID
     * 对内接口：直接从token.getId()获取
     * 对外接口：通过userApi.getUserDetail获取
     *
     * @param token 用户token
     * @return 用户ID
     */
    private String getUserCode(AccountTokenExtendDTO token) {
        // 对内接口：token.getId()有值
        if (token.getId() != null) {
            return token.getUsername().toString();
        }
        // 对外接口：通过userApi.getUserDetail获取
        Result<SysUserDTO> result = userApi.getUserDetail(token.getUsername());
        if (result.isSucc() && result.getData() != null) {
            return result.getData().getLoginName().toString();
        }
        return null;
    }

    /**
     * 校验AI桥接用户是否存在
     *
     * @param userCode 用户ID
     */
    private void checkAIBridgeUser(String userCode) {
        if (StrUtil.isNotBlank(userCode)) {
            Long count = ecmAIBridgeSyncMapper.selectCount(
                new LambdaQueryWrapper<EcmAIBridgeSync>()
                    .eq(EcmAIBridgeSync::getUserShowId, userCode)
            );
            AssertUtils.isTrue(count == 0, "当前用户在提交校验表中不存在，请联系管理员");
        }
    }

    /**
     * 提交业务
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkSingleBusi(AccountTokenExtendDTO token, Long busiId, List<EcmDocrightDefDTO> ecmDocrightDefDTOS, Integer changeFlag, EcmSubmitVO ecmSubmitVO) {
        String userId = token.getUsername();
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, busiId);
        List<FileInfoRedisDTO> fileInfos = busiCacheService.getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
        //校验业务是否有上传文件
        if (CollectionUtil.isEmpty(fileInfos)) {
            AssertUtils.isTrue(CollectionUtil.isEmpty(fileInfos), "存在未导入文件的业务，无法提交！");
        }
        //校验自动生成的业务编号是否修改，校验必填属性是否有值
        checkBusiInfo(ecmBusiInfoRedisDTO);
        List<FileInfoRedisDTO> collect = fileInfos.stream().filter(p -> IcmsConstants.ZERO.equals(p.getState())).collect(Collectors.toList());
        collect.forEach(f -> f.setSignFlag(IcmsConstants.ZERO));
        List<EcmDocrightDefDTO> docRightList = ecmDocrightDefDTOS;
        Map<String, Integer> maxMap = new HashMap<>();
        Map<String, Integer> minMap = new HashMap<>();
        if (!token.isOut()) {
            if (CollectionUtils.isEmpty(docRightList)) {
                docRightList = new ArrayList<>();
//                // 静态树从数据库查
//                if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType())) {
//                    docRightList = ecmBusiInfoRedisDTO.getDocRightList();
//                } else {
//                    docRightList = ecmBusiInfoRedisDTO.getDocRightList();
//                }
            } else {

            }
        }
        //文件最大最小限制
        Map<String, List<EcmDocrightDefDTO>> docRightMap = docRightList.stream()
                .collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));

        List<String> collect1 = docRightList.stream().map(EcmDocrightDefDTO::getDocCode).collect(Collectors.toList());
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectBatchIds(collect1);
        Map<String, List<EcmDocDef>> docDefMap = ecmDocDefs.stream().collect(Collectors.groupingBy(EcmDocDef::getDocCode));
        for (String docCode : docRightMap.keySet()) {
            // 取分组后每个docCode对应的第一个EcmDocrightDefDTO对象
            EcmDocrightDefDTO docrightDef = docRightMap.get(docCode).get(0);
            //判断是否启用权限配置
            if(IcmsConstants.ONE.toString().equals(docrightDef.getEnableLenLimit())) {
                maxMap.put(docCode, docrightDef.getMaxLen());
                minMap.put(docCode, docrightDef.getMinLen());
            }else{
                maxMap.put(docCode, docDefMap.get(docCode).get(0).getMaxFiles());
                minMap.put(docCode, docDefMap.get(docCode).get(0).getMinFiles());
            }
        }
        //分组
        Map<String, List<FileInfoRedisDTO>> fileGroupedByDocType = collect.stream().filter(p -> !ObjectUtils.isEmpty(p.getDocCode())).collect(Collectors.groupingBy(EcmFileInfoDTO::getDocCode));
        Map<String, List<EcmDocrightDefDTO>> docRightGroupedByDocType = docRightList.stream().collect(Collectors.groupingBy(EcmDocrightDefDTO::getDocCode));
        //提交校验
        //当资料节点下没文件时fileGroupedByDocType为空不能判断最小上传数
        docRightGroupedByDocType.forEach((docCode, docright) -> {
            EcmDocrightDefDTO ecmDocrightDefDTO = docright.get(0);
            List<FileInfoRedisDTO> fileInfoRedisDTOS = fileGroupedByDocType.get(docCode);
            //校验上传数量
            Integer maxLen = maxMap.get(docCode);
            Integer minLen = minMap.get(docCode);
            checkFileCount(fileInfoRedisDTOS, ecmDocrightDefDTO, maxLen, minLen);
        });
        //校验通过，持久化数据库添加影像业务提交节点记录
        EcmBusiVersion ecmBusiVersion = insertEcmBusiVersion(userId, busiId);
        if (StateConstants.CHANG_FLAG_TRUE.equals(changeFlag)) {
            //更新缓存，添加业务轨迹记录
            if (CollectionUtils.isEmpty(ecmBusiInfoRedisDTO.getEcmBusiVersions())) {
                List<EcmBusiVersion> ecmBusiVersions = new ArrayList<>();
                ecmBusiVersions.add(ecmBusiVersion);
                ecmBusiInfoRedisDTO.setEcmBusiVersions(ecmBusiVersions);
            } else {
                List<EcmBusiVersion> ecmBusiVersions = ecmBusiInfoRedisDTO.getEcmBusiVersions();
                ecmBusiVersions.add(ecmBusiVersion);
                ecmBusiInfoRedisDTO.setEcmBusiVersions(ecmBusiVersions);
            }
        }
        //更新db 业务状态
        LambdaUpdateWrapper<EcmBusiInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(EcmBusiInfo::getBusiId,busiId);
        wrapper.set(EcmBusiInfo::getStatus,BusiInfoConstants.BUSI_STATUS_ONE);
        // 保存AI桥接新增字段
        if (ecmSubmitVO != null) {
            if (StrUtil.isNotBlank(ecmSubmitVO.getDelegateType())) {
                wrapper.set(EcmBusiInfo::getDelegateType, ecmSubmitVO.getDelegateType());
            }
            if (StrUtil.isNotBlank(ecmSubmitVO.getDelegateTypeName())) {
                wrapper.set(EcmBusiInfo::getDelegateTypeName, ecmSubmitVO.getDelegateTypeName());
            }
            if (StrUtil.isNotBlank(ecmSubmitVO.getTypeBig())) {
                wrapper.set(EcmBusiInfo::getTypeBig, ecmSubmitVO.getTypeBig());
            }
            if (StrUtil.isNotBlank(ecmSubmitVO.getTypeBigName())) {
                wrapper.set(EcmBusiInfo::getTypeBigName, ecmSubmitVO.getTypeBigName());
            }
        }
        ecmBusiInfoMapper.update(null,wrapper);
        //更新redis
        ecmBusiInfoRedisDTO.setStatus(BusiInfoConstants.BUSI_STATUS_ONE);
        busiCacheService.saveAndUpate(ecmBusiInfoRedisDTO);
        //添加操作记录表
        busiOperationService.addOperation(busiId, IcmsConstants.ADD_BUSI, token, "提交业务");
        //保存提交业务日志
        saveSubmitLog(token, busiId);
        //发送消息
        sendMQMessage(ecmBusiInfoRedisDTO);
    }

    /**
     * 保存提交业务日志
     */
    private void saveSubmitLog(AccountTokenExtendDTO token, Long busiId) {
        //获取业务信息实体
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                .getEcmBusiInfoRedisDTO(token, busiId);
        //获取业务类型实体
        LambdaQueryWrapper<EcmAppDef> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(EcmAppDef::getAppCode, ecmBusiInfoRedisDTO.getAppCode());
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectOne(queryWrapper1);
        EcmBusiLog ecmBusiLog = new EcmBusiLog();
        ecmBusiLog.setBusiNo(ecmBusiInfoRedisDTO.getBusiNo());
        ecmBusiLog.setAppName(ecmAppDef.getAppName());
        ecmBusiLog.setAppCode(ecmBusiInfoRedisDTO.getAppCode());
        ecmBusiLog.setOrgCode(ecmBusiInfoRedisDTO.getOrgCode());
        ecmBusiLog.setOperatorId(token.getUsername());
        ecmBusiLog.setOperator(token.getName());
        ecmBusiLog.setOperatorType(BusiLogConstants.OPERATION_TYPE_TWO);
        ecmBusiLog.setOperateContent("提交业务" + ":" + ecmBusiInfoRedisDTO.getBusiNo());
        busiLogMapper.insert(ecmBusiLog);
    }

    /**
     * 提交成功发送MQ消息
     */
    public void sendMQMessage(EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        LambdaQueryWrapper<EcmAppDef> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmAppDef::getAppCode, ecmBusiInfoRedisDTO.getAppCode());
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectOne(wrapper);
        String queueName = ecmAppDef.getQueueName();
        if (StrUtil.isNotBlank(queueName)) {
            mqConfig.setQueueName(queueName);
        }
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(username);
        factory.setPassword(password);
        try {
            Connection connection;
            if (StrUtil.isNotBlank(addresses)) {
                connection = factory.newConnection(Address.parseAddresses(addresses));
            } else {
                factory.setHost(host);
                factory.setPort(port);
                connection = factory.newConnection();
            }
            try (Channel channel = connection.createChannel()) {
                channel.queueDeclare(mqConfig.getQueueName(), true, false, false, null);
                channel.exchangeDeclare(mqConfig.getExchangeName(), BuiltinExchangeType.FANOUT);
                channel.queueBind(mqConfig.getQueueName(), mqConfig.getExchangeName(), "");
                AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                        .expiration(queuesTimeout)
                        .build();
                channel.basicPublish(mqConfig.getExchangeName(), "", properties,
                        ecmBusiInfoRedisDTO.getBusiId().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                log.info("队列名称：{}", mqConfig.getExchangeName());
            } finally {
                try {
                    connection.close();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            log.error("消息发送失败：{}", e.getMessage());
        }
    }

    /**
     * 业务信息校验
     */
    private void checkBusiInfo(EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        //1、校验是否存在自动生成的业务编号
        String appCode = ecmBusiInfoRedisDTO.getAppCode();
        Long busiId = ecmBusiInfoRedisDTO.getBusiId();
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectById(busiId);
        String busiNo = ecmBusiInfo.getBusiNo();
        AssertUtils.isTrue(busiNo.contains(IcmsConstants.AUTO_BUSI_PREFIX), "存在自动生成的业务，请核对业务编号");
        //2、校验业务必填属性是否有值
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(new LambdaQueryWrapper<EcmAppAttr>().eq(EcmAppAttr::getAppCode, appCode));
        List<EcmAppAttr> appAttrList = ecmAppAttrs.stream().filter(s -> IcmsConstants.ZERO.equals(s.getIsNull())).collect(Collectors.toList());
        for (EcmAppAttr attr : appAttrList) {
            //校验必填项属性是否有值
            LambdaQueryWrapper<EcmBusiMetadata> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EcmBusiMetadata::getAppAttrId, attr.getAppAttrId());
            wrapper.eq(EcmBusiMetadata::getBusiId, busiId);
            List<EcmBusiMetadata> ecmBusiMetadata = ecmBusiMetadataMapper.selectList(wrapper);
            AssertUtils.isTrue(CollectionUtil.isEmpty(ecmBusiMetadata), "存在业务必填属性没有值的属性，请修改");
        }
    }

    private EcmBusiVersion insertEcmBusiVersion(String userId, Long busiId) {
        //校验通过，添加影像业务提交节点记录
        EcmBusiVersion ecmBusiVersion = new EcmBusiVersion();
        ecmBusiVersion.setBusiId(busiId);
        ecmBusiVersion.setCreateUser(userId);
        ecmBusiVersion.setCreateTime(new Date());
        ecmBusiVersionMapper.insert(ecmBusiVersion);
        return ecmBusiVersion;
    }

    private void checkFileCount(List<FileInfoRedisDTO> fileList, EcmDocrightDefDTO docRightExtend, Integer maxLen, Integer minLen) {

        int size;
        if (CollectionUtils.isEmpty(fileList)) {
            size = 0;
        } else {
            size = fileList.size();
        }

        if (minLen != null) {
            AssertUtils.isTrue(size < minLen,
                    "提交失败，【" + docRightExtend.getDocName() + "】资料类型未满足最小上传数量【" + minLen + "】！");
        }
        if (maxLen != null) {
            AssertUtils.isTrue(size > maxLen,
                    "提交失败，【" + docRightExtend.getDocName() + "】资料类型超过最大上传数量【" + maxLen + "】！");
        }

    }

    /**
     * 返回业务列表
     */
    public List<EcmBusiInfoDTO> getBusiInfoListByAppTypeId(String appCode, Long busiId, String pageFlag, AccountTokenExtendDTO token) {
        List<EcmBusiInfoDTO> ecmBusiInfoListResult = new ArrayList<>();
        //新增时获取当前页面业务类型对应业务
        List<Long> busiIds = new ArrayList<>();
        if (StrUtil.isNotBlank(pageFlag)) {
            UserBusiRedisDTO userBusiRedisDTO = busiCacheService.getUserPageRedis(pageFlag, token);
            if (userBusiRedisDTO != null) {
                if (CollectionUtil.isNotEmpty(userBusiRedisDTO.getRelation())) {
                    List<AppTypeBusiVO> relation = userBusiRedisDTO.getRelation();
                    List<AppTypeBusiVO> relationAppTypeIdList = relation.stream().filter(f -> f.getAppCode().equals(appCode)).collect(Collectors.toList());
                    if (CollectionUtil.isNotEmpty(relationAppTypeIdList)) {
                        busiIds = relationAppTypeIdList.get(0).getBusiIds();
                    }

                }
            }
        }
        //修改时获取页面业务类型对应的业务
        if (StrUtil.isBlank(pageFlag) && appCode != null) {
            busiIds.add(busiId);
        }
        if (CollectionUtil.isEmpty(busiIds)) {
            return ecmBusiInfoListResult;
        }
        List<EcmBusiInfo> ecmBusiInfos = ecmBusiInfoMapper.selectBatchIds(busiIds);
        if (CollectionUtil.isNotEmpty(ecmBusiInfos)) {
            ecmBusiInfoListResult = BeanUtil.copyToList(ecmBusiInfos, EcmBusiInfoDTO.class);
        }
        return ecmBusiInfoListResult;
    }

    /**
     * 获取页面业务类型
     */
    public List<EcmAppDefDTO> getPageBusiType(String busiId, String pageFlag, AccountTokenExtendDTO token) {
        List<EcmAppDefDTO> resultList = new ArrayList<>();
        List<EcmAppDef> ecmAppDefs = new ArrayList<>();
        //新增进入采集页面时对应的业务类型
        if (StrUtil.isNotBlank(pageFlag)) {
            UserBusiRedisDTO userBusiRedisDTO = busiCacheService.getUserPageRedis(pageFlag, token);
            if (userBusiRedisDTO != null) {
                if (CollectionUtil.isNotEmpty(userBusiRedisDTO.getAppType())) {
                    ecmAppDefs = ecmAppDefMapper.selectList(new LambdaQueryWrapper<EcmAppDef>().in(EcmAppDef::getAppCode, userBusiRedisDTO.getAppType()));
                }
            }
        }
        //修改时业务类型
        if (StrUtil.isBlank(pageFlag) && StrUtil.isNotBlank(busiId)) {
            EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectById(busiId);
            ecmAppDefs = ecmAppDefMapper.selectList(new LambdaQueryWrapper<EcmAppDef>().eq(EcmAppDef::getAppCode, ecmBusiInfo.getAppCode()));
        }
        if (CollectionUtil.isNotEmpty(ecmAppDefs)) {
            resultList = BeanUtil.copyToList(ecmAppDefs, EcmAppDefDTO.class);
        }
        return resultList;
    }

    /**
     * 获取页面唯一标识
     */
    public String getPageFlag(AccountTokenExtendDTO token) {
        //生成页面唯一表示uuid
        String pageFlag = UUIDUtils.generateUUID();
        String randomString = pageFlag.replace("-", "");
        busiCacheService.saveOrUpdateUser(pageFlag, new UserBusiRedisDTO());
        return randomString;
    }

    /**
     * 查询业务下所有文件名称
     */
    public List<String> searchFileNameList(Long busiId, Integer type, String name, String docId, AccountTokenExtendDTO token) {
        AssertUtils.isNull(busiId, "busiId:不能为空");
        AssertUtils.isNull(type, "type:不能为空");
        AssertUtils.isNull(docId, "docId:不能为空");
        //文件名称集合
        List<String> nameList = new ArrayList<>();
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token, busiId);
        searchFileNameToRedis(JSONObject.toJSONString(ecmBusiInfoRedisDTO), nameList, type, name, docId, busiId);
        return nameList;
    }

    private void searchFileNameToRedis(String s, List<String> fileNameList, Integer type, String name, String docId, Long busiId) {
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = JSONObject.parseObject(s, EcmBusiInfoRedisDTO.class);
        List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService.getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
        if (!CollectionUtils.isEmpty(fileInfoRedisEntities)) {
            if (StateConstants.ZERO.equals(type)) {
                if (String.valueOf(busiId).equals(docId)) {
                    fileNameList.addAll(fileInfoRedisEntities.stream()
                            .filter(p -> p.getNewFileName().contains(name) && StateConstants.ZERO.equals(p.getState()))
                            .map(FileInfoRedisDTO::getNewFileName).collect(Collectors.toList()));
                } else {
                    fileNameList.addAll(fileInfoRedisEntities.stream()
                            .filter(p -> p.getDocCode().equals(docId))
                            .filter(p -> p.getNewFileName().contains(name) && StateConstants.ZERO.equals(p.getState()))
                            .map(FileInfoRedisDTO::getNewFileName).collect(Collectors.toList()));
                }

            } else {
                if (String.valueOf(busiId).equals(docId)) {
                    fileNameList.addAll(fileInfoRedisEntities.stream().filter(p -> p.getCreateUserName().contains(name))
                            .map(FileInfoRedisDTO::getCreateUserName)
                            .distinct()
                            .collect(Collectors.toList()));
                } else {
                    fileNameList.addAll(fileInfoRedisEntities.stream()
                            .filter(p -> p.getDocCode().equals(docId))
                            .filter(p -> p.getCreateUserName().contains(name))
                            .map(FileInfoRedisDTO::getCreateUserName).distinct()
                            .collect(Collectors.toList()));
                }
            }
        }
    }

    private void searchFileNameToDb(Long busiId, List<String> fileNameList, Integer type, String name, String docId) {
        if (StateConstants.ZERO.equals(type)) {
            List<EcmFileInfo> ecmFileInfoList;
            if (busiId.toString().equals(docId)) {
                ecmFileInfoList = ecmFileInfoMapper.selectList(new LambdaQueryWrapper<EcmFileInfo>()
                        .eq(EcmFileInfo::getBusiId, busiId)
                        .like(EcmFileInfo::getNewFileName, name));
            } else {
                ecmFileInfoList = ecmFileInfoMapper.selectList(new LambdaQueryWrapper<EcmFileInfo>()
                        .eq(EcmFileInfo::getBusiId, busiId)
                        .like(EcmFileInfo::getNewFileName, name)
                        .eq(EcmFileInfo::getDocCode, docId));
            }
            List<String> fileNames = new ArrayList<>();
            if (!CollectionUtils.isEmpty(ecmFileInfoList)) {
                fileNames = ecmFileInfoList.stream().map(EcmFileInfo::getNewFileName).collect(Collectors.toList());
                fileNameList.addAll(fileNames);
            } else {
                fileNameList.addAll(new ArrayList<>());
            }
        } else {
            List<EcmFileInfo> ecmFileInfoList;
            if (docId.equals(busiId.toString())) {
                ecmFileInfoList = ecmFileInfoMapper.selectList(new LambdaQueryWrapper<EcmFileInfo>()
                        .like(EcmFileInfo::getCreateUserName, name)
                        .eq(EcmFileInfo::getBusiId, busiId));
            } else {
                ecmFileInfoList = ecmFileInfoMapper.selectList(new LambdaQueryWrapper<EcmFileInfo>()
                        .eq(EcmFileInfo::getBusiId, busiId)
                        .like(EcmFileInfo::getCreateUserName, name)
                        .eq(EcmFileInfo::getDocCode, docId));
            }
            List<String> fileNames = new ArrayList<>();
            if (!CollectionUtils.isEmpty(ecmFileInfoList)) {
                fileNames = ecmFileInfoList.stream().map(EcmFileInfo::getNewFileName).collect(Collectors.toList());
                fileNameList.addAll(fileNames);
            } else {
                fileNameList.addAll(new ArrayList<>());
            }
        }
    }

    /**
     * 获取移动端采集页面路径
     */
    public Result getMobilePagePath(List<Long> busiIdList, AccountTokenExtendDTO token) {
        AssertUtils.isNull(CollectionUtil.isEmpty(busiIdList), "采集页面无业务数据");
        EcmMobileParamsDTO ecmMobileParamsDTO = new EcmMobileParamsDTO();
        Result<SysParamDTO> pagePathResult = paramApi.searchValueByKey(IcmsConstants.ECMS_MOBILE_CAPTURE_PATH);
        if (pagePathResult.isSucc()) {
            BeanUtils.copyProperties(pagePathResult.getData(), ecmMobileParamsDTO);
            Map<String, String> map = dealUrl(ecmMobileParamsDTO.getValue(), token);
            String timestamp = map.get("timestamp");
            String encryptUrl = map.get("encryptUrl");
            ecmMobileParamsDTO.setValue(encryptUrl);
            ecmMobileParamsDTO.setPageFlag(token.getFlagId());
            redisUtil.set(RedisConstants.PAGE_BUSI_LIST + timestamp, JSONObject.toJSONString(busiIdList), TimeOutConstants.ONE_DAY);
            ecmMobileParamsDTO.setPageBusiListKey(RedisConstants.PAGE_BUSI_LIST + timestamp);
        }
        return Result.success(ecmMobileParamsDTO);
    }

    /**
     * 修改文件名+批注名
     */
    @WebsocketNoticeAnnotation(busiId = "#vo.busiId")
    @Transactional(rollbackFor = Exception.class)
    public void updateFileNameAndComment(EcmCommentVO vo, AccountTokenExtendDTO token) {
        AssertUtils.isNull(vo.getFileId(), "参数有误！");
        AssertUtils.isNull(vo.getBusiId(), "参数有误！");
        EcmFileInfo fileInfo = new EcmFileInfo();
        fileInfo.setFileId(vo.getFileId());
        fileInfo.setComment(vo.getComment());
        fileInfo.setNewFileName(vo.getNewFileName());
        fileInfo.setUpdateUserName(token.getName());
        fileInfo.setUpdateTime(new Date());
        //获取拓展名
        int index = vo.getNewFileName().lastIndexOf(".");
        fileInfo.setNewFileExt(vo.getNewFileName().substring(index + 1));
        ecmFileInfoMapper.updateById(fileInfo);
        //修改redis中的数据
        ArrayList<Long> longs = new ArrayList<>();
        longs.add(vo.getFileId());
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(vo.getBusiId(), longs);
        for (FileInfoRedisDTO fileInfoRedisDTO : fileInfoRedis) {
            if (fileInfoRedisDTO.getFileId().equals(fileInfo.getFileId())) {
                fileInfoRedisDTO.setNewFileName(vo.getNewFileName());
                fileInfoRedisDTO.setComment(vo.getComment());
            }
        }
        busiCacheService.updateFileInfoRedis(fileInfoRedis);
        //修改es中的数据
        operateFullQueryService.editEsFileInfo(vo.getFileId(),token.getName(),fileInfo.getUpdateTime(),null,null,vo.getNewFileName());
    }

    /**
     * 页面路径加密处理（路径+时间戳）
     */
    private Map<String, String> dealUrl(String mobilePageUrl, AccountTokenExtendDTO token) {
        Map map = new HashMap();
        String encryptUrl = "";
        if (token.isOut()) {
            encryptUrl = mobilePageUrl;
        } else {
            Long timestamp = Instant.now().toEpochMilli();
            map.put("timestamp", timestamp.toString());
            encryptUrl = mobilePageUrl + "?" + "timestamp=" + timestamp;
        }
        //对路径加密
        try {

            Integer loginEncryption = 1;
            switch (loginEncryption) {
                case LoginEncryptionConstant.RSA_LOGIN_TYPE:
                    encryptUrl = RsaUtils.encrypt(encryptUrl);
                    break;
                default:
                    encryptUrl = Sm2Util.encrypt(encryptUrl.getBytes());
            }
            if (encryptUrl.startsWith(LoginEncryptionConstant.ENCRYPTED_PREFIX)) {
                encryptUrl = encryptUrl.substring(LoginEncryptionConstant.ENCRYPTED_PREFIX.length());
            }
            map.put("encryptUrl", encryptUrl);
        } catch (Exception e) {
            log.error("路径解析有误");
        }
        return map;
    }

}
