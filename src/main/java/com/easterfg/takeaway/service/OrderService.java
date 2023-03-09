package com.easterfg.takeaway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easterfg.takeaway.domain.Order;
import com.easterfg.takeaway.domain.OrderStatusStatistics;
import com.easterfg.takeaway.dto.OrdersDTO;
import com.easterfg.takeaway.dto.PageData;
import com.easterfg.takeaway.query.PageQuery;

import java.util.Map;

/**
 * @author EasterFG on 2022/10/24
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     *
     * @param ordersDTO 订单数据传递
     * @return 三方支付表单
     */
    Map<String, Object> placeAnOrder(OrdersDTO ordersDTO);

    /**
     * 获取订单详情
     *
     * @param tradeNo 订单编号
     * @return 订单
     */
    Order getOrder(long tradeNo);

//    /**
//     * 用户支付
//     */
//    boolean userPay(long tradeNo, LocalDateTime paymentTime);

    /**
     * 统计状态
     */
    OrderStatusStatistics statistics();

    /**
     * 取消订单
     *
     * @param tradeNo      订单编号
     * @param cancelReason 取消原因
     */
    void cancelOrder(Long tradeNo, Long uid, String cancelReason);

//    /**
//     * 接受订单
//     *
//     * @param tradeNo 订单编号
//     * @return 是否成功
//     */
//    boolean approveOrder(Long tradeNo);
//
//    /**
//     * 拒绝订单
//     *
//     * @param tradeNo 订单编号
//     * @return 是否成功
//     */
//    boolean deliveryOrder(Long tradeNo);
//
//    /**
//     * 订单完成
//     *
//     * @param tradeNo 订单编号
//     * @return 是否成功
//     */
//    boolean completeOrder(Long tradeNo);

    /**
     * 更新订单状态
     *
     * @param tradeNo  订单编号
     * @param expected 预期状态
     * @param actual   实际状态
     * @return 是否修改成功
     */
    boolean updateOrderStatus(long tradeNo, int expected, int actual);

    /**
     * 订单退款
     *
     * @param tradeNo 订单编号
     */
    void refundOrder(long tradeNo, double amount);

    PageData<Order> listOrder(PageQuery pageQuery, Integer status, Long uid);
}
