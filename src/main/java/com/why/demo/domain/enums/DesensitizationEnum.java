package com.why.demo.domain.enums;

import com.why.demo.util.DesensitizationUtils;

import java.util.function.Function;

/**
 *  脱敏枚举类
 */
public enum DesensitizationEnum {

    /**
     * 用户名称脱敏
     */
    USERNAME(DesensitizationUtils::username),
    /**
     * 固定电话号码脱敏
     */
    FIXED_PHONE(DesensitizationUtils::fixedPhone),
    /**
     * 手机号码脱敏
     */
    MOBILE_PHONE(DesensitizationUtils::mobilePhone),
    /**
     * 金额脱敏
     */
    MONEY(DesensitizationUtils::money),
    /**
     * 身份证号脱敏
     */
    ID_CARD_NO(DesensitizationUtils::idCardNo),
    /**
     * 电子邮箱脱敏
     */
    EMAIL(DesensitizationUtils::email),
    /**
     * 地址脱敏
     */
    ADDRESS(DesensitizationUtils::address),
    /**
     * 银行卡账号脱敏
     */
    BANK_ACCOUNT(DesensitizationUtils::bankAccount),
    /**
     * 密码脱敏
     */
    PASSWORD(DesensitizationUtils::password),
    /**
     * 秘钥脱敏
     */
    SECRET_KEY(DesensitizationUtils::secretKey);

    /**
     * 成员变量  是一个接口类型
     */
    private final Function<String, String> function;

    DesensitizationEnum(Function<String, String> function) {
        this.function = function;
    }

    public Function<String, String> getFunction() {
        return function;
    }
}