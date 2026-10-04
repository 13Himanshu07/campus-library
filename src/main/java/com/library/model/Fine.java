package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Fine {
    private long id,issueId,userId; private String memberName,bookTitle; private BigDecimal amount; private boolean paid; private LocalDateTime createdAt,paidAt;
    public Fine(){ } public Fine(long id,long issueId,long userId,String memberName,String bookTitle,BigDecimal amount,boolean paid,LocalDateTime createdAt,LocalDateTime paidAt){this.id=id;this.issueId=issueId;this.userId=userId;this.memberName=memberName;this.bookTitle=bookTitle;this.amount=amount;this.paid=paid;this.createdAt=createdAt;this.paidAt=paidAt;}
    public long getId(){return id;} public void setId(long v){id=v;} public long getIssueId(){return issueId;} public void setIssueId(long v){issueId=v;} public long getUserId(){return userId;} public void setUserId(long v){userId=v;} public String getMemberName(){return memberName;} public void setMemberName(String v){memberName=v;} public String getBookTitle(){return bookTitle;} public void setBookTitle(String v){bookTitle=v;} public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;} public boolean isPaid(){return paid;} public void setPaid(boolean v){paid=v;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;} public LocalDateTime getPaidAt(){return paidAt;} public void setPaidAt(LocalDateTime v){paidAt=v;}
}
