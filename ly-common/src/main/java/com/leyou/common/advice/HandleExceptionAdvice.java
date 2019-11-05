package com.leyou.common.advice;

import com.leyou.common.exceptions.LyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/10/31 15:23
 * @description:
 */
@Slf4j
@ControllerAdvice
public class HandleExceptionAdvice {
    /**
     * 加上@ExceptionHandler后面可以加上value的值，表示拦截所有controller的值得异常,
     * 并返回异常信息
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException e) {
        //返回异常的状态码和错误信息，有e.getmessage来获取
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }


    @ExceptionHandler(LyException.class)
    public ResponseEntity<String> handleException(LyException e) {
        //返回异常的状态码和错误信息，有e.getmessage来获取

        return ResponseEntity.status(e.getStatus()).body(e.getMessage());
    }
}
