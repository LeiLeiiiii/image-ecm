package com.sunyard.module.system.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.module.system.api.dto.SysDictionaryDTO;
import com.sunyard.module.system.constant.MenuConstants;
import com.sunyard.module.system.mapper.SysDictionaryMapper;
import com.sunyard.module.system.mapper.SysMenuMapper;
import com.sunyard.module.system.po.SysDictionary;
import com.sunyard.module.system.po.SysMenu;

/**
 * 字典表
 *
 * @Author raochangmei 2022-05-25
 */
@Service
public class SysDictionaryService {
    @Resource
    private SysMenuMapper sysMenuMapper;
    @Resource
    private SysDictionaryMapper sysDictionaryMapper;

    /**
     * 根据key，获取值
     *
     * @param key key
     * @param systemCode 系统code
     * @return Result
     */
    public Map<String, SysDictionaryDTO> selectDictionByKeyMap(String key, Integer systemCode) {
        List<SysDictionary> list = sysDictionaryMapper
                .selectList(getQueryWrapper(systemCode).eq(SysDictionary::getDicKey, key));

        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        List<SysDictionary> children = sysDictionaryMapper.selectList(getQueryWrapper(systemCode)
                .eq(SysDictionary::getParentId, list.get(0).getId())
                .orderByAsc(SysDictionary::getDicSequen));
        Map map = new HashMap(12);
        for (SysDictionary sysDictionary : children) {
            SysDictionaryDTO dto = new SysDictionaryDTO();
            BeanUtils.copyProperties(sysDictionary, dto);
            map.put(sysDictionary.getDicVal(), dto);
        }
        return map;
    }

    /**
     * 获取字典列表
     * @param keys key
     * @return Result
     */
    public List<SysDictionaryDTO> getDictionaryList(String[] keys) {
        if (ObjectUtils.isEmpty(keys)) {
            return null;
        }
        List<SysDictionary> poList = sysDictionaryMapper
                .selectList(new LambdaQueryWrapper<SysDictionary>()
                    .in(SysDictionary::getDicKey, Arrays.asList(keys)));
        List<SysDictionaryDTO> list = new ArrayList<>();
        for (SysDictionary po : poList) {
            SysDictionaryDTO dto = new SysDictionaryDTO();
            BeanUtils.copyProperties(po, dto);
            list.add(dto);
        }
        return list;
    }

