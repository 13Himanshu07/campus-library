package com.library.service;

import com.library.entity.User;
import com.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapConfig {
 @Bean CommandLineRunner bootstrapAdmin(UserRepository users,PasswordEncoder encoder,@Value("${app.bootstrap-admin-email:}") String email,@Value("${app.bootstrap-admin-password:}") String password){return args->{if(email==null||email.isBlank())return;if(password==null||password.length()<12)throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD must be set privately and contain at least 12 characters");var existing=users.findByEmailIgnoreCase(email).orElse(null);if(existing!=null){if(existing.getRole()!=User.Role.ADMIN)throw new IllegalStateException("BOOTSTRAP_ADMIN_EMAIL already belongs to a non-admin account");return;}var u=new User();u.setName("Library Administrator");u.setEmail(email.toLowerCase());u.setPassword(encoder.encode(password));u.setMembershipId("ADMIN-001");u.setRole(User.Role.ADMIN);users.save(u);};}
}
