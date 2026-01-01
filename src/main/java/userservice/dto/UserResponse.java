package userservice.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

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
    private String kycStatus;
    private Boolean gdprConsent;
}