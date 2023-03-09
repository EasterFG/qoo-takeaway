package com.easterfg.takeaway.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author EasterFG on 2022/9/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageQuery {

    @ApiModelProperty("页数")
    @NotNull
    @Min(value = 1, message = "页数不能小于1")
    private int page;

    @ApiModelProperty("分页大小")
    @NotNull
    @Range(min = 5, max = 40, message = "分页大小必须在5~40之间")
    private int pageSize;
}
