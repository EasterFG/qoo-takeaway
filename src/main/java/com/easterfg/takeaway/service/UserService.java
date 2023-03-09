package com.easterfg.takeaway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easterfg.takeaway.domain.User;
import com.easterfg.takeaway.dto.Result;
import com.easterfg.takeaway.dto.UserLoginDTO;

/**
 * @author EasterFG on 2022/10/20
 */
public interface UserService extends IService<User> {

    Result login(String phone, String password);

    Result register(User user);

    void verificationCode(UserLoginDTO userLoginDTO);
}
