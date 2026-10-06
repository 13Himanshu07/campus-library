package com.library.dao.impl;

import com.library.dao.FineDAO;
import com.library.exception.DatabaseException;
import com.library.model.Fine;
import com.library.util.DBConnection;
import java.sql.*;
import java.util.*;

public class FineDAOImpl implements FineDAO {
    private static final String SELECT="SELECT f.id,f.issue_id,i.user_id,u.name member_name,b.title book_title,f.amount,f.paid,f.created_at,f.paid_at FROM fines f JOIN book_issues i ON i.id=f.issue_id JOIN users u ON u.id=i.user_id JOIN books b ON b.id=i.book_id";
    @Override public List<Fine> findAll(){return query(SELECT+" ORDER BY f.paid,f.created_at DESC",null);}
    @Override public List<Fine> findForUser(long id){return query(SELECT+" WHERE i.user_id=? ORDER BY f.created_at DESC",id);}
    private List<Fine> query(String sql,Long userId){List<Fine> rows=new ArrayList<>();try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){if(userId!=null)p.setLong(1,userId);try(ResultSet r=p.executeQuery()){while(r.next()){Timestamp created=r.getTimestamp("created_at"),paid=r.getTimestamp("paid_at");rows.add(new Fine(r.getLong("id"),r.getLong("issue_id"),r.getLong("user_id"),r.getString("member_name"),r.getString("book_title"),r.getBigDecimal("amount"),r.getBoolean("paid"),created==null?null:created.toLocalDateTime(),paid==null?null:paid.toLocalDateTime()));}}return rows;}catch(SQLException e){throw new DatabaseException("Could not load fines.",e);}}
    @Override public void markPaid(long id){try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE fines SET paid=TRUE,paid_at=CURRENT_TIMESTAMP WHERE id=? AND paid=FALSE")){p.setLong(1,id);if(p.executeUpdate()==0)throw new IllegalArgumentException("Fine was not found or is already paid.");}catch(SQLException e){throw new DatabaseException("Could not record fine payment.",e);}}
    @Override public void accrueOverdue(long issueId,java.math.BigDecimal amount){String sql="INSERT INTO fines(issue_id,amount) SELECT id,? FROM book_issues WHERE id=? AND return_date IS NULL AND due_date<CURRENT_DATE ON DUPLICATE KEY UPDATE amount=IF(paid,amount,VALUES(amount))";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setBigDecimal(1,amount);p.setLong(2,issueId);p.executeUpdate();}catch(SQLException e){throw new DatabaseException("Could not update an overdue fine.",e);}}
}