    /**
     * 获取字典列表
     * @param systemCode 系统code
     * @return Result
     */
    public Map<String, List<SysDictionaryDTO>> getDictionaryAll(String key, Integer systemCode) {
        // 这里代码其实不太合理，正常情况下systemCode应该是必传的，否则字典区分系统无意义
        List<String> keys = null;
        if (!ObjectUtils.isEmpty(key)) {
            String[] split = key.split(",");
            keys = Arrays.asList(split);
        }
        // 这个接口同时需用于对外rpc
        // 情况1：key传递时，走单独的组装方法，由于只会传几个字典key，因此使用两次数据库查询，这样效率高
        if (!ObjectUtils.isEmpty(keys)) {
            // 查询指定字典
            List<SysDictionary> parentId = sysDictionaryMapper
                    .selectList(getQueryWrapper(systemCode).in(SysDictionary::getDicKey, keys));
            List<Long> ids = parentId.stream().map(SysDictionary::getId)
                    .collect(Collectors.toList());
            // 分组
            Map<String, List<SysDictionaryDTO>> retMap = new HashMap(12);
            if (CollectionUtils.isEmpty(ids)) {
                return retMap;
            }
            // 子级的列表
            List<SysDictionary> children = sysDictionaryMapper
                    .selectList(getQueryWrapper(systemCode).in(SysDictionary::getParentId, ids));
            if (CollectionUtils.isEmpty(children)) {
                return retMap;
            }
            Map<Long, List<SysDictionary>> collect = children.stream()
                    .collect(Collectors.groupingBy(SysDictionary::getParentId));
            for (SysDictionary dictionary : parentId) {
                List<SysDictionary> sysDictionaries = collect.get(dictionary.getId());
                List<SysDictionaryDTO> list = new ArrayList<>();
                if (!CollectionUtils.isEmpty(sysDictionaries)) {
                    for (SysDictionary dictionary1 : sysDictionaries) {
                        SysDictionaryDTO extend = new SysDictionaryDTO();
                        BeanUtils.copyProperties(dictionary1, extend);
                        extend.setLabel(extend.getRemark());
                        extend.setValue(extend.getDicVal());
                        list.add(extend);
                    }
                }
                retMap.put(dictionary.getDicKey(), list);
            }
            return retMap;
        }

        // 情况2：不传递key时，走全部查询
        // 根据查询条件直接取出所有字典
        List<SysDictionary> allList = sysDictionaryMapper.selectList(getQueryWrapper(systemCode));
        if (allList.isEmpty()) {
            return null;
        }
        // 获取所有的pid，不包含null，不包含新创建且未新增字典数据的字典
        Set<Long> pidSet = allList.stream().map(SysDictionary::getParentId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        // 获取代码更新后所有新创建的数据字典（dicType为1）
        // 分离父集合和子集合，同时进行分组
        Map<Long, List<SysDictionary>> childMap = new HashMap<>();
        List<SysDictionary> filteredParentList = new ArrayList<>();
        for (SysDictionary dictionary : allList) {
            Long parentId = dictionary.getParentId();
            Long id = dictionary.getId();
            // 子集合分组
            if (pidSet.contains(parentId)) {
                childMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(dictionary);
            }
            // 父集合筛选
            if (pidSet.contains(id)) {
                filteredParentList.add(dictionary);
            }
        }
        // 分组结果映射
        Map<String, List<SysDictionaryDTO>> retMap = new HashMap<>(12);
        for (SysDictionary dictionary : filteredParentList) {
            List<SysDictionary> sysDictionaries = childMap.get(dictionary.getId());
            if (!CollectionUtils.isEmpty(sysDictionaries)) {
                List<SysDictionaryDTO> list = new ArrayList<>();
                for (SysDictionary child : sysDictionaries) {
                    SysDictionaryDTO dto = new SysDictionaryDTO();
                    BeanUtils.copyProperties(child, dto);
                    dto.setLabel(child.getRemark());
                    dto.setValue(child.getDicVal());
                    list.add(dto);
                }
                retMap.put(dictionary.getDicKey(), list);
            }
        }
        return retMap;
    }

    /**
     * 新增
     * @param sysDictionary 字典obj
     */
    public void addDictionary(SysDictionary sysDictionary) {
        checkDictionary(sysDictionary);
        if (sysDictionary.getParentId() != null) {
            if (StringUtils.hasText(sysDictionary.getDicVal())) {
                SysDictionary dictionary = sysDictionaryMapper
                        .selectOne(getQueryWrapper(sysDictionary.getSystemCode())
                                .eq(SysDictionary::getParentId, sysDictionary.getParentId())
                                .eq(SysDictionary::getDicVal, sysDictionary.getDicVal()));
                AssertUtils.notNull(dictionary, "【当前值】已存在");
            }
        }

        SysDictionary dictionary = sysDictionaryMapper
                .selectOne(getQueryWrapper(sysDictionary.getSystemCode())
                    .eq(SysDictionary::getDicKey, sysDictionary.getDicKey()));
        AssertUtils.notNull(dictionary, "【当前key】已存在");

        if (sysDictionary.getParentId() != null) {
            SysDictionary dictionary1 = sysDictionaryMapper.selectById(sysDictionary.getParentId());
            AssertUtils.isNull(dictionary1, "【当前父级】不存在");
            if (null != dictionary1.getSystemCode()) {
                AssertUtils.isTrue(
                        !dictionary1.getSystemCode().equals(sysDictionary.getSystemCode()),
                        "【系统分类】不匹配");
                sysDictionary.setSystemCode(dictionary1.getSystemCode());
            }
            sysDictionary.setDicLevel(dictionary1.getDicLevel() + 1);
        } else {
            sysDictionary.setDicLevel(1);
        }
        sysDictionaryMapper.insert(sysDictionary);
    }

    /**
     * 修改
     * @param sysDictionary 字典obj
     */
    public void updateDictionary(SysDictionary sysDictionary) {
        AssertUtils.isNull(sysDictionary.getId(), "参数有误");
        checkDictionary(sysDictionary);
        sysDictionaryMapper.update(null, new LambdaUpdateWrapper<SysDictionary>()
                .set(SysDictionary::getDicVal, sysDictionary.getDicVal())
                .set(null != sysDictionary.getSystemCode(), SysDictionary::getSystemCode,
                        sysDictionary.getSystemCode())
                .set(null != sysDictionary.getParentId(), SysDictionary::getParentId, sysDictionary.getParentId())
                .set(null != sysDictionary.getDicSequen(), SysDictionary::getDicSequen,
                        sysDictionary.getDicSequen())
                .set(null != sysDictionary.getRemark(), SysDictionary::getRemark, sysDictionary.getRemark())
                .set(SysDictionary::getDicExtra, sysDictionary.getDicExtra()).eq(SysDictionary::getId, sysDictionary.getId()));
    }

    /**
     * 查详情
     * @param sysDictionary 字典obj
     */
    public Map<String, Object> getInfoDictionary(SysDictionary sysDictionary) {
        AssertUtils.isNull(sysDictionary.getId(), "参数有误");
        SysDictionary dictionary = sysDictionaryMapper.selectById(sysDictionary.getId());
        Map map = new HashMap(6);
        if (dictionary == null) {
            return map;
        }

        map.put("dictionary", dictionary);
        List<SysDictionary> children = sysDictionaryMapper.selectList(
                getQueryWrapper(dictionary.getSystemCode()).eq(SysDictionary::getParentId, dictionary.getId()));
        map.put("children", children);
        return map;
    }

    /**
     * 查列表
     * @param sysDictionary 字典obj
     */
    public PageInfo getListDictionary(SysDictionary sysDictionary, PageForm page) {
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        if (!StringUtils.hasText(sysDictionary.getDicKey())) {
            List<SysDictionary> sysDictionaries = sysDictionaryMapper
                    .selectList(getQueryWrapper(sysDictionary.getSystemCode())
                            // 无搜索条件，则返回树
                            .eq(StringUtils.hasText(sysDictionary.getDicKey()), SysDictionary::getDicKey,
                                    sysDictionary.getDicKey())
                            .like(StringUtils.hasText(sysDictionary.getRemark()), SysDictionary::getRemark,
                                    sysDictionary.getRemark())
                            .like(StringUtils.hasText(sysDictionary.getDicVal()), SysDictionary::getDicVal,
                                    sysDictionary.getDicVal())
                            .orderByAsc(SysDictionary::getCreateTime));
            PageInfo<SysDictionaryDTO> pageInfo = PageCopyListUtils
                    .getPageInfo(new PageInfo<>(sysDictionaries), SysDictionaryDTO.class);
            return pageInfo;
        } else {
            List<SysDictionary> list = sysDictionaryMapper
                    .selectList(getQueryWrapper(sysDictionary.getSystemCode())
                            // 无搜索条件，则返回树
                            .eq(StringUtils.hasText(sysDictionary.getDicKey()), SysDictionary::getDicKey,
                                    sysDictionary.getDicKey())
                            .like(StringUtils.hasText(sysDictionary.getRemark()), SysDictionary::getRemark,
                                    sysDictionary.getRemark())
                            .like(StringUtils.hasText(sysDictionary.getDicVal()), SysDictionary::getDicVal,
                                    sysDictionary.getDicVal())
                            .orderByAsc(SysDictionary::getCreateTime));
            List<SysDictionaryDTO> listDto = list.stream().map(po -> {
                SysDictionaryDTO dto = new SysDictionaryDTO();
                BeanUtils.copyProperties(po, dto);
                return dto;
            }).collect(Collectors.toList());

            PageInfo<SysDictionaryDTO> pageInfo = PageCopyListUtils
                    .getPageInfo(new PageInfo<>(listDto), SysDictionaryDTO.class);
            List<SysDictionaryDTO> sysDictionaries = pageInfo.getList();
            if (!CollectionUtils.isEmpty(sysDictionaries)) {
                List<Long> ids = sysDictionaries.stream().map(SysDictionaryDTO::getId)
                        .collect(Collectors.toList());
                List<SysDictionary> list1 = sysDictionaryMapper
                        .selectList(getQueryWrapper(sysDictionary.getSystemCode())
                                .in(SysDictionary::getParentId, ids).orderByAsc(SysDictionary::getDicSequen));
                List<SysDictionaryDTO> listDto1 = list1.stream().map(po -> {
                    SysDictionaryDTO dto = new SysDictionaryDTO();
                    BeanUtils.copyProperties(po, dto);
                    return dto;
                }).collect(Collectors.toList());

                List<SysDictionaryDTO> children = PageCopyListUtils.copyListProperties(listDto1,
                        SysDictionaryDTO.class);
                Map<Long, List<SysDictionaryDTO>> collect = children.stream()
                        .collect(Collectors.groupingBy(SysDictionaryDTO::getParentId));
                Map<Long, List<SysDictionaryDTO>> collect1 = new HashMap<>(12);
                if (!CollectionUtils.isEmpty(children)) {
                    List<Long> parentId = children.stream().map(SysDictionaryDTO::getId)
                            .collect(Collectors.toList());
                    List<SysDictionary> list2 = sysDictionaryMapper
                            .selectList(getQueryWrapper(sysDictionary.getSystemCode())
                                    .in(SysDictionary::getParentId, parentId).orderByAsc(SysDictionary::getDicSequen));
                    List<SysDictionaryDTO> listDto2 = list2.stream().map(po -> {
                        SysDictionaryDTO dto = new SysDictionaryDTO();
                        BeanUtils.copyProperties(po, dto);
                        return dto;
                    }).collect(Collectors.toList());

                    List<SysDictionaryDTO> childrenTwo = PageCopyListUtils.copyListProperties(list2,
                            SysDictionaryDTO.class);
                    collect1 = childrenTwo.stream()
                            .collect(Collectors.groupingBy(SysDictionaryDTO::getParentId));
                }
                for (SysDictionaryDTO extend : sysDictionaries) {
                    extend.setChildren(collect.get(extend.getId()) == null ? new ArrayList<>()
                            : collect.get(extend.getId()));
                    for (SysDictionaryDTO extend1 : extend.getChildren()) {
                        extend1.setChildren(
                                collect1.get(extend1.getId()) == null ? new ArrayList<>()
                                        : collect1.get(extend1.getId()));
                    }
                }
            }
            return pageInfo;
        }

    }

    /**
     * 只查父级
     * @param sysDictionary 字典obj
     */
    public List<SysDictionary> getListParent(SysDictionary sysDictionary) {
        List<SysDictionary> sysDictionaries = sysDictionaryMapper
                .selectList(getQueryWrapper(sysDictionary.getSystemCode()));
        return sysDictionaries;
    }

    /**
     * 根据字典表名获取SysDictionary对象
     * @param dickey key
     */
    public SysDictionary getDictionary(String dickey) {
        if (!StringUtils.hasText(dickey)) {
            return null;
        }
        List<SysDictionary> dictionaryList = sysDictionaryMapper
                .selectList(new LambdaQueryWrapper<SysDictionary>().eq(SysDictionary::getDicKey, dickey));
        if (ObjectUtils.isEmpty(dictionaryList)) {
            return null;
        }
        return dictionaryList.get(0);
    }

    /**
     * 根据key，获取下级字典信息
     *
     * @param key key
     * @param systemCode 系统code
     * @return Result
     */
    public List<SysDictionary> selectValueByParentKey(String key, Integer systemCode) {
        List<SysDictionary> list = sysDictionaryMapper
                .selectList(getQueryWrapper(systemCode).eq(SysDictionary::getDicKey, key));

        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        List<SysDictionary> children = sysDictionaryMapper.selectList(getQueryWrapper(systemCode)
                .eq(SysDictionary::getParentId, list.get(0).getId()).orderByAsc(SysDictionary::getDicSequen));
        return children;
    }

    /**
     * 获取key:code value:Desc map集合
     */
    public Map<String, String> getDescByCode(String dicKey) {
        if (!StringUtils.hasText(dicKey)) {
            return null;
        }
        List<SysDictionary> sysDictionaryList = sysDictionaryMapper
                .selectList(new LambdaQueryWrapper<SysDictionary>().eq(SysDictionary::getDicKey, dicKey));
        if (ObjectUtils.isEmpty(sysDictionaryList)) {
            return null;
        }
        SysDictionary sysDictionary = sysDictionaryList.get(0);
        List<SysDictionary> sysDictionaries = sysDictionaryMapper.selectList(
                new LambdaQueryWrapper<SysDictionary>().eq(SysDictionary::getParentId, sysDictionary.getId()));
        if (ObjectUtils.isEmpty(sysDictionaries)) {
            return null;
        }
        Map<String, String> map = new HashMap<>(12);
        for (SysDictionary dictionary : sysDictionaries) {
            map.put(dictionary.getDicVal(), dictionary.getRemark());
        }
        return map;
    }

    /**
     * 根据父级key 获取自己的 val：extra的map集合
     */
    public Map<String, String> searchValExtraMapByParentKey(String dickey) {
        if (!StringUtils.hasText(dickey)) {
            return null;
        }
        List<SysDictionary> sysDictionaryList = sysDictionaryMapper
                .selectList(new LambdaQueryWrapper<SysDictionary>().eq(SysDictionary::getDicKey, dickey));
        if (CollectionUtils.isEmpty(sysDictionaryList)) {
            return null;
        }
        List<SysDictionary> sysDictionaryList1 = sysDictionaryMapper
                .selectList(new LambdaQueryWrapper<SysDictionary>().eq(SysDictionary::getParentId,
                        sysDictionaryList.get(0).getId()));
        if (CollectionUtils.isEmpty(sysDictionaryList1)) {
            return null;
        }
        Map<String, String> map = new HashMap<>(12);
        for (SysDictionary sysDictionary : sysDictionaryList1) {
            map.put(sysDictionary.getDicVal(), sysDictionary.getDicExtra());
        }
        return map;
    }

    /**
     * 更新字典表
     */
    public void updateValueByKey(String dicKey, String dicVal) {
        AssertUtils.isNull(dicKey, "参数错误");
        SysDictionary sysDictionary = sysDictionaryMapper.selectOne(
                new LambdaQueryWrapper<SysDictionary>().eq(SysDictionary::getDicKey, dicKey));
        if (sysDictionary != null) {
            sysDictionaryMapper.update(null, new LambdaUpdateWrapper<SysDictionary>()
                    .set(SysDictionary::getDicVal, dicVal).eq(SysDictionary::getDicKey, dicKey));
        }
    }

    /**
     * 获取字典树
     * 由于数据量较小，所以使用直接递归的方式组装数据，不会oom
     */
    public Result<List<Map<String, Object>>> getDictionaryTree(SysDictionary sysDictionary) {
        List<Map<String, Object>> dicList = new ArrayList<>();
        // 一级目录：系统名称和code
        List<SysMenu> sysMenus = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getMenuType, MenuConstants.TYPE_DIR));
        Map<Integer, String> firstMenu = sysMenus.stream()
                .collect(Collectors.toMap(SysMenu::getMenuSystem, SysMenu::getMenuName));
        // 一次性获取字典表中所有数据(只查询指定的字段)
        List<SysDictionary> list = sysDictionaryMapper
                .selectList(new LambdaQueryWrapper<SysDictionary>()
                        .select(
                                SysDictionary::getId,
                                SysDictionary::getParentId,
                                SysDictionary::getDicKey,
                                SysDictionary::getDicType,
                                SysDictionary::getSystemCode,
                                SysDictionary::getRemark)
                        .orderByAsc(SysDictionary::getRemark));
        if (list.isEmpty()) {
            // 组装一个最简单的树
            for (Map.Entry<Integer, String> entry : firstMenu.entrySet()) {
                Map<String, Object> map = new HashMap<>();
                // 1级目录中id无意义
                map.put("id", entry.getKey());
                map.put("remark", entry.getValue());
                map.put("dicList", new ArrayList<>());
                dicList.add(map);
            }
            return Result.success(dicList);
        }
        // 数据过滤，只取包含字典value的字典

