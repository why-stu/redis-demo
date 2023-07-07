package com.why.demo.exception;

import com.why.demo.common.ResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = {"com.why.demo.controller"})
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResultData handlerException(Exception e) {
        logger.error("GlobalExceptionHandler, Request Exception", e);
        return ResultData.fail(ErrorCode.SERVICE_ERROR);
    }

    @ExceptionHandler(YyException.class)
    public ResultData handlerYyException(YyException e) {
        return ResultData.fail(e.getErrorCode().getCode(), e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResultData handlerIllegalArgumentException(IllegalArgumentException e) {
        logger.error("GlobalExceptionHandler, Request IllegalArgumentException, {}", e.getMessage());
        return ResultData.fail(ErrorCode.REQUEST_PARAM_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultData exceptionHandler(MethodArgumentNotValidException e) {
        String[] errors = e.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).toArray(String[]::new);
        logger.error("GlobalExceptionHandler, Request MethodArgumentNotValidException, errorMessage={}, {}", String.join(",", errors), e.getMessage());
        return ResultData.fail(ErrorCode.BAD_REQUEST, String.join(",", errors));
    }
}
