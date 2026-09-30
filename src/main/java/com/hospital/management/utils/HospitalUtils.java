package com.hospital.management.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class HospitalUtils {

    private final AtomicInteger usageCounter = new AtomicInteger(0);

    public void recordUsage(String context) {
        int current = usageCounter.incrementAndGet();
        log.info("HospitalUtils used. Counter: {} | Context: {}", current, context);
    }
}
