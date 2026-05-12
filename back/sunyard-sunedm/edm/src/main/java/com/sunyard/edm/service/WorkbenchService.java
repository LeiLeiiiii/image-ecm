package com.sunyard.edm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shiro.util.Assert;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.constant.DocDictionaryKeyConstants;
import com.sunyard.edm.dto.DocBsHomeDTO;
import com.sunyard.edm.dto.DocBsMessageDTO;
import com.sunyard.edm.dto.DocBsRecentlyDTO;
import com.sunyard.edm.mapper.DocBsDocumentMapper;
import com.sunyard.edm.mapper.DocBsMessageMapper;
import com.sunyard.edm.mapper.DocBsMessageRangeMapper;
import com.sunyard.edm.mapper.DocBsRecentlyDocumentMapper;
import com.sunyard.edm.mapper.DocBsShapeInsideUserMapper;
import com.sunyard.edm.mapper.DocBsShapeMapper;
import com.sunyard.edm.mapper.DocSysAnnounUserMapper;
import com.sunyard.edm.mapper.DocSysTeamUserMapper;
import com.sunyard.edm.po.DocBsDocument;
import com.sunyard.edm.po.DocBsMessage;
import com.sunyard.edm.po.DocBsMessageRange;
import com.sunyard.edm.po.DocBsRecentlyDocument;
import com.sunyard.edm.po.DocBsShapeInsideUser;
import com.sunyard.edm.po.DocSysTeamUser;
import com.sunyard.edm.vo.DocBsMessageVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.DictionaryApi;
import com.sunyard.module.system.api.MessageApi;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.dto.MessageDTO;
import com.sunyard.module.system.api.dto.SysParamDTO;

/**
 * @Author PJW 2022/12/19 9:50
 * @DESC 首页-工作台实现类
 */
