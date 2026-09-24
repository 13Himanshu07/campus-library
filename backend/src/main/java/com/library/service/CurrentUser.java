package com.library.service;
import com.library.entity.User;
import com.library.exception.ApiException;
import com.library.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
@Component
public class CurrentUser {
 private final UserRepository users;
 public CurrentUser(UserRepository users){this.users=users;}
 public User get(){String email=SecurityContextHolder.getContext().getAuthentication().getName();return users.findByEmailIgnoreCase(email).orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"Authentication required"));}
}
