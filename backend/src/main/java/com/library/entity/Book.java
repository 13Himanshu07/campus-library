package com.library.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="books", uniqueConstraints=@UniqueConstraint(columnNames="isbn"))
@Getter @Setter @NoArgsConstructor
public class Book {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String title;
    @Column(nullable=false) private String author;
    @Column(nullable=false, unique=true) private String isbn;
    private String genre;
    @Column(length=3000) private String description;
    private String publisher;
    private Integer publicationYear;
    @Column(nullable=false) private int totalCopies;
    @Column(nullable=false) private int availableCopies;
    private String coverImageUrl;
    @Column(nullable=false) private boolean archived=false;
    private Instant createdAt=Instant.now();
    private Instant updatedAt=Instant.now();
    @PreUpdate void updateTimestamp(){ updatedAt=Instant.now(); }
}
