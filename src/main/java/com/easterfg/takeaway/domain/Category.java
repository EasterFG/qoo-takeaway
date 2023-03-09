package com.easterfg.takeaway.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.easterfg.takeaway.validator.group.AddOperate;
import com.easterfg.takeaway.validator.group.UpdateOperate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.LocalDateTime;

/**
 * @author EasterFG on 2022/9/27
 */
@Data
public class Category {

    @NotNull(message = "更新操作分类ID不能为空", groups = UpdateOperate.class)
    @Null(message = "新增操作分类ID必须为空", groups = {AddOperate.class, UpdateOperate.class})
    @TableId
    private Long id;

    /**
     * 1: 菜品分类 2:套餐分类
     */
    @NotNull(message = "类型不能为空", groups = AddOperate.class)
    @Range(min = 1, max = 2, message = "类型不存在", groups = {AddOperate.class, UpdateOperate.class})
    private Integer type;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空", groups = AddOperate.class)
    @Length(min = 1, max = 64, message = "分类名称长度必须在1-64之间", groups = {AddOperate.class, UpdateOperate.class})
    private String name;

    /**
     * 排序
     */
    @NotBlank(message = "分类名称不能为空", groups = AddOperate.class)
    @Length(min = 1, max = 64, message = "分类名称长度必须在1-64之间", groups = {AddOperate.class, UpdateOperate.class})
    private Integer sort;

    /**
     * 状态,0:启用 1:禁用
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer status;

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
     * 0表示未删除, 1表示已删除
     */
    @JsonIgnore
    @TableField("is_deleted")
    @TableLogic
    private Integer deleted;
}