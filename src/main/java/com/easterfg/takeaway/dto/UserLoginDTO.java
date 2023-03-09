package com.easterfg.takeaway.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author EasterFG on 2022/11/23
 */
@Data
public class UserLoginDTO {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String code;

    private String password;

}
