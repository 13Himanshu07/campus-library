package com.library.service;
import com.library.model.Book;
import java.util.List;
public interface BookService { Book add(Book book); Book update(Book book); void archive(long id); Book get(long id); List<Book> all(); List<Book> search(String query); }
