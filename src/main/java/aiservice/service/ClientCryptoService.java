package aiservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ClientCryptoService {

    @Value("${crypto.service.base-url}/api")
    private String cryptoServiceBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Builds a compact summary string of the user's crypto wallet, holdings, and recent transactions.
     */
    public String buildCryptoContext(long userId) {
        try {
            Map wallet = fetchWalletByUserId(userId);
            if (wallet == null) {
                return "crypto: no wallet";
            }

            Object walletIdObj = wallet.get("id");
            if (walletIdObj == null) {
                return "crypto: invalid wallet";
            }
            long walletId = toLong(walletIdObj);

            StringBuilder sb = new StringBuilder();
            sb.append("crypto.wallet: id=").append(walletId);
            sb.append(" status=").append(safe(wallet, "status"));
            sb.append(" balance=").append(safe(wallet, "balance")).append(" EUR");

            List<Map> holdings = fetchHoldings(walletId);
            if (holdings != null && !holdings.isEmpty()) {
                sb.append(" || holdings.count=").append(holdings.size());
                int maxHold = Math.min(5, holdings.size());
                for (int i = 0; i < maxHold; i++) {
                    Map h = holdings.get(i);
                    sb.append(" | holding[").append(i).append("] ")
                      .append(safe(h, "cryptoSymbol")).append("=")
                      .append(safe(h, "amount"));
                }
            } else {
                sb.append(" || holdings: none");
            }

            List<Map> txs = fetchTransactions(walletId);
            if (txs != null && !txs.isEmpty()) {
                sb.append(" || transactions.recent=");
                int maxTx = Math.min(3, txs.size());
                for (int t = 0; t < maxTx; t++) {
                    Map tx = txs.get(t);
                    sb.append("{")
                      .append("type=").append(safe(tx, "type"))
                      .append(", symbol=").append(safe(tx, "cryptoSymbol"))
                      .append(", amount=").append(safe(tx, "cryptoAmount"))
                      .append(", eur=").append(safe(tx, "eurAmount"))
                      .append(", status=").append(safe(tx, "status"))
                      .append("}");
                    if (t < maxTx - 1) sb.append(";");
                }
            }

            return sb.toString();
        } catch (Exception e) {
            return "Error building crypto context: " + e.getMessage();
        }
    }

    // =========================================================================
    // WALLET OPERATIONS
    // =========================================================================

    public Map createWallet(long userId) {
        String url = cryptoServiceBaseUrl + "/wallets?userId=" + userId;
        return restTemplate.postForObject(url, null, Map.class);
    }

    public Map fetchWalletByUserId(long userId) {
        String url = cryptoServiceBaseUrl + "/wallets/user/" + userId;
        return restTemplate.getForObject(url, Map.class);
    }

    public Map activateWallet(long userId) {
        String url = cryptoServiceBaseUrl + "/wallets/activate?userId=" + userId;
        return patchNoBody(url);
    }

    public Map deactivateWallet(long userId) {
        String url = cryptoServiceBaseUrl + "/wallets/deactivate?userId=" + userId;
        return patchNoBody(url);
    }

    // =========================================================================
    // HOLDINGS & TRANSACTIONS
    // =========================================================================

    public List<Map> fetchHoldings(long walletId) {
        String url = cryptoServiceBaseUrl + "/holdings/wallet/" + walletId;
        List<Map> body = restTemplate.getForObject(url, List.class);
        return body != null ? body : new ArrayList<>();
    }

    public List<Map> fetchTransactions(long walletId) {
        String url = cryptoServiceBaseUrl + "/transactions/wallet/" + walletId;
        List<Map> body = restTemplate.getForObject(url, List.class);
        return body != null ? body : new ArrayList<>();
    }

    // =========================================================================
    // COIN MARKET DATA
    // =========================================================================

    public List<Map> fetchCoinsDetails() {
        String url = cryptoServiceBaseUrl + "/coins/details";
        List<Map> body = restTemplate.getForObject(url, List.class);
        return body != null ? body : new ArrayList<>();
    }

    public Map fetchCoinDetails(String coinId) {
        String url = cryptoServiceBaseUrl + "/coins/" + coinId;
        return restTemplate.getForObject(url, Map.class);
    }

    // =========================================================================
    // BUY / SELL TRANSACTIONS
    // =========================================================================

    public Map buyCrypto(long walletId, String symbol, double eurAmount) {
        String url = cryptoServiceBaseUrl + "/transactions/buy?walletId=" + walletId;
        Map<String, Object> body = Map.of(
            "symbol", symbol,
            "eurAmount", eurAmount
        );
        return postWithBody(url, body);
    }

    public Map sellCrypto(long walletId, String symbol, double cryptoAmount) {
        String url = cryptoServiceBaseUrl + "/transactions/sell?walletId=" + walletId;
        Map<String, Object> body = Map.of(
            "symbol", symbol,
            "cryptoAmount", cryptoAmount
        );
        return postWithBody(url, body);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private Map postWithBody(String url, Map body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, Map.class).getBody();
    }

    private Map patchNoBody(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.PATCH, entity, Map.class).getBody();
    }

    private String safe(Map map, String key) {
        if (map == null) {
            return "";
        }
        Object v = map.get(key);
        return v != null ? v.toString() : "";
    }

    private long toLong(Object v) {
        if (v instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(String.valueOf(v));
    }
}
