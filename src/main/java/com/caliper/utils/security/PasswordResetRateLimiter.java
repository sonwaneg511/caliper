package com.caliper.utils.security;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetRateLimiter {

    private static final int MAX_REQUESTS = 3;
    private static final long WINDOW_MS = 15 * 60 * 1000L;

    private final ConcurrentHashMap<String, Deque<Long>> ipAttemptMap = new ConcurrentHashMap<>();

    public boolean isAllowed(String ipAddress) {
        long now = System.currentTimeMillis();
        ipAttemptMap.putIfAbsent(ipAddress, new ArrayDeque<>());
        Deque<Long> timestamps = ipAttemptMap.get(ipAddress);
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= MAX_REQUESTS) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    @Scheduled(fixedDelay = 600000)
    public void cleanup() {
        long now = System.currentTimeMillis();
        ipAttemptMap.entrySet().removeIf(entry -> {
            Deque<Long> q = entry.getValue();
            synchronized (q) {
                return q.isEmpty() || now - q.peekLast() > WINDOW_MS;
            }
        });
    }
}
