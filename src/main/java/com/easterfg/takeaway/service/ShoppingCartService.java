package com.easterfg.takeaway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easterfg.takeaway.domain.ShoppingCart;
import com.easterfg.takeaway.dto.Result;

/**
 * @author EasterFG on 2022/10/22
 */
public interface ShoppingCartService extends IService<ShoppingCart> {


    Result addDish(ShoppingCart shoppingCart);

    Result subDish(Long dishId, Long comboId);

    boolean clean(Long uid);
}
