package ens.etsmtl.ca.paymentservice.service.clients;

import ens.etsmtl.ca.paymentservice.service.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Component
public class InternalAuthTokenProvider {

    private final RestClient restClient;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String scope;

    public InternalAuthTokenProvider(
            @Value("${internal-auth.token-url}") String tokenUrl,
            @Value("${internal-auth.client-id}") String clientId,
            @Value("${internal-auth.client-secret}") String clientSecret,
            @Value("${internal-auth.scope:internal.service}") String scope
    ) {
        this.restClient = RestClient.builder().build();
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
    }

    public String fetchAccessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("scope", scope);

        try {
            Map<?, ?> response = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);

            if (response == null || response.get("access_token") == null) {
                throw new ExternalServiceException("Unable to obtain internal access token");
            }
            return response.get("access_token").toString();
        } catch (RestClientResponseException ex) {
            throw new ExternalServiceException("Unable to obtain internal access token: HTTP " + ex.getStatusCode().value());
        } catch (Exception ex) {
            throw new ExternalServiceException("Unable to obtain internal access token");
        }
    }
}
