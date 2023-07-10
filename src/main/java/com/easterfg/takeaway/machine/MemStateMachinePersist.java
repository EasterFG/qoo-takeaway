package com.easterfg.takeaway.machine;

import com.easterfg.takeaway.dao.OrderDAO;
import com.easterfg.takeaway.domain.Order;
import com.easterfg.takeaway.enums.OrderEvent;
import com.easterfg.takeaway.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @author EasterFG on 2023/6/20
 */
@Component
@Slf4j
public class MemStateMachinePersist implements StateMachinePersist<OrderStatus, OrderEvent, Long> {

    @Resource
    private OrderDAO orderDAO;

    private final HashMap<Long, StateMachineContext<OrderStatus, OrderEvent>> contexts = new HashMap<>();

    @Override
    public void write(StateMachineContext<OrderStatus, OrderEvent> context, Long s) throws Exception {
        contexts.put(s, context);
    }

    @Override
    public StateMachineContext<OrderStatus, OrderEvent> read(Long s) throws Exception {
        StateMachineContext<OrderStatus, OrderEvent> context = contexts.get(s);
        if (context == null) {
            // 尝试从数据库读取
            Order status = orderDAO.getOrderStatus(s, null);
            context = new DefaultStateMachineContext<>(status.getStatus(),
                    null, null, new DefaultExtendedState());
        }
        return context;
    }
}
