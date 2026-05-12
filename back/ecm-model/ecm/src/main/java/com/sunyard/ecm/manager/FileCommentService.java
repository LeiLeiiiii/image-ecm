package com.sunyard.ecm.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sunyard.ecm.annotation.WebsocketNoticeAnnotation;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.mapper.EcmFileCommentMapper;
import com.sunyard.ecm.mapper.EcmFileMarkCommentMapper;
import com.sunyard.ecm.po.EcmFileComment;
import com.sunyard.ecm.po.EcmFileMarkComment;
import com.sunyard.ecm.vo.EcmCommentVO;
import com.sunyard.ecm.vo.EcmFileComHisResultVO;
import com.sunyard.ecm.vo.EcmFileCommentVO;
import com.sunyard.ecm.vo.EcmFileMarkCommentVO;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.date.DateUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysUserDTO;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author rao
 * @date 2023/4/20
 * @describe 文件批注实现类
 */
@Service
public class FileCommentService {
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private EcmFileCommentMapper ecmFileCommentMapper;
    @Resource
    private EcmFileMarkCommentMapper ecmFileMarkCommentMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private BusiOperationService busiOperationService;
    @Resource
    private BusiCacheService busiCacheService;

    /**
     * 获取批注
     */
    public List<EcmFileCommentVO> getComment(EcmCommentVO vo, AccountTokenExtendDTO token) {

        AssertUtils.isNull(vo.getBusiId(), "业务id不能为空");
        AssertUtils.isNull(vo.getFileId(), "文件id不能为空");
        AssertUtils.isNull(vo.getNewFileId(), "最新文件id不能为空");

        List<EcmFileComment> busiIds = new ArrayList<>();

        if (vo.getIsPdf()!=null && vo.getIsPdf()) {
            busiIds = ecmFileCommentMapper.selectList(new QueryWrapper<EcmFileComment>()
                    .eq("busi_id", vo.getBusiId())
                    .eq("file_id", vo.getFileId())
                    .eq("new_file_id", vo.getNewFileId())
                    .eq("is_pdf",vo.getIsPdf())
                    .eq("file_page", vo.getFilePage()));
        }else {
            busiIds = ecmFileCommentMapper.selectList(new QueryWrapper<EcmFileComment>()
                    .eq("busi_id", vo.getBusiId())
                    .eq("file_id", vo.getFileId())
                    .eq("new_file_id", vo.getNewFileId()));
        }

        if (CollectionUtils.isEmpty(busiIds)) {
            return null;
        }
        List<String> userIds = busiIds.stream().map(EcmFileComment::getCreateUser).collect(Collectors.toList());
        Result<List<SysUserDTO>> result = userApi.getUserListByUsernames(userIds.toArray(new String[0]));
        List<SysUserDTO> userDTOList = new ArrayList<>();
        if (result.isSucc()) {
            userDTOList = result.getData();
        } else {
            throw new SunyardException(result.getMsg());
        }

        Map<String, List<SysUserDTO>> groupedUserByUserId = userDTOList.stream().collect(Collectors.groupingBy(SysUserDTO::getLoginName));
        List<EcmFileCommentVO> ecmCommentVOS = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(busiIds)) {
            ecmCommentVOS = BeanUtil.copyToList(busiIds, EcmFileCommentVO.class);
            for (EcmFileCommentVO ecmFileComment : ecmCommentVOS) {
                JSONObject jsonObject = JSONObject.parseObject(ecmFileComment.getCommentValue());
                jsonObject.put("id", ecmFileComment.getCommentId());
                jsonObject.put("createTime", DateUtils.dateTime(ecmFileComment.getCreateTime()));
                //转用户
                List<SysUserDTO> userDTOList1 = groupedUserByUserId.get(ecmFileComment.getCreateUser());
                jsonObject.put("createUser", CollectionUtils.isEmpty(userDTOList1) ? "" : userDTOList1.get(0).getName());
                //若是pdf文件则需要返回页数和张数
                if (ecmFileComment.getFilePage()!=null){
                    jsonObject.put("filePage", ecmFileComment.getFilePage());
                }
                ecmFileComment.setCommentValue(jsonObject.toJSONString());
            }
        }
        return ecmCommentVOS;
    }

    /**
     * 新增批注
     */
    @WebsocketNoticeAnnotation(busiId = "#vo.busiId")
    public void addComment(EcmCommentVO vo, AccountTokenExtendDTO token) {
        AssertUtils.isNull(vo.getBusiId(), "业务id不能为空");
        AssertUtils.isNull(vo.getFileId(), "文件id不能为空");
        AssertUtils.isNull(vo.getNewFileId(), "最新文件id不能为空");
        AssertUtils.isNull(vo.getNewFileName(), "最新文件名称不能为空");
        if (vo.getIsPdf()!= null && vo.getIsPdf()){
            AssertUtils.isNull(vo.getFilePage(), "pdf文件页数不能为空");
        }

        //数据处理
        if (!ObjectUtils.isEmpty(vo.getFileCommentListStr())) {
            JSONArray objects = JSONObject.parseArray(vo.getFileCommentListStr());
            List<EcmFileComment> list = new ArrayList<>();
            objects.forEach(s -> {
                EcmFileComment ecmFileComment = new EcmFileComment();
                ecmFileComment.setCommentValue(s.toString());
                list.add(ecmFileComment);
            });
            vo.setFileCommentList(list);
        } else {
            vo.setFileCommentList(new ArrayList<>());
        }

        List<EcmFileComment> busiIds = new ArrayList<>();
        if (vo.getIsPdf()!= null && vo.getIsPdf()){
            busiIds = ecmFileCommentMapper.selectList(new QueryWrapper<EcmFileComment>()
                    .eq("busi_id", vo.getBusiId())
                    .eq("new_file_id", vo.getNewFileId())
                    .eq("is_pdf", vo.getIsPdf())
                    .eq("file_page", vo.getFilePage()));
        }else {
            busiIds = ecmFileCommentMapper.selectList(new QueryWrapper<EcmFileComment>()
                    .eq("busi_id", vo.getBusiId())
                    .eq("new_file_id", vo.getNewFileId())
                    .eq(vo.getIsPdf() != null ,"is_pdf", vo.getIsPdf()));
        }
        Map<String, List<EcmFileComment>> collect = busiIds.stream().collect(Collectors.groupingBy(EcmFileComment::getCommentValue));
        List<Long> all = busiIds.stream().map(EcmFileComment::getCommentId).collect(Collectors.toList());
        List<Long> oldIds = new ArrayList<>();
        List<EcmFileComment> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(vo.getFileCommentList())) {
            //生成批注记录，一次保存所有批注使用同一记录Id
            long commentRecordId = snowflakeUtil.nextId();
            //需保留的ids
            for (EcmFileComment fileComment : vo.getFileCommentList()) {
                JSONObject jsonObject = JSONObject.parseObject(fileComment.getCommentValue());
                if ("line".equals(jsonObject.getString("type"))) {
                    if (jsonObject.get("path") == null && jsonObject.getInteger("x1") != null) {
                        if (jsonObject.getInteger("x1") == 0 && jsonObject.getInteger("y1") == 0 &&
                                jsonObject.getInteger("x2") == 0 && jsonObject.getInteger("y2") == 0
                        ) {
                            continue;
                        }
                        List<List<Object>> a = new ArrayList<>();
                        List<Object> a1 = new ArrayList<>();
                        a1.add("M");
                        a1.add(jsonObject.getInteger("x1") + 80);
                        a1.add(jsonObject.getInteger("y1") + 60);
                        List<Object> a2 = new ArrayList<>();
                        a2.add("L");
                        a2.add(jsonObject.getInteger("x2") + 80);
                        a2.add(jsonObject.getInteger("y2") + 60);
                        a.add(a1);
                        a.add(a2);
                        jsonObject.put("path", a);
                    }

                } else if ("i-text".equals(jsonObject.getString("type"))) {
                    JSONObject style = jsonObject.getJSONObject("styles");
                    style.remove("createTime");
                    style.remove("createUser");
                }
                String s = jsonObject.toJSONString();
                List<EcmFileComment> ecmFileComments = collect.get(s);
                if (CollectionUtils.isEmpty(ecmFileComments)) {
                    //新增的js
                    EcmFileComment ecmFileComment = new EcmFileComment();
                    ecmFileComment.setFileId(vo.getFileId());
                    ecmFileComment.setCommentValue(s);
                    ecmFileComment.setCreateUser(token.getUsername());
                    ecmFileComment.setBusiId(vo.getBusiId());
                    ecmFileComment.setNewFileId(vo.getNewFileId());
                    ecmFileComment.setFilePage(vo.getFilePage());
                    ecmFileComment.setIsPdf(vo.getIsPdf() == null ? false : vo.getIsPdf());
//                    ecmFileCommentMapper.insert(ecmFileComment);
                    list.add(ecmFileComment);
                } else {
                    //未变动的js
                    List<Long> collect1 = ecmFileComments.stream().map(EcmFileComment::getCommentId).collect(Collectors.toList());
                    oldIds.addAll(collect1);
                }
            }
        }

        //删除不需要的js
        all.removeIf(s -> oldIds.contains(s));
        commentInfos(all, list);
        //添加操作记录表
        busiOperationService.addOperation(vo.getBusiId(), IcmsConstants.ADD_COMMENT, token, "添加批注:" + vo.getNewFileName());
        //redis业务信息中对应的文件信息里添加批注数量
        addFileCommentListToRedis(vo.getBusiId(), vo.getNewFileId(), vo.getFileId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void commentInfos(List<Long> all, List<EcmFileComment> list) {
        if (!CollectionUtils.isEmpty(all)) {
            ecmFileCommentMapper.deleteBatchIds(all);
            //删除批注记录
//            deleteFileCommentRecord(vo.getBusiId(), vo.getFileId(), vo.getNewFileId(), all);
        }

        MybatisBatch<EcmFileComment> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, list);
        MybatisBatch.Method<EcmFileComment> method = new MybatisBatch.Method<>(
                EcmFileCommentMapper.class);
        mybatisBatch.execute(method.insert());

    }


    private void addFileCommentListToRedis(Long busiId, Long newFileId, Long fileId) {

        FileInfoRedisDTO fileInfoRedisSingle = busiCacheService.getFileInfoRedisSingle(busiId, fileId);
        //获取文件批注数量
        Long commentCount = ecmFileCommentMapper.selectCount(new QueryWrapper<EcmFileComment>()
                .eq("busi_id", busiId)
                .eq("file_id", fileId)
                .eq("new_file_id", newFileId));
        if (commentCount!=null && commentCount > 0L) {
            fileInfoRedisSingle.setFileCommentCount(commentCount.intValue());
            busiCacheService.updateFileInfoRedis(fileInfoRedisSingle);
        }else{
            fileInfoRedisSingle.setFileCommentCount(0);
            busiCacheService.updateFileInfoRedis(fileInfoRedisSingle);
        }
    }

    /**
     * 获取批注历史记录
     */
    public List<EcmFileComHisResultVO> getCommentHistory(EcmCommentVO vo, AccountTokenExtendDTO token) {
        List<EcmFileComHisResultVO> list = new ArrayList<>();
        //获取批注历史记录
        AssertUtils.isNull(vo.getBusiId(), "业务id不能为空");
        AssertUtils.isNull(vo.getFileId(), "文件id不能为空");
        AssertUtils.isNull(vo.getNewFileId(), "最新文件id不能为空");
        //查询批注
        LambdaQueryWrapper<EcmFileComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcmFileComment::getBusiId, vo.getBusiId());
        wrapper.eq(EcmFileComment::getFileId, vo.getFileId());
        wrapper.eq(EcmFileComment::getNewFileId, vo.getNewFileId());
        List<EcmFileComment> ecmFileComments = ecmFileCommentMapper.selectList(wrapper);
        //拿到本次查询所有的用户登录名
        Set<String> uniqueCreateUsers = ecmFileComments.stream()
                .map(EcmFileComment::getCreateUser)  // 提取 createUser
                .filter(Objects::nonNull)        // 过滤掉可能的 null 值
                .collect(Collectors.toSet());         // 收集到 Set 中去重
        //查询用户名称
        Result<List<SysUserDTO>> result = userApi.getUserListByUsernames(uniqueCreateUsers.toArray(new String[0]));
        List<SysUserDTO> userDTOList = new ArrayList<>();
        if (result.isSucc()) {
            userDTOList = result.getData();
        } else {
            throw new SunyardException(result.getMsg());
        }
        Map<String, List<SysUserDTO>> groupedUserByUserId = userDTOList.stream().collect(Collectors.groupingBy(SysUserDTO::getLoginName));
        if (CollectionUtil.isNotEmpty(ecmFileComments)) {
            //根据批注记录id分组
            Map<String, List<EcmFileComment>> recordList = ecmFileComments.stream()
                    .collect(Collectors.groupingBy(comment ->
                            comment.getCreateUser() + "_" + comment.getCreateTime()));
            recordList.forEach((k, v) -> {
                List<EcmFileComment> comments = recordList.get(k);
                //批注内容拼装批注commentId
                comments.stream().forEach(f -> {
                    String commentValue = f.getCommentValue();
                    JSONObject jsonObject = JSONObject.parseObject(commentValue);
                    jsonObject.put("comment_id", f.getCommentId());
                    f.setCommentValue(jsonObject.toJSONString());
                });
                List<String> valueList = comments.stream().map(EcmFileComment::getCommentValue).collect(Collectors.toList());
                //批注记录基本信息赋值
                EcmFileComment ecmFileComment = comments.get(0);
                EcmFileComHisResultVO hisResultVO = new EcmFileComHisResultVO();
                hisResultVO.setBusiId(ecmFileComment.getBusiId());
                hisResultVO.setBusiId(ecmFileComment.getFileId());
                hisResultVO.setBusiId(ecmFileComment.getNewFileId());
                hisResultVO.setCreateTime(ecmFileComment.getCreateTime());
                hisResultVO.setCreateUser(ecmFileComment.getCreateUser());
                List<SysUserDTO> userDTOS=groupedUserByUserId.get(ecmFileComment.getCreateUser());
                hisResultVO.setCreateUserName(CollectionUtils.isEmpty(userDTOS)?ecmFileComment.getCreateUser():userDTOS.get(0).getName());
                hisResultVO.setCommentRecord(k);
                hisResultVO.setCommentValueList(valueList);
                //类型为批注
                hisResultVO.setCommentType(IcmsConstants.COMMENT);
                list.add(hisResultVO);
            });
        }
        //查询评论
        LambdaQueryWrapper<EcmFileMarkComment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(EcmFileMarkComment::getBusiId, vo.getBusiId());
        commentWrapper.eq(EcmFileMarkComment::getFileId, vo.getFileId());
        commentWrapper.eq(EcmFileMarkComment::getNewFileId, vo.getNewFileId());
        commentWrapper.orderByAsc(EcmFileMarkComment::getCreateTime);
        List<EcmFileMarkComment> ecmFileMarkComments = ecmFileMarkCommentMapper.selectList(commentWrapper);
        if (CollectionUtil.isNotEmpty(ecmFileMarkComments)) {
            ecmFileMarkComments.forEach(f -> {
                EcmFileComHisResultVO ecmFileComHisResultVO = BeanUtil.copyProperties(f, EcmFileComHisResultVO.class);
                //类型为评论
                ecmFileComHisResultVO.setCommentType(IcmsConstants.REVIEW);
                ecmFileComHisResultVO.setCommentValueList(Arrays.asList(f.getCommentContent()));
                list.add(ecmFileComHisResultVO);
            });
        }

        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        // createTime 字段排序
        Collections.sort(list, new Comparator<EcmFileComHisResultVO>() {
            @Override
            public int compare(EcmFileComHisResultVO o1, EcmFileComHisResultVO o2) {
                return o1.getCreateTime().compareTo(o2.getCreateTime());
            }
        });
        Collections.reverse(list);
        return list;
    }

    /**
     * 新增批注评论
     */
    public void addMarkComment(EcmFileMarkCommentVO vo, AccountTokenExtendDTO token) {
        //获取批注历史记录
        AssertUtils.isNull(vo.getBusiId(), "业务id不能为空");
        AssertUtils.isNull(vo.getFileId(), "文件id不能为空");
        AssertUtils.isNull(vo.getNewFileId(), "最新文件id不能为空");
        AssertUtils.isNull(vo.getCommentContent(), "评论内容不能为空");
        EcmFileMarkComment ecmFileMarkComment = BeanUtil.copyProperties(vo, EcmFileMarkComment.class);
        ecmFileMarkComment.setCreateUser(token.getUsername());
        ecmFileMarkComment.setCreateUserName(token.getName());
        ecmFileMarkCommentMapper.insert(ecmFileMarkComment);
    }
}
