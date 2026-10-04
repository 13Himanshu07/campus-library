package com.library.dao.impl;

import com.library.dao.CategoryDAO;
import com.library.exception.DatabaseException;
import com.library.model.Category;
import com.library.util.DBConnection;
import java.sql.*;
import java.util.*;

public class CategoryDAOImpl implements CategoryDAO {
    @Override public Category create(Category c){try(Connection x=DBConnection.getConnection();PreparedStatement p=x.prepareStatement("INSERT INTO categories(name,description) VALUES(?,?)",Statement.RETURN_GENERATED_KEYS)){p.setString(1,c.getName());p.setString(2,c.getDescription());p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){if(r.next())c.setId(r.getLong(1));}return c;}catch(SQLException e){throw new DatabaseException("Could not add category.",e);}}
    @Override public List<Category> findAll(){List<Category> rows=new ArrayList<>();try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT id,name,description FROM categories ORDER BY name");ResultSet r=p.executeQuery()){while(r.next())rows.add(new Category(r.getLong("id"),r.getString("name"),r.getString("description")));return rows;}catch(SQLException e){throw new DatabaseException("Could not load categories.",e);}}
}
