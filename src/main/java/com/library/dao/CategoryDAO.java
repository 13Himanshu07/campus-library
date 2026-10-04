package com.library.dao;
import com.library.model.Category;
import java.util.List;
public interface CategoryDAO { Category create(Category category); List<Category> findAll(); }
