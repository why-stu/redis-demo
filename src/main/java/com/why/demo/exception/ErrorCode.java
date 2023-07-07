package com.why.demo.exception;

public enum ErrorCode {
    SUCCESS(200, "成功"),
    SERVICE_ERROR(500, "服务端异常"),
    BAD_REQUEST(60001, "请求无效"),
    REQUEST_PARAM_ERROR(60002, "请求参数错误"),

    ERROR_TO_GET_INTERFACE_INFO(61001, "获取接口信息失败"),
    PLEASE_NOT_RESUBMIT(61002, "请勿重复提交");

    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