        // 所有的pid，不包含parent=null，需要注意无法拿到新建的未写入字典数据的字典，需要在下一步中处理
        Set<Long> pidList = list.stream().map(SysDictionary::getParentId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        // 获取所有dicType=1的数据（适用于新数据，旧数据中无dicType字段），这一步是为了处理新创建的字典
        Set<Long> typeList = list.stream()
                .filter(m -> m.getDicType() != null && m.getDicType() == 1)
                .map(SysDictionary::getId).collect(Collectors.toSet());
        // set集合
        pidList.addAll(typeList);
        // 流式过滤获取list中id存在于pidList的所有对象(包括自身)
        List<SysDictionary> filteredList = list.stream().filter(m -> pidList.contains(m.getId()))
                .collect(Collectors.toList());
        // 由于历史遗留原因，旧数据中的dicType都为null
        // 组装一个最简单的树
        for (Map.Entry<Integer, String> entry : firstMenu.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            // 1级目录中id无意义
            map.put("id", entry.getKey());
            // 字典类型，0表示目录，目录无详情
            map.put("dicType", 0);
            map.put("remark", entry.getValue());
            map.put("systemCode", entry.getKey());
            map.put("dicList",
                    filteredList.stream().filter(m -> m.getSystemCode().equals(entry.getKey()))
                            .collect(Collectors.toList()));
            dicList.add(map);
        }
        return Result.success(dicList);
    }

