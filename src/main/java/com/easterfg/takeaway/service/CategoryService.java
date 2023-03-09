package com.easterfg.takeaway.service;

import com.easterfg.takeaway.domain.Category;
import com.easterfg.takeaway.dto.PageData;
import com.easterfg.takeaway.query.PageQuery;

import java.util.List;

/**
 * @author EasterFG on 2022/9/27
 */
public interface CategoryService {

    PageData<Category> listCategory(PageQuery query, String name, Integer type);

    List<Category> listCategoryByType(Integer type);

    boolean deleteCategory(Long id);

    Category getCategory(Long id);

    boolean saveCategory(Category category);

    boolean updateCategory(Category category);
}
