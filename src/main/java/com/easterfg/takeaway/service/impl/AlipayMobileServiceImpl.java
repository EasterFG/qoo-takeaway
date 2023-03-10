package com.easterfg.takeaway.service.impl;

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
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.easterfg.takeaway.config.PayConfig;
import com.easterfg.takeaway.dao.OrderDAO;
import com.easterfg.takeaway.domain.Order;
import com.easterfg.takeaway.dto.PayQueryDTO;
import com.easterfg.takeaway.exception.BusinessException;
import com.easterfg.takeaway.service.PayService;
import com.easterfg.takeaway.utils.constant.GlobalConstant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
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

    @Async
    @Override
    public void asyncNotify(Map<String, String> params) {
        String tradeStatus = params.get("trade_status");
        if (GlobalConstant.PAY_SUCCESS.equals(tradeStatus)) {
            // ????????????
            // ??????????????????
            String outTradeNo = params.get("trade_no");
            // ????????????
            String tradeNo = params.get("out_trade_no");
            // ????????????
            String payment = params.get("gmt_payment");
            // ????????????
//            Optional.of(GlobalConstant.ORDER_TIMEOUT.remove(Long.parseLong(tradeNo))).orElseThrow().cancel();
            LambdaUpdateWrapper<Order> wrapper = Wrappers.lambdaUpdate(Order.class);
            wrapper.eq(Order::getTradeNo, tradeNo);
            Order order = new Order();
            order.setOutTradeNo(outTradeNo);
            order.setPayStatus(1);
            order.setStatus(2);
            // ??????????????????
            order.setPaymentTime(LocalDateTime.parse(payment,
                    GlobalConstant.DEFAULT_FORMATTER));
            orderDAO.update(order, wrapper);
        }
    }

    @SneakyThrows
    @Override
    public String payOrder(Long tradeId, BigDecimal price, String timeout) {
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(String.valueOf(tradeId));
        model.setTotalAmount(price.toString());
        model.setSubject("????????????-" + tradeId);
        model.setProductCode("QUICK_WAP_WAY");
        model.setQuitUrl("http://localhost:9874/order/" + tradeId);
        model.setSellerId("2088621993751241");
        // ????????????????????????
        model.setTimeExpire(timeout);
        // ??????????????????
        request.setReturnUrl("http://localhost:9874/order/" + tradeId);
        // ??????????????????
        request.setNotifyUrl(payConfig.getNotify());
        // ??????body
        request.setBizModel(model);
        // ??????????????????
//        request.setNotifyUrl(payConfig.getNotify());
        AlipayTradeWapPayResponse response = client.pageExecute(request);
        String result = null;
        if (response.isSuccess()) {
            result = response.getBody();
        }
        return result;
    }

    @SneakyThrows
    @Override
    public PayQueryDTO queryOrder(long tradeNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(String.valueOf(tradeNo));
        request.setBizModel(model);
        AlipayTradeQueryResponse response = client.execute(request);
        if (response.isSuccess()) {
            return new PayQueryDTO(response.getTradeStatus(), response.getOutTradeNo(), response.getTradeNo(), response.getSendPayDate());
        }
        return null;
    }

    @Override
    @SneakyThrows
    public Future<Boolean> refund(String outTradeNo, double amount) {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setRefundAmount(String.valueOf(amount));
        model.setTradeNo(outTradeNo);
        request.setBizModel(model);
        AlipayTradeRefundResponse response = client.execute(request);
        log.info("?????????????????? => code: {} sub_code: {}", request.getProdCode(), response.getSubCode());
        if (response.isSuccess()) {
//            if (!response.getCode().equals("10000")) {
//                throw new BusinessException("40004", response.getSubMsg());
//            }
            // ????????????, ????????????????????????
            orderDAO.updatePayStatus(Long.valueOf(response.getOutTradeNo()), 2);
            return new AsyncResult<>(response.getCode().equals("10000"));
        }
        throw new BusinessException("40004", "????????????????????????????????????");
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
