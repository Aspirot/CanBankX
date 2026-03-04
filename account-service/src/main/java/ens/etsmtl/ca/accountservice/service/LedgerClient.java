package ens.etsmtl.ca.accountservice.service;

import ens.etsmtl.ca.accountservice.model.dto.LedgerEntryRequest;
import ens.etsmtl.ca.accountservice.service.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Component
public class LedgerClient {

    private final RestClient restClient;
    private final String ledgerBaseUrl;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String scope;

    public LedgerClient(
            @Value("${ledger-service.base-url:http://localhost:8085/internal}") String ledgerBaseUrl,
            @Value("${internal-auth.token-url:http://localhost:8080/realms/bank-realm/protocol/openid-connect/token}") String tokenUrl,
            @Value("${internal-auth.client-id:account-service}") String clientId,
            @Value("${internal-auth.client-secret:account-service-secret}") String clientSecret,
            @Value("${internal-auth.scope:internal.service}") String scope
    ) {
        this.restClient = RestClient.builder().build();
        this.ledgerBaseUrl = ledgerBaseUrl;
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
    }

    public void appendEntry(LedgerEntryRequest request) {
        String url = ledgerBaseUrl + "/api/v1/ledger/entries";
        String accessToken = fetchAccessToken();
        try {
            restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new ExternalServiceException("Ledger service rejected entry: HTTP " + ex.getStatusCode().value());
        } catch (Exception ex) {
            throw new ExternalServiceException("Ledger service is unavailable");
        }
    }

    private String fetchAccessToken() {
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
