package userservice.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AgentClientAssignmentResponse {
    private Long id;
    private Long agentId;
    private Long clientId;
    private Long assignedById;
    private LocalDateTime assignedAt;
    private String notes;
}
