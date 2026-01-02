package userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDeactivatedEvent {
    private Long userId;
    private String deactivatedByRole;
    private LocalDateTime deactivatedAt;
}
