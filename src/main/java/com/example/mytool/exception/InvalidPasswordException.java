package com.example.mytool.exception;
//专门用于处理密码验证失败的业务场景
public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
