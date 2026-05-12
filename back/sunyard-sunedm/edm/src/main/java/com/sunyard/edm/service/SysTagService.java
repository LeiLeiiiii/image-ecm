package com.sunyard.edm.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.edm.constant.DocConstants;
import com.sunyard.edm.dto.DocSysTagDTO;
import com.sunyard.edm.dto.PromptDTO;
import com.sunyard.edm.mapper.DocBsTagDocumentMapper;
import com.sunyard.edm.mapper.DocSysTagMapper;
import com.sunyard.edm.po.DocBsTagDocument;
import com.sunyard.edm.po.DocSysTag;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * @author raochangmei
 * @date 11.15
 * @Desc 系统管理-标签管理实现类
 */
@Service("docSysTagService")
public class SysTagService {
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private DocSysTagMapper tagMapper;
    @Resource
    private DocBsTagDocumentMapper tagDocumentMapper;


    /**
     * 查询列表
     */
    public List<DocSysTagDTO> selectTag(String tagName) {
        if (ObjectUtils.isEmpty(tagName)) {
            List<DocSysTag> sysTagList = tagMapper.selectList(new LambdaQueryWrapper<DocSysTag>()
                    .eq(DocSysTag::getParentId, DocConstants.ZERO)
                    .eq(DocSysTag::getTagLevel, DocConstants.ZERO)
                    .orderByDesc(DocSysTag::getUpdateTime));
            List<DocSysTagDTO> sysTagExtendList = PageCopyListUtils.copyListProperties(sysTagList, DocSysTagDTO.class);
            isHaveChild(sysTagExtendList);
            return sysTagExtendList;
        } else {
            //标签树结构 最初始的数据
            List<DocSysTag> sysTagList = tagMapper.selectList(new LambdaQueryWrapper<DocSysTag>()
                    .like(DocSysTag::getTagName, tagName)
                    .orderByDesc(DocSysTag::getUpdateTime));
            //获取父级id
            List<Long> parentIdList = sysTagList.stream().map(DocSysTag::getParentId).collect(Collectors.toList());
            //获取tag_id
            List<Long> tagIdList = sysTagList.stream().map(DocSysTag::getTagId).collect(Collectors.toList());
            //拿到最子级tagId
            Collection<Long> tagIdAndParentId = CollectionUtils.disjunction(parentIdList, tagIdList);

            //根据最子级标签id获取最高级的tag_id
            List<Long> tagIds = new ArrayList<>();
            //最大级别
            Integer tagLevel = DocConstants.ZERO;
            for (Long tagId : tagIdAndParentId) {
                List<DocSysTag> sysTags = tagMapper.selectList(new LambdaQueryWrapper<DocSysTag>()
                        .eq(DocSysTag::getTagId, tagId)
                        .ne(DocSysTag::getParentId, DocConstants.ZERO)
                        .orderByDesc(DocSysTag::getTagLevel));
                if (!CollectionUtils.isEmpty(sysTags)) {
                    tagIds.add(sysTags.get(0).getParentId());
                    if (sysTags.get(0).getTagLevel() > tagLevel) {
                        tagLevel = sysTags.get(0).getTagLevel();
                    }
                } else {
                    tagIds.add(tagId);
                }
            }
            //第一级
            List<DocSysTag> docSysTags = new ArrayList<>();
            if (!ObjectUtils.isEmpty(tagIds)) {
                docSysTags = tagMapper.selectList(new LambdaQueryWrapper<DocSysTag>()
                        .in(DocSysTag::getTagId, tagIds)
                        .eq(DocSysTag::getTagLevel, DocConstants.ZERO)
                        .orderByDesc(DocSysTag::getUpdateTime));
            }
            List<DocSysTagDTO> sysTagExtendList = PageCopyListUtils.copyListProperties(docSysTags, DocSysTagDTO.class);
            //递归赋值子标签
            tagIdList.addAll(parentIdList);
            handleChildren(sysTagExtendList, tagLevel, tagIdList);
            return sysTagExtendList;
        }
    }

    /**
     * 查询子标签
     */
    public List<DocSysTagDTO> selectChild(Long tagId) {
        AssertUtils.isNull(tagId, "参数错误！");
        List<DocSysTag> sysTagList = tagMapper.selectList(new LambdaQueryWrapper<DocSysTag>().eq(DocSysTag::getParentId, tagId)
                .eq(DocSysTag::getTagLevel, DocConstants.ZERO)
                .orderByDesc(DocSysTag::getUpdateTime));
        List<DocSysTagDTO> sysTagExtendList = PageCopyListUtils.copyListProperties(sysTagList, DocSysTagDTO.class);
        //判断是否存在子标签
        isHaveChild(sysTagExtendList);
        return sysTagExtendList;
    }

