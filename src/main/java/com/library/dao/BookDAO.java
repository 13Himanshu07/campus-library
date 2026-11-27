package com.library.dao;
import com.library.model.Book;
import java.util.List;
import java.util.Optional;
public interface BookDAO { Book create(Book book); default Book create(Book book,Long actor){return create(book);} Book update(Book book); default Book update(Book book,Long actor){return update(book);} void archive(long id); default void archive(long id,Long actor){archive(id);} Optional<Book> findById(long id); List<Book> findAll(); List<Book> search(String query); }
