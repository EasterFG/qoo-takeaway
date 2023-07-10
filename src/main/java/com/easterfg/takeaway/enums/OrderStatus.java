package com.easterfg.takeaway.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author EasterFG on 2023/6/12
 * <p>
 * 订单状态枚举
 */
@JsonFormat(shape = JsonFormat.Shape.NUMBER)
public enum OrderStatus {
    WAIT_PAYMENT(0, "待支付"),
    WAIT_ACCEPT(1, "待接单"),
    WAIT_DELIVERY(2, "待配送"),
    DELIVERING(3, "配送中"),
    REFUNDING(4, "退款中"),
    REFUNDED(5, "订单取消(已退款)"),
    FINISHED(6, "订单完成"),
    /**
     * 待支付状态被取消,进入此状态
     */
    CANCELLED(7, "订单取消");

    private final int code;
    private final String desc;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderStatus valueOf(int code) {
        OrderStatus[] statuses = values();
        if (code < 0 || code >= statuses.length)
            throw new IllegalArgumentException("Invalid OrderStatus code: " + code);
        return statuses[code];
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