    /**
     * 添加标签
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#tag.tagName")
    public Integer addTag(DocSysTag tag) {
        AssertUtils.isNull(tag.getTagName(), "标签名称不为空！");
        checkTagName(tag);

        List<DocSysTag> sysTagList = tagMapper.selectList(
                new LambdaQueryWrapper<DocSysTag>().eq(DocSysTag::getTagId, tag.getParentId()).orderByAsc(DocSysTag::getTagLevel));
        List<DocSysTag> list = new ArrayList<>();

        //生成的标签id
        Long tagId = snowflakeUtil.nextId();
        //获取tagCode
        String tagCode = getPingYin(tag.getTagName()) + "-" + tagId;
        //级别
        Integer level = DocConstants.ZERO;
        for (DocSysTag tag1 : sysTagList) {
            level++;
            DocSysTag docSysTag = new DocSysTag();
            docSysTag.setTagCode(tagCode);
            docSysTag.setParentId(tag1.getParentId());
            docSysTag.setTagName(tag.getTagName());
            docSysTag.setTagLevel(level);
            docSysTag.setTagId(tagId);
            docSysTag.setTagSequen(tag.getTagSequen());
            docSysTag.setRemark(tag.getRemark());
            docSysTag.setUpdateTime(new Date());
            list.add(docSysTag);
        }
        DocSysTag newTag = new DocSysTag();
        newTag.setTagCode(tagCode);
        newTag.setTagName(tag.getTagName());
        newTag.setParentId(tag.getParentId());
        newTag.setTagSequen(tag.getTagSequen());
        newTag.setRemark(tag.getRemark());
        newTag.setTagLevel(DocConstants.ZERO);
        newTag.setUpdateTime(new Date());
        newTag.setTagId(tagId);
        list.add(newTag);
        MybatisBatch<DocSysTag> docBatchs = new MybatisBatch<>(sqlSessionFactory, list);
        MybatisBatch.Method<DocSysTag> docMethod = new MybatisBatch.Method<>(DocSysTagMapper.class);
        docBatchs.execute(docMethod.insert());
        return list.size();
    }

    /**
     * 编辑标签
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#tag.tagName")
    public Integer updateTag(DocSysTag tag) {
        AssertUtils.isNull(tag.getTagName(), "标签名称不为空！");
        AssertUtils.isNull(tag.getTagId(), "参数错误！");
        checkTagName(tag);
        //标签名转为拼音
        String tagCode = getPingYin(tag.getTagName()) + "-" + tag.getTagId();
        return tagMapper.update(null,
                new LambdaUpdateWrapper<DocSysTag>().eq(DocSysTag::getTagId, tag.getTagId())
                        .set(DocSysTag::getTagName, tag.getTagName())
                        .set(DocSysTag::getTagCode, tagCode)
                        .set(DocSysTag::getRemark, tag.getRemark()));
    }

    /**
     * 删除提示
     */
    public PromptDTO delPrompt(Long[] tagIds) {
        List<Long> tagIdList = getTagIdList(tagIds);
        PromptDTO promptDTO = new PromptDTO();
        //标签个数
        promptDTO.setTagNum(tagIdList.size());
        //是否有关联文档
        List<DocBsTagDocument> tagDocuments = tagDocumentMapper.selectList(new LambdaQueryWrapper<DocBsTagDocument>()
                .in(DocBsTagDocument::getTagId, tagIdList));

        if (!CollectionUtils.isEmpty(tagDocuments)) {
            promptDTO.setIsRelDoc(DocConstants.ONE);
        } else {
            promptDTO.setIsRelDoc(DocConstants.ZERO);
        }
        return promptDTO;
    }

    /**
     * 批量删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delBatchTag(Long[] tagIds) {
        List<Long> tagIdList = getTagIdList(tagIds);
        //删除标签
        tagMapper.delete(new LambdaQueryWrapper<DocSysTag>().in(DocSysTag::getTagId, tagIdList));
        //删除标签与文档的关系
        tagDocumentMapper.delete(new LambdaQueryWrapper<DocBsTagDocument>().in(DocBsTagDocument::getTagId, tagIdList));
    }

    /**
     * 选择上级标签树
     */
    public List<DocSysTagDTO> getTagTree() {
        List<DocSysTagDTO> tagTree = tagMapper.searchTagTree(null);
        for (DocSysTagDTO tagExtend : tagTree) {
            tagExtend.setId(tagExtend.getTagId());
        }
        return tagTree;
    }

