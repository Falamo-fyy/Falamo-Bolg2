package com.example.mytool.exception;
//用于处理用户名已存在的业务逻辑异常
public class UsernameExistsException extends RuntimeException {
    public UsernameExistsException(String message) {
        super(message);
    }
} 