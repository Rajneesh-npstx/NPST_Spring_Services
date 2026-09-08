package com.fund_transfer.backend.config;

import com.fund_transfer.backend.common.security.keycloak.KeycloakJwtAuthConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity          // enables @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

   private final KeycloakJwtAuthConverter keycloakJwtAuthConverter;
   
   @Bean
   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
       http
           .csrf(csrf -> csrf.disable())          // stateless JWT API, no cookie sessions here
           .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
           .authorizeHttpRequests(auth -> auth
               .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
               .requestMatchers("/api/v1/**").authenticated()
               .anyRequest().authenticated())
           .oauth2ResourceServer(oauth -> oauth
               .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthConverter)));
       return http.build();
   }
}
