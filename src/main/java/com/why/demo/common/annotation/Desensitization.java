package com.why.demo.common.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.why.demo.config.DesensitizationSerializer;
import com.why.demo.domain.enums.DesensitizationEnum;

import java.lang.annotation.*;

/**
 * @author wanghy
 */
@Target({ElementType.FIELD, ElementType.TYPE}) // 注解只能用于字段、类/接口上
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@JsonSerialize(using = DesensitizationSerializer.class)
public @interface Desensitization {

    /**
     * 脱敏的字段类型
     */
    DesensitizationEnum desensitizationType();

}