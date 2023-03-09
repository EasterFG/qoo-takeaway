package com.easterfg.takeaway;

import com.easterfg.takeaway.domain.Employee;
import com.easterfg.takeaway.query.PageQuery;
import com.easterfg.takeaway.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author EasterFG on 2022/9/25
 */
@SpringBootTest
public class EmployeeServiceTest {

    @Resource
    private EmployeeService employeeService;

    @Test
    void listEmployee() {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPage(1);
        pageQuery.setPageSize(10);
    }

    @Test
    void insert() {
        Employee employee = new Employee();
        employee.setUsername("zs");
        employee.setName("张三");
        employee.setGender(0);
        employee.setPassword("00000");
        employee.setPhone("10086");
        employee.setIdCard("10086");
        employeeService.saveEmployee(employee);
    }
}
