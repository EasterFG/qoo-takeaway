package com.easterfg.takeaway.dto;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author EasterFG on 2022/11/11
 * <p>
 * 订单详情
 */
@Getter
@ToString
public class PayQueryDTO {

    /**
     * 交易状态
     */
    private final Status tradeStatus;

    /**
     * 订单编号
     */
    private final String tradeNo;

    /**
     * 外部流水号
     */
    private final String outTradeNo;

    /**
     * 支付时间
     */
    private final LocalDateTime paymentTime;

    public PayQueryDTO(String tradeStatus, String tradeNo, String outTradeNo, Date paymentTime) {
        this.tradeStatus = Status.valueOf(tradeStatus);
        this.tradeNo = tradeNo;
        this.outTradeNo = outTradeNo;
        this.paymentTime = LocalDateTime.ofInstant(paymentTime.toInstant(),
                ZoneId.systemDefault());
    }

    public enum Status {
        WAIT_BUYER_PAY, TRADE_CLOSED, TRADE_SUCCESS, TRADE_FINISHED
    }

}
