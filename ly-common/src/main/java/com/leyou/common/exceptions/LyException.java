package com.leyou.common.exceptions;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Getter;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/10/31 15:54
 * @description:
 */
@Getter
public class LyException extends RuntimeException {
    private int status;

    public LyException(ExceptionEnum e) {
        super(e.getMessage());
        this.status = e.getStatus();
    }

    public LyException(ExceptionEnum e, Throwable throwable) {
        super(e.getMessage(), throwable);
        this.status = e.getStatus();
    }

    public LyException(int status, String message) {
        super(message);
        this.status = status;
    }

    public LyException(int status, String message, Throwable throwable) {
        super(message, throwable);
        this.status = status;
    }

    public LyException(int status, Throwable throwable) {
        super(throwable);
        this.status = status;
    }
}
