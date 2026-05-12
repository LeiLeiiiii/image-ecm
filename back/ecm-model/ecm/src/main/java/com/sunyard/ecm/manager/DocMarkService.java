package com.sunyard.ecm.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.ecm.annotation.WebsocketNoticeAnnotation;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.redis.EcmBusiDocRedisDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.mapper.EcmBusiDocMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.po.EcmBusiDoc;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.vo.EcmDocMarkVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;

/**
 * @author： ty
 * @create： 2023/5/11 14:09
 * @desc：标记节点实现类
 */
@Service
public class DocMarkService {
    @Resource
    private SnowflakeUtils snowflakeUtil;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmBusiDocMapper ecmBusiDocMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private BusiOperationService busiOperationService;
    @Resource
    private BusiCacheService busiCacheService;

    /**
     * 新增资料标记
     */
    @Lock4j(keys = "#vo.docId + '_' + #vo.docName")
    @WebsocketNoticeAnnotation(busiId = "#vo.busiId")
    public Result addDocMark(EcmDocMarkVO vo, AccountTokenExtendDTO token) {
        AssertUtils.isTrue(IcmsConstants.SHOW_PAGE.equals(token.getIsShow()),"当前无权限,无法进行采集相关操作");
        //入参校验
        checkParam(vo,token);
        List<EcmBusiDoc> ecmBusiDocs;
        ecmBusiDocs = ecmBusiDocMapper
                .selectList(new QueryWrapper<EcmBusiDoc>().eq("busi_id", vo.getBusiId())
                        .eq("doc_code", vo.getDocId()).eq("doc_mark", StateConstants.COMMON_ONE));
        long count = ecmBusiDocs.stream().filter(p -> p.getDocName().equals(vo.getDocName()))
                .count();
        AssertUtils.isTrue(count > 0L, "资料标记名称已存在");
        //获取EcmBusiDoc对象
        EcmBusiDoc ecmBusiDoc = new EcmBusiDoc();
        ecmBusiDoc.setDocId(snowflakeUtil.nextId());
        ecmBusiDoc.setDocMark(StateConstants.COMMON_ONE);
        ecmBusiDoc.setBusiId(vo.getBusiId());
        ecmBusiDoc.setDocName(vo.getDocName());
        ecmBusiDoc.setDocSort(Float.intBitsToFloat(ecmBusiDocs.size() + 1));
//        EcmBusiDoc ecmBusiDoc1 = ecmBusiDocMapper.selectById(vo.getDocId());
//        if (ecmBusiDoc1 != null) {
//            ecmBusiDoc.setDocCode(ecmBusiDoc1.getDocCode());
//        } else {
//            ecmBusiDoc.setDocCode(vo.getDocId());
//        }
        ecmBusiDoc.setDocCode(vo.getDocId());
        //先更新数据库在更新缓存
        addDocMarkDB(vo, token, ecmBusiDoc);
        //redis缓存更新
        addDocMarkToRedis(vo, ecmBusiDoc, token);
        return Result.success(ecmBusiDoc);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addDocMarkDB(EcmDocMarkVO vo, AccountTokenExtendDTO token, EcmBusiDoc ecmBusiDoc) {
        //        //持久化数据库新增
        ecmBusiDocMapper.insert(ecmBusiDoc);
        //添加业务操作记录
        busiOperationService.addOperation(vo.getBusiId(), IcmsConstants.ADD_DOC_MARK, token,
                "新增资料标记:" + vo.getDocName());
    }

    /**
     * 删除资料标记
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#vo.busiId + '_' + #vo.docId")
    @WebsocketNoticeAnnotation(busiId = "#vo.busiId")
    public void deleteDocMark(EcmDocMarkVO vo, AccountTokenExtendDTO token) {
        AssertUtils.isTrue(IcmsConstants.SHOW_PAGE.equals(token.getIsShow()),"当前无权限,无法进行采集相关操作");
        AssertUtils.isNull(vo.getBusiId(), "参数错误");
        AssertUtils.isNull(vo.getDocId(), "参数错误");
        AssertUtils.isNull(vo.getDocName(), "DocName不能为空");
        //删除redis缓存标记信息
        deleteDocMarkToRedis(vo, token);
        //删除持久化数据库标记信息
        deleteDocMarkToDb(vo);
        //添加业务操作记录
        busiOperationService.addOperation(vo.getBusiId(), IcmsConstants.DELETE_DOC_MARK, token,
                "删除资料标记:" + vo.getDocName());
    }

    /**
     * 编辑资料标记
     */
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = "#vo.busiId + '_' + #vo.docId")
    @WebsocketNoticeAnnotation(busiId = "#vo.busiId")
    public Result editDocMark(EcmDocMarkVO vo, AccountTokenExtendDTO token) {
        AssertUtils.isNull(vo.getBusiId(), "参数错误");
        AssertUtils.isNull(vo.getDocId(), "参数错误");
        AssertUtils.isNull(vo.getDocName(), "DocName不能为空");
        //更新持久化数据库标记信息
        editDocMarkToDb(vo);
        //更新redis缓存标记信息
        editDocMarkToRedis(vo, token);
        //添加业务操作记录
        busiOperationService.addOperation(vo.getBusiId(), IcmsConstants.DELETE_DOC_MARK, token,
                "编辑资料标记:" + vo.getDocName());
        return null;
    }

