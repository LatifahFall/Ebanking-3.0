package cryptoservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import cryptoservice.model.SupportedCryptoCoins;

@RestController
@RequestMapping("/api/coins")
public class CryptoCoinsController {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Fetches full details on all supported 20 coins from CoinGecko
     * @return Full market data including prices, market caps, volumes, etc.
     */
    @GetMapping("/details")
    public ResponseEntity<?> getCoinsDetails() {
        String ids = SupportedCryptoCoins.coingeckoIdsCsv();
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=eur&ids=" + ids;
        
        try {
            var response = restTemplate.getForObject(url, Object.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching coin details: " + e.getMessage());
        }
    }

    /**
     * Fetches just the current prices for all supported coins (minimal response)
     * @return Price data only
     */
    @GetMapping("/prices")
    public ResponseEntity<?> getCoinsPrice() {
        String ids = SupportedCryptoCoins.coingeckoIdsCsv();
        String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + ids + "&vs_currencies=eur";
        
        try {
            var response = restTemplate.getForObject(url, Object.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching prices: " + e.getMessage());
        }
    }
}
