package com.easterfg.takeaway.controller;

import com.easterfg.takeaway.domain.Employee;
import com.easterfg.takeaway.dto.EmployeeLoginDTO;
import com.easterfg.takeaway.dto.Result;
import com.easterfg.takeaway.query.PageQuery;
import com.easterfg.takeaway.service.EmployeeService;
import com.easterfg.takeaway.utils.security.Authorize;
import com.easterfg.takeaway.utils.security.Role;
import com.easterfg.takeaway.validator.group.AddOperate;
import com.easterfg.takeaway.validator.group.UpdateOperate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author EasterFG on 2022/9/19
 * <p>
 * 员工接口, 只允许admin访问
 */
@RestController
@RequestMapping("/employee")
@Slf4j
@Api(tags = "员工接口")
public class EmployeeController {

    @Resource
    public EmployeeService employeeService;

    /**
     * 员工登录
     */
    @ApiOperation("员工登录接口")
    @PostMapping("/login")
    public Result login(@Validated @RequestBody EmployeeLoginDTO employeeLoginDto) {
        String token = employeeService.login(employeeLoginDto);
        return Result.success("success", token);
    }


    /**
     * 查询所有员工
     */
    @Authorize(Role.ADMIN)
    @ApiOperation("查询所有员工")
    @GetMapping("/page")
    public Result pageEmployee(@Validated PageQuery query, String name) {
        return Result.success(employeeService.listEmployee(query, name));
    }

    /**
     * 通过id查询员工
     * <p>
     * 逻辑删除自动匹配
     */
    @Authorize(Role.ADMIN)
    @ApiOperation("通过id查询员工")
    @GetMapping("/{id}")
    public Result getEmployee(@PathVariable Long id) {
        return Result.success(employeeService.getEmployee(id));
    }

    /**
     * 更新状态
     */
    @Authorize(Role.ADMIN)
    @ApiOperation("更新员工状态")
    @PatchMapping("status/{status}")
    public Result updateEmployeeStatus(@PathVariable Integer status, @RequestParam Long id) {
        if (status < 0 || status > 1) {
            return Result.failed("未知员工状态");
        }
        // TODO 禁用员工需要同时删除token (redis 保存token)

        if (employeeService.updateEmployeeStatus(id, status)) {
            return Result.success();
        }
        return Result.failed("更新员工状态失败");
    }

    /**
     * 更新员工
     */
    @Authorize(Role.ADMIN)
    @ApiOperation("更新员工数据")
    @PutMapping
    public Result updateEmployee(@Validated(UpdateOperate.class) @RequestBody Employee employee) {
        if ("admin".equals(employee.getUsername())) {
            return Result.failed("无法修改管理员信息");
        }
        employeeService.saveEmployee(employee);
        return Result.success();
    }

    /**
     * 新增员工
     */
    @Authorize(Role.ADMIN)
    @ApiOperation("新增员工")
    @PostMapping
    public Result addEmployee(@Validated(AddOperate.class) @RequestBody Employee employee) {
        // 通过数据库抛出的唯一键异常来验证
        employeeService.saveEmployee(employee);
        return Result.success();
    }

    /**
     * 删除员工
     */
    @Authorize(Role.ADMIN)
    @ApiOperation("根据id删除员工")
    @DeleteMapping("{id}")
    public Result deleteEmployee(@PathVariable Long id) {
        // 采用逻辑删除功能
        if (employeeService.deleteEmployee(id)) {
            return Result.success();
        }
        return Result.failed("用户不存在");
    }
}
