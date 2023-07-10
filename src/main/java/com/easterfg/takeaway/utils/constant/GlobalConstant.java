package com.easterfg.takeaway.utils.constant;

import java.time.format.DateTimeFormatter;

/**
 * @author EasterFG on 2022/10/24
 */
public class GlobalConstant {


    public static final String TRADE_KEY = "trade:";
    public static final String PAY_SUCCESS = "TRADE_SUCCESS";
    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * 订单状态
     */
    public static final String ORDER_STATUS_EXCEPTION = "订单状态异常";
    public static final int WAIT_PAYMENT = 1;
    public static final int WAIT_APPROVAL = 2;
    public static final int CANCEL = 6;

    private GlobalConstant() {
    }
}
