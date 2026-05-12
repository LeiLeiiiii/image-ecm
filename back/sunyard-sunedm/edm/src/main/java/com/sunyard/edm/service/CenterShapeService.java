package com.sunyard.edm.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.sunyard.framework.common.util.encryption.Sm2Util;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shiro.util.Assert;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.constant.DocDictionaryKeyConstants;
import com.sunyard.edm.dto.DocBsShapeAcceptDTO;
import com.sunyard.edm.dto.DocBsShapeLinkDTO;
import com.sunyard.edm.dto.DocBsShapeMeToDTO;
import com.sunyard.edm.dto.DocBsShapeOutsideLinkDTO;
import com.sunyard.edm.dto.DocBsShapeToMeDTO;
import com.sunyard.edm.mapper.DocBsDocumentMapper;
import com.sunyard.edm.mapper.DocBsShapeInsideUserMapper;
import com.sunyard.edm.mapper.DocBsShapeMapper;
import com.sunyard.edm.mapper.DocBsShapeOutsideLinkMapper;
import com.sunyard.edm.mapper.DocSysTeamUserMapper;
import com.sunyard.edm.po.DocBsDocument;
import com.sunyard.edm.po.DocBsShape;
import com.sunyard.edm.po.DocBsShapeInsideUser;
import com.sunyard.edm.po.DocBsShapeOutsideLink;
import com.sunyard.edm.po.DocSysTeamUser;
import com.sunyard.edm.util.DocUtils;
import com.sunyard.edm.vo.DocBsMessageVO;
import com.sunyard.edm.vo.DocBsShapeAcceptVO;
import com.sunyard.edm.vo.DocBsShapeAddVO;
import com.sunyard.edm.vo.DocBsShapeVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.encryption.RsaUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.system.api.DeptApi;
import com.sunyard.module.system.api.DictionaryApi;
import com.sunyard.module.system.api.InstApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysDeptDTO;
import com.sunyard.module.system.api.dto.SysInstDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;

/**
 * @Author PJW 2022/12/14 10:05
 * @DESC 文档中心-分享中心实现类
 */
@Service
public class CenterShapeService {

    private static Integer INSIDE = DocConstants.INSIDE;
    private static Integer OUTSIDE = DocConstants.OUTSIDE;
    private static Integer THREE = DocConstants.SHAPE_THREE;
    private static Integer WEEK = DocConstants.SHAPE_WEEK;
    private static Integer FOREVER = DocConstants.SHAPE_FOREVER;
    private static Integer ONE = DocConstants.SHAPE_ONE;
    private static Integer VALID = DocConstants.VALID;
    private static Integer INVALID = DocConstants.INVALID;

    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private DocBsDocumentMapper docBsDocumentMapper;
    @Resource
    private DocSysTeamUserMapper docSysTeamUserMapper;
    @Resource
    private DocBsShapeMapper docBsShapeMapper;
    @Resource
    private DocBsShapeInsideUserMapper docBsShapeInsideUserMapper;
    @Resource
    private DocBsShapeOutsideLinkMapper docBsShapeOutsideLinkMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private InstApi instApi;
    @Resource
    private DeptApi deptApi;
    @Resource
    private DictionaryApi sysDictionaryService;
    @Resource
    private WorkbenchService workbenchService;

