package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.example.demo.service.AuthFilterService;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final AuthFilterService authFilterService;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/**").permitAll()  
                .requestMatchers("/cdash/applications/**").permitAll()  
                .requestMatchers("/jobs/**").permitAll()
                .requestMatchers("/api/logos/**").permitAll()  
                .requestMatchers("/resume/**").permitAll()  
                .requestMatchers("/rdash/resume").permitAll()  
                .requestMatchers("/cdash/applications/api/jobapplications").permitAll() 
                .requestMatchers("/api/hello").permitAll()  
                .requestMatchers("/api/gather").permitAll()  

                .requestMatchers("/api/make-call").permitAll() 
                .requestMatchers("/api/**").authenticated()  // Adjust as needed
                // Bypass JWT authentication for this endpoint
                .anyRequest().authenticated())  
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(authFilterService, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
