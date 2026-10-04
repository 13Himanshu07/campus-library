package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookIssue {
    private long id,bookId,userId; private String bookTitle,author,memberName,memberEmail; private LocalDate issueDate,dueDate,returnDate; private IssueStatus status; private BigDecimal fineAmount=BigDecimal.ZERO; private boolean finePaid;
    public BookIssue(){ } public BookIssue(long id,long bookId,long userId,String bookTitle,String author,String memberName,String memberEmail,LocalDate issueDate,LocalDate dueDate,LocalDate returnDate,IssueStatus status,BigDecimal fineAmount,boolean finePaid){this.id=id;this.bookId=bookId;this.userId=userId;this.bookTitle=bookTitle;this.author=author;this.memberName=memberName;this.memberEmail=memberEmail;this.issueDate=issueDate;this.dueDate=dueDate;this.returnDate=returnDate;this.status=status;this.fineAmount=fineAmount==null?BigDecimal.ZERO:fineAmount;this.finePaid=finePaid;}
    public long getId(){return id;} public void setId(long v){id=v;} public long getBookId(){return bookId;} public void setBookId(long v){bookId=v;} public long getUserId(){return userId;} public void setUserId(long v){userId=v;}
    public String getBookTitle(){return bookTitle;} public void setBookTitle(String v){bookTitle=v;} public String getAuthor(){return author;} public void setAuthor(String v){author=v;} public String getMemberName(){return memberName;} public void setMemberName(String v){memberName=v;} public String getMemberEmail(){return memberEmail;} public void setMemberEmail(String v){memberEmail=v;}
    public LocalDate getIssueDate(){return issueDate;} public void setIssueDate(LocalDate v){issueDate=v;} public LocalDate getDueDate(){return dueDate;} public void setDueDate(LocalDate v){dueDate=v;} public LocalDate getReturnDate(){return returnDate;} public void setReturnDate(LocalDate v){returnDate=v;} public IssueStatus getStatus(){return status;} public void setStatus(IssueStatus v){status=v;} public BigDecimal getFineAmount(){return fineAmount;} public void setFineAmount(BigDecimal v){fineAmount=v;} public boolean isFinePaid(){return finePaid;} public void setFinePaid(boolean v){finePaid=v;}
    public enum IssueStatus { ISSUED, RETURNED, OVERDUE }
}
