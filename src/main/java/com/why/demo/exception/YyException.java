package com.why.demo.exception;

public class YyException extends RuntimeException {

    private ErrorCode errorCode = ErrorCode.SERVICE_ERROR;
    private String message = "服务端异常";

    public YyException() {
        super();
    }

    public YyException(String message) {
        super(message);
        this.message = message;
    }

    public YyException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }

    public YyException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }

    public YyException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }

    public YyException(String message, Throwable cause) {
        super(message, cause);
    }

    public YyException(Throwable cause) {
        super(cause);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage(){
        return message;
    }
}