    /**
     * 分享给我的-分享列表
     */
    public Result queryToMe(DocBsShapeVO s, PageForm p) {
        //如果没传文档类型、分享状态则直接返回空
        if (CollectionUtils.isEmpty(s.getDocType()) || CollectionUtils.isEmpty(s.getShapeState())) {
            return Result.success(new PageInfo<>());
        }

        if (!ObjectUtils.isEmpty(s.getShapeTimeDo())) {
            Date createEndDate = s.getShapeTimeDo();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(createEndDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date tomorrow = calendar.getTime();
            s.setShapeTimeDo(tomorrow);
        }
        //拿到当前用户的user_id、team_id、dept_id、inst_id
        List<Long> idList = new ArrayList<>();
        idList.add(s.getUserId());
        idList.add(s.getDeptId());
        idList.add(s.getInstId());
        List<DocSysTeamUser> teamUserList = docSysTeamUserMapper.selectList(new LambdaQueryWrapper<DocSysTeamUser>().eq(DocSysTeamUser::getUserId, s.getUserId()));
        teamUserList.forEach(item -> idList.add(item.getTeamId()));

        List<DocBsShapeInsideUser> docBsShapeInsideUserList = docBsShapeInsideUserMapper.selectList(new LambdaQueryWrapper<DocBsShapeInsideUser>().in(DocBsShapeInsideUser::getAcceptId, idList));
        List<Long> shapeIdList = docBsShapeInsideUserList.stream().map(DocBsShapeInsideUser::getShapeId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shapeIdList)) {
            return Result.success(new PageInfo<>());
        }
        Integer stats=2;
        if(s.getShapeState().size() == 1 && VALID.equals(s.getShapeState().get(0))){
            stats=VALID;
        }else if(s.getShapeState().size() == 1 && INVALID.equals(s.getShapeState().get(0))){
            stats=INVALID;
        }
        if (!org.springframework.util.ObjectUtils.isEmpty(s.getSharer())) {
            Result<List<SysUserDTO>> result = userApi
                    .getUserDetailByName(s.getSharer());
            List<Long> userIds = result.getData().stream().map(SysUserDTO::getUserId)
                    .collect(Collectors.toList());
            userIds.add(-Long.MAX_VALUE);
            s.setUserIds(userIds);
        }
        PageHelper.startPage(p.getPageNum(), p.getPageSize());
        List<DocBsShapeToMeDTO> result = docBsShapeMapper.queryToMe(s.getTagIdList(),s.getDocName(),
                s.getUserIds(),s.getShapeTimeTo(),s.getShapeTimeDo(),stats,new Date(),shapeIdList,s.getShapeTimeSort(),
                s.getInvalidTimeSort());

        if (!CollectionUtils.isEmpty(result)) {
            //如果‘文档格式’存在6
            if (s.getDocType().contains(Integer.valueOf(DocConstants.DOC_COMMON_SUFFIX_OTHER))) {
                List<String> allSuffixList = getAllSuffixList(s.getDocType());
                if (!CollectionUtils.isEmpty(allSuffixList)) {
                    result= result.stream().filter(r-> !allSuffixList.contains(r.getDocSuffix())).collect(Collectors.toList());
                }
            } else {
                List<String> suffixList = getSuffixList(s.getDocType());
                if(!CollectionUtils.isEmpty(suffixList)){
                    result=result.stream().filter(r-> suffixList.contains(r.getDocSuffix())).collect(Collectors.toList());

                }
            }
        }

        if (CollectionUtils.isEmpty(result)) {
            return Result.success(new PageInfo<>());
        }
        Set<Long> collect = result.stream()
                .map(DocBsShapeToMeDTO::getShapeUserId)
                .collect(Collectors.toSet());

        Result<List<SysUserDTO>> userListByUserIds = userApi.getUserListByUserIds(collect.toArray(new Long[0]));
        List<SysUserDTO> data = userListByUserIds.getData();

        Map<Long, String> userMap = data.stream()
                .collect(Collectors.toMap(SysUserDTO::getUserId, SysUserDTO::getName));

        for (DocBsShapeToMeDTO docBsShapeToMeDTO : result) {
            Long shapeUserId = docBsShapeToMeDTO.getShapeUserId();
            if (userMap.containsKey(shapeUserId)) {
                docBsShapeToMeDTO.setShapeUserName(userMap.get(shapeUserId));
            }
        }
        handleDocSizeToMe(result);
        return Result.success(new PageInfo<DocBsShapeToMeDTO>(result));
    }

