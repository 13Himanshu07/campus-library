package com.library.service;
import com.library.model.Book;
import java.util.List;
public interface BookService { Book add(Book book); default Book add(Book book,Long actor){return add(book);} Book update(Book book); default Book update(Book book,Long actor){return update(book);} void archive(long id); default void archive(long id,Long actor){archive(id);} Book get(long id); List<Book> all(); List<Book> search(String query); }
