package com.easterfg.takeaway.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.easterfg.takeaway.validator.group.AddOperate;
import com.easterfg.takeaway.validator.group.UpdateOperate;
import com.easterfg.takeaway.validator.constraint.HasCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Combo {

    /**
     * 套餐id
     */
    @TableId
    @NotNull(message = "更新操作套餐ID不能为空", groups = UpdateOperate.class)
    @Null(message = "新增操作套餐ID必须为空", groups = AddOperate.class)
    private Long id;

    /**
     * 套餐分类ID
     */
    @NotNull(message = "菜品分类不能为空", groups = AddOperate.class)
    @HasCategory(groups = {AddOperate.class, UpdateOperate.class})
    private Long categoryId;

    /**
     * 套餐名称
     */
    @NotNull(message = "套餐名称不能为空", groups = AddOperate.class)
    @Length(min = 2, max = 32, message = "套餐名称必须在2~32字之间", groups = {AddOperate.class, UpdateOperate.class})
    private String name;

    /**
     * 套餐价格
     */
    @NotNull(message = "套餐价格不能为空", groups = AddOperate.class)
    @Min(value = 0, message = "套餐价格不能低于0", groups = {AddOperate.class, UpdateOperate.class})
    private BigDecimal price;

    /**
     * 0:禁用 1:启用
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer status;

    /**
     * 编码
     */
    @JsonIgnore
    private String code;

    /**
     * 套餐描述
     */
    private String description;

    /**
     * 套餐图片
     */
    @NotNull(message = "图片不能为空", groups = AddOperate.class)
    private String image;

    @JsonIgnore
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @JsonIgnore
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @JsonIgnore
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    /**
     * 0: 未删除 1:已删除
     */
    @JsonIgnore
    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;

    /**
     * 套餐菜品
     */
    @TableField(exist = false)
    @NotNull(message = "套餐菜品不能为空", groups = AddOperate.class)
    @Size(min = 1, message = "套餐菜品数量至少为1", groups = {AddOperate.class, UpdateOperate.class})
    @Valid
    private List<ComboDish> comboDishes;
}

