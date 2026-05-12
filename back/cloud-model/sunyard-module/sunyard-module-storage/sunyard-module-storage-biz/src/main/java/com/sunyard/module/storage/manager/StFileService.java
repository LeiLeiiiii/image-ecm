package com.sunyard.module.storage.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.module.storage.mapper.StEquipmentMapper;
import com.sunyard.module.storage.po.StEquipment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.module.storage.dto.StFileDTO;
import com.sunyard.module.storage.mapper.StFileMapper;
import com.sunyard.module.storage.mapper.StSplitUploadMapper;
import com.sunyard.module.storage.po.StFile;
import com.sunyard.module.storage.po.StSplitUpload;

import cn.hutool.core.bean.BeanUtil;

/**
 * 文件服务实现类
 *
 * @author yzy
 * @Description
 * @since 2024/11/04 10:05
 */
@Service
public class StFileService {

    @Resource
    private StFileMapper stFileMapper;
    @Resource
    private StSplitUploadMapper stSplitUploadMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private StEquipmentMapper equipmentMapper;

    /**
     * 获取文件表信息集合
     *
     * @param fileIdList 文件id
     * @return Result
     */
    public List<StFileDTO> selectListByIdSourt(List<Long> fileIdList) {
        String orderStr = fileIdList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        List<StFileDTO> list = stFileMapper.selectListByIdSourt(fileIdList,orderStr);
        return injectFragmentation(list);
    }

    /**
     * 批量插入
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer insertBatch(List<StFileDTO> stFileDTOList) {
        // 1. 转换 DTO 列表为 PO 列表
        List<StFile> stFileList = new ArrayList<>();
        for (StFileDTO dto : stFileDTOList) {
            StFile stFile = new StFile();
            BeanUtil.copyProperties(dto, stFile);
            stFileList.add(stFile);
        }
        // 2. 批量插入 StFile
        MybatisBatch<StFile> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, stFileList);
        MybatisBatch.Method<StFile> method = new MybatisBatch.Method<>(StFileMapper.class);
        mybatisBatch.execute(method.insert());
        return stFileList.size(); // 返回插入的数量或其他需要的信息
    }

    /**
     * 插入
     *
     * @param stFileDTO
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer insert(StFileDTO stFileDTO) {
        StFile stFile = new StFile();
        BeanUtil.copyProperties(stFileDTO, stFile);
        stFileMapper.insert(stFile);
        //再插入分片信息表
        StSplitUpload stSplitUpload = new StSplitUpload();
        BeanUtil.copyProperties(stFileDTO, stSplitUpload);
        stSplitUpload.setFileId(stFile.getId());
        stSplitUploadMapper.insert(stSplitUpload);
        stFileDTO.setId(stFile.getId());
        return 0;
    }

    /**
     * 插入
     *
     * @param stFileDTO
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer update(StFileDTO stFileDTO) {
        StFile stFile = new StFile();
        BeanUtil.copyProperties(stFileDTO, stFile);
        stFileMapper.updateById(stFile);
        //再修改分片信息表
        StSplitUpload stSplitUpload = new StSplitUpload();
        BeanUtil.copyProperties(stFileDTO, stSplitUpload);
        stSplitUpload.setFileId(stFile.getId());
        stSplitUploadMapper.update(new LambdaUpdateWrapper<StSplitUpload>()
                .eq(StSplitUpload::getFileId, stFile.getId())
                .set(!ObjectUtils.isEmpty(stSplitUpload.getUploadId()),StSplitUpload::getUploadId,  stSplitUpload.getUploadId())
                .set(!ObjectUtils.isEmpty(stSplitUpload.getChunkSize()),StSplitUpload::getChunkSize,  stSplitUpload.getChunkSize())
                .set(!ObjectUtils.isEmpty(stSplitUpload.getChunkNum()),StSplitUpload::getChunkNum,  stSplitUpload.getChunkNum())
                .set(!ObjectUtils.isEmpty(stSplitUpload.getIsUploadOk()),StSplitUpload::getIsUploadOk,  stSplitUpload.getIsUploadOk())
        );
        return 0;
    }

    /**
     * 查询文件
     * @param id id
     * @return Result
     */
    public StFileDTO selectFileDTO(Long id) {
        StFileDTO list = stFileMapper.selectFileDTO(id);
        return injectFragmentation(list);
    }

    /**
     * 查询文件
     * @param  stFile
     * @return Result
     */
    public List<StFileDTO> selectFileDTOByPO(StFile stFile) {
        List<StFileDTO> list = stFileMapper.selectFileDTOByPO(stFile);
        return injectFragmentation(list);
    }

    /**
     * 批量查询文件
     * @param ids ids
     * @return Result
     */
    public List<StFileDTO> selectFileDTOByIds(List<Long> ids) {
        List<StFileDTO> list = stFileMapper.selectFileDTOByIds(ids);
        //注入设备相关信息
        injectEquipmentInfo(list);
        return injectFragmentation(list);
    }

