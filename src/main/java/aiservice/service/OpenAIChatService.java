package aiservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIChatService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getChatCompletion(String message) {
        try {
            String url = "https://api.openai.com/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("max_tokens", 100);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", message);

                requestBody.put("messages", List.of(userMessage));

            HttpEntity<Map> entity = new HttpEntity<>(requestBody, headers);

            Map response = restTemplate.postForObject(url, entity, Map.class);
            if (response == null || response.get("choices") == null) {
                return "Error: empty response from OpenAI";
            }

            List<Map> choices = (List<Map>) response.get("choices");
            if (choices.isEmpty()) {
                return "Error: no choices returned";
            }

            Map choice = choices.get(0);
            Map messageObj = (Map) choice.get("message");
            if (messageObj == null) {
                return "Error: missing message content";
            }
            Object content = messageObj.get("content");
            return content != null ? content.toString() : "Error: missing content";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
