package com.easterfg.takeaway.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easterfg.takeaway.dao.ComboDAO;
import com.easterfg.takeaway.dao.ShoppingCartDAO;
import com.easterfg.takeaway.domain.Combo;
import com.easterfg.takeaway.domain.Dish;
import com.easterfg.takeaway.domain.ShoppingCart;
import com.easterfg.takeaway.dto.Result;
import com.easterfg.takeaway.service.ComboService;
import com.easterfg.takeaway.service.DishService;
import com.easterfg.takeaway.service.ShoppingCartService;
import com.easterfg.takeaway.utils.ErrorCode;
import com.easterfg.takeaway.utils.security.UserContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author EasterFG on 2022/10/22
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartDAO, ShoppingCart> implements ShoppingCartService {

    @Resource
    private DishService dishService;

    @Resource
    private ComboService comboService;

    @Resource
    private ComboDAO comboDAO;

    @Override
    public Result addDish(ShoppingCart shoppingCart) {
        if (Objects.isNull(shoppingCart.getDishId()) && Objects.isNull(shoppingCart.getComboId())) {
            return Result.failed("A0400", "菜品id和套餐id不能同时为空");
        } else if (shoppingCart.getDishId() != null && Objects.nonNull(shoppingCart.getComboId())) {
            return Result.failed("A0400", "菜品id和套餐id不能同时存在");
        }
        // 模拟user id
        Long uid = UserContext.getUserId();
        // 通过用户ID 和 菜品ID 保存
        shoppingCart.setUserId(uid);
        ShoppingCart cart = getOne(shoppingCart.getDishId(), shoppingCart.getComboId(), uid);
        if (cart == null) {
            // 查询菜品数据 or 套餐数据, 保证套餐数据的可信
            if (shoppingCart.getDishId() != null) {
                Dish dish = dishService.getDish(shoppingCart.getDishId());
                if (dish == null) {
                    return Result.failed("A0401", "菜品不存在");
                }
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
            } else {
                Combo combo = comboDAO.getCombo(shoppingCart.getComboId());
                if (combo == null) {
                    return Result.failed("A0401", "套餐不存在");
                }
                shoppingCart.setName(combo.getName());
                shoppingCart.setAmount(combo.getPrice());
                shoppingCart.setImage(combo.getImage());
            }
            if (save(shoppingCart)) {
                return Result.success("success", 1);
            }
        } else {
            // 更新
            LambdaUpdateWrapper<ShoppingCart> wrapper = Wrappers.lambdaUpdate(ShoppingCart.class);
            wrapper.eq(ShoppingCart::getId, cart.getId()).set(ShoppingCart::getNumber, cart.getNumber() + 1);
            if (update(wrapper)) {
                return Result.success("success", cart.getNumber() + 1);
            }
        }
        return Result.failed(ErrorCode.UNKNOWN.getCode(), "购物车保存失败");
    }

    @Override
    public Result subDish(Long dishId, Long comboId) {
        if (dishId == null && comboId == null) {
            return Result.failed("A0400", "菜品id和套餐id不能同时为空");
        } else if (dishId != null && comboId != null) {
            return Result.failed("A0400", "菜品id和套餐id不能同时存在");
        }
        // 获取用户ID
        UserContext.User user = UserContext.getUser();
        Long uid = user.getId();
        UserContext.destroy();
        ShoppingCart cart = getOne(dishId, comboId, uid);
        if (cart == null) {
            return Result.failed("A0404", "数据库数据异常");
        }
        if (cart.getNumber() == 1) {
            if (removeById(cart.getId())) {
                return Result.success("success", 0);
            }
        } else {
            // 减一
            LambdaUpdateWrapper<ShoppingCart> wrapper = Wrappers.lambdaUpdate(ShoppingCart.class);
            wrapper.set(ShoppingCart::getNumber, cart.getNumber() - 1).eq(ShoppingCart::getId, cart.getId());
            if (update(wrapper)) {
                return Result.success("success", cart.getNumber() - 1);
            }
        }
        return Result.failed(ErrorCode.UNKNOWN);
    }

    @Override
    public boolean clean(Long uid) {
        LambdaQueryWrapper<ShoppingCart> wrapper = Wrappers.lambdaQuery(ShoppingCart.class);
        wrapper.eq(ShoppingCart::getUserId, uid);
        return remove(wrapper);
    }

    private ShoppingCart getOne(Long dishId, Long comboId, Long uid) {
        LambdaQueryWrapper<ShoppingCart> wrapper = Wrappers.lambdaQuery(ShoppingCart.class);
        wrapper
                .select(ShoppingCart::getId, ShoppingCart::getNumber)
                .eq(dishId != null, ShoppingCart::getDishId, dishId)
                .eq(comboId != null, ShoppingCart::getComboId, comboId)
                .eq(ShoppingCart::getUserId, uid);
        return getOne(wrapper);
    }
}
