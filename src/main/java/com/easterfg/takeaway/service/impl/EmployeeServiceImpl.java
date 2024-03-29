package com.easterfg.takeaway.service.impl;

import com.easterfg.takeaway.dao.EmployeeDAO;
import com.easterfg.takeaway.domain.Employee;
import com.easterfg.takeaway.dto.*;
import com.easterfg.takeaway.exception.BusinessException;
import com.easterfg.takeaway.query.PageQuery;
import com.easterfg.takeaway.service.EmployeeService;
import com.easterfg.takeaway.utils.security.BCrypt;
import com.easterfg.takeaway.utils.security.JwtUtil;
import com.easterfg.takeaway.utils.security.UserContext;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author EasterFG on 2022/9/19
 */
@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {


    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private EmployeeDAO employeeDAO;

    @Override
    public String login(EmployeeLoginDTO employeeLoginDto) {
        Employee employee = employeeDAO.findByUsername(employeeLoginDto.getUsername());
        if (employee == null || !BCrypt.checkpw(employeeLoginDto.getPassword(), employee.getPassword())) {
            // 用户or密码错误
            throw new BusinessException("40007", "用户名或密码错误");
        }
        if (employee.getStatus() == 0) {
            throw new BusinessException("40008", "用户已被禁用");
        }
        // 权限判定
        String[] role;
        if ("admin".equals(employee.getUsername())) {
            role = new String[]{"EMPLOYEE", "ADMIN"};
        } else {
            role = new String[]{"EMPLOYEE"};
        }
        // 签发jwt
        return jwtUtil.generateToken(
                employee.getId(),
                employee.getUsername(),
                employee.getName(),
                role);
    }

    @Override
    public Result logout() {
        return Result.success();
    }

    @Override
    public EmployeeInfoDTO me() {
        Long userId = UserContext.getUserId();
        Employee employee = employeeDAO.findById(userId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        String[] role;
        if ("admin".equals(employee.getUsername())) {
            role = new String[]{"EMPLOYEE", "ADMIN"};
        } else {
            role = new String[]{"EMPLOYEE"};
        }
        EmployeeInfoDTO infoDTO = new EmployeeInfoDTO();
        infoDTO.setName(employee.getName());
        infoDTO.setUserId(employee.getId());
        infoDTO.setRoles(role);
        return infoDTO;
    }

    @Override
    public Result updatePassword(ChangePasswordDTO changePasswordDTO) {
        Long userId = UserContext.getUserId();

        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getAgain())) {
            throw new BusinessException("40001", "两次密码不一致");
        }
        // 查询密码
        Employee employee = employeeDAO.findById(userId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        if (!employee.getPassword().equals(changePasswordDTO.getOldPassword())) {
            throw new BusinessException("40007", "密码错误");
        }

        // 修改密码
        int row = employeeDAO.updatePassword(userId, changePasswordDTO.getNewPassword());
        if (row < 1) {
            throw new BusinessException("员工不存在");
        }
        return Result.success("密码修改成功");
    }

    @Override
    public PageData<Employee> listEmployee(PageQuery pageQuery, String name) {
        PageMethod.startPage(pageQuery.getPage(), pageQuery.getPageSize());
        List<Employee> list = employeeDAO.listEmployee(name);
        PageInfo<Employee> info = new PageInfo<>(list);
        return new PageData<>(info.getTotal(), info.getList());
    }

    @Override
    public Employee getEmployee(Long id) {
        Employee employee = employeeDAO.findById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        return employee;
    }

    @Override
    public boolean updateEmployeeStatus(Long id, Integer status) {
        return employeeDAO.updateStatus(id, status) > 0;
    }

    @Override
    public void saveEmployee(Employee employee) {
        try {
            employeeDAO.insertEmployee(employee);
        } catch (DuplicateKeyException e) {
            String message = e.getMessage();
            if (message == null) {
                throw new BusinessException("服务器繁忙,请稍后再试");
            }
            if (message.contains("username")) {
                throw new BusinessException("用户名重复");
            } else if (message.contains("phone")) {
                throw new BusinessException("手机号重复");
            } else {
                throw new BusinessException("身份证号重复");
            }
        }
    }

    @Override
    public boolean deleteEmployee(Long id) {
        return employeeDAO.deleteById(id) > 0;
    }

    @Override
    public Result resetPassword() {
        return null;
    }
}
