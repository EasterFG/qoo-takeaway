package com.easterfg.takeaway.machine;

import com.easterfg.takeaway.domain.Order;
import com.easterfg.takeaway.enums.OrderEvent;
import com.easterfg.takeaway.enums.OrderStatus;
import com.easterfg.takeaway.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author EasterFG on 2023/6/20
 */
@Component
@AllArgsConstructor
public class MachineUtils {

    @Resource
    private StateMachine<OrderStatus, OrderEvent> orderStateMachine;

    @Resource
    private StateMachinePersister<OrderStatus, OrderEvent, Long> memStateMachinePersister;

    /**
     * 向状态机发送事件
     *
     * @param event 事件
     * @param order 订单对象 一般来说只需要 订单编号 订单状态
     */
    public void sendMessage(OrderEvent event, Order order) {
        try {
            orderStateMachine.start();
            memStateMachinePersister.restore(orderStateMachine, order.getTradeNo());
            var message = MessageBuilder.withPayload(event)
                    .setHeader("order", order).build();
            if (!orderStateMachine.sendEvent(message))
                throw new BusinessException("订单不允许此操作");
            memStateMachinePersister.persist(orderStateMachine, order.getTradeNo());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            orderStateMachine.stop();
        }
    }

}
