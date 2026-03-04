package ens.etsmtl.ca.clientservice.service;

import ens.etsmtl.ca.clientservice.config.KeycloakProperties;
import ens.etsmtl.ca.clientservice.service.exception.ValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class KeycloakAdminService {

    private final RestClient restClient;
    private final KeycloakProperties properties;

    public KeycloakAdminService(KeycloakProperties properties) {
        this.restClient = RestClient.builder().build();
        this.properties = properties;
    }

    public String createUser(String firstName, String lastName, String email, String password) {
        String adminAccessToken = getAdminAccessToken();
        String createUserUri = properties.getBaseUrl() + "/admin/realms/" + properties.getRealm() + "/users";

        Map<String, Object> payload = Map.of(
                "username", email,
                "email", email,
                "firstName", firstName,
                "lastName", lastName,
                "enabled", true,
                "emailVerified", false,
                "requiredActions", List.of("CONFIGURE_TOTP"),
                "credentials", List.of(Map.of(
                        "type", "password",
                        "temporary", false,
                        "value", password
                ))
        );

        try {
            ResponseEntity<Void> response = restClient.post()
                    .uri(createUserUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            URI location = response.getHeaders().getLocation();
            if (location == null) {
                throw new ValidationException("Keycloak user created but missing location header");
            }

            String path = location.getPath();
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash < 0 || lastSlash == path.length() - 1) {
                throw new ValidationException("Unable to parse Keycloak user id");
            }
            return path.substring(lastSlash + 1);
        } catch (RestClientResponseException ex) {
            String message = "Unable to create Keycloak user: HTTP " + ex.getStatusCode().value();
            if (ex.getStatusCode().value() == 409) {
                message = "A Keycloak account already exists for this email";
            }
            throw new ValidationException(message);
        }
    }

    public void verifyOtp(String username, String password, String otp) {
        authenticateWithOtp(username, password, otp);
    }

    public Map<String, Object> authenticateWithOtp(String username, String password, String otp) {
        String tokenUri = properties.getBaseUrl() + "/realms/" + properties.getRealm() + "/protocol/openid-connect/token";
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", properties.getAuthClientId());
        form.add("username", username);
        form.add("password", password);
        form.add("totp", otp);

        try {
            Map<?, ?> response = restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> tokens = (Map<String, Object>) response;
            return tokens;
        } catch (RestClientResponseException ex) {
            throw new ValidationException("OTP validation failed in Keycloak");
        }
    }

    private String getAdminAccessToken() {
        String tokenUri = properties.getBaseUrl() + "/realms/master/protocol/openid-connect/token";
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", properties.getAdminClientId());
        form.add("username", properties.getAdminUsername());
        form.add("password", properties.getAdminPassword());

        try {
            Map<?, ?> response = restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);

            Object token = Objects.requireNonNull(response).get("access_token");
            if (token == null) {
                throw new ValidationException("Unable to get Keycloak admin access token");
            }
            return token.toString();
        } catch (RestClientResponseException ex) {
            throw new ValidationException("Unable to authenticate against Keycloak admin API");
        }
    }
}

