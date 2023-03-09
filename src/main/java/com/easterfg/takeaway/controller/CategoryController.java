package com.easterfg.takeaway.controller;

import com.easterfg.takeaway.domain.Category;
import com.easterfg.takeaway.dto.Result;
import com.easterfg.takeaway.exception.BusinessException;
import com.easterfg.takeaway.query.PageQuery;
import com.easterfg.takeaway.service.CategoryService;
import com.easterfg.takeaway.utils.ErrorCode;
import com.easterfg.takeaway.utils.security.Authorize;
import com.easterfg.takeaway.utils.security.Role;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author EasterFG on 2022/9/27
 */
@Authorize(Role.EMPLOYEE)
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @GetMapping("page")
    public Result page(@Validated PageQuery query, String name, Integer type) {
        return Result.success(categoryService.listCategory(query, name, type));
    }

    /**
     * 列出分类 (禁用状态不列出)
     * <p>
     * 允许用户访问
     */
    @Authorize(Role.ALL)
    @GetMapping("list")
    public Result pageCategory(Integer type) {
        return Result.success(categoryService.listCategoryByType(type));
    }

    @GetMapping("{id}")
    public Result getCategory(@PathVariable Long id) {
        Category category = categoryService.getCategory(id);
        if (category == null) {
            return Result.failed(ErrorCode.CATEGORY_NOT_EXISTS);
        }
        return Result.success(category);
    }

    @PostMapping
    public Result addCategory(@RequestBody Category category) {
        if (categoryService.saveCategory(category)) {
            return Result.success("新增分类成功");
        } else {
            throw new BusinessException("新增分类失败");
        }
    }

    @PutMapping
    public Result updateCategory(@Validated @RequestBody Category category) {
        if (categoryService.updateCategory(category)) {
            return Result.success("更新成功");
        }
        return Result.failed("A0200", "更新失败");
    }

    @PatchMapping("/status/{status}")
    public Result updateEmployeeStatus(@PathVariable Integer status, @RequestParam Long id) {
        if (status < 0 || status > 1) {
            return Result.failed("A0001", "未知状态");
        }
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        if (categoryService.updateCategory(category)) {
            return Result.success("更新成功");
        }
        return Result.failed("A0200", "更新失败");
    }

    @DeleteMapping("{id}")
    public Result deleteCategory(@PathVariable Long id) {
        // 对应下的套餐和菜品都需要进行逻辑删除
        if (categoryService.deleteCategory(id)) {
            return Result.success();
        }
        return Result.failed(ErrorCode.CATEGORY_NOT_EXISTS);
    }

}
