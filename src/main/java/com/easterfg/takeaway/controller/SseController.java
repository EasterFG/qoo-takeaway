package com.easterfg.takeaway.controller;

import com.easterfg.takeaway.utils.constant.SseConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    private static final Map<Long, SseEmitter> SSE_CACHE = new ConcurrentHashMap<>();

    /**
     * 订阅服务器通知
     *
     * @param tradeNo 订单编号
     */
    @GetMapping("subscribe")
    public SseEmitter subscribeOrder(Long tradeNo) {
        // 设置超时时间为 3600
        SseEmitter sseEmitter = new SseEmitter(3600000L);
        SSE_CACHE.put(tradeNo, sseEmitter);
        sseEmitter.onTimeout(() -> SSE_CACHE.remove(tradeNo));
        sseEmitter.onCompletion(() -> log.info("sse completion"));
        return sseEmitter;
    }

    @GetMapping("/wait")
    public SseEmitter waitOrder() throws IOException {
        SseEmitter sseEmitter = new SseEmitter(3600000L);
        int id = SseConstant.WAIT_ID.getAndIncrement();
        SseConstant.WAIT_EMITTER.put(id, sseEmitter);
        sseEmitter.onTimeout(() -> SseConstant.WAIT_EMITTER.remove(id));
        sseEmitter.send("{id:" + id + "}", MediaType.APPLICATION_JSON);
        return sseEmitter;
    }

    @GetMapping("push")
    public String push(@RequestParam Integer id, String content) throws IOException {
        SseEmitter sseEmitter = SseConstant.WAIT_EMITTER.get(id);
        if (sseEmitter != null) {
            sseEmitter.send(content, MediaType.TEXT_PLAIN);
        }
        return "over";
    }

}
