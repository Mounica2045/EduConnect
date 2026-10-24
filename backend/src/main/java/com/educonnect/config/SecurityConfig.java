package com.educonnect.config;

import com.educonnect.security.CustomUserDetailsService;
import com.educonnect.security.JwtAuthenticationEntryPoint;
import com.educonnect.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 6.x Configuration using modern SecurityFilterChain and stateless JWT architecture.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CorsConfigurationSource corsConfigurationSource
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Explicit CORS configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // CSRF is disabled for stateless REST APIs utilizing Bearer token authentication
                .csrf(AbstractHttpConfigurer::disable)

                // Standardized 401 response for unauthenticated calls
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // Stateless session management (No HTTP sessions stored server-side)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public authentication endpoints
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()

                        // Public resource exploration endpoints (Read-only)
                        .requestMatchers(HttpMethod.GET, "/api/resources/**").permitAll()

                        // Public contact submission
                        .requestMatchers(HttpMethod.POST, "/api/contact").permitAll()

                        // Admin-only endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Authenticated student & member endpoints
                        .requestMatchers("/api/auth/me", "/api/auth/logout").authenticated()
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/resources/**").authenticated()
                        .requestMatchers("/api/bookmarks/**").authenticated()

                        // Default rule: all other endpoints require authentication
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