    /**
     * 我的分享-分享列表
     */
    public Result queryMeTo(DocBsShapeVO s, PageForm p) {
        //如果没传文档类型、分享状态则直接返回空
        if (CollectionUtils.isEmpty(s.getDocType()) || CollectionUtils.isEmpty(s.getShapeState())) {
            return Result.success(new PageInfo<>());
        }

        if (!ObjectUtils.isEmpty(s.getShapeTimeDo())) {
            Date createEndDate = s.getShapeTimeDo();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(createEndDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date tomorrow = calendar.getTime();
            s.setShapeTimeDo(tomorrow);
        }
        List<Long> idList = new ArrayList<>();
        idList.add(s.getUserId());
        idList.add(s.getDeptId());
        idList.add(s.getInstId());
        List<DocSysTeamUser> teamUserList = docSysTeamUserMapper.selectList(new LambdaQueryWrapper<DocSysTeamUser>().eq(DocSysTeamUser::getUserId, s.getUserId()));
        teamUserList.forEach(item -> idList.add(item.getTeamId()));
        Integer stats=2;
        if(s.getShapeState().size() == 1 && VALID.equals(s.getShapeState().get(0))){
            stats=VALID;
        }else if(s.getShapeState().size() == 1 && INVALID.equals(s.getShapeState().get(0))){
            stats=INVALID;
        }
        PageHelper.startPage(p.getPageNum(), p.getPageSize());
        List<DocBsShapeMeToDTO> result = docBsShapeMapper.queryMeTo(idList, s.getTagIdList(),
                s.getDocName(),s.getSharer(),s.getShapeTimeTo(),s.getShapeTimeDo(),stats,new Date(),
                s.getUserId(),s.getShapeType(),s.getShapeTimeSort(),s.getInvalidTimeSort()
                );

        if (!CollectionUtils.isEmpty(result)) {
            //如果‘文档格式’存在6
            if (s.getDocType().contains(Integer.valueOf(DocConstants.DOC_COMMON_SUFFIX_OTHER))) {
                List<String> allSuffixList = getAllSuffixList(s.getDocType());
                if (!CollectionUtils.isEmpty(allSuffixList)) {
                    result= result.stream().filter(r-> !allSuffixList.contains(r.getDocSuffix())).collect(Collectors.toList());
                }
            } else {
                List<String> suffixList = getSuffixList(s.getDocType());
                if(!CollectionUtils.isEmpty(suffixList)){
                    result=result.stream().filter(r-> suffixList.contains(r.getDocSuffix())).collect(Collectors.toList());

                }
            }
        }
        if (CollectionUtils.isEmpty(result)) {
            return Result.success(new PageInfo<>());
        }
        handleDocSizeMeTo(result);
        return Result.success(new PageInfo<DocBsShapeMeToDTO>(result));
    }

    /**
     * 我的分享-分享对象详情
     */
    public Result<List<DocBsShapeAcceptDTO>> queryAccept(Long shapeId) {
        Assert.notNull(shapeId, "参数错误");
        List<DocBsShapeAcceptDTO> docBsShapeAcceptDTOS = docBsShapeInsideUserMapper.searchListByShapeId(shapeId);
        List<Long> acceptIdCollect = docBsShapeAcceptDTOS.stream().map(DocBsShapeAcceptDTO::getAcceptId).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(acceptIdCollect)){
            return Result.success(docBsShapeAcceptDTOS);
        }
        Result<List<SysUserDTO>> userListByUserIds = userApi.getUserListByUserIds(acceptIdCollect.toArray(new Long[acceptIdCollect.size()]));
        Result<List<SysInstDTO>> instsByInstIds = instApi.getInstsByInstIds(acceptIdCollect.toArray(new Long[acceptIdCollect.size()]));
        Result<List<SysDeptDTO>> listResult = deptApi.selectByIds(acceptIdCollect.toArray(new Long[acceptIdCollect.size()]));
        List<Long> userIdCollect = userListByUserIds.getData().stream().map(SysUserDTO::getUserId).collect(Collectors.toList());
        List<Long> instIdCollect = instsByInstIds.getData().stream().map(SysInstDTO::getInstId).collect(Collectors.toList());
        List<Long> deptIdCollect = listResult.getData().stream().map(SysDeptDTO::getDeptId).collect(Collectors.toList());
        //过滤出分享给团队的
        List<DocBsShapeAcceptDTO> docBsShapeAcceptDTOSNew = docBsShapeAcceptDTOS.stream()
                .filter(s -> !userIdCollect.contains(s.getAcceptId()))
                .filter(s -> !instIdCollect.contains(s.getAcceptId()))
                .filter(s -> !deptIdCollect.contains(s.getAcceptId()))
                .collect(Collectors.toList());
        //填充用户,部门,机构对象名称和类型
        getDocBsShapeAcceptInfo(userListByUserIds, instsByInstIds, listResult, docBsShapeAcceptDTOSNew);
        return Result.success(docBsShapeAcceptDTOSNew);
    }

