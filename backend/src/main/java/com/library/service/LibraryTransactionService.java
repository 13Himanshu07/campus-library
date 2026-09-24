package com.library.service;

import com.library.entity.*;
import com.library.exception.ApiException;
import com.library.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;

@Service
public class LibraryTransactionService {
 private static final List<LibraryTransaction.Status> ACTIVE=List.of(LibraryTransaction.Status.BORROWED,LibraryTransaction.Status.OVERDUE);
 private final TransactionRepository transactions;private final BookRepository books;private final CurrentUser current;private final NotificationService notifications;private final MailService mail;private final int borrowDays,limit;private final BigDecimal fine;
 public LibraryTransactionService(TransactionRepository t,BookRepository b,CurrentUser c,NotificationService n,MailService m,@Value("${app.borrow-days:14}") int d,@Value("${app.borrow-limit:5}") int l,@Value("${app.fine-per-day:5}") BigDecimal f){transactions=t;books=b;current=c;notifications=n;mail=m;borrowDays=d;limit=l;fine=f;}
 @Transactional public LibraryTransaction borrow(Long bookId){var member=current.get();var book=books.findLockedById(bookId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Book not found"));if(book.getAvailableCopies()<1)throw new ApiException(HttpStatus.CONFLICT,"Book is currently unavailable");if(transactions.existsByBookIdAndMemberIdAndStatusIn(book.getId(),member.getId(),ACTIVE))throw new ApiException(HttpStatus.CONFLICT,"You already have this book borrowed");if(transactions.countByMemberIdAndStatusIn(member.getId(),ACTIVE)>=limit)throw new ApiException(HttpStatus.CONFLICT,"Borrowing limit reached");book.setAvailableCopies(book.getAvailableCopies()-1);var t=new LibraryTransaction();t.setBook(book);t.setMember(member);t.setBorrowDate(LocalDate.now());t.setDueDate(LocalDate.now().plusDays(borrowDays));t.setStatus(LibraryTransaction.Status.BORROWED);transactions.save(t);notifications.create(member,"Book borrowed",book.getTitle()+" is due on "+t.getDueDate()+".",Notification.Type.SYSTEM,true);mail.send(member.getEmail(),"Book borrowed successfully","Book: "+book.getTitle()+"\nBorrow date: "+t.getBorrowDate()+"\nDue date: "+t.getDueDate());return t;}
 @Transactional public LibraryTransaction giveBack(Long id){var member=current.get();var t=transactions.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Transaction not found"));if(!t.getMember().getId().equals(member.getId()))throw new ApiException(HttpStatus.FORBIDDEN,"This transaction belongs to another member");if(t.getStatus()==LibraryTransaction.Status.RETURNED)throw new ApiException(HttpStatus.CONFLICT,"Book has already been returned");LocalDate today=LocalDate.now();long late=Math.max(0,Duration.between(t.getDueDate().atStartOfDay(),today.atStartOfDay()).toDays());t.setReturnDate(today);t.setStatus(LibraryTransaction.Status.RETURNED);t.setFineAmount(fine.multiply(BigDecimal.valueOf(late)));t.getBook().setAvailableCopies(t.getBook().getAvailableCopies()+1);notifications.create(member,"Book returned",t.getBook().getTitle()+" was returned. Fine: ₹"+t.getFineAmount(),Notification.Type.SYSTEM,false);mail.send(member.getEmail(),"Book return confirmed",t.getBook().getTitle()+" was returned on "+today+". Fine: ₹"+t.getFineAmount());return t;}
 @Transactional(readOnly=true) public List<LibraryTransaction> history(){return transactions.findByMemberIdOrderByCreatedAtDesc(current.get().getId());}
 @Transactional(readOnly=true) public List<LibraryTransaction> all(){return transactions.findAll();}
}
