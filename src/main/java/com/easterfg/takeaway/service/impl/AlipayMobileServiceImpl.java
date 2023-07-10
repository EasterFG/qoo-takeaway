package com.easterfg.takeaway.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.easterfg.takeaway.config.PayConfig;
import com.easterfg.takeaway.dao.OrderDAO;
import com.easterfg.takeaway.domain.Order;
import com.easterfg.takeaway.dto.PayQueryDTO;
import com.easterfg.takeaway.enums.OrderEvent;
import com.easterfg.takeaway.enums.OrderStatus;
import com.easterfg.takeaway.exception.BusinessException;
import com.easterfg.takeaway.machine.MachineUtils;
import com.easterfg.takeaway.service.PayService;
import com.easterfg.takeaway.utils.constant.GlobalConstant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author EasterFG on 2022/11/10
 */
@Service
@Slf4j
public class AlipayMobileServiceImpl implements PayService {

    @Resource
    private AlipayClient client;

    @Resource
    private PayConfig payConfig;

    @Resource
    private OrderDAO orderDAO;

    @Resource
    private MachineUtils machineUtils;

    @Async
    @Override
    public void asyncNotify(Map<String, String> params) {
        String tradeStatus = params.get("trade_status");
        // 交易成功
        if (GlobalConstant.PAY_SUCCESS.equals(tradeStatus)) {
            // 支付成功
            // 支付宝流水号
            String outTradeNo = params.get("trade_no");
            // 订单编号
            String tradeNo = params.get("out_trade_no");
            // 支付时间
            String payment = params.get("gmt_payment");
            // 更新状态
//            Optional.of(GlobalConstant.ORDER_TIMEOUT.remove(Long.parseLong(tradeNo))).orElseThrow().cancel();
            Order order = new Order();
            order.setTradeNo(Long.valueOf(tradeNo));
            order.setOutTradeNo(outTradeNo);
            // 设置支付时间
            order.setPaymentTime(LocalDateTime.parse(payment,
                    GlobalConstant.DEFAULT_FORMATTER));
            orderDAO.updateOrder(order);
            // 通知状态机更新状态
            machineUtils.sendMessage(OrderEvent.PAYMENT, order);
        } else if ("TRADE_CLOSED".equals(tradeStatus)) {
            // 订单退款
            var tradeNo = params.get("out_trade_no");
            var cancelTime = params.get("gmt_refund");
            var order = new Order();
            order.setTradeNo(Long.valueOf(tradeNo));
            order.setPaymentTime(LocalDateTime.parse(cancelTime,
                    GlobalConstant.DEFAULT_FORMATTER));
            machineUtils.sendMessage(OrderEvent.REFUNDED, order);
        }
    }

    @SneakyThrows
    @Override
    public String payOrder(Long tradeNo, BigDecimal price, String timeout) {
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(String.valueOf(tradeNo));
        model.setTotalAmount(price.toString());
        model.setSubject("外卖订单-" + tradeNo);
        model.setProductCode("QUICK_WAP_WAY");
        model.setQuitUrl("http://localhost:9874/order/" + tradeNo);
//        model.setSellerId("2088621993751241");
        // 设置绝对超时时间
        model.setTimeExpire(timeout);
        // 设置返回路径
        request.setReturnUrl("http://localhost:9874/order/" + tradeNo);
        // 设置异步通知
        request.setNotifyUrl(payConfig.getNotify());
        // 设置body
        request.setBizModel(model);
        // 设置回调通知
//        request.setNotifyUrl(payConfig.getNotify());
        AlipayTradeWapPayResponse response = client.pageExecute(request);
        String result = null;
        if (response.isSuccess()) {
            result = response.getBody();
        }
        return result;
    }

    @Override
    public PayQueryDTO queryOrder(long tradeNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(String.valueOf(tradeNo));
        request.setBizModel(model);
        AlipayTradeQueryResponse response = null;
        try {
            response = client.execute(request);
            log.info("query dto {}", response);
        } catch (AlipayApiException e) {
            log.error("支付宝api出现异常", e);
            return null;
        }
        PayQueryDTO dto = new PayQueryDTO();
        dto.setSubCode(response.getSubCode());
        if (response.isSuccess()) {
            dto.conversion(response.getTradeStatus(), response.getOutTradeNo(),
                    response.getTradeNo(), response.getSendPayDate());
        }
        return dto;
//        return new PayQueryDTO(response.getSubCode(), response.getTradeStatus(), response.getOutTradeNo(), response.getTradeNo(), response.getSendPayDate());
//        if (response.isSuccess()) {
//            return new PayQueryDTO(response.getTradeStatus(), response.getOutTradeNo(), response.getTradeNo(), response.getSendPayDate());
//        }
//        return null;
    }

    @Override
    @SneakyThrows
    public Future<Boolean> refund(Long tradeNo, String outTradeNo, double amount) {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setRefundAmount(String.valueOf(amount));
        model.setOutTradeNo(String.valueOf(tradeNo));
        model.setTradeNo(outTradeNo);
        request.setBizModel(model);
        AlipayTradeRefundResponse response = client.execute(request);
        log.info("退款接口返回 => code: {} sub_code: {}", response.getCode(), response.getSubCode());
        if (response.isSuccess()) {
            if ("Y".equals(response.getFundChange())) {
                // 退款成功
                Order order = new Order();
                order.setTradeNo(Long.valueOf(response.getOutTradeNo()));
                order.setStatus(OrderStatus.REFUNDING);
                // 状态机 更新
                machineUtils.sendMessage(OrderEvent.REFUNDED, order);
            }
//            if (!response.getCode().equals("10000")) {
//                throw new BusinessException("40004", response.getSubMsg());
//            }
            // 退款成功, 修改订单支付状态
//            orderDAO.updatePayStatus(Long.valueOf(response.getOutTradeNo()), 2);
//            return new AsyncResult<>(response.getCode().equals("10000"));
        }
        throw new BusinessException("40004", "退款失败，请联系商家处理");
//        return false;
    }

    @SneakyThrows
    @Override
    public boolean cancel(Long tradeNo) {
        AlipayClient alipayClient = new DefaultAlipayClient(payConfig.alipayConfig());
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        AlipayTradeCloseModel model = new AlipayTradeCloseModel();
        model.setOutTradeNo(String.valueOf(tradeNo));
        request.setBizModel(model);
        AlipayTradeCloseResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            log.info("code = {}, sub_code = {}", response.getCode(), response.getSubCode());
            return "10000".equals(response.getCode());
        }
        return false;
    }
}
