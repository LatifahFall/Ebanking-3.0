package userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;
    
    @Column(length = 10)
    private String language = "en";
    
    @Column(name = "notification_email")
    private Boolean notificationEmail = true;
    
    @Column(name = "notification_sms")
    private Boolean notificationSms = true;
    
    @Column(name = "notification_push")
    private Boolean notificationPush = true;

    @Column(name = "notification_in_app")
    private Boolean notificationInApp = true;
    
    @Column(length = 20)
    private String theme = "light";
}