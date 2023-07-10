package com.easterfg.takeaway.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easterfg.takeaway.domain.Order;
import com.easterfg.takeaway.domain.OrderDetail;
import com.easterfg.takeaway.domain.OrderStatusCount;
import com.easterfg.takeaway.domain.StatusStatistics;
import com.easterfg.takeaway.enums.OrderStatus;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author EasterFG on 2022/10/24
 */
public interface OrderDAO extends BaseMapper<Order> {

    @Select("SELECT count(*) FROM takeout_order WHERE user_id = #{uid} order by create_time")
    int count(Long uid);

    @Select("""
            <script>
            SELECT count(*) FROM takeout_order
                <where>
                    <if test='status != null'>
                        status = #{status}
                    </if>
                    <if test='tradeNo != null'>
                        and trade_no = #{tradeNo}
                    </if>
                    <if test='phone != null'>
                        and phone = #{phone}
                    </if>
                    <if test='start != null and end != null'>
                        and date(create_time) between #{start} and #{end}
                    </if>
                </where>
            </script>
            """)
    int countByWhere(@Param("status") OrderStatus status, @Param("tradeNo") String tradeNo, @Param("phone") String phone,
                     @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Select({"<script>",
            " select trade_no, status, pay_method, pay_status, amount, remark, cancel_reason, address, consignee, phone, create_time, payment_time, close_time " +
                    "from takeout_order " +
                    "<where>" +
                    "    <if test='status != null'> status = #{status} </if>" +
                    "    <if test='tradeNo != null'> and trade_no = #{tradeNo} </if>" +
                    "    <if test='phone != null'>   and phone = #{phone} </if>" +
                    "    <if test='start != null and end != null'> and date(create_time) between #{start} and #{end} </if>" +
                    "</where> " +
                    "limit #{offset}, #{size}", "</script>"})
    List<Order> listOrder(@Param("status") OrderStatus status, @Param("tradeNo") String tradeNo, @Param("phone") String phone,
                          @Param("start") LocalDate start, @Param("end") LocalDate end, @Param("offset") int offset, @Param("size") int size);

    @Select(" SELECT trade_no, status, create_time, amount FROM takeout_order WHERE user_id = #{uid} ORDER BY create_time DESC limit #{offset}, #{size}")
    List<Order> listUserOrder(@Param("uid") Long uid, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT name, number, image, dish_flavor, amount FROM order_detail WHERE trade_no= #{traderNo}")
    List<OrderDetail> listDetail(Long traderNo);

    @Select(" SELECT trade_no, user_id, out_trade_no, status, amount, remark, cancel_reason, address, consignee, phone, create_time, payment_time, close_time " + "FROM takeout_order WHERE trade_no = #{tid};")
    @Result(column = "trade_no", property = "orderDetails", many = @Many(select = "listDetail"))
    Order getOrder(Long tid);

    //    @Select("select status, pay_status, amount, user_id from takeout_order where trade_no = #{tradeNo};")
    @Select({"<script>", "select  user_id, out_trade_no, status, pay_status, amount, create_time from takeout_order where trade_no = #{tradeNo}" + "<if test='uid != null'>" + "    and user_id = #{uid};" + "</if>", "</script>"})
    Order getOrderStatus(@Param("tradeNo") long tradeNo, @Param("uid") Long uid);

    @Update("update takeout_order set status = #{update} where status = #{expect} and trade_no = #{tradeNo}")
    void updateOrderStatus(@Param("tradeNo") Long tradeNo, @Param("expect") OrderStatus expect, @Param("update") OrderStatus update);

    /**
     * 更新订单信息
     *
     * @param order    订单
     */
    @Update({"<script>", "update takeout_order" +
            "  <set>" +
            "    <if test='order.outTradeNo != null'>out_trade_no = #{order.outTradeNo},</if>" +
            "    <if test='order.status != null'>status = #{order.status},</if>" +
            "    <if test='order.cancelReason != null'>cancel_reason = #{order.cancelReason},</if>" +
            "    <if test='order.paymentTime != null'>payment_time = #{order.paymentTime},</if>" +
            "    <if test='order.closeTime != null'>close_time = #{order.createTime}</if>" +
            "  </set>" + "where trade_no = #{order.tradeNo}" +
            "</script>"})
    void updateOrder(@Param("order") Order order);

    @Select("select status, count(*) `count` from takeout_order where date(close_time) = #{time} group by status;")
    List<StatusStatistics> statisticsByDate(LocalDate time);

    @Select("SELECT ifnull(sum(amount), 0) from takeout_order where status = 6 and date(close_time) = #{time}")
    BigDecimal statisticsAmount(LocalDate time);

    @Insert("insert into takeout_order (trade_no, user_id, pay_method, amount, remark, address, consignee, phone, create_time) " + "values ( #{tradeNo}, #{userId} ,#{payMethod}, #{amount}, #{remark}, #{address}, #{consignee}, #{phone}, #{createTime});")
    void createOrder(Order order);

}
