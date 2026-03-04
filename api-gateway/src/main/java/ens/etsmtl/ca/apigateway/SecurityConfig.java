package ens.etsmtl.ca.apigateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/clients/registrations").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/clients/registrations/confirm").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/clients/login").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/clients/login/challenge").permitAll()
                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                                .requestMatchers("/docs/client-service/**").permitAll()
                                .requestMatchers("/docs/account-service/**").permitAll()
                                .requestMatchers("/internal/**").hasAuthority("SCOPE_internal.service")
                                .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
