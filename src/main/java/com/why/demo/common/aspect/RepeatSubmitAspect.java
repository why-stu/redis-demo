package com.why.demo.common.aspect;

import com.why.demo.common.annotation.RepeatSubmit;
import com.why.demo.exception.ErrorCode;
import com.why.demo.exception.YyException;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class RepeatSubmitAspect {
    private static final Logger logger = LoggerFactory.getLogger(RepeatSubmitAspect.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    /**
     * 定义切点
     */
    @Pointcut("@annotation(com.why.demo.common.annotation.RepeatSubmit)")
    public void repeatSubmit() {}

    @Around("repeatSubmit()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (attributes == null) {
            logger.info("获取接口信息失败，无法执行");
            throw new YyException(ErrorCode.ERROR_TO_GET_INTERFACE_INFO);
        }
        HttpServletRequest request = attributes.getRequest();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        // 获取防重复提交注解
        RepeatSubmit annotation = method.getAnnotation(RepeatSubmit.class);
        String url = request.getRequestURI();
        /**
         *  通过前缀 + url
         *  可以在加上用户id，可以针对某个用户点击某个接口做校验
         */
        String redisKey = "repeat_submit_key:"
                .concat(url);
        logger.info("==========redisKey ====== {}",redisKey);
        Boolean exist = redisTemplate.hasKey(redisKey);
        boolean isExist = exist == null ? false : exist;

        if (!isExist) {
            redisTemplate.opsForValue().set(redisKey, redisKey, annotation.expireTime(), TimeUnit.SECONDS);
            try {
                //正常执行方法并返回
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                logger.error("方法执行异常", throwable);
                redisTemplate.delete(redisKey);
                throw new YyException(throwable);
            }
        } else {
            // 抛出异常
            throw new YyException(ErrorCode.PLEASE_NOT_RESUBMIT);
        }
    }
}