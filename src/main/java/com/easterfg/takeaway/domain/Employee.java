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
 * @author EasterFG on 2022/9/19
 */
@Data
public class Employee {

    @TableId
    @NotNull(message = "更新操作id不能为空", groups = UpdateOperate.class)
    @Null(message = "新增操作id必须为空", groups = AddOperate.class)
    private Long id;

    @NotBlank(message = "用户名不能为空", groups = AddOperate.class)
    @Length(min = 4, max = 16, message = "用户名长度必须为4-16位", groups = {AddOperate.class, UpdateOperate.class})
    private String username;

    @NotBlank(message = "姓名不能为空", groups = AddOperate.class)
    @Length(min = 2, max = 16, message = "姓名长度必须为2-16位", groups = {AddOperate.class, UpdateOperate.class})
    private String name;

    private String password;

    @NotBlank(message = "电话号码不能为空", groups = AddOperate.class)
    @Length(min = 11, max = 11, message = "电话号码长度必须为11位", groups = {AddOperate.class, UpdateOperate.class})
    private String phone;

    /**
     * 0:表示女, 1:表示男
     */
    @NotNull(message = "性别不能为空", groups = AddOperate.class)
    @Range(min = 0, max = 1, message = "未知性别", groups = {AddOperate.class, UpdateOperate.class})
    private Integer gender;

    private String idCard;

    /**
     * 0: 启用, 1: 禁用
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer status;

    @JsonIgnore
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 0表示未删除, 1表示已删除
     */
    @JsonIgnore
    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;
}