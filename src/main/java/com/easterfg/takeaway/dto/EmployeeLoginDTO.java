package com.easterfg.takeaway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author EasterFG on 2022/9/26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeLoginDTO {

    @NotBlank(message = "用户名不能未空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
