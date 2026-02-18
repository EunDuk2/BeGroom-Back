package com.example.BeGroom.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "settlementExecutor")
    public Executor settlementExecutor(){

        log.warn("settlementExecutor Bean 생성됨!"); // 이게 여러 번 찍히면 문제

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(12);
        executor.setMaxPoolSize(12);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("Settlement-Thread-");
        // 스레드 풀 다 차면 메인 스레드 실행
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        log.info("Active: {}, Pool Size: {}, Queue Size: {}",
                executor.getActiveCount(),
                executor.getPoolSize(),
                executor.getThreadPoolExecutor().getQueue().size());

        return executor;
    }
}
