package com.library.controller;

import com.library.entity.*;
import com.library.exception.ApiException;
import com.library.repository.*;
import com.library.security.JwtService;
import com.library.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.time.Instant;
import java.security.*;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final UserRepository users; private final PreferenceRepository prefs; private final PasswordEncoder encoder; private final JwtService jwt; private final CurrentUser current; private final MailService mail; private final PasswordResetRepository resets; private final String frontendUrl;
 public AuthController(UserRepository u,PreferenceRepository p,PasswordEncoder e,JwtService j,CurrentUser c,MailService m,PasswordResetRepository r,@Value("${app.frontend-url:http://localhost:5173}") String f){users=u;prefs=p;encoder=e;jwt=j;current=c;mail=m;resets=r;frontendUrl=f;}
 public record Register(@NotBlank String name,@Email @NotBlank String email,@NotBlank @Size(min=10) String password,@NotBlank String phone,@NotBlank String membershipId){}
 public record Login(@Email @NotBlank String email,@NotBlank String password){}
 @PostMapping("/register") public Map<String,Object> register(@Valid @RequestBody Register r){if(users.existsByEmailIgnoreCase(r.email()))throw new ApiException(HttpStatus.CONFLICT,"Email is already registered");if(users.existsByMembershipId(r.membershipId()))throw new ApiException(HttpStatus.CONFLICT,"Membership ID is already registered");var u=new User();u.setName(r.name());u.setEmail(r.email().toLowerCase());u.setPassword(encoder.encode(r.password()));u.setPhone(r.phone());u.setMembershipId(r.membershipId());u.setRole(User.Role.MEMBER);users.save(u);var p=new NotificationPreference();p.setUser(u);prefs.save(p);mail.send(u.getEmail(),"Library account created","Welcome to the library, "+u.getName()+".");return Map.of("success",true,"message","Account created","user",safe(u));}
 @PostMapping("/login") public Map<String,Object> login(@Valid @RequestBody Login r){var u=users.findByEmailIgnoreCase(r.email()).orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"Invalid email or password"));if(!encoder.matches(r.password(),u.getPassword())||u.getStatus()!=User.UserStatus.ACTIVE)throw new ApiException(HttpStatus.UNAUTHORIZED,"Invalid email or password");return Map.of("token",jwt.create(u),"user",safe(u));}
 @PostMapping("/forgot-password") @Transactional public Map<String,String> forgot(@RequestBody Map<String,String> body){String email=body.getOrDefault("email","");users.findByEmailIgnoreCase(email).ifPresent(u->{try{resets.deleteByUserId(u.getId());String raw=UUID.randomUUID()+"."+UUID.randomUUID();var token=new PasswordResetToken();token.setUser(u);token.setTokenHash(hash(raw));token.setExpiresAt(Instant.now().plusSeconds(3600));resets.save(token);mail.send(u.getEmail(),"Reset your library password","Reset your password within one hour: "+frontendUrl+"/reset-password?token="+raw);}catch(Exception ignored){}});return Map.of("message","If an account matches that email, password reset instructions will be sent.");}
 @PostMapping("/reset-password") @Transactional public Map<String,String> reset(@RequestBody Map<String,String> body){String raw=body.getOrDefault("token","");String password=body.getOrDefault("password","");if(password.length()<10)throw new ApiException(HttpStatus.BAD_REQUEST,"Password must be at least 10 characters");var t=resets.findByTokenHash(hash(raw)).filter(x->x.getUsedAt()==null&&x.getExpiresAt().isAfter(Instant.now())).orElseThrow(()->new ApiException(HttpStatus.BAD_REQUEST,"Reset link is invalid or expired"));t.getUser().setPassword(encoder.encode(password));t.setUsedAt(Instant.now());return Map.of("message","Password reset successfully. You can now sign in.");}
 private String hash(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
 @GetMapping("/me") public Map<String,Object> me(){return safe(current.get());}
 @PutMapping("/profile") public Map<String,Object> profile(@RequestBody Map<String,String> body){var u=current.get();String email=body.get("email");if(email!=null&&!email.equalsIgnoreCase(u.getEmail())&&users.existsByEmailIgnoreCase(email))throw new ApiException(HttpStatus.CONFLICT,"Email is already in use");if(body.containsKey("name"))u.setName(body.get("name"));if(body.containsKey("phone"))u.setPhone(body.get("phone"));if(email!=null)u.setEmail(email);return safe(u);}
 @PutMapping("/password") public Map<String,String> password(@RequestBody Map<String,String> b){var u=current.get();if(!encoder.matches(b.getOrDefault("currentPassword",""),u.getPassword()))throw new ApiException(HttpStatus.BAD_REQUEST,"Current password is incorrect");String n=b.getOrDefault("newPassword","");if(n.length()<10)throw new ApiException(HttpStatus.BAD_REQUEST,"New password must be at least 10 characters");if(!n.equals(b.get("confirmPassword")))throw new ApiException(HttpStatus.BAD_REQUEST,"Passwords do not match");u.setPassword(encoder.encode(n));return Map.of("message","Password updated");}
 private Map<String,Object> safe(User u){return Map.of("id",u.getId(),"name",u.getName(),"email",u.getEmail(),"phone",Objects.toString(u.getPhone(),""),"membershipId",Objects.toString(u.getMembershipId(),""),"role",u.getRole().name(),"status",u.getStatus().name());}
}
