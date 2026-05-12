package com.sunyard.edm.service.share;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.shiro.util.Assert;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.dto.DocBsDocFlowDTO;
import com.sunyard.edm.dto.DocBsDocumentDTO;
import com.sunyard.edm.mapper.DocBsDocFlowMapper;
import com.sunyard.edm.mapper.DocBsDocRelMapper;
import com.sunyard.edm.mapper.DocBsDocumentMapper;
import com.sunyard.edm.mapper.DocBsShapeMapper;
import com.sunyard.edm.mapper.DocBsTagDocumentMapper;
import com.sunyard.edm.mapper.DocSysTagMapper;
import com.sunyard.edm.po.DocBsDocFlow;
import com.sunyard.edm.po.DocBsDocRel;
import com.sunyard.edm.po.DocBsDocument;
import com.sunyard.edm.po.DocBsShape;
import com.sunyard.edm.po.DocBsTagDocument;
import com.sunyard.edm.po.DocSysTag;
import com.sunyard.edm.service.CenterCommonService;
import com.sunyard.edm.service.WorkbenchService;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.module.storage.api.FileHandleApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysUserDTO;

import joptsimple.internal.Strings;

/**
 * @Author PJW 2022/12/14 10:05
 * @DESC 分享中心对外分享实现类
 */
@Service
public class CenterOutShapeService {

    private static Integer INSIDE = DocConstants.INSIDE;
    private static Integer OUTSIDE = DocConstants.OUTSIDE;
    private static Integer THREE = DocConstants.SHAPE_THREE;
    private static Integer WEEK = DocConstants.SHAPE_WEEK;
    private static Integer FOREVER = DocConstants.SHAPE_FOREVER;
    private static Integer ONE = DocConstants.SHAPE_ONE;
    private static Integer VALID = DocConstants.VALID;
    private static Integer INVALID = DocConstants.INVALID;

    @Resource
    private DocBsShapeMapper docBsShapeMapper;
    @Resource
    private DocBsDocRelMapper docBsDocRelMapper;
    @Resource
    private WorkbenchService workbenchService;
    @Resource
    private DocBsDocumentMapper docBsDocumentMapper;
    @Resource
    private CenterCommonService docCommonService;
    @Resource
    private DocBsDocFlowMapper docBsDocFlowMapper;
    @Resource
    private DocBsTagDocumentMapper docBsTagDocumentMapper;
    @Resource
    private DocSysTagMapper docSysTagMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private FileHandleApi fileHandleApi;


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
     * 获取详情
     */
    public DocBsDocumentDTO getInfo(Long busId, Long u) {
        DocBsDocumentDTO documentExtend = getDocBsDocumentDTO(busId, u);
        return documentExtend;
    }

    private DocBsDocumentDTO getDocBsDocumentDTO(Long busId, Long u) {
        AssertUtils.isNull(busId, "参数错误！");
        DocBsDocument document = docBsDocumentMapper.selectById(busId);
        AssertUtils.isNull(document, "当前文档不存在！");
        DocBsDocumentDTO documentExtend = new DocBsDocumentDTO();
        DocBsDocumentDTO documentExtend1 = new DocBsDocumentDTO();
        BeanUtils.copyProperties(document, documentExtend1);

       // Result<SysFileDTO> resultSysFileDTO = fileHandleApi.getFileInfo(document.getFileId());
        //SysFileDTO sysFileDTO = resultSysFileDTO.getData();
        //创建人处理
        if (document.getDocOwner() != null) {
            Result<SysUserDTO> result = userApi.getUserByUserId(document.getDocOwner());
            SysUserDTO sysUserDTO = result.getData();
            documentExtend1.setDocOwnerStr(sysUserDTO.getName());
        }

        if (document.getDocCreator() != null) {
            Result<SysUserDTO> result = userApi.getUserByUserId(document.getDocCreator());
            SysUserDTO sysUserDTO = result.getData();
            documentExtend1.setDocCreatorStr(sysUserDTO.getName());
        }

        //文件目录处理
        documentExtend1.setFolderIdAllStr(docCommonService.handleFolderAll(document.getFolderId()));

        documentExtend.setDocBsDocument(documentExtend1);
        //关联附件列表
        List<DocBsDocument> docBsDocuments = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                .eq(DocBsDocument::getRelDoc, busId)
                .eq(DocBsDocument::getType, DocConstants.FILE));
        List<DocBsDocumentDTO> list = PageCopyListUtils.copyListProperties(docBsDocuments, DocBsDocumentDTO.class);
        docCommonService.handleDocSize(list);
        documentExtend.setAttchList(list);

        //动态
        handleFlow(busId, documentExtend);

