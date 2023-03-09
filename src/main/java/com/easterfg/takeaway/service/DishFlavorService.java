package com.easterfg.takeaway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easterfg.takeaway.domain.DishFlavor;

import java.util.List;

/**
 * @author EasterFG on 2022/9/27
 */
public interface DishFlavorService extends IService<DishFlavor> {

    /**
     * 对口味进行更改
     * 包括: update insert delete(逻辑删除), 更具属性的值进行不同操作
     *
     * @param list   dto对象集合
     * @param dishId 菜品id
     */
    void updateDishFlavor(List<DishFlavor> list, Long dishId);

}
