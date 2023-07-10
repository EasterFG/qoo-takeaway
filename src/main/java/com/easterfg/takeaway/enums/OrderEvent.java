package com.easterfg.takeaway.enums;

/**
 * @author EasterFG on 2023/6/16
 */
public enum OrderEvent {
    /**
     * 订单支付事件
     */
    PAYMENT,

    /**
     * 订单被接受事件
     */
    ACCEPT,

    /**
     * 订单开始配送
     */
    START_DELIVERY,

    /**
     * 订单配送完成
     */
    FINISH,

    /**
     * 订单取消
     */
    CANCEL,

    /**
     * 订单退款中
     */
    REFUNDING,

    /**
     * 订单退款完成
     */
    REFUNDED;
}
