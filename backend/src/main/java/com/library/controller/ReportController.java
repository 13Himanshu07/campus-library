package com.library.controller;

import com.library.entity.*;
import com.library.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.time.*;

@RestController @RequestMapping("/api/reports") @PreAuthorize("hasRole('LIBRARIAN')")
public class ReportController {
 private final BookRepository books;private final UserRepository users;private final TransactionRepository tx;
 public ReportController(BookRepository b,UserRepository u,TransactionRepository t){books=b;users=u;tx=t;}
 @GetMapping("/inventory") public Map<String,Object> inventory(){var all=books.findAll().stream().filter(b->!b.isArchived()).toList();int total=all.stream().mapToInt(Book::getTotalCopies).sum(),available=all.stream().mapToInt(Book::getAvailableCopies).sum();long overdue=tx.findOverdueOpen().size();return Map.of("totalBooks",all.size(),"totalCopies",total,"availableCopies",available,"borrowedCopies",total-available,"overdueCopies",overdue,"totalMembers",users.findByRole(User.Role.MEMBER).size(),"totalTransactions",tx.count());}
 @GetMapping("/borrowing") public Map<String,Object> borrowing(){var all=tx.findAll();Map<String,Long> popular=new LinkedHashMap<>(),active=new LinkedHashMap<>(),monthly=new TreeMap<>();for(var t:all){popular.merge(t.getBook().getTitle(),1L,Long::sum);active.merge(t.getMember().getName(),1L,Long::sum);monthly.merge(t.getBorrowDate().getYear()+"-"+String.format("%02d",t.getBorrowDate().getMonthValue()),1L,Long::sum);}return Map.of("popularBooks",popular,"activeMembers",active,"monthlyBorrowing",monthly,"returnCount",all.stream().filter(t->t.getStatus()==LibraryTransaction.Status.RETURNED).count(),"overdueCount",tx.findOverdueOpen().size());}
 @GetMapping("/popular-books") public Map<String,Long> popular(){return (Map<String,Long>)borrowing().get("popularBooks");}
 @GetMapping("/active-members") public Map<String,Long> active(){return (Map<String,Long>)borrowing().get("activeMembers");}
 @GetMapping("/dashboard") public Map<String,Object> dashboard(){var inv=inventory();var all=tx.findAll();long borrowed=all.stream().filter(t->t.getStatus()!=LibraryTransaction.Status.RETURNED).count();var trends=new TreeMap<String,Long>();all.forEach(t->trends.merge(t.getBorrowDate().toString(),1L,Long::sum));var recent=all.stream().sorted(Comparator.comparing(LibraryTransaction::getCreatedAt).reversed()).limit(8).toList();return Map.of("inventory",inv,"borrowedBooks",borrowed,"recentTransactions",recent,"borrowingTrends",trends);}
}
