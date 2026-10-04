package com.library.service.impl;

import com.library.dao.CategoryDAO;
import com.library.model.Category;
import com.library.service.CategoryService;
import com.library.util.ValidationUtil;
import java.util.List;

public class CategoryServiceImpl implements CategoryService {
    private final CategoryDAO categories;
    public CategoryServiceImpl(CategoryDAO categories){this.categories=categories;}
    @Override public Category add(String name,String description){return categories.create(new Category(0,ValidationUtil.required(name,"Category name"),description));}
    @Override public List<Category> all(){return categories.findAll();}
}
