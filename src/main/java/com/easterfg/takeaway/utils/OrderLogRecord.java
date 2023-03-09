package com.easterfg.takeaway.utils;

import java.lang.annotation.*;

/**
 * @author EasterFG on 2022/11/12
 * <p>
 * 订单日志记录
 * 日志信息包含
 * 用户下单
 * 商家接单
 * 开始配送
 * 配送完成
 * 订单取消
 * 订单退款
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OrderLogRecord {

    /**
     * 消息摘要
     */
    String value();

    /**
     * 订单修改后状态
     */
    int status();

}