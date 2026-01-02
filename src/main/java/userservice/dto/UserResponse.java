package userservice.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String login;
    private String email;
    private String fname;
    private String lname;
    private String phone;
    private String cin;
    private String address;
    private String role;
    private Boolean isActive;
    private String kycStatus;
    private Boolean gdprConsent;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}