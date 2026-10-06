package com.library.service.impl;

import com.library.dao.IssueDAO;
import com.library.exception.BookNotAvailableException;
import com.library.exception.ValidationException;
import com.library.model.BookIssue;
import com.library.service.IssueService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.library.service.BookService;

public class IssueServiceImpl implements IssueService {
    private static final Object[] CIRCULATION_LOCKS=new Object[64];
    static { for(int i=0;i<CIRCULATION_LOCKS.length;i++)CIRCULATION_LOCKS[i]=new Object(); }
    private final IssueDAO issues;private final BookService books;private final int dueDays,borrowLimit;private final BigDecimal finePerDay;
    public IssueServiceImpl(IssueDAO issues,BookService books,int dueDays,int borrowLimit,BigDecimal finePerDay){this.issues=issues;this.books=books;this.dueDays=dueDays;this.borrowLimit=borrowLimit;this.finePerDay=finePerDay;if(issues==null||books==null||finePerDay==null||dueDays<1||borrowLimit<1||finePerDay.signum()<0)throw new IllegalArgumentException("Loan settings and dependencies must be valid (fine may be zero).");}
    @Override public long issue(long bookId,long userId){synchronized(lockFor(bookId)){if(books.get(bookId).getAvailableCopies()<1)throw new BookNotAvailableException("No copies of this title are currently available.");List<BookIssue> active=issues.activeForUser(userId);if(active.size()>=borrowLimit)throw new ValidationException("Borrowing limit reached.");if(active.stream().anyMatch(i->i.getBookId()==bookId))throw new ValidationException("You already have this book on loan.");return issues.issue(bookId,userId,dueDays,borrowLimit);}}
    @Override public BookIssue returnBook(long issueId){BookIssue issue=issues.findById(issueId);synchronized(lockFor(issue.getBookId())){if(issue.getStatus()==BookIssue.IssueStatus.RETURNED)throw new ValidationException("This book has already been returned.");long lateDays=Math.max(0,LocalDate.now().toEpochDay()-issue.getDueDate().toEpochDay());BigDecimal fine=finePerDay.multiply(BigDecimal.valueOf(lateDays));return issues.returnBook(issueId,fine);}}
    private Object lockFor(long bookId){return CIRCULATION_LOCKS[Math.floorMod(Long.hashCode(bookId),CIRCULATION_LOCKS.length)];}
    @Override public List<BookIssue> activeFor(long userId){return issues.activeForUser(userId);}
    @Override public List<BookIssue> historyFor(long userId){return issues.historyForUser(userId);}
    @Override public List<BookIssue> all(){return issues.all();}
    @Override public List<BookIssue> overdue(){return issues.overdue();}
}