    /**
     * 查询资料标记
     */
    public List<EcmBusiDocRedisDTO> searchDocMark(EcmDocMarkVO vo, AccountTokenExtendDTO token) {
        AssertUtils.isNull(vo.getBusiId(), "业务id不能为空");
        AssertUtils.isNull(vo.getDocId(), "资料节点id不能为空");
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                vo.getBusiId());
        //获取标记节点
        List<EcmBusiDoc> ecmBusiDocs = ecmBusiInfoRedisDTO.getEcmBusiDocs();
        if (CollectionUtils.isEmpty(ecmBusiDocs)) {
            return new ArrayList<>();
        }
        //查选出资料节点id的标记节点
        List<EcmBusiDoc> ecmBusiDocs1 = ecmBusiDocs.stream()
                .filter(p -> p.getDocCode().equals(vo.getDocId())).collect(Collectors.toList());
        EcmDocDef ecmDocDef = ecmDocDefMapper.selectById(vo.getDocId());
        ArrayList<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = new ArrayList<>();
        for (EcmBusiDoc doc : ecmBusiDocs1) {
            EcmBusiDocRedisDTO dto = new EcmBusiDocRedisDTO();
            BeanUtils.copyProperties(doc, dto);
            dto.setImgLimit(ecmDocDef.getImgLimit());
            dto.setAudioLimit(ecmDocDef.getAudioLimit());
            dto.setOfficeLimit(ecmDocDef.getOfficeLimit());
            dto.setVideoLimit(ecmDocDef.getVideoLimit());
            dto.setOtherLimit(ecmDocDef.getOtherLimit());
            dto.setMaxLen(ecmDocDef.getMaxFiles());
            dto.setMinLen(ecmDocDef.getMinFiles());
            ecmBusiDocRedisDTOS.add(dto);
        }
        return ecmBusiDocRedisDTOS;
    }

    private void editDocMarkToDb(EcmDocMarkVO vo) {
        ecmBusiDocMapper.update(null,
                new LambdaUpdateWrapper<EcmBusiDoc>().set(EcmBusiDoc::getDocName, vo.getDocName())
                        .eq(EcmBusiDoc::getDocId, vo.getDocId()));
    }

    private void editDocMarkToRedis(EcmDocMarkVO vo, AccountTokenExtendDTO token) {
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                vo.getBusiId());
        List<EcmBusiDoc> ecmBusiDocs = ecmBusiInfoRedisDTO.getEcmBusiDocs();
        if (CollectionUtils.isEmpty(ecmBusiDocs)) {
            return;
        }

        List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService
                .getFileInfoRedis(ecmBusiInfoRedisDTO.getBusiId());
        if (CollectionUtils.isEmpty(fileInfoRedisEntities)) {
            return;
        }

        Iterator<EcmBusiDoc> iterator = ecmBusiDocs.iterator();
        while (iterator.hasNext()) {
            EcmBusiDoc ecmBusiDoc = iterator.next();
            if (ecmBusiDoc.getDocId().toString().equals(vo.getDocId())) {
                ecmBusiDoc.setDocName(vo.getDocName());
            }
        }
        ecmBusiInfoRedisDTO.setEcmBusiDocs(ecmBusiDocs);
        busiCacheService.saveAndUpate(ecmBusiInfoRedisDTO);
    }

    private void deleteDocMarkToRedis(EcmDocMarkVO vo, AccountTokenExtendDTO token) {

        List<FileInfoRedisDTO> fileInfoRedisEntities = busiCacheService
                .getFileInfoRedis(vo.getBusiId());
        if (CollectionUtils.isEmpty(fileInfoRedisEntities)) {
            return;
        }
        //删除标记节点，则文件归类到其资料节点
        for (FileInfoRedisDTO fileInfo : fileInfoRedisEntities) {
            if (Long.valueOf(vo.getDocId()).equals(fileInfo.getMarkDocId())) {
                fileInfo.setComment(fileInfo.getDocName());
            }
        }
        busiCacheService.updateFileInfoRedis(fileInfoRedisEntities);
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                vo.getBusiId());
        List<EcmBusiDoc> ecmBusiDocs = ecmBusiInfoRedisDTO.getEcmBusiDocs();
        if (CollectionUtils.isEmpty(ecmBusiDocs)) {
            return;
        }

        Iterator<EcmBusiDoc> iterator = ecmBusiDocs.iterator();
        while (iterator.hasNext()) {
            EcmBusiDoc ecmBusiDoc = iterator.next();
            if (ecmBusiDoc.getDocId().toString().equals(vo.getDocId())) {
                iterator.remove();
            }
        }
        //        ecmBusiDocs = ecmBusiDocs.stream().filter(p -> !p.getDocId().equals(vo.getDocId())).collect(Collectors.toList());
        ecmBusiInfoRedisDTO.setEcmBusiDocs(ecmBusiDocs);
        busiCacheService.saveAndUpate(ecmBusiInfoRedisDTO);
    }

    private void deleteDocMarkToDb(EcmDocMarkVO vo) {
        ecmBusiDocMapper.deleteById(vo.getDocId());
        //        ecmBusiDocRelMapper.delete(new QueryWrapper<EcmBusiDocRel>().eq("doc_code", vo.getDocId()));
    }

    private void addDocMarkToRedis(EcmDocMarkVO vo, EcmBusiDoc ecmBusiDoc,
                                   AccountTokenExtendDTO token) {
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService.getEcmBusiInfoRedisDTO(token,
                vo.getBusiId());
        List<EcmBusiDoc> ecmBusiDocs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ecmBusiInfoRedisDTO.getEcmBusiDocs())) {
            ecmBusiDocs = ecmBusiInfoRedisDTO.getEcmBusiDocs();
        }
        ecmBusiDocs.add(ecmBusiDoc);
        ecmBusiInfoRedisDTO.setEcmBusiDocs(ecmBusiDocs);
        busiCacheService.saveAndUpate(ecmBusiInfoRedisDTO);
    }


    private void checkParam(EcmDocMarkVO vo,AccountTokenExtendDTO token) {
        AssertUtils.isNull(vo.getBusiId(), "业务编号不能为空");
        AssertUtils.isNull(vo.getDocId(), "请选中资料节点");
        AssertUtils.isNull(vo.getDocName(), "资料标记名称不能为空");
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = busiCacheService
                .getEcmBusiInfoRedisDTO(token, vo.getBusiId());
        AssertUtils.isTrue(IcmsConstants.DYNAMIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType()),
                "动态树不允许创建标记");

    }
}
