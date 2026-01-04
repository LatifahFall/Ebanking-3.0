package com.ebanking.payment.repository;

import com.ebanking.payment.entity.UserBiometricEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBiometricEnrollmentRepository extends JpaRepository<UserBiometricEnrollment, UUID> {
    
    Optional<UserBiometricEnrollment> findByUserIdAndIsActiveTrue(Long userId);
    
    Optional<UserBiometricEnrollment> findByUserId(Long userId);
    
    boolean existsByUserIdAndIsActiveTrue(Long userId);
}

