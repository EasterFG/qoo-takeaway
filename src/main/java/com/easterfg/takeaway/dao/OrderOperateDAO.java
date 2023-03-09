package com.easterfg.takeaway.dao;

import com.easterfg.takeaway.domain.OrderOperate;
import org.apache.ibatis.annotations.Insert;

/**
 * @author EasterFG on 2022/11/14
 */
public interface OrderOperateDAO {

    @Insert("insert into order_operate (trade_no, operator_id, operator_name, status, message, create_time) values " +
            "(#{tradeNo}, #{operatorId}, #{operatorName}, #{status}, #{message}, now());")
    void insert(OrderOperate orderOperate);

}
