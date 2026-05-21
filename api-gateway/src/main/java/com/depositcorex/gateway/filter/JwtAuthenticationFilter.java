package com.depositcorex.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This filter intercepts all incoming HTTP requests entering the API Gateway.
 * It handles centralized JWT security validation, preventing unauthenticated traffic 
 * from reaching the downstream microservices.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    // Injecting the JWT signing secret key from application.properties or application.yml
    @Value("${jwt.secret}")
    private String jwtSecret;

    // List of bypass paths that do not require authentication (e.g., login, registration)
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/signup"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // STEP 1: Check if the requested URI is a public endpoint.
        // If it's public, skip token validation and let the request pass immediately.
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        
        // STEP 2: Extract the 'Authorization' header from the incoming HTTP request.
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        // STEP 3: Validate the format of the header. It must exist and must start with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange.getResponse()); // Block the request if the header is missing/invalid
        }

        // STEP 4: Extract the actual JWT token string by cutting off the "Bearer " prefix (7 characters)
        String token = authHeader.substring(7);
        try {
            // STEP 5: Parse and validate the cryptographic signature of the token
            Claims claims = parseToken(token);
            
            // STEP 6: Context Downstream Propagation (Token Mutation)
            // Instead of passing the raw JWT to microservices, we extract useful data (Claims)
            // and inject them into custom HTTP headers. Downstream microservices can now read 
            // user information simply by checking headers like "X-User-Id" without needing to parse the JWT again.
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Role", claims.get("role", String.class))
                    .header("X-User-Name", claims.get("name", String.class))
                    .build();
            
            // Forward the mutated request containing the user info headers down the filter chain
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
            
        } catch (JwtException | IllegalArgumentException e) {
            // If token parsing fails (expired token, tampered signature, invalid formatting),
            // catch the exception and block access.
            return unauthorized(exchange.getResponse());
        }
    }

    /**
     * Cryptographically validates the JWT token against our secret key 
     * and extracts the claims (payload) inside it.
     */
    private Claims parseToken(String token) {
        // Convert the string secret into a secure HmacSHA key object
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        // Use the JJWT parser builder to verify the signature and read the payload data
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Helper method to verify if the requested endpoint matches any path in our public bypass list.
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Helper method to intercept the response stream and immediately terminate it 
     * returning an HTTP status code 401 Unauthorized.
     */
    private Mono<Void> unauthorized(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete(); // Closes the connection and returns to client
    }

    /**
     * Defines the execution order of this filter. 
     * Returning a value of -1 guarantees that this authentication filter executes
     * ahead of almost every other custom filter in the API Gateway.
     */
    @Override
    public int getOrder() {
        return -1;
    }
}