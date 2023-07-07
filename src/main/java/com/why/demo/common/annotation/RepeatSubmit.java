package com.why.demo.common.annotation;

import java.lang.annotation.*;

/**
 * 自定义注解防止表单重复提交
 * @author wanghy
 */
@Target(ElementType.METHOD) // 注解只能用于方法
@Retention(RetentionPolicy.RUNTIME) // 修饰注解的生命周期
@Documented
public @interface RepeatSubmit {

    /**
     * 防重复操作过期时间,默认1s
     */
    long expireTime() default 1;
}