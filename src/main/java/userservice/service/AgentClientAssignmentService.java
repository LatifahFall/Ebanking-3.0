package userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import userservice.dto.AgentClientAssignmentResponse;
import userservice.dto.AssignClientRequest;
import userservice.model.AgentClientAssignment;
import userservice.model.User;
import userservice.repository.AgentClientAssignmentRepository;
import userservice.repository.UserRepository;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgentClientAssignmentService {

    private final AgentClientAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public AgentClientAssignmentResponse assignClient(AssignClientRequest request) {
        if (request.getAgentId().equals(request.getClientId())) {
            throw new IllegalArgumentException("Agent and client cannot be the same user");
        }

        User agent = userRepository.findById(request.getAgentId())
            .orElseThrow(() -> new IllegalArgumentException("Agent not found"));
        if (agent.getRole() != User.UserRole.AGENT) {
            throw new IllegalArgumentException("User is not an agent");
        }

        User client = userRepository.findById(request.getClientId())
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        if (client.getRole() != User.UserRole.CLIENT) {
            throw new IllegalArgumentException("User is not a client");
        }

        if (assignmentRepository.existsByClient(client)) {
            throw new IllegalArgumentException("Client already assigned to an agent");
        }

        User assignedBy = null;
        if (request.getAssignedBy() != null) {
            assignedBy = userRepository.findById(request.getAssignedBy())
                .orElseThrow(() -> new IllegalArgumentException("Assigning admin not found"));
            if (assignedBy.getRole() != User.UserRole.ADMIN) {
                throw new IllegalArgumentException("Assigning user is not an admin");
            }
        }

        AgentClientAssignment assignment = new AgentClientAssignment();
        assignment.setAgent(agent);
        assignment.setClient(client);
        assignment.setAssignedBy(assignedBy);
        assignment.setNotes(request.getNotes());

        AgentClientAssignment saved = assignmentRepository.save(assignment);
        return toResponse(saved);
    }

    private AgentClientAssignmentResponse toResponse(AgentClientAssignment assignment) {
        AgentClientAssignmentResponse response = new AgentClientAssignmentResponse();
        response.setId(assignment.getId());
        response.setAgentId(assignment.getAgent().getId());
        response.setClientId(assignment.getClient().getId());
        response.setAssignedById(assignment.getAssignedBy() != null ? assignment.getAssignedBy().getId() : null);
        response.setAssignedAt(assignment.getAssignedAt());
        response.setNotes(assignment.getNotes());
        return response;
    }

    @Transactional(readOnly = true)
    public List<User> getClientsForAgent(Long agentId) {
        User agent = userRepository.findById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found"));
        
        if (agent.getRole() != User.UserRole.AGENT) {
            throw new IllegalArgumentException("User is not an agent");
        }

        List<User> clients = assignmentRepository.findByAgent(agent).stream()
            .map(AgentClientAssignment::getClient)
            .collect(Collectors.toList());
        
        clients.forEach(Hibernate::initialize);
        return clients;
    }

    @Transactional(readOnly = true)
    public Optional<User> getAgentForClient(Long clientId) {
        User client = userRepository.findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        
        if (client.getRole() != User.UserRole.CLIENT) {
            throw new IllegalArgumentException("User is not a client");
        }

        Optional<AgentClientAssignment> assignment = assignmentRepository.findByClient(client);
        if (assignment.isPresent()) {
            User agent = assignment.get().getAgent();
            Hibernate.initialize(agent);
            return Optional.of(agent);
        }
        return Optional.empty();
    }
}
