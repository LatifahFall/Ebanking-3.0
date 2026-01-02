package userservice.service;

import userservice.model.User;
import userservice.model.UserPreferences;
import userservice.repository.UserRepository;
import userservice.repository.UserPreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import userservice.security.PasswordUtil;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ArrayList;
import jakarta.persistence.criteria.Predicate;
import userservice.dto.UpdateProfileRequest;
import userservice.dto.UserPreferencesRequest;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByLogin(user.getLogin())) {
            throw new IllegalArgumentException("Login already exists");
        }
        if (user.getCin() != null && userRepository.existsByCin(user.getCin())) {
            throw new IllegalArgumentException("CIN already exists");
        }
        user.setPasswordHash(PasswordUtil.hash(user.getPasswordHash()));
        user.setUpdatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        UserPreferences prefs = new UserPreferences();
        prefs.setUser(user);
        user.setPreferences(prefs);
        return userRepository.save(user);
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    @Transactional
    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(existingUser -> {
            if (updatedUser.getFname() != null) {
                existingUser.setFname(updatedUser.getFname());
            }
            if (updatedUser.getLogin() != null && !updatedUser.getLogin().equalsIgnoreCase(existingUser.getLogin())) {
                // Check if login exists for another user
                var existingLogin = userRepository.findByLogin(updatedUser.getLogin());
                if (existingLogin.isPresent() && !existingLogin.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Login already exists");
                }
                existingUser.setLogin(updatedUser.getLogin());
            }
            if (updatedUser.getEmail() != null && !updatedUser.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
                // Check if email exists for another user
                var existingEmail = userRepository.findByEmail(updatedUser.getEmail());
                if (existingEmail.isPresent() && !existingEmail.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Email already exists");
                }
                existingUser.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getLname() != null) {
                existingUser.setLname(updatedUser.getLname());
            }
            if (updatedUser.getPhone() != null) {
                existingUser.setPhone(updatedUser.getPhone());
            }
            if (updatedUser.getAddress() != null) {
                existingUser.setAddress(updatedUser.getAddress());
            }
            if (updatedUser.getCin() != null && !updatedUser.getCin().equals(existingUser.getCin())) {
                // Check if CIN exists for another user
                var existingCin = userRepository.findByCin(updatedUser.getCin());
                if (existingCin.isPresent() && !existingCin.get().getId().equals(id)) {
                    throw new IllegalArgumentException("CIN already exists");
                }
                existingUser.setCin(updatedUser.getCin());
            }
            if (updatedUser.getPasswordHash() != null) {
                existingUser.setPasswordHash(PasswordUtil.hash(updatedUser.getPasswordHash()));
            }
            if (updatedUser.getRole() != null) {
                existingUser.setRole(updatedUser.getRole());
            }
            existingUser.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(existingUser);
        }).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public User updateLastLogin(Long id) {
        return userRepository.findById(id).map(user -> {
            user.setLastLogin(LocalDateTime.now());
            return userRepository.save(user);
        }).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }



    public Page<User> searchUsers(String q, User.UserRole role, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            java.util.List<Predicate> predicates = new ArrayList<>();

            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }

            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                Predicate match = cb.or(
                    cb.like(cb.lower(root.get("login")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("fname")), like),
                    cb.like(cb.lower(root.get("lname")), like),
                    cb.like(cb.lower(root.get("phone")), like),
                    cb.like(cb.lower(root.get("cin")), like)
                );
                predicates.add(match);
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable);
    }

    @Transactional
    public User updateOwnProfile(Long id, UpdateProfileRequest request) {
        return userRepository.findById(id).map(user -> {
            if (request.getLogin() != null && !request.getLogin().equalsIgnoreCase(user.getLogin())) {
                // Check if login exists for another user
                var existingLogin = userRepository.findByLogin(request.getLogin());
                if (existingLogin.isPresent() && !existingLogin.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Login already exists");
                }
                user.setLogin(request.getLogin());
            }
            if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
                // Check if email exists for another user
                var existingEmail = userRepository.findByEmail(request.getEmail());
                if (existingEmail.isPresent() && !existingEmail.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Email already exists");
                }
                user.setEmail(request.getEmail());
            }
            if (request.getPassword() != null) {
                user.setPasswordHash(PasswordUtil.hash(request.getPassword()));
            }
            if (request.getPhone() != null) {
                user.setPhone(request.getPhone());
            }
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public UserPreferences getUserPreferences(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return preferencesRepository.findByUser(user)
            .orElseThrow(() -> new IllegalArgumentException("Preferences not found"));
    }

    @Transactional
    public UserPreferences updateUserPreferences(Long userId, UserPreferencesRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        UserPreferences prefs = preferencesRepository.findByUser(user)
            .orElseThrow(() -> new IllegalArgumentException("Preferences not found"));
        
        if (request.getLanguage() != null) {
            prefs.setLanguage(request.getLanguage());
        }
        if (request.getNotificationEmail() != null) {
            prefs.setNotificationEmail(request.getNotificationEmail());
        }
        if (request.getNotificationSms() != null) {
            prefs.setNotificationSms(request.getNotificationSms());
        }
        if (request.getNotificationPush() != null) {
            prefs.setNotificationPush(request.getNotificationPush());
        }
        if (request.getNotificationInApp() != null) {
            prefs.setNotificationInApp(request.getNotificationInApp());
        }
        if (request.getTheme() != null) {
            prefs.setTheme(request.getTheme());
        }
        
        return preferencesRepository.save(prefs);
    }

    public Page<User> searchAgentClients(Long agentId, String q, Pageable pageable) {
        User agent = userRepository.findById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found"));
        
        if (agent.getRole() != User.UserRole.AGENT) {
            throw new IllegalArgumentException("User is not an agent");
        }
        
        Specification<User> spec = (root, query, cb) -> {
            java.util.List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(cb.equal(root.get("role"), User.UserRole.CLIENT));
            
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                Predicate match = cb.or(
                    cb.like(cb.lower(root.get("login")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("fname")), like),
                    cb.like(cb.lower(root.get("lname")), like),
                    cb.like(cb.lower(root.get("phone")), like),
                    cb.like(cb.lower(root.get("cin")), like)
                );
                predicates.add(match);
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return userRepository.findAll(spec, pageable);
    }
    @Transactional
    public User activateUserProfile(Long id) {
        return userRepository.findById(id).map(user -> {
            user.setIsActive(true);
            return userRepository.save(user);
        }).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
    @Transactional
    public User deactivateUserProfile(Long id) {
        return userRepository.findById(id).map(user -> {
            user.setIsActive(false);
            return userRepository.save(user);
        }).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User authenticateUser(String login, String password) {
        Optional<User> user = userRepository.findByLogin(login);
        if (user == null || !user.isPresent()) {
            throw new IllegalArgumentException("Invalid login or password");
        }
        
        if (!PasswordUtil.matches(password, user.get().getPasswordHash())) {
            throw new IllegalArgumentException("Invalid login or password");
        }
        
        return user.get();
    }
}
