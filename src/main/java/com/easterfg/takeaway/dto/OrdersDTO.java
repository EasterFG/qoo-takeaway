package com.easterfg.takeaway.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author EasterFG on 2022/10/24
 */
@Data
public class OrdersDTO {

    /**
     * 备注
     */
    private String remark;

    @NotNull(message = "配送地址不能为空")
    private Long addressBookId;

    private Integer payMethod;
}