    private void getDocBsShapeAcceptInfo(Result<List<SysUserDTO>> userListByUserIds, Result<List<SysInstDTO>> instsByInstIds, Result<List<SysDeptDTO>> listResult, List<DocBsShapeAcceptDTO> docBsShapeAcceptDTOSNew) {
        for (SysUserDTO sysUserDTO:userListByUserIds.getData()) {
            DocBsShapeAcceptDTO dto=new DocBsShapeAcceptDTO();
            dto.setAcceptName(sysUserDTO.getName());
            dto.setAcceptType(0);
            docBsShapeAcceptDTOSNew.add(dto);
        }
        for (SysInstDTO sysInstDTO:instsByInstIds.getData()) {
            DocBsShapeAcceptDTO dto=new DocBsShapeAcceptDTO();
            dto.setAcceptName(sysInstDTO.getName());
            dto.setAcceptType(1);
            docBsShapeAcceptDTOSNew.add(dto);
        }
        for (SysDeptDTO sysDeptDTO:listResult.getData()) {
            DocBsShapeAcceptDTO dto=new DocBsShapeAcceptDTO();
            dto.setAcceptName(sysDeptDTO.getName());
            dto.setAcceptType(2);
            docBsShapeAcceptDTOSNew.add(dto);
        }
    }

    /**
     * 校验有效期
     */
    public Result checkValid(Long shapeId) {
        Assert.notNull(shapeId, "参数错误");
        DocBsShape docBsShape = docBsShapeMapper.selectById(shapeId);
        Assert.notNull(docBsShape, "无效数据");
        if (docBsShape.getInvalidTime().before(new Date())) {
            return Result.success("失效");
        }
        return Result.success("有效");
    }

    /**
     * 我的分享-外部分享-查询连接、密码
     */
    public Result queryLink(Long shapeId) {
        Assert.notNull(shapeId, "参数错误");
        DocBsShapeOutsideLink docBsShapeOutsideLink = docBsShapeOutsideLinkMapper.selectOne(new LambdaQueryWrapper<DocBsShapeOutsideLink>().eq(DocBsShapeOutsideLink::getShareId, shapeId));
        return Result.success(docBsShapeOutsideLink);
    }

