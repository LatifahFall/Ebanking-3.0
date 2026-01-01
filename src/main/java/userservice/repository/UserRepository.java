package userservice.repository;

import userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByLogin(String login);
    Optional<User> findByCin(String cin);
    boolean existsByEmail(String email);
    boolean existsByLogin(String login);
    boolean existsByCin(String cin);
}