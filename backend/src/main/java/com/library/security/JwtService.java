package com.library.security;

import com.library.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
 @Value("${app.jwt-secret:local-only-change-this-secret-before-deployment-123456789}") private String secret;
 @Value("${app.jwt-expiration-ms:86400000}") private long expiration;
 private SecretKey key(){ return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }
 public String create(User user){ return Jwts.builder().subject(user.getEmail()).claim("role",user.getRole().name()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+expiration)).signWith(key()).compact(); }
 public String subject(String token){ return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload().getSubject(); }
}
