package com.easterfg.takeaway.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.easterfg.takeaway.validator.group.AddOperate;
import com.easterfg.takeaway.validator.group.UpdateOperate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.LocalDateTime;

@Data
public class DishFlavor {

    /**
     * id, 更新操作通过业务逻辑操作
     */
    @TableId(type = IdType.AUTO)
    @Null(message = "新增操作ID必须为空", groups = AddOperate.class)
    private Long id;

    /**
     * 菜品ID
     */
    @JsonIgnore
    private Long dishId;

    /**
     * 口味名称
     */
    @NotNull(message = "口味名称不能为空", groups = {AddOperate.class, UpdateOperate.class})
    private String name;

    /**
     * 口味数据
     */
    @NotNull(message = "口味数据不能为空",groups = {AddOperate.class, UpdateOperate.class})
    private String value;

    /**
     * 创建时间
     */
    @JsonIgnore
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @JsonIgnore
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
     * 0:未删除 1:删除
     */
    @JsonIgnore
    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;
}

