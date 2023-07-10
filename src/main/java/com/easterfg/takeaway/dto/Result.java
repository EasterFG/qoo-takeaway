package com.easterfg.takeaway.dto;

import com.easterfg.takeaway.utils.ErrorCode;
// import io.swagger.annotations.ApiModel;
// import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author EasterFG on 2022/9/19
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
// @ApiModel("通用响应数据")
public class Result implements Serializable {

    private static final long serialVersionUID = 1572429473955502146L;

    /**
     * 返回码
     */
    // // @ApiModelProperty(value = "状态码", example = "A1000")
    private String code;

    /**
     * 操作消息
     */
    // // @ApiModelProperty(value = "返回消息", example = "成功")
    private String message;

    /**
     * 返回数据
     */
    // // @ApiModelProperty(value = "响应数据")
    private Object data;

    public static Result success(String message, Object data) {
        return new Result(ErrorCode.SUCCESS.getCode(), message, data);
    }

    public static Result success(String message) {
        return success(message, null);
    }

    public static Result success(Object data) {
        return success(null, data);
    }

    public static Result success() {
        return success("success", null);
    }

    public static Result failed(String code, String message, Object data) {
        return new Result(code, message, data);
    }

    public static Result failed(String code, String message) {
        return failed(code, message, null);
    }

    public static Result failed(String message) {
        return failed("40004", message);
    }

    public static Result failed(ErrorCode code) {
        return failed(code.getCode(), code.getDescription(), null);
    }

    public static Result failed(ErrorCode code, Object data) {
        return failed(code.getCode(), code.getDescription(), data);
    }

}
