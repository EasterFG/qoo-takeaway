package com.easterfg.takeaway.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author EasterFG on 2023/6/1
 */
@Data
public class ChangePasswordDTO {

    @NotNull(message = "旧密码不能为空")
    private String oldPassword;

    @NotNull(message = "新密码不能为空")
    private String newPassword;

    @NotNull(message = "重复密码不能为空")
    private String again;
}
