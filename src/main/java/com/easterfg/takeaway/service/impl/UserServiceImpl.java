package com.easterfg.takeaway.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easterfg.takeaway.dao.UserDAO;
import com.easterfg.takeaway.domain.User;
import com.easterfg.takeaway.dto.Result;
import com.easterfg.takeaway.dto.UserLoginDTO;
import com.easterfg.takeaway.exception.BusinessException;
import com.easterfg.takeaway.service.UserService;
import com.easterfg.takeaway.utils.ErrorCode;
import com.easterfg.takeaway.utils.SMSUtils;
import com.easterfg.takeaway.utils.security.BCrypt;
import com.easterfg.takeaway.utils.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author EasterFG on 2022/10/20
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserDAO, User> implements UserService {

    @Resource
    private JwtUtil jwtUtil;


    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Result login(String phone, String password) {
        // 查询数据库数据
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class);
        wrapper.select(User::getId, User::getName, User::getPhone, User::getPassword, User::getStatus)
                .eq(User::getPhone, phone);
        User user = getOne(wrapper);
        if (user == null) {
            return Result.failed(ErrorCode.USER_OR_PASSWORD_FAILED);
        }
        // 解密参数, 用户权限USER
        // 验证密码
        if (!BCrypt.checkpw(password, user.getPassword())) {
            return Result.failed(ErrorCode.USER_OR_PASSWORD_FAILED);
        }
        if (user.getStatus() == 0) {
            return Result.failed(ErrorCode.USER_IS_DISABLE);
        }
        // 签发token
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getName(), "USER");
        return Result.success("登录成功", token);
    }

    @Override
    public Result register(User user) {
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class);
        wrapper.eq(User::getPhone, user.getPhone());
        User has = getOne(wrapper);
        if (has != null) {
            return Result.failed(ErrorCode.PHONE_ALREADY_EXISTS);
        }
        // 加密密码
        String hash = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hash);
        // 插入数据
        if (save(user)) {
            // 签发token
            String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getName(), "USER");
            return Result.success("注册成功", token);
        }
        return Result.failed(ErrorCode.UNKNOWN);
    }

    @Override
    public void verificationCode(UserLoginDTO userLoginDTO) {
        String code = SMSUtils.generateValidateCode();
        log.info("code = {}", code);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(userLoginDTO.getPhone()))) {
            throw new BusinessException("获取验证码过于频繁，请在1分钟后重试");
        }
        // 生成验证码
        redisTemplate.opsForValue().set(userLoginDTO.getPhone(), code, 1, TimeUnit.MINUTES);
    }
}
