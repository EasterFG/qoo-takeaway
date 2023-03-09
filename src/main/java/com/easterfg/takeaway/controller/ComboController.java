package com.easterfg.takeaway.controller;

import com.easterfg.takeaway.domain.Combo;
import com.easterfg.takeaway.domain.ComboDish;
import com.easterfg.takeaway.dto.Result;
import com.easterfg.takeaway.query.PageQuery;
import com.easterfg.takeaway.service.ComboService;
import com.easterfg.takeaway.utils.security.Authorize;
import com.easterfg.takeaway.utils.security.Role;
import com.easterfg.takeaway.validator.group.AddOperate;
import com.easterfg.takeaway.validator.group.UpdateOperate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author EasterFG on 2022/9/28
 */
@Authorize(Role.EMPLOYEE)
@RestController
@RequestMapping("/combo")
@Slf4j
public class ComboController {

    @Resource
    private ComboService comboService;

    /**
     * 分页查询全部数据
     */
    @GetMapping("page")
    public Result pageCombo(@Validated PageQuery query, String name, Long categoryId, Integer status) {
        return Result.success(comboService.listCombo(query, name, categoryId, status));
    }

    /**
     * 列出套餐
     */
    @Authorize(Role.ALL)
    @GetMapping("list")
    public Result listCombo(@RequestParam Long categoryId) {
        return Result.success(comboService.listCombo(categoryId));
    }

    /**
     * 通过id查询对应套餐数据
     */
    @GetMapping("{id}")
    public Result getCombo(@PathVariable Long id) {
        return Result.success(comboService.getCombo(id));
    }

    @Authorize(Role.ALL)
    @GetMapping("/dish/{comboId}")
    public Result getComboDish(@PathVariable Long comboId) {
        // 获取套餐对应菜品数据
        List<ComboDish> comboDishes = comboService.getDishCombo(comboId);
        return Result.success(comboDishes);
    }


    @PatchMapping("/status/{status}")
    public Result updateDishStatus(@PathVariable Integer status, @RequestParam Long id) {
        if (status < 0 || status > 1) {
            return Result.failed("A0001", "未知状态");
        }
        if (comboService.updateComboStatus(id, status)) {
            return Result.success();
        }
        return Result.failed("套餐状态更新失败");
    }

    /**
     * 更新套餐数据
     */
    @PutMapping
    public Result updateCombo(@Validated(UpdateOperate.class) @RequestBody Combo combo) {
        return Result.success(comboService.updateCombo(combo));
    }

    /**
     * 新增套餐
     */
    @PostMapping
    public Result addCombo(@Validated(AddOperate.class) @RequestBody Combo combo) {
        if (comboService.saveCombo(combo)) {
            return Result.success();
        }
        return Result.failed("新增套餐失败");
    }

    /**
     * 删除套餐(逻辑删除),对应关系表不进行删除操作
     */
    @DeleteMapping
    public Result removeCombo(@RequestParam("ids") List<Long> ids) {
        comboService.deleteByIds(ids);
        return Result.success();
    }
}
