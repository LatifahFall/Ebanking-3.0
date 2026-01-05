package com.bank.graphql_gateway.resolver;

import com.bank.graphql_gateway.model.CreateUserInput;
import com.bank.graphql_gateway.model.UserDTO;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;

@Controller
public class MutationResolver {

    private final WebClient.Builder webClient;

    public MutationResolver(WebClient.Builder webClient) {
        this.webClient = webClient;
    }

    @MutationMapping
    public UserDTO createUser(@Argument CreateUserInput input) {
        return webClient.build()
                .post()
                .uri("http://localhost:8081/admin/users")
                .bodyValue(input)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }

    @MutationMapping
    public UserDTO activateUser(@Argument Long id) {
        return webClient.build()
                .put()
                .uri("http://localhost:8081/admin/users/{id}/activate", id)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }

    @MutationMapping
    public UserDTO deactivateUser(@Argument Long id) {
        return webClient.build()
                .put()
                .uri("http://localhost:8081/admin/users/{id}/deactivate", id)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }
}