    /**
     * 根据字典id查询字典属性及下属value
     */
    public Result getDicInfo(Long id) {
        // 字典基本属性
        SysDictionary sysDictionary = sysDictionaryMapper.selectById(id);
        // 关联value
        List<SysDictionary> list = sysDictionaryMapper.selectList(
                new LambdaQueryWrapper<SysDictionary>().eq(SysDictionary::getParentId, id)
                        .orderByAsc(SysDictionary::getDicSequen));
        Map<String, Object> map = new HashMap<>();
        map.put("info", sysDictionary);
        map.put("dicList", list);
        return Result.success(map);
    }

    /**
     * 新增字典
     */
    @Transactional(rollbackFor = Exception.class)
    public Result addDicKey(SysDictionary sysDictionary) {
        AssertUtils.isNull(sysDictionary.getDicKey(), "字典码不能为空");
        AssertUtils.isNull(sysDictionary.getRemark(), "字典名不能为空");
        AssertUtils.isNull(sysDictionary.getSystemCode(), "系统分类不能为空");
        if (sysDictionary.getParentId() != null) {
            AssertUtils.isNull(null, "不应传递parentId");
        }
        if (sysDictionary.getDicVal() != null && !sysDictionary.getDicVal().isEmpty()) {
            AssertUtils.isNull(null, "不应传递字典值");
        }
        if (sysDictionary.getDicExtra() != null && !sysDictionary.getDicExtra().isEmpty()) {
            AssertUtils.isNull(null, "不应传递扩展值");
        }
        // 禁止增加重复字典
        Long count = sysDictionaryMapper.selectCount(new LambdaQueryWrapper<SysDictionary>()
                .eq(SysDictionary::getDicKey, sysDictionary.getDicKey()));
        if (count > 0) {
            return Result.error("字典码已存在，请重新输入", ResultCode.PARAM_ERROR);
        }
        // 1为字典
        sysDictionary.setDicType(1);
        sysDictionaryMapper.insert(sysDictionary);
        return Result.success();
    }

