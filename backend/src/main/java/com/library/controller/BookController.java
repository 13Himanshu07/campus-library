package com.library.controller;

import com.library.entity.*;
import com.library.exception.ApiException;
import com.library.repository.*;
import com.library.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/books")
public class BookController {
 private final BookRepository books; private final NotificationService notify; private final UserRepository users; private final CurrentUser current; private final SearchHistoryRepository searches;
 public BookController(BookRepository b,NotificationService n,UserRepository u,CurrentUser c,SearchHistoryRepository h){books=b;notify=n;users=u;current=c;searches=h;}
 public record BookInput(@NotBlank String title,@NotBlank String author,@NotBlank String isbn,String genre,String description,String publisher,Integer publicationYear,@Min(0) int totalCopies,String coverImageUrl){}
 @GetMapping public Map<String,Object> all(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="12") int size){if(!q.isBlank()){var row=new SearchHistory();row.setUser(current.get());row.setSearchQuery(q.trim());searches.save(row);}var result=books.search(q,PageRequest.of(Math.max(0,page),Math.min(Math.max(size,1),100),Sort.by("title")));return Map.of("content",result.getContent(),"page",result.getNumber(),"size",result.getSize(),"totalElements",result.getTotalElements(),"totalPages",result.getTotalPages());}
 @GetMapping("/{id}") public Book one(@PathVariable Long id){return books.findByIdAndArchivedFalse(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Book not found"));}
 @PostMapping @PreAuthorize("hasRole('LIBRARIAN')") public Book create(@Valid @RequestBody BookInput i){var b=new Book();fill(b,i);b.setAvailableCopies(i.totalCopies());var saved=books.save(b);users.findByRole(User.Role.MEMBER).forEach(u->notify.create(u,"New book available",saved.getTitle()+" by "+saved.getAuthor()+" is now in the library.",Notification.Type.NEW_BOOK,true));return saved;}
 @PutMapping("/{id}") @PreAuthorize("hasRole('LIBRARIAN')") public Book update(@PathVariable Long id,@Valid @RequestBody BookInput i){var b=books.findByIdAndArchivedFalse(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Book not found"));int borrowed=b.getTotalCopies()-b.getAvailableCopies();if(i.totalCopies()<borrowed)throw new ApiException(HttpStatus.BAD_REQUEST,"Total copies cannot be below copies currently borrowed");fill(b,i);b.setAvailableCopies(i.totalCopies()-borrowed);return books.save(b);}
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('LIBRARIAN')") public Map<String,String> delete(@PathVariable Long id){var b=books.findByIdAndArchivedFalse(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Book not found"));if(b.getAvailableCopies()!=b.getTotalCopies())throw new ApiException(HttpStatus.CONFLICT,"Cannot delete a book with active borrowing transactions");b.setArchived(true);books.save(b);return Map.of("message","Book removed from the catalog");}
 private void fill(Book b,BookInput i){b.setTitle(i.title());b.setAuthor(i.author());b.setIsbn(i.isbn());b.setGenre(i.genre());b.setDescription(i.description());b.setPublisher(i.publisher());b.setPublicationYear(i.publicationYear());b.setTotalCopies(i.totalCopies());b.setCoverImageUrl(i.coverImageUrl());}
}