    /**
     * 添加分享
     */
    @Lock4j(keys = "#u")
    public Result<DocBsShapeLinkDTO> addShape(DocBsShapeAddVO s, Long u, String userName) {
        Assert.notNull(s.getShapeSection(), "参数错误");
        Assert.notNull(s.getShapeType(), "参数错误");
        if (INSIDE.equals(s.getShapeType())) {
            return addShapeInside(s, u, userName);
        } else if (OUTSIDE.equals(s.getShapeType())) {
            return addShapeOutSide(s, u);
        } else {
            return Result.error("未知分享类型", ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 我的分享-取消分享
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#shapeId")
    public Result cancelShape(Long shapeId) {
        Assert.notNull(shapeId, "参数错误");
        //修改到期时间为当前时间
        DocBsShape docBsShape = new DocBsShape();
        docBsShape.setShapeId(shapeId);
        docBsShape.setInvalidTime(new Date());
        docBsShapeMapper.updateById(docBsShape);
        return Result.success(true);
    }

    /**
     * 添加浏览次数
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#shapeId")
    public Result addShapePreview(Long shapeId, Integer type) {
        Assert.notNull(shapeId, "参数错误");
        if (DocConstants.ONE.equals(type)) {
            DocBsShape docBsShape = docBsShapeMapper.selectById(shapeId);
            docBsShapeMapper.update(new LambdaUpdateWrapper<DocBsShape>()
                    .set(DocBsShape::getShapePreview,docBsShape.getShapePreview()+1)
                    .eq(DocBsShape::getShapeId,docBsShape.getShapeId()));
        }
        return Result.success(true);
    }

    /**
     * 如果外链分享时单次，则通过次方法进行失效处理
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#shapeId")
    public Result changeLinkValid(Long shapeId) {
        Assert.notNull(shapeId, "参数错误");
        DocBsShape docBsShape = docBsShapeMapper.selectById(shapeId);
        if (ONE.equals(docBsShape.getShapeSection())) {
            DocBsShape shape = new DocBsShape();
            shape.setShapeId(shapeId);
            shape.setInvalidTime(new Date());
            docBsShapeMapper.updateById(shape);
        }
        return Result.success(true);
    }

    /**
     * 内部分享
     *
     */
    @Transactional(rollbackFor = Exception.class)
    public Result addShapeInside(DocBsShapeAddVO s, Long u, String userName) {
        Assert.notEmpty(s.getShapeAcceptList(), "参数错误");
        Assert.notEmpty(s.getDocIdList(), "参数错误");
        Assert.isTrue(!ONE.equals(s.getShapeSection()), "参数错误");
        //统一时间
        Date date = new Date();
        //获取到期时间
        Object o = calculationInvalidTime(s.getShapeSection(), date);
        if (o instanceof Result) {
            return (Result) o;
        }

        //消息通知对象
        List<DocBsMessageVO> shapeMsgBeanList = new ArrayList<>();
        //拿到分享记录beanList
        List<DocBsShape> docBsShapeList = new ArrayList<>();
        //拿到分享-对象beanList
        List<DocBsShapeInsideUser> docBsShapeInsideUserList = new ArrayList<>();
        //把分享对象全部转成用户id，用户消息通知
        List<Long> acceptUserId = getAcceptUserId(s.getShapeAcceptList());
        s.getDocIdList().forEach(item -> {
            //拿到分享记录bean
            DocBsShape docBsShape = new DocBsShape();
            long shapeId = snowflakeUtil.nextId();
            docBsShape.setShapeId(shapeId);
            docBsShape.setShapeUserId(u);
            docBsShape.setShapeType(INSIDE);
            docBsShape.setShapeSection(s.getShapeSection());
            docBsShape.setShapePreview(0);
            docBsShape.setDocId(item);
            docBsShape.setShapeTime(date);
            docBsShape.setInvalidTime((Date) o);
            docBsShapeList.add(docBsShape);
            //拿到分享-对象bean
            s.getShapeAcceptList().forEach(index -> {
                DocBsShapeInsideUser docBsShapeInsideUser = new DocBsShapeInsideUser();
                docBsShapeInsideUser.setId(snowflakeUtil.nextId());
                docBsShapeInsideUser.setAcceptType(index.getAcceptType());
                docBsShapeInsideUser.setAcceptId(index.getAcceptId());
                docBsShapeInsideUser.setShapeId(shapeId);
                docBsShapeInsideUserList.add(docBsShapeInsideUser);
            });
            //拿到消息通知对象
            shapeMsgBeanList.addAll(createShapeMsgBean(s.getShapeAcceptList(), acceptUserId, item, userName));
        });
        MybatisBatch<DocBsShape> docBsShapeMybatisBatch = new MybatisBatch<>(sqlSessionFactory, docBsShapeList);
        MybatisBatch.Method<DocBsShape> docBsShapeMethod = new MybatisBatch.Method<>(DocBsShapeMapper.class);
        docBsShapeMybatisBatch.execute(docBsShapeMethod.insert());

        MybatisBatch<DocBsShapeInsideUser> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, docBsShapeInsideUserList);
        MybatisBatch.Method<DocBsShapeInsideUser> method = new MybatisBatch.Method<>(DocBsShapeInsideUserMapper.class);
        mybatisBatch.execute(method.insert());
        if (DocConstants.MSG_YES.equals(s.getIsMsg())) {
            workbenchService.addMessageBatch(shapeMsgBeanList);
        }
        return Result.success(true);
    }

    /**
     * 外部分享
     *
     */
    @Transactional(rollbackFor = Exception.class)
    public Result addShapeOutSide(DocBsShapeAddVO s, Long u) {
        Assert.notEmpty(s.getDocIdList(), "参数错误");
        Assert.isTrue(s.getDocIdList().size() == 1, "仅限单文档外部分享");
        long shapeId = snowflakeUtil.nextId();
        //统一时间
        Date date = new Date();
        //获取到期时间
        Object o = calculationInvalidTime(s.getShapeSection(), date);
        if (o instanceof Result) {
            return (Result) o;
        }
        //获取加密后的密码
        Object pwd = getPwd();
        if (pwd instanceof Result) {
            return (Result) pwd;
        }
        Long docId = s.getDocIdList().get(0);
        //获取加密后的url
        Object url = getUrl(shapeId, docId, (String) pwd, s.getShapeLinkType());
        if (url instanceof Result) {
            return (Result) url;
        }

        //拿到分享记录bean
        DocBsShape docBsShape = new DocBsShape();
        docBsShape.setShapeId(shapeId);
        docBsShape.setShapeUserId(u);
        docBsShape.setShapeType(OUTSIDE);
        docBsShape.setShapeSection(s.getShapeSection());
        docBsShape.setShapePreview(0);
        docBsShape.setDocId(docId);
        docBsShape.setShapeTime(date);
        docBsShape.setInvalidTime((Date) o);
        //拿到分享-外链bean
        DocBsShapeOutsideLink docBsShapeOutsideLink = new DocBsShapeOutsideLink();
        docBsShapeOutsideLink.setLinkId(snowflakeUtil.nextId());
        try {
            docBsShapeOutsideLink.setLinkPwd(DocConstants.OUT_PUBLIC.equals(s.getShapeLinkType()) ? null : RsaUtils.encrypt((String) pwd));
        } catch (Exception e) {
            return Result.error("密码加密错误！", ResultCode.SYSTEM_ERROR);
        }
        docBsShapeOutsideLink.setLinkUrl((String) url);
        docBsShapeOutsideLink.setLinkType(s.getShapeLinkType());
        docBsShapeOutsideLink.setShareId(shapeId);

        docBsShapeMapper.insert(docBsShape);
        docBsShapeOutsideLinkMapper.insert(docBsShapeOutsideLink);
        //把外链bean返回
        DocBsShapeOutsideLinkDTO docBsShapeOutsideLinkDTO = new DocBsShapeOutsideLinkDTO();
        BeanUtils.copyProperties(docBsShapeOutsideLink, docBsShapeOutsideLinkDTO);
        Result<List<SysUserDTO>> user = userApi.getUserListByUserIds(new Long[]{u});
        docBsShapeOutsideLinkDTO.setShareUserName(user.getData().get(0).getName());
        docBsShapeOutsideLinkDTO.setShareSection(s.getShapeSection());
        Result<Map<String, String>> mapResult = sysDictionaryService.getDescByKey(DocDictionaryKeyConstants.DOC_SHAPE_SECTION);
        Map<String, String> map = mapResult.getData();
        String shareTime="";
        switch (s.getShapeSection()) {
            case 0:
                shareTime=DocDictionaryKeyConstants.DOC_SHAPE_SECTION_THREE;
                break;
            case 1:
                shareTime=DocDictionaryKeyConstants.DOC_SHAPE_SECTION_WEEK;
                break;
            case 2:
                shareTime=DocDictionaryKeyConstants.DOC_SHAPE_SECTION_FOREVER;
                break;
            case 3:
                shareTime=DocDictionaryKeyConstants.DOC_SHAPE_SECTION_ONE;
                break;
        }
        docBsShapeOutsideLinkDTO.setShareSectionStr(map.get(shareTime));
        return Result.success(docBsShapeOutsideLinkDTO);
    }

    /**
     * 计算到期时间
     */
    private Object calculationInvalidTime(Integer section, Date shapeTime) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(shapeTime);
        if (THREE.equals(section)) {
            instance.add(Calendar.DATE, 3);
        } else if (WEEK.equals(section)) {
            instance.add(Calendar.WEEK_OF_YEAR, 1);
        } else if (FOREVER.equals(section) || ONE.equals(section)) {
            instance.setTime(new Date(new GregorianCalendar(9999, Calendar.DECEMBER, 31).getTimeInMillis()));
        } else {
            return Result.error("未知有效期", ResultCode.PARAM_ERROR);
        }
        return instance.getTime();
    }

    /**
     * 生成密码
     *
     * @return
     */
    private Object getPwd() {
        String s = RandomStringUtils.randomAlphanumeric(6);
        return s;
    }

    /**
     * 获取外链的url
     */
    private Object getUrl(Long shapeId, Long docId, String pwd, Integer shapeType) {
        Map<String, String> map = new HashMap<>(DocConstants.SIXTEEN);
        map.put("shapeId", String.valueOf(shapeId));
        map.put("docId", String.valueOf(docId));
        if (DocConstants.OUT_PRIVATE.equals(shapeType)) {
            map.put("pwd", pwd);

        }
        StringBuffer url = new StringBuffer();
        try {
            url.append(RsaUtils.encrypt(JSONObject.toJSONString(map)));
        } catch (Exception e) {
            return Result.error("url生成错误！", ResultCode.SYSTEM_ERROR);
        }
        return url.toString();
    }

    /**
     * 计算size大小
     */
    private void handleDocSizeMeTo(List<DocBsShapeMeToDTO> docBsDocuments) {
        for (DocBsShapeMeToDTO documentExtend : docBsDocuments) {
            if (!ObjectUtils.isEmpty(documentExtend.getDocSize())) {
                documentExtend.setDocSizeStr(DocUtils.getFilseSize(Long.parseLong(documentExtend.getDocSize())));
            }
        }
    }

    private void handleDocSizeToMe(List<DocBsShapeToMeDTO> docBsDocuments) {
        for (DocBsShapeToMeDTO documentExtend : docBsDocuments) {
            if (!ObjectUtils.isEmpty(documentExtend.getDocSize())) {
                documentExtend.setDocSizeStr(DocUtils.getFilseSize(Long.parseLong(documentExtend.getDocSize())));
            }
        }
    }

    /**
     * 获取消息通知对象
     */
    private List<DocBsMessageVO> createShapeMsgBean(List<DocBsShapeAcceptVO> shapeAcceptList, List<Long> userId, Long docId, String userName) {
        //查询文档名称
        DocBsDocument docBsDocument = docBsDocumentMapper.selectById(docId);
        List<DocBsMessageVO> list = new ArrayList<>();
        List<Long> disUserId = userId.stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(disUserId)) {
            return list;
        }
        disUserId.forEach(i -> {
            DocBsMessageVO docBsMessageVo = new DocBsMessageVO();
            docBsMessageVo.setUserId(i);
            docBsMessageVo.setMessageType(DocConstants.DOC_MESSAGE_RANGE_SHAPE);
            docBsMessageVo.setMessageTitle("有文档分享给我");
            docBsMessageVo.setMessageContent(userName + "向您分享了文档（" + docBsDocument.getDocName() + "）");
            docBsMessageVo.setInformTime(new Date());
            list.add(docBsMessageVo);
        });
        return list;
    }

