package com.easterfg.takeaway.controller;

import com.alibaba.druid.support.json.JSONUtils;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.easterfg.takeaway.config.PayConfig;
import com.easterfg.takeaway.dto.Result;
import com.easterfg.takeaway.service.OrderService;
import com.easterfg.takeaway.service.PayService;
import com.easterfg.takeaway.utils.security.Authorize;
import com.easterfg.takeaway.utils.security.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author EasterFG on 2022/10/14
 * <p>
 * gotyrd5689@sandbox.com
 */
@Controller
@RequestMapping("/pay")
@Slf4j
// @Api(tags = "订单支付接口")
public class PayController {

    @Resource(name = "alipayMobileServiceImpl")
    private PayService payService;

    @Resource
    private OrderService orderService;

    @Resource
    private PayConfig payConfig;

    // 测试接口用户名 vcdmbx9889@sandbox.com

    /**
     * 支付宝回调接口
     *
     * @return success: 表示成功
     */
    @PostMapping("/notify")
    @ResponseBody
    public String asyncNotify(@RequestParam Map<String, String> params) throws AlipayApiException {
        log.info("async {}", JSONUtils.toJSONString(params));
        if (AlipaySignature.verifyV1(params, payConfig.getAlipayPublicKey(),
                "UTF-8", "RSA2")) {
            // 交易参数类型, 异步执行具体方法
            payService.asyncNotify(params);
            return "success";
        } else {
            return "failure";
        }
    }

    /**
     * 支付订单, 通过订单ID支付
     */
    @Authorize(Role.USER)
    // @ApiOperation("订单支付接口")
    @PostMapping("/order/{id}")
    @ResponseBody
    public Result payOrder(@PathVariable Long id) {
        String api = orderService.pay(id);
        return Result.success("success", api);
        // 查询接口
//        LambdaQueryWrapper<Order> wrapper = Wrappers.lambdaQuery(Order.class);
//        wrapper
//                .select(Order::getAmount, Order::getStatus, Order::getPayStatus, Order::getCreateTime)
//                .eq(Order::getTradeNo, id);
//        Order order = orderService.getOne(wrapper);
//        // 查询订单状态
//        if (order.getStatus() == OrderStatus.WAIT_PAYMENT) {
//            // 调用三方服务查询
//            PayQueryDTO payQuery = payService.queryOrder(id);
//            if (payQuery != null && payQuery.getTradeStatus().equals(PayQueryDTO.Status.TRADE_SUCCESS)) {
//                // 订单支付完成， 通知数据库更新
//                LambdaUpdateWrapper<Order> update = Wrappers.lambdaUpdate(Order.class);
//                // 修改订单状态 and 支付状态
//                update.set(Order::getOutTradeNo, payQuery.getOutTradeNo())
//                        .set(Order::getPayStatus, 1)
//                        .set(Order::getStatus, 2)
//                        .eq(Order::getTradeNo, id);
//                orderService.update(update);
//                return Result.failed("A2001", "订单已经支付");
//            }
//        } else if (order.getPayStatus() == PayStatus.PAID) {
//            return Result.failed("A2001", "订单已经支付");
//        } else if (order.getStatus() != OrderStatus.WAIT_PAYMENT) {
//            return Result.failed("A2002", "订单已经完成或已取消");
//        }
//        LocalDateTime timeout = order.getCreateTime().plusMinutes(15);
//        if (timeout.isBefore(LocalDateTime.now())) {
//            return Result.failed("A2003", "订单已经超时");
//        }
//        return Result.success("success", payService.payOrder(id, order.getAmount(),
//                GlobalConstant.DEFAULT_FORMATTER.format(timeout)));
    }

//    // @ApiOperation("外部查询接口")
//    @GetMapping("/query/{tradeNo}")
//    @ResponseBody
//    public Result payQueryDTO(@PathVariable long tradeNo) {
//        return Result.success(payService.queryOrder(tradeNo));
//    }
//
//    // @ApiOperation("外部退款接口")
//    @GetMapping("/refund/{tradeNo}/{amount}")
//    @ResponseBody
//    public Result refundDTO(@PathVariable long tradeNo, @PathVariable double amount) {
//        payService.refund(tradeNo, amount);
//        return Result.success();
//    }
}
