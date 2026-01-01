package userservice.controller;

import userservice.dto.UserResponse;
import userservice.dto.CreateUserRequest;
import userservice.model.User;
import userservice.model.AgentClientAssignment;
import userservice.service.UserService;
import userservice.repository.AgentClientAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/agent/clients")
@Tag(name = "Agents", description = "Agent management endpoints")
@RequiredArgsConstructor
public class AgentController {

    private final UserService userService;
    private final AgentClientAssignmentRepository assignmentRepository;

    @PutMapping("/{agentId}")
    @Operation(summary = "Update assigned client profile", description = "Update profile of a client assigned to this agent")
    public ResponseEntity<UserResponse> updateClientProfile(
        @PathVariable Long agentId,
        @RequestParam Long clientId,
        @Valid @RequestBody CreateUserRequest request) {
        
        // client exists
        User client = userService.getUserById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        
        // agent exists
        User agent = userService.getUserById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found"));
        
        // Verify agent has AGENT role
        if (agent.getRole() != User.UserRole.AGENT) {
            throw new IllegalArgumentException("User is not an agent");
        }
        
        // Verify assignment exists (client is assigned to this agent)
        boolean isAssigned = assignmentRepository.findByClient(client)
            .map(assignment -> assignment.getAgent().getId().equals(agentId))
            .orElse(false);
        
        if (!isAssigned) {
            throw new IllegalArgumentException("Client is not assigned to this agent");
        }
        User updatedUser = new User();
        updatedUser.setFname(request.getFname());
        updatedUser.setLname(request.getLname());
        updatedUser.setPhone(request.getPhone());
        updatedUser.setAddress(request.getAddress());
        updatedUser.setCin(request.getCin());
        updatedUser.setLogin(request.getLogin());
        updatedUser.setEmail(request.getEmail());
        updatedUser.setPasswordHash(request.getPassword());
        
        User result = userService.updateUser(clientId, updatedUser);
        return ResponseEntity.ok(mapToResponse(result));
    }

    @GetMapping("/{agentId}")
    @Operation(summary = "Fetch assigned client profile", description = "Fetch profile of a client assigned to this agent")
    public ResponseEntity<UserResponse> fetchClientProfile(
        @PathVariable Long agentId,
        @RequestParam Long clientId) {
        
        // Verify client exists
        User client = userService.getUserById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        
        // Verify agent exists
        User agent = userService.getUserById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found"));
        
        // Verify agent has AGENT role
        if (agent.getRole() != User.UserRole.AGENT) {
            throw new IllegalArgumentException("User is not an agent");
        }
        
        // Verify assignment exists (client is assigned to this agent)
        boolean isAssigned = assignmentRepository.findByClient(client)
            .map(assignment -> assignment.getAgent().getId().equals(agentId))
            .orElse(false);
        
        if (!isAssigned) {
            throw new IllegalArgumentException("Client is not assigned to this agent");
        }
        
        return ResponseEntity.ok(mapToResponse(client));
    }

    @GetMapping("/{agentId}/search")
    @Operation(summary = "Search assigned clients", description = "Search clients assigned to this agent")
    public ResponseEntity<Page<UserResponse>> searchAssignedClients(
        @PathVariable Long agentId,
        @RequestParam(required = false) String q,
        Pageable pageable
    ) {
        //  agent exists and has AGENT role
        User agent = userService.getUserById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found"));
        
        if (agent.getRole() != User.UserRole.AGENT) {
            throw new IllegalArgumentException("User is not an agent");
        }
        
        // get his assigned clients
        List<AgentClientAssignment> assignments = assignmentRepository.findByAgent(agent);
        List<Long> clientIds = assignments.stream()
            .map(assignment -> assignment.getClient().getId())
            .collect(Collectors.toList());
        
        // search among all clients (we'll filter after)
        Page<User> allClients = userService.searchAgentClients(agentId, q, pageable);
        
        // filter to only include assigned clients
        List<User> filteredClients = allClients.getContent().stream()
            .filter(user -> clientIds.contains(user.getId()))
            .collect(Collectors.toList());
        
        Page<UserResponse> result = new PageImpl<>(
            filteredClients.stream().map(this::mapToResponse).collect(Collectors.toList()),
            pageable,
            filteredClients.size()
        );
        
        return ResponseEntity.ok(result);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setLogin(user.getLogin());
        response.setEmail(user.getEmail());
        response.setFname(user.getFname());
        response.setLname(user.getLname());
        response.setPhone(user.getPhone());
        response.setCin(user.getCin());
        response.setAddress(user.getAddress());
        response.setRole(user.getRole().toString());
        response.setKycStatus(user.getKycStatus().toString());
        response.setGdprConsent(user.getGdprConsent());
        return response;
    }
}
