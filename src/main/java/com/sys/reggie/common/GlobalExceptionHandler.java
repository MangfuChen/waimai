package com.sys.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 * annotations---需要拦截controller的类型
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 进行指定的异常处理
     * SQLIntegrityConstraintViolationException异常
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());
        if(ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" ");
            String msg = split[2]+"已存在";
            return Result.error(msg);
        }
        return Result.error("服务繁忙，请稍后重试");
    }

    /**
     * 处理自定义异常
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public Result<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());

        return Result.error(ex.getMessage());
    }
}
