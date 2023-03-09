package com.easterfg.takeaway.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.easterfg.takeaway.validator.group.AddOperate;
import com.easterfg.takeaway.validator.group.UpdateOperate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Data
public class AddressBook {
    /**
     *
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键", hidden = true)
    private Long id;

    /**
     * 用户ID
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(value = "用户ID", hidden = true)
    private Long userId;

    /**
     * 收货人
     */
    @NotNull(message = "收货人不能为空", groups = AddOperate.class)
    @ApiModelProperty(value = "收货人")
    private String consignee;

    /**
     * 性别 0 女 1 男
     */
    @NotNull(message = "性别不能为空", groups = AddOperate.class)
    @ApiModelProperty(value = "性别 0 女 1 男")
    private Integer gender;

    /**
     * 手机号
     */
    @NotNull(message = "手机号不能为空", groups = AddOperate.class)
    @Length(max = 11, min = 11, message = "手机号长度不合法", groups = {AddOperate.class, UpdateOperate.class})
    @ApiModelProperty(value = "手机号")
    private String phone;

    /**
     * 行政区划代码
     */
    @NotNull(message = "行政区划代码不能为空")
    @ApiModelProperty(value = "行政区划代码")
    private Integer code;

    /**
     * 行政区名称
     */
    @NotNull(message = "地址不能为空")
    @ApiModelProperty(value = "行政区名称")
    private String city;

    /**
     * 详细地址
     */
    @NotNull(message = "详细地址不能为空", groups = AddOperate.class)
    @ApiModelProperty(value = "详细地址")
    private String detail;

    /**
     * 标签
     */
    @ApiModelProperty(value = "标签")
    private String label;

    /**
     * 是否默认 0 否 1 是
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @TableField("is_defaults")
    @ApiModelProperty(value = "是否默认 0 否 1 是")
    private Integer defaults;

    /**
     *
     */
    @JsonIgnore
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间", hidden = true)
    private LocalDateTime createTime;

    /**
     *
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "修改时间", hidden = true)
    private LocalDateTime updateTime;

    /**
     *
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonIgnore
    @ApiModelProperty(value = "创建用ID", hidden = true)
    private Long createUser;

    /**
     *
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonIgnore
    @ApiModelProperty(value = "更新用户ID", hidden = true)
    private Long updateUser;

    /**
     *
     */
    @TableLogic
    @JsonIgnore
    @ApiModelProperty(value = "是否删除", hidden = true)
    private Integer isDeleted;
}