    /**
     * 新增字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#sysDictionary.dicKey")
    public Result addDicValue(SysDictionary sysDictionary) {
        AssertUtils.isNull(sysDictionary.getDicKey(), "字典码不能为空");
        AssertUtils.isNull(sysDictionary.getRemark(), "字典名不能为空");
        AssertUtils.isNull(sysDictionary.getSystemCode(), "系统分类不能为空");
        AssertUtils.isNull(sysDictionary.getDicVal(), "字典值不能为空");
        AssertUtils.isNull(sysDictionary.getParentId(), "字典PID不能为空");
        Long count = sysDictionaryMapper.selectCount(new LambdaQueryWrapper<SysDictionary>()
                .eq(SysDictionary::getDicKey, sysDictionary.getDicKey()));
        if (count > 0) {
            return Result.error("字典码已存在，请重新输入", ResultCode.PARAM_ERROR);
        }
        // 字典数据为2
        sysDictionary.setDicType(2);
        sysDictionaryMapper.insert(sysDictionary);
        return Result.success();
    }

    /**
     * 修改字典
     */
    @Transactional(rollbackFor = Exception.class)
    public Result updateDicKey(SysDictionary sysDictionary) {
        AssertUtils.isNull(sysDictionary.getId(), "id不能为空");
        AssertUtils.isNull(sysDictionary.getRemark(), "字典名不能为空");
        sysDictionaryMapper.update(null,
                new LambdaUpdateWrapper<SysDictionary>()
                        .set(SysDictionary::getRemark, sysDictionary.getRemark())
                        .set(SysDictionary::getUpdateTime, new Date())
                        .eq(SysDictionary::getId, sysDictionary.getId()));
        return Result.success();
    }

