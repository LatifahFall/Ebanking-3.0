package cryptoservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class CoinsCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String COINS_CACHE_KEY = "crypto:coins:details";
    private static final long CACHE_TTL_MINUTES = 5;

    @Autowired
    public CoinsCacheService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void cacheCoinsDetails(List<Map<String, Object>> coinsDetails) {
        try {
            String jsonData = objectMapper.writeValueAsString(coinsDetails);
            redisTemplate.opsForValue().set(
                COINS_CACHE_KEY,
                jsonData,
                CACHE_TTL_MINUTES,
                TimeUnit.MINUTES
            );
        } catch (Exception e) {
            throw new RuntimeException("Error caching coins details: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getCachedCoinsDetails() {
        try {
            String cachedData = redisTemplate.opsForValue().get(COINS_CACHE_KEY);
            if (cachedData != null) {
                return objectMapper.readValue(cachedData, new TypeReference<List<Map<String, Object>>>() {});
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving cached coins: " + e.getMessage());
        }
    }

    public Map<String, Object> getCoinDetailsById(String coinId) {
        List<Map<String, Object>> allCoins = getCachedCoinsDetails();
        if (allCoins != null) {
            return allCoins.stream()
                .filter(coin -> coin.get("id").equals(coinId))
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    public boolean isCached() {
        return Boolean.TRUE.equals(redisTemplate.hasKey(COINS_CACHE_KEY));
    }

    public void clearCache() {
        redisTemplate.delete(COINS_CACHE_KEY);
    }
}