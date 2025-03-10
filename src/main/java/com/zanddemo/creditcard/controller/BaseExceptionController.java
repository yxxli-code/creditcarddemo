package com.zanddemo.creditcard.controller;

import com.zanddemo.creditcard.enums.ValidationResult;
import com.zanddemo.creditcard.exceptions.BusinessException;
import com.zanddemo.creditcard.valueobject.ApiResult;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
public class BaseExceptionController {

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = BusinessException.class)
    public ApiResult businessException(BusinessException e) {
        log.error("BaseExceptionController businessException {}:" + e.getCode(), e);
        return new ApiResult().setCode(e.getCode()).setMessage(e.getMessage()).setData(e.getData()).setSuccess(false);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public ApiResult exception(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception " + ex.getMessage(), ex);
        return new ApiResult(ValidationResult.SERVER_ERROR.getCode(), ex.getMessage()).setSuccess(false);
    }

}
