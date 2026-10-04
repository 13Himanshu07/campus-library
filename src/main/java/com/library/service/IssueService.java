package com.library.service;
import com.library.model.BookIssue;
import java.util.List;
public interface IssueService { long issue(long bookId,long userId); BookIssue returnBook(long issueId); List<BookIssue> activeFor(long userId); List<BookIssue> historyFor(long userId); List<BookIssue> all(); List<BookIssue> overdue(); }