@Service
public class WorkbenchService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private DocSysTeamUserMapper docSysTeamUserMapper;
    @Resource
    private DocBsDocumentMapper docBsDocumentMapper;
    @Resource
    private DocBsShapeMapper docBsShapeMapper;
    @Resource
    private DocBsShapeInsideUserMapper docBsShapeInsideUserMapper;
    @Resource
    private DocBsMessageMapper docBsMessageMapper;
    @Resource
    private DocBsRecentlyDocumentMapper docBsRecentlyDocumentMapper;
    @Resource
    private DocBsMessageRangeMapper docBsMessageRangeMapper;
    @Resource
    private DocSysAnnounUserMapper docSysAnnounUserMapper;
    @Resource
    private MessageApi messageApi;
    @Resource
    private ParamApi paramApi;
    @Resource
    private DictionaryApi sysDictionaryService;

    /**
     * 查询首页-公告栏模块的数据：全部
     */
    public Result queryNotice(Long userId, Long deptId, Long instId, PageForm p) {
        //发布对象及发布状态为公开的公告
        Assert.notNull(userId, "参数错误");
        Assert.notNull(deptId, "参数错误");
        Assert.notNull(instId, "参数错误");
        List<Long> list = new ArrayList<>();
        list.add(userId);
        list.add(deptId);
        list.add(instId);
        List<DocSysTeamUser> teamUserList = docSysTeamUserMapper.selectList(new LambdaQueryWrapper<DocSysTeamUser>().eq(DocSysTeamUser::getUserId, userId));
        teamUserList.forEach(item -> list.add(item.getTeamId()));
        if (CollectionUtils.isEmpty(list)) {
            return Result.success(true);
        }
        PageHelper.startPage(p.getPageNum(), p.getPageSize());
        List<DocBsHomeDTO> result = docSysAnnounUserMapper.searchListHome(list);
        return Result.success(new PageInfo<DocBsHomeDTO>(result));
    }

    /**
     * 查询首页-分享中心模块的数据：最新的5条
     */
    public Result<List<DocBsHomeDTO>> queryShape(Long userId, Long deptId, Long instId) {
        Assert.notNull(userId, "参数错误");
        List<Long> relIdList = new ArrayList<>();
        List<DocSysTeamUser> teamUserList = docSysTeamUserMapper.selectList(new LambdaQueryWrapper<DocSysTeamUser>().eq(DocSysTeamUser::getUserId, userId));
        teamUserList.forEach(item -> relIdList.add(item.getTeamId()));
        relIdList.add(userId);
        relIdList.add(deptId);
        relIdList.add(instId);
        if (CollectionUtils.isEmpty(relIdList)) {
            return Result.success(null);
        }
        List<DocBsShapeInsideUser> docBsShapeInsideUserList = docBsShapeInsideUserMapper.selectList(
                new LambdaQueryWrapper<DocBsShapeInsideUser>()
                        .select(DocBsShapeInsideUser::getShapeId, DocBsShapeInsideUser::getCreateTime)
                        .in(DocBsShapeInsideUser::getAcceptId, relIdList)
                        .orderByDesc(DocBsShapeInsideUser::getCreateTime)
        );
        if (CollectionUtils.isEmpty(docBsShapeInsideUserList)) {
            return Result.success(new ArrayList<>());
        }
        List<Long> shapeIdList = docBsShapeInsideUserList.stream().map(DocBsShapeInsideUser::getShapeId).distinct().collect(Collectors.toList());
        List<DocBsHomeDTO> result = docBsShapeMapper.searchListHome(shapeIdList);
        if(result.size()>5){
            result = result.subList(0, 5);
        }
        return Result.success(result);
    }

    /**
     * 查询首页-最新消息模块的数据：最新的5条
     */
    public Result<List<DocBsHomeDTO>> queryMessage(Long userId) {
        Assert.notNull(userId, "参数错误");
        PageHelper.startPage(1,5);
        List<DocBsMessage> docBsMessages = docBsMessageMapper.selectList(new LambdaQueryWrapper<DocBsMessage>()
                .eq(DocBsMessage::getUserId, userId)
                .orderByDesc(DocBsMessage::getInformTime));
        List<DocBsHomeDTO> result = new ArrayList<>();
        for(DocBsMessage message : docBsMessages){
            DocBsHomeDTO dto = new DocBsHomeDTO();
            dto.setIsRead(message.getMessageStatus());
            dto.setId(message.getMessageId());
            dto.setTime(message.getInformTime());
            dto.setDocFolder(message.getDocFolder());
            dto.setContent(message.getMessageContent()+message.getDocFolder());
            result.add(dto);
        }
        return Result.success(result);
    }

    /**
     * 查询首页-最新消息模块的数据总条数
     */
    public Result<Integer> queryMessageCount(Long userId) {
        Assert.notNull(userId, "参数错误");
        Result<Map<String, String>> dicValByDicKey = sysDictionaryService.getDicValByDicKey(DocDictionaryKeyConstants.DOC_MESSAGE_STATUS);
        Map<String, String> map = dicValByDicKey.getData();
        Long count = docBsMessageMapper.selectCount(new LambdaQueryWrapper<DocBsMessage>()
                .eq(DocBsMessage::getUserId, userId)
                .eq(DocBsMessage::getMessageStatus, map.get(DocDictionaryKeyConstants.DOC_MESSAGE_STATUS_NO)));
        return Result.success(count.intValue());
    }

    /**
     * 查询'消息通知'数据：30天之内
     */
    public Result<List<DocBsMessageDTO>> queryMessageAll(Long userId, Long deptId, Long instId) {
        Assert.notNull(userId, "参数错误");
        Assert.notNull(deptId, "参数错误");
        Assert.notNull(instId, "参数错误");
        List<Long> userIdList = new ArrayList<>();
        userIdList.add(userId);
        userIdList.add(deptId);
        userIdList.add(instId);
        List<DocBsMessageDTO> docBsMessageList = docBsMessageMapper.searchExtend(userId, userIdList);
        Result<SysParamDTO> sysParamDTOResult = paramApi.searchValueByKey("DOC_FOLDER_TREE_TYPE");
        SysParamDTO data = sysParamDTOResult.getData();
        if (!CollectionUtils.isEmpty(docBsMessageList)) {
            for (DocBsMessageDTO docBsMessageDTO : docBsMessageList) {
                if (docBsMessageDTO.getHouseId() == null) {
                    docBsMessageDTO.setIsAsh(false);
                } else {
                    if (DocConstants.STR_ONE.equals(data.getValue())) {
                        if (docBsMessageDTO.getDocId() == null) {
                            docBsMessageDTO.setIsAsh(false);
                        } else {
                            docBsMessageDTO.setIsAsh(true);
                        }
                    } else {
                        docBsMessageDTO.setIsAsh(true);
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(docBsMessageList)) {
            return Result.success(null);
        }
        return Result.success(docBsMessageList);
    }

    /**
     * 查询'最近打开'数据：最新的10条
     */
    public Result<List<DocBsRecentlyDTO>> queryRecently(Long userId, Long deptId, Long instId) {
        Assert.notNull(userId, "参数错误");
        Assert.notNull(deptId, "参数错误");
        Assert.notNull(instId, "参数错误");
        //查询'最近打开'最新的10条数据
        List<DocBsRecentlyDocument> docBsRecentlyDocumentList = docBsRecentlyDocumentMapper.selectList(new LambdaQueryWrapper<DocBsRecentlyDocument>()
                .eq(DocBsRecentlyDocument::getUserId, userId)
                .orderByDesc(DocBsRecentlyDocument::getUpdateTime)
                .last("limit 10"));
        if (CollectionUtils.isEmpty(docBsRecentlyDocumentList)) {
            return Result.success(null);
        }
        //拿到这10条数据的父级目录id
        List<Long> docIdList = docBsRecentlyDocumentList.stream().map(DocBsRecentlyDocument::getDocId).collect(Collectors.toList());
        List<DocBsDocument> documentList = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>().in(DocBsDocument::getBusId, docIdList));
        List<Long> parentIdList = documentList.stream().map(DocBsDocument::getFolderId).distinct().collect(Collectors.toList());

        List<Long> userIdList = new ArrayList<>();
        userIdList.add(userId);
        userIdList.add(deptId);
        userIdList.add(instId);
        List<DocSysTeamUser> teamUserList = docSysTeamUserMapper.selectList(new LambdaQueryWrapper<DocSysTeamUser>().eq(DocSysTeamUser::getUserId, userId));
        teamUserList.forEach(item -> userIdList.add(item.getTeamId()));
        List<DocBsRecentlyDTO> docBsRecentlyDTOS = docBsRecentlyDocumentMapper.searchExtend(userIdList, parentIdList, userId);
        return Result.success(docBsRecentlyDTOS);
    }

    /**
     * 添加‘最近打开文档’
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#docId")
    public Result addRecently(Long docId, Long userId) {
        Assert.notNull(docId, "参数错误");
        Assert.notNull(userId, "参数错误");
        //查询'最近打开'所有数据
        Long count = docBsRecentlyDocumentMapper.selectCount(new LambdaQueryWrapper<DocBsRecentlyDocument>()
                .eq(DocBsRecentlyDocument::getDocId, docId)
                .eq(DocBsRecentlyDocument::getUserId, userId));
        //统一时间
        Date date = new Date();
        if (count.intValue() == 0) {
            //不存在则添加
            DocBsRecentlyDocument docBsRecentlyDocument = new DocBsRecentlyDocument();
            docBsRecentlyDocument.setDocId(docId);
            docBsRecentlyDocument.setUserId(userId);
            docBsRecentlyDocument.setCreateTime(date);
            docBsRecentlyDocument.setUpdateTime(date);
            docBsRecentlyDocumentMapper.insert(docBsRecentlyDocument);
        } else {
            //存在则更新时间
            docBsRecentlyDocumentMapper.update(null, new LambdaUpdateWrapper<DocBsRecentlyDocument>()
                    .set(DocBsRecentlyDocument::getUpdateTime, date)
                    .eq(DocBsRecentlyDocument::getDocId, docId)
                    .eq(DocBsRecentlyDocument::getUserId, userId)
            );
        }
        return Result.success(true);
    }

    /**
     * 添加消息通知-批量
     */
    @Transactional(rollbackFor = Exception.class)
    public Result addMessageBatch(List<DocBsMessageVO> list) {
        List<DocBsMessage> msgInsertList = new ArrayList<>();
        //拿到userId：消息对象map集合，用于获取collectionId
        Map<Long, List<DocBsMessageVO>> msgMap = list.stream().collect(Collectors.groupingBy(DocBsMessageVO::getUserId));
        msgMap.forEach((key, value) -> {
           /* //获取用户的接收范围
            List<DocBsMessageRange> docBsMessageRangeList = docBsMessageRangeMapper.selectList(new LambdaQueryWrapper<DocBsMessageRange>().eq(DocBsMessageRange::getUserId, key));
            List<Integer> rangeKeyList = docBsMessageRangeList.stream().map(DocBsMessageRange::getRangeKey).collect(Collectors.toList());
            //如果在接受范围内，则进行消息添加*/
            value.forEach(item -> {
//                if (rangeKeyList.contains(item.getMessageType())) {
                    DocBsMessage docBsMessage = new DocBsMessage();
                    BeanUtils.copyProperties(item, docBsMessage);
                    msgInsertList.add(docBsMessage);
                    MessageDTO messageDTO = new MessageDTO();
                    messageDTO.setMsgSystem(DocConstants.APPLICATION);
                    messageDTO.setMsgType(String.valueOf(item.getMessageType()));
                    messageDTO.setMsgHead(item.getMessageTitle());
                    messageDTO.setMsgContent(item.getMessageContent());
                    messageDTO.setAcceptUser(key);
                    messageDTO.setInformTime(new Date());
                    messageApi.saveAndSendMsgJson(messageDTO);
//                }
            });
        });
        if (!CollectionUtils.isEmpty(msgInsertList)) {
            MybatisBatch<DocBsMessage> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, msgInsertList);
            MybatisBatch.Method<DocBsMessage> method = new MybatisBatch.Method<>(DocBsMessageMapper.class);
            mybatisBatch.execute(method.insert());
        }
        return Result.success(true);
    }

    /**
     * 消息接收范围设置
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#userId")
    public Result msgRangeSet(Integer[] key, Long userId) {
        //先删后加
        docBsMessageRangeMapper.delete(new LambdaUpdateWrapper<DocBsMessageRange>().eq(DocBsMessageRange::getUserId, userId));
        List<DocBsMessageRange> docBsMessageRanges = new ArrayList<>();
        for (Integer item : key) {
            DocBsMessageRange docBsMessageRange = new DocBsMessageRange();
            docBsMessageRange.setRangeKey(item);
            docBsMessageRange.setUserId(userId);
            docBsMessageRanges.add(docBsMessageRange);
        }
        if(!CollectionUtils.isEmpty(docBsMessageRanges)){
            MybatisBatch<DocBsMessageRange> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, docBsMessageRanges);
            MybatisBatch.Method<DocBsMessageRange> method = new MybatisBatch.Method<>(DocBsMessageRangeMapper.class);
            mybatisBatch.execute(method.insert());
        }
        return Result.success(true);
    }

    /**
     * 消息接收范围查询
     */
    public Result msgRangeQuery(Long userId) {
        Assert.notNull(userId, "参数错误");
        final List<DocBsMessageRange> result = docBsMessageRangeMapper.selectList(new LambdaQueryWrapper<DocBsMessageRange>().eq(DocBsMessageRange::getUserId, userId));
        List<String> keyList = result.stream().map(item -> item.getRangeKey() + "").collect(Collectors.toList());
        return Result.success(keyList);
    }

    /**
     * 一键已读
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#userId")
    public Result oneButtonRead(Long userId) {
        Result<Map<String, String>> dicValByDicKey = sysDictionaryService.getDicValByDicKey(DocDictionaryKeyConstants.DOC_MESSAGE_STATUS);
        Map<String, String> map = dicValByDicKey.getData();
        docBsMessageMapper.update(null, new LambdaUpdateWrapper<DocBsMessage>()
                .set(DocBsMessage::getMessageStatus, Integer.valueOf(map.get(DocDictionaryKeyConstants.DOC_MESSAGE_STATUS_YES)))
                .eq(DocBsMessage::getUserId, userId));
        return Result.success(true);
    }

    /**
     * 单独已读
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#messageId")
    public Result onlyRead(Long messageId) {
        Result<Map<String, String>> dicValByDicKey = sysDictionaryService.getDicValByDicKey(DocDictionaryKeyConstants.DOC_MESSAGE_STATUS);
        Map<String, String> map = dicValByDicKey.getData();
        docBsMessageMapper.update(null, new LambdaUpdateWrapper<DocBsMessage>()
                .set(DocBsMessage::getMessageStatus, Integer.valueOf(map.get(DocDictionaryKeyConstants.DOC_MESSAGE_STATUS_YES)))
                .eq(DocBsMessage::getMessageId, messageId));
        return Result.success(true);
    }
}