    /**
     * 编辑时，标签的回显
     */
    public Map selectUpdateTag(Long docId) {
        AssertUtils.isNull(docId, "参数错误！");
        List<DocBsTagDocument> tagDocuments = tagDocumentMapper.selectList(new LambdaQueryWrapper<DocBsTagDocument>()
                .eq(DocBsTagDocument::getDocId, docId));
        if (CollectionUtils.isEmpty(tagDocuments)) {
            //第一层
            List<DocSysTag> docSysTags = tagMapper.selectList(new LambdaQueryWrapper<DocSysTag>()
                    .eq(DocSysTag::getParentId, DocConstants.ZERO)
                    .eq(DocSysTag::getTagLevel, DocConstants.ZERO));
            List<DocSysTagDTO> frist1 = PageCopyListUtils.copyListProperties(docSysTags, DocSysTagDTO.class);
            Map map = new HashMap(DocConstants.SIXTEEN);
            map.put("list", frist1);
            return map;
        }
        List<Long> collect = tagDocuments.stream().map(DocBsTagDocument::getTagId).collect(Collectors.toList());
        List<DocSysTag> tagId = tagMapper.selectList(new LambdaQueryWrapper<DocSysTag>().in(DocSysTag::getTagId, collect));
        List<Long> collect3 = tagId.stream().map(DocSysTag::getParentId).collect(Collectors.toList());

        Map<Long, List<DocSysTag>> collect1 = tagId.stream().collect(Collectors.groupingBy(DocSysTag::getTagId));
        List<DocSysTag> tag = tagMapper.selectList(new LambdaQueryWrapper<DocSysTag>().in(DocSysTag::getTagId, collect3).eq(DocSysTag::getTagLevel, DocConstants.ZERO));
        List<Long> tags = tag.stream().map(DocSysTag::getTagId).collect(Collectors.toList());
        List<DocSysTag> collect4 = tagId.stream().filter(s -> s.getTagLevel().equals(DocConstants.ZERO)).collect(Collectors.toList());
        collect4.forEach(s -> {
            if (!tags.contains(s.getTagId())) {
                tag.add(s);
            }
        });
        List<List<Long>> list = new ArrayList<>();
        for (Long id : collect) {
            List<DocSysTag> docSysTags = collect1.get(id);
            List<Long> collect2 = docSysTags.stream().filter(s -> !s.getParentId().equals(DocConstants.ZERO.longValue())).sorted(Comparator.comparing(DocSysTag::getTagLevel, Comparator.nullsLast(Integer::compareTo)).reversed()).map(DocSysTag::getParentId).collect(Collectors.toList());
            collect2.add(id);
            if (!CollectionUtils.isEmpty(collect2)) {
                list.add(collect2);
            }
        }
        List<Long> collect2 = tag.stream().map(DocSysTag::getTagId).collect(Collectors.toList());
        List<DocSysTagDTO> docSysTagDTOS = PageCopyListUtils.copyListProperties(tag, DocSysTagDTO.class);
        Map<Long, List<DocSysTagDTO>> collect6 = docSysTagDTOS.stream().collect(Collectors.groupingBy(DocSysTagDTO::getParentId));
        Set<Long> longs = collect6.keySet();
        List<DocSysTag> docSysTags = tagMapper.selectList(new LambdaQueryWrapper<DocSysTag>()
                .in(DocSysTag::getParentId, longs)
                .eq(DocSysTag::getTagLevel, DocConstants.ZERO)
                .notIn(DocSysTag::getTagId, collect2));
        List<DocSysTagDTO> frist = PageCopyListUtils.copyListProperties(docSysTags, DocSysTagDTO.class);
        docSysTagDTOS.addAll(frist);
        //整理数据
        List<DocSysTagDTO> children = getChildren(docSysTagDTOS);
        Map map = new HashMap(DocConstants.SIXTEEN);
        map.put("list", children);
        map.put("checklist", list);
        return map;
    }


    public List<DocSysTagDTO> getChildren(List<DocSysTagDTO> ret) {
        if (org.springframework.util.CollectionUtils.isEmpty(ret)) {
            return null;
        }
        ret.sort(Comparator.comparing(DocSysTagDTO::getTagLevel));
        Map<Long, List<DocSysTagDTO>> collect = ret.stream().filter(s -> s.getParentId() != null).collect(Collectors.groupingBy(DocSysTagDTO::getParentId));
        Map<Long, List<DocSysTagDTO>> collect2 = ret.stream().collect(Collectors.groupingBy(DocSysTagDTO::getParentId));
        List<DocSysTagDTO> list2 = collect2.get(DocConstants.ZERO.longValue());
        for (DocSysTagDTO documentExtend : list2) {
            //第一层
            handleChildren(collect, documentExtend);
        }
        return list2;

    }

    private void handleChildren(Map<Long, List<DocSysTagDTO>> collect1, DocSysTagDTO documentExtend) {
        List<DocSysTagDTO> list = collect1.get(documentExtend.getTagId());
        if (org.springframework.util.CollectionUtils.isEmpty(list)) {
            return;
        } else {
            documentExtend.setChildren(list);
            for (DocSysTagDTO d : list) {
                handleChildren(collect1, d);
            }
        }
    }

