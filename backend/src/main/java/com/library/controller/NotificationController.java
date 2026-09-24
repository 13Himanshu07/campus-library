package com.library.controller;

import com.library.entity.*;
import com.library.repository.*;
import com.library.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/notifications")
public class NotificationController {
 private final NotificationService service;private final CurrentUser current;private final UserRepository users;private final PreferenceRepository prefs;
 public NotificationController(NotificationService s,CurrentUser c,UserRepository u,PreferenceRepository p){service=s;current=c;users=u;prefs=p;}
 @GetMapping public Map<String,Object> list(){var u=current.get();return Map.of("items",service.list(u.getId()),"unreadCount",service.unread(u.getId()));}
 @PutMapping("/{id}/read") public Map<String,String> read(@PathVariable Long id){service.markRead(id,current.get().getId());return Map.of("message","Marked as read");}
 @PutMapping("/read-all") public Map<String,String> readAll(){service.markAll(current.get().getId());return Map.of("message","All notifications marked as read");}
 @DeleteMapping("/{id}") public Map<String,String> delete(@PathVariable Long id){service.delete(id,current.get().getId());return Map.of("message","Notification deleted");}
 @PostMapping("/send") @PreAuthorize("hasRole('LIBRARIAN')") public Map<String,Integer> send(@RequestBody Map<String,Object> body){String title=Objects.toString(body.get("title"),"");String message=Objects.toString(body.get("message"),"");Notification.Type parsedType;try{parsedType=Notification.Type.valueOf(Objects.toString(body.get("type"),"SYSTEM"));}catch(Exception e){parsedType=Notification.Type.SYSTEM;}final Notification.Type type=parsedType;Object ids=body.get("recipientIds");List<User> targets;if(ids instanceof List<?> list&&!list.isEmpty()){var parsed=list.stream().map(x->Long.valueOf(x.toString())).toList();targets=users.findAllById(parsed);}else targets=users.findByRole(User.Role.MEMBER);targets.forEach(u->service.create(u,title,message,type,true));return Map.of("sent",targets.size());}
 @GetMapping("/preferences") public NotificationPreference preferences(){var u=current.get();return prefs.findByUserId(u.getId()).orElseGet(()->{var p=new NotificationPreference();p.setUser(u);return prefs.save(p);});}
 @PutMapping("/preferences") public NotificationPreference updatePreferences(@RequestBody NotificationPreference input){var u=current.get();var p=prefs.findByUserId(u.getId()).orElseGet(()->{var n=new NotificationPreference();n.setUser(u);return n;});p.setEmailNotifications(input.isEmailNotifications());p.setDueDateNotifications(input.isDueDateNotifications());p.setNewBookNotifications(input.isNewBookNotifications());p.setOverdueNotifications(input.isOverdueNotifications());return prefs.save(p);}
}
