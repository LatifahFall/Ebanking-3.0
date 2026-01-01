package userservice.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import userservice.model.AgentClientAssignment;
import userservice.model.User;

public interface AgentClientAssignmentRepository extends JpaRepository<AgentClientAssignment, Long> {
    boolean existsByClient(User client);
    Optional<AgentClientAssignment> findByClient(User client);
    List<AgentClientAssignment> findByAgent(User agent);
}
