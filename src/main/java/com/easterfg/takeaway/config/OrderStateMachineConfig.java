package com.easterfg.takeaway.config;

import com.easterfg.takeaway.enums.OrderEvent;
import com.easterfg.takeaway.enums.OrderStatus;
import com.easterfg.takeaway.machine.MemStateMachinePersist;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

/**
 * @author EasterFG on 2023/6/16
 */
@Configuration
@EnableStateMachine(name = "orderStateMachine")
public class OrderStateMachineConfig extends StateMachineConfigurerAdapter<OrderStatus, OrderEvent> {

    /**
     * 配置状态
     */
    @Override
    public void configure(StateMachineStateConfigurer<OrderStatus, OrderEvent> states) throws Exception {
        states
                .withStates()
                .initial(OrderStatus.WAIT_PAYMENT)
                .state(OrderStatus.WAIT_ACCEPT)
                .state(OrderStatus.WAIT_DELIVERY)
                .state(OrderStatus.DELIVERING)
                .state(OrderStatus.REFUNDING)
                .state(OrderStatus.FINISHED)
                .end(OrderStatus.CANCELLED)
                .end(OrderStatus.REFUNDED);
//                .and()
//                .withStates()
//                .states(EnumSet.of(OrderStatus.WAIT_PAYMENT, OrderStatus.WAIT_ACCEPT, OrderStatus.WAIT_DELIVERY, OrderStatus.DELIVERING))
//                .
    }

    /**
     * 配置状态转换事件关系
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStatus, OrderEvent> transitions) throws Exception {
        transitions
                .withExternal().source(OrderStatus.WAIT_PAYMENT).target(OrderStatus.WAIT_ACCEPT).event(OrderEvent.PAYMENT)
                .and()
                // 订单取消  WAIT_PAYMENT -  CANCELED
                .withExternal().source(OrderStatus.WAIT_PAYMENT).target(OrderStatus.CANCELLED).event(OrderEvent.CANCEL)
                .and()

                .withExternal().source(OrderStatus.WAIT_ACCEPT).target(OrderStatus.WAIT_DELIVERY).event(OrderEvent.ACCEPT)
                .and()
                // WAIT_ACCEPT -> REFUNDING 发起退款请求
                .withExternal().source(OrderStatus.WAIT_ACCEPT).target(OrderStatus.REFUNDING).event(OrderEvent.REFUNDING)
                .and()

                .withExternal().source(OrderStatus.WAIT_DELIVERY).target(OrderStatus.DELIVERING).event(OrderEvent.START_DELIVERY)
                .and()
                .withExternal().source(OrderStatus.WAIT_DELIVERY).target(OrderStatus.REFUNDING).event(OrderEvent.REFUNDING)
                .and()

                .withExternal().source(OrderStatus.DELIVERING).target(OrderStatus.FINISHED).event(OrderEvent.FINISH)
                .and()
                // DELIVERING -> REFUNDING
                .withExternal().source(OrderStatus.DELIVERING).target(OrderStatus.REFUNDING).event(OrderEvent.REFUNDED)
                .and()
                // 订单退款
                .withExternal().source(OrderStatus.REFUNDING).target(OrderStatus.REFUNDED).event(OrderEvent.REFUNDED)
                .and()
                // 订单完成后退款
                .withExternal().source(OrderStatus.FINISHED).target(OrderStatus.REFUNDING).event(OrderEvent.REFUNDING);
//                // 取消状态
//                .withExternal().source(OrderStatus.WAIT_ACCEPT).target(OrderStatus.CANCELLED).event(OrderEvent.CANCEL)
//                .and()
//                .withExternal().source(OrderStatus.WAIT_DELIVERY).target(OrderStatus.CANCELLED).event(OrderEvent.CANCEL)
//                .and()
//                .withExternal().source(OrderStatus.DELIVERING).target(OrderStatus.CANCELLED).event(OrderEvent.CANCEL);
//                .withChoice().source(OrderStatus.WAIT_PAYMENT).source(OrderStatus.WAIT_ACCEPT).source(OrderStatus.WAIT_DELIVERY).source(OrderStatus.DELIVERING)


    }

    @Bean
    public StateMachinePersister memStateMachinePersister(MemStateMachinePersist memStateMachinePersist) {
        return new DefaultStateMachinePersister(memStateMachinePersist);
    }
}
