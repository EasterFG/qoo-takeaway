package com.easterfg.takeaway.exception;

import lombok.Getter;

/**
 * @author EasterFG on 2022/10/24
 * <p>
 * 全局通用业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 6566321326715439236L;

    /**
     * 通用错误码
     * 20000 业务不可用
     * 20001 授权权限不足
     * 40001 缺少必选参数
     * 40002 非法参数
     * 40004 业务处理失败
     * 40005 接口限流
     * 40006 权限不足
     * 40007 校验失败
     * 40008 拒绝服务异常
     * 50000 服务器未知异常
     */
    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = "40004";
    }
}
