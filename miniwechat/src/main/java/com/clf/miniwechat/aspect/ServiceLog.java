package com.clf.miniwechat.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * service层日志
 * @author clf
 */
@Aspect
@Component
@Slf4j
public class ServiceLog {

    @Pointcut("execution(public * com.clf.miniwechat.service.*.*(..))")
    public void service(){
    }

    @Before("service()")
    public void before(JoinPoint joinPoint){
        Signature signature = joinPoint.getSignature();
        String method = signature.getDeclaringTypeName()+"."+signature.getName();
        log.warn("-----------------------------------------------------");
        log.warn("当前执行的service方法:"+method);
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            log.warn("参数:"+arg);
        }
    }

    @AfterReturning(pointcut = "service()",returning = "ret")
    public void after(Object ret){
        log.warn("service返回参数:"+ret);
        log.warn("-----------------------------------------------------");

    }
}
