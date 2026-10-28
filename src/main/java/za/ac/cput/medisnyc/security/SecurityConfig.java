package za.ac.cput.medisnyc.security;

/* SecurityConfig.java
   Wires up stateless JWT auth + Role-Based Access Control (RBAC) rules.
   Author: Phemelo
*/

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // enables @PreAuthorize("hasRole('...')") on controllers/services
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          ObjectMapper objectMapper) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Module 1: public auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Module 6: Admin only
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers("/api/notifications/**").hasRole("PATIENT")

                        // Patient records and orders are restricted by both role and owner checks.
                        .requestMatchers(HttpMethod.GET, "/api/patients").hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/patients/**")
                        .hasAnyRole("ADMIN", "DOCTOR", "PHARMACIST", "PATIENT")
                        .requestMatchers(HttpMethod.POST, "/api/patients").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orders/patient/**")
                        .hasAnyRole("ADMIN", "PHARMACIST", "PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/orders").hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers(HttpMethod.GET, "/api/orders/**")
                        .hasAnyRole("ADMIN", "PHARMACIST", "PATIENT")
                        .requestMatchers(HttpMethod.POST, "/api/orders")
                        .hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers(HttpMethod.PUT, "/api/orders/**").hasAnyRole("ADMIN", "PHARMACIST")

                        // Module 4: Pharmacy inventory (write ops stay pharmacist/admin only;
                        // doctors get read-only access to the medication list so they can
                        // pick medications when writing a prescription in Module 3)
                        .requestMatchers(HttpMethod.GET, "/api/medications/**")
                        .hasAnyRole("ADMIN", "PHARMACIST", "DOCTOR")
                        .requestMatchers("/api/inventory/**", "/api/medications/**")
                        .hasAnyRole("ADMIN", "PHARMACIST")

                        // Module 3: Doctor consultation & prescriptions (create/update)
                        .requestMatchers("/api/consultations/**", "/api/medical-records/**")
                        .hasAnyRole("ADMIN", "DOCTOR")

                        // Module 5: Prescription processing - queue/ready-for-collection/advance
                        // are pharmacist operations; a patient tracking their own prescription
                        // (by id or by their patientId) just needs read access.
                        .requestMatchers(HttpMethod.GET, "/api/prescription-processing/queue",
                                "/api/prescription-processing/ready-for-collection")
                        .hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers(HttpMethod.PUT, "/api/prescription-processing/*/advance")
                        .hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers(HttpMethod.GET, "/api/prescription-processing/doctor/**")
                        .hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/prescription-processing/patient/**")
                        .hasAnyRole("ADMIN", "PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/prescription-processing/**")
                        .hasAnyRole("ADMIN", "PHARMACIST", "DOCTOR", "PATIENT")
                        .requestMatchers("/api/prescription-processing/**")
                        .hasAnyRole("ADMIN", "PHARMACIST")

                        // Module 2: Doctor directory - anyone logged in can browse it
                        // (needed to pick a doctor when booking); only admins can add one.
                        .requestMatchers(HttpMethod.GET, "/api/doctors/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/doctors").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/doctors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/doctors/**").hasRole("ADMIN")

                        // Module 2: Appointments - patients, doctors and admins
                        .requestMatchers(HttpMethod.GET, "/api/appointments")
                        .hasAnyRole("ADMIN", "PHARMACIST")
                        .requestMatchers(HttpMethod.GET, "/api/appointments/doctor/**")
                        .hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/appointments/patient/**")
                        .hasAnyRole("ADMIN", "PATIENT")
                        .requestMatchers(HttpMethod.POST, "/api/appointments")
                        .hasAnyRole("ADMIN", "PATIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/appointments/*/cancel")
                        .hasAnyRole("ADMIN", "PATIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/appointments/*/status")
                        .hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers("/api/appointments/**")
                        .hasAnyRole("ADMIN", "DOCTOR", "PATIENT")

                        // Everything else just needs to be authenticated
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions.accessDeniedHandler((request, response, exception) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    objectMapper.writeValue(response.getOutputStream(),
                            Map.of("message", "You do not have permission to perform this action"));
                }))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}