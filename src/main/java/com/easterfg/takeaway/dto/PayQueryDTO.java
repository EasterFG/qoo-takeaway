package com.easterfg.takeaway.dto;

import lombok.Data;
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
public class PayQueryDTO {

    /**
     * 网关返回码
     */
    private String subCode;

    /**
     * 交易状态
     */
    private Status tradeStatus;

    /**
     * 订单编号
     */
    private String tradeNo;

    /**
     * 外部流水号
     */
    private String outTradeNo;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    public void conversion( String tradeStatus, String tradeNo, String outTradeNo, Date paymentTime) {
        this.tradeStatus = Status.valueOf(tradeStatus);
        this.tradeNo = tradeNo;
        this.outTradeNo = outTradeNo;
        this.paymentTime = LocalDateTime.ofInstant(paymentTime.toInstant(),
                ZoneId.systemDefault());
    }

    public enum Status {
        WAIT_BUYER_PAY, TRADE_CLOSED, TRADE_SUCCESS, TRADE_FINISHED
    }

    public void setSubCode(String subCode) {
        this.subCode = subCode;
    }
}
