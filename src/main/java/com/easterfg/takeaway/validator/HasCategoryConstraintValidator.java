package com.easterfg.takeaway.validator;

import com.easterfg.takeaway.dao.CategoryDAO;
import com.easterfg.takeaway.validator.constraint.HasCategory;

import javax.annotation.Resource;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author EasterFG on 2022/9/28
 */
public class HasCategoryConstraintValidator implements ConstraintValidator<HasCategory, Long> {

    @Resource
    private CategoryDAO categoryDAO;

    private boolean require;

    @Override
    public void initialize(HasCategory hasCategory) {
        require = hasCategory.value();
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        if (value == null) return !require;
        return categoryDAO.count(value) > 0;
    }
}
