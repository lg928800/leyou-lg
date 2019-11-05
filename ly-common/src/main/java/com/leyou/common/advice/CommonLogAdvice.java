package com.leyou.common.advice;

import com.leyou.common.exceptions.LyException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;


/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/2 20:20
 * @description:
 */
@Slf4j
@Aspect
@Component
public class CommonLogAdvice {

    @Around("@within(org.springframework.stereotype.Service)")
    public Object handleExceptionLog(ProceedingJoinPoint jp){
        log.debug("{}方法准备调用，参数: {}", jp.getSignature(), Arrays.toString(jp.getArgs()));
        try {
            long begin = System.currentTimeMillis();
            Object result = jp.proceed();
            long end = System.currentTimeMillis();
            log.debug("{}方法调用成功, 耗时{}ms", jp.getSignature(), end - begin);
            return result;
        } catch (Throwable throwable) {
            log.error("{}方法调用异常，原因：{}", jp.getSignature(), throwable.getMessage(), throwable);
            if(throwable instanceof LyException){
                throw (LyException)throwable;
            }
            throw new LyException(500, throwable);
        }
    }
}
