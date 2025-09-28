package bigcie.bigcie.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .requiresChannel(channel -> channel.anyRequest())//enforce https
                .authorizeHttpRequests(authz -> authz
//                        .requestMatchers("/api-docs").permitAll()
//                        .requestMatchers("/swagger-ui.html").permitAll()
                                .requestMatchers("/api/v1/register").permitAll()
                                .requestMatchers("/api/v1/property").permitAll()
                                .requestMatchers("/api/v1/property-summary").permitAll()
                                .requestMatchers("/api/v1/login").permitAll()
                                .requestMatchers("/api/v1/hello-world").permitAll()
                                .requestMatchers("/api/v1/unit").permitAll()
                                .requestMatchers("/api/v1/lease").permitAll()
                                .requestMatchers("/api/v1/tenant").permitAll()
                                .requestMatchers("/api/v1/users").permitAll()
                                .anyRequest().authenticated()
                )
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authenticationProvider(authenticationProvider)
//                .addFilterBefore(hostCheckFilter, UsernamePasswordAuthenticationFilter.class)
//                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
//                .addFilterBefore(jwAuthenFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf().disable()
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        // Explicitly return as CorsConfigurationSource
        return (CorsConfigurationSource) source;
    }
}
