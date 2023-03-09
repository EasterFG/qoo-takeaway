package com.easterfg.takeaway.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.easterfg.takeaway.validator.group.AddOperate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ComboDish {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 套餐ID
     */
    private Long comboId;

    /**
     * 菜品ID
     */
    private Long dishId;

    /**
     * 菜品名称(冗余字段)
     */
    private String name;

    /**
     * 价格(冗余字段)
     */
    @NotNull(message = "菜品数量不能为空", groups = AddOperate.class)
    @Min(value = 1, message = "菜品数量至少为1", groups = AddOperate.class)
    private BigDecimal price;

    /**
     * 份数
     */
    @NotNull(message = "菜品数量不能为空", groups = AddOperate.class)
    @Min(value = 1, message = "菜品数量至少为1", groups = AddOperate.class)
    private Integer copies;

    /**
     * 排序
     */
    private Integer sort;

    @JsonIgnore
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonIgnore
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @JsonIgnore
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @JsonIgnore
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}

