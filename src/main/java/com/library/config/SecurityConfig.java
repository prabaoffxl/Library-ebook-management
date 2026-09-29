package com.library.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.ApiResponse;
import com.library.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    objectMapper.writeValue(response.getOutputStream(), ApiResponse.error("Unauthorized access: " + authException.getMessage()));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    objectMapper.writeValue(response.getOutputStream(), ApiResponse.error("Access denied: " + accessDeniedException.getMessage()));
                })
            )
            .authorizeHttpRequests(auth -> auth
                // Static resources & Frontend
                .requestMatchers("/", "/index.html", "/login.html", "/admin-dashboard.html", "/librarian-dashboard.html", "/user-dashboard.html", "/licenses.html", "/reports.html", "/*.html", "/css/**", "/js/**", "/favicon.ico").permitAll()
                // Auth & Registration & Public Cover Images
                .requestMatchers("/api/auth/login", "/api/users/register", "/api/books/cover/**").permitAll()

                // Reports & Analytics APIs (Admin & Librarian only)
                .requestMatchers("/api/reports", "/api/reports/**").hasAnyRole("ADMIN", "LIBRARIAN")
                
                // Book & Search & Reading APIs
                .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/*", "/api/books/search", "/api/books/search/**", "/api/books/filter", "/api/books/categories", "/api/books/languages", "/api/books/authors", "/api/books/*/libraries", "/api/books/popular", "/api/books/popular/**", "/api/books/*/download", "/api/books/*/read", "/api/books/*/preview", "/api/books/*/progress").hasAnyRole("ADMIN", "LIBRARIAN", "USER")
                .requestMatchers(HttpMethod.PUT, "/api/books/*/progress").hasAnyRole("ADMIN", "LIBRARIAN", "USER")
                .requestMatchers("/api/search/history", "/api/search/history/**").hasAnyRole("ADMIN", "LIBRARIAN", "USER")
                .requestMatchers(HttpMethod.POST, "/api/books", "/api/books/*/upload", "/api/books/*/cover").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.PUT, "/api/books/*").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.DELETE, "/api/books/*").hasRole("ADMIN")

                // License APIs
                .requestMatchers(HttpMethod.GET, "/api/licenses/book/*/history").hasAnyRole("ADMIN", "LIBRARIAN", "USER")
                .requestMatchers(HttpMethod.POST, "/api/licenses/*/suspend", "/api/licenses/*/cancel").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/licenses/*").hasRole("ADMIN")
                .requestMatchers("/api/licenses", "/api/licenses/**").hasAnyRole("ADMIN", "LIBRARIAN")

                // User Profile & Activity & Notifications & Reading Progress APIs
                .requestMatchers(HttpMethod.GET, "/api/users/me", "/api/users/me/activity", "/api/users/me/reading-progress").hasAnyRole("ADMIN", "LIBRARIAN", "USER")
                .requestMatchers(HttpMethod.PUT, "/api/users/me").hasAnyRole("ADMIN", "LIBRARIAN", "USER")
                .requestMatchers("/api/notifications", "/api/notifications/**").hasAnyRole("ADMIN", "LIBRARIAN", "USER")
                .requestMatchers("/api/activities/recent").hasRole("ADMIN")
                .requestMatchers("/api/activities/library/*").hasAnyRole("ADMIN", "LIBRARIAN")

                // Library APIs
                .requestMatchers(HttpMethod.GET, "/api/libraries/active", "/api/libraries/search", "/api/libraries/*", "/api/libraries/*/books").hasAnyRole("ADMIN", "LIBRARIAN", "USER")
                .requestMatchers(HttpMethod.POST, "/api/libraries/*/books/*").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.DELETE, "/api/libraries/*/books/*").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.PUT, "/api/libraries/*").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.POST, "/api/libraries", "/api/libraries/*/librarians/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/libraries/*", "/api/libraries/*/librarians/*").hasRole("ADMIN")
                .requestMatchers("/api/libraries", "/api/libraries/**").hasAnyRole("ADMIN", "LIBRARIAN")

                // User Management APIs (Admin)
                .requestMatchers("/api/users/paginated", "/api/users/*/status").hasRole("ADMIN")
                .requestMatchers("/api/users", "/api/users/**").hasRole("ADMIN")

                // Digital Asset Management APIs
                .requestMatchers(HttpMethod.GET, "/api/assets", "/api/assets/*", "/api/assets/book/*", "/api/assets/search", "/api/assets/*/download").hasAnyRole("ADMIN", "LIBRARIAN", "USER")
                .requestMatchers(HttpMethod.GET, "/api/assets/*/integrity").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.POST, "/api/assets/**").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.PUT, "/api/assets/**").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.DELETE, "/api/assets/**").hasRole("ADMIN")

                // Dashboard stats
                .requestMatchers("/api/dashboard/**").authenticated()

                // Default
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
