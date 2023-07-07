package com.why.demo.common;

import com.why.demo.exception.ErrorCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "接口调用结果")
public class ResultData<T> implements Serializable {
    private static final long serialVersionUID = -3721099500253877290L;

    @ApiModelProperty(value = "是否成功", position = 0)
    private Boolean success;

    @ApiModelProperty(value = "返回码(200-成功，非200-失败)", position = 1)
    private int code;

    @ApiModelProperty(value = "错误描述信息", position = 2)
    private String message;

    @ApiModelProperty(value = "返回数据(可以为空)", position = 3)
    private T data;

    public ResultData(Boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ResultData<T> success() {
        return new ResultData<T>(true, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null);
    }

    public static <T> ResultData<T> success(String message) {
        return new ResultData<T>(true, ErrorCode.SUCCESS.getCode(), message, null);
    }

    public static <T> ResultData<T> success(T data) {
        return new ResultData<T>(true, ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    public static <T> ResultData<T> fail(int code, String message) {
        return new ResultData<>(false, code, message, null);
    }

    public static <T> ResultData<T> fail(ErrorCode errorCode) {
        return new ResultData<>(false, errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> ResultData<T> fail(ErrorCode errorCode, T data) {
        return new ResultData<T>(false, errorCode.getCode(), errorCode.getMessage(), data);
    }

    public static <T> ResultData<T> fail(ErrorCode errorCode, String message) {
        return new ResultData<>(false, errorCode.getCode(), message, null);
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
