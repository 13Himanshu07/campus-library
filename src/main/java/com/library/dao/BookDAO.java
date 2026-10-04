package com.library.dao;
import com.library.model.Book;
import java.util.List;
import java.util.Optional;
public interface BookDAO { Book create(Book book); Book update(Book book); void archive(long id); Optional<Book> findById(long id); List<Book> findAll(); List<Book> search(String query); }
