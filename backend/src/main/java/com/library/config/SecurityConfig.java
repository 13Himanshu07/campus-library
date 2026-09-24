package com.library.config;

import com.library.security.JwtFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;

@Configuration @EnableMethodSecurity
public class SecurityConfig {
 @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl;
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean SecurityFilterChain filterChain(HttpSecurity http,JwtFilter jwt)throws Exception {
  return http.csrf(c->c.disable()).cors(c->c.configurationSource(cors())).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(a->a
    .requestMatchers("/api/auth/**","/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**","/actuator/health").permitAll()
    .requestMatchers(HttpMethod.GET,"/api/books/**").authenticated()
    .requestMatchers("/api/books/**","/api/members/**","/api/reports/**").hasRole("LIBRARIAN")
    .requestMatchers(HttpMethod.POST,"/api/transactions/borrow").hasRole("MEMBER")
    .requestMatchers(HttpMethod.POST,"/api/transactions/*/return").hasRole("LIBRARIAN")
    .anyRequest().authenticated()).addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();
 }
 @Bean CorsConfigurationSource cors(){var c=new CorsConfiguration();c.setAllowedOrigins(List.of(frontendUrl));c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("Authorization","Content-Type"));c.setAllowCredentials(true);var s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;}
}
