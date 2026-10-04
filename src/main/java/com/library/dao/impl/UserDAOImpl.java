package com.library.dao.impl;

import com.library.dao.UserDAO;
import com.library.exception.DatabaseException;
import com.library.model.*;
import com.library.util.DBConnection;
import java.sql.*;
import java.util.*;

public class UserDAOImpl implements UserDAO {
    private static final String COLUMNS="id,name,email,password_hash,phone,membership_id,role,status";
    @Override public User create(User u){String sql="INSERT INTO users(name,email,password_hash,phone,membership_id,role,status) VALUES(?,?,?,?,?,?,?)";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){bind(p,u);p.executeUpdate();try(ResultSet k=p.getGeneratedKeys()){if(k.next())u.setId(k.getLong(1));}return u;}catch(SQLException e){throw new DatabaseException("Could not create user. Check that the email and membership ID are unique.",e);}}
    @Override public Optional<User> findByEmail(String email){return one("SELECT "+COLUMNS+" FROM users WHERE email=?",email);}
    @Override public Optional<User> findById(long id){return one("SELECT "+COLUMNS+" FROM users WHERE id=?",id);}
    private Optional<User> one(String sql,Object value){try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setObject(1,value);try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(map(r)):Optional.empty();}}catch(SQLException e){throw new DatabaseException("Could not load user.",e);}}
    @Override public List<User> findAll(){return many("SELECT "+COLUMNS+" FROM users ORDER BY created_at DESC",null);}
    @Override public List<User> findStudents(){return many("SELECT "+COLUMNS+" FROM users WHERE role='STUDENT' ORDER BY name",null);}
    private List<User> many(String sql,Object value){List<User> rows=new ArrayList<>();try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){if(value!=null)p.setObject(1,value);try(ResultSet r=p.executeQuery()){while(r.next())rows.add(map(r));}return rows;}catch(SQLException e){throw new DatabaseException("Could not load users.",e);}}
    @Override public User update(User u){String sql="UPDATE users SET name=?,email=?,phone=?,status=? WHERE id=?";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,u.getName());p.setString(2,u.getEmail());p.setString(3,u.getPhone());p.setString(4,u.getStatus().name());p.setLong(5,u.getId());p.executeUpdate();return u;}catch(SQLException e){throw new DatabaseException("Could not update user.",e);}}
    @Override public void updatePassword(long id,String hash){try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE users SET password_hash=? WHERE id=?")){p.setString(1,hash);p.setLong(2,id);if(p.executeUpdate()!=1)throw new DatabaseException("Account was not found.",null);}catch(SQLException e){throw new DatabaseException("Could not update password.",e);}}
    private void bind(PreparedStatement p,User u)throws SQLException{p.setString(1,u.getName());p.setString(2,u.getEmail());p.setString(3,u.getPasswordHash());p.setString(4,u.getPhone());p.setString(5,u.getMembershipId());p.setString(6,u.getRole().name());p.setString(7,u.getStatus().name());}
    static User map(ResultSet r)throws SQLException{User.Role role=User.Role.valueOf(r.getString("role"));User.Status status=User.Status.valueOf(r.getString("status"));User u=role==User.Role.STUDENT?new Student():new Librarian();u.setId(r.getLong("id"));u.setName(r.getString("name"));u.setEmail(r.getString("email"));u.setPasswordHash(r.getString("password_hash"));u.setPhone(r.getString("phone"));u.setMembershipId(r.getString("membership_id"));u.setRole(role);u.setStatus(status);return u;}
}
