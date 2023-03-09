package com.easterfg.takeaway.validator.constraint;

import com.easterfg.takeaway.validator.HasCategoryConstraintValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author EasterFG on 2022/9/28
 * <p>
 * 校验分类是否存在, 如果为空
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HasCategoryConstraintValidator.class)
public @interface HasCategory {

    /**
     * 是否必须存在
     */
    boolean value() default false;

    String message() default "菜品/套餐分类不存在";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
