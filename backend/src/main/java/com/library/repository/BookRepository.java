package com.library.repository;
import com.library.entity.Book;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import java.util.*;
public interface BookRepository extends JpaRepository<Book,Long> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select b from Book b where b.id=:id and b.archived=false") Optional<Book> findLockedById(@Param("id") Long id);
 Optional<Book> findByIdAndArchivedFalse(Long id);
 @Query("select b from Book b where b.archived=false and (lower(b.title) like lower(concat('%',:q,'%')) or lower(b.author) like lower(concat('%',:q,'%')) or lower(b.isbn) like lower(concat('%',:q,'%')) or lower(coalesce(b.genre,'')) like lower(concat('%',:q,'%'))) ")
 Page<Book> search(@Param("q") String query, Pageable pageable);
}
