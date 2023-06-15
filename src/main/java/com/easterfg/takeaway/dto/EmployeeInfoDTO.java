package com.easterfg.takeaway.dto;

import lombok.Data;

/**
 * @author EasterFG on 2023/5/31
 */
@Data
public class EmployeeInfoDTO {
    private Long userId;
    private String name;
    private String[] roles;
}
