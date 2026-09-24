package com.library.repository;
import com.library.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SearchHistoryRepository extends JpaRepository<SearchHistory,Long> { List<SearchHistory> findTop50ByUserIdOrderByCreatedAtDesc(Long userId); }
