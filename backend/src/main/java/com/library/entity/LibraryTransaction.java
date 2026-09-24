package com.library.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

@Entity @Table(name="library_transactions", indexes={@Index(columnList="member_id,status"), @Index(columnList="book_id,status")})
@Getter @Setter @NoArgsConstructor
public class LibraryTransaction {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.EAGER, optional=false) @JoinColumn(name="book_id", nullable=false) private Book book;
    @ManyToOne(fetch=FetchType.EAGER, optional=false) @JoinColumn(name="member_id", nullable=false) private User member;
    @Column(nullable=false) private LocalDate borrowDate;
    @Column(nullable=false) private LocalDate dueDate;
    private LocalDate returnDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status=Status.BORROWED;
    @Column(nullable=false, precision=10, scale=2) private BigDecimal fineAmount=BigDecimal.ZERO;
    private Instant createdAt=Instant.now();
    public enum Status { BORROWED, RETURNED, OVERDUE }
}
