package com.easterfg.takeaway.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easterfg.takeaway.domain.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author EasterFG on 2022/10/22
 */
public interface ShoppingCartDAO extends BaseMapper<ShoppingCart> {

    @Select("select id, name, image, dish_id, combo_id, dish_flavor, number, amount from shopping_cart where user_id = #{userId}")
    List<ShoppingCart> listByUserId(long userId);

    @Select("select ifnull(sum(amount), 0) from shopping_cart where user_id = #{userId};")
    BigDecimal totalAmount(Long userId);

    @Delete("delete from shopping_cart where user_id = #{id};")
    void deleteByUserId(long id);
}
