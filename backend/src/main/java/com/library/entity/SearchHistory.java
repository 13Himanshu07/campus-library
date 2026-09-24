package com.library.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name="search_history",indexes=@Index(columnList="user_id,created_at")) @Getter @Setter @NoArgsConstructor
public class SearchHistory {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
 @Column(name="search_query",nullable=false) private String searchQuery;
 @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
}
