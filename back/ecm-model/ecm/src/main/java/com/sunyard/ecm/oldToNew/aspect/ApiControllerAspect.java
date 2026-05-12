package com.sunyard.ecm.oldToNew.aspect;

import com.sunyard.ecm.util.AnalysisGrantToolUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class ApiControllerAspect {

    // 定义切点，指向 ApiController 中的所有方法
    @Pointcut("execution(* com.sunyard.ecm.oldToNew.controller.ApiController.*(..))" +
            " && !execution(* com.sunyard.ecm.oldToNew.controller.ApiController.handleCustomException(..))" +
                "&& !execution(* com.sunyard.ecm.oldToNew.controller.ApiController.download(..))")
    public void controllerMethods() {}

    // 在切点方法执行前进行校验
    @Before("controllerMethods()")
    public void validateBeforeMethod(JoinPoint joinPoint) throws ServletException {
        // 调用老影像鉴权Util
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) arg;
                AnalysisGrantToolUtils.checkParam(request);
            }
        }
    }
}

