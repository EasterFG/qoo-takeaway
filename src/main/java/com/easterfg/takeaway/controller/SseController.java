package com.easterfg.takeaway.controller;

import com.easterfg.takeaway.enums.OrderStatus;
import com.easterfg.takeaway.machine.OrderStateListener;
import com.easterfg.takeaway.utils.constant.SseConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author EasterFG on 2022/11/10
 * <p>
 * Server Sent Event 服务端推送
 */
@RestController
@RequestMapping("/sse")
@Slf4j
public class SseController {

    private final OrderStateListener orderStateListener;

    @Autowired
    public SseController(OrderStateListener orderStateListener) {
        this.orderStateListener = orderStateListener;
    }

    /**
     * 订阅服务器通知
     *
     * @param tradeNo 订单编号
     */
    @GetMapping(value = "/order/{tradeNo}/status", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<OrderStatus>> subscribeOrder(@PathVariable Long tradeNo) {
        log.info("trade status {}", tradeNo);
        Sinks.Many<OrderStatus> many = orderStateListener.addEmitter(tradeNo);

        return many.asFlux().map(data -> ServerSentEvent.builder(data).build())
                .doFinally(signalType -> orderStateListener.removeEmitter(tradeNo));
    }

    @GetMapping("{tradeNo}/send")
    public void sendMessage(@PathVariable Long tradeNo, Integer status) {
        Sinks.Many<OrderStatus> skins = orderStateListener.get(tradeNo);
        if (skins != null) {
            skins.tryEmitNext(OrderStatus.valueOf(status));
        }
    }
}
