package com.easterfg.takeaway.service.impl;

import com.easterfg.takeaway.domain.Dish;
import com.easterfg.takeaway.service.DishService;
import com.easterfg.takeaway.utils.security.UserContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author EasterFG on 2022/11/22
 */
@SpringBootTest
class DishServiceTest {

    @Resource
    private DishService dishService;

    @Test
    void dishTest() {
        List<Dish> dishes = dishService.listDish(1413384954989060097L);
        Dish dish = dishes.get(0);
        dish.setCategoryId(1413384954989060097L);
        UserContext.setUser(new UserContext.User(10000L, "", "", new ArrayList<>()));
        dishService.saveDish(dish);
    }

    @Test
    void updateTest() {
        List<Dish> dishes = dishService.listDish(1413384954989060097L);
        Dish dish = dishes.get(0);
        dish.setCategoryId(1413384954989060097L);
        UserContext.setUser(new UserContext.User(10000L, "", "", new ArrayList<>()));
        dish.getFlavors().get(0).setValue("[\"测试\"]");
        dishService.updateDish(dish);
    }

}