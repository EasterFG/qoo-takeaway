package com.easterfg.takeaway.utils.constant;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author EasterFG on 2022/11/23
 */
public class SseConstant {

    public static final AtomicInteger WAIT_ID = new AtomicInteger(1);

    /**
     * 商户端接收下单通知
     */
    public static final Map<Integer, SseEmitter> WAIT_EMITTER = new ConcurrentHashMap<>();

    private SseConstant() {
    }

}
