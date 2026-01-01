package userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignClientRequest {
    @NotNull
    private Long agentId;

    @NotNull
    private Long clientId;

    private Long assignedBy; // optional admin id

    private String notes;
}
