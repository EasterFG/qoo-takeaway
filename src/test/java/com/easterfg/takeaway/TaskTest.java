package com.easterfg.takeaway;

import com.easterfg.takeaway.service.OrderService;
import io.netty.util.HashedWheelTimer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author EasterFG on 2022/11/11
 */
@SpringBootTest
public class TaskTest {

    @Autowired
    private HashedWheelTimer hashedWheelTimer;

    @Autowired
    private ThreadPoolTaskScheduler scheduler;

    @Autowired
    private OrderService orderService;

    @Test
    void wheel() {
        hashedWheelTimer.newTimeout(
                (timeout) -> orderService.cancelOrder(6989772915919163392L, null, "订单超时自动取消")
                , 30, TimeUnit.SECONDS);
    }


    @Test
    void tabs() {
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            int max = 5;
//
//            @Override
//            public void run() {
//                System.out.println(LocalDateTime.now() + " - " + (max--));
//                if (max == 0) {
//                    timer.cancel();
//                }
//            }
//        }, 1000L, 1000L);
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.setPoolSize(4);
        Map<String, ScheduledFuture<?>> map = new HashMap<>();
        ScheduledFuture<?> schedule = scheduler.schedule(new Runnable() {
            int max = 5;

            @Override
            public void run() {
                System.out.println(LocalDateTime.now() + " - " + (max--));
                if (max == 0) {
                    Optional.of(map.remove("1")).ifPresent(s -> s.cancel(true));
                }
            }
        }, new CronTrigger("0/1 * * * * *"));
        map.put("1", schedule);
    }


//        scheduler.schedule(() -> System.out.println(LocalDateTime.now()), new CronTrigger("0/10 * * * * ? "));
}
