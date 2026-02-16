package com.drivingschool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Security configuration for the Driving School Management System
 * Handles authentication, authorization and password encryption
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Configure HTTP security - who can access what URLs
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                // Public pages (no authentication required)
                    .requestMatchers("/login").permitAll()
                    .requestMatchers("/h2-console/**").permitAll() // for development only

                // Admin pages
                    .requestMatchers("/admin/**").hasRole("ADMIN")

                // Instructor pages
                    .requestMatchers("/instructor/**").hasRole("INSTRUCTOR")

                // Trainee pages
                    .requestMatchers("/trainee/**").hasRole("TRAINEE")

                // all other requests require authentication
                    .anyRequest().authenticated()
                )
                .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/dashboard", true)  // Redirect after successful login
                    .failureUrl("/login?error=true")        // Redirect on failure
                    .permitAll()
                )
                .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
                )
                .rememberMe(remember -> remember
                    .key("uniqueAndSecret")
                    .tokenValiditySeconds(86400) // 24 hours
                )
                .exceptionHandling(exception -> exception
                    .accessDeniedPage("/access-denied")
                );

         // Disable CSRF for H2 console (development only)
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
        );

        // Allow H2 console frames (development only)
        http.headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
        );

        return http.build();
    }

    /**
     * Password encoder bean - uses BCrypt hashing
     * BCrypt automatically handles salting and is designed to be slow
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
