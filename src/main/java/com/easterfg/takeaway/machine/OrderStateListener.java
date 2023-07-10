package com.easterfg.takeaway.machine;

import com.easterfg.takeaway.dao.OrderDAO;
import com.easterfg.takeaway.domain.Order;
import com.easterfg.takeaway.enums.OrderEvent;
import com.easterfg.takeaway.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author EasterFG on 2023/6/17
 */
@Component
@WithStateMachine(name = "orderStateMachine")
@Slf4j
public class OrderStateListener {

    private final OrderDAO orderDAO;

    private final Map<Long, Sinks.Many<OrderStatus>> emitters = new ConcurrentHashMap<>();

    public OrderStateListener(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    /**
     * 订单支付
     */
    @OnTransition(source = "WAIT_PAYMENT", target = "WAIT_ACCEPT")
    public void paymentTransition(Message<OrderEvent> message) {
        transition(message, OrderStatus.WAIT_PAYMENT, OrderStatus.WAIT_ACCEPT);
    }

    /**
     * 订单取消
     */
    @OnTransition(source = "WAIT_PAYMENT", target = "CANCELLED")
    public void cancelTransition(Message<OrderEvent> message) {
        Order order = (Order) message.getHeaders().get("order");
        if (order == null) {
            return;
        }
        // 更新订单取消时间
        transition(order, OrderStatus.WAIT_PAYMENT, OrderStatus.CANCELLED);
        completeEmitter(order.getTradeNo());
    }

    /**
     * 订单接受
     */
    @OnTransition(source = "WAIT_ACCEPT", target = "WAIT_DELIVERY")
    public void acceptTransition(Message<OrderEvent> message) {
        transition(message, OrderStatus.WAIT_ACCEPT, OrderStatus.WAIT_DELIVERY);
    }

    /**
     * 订单开始配送
     */
    @OnTransition(source = "WAIT_DELIVERY", target = "DELIVERING")
    public void startDeliveryTransition(Message<OrderEvent> message) {
        transition(message, OrderStatus.WAIT_DELIVERY, OrderStatus.DELIVERING);

    }

    /**
     * 订单完成
     */
    @OnTransition(source = "DELIVERING", target = "FINISHED")
    public void finishTransition(Message<OrderEvent> message) {
        Order order = (Order) message.getHeaders().get("order");
        if (order == null) {
            return;
        }
        transition(order, OrderStatus.DELIVERING, OrderStatus.FINISHED);
        completeEmitter(order.getTradeNo());
    }

    /**
     * 订单进入退款中
     */
    @OnTransition(source = {"WAIT_ACCEPT", "WAIT_DELIVERY", "DELIVERING"}, target = "REFUNDING")
    public void refundingTransition(Message<OrderEvent> message) {
        Order order = (Order) message.getHeaders().get("order");
        if (order == null) {
            return;
        }
        transition(order, order.getStatus(), OrderStatus.REFUNDING);
        // 订单进入最终状态
        completeEmitter(order.getTradeNo());
    }

    /**
     * 退款中 -> 退款完成
     */
    @OnTransition(source = "REFUNDING", target = "REFUNDED")
    public void refundTransition(Message<OrderEvent> message) {
        Order order = (Order) message.getHeaders().get("order");
        if (order == null) {
            return;
        }
        transition(order, OrderStatus.REFUNDING, OrderStatus.REFUNDED);
        completeEmitter(order.getTradeNo());
    }

    private void transition(Message<OrderEvent> message, OrderStatus expect, OrderStatus update) {
        Order order = (Order) message.getHeaders().get("order");
        if (order == null) {
            return;
        }
        // 更新订单状态
        transition(order, expect, update);
    }

    private void transition(Order order, OrderStatus expect, OrderStatus update) {
        // 更新订单状态
        orderDAO.updateOrderStatus(order.getTradeNo(), expect, update);
        order.setStatus(update);
        // 通知sse
        Sinks.Many<OrderStatus> skins = emitters.get(order.getTradeNo());
        if (skins != null) {
            skins.tryEmitNext(update);
        }
    }

    public Sinks.Many<OrderStatus> addEmitter(Long tradeNo) {
        return emitters.computeIfAbsent(tradeNo, key -> Sinks.many().multicast().onBackpressureBuffer());
    }

    public void removeEmitter(Long tradeNo) {
        emitters.remove(tradeNo);
    }

    public Sinks.Many<OrderStatus> get(Long tradeNo) {
        return emitters.get(tradeNo);
    }

    public void completeEmitter(Long tradeNo) {
        Sinks.Many<OrderStatus> many = emitters.get(tradeNo);
        if (many != null) {
            many.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
        }
    }
}
