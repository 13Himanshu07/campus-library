package com.library.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="users", uniqueConstraints={@UniqueConstraint(columnNames="email"), @UniqueConstraint(columnNames="membership_id")})
@Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    @Column(nullable=false) private String email;
    @JsonIgnore @Column(nullable=false) private String password;
    private String phone;
    @Column(name="membership_id", unique=true) private String membershipId;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Role role=Role.MEMBER;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private UserStatus status=UserStatus.ACTIVE;
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt=Instant.now();
    @Column(name="updated_at", nullable=false) private Instant updatedAt=Instant.now();
    @PreUpdate void updateTimestamp(){ updatedAt=Instant.now(); }
    public enum Role { ADMIN, LIBRARIAN, MEMBER }
    public enum UserStatus { ACTIVE, PENDING, DISABLED }
}
