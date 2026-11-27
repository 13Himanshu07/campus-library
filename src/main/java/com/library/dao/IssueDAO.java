package com.library.dao;
import com.library.model.BookIssue;
import java.util.List;
public interface IssueDAO { long issue(long bookId,long userId,int dueDays,int borrowLimit); BookIssue findById(long issueId); BookIssue returnBook(long issueId,java.math.BigDecimal fineAmount); default BookIssue returnBook(long issueId,java.math.BigDecimal fineAmount,Long actorUserId){return returnBook(issueId,fineAmount);} List<BookIssue> activeForUser(long userId); List<BookIssue> historyForUser(long userId); List<BookIssue> all(); List<BookIssue> overdue(); }

