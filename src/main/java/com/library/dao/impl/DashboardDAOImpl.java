package com.library.dao.impl;

import com.library.dao.DashboardDAO;
import com.library.exception.DatabaseException;
import com.library.util.DBConnection;
import java.sql.*;
import java.util.*;

public class DashboardDAOImpl implements DashboardDAO {
    @Override public Map<String,Long> statistics(){String sql="SELECT (SELECT COUNT(*) FROM books WHERE archived=FALSE) titles,(SELECT COALESCE(SUM(total_copies),0) FROM books WHERE archived=FALSE) copies,(SELECT COALESCE(SUM(available_copies),0) FROM books WHERE archived=FALSE) available,(SELECT COUNT(*) FROM book_issues WHERE return_date IS NULL) open_issues,(SELECT COUNT(*) FROM book_issues WHERE return_date IS NULL AND due_date<CURRENT_DATE) overdue,(SELECT COUNT(*) FROM users WHERE role='STUDENT' AND status='ACTIVE') students,(SELECT COUNT(*) FROM users WHERE role='LIBRARIAN' AND status='PENDING') pending_librarians,(SELECT COALESCE(SUM(amount),0) FROM fines WHERE paid=FALSE) unpaid_fines";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){r.next();Map<String,Long> stats=new LinkedHashMap<>();stats.put("titles",r.getLong("titles"));stats.put("copies",r.getLong("copies"));stats.put("available",r.getLong("available"));stats.put("openIssues",r.getLong("open_issues"));stats.put("overdue",r.getLong("overdue"));stats.put("students",r.getLong("students"));stats.put("pendingLibrarians",r.getLong("pending_librarians"));stats.put("unpaidFinesCents",r.getBigDecimal("unpaid_fines").movePointRight(2).longValue());return stats;}catch(SQLException e){throw new DatabaseException("Could not load dashboard statistics.",e);}}
}
