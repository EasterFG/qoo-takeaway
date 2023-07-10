package com.easterfg.takeaway.controller;

import com.easterfg.takeaway.domain.Dish;
import com.easterfg.takeaway.dto.PageData;
import com.easterfg.takeaway.dto.Result;
import com.easterfg.takeaway.query.PageQuery;
import com.easterfg.takeaway.service.DishService;
import com.easterfg.takeaway.utils.ErrorCode;
import com.easterfg.takeaway.utils.security.Authorize;
import com.easterfg.takeaway.utils.security.Role;
import com.easterfg.takeaway.validator.group.AddOperate;
import com.easterfg.takeaway.validator.group.UpdateOperate;
// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author EasterFG on 2022/9/27
 */
@Authorize(Role.EMPLOYEE)
@RestController
@RequestMapping("/dish")
@Slf4j
// @Api(tags = "菜品接口")
public class DishController {

    @Resource
    private DishService dishService;

    /**
     * 查询所有菜品
     */
    // @ApiOperation("分页查询菜品")
    @GetMapping("page")
    public Result pageDish(@Validated PageQuery query, String name, Long categoryId, Integer status) {
        PageData<Dish> pageData = dishService.listDish(query, name, categoryId, status);
        return Result.success(pageData);
    }

    @Authorize(Role.ALL)
    @GetMapping("list")
    public Result listDish(Long categoryId) {
        return Result.success(dishService.listDish(categoryId));
    }

    /**
     * 查询指定菜品
     */
    @GetMapping("{id}")
    public Result getDish(@PathVariable Long id) {
        Dish dish = dishService.getDish(id);
        if (dish != null) {
            return Result.success("success", dish);
        }
        return Result.failed(ErrorCode.DISH_NOT_EXISTS);
    }

    @PatchMapping("/status/{status}")
    public Result updateDishStatus(@PathVariable Integer status, @RequestParam Long id) {
        if (status < 0 || status > 1) {
            return Result.failed("A0001", "未知状态");
        }
//        Dish dish = new Dish();
//        dish.setId(id);
//        dish.setStatus(status);
//        dishService.updateDish(dish);
//        LambdaUpdateWrapper<Dish> wrapper = Wrappers.lambdaUpdate(Dish.class);
//        wrapper.set(Dish::getStatus, status).eq(Dish::getId, id);
        if (dishService.updateDishStatus(id, status)) {
            return Result.success("更新成功");
        }
        return Result.failed("A0200", "更新失败");
    }

    /**
     * 添加新菜品
     */
    @PostMapping
    public Result addDish(@Validated(AddOperate.class) @RequestBody Dish dish) {
        // 需要验证套餐分类的合法性
        dishService.saveDish(dish);
        return Result.success();
    }

    /**
     * 更新菜品
     */
    @PutMapping
    public Result updateDish(@Validated(UpdateOperate.class) @RequestBody Dish dish) {
        // 对应的冗余字段也需要更新
        dishService.updateDish(dish);
        return Result.success();
    }

    /**
     * 删除菜品, 需要去重
     */
    @DeleteMapping
    public Result deleteDish(@RequestParam Set<Long> ids) {
        // 只会删除禁售的数据
        // 同时删除对应的口味
        // 回传删除的列数
        return Result.success(dishService.removeDish(ids));

    }

}