        handleInfo(busId, documentExtend, documentExtend1);
        //添加’最近打开‘记录
        if (!ObjectUtils.isEmpty(u)) {
            workbenchService.addRecently(busId, u);
        }
        return documentExtend;
    }

    /**
     * 企业详情处理
     *
     * @param busId
     * @param documentExtend
     * @param documentExtend1
     */
    private void handleInfo(Long busId, DocBsDocumentDTO documentExtend, DocBsDocumentDTO documentExtend1) {
        if (documentExtend1.getDocType().equals(DocConstants.COMPANY)) {
            //文档关联的标签
            List<DocBsTagDocument> tagDocuments = docBsTagDocumentMapper.selectList(new LambdaQueryWrapper<DocBsTagDocument>()
                    .eq(DocBsTagDocument::getDocId, busId));
            if (!CollectionUtils.isEmpty(tagDocuments)) {
                List<Long> tagIds = tagDocuments.stream().map(DocBsTagDocument::getTagId).collect(Collectors.toList());
                List<DocSysTag> tags = docSysTagMapper.selectList(new LambdaQueryWrapper<DocSysTag>()
                        .in(DocSysTag::getTagId, tagIds));
                if (!CollectionUtils.isEmpty(tags)) {
                    Map<Long, List<DocSysTag>> collect2 = tags.stream().collect(Collectors.groupingBy(DocSysTag::getTagId));

                    List<Long> collect1 = tags.stream().map(DocSysTag::getParentId).collect(Collectors.toList());
                    List<DocSysTag> tagList1 = docSysTagMapper.selectList(new LambdaQueryWrapper<DocSysTag>()
                            .in(DocSysTag::getTagId, collect1));
                    tagList1.addAll(tags);
                    Map<Long, List<DocSysTag>> collect3 = tagList1.stream().collect(Collectors.groupingBy(DocSysTag::getTagId));

                    List<String> list = new ArrayList<>();
                    for (Long id : collect2.keySet()) {
                        List<DocSysTag> tagList = collect2.get(id);
                        List<DocSysTag> all = new ArrayList();
                        for (DocSysTag tag : tagList) {
                            if (tag.getParentId() != DocConstants.ZERO.intValue()) {
                                DocSysTag tag1 = collect3.get(tag.getParentId()).get(0);
                                all.add(tag1);
                            } else {
                                all.add(tag);
                            }
                        }
                        //重新排序
                        all = all.stream()
                                .sorted(Comparator.comparing(DocSysTag::getTagLevel)).collect(Collectors.toList());

                        List<String> collect = all.stream().map(DocSysTag::getTagName).collect(Collectors.toList());
                        String join = Strings.join(collect, "/");
                        list.add(join);
                    }

                    documentExtend.setTagIdStr(Strings.join(list, "，"));
                    documentExtend.setDocSysTags(tags);
                }
            }

            //文档关联的文档
            List<DocBsDocRel> docBsDocRels = docBsDocRelMapper.selectList(new LambdaQueryWrapper<DocBsDocRel>().eq(DocBsDocRel::getDocId, busId).or().eq(DocBsDocRel::getRelId, busId));
            if (!CollectionUtils.isEmpty(docBsDocRels)) {
                List<Long> collect = docBsDocRels.stream().map(DocBsDocRel::getRelId).collect(Collectors.toList());
                collect.addAll(docBsDocRels.stream().map(DocBsDocRel::getDocId).collect(Collectors.toList()));
                List<DocBsDocument> relDoc = docBsDocumentMapper.selectList(new LambdaQueryWrapper<DocBsDocument>()
                        .in(DocBsDocument::getBusId, collect).ne(DocBsDocument::getBusId, busId));
                //计算文件大小
                List<DocBsDocumentDTO> docBsDocuments1 = PageCopyListUtils.copyListProperties(relDoc, DocBsDocumentDTO.class);
                docCommonService.handleDocSize(docBsDocuments1);
                documentExtend.setDocumentList(docBsDocuments1);
            }

        }
    }

    /**
     * 动态处理
     *
     * @param busId
     * @param documentExtend
     */
    private void handleFlow(Long busId, DocBsDocumentDTO documentExtend) {
        List<DocBsDocFlow> flows = docBsDocFlowMapper.selectList(new LambdaQueryWrapper<DocBsDocFlow>()
                .eq(DocBsDocFlow::getDocId, busId));
        if (!CollectionUtils.isEmpty(flows)) {
            List<DocBsDocFlowDTO> docBsDocFlowDTOS = PageCopyListUtils.copyListProperties(flows, DocBsDocFlowDTO.class);
            List<Long> userIds = docBsDocFlowDTOS.stream().map(DocBsDocFlowDTO::getUserId).collect(Collectors.toList());
            Result<List<SysUserDTO>> userId = userApi.getUserListByUserIds(userIds.toArray(new Long[userIds.size()]));
            Map<Long, List<SysUserDTO>> collect = userId.getData().stream().collect(Collectors.groupingBy(SysUserDTO::getUserId));
            docBsDocFlowDTOS.forEach(s -> {
                List<SysUserDTO> sysUsers = collect.get(s.getUserId());
                if (!CollectionUtils.isEmpty(sysUsers)) {
                    s.setUserIdStr(sysUsers.get(0).getName());
                }
            });
            documentExtend.setDocBsDocFlowList(docBsDocFlowDTOS);
        }
    }

}
