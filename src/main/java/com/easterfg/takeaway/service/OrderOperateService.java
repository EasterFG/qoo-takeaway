package com.easterfg.takeaway.service;

import com.easterfg.takeaway.utils.security.UserContext;

/**
 * @author EasterFG on 2022/11/14
 */
public interface OrderOperateService {

    /**
     * 记录订单操作日志
     *
     * @param tradeNo 订单编号
     * @param message 操作信息
     * @param status  状态
     */
    void recordLog(UserContext.User user, Long tradeNo, String message, Integer status);

}