    /**
     * 中文转拼音  其它字符类型不变
     *
     */
    private String getPingYin(String tagName) {

        //创建转换对象
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        //转换类型（大写or小写）
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        //定义中文声调的输出格式
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        //定义字符的输出格式
        format.setVCharType(HanyuPinyinVCharType.WITH_U_AND_COLON);

        //转换为字节数组
        char[] input = tagName.trim().toCharArray();
        // 用StringBuffer（字符串缓冲）来接收处理的数据
        StringBuilder outPut = new StringBuilder();

        try {
            for (int i = 0; i < input.length; i++) {
                //判断是否是一个汉子字符
                if (String.valueOf(input[i]).matches("[\\u4E00-\\u9FA5]+")) {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
                    outPut.append(temp[0]);
                } else {
                    // 如果不是汉字字符，直接拼接
                    outPut.append(input[i]);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        return outPut.toString();
    }

    /**
     * 判断是否存在子标签
     */
    private void isHaveChild(List<DocSysTagDTO> sysTagExtendList) {
        if (CollectionUtils.isEmpty(sysTagExtendList)) {
            return;
        }
        //告诉前端是否存在子标签
        for (DocSysTagDTO sysTagExtend : sysTagExtendList) {
            List<DocSysTag> sysTags = tagMapper.selectList(new LambdaQueryWrapper<DocSysTag>().eq(DocSysTag::getParentId, sysTagExtend.getTagId()));
            if (!CollectionUtils.isEmpty(sysTags)) {
                sysTagExtend.setIsChildren(DocConstants.ONE);
            }
        }
    }

    /**
     * 根据最高级tag_id获取所有子级tag_id 包含本身
     *
     */
    private List<Long> getTagIdList(Long[] tagIds) {
        AssertUtils.isNull(tagIds, "参数错误！");
        //根据最高级得到所有子标签tag_id
        List<DocSysTag> sysTagList = tagMapper.selectList(new LambdaQueryWrapper<DocSysTag>().in(DocSysTag::getParentId, Arrays.asList(tagIds)));
        //获取tag_id
        List<Long> tagIdList = sysTagList.stream().map(DocSysTag::getTagId).collect(Collectors.toList());
        //加上本身的标签id
        tagIdList.addAll(Arrays.asList(tagIds));
        return tagIdList;
    }

    /**
     * 递归 列表标签展示
     *
     */
    private List<DocSysTagDTO> handleChildren(List<DocSysTagDTO> sysTagExtendList, Integer tagLevel, List<Long> tagIdList) {
        if (tagLevel < DocConstants.ZERO || CollectionUtils.isEmpty(sysTagExtendList)) {
            return null;
        }
        //获取所有父级tagId
        List<Long> tagIds = sysTagExtendList.stream().map(DocSysTagDTO::getTagId).collect(Collectors.toList());
        List<DocSysTag> sysTagList = tagMapper.selectList(new LambdaQueryWrapper<DocSysTag>().in(DocSysTag::getParentId, tagIds).eq(DocSysTag::getTagLevel, DocConstants.ZERO).in(DocSysTag::getTagId, tagIdList));
        List<DocSysTagDTO> sysTagExtendLevel = PageCopyListUtils.copyListProperties(sysTagList, DocSysTagDTO.class);
        Map<Long, List<DocSysTagDTO>> collect = sysTagExtendLevel.stream()
                .collect(Collectors.groupingBy(DocSysTagDTO::getParentId));
        for (DocSysTagDTO extend : sysTagExtendList) {
            extend.setChildren(collect.get(extend.getTagId()) == null ? new ArrayList<>()
                    : collect.get(extend.getTagId()));
        }
        return handleChildren(sysTagExtendLevel, tagLevel - DocConstants.ONE, tagIdList);
    }

    /**
     * 校验标签同级名称是否重复
     */
    private void checkTagName(DocSysTag tag) {
        //校验同级文件夹是否重复
        Long count = tagMapper.selectCount(new LambdaQueryWrapper<DocSysTag>()
                .eq(DocSysTag::getTagName, tag.getTagName())
                .eq(!ObjectUtils.isEmpty(tag.getParentId()), DocSysTag::getParentId, tag.getParentId())
                .eq(ObjectUtils.isEmpty(tag.getParentId()), DocSysTag::getParentId, DocConstants.ZERO)
                .eq(DocSysTag::getTagLevel, DocConstants.ZERO)
                .ne(!ObjectUtils.isEmpty(tag.getTagId()), DocSysTag::getTagId, tag.getTagId()));

        AssertUtils.isTrue(count != null && count > 0, "标签名称重复");
    }
}
