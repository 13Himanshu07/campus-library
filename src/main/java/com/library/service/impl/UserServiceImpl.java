package com.library.service.impl;

import com.library.dao.UserDAO;
import com.library.exception.AuthenticationException;
import com.library.exception.UserNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.*;
import com.library.service.UserService;
import com.library.util.PasswordUtil;
import com.library.util.ValidationUtil;
import java.util.List;
import java.util.UUID;

public class UserServiceImpl implements UserService {
    private final UserDAO users;
    public UserServiceImpl(UserDAO users){this.users=users;}
    @Override public User register(String name,String email,String password,String phone,String membershipId){return register(name,email,password,phone,membershipId,false);}
    @Override public User register(String name,String email,String password,String phone,String membershipId,boolean librarianApplication){
        String cleanName=ValidationUtil.required(name,"Name",120);String cleanEmail=ValidationUtil.email(email);
        if(password==null||password.length()<10)throw new ValidationException("Password must be at least 10 characters.");
        String membership=membershipId==null||membershipId.isBlank()?"ST-"+UUID.randomUUID().toString().substring(0,8).toUpperCase():membershipId.trim();
        User u=librarianApplication?new Librarian():new Student();u.setName(cleanName);u.setEmail(cleanEmail);u.setPasswordHash(PasswordUtil.hash(password));u.setPhone(phone);u.setMembershipId(membership);u.setStatus(librarianApplication?User.Status.PENDING:User.Status.ACTIVE);return users.create(u);
    }
    @Override public User authenticate(String email,String password){User u=users.findByEmail(ValidationUtil.email(email)).orElseThrow(()->new AuthenticationException("Email or password is incorrect."));if(u.getStatus()!=User.Status.ACTIVE||!PasswordUtil.matches(password,u.getPasswordHash()))throw new AuthenticationException("Email or password is incorrect.");return u;}
    @Override public User getById(long id){return users.findById(id).orElseThrow(()->new UserNotFoundException("Account not found."));}
    @Override public User update(User user){ValidationUtil.required(user.getName(),"Name");user.setEmail(ValidationUtil.email(user.getEmail()));return users.update(user);}
    @Override public void changePassword(long userId,String current,String next,String confirmation){User u=getById(userId);if(!PasswordUtil.matches(current,u.getPasswordHash()))throw new AuthenticationException("Current password is incorrect.");if(next==null||next.length()<10)throw new ValidationException("New password must be at least 10 characters.");if(!next.equals(confirmation))throw new ValidationException("Password confirmation does not match.");users.updatePassword(userId,PasswordUtil.hash(next));}
    @Override public List<User> getAll(){return users.findAll();}
    @Override public List<User> getStudents(){return users.findStudents();}
    @Override public List<User> pendingLibrarians(){return users.findAll().stream().filter(u->u.getRole()==User.Role.LIBRARIAN&&u.getStatus()==User.Status.PENDING).toList();}
    @Override public void decideLibrarianApplication(long id,boolean approved){User user=getById(id);if(user.getRole()!=User.Role.LIBRARIAN||user.getStatus()!=User.Status.PENDING)throw new ValidationException("Pending librarian application was not found.");user.setStatus(approved?User.Status.ACTIVE:User.Status.DISABLED);users.update(user);}
}
