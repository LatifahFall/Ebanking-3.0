package userservice.controller;

import userservice.dto.CreateUserRequest;
import userservice.dto.UserResponse;
import userservice.dto.AssignClientRequest;
import userservice.dto.AgentClientAssignmentResponse;
import userservice.model.User;
import userservice.service.UserService;
import userservice.service.AgentClientAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;



@RestController
@RequestMapping("/admin/users")
@Tag(name = "Admins", description = "Admin management endpoints")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserService userService;
    private final AgentClientAssignmentService assignmentService;
    private final userservice.service.UserEventProducer userEventProducer;
    
    @PostMapping
    @Operation(summary = "Create a new user", description = "Register a new user account")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = new User();
        user.setLogin(request.getLogin());
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());
        user.setFname(request.getFname());
        user.setLname(request.getLname());
        user.setPhone(request.getPhone());
        user.setCin(request.getCin());
        user.setAddress(request.getAddress());
        user.setRole(User.UserRole.valueOf(request.getRole()));
        User createdUser = userService.createUser(user);
        //kafka event
        userEventProducer.publishUserCreatedEvent(createdUser); 
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(createdUser));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by user ID")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return userService.getUserById(id)
            .map(user -> ResponseEntity.ok(mapToResponse(user)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update user details")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,@Valid @RequestBody CreateUserRequest request) {
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
        updatedUser.setRole(User.UserRole.valueOf(request.getRole()));
        User user = userService.updateUser(id, updatedUser);
        // kafka event
        userEventProducer.publishUserUpdatedEvent(user);
        return ResponseEntity.ok(mapToResponse(user));
    }


    @PostMapping("/assignments")
    @Operation(summary = "Assign client to agent", description = "Create a new agent-client assignment")
    public ResponseEntity<AgentClientAssignmentResponse> assignClient(@Valid @RequestBody AssignClientRequest request) {
        AgentClientAssignmentResponse response = assignmentService.assignClient(request);
        // kafka event
        userEventProducer.publishClientAssignedEvent(request.getClientId(), request.getAgentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/assignments")
    @Operation(summary = "Disassign client from agent", description = "Remove an agent-client assignment")
    public ResponseEntity<Void> disassignClient(@RequestParam Long clientId, @RequestParam Long agentId) {
        assignmentService.disassignClient(clientId, agentId);
        // kafka event
        userEventProducer.publishClientUnassignedEvent(clientId, agentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by role and free-text query")
    public ResponseEntity<Page<UserResponse>> searchUsers(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) User.UserRole role,
        Pageable pageable
    ) {
        Page<UserResponse> result = userService.searchUsers(q, role, pageable)
            .map(this::mapToResponse);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/agents/{agentId}/clients")
    @Operation(summary = "Get all clients assigned to an agent", description = "Retrieve all clients assigned to a specific agent")
    public ResponseEntity<List<UserResponse>> getClientsForAgent(@PathVariable Long agentId) {
        List<User> clients = assignmentService.getClientsForAgent(agentId);
        List<UserResponse> response = clients.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clients/{clientId}/agent")
    @Operation(summary = "Get agent assigned to a client", description = "Retrieve the agent assigned to a specific client")
    public ResponseEntity<UserResponse> getAgentForClient(@PathVariable Long clientId) {
        return assignmentService.getAgentForClient(clientId)
            .map(agent -> ResponseEntity.ok(mapToResponse(agent)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/activate")
    @Operation(summary = "Activate a user's profile", description = "Activate profile of a user by admin")
    public ResponseEntity<UserResponse> activateUserProfile(
        @RequestParam Long userId) {
        
        // user exists
        User user = userService.getUserById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        User result = userService.activateUserProfile(userId);
        // kafka event
        userEventProducer.publishUserActivatedEvent(userId, "ADMIN");
        return ResponseEntity.ok(mapToResponse(result));
    }        


    @PatchMapping("/deactivate")
    @Operation(summary = "Deactivate a user's profile", description = "Deactivate profile of a user by admin")
    public ResponseEntity<UserResponse> deactivateUserProfile(@RequestParam Long userId) {
        User user = userService.getUserById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        User result = userService.deactivateUserProfile(userId);
        // kafka event
        userEventProducer.publishUserDeactivatedEvent(userId, "ADMIN");
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