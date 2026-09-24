package com.library.repository;
import com.library.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PasswordResetRepository extends JpaRepository<PasswordResetToken,Long> { Optional<PasswordResetToken> findByTokenHash(String hash); void deleteByUserId(Long userId); }
