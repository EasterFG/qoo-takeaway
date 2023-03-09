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

    @Select("select name, number, dish_flavor from order_detail where id = #{id}")
    List<OrderDetail> listOrderDetail(Long id);

    @Insert("insert into order_detail (name, image, order_id, dish_id, combo_id, dish_flavor, number, amount) " +
            "values (#{name}, #{image}, #{orderId}, #{dishId}, #{comboId}, #{dishFlavor}, #{number}, #{amount});")
    int insert(OrderDetail orderDetail);
}
