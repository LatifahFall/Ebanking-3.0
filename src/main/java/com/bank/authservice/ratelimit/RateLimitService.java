package com.bank.authservice.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> refreshBuckets = new ConcurrentHashMap<>();

    /* ================= LOGIN ================= */
    // 5 tentatives / minute / IP + username
    public boolean allowLogin(String ip, String username) {
        String key = ip + ":" + username;

        Bucket bucket = loginBuckets.computeIfAbsent(
                key,
                k -> newBucket(5, Duration.ofMinutes(1))
        );

        return bucket.tryConsume(1);
    }

    /* ================= REFRESH ================= */
    // 10 refresh / minute / IP
    public boolean allowRefresh(String ip) {
        Bucket bucket = refreshBuckets.computeIfAbsent(
                ip,
                k -> newBucket(10, Duration.ofMinutes(1))
        );

        return bucket.tryConsume(1);
    }

    /* ================= FACTORY ================= */
    private Bucket newBucket(long capacity, Duration duration) {
        Refill refill = Refill.intervally(capacity, duration);
        Bandwidth limit = Bandwidth.classic(capacity, refill);

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
