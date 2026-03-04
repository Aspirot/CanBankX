package ens.etsmtl.ca.apigateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@SpringBootApplication
public class ApiGatewayApplication {

    @Value("${gateway.client-service-url:http://localhost:8081}")
    private String clientServiceUrl;
    @Value("${gateway.account-service-url:http://localhost:8082}")
    private String accountServiceUrl;
    @Value("${gateway.ledger-service-url:http://localhost:8084}")
    private String ledgerServiceUrl;

    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {
        return route("client_service")
                .route(path("/api/v1/clients/**"), http())
                .before(uri(clientServiceUrl))
                .build()
                .and(route("client_service_docs")
                        .route(path("/docs/client-service/**"), http())
                        .before(stripPrefix(2))
                        .before(uri(clientServiceUrl))
                        .build())
                .and(route("account_service")
                        .route(path("/api/v1/accounts/**"), http())
                        .before(uri(accountServiceUrl))
                        .build())
                .and(route("account_service_docs")
                        .route(path("/docs/account-service/**"), http())
                        .before(stripPrefix(2))
                        .before(uri(accountServiceUrl))
                        .build())
                .and(route("internal_ledger_service")
                        .route(path("/internal/api/v1/ledger/**"), http())
                        .before(stripPrefix(1))
                        .before(uri(ledgerServiceUrl))
                        .build());
    }
}
