package com.easterfg.takeaway.utils.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author EasterFG on 2023/6/12
 * <p>
 * 订单状态枚举
 */
@JsonFormat(shape = JsonFormat.Shape.NUMBER)
public enum OrderStatus {
    WAIT_PAYMENT(0),
    WAIT_ACCEPT(1),
    WAIT_DELIVERY(2),
    DELIVERING(3),
    FINISHED(4),
    CANCELLED(5);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
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
}
