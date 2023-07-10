package com.easterfg.takeaway.controller;

import com.easterfg.takeaway.domain.Order;
import com.easterfg.takeaway.dto.OrderDTO;
import com.easterfg.takeaway.dto.PageData;
import com.easterfg.takeaway.dto.RefundDTO;
import com.easterfg.takeaway.dto.Result;
import com.easterfg.takeaway.enums.OrderStatus;
import com.easterfg.takeaway.query.PageQuery;
import com.easterfg.takeaway.service.OrderService;
import com.easterfg.takeaway.utils.security.Authorize;
import com.easterfg.takeaway.utils.security.Role;
import com.easterfg.takeaway.utils.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.net.URI;
import java.time.LocalDate;

/**
 * @author EasterFG on 2022/10/19
 */
@Authorize(Role.EMPLOYEE)
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

//    @Resource
//    public SnowflakeIdWorker snowflakeIdWorker;

    @Resource
    public OrderService orderService;

//    @Resource
//    public StringRedisTemplate template;

//    @Resource
//    private OrderDAO orderMapper;

//
//     请求生成唯一订单编号
//
//    @Authorize(Role.USER)
//    @GetMapping("/trade")
//    public Result getTradeNo() {
//        UserContext.User user = UserContext.getUser();
//        Long uid = user.getId();
//        log.info("uid  = {}", uid);
//        UserContext.destroy();
//        long tid = snowflakeIdWorker.nextId();
//        // 数据存储到redis中, 过期时间15min
//        template.opsForValue().set(GlobalConstant.TRADE_KEY + tid,
//                String.valueOf(uid), 1, TimeUnit.MINUTES);
//        return Result.success(tid);
//    }

    /**
     * 创建订单
     */
    @Authorize(Role.USER)
    @PostMapping("/create")
    public ResponseEntity<Result> createOrder(@Validated @RequestBody OrderDTO orderDTO) {
        Long tradeNo = orderService.create(orderDTO);
        // 返回 201, 通知客户端成功
        return ResponseEntity
                .created(URI.create("order/" + tradeNo))
                .body(Result.success(tradeNo));
    }


    @Authorize
    @GetMapping("/list")
    public Result listOrder(@Validated PageQuery pageQuery, Integer status, String tradeNo, String phone,
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        // 根据前端传递的值,获取枚举
        OrderStatus os = null;
        if (status != null) {
            os = OrderStatus.valueOf(status);
        }
        UserContext.User user = UserContext.getUser();
        PageData<Order> pageData = orderService.listOrder(user.hasRole(Role.USER) ? user.getId() : null, pageQuery, os,
                tradeNo, phone, start, end);
        return Result.success(pageData);
    }

    @Authorize
    @GetMapping("{id}")
    public Result getOrder(@PathVariable Long id) {
        return Result.success(orderService.getOrder(id));
    }

    @GetMapping("/count")
    public Result orderCount() {
        // 查询信息
        return Result.success(orderService.orderCount());
//        return Result.success(orderService.orderCount());
    }

    @Authorize
    @PostMapping("/cancel/{tradeNo}")
    public Result cancelOrder(@PathVariable("tradeNo") Long tradeNo, String cancelReason) {
        UserContext.User user = UserContext.getUser();
        orderService.cancel(tradeNo, user.hasRole(Role.USER) ? Role.USER : Role.EMPLOYEE,
                cancelReason == null ? "用户主动取消" : cancelReason);
//        orderService.cancelOrder(tradeNo,
//                user.hasRole(Role.USER) ? user.getId() : null,
//                cancelReason == null ? "用户主动取消" : cancelReason);
////        UserContext.destroy();
        return Result.success();
    }

    /**
     * 商家接单
     */
    @PostMapping("/approve/{tradeNo}")
    public Result approveOrder(@PathVariable("tradeNo") Long tradeNo) {
        orderService.accept(tradeNo);
        return Result.success();
//        if (orderService.updateOrderStatus(tradeNo, OrderStatus.WAIT_ACCEPT,
//                OrderStatus.WAIT_DELIVERY)) {
//            return Result.success();
//        }
//        return Result.failed("接单失败");
    }

    /**
     * 商家开始配送
     */
    @Authorize(Role.EMPLOYEE)
    @PostMapping("/delivery/{tradeNo}")
    public Result deliveryOrder(@PathVariable("tradeNo") Long tradeNo) {
//        if (orderService.updateOrderStatus(tradeNo, OrderStatus.WAIT_DELIVERY,
//                OrderStatus.DELIVERING)) {
//            return Result.success();
//        }
//        return Result.failed("failed");
        orderService.startDelivery(tradeNo);
        return Result.success();
    }


    /**
     * 订单配送完成
     */
    @Authorize(Role.EMPLOYEE)
    @PostMapping("/complete/{tradeNo}")
    public Result completeOrder(@PathVariable("tradeNo") Long tradeNo) {
//        if (orderService.updateOrderStatus(tradeNo, OrderStatus.DELIVERING,
//                OrderStatus.FINISHED)) {
//            return Result.success();
//        }
//        return Result.failed("failed");
        orderService.finished(tradeNo);
        return Result.success();
    }

    @Authorize(Role.ADMIN)
    @PostMapping("/refund")
    public Result refund(@RequestBody RefundDTO refundDTO) {
        log.info("退款请求 {}", refundDTO);
        orderService.refund(refundDTO);
//        orderService.refundOrder(refundDTO.getTradeNo(), refundDTO.getAmount());
        return Result.success("订单退已发起");
    }


}
