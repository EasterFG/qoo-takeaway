package com.easterfg.takeaway.utils.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author EasterFG on 2023/6/13
 * <p>
 * 支付状态枚举
 */
@JsonFormat(shape = JsonFormat.Shape.NUMBER)
public enum PayStatus {

    UNPAID(0),
    PAID(1),
    REFUNDED(2),
    CANCEL(3);

    private final int code;

    PayStatus(int code) {
        this.code = code;
    }

    public static PayStatus valueOf(int code) {
        PayStatus[] statuses = values();
        if (code < 0 || code >= statuses.length)
            throw new IllegalArgumentException("Invalid PayStatus code: " + code);
        return statuses[code];
    }

    public int getCode() {
        return code;
    }

}
