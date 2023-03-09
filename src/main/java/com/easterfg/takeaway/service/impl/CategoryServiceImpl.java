package com.easterfg.takeaway.service.impl;

import com.easterfg.takeaway.dao.CategoryDAO;
import com.easterfg.takeaway.dao.ComboDAO;
import com.easterfg.takeaway.dao.DishDAO;
import com.easterfg.takeaway.domain.Category;
import com.easterfg.takeaway.dto.PageData;
import com.easterfg.takeaway.exception.BusinessException;
import com.easterfg.takeaway.query.PageQuery;
import com.easterfg.takeaway.service.CategoryService;
import com.easterfg.takeaway.utils.security.UserContext;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author EasterFG on 2022/9/27
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Resource
    private DishDAO dishDAO;

    @Resource
    private ComboDAO comboDAO;

    @Resource
    private CategoryDAO categoryDAO;

    @Override
    public PageData<Category> listCategory(PageQuery query, String name, Integer type) {
        Category category = new Category();
        category.setName(name);
        category.setType(type);
        PageMethod.startPage(query.getPage(), query.getPageSize());
        List<Category> list = categoryDAO.listCategory(category);
        PageInfo<Category> info = new PageInfo<>(list);
        return new PageData<>(info.getTotal(), info.getList());
    }

    @Override
    public List<Category> listCategoryByType(Integer type) {
        return categoryDAO.listCategoryByType(type);
    }

    @Override
    public boolean deleteCategory(Long id) {
        // 查询分类下是否有数据
        if (dishDAO.countByCid(id) > 0) {
            throw new BusinessException("此分类下还存在菜品数据,无法删除");
        } else if (comboDAO.countByCid(id) > 0) {
            throw new BusinessException("此分类下还存在套餐数据,无法删除");
        }
        // 删除category数据
        return categoryDAO.deleteById(id) > 0;
    }

    @Override
    public Category getCategory(Long id) {
        return categoryDAO.getCategoryById(id);
    }

    @Override
    public boolean saveCategory(Category category) {
        Long uid = UserContext.getUserId();
        category.setCreateUser(uid);
        return categoryDAO.insertCategory(category) > 0;
    }

    @Override
    public boolean updateCategory(Category category) {
        Long id = UserContext.getUserId();
        category.setUpdateUser(id);
        return categoryDAO.updateCategory(category) > 0;
    }
}
