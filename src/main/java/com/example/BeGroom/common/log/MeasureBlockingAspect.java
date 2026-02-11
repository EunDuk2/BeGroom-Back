package com.example.BeGroom.common.log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

@Aspect
@Component
public class MeasureBlockingAspect {
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    @Around("@annotation(com.example.BeGroom.common.log.MeasureBlocking)")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startCpuTime = threadMXBean.getCurrentThreadCpuTime();
        long startTime = System.nanoTime();

        try {
            return joinPoint.proceed();
        } finally {
            long endCpuTime = threadMXBean.getCurrentThreadCpuTime();
            long endTime = System.nanoTime();

            long wallClockTime = (endTime - startTime) / 1_000_000; // ms
            long cpuTime = (endCpuTime - startCpuTime) / 1_000_000; // ms
            long blockingTime = wallClockTime - cpuTime;            // 대기 시간 (I/O, Lock 등)

            String methodName = joinPoint.getSignature().getName();

            System.out.printf("⏱️ [%s] 전체수행: %dms | CPU작업: %dms | 대기(Blocking): %dms%n",
                    methodName, wallClockTime, cpuTime, blockingTime);
        }
    }

}
