package com.library.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="notifications", indexes=@Index(columnList="user_id,is_read"))
@Getter @Setter @NoArgsConstructor
public class Notification {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.EAGER, optional=false) @JoinColumn(name="user_id", nullable=false) private User user;
    @Column(nullable=false) private String title;
    @Column(nullable=false, length=2000) private String message;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Type type=Type.SYSTEM;
    @Column(name="is_read", nullable=false) private boolean read=false;
    private Instant createdAt=Instant.now();
    public enum Type { DUE_DATE, OVERDUE, NEW_BOOK, SYSTEM }
}
