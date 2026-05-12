package com.sunyard.ecm.aspect;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import com.sunyard.ecm.annotation.WebsocketNoticeAnnotation;
import com.sunyard.ecm.constant.RedisConstants;
import com.sunyard.ecm.controller.BaseController;
import com.sunyard.ecm.websocket.WebSocketMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author zhouleibin
 * @Desc websocket通知切面
 * @since 2024/6/1
 */

@Slf4j
@Aspect
@Order(99)
@Component
public class WebsocketNoticeAspect extends BaseController {

    private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 定义切入点
     */
    @Pointcut("@annotation(com.sunyard.ecm.annotation.WebsocketNoticeAnnotation)")
    public void notice() {
    }

    /**
     * 后置通知
     *
     * @param joinPoint
     */
    @AfterReturning(value = "notice()")
    private void after(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        WebsocketNoticeAnnotation annotation = method.getAnnotation(WebsocketNoticeAnnotation.class);
        WebSocketMessageDTO dto = new WebSocketMessageDTO();
        //获取消息内容,如果注解传了消息内容则发送消息内容
        String msg=annotation.msg();
        if(!"".equals(msg)){
            dto.setContentText(msg);
        }else{
            dto.setContentText("true");
        }
        if ("all".equals(annotation.msgType())) {
            dto.setMsgType("all");
        } else {
            List<String> buisIdList = new ArrayList<>();
            Object busiId = evaluateExpression(annotation.busiId(), joinPoint);
            if(busiId instanceof Long||busiId instanceof String) {
                buisIdList.add(busiId.toString());
            }else if(busiId instanceof Collection){
                // 处理集合类型（支持 List、Set 等）
                Collection<?> rawCollection = (Collection<?>) busiId;
                for (Object item : rawCollection) {
                    if (item != null) {
                        buisIdList.add(item.toString());
                    }
                }
            }
            dto.setBuisIdList(buisIdList);
        }
        SpringUtil.getBean(StringRedisTemplate.class).convertAndSend(RedisConstants.REDIS_CHANNEL, JSONObject.toJSONString(dto));
    }

    /**
     * 解析el表达式
     *
     * @param expression
     * @param point
     * @return
     */
    private Object evaluateExpression(String expression, JoinPoint point) {
        //如果表达式直接传递了硬编码的字符串，那么它实际上不需要进行SpEL解析
        if ("all".equals(expression)){
            return expression;
        }
        // 获取目标对象
        Object target = point.getTarget();
        // 获取方法参数
        Object[] args = point.getArgs();
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();

        EvaluationContext context = new MethodBasedEvaluationContext(target, method, args, parameterNameDiscoverer);
        Expression exp = spelExpressionParser.parseExpression(expression);
        return exp.getValue(context);
    }
}