    /**
     * 修改字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    public Result updateDicValue(SysDictionary sysDictionary) {
        AssertUtils.isNull(sysDictionary.getId(), "id不能为空");
        AssertUtils.isNull(sysDictionary.getRemark(), "字典名不能为空");
        AssertUtils.isNull(sysDictionary.getDicVal(), "字典值不能为空");
        sysDictionaryMapper.update(null,
                new LambdaUpdateWrapper<SysDictionary>()
                        .set(SysDictionary::getRemark, sysDictionary.getRemark())
                        .set(SysDictionary::getDicVal, sysDictionary.getDicVal())
                        .set(SysDictionary::getDicExtra, sysDictionary.getDicExtra())
                        .set(SysDictionary::getDicSequen, sysDictionary.getDicSequen())
                        .set(SysDictionary::getUpdateTime, new Date())
                        .eq(SysDictionary::getId, sysDictionary.getId()));
        return Result.success();
    }

    /**
     * 删除字典
     * 联动删除字典值
     */
    @Transactional(rollbackFor = Exception.class)
    public Result deleteDicKey(Long id) {
        sysDictionaryMapper.deleteById(id);
        sysDictionaryMapper
                .delete(new LambdaQueryWrapper<SysDictionary>().eq(SysDictionary::getParentId, id));
        return Result.success();
    }

