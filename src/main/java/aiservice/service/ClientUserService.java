package aiservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ClientUserService {

	@Value("${user.service.base-url}")
	private String userServiceBaseUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	/**
	 * Fetches user profile and preferences, and returns a compact string summary
	 * suitable to inject into a prompt for personalization.
	 */
	public String buildUserContext(long userId) {
		try {
			Map profile = fetchProfile(userId);
			Map preferences = fetchPreferences(userId);

			StringBuilder sb = new StringBuilder();
			sb.append("user.profile: ")
			  .append(safe(profile, "fname")).append(' ')
			  .append(safe(profile, "lname"))
			  .append(" | role=").append(safe(profile, "role"))
			  .append(" | email=").append(safe(profile, "email"))
			  .append(" | phone=").append(safe(profile, "phone"))
			  .append(" | address=").append(safe(profile, "address"))
			  .append(" | kyc=").append(safe(profile, "kycStatus"))
			  .append(" | active=").append(safe(profile, "isActive"))
			  .append(" | lastLogin=").append(safe(profile, "lastLogin"));

			sb.append(" || user.preferences: ")
			  .append("language=").append(safe(preferences, "language"))
			  .append(" | theme=").append(safe(preferences, "theme"))
			  .append(" | notifEmail=").append(safe(preferences, "notificationEmail"))
			  .append(" | notifSms=").append(safe(preferences, "notificationSms"))
			  .append(" | notifPush=").append(safe(preferences, "notificationPush"))
			  .append(" | notifInApp=").append(safe(preferences, "notificationInApp"));

			return sb.toString();
		} catch (Exception e) {
			return "Error building user context: " + e.getMessage();
		}
	}

	public Map fetchProfile(long userId) {
		String url = userServiceBaseUrl + "/me/" + userId;
		return restTemplate.getForObject(url, Map.class);
	}

	public Map fetchPreferences(long userId) {
		String url = userServiceBaseUrl + "/me/" + userId + "/preferences";
		return restTemplate.getForObject(url, Map.class);
	}

	public Map updateLastLogin(long userId) {
		String url = userServiceBaseUrl + "/me/" + userId + "/last-login";
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
}
