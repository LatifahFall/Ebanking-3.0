package userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientUnassignedEvent {
    private Long clientId;
    private Long agentId;
    private String unassignedByRole;
    private LocalDateTime unassignedAt;
}
