package userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {
    private Long userId;
    private String userName;
    private Long updaterId;           // Optionnel (null si admin)
    private String updaterRole;       // ADMIN ou AGENT
    private String updaterName;       // Optionnel (null si admin)
    private LocalDateTime updatedAt;
    
    // Constructeur sans updaterId/updaterName pour AdminController
    public UserUpdatedEvent(Long userId, String userName, String updaterRole, LocalDateTime updatedAt) {
        this.userId = userId;
        this.userName = userName;
        this.updaterRole = updaterRole;
        this.updatedAt = updatedAt;
    }
}