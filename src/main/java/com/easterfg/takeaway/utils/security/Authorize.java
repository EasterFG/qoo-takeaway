package com.easterfg.takeaway.utils.security;

import java.lang.annotation.*;

/**
 * @author EasterFG on 2022/10/22
 * <p>
 * 权限验证注解, 默认允许全部权限
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Authorize {

    /**
     * 用户角色
     */
    Role[] value() default Role.ALL;
}
