package com.library.service.impl;

import com.library.dao.BookDAO;
import com.library.exception.ValidationException;
import com.library.model.Book;
import com.library.service.BookService;
import com.library.util.ValidationUtil;
import java.util.List;

public class BookServiceImpl implements BookService {
    private final BookDAO books;
    public BookServiceImpl(BookDAO books){this.books=books;}
    @Override public Book add(Book b){validate(b);b.setAvailableCopies(b.getTotalCopies());return books.create(b);}
    @Override public Book update(Book b){validate(b);Book old=get(b.getId());int borrowed=old.getTotalCopies()-old.getAvailableCopies();if(b.getTotalCopies()<borrowed)throw new ValidationException("Total copies cannot be below copies currently on loan.");b.setAvailableCopies(b.getTotalCopies()-borrowed);return books.update(b);}
    private void validate(Book b){b.setTitle(ValidationUtil.required(b.getTitle(),"Title",255));b.setAuthor(ValidationUtil.required(b.getAuthor(),"Author",180));b.setIsbn(ValidationUtil.required(b.getIsbn(),"ISBN",32));if(b.getTotalCopies()<0)throw new ValidationException("Total copies cannot be negative.");}
    @Override public void archive(long id){get(id);books.archive(id);}
    @Override public Book get(long id){return books.findById(id).orElseThrow(()->new ValidationException("Book was not found."));}
    @Override public List<Book> all(){return books.findAll();}
    @Override public List<Book> search(String query){return query==null||query.isBlank()?all():books.search(query);}
}
