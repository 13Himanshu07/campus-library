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
 @Bean CommandLineRunner bootstrapLibrarian(UserRepository users,PasswordEncoder encoder,@Value("${app.bootstrap-librarian-email:}") String email,@Value("${app.bootstrap-librarian-password:}") String password){return args->{if(email!=null&&!email.isBlank()&&password!=null&&password.length()>=12&&!users.existsByEmailIgnoreCase(email)){var u=new User();u.setName("Library Administrator");u.setEmail(email);u.setPassword(encoder.encode(password));u.setMembershipId("LIBRARIAN-001");u.setRole(User.Role.LIBRARIAN);users.save(u);}};}
}
