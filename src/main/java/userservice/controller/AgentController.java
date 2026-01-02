package userservice.controller;

import userservice.dto.UserResponse;
import userservice.dto.CreateUserRequest;
import userservice.dto.AssignClientRequest;
import userservice.model.User;
import userservice.model.AgentClientAssignment;
import userservice.service.UserService;
import userservice.service.AgentClientAssignmentService;
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
    private final AgentClientAssignmentService assignmentService;
    private final userservice.service.UserEventProducer userEventProducer;

    @PostMapping("/{agentId}")
    @Operation(summary = "Create assigned client profile", description = "Create a client assigned to this agent")
    public ResponseEntity<UserResponse> createClient(
        @PathVariable Long agentId,
        @Valid @RequestBody CreateUserRequest request) {
        
        // agent exists
        User agent = userService.getUserById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found"));
        
        // Verify agent has AGENT role
        if (agent.getRole() != User.UserRole.AGENT) {
            throw new IllegalArgumentException("User is not an agent");
        }

        User newUser = new User();
        newUser.setFname(request.getFname());
        newUser.setLname(request.getLname());
        newUser.setPhone(request.getPhone());
        newUser.setAddress(request.getAddress());
        newUser.setCin(request.getCin());
        newUser.setLogin(request.getLogin());
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(request.getPassword());
        
        User result = userService.createUser(newUser);
        AssignClientRequest assignRequest = new AssignClientRequest();
        assignRequest.setAgentId(agentId);
        assignRequest.setClientId(result.getId());
        assignRequest.setNotes("Auto-assigned during user creation");
        assignmentService.assignClient(assignRequest);
        // kafka events
        userEventProducer.publishUserCreatedEventWithCreator(result, agent);
        userEventProducer.publishClientAssignedEvent(result.getId(), agentId);
        return ResponseEntity.ok(mapToResponse(result));
    }

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
        boolean isAssigned = assignmentService.getAgentForClient(clientId)
            .map(assignedAgent -> assignedAgent.getId().equals(agentId))
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
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            updatedUser.setPasswordHash(request.getPassword());
        }         
        User result = userService.updateUser(clientId, updatedUser);
        // kafka event
        userEventProducer.publishUserUpdatedEventWithUpdater(result, agent);
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
        boolean isAssigned = assignmentService.getAgentForClient(clientId)
            .map(assignedAgent -> assignedAgent.getId().equals(agentId))
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
        List<User> clients = assignmentService.getClientsForAgent(agentId);
        List<Long> clientIds = clients.stream()
            .map(User::getId)
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

    @PatchMapping("/{agentId}/activate")
    @Operation(summary = "Activate an assigned client's profile", description = "Activate profile of a client assigned to this agent")
    public ResponseEntity<UserResponse> activateClientProfile(
        @PathVariable Long agentId,
        @RequestParam Long clientId) {
        
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
        boolean isAssigned = assignmentService.getAgentForClient(clientId)
            .map(assignedAgent -> assignedAgent.getId().equals(agentId))
            .orElse(false);
        
        if (!isAssigned) {
            throw new IllegalArgumentException("Client is not assigned to this agent");
        }        
        User result = userService.activateUserProfile(clientId);
        // kafka event
        userEventProducer.publishUserActivatedEvent(clientId, "AGENT");
        return ResponseEntity.ok(mapToResponse(result));
    }

    @PatchMapping("/{agentId}/deactivate")
    @Operation(summary = "Deactivate an assigned client's profile", description = "Deactivate profile of a client assigned to this agent")
    public ResponseEntity<UserResponse> deactivateClientProfile(
        @PathVariable Long agentId,
        @RequestParam Long clientId) {
        
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
        boolean isAssigned = assignmentService.getAgentForClient(clientId)
            .map(assignedAgent -> assignedAgent.getId().equals(agentId))
            .orElse(false);
        
        if (!isAssigned) {
            throw new IllegalArgumentException("Client is not assigned to this agent");
        }        
        User result = userService.deactivateUserProfile(clientId);
        // kafka event
        userEventProducer.publishUserDeactivatedEvent(clientId, "AGENT");
        return ResponseEntity.ok(mapToResponse(result));
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
        response.setIsActive(user.getIsActive());
        response.setKycStatus(user.getKycStatus().toString());
        response.setGdprConsent(user.getGdprConsent());
        response.setLastLogin(user.getLastLogin());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
