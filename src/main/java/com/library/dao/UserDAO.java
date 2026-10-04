package com.library.dao;
import com.library.model.User;
import java.util.List;
import java.util.Optional;
public interface UserDAO { User create(User user); Optional<User> findByEmail(String email); Optional<User> findById(long id); List<User> findAll(); List<User> findStudents(); User update(User user); void updatePassword(long userId,String passwordHash); }
