package com.easterfg.takeaway.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class ShoppingCart {
    /**
     * 主键
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(value = "主键")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 菜品名称
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(value = "菜品名称")
    private String name;

    /**
     * 图片
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(value = "图片")
    private String image;

    /**
     * 用户ID
     */
    @JsonIgnore
    @ApiModelProperty(value = "用户ID")
    private Long userId;

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
     * 数量
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(value = "数量")
    private Integer number;

    /**
     * 金额
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    /**
     * 创建时间
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
