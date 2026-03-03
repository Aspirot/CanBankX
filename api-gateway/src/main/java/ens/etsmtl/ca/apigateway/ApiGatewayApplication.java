package ens.etsmtl.ca.apigateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@SpringBootApplication
public class ApiGatewayApplication {

    @Value("${gateway.client-service-url:http://localhost:8081}")
    private String clientServiceUrl;

    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {
        return route("client_service")
                .route(path("/api/v1/clients/**"), http())
                .before(uri(clientServiceUrl))
                .build();
    }
}
