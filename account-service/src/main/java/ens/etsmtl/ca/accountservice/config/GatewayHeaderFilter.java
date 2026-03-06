package ens.etsmtl.ca.accountservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class GatewayHeaderFilter extends OncePerRequestFilter {

    private final String headerName;
    private final String expectedSecret;

    public GatewayHeaderFilter(
            @Value("${internal.gateway.header-name:X-Internal-Gateway}") String headerName,
            @Value("${internal.gateway.secret:canbankx-gateway-secret}") String expectedSecret
    ) {
        this.headerName = headerName;
        this.expectedSecret = expectedSecret;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean protectedApi = path.startsWith("/api/");
        boolean options = HttpMethod.OPTIONS.matches(request.getMethod());

        if (protectedApi && !options) {
            String headerValue = request.getHeader(headerName);
            if (!expectedSecret.equals(headerValue)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Direct access is forbidden");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
