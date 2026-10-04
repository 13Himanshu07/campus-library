package com.library.model;

import java.io.Serializable;

public class Book implements Serializable {
    private long id, categoryId;
    private String title,author,isbn,categoryName,description,publisher;
    private Integer publicationYear;
    private int totalCopies,availableCopies;
    public Book(){ }
    public Book(long id,String title,String author,String isbn,long categoryId,String categoryName,String description,String publisher,Integer year,int total,int available){this.id=id;this.title=title;this.author=author;this.isbn=isbn;this.categoryId=categoryId;this.categoryName=categoryName;this.description=description;this.publisher=publisher;this.publicationYear=year;this.totalCopies=total;this.availableCopies=available;}
    public long getId(){return id;} public void setId(long v){id=v;} public long getCategoryId(){return categoryId;} public void setCategoryId(long v){categoryId=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;} public String getAuthor(){return author;} public void setAuthor(String v){author=v;}
    public String getIsbn(){return isbn;} public void setIsbn(String v){isbn=v;} public String getCategoryName(){return categoryName;} public void setCategoryName(String v){categoryName=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;} public String getPublisher(){return publisher;} public void setPublisher(String v){publisher=v;}
    public Integer getPublicationYear(){return publicationYear;} public void setPublicationYear(Integer v){publicationYear=v;}
    public int getTotalCopies(){return totalCopies;} public void setTotalCopies(int v){totalCopies=v;} public int getAvailableCopies(){return availableCopies;} public void setAvailableCopies(int v){availableCopies=v;}
}
