package com.easterfg.takeaway.service;

import com.easterfg.takeaway.domain.Employee;
import com.easterfg.takeaway.dto.*;
import com.easterfg.takeaway.query.PageQuery;

/**
 * @author EasterFG on 2022/9/19
 */
public interface EmployeeService {

    /**
     * 登录
     *
     * @param employeeLoginDto 登录数据
     * @return 结果
     */
    String login(EmployeeLoginDTO employeeLoginDto);

    /**
     * 退出登录
     *
     * @return 结果
     */
    Result logout();

    EmployeeInfoDTO me();

    Result updatePassword(ChangePasswordDTO changePasswordDTO);

    PageData<Employee> listEmployee(PageQuery pageQuery, String name);

    Employee getEmployee(Long id);

    boolean updateEmployeeStatus(Long id, Integer status);

    void saveEmployee(Employee employee);

    boolean deleteEmployee(Long id);

    Result resetPassword();
}
