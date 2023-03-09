package com.easterfg.takeaway.service.impl;

import com.easterfg.takeaway.dao.OrderOperateDAO;
import com.easterfg.takeaway.domain.OrderOperate;
import com.easterfg.takeaway.service.OrderOperateService;
import com.easterfg.takeaway.utils.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author EasterFG on 2022/11/14
 */
@Service
@Slf4j
public class OrderOperateServiceImpl implements OrderOperateService {

    @Resource
    private OrderOperateDAO orderOperateDAO;

    @Override
    public void recordLog(UserContext.User user, Long tradeNo, String message, Integer status) {
        OrderOperate orderOperate = new OrderOperate();
        if (user == null) {
            orderOperate.setOperatorId(null);
            orderOperate.setOperatorName("系统");
        } else {
            orderOperate.setOperatorId(user.getId());
            orderOperate.setOperatorName(user.getName());
        }
        orderOperate.setTradeNo(tradeNo);
        orderOperate.setMessage(message);
        orderOperate.setStatus(status);
        orderOperateDAO.insert(orderOperate);
        log.info("订单状态变化 --> {}", orderOperate);
    }
}
