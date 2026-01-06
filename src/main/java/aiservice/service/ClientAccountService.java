package aiservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ClientAccountService {

    @Value("${account.service.base-url}/api/accounts")
    private String accountServiceBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Builds a compact summary string of the user's accounts (first few) with balances and recent txs.
     */
    public String buildAccountContext(long userId) {
        try {
            List<Map> accounts = fetchAccountsByUserId(userId);
            if (accounts == null || accounts.isEmpty()) {
                return "accounts: none";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("accounts.count=").append(accounts.size());

            int maxAccounts = Math.min(3, accounts.size());
            for (int i = 0; i < maxAccounts; i++) {
                Map acc = accounts.get(i);
                Object accId = acc.get("id");
                sb.append(" | account[").append(i).append("] id=").append(accId);
                sb.append(" type=").append(safe(acc, "type"));
                sb.append(" status=").append(safe(acc, "status"));

                Map balance = accId != null ? fetchBalance(toLong(accId)) : null;
                if (balance != null) {
                    sb.append(" balance=").append(safe(balance, "amount"));
                    sb.append(" currency=").append(safe(balance, "currency"));
                }

                List<Map> txs = accId != null ? fetchTransactions(toLong(accId), 3) : List.of();
                if (txs != null && !txs.isEmpty()) {
                    sb.append(" txs=");
                    int maxTx = Math.min(3, txs.size());
                    for (int t = 0; t < maxTx; t++) {
                        Map tx = txs.get(t);
                        sb.append("{")
                          .append("type=").append(safe(tx, "type"))
                          .append(", amount=").append(safe(tx, "amount"))
                          .append(", currency=").append(safe(tx, "currency"))
                          .append(", date=").append(safe(tx, "date"))
                          .append("}");
                        if (t < maxTx - 1) sb.append(";");
                    }
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error building account context: " + e.getMessage();
        }
    }

    public Map fetchAccountById(long id) {
        String url = accountServiceBaseUrl + "/" + id;
        return restTemplate.getForObject(url, Map.class);
    }

    public List<Map> fetchAccountsByUserId(long userId) {
        String url = accountServiceBaseUrl + "?userId=" + userId;
        List<Map> body = restTemplate.getForObject(url, List.class);
        return body != null ? body : new ArrayList<>();
    }

    public Map fetchBalance(long accountId) {
        String url = accountServiceBaseUrl + "/" + accountId + "/balance";
        return restTemplate.getForObject(url, Map.class);
    }

    public List<Map> fetchTransactions(long accountId, int limit) {
        String url = accountServiceBaseUrl + "/" + accountId + "/transactions?limit=" + limit;
        List<Map> body = restTemplate.getForObject(url, List.class);
        return body != null ? body : new ArrayList<>();
    }

    public Map fetchStatement(long accountId, LocalDateTime start, LocalDateTime end) {
        String url = accountServiceBaseUrl + "/" + accountId + "/statement?startDate=" + start + "&endDate=" + end;
        return restTemplate.getForObject(url, Map.class);
    }

    public Map suspendAccount(long id, Map payload) {
        String url = accountServiceBaseUrl + "/" + id + "/suspend";
        return postWithBody(url, payload);
    }

    public Map closeAccount(long id, Map payload) {
        String url = accountServiceBaseUrl + "/" + id + "/close";
        return postWithBody(url, payload);
    }

    private Map postWithBody(String url, Map body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, Map.class).getBody();
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
