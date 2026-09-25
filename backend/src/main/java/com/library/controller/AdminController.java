package com.library.controller;

import com.library.service.AdminService;
import com.library.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
 private final AdminService service;
 public AdminController(AdminService service){this.service=service;}

 @GetMapping("/librarian-applications")
 public List<Map<String,Object>> pendingLibrarians(){return service.pendingLibrarians();}

 @PostMapping("/librarian-applications/{id}/approve")
 public Map<String,Object> approve(@PathVariable Long id){return service.approveLibrarian(id);}

 @PostMapping("/librarian-applications/{id}/reject")
 public Map<String,Object> reject(@PathVariable Long id){return service.rejectLibrarian(id);}

 @GetMapping("/users/count")
 public Map<String,Long> userCounts(){return service.userCounts();}

 @DeleteMapping("/users")
 public Map<String,Integer> clearUsers(@RequestBody Map<String,String> body){if(!"DELETE ALL USERS".equals(body.get("confirmation")))throw new ApiException(HttpStatus.BAD_REQUEST,"Type DELETE ALL USERS to confirm");return service.clearRegisteredUsers();}
}
