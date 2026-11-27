package com.library.dao;
import com.library.model.User;
import java.util.List;
import java.util.Optional;
public interface UserDAO { User create(User user); default User create(User user,String action){return create(user);} Optional<User> findByEmail(String email); Optional<User> findById(long id); List<User> findAll(); List<User> findStudents(); User update(User user); default User update(User user,Long actor,String action){return update(user);} void updatePassword(long userId,String passwordHash); default void updatePassword(long userId,String passwordHash,Long actor){updatePassword(userId,passwordHash);} }
