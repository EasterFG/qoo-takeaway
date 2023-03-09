package com.easterfg.takeaway.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.easterfg.takeaway.validator.constraint.HasCategory;
import com.easterfg.takeaway.validator.group.AddOperate;
import com.easterfg.takeaway.validator.group.UpdateOperate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Dish {

    @TableId
    @NotNull(message = "更新操作菜品ID不能为空", groups = UpdateOperate.class)
    @Null(message = "新增操作菜品ID必须为空", groups = AddOperate.class)
    private Long id;

    /**
     * 菜品名称
     */
    @NotBlank(message = "菜品名称不能为空", groups = AddOperate.class)
    private String name;

    /**
     * 菜品分类
     */
    @NotNull(message = "菜品分类不能为空", groups = AddOperate.class)
    @HasCategory(groups = {AddOperate.class, UpdateOperate.class})
    private Long categoryId;

    /**
     * 菜品价格
     */
    @NotNull(message = "菜品价格不能为空", groups = AddOperate.class)
    @Min(value = 0, message = "价格不能为负数", groups = {AddOperate.class, UpdateOperate.class})
    private BigDecimal price;

    /**
     * 商品码
     */
    @JsonIgnore
    private String code;

    /**
     * 图片
     */
    @NotBlank(message = "图片路径不能为空", groups = AddOperate.class)
    private String image;

    /**
     * 菜品描述
     */
    private String description;

    /**
     * 0:停售 1:起售
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    @JsonIgnore
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @JsonIgnore
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    /**
     * 修改人
     */
    @JsonIgnore
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    /**
     * 0:已删除 1:未删除
     */
    @JsonIgnore
    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;

    /**
     * 忽略字段
     * 需要校验数据
     */
    @Valid
    @TableField(exist = false)
    private List<DishFlavor> flavors;
}

