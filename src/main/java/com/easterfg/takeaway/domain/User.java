package com.easterfg.takeaway.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Data
public class User {
    /**
     * 用户ID
     */
    @TableId
    @ApiModelProperty(value = "用户ID", hidden = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    @NotNull(message = "昵称不能为空")
    @Length(min = 2, max = 16, message = "昵称长度为2-16")
    private String name;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    @NotNull(message = "手机号不能为空")
    @Length(min = 11, max = 11, message = "手机号长度不合法")
    private String phone;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    @NotNull(message = "密码不能为空")
    @Length(min = 6, max = 16, message = "密码长度为6-16")
    private String password;

    /**
     * 性别 0: 女 1:男
     */
    @ApiModelProperty(value = "性别 0: 女 1:男")
    @NotNull(message = "性别不能为空")
    @Range(min = 0, max = 1, message = "未知性别")
    private Integer gender;

    /**
     * 头像
     */
    @ApiModelProperty(value = "头像", hidden = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String avatar;

    /**
     * 状态 0: 禁用 1:启用
     */
    @ApiModelProperty(value = "状态 0: 禁用 1:启用", hidden = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer status;

    /**
     * 注册时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "注册时间", hidden = true)
    @JsonIgnore
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "修改时间", hidden = true)
    @JsonIgnore
    private LocalDateTime updateTime;
}

