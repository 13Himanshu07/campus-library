package com.library.service;

import com.library.entity.*;
import com.library.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @Transactional
public class NotificationService {
 private final NotificationRepository notifications; private final PreferenceRepository preferences; private final MailService mail;
 public NotificationService(NotificationRepository n,PreferenceRepository p,MailService m){notifications=n;preferences=p;mail=m;}
 public Notification create(User user,String title,String message,Notification.Type type,boolean email){if(notifications.existsByUserIdAndTypeAndTitleAndMessage(user.getId(),type,title,message))return notifications.findTop100ByUserIdOrderByCreatedAtDesc(user.getId()).stream().filter(n->n.getTitle().equals(title)&&n.getMessage().equals(message)&&n.getType()==type).findFirst().orElse(null);var n=new Notification();n.setUser(user);n.setTitle(title);n.setMessage(message);n.setType(type);notifications.save(n);var pref=preferences.findByUserId(user.getId()).orElse(null);if(email&&pref!=null&&pref.isEmailNotifications()&&allowed(pref,type))mail.send(user.getEmail(),title,message);return n;}
 private boolean allowed(NotificationPreference p,Notification.Type t){return switch(t){case DUE_DATE->p.isDueDateNotifications();case NEW_BOOK->p.isNewBookNotifications();case OVERDUE->p.isOverdueNotifications();default->true;};}
 public List<Notification> list(Long user){return notifications.findTop100ByUserIdOrderByCreatedAtDesc(user);}
 public long unread(Long user){return notifications.countByUserIdAndReadFalse(user);}
 public void markRead(Long id,Long user){var n=notifications.findByIdAndUserId(id,user).orElseThrow(()->new com.library.exception.ApiException(org.springframework.http.HttpStatus.NOT_FOUND,"Notification not found"));n.setRead(true);}
 public void markAll(Long user){list(user).forEach(n->n.setRead(true));}
 public void delete(Long id,Long user){var n=notifications.findByIdAndUserId(id,user).orElseThrow(()->new com.library.exception.ApiException(org.springframework.http.HttpStatus.NOT_FOUND,"Notification not found"));notifications.delete(n);}
}
