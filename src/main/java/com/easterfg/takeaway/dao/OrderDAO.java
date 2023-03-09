package com.easterfg.takeaway.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easterfg.takeaway.domain.Order;
import com.easterfg.takeaway.domain.OrderStatusStatistics;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author EasterFG on 2022/10/24
 */
public interface OrderDAO extends BaseMapper<Order> {

    @Select("SELECT count(*) FROM takeout_order WHERE user_id = #{uid} order by create_time")
    int count(Long uid);

    List<Order> listUserOrder(Long uid);

    Order getOrder(Long tid);

    //    @Select("select status, pay_status, amount, user_id from takeout_order where trade_no = #{tradeNo};")
    Order getOrderStatus(@Param("tradeNo") long tradeNo, @Param("uid") Long uid);

    /**
     * 更新订单状态
     *
     * @param order    订单信息
     * @param expected 预期状态
     * @return 结果
     */
    int updateStatus(@Param("data") Order order, @Param("expected") int expected);

    @Select("select sum(IF(status = 2, 1, 0)) as confirm, " +
            "sum(IF(status = 3, 1, 0)) as prepare, " +
            "sum(IF(status = 4, 1, 0)) as proceed " +
            "from takeout_order " +
            "limit 1000;")
    OrderStatusStatistics statistics();


    @Select("SELECT count(*) as total, sum(if(status = 5, 1, 0)) as complete, sum(if(status = 6,1,0)) as cancel" +
            " from takeout_order where close_time between #{time} and adddate(#{time}, 1)")
    OrderStatusStatistics statisticsByDate(LocalDateTime time);

    @Select("SELECT ifnull(sum(amount), 0) from takeout_order where status = 5 and close_time between #{time} and adddate(#{time}, 1)")
    BigDecimal statisticsAmount(LocalDateTime time);

    @Update("update takeout_order set pay_status = #{payStatus} where trade_no = #{tradeNo}")
    void updatePayStatus(@Param("tradeNo") Long tradeNo, @Param("payStatus") int payStatus);

    List<Order> listOrder(Integer status);

    @Insert("insert into takeout_order (user_id, trade_no, pay_method, amount, remark, address, consignee, phone, create_time) " +
            "values (#{userId}, #{tradeNo}, #{payMethod}, #{amount}, #{remark}, #{address}, #{consignee}, #{phone}, #{createTime});")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertOrder(Order order);

}
