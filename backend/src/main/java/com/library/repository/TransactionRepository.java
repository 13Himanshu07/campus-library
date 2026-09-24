package com.library.repository;
import com.library.entity.LibraryTransaction;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface TransactionRepository extends JpaRepository<LibraryTransaction,Long> {
 long countByMemberIdAndStatusIn(Long memberId, Collection<LibraryTransaction.Status> statuses);
 boolean existsByBookIdAndMemberIdAndStatusIn(Long bookId,Long memberId,Collection<LibraryTransaction.Status> statuses);
 List<LibraryTransaction> findByMemberIdOrderByCreatedAtDesc(Long memberId);
 List<LibraryTransaction> findByStatusIn(Collection<LibraryTransaction.Status> statuses);
 @Query("select t from LibraryTransaction t where t.returnDate is null and t.dueDate < current_date and t.status<>com.library.entity.LibraryTransaction.Status.RETURNED") List<LibraryTransaction> findOverdueOpen();
 @Query("select t from LibraryTransaction t where t.returnDate is null and t.status=com.library.entity.LibraryTransaction.Status.BORROWED and t.dueDate=:date") List<LibraryTransaction> findDueOn(@Param("date") java.time.LocalDate date);
 List<LibraryTransaction> findByMemberIdAndBookIdAndStatusIn(Long memberId,Long bookId,Collection<LibraryTransaction.Status> statuses);
}
