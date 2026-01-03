// package cryptoservice.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.client.RestTemplate;
// import cryptoservice.model.SupportedCryptoCoins;

// @RestController
// @RequestMapping("/api/coins")
// public class CryptoCoinsController {

//     @Autowired
//     private RestTemplate restTemplate;

//     /**
//      * Fetches full details on all supported 20 coins from CoinGecko
//      * @return Full market data including prices, market caps, volumes, etc.
//      */
//     @GetMapping("/details")
//     public ResponseEntity<?> getCoinsDetails() {
//         String ids = SupportedCryptoCoins.coingeckoIdsCsv();
//         String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=eur&ids=" + ids;
        
//         try {
//             var response = restTemplate.getForObject(url, Object.class);
//             return ResponseEntity.ok(response);
//         } catch (Exception e) {
//             return ResponseEntity.status(500).body("Error fetching coin details: " + e.getMessage());
//         }
//     }

//     /**
//      * Fetches just the current prices for all supported coins (minimal response)
//      * @return Price data only
//      */
//     @GetMapping("/prices")
//     public ResponseEntity<?> getCoinsPrice() {
//         String ids = SupportedCryptoCoins.coingeckoIdsCsv();
//         String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + ids + "&vs_currencies=eur";
        
//         try {
//             var response = restTemplate.getForObject(url, Object.class);
//             return ResponseEntity.ok(response);
//         } catch (Exception e) {
//             return ResponseEntity.status(500).body("Error fetching prices: " + e.getMessage());
//         }
//     }
// }

package cryptoservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import cryptoservice.model.SupportedCryptoCoins;
import cryptoservice.service.CoinsCacheService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coins")
public class CryptoCoinsController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CoinsCacheService coinsCacheService;

    /**
     * Fetches full details on all supported 20 coins from CoinGecko
     * Caches the result in Redis for 5 minutes to avoid rate limit
     * @return Full market data including prices, market caps, volumes, etc.
     */
    @GetMapping("/details")
    public ResponseEntity<?> getCoinsDetails() {
        String ids = SupportedCryptoCoins.coingeckoIdsCsv();
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=eur&ids=" + ids;
        
        try {
            // Fetch from CoinGecko
            List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);
            
            // Cache the result in Redis
            coinsCacheService.cacheCoinsDetails(response);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching coin details: " + e.getMessage());
        }
    }

    /**
     * Fetches details for a specific coin from Redis cache
     * Falls back to full refresh if cache is empty
     * @param coinId the coin ID (e.g., "bitcoin")
     * @return Coin details or 404 if not found
     */
    @GetMapping("/{coinId}")
    public ResponseEntity<?> getCoinById(@PathVariable String coinId) {
        try {
            // Try to get from cache first
            Map<String, Object> coin = coinsCacheService.getCoinDetailsById(coinId);
            
            if (coin != null) {
                return ResponseEntity.ok(coin);
            }
            
            // If cache is empty, refresh from CoinGecko
            if (!coinsCacheService.isCached()) {
                getCoinsDetails(); // Populate cache
                coin = coinsCacheService.getCoinDetailsById(coinId);
                
                if (coin != null) {
                    return ResponseEntity.ok(coin);
                }
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching coin: " + e.getMessage());
        }
    }
}
