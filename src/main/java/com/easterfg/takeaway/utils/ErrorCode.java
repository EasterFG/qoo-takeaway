package com.easterfg.takeaway.utils;

/**
 * @author EasterFG on 2022/9/21
 */
public enum ErrorCode {
    SUCCESS("00000", "成功"),
    UNKNOWN("A0002", "未知异常, 请稍后再试"),
    USER_BASE_ERROR("A0001", "用户端错误"),

    // 返回 401
//    USER_NOT_LOGIN("A0100", "用户未登录"),
    USERNAME_ALREADY_EXISTS("A0101", "用户名已存在"),

    PHONE_ALREADY_EXISTS("A0102", "手机号已存在"),

    ID_CARD_ALREADY_EXISTS("A0103", "身份证已存在"),

    USER_NOT_EXISTS("A0201", "用户不存在"),

    USER_IS_DISABLE("A0202", "用户已被禁用"),

    USER_OR_PASSWORD_FAILED("A0203", "用户名或密码错误"),

    USER_PASSWORD_ERROR("A0210", "密码校验失败"),

    // 授权异常
    AUTHORIZATION_EXPIRED("A0311", "授权过期"),

    AUTHORIZATION_SIGNATURE_EXCEPTION("A0312", "授权签名异常"),

    NOT_LOGGED_IN("A0313", "用户未登录"),
    NO_PERMISSION("A0314", "无访问权"),


    // 用户请求参数错误
    USER_REQUEST_PARAMETER_ERROR("A0400", "用户请求参数错误"),

    //
    CATEGORY_NOT_EXISTS("A0501", "分类不存在"),

    // dish not
    DISH_NOT_EXISTS("A0511", "菜品不存在"),

    // cobo
    COMBO_NOT_EXISTS("A0521", "套餐不存在"),

    DELETE_FAILED("A0601", "删除失败"),

    FILE_UPLOAD_FAILED("A1001", "文件上传失败"),

    // 非法文件
    ILLEGAL_FILE("A1002", "非法文件"),

    FILE_NOT_FOUND("A1003", "文件不存在"),

    FILE_SIZE_OVERRUN("A1004", "文件大小超过限制");

    private final String code;
    private final String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
