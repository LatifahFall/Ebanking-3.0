package userservice.dto;

import lombok.Data;

@Data
public class UserPreferencesRequest {
    private String language;
    private Boolean notificationEmail;
    private Boolean notificationSms;
    private Boolean notificationPush;
    private Boolean notificationInApp;
    private String theme;
}
