package com.library.dao.impl;

import com.library.dao.IssueDAO;
import com.library.exception.*;
import com.library.model.BookIssue;
import com.library.util.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class IssueDAOImpl implements IssueDAO {
    @Override public long issue(long bookId,long userId,int dueDays,int borrowLimit){
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try {
                try(PreparedStatement p=c.prepareStatement("SELECT role,status FROM users WHERE id=? FOR UPDATE")){p.setLong(1,userId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new UserNotFoundException("Student account was not found.");if(!"STUDENT".equals(r.getString("role"))||!"ACTIVE".equals(r.getString("status")))throw new ValidationException("Only active student accounts can borrow books.");}}
                int available;
                try(PreparedStatement p=c.prepareStatement("SELECT available_copies FROM books WHERE id=? AND archived=FALSE FOR UPDATE")){p.setLong(1,bookId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new BookNotAvailableException("That book is not available in the catalog.");available=r.getInt(1);}}
                if(available<1)throw new BookNotAvailableException("No copies of this title are currently available.");
                try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM book_issues WHERE user_id=? AND status IN ('ISSUED','OVERDUE')")){p.setLong(1,userId);try(ResultSet r=p.executeQuery()){r.next();if(r.getInt(1)>=borrowLimit)throw new ValidationException("Borrowing limit reached.");}}
                try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM book_issues WHERE user_id=? AND book_id=? AND status IN ('ISSUED','OVERDUE')")){p.setLong(1,userId);p.setLong(2,bookId);try(ResultSet r=p.executeQuery()){r.next();if(r.getInt(1)>0)throw new ValidationException("You already have this book on loan.");}}
                long issueId;try(PreparedStatement p=c.prepareStatement("INSERT INTO book_issues(book_id,user_id,issue_date,due_date,status) VALUES(?,?,?,?,'ISSUED')",Statement.RETURN_GENERATED_KEYS)){p.setLong(1,bookId);p.setLong(2,userId);p.setDate(3,java.sql.Date.valueOf(LocalDate.now()));p.setDate(4,java.sql.Date.valueOf(LocalDate.now().plusDays(dueDays)));p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("No issue id returned");issueId=r.getLong(1);}}
                try(PreparedStatement p=c.prepareStatement("UPDATE books SET available_copies=available_copies-1 WHERE id=? AND available_copies>0")){p.setLong(1,bookId);if(p.executeUpdate()!=1)throw new SQLException("Copy count changed during checkout");}
                c.commit();return issueId;
            } catch(RuntimeException|SQLException e){rollback(c,e);throw e;}
        }catch(BookNotAvailableException|ValidationException|UserNotFoundException e){throw e;}catch(SQLException e){throw new DatabaseException("Could not issue the book; the database transaction was rolled back.",e);}
    }
    @Override public BookIssue findById(long id){return query(select()+" WHERE i.id=?",id).stream().findFirst().orElseThrow(()->new IssueNotFoundException("Loan record was not found."));}
    @Override public BookIssue returnBook(long issueId,BigDecimal fine){
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try {
                long bookId,userId;LocalDate due;String status;
                try(PreparedStatement p=c.prepareStatement("SELECT book_id,user_id,due_date,status FROM book_issues WHERE id=? FOR UPDATE")){p.setLong(1,issueId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IssueNotFoundException("Loan record was not found.");bookId=r.getLong(1);userId=r.getLong(2);due=r.getDate(3).toLocalDate();status=r.getString(4);}}
                if("RETURNED".equals(status))throw new ValidationException("This book has already been returned.");
                LocalDate today=LocalDate.now();
                try(PreparedStatement p=c.prepareStatement("UPDATE book_issues SET return_date=?,status='RETURNED' WHERE id=?")){p.setDate(1,java.sql.Date.valueOf(today));p.setLong(2,issueId);p.executeUpdate();}
                try(PreparedStatement p=c.prepareStatement("UPDATE books SET available_copies=available_copies+1 WHERE id=? AND available_copies<total_copies")){p.setLong(1,bookId);if(p.executeUpdate()!=1)throw new SQLException("Book inventory would exceed total copies");}
                if(fine.signum()>0)try(PreparedStatement p=c.prepareStatement("INSERT INTO fines(issue_id,amount) VALUES(?,?) ON DUPLICATE KEY UPDATE amount=VALUES(amount)")){p.setLong(1,issueId);p.setBigDecimal(2,fine);p.executeUpdate();}
                c.commit();return findIssue(c,issueId);
            }catch(RuntimeException|SQLException e){rollback(c,e);throw e;}
        }catch(IssueNotFoundException|ValidationException e){throw e;}catch(SQLException e){throw new DatabaseException("Could not return the book; the database transaction was rolled back.",e);}
    }
    private void rollback(Connection c,Exception original){try{c.rollback();}catch(SQLException rollback){original.addSuppressed(rollback);}}
    private BookIssue findIssue(Connection c,long id)throws SQLException{try(PreparedStatement p=c.prepareStatement(select()+" WHERE i.id=?")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){r.next();return map(r);}}}
    @Override public List<BookIssue> activeForUser(long id){return query(select()+" WHERE i.user_id=? AND i.return_date IS NULL ORDER BY i.due_date",id);}
    @Override public List<BookIssue> historyForUser(long id){return query(select()+" WHERE i.user_id=? ORDER BY i.created_at DESC",id);}
    @Override public List<BookIssue> all(){return query(select()+" ORDER BY i.created_at DESC",null);}
    @Override public List<BookIssue> overdue(){return query(select()+" WHERE i.return_date IS NULL AND i.due_date<CURRENT_DATE ORDER BY i.due_date",null);}
    private String select(){return "SELECT i.id,i.book_id,i.user_id,b.title book_title,b.author,u.name member_name,u.email member_email,i.issue_date,i.due_date,i.return_date,CASE WHEN i.return_date IS NULL AND i.due_date<CURRENT_DATE THEN 'OVERDUE' ELSE i.status END display_status,COALESCE(f.amount,0) fine_amount,COALESCE(f.paid,FALSE) fine_paid FROM book_issues i JOIN books b ON b.id=i.book_id JOIN users u ON u.id=i.user_id LEFT JOIN fines f ON f.issue_id=i.id";}
    private List<BookIssue> query(String sql,Long id){List<BookIssue> rows=new ArrayList<>();try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){if(id!=null)p.setLong(1,id);try(ResultSet r=p.executeQuery()){while(r.next())rows.add(map(r));}return rows;}catch(SQLException e){throw new DatabaseException("Could not load loan records.",e);}}
    private BookIssue map(ResultSet r)throws SQLException{java.sql.Date returned=r.getDate("return_date");return new BookIssue(r.getLong("id"),r.getLong("book_id"),r.getLong("user_id"),r.getString("book_title"),r.getString("author"),r.getString("member_name"),r.getString("member_email"),r.getDate("issue_date").toLocalDate(),r.getDate("due_date").toLocalDate(),returned==null?null:returned.toLocalDate(),BookIssue.IssueStatus.valueOf(r.getString("display_status")),r.getBigDecimal("fine_amount"),r.getBoolean("fine_paid"));}
}
