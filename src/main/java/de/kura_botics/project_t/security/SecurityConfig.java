package de.kura_botics.project_t.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Central security configuration for the application.
 * Defines access rules, login/logout behavior, and password encoding.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final int REMEMBER_ME_TOKEN_VALIDITY_SECONDS = 60 * 60 * 24 * 30; // 30 days
    private static final String SUCCESS_URL = "/";

    // TODO (Security): Move this hardcoded secret key to application.properties (e.g., app.security.remember-me.key)
    private static final String REMEMBER_ME_KEY = "create-your-own-key-mine-is-secret";

    /**
     * Configures the security filter chain.
     * Sets up route authorization, form-based login, remember-me functionality, and logout.
     *
     * @param httpSecurity             The Spring HttpSecurity builder.
     * @param customUserDetailsService The custom service to load user data for the remember-me token.
     * @return The built SecurityFilterChain.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, CustomUserDetailsService customUserDetailsService) throws Exception {

        httpSecurity.authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/", "/register", "/css/**", "/js/**", "/error").permitAll()
                .anyRequest().authenticated()
        );

        httpSecurity.formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl(SUCCESS_URL, true)
                .permitAll()
        );

        httpSecurity.rememberMe(remember -> remember
                .key(REMEMBER_ME_KEY)
                .userDetailsService(customUserDetailsService)
                .tokenValiditySeconds(REMEMBER_ME_TOKEN_VALIDITY_SECONDS)
        );

        httpSecurity.logout(logout -> logout
                .logoutSuccessUrl(SUCCESS_URL)
                .permitAll()
        );

        return httpSecurity.build();
    }

    /**
     * Defines the password encoder used for authenticating users.
     *
     * @return A BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}