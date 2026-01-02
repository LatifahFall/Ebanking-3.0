package userservice.controller;

import userservice.dto.UserResponse;
import userservice.dto.UpdateProfileRequest;
import userservice.dto.UserPreferencesRequest;
import userservice.model.User;
import userservice.model.UserPreferences;
import userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/me")
@Tag(name = "Me", description = "Authenticated user profile endpoints")
@RequiredArgsConstructor
public class MeController {

    private final UserService userService;
    private final userservice.service.UserEventProducer userEventProducer;

    @GetMapping("/{id}")
    @Operation(summary = "Get my profile", description = "Retrieve the current user's profile by id")
    public ResponseEntity<UserResponse> getMyProfile(@PathVariable Long id) {
        return userService.getUserById(id)
            .map(user -> ResponseEntity.ok(mapToResponse(user)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update my profile", description = "Update login, email, password, or phone")
    public ResponseEntity<UserResponse> updateMyProfile(@PathVariable Long id, @Valid @RequestBody UpdateProfileRequest request) {
        User updated = userService.updateOwnProfile(id, request);
        //kafka event
        userEventProducer.publishUserUpdatedEventWithUpdater(updated, updated);
        return ResponseEntity.ok(mapToResponse(updated));
    }
    
    @PatchMapping("/{id}/last-login")
    @Operation(summary = "Update my last login timestamp")
    public ResponseEntity<UserResponse> updateLastLogin(@PathVariable Long id) {
        User user = userService.updateLastLogin(id);
        return ResponseEntity.ok(mapToResponse(user));
    }

    @GetMapping("/{id}/preferences")
    @Operation(summary = "Get my preferences", description = "Retrieve language, theme, and notification settings")
    public ResponseEntity<UserPreferences> getPreferences(@PathVariable Long id) {
        UserPreferences prefs = userService.getUserPreferences(id);
        return ResponseEntity.ok(prefs);
    }

    @PutMapping("/{id}/preferences")
    @Operation(summary = "Update my preferences", description = "Update language, theme, and notification settings")
    public ResponseEntity<UserPreferences> updatePreferences(@PathVariable Long id, @RequestBody UserPreferencesRequest request) {
        UserPreferences prefs = userService.updateUserPreferences(id, request);
        return ResponseEntity.ok(prefs);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Verify login credentials and return user profile")
    public ResponseEntity<UserResponse> authenticateUser(
        @RequestParam String login, 
        @RequestParam String password) {
        
        User user = userService.authenticateUser(login, password);
        return ResponseEntity.ok(mapToResponse(user));
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
