package com.library.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name="password_reset_tokens",indexes=@Index(columnList="token_hash")) @Getter @Setter @NoArgsConstructor
public class PasswordResetToken {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
 @Column(name="token_hash",nullable=false,unique=true) private String tokenHash;
 @Column(nullable=false) private Instant expiresAt;
 private Instant usedAt;
}
