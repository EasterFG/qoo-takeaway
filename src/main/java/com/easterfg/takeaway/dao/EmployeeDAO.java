package com.easterfg.takeaway.dao;

import com.easterfg.takeaway.domain.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author EasterFG on 2022/9/19
 */
public interface EmployeeDAO {

    @Select("select id, username, name, password, status FROM employee where username = #{username}")
    Employee findByUsername(String username);

    List<Employee> listEmployee(String name);

    @Select("select id, username, name, password, phone, gender, id_card from employee where id = #{id} and is_deleted = 0;")
    Employee findById(Long id);

    @Update("update employee set status = #{param2}, update_time = now() where is_deleted = 0 and id = #{param1}")
    int updateStatus(Long id, Integer status);

    @Insert("insert into employee (username, name, password, phone, gender, id_card, create_time, update_time) " +
            "values (#{username}, #{name}, #{password}, #{phone}, #{gender}, #{idCard}, now(), now())")
    void insertEmployee(Employee employee);

    @Update("update employee set is_deleted = 1, update_time = now() where is_deleted = 0 and id = #{id};")
    int deleteById(Long id);
}