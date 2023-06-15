package com.easterfg.takeaway.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easterfg.takeaway.domain.OrderDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author EasterFG on 2022/10/24
 */
public interface OrderDetailDAO extends BaseMapper<OrderDetail> {

    @Select("select name, number, dish_flavor from order_detail where trade_no = #{tradeNo}")
    List<OrderDetail> listOrderDetail(Long tradeNo);

    @Insert("insert into order_detail (name, image, trade_no, dish_id, combo_id, dish_flavor, number, amount) " +
            "values (#{name}, #{image}, #{tradeNo}, #{dishId}, #{comboId}, #{dishFlavor}, #{number}, #{amount});")
    int insert(OrderDetail orderDetail);
}
