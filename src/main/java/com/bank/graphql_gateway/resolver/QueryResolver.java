package com.bank.graphql_gateway.resolver;

import com.bank.graphql_gateway.model.UserDTO;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Controller
public class QueryResolver {

    private final WebClient.Builder webClient;

    public QueryResolver(WebClient.Builder webClient) {
        this.webClient = webClient;
    }

    @QueryMapping
    public String health() {
        return "GraphQL Gateway is UP";
    }

    @QueryMapping
    public List<UserDTO> users() {
        return webClient.build()
                .get()
                .uri("http://localhost:8081/admin/users/search")
                .retrieve()
                .bodyToFlux(UserDTO.class)
                .collectList()
                .block();
    }

    @QueryMapping
    public UserDTO userById(@Argument Long id) {
        return webClient.build()
                .get()
                .uri("http://localhost:8081/admin/users/{id}", id)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }
}
