package com.library.dao.impl;

import com.library.dao.BookDAO;
import com.library.exception.DatabaseException;
import com.library.model.Book;
import com.library.util.DBConnection;
import java.sql.*;
import java.util.*;

public class BookDAOImpl implements BookDAO {
    private static final String SELECT="SELECT b.id,b.title,b.author,b.isbn,COALESCE(b.category_id,0) category_id,COALESCE(c.name,'Uncategorized') category_name,b.description,b.publisher,b.publication_year,b.total_copies,b.available_copies FROM books b LEFT JOIN categories c ON c.id=b.category_id WHERE b.archived=FALSE";
    @Override public Book create(Book b){String sql="INSERT INTO books(title,author,isbn,category_id,description,publisher,publication_year,total_copies,available_copies) VALUES(?,?,?,?,?,?,?,?,?)";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){bind(p,b);p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){if(r.next())b.setId(r.getLong(1));}return b;}catch(SQLException e){throw new DatabaseException("Could not add book. ISBN values must be unique.",e);}}
    @Override public Book update(Book b){String sql="UPDATE books SET title=?,author=?,isbn=?,category_id=?,description=?,publisher=?,publication_year=?,total_copies=?,available_copies=? WHERE id=? AND archived=FALSE";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){bind(p,b);p.setLong(10,b.getId());if(p.executeUpdate()==0)throw new DatabaseException("Book was not found.",null);return b;}catch(SQLException e){throw new DatabaseException("Could not update book.",e);}}
    @Override public void archive(long id){String sql="UPDATE books SET archived=TRUE WHERE id=? AND available_copies=total_copies";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,id);if(p.executeUpdate()==0)throw new IllegalStateException("Cannot remove a book while copies are on loan, or the book no longer exists.");}catch(SQLException e){throw new DatabaseException("Could not remove book.",e);}}
    @Override public Optional<Book> findById(long id){List<Book> rows=query(SELECT+" AND b.id=?",id);return rows.stream().findFirst();}
    @Override public List<Book> findAll(){return query(SELECT+" ORDER BY b.title",null);}
    @Override public List<Book> search(String query){String sql=SELECT+" AND (LOWER(b.title) LIKE ? OR LOWER(b.author) LIKE ? OR LOWER(b.isbn) LIKE ? OR LOWER(COALESCE(c.name,'')) LIKE ?) ORDER BY b.title";String term="%"+(query==null?"":query.trim().toLowerCase(Locale.ROOT))+"%";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){for(int i=1;i<=4;i++)p.setString(i,term);return read(p);}catch(SQLException e){throw new DatabaseException("Could not search books.",e);}}
    private List<Book> query(String sql,Object id){try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){if(id!=null)p.setObject(1,id);return read(p);}catch(SQLException e){throw new DatabaseException("Could not load books.",e);}}
    private List<Book> read(PreparedStatement p)throws SQLException{List<Book> rows=new ArrayList<>();try(ResultSet r=p.executeQuery()){while(r.next())rows.add(map(r));}return rows;}
    private Book map(ResultSet r)throws SQLException{return new Book(r.getLong("id"),r.getString("title"),r.getString("author"),r.getString("isbn"),r.getLong("category_id"),r.getString("category_name"),r.getString("description"),r.getString("publisher"),(Integer)r.getObject("publication_year"),r.getInt("total_copies"),r.getInt("available_copies"));}
    private void bind(PreparedStatement p,Book b)throws SQLException{p.setString(1,b.getTitle());p.setString(2,b.getAuthor());p.setString(3,b.getIsbn());if(b.getCategoryId()>0)p.setLong(4,b.getCategoryId());else p.setNull(4,Types.BIGINT);p.setString(5,b.getDescription());p.setString(6,b.getPublisher());if(b.getPublicationYear()==null)p.setNull(7,Types.INTEGER);else p.setInt(7,b.getPublicationYear());p.setInt(8,b.getTotalCopies());p.setInt(9,b.getAvailableCopies());}
}
