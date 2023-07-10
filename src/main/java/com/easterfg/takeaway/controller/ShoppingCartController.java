package com.easterfg.takeaway.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.easterfg.takeaway.domain.ShoppingCart;
import com.easterfg.takeaway.service.ShoppingCartService;
import com.easterfg.takeaway.utils.ErrorCode;
import com.easterfg.takeaway.dto.Result;
import com.easterfg.takeaway.utils.security.Authorize;
import com.easterfg.takeaway.utils.security.Role;
import com.easterfg.takeaway.utils.security.UserContext;
// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author EasterFG on 2022/10/22
 */
@Authorize(Role.USER)
@RestController
@RequestMapping("/shopping-cart")
// @Api(tags = "购物车")
public class ShoppingCartController {

    @Resource
    private ShoppingCartService shoppingCartService;

    // @ApiOperation("列出用户的购物车")
    @GetMapping("/list")
    public Result list() {
        //
        LambdaQueryWrapper<ShoppingCart> wrapper = Wrappers.lambdaQuery(ShoppingCart.class);
        List<ShoppingCart> list = shoppingCartService.list(wrapper);
        return Result.success(list);
    }

    // @ApiOperation("添加菜品到购物车中")
    @PostMapping("add")
    public Result addDish(@Validated @RequestBody ShoppingCart shoppingCart) {
        // 手动验证
        return shoppingCartService.addDish(shoppingCart);
    }


    // @ApiOperation("从购物车扣除商品")
    @PostMapping("sub")
    public Result subDish(@RequestBody ShoppingCart shoppingCart) {
        return shoppingCartService.subDish(shoppingCart.getDishId(), shoppingCart.getComboId());
    }

    // @ApiOperation("清空购物车")
    @DeleteMapping("clean")
    public Result cleanCart() {
        UserContext.User user = UserContext.getUser();
        Long uid = user.getId();
        UserContext.destroy();
        if (shoppingCartService.clean(uid)) {
            return Result.success("success");
        }
        return Result.failed(ErrorCode.UNKNOWN);
    }
}
