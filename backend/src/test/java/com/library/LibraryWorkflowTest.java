package com.library;

import com.library.entity.Book;
import com.library.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={
 "spring.datasource.url=jdbc:h2:mem:library-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
 "spring.datasource.driver-class-name=org.h2.Driver",
 "spring.datasource.username=sa",
 "spring.datasource.password=",
 "spring.jpa.hibernate.ddl-auto=create-drop",
 "spring.mail.host=localhost",
 "spring.mail.port=1",
 "app.jwt-secret=integration-test-secret-that-is-longer-than-32-bytes"
})
class LibraryWorkflowTest {
 @Autowired TestRestTemplate http;
 @Autowired BookRepository books;

 @Test void memberRegistersLogsInBorrowsReturnsAndCannotOpenLibrarianApi(){
  String email="reader-"+UUID.randomUUID()+"@college.test";
  var registration=new LinkedHashMap<String,Object>();
  registration.put("name","Test Reader");registration.put("email",email);registration.put("password","BookwormPass9!");registration.put("phone","9876543210");registration.put("membershipId","member-"+UUID.randomUUID());registration.put("role","LIBRARIAN");
  var registered=http.postForEntity("/api/auth/register",registration,Map.class);
  assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.OK);
  assertThat(registered.getBody()).doesNotContainKey("password");

  var login=http.postForEntity("/api/auth/login",Map.of("email",email,"password","BookwormPass9!"),Map.class);
  assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
  assertThat((String)((Map<?,?>)login.getBody().get("user")).get("role")).isEqualTo("MEMBER");
  String token=(String)login.getBody().get("token");
  var headers=new HttpHeaders();headers.setBearerAuth(token);headers.setContentType(MediaType.APPLICATION_JSON);
  var memberOnly=http.exchange("/api/members",HttpMethod.GET,new HttpEntity<>(headers),Map.class);
  assertThat(memberOnly.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

  var book=new Book();book.setTitle("Concurrency in Practice");book.setAuthor("A. Writer");book.setIsbn("978-0-00-000001-1");book.setTotalCopies(1);book.setAvailableCopies(1);books.save(book);
  var search=http.exchange("/api/books?q=concurrency",HttpMethod.GET,new HttpEntity<>(headers),Map.class);
  assertThat(search.getStatusCode()).isEqualTo(HttpStatus.OK);
  assertThat((List<?>)search.getBody().get("content")).hasSize(1);
  var borrowed=http.postForEntity("/api/transactions/borrow",new HttpEntity<>(Map.of("bookId",book.getId()),headers),Map.class);
  assertThat(borrowed.getStatusCode()).isEqualTo(HttpStatus.OK);
  assertThat(books.findById(book.getId()).orElseThrow().getAvailableCopies()).isZero();
  Long transactionId=((Number)borrowed.getBody().get("id")).longValue();

  var duplicate=http.postForEntity("/api/transactions/borrow",new HttpEntity<>(Map.of("bookId",book.getId()),headers),Map.class);
  assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  var returned=http.postForEntity("/api/transactions/"+transactionId+"/return",new HttpEntity<>(headers),Map.class);
  assertThat(returned.getStatusCode()).isEqualTo(HttpStatus.OK);
  assertThat(returned.getBody().get("status")).isEqualTo("RETURNED");
  assertThat(books.findById(book.getId()).orElseThrow().getAvailableCopies()).isEqualTo(1);
 }
}
