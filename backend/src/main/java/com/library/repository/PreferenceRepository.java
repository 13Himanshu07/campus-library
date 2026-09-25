package com.library.repository;
import com.library.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PreferenceRepository extends JpaRepository<NotificationPreference,Long> { Optional<NotificationPreference> findByUserId(Long userId); void deleteByUserId(Long userId); }
