package com.easterfg.takeaway.handler;

import com.easterfg.takeaway.domain.OrderOperate;
import com.easterfg.takeaway.service.OrderOperateService;
import com.easterfg.takeaway.utils.OrderLogRecord;
import com.easterfg.takeaway.utils.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * @author EasterFG on 2022/11/12
 */
@Aspect
@Component
@Slf4j
public class OrderLogHandler {

    @Resource
    private OrderOperateService orderOperateService;


    private final SpelExpressionParser parser = new SpelExpressionParser();
    /**
     * 参数名称解析器
     */
    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    //    @AfterReturning("@annotation(com.easterfg.takeout.utils.OrderLogRecord)")
    public void handler(JoinPoint point) {
        log.info("订单日志记录");
        // 获取方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        // 取注解
        OrderLogRecord logRecord = method.getAnnotation(OrderLogRecord.class);
//        UserContext.User user =
        UserContext.User user = UserContext.getUser();
        // 获取参数
        Object[] args = point.getArgs();
        Long tradeNo = null;
        if (args[0] instanceof Long) {
            tradeNo = (Long) args[0];
        }
        // user 可能为空
        if (user == null) {
            // 如果user为空, 认定为系统操作
            user = new UserContext.User(null, null, "系统", new ArrayList<>());
        }
        // 输出日志
        OrderOperate operate = new OrderOperate();
        operate.setTradeNo(tradeNo);
        operate.setOperatorId(user.getId());
        operate.setOperatorName(user.getName());
        operate.setMessage(logRecord.value());
        operate.setStatus(logRecord.status());
        operate.setCreateTime(LocalDateTime.now());
//        orderOperateService.save(operate);
        log.info("订单操作日志 msg: {}, tid: {}, uid: {}, name: {}", logRecord.value(), tradeNo, user.getId(), user.getName());
        // 使用完成不需要销毁, 交由控制器统一销毁
    }

    /**
     * 被授权注解的controller 对象, 需要销毁user对象, 在此处统一处理
     */
    @After("@annotation(com.easterfg.takeaway.utils.security.Authorize)")
    public void destroyUser() {
        UserContext.destroy();
    }

    private String[] parser(JoinPoint point, String... values) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        String[] parameterNames = discoverer.getParameterNames(signature.getMethod());
        if (parameterNames == null) {
            return new String[]{""};
        }
        Object[] args = point.getArgs();
        String[] result = new String[values.length];
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int j = 0; j < parameterNames.length; j++) {
            context.setVariable(parameterNames[j], args[j]);
        }
        for (int i = 0; i < values.length; i++) {
            Expression expression = parser.parseExpression(values[i]);
            result[i] = expression.getValue(context, String.class);
        }
        return result;
    }
}
