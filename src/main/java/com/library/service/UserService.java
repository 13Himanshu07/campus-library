package com.library.service;
import com.library.model.User;
import java.util.List;
public interface UserService { User register(String name,String email,String password,String phone,String membershipId); User register(String name,String email,String password,String phone,String membershipId,boolean librarianApplication); User authenticate(String email,String password); User getById(long id); User update(User user); void changePassword(long userId,String currentPassword,String newPassword,String confirmation); List<User> getAll(); List<User> getStudents(); List<User> pendingLibrarians(); void decideLibrarianApplication(long id,boolean approved); }
