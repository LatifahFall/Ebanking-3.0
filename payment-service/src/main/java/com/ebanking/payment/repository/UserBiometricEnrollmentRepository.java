package com.ebanking.payment.repository;

import com.ebanking.payment.entity.UserBiometricEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBiometricEnrollmentRepository extends JpaRepository<UserBiometricEnrollment, UUID> {
    
    Optional<UserBiometricEnrollment> findByUserIdAndIsActiveTrue(UUID userId);
    
    Optional<UserBiometricEnrollment> findByUserId(UUID userId);
    
    boolean existsByUserIdAndIsActiveTrue(UUID userId);
}

