package com.library.service;
import com.library.model.Category;
import java.util.List;
public interface CategoryService { Category add(String name,String description); List<Category> all(); }