    /**
     * 拿到分享对象中所有的userid集合
     */
    private List<Long> getAcceptUserId(List<DocBsShapeAcceptVO> shapeAcceptList) {
        List<Long> userId = new ArrayList<>();
        shapeAcceptList.forEach(item -> {
            if (DocConstants.USER.equals(item.getAcceptType())) {
                userId.add(item.getAcceptId());
            } else if (DocConstants.TEAM.equals(item.getAcceptType())) {
                //查询团队下所有的人
                List<DocSysTeamUser> teamUserList = docSysTeamUserMapper.selectList(new LambdaQueryWrapper<DocSysTeamUser>().eq(DocSysTeamUser::getTeamId, item.getAcceptId()));
                userId.addAll(teamUserList.stream().map(DocSysTeamUser::getUserId).collect(Collectors.toList()));
            } else if (DocConstants.DEPT.equals(item.getAcceptType())) {
                //查询部门下所有的人
                Result<List<SysUserDTO>> deptUserList = userApi.getUserByDeptIdAndRoleId(item.getAcceptId(), null);
                userId.addAll(deptUserList.getData().stream().map(SysUserDTO::getUserId).collect(Collectors.toList()));
            } else if (DocConstants.INST.equals(item.getAcceptType())) {
                //查询机构下所有的人
                Result<List<SysUserDTO>> instUserList = userApi.getUsersByInstId(item.getAcceptId());
                userId.addAll(instUserList.getData().stream().map(SysUserDTO::getUserId).collect(Collectors.toList()));
            } else {
            }
        });
        return userId;
    }

    /**
     * 根据传入的docType 拿到后缀list
     */
    private List<String> getSuffixList(List<Integer> docType) {
        List<String> suffixList = new ArrayList<>();
        Result<Map<String, String>> mapResult = sysDictionaryService.searchValExtraMapByParentKey(DocDictionaryKeyConstants.DOC_COMMON_SUFFIX);
        Map<String, String> map = mapResult.getData();
        docType.forEach(item -> {
            suffixList.addAll(Arrays.asList(map.get(String.valueOf(item)).split(",")));
        });
        return suffixList;
    }

    /**
     * 根据传入的docType，过滤掉当前后缀list
     *
     * @return
     */
    private List<String> getAllSuffixList(List<Integer> docType) {
        List<String> suffixList = new ArrayList<>();
        Result<Map<String, String>> mapResult = sysDictionaryService.searchValExtraMapByParentKey(DocDictionaryKeyConstants.DOC_COMMON_SUFFIX);
        Map<String, String> map = mapResult.getData();
        docType.forEach(item -> {
            map.remove(String.valueOf(item));
        });
        map.forEach((key, value) -> {
            suffixList.addAll(Arrays.asList(map.get(String.valueOf(key)).split(",")));

        });
        return suffixList;
    }
}