    /**
     * 删除字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    public Result deleteDicValue(Long id) {
        sysDictionaryMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 根据一级key 获取到所有二级key 的 key code map集合
     */
    public Map<String, String> getDicValByDicKey(String dicKey) {
        if (!StringUtils.hasText(dicKey)) {
            return null;
        }
        List<SysDictionary> sysDictionaryList = sysDictionaryMapper
                .selectList(new LambdaQueryWrapper<SysDictionary>().eq(SysDictionary::getDicKey, dicKey));
        if (ObjectUtils.isEmpty(sysDictionaryList)) {
            return null;
        }
        List<SysDictionary> sysDictionaryList1 = sysDictionaryMapper
                .selectList(new LambdaQueryWrapper<SysDictionary>().eq(SysDictionary::getParentId,
                        sysDictionaryList.get(0).getId()));
        if (ObjectUtils.isEmpty(sysDictionaryList1)) {
            return null;
        }
        Map<String, String> map = new HashMap<>(12);
        for (SysDictionary sysDictionary : sysDictionaryList1) {
            map.put(sysDictionary.getDicKey(), sysDictionary.getDicVal());
        }
        return map;
    }

    /**
     * 根据一级key 获取到 二级key Desc map集合
     */
    public Map<String, String> getDescByKey(String dickey) {
        if (!StringUtils.hasText(dickey)) {
            return null;
        }
        List<SysDictionary> sysDictionaryList = sysDictionaryMapper
                .selectList(new LambdaQueryWrapper<SysDictionary>().eq(SysDictionary::getDicKey, dickey));
        if (CollectionUtils.isEmpty(sysDictionaryList)) {
            return null;
        }
        List<SysDictionary> sysDictionaryList1 = sysDictionaryMapper
                .selectList(new LambdaQueryWrapper<SysDictionary>().eq(SysDictionary::getParentId,
                        sysDictionaryList.get(0).getId()));
        if (CollectionUtils.isEmpty(sysDictionaryList1)) {
            return null;
        }
        Map<String, String> map = new HashMap<>(12);
        for (SysDictionary sysDictionary : sysDictionaryList1) {
            map.put(sysDictionary.getDicKey(), sysDictionary.getRemark());
        }
        return map;
    }

