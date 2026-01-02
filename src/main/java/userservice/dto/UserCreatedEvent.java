package userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    private Long userId;
    private String userName;
    private Long creatorId;
    private String creatorRole;
    private String creatorName;
    private LocalDateTime createdAt;
    
    // Constructor sans creatorId et creatorName pour AdminController
    public UserCreatedEvent(Long userId, String userName, String creatorRole, LocalDateTime createdAt) {
        this.userId = userId;
        this.userName = userName;
        this.creatorRole = creatorRole;
        this.createdAt = createdAt;
    }
}