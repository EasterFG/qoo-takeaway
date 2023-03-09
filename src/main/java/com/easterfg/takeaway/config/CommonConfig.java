package com.easterfg.takeaway.config;

import com.easterfg.takeaway.utils.SnowflakeIdWorker;
import io.netty.util.HashedWheelTimer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author EasterFG on 2022/11/11
 * <p>
 * 通用配置文件
 */
@Configuration
public class CommonConfig {

    @Bean
    public SnowflakeIdWorker snowflakeIdWorker() {
        return new SnowflakeIdWorker(1, 1);
    }

    /**
     * 时间轮 netty
     */
    @Bean
    public HashedWheelTimer hashedWheelTimer() {
        return new HashedWheelTimer(100, TimeUnit.MILLISECONDS, 512);
    }
}
