package com.library.repository;
import com.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface UserRepository extends JpaRepository<User,Long> {
 Optional<User> findByEmailIgnoreCase(String email);
 boolean existsByEmailIgnoreCase(String email);
 boolean existsByMembershipId(String membershipId);
 List<User> findByRole(User.Role role);
 List<User> findByRoleAndStatus(User.Role role,User.UserStatus status);
}
