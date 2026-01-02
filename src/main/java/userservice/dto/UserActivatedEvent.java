package userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivatedEvent {
    private Long userId;
    private String activatedByRole;
    private LocalDateTime activatedAt;
}
