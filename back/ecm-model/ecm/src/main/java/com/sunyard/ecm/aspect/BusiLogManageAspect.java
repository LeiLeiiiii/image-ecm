package com.sunyard.ecm.aspect;

import com.sunyard.ecm.annotation.LogManageAnnotation;
import com.sunyard.ecm.constant.BusiLogConstants;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.controller.BaseController;
import com.sunyard.ecm.dto.EcmBusExtendDTO;
import com.sunyard.ecm.dto.EditBusiAttrDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmStructureTreeDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.po.EcmBusiLog;
import com.sunyard.ecm.manager.AsyncBusiLogService;
import com.sunyard.ecm.vo.FileInfoVO;
import com.sunyard.ecm.vo.MergFileVO;
import com.sunyard.ecm.vo.RotateFileVO;
import com.sunyard.ecm.vo.SplitFileVO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * @author scm
 * @Desc 业务日志自定义切面
 * @since 2023/8/1 14:18
 */
@Slf4j
@Aspect
@Order(1)
@Component
public class BusiLogManageAspect extends BaseController {

    @Resource
    private AsyncBusiLogService asyncBusiLogService;

    /**
     * 切点定义，于指定注解位置处织入代码
     */
    @Pointcut("@annotation(com.sunyard.ecm.annotation.LogManageAnnotation)")
    public void busiLogPointCut() {
    }