    public List<StFileDTO> selectFileDTOByIds1(List<Long> ids) {
        List<StFileDTO> list = stFileMapper.selectFileDTOByIds1(ids);
        //注入设备相关信息
        injectEquipmentInfo(list);
        return injectFragmentation(list);
    }
    /**
     * 将List注入分片属性
     *
     * @param stFileDTOList 文件DTO
     * @return Result
     */
    private List<StFileDTO> injectFragmentation(List<StFileDTO> stFileDTOList) {
        if (CollectionUtils.isEmpty(stFileDTOList)) {
            return stFileDTOList;
        }
        List<Long> ids = stFileDTOList.stream().map(StFileDTO::getId).collect(Collectors.toList());

        LambdaQueryWrapper<StSplitUpload> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(StSplitUpload::getFileId, ids);
        List<StSplitUpload> entities = stSplitUploadMapper.selectList(lambdaQueryWrapper);
        if (CollectionUtils.isEmpty(entities)) {
            return stFileDTOList;
        }
        // 将分片表查询结果与DTO进行匹配
        Map<Long, StSplitUpload> entityMap = entities.stream()
                .collect(Collectors.toMap(StSplitUpload::getFileId, Function.identity()));

        for (StFileDTO dto : stFileDTOList) {
            StSplitUpload entity = entityMap.get(dto.getId());
            if (entity != null) {
                // 将查询结果的属性赋值给dto
                dto.setUploadId(entity.getUploadId());
                dto.setChunkSize(entity.getChunkSize());
                dto.setChunkNum(entity.getChunkNum());
                dto.setIsUploadOk(entity.getIsUploadOk());
            }
        }
        return stFileDTOList;

    }

    /**
     * 将文件DTO注入分片属性
     *
     * @param stFileDTO 文件DTO
     * @return Result
     */
    private StFileDTO injectFragmentation(StFileDTO stFileDTO) {
        if (ObjectUtils.isEmpty(stFileDTO)) {
            return stFileDTO;
        }
        LambdaQueryWrapper<StSplitUpload> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(StSplitUpload::getFileId, stFileDTO.getId());
        StSplitUpload entity = stSplitUploadMapper.selectOne(lambdaQueryWrapper);
        // 将分片表查询结果的属性赋值给dto
        if (ObjectUtils.isEmpty(entity)) {
            return stFileDTO;
        }
        stFileDTO.setUploadId(entity.getUploadId());
        stFileDTO.setChunkSize(entity.getChunkSize());
        stFileDTO.setChunkNum(entity.getChunkNum());
        stFileDTO.setIsUploadOk(entity.getIsUploadOk());
        return stFileDTO;

    }

    /**
     * 修改文件加密长度
     *
     * @param length 长度
     * @param fileId 长度
     * @return Result
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateFileEncryptLength(Long length, Long fileId) {
        StFile stFile = new StFile();
        stFile.setId(fileId);
        stFile.setEncryptLen(length);
        return stFileMapper.updateById(stFile);
    }

    /**
     * 获取文件加密长度
     *
     * @param fileId 长度
     * @return Result
     */
    public Long getFileEncryptLength(Long fileId) {
        StFile stFile = stFileMapper.selectById(fileId);
        if (!org.apache.commons.lang3.ObjectUtils.isEmpty(stFile)) {
            return stFile.getEncryptLen();
        } else {
            return null;
        }
    }

    /**
     * 物理删除文件
     */
    public void physicalDeleteById(Long id) {
        // 执行物理删除
        stFileMapper.physicalDeleteById(id);
    }

    /**
     * 给文件DTO列表注入设备相关信息
     * @param fileDTOList 文件DTO列表
     */
    public void injectEquipmentInfo(List<StFileDTO> fileDTOList) {
        if (CollectionUtils.isEmpty(fileDTOList)) {
            return;
        }

        // 1. 提取所有关联的设备ID（去重）
        Set<Long> equipmentIds = fileDTOList.stream()
                .map(StFileDTO::getEquipmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (equipmentIds.isEmpty()) {
            return; // 没有设备ID，直接返回
        }

        // 2. 批量查询设备信息并转换为Map
        Map<Long, StEquipment> equipmentMap = getEquipmentMap(equipmentIds);

        // 3. 注入设备信息到每个DTO
        for (StFileDTO dto : fileDTOList) {
            injectSingleDTO(dto, equipmentMap);
        }
    }

    /**
     * 批量查询设备信息并转换为Map
     * @param equipmentIds 设备ID集合
     * @return 设备ID为key的设备信息Map
     */
    private Map<Long, StEquipment> getEquipmentMap(Set<Long> equipmentIds) {
        // 构建查询条件
        LambdaQueryWrapper<StEquipment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(StEquipment::getId, equipmentIds)
                .select(
                        StEquipment::getId,
                        StEquipment::getStorageAddress,
                        StEquipment::getBucket
                );
        // 执行查询
        List<StEquipment> equipmentList = equipmentMapper.selectList(queryWrapper);

        // 转换为Map
        return equipmentList.stream()
                .collect(Collectors.toMap(
                        StEquipment::getId,
                        equipment -> equipment,
                        (existing, replacement) -> existing
                ));
    }

    /**
     * 给单个DTO注入设备信息
     * @param dto 文件DTO
     * @param equipmentMap 设备信息Map
     */
    private void injectSingleDTO(StFileDTO dto, Map<Long, StEquipment> equipmentMap) {
        StEquipment equipment = equipmentMap.get(dto.getEquipmentId());
        if (equipment != null) {
            dto.setUrl(buildUrl(equipment, dto.getObjectKey()));
            dto.setBucketName(equipment.getBucket());
            dto.setFilePath(buildFilePath(equipment, dto.getObjectKey()));
        } else {
            // 设备不存在时的默认处理
            dto.setUrl("");
            dto.setBucketName("");
            dto.setFilePath("");
        }
    }

    /**
     * 构建文件URL
     */
    private String buildUrl(StEquipment equipment, String objectKey) {
        return String.join("/",
                equipment.getStorageAddress(),
                equipment.getBucket(),
                objectKey);
    }

    /**
     * 构建文件路径
     */
    private String buildFilePath(StEquipment equipment, String objectKey) {
        return String.join("/",
                equipment.getBucket(),
                objectKey);
    }

}
