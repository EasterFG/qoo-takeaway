package com.easterfg.takeaway.service;

import com.easterfg.takeaway.domain.Dish;
import com.easterfg.takeaway.dto.PageData;
import com.easterfg.takeaway.query.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * @author EasterFG on 2022/9/27
 */
public interface DishService {


    PageData<Dish> listDish(PageQuery query, String name, Long categoryId, Integer status);

    /**
     * 获取菜品
     */
    Dish getDish(Long id);

    /**
     * 获取菜品详情
     */
    List<Dish> listDish(Long categoryId);

    /**
     * 保存菜品
     */
    void saveDish(Dish dish);

    /**
     * 更新菜品
     */
    boolean updateDish(Dish dish);

    boolean updateDishStatus(Long id, Integer status);

    int removeDish(Collection<Long> ids);

}
