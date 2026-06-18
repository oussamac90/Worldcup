package com.goalkeeperdash.backoffice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Back-office security (§7.1): a SEPARATE, higher-priority filter chain matching
 * {@code /admin/**}. Cookie session + form login, role ADMIN. It MUST NOT share
 * the player JWT path (which is a different chain for {@code /api/**}).
 */
@Configuration
public class AdminSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http,
                                                AdminUserDetailsService adminUserDetailsService,
                                                PasswordEncoder passwordEncoder) throws Exception {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        http
                .securityMatcher("/admin/**")
                .authenticationProvider(provider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login", "/admin/css/**").permitAll()
                        .anyRequest().hasRole("ADMIN"))
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .defaultSuccessUrl("/admin", true)
                        .failureUrl("/admin/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout")
                        .permitAll());
        return http.build();
    }
}
