package com.library.service;

import com.library.entity.*;
import com.library.exception.ApiException;
import com.library.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class AdminService {
 private final UserRepository users;
 private final TransactionRepository transactions;
 private final BookRepository books;
 private final NotificationRepository notifications;
 private final PreferenceRepository preferences;
 private final SearchHistoryRepository searches;
 private final PasswordResetRepository resets;
 private final CurrentUser current;

 public AdminService(UserRepository users,TransactionRepository transactions,BookRepository books,NotificationRepository notifications,PreferenceRepository preferences,SearchHistoryRepository searches,PasswordResetRepository resets,CurrentUser current){this.users=users;this.transactions=transactions;this.books=books;this.notifications=notifications;this.preferences=preferences;this.searches=searches;this.resets=resets;this.current=current;}

 @Transactional(readOnly=true)
 public List<Map<String,Object>> pendingLibrarians(){return users.findByRoleAndStatus(User.Role.LIBRARIAN,User.UserStatus.PENDING).stream().map(this::safe).toList();}

 @Transactional
 public Map<String,Object> approveLibrarian(Long id){var u=users.findById(id).filter(x->x.getRole()==User.Role.LIBRARIAN&&x.getStatus()==User.UserStatus.PENDING).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Pending librarian application not found"));u.setStatus(User.UserStatus.ACTIVE);return safe(u);}

 @Transactional
 public Map<String,Object> rejectLibrarian(Long id){var u=users.findById(id).filter(x->x.getRole()==User.Role.LIBRARIAN&&x.getStatus()==User.UserStatus.PENDING).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Pending librarian application not found"));u.setStatus(User.UserStatus.DISABLED);return safe(u);}

 @Transactional(readOnly=true)
 public Map<String,Long> userCounts(){var admin=current.get();long members=users.findByRole(User.Role.MEMBER).size();long librarians=users.findByRole(User.Role.LIBRARIAN).size();long otherAdmins=users.findByRole(User.Role.ADMIN).stream().filter(u->!u.getId().equals(admin.getId())).count();return Map.of("members",members,"librarians",librarians,"otherAdmins",otherAdmins,"total",members+librarians+otherAdmins);}

 @Transactional
 public Map<String,Integer> clearRegisteredUsers(){var admin=current.get();var toRemove=users.findAll().stream().filter(u->!u.getId().equals(admin.getId())).toList();
  for(var user:toRemove){var history=transactions.findByMemberIdOrderByCreatedAtDesc(user.getId());for(var transaction:history){if(transaction.getStatus()==LibraryTransaction.Status.BORROWED||transaction.getStatus()==LibraryTransaction.Status.OVERDUE){var book=transaction.getBook();book.setAvailableCopies(Math.min(book.getTotalCopies(),book.getAvailableCopies()+1));books.save(book);}}}
  for(var user:toRemove){transactions.deleteByMemberId(user.getId());notifications.deleteByUserId(user.getId());preferences.deleteByUserId(user.getId());searches.deleteByUserId(user.getId());resets.deleteByUserId(user.getId());}
  users.deleteAll(toRemove);
  return Map.of("deleted",toRemove.size());
 }

 private Map<String,Object> safe(User u){return Map.of("id",u.getId(),"name",u.getName(),"email",u.getEmail(),"membershipId",Objects.toString(u.getMembershipId(),""),"status",u.getStatus().name(),"createdAt",u.getCreatedAt());}
}
