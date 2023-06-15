package com.easterfg.takeaway.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easterfg.takeaway.domain.Order;
import com.easterfg.takeaway.domain.OrderDetail;
import com.easterfg.takeaway.domain.OrderStatusCount;
import com.easterfg.takeaway.domain.StatusStatistics;
import com.easterfg.takeaway.utils.enums.OrderStatus;
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

    @Select({"<script>", " SELECT count(*) FROM takeout_order" +
            "<where>" +
            "    <if test='status != null'>" +
            "            status = #{status}" +
            "    </if>" +
            "</where>",
            "</script>"})
    int countByStatus(OrderStatus status);

    //    @Results(id = "statusMap", value = {
//            @Result(column = "status", property = "status", typeHandler = OrderStatusTypeHandler.class),
//            @Result(column = "pay_status", property = "payStatus", typeHandler = PayStatusTypeHandler.class),
//            @Result(column = "trade_no", property = "orderDetails",
//                    many = @Many(select = "listDetail", fetchType = FetchType.LAZY))
//    })
    @Select(" SELECT trade_no, status, create_time, amount FROM takeout_order o WHERE user_id = #{uid} ORDER BY create_time DESC")
    List<Order> listUserOrder(Long uid);

    @Select("SELECT name, number, image, dish_flavor, amount FROM order_detail WHERE trade_no= #{traderNo}")
    List<OrderDetail> listDetail(Long traderNo);

    @Select(" SELECT trade_no, user_id, out_trade_no, amount, remark, cancel_reason, address, consignee, phone, create_time, payment_time, close_time " +
            "FROM takeout_order WHERE trade_no = #{tid};")
    @Result(column = "trade_no", property = "orderDetails", many = @Many(select = "listDetail"))
//    @ResultMap("statusMap")
    Order getOrder(Long tid);

    //    @Select("select status, pay_status, amount, user_id from takeout_order where trade_no = #{tradeNo};")
    @Select({"<script>", "select status, pay_status, amount, user_id from takeout_order where trade_no = #{tradeNo}" +
            "<if test='uid != null'>" +
            "    and user_id = #{uid};" +
            "</if>", "</script>"})
    Order getOrderStatus(@Param("tradeNo") long tradeNo, @Param("uid") Long uid);

    /**
     * 更新订单状态
     *
     * @param order    订单信息
     * @param expected 预期状态
     * @return 结果
     */

    int updateStatus(@Param("data") Order order, @Param("expected") int expected);

    /**
     * 更新订单信息
     *
     * @param order    订单
     * @param expected 预期状态值(如果为空,表示没有预期值)
     */
    @Update({"<script>",
            "update takeout_order" +
                    "  <set>" +
                    "    <if test='order.outTradeNo != null'>out_trade_no = #{order.outTradeNo},</if>" +
                    "    <if test='order.status != null'>status = #{order.status},</if>" +
                    "    <if test='order.payStatus != null'>pay_status = #{order.payStatus},</if>" +
                    "    <if test='order.cancelReason != null'>cancel_reason = #{order.cancelReason},</if>" +
                    "    <if test='order.paymentTime != null'>payment_time = #{order.paymentTime},</if>" +
                    "    <if test='order.closeTime != null'>close_time = #{order.createTime}</if>" +
                    "  </set>" +
                    "where trade_no = #{order.tradeNo}" +
                    "  <if test='expected != null'>and status = #{expected}</if>",
            "</script>"})
    int updateOrder2(@Param("order") Order order, @Param("expected") OrderStatus expected);


    /**
     * 更新订单信息
     *
     * @return 结果
     */
    @Update("update takeout_order set " +
            "status = #{status}," +
            "pay_status = #{payStatus} " +
            "where trade_no = #{tradeNo}")
    int updateOrder(Order order);

    @Select("select sum(IF(status = 1, 1, 0)) as confirm, " +
            "sum(IF(status = 2, 1, 0)) as prepare, " +
            "sum(IF(status = 3, 1, 0)) as proceed " +
            "from takeout_order " +
            "limit 1000;")
    OrderStatusCount statistics();


    //    @Select("SELECT count(*) as total, sum(if(status = 5, 1, 0)) as complete, sum(if(status = 6,1,0)) as cancel" +
//            " from takeout_order where close_time between #{time} and adddate(#{time}, 1)")
    @Select("select status, count(*) from takeout_order" +
            " where close_time between #{time} and ADDDATE(#{time}, 1) group by status;")
    List<StatusStatistics> statisticsByDate(LocalDateTime time);

    @Select("SELECT ifnull(sum(amount), 0) from takeout_order where status = 5 and close_time between #{time} and adddate(#{time}, 1)")
    BigDecimal statisticsAmount(LocalDateTime time);

    @Update("update takeout_order set pay_status = #{payStatus} where trade_no = #{tradeNo}")
    void updatePayStatus(@Param("tradeNo") Long tradeNo, @Param("payStatus") int payStatus);

    @Select({"<script>",
            " select trade_no, status, pay_method, pay_status, amount, remark, cancel_reason, address, consignee, phone, create_time, payment_time, close_time " +
                    "from takeout_order " +
                    "<where>" +
                    "    <if test='status != null'>" +
                    "       status = #{status}" +
                    "    </if>" +
                    "</where> " +
                    "limit #{offset}, #{size}",
            "</script>"})
//    @ResultMap("statusMap")
    List<Order> listOrder(
            @Param("status") OrderStatus status,
            @Param("offset") int offset,
            @Param("size") int size);

    @Insert("insert into takeout_order (trade_no, user_id, pay_method, amount, remark, address, consignee, phone, create_time) " +
            "values ( #{tradeNo}, #{userId} ,#{payMethod}, #{amount}, #{remark}, #{address}, #{consignee}, #{phone}, #{createTime});")
    void insertOrder(Order order);

}
