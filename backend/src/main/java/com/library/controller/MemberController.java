package com.library.controller;

import com.library.entity.*;
import com.library.exception.ApiException;
import com.library.repository.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/members") @PreAuthorize("hasRole('LIBRARIAN')")
public class MemberController {
 private final UserRepository users;private final TransactionRepository tx;private final PreferenceRepository prefs;private final PasswordEncoder encoder;
 public MemberController(UserRepository u,TransactionRepository t,PreferenceRepository p,PasswordEncoder e){users=u;tx=t;prefs=p;encoder=e;}
 @GetMapping public List<Map<String,Object>> all(@RequestParam(defaultValue="") String q){return users.findByRole(User.Role.MEMBER).stream().filter(u->(u.getName()+u.getEmail()+u.getMembershipId()).toLowerCase().contains(q.toLowerCase())).map(this::dto).toList();}
 @GetMapping("/{id}") public Map<String,Object> one(@PathVariable Long id){return dto(member(id));}
 @GetMapping("/{id}/history") public List<LibraryTransaction> history(@PathVariable Long id){member(id);return tx.findByMemberIdOrderByCreatedAtDesc(id);}
 @PostMapping public Map<String,Object> create(@RequestBody Map<String,String> b){String email=b.getOrDefault("email","");if(users.existsByEmailIgnoreCase(email))throw new ApiException(HttpStatus.CONFLICT,"Email is already registered");var u=new User();u.setName(b.getOrDefault("name",""));u.setEmail(email);u.setPhone(b.get("phone"));u.setMembershipId(b.getOrDefault("membershipId",UUID.randomUUID().toString().substring(0,8)));u.setPassword(encoder.encode(b.getOrDefault("password",UUID.randomUUID().toString()+"Aa1!")));u.setRole(User.Role.MEMBER);users.save(u);var p=new NotificationPreference();p.setUser(u);prefs.save(p);return dto(u);}
 @PutMapping("/{id}") public Map<String,Object> update(@PathVariable Long id,@RequestBody Map<String,String> b){var u=member(id);if(b.containsKey("name"))u.setName(b.get("name"));if(b.containsKey("phone"))u.setPhone(b.get("phone"));if(b.containsKey("email")){if(users.existsByEmailIgnoreCase(b.get("email"))&&!u.getEmail().equalsIgnoreCase(b.get("email")))throw new ApiException(HttpStatus.CONFLICT,"Email is already registered");u.setEmail(b.get("email"));}return dto(u);}
 @DeleteMapping("/{id}") public Map<String,String> disable(@PathVariable Long id){var u=member(id);u.setStatus(User.UserStatus.DISABLED);return Map.of("message","Member disabled");}
 private User member(Long id){return users.findById(id).filter(u->u.getRole()==User.Role.MEMBER).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Member not found"));}
 private Map<String,Object> dto(User u){return Map.of("id",u.getId(),"name",u.getName(),"email",u.getEmail(),"membershipId",Objects.toString(u.getMembershipId(),""),"status",u.getStatus().name(),"phone",Objects.toString(u.getPhone(),""),"joined",u.getCreatedAt(),"booksBorrowed",tx.countByMemberIdAndStatusIn(u.getId(),List.of(LibraryTransaction.Status.BORROWED,LibraryTransaction.Status.OVERDUE)));}
}
