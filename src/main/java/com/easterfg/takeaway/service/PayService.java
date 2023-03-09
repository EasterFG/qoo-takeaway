package com.easterfg.takeaway.service;

import com.easterfg.takeaway.dto.PayQueryDTO;
import org.springframework.scheduling.annotation.Async;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author EasterFG on 2022/10/14
 */
public interface PayService {

    void asyncNotify(Map<String, String> params);

    /**
     * 下单接口
     */
    String payOrder(Long tradeId, BigDecimal price, String timeout);


    /**
     * 支付结果查询
     *
     * @return 查询结果
     */
    PayQueryDTO queryOrder(long tradeNo);

    /**
     * 退款接口
     */
    @Async
    Future<Boolean> refund(String outTradeNo, double amount);

    /**
     * 取消订单接口
     */
    boolean cancel(Long tradeNo);

}
