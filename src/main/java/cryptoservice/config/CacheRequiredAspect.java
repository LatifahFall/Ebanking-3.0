package cryptoservice.config;

import cryptoservice.model.SupportedCryptoCoins;
import cryptoservice.service.CoinsCacheService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Aspect
@Component
public class CacheRequiredAspect {

    private final CoinsCacheService coinsCacheService;
    private final RestTemplate restTemplate;

    public CacheRequiredAspect(CoinsCacheService coinsCacheService, RestTemplate restTemplate) {
        this.coinsCacheService = coinsCacheService;
        this.restTemplate = restTemplate;
    }

    /**
     * Automatically check cache before any transaction operation
     * If cache is empty, populate it from CoinGecko
     */
    @Before("execution(* cryptoservice.service.CryptoTransactionService.*(..)) && " +
            "!execution(* cryptoservice.service.CryptoTransactionService.getTransactionsByWalletId(..))")
    public void checkAndPopulateCacheBeforeTransaction() {
        if (!coinsCacheService.isCached()) {
            populateCache();
        }
    }

    /**
     * Fetch coin details from CoinGecko and cache them in Redis
     */
    private void populateCache() {
        try {
            String ids = SupportedCryptoCoins.coingeckoIdsCsv();
            String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=eur&ids=" + ids;
            
            // fetch from CoinGecko
            List<Map<String, Object>> coinsData = restTemplate.getForObject(url, List.class);
            
            if (coinsData != null && !coinsData.isEmpty()) {
                // Cache the result in Redis
                coinsCacheService.cacheCoinsDetails(coinsData);
                System.out.println("Cache populated automatically with " + coinsData.size() + " coins");
            } else {
                throw new IllegalStateException("Failed to fetch coins data from CoinGecko");
            }
            
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to populate crypto prices cache from CoinGecko: " + e.getMessage(), e
            );
        }
    }
}