package userservice.service;

import userservice.model.User;
import userservice.model.UserPreferences;
import userservice.repository.UserRepository;
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

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
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
            if (updatedUser.getLogin() != null) {
                existingUser.setLogin(updatedUser.getLogin());
            }
            if (updatedUser.getEmail() != null) {
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
            if (updatedUser.getCin() != null) {
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
    
    
}