    /**
     * 根据一级key 查询到二级 k ey name map集合
     */
    public Map<String, String> getNameByKey(String dickey) {
        if (!StringUtils.hasText(dickey)) {
            return null;
        }
        List<SysDictionary> sysDictionaryList = sysDictionaryMapper
                .selectList(new LambdaQueryWrapper<SysDictionary>().eq(SysDictionary::getDicKey, dickey));
        if (CollectionUtils.isEmpty(sysDictionaryList)) {
            return null;
        }
        List<SysDictionary> sysDictionaryList1 = sysDictionaryMapper
                .selectList(new LambdaQueryWrapper<SysDictionary>().eq(SysDictionary::getParentId,
                        sysDictionaryList.get(0).getId()));
        if (CollectionUtils.isEmpty(sysDictionaryList1)) {
            return null;
        }
        Map<String, String> map = new HashMap<>(12);
        for (SysDictionary sysDictionary : sysDictionaryList1) {
            map.put(sysDictionary.getDicKey(), sysDictionary.getDicExtra());
        }
        return map;
    }

    /**
     * 统一的校验
     */
    private void checkDictionary(SysDictionary sysDictionary) {
        AssertUtils.isNull(sysDictionary.getDicKey(), "【字典表的key】不能为空");
    }

    /**
     * 统一查询方法
     */
    private LambdaQueryWrapper<SysDictionary> getQueryWrapper(Integer systemCode) {
        return new LambdaQueryWrapper<SysDictionary>()
                .eq(null != systemCode, SysDictionary::getSystemCode, systemCode);
    }

    /**
     * 查询系统字典
     */
    public List<Map<String, String>> selectSystemDictionary(Integer systemCode) {
        List<Map<String, String>> list = new ArrayList<>();
        List<SysDictionary> dictionaryList = sysDictionaryMapper.selectList(new LambdaQueryWrapper<SysDictionary>()
                .eq(SysDictionary::getSystemCode, systemCode)
                .isNull(SysDictionary::getParentId));
        // 将字典中的 dicKey 和 remark 封装成map
        if (!CollectionUtils.isEmpty(dictionaryList)) {
            for (SysDictionary sysDictionary : dictionaryList) {
                Map<String, String> map = new HashMap<>();
                map.put("label", sysDictionary.getRemark());
                map.put("value", sysDictionary.getDicKey());
                list.add(map);
            }
        }
        return list;
    }
}
