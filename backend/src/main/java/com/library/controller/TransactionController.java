package com.library.controller;

import com.library.entity.LibraryTransaction;
import com.library.service.LibraryTransactionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/transactions")
public class TransactionController {
 private final LibraryTransactionService transactions;
 public TransactionController(LibraryTransactionService transactions){this.transactions=transactions;}
 @PostMapping("/borrow") @PreAuthorize("hasRole('MEMBER')") public LibraryTransaction borrow(@RequestBody Map<String,Long> body){return transactions.borrow(body.get("bookId"));}
 @PostMapping("/{id}/return") @PreAuthorize("hasRole('LIBRARIAN')") public LibraryTransaction giveBack(@PathVariable Long id){return transactions.giveBack(id);}
 @GetMapping("/my-history") @PreAuthorize("hasRole('MEMBER')") public List<LibraryTransaction> mine(){return transactions.history();}
 @GetMapping @PreAuthorize("hasRole('LIBRARIAN')") public List<LibraryTransaction> all(){return transactions.all();}
}
