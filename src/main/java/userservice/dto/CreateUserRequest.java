package userservice.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class CreateUserRequest {
    
    @NotBlank(message = "Login is required")
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    private String login;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @NotBlank(message = "First name is required")
    private String fname;
    
    @NotBlank(message = "Last name is required")
    private String lname;
    
    private String phone;
    
    private String cin;
    
    private String address;
    
    private String role = "CLIENT";
}