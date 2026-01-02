package userservice.service;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import userservice.dto.UserCreatedEvent;
import userservice.dto.UserUpdatedEvent;
import userservice.dto.ClientAssignedEvent;
import userservice.dto.ClientUnassignedEvent;
import userservice.dto.UserActivatedEvent;
import userservice.dto.UserDeactivatedEvent;
import userservice.model.User;
@Service
@Slf4j
public class UserEventProducer {
    
    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    // Pour AdminController - sans creatorId/creatorName
    public void publishUserCreatedEvent(User createdUser) {
        if (kafkaTemplate == null) {
            log.warn("Kafka not configured - skipping event publication for user: {}", createdUser.getId());
            return;
        }
        try {
            UserCreatedEvent event = new UserCreatedEvent(
                createdUser.getId(),
                createdUser.getFname()+" "+createdUser.getLname(),
                "ADMIN",
                LocalDateTime.now()
            );
            kafkaTemplate.send("user.created", String.valueOf(createdUser.getId()), event);
            log.info("User created event published for user: {}", createdUser.getId());
        } catch (Exception e) {
            log.warn("Failed to publish user created event: {}", e.getMessage());
        }
    }
    
    // Pour AgentController - avec toutes les infos
    public void publishUserCreatedEventWithCreator(User createdUser, User creator) {
        if (kafkaTemplate == null) {
            log.warn("Kafka not configured - skipping event publication for user: {}", createdUser.getId());
            return;
        }
        try {
            UserCreatedEvent event = new UserCreatedEvent(
                createdUser.getId(),
                createdUser.getFname()+" "+createdUser.getLname(),
                creator.getId(),
                creator.getRole().toString(),
                creator.getFname()+" "+creator.getLname(),
                LocalDateTime.now()
            );
            kafkaTemplate.send("user.created", String.valueOf(createdUser.getId()), event);
            log.info("User created event published for user: {} by creator: {}", createdUser.getId(), creator.getId());
        } catch (Exception e) {
            log.warn("Failed to publish user created event: {}", e.getMessage());
        }
    }


        // Pour AdminController
    public void publishUserUpdatedEvent(User updatedUser) {
        if (kafkaTemplate == null) {
            log.warn("Kafka not configured - skipping event publication");
            return;
        }
        try {
            UserUpdatedEvent event = new UserUpdatedEvent(
                updatedUser.getId(),
                updatedUser.getFname()+" "+updatedUser.getLname(),
                "ADMIN",
                LocalDateTime.now()
            );
            kafkaTemplate.send("user.updated", String.valueOf(updatedUser.getId()), event);
            log.info("User updated event published for user: {}", updatedUser.getId());
        } catch (Exception e) {
            log.warn("Failed to publish user updated event: {}", e.getMessage());
        }
    }

    // Pour AgentController  and MeController
    public void publishUserUpdatedEventWithUpdater(User updatedUser, User updater) {
        if (kafkaTemplate == null) {
            log.warn("Kafka not configured - skipping event publication");
            return;
        }
        try {
            UserUpdatedEvent event = new UserUpdatedEvent(
                updatedUser.getId(),
                updatedUser.getFname()+" "+updatedUser.getLname(),
                updater.getId(),
                updater.getRole().toString(),
                updater.getFname()+" "+updater.getLname(),
                LocalDateTime.now()
            );
            kafkaTemplate.send("user.updated", String.valueOf(updatedUser.getId()), event);
            log.info("User updated event published for user: {} by updater: {}", updatedUser.getId(), updater.getId());
        } catch (Exception e) {
            log.warn("Failed to publish user updated event: {}", e.getMessage());
        }
    }
    
    // Pour assignment - ADMIN uniquement
    public void publishClientAssignedEvent(Long clientId, Long agentId) {
        if (kafkaTemplate == null) {
            log.warn("Kafka not configured - skipping event publication");
            return;
        }
        try {
            ClientAssignedEvent event = new ClientAssignedEvent(
                clientId,
                agentId,
                "ADMIN",
                LocalDateTime.now()
            );
            kafkaTemplate.send("client.assigned", String.valueOf(clientId), event);
            log.info("Client assigned event published: client {} to agent {}", clientId, agentId);
        } catch (Exception e) {
            log.warn("Failed to publish client assigned event: {}", e.getMessage());
        }
    }
    
    // Pour disassignment - ADMIN uniquement
    public void publishClientUnassignedEvent(Long clientId, Long agentId) {
        if (kafkaTemplate == null) {
            log.warn("Kafka not configured - skipping event publication");
            return;
        }
        try {
            ClientUnassignedEvent event = new ClientUnassignedEvent(
                clientId,
                agentId,
                "ADMIN",
                LocalDateTime.now()
            );
            kafkaTemplate.send("client.unassigned", String.valueOf(clientId), event);
            log.info("Client unassigned event published: client {} from agent {}", clientId, agentId);
        } catch (Exception e) {
            log.warn("Failed to publish client unassigned event: {}", e.getMessage());
        }
    }
    
    // Pour activation - ADMIN ou AGENT
    public void publishUserActivatedEvent(Long userId, String role) {
        if (kafkaTemplate == null) {
            log.warn("Kafka not configured - skipping event publication");
            return;
        }
        try {
            UserActivatedEvent event = new UserActivatedEvent(
                userId,
                role,
                LocalDateTime.now()
            );
            kafkaTemplate.send("user.activated", String.valueOf(userId), event);
            log.info("User activated event published for user: {} by role: {}", userId, role);
        } catch (Exception e) {
            log.warn("Failed to publish user activated event: {}", e.getMessage());
        }
    }
    
    // Pour désactivation - ADMIN ou AGENT
    public void publishUserDeactivatedEvent(Long userId, String role) {
        if (kafkaTemplate == null) {
            log.warn("Kafka not configured - skipping event publication");
            return;
        }
        try {
            UserDeactivatedEvent event = new UserDeactivatedEvent(
                userId,
                role,
                LocalDateTime.now()
            );
            kafkaTemplate.send("user.deactivated", String.valueOf(userId), event);
            log.info("User deactivated event published for user: {} by role: {}", userId, role);
        } catch (Exception e) {
            log.warn("Failed to publish user deactivated event: {}", e.getMessage());
        }
    }
}