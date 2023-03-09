package com.easterfg.takeaway.controller;

import com.easterfg.takeaway.domain.User;
import com.easterfg.takeaway.dto.EmployeeLoginDTO;
import com.easterfg.takeaway.dto.Result;
import com.easterfg.takeaway.dto.UserLoginDTO;
import com.easterfg.takeaway.service.UserService;
import com.easterfg.takeaway.utils.MapUtils;
import com.easterfg.takeaway.utils.security.Authorize;
import com.easterfg.takeaway.utils.security.Role;
import com.easterfg.takeaway.utils.security.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author EasterFG on 2022/10/20
 */
@RestController
@RequestMapping(value = "/user")
@Api(tags = "用户接口")
public class UserController {


    @Resource
    private UserService userService;

    @ApiOperation("用户登录接口")
    @PostMapping(value = "/login")

    public Result login(@Validated @RequestBody EmployeeLoginDTO employeeLoginDTO) {
        return userService.login(employeeLoginDTO.getUsername(), employeeLoginDTO.getPassword());
    }

    @PostMapping("/code")
    public Result verificationCode(@Validated @RequestBody UserLoginDTO userLoginDTO) {
        userService.verificationCode(userLoginDTO);
        return Result.success();
    }

    /**
     * 用户注册
     */
    @ApiOperation("用户注册接口")
    @PostMapping("/register")
    public Result register(@Validated @RequestBody User user) {
        return userService.register(user);
    }


    /**
     * 查询用户信息
     */
    @Authorize(Role.USER)
    @ApiOperation("获取当前用户详情")
    @GetMapping
    public Result getUserInfo() {
        UserContext.User context = UserContext.getUser();
        User user = userService.getById(context.getId());
        // 手机号脱敏
        String phone = user.getPhone();
        Map<String, Object> data = MapUtils.hashMap()
                .put("phone", phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"))
                .put("name", user.getName())
                .put("gender", user.getGender())
                .put("avatar", user.getAvatar())
                .build();
        UserContext.destroy();
        return Result.success(data);
    }

    @Authorize(Role.EMPLOYEE)
    @ApiOperation("获取用户总数")
    @GetMapping("/count")
    public Result countUser() {
        return Result.success(userService.count());
    }
}
