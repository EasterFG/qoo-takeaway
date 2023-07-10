package com.easterfg.takeaway.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easterfg.takeaway.domain.User;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * @author EasterFG on 2022/10/20
 */
public interface UserDAO extends BaseMapper<User> {

    @Select("select count(*) from user;")
    int countUser();

    @Select("select count(*) from user where date(create_time) = #{time}")
    int countUserByDate(LocalDate time);

}
