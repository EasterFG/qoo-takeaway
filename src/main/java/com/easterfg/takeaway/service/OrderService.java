package com.easterfg.takeaway.service;

import com.easterfg.takeaway.domain.Order;
import com.easterfg.takeaway.domain.OrderStatusCount;
import com.easterfg.takeaway.dto.OrderDTO;
import com.easterfg.takeaway.dto.PageData;
import com.easterfg.takeaway.dto.RefundDTO;
import com.easterfg.takeaway.dto.Result;
import com.easterfg.takeaway.enums.OrderStatus;
import com.easterfg.takeaway.query.PageQuery;
import com.easterfg.takeaway.utils.security.Role;

import java.time.LocalDate;

/**
 * @author EasterFG on 2022/10/24
 */
public interface OrderService {


    /**
     * 创建订单
     */
    Long create(OrderDTO orderDTO);

    /**
     * 支付订单
     */
    String pay(Long tradeNo);

    /**
     * 接受订单
     */
    void accept(Long tradeNo);

    /**
     * 配送订单
     */
    void startDelivery(Long tradeNo);

    /**
     * 订单完成
     */
    void finished(long tradeNo);

    void refund(RefundDTO refundDTO);

    /**
     * 订单取消
     */
    Result cancel(Long tradeNo, Role source, String cancelReason);

    /**
     * 获取订单详情
     *
     * @param tradeNo 订单编号
     * @return 订单
     */
    Order getOrder(long tradeNo);

    /**
     * 统计状态
     *
     * @return
     */
    OrderStatusCount orderCount();

    PageData<Order> listOrder(Long uid, PageQuery pageQuery, OrderStatus status, String tradeNO, String phone, LocalDate start, LocalDate end);
}