    /**
     * @param joinPoint
     */
    @AfterReturning("busiLogPointCut()")
    public void saveBusiLog(JoinPoint joinPoint) {
        // 于切面织入点通过反射机制获取织入处方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        // 获取注解的设置值
        LogManageAnnotation busiLogAnnotation = method.getAnnotation(LogManageAnnotation.class);
        String operate = busiLogAnnotation != null ? busiLogAnnotation.value() : "";
        // 通过反射获取入参
        Map<String, Object> params = getParams(joinPoint, method);
        // 通过反射获取底层异常
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        /*
        “至”作为批量操作的识别标签
         因AOP后置，删除/批量删除业务日志写入了具体方法中 saveDelLog
         因AOP后置，归类日志写入了具体方法中 saveClassifyLog
         因AOP后置，复用日志写入了具体方法中 saveRepeatLog
         因AOP失效，提交日志写入具体方法中 saveSubmitLog
        */
        // 新建日志
        EcmBusiLog ecmBusiLog = new EcmBusiLog();
        ecmBusiLog.setErrorInfo(exceptionTypes.length == 0 ? null : Arrays.toString(exceptionTypes));
        Integer operatorType;
        /**
         * 通过方法名称及入参确认业务
         */
        if (methodName.equals(BusiLogConstants.ADDBUSIMETHOD) && params.containsKey(BusiLogConstants.ADDANDEDIT)) {
            // 新增业务
            EcmBusiInfoRedisDTO ecmBusiInfoExtend = (EcmBusiInfoRedisDTO) params.get(BusiLogConstants.ADDANDEDIT);
            operatorType=BusiLogConstants.OPERATION_TYPE_ZERO;
            asyncBusiLogService.saveAddLogByDTO(ecmBusiLog, operate, ecmBusiInfoExtend, getToken(),operatorType);
        } else if (params.containsKey(BusiLogConstants.ADDANDEDITOPEN)) {
            // 新增，编辑业务-对外(EcmBusExtendDTO)
            EcmBusExtendDTO dto = (EcmBusExtendDTO) params.get(BusiLogConstants.ADDANDEDITOPEN);
            operatorType=BusiLogConstants.OPERATION_TYPE_TWO;
            asyncBusiLogService.saveEditLogByDTO(ecmBusiLog, operate, dto, getToken(),operatorType);
        } else if (methodName.equals(BusiLogConstants.EDITBUSIMETHOD) && params.containsKey(BusiLogConstants.ADDANDEDIT)) {
            // 编辑业务(EcmBusiInfoRedisDTO)
            operatorType=BusiLogConstants.OPERATION_TYPE_TWO;
            EcmBusiInfoRedisDTO ecmBusiInfoExtend = (EcmBusiInfoRedisDTO) params.get(BusiLogConstants.ADDANDEDIT);
            asyncBusiLogService.saveEditLogByDTO(ecmBusiLog, operate, ecmBusiInfoExtend, getToken(),operatorType);
        } else if (params.containsKey(BusiLogConstants.EDITATTROPEN)) {
            // 编辑业务属性-对外(EditBusiAttrDTO)
            operatorType=BusiLogConstants.OPERATION_TYPE_TWO;
            EditBusiAttrDTO dto = (EditBusiAttrDTO) params.get(BusiLogConstants.EDITATTROPEN);
            asyncBusiLogService.saveEditLogByDTO(ecmBusiLog, operate, dto,operatorType);
        } else if (params.containsKey(BusiLogConstants.GETSORTDEL) && methodName.equals(BusiLogConstants.QUERYBUSIMETHOD_MOBILE)) {
            // 查看业务（EcmsCaptureVO）
            operatorType=BusiLogConstants.OPERATION_TYPE_ONE;
            asyncBusiLogService.handleSaveLog(params, operate, exceptionTypes,operatorType);
        } else if (params.containsKey(BusiLogConstants.GETSORTDEL) && methodName.equals(BusiLogConstants.QUERYBUSIMETHOD_CAPTURE)) {
            operatorType=BusiLogConstants.OPERATION_TYPE_ONE;
            asyncBusiLogService.handleSaveLog(params, operate, exceptionTypes,operatorType);
        } else if (params.containsKey(BusiLogConstants.GETSORTDEL) && methodName.equals(BusiLogConstants.QUERYBUSIMETHOD_PC)) {
            // 查看业务（EcmsCaptureVO）
            EcmStructureTreeDTO vo = (EcmStructureTreeDTO) params.get(BusiLogConstants.GETSORTDEL);
            operatorType=BusiLogConstants.OPERATION_TYPE_ONE;
            if (IcmsConstants.SIGN_FLAG_ONE.equals(vo.getIsShow())) {
                operate = "采集业务";
                operatorType=BusiLogConstants.OPERATION_TYPE_TWO;
            }
            asyncBusiLogService.saveBatchLogByBusiId(ecmBusiLog, operate, vo.getBusiIdList(), getToken(),operatorType);
        } else if (params.containsKey(BusiLogConstants.UPFILE)) {
            // 保存文件（EcmFileInfoDTO）
            operatorType=BusiLogConstants.OPERATION_TYPE_FOUR;
            EcmFileInfoDTO ecmFileInfoDTO = (EcmFileInfoDTO) params.get(BusiLogConstants.UPFILE);
            asyncBusiLogService.saveUploadLog(ecmBusiLog, operate, ecmFileInfoDTO, getToken(),operatorType);
        } else if (params.containsKey(BusiLogConstants.GETSORTDEL) && methodName.equals(BusiLogConstants.DELMETHOD)) {
            // 删除文件（FileInfoVO）
            operatorType=BusiLogConstants.OPERATION_TYPE_SEVEN;
            FileInfoVO vo = (FileInfoVO) params.get(BusiLogConstants.GETSORTDEL);
            asyncBusiLogService.saveDeleteLog(ecmBusiLog, operate, vo, getToken(),operatorType);
        } else if (params.containsKey(BusiLogConstants.EDITFILE) && methodName.equals(BusiLogConstants.EDITMETHOD)) {
            // 编辑文件(RotateFileVO)
            operatorType=BusiLogConstants.OPERATION_TYPE_SIX;
            RotateFileVO vo = (RotateFileVO) params.get(BusiLogConstants.EDITFILE);
            asyncBusiLogService.saveEditLog(ecmBusiLog, operate, vo, getToken(),operatorType);
        } else if (params.containsKey(BusiLogConstants.RESTOREFILE) && methodName.equals(BusiLogConstants.RESTOREMETHOD)) {
            // 还原文件(FileInfoVO)
            operatorType=BusiLogConstants.OPERATION_TYPE_SIX;
            FileInfoVO vo = (FileInfoVO) params.get(BusiLogConstants.RESTOREFILE);
            asyncBusiLogService.saveRestoreLog(ecmBusiLog, operate, vo, getToken(),operatorType);
        } else if (params.containsKey(BusiLogConstants.EDITFILE) && methodName.equals(BusiLogConstants.MERGEMETHOD)) {
            // 合并文件(MergFileVO) - 添加合并完成后文件名
            operatorType=BusiLogConstants.OPERATION_TYPE_SIX;
            MergFileVO vo = (MergFileVO) params.get(BusiLogConstants.EDITFILE);
            asyncBusiLogService.saveMergeLog(ecmBusiLog, operate, vo, getToken(),operatorType);
        } else if (params.containsKey(BusiLogConstants.SPLITFILE) && methodName.equals(BusiLogConstants.SPLITMETHOD)) {
            // 拆分文件(SplitFileVO)
            operatorType=BusiLogConstants.OPERATION_TYPE_SIX;
            SplitFileVO vo = (SplitFileVO) params.get(BusiLogConstants.SPLITFILE);
            asyncBusiLogService.saveSplitLog(ecmBusiLog, operate, vo, getToken(),operatorType);
        }
    }

    /**
     * 通过反射获得入参的属性值
     *
     * @param joinPoint 切入点
     * @param method    织入方法
     * @return 参数hashMap
     */
    private static Map<String, Object> getParams(JoinPoint joinPoint, Method method) {
        Object[] args = joinPoint.getArgs();
        DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        String[] parameterNames = discoverer.getParameterNames(method);
        HashMap<String, Object> paramMap = new HashMap<>(32);
        for (int i = 0; i < Objects.requireNonNull(parameterNames).length; i++) {
            paramMap.put(parameterNames[i], args[i]);
        }
        return paramMap;
    }

}
