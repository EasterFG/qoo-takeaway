package com.easterfg.takeaway.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class OrderDetail {

    private Long id;

    /**
     * 菜品名称
     */
    @ApiModelProperty(value = "菜品名称")
    private String name;

    /**
     * 菜品图片
     */
    @ApiModelProperty(value = "菜品图片")
    private String image;

    /**
     * 订单ID
     */
    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    /**
     * 菜品ID
     */
    @ApiModelProperty(value = "菜品ID")
    private Long dishId;

    /**
     * 套餐ID
     */
    @ApiModelProperty(value = "套餐ID")
    private Long comboId;

    /**
     * 菜品口味
     */
    @ApiModelProperty(value = "菜品口味")
    private String dishFlavor;

    /**
     * 菜品数量
     */
    @ApiModelProperty(value = "菜品数量")
    private Integer number;

    /**
     * 菜品金额
     */
    @ApiModelProperty(value = "菜品金额")
    private BigDecimal amount;
}

